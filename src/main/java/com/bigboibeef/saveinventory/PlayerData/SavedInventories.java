package com.bigboibeef.saveinventory.PlayerData;

import com.bigboibeef.saveinventory.SaveInventory;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Style;
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
    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(ItemStack.class, new ItemStackAdapter())
            .setPrettyPrinting()
            .create();
    private static final Path SAVE_FILE = Paths.get("saved_inventories.json");

    public static void loadData() {
        if (Files.exists(SAVE_FILE)) {
            try (Reader reader = Files.newBufferedReader(SAVE_FILE)) {
                Type type = new TypeToken<HashMap<String, HashMap<Integer, ItemStack>>>() {}.getType();
                HashMap<String, HashMap<Integer, ItemStack>> loaded = GSON.fromJson(reader, type);
                if (loaded != null) {
                    savedInventories.clear();
                    savedInventories.putAll(loaded);
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
        for (int i = 0; i < player.getInventory().size(); i++) {
            items.put(i, player.getInventory().getStack(i).copy());
        }
        savedInventories.put(invName, items);
        saveData();
        player.playSound(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP);
        player.sendMessage(Text.literal("[SI] ")
                .setStyle(Style.EMPTY.withColor(0xEFB13C))
                .append(Text.literal("Saved inventory as ")
                        .styled(s -> s.withColor(Formatting.GREEN))
                        .append(Text.literal(invName).styled(s -> s.withColor(Formatting.AQUA)))));
    }

    public static void removeInventory(ClientPlayerEntity player, String invName) {
        if (savedInventories.remove(invName) != null) {
            saveData();
            player.playSound(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP);
            player.sendMessage(Text.literal("[SI] ")
                    .setStyle(Style.EMPTY.withColor(0xEFB13C))
                    .append(Text.literal("Removed inventory called ")
                            .styled(s -> s.withColor(Formatting.GREEN))
                            .append(Text.literal(invName).styled(s -> s.withColor(Formatting.AQUA)))));
        } else {
            player.playSound(SoundEvents.ENTITY_VILLAGER_HURT);
            player.sendMessage(Text.literal("[SI] ")
                    .setStyle(Style.EMPTY.withColor(0xEFB13C))
                    .append(Text.literal("No inventory named ")
                            .styled(s -> s.withColor(Formatting.RED))
                            .append(Text.literal(invName).styled(s -> s.withColor(Formatting.AQUA)))));
        }
    }

    public static void loadInventory(String invName) {
        loadData();
        ClientPlayerEntity player = SaveInventory.getPlayer();
        if (player == null) return;

        if (!savedInventories.containsKey(invName)) {
            player.sendMessage(Text.literal("[SI] Inventory '")
                    .styled(s -> s.withColor(Formatting.RED))
                    .append(Text.literal(invName).styled(s -> s.withColor(Formatting.AQUA)))
                    .append(Text.literal("' not found.")));
            return;
        }

        HashMap<Integer, ItemStack> desiredMap = savedInventories.get(invName);
        ScreenHandler handler = player.playerScreenHandler;
        ClientPlayNetworkHandler net = MinecraftClient.getInstance().getNetworkHandler();
        int syncId = handler.syncId;

        Map<Integer, Slot> slotByIndex = new HashMap<>();
        Map<Integer, ItemStack> currentStacks = new HashMap<>();
        for (Slot slot : handler.slots) {
            if (slot.inventory != player.getInventory()) continue;
            int idx = slot.getIndex();
            slotByIndex.put(idx, slot);
            currentStacks.put(idx, slot.getStack().copy());
        }

        for (Map.Entry<Integer, ItemStack> entry : desiredMap.entrySet()) {
            int targetIdx = entry.getKey();
            ItemStack want = entry.getValue();
            if (want.isEmpty()) continue;

            ItemStack haveHere = currentStacks.getOrDefault(targetIdx, ItemStack.EMPTY);
            if (haveHere.getItem().equals(want.getItem()) && haveHere.getCount() == want.getCount()) {
                //SaveInventory.LOGGER.info("Slot " + targetIdx + " already has desired, skipping.");
                continue;
            }

            Integer sourceIdx = null;
            for (Map.Entry<Integer, ItemStack> e2 : currentStacks.entrySet()) {
                int idx2 = e2.getKey();
                ItemStack st = e2.getValue();
                if (!st.isEmpty() && st.getItem().equals(want.getItem()) && st.getCount() == want.getCount()) {
                    sourceIdx = idx2;
                    break;
                }
            }
            if (sourceIdx == null) {
                //SaveInventory.LOGGER.info("Desired " + want + " not found; leaving slot " + targetIdx + " empty.");
                continue;
            }

            Slot sourceSlot = slotByIndex.get(sourceIdx);
            Slot targetSlot = slotByIndex.get(targetIdx);
            //SaveInventory.LOGGER.info("Swapping " + want + " from slot " + sourceIdx + " to slot " + targetIdx);

            int a1 = handler.nextRevision();
            net.sendPacket(new ClickSlotC2SPacket(syncId, a1, sourceSlot.id, 0, SlotActionType.PICKUP, ItemStack.EMPTY, Int2ObjectMaps.emptyMap()));
            int a2 = handler.nextRevision();
            net.sendPacket(new ClickSlotC2SPacket(syncId, a2, targetSlot.id, 0, SlotActionType.PICKUP, ItemStack.EMPTY, Int2ObjectMaps.emptyMap()));
            int a3 = handler.nextRevision();
            net.sendPacket(new ClickSlotC2SPacket(syncId, a3, sourceSlot.id, 0, SlotActionType.PICKUP, ItemStack.EMPTY, Int2ObjectMaps.emptyMap()));

            currentStacks.put(sourceIdx, haveHere);
            currentStacks.put(targetIdx, want.copy());
        }

        List<Integer> emptyTargets = new ArrayList<>();
        for (int i = 0; i < player.getInventory().size(); i++) {
            if (desiredMap.getOrDefault(i, ItemStack.EMPTY).isEmpty() &&
                    currentStacks.getOrDefault(i, ItemStack.EMPTY).isEmpty()) {
                emptyTargets.add(i);
            }
        }

        for (Map.Entry<Integer, ItemStack> e : new ArrayList<>(currentStacks.entrySet())) {
            int idx = e.getKey();
            ItemStack st = e.getValue();
            if (st.isEmpty()) continue;
            if (desiredMap.getOrDefault(idx, ItemStack.EMPTY).isEmpty()) {
                if (emptyTargets.isEmpty()) break;
                int destIdx = emptyTargets.remove(0);
                if (destIdx == idx) continue;

                Slot sourceSlot = slotByIndex.get(idx);
                Slot destSlot = slotByIndex.get(destIdx);
                //SaveInventory.LOGGER.info("Moving extra " + st + " from slot " + idx + " to empty slot " + destIdx);
                int b1 = handler.nextRevision();
                net.sendPacket(new ClickSlotC2SPacket(syncId, b1, sourceSlot.id, 0, SlotActionType.PICKUP, ItemStack.EMPTY, Int2ObjectMaps.emptyMap()));
                int b2 = handler.nextRevision();
                net.sendPacket(new ClickSlotC2SPacket(syncId, b2, destSlot.id, 0, SlotActionType.PICKUP, ItemStack.EMPTY, Int2ObjectMaps.emptyMap()));

                currentStacks.put(idx, ItemStack.EMPTY);
                currentStacks.put(destIdx, st.copy());
            }
        }

        handler.sendContentUpdates();
        player.playSound(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP);
            player.sendMessage(Text.literal("[SI] ")
                .setStyle(Style.EMPTY.withColor(0xEFB13C))
                .append(Text.literal("Inventory organized: ")
                        .styled(s -> s.withColor(Formatting.GREEN))
                        .append(Text.literal(invName).styled(s -> s.withColor(Formatting.AQUA)))));
    }

    public static Set<String> getInventories() {
        loadData();
        return savedInventories.keySet();
    }
}
