package com.austinmreppert.graphio.client.gui;

import com.austinmreppert.graphio.GraphIO;
import com.austinmreppert.graphio.container.RouterStorageContainer;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * The screen used to display a router's storage.
 */
@OnlyIn(Dist.CLIENT)
public class RouterStorageScreen extends AbstractContainerScreen<RouterStorageContainer> implements MenuAccess<RouterStorageContainer> {

  private static final ResourceLocation CONTAINER_BACKGROUND = new ResourceLocation(GraphIO.MOD_ID, "textures/gui/container/router_storage.png");
  private final int containerRows;

  public RouterStorageScreen(final AbstractContainerMenu container, final Inventory inventory, final Component title) {
    super((RouterStorageContainer) container, inventory, title);
    final RouterStorageContainer routerStorageContainer = (RouterStorageContainer) container;

    this.passEvents = false;
    this.containerRows = routerStorageContainer.getRowCount();
    this.imageHeight = 114 + this.containerRows * RouterStorageContainer.SLOT_SIZE;
    this.inventoryLabelY = this.imageHeight - 94;
  }

  /**
   * Renders the black background and tooltips.
   *
   * @param poseStack    The {@link PoseStack} used while rendering.
   * @param mouseX       The x position of the mouse.
   * @param mouseY       The y position of the mouse.
   * @param partialTicks Ticks since last frame.
   */
  @ParametersAreNonnullByDefault
  public void render(final PoseStack poseStack, final int mouseX, final int mouseY, final float partialTicks) {
    this.renderBackground(poseStack);
    super.render(poseStack, mouseX, mouseY, partialTicks);
    this.renderTooltip(poseStack, mouseX, mouseY);
  }

  /**
   * Renders the background.
   *
   * @param poseStack    The {@link PoseStack} used when rendering the background.
   * @param partialTicks Ticks since last frame.
   * @param mouseX       The x position of the mouse.
   * @param mouseY       The y position of the mouse.
   */
  @Override
  @ParametersAreNonnullByDefault
  protected void renderBg(final PoseStack poseStack, final float partialTicks, final int mouseX, final int mouseY) {
    RenderSystem.setShader(GameRenderer::getPositionTexShader);
    RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    RenderSystem.setShaderTexture(0, CONTAINER_BACKGROUND);
    int left = (this.width - this.imageWidth) / 2;
    int top = (this.height - this.imageHeight) / 2;
    this.blit(poseStack, left, top, 0, 0, this.imageWidth, this.containerRows * RouterStorageContainer.SLOT_SIZE + 17);
    this.blit(poseStack, left, top + this.containerRows * RouterStorageContainer.SLOT_SIZE + 17, 0, 126, this.imageWidth, 96);
  }
}
