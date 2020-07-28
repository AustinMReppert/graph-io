package xyz.austinmreppert.graph_io.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.IHasContainer;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.IContainerListener;
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

import java.util.ArrayList;

public class ControllerNodeScreen extends ContainerScreen<ControllerNodeContainer> implements IHasContainer<ControllerNodeContainer>, IContainerListener {

  public int inventoryRows;
  private float currentScroll;
  private boolean isScrolling;
  private ArrayList<TextFieldWidget> mappings;
  protected TextFieldWidget inputField;

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

  private boolean locked = false;

  private static final ResourceLocation BACKGROUND = new ResourceLocation(GraphIO.MOD_ID, "textures/gui/container/controller_node_gui.png");

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
    mappings = new ArrayList<>();
  }

  private void onTextChanged(String text) {
    if (locked) return;
    ArrayList<String> mappingStrings = new ArrayList<>(mappings.size());
    for (int i = 0; i < mappings.size(); ++i) {
      mappingStrings.add(mappings.get(i).getText());
    }

    CompoundNBT mappingsNBT = new CompoundNBT();
    ListNBT list = new ListNBT();
    for (String s : mappingStrings) {
      CompoundNBT mapping = new CompoundNBT();
      mapping.putString("mapping", s);
      list.add(mapping);
    }
    mappingsNBT.put("mappings", list);
    PacketHander.INSTANCE.sendToServer(new SetMappingsPacket(container.getControllerNodeTE().getPos(), mappingsNBT));
  }

  public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
    if (keyCode == GLFW.GLFW_KEY_ESCAPE)
      minecraft.player.closeScreen();
    else if (keyCode == GLFW.GLFW_KEY_ENTER && !locked) {
      for (int i = 0; i < mappings.size(); ++i) {
        mappings.get(i).setFocused2(false);
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
      if (canScroll(mappings.size()))
        currentScroll = 1.0F;
      else
        currentScroll = (float) mappings.size() / MAPPINGS_PER_PAGE;
      children.add(mapping);
      mappings.add(mapping);
      scrollTo(currentScroll);
      mapping.setFocused2(true);
      setListener(mapping);
      return true;
    } else if (keyCode == GLFW.GLFW_KEY_DELETE && !locked && getListener() != null && getListener() instanceof TextFieldWidget && ((TextFieldWidget) getListener()).canWrite()) {
      if (mappings.size() < 1) return true;
      int index = mappings.indexOf(getListener());
      currentScroll = (currentScroll * mappings.size() - 1) / (mappings.size() - 1);
      mappings.remove(index);
      children.remove(getListener());
      scrollTo(currentScroll);
      onTextChanged("");
      return true;
    } else if (getListener() != null && !locked && getListener() instanceof TextFieldWidget && ((TextFieldWidget) getListener()).canWrite()) {
      return getListener().keyPressed(keyCode, scanCode, modifiers);
    }
    return super.keyPressed(keyCode, scanCode, modifiers);
  }

  public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
    renderBackground(matrixStack);
    super.render(matrixStack, mouseX, mouseY, partialTicks);
    func_230459_a_(matrixStack, mouseX, mouseY);

    for (TextFieldWidget mapping : mappings)
      mapping.render(matrixStack, mouseX, mouseY, partialTicks);
  }

  public void renderScrollbar(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
    if (canScroll(mappings.size()))
      blit(matrixStack, guiLeft + SCROLL_BAR_X, (int) (guiTop + SCROLL_BAR_Y + currentScroll * (SCROLL_AREA_HEIGHT - SCROLL_BAR_HEIGHT)), getBlitOffset(), SCROLL_BAR_TEXTURE_X, SCROLL_BAR_TEXTURE_Y, SCROLL_BAR_WIDTH, SCROLL_BAR_HEIGHT, 256, 512);
    else
      blit(matrixStack, guiLeft + SCROLL_BAR_X, (int) (guiTop + SCROLL_BAR_Y + currentScroll * (SCROLL_AREA_HEIGHT - SCROLL_BAR_HEIGHT)), getBlitOffset(), SCROLL_BAR_INACTIVE_TEXTURE_X, SCROLL_BAR_INACTIVE_TEXTURE_Y, SCROLL_BAR_WIDTH, SCROLL_BAR_HEIGHT, 256, 512);
  }

  @Override
  public void init() {
    super.init();
    locked = true;
    mappings.clear();
    for (int i = 0; i < container.getControllerNodeTE().getMappings().size(); ++i) {
      TextFieldWidget mapping = new TextFieldWidget(font, guiLeft + MAPPING_X, guiTop + MAPPING_Y + (i % 5) * (MAPPING_HEIGHT + 6), MAPPING_WIDTH, MAPPING_HEIGHT, new TranslationTextComponent("container.repair"));
      mapping.setCanLoseFocus(true);
      mapping.setTextColor(Color.func_240745_a_("#FFFFFF").func_240742_a_());
      mapping.setDisabledTextColour(-1);
      mapping.setEnableBackgroundDrawing(true);
      mapping.setResponder(this::onTextChanged);
      mapping.setMaxStringLength(40);
      mapping.setEnabled(true);
      mapping.setText(container.getControllerNodeTE().getMappings().get(i).getRaw());
      if (i >= MAPPINGS_PER_PAGE)
        mapping.setVisible(false);
      children.add(mapping);
      mappings.add(mapping);
    }
    scrollTo(currentScroll);
    locked = false;
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
    int discretePos = (int) ((mappings.size() - 1) * pos);
    // The index of the top element being displayed
    int discreteTop = MathHelper.clamp(discretePos, 0, Math.max(0, mappings.size() - MAPPINGS_PER_PAGE));

    for (int mappingNum = 0; mappingNum < mappings.size(); ++mappingNum) {
      TextFieldWidget mapping = mappings.get(mappingNum);
      if (mappingNum >= discreteTop && mappingNum < discreteTop + MAPPINGS_PER_PAGE) {
        mapping.y = (int) (guiTop + MAPPING_Y + (mappingNum - discreteTop) * (MAPPING_HEIGHT + 6));
        mapping.setVisible(true);
      } else
        mapping.setVisible(false);
    }

  }

  public boolean mouseClicked(double mouseX, double mouseY, int button) {
    if (canScroll(mappings.size()) && mouseX > (double) (guiLeft + SCROLL_BAR_X) && mouseX < (double) (guiLeft + SCROLL_BAR_X + SCROLL_BAR_WIDTH) && mouseY > (double) (guiTop + SCROLL_BAR_Y) && mouseY <= (double) (guiTop + SCROLL_BAR_Y + SCROLL_AREA_HEIGHT))
      isScrolling = true;
    return super.mouseClicked(mouseX, mouseY, button);
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
    if (canScroll(mappings.size())) {
      currentScroll = (float) MathHelper.clamp((currentScroll - scroll / mappings.size()), 0.0D, 1.0D);
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
