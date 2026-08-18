package com.i114rk.survivaltools.item;

import com.i114rk.survivaltools.SurvivalTools;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;

/**
 * Spawns a villager on the block the player right clicks. Every spawn costs one of the
 * {@link #DURABILITY} uses and puts the item on a {@link #COOLDOWN_TICKS} long cooldown, which
 * vanilla itself enforces for both {@code use} and {@code useOn}.
 */
public class VillagerSpawnerItem extends Item {
	/** How many villagers a single spawner can create before it breaks. */
	public static final int DURABILITY = 32;

	/** 64 seconds between two spawns. */
	public static final int COOLDOWN_TICKS = 64 * 20;

	private static final int DURABILITY_COST = 1;

	public VillagerSpawnerItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult useOn(UseOnContext context) {
		Level level = context.getLevel();
		Player player = context.getPlayer();
		if (player == null) {
			return InteractionResult.PASS;
		}

		if (level.isClientSide()) {
			return InteractionResult.SUCCESS;
		}

		ServerLevel serverLevel = (ServerLevel)level;
		BlockPos spawnPos = resolveSpawnPos(context);
		boolean movedUp = !spawnPos.equals(context.getClickedPos()) && context.getClickedFace() == Direction.UP;

		Villager villager = EntityTypes.VILLAGER.spawn(
			serverLevel, null, player, spawnPos, EntitySpawnReason.SPAWN_ITEM_USE, true, movedUp
		);
		if (villager == null) {
			sendStatus(player, "no_space");
			return InteractionResult.FAIL;
		}

		serverLevel.gameEvent(player, GameEvent.ENTITY_PLACE, spawnPos);
		playSpawnEffects(serverLevel, villager);

		ItemStack stack = context.getItemInHand();
		// The cooldown is added first: hurtAndBreak can empty the stack, and an empty stack would
		// put the cooldown on air instead of on this item.
		player.getCooldowns().addCooldown(stack, COOLDOWN_TICKS);
		stack.hurtAndBreak(DURABILITY_COST, player, context.getHand());
		return InteractionResult.SUCCESS;
	}

	/** Places the villager inside the clicked block when it has no collision, next to it otherwise. */
	static BlockPos resolveSpawnPos(UseOnContext context) {
		BlockPos clickedPos = context.getClickedPos();
		BlockState state = context.getLevel().getBlockState(clickedPos);
		if (state.getCollisionShape(context.getLevel(), clickedPos).isEmpty()) {
			return clickedPos;
		}

		return clickedPos.relative(context.getClickedFace());
	}

	private static void playSpawnEffects(ServerLevel level, Villager villager) {
		level.sendParticles(
			ParticleTypes.HAPPY_VILLAGER,
			villager.getX(), villager.getY() + 1.0, villager.getZ(),
			16, 0.4, 0.6, 0.4, 0.0
		);
		level.playSound(
			null, villager.getX(), villager.getY(), villager.getZ(),
			SoundEvents.VILLAGER_CELEBRATE, SoundSource.NEUTRAL, 1.0F, 1.0F
		);
	}

	private static void sendStatus(Player player, String key) {
		if (player instanceof ServerPlayer serverPlayer) {
			serverPlayer.sendSystemMessage(Component.translatable("item." + SurvivalTools.MOD_ID + ".villager_spawner." + key), true);
		}
	}
}
