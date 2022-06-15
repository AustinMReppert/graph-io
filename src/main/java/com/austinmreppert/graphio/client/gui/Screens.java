package com.austinmreppert.graphio.client.gui;

import com.austinmreppert.graphio.GraphIO;
import com.austinmreppert.graphio.container.ContainerTypes;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber(modid = GraphIO.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class Screens {

  @SubscribeEvent
  public static void onFMLClientSetupEvent(final FMLClientSetupEvent event) {
    event.enqueueWork(Screens::register);
  }

  private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, GraphIO.MOD_ID);

  public static void register() {
    MenuScreens.register(ContainerTypes.ROUTER_CONTAINER.get(), RouterScreen::new);
    MenuScreens.register(ContainerTypes.ROUTER_STORAGE_CONTAINER.get(), RouterStorageScreen::new);
  }

}
