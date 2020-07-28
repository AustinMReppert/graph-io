package xyz.austinmreppert.graph_io.block;

import net.minecraft.block.Block;
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
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.network.NetworkHooks;
import xyz.austinmreppert.graph_io.tileentity.ControllerNodeTE;
import xyz.austinmreppert.graph_io.tileentity.TileEntityTypes;

import javax.annotation.Nullable;

public class ControllerNodeBlock extends ContainerBlock {

  public ControllerNodeBlock() {
    super(Properties.create(Material.REDSTONE_LIGHT).setLightLevel((bs) -> {
      return 15;
    }).hardnessAndResistance(3.0F).notSolid().setOpaque(ControllerNodeBlock::func_235436_b_));
  }

  private static boolean func_235436_b_(BlockState p_235436_0_, IBlockReader p_235436_1_, BlockPos p_235436_2_) {
    return false;
  }

  @Override
  public boolean hasTileEntity(BlockState state) {
    return true;
  }

  /**
   * The type of render function called. MODEL for mixed tesr and static model
   */
  public BlockRenderType getRenderType(BlockState state) {
    return BlockRenderType.MODEL;
  }

  @Nullable
  @Override
  public TileEntity createTileEntity(BlockState state, IBlockReader world) {
    return TileEntityTypes.CONTROLLER_NODE.create();
  }

  @Override
  public void onBlockPlacedBy(World worldIn, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
    super.onBlockPlacedBy(worldIn, pos, state, placer, stack);
  }

  @Override
  public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
    if (!worldIn.isRemote) {
      final TileEntity tileEntity = worldIn.getTileEntity(pos);
      if (tileEntity instanceof ControllerNodeTE)
        NetworkHooks.openGui((ServerPlayerEntity) player, (INamedContainerProvider) tileEntity, pos);
    }
    return ActionResultType.SUCCESS;
  }

  @Nullable
  @Override
  public TileEntity createNewTileEntity(IBlockReader blockReader) {
    return TileEntityTypes.CONTROLLER_NODE.create();
  }

  @Override
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
