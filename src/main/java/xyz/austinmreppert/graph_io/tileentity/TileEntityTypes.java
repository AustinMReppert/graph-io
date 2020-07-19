package xyz.austinmreppert.graph_io.tileentity;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.registries.ObjectHolder;
import xyz.austinmreppert.graph_io.GraphIO;

@ObjectHolder(GraphIO.MOD_ID)
public class TileEntityTypes {

  public static TileEntityType<ControllerNodeTE> CONTROLLER_NODE;

}
