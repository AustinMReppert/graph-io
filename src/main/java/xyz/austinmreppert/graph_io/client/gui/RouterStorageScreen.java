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

@OnlyIn(Dist.CLIENT)
public class RouterStorageScreen extends AbstractContainerScreen<RouterStorageContainer> implements MenuAccess<RouterStorageContainer> {
  private static final ResourceLocation CONTAINER_BACKGROUND = new ResourceLocation(GraphIO.MOD_ID, "textures/gui/container/router_storage.png");
  private final int containerRows;
  private RouterStorageContainer routerStorageContainer;

  public RouterStorageScreen(AbstractContainerMenu container, Inventory inventory, Component title) {
    super((RouterStorageContainer) container, inventory, title);
    routerStorageContainer = (RouterStorageContainer) container;

    this.passEvents = false;
    this.containerRows = routerStorageContainer.getRowCount();
    this.imageHeight = 114 + this.containerRows * 18;
    this.inventoryLabelY = this.imageHeight - 94;
  }

  public void render(PoseStack p_98418_, int p_98419_, int p_98420_, float p_98421_) {
    this.renderBackground(p_98418_);
    super.render(p_98418_, p_98419_, p_98420_, p_98421_);
    this.renderTooltip(p_98418_, p_98419_, p_98420_);
  }

  protected void renderBg(PoseStack p_98413_, float p_98414_, int p_98415_, int p_98416_) {
    RenderSystem.setShader(GameRenderer::getPositionTexShader);
    RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    RenderSystem.setShaderTexture(0, CONTAINER_BACKGROUND);
    int var5 = (this.width - this.imageWidth) / 2;
    int var6 = (this.height - this.imageHeight) / 2;
    this.blit(p_98413_, var5, var6, 0, 0, this.imageWidth, this.containerRows * 18 + 17);
    this.blit(p_98413_, var5, var6 + this.containerRows * 18 + 17, 0, 126, this.imageWidth, 96);
  }
}
