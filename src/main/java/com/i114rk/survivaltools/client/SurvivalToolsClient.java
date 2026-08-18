package com.i114rk.survivaltools.client;

import com.i114rk.survivaltools.item.VillagerTeleporterItem;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

/**
 * Draws a ring of flame particles above the block the player is aiming at while holding the
 * villager teleporter, so the destination is visible even far outside the interaction range.
 */
public class SurvivalToolsClient implements ClientModInitializer {
	private static final int PARTICLES_PER_TICK = 3;
	private static final double RING_RADIUS = 0.32;
	private static final double RING_HEIGHT = 0.15;
	private static final double ROTATION_SPEED = 0.25;

	@Override
	public void onInitializeClient() {
		ClientTickEvents.END_CLIENT_TICK.register(SurvivalToolsClient::drawTargetMarker);
	}

	private static void drawTargetMarker(Minecraft client) {
		LocalPlayer player = client.player;
		ClientLevel level = client.level;
		if (player == null || level == null || client.isPaused()) {
			return;
		}

		if (!VillagerTeleporterItem.isHeld(player, InteractionHand.MAIN_HAND) && !VillagerTeleporterItem.isHeld(player, InteractionHand.OFF_HAND)) {
			return;
		}

		HitResult hit = VillagerTeleporterItem.rayTrace(player, VillagerTeleporterItem.TARGET_RANGE);
		if (!(hit instanceof BlockHitResult blockHit) || hit.getType() != HitResult.Type.BLOCK) {
			return;
		}

		BlockPos destination = blockHit.getBlockPos().above();
		double centerX = destination.getX() + 0.5;
		double centerY = destination.getY() + RING_HEIGHT;
		double centerZ = destination.getZ() + 0.5;

		for (int i = 0; i < PARTICLES_PER_TICK; i++) {
			double angle = player.tickCount * ROTATION_SPEED + i * (Math.PI * 2.0 / PARTICLES_PER_TICK);
			level.addParticle(
				ParticleTypes.FLAME, centerX + Math.cos(angle) * RING_RADIUS, centerY, centerZ + Math.sin(angle) * RING_RADIUS, 0.0, 0.01, 0.0
			);
		}
	}
}
