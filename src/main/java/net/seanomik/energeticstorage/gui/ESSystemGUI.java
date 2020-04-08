package net.seanomik.energeticstorage.gui;

import net.seanomik.energeticstorage.files.PlayersFile;
import net.seanomik.energeticstorage.objects.ESDrive;
import net.seanomik.energeticstorage.objects.ESSystem;
import net.seanomik.energeticstorage.utils.Reference;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class ESSystemGUI implements InventoryHolder, Listener {
    private final Inventory inv;
    private final String title = "ES System";

    private Map<Player, ESSystem> openSystems = new HashMap<>();

    public ESSystemGUI() {
        inv = Bukkit.createInventory(this, 9, title);
    }

    @Override
    public Inventory getInventory() {
        return inv;
    }

    // You can call this whenever you want to put the items in
    public void initializeItems(Player player, ESSystem openSystem) {
        for (int i = 0; i < inv.getSize(); i++) {
            inv.setItem(i, createGuiItem(Material.BLACK_STAINED_GLASS_PANE, ""));
        }

        // Store the players open system
        if (openSystems.containsKey(player)) {
            openSystems.replace(player, openSystem);
        } else {
            openSystems.put(player, openSystem);
        }

        int maxSpace = 0;
        int filledSpace = 0;
        int filledTypes = 0;
        for (ESDrive drive : openSystem.getESDrives()) {
            maxSpace += drive.getSize();
            filledSpace += drive.getFilledSpace();
            filledTypes += drive.getFilledTypes();
        }

        // Get color of items text
        ChatColor spaceColor = ChatColor.GREEN;
        if (filledSpace >= maxSpace * 0.8) {
            spaceColor = ChatColor.RED;
        } else if (filledSpace >= maxSpace * 0.5) {
            spaceColor = ChatColor.YELLOW;
        }

        // Get color of types text
        ChatColor itemsColor = ChatColor.GREEN;
        if (filledTypes >= Reference.MAX_DRIVE_TYPES * 0.8) {
            itemsColor = ChatColor.RED;
        } else if (filledTypes >= Reference.MAX_DRIVE_TYPES * 0.5) {
            itemsColor = ChatColor.YELLOW;
        }

        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.BLUE + "Filled Items: " + spaceColor + filledSpace + ChatColor.BLUE + "/" + ChatColor.GREEN + maxSpace);
        lore.add(ChatColor.BLUE + "Filled Types: " + itemsColor + filledTypes + ChatColor.BLUE + "/" + ChatColor.GREEN + Reference.MAX_DRIVE_TYPES);
        inv.setItem(5, createGuiItem(Material.GLASS_PANE, "Drives", lore));
        inv.setItem(4, createGuiItem(Material.IRON_BARS, "Security"));
        inv.setItem(3, createGuiItem(Material.CHEST, "Terminal"));
    }

    private ItemStack createGuiItem(Material material, String name, List<String> description) {
        ItemStack item = new ItemStack(material, 1);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(description);
        item.setItemMeta(meta);

        return item;
    }

    private ItemStack createGuiItem(Material material, List<String> description) {
        ItemStack item = new ItemStack(material, 1);
        ItemMeta meta = item.getItemMeta();
        meta.setLore(description);
        item.setItemMeta(meta);

        return item;
    }

    private ItemStack createGuiItem(Material material, String name) {
        ItemStack item = new ItemStack(material, 1);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        item.setItemMeta(meta);

        return item;
    }

    // You can open the inventory with this
    public void openInventory(Player p, ESSystem esSystem) {
        if (openSystems.containsKey(p)) {
            openSystems.replace(p, esSystem);
        } else {
            openSystems.put(p, esSystem);
        }

        p.openInventory(inv);
    }

    private enum ClickType {
        NONE,
        SWAP,
        SWAP_RIGHT_CLICK,
        INTO,
        INTO_HALF,
        OUT,
        OUT_HALF,
        SHIFT_OUT,
        SHIFT_IN,
        INVENTORY_CLICK
    }

    private ClickType findClickType(InventoryClickEvent event) {
        Inventory inventory = event.getClickedInventory();

        if (inventory == null || inventory.getHolder() == null || inventory.getHolder() != this) {
            // Check for a shift click or bottom inventory click.
            if (event.getView().getTitle().equals(title)) {
                return (event.isShiftClick()) ? ClickType.SHIFT_IN : ClickType.INVENTORY_CLICK;
            }

            return ClickType.NONE;
        }

        ItemStack clickedItem = event.getCurrentItem();
        ItemStack cursor = event.getCursor();

        if ((clickedItem == null || clickedItem.getType() == Material.AIR) && (cursor == null || cursor.getType() == Material.AIR)) {
            return ClickType.NONE;
        } else if ( (clickedItem == null || clickedItem.getType() == Material.AIR) && (cursor != null || cursor.getType() != Material.AIR) ) {
            return (event.isLeftClick()) ? ClickType.INTO : ClickType.INTO_HALF;
        } else if (cursor == null || cursor.getType() == Material.AIR) {
            return (event.isShiftClick()) ? ClickType.SHIFT_OUT : (event.isLeftClick()) ? ClickType.OUT : ClickType.OUT_HALF;
        }

        return (event.isLeftClick()) ? ClickType.SWAP : ClickType.SWAP_RIGHT_CLICK;
    }

    // Remove cached player data
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        Inventory inventory = event.getInventory();

        if (inventory == null || inventory.getHolder() == null || inventory.getHolder() != this) {
            return;
        } else {
            Player player = (Player) event.getPlayer();
            openSystems.remove(player);
        }
    }

    // Check for clicks on items
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        ClickType clickType = findClickType(event);

        if (clickType != ClickType.NONE) {
            event.setCancelled(true);

            Player player = (Player) event.getWhoClicked();
            ItemStack clickedItem = event.getCurrentItem(); // Will be valid if clicks an item (i.e. taking an item from the inventory)
            ItemStack cursor = event.getCursor(); // Will be valid if an item is put into the inventory
            int slot = event.getSlot();
            ESSystem esSystem = openSystems.get(player);

            switch (clickType) {
                case SHIFT_IN:
                    break;
                case SWAP_RIGHT_CLICK:
                    break;
                case SWAP:
                    break;
                case INTO_HALF:
                    break;
                case INTO:
                    break;
                case OUT_HALF:
                    break;
                case OUT:
                    if (slot == 3) {
                        Reference.ES_TERMINAL_GUI.openInventory(player, esSystem);
                    } else if (slot == 4) {
                        Reference.ES_SYSTEM_SECURITY_GUI.openInventory(player, esSystem);
                    } else if (slot == 5) {
                        Reference.ES_DRIVE_GUI.openInventory(player, esSystem);
                    }
                    break;
                case SHIFT_OUT:
                    break;
                case INVENTORY_CLICK:
                    event.setCancelled(false);
                    break;
            }
        }
    }
}
