package xyz.austinmreppert.graph_io.block;

import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.level.Level;
import net.minecraftforge.fmllegacy.network.NetworkHooks;
import xyz.austinmreppert.graph_io.data.mappings.Mapping;
import xyz.austinmreppert.graph_io.data.tiers.BaseTier;
import xyz.austinmreppert.graph_io.blockentity.RouterBlockEntity;
import xyz.austinmreppert.graph_io.blockentity.BlockEntityTypes;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

public class RouterBlock extends BaseEntityBlock {

  private BaseTier baseTier;

  private RouterBlock() {
    super(Properties.of(Material.HEAVY_METAL).lightLevel((bs) -> 15));
  }

  public RouterBlock(BaseTier baseTier) {
    this();
    this.baseTier = baseTier;
  }

  @Override
  @Nonnull
  @ParametersAreNonnullByDefault
  public RenderShape getRenderShape(BlockState state) {
    return RenderShape.MODEL;
  }

  @Override
  @ParametersAreNonnullByDefault
  public void setPlacedBy(Level worldIn, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
    super.setPlacedBy(worldIn, pos, state, placer, stack);
  }

  @Nullable
  @Override
  public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
    return level.isClientSide() ? null : createTickerHelper(blockEntityType, BlockEntityTypes.ROUTER, (Level routerLevel, BlockPos blockPos, BlockState blockState, RouterBlockEntity routerBlockEntity) -> {
      routerBlockEntity.serverTick(blockPos);
    });
  }

  @Override
  @Nonnull
  @ParametersAreNonnullByDefault
  public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn, BlockHitResult hit) {
    if (!worldIn.isClientSide) {
      final BlockEntity blockEntity = worldIn.getBlockEntity(pos);
      if (blockEntity instanceof RouterBlockEntity) {
        RouterBlockEntity router = (RouterBlockEntity) blockEntity;
        if (player.isCrouching())
          NetworkHooks.openGui((ServerPlayer) player, (MenuProvider) blockEntity, pos);
        else
          NetworkHooks.openGui((ServerPlayer) player, (MenuProvider) blockEntity, (packetBuffer) -> {
            packetBuffer.writeBlockPos(pos);
            packetBuffer.writeNbt(Mapping.write(router.getMappings()));
          });
      }
    }
    return InteractionResult.SUCCESS;
  }

  @Nullable
  @Override
  public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
    return new RouterBlockEntity(baseTier, pos, state);
  }

}
