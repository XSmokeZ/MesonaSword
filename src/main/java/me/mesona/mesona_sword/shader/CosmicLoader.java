package me.mesona.mesona_sword.shader;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.model.geometry.IGeometryLoader;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class CosmicLoader implements IGeometryLoader<CosmicGeometry> {

    public static final CosmicLoader INSTANCE = new CosmicLoader();
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("mesona_sword", "cosmic");

    @Override
    public @NotNull CosmicGeometry read(@NotNull JsonObject json, JsonDeserializationContext ctx) throws JsonParseException {
        JsonObject clean = json.deepCopy();
        clean.remove("loader");
        clean.remove("cosmic");

        BlockModel base = ctx.deserialize(clean, BlockModel.class);
        List<ResourceLocation> masks = new ArrayList<>();

        JsonObject cosmic = json.getAsJsonObject("cosmic");
        if (cosmic != null && cosmic.has("mask")) {
            if (cosmic.get("mask").isJsonArray()) {
                cosmic.getAsJsonArray("mask").forEach(e -> masks.add(ResourceLocation.tryParse(e.getAsString())));
            } else {
                masks.add(ResourceLocation.tryParse(cosmic.get("mask").getAsString()));
            }
        }

        return new CosmicGeometry(base, masks);
    }
}