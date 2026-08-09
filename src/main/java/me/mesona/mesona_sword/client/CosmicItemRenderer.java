package me.mesona.mesona_sword.client;

import com.mojang.blaze3d.vertex.PoseStack;
import me.mesona.mesona_sword.client.model.CosmicBakeModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class CosmicItemRenderer extends BlockEntityWithoutLevelRenderer {

    public CosmicItemRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }

    @Override
    public void renderByItem(@NotNull ItemStack stack, @NotNull ItemDisplayContext displayContext, @NotNull PoseStack poseStack, @NotNull MultiBufferSource buffer, int packedLight, int packedOverlay) {
        BakedModel model = Minecraft.getInstance().getItemRenderer().getItemModelShaper().getItemModel(stack.getItem());

        poseStack.pushPose();
        model = model.applyTransform(displayContext, poseStack, false);
        poseStack.translate(-0.5D, -0.5D, -0.5D);

        if (model instanceof CosmicBakeModel cosmicModel) {
            cosmicModel.renderItem(stack, displayContext, poseStack, buffer, packedLight, packedOverlay,
                    Minecraft.getInstance().getItemRenderer().getItemModelShaper(),
                    Minecraft.getInstance().getTextureManager());
        } else {
            BakedModel resolvedModel = Minecraft.getInstance().getItemRenderer().getModel(stack, null, null, 0);
            Minecraft.getInstance().getItemRenderer().render(stack, displayContext, false, poseStack, buffer, packedLight, packedOverlay, resolvedModel);
        }

        poseStack.popPose();
    }
}