/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.minecraftforge.fml;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.config.IConfigEvent;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.internal.ForgeBindings;

import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import java.util.function.Supplier;

/**
 * Used to allow access to certain things from the game layer
 */
public class Bindings {
    private Bindings() {}

    /**
     * The provider of the bindings
     * @implNote May also throw a {@link ServiceConfigurationError}
     */
    private static final IBindingsProvider PROVIDER = new ForgeBindings();

    /**
     * @return A supplier of net.minecraftforge.common.MinecraftForge#EVENT_BUS
     */
    public static Supplier<IEventBus> getForgeBus() {
        return PROVIDER.getForgeBusSupplier();
    }

    public static Supplier<I18NParser> getMessageParser() {
        return PROVIDER.getMessageParser();
    }

    public static Supplier<IConfigEvent.ConfigConfig> getConfigConfiguration() {
        return PROVIDER.getConfigConfiguration();
    }
}
