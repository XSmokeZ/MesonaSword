package me.mesona.mesona_sword.client.effect;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import me.mesona.mesona_sword.MesonaSword;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@EventBusSubscriber(modid = MesonaSword.MODID, value = Dist.CLIENT)
public class SweepEffectRenderer {

    private static final int TOTAL_FRAMES = 6; // 总帧数
    private static final int FRAME_TIME = 2;   // 每帧持续tick

    private static final List<SweepEffect> effects = new ArrayList<>();

    public static void addEffect(Vec3 pos, float yaw, float scale) {
        effects.add(new SweepEffect(pos, yaw, scale, TOTAL_FRAMES * FRAME_TIME));
    }

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) return;

        Iterator<SweepEffect> it = effects.iterator();
        while (it.hasNext()) {
            if (it.next().tick()) it.remove();
        }
        if (effects.isEmpty()) return;

        Vec3 cam = event.getCamera().getPosition();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.depthMask(false);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);

        Tesselator t = Tesselator.getInstance();

        for (SweepEffect e : effects) {
            render(e, cam, t);
        }

        RenderSystem.depthMask(true);
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }

    private static void render(SweepEffect e, Vec3 cam, Tesselator t) {
        int frame = e.getFrameIndex(TOTAL_FRAMES);
        ResourceLocation tex = ResourceLocation.fromNamespaceAndPath(
            MesonaSword.MODID, "textures/particle/sweep_" + frame + ".png"
        );
        RenderSystem.setShaderTexture(0, tex);

        float relX = (float) (e.pos.x - cam.x);
        float relY = (float) (e.pos.y + e.scale * 0.5 - cam.y);
        float relZ = (float) (e.pos.z - cam.z);

        PoseStack pose = new PoseStack();
        pose.translate(relX, relY, relZ);
        pose.mulPose(new Quaternionf().fromAxisAngleDeg(new Vector3f(0, 1, 0), e.yaw));

        float s = e.scale;
        Matrix4f m = pose.last().pose();

        BufferBuilder buffer = t.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        buffer.addVertex(m, -s, -s, 0).setUv(0, 1);
        buffer.addVertex(m,  s, -s, 0).setUv(1, 1);
        buffer.addVertex(m,  s,  s, 0).setUv(1, 0);
        buffer.addVertex(m, -s,  s, 0).setUv(0, 0);
        BufferUploader.drawWithShader(buffer.buildOrThrow());
    }
}