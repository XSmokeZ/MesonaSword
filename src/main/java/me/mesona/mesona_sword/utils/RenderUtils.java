package me.mesona.mesona_sword.utils;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.world.inventory.InventoryMenu;

public class RenderUtils {

    public static final RenderStateShard.TextureStateShard COSMIC_TEXTURE_ISOLATED =
            new RenderStateShard.TextureStateShard(
                    InventoryMenu.BLOCK_ATLAS,
                    false,
                    false
            );

    public static final RenderStateShard.LayeringStateShard POLYGON_OFFSET_LAYERING =
            new RenderStateShard.LayeringStateShard(
                    "polygon_offset_layering",
                    () -> {
                        RenderSystem.polygonOffset(-1.0F, -10.0F);
                        RenderSystem.enablePolygonOffset();
                    },
                    () -> {
                        RenderSystem.polygonOffset(0.0F, 0.0F);
                        RenderSystem.disablePolygonOffset();
                    }
            );
}