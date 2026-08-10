package me.mesona.mesona_sword;

import me.mesona.mesona_sword.utils.ModDataComponents;
import me.mesona.mesona_sword.utils.ModTier;
import me.mesona.mesona_sword.utils.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.neoforged.neoforge.entity.PartEntity;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class GrassSwordItem extends SwordItem {

    /**
     * 剑的攻击模式枚举
     */
    private enum SwordMode {
        NORMAL("普通模式", ChatFormatting.GREEN),
        SWEEP("横扫模式", ChatFormatting.YELLOW),
        EXECUTE("处决模式", ChatFormatting.DARK_RED);

        private final String name;
        private final ChatFormatting color;

        // 缓存数组，避免每次 next() 都重新拷贝
        private static final SwordMode[] VALUES = values();
        private static final Component[] DESCRIPTION = new Component[]{
                Component.empty(),
                Component.literal("普通模式").withStyle(ChatFormatting.GREEN).append(Component.literal(" 造成接近无穷的伤害").withStyle(ChatFormatting.WHITE)),
                Component.literal("横扫模式").withStyle(ChatFormatting.YELLOW).append(Component.literal(" 造成大面积的虚空伤害").withStyle(ChatFormatting.WHITE)),
                Component.literal("处决模式").withStyle(ChatFormatting.DARK_RED).append(Component.literal(" 直接调用kill命令").withStyle(ChatFormatting.WHITE))
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

    // 注册器
    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(MesonaSword.MODID);
    public static final DeferredItem<GrassSwordItem> MESONA_SWORD = ITEMS.register("mesona_sword", GrassSwordItem::new);

    public GrassSwordItem() {
        super(
                Tiers.NETHERITE,
                new Properties()
                        .attributes(createAttributes(
                                ModTier.MESONA,
                                Float.POSITIVE_INFINITY,
                                0
                        ))
                        .rarity(Rarity.EPIC)
                        .stacksTo(1)
                        .fireResistant()
                        .durability(0)
        );
    }

    // 攻击逻辑重写
    @Override
    public boolean onLeftClickEntity(@NotNull ItemStack stack, Player player, @NotNull Entity entity) {
        Level level = player.level();
        SwordMode mode = getMode(stack);
        if (mode == SwordMode.EXECUTE && !level.isClientSide && level instanceof ServerLevel serverLevel && entity instanceof LivingEntity victim) {
            var damageSource = player.damageSources().source(MesonaSword.MESONA_DAMAGE, victim, player);
            sweepAttack(serverLevel, player, victim);//横扫
            if (victim instanceof EnderDragon dragon ) {
                dragon.hurt(dragon.head, damageSource, Float.POSITIVE_INFINITY);
            } else {
                hurt(victim, damageSource, Float.POSITIVE_INFINITY);
            }

            if (!victim.isDeadOrDying()) {
                victim.setHealth(0);//设置血量为零
                this.die(victim, damageSource);//修正设置死亡
                player.killedEntity(serverLevel, victim);//添加至信息统计
                //player.getCombatTracker().recordDamage(damageSource, victim.getHealth());//添加至伤害记录
            }
        }
        return false;
    }

    // 用于切换模式
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        // 仅在服务端切换模式
        if(!level.isClientSide && player.isShiftKeyDown()) {
            SwordMode currentMode = getMode(stack);
            SwordMode nextMode = currentMode.next();
            setMode(stack, nextMode);

            // 向玩家发送提示
            player.displayClientMessage(
                    Component.literal("§a[凉粉草] §f已切换至")
                            .append(nextMode.getName()).withStyle(nextMode.getColor()), true
            );

            // 播放音效
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.NOTE_BLOCK_PLING, player.getSoundSource(), 1.0F, 1.0F);
            return InteractionResultHolder.sidedSuccess(stack, false);
        }
        return InteractionResultHolder.pass(stack);
    }

    // 无附魔光效
    @Override
    public boolean isFoil(@NotNull ItemStack stack) {
        return false;
    }

    // 不需要维修
    @Override
    public boolean isRepairable(@NotNull ItemStack stack) {
        return false;
    }

    // 添加描述文本
    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context,
                                List<Component> tooltipComponents, TooltipFlag flag) {
        SwordMode currentMode = getMode(stack);
        if(flag.hasShiftDown()) {
            tooltipComponents.add(Component.literal(TextUtils.makeFabulous(I18n.get("tooltip.mesona_sword.has_shift_down"))));
            Collections.addAll(tooltipComponents, SwordMode.DESCRIPTION);
        } else {
            tooltipComponents.add(Component.literal(I18n.get("tooltip.mesona_sword.desc")).append(Component.literal(currentMode.getName()).withStyle(currentMode.getColor())));
            tooltipComponents.add(Component.literal(I18n.get("tooltip.mesona_sword.display_shift")));
        }
    }

    /**
     * 横扫攻击
     *
     * @param level        世界
     * @param livingEntity 玩家
     * @param victim       被攻击者
     */
    private void sweepAttack(Level level, LivingEntity livingEntity, Entity victim) {
        if (livingEntity instanceof Player player) {
            for (LivingEntity livingentity : level.getEntitiesOfClass(LivingEntity.class, player.getItemInHand(InteractionHand.MAIN_HAND).getSweepHitBox(player, victim))) {
                double entityReachSq = Mth.square(player.entityInteractionRange()); // Use entity reach instead of constant 9.0. Vanilla uses bottom center-to-center checks here, so don't update this to use canReach, since it uses closest-corner checks.
                if (!player.isAlliedTo(livingentity) && (!(livingentity instanceof ArmorStand) || !((ArmorStand) livingentity).isMarker()) && player.distanceToSqr(livingentity) < entityReachSq) {
                    livingentity.knockback(0.6F, Mth.sin(player.getYRot() * ((float) Math.PI / 180F)), -Mth.cos(player.getYRot() * ((float) Math.PI / 180F)));
//                    hurt(livingentity, player.damageSources().source(MesonaSword.MESONA_DAMAGE, livingentity, player), 10);
                }
            }
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_ATTACK_SWEEP, player.getSoundSource(), 1.0F, 1.0F);
            double d0 = -Mth.sin(player.getYRot() * ((float) Math.PI / 180F));
            double d1 = Mth.cos(player.getYRot() * ((float) Math.PI / 180F));
            if (level instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.SWEEP_ATTACK, player.getX() + d0, player.getY(0.5D), player.getZ() + d1, 0, d0, 0.0D, d1, 0.0D);
            }
        }
    }

    /**
     * 伤害函数
     *
     * @param victim  受害者
     * @param pSource 伤害源
     * @param pAmount 伤害量
     */
    private void hurt(LivingEntity victim, DamageSource pSource, float pAmount) {
        if (victim.level().isClientSide || victim.isDeadOrDying()) {
        } else {
            if (victim.isMultipartEntity()) {
                for (Entity part :victim.getParts()) {
                    if (part instanceof PartEntity<?> partEntity && partEntity.getParent() == victim) {
                        part.hurt(pSource, pAmount);
                    }
                }
            }
            if (victim.isSleeping() && !victim.level().isClientSide) {
                victim.stopSleeping();
            }

            boolean flag = false;

            victim.setNoActionTime(0);
            victim.walkAnimation.setSpeed(1.5F);
            victim.lastHurt = pAmount;
            victim.invulnerableTime = 20;
            victim.getCombatTracker().recordDamage(pSource, pAmount);
            victim.setHealth(victim.getHealth() - pAmount);
            victim.gameEvent(GameEvent.ENTITY_DAMAGE);
            victim.hurtDuration = 10;
            victim.hurtTime = victim.hurtDuration;



            Entity entity1 = pSource.getEntity();
            if (entity1 != null) {
                if (entity1 instanceof LivingEntity livingentity1) {
                    if (!pSource.is(DamageTypeTags.NO_ANGER)) {
                        victim.setLastHurtByMob(livingentity1);
                    }
                }

                if (entity1 instanceof Player player1) {
                    victim.lastHurtByPlayerTime = 100;
                    victim.setLastHurtByPlayer(player1);
                } else if (entity1 instanceof net.minecraft.world.entity.TamableAnimal tamableEntity) {
                    if (tamableEntity.isTame()) {
                        victim.lastHurtByPlayerTime = 100;
                        LivingEntity livingentity2 = tamableEntity.getOwner();
                        if (livingentity2 instanceof Player player2) {
                            victim.setLastHurtByPlayer(player2);
                        } else {
                            victim.setLastHurtByPlayer(null);
                        }
                    }
                }
            }

            victim.level().broadcastDamageEvent(victim, pSource);

            if (!pSource.is(DamageTypeTags.NO_IMPACT)) {
                victim.hurtMarked = true;
            }

            if (entity1 != null && !pSource.is(DamageTypeTags.IS_EXPLOSION)) {
                double d0 = entity1.getX() - victim.getX();

                double d1;
                for(d1 = entity1.getZ() - victim.getZ(); d0 * d0 + d1 * d1 < 1.0E-4D; d1 = (Math.random() - Math.random()) * 0.01D) {
                    d0 = (Math.random() - Math.random()) * 0.01D;
                }

                victim.knockback(0.4F, d0, d1);
                if (!flag) {
                    victim.indicateDamage(d0, d1);
                }
            }

            if (victim.isDeadOrDying()) {
                die(victim, pSource);
            } else {
                SoundEvent soundevent = SoundEvents.GENERIC_HURT;
                victim.playSound(soundevent, 2F, victim.getVoicePitch());
            }

            boolean flag2 = true;
            victim.lastDamageSource = pSource;
            victim.lastDamageStamp = victim.level().getGameTime();

            if (victim instanceof ServerPlayer) {
                CriteriaTriggers.ENTITY_HURT_PLAYER.trigger((ServerPlayer)victim, pSource, pAmount, pAmount, flag);
            }

            if (entity1 instanceof ServerPlayer) {
                CriteriaTriggers.PLAYER_HURT_ENTITY.trigger((ServerPlayer)entity1, victim, pSource, pAmount, pAmount, flag);
            }

        }

    }

    /**
     * 死亡修正函数
     *
     * @param victim        受害者
     * @param pDamageSource 伤害量
     */
    private void die(LivingEntity victim, DamageSource pDamageSource) {
        if (!victim.isRemoved() && !victim.dead) {
            Entity entity = pDamageSource.getEntity();
            LivingEntity livingentity = victim.getKillCredit();
            if (victim.deathScore >= 0 && livingentity != null) {
                livingentity.awardKillScore(victim, victim.deathScore, pDamageSource);
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
                    victim.dropAllDeathLoot(serverlevel, pDamageSource);
                    createWitherRose(victim, livingentity);
                }

                victim.level().broadcastEntityEvent(victim, (byte) 3);
            }

            victim.setPose(Pose.DYING);
        }
    }

    /**
     * 凋零玫瑰生成修正
     *
     * @param victim        受害者
     * @param pEntitySource 攻击的实体源
     */
    private void createWitherRose(LivingEntity victim, @Nullable LivingEntity pEntitySource) {
        if (!victim.level().isClientSide) {
            boolean flag = false;
            if (pEntitySource instanceof WitherBoss) {
                BlockPos blockpos = victim.blockPosition();
                BlockState blockstate = Blocks.WITHER_ROSE.defaultBlockState();
                if (victim.level().isEmptyBlock(blockpos) && blockstate.canSurvive(victim.level(), blockpos)) {
                    victim.level().setBlock(blockpos, blockstate, 3);
                    flag = true;
                }


                if (!flag) {
                    ItemEntity itementity = new ItemEntity(victim.level(), victim.getX(), victim.getY(), victim.getZ(), new ItemStack(Items.WITHER_ROSE));
                    victim.level().addFreshEntity(itementity);
                }
            }

        }
    }

    /**
     * 获取攻击模式组件
     *
     * @param stack 这把剑的物品
     * @return      攻击模式
     */
    private SwordMode getMode(ItemStack stack) {
        Integer modeIndex = stack.get(ModDataComponents.SWORD_MODE.get());
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
        stack.set(ModDataComponents.SWORD_MODE.get(), mode.ordinal());
    }
}