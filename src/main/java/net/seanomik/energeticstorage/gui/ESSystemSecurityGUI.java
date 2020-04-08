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
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.*;

public class ESSystemSecurityGUI implements InventoryHolder, Listener {
    private final Inventory inv;
    private final String title = "ES System Security";
    private final String removePlayerTitle = "Un-Trust Player";

    private Map<Player, ESSystem> openSystems = new HashMap<>();
    private Map<Player, Integer> openPages = new HashMap<>();

    public ESSystemSecurityGUI() {
        inv = Bukkit.createInventory(this, 9, title);
    }

    @Override
    public Inventory getInventory() {
        return inv;
    }

    // You can call this whenever you want to put the items in
    public void initializeItems(Player player, ESSystem openSystem) {
        Inventory inv = player.getOpenInventory().getTopInventory();

        for (int i = 0; i < inv.getSize(); i++) {
            inv.setItem(i, createGuiItem(Material.BLACK_STAINED_GLASS_PANE, ""));
        }

        inv.setItem(3, createGuiItem(Material.LIME_CONCRETE, "Trust player"));
        inv.setItem(4, createGuiItem(Material.RED_CONCRETE, "Un-Trust player"));
        if (openSystem.isPublic()) {
            inv.setItem(5, createGuiItem(Material.IRON_BARS, "Set system to private."));
        } else {
            inv.setItem(5, createGuiItem(Material.WHITE_STAINED_GLASS_PANE, "Set system to public."));
        }
    }

    public void initializeRemovePlayerItems(Player player, ESSystem openSystem) {
        Inventory inv = player.getOpenInventory().getTopInventory();
        //ESSystem openSystem = openSystems.get(player);

        int pageIndex = 0;
        if (!openPages.containsKey(player)) {
            openPages.put(player, pageIndex);
        } else {
            pageIndex = openPages.get(player);
        }

        for (int i = 0; i < inv.getSize(); i++) {
            inv.setItem(i, createGuiItem(Material.BLACK_STAINED_GLASS_PANE, ""));
        }

        List<UUID> trustedPlayers = openSystem.getTrustedPlayers();

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
                if (itemIndex < trustedPlayers.size()) {
                    UUID trustedUUID = trustedPlayers.get(itemIndex);
                    OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(trustedUUID);


                    ItemStack item = new ItemStack(Material.PLAYER_HEAD);

                    SkullMeta headMeta = (SkullMeta) item.getItemMeta();
                    headMeta.setOwningPlayer(offlinePlayer);
                    headMeta.setDisplayName(offlinePlayer.getName());
                    headMeta.setLore(Arrays.asList("Click to un-trust."));

                    item.setItemMeta(headMeta);

                    inv.setItem(i, item);
                } else {
                    inv.clear(i);
                }
            } else {
                inv.clear(i);
            }

