package com.i114rk.survivaltools.villager;

import com.i114rk.survivaltools.SurvivalTools;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Pins a villager in place when the player crouches and right clicks it. A pinned villager keeps
 * its whole AI, so it still looks around, turns towards the player and trades, but its movement
 * speed is scaled to zero, which means pathfinding can no longer carry it anywhere. Crouch and
 * right click it again to release it.
 *
 * <p>{@link net.minecraft.world.entity.Mob#setNoAi(boolean)} is deliberately not used: it would
 * also freeze the head and body rotation.
 */
public final class VillagerPinning {
	public static final String PINNED_TAG = "isPinned";

	/** Scales the movement speed to exactly zero, and is saved with the entity. */
	private static final Identifier PINNED_MODIFIER_ID = SurvivalTools.id("pinned");
	private static final AttributeModifier PINNED_MODIFIER = new AttributeModifier(
		PINNED_MODIFIER_ID, -1.0, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
	);

	/** Keeps a pinned villager from hopping in place when its path wants it to climb a block. */
	private static final Identifier NO_JUMP_MODIFIER_ID = SurvivalTools.id("pinned_no_jump");
	private static final AttributeModifier NO_JUMP_MODIFIER = new AttributeModifier(
		NO_JUMP_MODIFIER_ID, -1.0, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
	);

	private VillagerPinning() {
	}

	/**
	 * Handles a crouching right click on a villager. Registered as a {@code UseEntityCallback}
	 * because it has to run before the villager opens its trading screen.
	 */
	public static InteractionResult onUseEntity(Player player, Level level, InteractionHand hand, Entity entity) {
		if (hand != InteractionHand.MAIN_HAND || !player.isShiftKeyDown() || !(entity instanceof Villager villager)) {
			return InteractionResult.PASS;
		}

		if (level.isClientSide()) {
			// The pinned state lives in a scoreboard tag, which is never sent to the client, so the
			// click is always consumed there to keep the trading screen from opening.
			return InteractionResult.SUCCESS;
		}

		boolean pinned = !isPinned(villager);
		setPinned(villager, pinned);
		playFeedback((ServerLevel)level, villager, pinned);
		sendStatus(player, pinned ? "pinned" : "unpinned");
		return InteractionResult.SUCCESS;
	}

	public static boolean isPinned(Villager villager) {
		return villager.entityTags().contains(PINNED_TAG);
	}

	public static void setPinned(Villager villager, boolean pinned) {
		AttributeInstance speed = villager.getAttribute(Attributes.MOVEMENT_SPEED);
		AttributeInstance jumpStrength = villager.getAttribute(Attributes.JUMP_STRENGTH);
		if (speed == null) {
			return;
		}

		if (pinned) {
			villager.addTag(PINNED_TAG);
			speed.addOrReplacePermanentModifier(PINNED_MODIFIER);
			if (jumpStrength != null) {
				jumpStrength.addOrReplacePermanentModifier(NO_JUMP_MODIFIER);
			}

			// Drops the path the villager was walking along, otherwise it keeps the stale target.
			villager.getNavigation().stop();
			villager.setDeltaMovement(Vec3.ZERO);
		} else {
			villager.removeTag(PINNED_TAG);
			speed.removeModifier(PINNED_MODIFIER_ID);
			if (jumpStrength != null) {
				jumpStrength.removeModifier(NO_JUMP_MODIFIER_ID);
			}
		}
	}

	private static void playFeedback(ServerLevel level, Villager villager, boolean pinned) {
		Vec3 position = villager.position();
		level.sendParticles(
			pinned ? ParticleTypes.ENCHANT : ParticleTypes.HAPPY_VILLAGER,
			position.x, position.y + 1.0, position.z, 12, 0.4, 0.6, 0.4, 0.0
		);
		level.playSound(
			null, position.x, position.y, position.z,
			pinned ? SoundEvents.IRON_TRAPDOOR_CLOSE : SoundEvents.IRON_TRAPDOOR_OPEN,
			SoundSource.NEUTRAL, 0.8F, 1.4F
		);
	}

	private static void sendStatus(Player player, String key) {
		if (player instanceof ServerPlayer serverPlayer) {
			serverPlayer.sendSystemMessage(Component.translatable("message." + SurvivalTools.MOD_ID + ".villager_" + key), true);
		}
	}
}
