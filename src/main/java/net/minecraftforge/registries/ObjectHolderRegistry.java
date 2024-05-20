/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.minecraftforge.registries;

import com.google.common.collect.Maps;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.MappingResolver;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.forgespi.language.ModFileScanData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.lang.annotation.ElementType;
import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Internal registry for tracking {@link ObjectHolder} references
 */
public class ObjectHolderRegistry
{
    /**
     * Exposed to allow modders to register their own notification handlers.
     * This runnable will be called after a registry snapshot has been injected and finalized.
     * The internal list is backed by a HashSet so it is HIGHLY recommended you implement a proper equals
     * and hashCode function to de-duplicate callers here.
     * The default @ObjectHolder implementation uses the hashCode/equals for the field the annotation is on.
     */
    public static synchronized void addHandler(Consumer<Predicate<ResourceLocation>> ref)
    {
        objectHolders.add(ref);
    }

    /**
     * Removed the specified handler from the notification list.
     *
     * The internal list is backed by a hash set, and so proper hashCode and equals operations are required for success.
     *
     * The default @ObjectHolder implementation uses the hashCode/equals for the field the annotation is on.
     *
     * @return true if handler was matched and removed.
     */
    public static synchronized boolean removeHandler(Consumer<Predicate<ResourceLocation>> ref)
    {
        return objectHolders.remove(ref);
    }

    //==============================================================
    // Everything below is internal, do not use.
    //==============================================================

    private static final Logger LOGGER = LogManager.getLogger();
    private static final Set<Consumer<Predicate<ResourceLocation>>> objectHolders = new HashSet<>();
    private static final Type OBJECT_HOLDER = Type.getType(ObjectHolder.class);
    private static final Type MOD = Type.getType(Mod.class);
    // Hardcoded list of vanilla classes that should have object holders for each field of the given registry type.
    // IMPORTANT: Updates to this collection must be reflected in ObjectHolderDefinalize. Duplicated cuz classloaders, yay!
    // Classnames are validated below.
    private static final List<VanillaObjectHolderData> VANILLA_OBJECT_HOLDERS = List.of(
            new VanillaObjectHolderData("net.minecraft.class_2246", "block", "net.minecraft.class_2248"), // Block, Blocks
            new VanillaObjectHolderData("net.minecraft.class_1802", "item", "net.minecraft.class_1792"), // Items, Item
            new VanillaObjectHolderData("net.minecraft.class_1893", "enchantment", "net.minecraft.class_1887"), // Enchantments, Enchantment
            new VanillaObjectHolderData("net.minecraft.class_1294", "mob_effect", "net.minecraft.class_1291"), // MobEffects, MobEffect
            new VanillaObjectHolderData("net.minecraft.class_2398", "particle_type", "net.minecraft.class_2396"), // ParticleTypes, ParticleType
            new VanillaObjectHolderData("net.minecraft.class_3417", "sound_event", "net.minecraft.class_3414") // SoundEvents, SoundEvent
    );

    public static void findObjectHolders()
    {
        LOGGER.debug(ForgeRegistry.REGISTRIES,"Processing ObjectHolder annotations");
        final List<ModFileScanData.AnnotationData> annotations = ModList.get().getAllScanData().stream()
            .map(ModFileScanData::getAnnotations)
            .flatMap(Collection::stream)
            .filter(a -> OBJECT_HOLDER.equals(a.annotationType()) || MOD.equals(a.annotationType()))
            .toList();

        Map<Type, String> classModIds = Maps.newHashMap();
        Map<Type, Class<?>> classCache = Maps.newHashMap();

        // Gather all @Mod classes so that @ObjectHolder's in those classes don't need to specify the mod id; modder convenience
        annotations.stream()
                .filter(a -> MOD.equals(a.annotationType()))
                .forEach(data -> classModIds.put(data.clazz(), (String)data.annotationData().get("value")));

        // Validate all the vanilla class-level object holders then scan those first
        VANILLA_OBJECT_HOLDERS.forEach(data -> {
            try
            {
                Class<?> holderClass = Class.forName(data.holderClass(), true, ObjectHolderRegistry.class.getClassLoader());
                Class<?> registryClass = Class.forName(data.registryType(), true, ObjectHolderRegistry.class.getClassLoader());

                Type holderType = Type.getType(holderClass);
                classCache.put(holderType, holderClass);
                scanTarget(classModIds, classCache, holderType, null, registryClass, data.registryName(), "minecraft", true, true);
            }
            catch (ClassNotFoundException e)
            {
                throw new RuntimeException("Vanilla class not found, should not be possible", e);
            }
        });

        // Scan actual fields annotated with @ObjectHolder second
        annotations.stream()
                .filter(a -> OBJECT_HOLDER.equals(a.annotationType())).filter(a -> a.targetType() == ElementType.FIELD)
                .forEach(data -> scanTarget(classModIds, classCache, data.clazz(),
                        data.memberName(), null, (String)data.annotationData().get("registryName"),
                        (String)data.annotationData().get("value"), false, false));

        LOGGER.debug(ForgeRegistry.REGISTRIES,"Found {} ObjectHolder annotations", objectHolders.size());
    }

