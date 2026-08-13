package me.mesona.mesona_sword.client.shader;

import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import me.mesona.mesona_sword.MesonaSword;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@EventBusSubscriber(modid = MesonaSword.MODID, value = Dist.CLIENT)
public class MesonaShaders {
    private static final Logger LOGGER = LoggerFactory.getLogger(MesonaShaders.class);

    public static final float[] COSMIC_UVS = new float[40];
    public static TextureAtlasSprite[] COSMIC_SPRITES = new TextureAtlasSprite[10];

    public static ShaderInstance COSMIC_SHADER;

    public static Uniform cosmicTime;
    public static Uniform cosmicYaw;
    public static Uniform cosmicPitch;
    public static Uniform cosmicExternalScale;
    public static Uniform cosmicOpacity;
    public static Uniform cosmicUVs;

    @SubscribeEvent
    public static void onRegisterShaders(RegisterShadersEvent event) {
        try {
            event.registerShader(new ShaderInstance(event.getResourceProvider(), ResourceLocation.fromNamespaceAndPath(MesonaSword.MODID, "cosmic"), DefaultVertexFormat.BLOCK), shader -> {
                COSMIC_SHADER = shader;
                cosmicTime = COSMIC_SHADER.getUniform("time");
                cosmicYaw = COSMIC_SHADER.getUniform("yaw");
                cosmicPitch = COSMIC_SHADER.getUniform("pitch");
                cosmicExternalScale = COSMIC_SHADER.getUniform("externalScale");
                cosmicOpacity = COSMIC_SHADER.getUniform("opacity");
                cosmicUVs = COSMIC_SHADER.getUniform("cosmicuvs");
                COSMIC_SHADER.apply();
            });
        } catch (Exception e) {
            LOGGER.error("Failed to register MesonaSword cosmic shader!", e);
        }
    }
}