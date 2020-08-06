package xyz.austinmreppert.graph_io.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.IHasContainer;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.IContainerListener;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.Color;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import org.lwjgl.glfw.GLFW;
import xyz.austinmreppert.graph_io.GraphIO;
import xyz.austinmreppert.graph_io.container.ControllerNodeContainer;
import xyz.austinmreppert.graph_io.network.PacketHander;
import xyz.austinmreppert.graph_io.network.SetMappingsPacket;
import xyz.austinmreppert.graph_io.tileentity.Mapping;

import javax.annotation.Nullable;
import java.util.ArrayList;

public class ControllerNodeScreen extends ContainerScreen<ControllerNodeContainer> implements IHasContainer<ControllerNodeContainer>, IContainerListener {

  public int inventoryRows;
  private float currentScroll;
  private boolean isScrolling;
  private ArrayList<TextFieldWidget> rawMappings;
  private ArrayList<Mapping> mappingsCopy;
  protected TextFieldWidget inputField;

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

  private final int DISTRIBUTION_BUTTONS_X = MAPPINGS_AREA_X;
  private final int DISTRIBUTION_BUTTONS_Y = HOTBAR_Y;


  private boolean locked = false;

  private static final ResourceLocation BACKGROUND = new ResourceLocation(GraphIO.MOD_ID, "textures/gui/container/controller_node_gui.png");
  private static final ResourceLocation RECIPE_BUTTON_TEXTURE = new ResourceLocation(GraphIO.MOD_ID, "textures/gui/round_robin.png");

  private ToggleImageButton distributeRandomlyButton;
  private ToggleImageButton distributeCyclicallyButton;
  private ToggleImageButton distributeNaturallyButton;
  private ToggleImageButton filterSchemeButton;
  private int lastFocusedMapping;

  public ControllerNodeScreen(Container screenContainer, PlayerInventory inv, ITextComponent titleIn) {
    super((ControllerNodeContainer) screenContainer, inv, titleIn);
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
    if (locked) return;
  }

  private void updateMappingGUI() {
    if (lastFocusedMapping < 0 || lastFocusedMapping >= mappingsCopy.size()) {
      distributeNaturallyButton.setEnabled(false);
      distributeCyclicallyButton.setEnabled(false);
      distributeRandomlyButton.setEnabled(false);
      return;
    }
    Mapping mapping = mappingsCopy.get(lastFocusedMapping);
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
    if (mappingFilterScheme == Mapping.FilterScheme.BLACK_LIST) {
      filterSchemeButton.setEnabled(false);
    } else if (mappingFilterScheme == Mapping.FilterScheme.WHITE_LIST) {
      filterSchemeButton.setEnabled(true);
    }
  }

  private void updateMappings() {
    PacketHander.INSTANCE.sendToServer(new SetMappingsPacket(container.getControllerNodeTE().getPos(), Mapping.toNBT(mappingsCopy)));
  }

  @Override
  public boolean charTyped(char p_231042_1_, int p_231042_2_) {
    if (getListener() != null && getListener() instanceof TextFieldWidget && ((TextFieldWidget) getListener()).canWrite()) {
      TextFieldWidget listener = (TextFieldWidget) getListener();
      int index = rawMappings.indexOf(listener);
      boolean pressed = listener.charTyped(p_231042_1_, p_231042_2_);
      if (index != -1) {
        mappingsCopy.get(index).setRaw(listener.getText());
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
      mapping.setTextColor(Color.func_240745_a_("#FFFFFF").func_240742_a_());
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
      mappingsCopy.add(new Mapping("", Mapping.DistributionScheme.NATURAL, Mapping.FilterScheme.BLACK_LIST, container.getControllerNodeTE().getFilterSize()));
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
        rawMappings.remove(index);
        mappingsCopy.remove(index);
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
        mappingsCopy.get(index).setRaw(listener.getText());
        updateMappings();
      }
      return pressed;
    }
    return super.keyPressed(keyCode, scanCode, modifiers);
  }

  public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
    renderBackground(matrixStack);
    super.render(matrixStack, mouseX, mouseY, partialTicks);
    func_230459_a_(matrixStack, mouseX, mouseY);

