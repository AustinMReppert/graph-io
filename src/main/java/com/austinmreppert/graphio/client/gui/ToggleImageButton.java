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

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Optional;

/**
 * A toggleable image button.
 */
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
  private boolean hidden;

  public ToggleImageButton(final int x, final int y, final int width, final int height, final int xTexStart, final int yTexStart,
                           final int yDiffText, final ResourceLocation resourceLocation, final int textureWidth, final int textureHeight,
                           final OnPress onPress, final Component toolTip, final Component enabledToolTip, final Screen screen) {
    this(x, y, width, height, xTexStart, yTexStart, yDiffText, resourceLocation, textureWidth, textureHeight, onPress, toolTip, screen);
    this.enabledToolTip = enabledToolTip;
  }

  public ToggleImageButton(final int x, final int y, final int width, final int height, final int xTexStart, final int yTexStart,
                           final int yDiffText, final ResourceLocation resourceLocationIn, final int textureWidth,
                           final int textureHeight, final OnPress onPressIn, final Component toolTip, final Screen screen) {
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
    hidden = false;
  }

  @Override
  public boolean mouseClicked(double p_93641_, double p_93642_, int p_93643_) {
    return super.mouseClicked(p_93641_, p_93642_, p_93643_);
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
    if(hidden)
      return;
    RenderSystem.setShaderTexture(0, resourceLocation);
    int i = yTexStart;
    if (!isHoveredOrFocused() && enabled)
      i += yDiffText * 2;
    else if (isHovered && !enabled)
      i += yDiffText;
    else if (isHoveredOrFocused() && enabled)
      i += yDiffText * 3;

    RenderSystem.enableDepthTest();
    blit(poseStack, x, y, (float) xTexStart, (float) i, width, height, textureWidth, textureHeight);
    if (isHovered)
      renderToolTip(poseStack, mouseX, mouseY);
  }

  /**
   * Renders the tooltip when hovering over the button.
   *
   * @param poseStack The {@link PoseStack} used while rendering the tooltip.
   * @param mouseX    The x position of the mouse.
   * @param mouseY    The y positoin of the mouse.
   */
  @Override
  @ParametersAreNonnullByDefault
  public void renderToolTip(final PoseStack poseStack, final int mouseX, final int mouseY) {
    if(hidden)
      return;
    final List<Component> list = Lists.newArrayList();
    if (enabled && enabledToolTip != null)
      list.add(enabledToolTip);
    else
      list.add(toolTip);
    final Font font = Minecraft.getInstance().font;
    screen.renderTooltip(poseStack, list, Optional.empty(), mouseX, mouseY, font);
  }

  /**
   * Gets whether the button is enabled.
   *
   * @return Whether the button enabled.
   */
  public boolean isEnabled() {
    return enabled;
  }

  /**
   * Sets the button to be enabled/disabled.
   *
   * @param enabled Whether the button is enabled.
   */
  public void setEnabled(final boolean enabled) {
    this.enabled = enabled;
  }

  /**
   * Switches the button on/off.
   */
  public void toggle() {
    enabled = !enabled;
  }

  public void setHidden(boolean hidden) {
    this.hidden = hidden;
  }
}
