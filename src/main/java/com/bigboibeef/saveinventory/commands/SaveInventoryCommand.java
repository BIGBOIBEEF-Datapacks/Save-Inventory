package com.bigboibeef.saveinventory.commands;

import com.bigboibeef.saveinventory.PlayerData.SavedInventories;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.network.ClientPlayerEntity;

import static com.bigboibeef.saveinventory.SaveInventory.*;

public class SaveInventoryCommand {
    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommandManager.literal("si")
                    .then(ClientCommandManager.literal("load")
                            .then(ClientCommandManager.argument("name", StringArgumentType.string())
                                    .suggests((context, builder) -> {
                                        ClientPlayerEntity player = getPlayer();
                                        if (player != null) {
                                            String typed = context.getInput().substring(context.getInput().lastIndexOf(' ') + 1).toLowerCase();
                                            for (String name : SavedInventories.getInventories()) {
                                                if (name.toLowerCase().startsWith(typed))
                                                builder.suggest(name);
                                            }
                                        }
                                        return builder.buildFuture();
                                    })
                                    .executes(context -> {
                                        ClientPlayerEntity player = getPlayer();
                                        String name = StringArgumentType.getString(context, "name");
                                        if (player != null && name != null) {
                                            SavedInventories.loadInventory(name);
                                            LOGGER.info("Loaded inventory: " + name);
                                        }
                                        return Command.SINGLE_SUCCESS;
                                    })
                            )
                    )
                    .then(ClientCommandManager.literal("save")
                            .then(ClientCommandManager.argument("name", StringArgumentType.string())
                                    .executes(context -> {
                                        ClientPlayerEntity player = getPlayer();
                                        String name = StringArgumentType.getString(context, "name");
                                        if (player != null && name != null) {
                                            SavedInventories.addInventory(player, name);
                                            LOGGER.info("Saved inventory: " + name);
                                        }
                                        return Command.SINGLE_SUCCESS;
                                    })
                            )
                    )
                    .then(ClientCommandManager.literal("remove")
                            .then(ClientCommandManager.argument("name", StringArgumentType.string())
                                    .suggests((context, builder) -> {
                                        ClientPlayerEntity player = getPlayer();
                                        if (player != null) {
                                            for (String name : SavedInventories.getInventories()) {
                                                builder.suggest(name);
                                            }
                                        }
                                        return builder.buildFuture();
                                    })
                                    .executes(context -> {
                                        ClientPlayerEntity player = getPlayer();
                                        String name = StringArgumentType.getString(context, "name");
                                        if (player != null && name != null) {
                                            SavedInventories.removeInventory(player, name);
                                            LOGGER.info("Removed inventory: " + name);
                                        }
                                        return Command.SINGLE_SUCCESS;
                                    })
                            )
                    )
            );
        });

    }
}
