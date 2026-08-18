package com.i114rk.survivaltools.item;

import com.i114rk.survivaltools.SurvivalTools;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import org.jspecify.annotations.Nullable;

/**
 * Teleports the nearest villager onto the block the player is aiming at, even when that
 * block is far outside the vanilla interaction range. Every teleported villager is marked
 * with the {@link #TELEPORTED_TAG} scoreboard tag so it cannot be moved twice; right
 * clicking a marked villager removes the mark again.
 */
public class VillagerTeleporterItem extends Item {
	public static final String TELEPORTED_TAG = "isTeleported";
	public static final int DURABILITY = 500;

	/** How far the player can aim, deliberately far beyond the vanilla interaction range. */
	public static final double TARGET_RANGE = 128.0;

	/** Radius around the player that is searched for a villager to teleport. */
	public static final double SEARCH_RADIUS = 640.0;

	private static final int DURABILITY_COST = 1;

	public VillagerTeleporterItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		// Only reached when the client found no target inside its interaction range, so the
		// aimed at block or villager has to be looked up again with our own, longer ray.
		HitResult hit = rayTrace(player, TARGET_RANGE);
		if (hit instanceof EntityHitResult entityHit) {
			return clearMark(level, player, entityHit.getEntity());
		}

		if (hit instanceof BlockHitResult blockHit && hit.getType() == HitResult.Type.BLOCK) {
			return teleportNearestVillager(level, player, hand, blockHit.getBlockPos());
		}

		return InteractionResult.PASS;
	}

	/**
	 * Handles a right click on a block that is inside the interaction range. Registered as a
	 * {@code UseBlockCallback} so that the teleporter also works while aiming at blocks with
	 * their own right click behaviour, such as chests.
	 */
	public static InteractionResult onUseBlock(Player player, Level level, InteractionHand hand, BlockHitResult hitResult) {
		if (!isHeld(player, hand)) {
			return InteractionResult.PASS;
		}

		return teleportNearestVillager(level, player, hand, hitResult.getBlockPos());
	}

	/**
	 * Handles a right click on an entity that is inside the interaction range. Registered as a
	 * {@code UseEntityCallback} because it has to run before the villager opens its trading
	 * screen.
	 */
	public static InteractionResult onUseEntity(Player player, Level level, InteractionHand hand, Entity entity) {
		if (!isHeld(player, hand)) {
			return InteractionResult.PASS;
		}

		return clearMark(level, player, entity);
	}

	/**
	 * Ray traces from the player's eyes, preferring a villager over the block behind it.
	 * Used both for the interaction itself and for the particles drawn on the client.
	 */
	public static HitResult rayTrace(Player player, double range) {
		Level level = player.level();
		Vec3 from = player.getEyePosition();
		Vec3 view = player.getViewVector(1.0F);
		Vec3 to = from.add(view.scale(range));

		BlockHitResult blockHit = level.clip(new ClipContext(from, to, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));
		Vec3 limit = blockHit.getType() == HitResult.Type.MISS ? to : blockHit.getLocation();

		AABB searchArea = player.getBoundingBox().expandTowards(view.scale(range)).inflate(1.0);
		EntityHitResult entityHit = ProjectileUtil.getEntityHitResult(
			level, player, from, limit, searchArea, entity -> entity instanceof Villager && entity.isPickable(), 0.0F
		);

		return entityHit != null ? entityHit : blockHit;
	}

	public static boolean isHeld(Player player, InteractionHand hand) {
		return player.getItemInHand(hand).getItem() instanceof VillagerTeleporterItem;
	}

	private static InteractionResult teleportNearestVillager(Level level, Player player, InteractionHand hand, BlockPos clickedPos) {
		if (level.isClientSide()) {
			return InteractionResult.SUCCESS;
		}

		ServerLevel serverLevel = (ServerLevel)level;
		Villager villager = findNearestUnmarkedVillager(serverLevel, player);
		if (villager == null) {
			sendStatus(player, "no_villager");
			return InteractionResult.SUCCESS;
		}

		BlockPos destination = clickedPos.above();
		Vec3 target = new Vec3(destination.getX() + 0.5, destination.getY(), destination.getZ() + 0.5);

		playTeleportEffects(serverLevel, villager.position());
		villager.teleportTo(target.x, target.y, target.z);
		villager.addTag(TELEPORTED_TAG);
		playTeleportEffects(serverLevel, target);

		player.getItemInHand(hand).hurtAndBreak(DURABILITY_COST, player, hand);
		return InteractionResult.SUCCESS;
	}

	private static InteractionResult clearMark(Level level, Player player, Entity entity) {
		if (!(entity instanceof Villager villager)) {
			return InteractionResult.PASS;
		}

		if (level.isClientSide()) {
			// Scoreboard tags are never sent to the client, so the click is always consumed
			// there to keep the trading screen from opening.
			return InteractionResult.SUCCESS;
		}

		if (!villager.removeTag(TELEPORTED_TAG)) {
			sendStatus(player, "not_marked");
			return InteractionResult.SUCCESS;
		}

		ServerLevel serverLevel = (ServerLevel)level;
		Vec3 position = villager.position();
		serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER, position.x, position.y + 1.0, position.z, 12, 0.4, 0.6, 0.4, 0.0);
		serverLevel.playSound(null, position.x, position.y, position.z, SoundEvents.VILLAGER_YES, SoundSource.NEUTRAL, 1.0F, 1.0F);
		sendStatus(player, "mark_cleared");
		return InteractionResult.SUCCESS;
	}

	private static @Nullable Villager findNearestUnmarkedVillager(ServerLevel level, Player player) {
		AABB searchArea = AABB.ofSize(player.position(), SEARCH_RADIUS * 2.0, SEARCH_RADIUS * 2.0, SEARCH_RADIUS * 2.0);
		Villager nearest = null;
		double nearestDistance = Double.MAX_VALUE;

		for (Villager villager : level.getEntitiesOfClass(
			Villager.class, searchArea, candidate -> candidate.isAlive() && !candidate.entityTags().contains(TELEPORTED_TAG)
		)) {
			double distance = villager.distanceToSqr(player);
			if (distance < nearestDistance) {
				nearestDistance = distance;
				nearest = villager;
			}
		}

		return nearest;
	}

	private static void playTeleportEffects(ServerLevel level, Vec3 position) {
		level.sendParticles(ParticleTypes.FLAME, true, false, position.x, position.y + 1.0, position.z, 24, 0.3, 0.6, 0.3, 0.02);
		level.playSound(null, position.x, position.y, position.z, SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);
	}

	private static void sendStatus(Player player, String key) {
		if (player instanceof ServerPlayer serverPlayer) {
			serverPlayer.sendSystemMessage(Component.translatable("item." + SurvivalTools.MOD_ID + ".villager_teleporter." + key), true);
		}
	}
}
