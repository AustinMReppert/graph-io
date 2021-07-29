package xyz.austinmreppert.graph_io.blockentity;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.ObjectHolder;
import xyz.austinmreppert.graph_io.GraphIO;

@ObjectHolder(GraphIO.MOD_ID)
public class BlockEntityTypes {

  public static BlockEntityType<RouterBlockEntity> ROUTER;

}
