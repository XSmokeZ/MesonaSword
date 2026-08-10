package me.mesona.mesona_sword.client;

import me.mesona.mesona_sword.GrassSwordItem;
import me.mesona.mesona_sword.MesonaSword;
import me.mesona.mesona_sword.api.client.render.CosmicRenderQueue;
import me.mesona.mesona_sword.client.model.CosmicModelLoader;
import me.mesona.mesona_sword.client.shader.MesonaShaders;
import me.mesona.mesona_sword.utils.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;
import net.neoforged.neoforge.client.event.RenderFrameEvent;
import net.neoforged.neoforge.client.event.TextureAtlasStitchedEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

@EventBusSubscriber(modid = MesonaSword.MODID, value = Dist.CLIENT)
public class MesonaSwordClient {

    public static boolean inventoryRender = false;

    @SubscribeEvent
    public static void registerGeometryLoaders(ModelEvent.RegisterGeometryLoaders event) {
        event.register(CosmicModelLoader.ID, CosmicModelLoader.INSTANCE);
    }

    @SubscribeEvent
    public static void onTextureAtlasStitched(TextureAtlasStitchedEvent event) {
        if (event.getAtlas().location().toString().equals("minecraft:textures/atlas/blocks.png")) {
            for (int i = 0; i < 10; i++) {
                ResourceLocation spriteLoc = ResourceLocation.fromNamespaceAndPath(MesonaSword.MODID, "misc/cosmic/cosmic_" + i);
                TextureAtlasSprite sprite = event.getAtlas().getSprite(spriteLoc);
                if (sprite != null) {
                    MesonaShaders.COSMIC_UVS[i * 4] = sprite.getU0();
                    MesonaShaders.COSMIC_UVS[i * 4 + 1] = sprite.getV0();
                    MesonaShaders.COSMIC_UVS[i * 4 + 2] = sprite.getU1();
                    MesonaShaders.COSMIC_UVS[i * 4 + 3] = sprite.getV1();
                    MesonaShaders.COSMIC_SPRITES[i] = sprite;
                }
            }
        }
    }

    @SubscribeEvent
    public static void onRegisterShaders(RegisterShadersEvent event) {
        MesonaShaders.onRegisterShaders(event);
    }

    @SubscribeEvent
    public static void onRenderFrame(RenderFrameEvent.Pre event) {
        CosmicRenderQueue.renderAll();
    }

    @SubscribeEvent
    public static void onTooltip(ItemTooltipEvent event) {
        if (event.getItemStack().getItem() instanceof GrassSwordItem) {
            for (int i = 0; i < event.getToolTip().size(); i++) {
                String line = event.getToolTip().get(i).getString();
                if (line.contains(I18n.get("attribute.name.generic.attack_damage"))) {
                    event.getToolTip().set(i,
                            Component.literal(TextUtils.makeFabulous(I18n.get("tooltip.mesona_sword.infinity")))
                                    .append(" ")
                                    .append(Component.translatable("tooltip.mesona_sword.attack_damage.desc")
                                            .withStyle(ChatFormatting.DARK_GREEN))
                    );
                    return;
                }
            }
        }
    }
}