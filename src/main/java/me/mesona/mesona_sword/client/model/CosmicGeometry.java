package me.mesona.mesona_sword.client.model;

import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.model.geometry.IGeometryBakingContext;
import net.neoforged.neoforge.client.model.geometry.IUnbakedGeometry;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
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
    public @NotNull BakedModel bake(@NotNull IGeometryBakingContext context, @NotNull ModelBaker baker, @NotNull Function<Material, TextureAtlasSprite> spriteGetter, @NotNull ModelState modelState, @NotNull ItemOverrides overrides) {
        BakedModel bakedBase = this.baseModel.bake(baker, this.baseModel, spriteGetter, modelState, true);
        return new CosmicBakeModel(bakedBase, this.maskTexture);
    }

    @Override
    public void resolveParents(@NotNull Function<ResourceLocation, UnbakedModel> modelGetter, @NotNull IGeometryBakingContext context) {
        this.baseModel.resolveParents(modelGetter);
    }
}