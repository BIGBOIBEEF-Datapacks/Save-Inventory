package com.bigboibeef.saveinventory;

import com.bigboibeef.saveinventory.commands.SaveInventoryCommand;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
