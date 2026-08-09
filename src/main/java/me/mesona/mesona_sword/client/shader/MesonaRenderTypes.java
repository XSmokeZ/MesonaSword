package me.mesona.mesona_sword.client.shader;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import me.mesona.mesona_sword.MesonaSword;
import me.mesona.mesona_sword.utils.RenderUtils;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;

public class MesonaRenderTypes {
    public static RenderType COSMIC = RenderType.create(
            MesonaSword.rl("cosmic").toString(),
            DefaultVertexFormat.BLOCK,
            VertexFormat.Mode.QUADS, 2097152, true, false,
            RenderType.CompositeState.builder()
                    .setShaderState(new RenderStateShard.ShaderStateShard(() -> MesonaShaders.COSMIC_SHADER))
                    .setDepthTestState(RenderStateShard.EQUAL_DEPTH_TEST)
                    .setLightmapState(RenderStateShard.LIGHTMAP)
                    .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                    .setTextureState(RenderUtils.COSMIC_TEXTURE_ISOLATED)
                    .createCompositeState(true)
    );
}