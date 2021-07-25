package xyz.austinmreppert.graph_io.tileentity;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.ObjectHolder;
import xyz.austinmreppert.graph_io.GraphIO;

@ObjectHolder(GraphIO.MOD_ID)
public class TileEntityTypes {

  public static BlockEntityType<RouterTE> ROUTER;

}
