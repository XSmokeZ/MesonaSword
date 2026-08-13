package me.mesona.mesona_sword.shader;

import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.model.geometry.IGeometryBakingContext;
import net.neoforged.neoforge.client.model.geometry.IUnbakedGeometry;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Function;

public class CosmicGeometry implements IUnbakedGeometry<CosmicGeometry> {

    private final BlockModel baseModel;
    private final List<ResourceLocation> maskTexture;

    public CosmicGeometry(BlockModel baseModel, List<ResourceLocation> maskTexture) {
        this.baseModel = baseModel;
        this.maskTexture = maskTexture;
    }

    @Override
    public @NotNull BakedModel bake(@NotNull IGeometryBakingContext context, @NotNull ModelBaker baker,
                                    @NotNull Function<Material, TextureAtlasSprite> spriteGetter,
                                    @NotNull ModelState modelState, @NotNull ItemOverrides overrides) {
        return new CosmicModel(baseModel.bake(baker, baseModel, spriteGetter, modelState, true), maskTexture);
    }

    @Override
    public void resolveParents(@NotNull Function<ResourceLocation, UnbakedModel> modelGetter, @NotNull IGeometryBakingContext context) {
        baseModel.resolveParents(modelGetter);
    }
}