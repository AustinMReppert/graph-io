package xyz.austinmreppert.graph_io.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fmllegacy.network.NetworkRegistry;
import net.minecraftforge.fmllegacy.network.simple.SimpleChannel;
import xyz.austinmreppert.graph_io.GraphIO;

public class PacketHandler {

  private static final String PROTOCOL_VERSION = "1";

  public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(new ResourceLocation(GraphIO.MOD_ID, "main"),
    () -> PROTOCOL_VERSION, PROTOCOL_VERSION::equals, PROTOCOL_VERSION::equals
  );

  public static void init() {
    int i = 0;
    INSTANCE.registerMessage(i++, SetMappingsPacket.class, SetMappingsPacket::encode, SetMappingsPacket::decode, SetMappingsPacket::handle);
  }

}