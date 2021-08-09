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

public class SetMappingsPacket {

  private final BlockPos blockPos;
  private final CompoundTag routerTENBT;
  private final int windowID;

  public SetMappingsPacket(BlockPos blockPos, CompoundTag routerTENBT, int windowID) {
    super();
    this.blockPos = blockPos;
    this.routerTENBT = routerTENBT;
    this.windowID = windowID;
  }

  public static void handle(SetMappingsPacket packet, Supplier<NetworkEvent.Context> context) {
    context.get().enqueueWork(() -> {
      if(context.get().getDirection().getReceptionSide() == LogicalSide.SERVER) {
        ServerPlayer sender = context.get().getSender();
        PacketHandler.INSTANCE.send(PacketDistributor.TRACKING_CHUNK.with(() -> {
          return sender.getLevel().getChunkAt(sender.blockPosition());
        }), packet);

        BlockEntity blockEntity = context.get().getSender().getCommandSenderWorld().getBlockEntity(packet.blockPos);
        if (blockEntity instanceof RouterBlockEntity routerBlockEntity) {
          routerBlockEntity.readMappings(packet.routerTENBT);
          routerBlockEntity.setChanged();
        }
      } else {
        SetMappingPacketClient.handle(packet);
      }
    });
    context.get().setPacketHandled(true);
  }

  public static void encode(SetMappingsPacket packet, FriendlyByteBuf packetBuffer) {
    packetBuffer.writeBlockPos(packet.blockPos);
    packetBuffer.writeNbt(packet.routerTENBT);
    packetBuffer.writeInt(packet.windowID);
  }

  public static SetMappingsPacket decode(FriendlyByteBuf packetBuffer) {
    BlockPos routerPos = packetBuffer.readBlockPos();
    CompoundTag routerTENBT = packetBuffer.readNbt();
    int windowID = packetBuffer.readInt();
    return new SetMappingsPacket(routerPos, routerTENBT, windowID);
  }

  public CompoundTag getRouterTENBT() {
    return routerTENBT;
  }

  public BlockPos getBlockPos() {
    return blockPos;
  }

  public int getWindowID() {
    return windowID;
  }

}
