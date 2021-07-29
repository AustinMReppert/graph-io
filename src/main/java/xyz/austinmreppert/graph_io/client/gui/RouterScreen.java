package xyz.austinmreppert.graph_io.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import org.lwjgl.glfw.GLFW;
import xyz.austinmreppert.graph_io.GraphIO;
import xyz.austinmreppert.graph_io.container.RouterContainer;
import xyz.austinmreppert.graph_io.data.mappings.Mapping;
import xyz.austinmreppert.graph_io.network.PacketHander;
import xyz.austinmreppert.graph_io.network.SetMappingsPacket;
import xyz.austinmreppert.graph_io.blockentity.RouterBlockEntity;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.Optional;

public class RouterScreen extends AbstractContainerScreen<RouterContainer> implements MenuAccess<RouterContainer> {

  private static final ResourceLocation BACKGROUND = new ResourceLocation(GraphIO.MOD_ID, "textures/gui/container/router.png");
  private static final ResourceLocation NATURAL_BUTTON_TEXTURE = new ResourceLocation(GraphIO.MOD_ID, "textures/gui/natural_button.png");
  private static final ResourceLocation CYCLIC_BUTTON_TEXTURE = new ResourceLocation(GraphIO.MOD_ID, "textures/gui/cyclic_button.png");
  private static final ResourceLocation RANDOM_BUTTON_TEXTURE = new ResourceLocation(GraphIO.MOD_ID, "textures/gui/random_button.png");
  private static final ResourceLocation FILTER_SCHEME_BUTTON_TEXTURE = new ResourceLocation(GraphIO.MOD_ID, "textures/gui/filter_scheme_button.png");
  private static final ResourceLocation MINUS_BUTTON_TEXTURE = new ResourceLocation(GraphIO.MOD_ID, "textures/gui/decrease_stack_size_button.png");
  private static final ResourceLocation PLUS_BUTTON_TEXTURE = new ResourceLocation(GraphIO.MOD_ID, "textures/gui/increase_stack_size_button.png");
  private final int HOTBAR_X = 108;
  private final int HOTBAR_Y = 232;
  private final int INVENTORY_X = 108;
  private final int INVENTORY_Y = 174;
  private final int SLOT_SIZE = 18;
  private final int SCROLL_BAR_TEXTURE_X = 276;
  private final int SCROLL_BAR_TEXTURE_Y = 0;
  private final int SCROLL_BAR_INACTIVE_TEXTURE_X = 282;
  private final int SCROLL_BAR_INACTIVE_TEXTURE_Y = 0;
  private final int SCROLL_BAR_X = 262;
  private final int SCROLL_BAR_Y = 18;
  private final int SCROLL_BAR_WIDTH = 6;
  private final int SCROLL_BAR_HEIGHT = 27;
  private final int SCROLL_AREA_X = SCROLL_BAR_X;
  private final int SCROLL_AREA_Y = SCROLL_BAR_Y;
  private final int SCROLL_AREA_HEIGHT = 140;
  private final int SCROLL_AREA_WIDTH = SCROLL_BAR_WIDTH;
  private final int MAPPINGS_AREA_X = 5;
  private final int MAPPINGS_AREA_Y = SCROLL_BAR_Y;
  private final int MAPPINGS_AREA_WIDTH = 256;
  private final int MAPPINGS_AREA_HEIGHT = SCROLL_AREA_HEIGHT;
  private final int MAPPING_X = MAPPINGS_AREA_X + 1;
  private final int MAPPING_Y = MAPPINGS_AREA_Y + 1;
  private final int MAPPING_HEIGHT = 12;
  private final int MAPPING_WIDTH = MAPPINGS_AREA_WIDTH - 2;
  private final int MAX_SCROLL = SCROLL_BAR_Y + SCROLL_AREA_HEIGHT - SCROLL_BAR_HEIGHT;
  private final int MAPPINGS_PER_PAGE = 8;
  private final int BACKGROUND_TEXTURE_X = 0;
  private final int BACKGROUND_TEXTURE_Y = 0;
  private final int BACKGROUND_TEXTURE_WIDTH = 276;
  private final int BACKGROUND_TEXTURE_HEIGHT = 256;
  private final int SLOT_TEXTURE_X = 288;
  private final int SLOT_TEXTURE_Y = 0;
  private final int SLOT_TEXTURE_WIDTH = 18;
  private final int SLOT_TEXTURE_HEIGHT = 18;
  private final int DISTRIBUTION_BUTTONS_X = MAPPINGS_AREA_X;
  private final int DISTRIBUTION_BUTTONS_Y = HOTBAR_Y - 1;
  private final int TEXT_COLOR = TextColor.parseColor("#FFFFFF").getValue();
  private final ArrayList<EditBox> rawMappings;
  private final Component ITEMS_PER_TICK = new TranslatableComponent("graphio.gui.items_per_tick");
  private final Component BUCKETS_PER_TICK = new TranslatableComponent("graphio.gui.buckets_per_tick");
  private final Component ENERGY_PER_TICK = new TranslatableComponent("graphio.gui.energy_per_tick");
  private final Component TICK_DELAY = new TranslatableComponent("graphio.gui.tick_delay");
  private final Component ENERGY = new TranslatableComponent("graphio.gui.energy");
  public int inventoryRows;
  protected EditBox inputField;
  private float currentScroll;
  private boolean isScrolling;
  private ToggleImageButton distributeRandomlyButton;
  private ToggleImageButton distributeCyclicallyButton;
  private ToggleImageButton distributeNaturallyButton;
  private ToggleImageButton filterSchemeButton;
  private ImageButton decreaseStackSizeButton, increaseStackSizeButton, decreaseBucketsButton, increaseBucketsButton,
    decreaseEnergyButton, increaseEnergyButton, decreaseTickDelay, increaseTickDelay;
  private int lastFocusedMapping;
  private RouterBlockEntity routerBlockEntity;

