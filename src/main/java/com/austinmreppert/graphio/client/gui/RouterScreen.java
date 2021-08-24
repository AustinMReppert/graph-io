package com.austinmreppert.graphio.client.gui;

import com.austinmreppert.graphio.GraphIO;
import com.austinmreppert.graphio.blockentity.RouterBlockEntity;
import com.austinmreppert.graphio.container.RouterContainer;
import com.austinmreppert.graphio.data.RedstoneMode;
import com.austinmreppert.graphio.data.mappings.Mapping;
import com.austinmreppert.graphio.network.SetRouterBEMappingsPacket;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.client.renderer.GameRenderer;
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

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * The screen used to show the router's mappings.
 */
public class RouterScreen extends AbstractContainerScreen<RouterContainer> implements MenuAccess<RouterContainer> {

  private static final ResourceLocation BACKGROUND = new ResourceLocation(GraphIO.MOD_ID, "textures/gui/container/router.png");
  private static final ResourceLocation NATURAL_BUTTON_TEXTURE = new ResourceLocation(GraphIO.MOD_ID, "textures/gui/natural_button.png");
  private static final ResourceLocation CYCLIC_BUTTON_TEXTURE = new ResourceLocation(GraphIO.MOD_ID, "textures/gui/cyclic_button.png");
  private static final ResourceLocation REDSTONE_MODE_BUTTON_TEXTURE = new ResourceLocation(GraphIO.MOD_ID, "textures/gui/redstone_mode_button.png");
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
  private final Component ITEMS_PER_UPDATE = new TranslatableComponent(GraphIO.MOD_ID + ".gui.items_per_update");
  private final Component FLUID_PER_UPDATE = new TranslatableComponent(GraphIO.MOD_ID + ".gui.fluid_per_update");
  private final Component ENERGY_PER_UPDATE = new TranslatableComponent(GraphIO.MOD_ID + ".gui.energy_per_update");
  private final Component UPDATE_DELAY = new TranslatableComponent(GraphIO.MOD_ID + ".gui.update_delay");
  private final Component ENERGY = new TranslatableComponent(GraphIO.MOD_ID + ".gui.energy");
  private final Component TICK = new TranslatableComponent(GraphIO.MOD_ID + ".gui.tick");
  public int inventoryRows;
  protected EditBox inputField;
  private float currentScroll;
  private boolean isScrolling;
  private ToggleImageButton distributeRandomlyButton;
  private ToggleImageButton distributeCyclicallyButton;
  private ToggleImageButton distributeNaturallyButton;
  private ToggleImageButton filterSchemeButton;
  private OptionImageButton redstoneModeButton;
  private ImageButton decreaseStackSizeButton, increaseStackSizeButton, decreaseFluidButton, increaseFluidButton,
      decreaseEnergyButton, increaseEnergyButton, decreaseUpdateDelay, increaseUpdateDelay;
  private int lastFocusedMapping;
  private RouterBlockEntity routerBlockEntity;