    for (TextFieldWidget mapping : rawMappings)
      mapping.render(matrixStack, mouseX, mouseY, partialTicks);
  }

  public void renderScrollbar(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
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
      for (int i = 0; i < container.getControllerNodeTE().getFilterSize(); ++i) {
        Mapping mapping = mappingsCopy.get(index);
        Inventory tmpFilterInventory = container.getTmpFilterInventory();
        Inventory filterInventory = mapping.getFilterInventory();
        tmpFilterInventory.setInventorySlotContents(i, filterInventory.getStackInSlot(i));
      }
    }
    super.setListener(listener);
  }

  @Override
  protected void handleMouseClick(Slot slotIn, int slotId, int mouseButton, ClickType type) {
    if (slotIn != null && slotIn instanceof FilterSlot && lastFocusedMapping >= 0 && lastFocusedMapping < mappingsCopy.size()) {
      super.handleMouseClick(slotIn, slotId, mouseButton, type);
      for (int i = 0; i < container.getControllerNodeTE().getFilterSize(); ++i) {
        Mapping mapping = mappingsCopy.get(lastFocusedMapping);
        Inventory tmpFilterInventory = container.getTmpFilterInventory();
        Inventory filterInventory = mapping.getFilterInventory();
        filterInventory.setInventorySlotContents(i, tmpFilterInventory.getStackInSlot(i));
      }
      updateMappings();
    } else if (!(slotIn instanceof FilterSlot)) {
      super.handleMouseClick(slotIn, slotId, mouseButton, type);
    }
  }

  @Override
  public void init() {
    super.init();
    lastFocusedMapping = -1;
    rawMappings.clear();
    ArrayList<Mapping> mappings = container.getControllerNodeTE().getMappings();
    mappingsCopy = new ArrayList<Mapping>(mappings.size());
    for (Mapping mapping : mappings)
      mappingsCopy.add(new Mapping(mapping));
    for (int i = 0; i < mappingsCopy.size(); ++i) {
      TextFieldWidget mapping = new TextFieldWidget(font, guiLeft + MAPPING_X, guiTop + MAPPING_Y + (i % 5) * (MAPPING_HEIGHT + 6), MAPPING_WIDTH, MAPPING_HEIGHT, new TranslationTextComponent("container.repair"));
      mapping.setCanLoseFocus(true);
      mapping.setTextColor(Color.func_240745_a_("#FFFFFF").func_240742_a_());
      mapping.setDisabledTextColour(-1);
      mapping.setEnableBackgroundDrawing(true);
      mapping.setResponder(this::onTextChanged);
      mapping.setMaxStringLength(40);
      mapping.setEnabled(true);
      mapping.setText(mappingsCopy.get(i).getRaw());
      if (i >= MAPPINGS_PER_PAGE)
        mapping.setVisible(false);
      children.add(mapping);
      rawMappings.add(mapping);
    }
    scrollTo(currentScroll);

    this.addButton(distributeNaturallyButton = new ToggleImageButton(this.guiLeft + DISTRIBUTION_BUTTONS_X, guiTop + DISTRIBUTION_BUTTONS_Y, 20, 18, 0, 0, 19, RECIPE_BUTTON_TEXTURE, 256, 256, (p_214076_1_) -> {
      if (lastFocusedMapping < 0 || lastFocusedMapping >= mappingsCopy.size()) return;
      Mapping mapping = mappingsCopy.get(lastFocusedMapping);
      mapping.setDistributionScheme(Mapping.DistributionScheme.NATURAL);
      updateMappingGUI();
      updateMappings();
    }, new TranslationTextComponent("gui.graphio.natural"), this));

    this.addButton(distributeCyclicallyButton = new ToggleImageButton(this.guiLeft + DISTRIBUTION_BUTTONS_X + 24, guiTop + DISTRIBUTION_BUTTONS_Y, 20, 18, 0, 0, 19, RECIPE_BUTTON_TEXTURE, 256, 256, (p_214076_1_) -> {
      if (lastFocusedMapping < 0 || lastFocusedMapping >= mappingsCopy.size()) return;
      Mapping mapping = mappingsCopy.get(lastFocusedMapping);
      mapping.setDistributionScheme(Mapping.DistributionScheme.CYCLIC);
      updateMappingGUI();
      updateMappings();
    }, new TranslationTextComponent("gui.graphio.cyclic"), this));

    this.addButton(distributeRandomlyButton = new ToggleImageButton(this.guiLeft + DISTRIBUTION_BUTTONS_X + 24 * 2, guiTop + DISTRIBUTION_BUTTONS_Y, 20, 18, 0, 0, 19, RECIPE_BUTTON_TEXTURE, 256, 256, (p_214076_1_) -> {
      if (lastFocusedMapping < 0 || lastFocusedMapping >= mappingsCopy.size()) return;
      Mapping mapping = mappingsCopy.get(lastFocusedMapping);
      mapping.setDistributionScheme(Mapping.DistributionScheme.RANDOM);
      updateMappingGUI();
      updateMappings();
    }, new TranslationTextComponent("gui.graphio.random"), this));

    this.addButton(filterSchemeButton = new ToggleImageButton(this.guiLeft - 20, guiTop + INVENTORY_Y, 20, 18, 0, 0, 19, RECIPE_BUTTON_TEXTURE, 256, 256, (p_214076_1_) -> {
      if (lastFocusedMapping < 0 || lastFocusedMapping >= mappingsCopy.size()) return;
      Mapping mapping = mappingsCopy.get(lastFocusedMapping);
      if (filterSchemeButton.isEnabled())
        mapping.setFilterScheme(Mapping.FilterScheme.BLACK_LIST);
      else
        mapping.setFilterScheme(Mapping.FilterScheme.WHITE_LIST);
      updateMappingGUI();
      updateMappings();
    }, new TranslationTextComponent("gui.graphio.black_list"), new TranslationTextComponent("gui.graphio.white_list"), this));

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
        mapping.y = (int) (guiTop + MAPPING_Y + (mappingNum - discreteTop) * (MAPPING_HEIGHT + 6));
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
  protected void drawGuiContainerBackgroundLayer(MatrixStack matrixStack, float partialTicks, int x, int y) {
    RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
    minecraft.getTextureManager().bindTexture(BACKGROUND);
    blit(matrixStack, guiLeft, guiTop, getBlitOffset(), BACKGROUND_TEXTURE_X, BACKGROUND_TEXTURE_Y, BACKGROUND_TEXTURE_WIDTH, BACKGROUND_TEXTURE_HEIGHT, 256, 512);
    renderScrollbar(matrixStack, x, y, partialTicks);
  }

  // RenderForeground
  @Override
  protected void drawGuiContainerForegroundLayer(MatrixStack matrixStack, int x, int y) {
    font.func_238422_b_(matrixStack, playerInventory.getDisplayName(), (float) playerInventoryTitleX, (float) playerInventoryTitleY, 4210752);
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

  @Override
  public void sendAllContents(Container containerToSend, NonNullList<ItemStack> itemsList) {
  }

  @Override
  public void sendSlotContents(Container containerToSend, int slotInd, ItemStack stack) {
  }

  @Override
  public void sendWindowProperty(Container containerIn, int varToUpdate, int newValue) {
  }

}
