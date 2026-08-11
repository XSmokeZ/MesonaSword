package me.mesona.mesona_sword.utils;

import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.SimpleTier;

import java.util.stream.Stream;

public final class ModTier {
    public static final Tier MESONA = new SimpleTier(
            BlockTags.AIR,
            9,
            0,
            Float.POSITIVE_INFINITY,
            100,
            () -> Ingredient.of(
                    Items.SHORT_GRASS,
                    Items.TALL_GRASS,
                    Items.FERN,
                    Items.LARGE_FERN
            )
    );
}
