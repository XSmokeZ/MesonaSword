package me.mesona.mesona_sword.utils;

import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.SimpleTier;

public final class ModTier {
    public static final Tier MESONA = new SimpleTier(
            BlockTags.AIR,
            0,
            0,
            Float.POSITIVE_INFINITY,
            30,
            () -> Ingredient.EMPTY
    );
}
