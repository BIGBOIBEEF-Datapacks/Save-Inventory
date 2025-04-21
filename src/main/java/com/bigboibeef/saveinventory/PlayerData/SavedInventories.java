package com.bigboibeef.saveinventory.PlayerData;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;
import net.minecraft.util.collection.DefaultedList;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static com.mojang.text2speech.Narrator.LOGGER;

public class SavedInventories {

    private static final Map<String, HashMap<Integer, ItemStack>> savedInventories = new HashMap<>();
    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(ItemStack.class, new ItemStackAdapter())
            .setPrettyPrinting()
            .create();
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

    public static void addInventory(ClientPlayerEntity player, String invName) {
        if (player == null) return;

        HashMap<Integer, ItemStack> items = new HashMap<>();
        DefaultedList<ItemStack> playerItems = player.getInventory().main;

        for (int i = 0; i < playerItems.size(); i++) {
            ItemStack stack = playerItems.get(i);
            items.put(i, stack != null ? stack.copy() : ItemStack.EMPTY);
        }

        DefaultedList<ItemStack> playerArmor = player.getInventory().armor;
        for (int i = 0; i < playerArmor.size(); i++) {
            ItemStack stack = playerArmor.get(i);
            items.put(36 + i, stack != null ? stack.copy(): ItemStack.EMPTY);
        }

        ItemStack stack = player.getInventory().offHand.get(0);
        items.put(40, stack != null ? stack.copy(): ItemStack.EMPTY);
        LOGGER.info(playerItems.size() + "");

        System.out.println("Saving inventory for: " + invName);
        for (Map.Entry<Integer, ItemStack> entry : items.entrySet()) {
            System.out.println("Slot " + entry.getKey() + " = " + entry.getValue().getItem().toString());
        }
        savedInventories.put(invName, items);
        saveData();


        LOGGER.info("Saved inventory (" + invName + ") successfully.");
        player.playSound(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP);
        player.sendMessage(
                Text.literal("[SI] ")
                .setStyle(Style.EMPTY.withColor((239 << 16) | (177 << 8) | 60))

                .append(Text.literal("You have saved your inventory as ")
                .styled(style -> style.withColor(Formatting.GREEN))
                .append(Text.literal(invName).styled(style -> style.withColor(Formatting.AQUA)))));
    }

    public static void removeInventory(ClientPlayerEntity player, String invName) {
        if (savedInventories.containsKey(invName) && player != null) {
            savedInventories.remove(invName);
            saveData();


            LOGGER.info("Removed inventory (" + invName + ") successfully.");
            player.playSound(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP);
            player.sendMessage(
                    Text.literal("[SI] ")
                    .setStyle(Style.EMPTY.withColor((239 << 16) | (177 << 8) | 60))

                    .append(Text.literal("You have removed your inventory called ")
                    .styled(style -> style.withColor(Formatting.GREEN))
                    .append(Text.literal(invName).styled(style -> style.withColor(Formatting.AQUA)))));
        } else {
            LOGGER.info("Removed inventory (" + invName + ") unsuccessfully. (no inventory named " + invName + ")");
            if (player != null) {
                player.playSound(SoundEvents.ENTITY_VILLAGER_HURT);
                player.sendMessage(
                    Text.literal("[SI] ")
                    .setStyle(Style.EMPTY.withColor((239 << 16) | (177 << 8) | 60))

                    .append(Text.literal("You do not have an inventory called ")
                    .styled(style -> style.withColor(Formatting.RED))
                    .append(Text.literal(invName).styled(style -> style.withColor(Formatting.AQUA)))));
            }
        }
    }

    public static void loadInventory(ClientPlayerEntity player, String invName) {
        if (player == null || !savedInventories.containsKey(invName)) {

            LOGGER.info("Loaded inventory (" + invName + ") unsuccessfully. (no inventory named " + invName + ")");
            if (player != null) {
                player.playSound(SoundEvents.ENTITY_VILLAGER_HURT);
                player.sendMessage(
                    Text.literal("[SI] ")
                    .setStyle(Style.EMPTY.withColor((239 << 16) | (177 << 8) | 60))

                    .append(Text.literal("You do not have an inventory called ")
                    .styled(style -> style.withColor(Formatting.RED))
                    .append(Text.literal(invName).styled(style -> style.withColor(Formatting.AQUA)))));
            }
            return;
        }

        HashMap<Integer, ItemStack> saved = savedInventories.get(invName);

        DefaultedList<ItemStack> playerItems = player.getInventory().main;
        for (int i = 0; i < playerItems.size(); i++) {
            ItemStack savedStack = saved.get(i);
            playerItems.set(i, savedStack != null ? savedStack.copy() : ItemStack.EMPTY);
        }

        DefaultedList<ItemStack> playerArmor = player.getInventory().armor;
        for (int i = 0; i < playerArmor.size(); i++) {
            ItemStack savedStack = saved.get(i + 36);
            playerArmor.set(i, savedStack != null ? savedStack.copy(): ItemStack.EMPTY);
        }

        ItemStack savedStack = saved.get(40);
        player.getInventory().offHand.set(0, savedStack != null ? savedStack.copy(): ItemStack.EMPTY);

        LOGGER.info("Loaded inventory (" + invName + ") successfully.");
        player.playSound(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP);
        player.sendMessage(
                Text.literal("[SI] ")
                .setStyle(Style.EMPTY.withColor((239 << 16) | (177 << 8) | 60))

                .append(Text.literal("Inventory loaded: ")
                .styled(style -> style.withColor(Formatting.GREEN))
                .append(Text.literal(invName).styled(style -> style.withColor(Formatting.AQUA)))));
    }

    public static Set<String> getInventories(ClientPlayerEntity player) {
        return savedInventories.keySet();
    }

    //in future, make a show inventory (name)
    //it will make a mini gui of your inventory, like a chest with all your items in it
}
