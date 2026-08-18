package com.i114rk.survivaltools.item;

import java.util.function.Function;

import com.i114rk.survivaltools.SurvivalTools;

import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;

public final class ModItems {
	public static final Item VILLAGER_TELEPORTER = register(
		"villager_teleporter", VillagerTeleporterItem::new, new Item.Properties().durability(VillagerTeleporterItem.DURABILITY)
	);

	/** Crafting material for the villager spawner. */
	public static final Item VILLAGER_HEART = register("villager_heart", Item::new, new Item.Properties());

	public static final Item VILLAGER_SPAWNER = register(
		"villager_spawner", VillagerSpawnerItem::new, new Item.Properties().durability(VillagerSpawnerItem.DURABILITY)
	);

	/** Stores a single mob when used on it, turning into {@link #FULL_HEART}. */
	public static final Item EMPTY_HEART = register("empty_heart", EmptyHeartItem::new, new Item.Properties());

	public static final Item FULL_HEART = register("full_heart", FullHeartItem::new, new Item.Properties().stacksTo(1));

	private ModItems() {
	}

	/** Loads this class and adds its items to the creative inventory. */
	public static void initialize() {
		CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.TOOLS_AND_UTILITIES).register(output -> {
			output.accept(VILLAGER_TELEPORTER);
			output.accept(VILLAGER_HEART);
			output.accept(VILLAGER_SPAWNER);
			output.accept(EMPTY_HEART);
			output.accept(FULL_HEART);
		});
	}

	private static Item register(String path, Function<Item.Properties, Item> itemFactory, Item.Properties properties) {
		ResourceKey<Item> id = ResourceKey.create(Registries.ITEM, SurvivalTools.id(path));
		return Registry.register(BuiltInRegistries.ITEM, id, itemFactory.apply(properties.setId(id)));
	}
}
