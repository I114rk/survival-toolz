package com.i114rk.survivaltools.compat.jade;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;

import snownee.jade.api.Accessor;
import snownee.jade.api.view.ClientViewGroup;
import snownee.jade.api.view.IClientExtensionProvider;
import snownee.jade.api.view.ItemView;
import snownee.jade.api.view.ViewGroup;

/**
 * Renders the item storage groups collected by {@link StoredMobItemStorage}, adding the spawn egg of
 * the stored mob right behind every full heart. Mobs without a spawn egg are only named by the
 * heart itself, which already carries the mob's name.
 */
public final class StoredMobItemStorageClient implements IClientExtensionProvider<ItemStack, ItemView> {
	public static final StoredMobItemStorageClient INSTANCE = new StoredMobItemStorageClient();

	private StoredMobItemStorageClient() {
	}

	@Override
	public Identifier getUid() {
		return StoredMobItemStorage.UID;
	}

	@Override
	public List<ClientViewGroup<ItemView>> getClientGroups(Accessor<?> accessor, List<ViewGroup<ItemStack>> groups) {
		// The groups were just decoded for this frame, so extending them in place is safe and keeps
		// Jade in charge of the group titles and progress bars.
		for (ViewGroup<ItemStack> group : groups) {
			group.views = withSpawnEggs(group.views);
		}

		return ClientViewGroup.map(groups, ItemView::new, null);
	}

	private static List<ItemStack> withSpawnEggs(List<ItemStack> stacks) {
		List<ItemStack> expanded = new ArrayList<>(stacks.size() + 1);
		for (ItemStack stack : stacks) {
			expanded.add(stack);

			EntityType<?> stored = StoredMobItemStorage.storedMobOf(stack);
			if (stored != null) {
				SpawnEggItem.byId(stored).ifPresent(egg -> expanded.add(new ItemStack(egg)));
			}
		}

		return expanded;
	}
}
