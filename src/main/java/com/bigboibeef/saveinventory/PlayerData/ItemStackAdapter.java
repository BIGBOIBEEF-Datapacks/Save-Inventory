package com.bigboibeef.saveinventory.PlayerData;

import com.google.gson.*;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import net.minecraft.component.ComponentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.PotionItem;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import java.lang.reflect.Type;

import static com.bigboibeef.saveinventory.SaveInventory.LOGGER;
import static com.bigboibeef.saveinventory.SaveInventory.getPlayer;

public class ItemStackAdapter implements JsonSerializer<ItemStack>, JsonDeserializer<ItemStack> {

    @Override
    public JsonElement serialize(ItemStack stack, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject json = new JsonObject();
        json.addProperty("item", Registries.ITEM.getId(stack.getItem()).toString());
        json.addProperty("count", stack.getCount());
        json.addProperty("damage", stack.getDamage());

        if (stack.getItem() instanceof PotionItem potion) {
            Codec<PotionContentsComponent> codec = DataComponentTypes.POTION_CONTENTS.getCodecOrThrow();
            JsonElement potionJson = codec.encodeStart(JsonOps.INSTANCE,  potion.getComponents().get(DataComponentTypes.POTION_CONTENTS))
                    .result()
                    .orElse(JsonNull.INSTANCE);

            json.add("potion", potionJson);
        }

        return json;
    }

    @Override
    public ItemStack deserialize(JsonElement jsonElement, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject json = jsonElement.getAsJsonObject();

        String itemId = json.get("item").getAsString();
        int count = json.has("count") ? json.get("count").getAsInt() : 1;
        int damage = json.has("damage") ? json.get("damage").getAsInt() : 0;

        Item item = Registries.ITEM.get(Identifier.of(itemId));
        if (item == Items.AIR && !itemId.equals("minecraft:air")) {
            throw new JsonParseException("Unknown item: " + itemId);
        }

        ItemStack stack = new ItemStack(item, count);
        stack.setDamage(damage);

        if (json.has("potion")) {
            Codec<PotionContentsComponent> codec = DataComponentTypes.POTION_CONTENTS.getCodecOrThrow();
            JsonElement potionJson = json.get("potion");

            codec.decode(JsonOps.INSTANCE, potionJson)
                    .result()
                    .map(com.mojang.datafixers.util.Pair::getFirst)
                    .ifPresent(component -> stack.set(DataComponentTypes.POTION_CONTENTS, component));
        }

        return stack;
    }

}
