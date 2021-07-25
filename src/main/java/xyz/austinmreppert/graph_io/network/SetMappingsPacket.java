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
import xyz.austinmreppert.graph_io.container.RouterContainer;
import xyz.austinmreppert.graph_io.tileentity.RouterTE;

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
        BlockEntity te = context.get().getSender().getCommandSenderWorld().getBlockEntity(packet.blockPos);
        if (te instanceof RouterTE) {
          RouterTE routerTE = (RouterTE) te;
          routerTE.readMappings(packet.routerTENBT);
          routerTE.setChanged();
        }
      } else {
        Player playerentity = Minecraft.getInstance().player;
        AbstractContainerMenu openContainer = playerentity.containerMenu;
        if (openContainer instanceof RouterContainer && playerentity.containerMenu.containerId == packet.windowID) {
          RouterContainer router = (RouterContainer) openContainer;
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
