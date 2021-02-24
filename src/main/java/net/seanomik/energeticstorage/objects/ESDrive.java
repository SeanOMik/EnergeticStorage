package net.seanomik.energeticstorage.objects;

import de.tr7zw.changeme.nbtapi.NBTItem;
import net.seanomik.energeticstorage.utils.ItemConstructor;
import net.seanomik.energeticstorage.utils.ItemSerialization;
import net.seanomik.energeticstorage.utils.Reference;
import net.seanomik.energeticstorage.utils.Utils;
import org.apache.commons.text.StringEscapeUtils;
import org.bukkit.Material;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.inventory.ItemStack;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.*;

public class ESDrive implements Cloneable {
    private UUID uuid;
    private int size;
    private Map<ItemStack, Integer> items = new HashMap<>(); // Item, amount

    public ESDrive(int size) {
        this.size = size;
    }

    public ESDrive(int size, Map<ItemStack, Integer> items) {
        this(size);
        uuid = UUID.randomUUID();

        this.items = items;
    }

    public ESDrive(ItemStack driveItem) {
        NBTItem driveNBT = new NBTItem(driveItem);

        if (driveNBT.hasKey("ES_DriveItems")) {
            try {
                JSONParser jsonParser = new JSONParser();
                JSONArray itemJsonArray = (JSONArray) jsonParser.parse(driveNBT.getString("ES_DriveItems"));

                for (int i = 0; i < itemJsonArray.size(); i++) {
                    JSONObject itemObject = (JSONObject) itemJsonArray.get(i);

                    Map.Entry<ItemStack, Integer> item = ItemSerialization.deserializeItem((String) itemObject.get("itemYAML"));

                    items.put(item.getKey(), item.getValue());
                }
            } catch (ParseException | InvalidConfigurationException e) {
                e.printStackTrace();
            }
        }

        size = driveNBT.getInteger("ES_DriveMaxItemAmount");
        uuid = (driveNBT.hasKey("ES_DriveUUID")) ? UUID.fromString(driveNBT.getString("ES_DriveUUID")) : UUID.randomUUID();
    }

    public UUID getUUID() {
        return uuid;
    }

    public int getSize() {
        return size;
    }

    public int getFilledSpace() {
        int filled = 0;

        for (int amount : items.values()) {
            filled += amount;
        }

        return filled;
    }

    public int getFilledTypes() {
        List<Material> foundItems = new ArrayList<>();

        for (ItemStack item : items.keySet()) {
            if (!foundItems.contains(item.getType())) {
                foundItems.add(item.getType());
            }
        }

        return foundItems.size();
    }

    public Map<ItemStack, Integer> getItems() { return items; }

    public void setUUID(UUID uuid) {
        this.uuid = uuid;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public void setItems(Map<ItemStack, Integer> items) { this.items = items; }

    public ESDrive clone() {
        try {
            ESDrive drive = (ESDrive) super.clone();
            if (this.items != null) {
                items = new HashMap<>(items);
            }

            return drive;
        } catch (CloneNotSupportedException var2) {
            throw new Error(var2);
        }
    }

    public boolean isAvailable(ItemStack item) {
        return (Utils.isItemValid(item)) ? getFilledTypes() < Reference.MAX_DRIVE_TYPES && getFilledSpace() < size : getFilledSpace() < size;
    }

    public boolean addItem(ItemStack item) {
        item = item.clone();

        if (isAvailable(item)) {
            // The item is contained, then update the amount.
            if (Utils.containsSimilarItem(new ArrayList<>(items.keySet()), item, true)) {
                int amount = (int) items.values().toArray()[Utils.indexOfSimilarItem(new ArrayList<>(items.keySet()), item)] + item.getAmount();
                items = Utils.removeSimilarItem(items, item);
                items.put(item, amount);
            } else {
                items.put(item, item.getAmount());
            }

            return true;
        }

        return false;
    }
    
    public ItemStack removeItem(ItemStack item) {
        // If there isn't enough items stored to take out the requested amount, then just take out all that we can.
        int foundItemAmount = (int) items.values().toArray()[Utils.indexOfSimilarItem(new ArrayList<>(items.keySet()), item)];
        if (foundItemAmount - item.getAmount() < 1) {
            items = Utils.removeSimilarItem(items, item);
            item.setAmount(foundItemAmount);
        } else {
            int newAmount = foundItemAmount - item.getAmount();

            items = Utils.removeSimilarItem(items, item);
            items.put(item, newAmount);
        }

        return item;
    }

    private String exceptionMessage(Exception e) {
        return "An exception occurred in ESDrive (UUID:" + uuid + ", Exception: " + e.getMessage() + ")";
    }

    public ItemStack getDriveItem() {
        try {
            ItemStack drive = ItemConstructor.createDrive(size, getFilledSpace(), getFilledTypes());
            NBTItem driveNBT = new NBTItem(drive);

            JSONArray itemsJson = new JSONArray();
            for (Map.Entry<ItemStack, Integer> entry : items.entrySet()) {
                try {
                    String object = "{\"itemYAML\":\"" + StringEscapeUtils.escapeJson(ItemSerialization.serializeItem(entry.getKey(), entry.getValue())) + "\"}";
                    JSONObject itemJSON = (JSONObject) new JSONParser().parse(object);

                    itemsJson.add(itemJSON);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }

            driveNBT.setString("ES_DriveItems", itemsJson.toJSONString());
            drive = driveNBT.getItem();

            return drive;
        } catch (Exception e) {
            System.out.println(exceptionMessage(e));
        }

        return null;
    }
}
