package com.bigboibeef.treechopper.PlayerData;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class SavedInventories {

    private static final Map<String, HashMap<Integer, ItemStack>> savedInventories = new HashMap<>();
    private static final Gson GSON = new GsonBuilder().registerTypeAdapter(ItemStack.class, new ItemStackAdapter()).setPrettyPrinting().create();
    private static final Path SAVE_FILE = Paths.get("saved_inventories.json");

    public static void loadData() {
        if (Files.exists(SAVE_FILE)) {
            try (Reader reader = Files.newBufferedReader(SAVE_FILE)) {
                Type type = new TypeToken<HashMap<String, HashMap<Integer, ItemStack>>>() {}.getType();
                HashMap<String, HashMap<Integer, ItemStack>> loadedData = GSON.fromJson(reader, type);
                if (loadedData != null) {
                    savedInventories.clear();
                    savedInventories.putAll(loadedData);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void saveData() {
        try (Writer writer = Files.newBufferedWriter(SAVE_FILE)) {
            GSON.toJson(savedInventories, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void addInventory(ServerPlayerEntity player, String invName) {
        HashMap<Integer, ItemStack> items = new HashMap<>();
        Inventory inv = player.getInventory();
        for (int i = 0; i < inv.size(); i++) {
            ItemStack stack = inv.getStack(i);
            items.put(i, stack != null ? stack.copy() : ItemStack.EMPTY);
        }

        savedInventories.put(invName, items);
        saveData();

        player.sendMessage(Text.literal("You have saved your inventory as ")
                .styled(style -> style.withColor(Formatting.GREEN))
                .append(Text.literal(invName).styled(style -> style.withColor(Formatting.AQUA))));
    }

    public static void removeInventory(ServerPlayerEntity player, String invName) {
        Map<String, HashMap<Integer, ItemStack>> map = savedInventories;
        if (map == null || !map.containsKey(invName)) {
            player.sendMessage(Text.literal("You do not have an inventory called ")
                    .styled(style -> style.withColor(Formatting.RED))
                    .append(Text.literal(invName).styled(style -> style.withColor(Formatting.AQUA))));
        } else {
            map.remove(invName);
            saveData();
            player.sendMessage(Text.literal("You have removed your inventory called ")
                    .styled(style -> style.withColor(Formatting.GREEN))
                    .append(Text.literal(invName).styled(style -> style.withColor(Formatting.AQUA))));
        }
    }

    public static void loadInventory(ServerPlayerEntity player, String invName) {
        Map<String, HashMap<Integer, ItemStack>> map = savedInventories;
        if (map == null || !map.containsKey(invName)) {
            player.sendMessage(Text.literal("You do not have an inventory called ").styled(style -> style.withColor(Formatting.RED)).append(Text.literal(invName).styled(style -> style.withColor(Formatting.AQUA))));
            return;
        }

        HashMap<Integer, ItemStack> items = map.get(invName);
        Inventory inv = player.getInventory();

        if (!items.isEmpty()) {
            for (int i = 0; i < inv.size(); i++) {
                ItemStack saved = items.get(i);
                inv.setStack(i, saved != null ? saved.copy() : ItemStack.EMPTY);
            }

            player.sendMessage(Text.literal("Inventory loaded: ")
                    .styled(style -> style.withColor(Formatting.GREEN))
                    .append(Text.literal(invName).styled(style -> style.withColor(Formatting.AQUA))));
        } else {
            player.sendMessage(Text.literal("Saved inventory was empty!").styled(style -> style.withColor(Formatting.RED)));
        }
    }

    public static void listInventories(ServerPlayerEntity player) {
        Map<String, HashMap<Integer, ItemStack>> map = savedInventories;
        if (map == null || map.isEmpty()) {
            player.sendMessage(Text.literal("You do not have any saved inventories.")
                    .styled(style -> style.withColor(Formatting.RED)));
            return;
        }

        StringBuilder invs = new StringBuilder();
        int count = 0;
        for (String name : map.keySet()) {
            invs.append(name).append(", ");
            count++;
        }

        if (invs.length() > 2) {
            invs.setLength(invs.length() - 2);
        }

        player.sendMessage(Text.literal("You have " + count + " inventories: " + invs)
                .styled(style -> style.withColor(Formatting.AQUA)));
    }

    public static Set<String> getInventories(ServerPlayerEntity player) {
        Map<String, HashMap<Integer, ItemStack>> map = savedInventories;
        return map != null ? map.keySet() : Collections.emptySet();
    }
}
