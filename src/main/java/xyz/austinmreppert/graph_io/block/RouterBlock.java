package xyz.austinmreppert.graph_io.block;

import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.ContainerBlock;
import net.minecraft.block.material.Material;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;
import xyz.austinmreppert.graph_io.data.mappings.Mapping;
import xyz.austinmreppert.graph_io.data.tiers.BaseTier;
import xyz.austinmreppert.graph_io.tileentity.RouterTE;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

public class RouterBlock extends ContainerBlock {

  private BaseTier baseTier;

  private RouterBlock() {
    super(Properties.create(Material.REDSTONE_LIGHT).setLightLevel((bs) -> 15));
  }

  public RouterBlock(BaseTier baseTier) {
    this();
    this.baseTier = baseTier;
  }

  @Override
  @Nonnull
  @ParametersAreNonnullByDefault
  public BlockRenderType getRenderType(BlockState state) {
    return BlockRenderType.MODEL;
  }

  @Override
  public boolean hasTileEntity(BlockState state) {
    return true;
  }

  @Override
  @Nullable
  public TileEntity createTileEntity(BlockState state, IBlockReader world) {
    return new RouterTE(baseTier);
  }

  @Nullable
  @Override
  @ParametersAreNonnullByDefault
  public TileEntity createNewTileEntity(IBlockReader blockReader) {
    return new RouterTE(baseTier);
  }

  @Override
  @ParametersAreNonnullByDefault
  public void onBlockPlacedBy(World worldIn, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
    super.onBlockPlacedBy(worldIn, pos, state, placer, stack);
  }

  @Override
  @Nonnull
  @ParametersAreNonnullByDefault
  public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
    if (!worldIn.isRemote) {
      final TileEntity tileEntity = worldIn.getTileEntity(pos);
      if (tileEntity instanceof RouterTE) {
        RouterTE router = (RouterTE) tileEntity;
        if (player.isSneaking())
          NetworkHooks.openGui((ServerPlayerEntity) player, (INamedContainerProvider) tileEntity, pos);
        else
          NetworkHooks.openGui((ServerPlayerEntity) player, (INamedContainerProvider) tileEntity, (packetBuffer) -> {
            packetBuffer.writeBlockPos(pos);
            packetBuffer.writeCompoundTag(Mapping.write(router.getMappings()));
          });
      }
    }
    return ActionResultType.SUCCESS;
  }

}
