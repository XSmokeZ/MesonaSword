package me.mesona.mesona_sword.client.shader;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import me.mesona.mesona_sword.MesonaSword;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;

public class MesonaRenderTypes {

    public static final RenderStateShard.TextureStateShard COSMIC_TEXTURE_ISOLATED =
            new RenderStateShard.TextureStateShard(
                    InventoryMenu.BLOCK_ATLAS,
                    false,
                    false
            );

    public static RenderType COSMIC = RenderType.create(
            ResourceLocation.fromNamespaceAndPath(MesonaSword.MODID, "cosmic").toString(),
            DefaultVertexFormat.BLOCK,
            VertexFormat.Mode.QUADS, 2097152, true, false,
            RenderType.CompositeState.builder()
                    .setShaderState(new RenderStateShard.ShaderStateShard(() -> MesonaShaders.COSMIC_SHADER))
                    .setDepthTestState(RenderStateShard.EQUAL_DEPTH_TEST)
                    .setLightmapState(RenderStateShard.LIGHTMAP)
                    .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                    .setTextureState(COSMIC_TEXTURE_ISOLATED)
                    .createCompositeState(true)
    );
}