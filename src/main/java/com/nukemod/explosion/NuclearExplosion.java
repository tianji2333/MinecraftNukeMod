package com.nukemod.explosion;

import com.nukemod.NukeMod;
import com.nukemod.block.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Heightmap;

import java.util.*;

/**
 * 自定义核弹爆炸系统 v2
 * - 粗网格 + 补洞算法（更快更平滑）
 * - 碗状弹坑（上轻下重偏斜）
 * - 核辐射扩散 + 冲击波 + 末日火海
 */
public class NuclearExplosion {

    private final ServerWorld world;
    private final double x, y, z;
    private final float power;
    private final Random random;

    private final Map<BlockPos, BlockState> destroyedBlocks = new LinkedHashMap<>();
    private final Set<BlockPos> toDestroy = new HashSet<>();

    // ─── 配置参数 ───
    private static final int   FILL_THRESHOLD    = 3;      // 补洞阈值（≥N个被毁邻居即补）
    private static final int   MAX_DEBRIS        = 200;
    private static final int   FIRE_COUNT        = 3000;
    private static final float ENTITY_DAMAGE_MAX = 300.0F;
    private static final float ENTITY_LAUNCH     = 10.0F;
    private static final int   LAUNCH_RADIUS_MUL = 2;

    public NuclearExplosion(ServerWorld world, double x, double y, double z, float power) {
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.power = power;
        this.random = world.random;
    }

    public void explode() {
        long t = System.currentTimeMillis();

        spawnFlash();
        destroyBlocks();       // 粗网格 + 补洞
        fillGaps();            // 二次补漏
        ejectDebris();
        spreadFire();
        spreadRadiation();     // ✨ 新：核辐射扩散
        spawnMushroomCloud();
        spawnShockwaveRings();
        doEntityDamage();
        playSounds();

        t = System.currentTimeMillis() - t;
        NukeMod.LOGGER.info("核爆完成: 摧毁{}个方块, 耗时{}ms", destroyedBlocks.size(), t);
    }

    // ═══════════════════════════════════════
    // 高空白光
    // ═══════════════════════════════════════
    private void spawnFlash() {
        for (int i = 0; i < 6; i++) {
            world.spawnParticles(ParticleTypes.FLASH,
                    x, y + 10 + i * 8, z,
                    2, 20 + i * 5, 2, 20 + i * 5, 0);
        }
    }

    // ═══════════════════════════════════════
    // 破块：全分辨率逐块检测 + 补洞
    // 步长1无网格痕迹，补洞保平滑
    // ═══════════════════════════════════════
    private void destroyBlocks() {
        int r = (int) Math.ceil(power);
        BlockPos center = BlockPos.ofFloored(x, y, z);

        for (int dx = -r; dx <= r; dx++) {
            for (int dy = -r; dy <= r; dy++) {
                for (int dz = -r; dz <= r; dz++) {
                    BlockPos pos = center.add(dx, dy, dz);
                    if (toDestroy.contains(pos)) continue;
                    if (!world.isChunkLoaded(pos)) continue;

                    double dist = Math.sqrt(dx*dx + dy*dy + dz*dz);
                    if (dist > r || dist < 1.5) continue;

                    BlockState state = world.getBlockState(pos);
                    if (state.isAir()) continue;
                    if (state.getBlock() == Blocks.BEDROCK) continue;

                    // ── 破坏概率计算 ──
                    double chance = 1.0 - dist*dist / (r*r) * 0.88;

                    // 碗状偏斜：上轻下重
                    double height = pos.getY() - y;
                    if (height > 0) {
                        chance *= Math.max(0.1, 1.0 - height / (r * 0.35));
                    } else {
                        chance *= Math.min(1.5, 1.0 - height / (r * 0.55));
                    }

                    // 抗爆性抵抗
                    float resistance = state.getBlock().getBlastResistance();
                    if (resistance > 0) {
                        chance *= (1.0 - Math.min(1.0, resistance / 1200.0) * 0.45);
                    }

                    chance = MathHelper.clamp(chance, 0.0, 1.0);

                    if (random.nextFloat() < chance) {
                        toDestroy.add(pos);
                    }
                }
            }
        }

        // ── 执行破坏 ──
        for (BlockPos pos : toDestroy) {
            BlockState state = world.getBlockState(pos);
            if (!state.isAir()) {
                destroyedBlocks.put(pos, state);
                world.setBlockState(pos, Blocks.AIR.getDefaultState(), 2);
            }
        }
    }

