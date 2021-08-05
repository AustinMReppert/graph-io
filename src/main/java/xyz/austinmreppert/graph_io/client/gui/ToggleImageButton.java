package xyz.austinmreppert.graph_io.client.gui;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

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
  private final Component toolTip;
  private Component enabledToolTip;
  private final Screen screen;

  public ToggleImageButton(int x, int y, int width, int height, int xTexStart, int yTexStart, int yDiffText, ResourceLocation resourceLocation, int textureWidth, int textureHeight, OnPress onPress, Component toolTip, Component enabledToolTip, Screen screen) {
    this(x, y, width, height, xTexStart, yTexStart, yDiffText, resourceLocation, textureWidth, textureHeight, onPress, toolTip, screen);
    this.enabledToolTip = enabledToolTip;
  }

  public ToggleImageButton(int x, int y, int width, int height, int xTexStart, int yTexStart, int yDiffText, ResourceLocation resourceLocationIn, int textureWidth, int textureHeight, OnPress onPressIn, Component toolTip, Screen screen) {
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
  public void renderButton(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
    Minecraft minecraft = Minecraft.getInstance();
    RenderSystem.setShaderTexture(0, resourceLocation);
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
  public void renderToolTip(PoseStack matrixStack, int mouseX, int mouseY) {
    List<Component> list = Lists.newArrayList();
    if (enabled && enabledToolTip != null)
      list.add(enabledToolTip);
    else
      list.add(toolTip);
    Font font = Minecraft.getInstance().font;
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
