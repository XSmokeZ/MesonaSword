package me.mesona.mesona_sword;

import me.mesona.mesona_sword.network.SweepAttackPacket;
import me.mesona.mesona_sword.register.ModDataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

@Mod(MesonaSword.MODID)
public class MesonaSword {
    public static final String MODID = "mesona_sword";
    public static final ResourceKey<DamageType> MESONA_DAMAGE =
            ResourceKey.create(Registries.DAMAGE_TYPE, ResourceLocation.fromNamespaceAndPath(MODID, "mesona_damage"));

    public MesonaSword(IEventBus modEventBus) {
        GrassSwordItem.ITEMS.register(modEventBus);
        ModDataComponents.COMPONENTS.register(modEventBus);
        modEventBus.addListener(this::registerPackets);
    }

    public static ResourceLocation rl(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }

    private void registerPackets(RegisterPayloadHandlersEvent event) {
        event.registrar("1")
                .playToServer(SweepAttackPacket.TYPE, SweepAttackPacket.STREAM_CODEC, SweepAttackPacket::handle);
    }
}