    private static void scanTarget(Map<Type, String> classModIds, Map<Type, Class<?>> classCache, Type type,
            @Nullable String annotationTarget, @Nullable Class<?> registryClass, String registryName,
            String value, boolean isClass, boolean extractFromValue)
    {
        Class<?> clazz;
        if (classCache.containsKey(type))
        {
            clazz = classCache.get(type);
        }
        else
        {
            try
            {
                clazz = Class.forName(type.getClassName(), extractFromValue, ObjectHolderRegistry.class.getClassLoader());
                classCache.put(type, clazz);
            }
            catch (ClassNotFoundException ex)
            {
                // unpossible?
                throw new RuntimeException(ex);
            }
        }
        if (isClass)
        {
            scanClassForFields(classModIds, type, new ResourceLocation(registryName), registryClass, value, clazz, extractFromValue);
        }
        else
        {
            if (value.indexOf(':') == -1)
            {
                String prefix = classModIds.get(type);
                if (prefix == null)
                {
                    LOGGER.warn(ForgeRegistry.REGISTRIES,"Found an unqualified ObjectHolder annotation ({}) without a modid context at {}.{}, ignoring", value, type, annotationTarget);
                    throw new IllegalStateException("Unqualified reference to ObjectHolder");
                }
                value = prefix + ':' + value;
            }
            try
            {
                Field f = clazz.getDeclaredField(annotationTarget);
                ObjectHolderRef ref = ObjectHolderRef.create(new ResourceLocation(registryName), f, value, extractFromValue);
                if (ref != null)
                    addHandler(ref);
            }
            catch (NoSuchFieldException ex)
            {
                // unpossible?
                throw new RuntimeException(ex);
            }
        }
    }

    private static void scanClassForFields(Map<Type, String> classModIds, Type targetClass,
            ResourceLocation registryName, Class<?> registryClass, String value, Class<?> clazz, boolean extractFromExistingValues)
    {
        classModIds.put(targetClass, value);
        final int flags = Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC | Opcodes.ACC_SYNTHETIC;
        for (Field f : clazz.getFields())
        {
            if (((f.getModifiers() & flags) != flags) || f.isAnnotationPresent(ObjectHolder.class) || !registryClass.isAssignableFrom(f.getType()))
                continue;
            ObjectHolderRef ref = ObjectHolderRef.create(registryName, f, value + ':' + f.getName().toLowerCase(Locale.ENGLISH), extractFromExistingValues);
            if (ref != null)
                addHandler(ref);
        }
    }

    private static ResourceLocation getRegistryName(Map<Type, ResourceLocation> classRegistryNames, @Nullable String registryName,
            Type targetClass, Object declaration)
    {
        if (registryName != null)
            return new ResourceLocation(registryName);

        if (classRegistryNames.containsKey(targetClass))
            return classRegistryNames.get(targetClass);

        throw new IllegalStateException("No registry name was declared for " + declaration);
    }

    public static void applyObjectHolders()
    {
        try
        {
            LOGGER.debug(ForgeRegistry.REGISTRIES, "Applying holder lookups");
            applyObjectHolders(key -> true);
            LOGGER.debug(ForgeRegistry.REGISTRIES, "Holder lookups applied");
        } catch (RuntimeException e)
        {
            // It is more important that the calling contexts continue without exception to prevent further cascading errors
            LOGGER.error("", e);
        }
    }

    public static void applyObjectHolders(Predicate<ResourceLocation> filter)
    {
        RuntimeException aggregate = new RuntimeException("Failed to apply some object holders, see suppressed exceptions for details");
        objectHolders.forEach(objectHolder -> {
            try
            {
                objectHolder.accept(filter);
            }
            catch (Exception e)
            {
                aggregate.addSuppressed(e);
            }
        });

        if (aggregate.getSuppressed().length > 0)
        {
            throw aggregate;
        }
    }

    private record VanillaObjectHolderData(String holderClass, String registryName, String registryType) {
        private static final MappingResolver mappingResolver = FabricLoader.getInstance().getMappingResolver();

        // Kilt: We're using Intermediary class names now, so we need to have a way to remap it in dev.
        @Override
        public String holderClass() {
            return mappingResolver.mapClassName("intermediary", holderClass.replace(".", "/")).replace("/", ".");
        }

        @Override
        public String registryType() {
            return mappingResolver.mapClassName("intermediary", registryType.replace(".", "/")).replace("/", ".");
        }
    }
}
