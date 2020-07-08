package net.seanomik.energeticstorage.gui;

import net.seanomik.energeticstorage.EnergeticStorage;
import net.seanomik.energeticstorage.files.PlayersFile;
import net.seanomik.energeticstorage.objects.ESSystem;
import net.seanomik.energeticstorage.utils.Reference;
import net.seanomik.energeticstorage.utils.Utils;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.craftbukkit.libs.it.unimi.dsi.fastutil.Hash;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class ESTerminalGUI implements InventoryHolder, Listener {
    private final Inventory globalInv;
    private final String title = "ES Terminal";

    private Map<UUID, ESSystem> openSystems = new HashMap<>();
    private Map<UUID, Integer> openPages = new HashMap<>();
    private Map<UUID, Map<ItemStack, Integer>> openSearches = new HashMap<>();

    public ESTerminalGUI() {
        globalInv = Bukkit.createInventory(this, 9*6, title);
    }

    @Override
    public Inventory getInventory() {
        return globalInv;
    }

    // You can open the inventory with this
    public void openInventory(Player p, ESSystem openSystem) {
        if (openSystems.containsKey(p.getUniqueId())) {
            openSystems.replace(p.getUniqueId(), openSystem);
        } else {
            openSystems.put(p.getUniqueId(), openSystem);
        }

        Inventory cloneInv = Bukkit.createInventory(this, 9*6, title);
        p.openInventory(cloneInv);
        initializeItems(p, openSystem);
    }

    // You can call this whenever you want to put the items in
    private void initializeItems(Player player, ESSystem openSystem) {
        // Only initialize the items for the players inventory, not all of them.
        Inventory inv = player.getOpenInventory().getTopInventory();

        for (int i = 0; i <9*6; i++) {
            inv.setItem(i, createGuiItem(Material.BLACK_STAINED_GLASS_PANE, ""));
        }

        inv.clear(0);

        inv.setItem(1, createGuiItem(Material.LIME_STAINED_GLASS_PANE, "To insert items, put them to the left."));
        inv.setItem(9, createGuiItem(Material.LIME_STAINED_GLASS_PANE, "To insert items, put them above."));

        inv.setItem(49, createGuiItem(Material.COMPASS, "Search Items"));
        inv.setItem(48, createGuiItem(Material.PAPER, "Last page"));
        inv.setItem(50, createGuiItem(Material.PAPER, "Next page"));

        // Store the current player page if it hasn't been stored already.
        // If the page has been saved, then get it.
        int pageIndex = 0;
        if (!openPages.containsKey(player.getUniqueId())) {
            openPages.put(player.getUniqueId(), pageIndex);
        } else {
            pageIndex = openPages.get(player.getUniqueId());
        }

        // Fill items with the searching items if there is a search
        Map<ItemStack, Integer> items = openSystem.getAllItems();
        if (openSearches.containsKey(player.getUniqueId())) {
            items = openSearches.get(player.getUniqueId());
        }

        for (int i = 10; i < 44; i++) {
            // Ignore the borders
            if (i == 18 || i == 27 || i == 36 || i == 17 || i == 26 || i == 35) {
                continue;
            }

            // Find the current line we're filling
            int lineIndex = i / 9 - 1;

            // Fill the box if an item can go there, else just empty it.
            if (i - (10 + lineIndex * 9) <= 6) { // Each line is 9 boxes.
                int itemIndex = i - (10 + lineIndex * 2) + pageIndex * 28; // The start of a new line is + 2 boxes, with each page showing 28 items.
                if (itemIndex < items.size()) {
                    try {
                        ItemStack item = (ItemStack) items.keySet().toArray()[itemIndex];
                        int amount = (int) items.values().toArray()[itemIndex];

                        ItemMeta itemMeta = item.getItemMeta();
                        if (itemMeta.hasLore()) {
                            List<String> lore = itemMeta.getLore();
                            if (Utils.listStringContainsString(lore, "Amount: ")) {
                                lore.removeIf(str -> (str.contains("Amount: ")));
                            }

                            lore.add("Amount: " + amount);
                            itemMeta.setLore(lore);
                        } else {
                            itemMeta.setLore(Arrays.asList("Amount: " + amount));
                        }
                        item.setItemMeta(itemMeta);

                        item.setAmount(Math.min(amount, 64));

                        inv.setItem(i, item);
                    } catch (NullPointerException e) {
                        System.out.println(Reference.PREFIX + ChatColor.RED + "A null item was stored and just attempted to load!");
                        inv.setItem(i, createGuiItem(Material.RED_STAINED_GLASS_PANE, "There was an error trying to load this item!"));
                    }
                } else {
                    inv.clear(i);
                }
            } else {
                inv.clear(i);
            }
        }
    }

    private ItemStack createGuiItem(Material material, String name) {
        ItemStack item = new ItemStack(material, 1);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        item.setItemMeta(meta);

        return item;
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
            // Check for a shift click or bottom click.
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

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        Inventory inventory = event.getInventory();

        if (inventory == null || inventory.getHolder() == null || inventory.getHolder() != this) {
            return;
        } else {
            event.setCancelled(true);
        }
    }

    // Remove cached player data
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        Inventory inventory = event.getInventory();

        if (inventory.getHolder() != null && inventory.getHolder() == this) {
            Player player = (Player) event.getPlayer();
            PlayersFile.savePlayerSystem(openSystems.get(player.getUniqueId()));

            // Check if the closing inventory is not just opening the search menu
            Bukkit.getScheduler().runTaskLaterAsynchronously(EnergeticStorage.getPlugin(), () -> {
                InventoryView view = player.getOpenInventory();
                if (!view.getTitle().equals("Search Terminal.") && !view.getTitle().equals(title)) {
                    openSystems.remove(player.getUniqueId());
                    openPages.remove(player.getUniqueId());
                    openSearches.remove(player.getUniqueId());
                }
        }, (long) 1);
        }
    }

    // Check for clicks on items
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        ClickType clickType = findClickType(event);

        if (clickType != ClickType.NONE && clickType != ClickType.INVENTORY_CLICK) {
            event.setCancelled(true);

            Player player = (Player) event.getWhoClicked();
            ItemStack clickedItem = event.getCurrentItem(); // Will be valid if clicks an item (i.e. taking an item from the inventory)
            ItemStack cursor = event.getCursor(); // Will be valid if an item is put into the inventory
            int slot = event.getSlot();

            ESSystem openSystem = openSystems.get(player.getUniqueId());

            // Make sure no items will get copied to other players open inventory
            Inventory inv = player.getOpenInventory().getTopInventory();

            int pageIndex = openPages.get(player.getUniqueId());
            if (slot == 48) { // Back page
                if (pageIndex != 0) {
                    pageIndex--;

                    openPages.replace(player.getUniqueId(), pageIndex);
                    initializeItems(player, openSystem);
                }
            } else if (slot == 49) { // Search
                new AnvilGUI.Builder()
                        .onComplete((plr, text) -> {
                            if (text != null && !text.isEmpty()) {
                                Map<ItemStack, Integer> items = openSystem.getAllItems();
                                Map<ItemStack, Integer> search = new HashMap<>();
                                for (Map.Entry<ItemStack, Integer> entry : items.entrySet()) {
                                    ItemStack item = entry.getKey();
                                    ItemMeta itemMeta = item.getItemMeta();
                                    int amount = entry.getValue();

                                    text = text.toLowerCase();
                                    List<String> lore = itemMeta.getLore();
                                    if (Utils.listStringContainsString(lore, text) || itemMeta.getDisplayName().toLowerCase().contains(text) || item.getType().toString().toLowerCase().contains(text) || item.getType().toString().toLowerCase().replace("_", " ").contains(text)) {
                                        search.put(item, amount);
                                    }
                                }

                                openSearches.put(plr.getUniqueId(), search);

                                Bukkit.getScheduler().runTaskLater(EnergeticStorage.getPlugin(), () -> {
                                    openInventory(plr, openSystem);
                                }, (long) 0.5);
                            } else {
                                openSearches.remove(plr.getUniqueId());

                                Bukkit.getScheduler().runTaskLater(EnergeticStorage.getPlugin(), () -> {
                                    openInventory(plr, openSystem);
                                }, (long) 0.5);
                            }

                            return AnvilGUI.Response.close();
                        })
                        .text("Enter Item name")
                        .item(new ItemStack(Material.PLAYER_HEAD))
                        .title("Search Terminal.")
                        .plugin(EnergeticStorage.getPlugin())
                        .open(player);
            } else if (slot == 50) {
                Map<ItemStack, Integer> items = openSystem.getAllItems();

                if (items.size() > (pageIndex + 1) * 28 ) {
                    pageIndex++;
                    openPages.replace(player.getUniqueId(), pageIndex);
                    initializeItems(player, openSystem);
                }
            } else {
                switch (clickType) {
                    case SHIFT_IN:
                        if (Utils.isItemValid(clickedItem)) {
                            if (openSystem.addItem(clickedItem)) {
                                event.setCancelled(false);

                                Bukkit.getScheduler().runTaskLater(EnergeticStorage.getPlugin(), () -> {
                                    initializeItems(player, openSystem);
                                }, (long) 0.1);
                            }
                        }

                        break;
                    // Currently just ignore a into half since there really isn't any point of it.
                    case SWAP_RIGHT_CLICK:
                        if (cursor.isSimilar(clickedItem)) {
                            // This will take an item out one by one when the player is holding the same material.
                            ItemStack takingItem = cursor.clone();
                            takingItem.setAmount(1); // Only request to take one item
                            openSystem.removeItem(takingItem);
                            cursor.setAmount(cursor.getAmount() + 1);

                            Bukkit.getScheduler().runTaskLater(EnergeticStorage.getPlugin(), () -> {
                                initializeItems(player, openSystem);
                            }, (long) 0.1);

                            break;
                        }
                    case SWAP:
                        event.setCancelled(true);

                        if (openSystem.addItem(cursor)) {
                            // Remove cursor item
                            event.getView().setCursor(null);

                            Bukkit.getScheduler().runTaskLater(EnergeticStorage.getPlugin(), () -> {
                                initializeItems(player, openSystem);
                            }, (long) 0.1);
                        }

                        break;
                    case INTO_HALF:
                        if (Utils.isItemValid(cursor)) {
                            // Only put one item into the system when the player right clicks with the stack.
                            // if we don't do this, the user can duplicate stacks of items.
                            ItemStack itemStack = cursor.clone();
                            itemStack.setAmount(1);
                            if (openSystem.addItem(itemStack)) {
                                event.setCancelled(false);

                                Bukkit.getScheduler().runTaskLater(EnergeticStorage.getPlugin(), () -> {
                                    initializeItems(player, openSystem);
                                }, (long) 0.1);
                            }
                        }

                        break;
                    case INTO:
                        if (Utils.isItemValid(cursor)) {
                            if (openSystem.addItem(cursor)) {
                                event.setCancelled(false);

                                Bukkit.getScheduler().runTaskLater(EnergeticStorage.getPlugin(), () -> {
                                    initializeItems(player, openSystem);
                                }, (long) 0.1);
                            }
                        }

                        break;
                    case OUT_HALF:
                    case SHIFT_OUT:
                    case OUT:
                        if (Utils.isItemValid(clickedItem)) {
                            ItemStack takingItem = clickedItem.clone();

                            /*Map<ItemStack, Integer> items = openSystem.getAllItems();
                            int amount = items.values().toArray()[Utils.indexOfSimilarItem(items.keySet(), clickedItem)]*/
                            takingItem.setAmount((clickType == ClickType.OUT_HALF && clickedItem.getAmount() / 2 > 0) ? clickedItem.getAmount() / 2 : 64);

                            takingItem = openSystem.removeItem(takingItem);
                            // Remove the item from the search map if its in there
                            if (openSearches.containsKey(player.getUniqueId())) {
                                for (ItemStack item : openSearches.get(player.getUniqueId()).keySet()) {
                                    ItemStack clone = item.clone();
                                    Utils.removeAmountFromLore(clone);

                                    openSearches.get(player.getUniqueId()).entrySet().removeIf(i -> (clone.equals(i.getKey())));
                                }
                            }

                            if (clickType == ClickType.SHIFT_OUT) {
                                player.getInventory().addItem(takingItem);
                            } else {
                                event.getView().setCursor(takingItem);
                            }

                            Bukkit.getScheduler().runTaskLater(EnergeticStorage.getPlugin(), () -> {
                                initializeItems(player, openSystem);
                            }, (long) 0.1);
                        }
                        break;
                }
            }
        }
    }
}