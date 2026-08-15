package me.mesona.mesona_sword.listener;

import com.mojang.blaze3d.platform.InputConstants;
import me.mesona.mesona_sword.register.GrassSwordItem;
import me.mesona.mesona_sword.util.ListHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.fml.common.EventBusSubscriber;

import static me.mesona.mesona_sword.MesonaSword.MODID;

@EventBusSubscriber(modid = MODID)
public class EventHandle {

    @SubscribeEvent
    public static void onLivingUpdate(EntityTickEvent.Pre event) {
        if (event.getEntity() instanceof LivingEntity livingEntity) {
            if (ListHelper.entityContainsInList(livingEntity)) {
                if (livingEntity.getHealth() > 0) {
                    if (!(livingEntity instanceof Player))
                        livingEntity.die(livingEntity.damageSources().fellOutOfWorld());
                    livingEntity.setHealth(0);
                    ListHelper.removeEntityToList(livingEntity);
                }
            }
            if (ListHelper.entityContainsInRemoveList(livingEntity)) {
                livingEntity.discard();
                livingEntity.setInvisible(true);
                livingEntity.removeVehicle();
                livingEntity.remove(Entity.RemovalReason.DISCARDED);
                livingEntity.onRemovedFromLevel();
                livingEntity.setHealth(0);
                ListHelper.removeEntityToRemoveList(livingEntity);
            }
        }
    }

    @SubscribeEvent
    public static void onLivingIncomingDamage(LivingIncomingDamageEvent event) {
        if (event.getEntity().level().isClientSide) return;

        LivingEntity victim = event.getEntity();
        DamageSource source = event.getSource();
        Entity attacker = source.getEntity();

        if (!(attacker instanceof Player player)) return;

        ItemStack weapon = player.getMainHandItem();
        if (!(weapon.getItem() instanceof GrassSwordItem)) return;

        if (GrassSwordItem.getMode(weapon) != GrassSwordItem.SwordMode.NORMAL) return;

        player.heal(1.0f);

        float bonusDamage = victim.getMaxHealth() * 0.1f;
        float newDamage = event.getAmount() + bonusDamage;

        event.setAmount(newDamage);
    }
}
