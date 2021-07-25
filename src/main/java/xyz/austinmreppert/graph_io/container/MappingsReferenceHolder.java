package xyz.austinmreppert.graph_io.container;

import net.minecraft.nbt.Tag;
import xyz.austinmreppert.graph_io.data.mappings.Mapping;

import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class MappingsReferenceHolder {

  private ArrayList<Mapping> previous;

  public MappingsReferenceHolder(Supplier<ArrayList<Mapping>> get, Consumer<Tag> set) {
    this.get = get;
    this.set = set;
  }

  public Supplier<ArrayList<Mapping>> get;
  public Consumer<Tag> set;

  public boolean isDirty() {
    ArrayList<Mapping> current = this.get.get();
    boolean dirty = !current.equals(previous);
    previous = current;
    return dirty;
  }

}