  public RouterScreen(AbstractContainerMenu screenContainer, Inventory inv, Component titleIn) {
    super((RouterContainer) screenContainer, inv, titleIn);
    routerBlockEntity = ((RouterContainer) screenContainer).getRouterTE();
    passEvents = false;
    imageWidth = 276;
    imageHeight = 256;
    width = 276;
    height = 256;
    // Inventory start
    inventoryLabelX = 107;
    inventoryLabelY = 162;
    rawMappings = new ArrayList<>();
  }

  private void onTextChanged(String text) {
  }

  private void updateMappingGUI() {
    getLastFocusedMapping().ifPresentOrElse((mapping) -> {
      Mapping.DistributionScheme mappingDistributionScheme = mapping.getDistributionScheme();
      distributeNaturallyButton.setEnabled(mappingDistributionScheme == Mapping.DistributionScheme.NATURAL);
      distributeCyclicallyButton.setEnabled(mappingDistributionScheme == Mapping.DistributionScheme.CYCLIC);
      distributeRandomlyButton.setEnabled(mappingDistributionScheme == Mapping.DistributionScheme.RANDOM);
      filterSchemeButton.setEnabled(mapping.getFilterScheme() == Mapping.FilterScheme.WHITE_LIST);
    }, () -> {
      distributeNaturallyButton.setEnabled(false);
      distributeCyclicallyButton.setEnabled(false);
      distributeRandomlyButton.setEnabled(false);
      // TODO: Disable buttons on the left
    });
  }

  private void updateMappings() {
    PacketHander.INSTANCE.sendToServer(new SetMappingsPacket(routerBlockEntity.getBlockPos(), Mapping.write(getMappings()), menu.containerId));
  }

  public ArrayList<Mapping> getMappings() {
    return menu.getTrackedMappingsReference().get.get();
  }

