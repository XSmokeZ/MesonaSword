package me.mesona.mesona_sword.client.model;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import me.mesona.mesona_sword.api.client.render.CosmicRenderCall;
import me.mesona.mesona_sword.api.client.render.CosmicRenderQueue;
import me.mesona.mesona_sword.api.client.util.TransformUtils;
import me.mesona.mesona_sword.api.iface.transform.CosmicRenderable;
import me.mesona.mesona_sword.api.client.model.bakedmodels.WrappedItemModel;
import me.mesona.mesona_sword.client.MesonaSwordClient;
import me.mesona.mesona_sword.client.compat.IrisCompat;
import me.mesona.mesona_sword.client.shader.MesonaRenderTypes;
import me.mesona.mesona_sword.client.shader.MesonaShaders;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemModelShaper;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class CosmicBakeModel extends WrappedItemModel implements CosmicRenderable {
    private final List<ResourceLocation> maskSprite;

    public CosmicBakeModel(final BakedModel wrapped, final List<ResourceLocation> maskSprite) {
        super(wrapped);
        this.maskSprite = maskSprite;
        this.cosmic = true;
    }

    @Override
    public void renderItem(
            ItemStack stack,
            ItemDisplayContext transformType,
            PoseStack pStack,
            MultiBufferSource source,
            int packedLight,
            int packedOverlay,
            ItemModelShaper itemModelShaper,
            TextureManager textureManager
    ) {
        this.parentState = TransformUtils.DEFAULT_TOOL;

        this.renderWrapped(
                stack,
                pStack,
                source,
                packedLight,
                packedOverlay,
                true
        );

        if (source instanceof MultiBufferSource.BufferSource bs) {
            bs.endBatch();
        }

        if (IrisCompat.shouldDefer(transformType)) {
            CosmicRenderQueue.enqueue(
                    new CosmicRenderCall(
                            this,
                            stack,
                            transformType,
                            pStack,
                            packedLight,
                            packedOverlay,
                            RenderSystem.getProjectionMatrix(),
                            RenderSystem.getModelViewMatrix()
                    )
            );
            return;
        }

        renderCosmicLayer(
                stack,
                transformType,
                pStack,
                source,
                packedLight,
                packedOverlay
        );
    }

    @Override
    public void renderCosmicLayer(
            ItemStack stack,
            ItemDisplayContext transformType,
            PoseStack pStack,
            MultiBufferSource source,
            int packedLight,
            int packedOverlay
    ) {
        final Minecraft mc = Minecraft.getInstance();

        float yaw = 0.0f;
        float pitch = 0.0f;
        float scale = 1f;

        if (MesonaSwordClient.inventoryRender
                || transformType == ItemDisplayContext.GUI) {
            scale = 100.0F;
        } else {
            yaw = (float) (mc.player.getYRot() * 2.0f * Math.PI / 360.0);
            pitch = -(float) (mc.player.getXRot() * 2.0f * Math.PI / 360.0);
        }

        if (MesonaShaders.cosmicTime != null) {
            MesonaShaders.cosmicTime.set(mc.level.getGameTime() % Integer.MAX_VALUE);
        }
        if (MesonaShaders.cosmicYaw != null) {
            MesonaShaders.cosmicYaw.set(yaw);
        }
        if (MesonaShaders.cosmicPitch != null) {
            MesonaShaders.cosmicPitch.set(pitch);
        }
        if (MesonaShaders.cosmicExternalScale != null) {
            MesonaShaders.cosmicExternalScale.set(scale);
        }
        if (MesonaShaders.cosmicOpacity != null) {
            MesonaShaders.cosmicOpacity.set(1.0F);
        }

        if (MesonaShaders.cosmicUVs != null) {
            MesonaShaders.cosmicUVs.set(MesonaShaders.COSMIC_UVS);
        }

        final VertexConsumer cons = source.getBuffer(MesonaRenderTypes.COSMIC);

        List<TextureAtlasSprite> atlasSprite = new ArrayList<>();
        for (ResourceLocation res : maskSprite) {
            TextureAtlasSprite sprite = mc.getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(res);
            atlasSprite.add(sprite);
        }

        List<net.minecraft.client.renderer.block.model.BakedQuad> quads = bakeItem(atlasSprite);

        mc.getItemRenderer().renderQuadList(
                pStack,
                cons,
                quads,
                stack,
                packedLight,
                packedOverlay
        );

        if (source instanceof MultiBufferSource.BufferSource bs) {
            bs.endBatch(MesonaRenderTypes.COSMIC);
        }
    }
}