package xyz.austinmreppert.graph_io.network;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;
import xyz.austinmreppert.graph_io.container.RouterContainer;
import xyz.austinmreppert.graph_io.tileentity.RouterTE;

import java.util.function.Supplier;

public class SetMappingsPacket {

  private final BlockPos blockPos;
  private final CompoundNBT routerTENBT;
  private final int windowID;

  public SetMappingsPacket(BlockPos blockPos, CompoundNBT routerTENBT, int windowID) {
    super();
    this.blockPos = blockPos;
    this.routerTENBT = routerTENBT;
    this.windowID = windowID;
  }

  public static void handle(SetMappingsPacket packet, Supplier<NetworkEvent.Context> context) {
    context.get().enqueueWork(() -> {
      if(context.get().getDirection().getReceptionSide() == LogicalSide.SERVER) {
        ServerPlayerEntity sender = context.get().getSender();
        TileEntity te = context.get().getSender().getEntityWorld().getTileEntity(packet.blockPos);
        if (te instanceof RouterTE) {
          RouterTE routerTE = (RouterTE) te;
          routerTE.readMappings(packet.routerTENBT);
          routerTE.markDirty();
        }
      } else {
        PlayerEntity playerentity = Minecraft.getInstance().player;
        Container openContainer = playerentity.openContainer;
        if (openContainer instanceof RouterContainer && playerentity.openContainer.windowId == packet.windowID) {
          RouterContainer router = (RouterContainer) openContainer;
          router.getTrackedMappingsReference().set.accept(packet.routerTENBT);
        }
      }
    });
    context.get().setPacketHandled(true);
  }

  public static void encode(SetMappingsPacket packet, PacketBuffer packetBuffer) {
    packetBuffer.writeBlockPos(packet.blockPos);
    packetBuffer.writeCompoundTag(packet.routerTENBT);
    packetBuffer.writeInt(packet.windowID);
  }

  public static SetMappingsPacket decode(PacketBuffer packetBuffer) {
    BlockPos routerPos = packetBuffer.readBlockPos();
    CompoundNBT routerTENBT = packetBuffer.readCompoundTag();
    int windowID = packetBuffer.readInt();
    return new SetMappingsPacket(routerPos, routerTENBT, windowID);
  }


}
