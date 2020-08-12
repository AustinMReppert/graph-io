package xyz.austinmreppert.graph_io.network;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;
import xyz.austinmreppert.graph_io.tileentity.RouterTE;

import java.util.function.Supplier;

public class SetMappingsPacket {

  private final BlockPos blockPos;
  private final CompoundNBT routerTENBT;

  public SetMappingsPacket(BlockPos blockPos, CompoundNBT routerTENBT) {
    super();
    this.blockPos = blockPos;
    this.routerTENBT = routerTENBT;
  }

  public static void handle(SetMappingsPacket packet, Supplier<NetworkEvent.Context> context) {
    context.get().enqueueWork(() -> {
      if(context.get().getDirection().getReceptionSide() == LogicalSide.SERVER) {
        ServerPlayerEntity sender = context.get().getSender();
        TileEntity te = context.get().getSender().getEntityWorld().getTileEntity(packet.blockPos);
        if (te instanceof RouterTE) {
          RouterTE routerTE = (RouterTE) te;
          routerTE.getMappingsFromNBT(packet.routerTENBT);
          routerTE.markDirty();
        }
        PacketHander.INSTANCE.sendTo(packet, context.get().getNetworkManager(), NetworkDirection.PLAY_TO_CLIENT);
      } else {
        TileEntity te = Minecraft.getInstance().world.getTileEntity(packet.blockPos);
        if (te instanceof RouterTE) {
          RouterTE routerTE = (RouterTE) te;
          routerTE.getMappingsFromNBT(packet.routerTENBT);
          routerTE.markDirty();
        }
      }
    });
    context.get().setPacketHandled(true);
  }

  public static void encode(SetMappingsPacket packet, PacketBuffer packetBuffer) {
    packetBuffer.writeBlockPos(packet.blockPos);
    packetBuffer.writeCompoundTag(packet.routerTENBT);
  }

  public static SetMappingsPacket decode(PacketBuffer packetBuffer) {
    BlockPos routerPos = packetBuffer.readBlockPos();
    CompoundNBT routerTENBT = packetBuffer.readCompoundTag();
    return new SetMappingsPacket(routerPos, routerTENBT);
  }


}
