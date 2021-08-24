package com.austinmreppert.graphio.block;

import com.austinmreppert.graphio.blockentity.BlockEntityTypes;
import com.austinmreppert.graphio.blockentity.RouterBlockEntity;
import com.austinmreppert.graphio.data.mappings.Mapping;
import com.austinmreppert.graphio.data.tiers.BaseTier;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.fmllegacy.network.NetworkHooks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

public class RouterBlock extends BaseEntityBlock {

  private BaseTier baseTier;

  private RouterBlock() {
    super(Properties.of(Material.HEAVY_METAL).lightLevel((bs) -> 0).strength(2));
  }

  public RouterBlock(final BaseTier baseTier) {
    this();
    this.baseTier = baseTier;
  }

  @Override
  @Nonnull
  @ParametersAreNonnullByDefault
  public RenderShape getRenderShape(final BlockState state) {
    return RenderShape.MODEL;
  }

  /**
   * Creates a block entity ticker for the router.
   *
   * @param level           The level of the router.
   * @param state           The block state of the router.
   * @param blockEntityType The type of the block entity.
   * @param <T>             _.
   * @return A ticker for the router block entity or null for the client.
   */
  @Nullable
  @Override
  @ParametersAreNonnullByDefault
  public <T extends BlockEntity> BlockEntityTicker<T> getTicker(final Level level, final BlockState state, final BlockEntityType<T> blockEntityType) {
    return level.isClientSide() ? null : createTickerHelper(blockEntityType, BlockEntityTypes.ROUTER, (final Level routerLevel, final BlockPos blockPos, final BlockState blockState, final RouterBlockEntity routerBlockEntity) -> {
      routerBlockEntity.serverTick();
    });
  }

  /**
   * Opens the appropriate router gui.
   *
   * @param state  The block state.
   * @param level  The block's level.
   * @param pos    The position of the block.
   * @param player The player who used the block.
   * @param handIn The hand that was sued.
   * @param hit    The type of hit.
   * @return The event status.
   */
  @Override
  @Nonnull
  @ParametersAreNonnullByDefault
  public InteractionResult use(final BlockState state, final Level level, final BlockPos pos, final Player player,
                               final InteractionHand handIn, final BlockHitResult hit) {
    if (!level.isClientSide) {
      final BlockEntity blockEntity = level.getBlockEntity(pos);
      if (blockEntity instanceof RouterBlockEntity router) {
        if (player.isCrouching())
          NetworkHooks.openGui((ServerPlayer) player, (MenuProvider) blockEntity, pos);
        else
          NetworkHooks.openGui((ServerPlayer) player, (MenuProvider) blockEntity, (packetBuffer) -> {
            packetBuffer.writeBlockPos(pos);
          });
      }
      return InteractionResult.CONSUME;
    }
    return InteractionResult.SUCCESS;
  }

  /**
   * Creates the {@link RouterBlockEntity}.
   *
   * @param pos   The {@link BlockPos} of the router.
   * @param state The {@link BlockState} of the router.
   * @return A {@link RouterBlockEntity} for the router block.
   */
  @Override
  @ParametersAreNonnullByDefault
  public BlockEntity newBlockEntity(final BlockPos pos, final BlockState state) {
    return new RouterBlockEntity(baseTier, pos, state);
  }

}
