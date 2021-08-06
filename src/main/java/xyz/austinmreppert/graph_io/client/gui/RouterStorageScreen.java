package xyz.austinmreppert.graph_io.client.gui;

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
import xyz.austinmreppert.graph_io.GraphIO;
import xyz.austinmreppert.graph_io.container.RouterStorageContainer;

import javax.annotation.ParametersAreNonnullByDefault;

@OnlyIn(Dist.CLIENT)
public class RouterStorageScreen extends AbstractContainerScreen<RouterStorageContainer> implements MenuAccess<RouterStorageContainer> {

  private static final ResourceLocation CONTAINER_BACKGROUND = new ResourceLocation(GraphIO.MOD_ID, "textures/gui/container/router_storage.png");
  private final int containerRows;

  public RouterStorageScreen(AbstractContainerMenu container, Inventory inventory, Component title) {
    super((RouterStorageContainer) container, inventory, title);
    RouterStorageContainer routerStorageContainer = (RouterStorageContainer) container;

    this.passEvents = false;
    this.containerRows = routerStorageContainer.getRowCount();
    this.imageHeight = 114 + this.containerRows * RouterStorageContainer.SLOT_SIZE;
    this.inventoryLabelY = this.imageHeight - 94;
  }

  @ParametersAreNonnullByDefault
  public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
    this.renderBackground(poseStack);
    super.render(poseStack, mouseX, mouseY, partialTicks);
    this.renderTooltip(poseStack, mouseX, mouseY);
  }

  @ParametersAreNonnullByDefault
  protected void renderBg(PoseStack poseStack, float partialTicks, int mouseX, int mouseY) {
    RenderSystem.setShader(GameRenderer::getPositionTexShader);
    RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    RenderSystem.setShaderTexture(0, CONTAINER_BACKGROUND);
    int left = (this.width - this.imageWidth) / 2;
    int top = (this.height - this.imageHeight) / 2;
    this.blit(poseStack, left, top, 0, 0, this.imageWidth, this.containerRows * RouterStorageContainer.SLOT_SIZE + 17);
    this.blit(poseStack, left, top + this.containerRows * RouterStorageContainer.SLOT_SIZE + 17, 0, 126, this.imageWidth, 96);
  }
}
