package xyz.austinmreppert.graph_io.item;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import xyz.austinmreppert.graph_io.capabilities.Capabilities;
import xyz.austinmreppert.graph_io.capabilities.IdentifierCapabilityProvider;
import xyz.austinmreppert.graph_io.client.render.Highlighter;

import javax.annotation.Nullable;

public class IdentifierItem extends Item {

  public IdentifierItem(Properties properties) {
    super(properties);
  }

  @Override
  public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn) {
    Vector3d playerEyePos = playerIn.getPositionVec().add(0, playerIn.getEyeHeight(), 0);
    Vector3d lookVec = playerEyePos.add(playerIn.getLookVec().scale(5.0F));
    BlockRayTraceResult res = worldIn.rayTraceBlocks(new RayTraceContext(playerEyePos, lookVec, RayTraceContext.BlockMode.OUTLINE, RayTraceContext.FluidMode.ANY, playerIn));
    ItemStack is = playerIn.getHeldItem(handIn);
    is.getCapability(Capabilities.IDENTIFIER_CAPABILITY).ifPresent(identifierCapability -> {
      if (playerIn.isSneaking() && res.getType() == RayTraceResult.Type.BLOCK) {
        // TODO: SYNC from server to client
        if(worldIn.isRemote) {
          if (identifierCapability.getBlockPos() != null)
            Highlighter.unhighlightBlock(identifierCapability.getBlockPos());
        }
        identifierCapability.setBlockPos(res.getPos());
      } else if (!playerIn.isSneaking() && identifierCapability.getBlockPos() != null) {
        BlockPos identifierPos = identifierCapability.getBlockPos();
        if (worldIn.isRemote) {
          Minecraft.getInstance().player.sendChatMessage("Identifier points to (" + identifierPos.getX() + ", " + identifierPos.getY() + ", " + identifierPos.getZ() + ")");
          Highlighter.toggleHighlightBlock(identifierPos, 255, 0, 0, 125);
        }
      }
    });

    return super.onItemRightClick(worldIn, playerIn, handIn);
  }

  @Nullable
  @Override
  public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundNBT nbt) {
    return new IdentifierCapabilityProvider(stack);
  }

}
