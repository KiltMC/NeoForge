/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.math.Transformation;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.minecraft.client.renderer.block.model.BlockElement;
import net.minecraft.client.renderer.block.model.BlockElementFace;
import net.minecraft.client.renderer.block.model.BlockFaceUV;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemOverride;
import net.minecraft.client.renderer.block.model.ItemTransform;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.neoforged.neoforge.client.model.geometry.GeometryLoaderManager;
import net.neoforged.neoforge.client.model.geometry.IUnbakedGeometry;
import net.neoforged.neoforge.common.util.TransformationHelper;
import org.jetbrains.annotations.Nullable;

/**
 * A version of {@link BlockModel.Deserializer} capable of deserializing models with custom loaders, as well as other
 * changes introduced to the spec by Forge.
 */
public class ExtendedBlockModelDeserializer extends BlockModel.Deserializer {
    // Kilt: make INSTANCE non-final
    public static Gson INSTANCE = (new GsonBuilder())
            .registerTypeAdapter(BlockModel.class, new ExtendedBlockModelDeserializer())
            .registerTypeAdapter(BlockElement.class, new BlockElement.Deserializer())
            .registerTypeAdapter(BlockElementFace.class, new BlockElementFace.Deserializer())
            .registerTypeAdapter(BlockFaceUV.class, new BlockFaceUV.Deserializer())
            .registerTypeAdapter(ItemTransform.class, new ItemTransform.Deserializer())
            .registerTypeAdapter(ItemTransforms.class, new ItemTransforms.Deserializer())
            .registerTypeAdapter(ItemOverride.class, new ItemOverride.Deserializer())
            .registerTypeAdapter(Transformation.class, new TransformationHelper.Deserializer())
            .create();

    @Override
    public BlockModel deserialize(JsonElement element, Type targetType, JsonDeserializationContext deserializationContext) throws JsonParseException {
        BlockModel model = super.deserialize(element, targetType, deserializationContext);
        return this.kilt$deserialize(element, targetType, deserializationContext, model);
    }

    // Kilt: split off into custom deserialize, for improved mod compatibility
    public BlockModel kilt$deserialize(JsonElement element, Type targetType, JsonDeserializationContext deserializationContext, BlockModel model) throws JsonParseException {
        JsonObject jsonobject = element.getAsJsonObject();
        IUnbakedGeometry<?> geometry = deserializeGeometry(deserializationContext, jsonobject);

        List<BlockElement> elements = model.getElements();
        if (geometry != null) {
            elements.clear();
            model.kilt$getCustomData().setCustomGeometry(geometry);
        }

        if (jsonobject.has("transform")) {
            JsonElement transform = jsonobject.get("transform");
            model.kilt$getCustomData().setRootTransform(deserializationContext.deserialize(transform, Transformation.class));
        }

        if (jsonobject.has("render_type")) {
            var renderTypeHintName = GsonHelper.getAsString(jsonobject, "render_type");
            model.kilt$getCustomData().setRenderTypeHint(ResourceLocation.parse(renderTypeHintName));
        }

        if (jsonobject.has("visibility")) {
            JsonObject visibility = GsonHelper.getAsJsonObject(jsonobject, "visibility");
            for (Map.Entry<String, JsonElement> part : visibility.entrySet()) {
                model.kilt$getCustomData().visibilityData.setVisibilityState(part.getKey(), part.getValue().getAsBoolean());
            }
        }

        return model;
    }

    @Nullable
    public static IUnbakedGeometry<?> deserializeGeometry(JsonDeserializationContext deserializationContext, JsonObject object) throws JsonParseException {
        if (!object.has("loader"))
            return null;

        ResourceLocation name;
        boolean optional;
        if (object.get("loader").isJsonObject()) {
            JsonObject loaderObj = object.getAsJsonObject("loader");
            name = ResourceLocation.parse(GsonHelper.getAsString(loaderObj, "id"));
            optional = GsonHelper.getAsBoolean(loaderObj, "optional", false);
        } else {
            name = ResourceLocation.parse(GsonHelper.getAsString(object, "loader"));
            optional = false;
        }

        var loader = GeometryLoaderManager.get(name);
        if (loader == null) {
            if (optional) {
                return null;
            }
            throw new JsonParseException(String.format(Locale.ENGLISH, "Model loader '%s' not found. Registered loaders: %s", name, GeometryLoaderManager.getLoaderList()));
        }

        return loader.read(object, deserializationContext);
    }
}