  public RouterScreen(final AbstractContainerMenu screenContainer, final Inventory inv, final Component titleIn) {
    super((RouterContainer) screenContainer, inv, titleIn);
    routerBlockEntity = ((RouterContainer) screenContainer).getRouterBlockEntity();
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

  /**
   * Called when a mapping's text component is changed.
   *
   * @param text The new text.
   */
  private void onTextChanged(final String text) {
  }

  private void updateMappingGUI() {
    getLastFocusedMapping().ifPresentOrElse((mapping) -> {
      Mapping.DistributionScheme mappingDistributionScheme = mapping.getDistributionScheme();
      distributeNaturallyButton.setEnabled(mappingDistributionScheme == Mapping.DistributionScheme.NATURAL);
      distributeCyclicallyButton.setEnabled(mappingDistributionScheme == Mapping.DistributionScheme.CYCLIC);
      distributeRandomlyButton.setEnabled(mappingDistributionScheme == Mapping.DistributionScheme.RANDOM);
      filterSchemeButton.setEnabled(mapping.getFilterScheme() == Mapping.FilterScheme.WHITE_LIST);
      filterSchemeButton.setHidden(false);
    }, () -> {
      distributeNaturallyButton.setEnabled(false);
      distributeCyclicallyButton.setEnabled(false);
      distributeRandomlyButton.setEnabled(false);
      filterSchemeButton.setHidden(true);
      // TODO: Disable buttons on the left
    });
  }

  /**
   * Gets a list of the current mappings.
   *
   * @return A list of the current mappings.
   */
  public ArrayList<Mapping> getMappings() {
    return menu.getMappings();
  }

  /**
   * Called when a user types. Used to update the mappings.
   *
   * @param typedChar The character that was types.
   * @param modifiers The modifiers such as alt, shift, etc.
   * @return Whether the event was consumed.
   */
  @Override
  public boolean charTyped(final char typedChar, final int modifiers) {
    if (getFocused() != null && getFocused() instanceof EditBox listener && ((EditBox) getFocused()).canConsumeInput()) {
      int index = rawMappings.indexOf(listener);
      boolean pressed = listener.charTyped(typedChar, modifiers);
      if (index != -1) {
        getMappings().get(index).setRaw(listener.getValue());
        menu.setServerMappings();
      }
      return pressed;
    }
    return false;
  }

  /**
   * Used when a keyboard button is pressed.
   *
   * @param keyCode   The key of the button with respect to the OS.
   * @param scanCode  The key of the button.
   * @param modifiers The modifiers such as alt, shift, etc.
   * @return Whether the event was consumed.
   */
  public boolean keyPressed(final int keyCode, final int scanCode, final int modifiers) {
    if (keyCode == GLFW.GLFW_KEY_ESCAPE)
      minecraft.player.closeContainer();
    else if (keyCode == GLFW.GLFW_KEY_ENTER && getMappings().size() < routerBlockEntity.getMaxMappings()) {
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
      menu.setServerMappings();
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
        menu.setServerMappings();
        updateMappingGUI();
      }
      return true;
    } else if (getFocused() != null && getFocused() instanceof EditBox listener && ((EditBox) getFocused()).canConsumeInput()) {
      int index = rawMappings.indexOf(listener);
      boolean pressed = listener.keyPressed(keyCode, scanCode, modifiers);
      if (pressed && index != -1) {
        getMappings().get(index).setRaw(listener.getValue());
        menu.setServerMappings();
      }
      return pressed;
    }
    return super.keyPressed(keyCode, scanCode, modifiers);
  }

  /**
   * Renders the black background, mappings, and tool tips.
   *
   * @param poseStack    The {@link PoseStack} used to render this gui.
   * @param mouseX       The x position of the mouse.
   * @param mouseY       The y position of the mouse.
   * @param partialTicks Ticks since last frame.
   */
  @Override
  @ParametersAreNonnullByDefault
  public void render(final PoseStack poseStack, final int mouseX, final int mouseY, final float partialTicks) {
    redstoneModeButton.setSelected(routerBlockEntity.redstoneMode.ordinal());
    this.renderBackground(poseStack);
    super.render(poseStack, mouseX, mouseY, partialTicks);

    if (this.menu.getCarried().isEmpty() && this.hoveredSlot != null && this.hoveredSlot.hasItem()) {
      this.renderTooltip(poseStack, this.hoveredSlot.getItem(), mouseX, mouseY);
    }

    for (final EditBox mapping : rawMappings)
      mapping.render(poseStack, mouseX, mouseY, partialTicks);
  }

  /**
   * Renders the scrollbar.
   *
   * @param poseStack The {@link PoseStack} used to render the scrollbar.
   */
  public void renderScrollbar(final PoseStack poseStack) {
    if (canScroll(rawMappings.size()))
      blit(poseStack, leftPos + SCROLL_BAR_X, (int) (topPos + SCROLL_BAR_Y + currentScroll * (SCROLL_AREA_HEIGHT - SCROLL_BAR_HEIGHT)), getBlitOffset(), SCROLL_BAR_TEXTURE_X, SCROLL_BAR_TEXTURE_Y, SCROLL_BAR_WIDTH, SCROLL_BAR_HEIGHT, 256, 512);
    else
      blit(poseStack, leftPos + SCROLL_BAR_X, (int) (topPos + SCROLL_BAR_Y + currentScroll * (SCROLL_AREA_HEIGHT - SCROLL_BAR_HEIGHT)), getBlitOffset(), SCROLL_BAR_INACTIVE_TEXTURE_X, SCROLL_BAR_INACTIVE_TEXTURE_Y, SCROLL_BAR_WIDTH, SCROLL_BAR_HEIGHT, 256, 512);
  }

