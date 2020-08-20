package xyz.austinmreppert.graph_io.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.opengl.GL11;
import xyz.austinmreppert.graph_io.GraphIO;

import java.util.ArrayList;

@Mod.EventBusSubscriber(modid = GraphIO.MOD_ID, value = Dist.CLIENT)
public class Highlighter {

  private static final ArrayList<HighlightedBlock> highlightedBlocks = new ArrayList<>();

  @SubscribeEvent
  public static void onRenderWorld(RenderWorldLastEvent e) {
    for (HighlightedBlock highlightedBlock : highlightedBlocks)
      highlightBlock(e.getMatrixStack(), highlightedBlock.blockPos, highlightedBlock.padding, highlightedBlock.r, highlightedBlock.g, highlightedBlock.b, highlightedBlock.a);
  }

  public static void highlightBlock(BlockPos blockPos, float padding, int r, int g, int b, int a) {
    HighlightedBlock highlightedBlock = new HighlightedBlock(blockPos, padding, r, g, b, a);
    if (!highlightedBlocks.contains(highlightedBlock))
      highlightedBlocks.add(highlightedBlock);
  }

  public static void toggleHighlightBlock(BlockPos blockPos, float padding, int r, int g, int b, int a) {
    HighlightedBlock highlightedBlock = new HighlightedBlock(blockPos, padding, r, g, b, a);
    if (!highlightedBlocks.contains(highlightedBlock))
      highlightedBlocks.add(highlightedBlock);
    else
      unhighlightBlock(blockPos);
  }

  public static void unhighlightBlock(BlockPos blockPos) {
    highlightedBlocks.removeIf(highlightedBlock -> blockPos.equals(highlightedBlock.blockPos));
  }

  private static void highlightBlock(MatrixStack matrixStack, BlockPos blockPos, float padding, int r, int g, int b, int a) {
    if (!blockPos.withinDistance(Minecraft.getInstance().player.getPositionVec(), Minecraft.getInstance().gameSettings.renderDistanceChunks * 16))
      return;
    int x = blockPos.getX();
    int y = blockPos.getY();
    int z = blockPos.getZ();

    Tessellator tes = Tessellator.getInstance();
    BufferBuilder bb = tes.getBuffer();

    matrixStack.push();

    RenderSystem.disableDepthTest();
    RenderSystem.enableCull();
    RenderSystem.disableTexture();
    RenderSystem.enableBlend();
    RenderSystem.defaultBlendFunc();

    Vector3d projectedView = Minecraft.getInstance().gameRenderer.getActiveRenderInfo().getProjectedView();
    matrixStack.translate(-projectedView.x, -projectedView.y, -projectedView.z);

    Matrix4f mat = matrixStack.getLast().getMatrix();

    bb.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);

    float x1 = x - padding;
    float x2 = x + padding + 1.0F;
    float y1 = y - padding;
    float y2 = y + padding + 1.0F;
    float z1 = z - padding;
    float z2 = z + padding + 1.0F;

    bb.pos(mat, x2, y2, z1).color(r, g, b, a).endVertex();
    bb.pos(mat, x2, y1, z1).color(r, g, b, a).endVertex();
    bb.pos(mat, x1, y1, z1).color(r, g, b, a).endVertex();
    bb.pos(mat, x1, y2, z1).color(r, g, b, a).endVertex();

    bb.pos(mat, x1, y2, z2).color(r, g, b, a).endVertex();
    bb.pos(mat, x1, y1, z2).color(r, g, b, a).endVertex();
    bb.pos(mat, x2, y1, z2).color(r, g, b, a).endVertex();
    bb.pos(mat, x2, y2, z2).color(r, g, b, a).endVertex();

    bb.pos(mat, x1, y2, z1).color(r, g, b, a).endVertex();
    bb.pos(mat, x1, y1, z1).color(r, g, b, a).endVertex();
    bb.pos(mat, x1, y1, z2).color(r, g, b, a).endVertex();
    bb.pos(mat, x1, y2, z2).color(r, g, b, a).endVertex();

    bb.pos(mat, x2, y2, z2).color(r, g, b, a).endVertex();
    bb.pos(mat, x2, y1, z2).color(r, g, b, a).endVertex();
    bb.pos(mat, x2, y1, z1).color(r, g, b, a).endVertex();
    bb.pos(mat, x2, y2, z1).color(r, g, b, a).endVertex();

    bb.pos(mat, x1, y1, z2).color(r, g, b, a).endVertex();
    bb.pos(mat, x1, y1, z1).color(r, g, b, a).endVertex();
    bb.pos(mat, x2, y1, z1).color(r, g, b, a).endVertex();
    bb.pos(mat, x2, y1, z2).color(r, g, b, a).endVertex();

    bb.pos(mat, x2, y2, z2).color(r, g, b, a).endVertex();
    bb.pos(mat, x2, y2, z1).color(r, g, b, a).endVertex();
    bb.pos(mat, x1, y2, z1).color(r, g, b, a).endVertex();
    bb.pos(mat, x1, y2, z2).color(r, g, b, a).endVertex();

    tes.draw();
    matrixStack.pop();
  }

  private static class HighlightedBlock {

    public BlockPos blockPos;
    public float padding;
    public int r;
    public int g;
    public int b;
    public int a;

    public HighlightedBlock(BlockPos blockPos, float padding, int r, int g, int b, int a) {
      this.blockPos = blockPos;
      this.padding = padding;
      this.r = r;
      this.g = g;
      this.b = b;
      this.a = a;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof HighlightedBlock)) return false;
      BlockPos blockPos = ((HighlightedBlock) o).blockPos;
      return this.blockPos.getX() == blockPos.getX() && this.blockPos.getY() == blockPos.getY() && this.blockPos.getZ() == blockPos.getZ();
    }

  }

}