  @Override
  public boolean charTyped(char typedChar, int modifiers) {
    if (getFocused() != null && getFocused() instanceof EditBox && ((EditBox) getFocused()).canConsumeInput()) {
      EditBox listener = (EditBox) getFocused();
      int index = rawMappings.indexOf(listener);
      boolean pressed = listener.charTyped(typedChar, modifiers);
      if (index != -1) {
        getMappings().get(index).setRaw(listener.getValue());
        updateMappings();
      }
      return pressed;
    }
    return false;
  }

  public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
    if (keyCode == GLFW.GLFW_KEY_ESCAPE)
      minecraft.player.closeContainer();
    else if (keyCode == GLFW.GLFW_KEY_ENTER) {
      for (EditBox rawMapping : rawMappings)
        rawMapping.setFocus(false);
      EditBox mapping = createMappingTextField("", getMappings().size());
      if (canScroll(rawMappings.size()))
        currentScroll = 1.0F;
      else
        currentScroll = (float) rawMappings.size() / MAPPINGS_PER_PAGE;
      addRenderableWidget(mapping);
      getMappings().add(new Mapping("", Mapping.DistributionScheme.NATURAL, Mapping.FilterScheme.BLACK_LIST,
        routerBlockEntity.getTier()));
      rawMappings.add(mapping);
      scrollTo(currentScroll);
      mapping.setFocus(true);
      setFocused(mapping);
      updateMappings();
      return true;
    } else if (keyCode == GLFW.GLFW_KEY_DELETE && getFocused() != null && getFocused() instanceof EditBox && ((EditBox) getFocused()).canConsumeInput()) {
      if (rawMappings.size() < 1) return true;
      int index = rawMappings.indexOf(getFocused());
      if (index != -1) {
        //currentScroll = (currentScroll * rawMappings.size() - 1) / (rawMappings.size() - 1);
        menu.disableFilterSlots();
        lastFocusedMapping = -1;
        rawMappings.remove(index);
        getMappings().remove(index);
        removeWidget(getFocused());
        setFocused(null);
        scrollTo(currentScroll);
        lastFocusedMapping = -1;
        updateMappings();
        updateMappingGUI();
      }
      return true;
    } else if (getFocused() != null && getFocused() instanceof EditBox && ((EditBox) getFocused()).canConsumeInput()) {
      EditBox listener = (EditBox) getFocused();
      int index = rawMappings.indexOf(listener);
      boolean pressed = listener.keyPressed(keyCode, scanCode, modifiers);
      if (pressed && index != -1) {
        getMappings().get(index).setRaw(listener.getValue());
        updateMappings();
      }
      return pressed;
    }
    return super.keyPressed(keyCode, scanCode, modifiers);
  }

  @Override
  @ParametersAreNonnullByDefault
  public void render(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
    renderBackground(matrixStack);
    super.render(matrixStack, mouseX, mouseY, partialTicks);

    if (this.menu.getCarried().isEmpty() && this.hoveredSlot != null && this.hoveredSlot.hasItem()) {
      this.renderTooltip(matrixStack, this.hoveredSlot.getItem(), mouseX, mouseY);
    }

    for (EditBox mapping : rawMappings)
      mapping.render(matrixStack, mouseX, mouseY, partialTicks);
  }

  public void renderScrollbar(PoseStack matrixStack) {
    if (canScroll(rawMappings.size()))
      blit(matrixStack, leftPos + SCROLL_BAR_X, (int) (topPos + SCROLL_BAR_Y + currentScroll * (SCROLL_AREA_HEIGHT - SCROLL_BAR_HEIGHT)), getBlitOffset(), SCROLL_BAR_TEXTURE_X, SCROLL_BAR_TEXTURE_Y, SCROLL_BAR_WIDTH, SCROLL_BAR_HEIGHT, 256, 512);
    else
      blit(matrixStack, leftPos + SCROLL_BAR_X, (int) (topPos + SCROLL_BAR_Y + currentScroll * (SCROLL_AREA_HEIGHT - SCROLL_BAR_HEIGHT)), getBlitOffset(), SCROLL_BAR_INACTIVE_TEXTURE_X, SCROLL_BAR_INACTIVE_TEXTURE_Y, SCROLL_BAR_WIDTH, SCROLL_BAR_HEIGHT, 256, 512);
  }

  @Override
  public void setFocused(@Nullable GuiEventListener listener) {
    int index = rawMappings.indexOf(listener);
    if (index != -1) {
      lastFocusedMapping = index;
      updateMappingGUI();
      menu.setFilterSlotContents(getMappings().get(index).getFilterInventory());
    }
    super.setFocused(listener);
  }

  @Override
  @ParametersAreNonnullByDefault
  protected void slotClicked(@Nullable Slot slotIn, int slotId, int mouseButton, ClickType type) {
    if (slotIn instanceof FilterSlot) {
      getLastFocusedMapping().ifPresent((focused) -> {
        super.slotClicked(slotIn, slotId, mouseButton, type);
        menu.copySlotContents(focused.getFilterInventory());
        updateMappings();
      });
    } else super.slotClicked(slotIn, slotId, mouseButton, type);
  }

  @Override
  public void init() {
    super.init();
    lastFocusedMapping = -1;
    createMappingTextFields();
    scrollTo(currentScroll);

    this.addRenderableWidget(distributeNaturallyButton = new ToggleImageButton(this.leftPos + DISTRIBUTION_BUTTONS_X, topPos + DISTRIBUTION_BUTTONS_Y, 18, 18, 0, 0, 18, NATURAL_BUTTON_TEXTURE, 256, 256, (button) -> {
      getLastFocusedMapping().ifPresent(mapping -> {
        mapping.setDistributionScheme(Mapping.DistributionScheme.NATURAL);
        updateMappingGUI();
        updateMappings();
      });
    }, new TranslatableComponent("gui.graphio.natural"), this));

    this.addRenderableWidget(distributeCyclicallyButton = new ToggleImageButton(this.leftPos + DISTRIBUTION_BUTTONS_X + 24, topPos + DISTRIBUTION_BUTTONS_Y, 18, 18, 0, 0, 18, CYCLIC_BUTTON_TEXTURE, 256, 256, (button) -> {
      getLastFocusedMapping().ifPresent(mapping -> {
        mapping.setDistributionScheme(Mapping.DistributionScheme.CYCLIC);
        updateMappingGUI();
        updateMappings();
      });
    }, new TranslatableComponent("gui.graphio.cyclic"), this));

    this.addRenderableWidget(distributeRandomlyButton = new ToggleImageButton(this.leftPos + DISTRIBUTION_BUTTONS_X + 24 * 2, topPos + DISTRIBUTION_BUTTONS_Y, 18, 18, 0, 0, 18, RANDOM_BUTTON_TEXTURE, 256, 256, (button) -> {
      getLastFocusedMapping().ifPresent(mapping -> {
        mapping.setDistributionScheme(Mapping.DistributionScheme.RANDOM);
        updateMappingGUI();
        updateMappings();
      });
    }, new TranslatableComponent("gui.graphio.random"), this));

    this.addRenderableWidget(filterSchemeButton = new ToggleImageButton(this.leftPos - 20, topPos + INVENTORY_Y, 20, 18, 0, 0, 19, FILTER_SCHEME_BUTTON_TEXTURE, 256, 256, (button) -> {
      getLastFocusedMapping().ifPresent(mapping -> {
        if (filterSchemeButton.isEnabled())
          mapping.setFilterScheme(Mapping.FilterScheme.BLACK_LIST);
        else
          mapping.setFilterScheme(Mapping.FilterScheme.WHITE_LIST);
        updateMappingGUI();
        updateMappings();
      });
    }, new TranslatableComponent("gui.graphio.black_list"), new TranslatableComponent("gui.graphio.white_list"), this));

    this.addRenderableWidget(decreaseStackSizeButton = new ImageButton(this.leftPos - 80, topPos + 28, 11, 11, 0, 0, 11, MINUS_BUTTON_TEXTURE, 256, 256, (button) -> {
      getLastFocusedMapping().ifPresent(mapping -> {
        mapping.changeItemsPerTick(-1);
        updateMappingGUI();
        updateMappings();
      });
    }, new TranslatableComponent("gui.graphio.decrease_stack_size")));

    this.addRenderableWidget(increaseStackSizeButton = new ImageButton(this.leftPos - 50, topPos + 28, 11, 11, 0, 0, 11, PLUS_BUTTON_TEXTURE, 256, 256, (button) -> {
      getLastFocusedMapping().ifPresent(mapping -> {
        mapping.changeItemsPerTick(1);
        updateMappingGUI();
        updateMappings();
      });
    }, new TranslatableComponent("gui.graphio.decrease_stack_size")));

    this.addRenderableWidget(decreaseBucketsButton = new ImageButton(this.leftPos - 80, topPos + 59, 11, 11, 0, 0, 11, MINUS_BUTTON_TEXTURE, 256, 256, (button) -> {
      getLastFocusedMapping().ifPresent(mapping -> {
        mapping.changeBucketsPerTick(-1 * (hasShiftDown() ? 1000 : 100));
        updateMappingGUI();
        updateMappings();
      });
    }, new TranslatableComponent("gui.graphio.decrease_stack_size")));

    this.addRenderableWidget(increaseBucketsButton = new ImageButton(this.leftPos - 50, topPos + 59, 11, 11, 0, 0, 11, PLUS_BUTTON_TEXTURE, 256, 256, (button) -> {
      getLastFocusedMapping().ifPresent(mapping -> {
        mapping.changeBucketsPerTick(hasShiftDown() ? 1000 : 100);
        updateMappingGUI();
        updateMappings();
      });
    }, new TranslatableComponent("gui.graphio.decrease_stack_size")));

    this.addRenderableWidget(decreaseEnergyButton = new ImageButton(this.leftPos - 80, topPos + 90, 11, 11, 0, 0, 11, MINUS_BUTTON_TEXTURE, 256, 256, (button) -> {
      getLastFocusedMapping().ifPresent(mapping -> {
        mapping.changeEnergyPerTick(-1 * (hasShiftDown() ? 1000 : 100));
        updateMappingGUI();
        updateMappings();
      });
    }, new TranslatableComponent("gui.graphio.decrease_stack_size")));

    this.addRenderableWidget(increaseEnergyButton = new ImageButton(this.leftPos - 50, topPos + 90, 11, 11, 0, 0, 11, PLUS_BUTTON_TEXTURE, 256, 256, (button) -> {
      getLastFocusedMapping().ifPresent(mapping -> {
        mapping.changeEnergyPerTick(hasShiftDown() ? 100 : 1);
        updateMappingGUI();
        updateMappings();
      });
    }, new TranslatableComponent("gui.graphio.decrease_stack_size")));

    this.addRenderableWidget(decreaseTickDelay = new ImageButton(this.leftPos - 80, topPos + 121, 11, 11, 0, 0, 11, MINUS_BUTTON_TEXTURE, 256, 256, (button) -> {
      getLastFocusedMapping().ifPresent(mapping -> {
        mapping.changeTickDelay(-1);
        updateMappingGUI();
        updateMappings();
      });
    }, new TranslatableComponent("gui.graphio.decrease_stack_size")));

    this.addRenderableWidget(increaseTickDelay = new ImageButton(this.leftPos - 50, topPos + 121, 11, 11, 0, 0, 11, PLUS_BUTTON_TEXTURE, 256, 256, (button) -> {
      getLastFocusedMapping().ifPresent(mapping -> {
        mapping.changeTickDelay(1);
        updateMappingGUI();
        updateMappings();
      });
    }, new TranslatableComponent("gui.graphio.decrease_stack_size")));

  }

  public Optional<Mapping> getLastFocusedMapping() {
    if (lastFocusedMapping < 0 || lastFocusedMapping >= getMappings().size()) return Optional.empty();
    return Optional.of(getMappings().get(lastFocusedMapping));
  }

  public Optional<EditBox> getLastFocusedMappingTF() {
    if (lastFocusedMapping < 0 || lastFocusedMapping >= rawMappings.size()) return Optional.empty();
    return Optional.of(rawMappings.get(lastFocusedMapping));
  }

  private void createMappingTextFields() {
    rawMappings.clear();
    for (int i = 0; i < getMappings().size(); ++i) {
      EditBox mapping = createMappingTextField(getMappings().get(i).getRaw(), i);
      if (i >= MAPPINGS_PER_PAGE)
        mapping.setVisible(false);
      addRenderableWidget(mapping);
      rawMappings.add(mapping);
    }
  }

  public EditBox createMappingTextField(String contents, int index) {
    EditBox mapping = new EditBox(font, leftPos + MAPPING_X, topPos + MAPPING_Y + (index % 5) * (MAPPING_HEIGHT + 6), MAPPING_WIDTH, MAPPING_HEIGHT, new TranslatableComponent("container.repair"));
    mapping.setCanLoseFocus(true);
    mapping.setTextColor(TEXT_COLOR);
    mapping.setTextColorUneditable(-1);
    mapping.setBordered(true);
    mapping.setResponder(this::onTextChanged);
    mapping.setMaxLength(40);
    mapping.setEditable(true);
    mapping.setValue(contents);
    return mapping;
  }

  public boolean mouseDragged(double mouseX, double mouseY, int mouseButton, double dragX, double dragY) {
    if (isScrolling) {
      int i = topPos + SCROLL_BAR_Y;
      currentScroll = (float) Mth.clamp((mouseY - i - (SCROLL_BAR_HEIGHT / 2.0D)) / (SCROLL_AREA_HEIGHT - SCROLL_BAR_HEIGHT), 0.0D, 1.0D);
      scrollTo(currentScroll);
      return true;
    } else
      return super.mouseDragged(mouseX, mouseY, mouseButton, dragX, dragY);
  }

  /**
   * Updates the mappings position int the gui when scrolled.
   */
  public void scrollTo(float pos) {
    // The current scroll pos in as an index of the scrollable items
    int discretePos = (int) ((rawMappings.size() - 1) * pos);
    // The index of the top element being displayed
    int discreteTop = Mth.clamp(discretePos, 0, Math.max(0, rawMappings.size() - MAPPINGS_PER_PAGE));

    for (int mappingNum = 0; mappingNum < rawMappings.size(); ++mappingNum) {
      EditBox mapping = rawMappings.get(mappingNum);
      if (mappingNum >= discreteTop && mappingNum < discreteTop + MAPPINGS_PER_PAGE) {
        mapping.y = topPos + MAPPING_Y + (mappingNum - discreteTop) * (MAPPING_HEIGHT + 6);
        mapping.setVisible(true);
      } else
        mapping.setVisible(false);
    }

  }

  public boolean mouseClicked(double mouseX, double mouseY, int button) {
    if (canScroll(rawMappings.size()) && mouseX > (double) (leftPos + SCROLL_BAR_X) && mouseX < (double) (leftPos + SCROLL_BAR_X + SCROLL_BAR_WIDTH) && mouseY > (double) (topPos + SCROLL_BAR_Y) && mouseY <= (double) (topPos + SCROLL_BAR_Y + SCROLL_AREA_HEIGHT))
      isScrolling = true;
    return super.mouseClicked(mouseX, mouseY, button);
  }

  @Override
  protected boolean checkHotbarKeyPressed(int keyCode, int scanCode) {
    return false;
  }

  @Override
  public boolean mouseReleased(double mouseX, double mouseY, int button) {
    isScrolling = false;
    return super.mouseReleased(mouseX, mouseY, button);
  }

  @Override
  @ParametersAreNonnullByDefault
  protected void renderBg(PoseStack matrixStack, float partialTicks, int x, int y) {
    //RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
    RenderSystem.setShaderTexture(0, BACKGROUND);
    blit(matrixStack, leftPos, topPos, getBlitOffset(), BACKGROUND_TEXTURE_X, BACKGROUND_TEXTURE_Y, BACKGROUND_TEXTURE_WIDTH, BACKGROUND_TEXTURE_HEIGHT, 256, 512);
    blit(matrixStack, leftPos - 89, topPos, getBlitOffset(), 276, 27, 89, 166, 256, 512);
    renderScrollbar(matrixStack);
    getLastFocusedMapping().ifPresent(mapping -> {
      // Draw the filter slots
      for (int i = 0; i < mapping.getFilterInventory().getContainerSize(); ++i)
        blit(matrixStack, leftPos + 4 + (i % 5) * SLOT_SIZE, topPos + INVENTORY_Y - 1 + (i >= 5 ? SLOT_SIZE : 0), getBlitOffset(), SLOT_TEXTURE_X, SLOT_TEXTURE_Y, SLOT_TEXTURE_WIDTH, SLOT_TEXTURE_HEIGHT, 256, 512);
    });
  }

  @Override
  @ParametersAreNonnullByDefault
  protected void renderLabels(PoseStack matrixStack, int x, int y) {
    super.renderLabels(matrixStack, x, y);
    font.draw(matrixStack, playerInventoryTitle, (float) inventoryLabelX, (float) inventoryLabelY, 4210752);

    String itemsPerTickStr = "?";
    String bucketsPerTickStr = "?";
    String energyPerTickStr = "?";
    String tickDelayStr = "?";
    String energyStr = routerBlockEntity.getEnergyStorage().getEnergyStored() / 1000 + "/" + routerBlockEntity.getEnergyStorage().getMaxEnergyStored() / 1000;
    if (getLastFocusedMapping().isPresent()) {
      Mapping focused = getLastFocusedMapping().get();
      itemsPerTickStr = focused.getItemsPerTick() + "";
      bucketsPerTickStr = focused.getBucketsPerTick() + "";
      energyPerTickStr = focused.getEnergyPerTick() + "";
      tickDelayStr = focused.getTickDelay() + "";
    }
    font.draw(matrixStack, ITEMS_PER_TICK, -80, 10, TEXT_COLOR);
    font.draw(matrixStack, itemsPerTickStr, -80, 19, TEXT_COLOR);
    font.draw(matrixStack, BUCKETS_PER_TICK, -80, 41, TEXT_COLOR);
    font.draw(matrixStack, bucketsPerTickStr, -80, 50, TEXT_COLOR);
    font.draw(matrixStack, ENERGY_PER_TICK, -80, 72, TEXT_COLOR);
    font.draw(matrixStack, energyPerTickStr, -80, 81, TEXT_COLOR);
    font.draw(matrixStack, TICK_DELAY, -80, 103, TEXT_COLOR);
    font.draw(matrixStack, tickDelayStr, -80, 112, TEXT_COLOR);
    font.draw(matrixStack, ENERGY, -80, 134, TEXT_COLOR);
    font.draw(matrixStack, energyStr, -80, 144, TEXT_COLOR);
  }

  private boolean canScroll(int items) {
    return items > MAPPINGS_PER_PAGE;
  }

  public boolean mouseScrolled(double mouseX, double mouseY, double scroll) {
    if (canScroll(rawMappings.size())) {
      currentScroll = (float) Mth.clamp((currentScroll - scroll / rawMappings.size()), 0.0D, 1.0D);
      scrollTo(currentScroll);
    }
    return true;
  }

  public void update() {
    final int cursorPos = getLastFocusedMappingTF().isPresent() ? getLastFocusedMappingTF().get().getCursorPosition() : -1;
    for (EditBox rawMapping : rawMappings)
      removeWidget(rawMapping);
    createMappingTextFields();
    getLastFocusedMappingTF().ifPresentOrElse((focused) -> {
      setFocused(focused);
      focused.setFocus(true);
      if (cursorPos != -1)
        focused.setCursorPosition(cursorPos);
    }, () -> {
      lastFocusedMapping = getMappings().size() - 1;
    });
    scrollTo(currentScroll);
  }

}
