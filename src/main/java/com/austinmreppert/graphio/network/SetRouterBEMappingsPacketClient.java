package com.austinmreppert.graphio.network;

import com.austinmreppert.graphio.blockentity.RouterBlockEntity;
import com.austinmreppert.graphio.container.RouterContainer;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Handles the client side only portion of {@link SetRouterBEMappingsPacket}.
 */
public class SetRouterBEMappingsPacketClient {

  /**
   * Handles a {@link SetRouterBEMappingsPacket} for the client.
   *
   * @param packet The packet to handle.
   */
  static void handle(final SetRouterBEMappingsPacket packet) {
    final Player playerEntity = Minecraft.getInstance().player;
    final AbstractContainerMenu openContainer = playerEntity.containerMenu;
    final BlockEntity be = playerEntity.level.getBlockEntity(packet.blockPos());
    if (openContainer instanceof RouterContainer router && be instanceof RouterBlockEntity routerBlockEntity && packet.blockPos().equals(routerBlockEntity.getBlockPos())) {
      router.getTrackedMappingsReference().set.accept(packet.routerTENBT());
    }
  }

}
