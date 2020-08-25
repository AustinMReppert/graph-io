package xyz.austinmreppert.graph_io.client.gui;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.ImageButton;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.*;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

public class ToggleImageButton extends ImageButton {

  private final ResourceLocation resourceLocation;
  private final int xTexStart;
  private final int yTexStart;
  private final int yDiffText;
  private final int textureWidth;
  private final int textureHeight;
  private boolean enabled;
  private final ITextComponent toolTip;
  private ITextComponent enabledToolTip;
  private final Screen screen;

  public ToggleImageButton(int x, int y, int width, int height, int xTexStart, int yTexStart, int yDiffText, ResourceLocation resourceLocation, int textureWidth, int textureHeight, IPressable onPress, ITextComponent toolTip, ITextComponent enabledToolTip, Screen screen) {
    this(x, y, width, height, xTexStart, yTexStart, yDiffText, resourceLocation, textureWidth, textureHeight, onPress, toolTip, screen);
    this.enabledToolTip = enabledToolTip;
  }

  public ToggleImageButton(int x, int y, int width, int height, int xTexStart, int yTexStart, int yDiffText, ResourceLocation resourceLocationIn, int textureWidth, int textureHeight, IPressable onPressIn, ITextComponent toolTip, Screen screen) {
    super(x, y, width, height, xTexStart, yTexStart, yDiffText, resourceLocationIn, textureWidth, textureHeight, onPressIn, toolTip);
    this.textureWidth = textureWidth;
    this.textureHeight = textureHeight;
    this.xTexStart = xTexStart;
    this.yTexStart = yTexStart;
    this.yDiffText = yDiffText;
    this.resourceLocation = resourceLocationIn;
    this.toolTip = toolTip;
    this.screen = screen;
    enabled = false;
  }

  @Override
  @ParametersAreNonnullByDefault
  public void renderButton(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
    Minecraft minecraft = Minecraft.getInstance();
    minecraft.getTextureManager().bindTexture(resourceLocation);
    int i = yTexStart;
    if (!isHovered() && enabled)
      i += yDiffText * 2;
    else if (isHovered && !enabled)
      i += yDiffText;
    else if (isHovered() && enabled)
      i += yDiffText * 3;

    RenderSystem.enableDepthTest();
    blit(matrixStack, x, y, (float) xTexStart, (float) i, width, height, textureWidth, textureHeight);
    if (isHovered)
      renderToolTip(matrixStack, mouseX, mouseY);
  }

  @Override
  @ParametersAreNonnullByDefault
  public void renderToolTip(MatrixStack matrixStack, int mouseX, int mouseY) {
    List<ITextComponent> list = Lists.newArrayList();
    if (enabled && enabledToolTip != null)
      list.add(enabledToolTip);
    else
      list.add(toolTip);
    FontRenderer font = Minecraft.getInstance().fontRenderer;
    // TODO: Uncomment once forge adds this
     // net.minecraftforge.fml.client.gui.GuiUtils.drawHoveringText(matrixStack, list, mouseX, mouseY, screen.width, screen.height, -1, font);
  }

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public void toggle() {
    enabled = !enabled;
  }

}
