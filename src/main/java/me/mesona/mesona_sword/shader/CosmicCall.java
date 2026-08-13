package me.mesona.mesona_sword.shader;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public class CosmicCall {

    public final CosmicModel model;
    public final ItemStack stack;
    public final ItemDisplayContext context;
    public final int light, overlay;
    public final Matrix4f pose, projection, modelView;
    public final Matrix3f normal;

    public CosmicCall(CosmicModel model, ItemStack stack, ItemDisplayContext context, PoseStack poseStack,
                      int light, int overlay, Matrix4f projection, Matrix4f modelView) {
        this.model = model;
        this.stack = stack;
        this.context = context;
        this.light = light;
        this.overlay = overlay;
        PoseStack.Pose p = poseStack.last();
        this.pose = new Matrix4f(p.pose());
        this.normal = new Matrix3f(p.normal());
        this.projection = new Matrix4f(projection);
        this.modelView = new Matrix4f(modelView);
    }
}