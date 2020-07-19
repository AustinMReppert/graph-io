package xyz.austinmreppert.graph_io.network;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import xyz.austinmreppert.graph_io.GraphIO;

import java.util.function.BiConsumer;

public class PacketHander {

  private static final String POROTOCOL_VERSION = "1";

  public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(new ResourceLocation(GraphIO.MOD_ID, "main"),
    () -> POROTOCOL_VERSION, POROTOCOL_VERSION::equals, POROTOCOL_VERSION::equals
  );

  public static void init() {
    int i = 0;
    INSTANCE.registerMessage(i++, SetMappingsPacket.class, SetMappingsPacket::encode, SetMappingsPacket::decode, SetMappingsPacket::handle);
  }

}
