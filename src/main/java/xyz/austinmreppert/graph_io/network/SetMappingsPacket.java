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
import xyz.austinmreppert.graph_io.tileentity.ControllerNodeTE;

import java.util.function.Supplier;

public class SetMappingsPacket {

  private final BlockPos blockPos;
  private final CompoundNBT controllerNodeTENBT;

  public SetMappingsPacket(BlockPos blockPos, CompoundNBT controllerNodeTENBT) {
    super();
    this.blockPos = blockPos;
    this.controllerNodeTENBT = controllerNodeTENBT;
  }

  public static void handle(SetMappingsPacket packet, Supplier<NetworkEvent.Context> context) {
    context.get().enqueueWork(() -> {
      if(context.get().getDirection().getReceptionSide() == LogicalSide.SERVER) {
        ServerPlayerEntity sender = context.get().getSender();
        TileEntity te = context.get().getSender().getEntityWorld().getTileEntity(packet.blockPos);
        if (te instanceof ControllerNodeTE) {
          ControllerNodeTE controllerNodeTE = (ControllerNodeTE) te;
          controllerNodeTE.getMappingsFromNBT(packet.controllerNodeTENBT);
          controllerNodeTE.markDirty();
        }
        PacketHander.INSTANCE.sendTo(packet, context.get().getNetworkManager(), NetworkDirection.PLAY_TO_CLIENT);
      } else {
        TileEntity te = Minecraft.getInstance().world.getTileEntity(packet.blockPos);
        if (te instanceof ControllerNodeTE) {
          ControllerNodeTE controllerNodeTE = (ControllerNodeTE) te;
          controllerNodeTE.getMappingsFromNBT(packet.controllerNodeTENBT);
          controllerNodeTE.markDirty();
        }
      }
    });
    context.get().setPacketHandled(true);
  }

  public static void encode(SetMappingsPacket packet, PacketBuffer packetBuffer) {
    packetBuffer.writeBlockPos(packet.blockPos);
    packetBuffer.writeCompoundTag(packet.controllerNodeTENBT);
  }

  public static SetMappingsPacket decode(PacketBuffer packetBuffer) {
    BlockPos controllerPos = packetBuffer.readBlockPos();
    CompoundNBT controllerNodeTENBT = packetBuffer.readCompoundTag();
    return new SetMappingsPacket(controllerPos, controllerNodeTENBT);
  }


}
