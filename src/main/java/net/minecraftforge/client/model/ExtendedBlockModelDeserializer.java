/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.minecraftforge.client.model;

import com.google.gson.*;
import com.mojang.math.Transformation;
import net.minecraft.client.renderer.block.model.BlockElement;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraftforge.client.model.geometry.GeometryLoaderManager;
import net.minecraftforge.client.model.geometry.IUnbakedGeometry;
import org.jetbrains.annotations.Nullable;
import xyz.bluspring.kilt.injections.client.renderer.block.model.BlockModelInjection;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * A version of {@link BlockModel.Deserializer} capable of deserializing models with custom loaders, as well as other
 * changes introduced to the spec by Forge.
 */
public class ExtendedBlockModelDeserializer extends BlockModel.Deserializer
{
    public static Gson INSTANCE;

    public BlockModel deserialize(JsonElement element, Type targetType, JsonDeserializationContext deserializationContext) throws JsonParseException
    {
        BlockModel model = super.deserialize(element, targetType, deserializationContext);
        return this.kilt$deserialize(element, targetType, deserializationContext, model);
    }

    // Kilt: Separate to allow regular deserialize to be called by other mods, while this method can be called by Kilt itself
    public BlockModel kilt$deserialize(JsonElement element, Type targetType, JsonDeserializationContext deserializationContext, BlockModel model) throws JsonParseException
    {
        JsonObject jsonobject = element.getAsJsonObject();
        IUnbakedGeometry<?> geometry = deserializeGeometry(deserializationContext, jsonobject);

        List<BlockElement> elements = model.getElements();
        if (geometry != null)
        {
            elements.clear();
            ((BlockModelInjection) model).kilt$getCustomData().setCustomGeometry(geometry);
        }

        if (jsonobject.has("transform"))
        {
            JsonElement transform = jsonobject.get("transform");
            ((BlockModelInjection) model).kilt$getCustomData().setRootTransform(deserializationContext.deserialize(transform, Transformation.class));
        }

        if (jsonobject.has("render_type"))
        {
            var renderTypeHintName = GsonHelper.getAsString(jsonobject, "render_type");
            ((BlockModelInjection) model).kilt$getCustomData().setRenderTypeHint(new ResourceLocation(renderTypeHintName));
        }

        if (jsonobject.has("visibility"))
        {
            JsonObject visibility = GsonHelper.getAsJsonObject(jsonobject, "visibility");
            for (Map.Entry<String, JsonElement> part : visibility.entrySet())
            {
                ((BlockModelInjection) model).kilt$getCustomData().visibilityData.setVisibilityState(part.getKey(), part.getValue().getAsBoolean());
            }
        }

        return model;
    }

    @Nullable
    public static IUnbakedGeometry<?> deserializeGeometry(JsonDeserializationContext deserializationContext, JsonObject object) throws JsonParseException
    {
        if (!object.has("loader"))
            return null;

        var name = new ResourceLocation(GsonHelper.getAsString(object, "loader"));
        var loader = GeometryLoaderManager.get(name);
        if (loader == null)
            throw new JsonParseException(String.format(Locale.ENGLISH, "Model loader '%s' not found. Registered loaders: %s", name, GeometryLoaderManager.getLoaderList()));

        return loader.read(object, deserializationContext);
    }
}
