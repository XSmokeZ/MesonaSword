package me.mesona.mesona_sword.client.model;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.model.geometry.IGeometryLoader;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class CosmicModelLoader implements IGeometryLoader<CosmicGeometry> {
    public static final CosmicModelLoader INSTANCE = new CosmicModelLoader();
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("mesona_sword", "cosmic");

    @Override
    public @NotNull CosmicGeometry read(@NotNull JsonObject modelContents, JsonDeserializationContext deserializationContext) throws JsonParseException {
        JsonObject clean = modelContents.deepCopy();
        clean.remove("loader");
        clean.remove("cosmic");

        BlockModel baseModel = deserializationContext.deserialize(clean, BlockModel.class);
        List<ResourceLocation> maskTexture = new ArrayList<>();

        JsonObject cosmic = modelContents.getAsJsonObject("cosmic");
        if (cosmic != null && cosmic.has("mask")) {
            if (cosmic.get("mask").isJsonArray()) {
                var masks = cosmic.getAsJsonArray("mask");
                for (int i = 0; i < masks.size(); i++) {
                    maskTexture.add(ResourceLocation.tryParse(masks.get(i).getAsString()));
                }
            } else {
                maskTexture.add(ResourceLocation.tryParse(cosmic.get("mask").getAsString()));
            }
        }

        return new CosmicGeometry(baseModel, maskTexture);
    }
}