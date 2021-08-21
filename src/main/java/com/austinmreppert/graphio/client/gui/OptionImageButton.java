package com.austinmreppert.graphio.client.gui;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fmlclient.gui.GuiUtils;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

/**
 * An image button that cycles through options.
 */
public class OptionImageButton extends ImageButton {

  private final ResourceLocation resourceLocation;
  private final int xTexStart;
  private final int yTexStart;
  private final int yDiffText;
  private final int textureWidth;
  private final int textureHeight;
  private final List<Component> options;
  private final Screen screen;
  private int selected;

  public OptionImageButton(final int x, final int y, final int width, final int height, final int xTexStart, final int yTexStart,
                           final int yDiffText, final ResourceLocation resourceLocation, final int textureWidth, final int textureHeight,
                           final OnPress onPress, final Component toolTip, final List<Component> options, final Screen screen) {
    this(x, y, width, height, xTexStart, yTexStart, yDiffText, resourceLocation, textureWidth, textureHeight, onPress, options, screen);
  }

  public OptionImageButton(final int x, final int y, final int width, final int height, final int xTexStart, final int yTexStart,
                           final int yDiffText, final ResourceLocation resourceLocationIn, final int textureWidth,
                           final int textureHeight, final OnPress onPressIn, final List<Component> options, final Screen screen) {
    super(x, y, width, height, xTexStart, yTexStart, yDiffText, resourceLocationIn, textureWidth, textureHeight, onPressIn, options.get(0));
    this.textureWidth = textureWidth;
    this.textureHeight = textureHeight;
    this.xTexStart = xTexStart;
    this.yTexStart = yTexStart;
    this.yDiffText = yDiffText;
    this.resourceLocation = resourceLocationIn;
    this.options = options;
    this.selected = 0;
    this.screen = screen;
  }

  /**
   * Renders the button.
   *
   * @param poseStack    The {@link PoseStack} used to render the button.
   * @param mouseX       The x position of the mouse.
   * @param mouseY       The y position of the mouse.
   * @param partialTicks Ticks since last frame.
   */
  @Override
  @ParametersAreNonnullByDefault
  public void renderButton(final PoseStack poseStack, final int mouseX, final int mouseY, final float partialTicks) {
    RenderSystem.setShaderTexture(0, resourceLocation);
    int i = yTexStart + selected*yDiffText;

    RenderSystem.enableDepthTest();
    blit(poseStack, x, y, (float) xTexStart, (float) i, width, height, textureWidth, textureHeight);
    if (isHovered)
      renderToolTip(poseStack, mouseX, mouseY);
  }

  @Override
  public void onClick(double p_93371_, double p_93372_) {
    selected = (selected + 1) % options.size();
    super.onClick(p_93371_, p_93372_);
  }

  /**
   * Renders the tooltip when hovering over the button.
   *
   * @param poseStack The {@link PoseStack} used while rendering the tooltip.
   * @param mouseX    The x position of the mouse.
   * @param mouseY    The y position of the mouse.
   */
  @Override
  @ParametersAreNonnullByDefault
  public void renderToolTip(final PoseStack poseStack, final int mouseX, final int mouseY) {
    final List<Component> list = Lists.newArrayList();
    list.add(options.get(selected));
    final Font font = Minecraft.getInstance().font;
    GuiUtils.drawHoveringText(poseStack, list, mouseX, mouseY, screen.width, screen.height, -1, font);
  }

  /**
   * Gets the index of the selected option.
   * @return The index of the selected option.
   */
  public int getSelected() {
    return selected;
  }

  /**
   * Sets the selected option.
   * @param selected The index of the option.
   */
  public void setSelected(int selected) {
    this.selected = selected;
  }
}
