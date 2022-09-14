package com.austinmreppert.graphio.client.render;

import com.austinmreppert.graphio.GraphIO;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix4f;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;

/**
 * Highlights blocks.
 */
@Mod.EventBusSubscriber(modid = GraphIO.MOD_ID, value = Dist.CLIENT)
public class Highlighter {

  private static final ArrayList<HighlightedBlock> highlightedBlocks = new ArrayList<>();

  /**
   * Called at various stages of level rendering. Highlights all the blocks in {@code highlightedBlocks} within render distance.
   *
   * @param e The {@link RenderLevelStageEvent}.
   */
  @SubscribeEvent
  public static void drawLast(final RenderLevelStageEvent e) {
    if(e.getStage() == RenderLevelStageEvent.Stage.AFTER_TRIPWIRE_BLOCKS) {
      for (final var highlightedBlock : highlightedBlocks) {
        highlightBlock(e.getPoseStack(), e.getCamera(), highlightedBlock.blockPos, highlightedBlock.level, highlightedBlock.padding, highlightedBlock.r, highlightedBlock.g, highlightedBlock.b, highlightedBlock.a);
      }
    }

  }

  /**
   * Adds a block to the list of blocks to be highlighted.
   *
   * @param blockPos The position of the block.
   * @param level    The dimension the block is in.
   * @param padding  The amount of space between the highlight and the block.
   * @param r        Red.
   * @param g        Green.
   * @param b        Blue.
   * @param a        Alpha.
   */
  public static void highlightBlock(final BlockPos blockPos, final ResourceKey<Level> level, final double padding,
                                    final int r, final int g, final int b, final int a) {
    final var highlightedBlock = new HighlightedBlock(blockPos, level, padding, r, g, b, a);
    if (!highlightedBlocks.contains(highlightedBlock))
      highlightedBlocks.add(highlightedBlock);
  }

  /**
   * Adds or removes a block to be highlighted.
   *
   * @param blockPos The position of the block.
   * @param level    The level of the block.
   * @param padding  The amount of space between the highlight and the block.
   * @param r        Red.
   * @param g        Green.
   * @param b        Blue.
   * @param a        Alpha.
   */
  public static void toggleHighlightBlock(final BlockPos blockPos, final ResourceKey<Level> level, final double padding,
                                          final int r, final int g, final int b, final int a) {
    final var highlightedBlock = new HighlightedBlock(blockPos, level, padding, r, g, b, a);
    if (!highlightedBlocks.contains(highlightedBlock))
      highlightedBlocks.add(highlightedBlock);
    else
      unhighlightBlock(blockPos, level);
  }

  /**
   * Unhighlights a block.
   *
   * @param blockPos The position of the block.
   * @param level    The level of the block.
   */
  public static void unhighlightBlock(final BlockPos blockPos, final ResourceKey<Level> level) {
    highlightedBlocks.removeIf(highlightedBlock -> highlightedBlock.level.equals(level) && blockPos.equals(highlightedBlock.blockPos));
  }

  /**
   * Highlights a block.
   *
   * @param poseStack The {@link PoseStack} to use.
   * @param blockPos  The position of the block.
   * @param level     The level of the block.
   * @param padding   The amount of space between the highlight and the block.
   * @param r         Red.
   * @param g         Green.
   * @param b         Blue.
   * @param a         Alpha.
   */
  private static void highlightBlock(final PoseStack poseStack, Camera camera, final BlockPos blockPos, final ResourceKey<Level> level,
                                      final double padding, final int r, final int g, final int b, final int a) {
    if (!Minecraft.getInstance().level.dimension().equals(level) || !blockPos.closerThan(Minecraft.getInstance().player.getOnPos(), Minecraft.getInstance().options.getEffectiveRenderDistance() * 16))
      return;
    final int x = blockPos.getX();
    final int y = blockPos.getY();
    final int z = blockPos.getZ();

    final MultiBufferSource.BufferSource buffer = Minecraft.getInstance().renderBuffers().bufferSource();
    final var bb = buffer.getBuffer(GraphIORenderTypes.HIGHLIGHTER);

    poseStack.pushPose();

    RenderSystem.disableDepthTest();
    RenderSystem.enableCull();
    RenderSystem.disableTexture();
    RenderSystem.enableBlend();
    RenderSystem.defaultBlendFunc();

    final Vec3 projectedView = camera.getPosition();
    poseStack.translate(-projectedView.x, -projectedView.y, -projectedView.z);

    final Matrix4f mat = poseStack.last().pose();

    final float x1 = (float) ((double)(x) - padding);
    final float x2 = (float) ((double)x + padding + 1.0D);
    final float y1 = (float) ((double)y - padding);
    final float y2 = (float) ((double)y + padding + 1.0D);
    final float z1 = (float) ((double)z - padding);
    final float z2 = (float) ((double)z + padding + 1.0D);

    bb.vertex(mat, x2, y2, z1).color(r, g, b, a).endVertex();
    bb.vertex(mat, x2, y1, z1).color(r, g, b, a).endVertex();
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

    poseStack.popPose();
    buffer.endBatch(GraphIORenderTypes.HIGHLIGHTER);
  }

  private record HighlightedBlock(BlockPos blockPos, ResourceKey<Level> level, double padding, int r, int g, int b,
                                  int a) {

    /**
     * Gets whether two highlighted blocks are the same.
     *
     * @param o The other {@link HighlightedBlock}.
     * @return Whether two highlighted blocks are the same. True if they have the same block pos and level, false otherwise.
     */
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

