package com.austinmreppert.graphio.client.render;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;

public class GraphIORenderTypes extends RenderType {

  public GraphIORenderTypes(String pName, VertexFormat pFormat, VertexFormat.Mode pMode, int pBufferSize, boolean pAffectsCrumbling, boolean pSortOnUpload, Runnable pSetupState, Runnable pClearState) {
    super(pName, pFormat, pMode, pBufferSize, pAffectsCrumbling, pSortOnUpload, pSetupState, pClearState);
  }

  public static final RenderType HIGHLIGHTER = RenderType.create("highlighter",
      DefaultVertexFormat.POSITION_COLOR,
      VertexFormat.Mode.QUADS, 256,
      false,
      true,
      RenderType.CompositeState.builder()
          .setTextureState(NO_TEXTURE)
          .setShaderState(RenderStateShard.POSITION_COLOR_SHADER)
          .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
          .setDepthTestState(NO_DEPTH_TEST)
          .setLightmapState(NO_LIGHTMAP)
          .setCullState(CULL)
          .setWriteMaskState(COLOR_WRITE)
          .createCompositeState(false));


}
