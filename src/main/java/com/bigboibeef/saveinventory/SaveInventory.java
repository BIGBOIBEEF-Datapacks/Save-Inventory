package com.bigboibeef.saveinventory;

import com.bigboibeef.saveinventory.commands.SaveInventoryCommand;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.screen.slot.SlotActionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;

public class SaveInventory implements ClientModInitializer {

	public static final Logger LOGGER = LoggerFactory.getLogger("saveinventory");

	@Override
	public void onInitializeClient() {
		System.out.println("SaveInventory mod loaded!");
		SaveInventoryCommand.register();
	}

	// Always fetch the latest client instance
	public static MinecraftClient getClient() {
		return MinecraftClient.getInstance();
	}

	public static ClientPlayerEntity getPlayer() {
		return getClient().player;
	}

	public static PlayerInventory getInventory() {
		ClientPlayerEntity player = getPlayer();
		return player != null ? player.getInventory() : null;
	}
}