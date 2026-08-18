package com.i114rk.survivaltools.item;

import com.i114rk.survivaltools.SurvivalTools;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Soul collector for mobs: right clicking any living entity stores it inside the heart, which
 * turns into a {@link FullHeartItem}. One heart holds exactly one mob.
 */
public class EmptyHeartItem extends Item {
	public EmptyHeartItem(Properties properties) {
		super(properties);
	}

	/**
	 * Handles a right click on a mob. Registered as a {@code UseEntityCallback} because it has to
	 * run before the mob's own interaction, such as a villager opening its trading screen.
	 */
	public static InteractionResult onUseEntity(Player player, Level level, InteractionHand hand, Entity entity) {
		ItemStack stack = player.getItemInHand(hand);
		if (!(stack.getItem() instanceof EmptyHeartItem)) {
			return InteractionResult.PASS;
		}

		if (!(entity instanceof LivingEntity living) || entity instanceof Player) {
			return InteractionResult.PASS;
		}

		if (level.isClientSide()) {
			return InteractionResult.SUCCESS;
		}

		ServerLevel serverLevel = (ServerLevel)level;
		ItemStack filled = FullHeartItem.createFrom(living);
		playCaptureEffects(serverLevel, living);
		living.discard();

		stack.shrink(1);
		if (stack.isEmpty()) {
			player.setItemInHand(hand, filled);
		} else if (!player.getInventory().add(filled)) {
			player.drop(filled, false);
		}

		sendStatus(player, "captured");
		return InteractionResult.SUCCESS;
	}

	private static void playCaptureEffects(ServerLevel level, LivingEntity entity) {
		level.sendParticles(
			ParticleTypes.HEART,
			entity.getX(), entity.getY() + entity.getBbHeight() * 0.5, entity.getZ(),
			8, 0.4, 0.4, 0.4, 0.0
		);
		level.playSound(
			null, entity.getX(), entity.getY(), entity.getZ(),
			SoundEvents.BEACON_DEACTIVATE, SoundSource.PLAYERS, 0.5F, 1.6F
		);
	}

	private static void sendStatus(Player player, String key) {
		if (player instanceof ServerPlayer serverPlayer) {
			serverPlayer.sendSystemMessage(Component.translatable("item." + SurvivalTools.MOD_ID + ".empty_heart." + key), true);
		}
	}
}
