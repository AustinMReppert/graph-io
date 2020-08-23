package xyz.austinmreppert.graph_io.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.IHasContainer;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.ImageButton;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.Color;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import org.lwjgl.glfw.GLFW;
import xyz.austinmreppert.graph_io.GraphIO;
import xyz.austinmreppert.graph_io.container.RouterContainer;
import xyz.austinmreppert.graph_io.data.mappings.Mapping;
import xyz.austinmreppert.graph_io.network.PacketHander;
import xyz.austinmreppert.graph_io.network.SetMappingsPacket;
import xyz.austinmreppert.graph_io.tileentity.RouterTE;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.Optional;

public class RouterScreen extends ContainerScreen<RouterContainer> implements IHasContainer<RouterContainer> {

  private static final ResourceLocation BACKGROUND = new ResourceLocation(GraphIO.MOD_ID, "textures/gui/container/router.png");
  private static final ResourceLocation RECIPE_BUTTON_TEXTURE = new ResourceLocation(GraphIO.MOD_ID, "textures/gui/cyclic_button.png");
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
  private final int DISTRIBUTION_BUTTONS_Y = HOTBAR_Y;
  private final int TEXT_COLOR = Color.func_240745_a_("#FFFFFF").func_240742_a_();
  public int inventoryRows;
  protected TextFieldWidget inputField;
  private float currentScroll;
  private boolean isScrolling;
  private ArrayList<TextFieldWidget> rawMappings;
  private boolean locked = false;
  private ITextComponent ITEMS_PER_TICK = new TranslationTextComponent("graphio.gui.items_per_tick");
  private ITextComponent BUCKETS_PER_TICK = new TranslationTextComponent("graphio.gui.buckets_per_tick");
  private ITextComponent ENERGY_PER_TICK = new TranslationTextComponent("graphio.gui.energy_per_tick");
  private ITextComponent TICK_DELAY = new TranslationTextComponent("graphio.gui.tick_delay");
  private ITextComponent ENERGY = new TranslationTextComponent("graphio.gui.energy");
  private ToggleImageButton distributeRandomlyButton;
  private ToggleImageButton distributeCyclicallyButton;
  private ToggleImageButton distributeNaturallyButton;
  private ToggleImageButton filterSchemeButton;
  private ImageButton decreaseStackSizeButton, increaseStackSizeButton, decreaseBucketsButton, increaseBucketsButton,
    decreaseEnergyButton, increaseEnergyButton, decreaseTickDelay, increaseTickDelay;
  private int lastFocusedMapping;
  private RouterTE routerTE;

  public RouterScreen(Container screenContainer, PlayerInventory inv, ITextComponent titleIn) {
    super((RouterContainer) screenContainer, inv, titleIn);
    routerTE = ((RouterContainer) screenContainer).getRouterTE();
    passEvents = false;
    xSize = 276;
    ySize = 256;
    width = 276;
    height = 256;
    // Inventory start
    playerInventoryTitleX = 107;
    playerInventoryTitleY = 162;
    rawMappings = new ArrayList<>();
  }

  private void onTextChanged(String text) {
  }

  private void updateMappingGUI() {
    getLastFocusedMapping().ifPresentOrElse((mapping) -> {
      Mapping.DistributionScheme mappingDistributionScheme = mapping.getDistributionScheme();
      if (mappingDistributionScheme == Mapping.DistributionScheme.NATURAL) {
        distributeNaturallyButton.setEnabled(true);
        distributeCyclicallyButton.setEnabled(false);
        distributeRandomlyButton.setEnabled(false);
      } else if (mappingDistributionScheme == Mapping.DistributionScheme.CYCLIC) {
        distributeNaturallyButton.setEnabled(false);
        distributeCyclicallyButton.setEnabled(true);
        distributeRandomlyButton.setEnabled(false);
      } else if (mappingDistributionScheme == Mapping.DistributionScheme.RANDOM) {
        distributeNaturallyButton.setEnabled(false);
        distributeCyclicallyButton.setEnabled(false);
        distributeRandomlyButton.setEnabled(true);
      } else {
        distributeNaturallyButton.setEnabled(false);
        distributeCyclicallyButton.setEnabled(false);
        distributeRandomlyButton.setEnabled(false);
      }

      Mapping.FilterScheme mappingFilterScheme = mapping.getFilterScheme();
      if (mappingFilterScheme == Mapping.FilterScheme.BLACK_LIST)
        filterSchemeButton.setEnabled(false);
      else if (mappingFilterScheme == Mapping.FilterScheme.WHITE_LIST)
        filterSchemeButton.setEnabled(true);
    }, () -> {
      distributeNaturallyButton.setEnabled(false);
      distributeCyclicallyButton.setEnabled(false);
      distributeRandomlyButton.setEnabled(false);
      // TODO: Disable buttons on the left
    });
  }

