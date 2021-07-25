package xyz.austinmreppert.graph_io.item;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import xyz.austinmreppert.graph_io.capabilities.Capabilities;
import xyz.austinmreppert.graph_io.capabilities.IdentifierCapabilityProvider;
import xyz.austinmreppert.graph_io.client.render.Highlighter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

public class IdentifierItem extends Item {

  private static final TranslatableComponent POINTS_TO = new TranslatableComponent("graphio.chat.identifier_points_to");

  public IdentifierItem(Properties properties) {
    super(properties);
  }

  @Override
  @Nonnull
  @ParametersAreNonnullByDefault
  public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
    Vec3 playerEyePos = playerIn.position().add(0, playerIn.getEyeHeight(), 0);
    Vec3 lookVec = playerEyePos.add(playerIn.getLookAngle().scale(5.0F));
    BlockHitResult res = worldIn.clip(new ClipContext(playerEyePos, lookVec, ClipContext.Block.OUTLINE, ClipContext.Fluid.ANY, playerIn));
    ItemStack is = playerIn.getItemInHand(handIn);
    is.getCapability(Capabilities.IDENTIFIER_CAPABILITY).ifPresent(identifierCapability -> {
      if (playerIn.isCrouching() && res.getType() == BlockHitResult.Type.BLOCK) {
        // TODO: SYNC from server to client
        if (worldIn.isClientSide) {
          if (identifierCapability.getBlockPos() != null)
            Highlighter.unhighlightBlock(identifierCapability.getBlockPos());
        }
        identifierCapability.setBlockPos(res.getBlockPos());
      } else if (!playerIn.isCrouching() && identifierCapability.getBlockPos() != null) {
        BlockPos identifierPos = identifierCapability.getBlockPos();
        if (worldIn.isClientSide) {
          Minecraft.getInstance().gui.getChat().addMessage(Component.nullToEmpty(POINTS_TO.getString() +  " (" + identifierPos.getX() + ", " + identifierPos.getY() + ", " + identifierPos.getZ() + ")"));
          Highlighter.toggleHighlightBlock(identifierPos, 0.01F, 255, 0, 0, 125);
        }
      }
    });

    return super.use(worldIn, playerIn, handIn);
  }

  @Nullable
  @Override
  public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
    return new IdentifierCapabilityProvider();
  }

}
