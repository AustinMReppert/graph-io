package xyz.austinmreppert.graph_io.network;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fmllegacy.network.NetworkEvent;
import net.minecraftforge.fmllegacy.network.PacketDistributor;
import xyz.austinmreppert.graph_io.blockentity.RouterBlockEntity;
import xyz.austinmreppert.graph_io.container.RouterContainer;

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
        // TODO: PACKET IS SENT TO THE SENDER! FIX LATER
        Player playerEntity = Minecraft.getInstance().player;
        AbstractContainerMenu openContainer = playerEntity.containerMenu;
        BlockEntity be = playerEntity.level.getBlockEntity(packet.blockPos);
        if (openContainer instanceof RouterContainer router && be instanceof RouterBlockEntity routerBlockEntity && packet.blockPos.equals(routerBlockEntity.getBlockPos())) {
          router.getTrackedMappingsReference().set.accept(packet.routerTENBT);
        }
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


}
