package com.austinmreppert.graphio.network;

import com.austinmreppert.graphio.blockentity.RouterBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fmllegacy.network.NetworkEvent;
import net.minecraftforge.fmllegacy.network.PacketDistributor;

import java.util.function.Supplier;

/**
 * This packet is used to set a Router's mappings.
 */
public record SetRouterBEMappingsPacket(BlockPos blockPos, CompoundTag routerTENBT, int windowID) {
  /**
   * Handles a {@link SetRouterBEMappingsPacket}.
   *
   * @param packet  The packet to handle.
   * @param context The network event context.
   */
  public static void handle(final SetRouterBEMappingsPacket packet, final Supplier<NetworkEvent.Context> context) {
    context.get().enqueueWork(() -> {
      if (context.get().getDirection().getReceptionSide() == LogicalSide.SERVER) {
        final ServerPlayer sender = context.get().getSender();
        PacketHandler.INSTANCE.send(PacketDistributor.TRACKING_CHUNK.with(() -> {
          return sender.getLevel().getChunkAt(sender.blockPosition());
        }), packet);

        final BlockEntity blockEntity = context.get().getSender().getCommandSenderWorld().getBlockEntity(packet.blockPos);
        if (blockEntity instanceof RouterBlockEntity routerBlockEntity) {
          routerBlockEntity.readMappings(packet.routerTENBT);
          routerBlockEntity.setChanged();
        }
      } else {
        SetRouterBEMappingsPacketClient.handle(packet);
      }
    });
    context.get().setPacketHandled(true);
  }

  /**
   * Writes a {@link SetRouterBEMappingsPacket} into a {@link FriendlyByteBuf}.
   *
   * @param packet       The packet to write.
   * @param packetBuffer The {@link FriendlyByteBuf} to write to.
   */
  public static void encode(final SetRouterBEMappingsPacket packet, final FriendlyByteBuf packetBuffer) {
    packetBuffer.writeBlockPos(packet.blockPos);
    packetBuffer.writeNbt(packet.routerTENBT);
    packetBuffer.writeInt(packet.windowID);
  }

  /**
   * Creates a {@link SetRouterBEMappingsPacket} from a packet buffer.
   *
   * @param packetBuffer The buffer used to construct the packet.
   * @return A {@link SetRouterBEMappingsPacket}.
   */
  public static SetRouterBEMappingsPacket decode(final FriendlyByteBuf packetBuffer) {
    final var routerPos = packetBuffer.readBlockPos();
    final var routerTENBT = packetBuffer.readNbt();
    final var windowID = packetBuffer.readInt();
    return new SetRouterBEMappingsPacket(routerPos, routerTENBT, windowID);
  }

}
