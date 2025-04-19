package com.bigboibeef.treechopper.commands;

import com.bigboibeef.treechopper.PlayerData.SavedInventories;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.inventory.Inventory;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.apache.logging.log4j.core.jmx.Server;
import static com.bigboibeef.treechopper.TreeChopper.LOGGER;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;

public class SaveInventory {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("si")
                .then(CommandManager.literal("load")
                        .then(CommandManager.argument("name", StringArgumentType.string())
                                .suggests((context, builder) -> {
                                        for (String name : SavedInventories.getInventories(context.getSource().getPlayer())) {
                                            builder.suggest(name);
                                            LOGGER.info("Suggest: " + name);
                                        }
                                    return builder.buildFuture();
                                })
                                .executes(context -> {
                                    ServerPlayerEntity player = context.getSource().getPlayer();
                                    String name = StringArgumentType.getString(context, "name");
                                    if (player != null && name != null) {
                                        SavedInventories.loadInventory(player, name);
                                    }
                                    return Command.SINGLE_SUCCESS;
                                })
                        )
                )
                .then(CommandManager.literal("save")
                        .then(CommandManager.argument("name", StringArgumentType.string())
                                .executes(context -> {
                                    ServerPlayerEntity player = context.getSource().getPlayer();
                                    String name = StringArgumentType.getString(context, "name");
                                    if (player != null && name != null) {
                                        SavedInventories.addInventory(player, name);
                                    }
                                    return Command.SINGLE_SUCCESS;
                                })
                        )
                )
                .then(CommandManager.literal("remove")
                        .then(CommandManager.argument("name", StringArgumentType.string())
                                .suggests((context, builder) -> {
                                    for (String name : SavedInventories.getInventories(context.getSource().getPlayer())) {
                                        builder.suggest(name);
                                        LOGGER.info("Suggest: " + name);
                                    }
                                    return builder.buildFuture();
                                })
                                .executes(context -> {
                                    ServerPlayerEntity player = context.getSource().getPlayer();
                                    String name = StringArgumentType.getString(context, "name");
                                    if (player != null && name != null) {
                                        SavedInventories.removeInventory(player, name);
                                    }
                                    return Command.SINGLE_SUCCESS;
                                })

                        )
                )
                .then(CommandManager.literal("list")
                        .executes(context -> {
                            ServerPlayerEntity player = context.getSource().getPlayer();
                            if (player != null) {
                                SavedInventories.listInventories(player);
                            }
                            return Command.SINGLE_SUCCESS;
                        })
                )
        );
    }
}
