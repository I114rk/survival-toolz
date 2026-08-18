package com.i114rk.survivaltools.compat.jade;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import com.i114rk.survivaltools.SurvivalTools;
import com.i114rk.survivaltools.item.FullHeartItem;

import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.Container;
import net.minecraft.world.RandomizableContainer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.ContainerEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.TypedEntityData;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;

import org.jspecify.annotations.Nullable;

import snownee.jade.api.Accessor;
import snownee.jade.api.view.IServerExtensionProvider;
import snownee.jade.api.view.ItemViewUtils;
import snownee.jade.api.view.ViewGroup;

/**
 * Takes over Jade's item storage view for containers that hold a full heart, so that
 * {@link StoredMobItemStorageClient} can draw the stored mob next to it.
 *
 * <p>Jade tries the registered item storage providers in priority order and uses the first one that
 * returns a list. This provider runs before Jade's own one (priority {@code 0} against {@code 9999})
 * and returns {@code null} for everything it has nothing to add to, which hands those containers
 * straight back to Jade.
 */
public final class StoredMobItemStorage implements IServerExtensionProvider<ItemStack> {
	public static final StoredMobItemStorage INSTANCE = new StoredMobItemStorage();
	static final Identifier UID = SurvivalTools.id("stored_mob");

	// Typed explicitly: ItemViewUtils.groupOf is overloaded for containers and platform storages.
	private static final Function<Accessor<?>, Container> CONTAINER_FINDER = StoredMobItemStorage::findContainer;

	private StoredMobItemStorage() {
	}

	@Override
	public Identifier getUid() {
		return UID;
	}

	@Override
	public boolean shouldRequestData(Accessor<?> accessor) {
		return accessor.getTarget() instanceof Container;
	}

	@Override
	public @Nullable List<ViewGroup<ItemStack>> getGroups(Accessor<?> accessor) {
		if (!(accessor.getTarget() instanceof Container target)) {
			return null;
		}

		Container contents = findContainer(accessor);
		if (contents == null || !mayLookInside(target, accessor.getPlayer()) || !holdsStoredMob(contents)) {
			return null;
		}

		// The target is the cache key of Jade's item collector, the finder is what it reads from.
		return ItemViewUtils.groupOf(target, accessor, CONTAINER_FINDER);
	}

	/** The entity stored in the stack, or {@code null} if it is not a filled heart. */
	static @Nullable EntityType<?> storedMobOf(ItemStack stack) {
		if (!(stack.getItem() instanceof FullHeartItem)) {
			return null;
		}

		TypedEntityData<EntityType<?>> stored = stack.get(DataComponents.ENTITY_DATA);
		return stored == null ? null : stored.type();
	}

	/**
	 * Resolves what a container really shows: both halves of a double chest, the container itself
	 * otherwise. Mirrors what Jade's own provider does.
	 */
	private static @Nullable Container findContainer(Accessor<?> accessor) {
		if (!(accessor.getTarget() instanceof Container container)) {
			return null;
		}

		if (container instanceof ChestBlockEntity chest && chest.getBlockState().getBlock() instanceof ChestBlock chestBlock) {
			Container compound = ChestBlock.getContainer(
				chestBlock,
				chest.getBlockState(),
				Objects.requireNonNull(chest.getLevel()),
				chest.getBlockPos(),
				true
			);
			if (compound != null) {
				return compound;
			}
		}

		return container;
	}

	/** Keeps loot chests and locked containers as secret as they are without this mod. */
	private static boolean mayLookInside(Container container, Player player) {
		if (container instanceof RandomizableContainer randomizable && randomizable.getLootTable() != null) {
			return false;
		}

		if (container instanceof ContainerEntity containerEntity && containerEntity.getContainerLootTable() != null) {
			return false;
		}

		if (player.isCreative() || player.isSpectator()) {
			return true;
		}

		return !(container instanceof BaseContainerBlockEntity lockable) || lockable.canOpen(player);
	}

	private static boolean holdsStoredMob(Container container) {
		for (int slot = 0; slot < container.getContainerSize(); slot++) {
			if (storedMobOf(container.getItem(slot)) != null) {
				return true;
			}
		}

		return false;
	}
}
