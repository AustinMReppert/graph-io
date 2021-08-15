package com.austinmreppert.graphio.container;

import com.austinmreppert.graphio.data.mappings.Mapping;
import net.minecraft.nbt.Tag;

import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Keeps track of changes to a list of mappings.
 */
public class MappingsReferenceHolder {

  private ArrayList<Mapping> previous;

  public MappingsReferenceHolder(Supplier<ArrayList<Mapping>> get, Consumer<Tag> set) {
    this.get = get;
    this.set = set;
  }

  public Supplier<ArrayList<Mapping>> get;
  public Consumer<Tag> set;

  /**
   * Gets whether the mappings have changed.
   *
   * @return Whether the mappings have changed.
   */
  public boolean isDirty() {
    ArrayList<Mapping> current = this.get.get();
    boolean dirty = !current.equals(previous);
    previous = current;
    return dirty;
  }

}
