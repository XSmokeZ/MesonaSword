package me.mesona.mesona_sword.register;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageType;

import static me.mesona.mesona_sword.MesonaSword.MODID;

public class ModDamage {
    public static final ResourceKey<DamageType> MESONA_DAMAGE =
            ResourceKey.create(Registries.DAMAGE_TYPE, ResourceLocation.fromNamespaceAndPath(MODID, "mesona_damage"));
}
