package com.austinmreppert.graphio.network;

import com.austinmreppert.graphio.blockentity.RouterBlockEntity;
import com.austinmreppert.graphio.data.RedstoneMode;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * This packet is used to set a Router's redstone mode.
 */
public record SetRouterRedstoneMode(BlockPos blockPos, RedstoneMode redstoneMode, int windowID) {
  /**
   * Handles a {@link SetRouterRedstoneMode}.
   *
   * @param packet  The packet to handle.
   * @param context The network event context.
   */
  public static void handle(final SetRouterRedstoneMode packet, final Supplier<NetworkEvent.Context> context) {
    context.get().enqueueWork(() -> {
      if (context.get().getDirection().getReceptionSide() == LogicalSide.SERVER) {
        final ServerPlayer sender = context.get().getSender();
        final var blockEntity = sender.getCommandSenderWorld().getBlockEntity(packet.blockPos);
        if (blockEntity instanceof RouterBlockEntity routerBlockEntity) {
          routerBlockEntity.redstoneMode = packet.redstoneMode;
          routerBlockEntity.setChanged();
        }
      }
    });
    context.get().setPacketHandled(true);
  }

  /**
   * Writes a {@link SetRouterRedstoneMode} into a {@link FriendlyByteBuf}.
   *
   * @param packet       The packet to write.
   * @param packetBuffer The {@link FriendlyByteBuf} to write to.
   */
  public static void encode(final SetRouterRedstoneMode packet, final FriendlyByteBuf packetBuffer) {
    packetBuffer.writeBlockPos(packet.blockPos);
    packetBuffer.writeInt(packet.redstoneMode.ordinal());
    packetBuffer.writeInt(packet.windowID);
  }

  /**
   * Creates a {@link SetRouterRedstoneMode} from a packet buffer.
   *
   * @param packetBuffer The buffer used to construct the packet.
   * @return A {@link SetRouterRedstoneMode}.
   */
  public static SetRouterRedstoneMode decode(final FriendlyByteBuf packetBuffer) {
    final var routerPos = packetBuffer.readBlockPos();
    final var redstoneMode = RedstoneMode.valueOf(packetBuffer.readInt());
    final var windowID = packetBuffer.readInt();
    return new SetRouterRedstoneMode(routerPos, redstoneMode, windowID);
  }

}
