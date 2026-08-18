package com.i114rk.survivaltools.item;

import com.i114rk.survivaltools.SurvivalTools;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.TypedEntityData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.TagValueOutput;

import org.jspecify.annotations.Nullable;

/**
 * An {@link EmptyHeartItem} that has swallowed a mob. The whole entity is stored in the vanilla
 * {@code minecraft:entity_data} component, so everything about it survives: a sheep keeps its
 * colour, a zombie keeps its diamond armour, and a tamed pet keeps its owner.
 *
 * <p>Right clicking a block releases the mob again and turns the heart back into an empty one.
 * The stored mob's custom name is mirrored onto the item, which also means renaming the heart in
 * an anvil renames the mob it releases.
 */
public class FullHeartItem extends Item {
	public FullHeartItem(Properties properties) {
		super(properties);
	}

	/** Packs a living entity into a new full heart. The entity itself is not removed here. */
	public static ItemStack createFrom(LivingEntity entity) {
		ItemStack stack = new ItemStack(ModItems.FULL_HEART);

		try (ProblemReporter.ScopedCollector reporter = new ProblemReporter.ScopedCollector(entity.problemPath(), SurvivalTools.LOGGER)) {
			TagValueOutput output = TagValueOutput.createWithContext(reporter, entity.registryAccess());
			entity.saveWithoutId(output);
			CompoundTag tag = output.buildResult();

			// The mob is released wherever the heart is used, so nothing about its old placement
			// may travel with it. The UUID is dropped so a released mob never clashes with itself.
			tag.remove("Pos");
			tag.remove("Motion");
			tag.remove("UUID");
			tag.remove("Passengers");
			tag.remove("leash");
			stack.set(DataComponents.ENTITY_DATA, TypedEntityData.of(entity.getType(), tag));
		}

		if (entity.hasCustomName()) {
			stack.set(DataComponents.CUSTOM_NAME, entity.getCustomName());
		}

		// The default name tells the player what is inside; a custom name still takes precedence.
		stack.set(
			DataComponents.ITEM_NAME,
			Component.translatable("item." + SurvivalTools.MOD_ID + ".full_heart.stored", entity.getType().getDescription())
		);
		return stack;
	}

	@Override
	public InteractionResult useOn(UseOnContext context) {
		Player player = context.getPlayer();
		if (player == null) {
			return InteractionResult.PASS;
		}

		ItemStack stack = context.getItemInHand();
		TypedEntityData<EntityType<?>> stored = stack.get(DataComponents.ENTITY_DATA);
		if (stored == null) {
			sendStatus(player, "empty");
			return InteractionResult.FAIL;
		}

		Level level = context.getLevel();
		if (level.isClientSide()) {
			return InteractionResult.SUCCESS;
		}

		ServerLevel serverLevel = (ServerLevel)level;
		BlockPos spawnPos = VillagerSpawnerItem.resolveSpawnPos(context);
		Entity entity = release(serverLevel, stored, stack, spawnPos);
		if (entity == null) {
			sendStatus(player, "release_failed");
			return InteractionResult.FAIL;
		}

		serverLevel.gameEvent(player, GameEvent.ENTITY_PLACE, spawnPos);
		playEffects(serverLevel, entity);
		player.setItemInHand(context.getHand(), new ItemStack(ModItems.EMPTY_HEART));
		return InteractionResult.SUCCESS;
	}

	private static @Nullable Entity release(ServerLevel level, TypedEntityData<EntityType<?>> stored, ItemStack stack, BlockPos spawnPos) {
		Entity entity = stored.type().create(level, EntitySpawnReason.SPAWN_ITEM_USE);
		if (entity == null) {
			return null;
		}

		stored.loadInto(entity);
		// Renaming the heart in an anvil renames the mob inside it.
		Component customName = stack.get(DataComponents.CUSTOM_NAME);
		if (customName != null) {
			entity.setCustomName(customName);
		}

		entity.snapTo(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5, entity.getYRot(), entity.getXRot());
		return level.addFreshEntity(entity) ? entity : null;
	}

	private static void playEffects(ServerLevel level, Entity entity) {
		level.sendParticles(ParticleTypes.HEART, entity.getX(), entity.getY() + entity.getBbHeight() * 0.5, entity.getZ(), 8, 0.4, 0.4, 0.4, 0.0);
		level.playSound(null, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 0.5F, 1.6F);
	}

	private static void sendStatus(Player player, String key) {
		if (player instanceof ServerPlayer serverPlayer) {
			serverPlayer.sendSystemMessage(Component.translatable("item." + SurvivalTools.MOD_ID + ".full_heart." + key), true);
		}
	}
}