            inv.setItem(49, createGuiItem(Material.BEDROCK, "Back"));
            inv.setItem(48, createGuiItem(Material.PAPER, "Last page"));
            inv.setItem(50, createGuiItem(Material.PAPER, "Next page"));
        }
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

    public void openInventory(Player p, ESSystem esSystem) {
        if (openSystems.containsKey(p)) {
            openSystems.replace(p, esSystem);
        } else {
            openSystems.put(p, esSystem);
        }

        Inventory cloneInv = Bukkit.createInventory(this, 9, title);
        p.openInventory(cloneInv);
        initializeItems(p, esSystem);
    }

    public void openRemoveInventory(Player p, ESSystem esSystem) {
        if (openSystems.containsKey(p)) {
            openSystems.replace(p, esSystem);
        } else {
            openSystems.put(p, esSystem);
        }

        Inventory removeInv = Bukkit.createInventory(this, 9 * 6, removePlayerTitle);
        p.openInventory(removeInv);
        initializeRemovePlayerItems(p, esSystem);
    }

    // Remove cached player data
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        Inventory inventory = event.getInventory();

        // Checks if the closing inventory is this inventory.
        if (inventory.getHolder() != null && inventory.getHolder() == this) {
            // This checks if the closing inventory is not a menu of this GUI class.
            Bukkit.getScheduler().runTaskLater(EnergeticStorage.getPlugin(), ()-> {
                Inventory inventory1 = event.getPlayer().getOpenInventory().getTopInventory();

                Player player = (Player) event.getPlayer();
                if (inventory1.getHolder() == null || inventory1.getHolder() != this) {
                    openSystems.remove(player);
                }
            }, (long) 0.1);
        }
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

            ESSystem openSystem = openSystems.get(player);

            // Handle type of click.
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
                    if (event.getView().getTitle().equals(removePlayerTitle)) {
                        // At remove player menu
                        int pageIndex = openPages.get(player);
                        if (slot == 48) { // Last page
                            List<UUID> trustedUUIDs = openSystem.getTrustedPlayers();
                            if (trustedUUIDs.size() > pageIndex * 28 ) {
                                openPages.replace(player, pageIndex + 1);
                            }
                        } else if (slot == 49) { // Back
                            openInventory(player, openSystem);
                        } else if (slot == 50) { // Next page
                            openPages.replace(player, Math.max(0, pageIndex - 1));
                        } else {
                            if (Utils.isItemValid(clickedItem) && clickedItem.getType() == Material.PLAYER_HEAD) {
                                SkullMeta headMeta = (SkullMeta) clickedItem.getItemMeta();

                                OfflinePlayer unTrustingPlayer = headMeta.getOwningPlayer();
                                openSystem.removeTrustedPlayer(unTrustingPlayer.getUniqueId());

                                player.sendMessage(Reference.PREFIX + ChatColor.GREEN + unTrustingPlayer.getName() + " has been un-trusted from the system!");

                                player.closeInventory();
                                PlayersFile.savePlayerSystem(openSystem);
                            }
                        }
                    } else {
                        // At main menu
                        if (slot == 3) { // Add player
                            new AnvilGUI.Builder()
                                    .onComplete((plr, text) -> {
                                        if (text != null && !text.isEmpty()) {
                                            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(text);
                                            if (offlinePlayer.hasPlayedBefore()) {
                                                if (!openSystem.isPlayerTrusted(offlinePlayer.getUniqueId())) {
                                                    openSystem.addTrustedPlayer(offlinePlayer.getUniqueId());
                                                    plr.sendMessage(Reference.PREFIX + ChatColor.GREEN + text + " has been trusted in the system!");

                                                    if (offlinePlayer.isOnline()) {
                                                        Player onlinePlayer = offlinePlayer.getPlayer();
                                                        onlinePlayer.sendMessage(Reference.PREFIX + ChatColor.GREEN + "You were just trusted into " + plr.getDisplayName() + "'s ES System!");
                                                    }

                                                    PlayersFile.savePlayerSystem(openSystem);
                                                } else {
                                                    plr.sendMessage(Reference.PREFIX + ChatColor.RED + text + " is already trusted in the system!");
                                                }
                                            } else {
                                                plr.sendMessage(Reference.PREFIX + ChatColor.RED + text + " doesn't exist!");
                                                return AnvilGUI.Response.text("Player doesn't exist!");
                                            }
                                        }

                                        return AnvilGUI.Response.close();
                                    }).text("Player Name")
                                      .item(new ItemStack(Material.PLAYER_HEAD))
                                      .title("Enter player to trust.")
                                      .plugin(EnergeticStorage.getPlugin())
                                      .open(player);
                        } else if (slot == 4) { // Remove player
                            openRemoveInventory(player, openSystem);
                        } else if (slot == 5) { // Set to public/private
                            openSystem.setPublic(!openSystem.isPublic());
                            initializeItems(player, openSystem);
                            PlayersFile.savePlayerSystem(openSystem);
                        }
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
