package com.bigboibeef.treechopper.PlayerData;

import com.google.gson.*;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.lang.reflect.Type;

public class ItemStackAdapter implements JsonSerializer<ItemStack>, JsonDeserializer<ItemStack> {

    @Override
    public JsonElement serialize(ItemStack stack, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject json = new JsonObject();
        json.addProperty("item", Registries.ITEM.getId(stack.getItem()).toString());
        json.addProperty("count", stack.getCount());
        json.addProperty("damage", stack.getDamage());
        return json;
    }

    @Override
    public ItemStack deserialize(JsonElement jsonElement, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject json = jsonElement.getAsJsonObject();

        String itemId = json.get("item").getAsString();
        int count = json.has("count") ? json.get("count").getAsInt() : 1;
        int damage = json.has("damage") ? json.get("damage").getAsInt() : 0;

        Item item = Registries.ITEM.get(Identifier.of(itemId));
        if (item == null) {
            throw new JsonParseException("Unknown item: " + itemId);
        }

        ItemStack stack = new ItemStack(item, count);
        stack.setDamage(damage);
        return stack;
    }
}
