/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.crafting;

import java.util.List;
import java.util.stream.Stream;

import net.neoforged.neoforge.registries.NeoForgeRegistries;
import xyz.bluspring.kilt.workarounds.resource.FabricCustomIngredientSerializer;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredient;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredientSerializer;

/**
 * Interface that modders can implement to create new behaviors for {@link Ingredient}s.
 *
 * <p>This is not directly implemented on vanilla {@link Ingredient}s, but conversions are possible:
 * <ul>
 * <li>{@link #toVanilla()} converts a custom ingredient to a vanilla {@link Ingredient}.</li>
 * <li>{@link Ingredient#getCustomIngredient()} retrieves the custom ingredient inside a vanilla {@link Ingredient}.</li>
 * </ul>
 *
 * <p>Implementations of this interface <b>must implement {@link Object#equals} and {@link Object#hashCode}</b>
 * to ensure that the ingredient also supports them.
 */
public interface ICustomIngredient extends CustomIngredient { // Kilt: Attach to Fabric API's CustomIngredient system
    /**
     * Checks if a stack matches this ingredient.
     * The stack <b>must not</b> be modified in any way.
     *
     * @param stack the stack to test
     * @return {@code true} if the stack matches this ingredient, {@code false} otherwise
     */
    boolean test(ItemStack stack);

    /**
     * {@return the list of stacks that this ingredient accepts}
     *
     * <p>The following guidelines should be followed for good compatibility:
     * <ul>
     * <li>These stacks are generally used for display purposes, and need not be exhaustive or perfectly accurate.</li>
     * <li>An exception is ingredients that {@linkplain #isSimple() are simple},
     * for which it is important that the returned stacks correspond exactly to all the accepted {@link Item}s.</li>
     * <li>At least one stack must be returned for the ingredient not to be considered {@linkplain Ingredient#hasNoItems() accidentally empty}.</li>
     * <li>The ingredient should try to return at least one stack with each accepted {@link Item}.
     * This allows mods that inspect the ingredient to figure out which stacks it might accept.</li>
     * </ul>
     *
     * <p>Note: no caching needs to be done by the implementation, this is already handled by the ingredient itself.
     */
    Stream<ItemStack> getItems();

    @Override
    default List<ItemStack> getMatchingStacks() { // Kilt: Handle Fabric's implementation of getItems
        return this.getItems().toList();
    }

    /**
     * Returns whether this ingredient always requires {@linkplain #test direct stack testing}.
     *
     * @return {@code true} if this ingredient ignores NBT data when matching stacks, {@code false} otherwise
     * @see Ingredient#isSimple()
     */
    boolean isSimple();

    // Kilt: Handle Fabric's implementation of isSimple.
    @Override
    default boolean requiresTesting() {
        return this.isSimple();
    }

    /**
     * {@return the type of this ingredient}
     *
     * <p>The type must be registered to {@link NeoForgeRegistries#INGREDIENT_TYPES}.
     */
    IngredientType<?> getType();

    // Kilt: Handle Fabric API's serialization system.
    @Override
    default CustomIngredientSerializer<?> getSerializer() {
        return FabricCustomIngredientSerializer.getOrCreate(this.getType());
    }

    /**
     * {@return a new {@link Ingredient} behaving as defined by this custom ingredient}
     */
    /* // Kilt: Handled by Fabric API
    @ApiStatus.NonExtendable
    default Ingredient toVanilla() {
        return IngredientInjection.create(this);
    }
     */
}
