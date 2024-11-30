/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.minecraftforge.common.capabilities;

import com.google.common.reflect.TypeToken;
import net.minecraftforge.fml.common.asm.CapabilityTokenSubclass;
import xyz.bluspring.kilt.Kilt;

import java.lang.reflect.Type;

/**
 * Inspired by {@link com.google.common.reflect.TypeToken TypeToken}, use a subclass to capture
 * generic types. Then uses {@link CapabilityTokenSubclass a transformer}
 * to convert that generic into a string returned by {@link #getType}
 * This allows us to know the generic type, without having a hard reference to the
 * class.
 *
 * Example usage:
 * <pre>{@code
 *    public static Capability<IDataHolder> DATA_HOLDER_CAPABILITY
 *    		= CapabilityManager.get(new CapabilityToken<>(){});
 * }</pre>
 *
 */
public abstract class CapabilityToken<T>
{
    private TypeToken<T> typeToken;

    public CapabilityToken() {
        try {
            typeToken = new TypeToken<>(this.getClass()) {};
            type = typeToken.getType();
        } catch (Exception ignored) {
            typeToken = null;
            type = null;
        }
    }

    private Type type;

    protected final String getType()
    {
        if (this.type == null) {
            Kilt.Companion.getLogger().error("ruh roh, a type is unknown");
            return "UNKNOWN";
        }

        return this.type.getTypeName().replace(".", "/");
    }

    @Override
    public String toString()
    {
        return "CapabilityToken[" + getType() + "]";
    }
}
