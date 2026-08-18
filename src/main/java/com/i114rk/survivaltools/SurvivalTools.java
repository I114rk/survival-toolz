package com.i114rk.survivaltools;

import com.i114rk.survivaltools.item.EmptyHeartItem;
import com.i114rk.survivaltools.item.ModItems;
import com.i114rk.survivaltools.item.VillagerTeleporterItem;
import com.i114rk.survivaltools.villager.VillagerPinning;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;

import net.minecraft.resources.Identifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SurvivalTools implements ModInitializer {
	public static final String MOD_ID = "survival-tools";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		ModItems.initialize();

		// All of these callbacks run before the vanilla interaction, which is what lets them take
		// priority over a villager's trading screen and over blocks with their own use behaviour.
		// Targets outside the interaction range never reach these callbacks and are handled by
		// VillagerTeleporterItem#use instead.
		//
		// Pinning is registered first so that crouching wins over the teleporter's mark removal,
		// which reacts to a plain right click.
		UseEntityCallback.EVENT.register((player, level, hand, entity, hitResult) -> VillagerPinning.onUseEntity(player, level, hand, entity));
		UseEntityCallback.EVENT.register((player, level, hand, entity, hitResult) -> EmptyHeartItem.onUseEntity(player, level, hand, entity));
		UseEntityCallback.EVENT.register((player, level, hand, entity, hitResult) -> VillagerTeleporterItem.onUseEntity(player, level, hand, entity));
		UseBlockCallback.EVENT.register(VillagerTeleporterItem::onUseBlock);
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
