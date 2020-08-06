package xyz.austinmreppert.graph_io.client.gui;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.ImageButton;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldVertexBufferUploader;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.text.*;

import java.util.List;

public class ToggleImageButton extends ImageButton {

  private final ResourceLocation resourceLocation;
  private final int xTexStart;
  private final int yTexStart;
  private final int yDiffText;
  private final int textureWidth;
  private final int textureHeight;
  private boolean enabled;
  private ITextComponent toolTip;
  private ITextComponent enabledToolTip;
  private Screen screen;

  public ToggleImageButton(int x, int y, int width, int height, int xTexStart, int yTexStart, int yDiffText, ResourceLocation resourceLocation, int textureWidth, int textureHeight, IPressable onPress, ITextComponent toolTip, ITextComponent enabledToolTip, Screen screen) {
    this(x, y, width, height, xTexStart, yTexStart, yDiffText, resourceLocation, textureWidth, textureHeight, onPress, toolTip, screen);
    this.enabledToolTip = enabledToolTip;
  }

  public ToggleImageButton(int xIn, int yIn, int widthIn, int heightIn, int xTexStart, int yTexStart, int yDiffText, ResourceLocation resourceLocationIn, int textureWidth, int textureHeight, IPressable onPressIn, ITextComponent toolTip, Screen screen) {
    super(xIn, yIn, widthIn, heightIn, xTexStart, yTexStart, yDiffText, resourceLocationIn, textureWidth, textureHeight, onPressIn, toolTip);
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
  public void renderButton(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
    Minecraft minecraft = Minecraft.getInstance();
    minecraft.getTextureManager().bindTexture(resourceLocation);
    int i = yTexStart;
    if (!isHovered() && enabled)
      i += yDiffText;
    else if (isHovered && !enabled)
      i += yDiffText * 2;
    else if (isHovered() && enabled)
      i += yDiffText * 3;

    RenderSystem.enableDepthTest();
    blit(matrixStack, x, y, (float) xTexStart, (float) i, width, height, textureWidth, textureHeight);
    if (isHovered) {
      renderToolTip(matrixStack, mouseX, mouseY);
    }
  }

  @Override
  public void renderToolTip(MatrixStack matrixStack, int mouseX, int mouseY) {
    List<ITextComponent> list = Lists.newArrayList();
    if (enabled && enabledToolTip != null)
      list.add(enabledToolTip);
    else
      list.add(toolTip);

    FontRenderer font = Minecraft.getInstance().fontRenderer;
    net.minecraftforge.fml.client.gui.GuiUtils.drawHoveringText(matrixStack, list, mouseX, mouseY, screen.width, screen.height, -1, font);
    if (false && !list.isEmpty()) {
      int i = 0;

      for (ITextProperties itextproperties : list) {
        int j = font.func_238414_a_(itextproperties);
        if (j > i) {
          i = j;
        }
      }

      int i2 = mouseX + 12;
      int j2 = mouseY - 12;
      int k = 8;
      if (list.size() > 1) {
        k += 2 + (list.size() - 1) * 10;
      }

      if (i2 + i > screen.width) {
        i2 -= 28 + i;
      }

      if (j2 + k + 6 > screen.height) {
        j2 = screen.height - k - 6;
      }

      matrixStack.push();
      int l = -267386864;
      int i1 = 1347420415;
      int j1 = 1344798847;
      int k1 = 400;
      Tessellator tessellator = Tessellator.getInstance();
      BufferBuilder bufferbuilder = tessellator.getBuffer();
      bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);
      Matrix4f matrix4f = matrixStack.getLast().getMatrix();
      fillGradient(matrix4f, bufferbuilder, i2 - 3, j2 - 4, i2 + i + 3, j2 - 3, 400, -267386864, -267386864);
      fillGradient(matrix4f, bufferbuilder, i2 - 3, j2 + k + 3, i2 + i + 3, j2 + k + 4, 400, -267386864, -267386864);
      fillGradient(matrix4f, bufferbuilder, i2 - 3, j2 - 3, i2 + i + 3, j2 + k + 3, 400, -267386864, -267386864);
      fillGradient(matrix4f, bufferbuilder, i2 - 4, j2 - 3, i2 - 3, j2 + k + 3, 400, -267386864, -267386864);
      fillGradient(matrix4f, bufferbuilder, i2 + i + 3, j2 - 3, i2 + i + 4, j2 + k + 3, 400, -267386864, -267386864);
      fillGradient(matrix4f, bufferbuilder, i2 - 3, j2 - 3 + 1, i2 - 3 + 1, j2 + k + 3 - 1, 400, 1347420415, 1344798847);
      fillGradient(matrix4f, bufferbuilder, i2 + i + 2, j2 - 3 + 1, i2 + i + 3, j2 + k + 3 - 1, 400, 1347420415, 1344798847);
      fillGradient(matrix4f, bufferbuilder, i2 - 3, j2 - 3, i2 + i + 3, j2 - 3 + 1, 400, 1347420415, 1347420415);
      fillGradient(matrix4f, bufferbuilder, i2 - 3, j2 + k + 2, i2 + i + 3, j2 + k + 3, 400, 1344798847, 1344798847);
      RenderSystem.enableDepthTest();
      RenderSystem.disableTexture();
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      RenderSystem.shadeModel(7425);
      bufferbuilder.finishDrawing();
      WorldVertexBufferUploader.draw(bufferbuilder);
      RenderSystem.shadeModel(7424);
      RenderSystem.disableBlend();
      RenderSystem.enableTexture();
      IRenderTypeBuffer.Impl irendertypebuffer$impl = IRenderTypeBuffer.getImpl(Tessellator.getInstance().getBuffer());
      matrixStack.translate(0.0D, 0.0D, 400.0D);

      for (int l1 = 0; l1 < list.size(); ++l1) {
        ITextProperties itextproperties1 = list.get(l1);
        if (itextproperties1 != null) {
          font.func_238416_a_(itextproperties1, (float) i2, (float) j2, -1, true, matrix4f, irendertypebuffer$impl, false, 0, 15728880);
        }

        if (l1 == 0) {
          j2 += 2;
        }

        j2 += 10;
      }

      irendertypebuffer$impl.finish();
      matrixStack.pop();
    }
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
