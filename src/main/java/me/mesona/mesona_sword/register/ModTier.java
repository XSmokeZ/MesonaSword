package me.mesona.mesona_sword.register;

import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.SimpleTier;

public final class ModTier {
    public static final Tier MESONA = new SimpleTier(
            BlockTags.AIR,
            1145,
            0,
            0,
            100,
            () -> Ingredient.of(
                    Items.SHORT_GRASS,
                    Items.TALL_GRASS,
                    Items.FERN,
                    Items.LARGE_FERN
            )
    );
}