  /**
   * Sets the focus to a certain component.
   *
   * @param component The component to receive focus.
   */
  @Override
  public void setFocused(final @Nullable GuiEventListener component) {
    final int index = rawMappings.indexOf(component);
    if (index != -1) {
      lastFocusedMapping = index;
      updateMappingGUI();
      menu.setFilterSlotContents(getMappings().get(index).getFilterInventory());
    }
    super.setFocused(component);
  }

  /**
   * Called when a slot is clicked. This function is used copy items to filter slots.
   *
   * @param slotIn      The slot that was clicked.
   * @param slotId      The id of the clicked slot.
   * @param mouseButton What mouse button was used.
   * @param type        The type of click.
   */
  @Override
  @ParametersAreNonnullByDefault
  protected void slotClicked(final @Nullable Slot slotIn, final int slotId, final int mouseButton, final ClickType type) {
    if (slotIn instanceof FilterSlot) {
      getLastFocusedMapping().ifPresent((focused) -> {
        super.slotClicked(slotIn, slotId, mouseButton, type);
        menu.copySlotContents(focused.getFilterInventory());
        menu.setServerMappings();
      });
    } else super.slotClicked(slotIn, slotId, mouseButton, type);
  }

  /**
   * Sets up the GUI.
   */
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
        menu.setServerMappings();
      });
    }, new TranslatableComponent("gui.graphio.natural"), this));

    this.addRenderableWidget(distributeCyclicallyButton = new ToggleImageButton(this.leftPos + DISTRIBUTION_BUTTONS_X + 24, topPos + DISTRIBUTION_BUTTONS_Y, 18, 18, 0, 0, 18, CYCLIC_BUTTON_TEXTURE, 256, 256, (button) -> {
      getLastFocusedMapping().ifPresent(mapping -> {
        mapping.setDistributionScheme(Mapping.DistributionScheme.CYCLIC);
        updateMappingGUI();
        menu.setServerMappings();
      });
    }, new TranslatableComponent("gui.graphio.cyclic"), this));

    this.addRenderableWidget(distributeRandomlyButton = new ToggleImageButton(this.leftPos + DISTRIBUTION_BUTTONS_X + 24 * 2, topPos + DISTRIBUTION_BUTTONS_Y, 18, 18, 0, 0, 18, RANDOM_BUTTON_TEXTURE, 256, 256, (button) -> {
      getLastFocusedMapping().ifPresent(mapping -> {
        mapping.setDistributionScheme(Mapping.DistributionScheme.RANDOM);
        updateMappingGUI();
        menu.setServerMappings();
      });
    }, new TranslatableComponent("gui.graphio.random"), this));

    List<Component> options = new ArrayList<>();
    options.add(new TranslatableComponent(GraphIO.MOD_ID + ".gui.redstone_mode_ignored"));
    options.add(new TranslatableComponent(GraphIO.MOD_ID + ".gui.redstone_mode_active"));
    options.add(new TranslatableComponent(GraphIO.MOD_ID + ".gui.redstone_mode_inactive"));
    this.addRenderableWidget(redstoneModeButton = new OptionImageButton(this.leftPos + DISTRIBUTION_BUTTONS_X + 24 * 3, topPos + DISTRIBUTION_BUTTONS_Y, 18, 18, 0, 0, 18, REDSTONE_MODE_BUTTON_TEXTURE, 256, 256, (button) -> {
      routerBlockEntity.redstoneMode = RedstoneMode.valueOf(redstoneModeButton.getSelected());
      menu.setSeverRedstoneMode(RedstoneMode.valueOf(redstoneModeButton.getSelected()));
    }, options, this));
    redstoneModeButton.setSelected(routerBlockEntity.redstoneMode.ordinal());

    this.addRenderableWidget(filterSchemeButton = new ToggleImageButton(this.leftPos - 20, topPos + INVENTORY_Y, 20, 18, 0, 0, 19, FILTER_SCHEME_BUTTON_TEXTURE, 256, 256, (button) -> {
      getLastFocusedMapping().ifPresent(mapping -> {
        if (filterSchemeButton.isEnabled())
          mapping.setFilterScheme(Mapping.FilterScheme.BLACK_LIST);
        else
          mapping.setFilterScheme(Mapping.FilterScheme.WHITE_LIST);
        updateMappingGUI();
        menu.setServerMappings();
      });
    }, new TranslatableComponent("gui.graphio.black_list"), new TranslatableComponent("gui.graphio.white_list"), this));
    filterSchemeButton.setHidden(true);

    this.addRenderableWidget(decreaseStackSizeButton = new ImageButton(this.leftPos - 80, topPos + 28, 11, 11, 0, 0, 11, MINUS_BUTTON_TEXTURE, 256, 256, (button) -> {
      getLastFocusedMapping().ifPresent(mapping -> {
        mapping.changeItemsPerTick(-1);
        updateMappingGUI();
        menu.setServerMappings();
      });
    }, new TranslatableComponent("gui.graphio.decrease_stack_size")));

    this.addRenderableWidget(increaseStackSizeButton = new ImageButton(this.leftPos - 50, topPos + 28, 11, 11, 0, 0, 11, PLUS_BUTTON_TEXTURE, 256, 256, (button) -> {
      getLastFocusedMapping().ifPresent(mapping -> {
        mapping.changeItemsPerTick(1);
        updateMappingGUI();
        menu.setServerMappings();
      });
    }, new TranslatableComponent("gui.graphio.decrease_stack_size")));

    this.addRenderableWidget(decreaseFluidButton = new ImageButton(this.leftPos - 80, topPos + 59, 11, 11, 0, 0, 11, MINUS_BUTTON_TEXTURE, 256, 256, (button) -> {
      getLastFocusedMapping().ifPresent(mapping -> {
        mapping.changeFluidPerUpdate(-1 * (hasShiftDown() ? 1000 : 100));
        updateMappingGUI();
        menu.setServerMappings();
      });
    }, new TranslatableComponent("gui.graphio.decrease_stack_size")));

    this.addRenderableWidget(increaseFluidButton = new ImageButton(this.leftPos - 50, topPos + 59, 11, 11, 0, 0, 11, PLUS_BUTTON_TEXTURE, 256, 256, (button) -> {
      getLastFocusedMapping().ifPresent(mapping -> {
        mapping.changeFluidPerUpdate(hasShiftDown() ? 1000 : 100);
        updateMappingGUI();
        menu.setServerMappings();
      });
    }, new TranslatableComponent("gui.graphio.decrease_stack_size")));

    this.addRenderableWidget(decreaseEnergyButton = new ImageButton(this.leftPos - 80, topPos + 90, 11, 11, 0, 0, 11, MINUS_BUTTON_TEXTURE, 256, 256, (button) -> {
      getLastFocusedMapping().ifPresent(mapping -> {
        mapping.changeEnergyPerUpdate(-1 * (hasShiftDown() ? 1000 : 100));
        updateMappingGUI();
        menu.setServerMappings();
      });
    }, new TranslatableComponent("gui.graphio.decrease_stack_size")));

    this.addRenderableWidget(increaseEnergyButton = new ImageButton(this.leftPos - 50, topPos + 90, 11, 11, 0, 0, 11, PLUS_BUTTON_TEXTURE, 256, 256, (button) -> {
      getLastFocusedMapping().ifPresent(mapping -> {
        mapping.changeEnergyPerUpdate(hasShiftDown() ? 100 : 1);
        updateMappingGUI();
        menu.setServerMappings();
      });
    }, new TranslatableComponent("gui.graphio.decrease_stack_size")));

    this.addRenderableWidget(decreaseUpdateDelay = new ImageButton(this.leftPos - 80, topPos + 121, 11, 11, 0, 0, 11, MINUS_BUTTON_TEXTURE, 256, 256, (button) -> {
      getLastFocusedMapping().ifPresent(mapping -> {
        mapping.changeUpdateDelay(-1);
        updateMappingGUI();
        menu.setServerMappings();
      });
    }, new TranslatableComponent("gui.graphio.decrease_stack_size")));

    this.addRenderableWidget(increaseUpdateDelay = new ImageButton(this.leftPos - 50, topPos + 121, 11, 11, 0, 0, 11, PLUS_BUTTON_TEXTURE, 256, 256, (button) -> {
      getLastFocusedMapping().ifPresent(mapping -> {
        mapping.changeUpdateDelay(1);
        updateMappingGUI();
        menu.setServerMappings();
      });
    }, new TranslatableComponent("gui.graphio.decrease_stack_size")));

  }

  /**
   * Gets the last focused mapping.
   *
   * @return The last focused mapping.
   */
  public Optional<Mapping> getLastFocusedMapping() {
    if (lastFocusedMapping < 0 || lastFocusedMapping >= getMappings().size()) return Optional.empty();
    return Optional.of(getMappings().get(lastFocusedMapping));
  }

  /**
   * Gets the last focused mapping's text field.
   *
   * @return The last focused mapping's text field.
   */
  public Optional<EditBox> getLastFocusedMappingTF() {
    if (lastFocusedMapping < 0 || lastFocusedMapping >= rawMappings.size()) return Optional.empty();
    return Optional.of(rawMappings.get(lastFocusedMapping));
  }

  /**
   * Creates renderable components for the mappings.
   */
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

  /**
   * Creates a {@linK EditBox} from a {@link Mapping}.
   *
   * @param contents The raw contents of the mapping.
   * @param index    The index of the Mapping.
   * @return An {@linK EditBox} with the raw contents of the {@link Mapping}.
   */
  public EditBox createMappingTextField(final String contents, final int index) {
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

  /**
   * Called when the mouse is dragged.
   *
   * @param mouseX      The mouse X position.
   * @param mouseY      The mouse Y position.
   * @param mouseButton The mouse button used while dragging.
   * @param dragX       How much the mouse was dragged in the X direction.
   * @param dragY       How much the mouse was dragged in the U direction.
   * @return Whether the event was consumed.
   */
  public boolean mouseDragged(final double mouseX, final double mouseY, final int mouseButton, final double dragX, final double dragY) {
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
  public void scrollTo(final float pos) {
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

  /**
   * @param mouseX The mouse X position.
   * @param mouseY The mouse Y position.
   * @param button The mouse button that was  used.
   * @return Whether the event was consumed.
   */
  public boolean mouseClicked(final double mouseX, final double mouseY, final int button) {
    if (canScroll(rawMappings.size()) && mouseX > (double) (leftPos + SCROLL_BAR_X) && mouseX < (double) (leftPos + SCROLL_BAR_X + SCROLL_BAR_WIDTH) && mouseY > (double) (topPos + SCROLL_BAR_Y) && mouseY <= (double) (topPos + SCROLL_BAR_Y + SCROLL_AREA_HEIGHT))
      isScrolling = true;
    return super.mouseClicked(mouseX, mouseY, button);
  }

  /**
   * Check if the hotbar keys should be used.
   *
   * @param keyCode  The key of the button with respect to the OS.
   * @param scanCode The key of the button.
   * @return Wether the hotbar keys should be used.
   */
  @Override
  protected boolean checkHotbarKeyPressed(final int keyCode, final int scanCode) {
    return false;
  }

  /**
   * Mouse release event.
   *
   * @param mouseX The X position of the mouse.
   * @param mouseY The Y position of the mouse.
   * @param button The button that was released.
   * @return Whether the event was consumed.
   */
  @Override
  public boolean mouseReleased(final double mouseX, final double mouseY, final int button) {
    isScrolling = false;
    return super.mouseReleased(mouseX, mouseY, button);
  }

  /**
   * Renders the backgrounds textures and slots.
   *
   * @param poseStack    The {@link PoseStack} used to render the GUI.
   * @param partialTicks Ticks since last frame.
   * @param mouseX       The X position of the mouse.
   * @param mouseY       The Y position of the mouse.
   */
  @Override
  @ParametersAreNonnullByDefault
  protected void renderBg(final PoseStack poseStack, final float partialTicks, final int mouseX, final int mouseY) {
    RenderSystem.setShader(GameRenderer::getPositionTexShader);
    RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    RenderSystem.setShaderTexture(0, BACKGROUND);
    blit(poseStack, leftPos, topPos, getBlitOffset(), BACKGROUND_TEXTURE_X, BACKGROUND_TEXTURE_Y, BACKGROUND_TEXTURE_WIDTH, BACKGROUND_TEXTURE_HEIGHT, 256, 512);
    blit(poseStack, leftPos - 89, topPos, getBlitOffset(), 276, 27, 89, 166, 256, 512);
    renderScrollbar(poseStack);
    getLastFocusedMapping().ifPresent(mapping -> {
      // Draw the filter slots
      for (int i = 0; i < mapping.getFilterInventory().getContainerSize(); ++i)
        blit(poseStack, leftPos + 4 + (i % 5) * SLOT_SIZE, topPos + INVENTORY_Y - 1 + (i >= 5 ? SLOT_SIZE : 0), getBlitOffset(), SLOT_TEXTURE_X, SLOT_TEXTURE_Y, SLOT_TEXTURE_WIDTH, SLOT_TEXTURE_HEIGHT, 256, 512);
    });
  }

  /**
   * Renders text.
   *
   * @param poseStack The {@link PoseStack} to render the text with.
   * @param mouseX    The x position of the mouse.
   * @param mouseY    The y position of the mouse.
   */
  @Override
  @ParametersAreNonnullByDefault
  protected void renderLabels(final PoseStack poseStack, final int mouseX, final int mouseY) {
    super.renderLabels(poseStack, mouseX, mouseY);
    font.draw(poseStack, playerInventoryTitle, (float) inventoryLabelX, (float) inventoryLabelY, 4210752);

    String itemsPerUpdateStr = "?";
    String fluidPerUpdateStr = "?";
    String energyPerUpdateStr = "?";
    String updateDelayStr = "?";
    String energyStr = formatEnergy(routerBlockEntity.getEnergyStorage().getEnergyStored()) + "/" + routerBlockEntity.getEnergyStorage().getMaxEnergyStored() / 1000;
    if (getLastFocusedMapping().isPresent()) {
      Mapping focused = getLastFocusedMapping().get();
      itemsPerUpdateStr = focused.getItemsPerUpdate() + "";
      fluidPerUpdateStr = focused.getFluidPerUpdate() + "";
      energyPerUpdateStr = focused.getEnergyPerUpdate() + "";
      updateDelayStr = focused.getUpdateDelay() + " " + TICK.getString();
    }
    font.draw(poseStack, ITEMS_PER_UPDATE, -80, 10, TEXT_COLOR);
    font.draw(poseStack, itemsPerUpdateStr, -80, 19, TEXT_COLOR);
    font.draw(poseStack, FLUID_PER_UPDATE, -80, 41, TEXT_COLOR);
    font.draw(poseStack, fluidPerUpdateStr, -80, 50, TEXT_COLOR);
    font.draw(poseStack, ENERGY_PER_UPDATE, -80, 72, TEXT_COLOR);
    font.draw(poseStack, energyPerUpdateStr, -80, 81, TEXT_COLOR);
    font.draw(poseStack, UPDATE_DELAY, -80, 103, TEXT_COLOR);
    font.draw(poseStack, updateDelayStr, -80, 112, TEXT_COLOR);
    font.draw(poseStack, ENERGY, -80, 134, TEXT_COLOR);
    font.draw(poseStack, energyStr, -80, 144, TEXT_COLOR);
  }

  /**
   * Formats the energy string.
   * @param energyStored The amount of energy.
   * @return A formatted string of {@code energyStored}.
   */
  private String formatEnergy(final int energyStored) {
    return String.format("%.2f", energyStored/1000.0F);
  }

  /**
   * Gets whether the user can scroll.
   *
   * @param numMappings The current number of mappings.
   * @return Whether the user can scroll.
   */
  private boolean canScroll(final int numMappings) {
    return numMappings > MAPPINGS_PER_PAGE;
  }

  /**
   * Mouse scroll event.
   *
   * @param mouseX The x position of the mouse.
   * @param mouseY The y position of the mouse.
   * @param scroll How much was scrolled.
   * @return Whether the event was consumed.
   */
  @Override
  public boolean mouseScrolled(final double mouseX, final double mouseY, final double scroll) {
    if (canScroll(rawMappings.size())) {
      currentScroll = (float) Mth.clamp((currentScroll - scroll / rawMappings.size()), 0.0D, 1.0D);
      scrollTo(currentScroll);
    }
    return true;
  }

  /**
   * Called to force updates due to an external change. Usually this is called from {@link SetRouterBEMappingsPacket}'s handler.
   */
  public void update() {
    final int cursorPos = getLastFocusedMappingTF().isPresent() ? getLastFocusedMappingTF().get().getCursorPosition() : -1;
    final int highlightPos = getLastFocusedMappingTF().isPresent() ? getLastFocusedMappingTF().get().highlightPos : -1;

    for (EditBox rawMapping : rawMappings)
      removeWidget(rawMapping);
    createMappingTextFields();
    getLastFocusedMappingTF().ifPresentOrElse((focused) -> {
      setFocused(focused);
      focused.setFocus(true);
      if (cursorPos != -1)
        focused.setCursorPosition(cursorPos);
      if (highlightPos != -1)
        focused.setHighlightPos(highlightPos);
    }, () -> {
      //lastFocusedMapping = getMappings().size() - 1;
    });
    scrollTo(currentScroll);
  }

  /**
   * Gets the redstone mode button.
   * @return The redstone  mode button.
   */
  public OptionImageButton getRedstoneModeButton() {
    return redstoneModeButton;
  }

}
