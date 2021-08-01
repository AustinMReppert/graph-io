package xyz.austinmreppert.graph_io.item;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
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
        if (worldIn.isClientSide) {
          if (identifierCapability.getBlockPos() != null)
            Highlighter.unhighlightBlock(identifierCapability.getBlockPos());
        }
        identifierCapability.setBlockPos(res.getBlockPos());
        identifierCapability.setLevel(playerIn.level.dimension());
      } else if (!playerIn.isCrouching() && identifierCapability.getBlockPos() != null) {
        BlockPos identifierPos = identifierCapability.getBlockPos();
        ResourceKey<Level> identifierLevel = identifierCapability.getLevel();
        if (worldIn.isClientSide) {
          Minecraft.getInstance().gui.getChat().addMessage(Component.nullToEmpty(POINTS_TO.getString() +  " (" + identifierPos.getX() + ", " + identifierPos.getY() + ", " + identifierPos.getZ() + ") " + (identifierLevel != null ? identifierLevel.location() : "")));
          Highlighter.toggleHighlightBlock(identifierPos, 0.01F, 255, 0, 0, 125);
        }
      }
    });
    return InteractionResultHolder.consume(is);
  }

  @Nullable
  @Override
  public CompoundTag getShareTag(ItemStack is) {
    CompoundTag nbt = new CompoundTag();
    is.getCapability(Capabilities.IDENTIFIER_CAPABILITY).ifPresent(identifierCapability -> {
      if (identifierCapability.getBlockPos() != null) {
        nbt.putInt("x", identifierCapability.getBlockPos().getX());
        nbt.putInt("y", identifierCapability.getBlockPos().getY());
        nbt.putInt("z", identifierCapability.getBlockPos().getZ());
      }
      if(identifierCapability.getLevel() != null)
        nbt.putString("levelLocation", identifierCapability.getLevel().location().toString());
    });
    return nbt;
  }

  @Override
  public void readShareTag(ItemStack is, @Nullable CompoundTag nbt) {
    if(nbt == null)
      return;
    is.getCapability(Capabilities.IDENTIFIER_CAPABILITY).ifPresent(identifierCapability -> {
      if (nbt.contains("x") && nbt.contains("y") && nbt.contains("z"))
        identifierCapability.setBlockPos(new BlockPos(nbt.getInt("x"), nbt.getInt("y"), nbt.getInt("z")));
      if(nbt.contains("levelLocation"))
        identifierCapability.setLevel(ResourceKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(nbt.getString("levelLocation"))));
    });
  }

  @Nullable
  @Override
  public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
    return new IdentifierCapabilityProvider();
  }

}
