package xyz.austinmreppert.graph_io.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix4f;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import xyz.austinmreppert.graph_io.GraphIO;

import java.util.ArrayList;

@Mod.EventBusSubscriber(modid = GraphIO.MOD_ID, value = Dist.CLIENT)
public class Highlighter {

  private static final ArrayList<HighlightedBlock> highlightedBlocks = new ArrayList<>();

  @SubscribeEvent
  public static void onRenderWorld(RenderWorldLastEvent e) {
    for (HighlightedBlock highlightedBlock : highlightedBlocks)
      highlightBlock(e.getMatrixStack(), highlightedBlock.blockPos, highlightedBlock.level, highlightedBlock.padding, highlightedBlock.r, highlightedBlock.g, highlightedBlock.b, highlightedBlock.a);
  }

  public static void highlightBlock(BlockPos blockPos, ResourceKey<Level> level, float padding, int r, int g, int b, int a) {
    HighlightedBlock highlightedBlock = new HighlightedBlock(blockPos, level, padding, r, g, b, a);
    if (!highlightedBlocks.contains(highlightedBlock))
      highlightedBlocks.add(highlightedBlock);
  }

  public static void toggleHighlightBlock(BlockPos blockPos, ResourceKey<Level> level, float padding, int r, int g, int b, int a) {
    HighlightedBlock highlightedBlock = new HighlightedBlock(blockPos, level, padding, r, g, b, a);
    if (!highlightedBlocks.contains(highlightedBlock))
      highlightedBlocks.add(highlightedBlock);
    else
      unhighlightBlock(blockPos, level);
  }

  public static void unhighlightBlock(BlockPos blockPos, ResourceKey<Level> level) {
    highlightedBlocks.removeIf(highlightedBlock-> highlightedBlock.level.equals(level) && blockPos.equals(highlightedBlock.blockPos));
  }

  private static void highlightBlock(PoseStack matrixStack, BlockPos blockPos, ResourceKey<Level> level, float padding, int r, int g, int b, int a) {
    if (!Minecraft.getInstance().level.dimension().equals(level) || !blockPos.closerThan(Minecraft.getInstance().player.position(), Minecraft.getInstance().options.renderDistance * 16))
      return;
    int x = blockPos.getX();
    int y = blockPos.getY();
    int z = blockPos.getZ();

    Tesselator tes = Tesselator.getInstance();
    BufferBuilder bb = tes.getBuilder();

    matrixStack.pushPose();

    RenderSystem.disableDepthTest();
    RenderSystem.enableCull();
    RenderSystem.disableTexture();
    RenderSystem.enableBlend();
    RenderSystem.defaultBlendFunc();

    Vec3 projectedView = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
    matrixStack.translate(-projectedView.x, -projectedView.y, -projectedView.z);

    Matrix4f mat = matrixStack.last().pose();

    bb.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

    float x1 = x - padding;
    float x2 = x + padding + 1.0F;
    float y1 = y - padding;
    float y2 = y + padding + 1.0F;
    float z1 = z - padding;
    float z2 = z + padding + 1.0F;

    for (float v : new float[]{y2, y1}) {
      bb.vertex(mat, x2, v, z1).color(r, g, b, a).endVertex();
    }
    bb.vertex(mat, x1, y1, z1).color(r, g, b, a).endVertex();
    bb.vertex(mat, x1, y2, z1).color(r, g, b, a).endVertex();

    bb.vertex(mat, x1, y2, z2).color(r, g, b, a).endVertex();
    bb.vertex(mat, x1, y1, z2).color(r, g, b, a).endVertex();
    bb.vertex(mat, x2, y1, z2).color(r, g, b, a).endVertex();
    bb.vertex(mat, x2, y2, z2).color(r, g, b, a).endVertex();

    bb.vertex(mat, x1, y2, z1).color(r, g, b, a).endVertex();
    bb.vertex(mat, x1, y1, z1).color(r, g, b, a).endVertex();
    bb.vertex(mat, x1, y1, z2).color(r, g, b, a).endVertex();
    bb.vertex(mat, x1, y2, z2).color(r, g, b, a).endVertex();

    bb.vertex(mat, x2, y2, z2).color(r, g, b, a).endVertex();
    bb.vertex(mat, x2, y1, z2).color(r, g, b, a).endVertex();
    bb.vertex(mat, x2, y1, z1).color(r, g, b, a).endVertex();
    bb.vertex(mat, x2, y2, z1).color(r, g, b, a).endVertex();

    bb.vertex(mat, x1, y1, z2).color(r, g, b, a).endVertex();
    bb.vertex(mat, x1, y1, z1).color(r, g, b, a).endVertex();
    bb.vertex(mat, x2, y1, z1).color(r, g, b, a).endVertex();
    bb.vertex(mat, x2, y1, z2).color(r, g, b, a).endVertex();

    bb.vertex(mat, x2, y2, z2).color(r, g, b, a).endVertex();
    bb.vertex(mat, x2, y2, z1).color(r, g, b, a).endVertex();
    bb.vertex(mat, x1, y2, z1).color(r, g, b, a).endVertex();
    bb.vertex(mat, x1, y2, z2).color(r, g, b, a).endVertex();

    tes.end();
    matrixStack.popPose();
  }

  private static class HighlightedBlock {

    public BlockPos blockPos;
    public ResourceKey<Level> level;
    public float padding;
    public int r;
    public int g;
    public int b;
    public int a;

    public HighlightedBlock(BlockPos blockPos, ResourceKey<Level> level, float padding, int r, int g, int b, int a) {
      this.blockPos = blockPos;
      this.level = level;
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
      ResourceKey<Level> level = ((HighlightedBlock) o).level;
      return this.blockPos.getX() == blockPos.getX() && this.blockPos.getY() == blockPos.getY() && this.blockPos.getZ() == blockPos.getZ() && this.level.equals(level);
    }

  }

}

