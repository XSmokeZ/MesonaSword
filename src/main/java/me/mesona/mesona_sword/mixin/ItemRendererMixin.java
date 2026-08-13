package me.mesona.mesona_sword.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import me.mesona.mesona_sword.shader.CosmicModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemRenderer.class)
public class ItemRendererMixin {

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    public void mesona$onRenderItem(ItemStack stack, ItemDisplayContext context, boolean leftHand,
                                     PoseStack pose, MultiBufferSource buffers, int light, int overlay,
                                     BakedModel model, CallbackInfo ci) {
        if (!(model instanceof CosmicModel cosmic)) return;

        pose.pushPose();
        cosmic.applyTransform(context, pose, leftHand);
        pose.translate(-0.5, -0.5, -0.5);
        cosmic.renderItem(stack, context, pose, buffers, light, overlay);
        pose.popPose();
        ci.cancel();
    }
}