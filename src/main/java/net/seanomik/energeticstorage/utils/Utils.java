package net.seanomik.energeticstorage.utils;

import jdk.internal.jline.internal.Nullable;
import net.seanomik.energeticstorage.objects.ESSystem;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

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

    public static void removeSimilarItem(Map<ItemStack, Integer> itemStacks, ItemStack item) {
        itemStacks.entrySet().removeIf(entry -> removeAmountFromLore(entry.getKey()).isSimilar(item));
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
}
