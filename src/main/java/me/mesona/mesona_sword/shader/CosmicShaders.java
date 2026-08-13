package me.mesona.mesona_sword.shader;

import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@EventBusSubscriber(modid = "mesona_sword", value = Dist.CLIENT)
public class CosmicShaders {

    private static final Logger LOGGER = LoggerFactory.getLogger(CosmicShaders.class);
    public static final float[] COSMIC_UVS = new float[40];
    public static TextureAtlasSprite[] COSMIC_SPRITES = new TextureAtlasSprite[10];

    public static ShaderInstance COSMIC_SHADER;
    private static Uniform uTime, uYaw, uPitch, uScale, uOpacity, uUVs;

    @SubscribeEvent
    public static void onRegisterShaders(RegisterShadersEvent event) {
        try {
            event.registerShader(new ShaderInstance(event.getResourceProvider(),
                    ResourceLocation.fromNamespaceAndPath("mesona_sword", "cosmic"), DefaultVertexFormat.BLOCK), shader -> {
                COSMIC_SHADER = shader;
                uTime = shader.getUniform("time");
                uYaw = shader.getUniform("yaw");
                uPitch = shader.getUniform("pitch");
                uScale = shader.getUniform("externalScale");
                uOpacity = shader.getUniform("opacity");
                uUVs = shader.getUniform("cosmicuvs");
                shader.apply();
            });
        } catch (Exception e) {
            LOGGER.error("Failed to register cosmic shader!", e);
        }
    }

    public static void setUniforms(long time, float yaw, float pitch, float scale, float opacity) {
        if (uTime != null) uTime.set(time);
        if (uYaw != null) uYaw.set(yaw);
        if (uPitch != null) uPitch.set(pitch);
        if (uScale != null) uScale.set(scale);
        if (uOpacity != null) uOpacity.set(opacity);
        if (uUVs != null) uUVs.set(COSMIC_UVS);
    }
}