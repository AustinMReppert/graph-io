package com.austinmreppert.graphio.item;

import com.austinmreppert.graphio.GraphIO;
import com.austinmreppert.graphio.capabilities.Capabilities;
import com.austinmreppert.graphio.capabilities.IdentifierCapabilityProvider;
import com.austinmreppert.graphio.client.render.Highlighter;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.concurrent.atomic.AtomicReference;

/**
 * This item stores a specific location and dimension.
 */
public class IdentifierItem extends Item {

  private static final Component POINTS_TO = Component.translatable(GraphIO.MOD_ID + ".chat.identifier_points_to");

  public IdentifierItem(final Properties properties) {
    super(properties);
  }

  /**
   * Called when the player right clicks. If it is a shift-right-click, then the {@link IdentifierItem} will attempt to store the position.
   * If it is a right-click, the {@link IdentifierItem} will attempt to highlight the stored position.
   *
   * @param level  The current {@link Level} of the {@code player}.
   * @param player The {@link Player} who right-clicked.
   * @param hand   The {@link InteractionHand} of the {@code player}.
   * @return The status of the right-click.
   */
  @Override
  @Nonnull
  @ParametersAreNonnullByDefault
  public InteractionResultHolder<ItemStack> use(final Level level, final Player player, final InteractionHand hand) {
    final Vec3 playerEyePos = player.position().add(0, player.getEyeHeight(), 0);
    final Vec3 lookVec = playerEyePos.add(player.getLookAngle().scale(5.0F));
    final BlockHitResult res = level.clip(new ClipContext(playerEyePos, lookVec, ClipContext.Block.OUTLINE, ClipContext.Fluid.ANY, player));
    final ItemStack is = player.getItemInHand(hand);
    is.getCapability(Capabilities.IDENTIFIER_CAPABILITY).ifPresent((final var identifierCapability) -> {
      if (player.isCrouching() && res.getType() == BlockHitResult.Type.BLOCK) {
        if (level.isClientSide && identifierCapability.getBlockPos() != null && identifierCapability.getLevel() != null)
          Highlighter.unhighlightBlock(identifierCapability.getBlockPos(), identifierCapability.getLevel());
        identifierCapability.setBlockPos(res.getBlockPos());
        identifierCapability.setLevel(player.level.dimension());
      } else if (!player.isCrouching() && identifierCapability.getBlockPos() != null) {
        final BlockPos identifierPos = identifierCapability.getBlockPos();
        final ResourceKey<Level> identifierLevel = identifierCapability.getLevel();
        if (level.isClientSide) {
          Minecraft.getInstance().gui.getChat().addMessage(Component.nullToEmpty(POINTS_TO.getString() + " (" + identifierPos.getX() + ", " + identifierPos.getY() + ", " + identifierPos.getZ() + ") " + (identifierLevel != null ? identifierLevel.location() : "")));
          Highlighter.toggleHighlightBlock(identifierPos, identifierLevel, 0.02F, 255, 0, 0, 125);
        }
      }
    });
    return InteractionResultHolder.success(is);
  }

  /**
   * Saves an {@link ItemStack}'s identifier capability into a {@link CompoundTag}. This is used for synchronization.
   *
   * @param is The {@link ItemStack} used to generate the {@link CompoundTag};
   * @return A {@link CompoundTag} to store the identifier capability data.
   */
  @Nullable
  @Override
  public CompoundTag getShareTag(final ItemStack is) {
    final var nbt = new AtomicReference<>(super.getShareTag(is));
    if(nbt.get() == null)
      nbt.set(new CompoundTag());
    is.getCapability(Capabilities.IDENTIFIER_CAPABILITY).ifPresent((final var instance) -> {
      nbt.get().put("identifierCapability", instance.serializeNBT());
    });

    return nbt.get();
  }

  /**
   * Loads an identifier capability from a {@link CompoundTag} into an {@link ItemStack}. This is used for synchronization.
   *
   * @param is  The {@link ItemStack} to put the read identifier capability onto.
   * @param nbt The {@link CompoundTag} that stores the identifier capability.
   */
  @Override
  public void readShareTag(final ItemStack is, @Nullable final CompoundTag nbt) {
    super.readShareTag(is, nbt);
    if(nbt != null)
      is.getCapability(Capabilities.IDENTIFIER_CAPABILITY).ifPresent((final var instance) -> {
        instance.deserializeNBT(nbt.getCompound("identifierCapability"));
      });
  }

  /**
   * Creates an identifier capability provider.
   *
   * @param stack The {@link ItemStack} associated with the capability.
   * @param nbt   A {@link CompoundTag} used to restore the {@code stack}'s NBT data.
   * @return An identifier capability provider.
   */
  @Nullable
  @Override
  public ICapabilityProvider initCapabilities(final ItemStack stack, @Nullable final CompoundTag nbt) {
    super.initCapabilities(stack, nbt);
    return new IdentifierCapabilityProvider();
  }

}
