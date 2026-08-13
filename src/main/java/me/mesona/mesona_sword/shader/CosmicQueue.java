package me.mesona.mesona_sword.shader;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

public class CosmicQueue {

    private static final List<CosmicCall> QUEUE = new ArrayList<>();

    public static void enqueue(CosmicCall call) {
        QUEUE.add(call);
    }

    public static void renderAll() {
        if (QUEUE.isEmpty()) return;

        Minecraft mc = Minecraft.getInstance();
        MultiBufferSource.BufferSource source = mc.renderBuffers().bufferSource();
        Matrix4f oldProj = new Matrix4f(RenderSystem.getProjectionMatrix());
        Matrix4f oldModelView = new Matrix4f(RenderSystem.getModelViewMatrix());

        for (CosmicCall call : QUEUE) {
            RenderSystem.setProjectionMatrix(call.projection, RenderSystem.getVertexSorting());
            RenderSystem.getModelViewStack().set(call.modelView);
            RenderSystem.applyModelViewMatrix();

            PoseStack pose = new PoseStack();
            pose.last().pose().set(call.pose);
            pose.last().normal().set(call.normal);

            call.model.renderCosmicLayer(call.stack, call.context, pose, source, call.light, call.overlay);
        }

        source.endBatch();
        RenderSystem.setProjectionMatrix(oldProj, RenderSystem.getVertexSorting());
        RenderSystem.getModelViewStack().set(oldModelView);
        RenderSystem.applyModelViewMatrix();
        QUEUE.clear();
    }
}