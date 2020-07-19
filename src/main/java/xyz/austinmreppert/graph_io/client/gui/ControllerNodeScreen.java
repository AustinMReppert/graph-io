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

  private final int MAX_SCROLL = 200;
  private final int SCROLL_BAR_X = 94;
  private final int SCROLL_BAR_Y = 18;
  private final int SCROLL_BAR_WIDTH = 6;
  private final int SCROLL_BAR_HEIGHT = 27;
  private final int SCROLL_AREA_HEIGHT = 230;
  private final int MAPPINGS_PER_PAGE = 9;


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
    field_238745_s_ = 162;
    field_238744_r_ = 107;
    mappings = new ArrayList<>();
  }

  private void onType(String p_214075_1_) {
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
      TextFieldWidget mapping = new TextFieldWidget(font, guiLeft + 5, guiTop + SCROLL_BAR_Y * font.FONT_HEIGHT * 3, 80, 12, new TranslationTextComponent("container.repair"));
      mapping.setCanLoseFocus(true);
      mapping.setTextColor(Color.func_240745_a_("#FFFFFF").func_240742_a_());
      mapping.setDisabledTextColour(-1);
      mapping.setEnableBackgroundDrawing(true);
      mapping.setMaxStringLength(40);
      mapping.setResponder(this::onType);
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
      setFocused(mapping);
      return true;
    } else if (getFocused() != null && getFocused() instanceof TextFieldWidget && ((TextFieldWidget) getFocused()).canWrite()) {
      return getFocused().keyPressed(keyCode, scanCode, modifiers);
    }
    return super.keyPressed(keyCode, scanCode, modifiers);
  }

  public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
    renderBackground(matrixStack);
    super.render(matrixStack, mouseX, mouseY, partialTicks);
    func_230459_a_(matrixStack, mouseX, mouseY);

    for (TextFieldWidget mapping : mappings)
      mapping.render(matrixStack, mouseX, mouseY + 100, partialTicks);
  }

  public void renderScrollbar(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
    if (canScroll(mappings.size()))
      blit(matrixStack, guiLeft + SCROLL_BAR_X, (int) (guiTop + SCROLL_BAR_Y + currentScroll * (SCROLL_AREA_HEIGHT - SCROLL_BAR_HEIGHT)), getBlitOffset(), 276.0F, 0.0F, SCROLL_BAR_WIDTH, SCROLL_BAR_HEIGHT, 256, 512);
    else
      blit(matrixStack, guiLeft + SCROLL_BAR_X, (int) (guiTop + SCROLL_BAR_Y + currentScroll * (SCROLL_AREA_HEIGHT - SCROLL_BAR_HEIGHT)), getBlitOffset(), 282.0F, 0.0F, SCROLL_BAR_WIDTH, SCROLL_BAR_HEIGHT, 256, 512);
  }

  @Override
  public void init() {
    super.init();
    mappings.clear();
    for (int i = 0; i < container.getControllerNodeTE().getMappings().size(); ++i) {
      TextFieldWidget mapping = new TextFieldWidget(font, guiLeft + 6, guiTop + SCROLL_BAR_Y + (i % 5) * font.FONT_HEIGHT * 3, 86, 12, new TranslationTextComponent("container.repair"));
      mapping.setCanLoseFocus(true);
      mapping.setTextColor(Color.func_240745_a_("#FFFFFF").func_240742_a_());
      mapping.setDisabledTextColour(-1);
      mapping.setEnableBackgroundDrawing(true);
      mapping.setResponder(this::onType);
      mapping.setMaxStringLength(40);
      mapping.setEnabled(true);
      mapping.setText(container.getControllerNodeTE().getMappings().get(i));
      if (i >= MAPPINGS_PER_PAGE)
        mapping.setVisible(false);
      children.add(mapping);
      mappings.add(mapping);
    }
    scrollTo(currentScroll);
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
        mapping.y = guiTop + SCROLL_BAR_Y + (mappingNum - discreteTop) * font.FONT_HEIGHT * 3;
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

  // RenderBackground
  @Override
  protected void func_230450_a_(MatrixStack matrixStack, float partialTicks, int x, int y) {
    RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
    minecraft.getTextureManager().bindTexture(BACKGROUND);
    blit(matrixStack, guiLeft, guiTop, getBlitOffset(), 0.0F, 0.0F, 276, 256, 256, 512);
    renderScrollbar(matrixStack, x, y, partialTicks);
  }

  // RenderForeground
  @Override
  protected void func_230451_b_(MatrixStack matrixStack, int x, int y) {
    font.func_238422_b_(matrixStack, playerInventory.getDisplayName(), (float) field_238744_r_, (float) field_238745_s_, 4210752);
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
