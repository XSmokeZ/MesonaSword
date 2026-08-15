package me.mesona.mesona_sword.register;

import me.mesona.mesona_sword.MesonaSword;
import me.mesona.mesona_sword.network.SweepEffectBatchPacket;
import me.mesona.mesona_sword.util.ListHelper;
import me.mesona.mesona_sword.util.TextUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.entity.PartEntity;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GrassSwordItem extends SwordItem {

    /**
     * 剑的攻击模式枚举
     */
    public enum SwordMode {
        NORMAL("日常模式", ChatFormatting.GREEN),
        SWEEP("范围模式", ChatFormatting.YELLOW),
        EXECUTE("幻梦异曲", ChatFormatting.LIGHT_PURPLE);

        private final String name;
        private final ChatFormatting color;

        // 缓存数组，避免每次 next() 都重新拷贝
        private static final SwordMode[] VALUES = values();
        private static final Component[] DESCRIPTION = new Component[]{
                Component.empty(),
                Component.literal("日常模式").withStyle(ChatFormatting.GREEN).append(Component.literal(" 在永恒的世间里，这是最纯粹的诗篇").withStyle(ChatFormatting.WHITE)),
                Component.literal("范围模式").withStyle(ChatFormatting.YELLOW).append(Component.literal(" 然而现实的裂隙毁坏了一切").withStyle(ChatFormatting.WHITE)),
                Component.literal("幻梦异曲").withStyle(ChatFormatting.LIGHT_PURPLE).append(Component.literal(" 但愿世间一切安享美梦").withStyle(ChatFormatting.WHITE))
        };

        SwordMode(String name, ChatFormatting color) {
            this.name = name;
            this.color = color;
        }

        public String getName() {
            return name;
        }

        public ChatFormatting getColor() {
            return color;
        }

        public SwordMode next() {
            return VALUES[(ordinal() + 1) % values().length];
        }
    }

    // 属性
    private static final ResourceLocation REACH_MODIFIER = ResourceLocation.fromNamespaceAndPath(MesonaSword.MODID, "execute_reach");

    private static final HowToHurt[] methodToHurt = new HowToHurt[]{
            ((source, victim) -> victim.hurt(source, 1145141)),      // 大数字
            ((source, victim) -> hurt(victim, source, Float.POSITIVE_INFINITY)), // 重写伤害逻辑
            ((source, victim) -> {victim.setHealth(0);die(victim, source);}),    // 尝试改血
            ((source, victim) -> killEntity(victim, source.getEntity()))        // 冲冲冲
    };

    // 注册器
    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(MesonaSword.MODID);
    public static final DeferredItem<GrassSwordItem> MESONA_SWORD = ITEMS.register("mesona_sword", GrassSwordItem::new);

    /**
     * 构造器
     */
    public GrassSwordItem() {
        super(
                ModTier.MESONA,
                new Properties()
                        .attributes(createAttributes(
                                ModTier.MESONA,
                                Float.MAX_VALUE,
                                0
                        ))
                        .rarity(Rarity.EPIC)
                        .stacksTo(1)
                        .fireResistant()
        );
    }

    // 攻击逻辑重写
    @Override
    public boolean onLeftClickEntity(@NotNull ItemStack stack, Player player, @NotNull Entity entity) {
        Level level = player.level();
        SwordMode mode = getMode(stack);
        if (!level.isClientSide && level instanceof ServerLevel serverLevel && entity instanceof LivingEntity victim) {
            // 分析攻击模式
            switch (mode) {
                case SWEEP -> sweepDamage(serverLevel, player, victim, stack, true);
                case EXECUTE -> {
                    var damageSource = player.damageSources().source(ModDamage.MESONA_DAMAGE, victim, player);

                    if (!victim.isDeadOrDying()) {
                        stack.hurtAndBreak(20, player, player.getEquipmentSlotForItem(stack));   // 消耗耐久
                        spawnExecuteParticles(serverLevel, victim);     // 幻梦异曲：生成樱花花瓣和落叶粒子效果
                    }

                    if (victim instanceof EnderDragon dragon) {
                        dragon.hurt(dragon.head, damageSource, Float.MAX_VALUE);
                    } else /* 尝试多种击杀方法 */
                        for (HowToHurt how : methodToHurt) {
                            if (victim.isDeadOrDying()) break;
                            how.hurt(damageSource, victim);
                        }
                }
            }
        }
        return super.onLeftClickEntity(stack, player, entity);
    }

    // 挥动逻辑重写
    @Override
    public boolean onEntitySwing(ItemStack stack, LivingEntity entity, InteractionHand hand) {
        if (entity instanceof Player player) {
            if ((stack.getItem() instanceof GrassSwordItem) && getMode(stack) == SwordMode.SWEEP) {
                sweepDamage(player.level(), player, entity, stack, false);
            }
        }
        return super.onEntitySwing(stack, entity, hand);
    }

    // 这是我瞎写的
    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity victim, LivingEntity attacker) {
        if (getMode(stack) == SwordMode.NORMAL) {
            attacker.heal(1.0F);
        }
        return true;
    }

    // 用于切换模式 / 触发标记
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        // 仅在服务端处理
        if (!level.isClientSide) {
            // Shift+右键切换模式
            if (player.isShiftKeyDown()) {
                SwordMode currentMode = getMode(stack);
                SwordMode nextMode = currentMode.next();
                setMode(stack, nextMode);

                player.displayClientMessage(
                        Component.literal("").append(stack.getDisplayName()).append(" §r§f已切换至")
                                .append(nextMode.getName()).withStyle(nextMode.getColor()), true
                );

                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.PLAYER_LEVELUP, player.getSoundSource(), 1.0F, 1.0F);
                return InteractionResultHolder.sidedSuccess(stack, false);
            }

            if (getMode(stack) == SwordMode.SWEEP) {
                BellMarkAttachment.cleanupIndex(level, (ServerPlayer) player);
                LivingEntity[] markedTargets = findMarkedTargets(player, level);
                if (markedTargets.length > 0) {
                    for (LivingEntity markedTarget : markedTargets) {
                        BellMarkAttachment.removeMark(markedTarget, (ServerPlayer) player);
                        sweepDamage(level, player, markedTarget, stack, false);
                    }
                    return InteractionResultHolder.sidedSuccess(stack, false);
                }
            }
        }
        return InteractionResultHolder.pass(stack);
    }

    // 这里用来动态修改玩家触及距离
    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);
        if (!level.isClientSide && entity instanceof LivingEntity living) {
            SwordMode mode = getMode(stack);
            var attr = living.getAttribute(Attributes.ENTITY_INTERACTION_RANGE);
            if (attr != null) {
                boolean hasModifier = attr.getModifier(REACH_MODIFIER) != null;
                if (isSelected && mode == SwordMode.EXECUTE) {
                    if (!hasModifier) {
                        attr.addTransientModifier(new AttributeModifier(
                                REACH_MODIFIER, 2, AttributeModifier.Operation.ADD_VALUE
                        ));
                        if (living instanceof Player player) {
                            player.clearFire();
                            player.deathTime = 0;
                        }
                    }
                } else if (hasModifier) {
                    attr.removeModifier(REACH_MODIFIER);
                }
            }
        }
    }

    // 添加描述文本
    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context,
                                List<Component> tooltipComponents, TooltipFlag flag) {
        SwordMode currentMode = getMode(stack);
        if(flag.hasShiftDown()) {
            tooltipComponents.add(Component.literal(TextUtil.makeFabulous(I18n.get("tooltip.mesona_sword.has_shift_down"))));
            Collections.addAll(tooltipComponents, SwordMode.DESCRIPTION);
        } else {
            tooltipComponents.add(Component.literal(I18n.get("tooltip.mesona_sword.desc")).append(Component.literal(currentMode.getName()).withStyle(currentMode.getColor())));
            tooltipComponents.add(Component.literal(I18n.get("tooltip.mesona_sword.display_shift")));
        }
    }

    /**
     * 横扫伤害
     * 
     * @param level         世界
     * @param sourceEntity  玩家
     * @param centerEntity  中心实体
     */
    public static void sweepDamage(Level level, LivingEntity sourceEntity, Entity centerEntity, ItemStack stack, boolean markTargets) {
        if (sourceEntity instanceof Player player && !level.isClientSide) {

            double sweepRange = 8.0;        // 攻击半径

            // 设置中心位置
            double centerX = centerEntity.getX();
            double centerY = centerEntity.getY() + centerEntity.getEyeHeight();
            double centerZ = centerEntity.getZ();

            // 构建范围
            AABB sweepArea = new AABB(
                    centerX - sweepRange, centerY - sweepRange, centerZ - sweepRange,
                    centerX + sweepRange, centerY + sweepRange, centerZ + sweepRange
            );

            int damageCount = 0;
            boolean flag = false;
            List<SweepEffectBatchPacket.EffectData> effectList = new ArrayList<>();

            for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, sweepArea)) {

                if (player.isAlliedTo(target) ||
                    player.distanceToSqr(target) > sweepRange * sweepRange ||
                    target == player ||
                    target instanceof ArmorStand armorStand && armorStand.isMarker() ||     // 防止友伤
                    target.isDeadOrDying()                                                  // 距离检查
                    ) continue;

                target.hurt(player.damageSources().fellOutOfWorld(), 200.0F);      // 造成伤害
                flag = true;

                target.knockback(0.6F,
                    Mth.sin(player.getYRot() * ((float) Math.PI / 180F)),
                    -Mth.cos(player.getYRot() * ((float) Math.PI / 180F)));           // 击退

                // 统计要消耗的耐久
                if(damageCount < 20)
                    damageCount++;

                // 是否给目标挂上 bell 标记
                if(markTargets) {
                    BellMarkAttachment.setMark(target, (ServerPlayer) player);
                }

                /* 以下是粒子逻辑 */
                /* Start */

                // 收集数据
                float randomYaw = player.level().getRandom().nextFloat() * 360f;
                float scale = (float) ((target.getBbWidth() + target.getBbHeight()));
                effectList.add(new SweepEffectBatchPacket.EffectData(
                    target.getX(), target.getY(), target.getZ(), randomYaw, scale));
            }
            // 发送横扫粒子数据
            if (!effectList.isEmpty()) {
                ((ServerPlayer) player).connection.send(
                    new SweepEffectBatchPacket(effectList));
            }
            /* End */

            stack.hurtAndBreak(damageCount, player, player.getEquipmentSlotForItem(stack));          // 消耗耐久

            if (flag) level.playSound(null, player.getX(), player.getY(), player.getZ(),    // 播放音效
                    SoundEvents.WARDEN_DEATH, player.getSoundSource(), 1.0F, 1.0F);
        }
    }
    
    /**
     * 幻梦模式粒子效果
     * 在生物死亡位置生成樱花花瓣
     */
    private static void spawnExecuteParticles(ServerLevel level, LivingEntity victim) {
        double x = victim.getX();
        double y = victim.getY() + victim.getBbHeight() * 0.5;
        double z = victim.getZ();

        // 樱花花瓣 - 粉色飘落效果
        level.sendParticles(
            ParticleTypes.CHERRY_LEAVES,
            x, y, z,
            50,  // 数量
            victim.getBbWidth() * 0.5,  // X偏移范围
            victim.getBbHeight() * 0.5, // Y偏移范围
            victim.getBbWidth() * 0.5,  // Z偏移范围
            0.05  // 速度
        );

        level.sendParticles(
            ParticleTypes.FALLING_WATER,
            x, y, z,
            30,  // 数量
            victim.getBbWidth() * 0.3,
            victim.getBbHeight() * 0.3,
            victim.getBbWidth() * 0.3,
            0.03
        );

        // 额外的白色花瓣效果
        level.sendParticles(
            ParticleTypes.EFFECT,  // 药水粒子，白色星点
            x, y + 0.5, z,
            20,
            victim.getBbWidth() * 0.4,
            victim.getBbHeight() * 0.4,
            victim.getBbWidth() * 0.4,
            0.02
        );
    }

    /**
     * 伤害函数
     *
     * @param victim  受害者
     * @param pSource 伤害源
     * @param pAmount 伤害量
     */
    private static void hurt(LivingEntity victim, DamageSource pSource, float pAmount) {

        // 不在客户端处理，同时不要鞭尸
        if (victim.level().isClientSide || victim.isDeadOrDying()) return;

        // 多组件生物的特殊处理方法
        if (victim.isMultipartEntity()) {
            for (Entity part : victim.getParts()) {
                if (part instanceof PartEntity<?> partEntity && partEntity.getParent() == victim) {
                    part.hurt(pSource, pAmount);
                }
            }
        }

        // 别睡了，汝死期将至矣！
        if (victim.isSleeping() && !victim.level().isClientSide) {
            victim.stopSleeping();
        }

        // 这里是关于生物受伤的逻辑
        victim.setNoActionTime(0);                                  // 清除生物闲置时间
        victim.walkAnimation.setSpeed(1.5F);                        // 设置动画速度为1.5倍
        victim.lastHurt = pAmount;                                  // 记录伤害值
        victim.invulnerableTime = 20;                               // 模拟原版无敌帧
        victim.getCombatTracker().recordDamage(pSource, pAmount);   // 战斗追踪器中记录这次伤害，用于死亡消息显示
        victim.setHealth(victim.getHealth() - pAmount);             // 通过改血应用伤害
        victim.gameEvent(GameEvent.ENTITY_DAMAGE);                  // 触发游戏事件
        victim.hurtDuration = 10;                                   // 受伤动画时间
        victim.hurtTime = victim.hurtDuration;                      // 设置hurtTime最大值
        victim.lastDamageSource = pSource;                          // 谁造成的伤害
        victim.lastDamageStamp = victim.level().getGameTime();      // 上一次受伤时间


        // 获取攻击源
        Entity sourceEntity = pSource.getEntity();

        if (sourceEntity != null) {

            // 设置仇恨Mob（如果有
            if (sourceEntity instanceof LivingEntity livingSource && !pSource.is(DamageTypeTags.NO_ANGER)) {
                    victim.setLastHurtByMob(livingSource);
            }

            // 设置仇恨Player
            if (sourceEntity instanceof Player player) {
                victim.lastHurtByPlayerTime = 100;
                victim.setLastHurtByPlayer(player);
            } else if (sourceEntity instanceof TamableAnimal tamableEntity) {
                if (tamableEntity.isTame()) {
                    victim.lastHurtByPlayerTime = 100;
                    LivingEntity owner = tamableEntity.getOwner();
                    if (owner instanceof Player player) {
                        victim.setLastHurtByPlayer(player);
                    } else {
                        victim.setLastHurtByPlayer(null);
                    }
                }
            }
        }

        // 广播生物受伤以及受伤动画特殊处理
        victim.level().broadcastDamageEvent(victim, pSource);
        if (!pSource.is(DamageTypeTags.NO_IMPACT)) {
            victim.hurtMarked = true;
        }

        boolean flag = false;       // 这是棍母

        // 是否击退的逻辑判断
        if (sourceEntity != null && !pSource.is(DamageTypeTags.IS_EXPLOSION)) {
            double d0 = sourceEntity.getX() - victim.getX();

            double d1;
            for(d1 = sourceEntity.getZ() - victim.getZ(); d0 * d0 + d1 * d1 < 1.0E-4D; d1 = (Math.random() - Math.random()) * 0.01D) {
                d0 = (Math.random() - Math.random()) * 0.01D;
            }

            victim.knockback(0.4F, d0, d1);
            if (!flag) {
                victim.indicateDamage(d0, d1);
            }
        }

        // 因受伤而死亡的逻辑
        if (victim.isDeadOrDying()) {
            die(victim, pSource);
        } else {
            SoundEvent soundevent = SoundEvents.GENERIC_HURT;
            victim.playSound(soundevent, 2F, victim.getVoicePitch());
        }


        // 触发成就系统
        if (victim instanceof ServerPlayer) {
            CriteriaTriggers.ENTITY_HURT_PLAYER.trigger((ServerPlayer)victim, pSource, pAmount, pAmount, flag);
        }

        if (sourceEntity instanceof ServerPlayer) {
            CriteriaTriggers.PLAYER_HURT_ENTITY.trigger((ServerPlayer)sourceEntity, victim, pSource, pAmount, pAmount, flag);
        }
    }

    /**
     * 死亡修正函数
     *
     * @param victim        受害者
     * @param source        伤害量
     */
    private static void die(LivingEntity victim, DamageSource source) {
        if (!victim.isRemoved() && !victim.dead) {
            Entity entity = source.getEntity();
            LivingEntity livingentity = victim.getKillCredit();
            if (victim.deathScore >= 0 && livingentity != null) {
                livingentity.awardKillScore(victim, victim.deathScore, source);
            }

            if (victim.isSleeping()) {
                victim.stopSleeping();
            }

            victim.dead = true;
            victim.getCombatTracker().recheckStatus();
            Level level = victim.level();
            if (level instanceof ServerLevel serverlevel) {
                if (entity == null || entity.killedEntity(serverlevel, victim)) {
                    victim.gameEvent(GameEvent.ENTITY_DIE);
                    victim.dropAllDeathLoot(serverlevel, source);
                }

                victim.level().broadcastEntityEvent(victim, (byte) 3);
            }

            victim.setPose(Pose.DYING);
        }
    }

    /**
     * 奇妙击杀函数
     *
     * @param victim        受害者
     * @param sourceEntity  攻击源
     */
    public static void killEntity(LivingEntity victim, Entity sourceEntity) {
        if (sourceEntity instanceof Player player) {
            victim.setRemainingFireTicks(1000);
            if (player != null) {
                victim.hurt(player.damageSources().playerAttack(player), Float.POSITIVE_INFINITY);
                if (!(victim instanceof Player))
                    victim.die(player.damageSources().playerAttack(player));
                victim.setLastHurtByPlayer(player);
            }
            victim.setHealth(0);
            if (!(victim instanceof Player))
                ListHelper.addEntityToList(victim);
        }
    }

    /**
     * 移除实体函数
     *
     * @param living 要移除的实体
     */
    private void removeEntity(LivingEntity living) {
        living.setPos(-9999,-9999,-9999);
        if (!(living instanceof Player)) {
            ListHelper.addEntityToRemoveList(living);
            living.discard();
            living.setInvisible(true);
            living.removeVehicle();
            living.remove(Entity.RemovalReason.DISCARDED);
            living.onRemovedFromLevel();
            living.setRemoved(Entity.RemovalReason.DISCARDED);
        }
        living.setHealth(0);
    }

    /**
     * 查找指定玩家标记的所有已加载实体
     *
     * @param player 玩家
     * @param level  世界
     * @return       被标记的实体数组
     */
    private static LivingEntity[] findMarkedTargets(Player player, Level level) {
        List<LivingEntity> markedEntities = BellMarkAttachment.getMarkedEntities(level, player.getUUID());
        // 过滤掉玩家自身（理论上不会被标记自己，但保险起见）
        return markedEntities.stream()
                .filter(e -> e != player)
                .toArray(LivingEntity[]::new);
    }

    /**
     * 获取攻击模式组件
     *
     * @param stack 这把剑的物品
     * @return      攻击模式
     */
    public static SwordMode getMode(ItemStack stack) {
        Integer modeIndex = stack.get(ModDataComponent.SWORD_MODE.get());
        if(modeIndex != null && modeIndex >= 0 && modeIndex < SwordMode.VALUES.length) {
            return SwordMode.values()[modeIndex];
        }
        return SwordMode.NORMAL;
    }

    /**
     * 设置攻击模式组件
     *
     * @param stack 这把剑的物品
     * @param mode  设置的攻击模式
     */
    private void setMode(ItemStack stack, SwordMode mode) {
        stack.set(ModDataComponent.SWORD_MODE.get(), mode.ordinal());
    }

    @FunctionalInterface
    private interface HowToHurt {
        void hurt(DamageSource source, LivingEntity victim);
    }
}