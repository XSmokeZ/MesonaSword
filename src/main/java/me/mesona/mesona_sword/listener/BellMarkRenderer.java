package me.mesona.mesona_sword.listener;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import me.mesona.mesona_sword.MesonaSword;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class BellMarkRenderer {

    private static final ResourceLocation BELL_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(MesonaSword.MODID, "textures/particle/bell.png");

    // 客户端维护的标记实体网络ID集合
    private static final Set<Integer> markedEntityIds = new HashSet<>();

    public static void addMark(int entityId) {
        markedEntityIds.add(entityId);
    }

    public static void removeMark(int entityId) {
        markedEntityIds.remove(entityId);
    }

    public static void syncMarks(Set<Integer> ids) {
        markedEntityIds.clear();
        markedEntityIds.addAll(ids);
    }

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) return;

        if (markedEntityIds.isEmpty()) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        Vec3 cam = event.getCamera().getPosition();
        float partialTick = event.getPartialTick().getGameTimeDeltaPartialTick(false);

        // 清理已死亡/不存在的实体
        markedEntityIds.removeIf(id -> {
            Entity entity = mc.level.getEntity(id);
            return entity == null || !entity.isAlive();
        });

        if (markedEntityIds.isEmpty()) return;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.depthMask(false);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, BELL_TEXTURE);

        Tesselator t = Tesselator.getInstance();

        for (int entityId : markedEntityIds) {
            Entity entity = mc.level.getEntity(entityId);
            if (entity != null && entity.isAlive()) {
                renderBell(entity, cam, t, partialTick);
            }
        }

        RenderSystem.depthMask(true);
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }

    private static void renderBell(Entity entity, Vec3 cam, Tesselator t, float partialTick) {
        double x = entity.getX();
        double y = entity.getY() + entity.getBbHeight() + 0.5;
        double z = entity.getZ();

        float relX = (float) (x - cam.x);
        float relY = (float) (y - cam.y);
        float relZ = (float) (z - cam.z);

        PoseStack pose = new PoseStack();
        pose.translate(relX, relY, relZ);

        Minecraft mc = Minecraft.getInstance();
        if (mc.gameRenderer.getMainCamera() != null) {
            Quaternionf cameraRotation = mc.gameRenderer.getMainCamera().rotation();
            pose.mulPose(cameraRotation);
        }

        float bob = (float) Math.sin((mc.level != null ? mc.level.getGameTime() + partialTick : 0) * 0.1) * 0.05f;
        pose.translate(0, bob, 0);

        float scale = 0.3f;
        Matrix4f m = pose.last().pose();

        BufferBuilder buffer = t.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        buffer.addVertex(m, -scale, -scale, 0).setUv(0, 1);
        buffer.addVertex(m, scale, -scale, 0).setUv(1, 1);
        buffer.addVertex(m, scale, scale, 0).setUv(1, 0);
        buffer.addVertex(m, -scale, scale, 0).setUv(0, 0);
        BufferUploader.drawWithShader(buffer.buildOrThrow());
    }
}