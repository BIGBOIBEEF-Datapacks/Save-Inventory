package com.bigboibeef.treechopper;

import com.bigboibeef.treechopper.commands.SaveInventory;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TreeChopper implements ModInitializer {
	public static final String MOD_ID = "treechopper";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LogBreakEvent.register();
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> SaveInventory	.register(dispatcher));
	}
}