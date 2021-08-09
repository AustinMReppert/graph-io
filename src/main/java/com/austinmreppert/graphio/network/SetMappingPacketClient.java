package com.austinmreppert.graphio.network;

import com.austinmreppert.graphio.blockentity.RouterBlockEntity;
import com.austinmreppert.graphio.container.RouterContainer;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntity;

public class SetMappingPacketClient {

  static void handle(SetMappingsPacket packet) {
    Player playerEntity = Minecraft.getInstance().player;
    AbstractContainerMenu openContainer = playerEntity.containerMenu;
    BlockEntity be = playerEntity.level.getBlockEntity(packet.getBlockPos());
    if (openContainer instanceof RouterContainer router && be instanceof RouterBlockEntity routerBlockEntity && packet.getBlockPos().equals(routerBlockEntity.getBlockPos())) {
      router.getTrackedMappingsReference().set.accept(packet.getRouterTENBT());
    }
  }

}
