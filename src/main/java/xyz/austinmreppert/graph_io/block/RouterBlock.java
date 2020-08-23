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
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;
import xyz.austinmreppert.graph_io.data.tiers.Tier;
import xyz.austinmreppert.graph_io.tileentity.RouterTE;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

public class RouterBlock extends ContainerBlock {

  private Tier tier;

  private RouterBlock() {
    super(Properties.create(Material.REDSTONE_LIGHT).setLightLevel((bs) -> 15).hardnessAndResistance(3.0F).notSolid().setOpaque(RouterBlock::func_235436_b_));
  }

  public RouterBlock(Tier tier) {
    this();
    this.tier = tier;
  }

  private static boolean func_235436_b_(BlockState p_235436_0_, IBlockReader p_235436_1_, BlockPos p_235436_2_) {
    return false;
  }

  @Override
  public boolean hasTileEntity(BlockState state) {
    return true;
  }

  @Override
  @Nonnull
  @ParametersAreNonnullByDefault
  public BlockRenderType getRenderType(BlockState state) {
    return BlockRenderType.MODEL;
  }

  @Override
  @Nullable
  public TileEntity createTileEntity(BlockState state, IBlockReader world) {
    return new RouterTE(tier);
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
      if (tileEntity instanceof RouterTE)
        NetworkHooks.openGui((ServerPlayerEntity) player, (INamedContainerProvider) tileEntity, pos);
    }
    return ActionResultType.SUCCESS;
  }

  @Nullable
  @Override
  @ParametersAreNonnullByDefault
  public TileEntity createNewTileEntity(IBlockReader blockReader) {
    return new RouterTE(tier);
  }

  @Override
  @ParametersAreNonnullByDefault
  public void onBlockHarvested(World worldIn, BlockPos pos, BlockState state, PlayerEntity player) {
    Vector3d playerEyePos = player.getPositionVec().add(0, player.getEyeHeight(), 0);
    Vector3d lookVec = playerEyePos.add(player.getLookVec().scale(5.0F));
    BlockRayTraceResult res = worldIn.rayTraceBlocks(new RayTraceContext(playerEyePos, lookVec, RayTraceContext.BlockMode.OUTLINE, RayTraceContext.FluidMode.ANY, player));
    if (res.getType() == RayTraceResult.Type.BLOCK) {
      System.out.println(res.getFace());
    }
    super.onBlockHarvested(worldIn, pos, state, player);
  }
}
