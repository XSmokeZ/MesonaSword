package me.mesona.mesona_sword.shader;

import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.mojang.math.Transformation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.*;
import java.util.function.Function;

public class CosmicModel implements BakedModel {

    private static final ItemModelGenerator ITEM_MODEL_GENERATOR = new ItemModelGenerator();
    private static final FaceBakery FACE_BAKERY = new FaceBakery();
    private static final Transformation FLIP_X = new Transformation(null, null, new Vector3f(-1, 1, 1), null);

    private final BakedModel wrapped;
    private final List<ResourceLocation> maskSprite;
    private final Map<ItemDisplayContext, Transformation> transforms;
    private final ItemOverrides overrideList;

    @Nullable private LivingEntity entity;
    @Nullable private ClientLevel world;

    public CosmicModel(BakedModel wrapped, List<ResourceLocation> maskSprite) {
        this.wrapped = wrapped;
        this.maskSprite = maskSprite;
        this.transforms = buildToolTransforms();
        this.overrideList = new ItemOverrides() {
            @Override
            public BakedModel resolve(@NotNull BakedModel originalModel, @NotNull ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity, int seed) {
                CosmicModel.this.entity = entity;
                CosmicModel.this.world = level != null ? level : (entity != null ? (ClientLevel) entity.level() : null);
                return originalModel;
            }
        };
    }

    private static Map<ItemDisplayContext, Transformation> buildToolTransforms() {
        Map<ItemDisplayContext, Transformation> map = new HashMap<>();
        map.put(ItemDisplayContext.GROUND, tr(0, 2, 0, 0, 0, 0, 0.5f));
        map.put(ItemDisplayContext.FIXED, tr(0, 0, 0, 0, 180, 0, 1));
        map.put(ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, tr(0, 4, 0.5f, 0, -90, 55, 0.85f));
        map.put(ItemDisplayContext.THIRD_PERSON_LEFT_HAND, tr(0, 4, 0.5f, 0, 90, -55, 0.85f));
        map.put(ItemDisplayContext.FIRST_PERSON_RIGHT_HAND, tr(1.13f, 3.2f, 1.13f, 0, -90, 25, 0.68f));
        map.put(ItemDisplayContext.FIRST_PERSON_LEFT_HAND, tr(1.13f, 3.2f, 1.13f, 0, 90, -25, 0.68f));
        return ImmutableMap.copyOf(map);
    }

    private static Transformation tr(float tx, float ty, float tz, float rx, float ry, float rz, float s) {
        return new Transformation(
                new Vector3f(tx / 16, ty / 16, tz / 16),
                new Quaternionf().rotationXYZ((float) Math.toRadians(rx), (float) Math.toRadians(ry), (float) Math.toRadians(rz)),
                new Vector3f(s, s, s),
                null
        );
    }

    public void renderItem(ItemStack stack, ItemDisplayContext ctx, PoseStack pose, MultiBufferSource source, int light, int overlay) {
        renderWrapped(stack, pose, source, light, overlay, true);

        if (source instanceof MultiBufferSource.BufferSource bs) bs.endBatch();

        if (IrisCompat.shouldDefer(ctx)) {
            CosmicQueue.enqueue(new CosmicCall(this, stack, ctx, pose, light, overlay,
                    RenderSystem.getProjectionMatrix(), RenderSystem.getModelViewMatrix()));
            return;
        }

        renderCosmicLayer(stack, ctx, pose, source, light, overlay);
    }

    public void renderCosmicLayer(ItemStack stack, ItemDisplayContext ctx, PoseStack pose, MultiBufferSource source, int light, int overlay) {
        Minecraft mc = Minecraft.getInstance();
        float yaw = 0, pitch = 0, scale = 1;

        if (CosmicClient.inventoryRender || ctx == ItemDisplayContext.GUI) {
            scale = 100;
        } else if (mc.player != null) {
            yaw = (float) Math.toRadians(mc.player.getYRot() * 2);
            pitch = -(float) Math.toRadians(mc.player.getXRot() * 2);
        }

        CosmicShaders.setUniforms(mc.level != null ? mc.level.getGameTime() % Integer.MAX_VALUE : 0, yaw, pitch, scale, 1);

        List<TextureAtlasSprite> sprites = new ArrayList<>();
        for (ResourceLocation res : maskSprite) {
            sprites.add(mc.getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(res));
        }

        VertexConsumer cons = source.getBuffer(CosmicRenderTypes.COSMIC);
        mc.getItemRenderer().renderQuadList(pose, cons, bakeItem(sprites), stack, light, overlay);

        if (source instanceof MultiBufferSource.BufferSource bs) bs.endBatch(CosmicRenderTypes.COSMIC);
    }

    private void renderWrapped(ItemStack stack, PoseStack pose, MultiBufferSource source, int light, int overlay, boolean fabulous) {
        BakedModel model = wrapped.getOverrides().resolve(wrapped, stack, world, entity, 0);
        ItemRenderer renderer = Minecraft.getInstance().getItemRenderer();
        for (BakedModel pass : model.getRenderPasses(stack, fabulous)) {
            for (RenderType type : pass.getRenderTypes(stack, fabulous)) {
                renderer.renderModelLists(pass, stack, light, overlay, pose, source.getBuffer(type));
            }
        }
    }

    private static List<BakedQuad> bakeItem(List<TextureAtlasSprite> sprites) {
        List<BakedQuad> quads = new LinkedList<>();
        for (int i = 0; i < sprites.size(); i++) {
            TextureAtlasSprite sprite = sprites.get(i);
            for (BlockElement element : ITEM_MODEL_GENERATOR.processFrames(i, "layer" + i, sprite.contents())) {
                for (Map.Entry<Direction, BlockElementFace> entry : element.faces.entrySet()) {
                    quads.add(FACE_BAKERY.bakeQuad(element.from, element.to, entry.getValue(), sprite, entry.getKey(),
                            new ModelState() {
                                public boolean isUvLocked() { return false; }
                            }, element.rotation, element.shade));
                }
            }
        }
        return quads;
    }

    @Override
    public @NotNull List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @NotNull RandomSource rand) {
        return Collections.emptyList();
    }

    @Override
    public boolean useAmbientOcclusion() { return wrapped.useAmbientOcclusion(); }

    @Override
    public boolean isGui3d() { return wrapped.isGui3d(); }

    @Override
    public boolean usesBlockLight() { return wrapped.usesBlockLight(); }

    @Override
    public boolean isCustomRenderer() { return true; }

    @Override
    public @NotNull TextureAtlasSprite getParticleIcon() { return wrapped.getParticleIcon(); }

    @Override
    public @NotNull ItemOverrides getOverrides() { return overrideList; }

    @Override
    public @NotNull BakedModel applyTransform(@NotNull ItemDisplayContext context, @NotNull PoseStack pose, boolean leftFlip) {
        Transformation t = transforms.getOrDefault(context, Transformation.identity());
        Vector3f tr = t.getTranslation(), sc = t.getScale();
        pose.translate(tr.x(), tr.y(), tr.z());
        pose.mulPose(t.getLeftRotation());
        pose.scale(sc.x(), sc.y(), sc.z());
        pose.mulPose(t.getRightRotation());
        if (leftFlip) pose.mulPose(Axis.YN.rotationDegrees(180));
        return this;
    }
}