  private void updateMappings() {
    PacketHander.INSTANCE.sendToServer(new SetMappingsPacket(routerTE.getPos(), Mapping.toNBT(getMappings()), container.windowId));
  }

  public ArrayList<Mapping> getMappings() {
    return container.getTrackedMappingsReference().get.get();
  }

  @Override
  public boolean charTyped(char p_231042_1_, int p_231042_2_) {
    if (getListener() != null && getListener() instanceof TextFieldWidget && ((TextFieldWidget) getListener()).canWrite()) {
      TextFieldWidget listener = (TextFieldWidget) getListener();
      int index = rawMappings.indexOf(listener);
      boolean pressed = listener.charTyped(p_231042_1_, p_231042_2_);
      if (index != -1) {
        getMappings().get(index).setRaw(listener.getText());
        updateMappings();
      }
      return pressed;
    }
    return false;
  }

  public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
    if (keyCode == GLFW.GLFW_KEY_ESCAPE)
      minecraft.player.closeScreen();
    else if (keyCode == GLFW.GLFW_KEY_ENTER && !locked) {
      for (int i = 0; i < rawMappings.size(); ++i) {
        rawMappings.get(i).setFocused2(false);
      }
      TextFieldWidget mapping = new TextFieldWidget(font, guiLeft + MAPPING_X, guiTop + MAPPING_Y * (font.FONT_HEIGHT + 6), MAPPING_WIDTH, MAPPING_HEIGHT, new TranslationTextComponent("container.repair"));
      mapping.setCanLoseFocus(true);
      mapping.setTextColor(TEXT_COLOR);
      mapping.setDisabledTextColour(-1);
      mapping.setEnableBackgroundDrawing(true);
      mapping.setMaxStringLength(40);
      mapping.setResponder(this::onTextChanged);
      mapping.setText("");
      mapping.setVisible(false);
      mapping.setEnabled(true);
      if (canScroll(rawMappings.size()))
        currentScroll = 1.0F;
      else
        currentScroll = (float) rawMappings.size() / MAPPINGS_PER_PAGE;
      children.add(mapping);
      getMappings().add(new Mapping("", Mapping.DistributionScheme.NATURAL, Mapping.FilterScheme.BLACK_LIST,
        routerTE.getTier()));
      rawMappings.add(mapping);
      scrollTo(currentScroll);
      mapping.setFocused2(true);
      setListener(mapping);
      updateMappings();
      return true;
    } else if (keyCode == GLFW.GLFW_KEY_DELETE && !locked && getListener() != null && getListener() instanceof TextFieldWidget && ((TextFieldWidget) getListener()).canWrite()) {
      if (rawMappings.size() < 1) return true;
      int index = rawMappings.indexOf(getListener());
      if (index != -1) {
        //currentScroll = (currentScroll * rawMappings.size() - 1) / (rawMappings.size() - 1);
        container.disableFilterSlots();
        lastFocusedMapping = -1;
        rawMappings.remove(index);
        getMappings().remove(index);
        children.remove(getListener());
        setListener(null);
        scrollTo(currentScroll);
        lastFocusedMapping = -1;
        updateMappings();
        updateMappingGUI();
      }
      return true;
    } else if (getListener() != null && getListener() instanceof TextFieldWidget && ((TextFieldWidget) getListener()).canWrite()) {
      TextFieldWidget listener = (TextFieldWidget) getListener();
      int index = rawMappings.indexOf(listener);
      boolean pressed = listener.keyPressed(keyCode, scanCode, modifiers);
      if (pressed && index != -1) {
        getMappings().get(index).setRaw(listener.getText());
        updateMappings();
      }
      return pressed;
    }
    return super.keyPressed(keyCode, scanCode, modifiers);
  }

  @ParametersAreNonnullByDefault
  public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
    renderBackground(matrixStack);
    super.render(matrixStack, mouseX, mouseY, partialTicks);
    func_230459_a_(matrixStack, mouseX, mouseY);

    for (TextFieldWidget mapping : rawMappings)
      mapping.render(matrixStack, mouseX, mouseY, partialTicks);
  }

  public void renderScrollbar(MatrixStack matrixStack) {
    if (canScroll(rawMappings.size()))
      blit(matrixStack, guiLeft + SCROLL_BAR_X, (int) (guiTop + SCROLL_BAR_Y + currentScroll * (SCROLL_AREA_HEIGHT - SCROLL_BAR_HEIGHT)), getBlitOffset(), SCROLL_BAR_TEXTURE_X, SCROLL_BAR_TEXTURE_Y, SCROLL_BAR_WIDTH, SCROLL_BAR_HEIGHT, 256, 512);
    else
      blit(matrixStack, guiLeft + SCROLL_BAR_X, (int) (guiTop + SCROLL_BAR_Y + currentScroll * (SCROLL_AREA_HEIGHT - SCROLL_BAR_HEIGHT)), getBlitOffset(), SCROLL_BAR_INACTIVE_TEXTURE_X, SCROLL_BAR_INACTIVE_TEXTURE_Y, SCROLL_BAR_WIDTH, SCROLL_BAR_HEIGHT, 256, 512);
  }

  @Override
  public void setListener(@Nullable IGuiEventListener listener) {
    int index = rawMappings.indexOf(listener);
    if (index != -1) {
      lastFocusedMapping = index;
      updateMappingGUI();
      container.setFilterSlotContents(getMappings().get(index).getFilterInventory());
    }
    super.setListener(listener);
  }

  @Override
  @ParametersAreNonnullByDefault
  protected void handleMouseClick(@Nullable Slot slotIn, int slotId, int mouseButton, ClickType type) {
    if (slotIn instanceof FilterSlot) {
      getLastFocusedMapping().ifPresent((focused) -> {
        super.handleMouseClick(slotIn, slotId, mouseButton, type);
        container.copySlotContents(focused.getFilterInventory());
        updateMappings();
      });
    } else super.handleMouseClick(slotIn, slotId, mouseButton, type);
  }

  @Override
  public void init() {
    super.init();
    lastFocusedMapping = -1;
    createMappingTextFields();
    scrollTo(currentScroll);

    this.addButton(distributeNaturallyButton = new ToggleImageButton(this.guiLeft + DISTRIBUTION_BUTTONS_X, guiTop + DISTRIBUTION_BUTTONS_Y, 20, 18, 0, 0, 19, RECIPE_BUTTON_TEXTURE, 256, 256, (button) -> {
      getLastFocusedMapping().ifPresent(mapping -> {
        mapping.setDistributionScheme(Mapping.DistributionScheme.NATURAL);
        updateMappingGUI();
        updateMappings();
      });
    }, new TranslationTextComponent("gui.graphio.natural"), this));

    this.addButton(distributeCyclicallyButton = new ToggleImageButton(this.guiLeft + DISTRIBUTION_BUTTONS_X + 24, guiTop + DISTRIBUTION_BUTTONS_Y, 20, 18, 0, 0, 19, RECIPE_BUTTON_TEXTURE, 256, 256, (button) -> {
      getLastFocusedMapping().ifPresent(mapping -> {
        mapping.setDistributionScheme(Mapping.DistributionScheme.CYCLIC);
        updateMappingGUI();
        updateMappings();
      });
    }, new TranslationTextComponent("gui.graphio.cyclic"), this));

    this.addButton(distributeRandomlyButton = new ToggleImageButton(this.guiLeft + DISTRIBUTION_BUTTONS_X + 24 * 2, guiTop + DISTRIBUTION_BUTTONS_Y, 20, 18, 0, 0, 19, RECIPE_BUTTON_TEXTURE, 256, 256, (button) -> {
      getLastFocusedMapping().ifPresent(mapping -> {
        mapping.setDistributionScheme(Mapping.DistributionScheme.RANDOM);
        updateMappingGUI();
        updateMappings();
      });
    }, new TranslationTextComponent("gui.graphio.random"), this));

    this.addButton(filterSchemeButton = new ToggleImageButton(this.guiLeft - 20, guiTop + INVENTORY_Y, 20, 18, 0, 0, 19, FILTER_SCHEME_BUTTON_TEXTURE, 256, 256, (button) -> {
      getLastFocusedMapping().ifPresent(mapping -> {
        if (filterSchemeButton.isEnabled())
          mapping.setFilterScheme(Mapping.FilterScheme.BLACK_LIST);
        else
          mapping.setFilterScheme(Mapping.FilterScheme.WHITE_LIST);
        updateMappingGUI();
        updateMappings();
      });
    }, new TranslationTextComponent("gui.graphio.black_list"), new TranslationTextComponent("gui.graphio.white_list"), this));

    this.addButton(decreaseStackSizeButton = new ImageButton(this.guiLeft - 80, guiTop + 28, 11, 11, 0, 0, 11, MINUS_BUTTON_TEXTURE, 256, 256, (button) -> {
      getLastFocusedMapping().ifPresent(mapping -> {
        mapping.changeItemsPerTick(-1);
        updateMappingGUI();
        updateMappings();
      });
    }, new TranslationTextComponent("gui.graphio.decrease_stack_size")));

    this.addButton(increaseStackSizeButton = new ImageButton(this.guiLeft - 50, guiTop + 28, 11, 11, 0, 0, 11, PLUS_BUTTON_TEXTURE, 256, 256, (button) -> {
      getLastFocusedMapping().ifPresent(mapping -> {
        mapping.changeItemsPerTick(1);
        updateMappingGUI();
        updateMappings();
      });
    }, new TranslationTextComponent("gui.graphio.decrease_stack_size")));

    this.addButton(decreaseBucketsButton = new ImageButton(this.guiLeft - 80, guiTop + 59, 11, 11, 0, 0, 11, MINUS_BUTTON_TEXTURE, 256, 256, (button) -> {
      getLastFocusedMapping().ifPresent(mapping -> {
        mapping.changeBucketsPerTick(-1 * (hasShiftDown() ? 1000 : 100));
        updateMappingGUI();
        updateMappings();
      });
    }, new TranslationTextComponent("gui.graphio.decrease_stack_size")));

    this.addButton(increaseBucketsButton = new ImageButton(this.guiLeft - 50, guiTop + 59, 11, 11, 0, 0, 11, PLUS_BUTTON_TEXTURE, 256, 256, (button) -> {
      getLastFocusedMapping().ifPresent(mapping -> {
        mapping.changeBucketsPerTick(hasShiftDown() ? 1000 : 100);
        updateMappingGUI();
        updateMappings();
      });
    }, new TranslationTextComponent("gui.graphio.decrease_stack_size")));

    this.addButton(decreaseEnergyButton = new ImageButton(this.guiLeft - 80, guiTop + 90, 11, 11, 0, 0, 11, MINUS_BUTTON_TEXTURE, 256, 256, (button) -> {
      getLastFocusedMapping().ifPresent(mapping -> {
        mapping.changeEnergyPerTick(-1 * (hasShiftDown() ? 1000 : 100));
        updateMappingGUI();
        updateMappings();
      });
    }, new TranslationTextComponent("gui.graphio.decrease_stack_size")));

    this.addButton(increaseEnergyButton = new ImageButton(this.guiLeft - 50, guiTop + 90, 11, 11, 0, 0, 11, PLUS_BUTTON_TEXTURE, 256, 256, (button) -> {
      getLastFocusedMapping().ifPresent(mapping -> {
        mapping.changeEnergyPerTick(hasShiftDown() ? 100 : 1);
        updateMappingGUI();
        updateMappings();
      });
    }, new TranslationTextComponent("gui.graphio.decrease_stack_size")));

    this.addButton(decreaseTickDelay = new ImageButton(this.guiLeft - 80, guiTop + 121, 11, 11, 0, 0, 11, MINUS_BUTTON_TEXTURE, 256, 256, (button) -> {
      getLastFocusedMapping().ifPresent(mapping -> {
        mapping.changeTickDelay(-1);
        updateMappingGUI();
        updateMappings();
      });
    }, new TranslationTextComponent("gui.graphio.decrease_stack_size")));

    this.addButton(increaseTickDelay = new ImageButton(this.guiLeft - 50, guiTop + 121, 11, 11, 0, 0, 11, PLUS_BUTTON_TEXTURE, 256, 256, (p_214076_1_) -> {
      getLastFocusedMapping().ifPresent(mapping -> {
        mapping.changeTickDelay(1);
        updateMappingGUI();
        updateMappings();
      });
    }, new TranslationTextComponent("gui.graphio.decrease_stack_size")));

  }

  public Optional<Mapping> getLastFocusedMapping() {
    if (lastFocusedMapping < 0 || lastFocusedMapping >= getMappings().size()) return Optional.empty();
    return Optional.of(getMappings().get(lastFocusedMapping));
  }

  public Optional<TextFieldWidget> getLastFocusedMappingTF() {
    if (lastFocusedMapping < 0 || lastFocusedMapping >= rawMappings.size()) return Optional.empty();
    return Optional.of(rawMappings.get(lastFocusedMapping));
  }

  private void createMappingTextFields() {
    rawMappings.clear();
    for (int i = 0; i < getMappings().size(); ++i) {
      TextFieldWidget mapping = new TextFieldWidget(font, guiLeft + MAPPING_X, guiTop + MAPPING_Y + (i % 5) * (MAPPING_HEIGHT + 6), MAPPING_WIDTH, MAPPING_HEIGHT, new TranslationTextComponent("container.repair"));
      mapping.setCanLoseFocus(true);
      mapping.setTextColor(TEXT_COLOR);
      mapping.setDisabledTextColour(-1);
      mapping.setEnableBackgroundDrawing(true);
      mapping.setResponder(this::onTextChanged);
      mapping.setMaxStringLength(40);
      mapping.setEnabled(true);
      mapping.setText(getMappings().get(i).getRaw());
      if (i >= MAPPINGS_PER_PAGE)
        mapping.setVisible(false);
      children.add(mapping);
      rawMappings.add(mapping);
    }
  }

  public boolean mouseDragged(double mouseX, double mouseY, int p_231045_5_, double dragX, double dragY) {
    if (isScrolling) {
      int i = guiTop + SCROLL_BAR_Y;
      currentScroll = (float) MathHelper.clamp((mouseY - i - (SCROLL_BAR_HEIGHT / 2.0D)) / (SCROLL_AREA_HEIGHT - SCROLL_BAR_HEIGHT), 0.0D, 1.0D);
      scrollTo(currentScroll);
      return true;
    } else
      return super.mouseDragged(mouseX, mouseY, p_231045_5_, dragX, dragY);

  }

  /**
   * Updates the mappings position int the gui when scrolled.
   */
  public void scrollTo(float pos) {
    // The current scroll pos in as an index of the scrollable items
    int discretePos = (int) ((rawMappings.size() - 1) * pos);
    // The index of the top element being displayed
    int discreteTop = MathHelper.clamp(discretePos, 0, Math.max(0, rawMappings.size() - MAPPINGS_PER_PAGE));

    for (int mappingNum = 0; mappingNum < rawMappings.size(); ++mappingNum) {
      TextFieldWidget mapping = rawMappings.get(mappingNum);
      if (mappingNum >= discreteTop && mappingNum < discreteTop + MAPPINGS_PER_PAGE) {
        mapping.y = guiTop + MAPPING_Y + (mappingNum - discreteTop) * (MAPPING_HEIGHT + 6);
        mapping.setVisible(true);
      } else
        mapping.setVisible(false);
    }

  }

  public boolean mouseClicked(double mouseX, double mouseY, int button) {
    if (canScroll(rawMappings.size()) && mouseX > (double) (guiLeft + SCROLL_BAR_X) && mouseX < (double) (guiLeft + SCROLL_BAR_X + SCROLL_BAR_WIDTH) && mouseY > (double) (guiTop + SCROLL_BAR_Y) && mouseY <= (double) (guiTop + SCROLL_BAR_Y + SCROLL_AREA_HEIGHT))
      isScrolling = true;
    return super.mouseClicked(mouseX, mouseY, button);
  }

  @Override
  protected boolean itemStackMoved(int keyCode, int scanCode) {
    return false;
  }

  @Override
  public boolean mouseReleased(double mouseX, double mouseY, int button) {
    isScrolling = false;
    return super.mouseReleased(mouseX, mouseY, button);
  }

  @Override
  @ParametersAreNonnullByDefault
  protected void drawGuiContainerBackgroundLayer(MatrixStack matrixStack, float partialTicks, int x, int y) {
    RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
    minecraft.getTextureManager().bindTexture(BACKGROUND);
    blit(matrixStack, guiLeft, guiTop, getBlitOffset(), BACKGROUND_TEXTURE_X, BACKGROUND_TEXTURE_Y, BACKGROUND_TEXTURE_WIDTH, BACKGROUND_TEXTURE_HEIGHT, 256, 512);
    blit(matrixStack, guiLeft - 89, guiTop, getBlitOffset(), 276, 27, 89, 166, 256, 512);
    renderScrollbar(matrixStack);
    getLastFocusedMapping().ifPresent(mapping -> {
      // Draw the filter slots
      for (int i = 0; i < mapping.getFilterInventory().getSizeInventory(); ++i)
        blit(matrixStack, guiLeft + 4 + (i % 5) * SLOT_SIZE, guiTop + INVENTORY_Y - 1 + (i >= 5 ? SLOT_SIZE : 0), getBlitOffset(), SLOT_TEXTURE_X, SLOT_TEXTURE_Y, SLOT_TEXTURE_WIDTH, SLOT_TEXTURE_HEIGHT, 256, 512);
    });
  }

  @Override
  @ParametersAreNonnullByDefault
  protected void drawGuiContainerForegroundLayer(MatrixStack matrixStack, int x, int y) {
    super.drawGuiContainerForegroundLayer(matrixStack, x, y);
    font.func_238422_b_(matrixStack, playerInventory.getDisplayName(), (float) playerInventoryTitleX, (float) playerInventoryTitleY, 4210752);

    String itemsPerTickStr = "?";
    String bucketsPerTickStr = "?";
    String energyPerTickStr = "?";
    String tickDelayStr = "?";
    String energyStr = routerTE.getEnergyStorage().getEnergyStored() / 1000 + "/" + routerTE.getEnergyStorage().getMaxEnergyStored() / 1000;
    if (getLastFocusedMapping().isPresent()) {
      Mapping focused = getLastFocusedMapping().get();
      itemsPerTickStr = focused.getItemsPerTick() + "";
      bucketsPerTickStr = focused.getBucketsPerTick() + "";
      energyPerTickStr = focused.getEnergyPerTick() + "";
      tickDelayStr = focused.getTickDelay() + "";
    }
    font.func_238422_b_(matrixStack, ITEMS_PER_TICK, -80, 10, TEXT_COLOR);
    font.drawString(matrixStack, itemsPerTickStr, -80, 19, TEXT_COLOR);
    font.func_238422_b_(matrixStack, BUCKETS_PER_TICK, -80, 41, TEXT_COLOR);
    font.drawString(matrixStack, bucketsPerTickStr, -80, 50, TEXT_COLOR);
    font.func_238422_b_(matrixStack, ENERGY_PER_TICK, -80, 72, TEXT_COLOR);
    font.drawString(matrixStack, energyPerTickStr, -80, 81, TEXT_COLOR);
    font.func_238422_b_(matrixStack, TICK_DELAY, -80, 103, TEXT_COLOR);
    font.drawString(matrixStack, tickDelayStr, -80, 112, TEXT_COLOR);
    font.func_238422_b_(matrixStack, ENERGY, -80, 134, TEXT_COLOR);
    font.drawString(matrixStack, energyStr, -80, 144, TEXT_COLOR);
  }

  private boolean canScroll(int items) {
    return items > MAPPINGS_PER_PAGE;
  }

  public boolean mouseScrolled(double mouseX, double mouseY, double scroll) {
    if (canScroll(rawMappings.size())) {
      currentScroll = (float) MathHelper.clamp((currentScroll - scroll / rawMappings.size()), 0.0D, 1.0D);
      scrollTo(currentScroll);
    }
    return true;
  }

  public void update() {
    final int cursorPos = getLastFocusedMappingTF().isPresent() ? getLastFocusedMappingTF().get().getCursorPosition() : -1;
    for (TextFieldWidget rawMapping : rawMappings)
      children.remove(rawMapping);
    createMappingTextFields();
    getLastFocusedMappingTF().ifPresentOrElse((focused) -> {
      setListener(focused);
      focused.setFocused2(true);
      if (cursorPos != -1)
        focused.setCursorPosition(cursorPos);
    }, () -> {
      lastFocusedMapping = getMappings().size() - 1;
    });
    scrollTo(currentScroll);
  }

}
