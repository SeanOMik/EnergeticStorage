package net.seanomik.energeticstorage.utils;

import de.tr7zw.changeme.nbtapi.NBTItem;
import de.tr7zw.changeme.nbtapi.NBTTileEntity;
import net.seanomik.energeticstorage.Skulls;
import net.seanomik.energeticstorage.objects.ESSystem;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import javax.annotation.Nullable;
import java.util.*;

public class Utils {
    public static String convertLocationToString(final Location l) {
        if (l == null) {
            return "";
        }
        return l.getWorld().getName() + ":" + l.getBlockX() + ":" + l.getBlockY() + ":" + l.getBlockZ();
    }

    public static Location convertStringToLocation(final String s) {
        if (s == null || s.trim() == "") {
            return null;
        }
        final String[] parts = s.split(":");
        if (parts.length == 4) {
            final World w = Bukkit.getServer().getWorld(parts[0]);
            final int x = Integer.parseInt(parts[1]);
            final int y = Integer.parseInt(parts[2]);
            final int z = Integer.parseInt(parts[3]);
            return new Location(w, x, y, z);
        }
        return null;
    }

    /**
     * @param item Item to check if valid
     * This checks if the item is not null and also not air.
     *
     * @return boolean - If the item is vallid or not
     */
    public static boolean isItemValid(@Nullable ItemStack item) {
        return item != null && item.getType() != Material.AIR;
    }

    public static ESSystem findSystemAtLocation(Location location) {
        for (List<ESSystem> systems : Reference.ES_SYSTEMS.values()) {
            for (ESSystem system : systems) {
                if (system.getLocation().getBlockX() == location.getBlockX() && system.getLocation().getBlockY() == location.getBlockY() && system.getLocation().getBlockZ() == location.getBlockZ()) {
                    return system;
                }
            }
        }

        return null;
    }

    public static boolean containsSimilarItem(List<ItemStack> itemStacks, ItemStack item, boolean ignoreMeta) {
        for (ItemStack itemStack : itemStacks) {
            if (removeAmountFromLore(itemStack).isSimilar(item)) {
                return true;
            }
        }

        return false;
    }

    public static Map<ItemStack, Integer> removeSimilarItem(Map<ItemStack, Integer> itemStacks, ItemStack item) {
        removeAmountFromLore(item);
        // Doing this does not work. So we're gonna have to make some ugly code...
        // The reason it doesn't work is due to Spigot implementing its own `hashCode` implementation,
        // which Java's Iterator#remove method relies on and regenerates the hashCode when removing.
        //itemStacks.entrySet().removeIf(entry -> removeAmountFromLore(entry.getKey()).isSimilar(item));

        Map<ItemStack, Integer> items = new HashMap<>();
        for (Map.Entry<ItemStack, Integer> entry : itemStacks.entrySet()) {
            if (!removeAmountFromLore(entry.getKey()).isSimilar(item)) {
                items.put(entry.getKey(), entry.getValue());
            }
        }

        return items;
    }

    public static int indexOfSimilarItem(List<ItemStack> itemStacks, ItemStack item) {
        removeAmountFromLore(item);

        for (ItemStack itemStack : itemStacks) {
            removeAmountFromLore(itemStack);
            if (itemStack.isSimilar(item)) {
                return itemStacks.indexOf(itemStack);
            }
        }

        return -1;
    }

    public static ItemStack removeAmountFromLore(ItemStack item) {
        ItemMeta itemMeta = item.getItemMeta();
        if (itemMeta != null && itemMeta.getLore() != null && !itemMeta.getLore().isEmpty()) {
            itemMeta.setLore(removeAmountFromLore(itemMeta.getLore()));
            item.setItemMeta(itemMeta);
        }

        return item;
    }

    public static List<String> removeAmountFromLore(List<String> lore) {
        if (lore != null) {
            lore.removeIf(line -> line.contains("Amount: "));
        }

        return lore;
    }

    public static boolean listStringContainsString(List<String> list, String string) {
        string = string.toLowerCase();
        for (String str : list) {
            if (str.toLowerCase().contains(string)) {
                return true;
            }
        }

        return false;
    }

    public static boolean isBlockASystem(Block block) {
        NBTTileEntity blockNBT = new NBTTileEntity(block.getState());
        String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
        if (version.substring(0, 5).equals("v1_15") || version.substring(0, 5).equals("v1_14")) {
            return blockNBT.getCompound("Owner").getCompound("Properties").getCompoundList("textures").get(0).getString("Value").equals(Skulls.Computer.getTexture());
        } else if (version.substring(0, 5).equals("v1_16")) {
            return blockNBT.getCompound("SkullOwner").getCompound("Properties").getCompoundList("textures").get(0).getString("Value").equals(Skulls.Computer.getTexture());
        }

        return false;
    }

    public static boolean isItemADrive(ItemStack item) {
        NBTItem nbtItem = new NBTItem(item);
        return nbtItem.hasKey("ES_Drive");
    }
}