    // ═══════════════════════════════════════
    // 补洞平滑：清除残留孤立块块
    // ═══════════════════════════════════════
    private void fillGaps() {
        int r = (int) Math.ceil(power);
        BlockPos center = BlockPos.ofFloored(x, y, z);
        Set<BlockPos> fill = new HashSet<>();

        for (BlockPos p : destroyedBlocks.keySet()) {
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        if (dx == 0 && dy == 0 && dz == 0) continue;
                        BlockPos n = p.add(dx, dy, dz);
                        if (destroyedBlocks.containsKey(n) || fill.contains(n)) continue;
                        if (!world.isChunkLoaded(n)) continue;

                        double dist = Math.sqrt(n.getSquaredDistance(center));
                        if (dist > r) continue;

                        int nd = 0;
                        for (int rx = -1; rx <= 1; rx++) {
                            for (int ry = -1; ry <= 1; ry++) {
                                for (int rz = -1; rz <= 1; rz++) {
                                    if (rx == 0 && ry == 0 && rz == 0) continue;
                                    if (destroyedBlocks.containsKey(n.add(rx, ry, rz))) nd++;
                                }
                            }
                        }
                        if (nd >= FILL_THRESHOLD) {
                            fill.add(n);
                        }
                    }
                }
            }
        }

        for (BlockPos p : fill) {
            BlockState state = world.getBlockState(p);
            if (!state.isAir() && state.getBlock() != Blocks.BEDROCK) {
                destroyedBlocks.put(p, state);
                world.setBlockState(p, Blocks.AIR.getDefaultState(), 2);
            }
        }
    }

    // ═══════════════════════════════════════
    // 碎片飞溅
    // ═══════════════════════════════════════
    private void ejectDebris() {
        List<Map.Entry<BlockPos, BlockState>> list = new ArrayList<>(destroyedBlocks.entrySet());
        java.util.Random jr = new java.util.Random();
        Collections.shuffle(list, jr);

        int count = Math.min(MAX_DEBRIS, list.size());
        int spawned = 0;
        for (int i = 0; spawned < count && i < list.size(); i++) {
            Map.Entry<BlockPos, BlockState> entry = list.get(i);
            BlockPos pos = entry.getKey();
            BlockState state = entry.getValue();
            Block block = state.getBlock();

            if (state.isAir() || block == Blocks.WATER || block == Blocks.LAVA) continue;
            Item item = block.asItem();
            if (item == Items.AIR) continue;

            double dx = pos.getX() + 0.5 - x;
            double dy = pos.getY() + 0.5 - y;
            double dz = pos.getZ() + 0.5 - z;
            double dist = Math.sqrt(dx*dx + dy*dy + dz*dz);
            if (dist < 2.0) continue;

            double speed = Math.min(5.0, 15.0 / Math.max(dist, 1.0)) * (0.6 + jr.nextDouble() * 0.8);
            double vx = (dx / dist) * speed;
            double vy = (dy / dist) * speed + 0.5 + jr.nextDouble() * 0.8;
            double vz = (dz / dist) * speed;

            ItemEntity debris = new ItemEntity(world,
                    pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                    new ItemStack(item, 1));
            debris.setVelocity(vx, vy, vz);
            debris.setPickupDelay(60);
            debris.setGlowing(true);  // 发光碎片更酷
            world.spawnEntity(debris);
            spawned++;
        }
    }

    // ═══════════════════════════════════════
    // 末日火海
    // ═══════════════════════════════════════
    private void spreadFire() {
        int r = (int) (power * 1.2F);
        BlockPos c = BlockPos.ofFloored(x, y, z);

        // 第1遍：随机火焰
        for (int i = 0; i < FIRE_COUNT; i++) {
            int fx = random.nextBetween(-r, r);
            int fy = random.nextBetween(-r/3, r/3);
            int fz = random.nextBetween(-r, r);
            if (fx*fx + fy*fy + fz*fz > r*r) continue;

            BlockPos pos = c.add(fx, fy, fz);
            if (world.getBlockState(pos).isAir()
                    && world.getBlockState(pos.down()).isFullCube(world, pos.down())) {
                world.setBlockState(pos, Blocks.FIRE.getDefaultState(), 2);
            }
        }

        // 第2遍：坑沿点燃
        for (BlockPos p : destroyedBlocks.keySet()) {
            BlockPos above = p.up();
            if (world.getBlockState(above).isAir() && random.nextFloat() < 0.12f) {
                if (world.getBlockState(p).isFullCube(world, p)) {
                    world.setBlockState(above, Blocks.FIRE.getDefaultState(), 2);
                }
            }
        }

        // 第3遍：点燃植被
        for (int i = 0; i < 300; i++) {
            int fx = random.nextBetween(-r, r);
            int fy = random.nextBetween(-r, r);
            int fz = random.nextBetween(-r, r);
            BlockPos pos = c.add(fx, fy, fz);
            Block b = world.getBlockState(pos).getBlock();
            if (b == Blocks.OAK_LEAVES || b == Blocks.SPRUCE_LEAVES ||
                b == Blocks.BIRCH_LEAVES || b == Blocks.JUNGLE_LEAVES ||
                b == Blocks.ACACIA_LEAVES || b == Blocks.DARK_OAK_LEAVES ||
                b == Blocks.MANGROVE_ROOTS || b == Blocks.GRASS_BLOCK ||
                isLogBlock(b)) {
                world.setBlockState(pos, Blocks.FIRE.getDefaultState(), 2);
            }
        }

        // 第4遍：核心火球
        for (int dx = -4; dx <= 4; dx++) {
            for (int dz = -4; dz <= 4; dz++) {
                BlockPos core = c.add(dx, -1, dz);
                if (!world.getBlockState(core).isAir()) {
                    world.setBlockState(core, Blocks.FIRE.getDefaultState(), 2);
                }
            }
        }
    }

    private boolean isLogBlock(Block b) {
        return b == Blocks.OAK_LOG || b == Blocks.SPRUCE_LOG || b == Blocks.BIRCH_LOG ||
               b == Blocks.JUNGLE_LOG || b == Blocks.ACACIA_LOG || b == Blocks.DARK_OAK_LOG ||
               b == Blocks.MANGROVE_LOG || b == Blocks.CHERRY_LOG;
    }

    // ═══════════════════════════════════════
    // ✨ 新：核辐射扩散（地表变成辐射泥土）
    // ═══════════════════════════════════════
    private void spreadRadiation() {
        int r = (int) (power * 0.35F);
        BlockPos c = BlockPos.ofFloored(x, y, z);
        BlockState irradiatedDirt = ModBlocks.IRRADIATED_DIRT.getDefaultState();

        for (int i = 0; i < 200; i++) {
            int fx = random.nextBetween(-r, r);
            int fz = random.nextBetween(-r, r);
            if (fx*fx + fz*fz > r*r) continue;

            // 找到地表最高方块
            BlockPos top = world.getTopPosition(Heightmap.Type.WORLD_SURFACE, c.add(fx, 0, fz));
            BlockState state = world.getBlockState(top);

            if (state.getBlock() == Blocks.GRASS_BLOCK ||
                state.getBlock() == Blocks.DIRT ||
                state.getBlock() == Blocks.PODZOL ||
                state.getBlock() == Blocks.STONE ||
                state.getBlock() == Blocks.SAND ||
                state.getBlock() == Blocks.GRAVEL) {
                world.setBlockState(top, irradiatedDirt, 2);
            }
        }

        // 在距离边缘 2-3 格处画一个"污染环"
        for (int i = 0; i < 100; i++) {
            double angle = random.nextDouble() * 2 * Math.PI;
            double dist = r * (0.7 + random.nextDouble() * 0.3);
            int fx = (int) (Math.cos(angle) * dist);
            int fz = (int) (Math.sin(angle) * dist);
            BlockPos top = world.getTopPosition(Heightmap.Type.WORLD_SURFACE, c.add(fx, 0, fz));
            BlockState state = world.getBlockState(top);
            if (state.getBlock() == Blocks.GRASS_BLOCK || state.getBlock() == Blocks.DIRT) {
                world.setBlockState(top, irradiatedDirt, 2);
            }
        }

        // 在弹坑内随机撒一些辐射土
        for (BlockPos p : destroyedBlocks.keySet()) {
            if (random.nextFloat() < 0.08f) {
                BlockPos below = p.down();
                BlockState bs = world.getBlockState(below);
                if (bs.getBlock() == Blocks.STONE || bs.getBlock() == Blocks.DEEPSLATE ||
                    bs.getBlock() == Blocks.GRAVEL) {
                    world.setBlockState(p, irradiatedDirt, 2);
                }
            }
        }
    }

    // ═══════════════════════════════════════
    // 蘑菇云
    // ═══════════════════════════════════════
    private void spawnMushroomCloud() {
        int ch = Math.max(30, (int)(power * 1.0F));

        // 茎
        world.spawnParticles(ParticleTypes.LARGE_SMOKE, x, y+1, z, 400, 3.0, 0.5, 3.0, 0.2);
        world.spawnParticles(ParticleTypes.SMOKE, x, y+1, z, 300, 4.0, 0.3, 4.0, 0.15);
        world.spawnParticles(ParticleTypes.LARGE_SMOKE, x, y+ch/2, z, 200, 5.0, 2.0, 5.0, 0.1);

        // 冠
        world.spawnParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE,
                x, y+ch, z, 180, power*0.5, ch*0.1, power*0.5, 0.03);
        world.spawnParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE,
                x, y+ch*1.3, z, 120, power*0.7, ch*0.06, power*0.7, 0.02);
        world.spawnParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE,
                x, y+ch*1.5, z, 80, power*0.9, ch*0.04, power*0.9, 0.01);

        // 炽热内核
        world.spawnParticles(ParticleTypes.LAVA, x, y+ch*0.8, z, 80, power*0.25, ch*0.08, power*0.25, 0.05);
        world.spawnParticles(ParticleTypes.FLAME, x, y+ch, z, 60, power*0.35, ch*0.05, power*0.35, 0.02);

        // 底部炸裂
        world.spawnParticles(ParticleTypes.EXPLOSION_EMITTER, x, y+3, z, 1, 0, 0, 0, 0);
    }

    // ═══════════════════════════════════════
    // 冲击波环
    // ═══════════════════════════════════════
    private void spawnShockwaveRings() {
        float maxRing = power * 1.2F;
        for (float ring = 2; ring <= maxRing; ring += 3.0F) {
            int count = MathHelper.clamp((int)(ring * 1.5), 20, 120);
            for (int i = 0; i < count; i++) {
                double angle = 2 * Math.PI * i / count;
                double px = x + Math.cos(angle) * ring;
                double pz = z + Math.sin(angle) * ring;

                if (ring < power * 0.3F) {
                    world.spawnParticles(ParticleTypes.FLAME, px, y+0.5, pz, 1, 0.1, 0.1, 0.1, 0.05);
                } else if (ring < power * 0.7F) {
                    world.spawnParticles(ParticleTypes.LARGE_SMOKE, px, y+0.5, pz, 1, 0.2, 0.2, 0.2, 0.08);
                } else {
                    world.spawnParticles(ParticleTypes.CLOUD, px, y+0.3, pz, 2, 0.3, 0.1, 0.3, 0.05);
                }
            }
        }
    }

    // ═══════════════════════════════════════
    // 实体伤害 + 击飞 + 辐射病
    // ═══════════════════════════════════════
    private void doEntityDamage() {
        int blastRadius = (int)(power * LAUNCH_RADIUS_MUL);
        Box box = new Box(BlockPos.ofFloored(x, y, z)).expand(blastRadius);

        world.getEntitiesByClass(LivingEntity.class, box,
                e -> e.isAlive() && !e.isSpectator())
                .forEach(e -> {
                    double dx = e.getX() - x;
                    double dy = e.getY() - y;
                    double dz = e.getZ() - z;
                    double distSq = dx*dx + dy*dy + dz*dz;
                    if (distSq > blastRadius*blastRadius) return;

                    double dist = Math.sqrt(distSq);

                    // ── 秒杀区：爆炸半径内的实体直接蒸发 ──
                    if (dist < power * 0.8) {
                        e.damage(world.getDamageSources().explosion(null), 1000.0F);
                        e.setVelocity(0, 5.0, 0);
                        e.velocityModified = true;
                        e.setOnFireFor(20);
                        e.addStatusEffect(new StatusEffectInstance(NukeMod.RADIATION, 600, 4));
                        return;
                    }

                    // ── 重伤区 ──
                    double intensity = Math.max(0, 1.0 - dist / blastRadius);
                    intensity = intensity * intensity * 0.85 + intensity * 0.15;

                    float damage = (float)(ENTITY_DAMAGE_MAX * intensity);
                    if (damage > 1) {
                        e.damage(world.getDamageSources().explosion(null), damage);
                    }

                    double launch = ENTITY_LAUNCH * intensity;
                    if (dist > 0.5) {
                        e.addVelocity((dx/dist)*launch*1.2,
                                launch*0.8 + intensity*1.5,
                                (dz/dist)*launch*1.2);
                    } else {
                        e.addVelocity(0, launch*1.5, 0);
                    }
                    e.velocityModified = true;

                    if (intensity > 0.2) e.setOnFireFor((int)(intensity * 15));

                    e.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 160, 4));
                    e.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, 80, 0));
                    e.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 120, 0));
                    e.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 240, 2));
                    e.addStatusEffect(new StatusEffectInstance(NukeMod.RADIATION, 200, intensity > 0.5 ? 2 : 1));
                    if (intensity > 0.3) {
                        e.addStatusEffect(new StatusEffectInstance(StatusEffects.WITHER, 60, 0));
                    }
                });

        world.getEntitiesByClass(ProjectileEntity.class, box, Entity::isAlive).forEach(Entity::discard);
        world.getEntitiesByClass(ItemEntity.class,
                new Box(BlockPos.ofFloored(x, y, z)).expand(power * 0.4F),
                Entity::isAlive).forEach(e -> {
            if (random.nextFloat() < 0.7f) e.discard();  // 70%掉落物被气浪摧毁
        });
    }

    // ═══════════════════════════════════════
    // 音效
    // ═══════════════════════════════════════
    private void playSounds() {
        world.playSound(null, x, y, z,
                SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.BLOCKS, 6.0F, 0.3F);
        world.playSound(null, x, y, z,
                SoundEvents.ENTITY_WITHER_SPAWN, SoundCategory.BLOCKS, 4.0F, 0.5F);
    }

    public int getDestroyedBlockCount() {
        return destroyedBlocks.size();
    }
}
