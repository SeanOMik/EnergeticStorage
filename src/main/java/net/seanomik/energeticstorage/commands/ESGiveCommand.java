package net.seanomik.energeticstorage.commands;

import net.seanomik.energeticstorage.EnergeticStorage;
import net.seanomik.energeticstorage.files.PlayersFile;
import net.seanomik.energeticstorage.utils.ItemConstructor;
import net.seanomik.energeticstorage.utils.PermissionChecks;
import net.seanomik.energeticstorage.utils.Reference;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.sql.Ref;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ESGiveCommand implements TabExecutor {
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> tab = new ArrayList<>();

        switch (args.length) {
            case 1:
                tab.addAll(Arrays.asList("drive", "system"));
                break;
            case 2:
                if (args[0].equals("drive")) {
                    tab.addAll(Arrays.asList("1k", "4k", "16k", "64k"));
                } else if (args[0].equals("system")) {
                    for (Player player : Bukkit.getServer().getOnlinePlayers()) {
                        tab.add(player.getDisplayName());
                    }
                }
                break;
            case 3:
                if (args[0].equals("drive")) {
                    for (Player player : Bukkit.getServer().getOnlinePlayers()) {
                        tab.add(player.getDisplayName());
                    }
                }

                break;
        }

        return tab;
    }

    private String generateCommandUsage(String[] args) {
        if (args.length > 0) {
            if (args[0].equals("drive")) {
                return Reference.PREFIX + "Usage: /esgive drive [1k, 4k, 16k, 64k] (player)";
            }

            return Reference.PREFIX + "Usage: /esgive [drive/system] [1k, 4k, 16k, 64k] (player)";
        } else {
            return Reference.PREFIX + "Usage: /esgive [drive/system] [1k, 4k, 16k, 64k] (player)";
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!PermissionChecks.canESGive(sender)) {
            sender.sendMessage(Reference.PREFIX + ChatColor.RED + "You don't have permission for this command!");

            return true;
        }

        switch (args[0]) { // Switch on item type
            case "save":
                PlayersFile.savePlayerSystems(Reference.ES_SYSTEMS.get(((Player) sender).getUniqueId()));
                sender.sendMessage("Saved systems!");
                break;
            case "system":
                //Player p = (Player) sender;

                if (args.length == 2) {
                    if (!PermissionChecks.canESGiveOthers(sender)) {
                        sender.sendMessage(Reference.PREFIX + ChatColor.RED + "You don't have permission to give an item to another player!");

                        return true;
                    }

                    Player player = Bukkit.getPlayer(args[1]);
                    if (player != null) {
                        player.getInventory().addItem(ItemConstructor.createSystemBlock());

                        sender.sendMessage(Reference.PREFIX + ChatColor.GREEN + "Gave an ES System to " + player.getDisplayName());
                    } else {
                        sender.sendMessage(Reference.PREFIX + ChatColor.RED + "Player does not exist or is not online!");
                    }
                } else if (args.length == 1) {
                    if (sender instanceof Player) {
                        Player player = (Player) sender;
                        player.getInventory().addItem(ItemConstructor.createSystemBlock());
                    } else {
                        sender.sendMessage(Reference.PREFIX + ChatColor.RED + "Supply a player to run this command!");
                        sender.sendMessage(generateCommandUsage(args));
                    }
                }
                break;
            case "drive":
                if (args.length < 2) {
                    sender.sendMessage(generateCommandUsage(args));
                    break;
                }

                if (args[1].equals("1k") || args[1].equals("4k") || args[1].equals("16k") || args[1].equals("64k")) {
                    int size = Integer.parseInt(args[1].replace("k", "")) * 1024;

                    if (args.length == 3) {
                        if (!PermissionChecks.canESGiveOthers(sender)) {
                            sender.sendMessage(Reference.PREFIX + ChatColor.RED + "You don't have permission to give an item to another player!");

                            return true;
                        }

                        Player player = Bukkit.getPlayer(args[2]);
                        if (player != null) {
                            player.getInventory().addItem(ItemConstructor.createDrive(size, 0, 0));

                            sender.sendMessage(Reference.PREFIX + ChatColor.GREEN + "Gave an ES Drive to " + player.getDisplayName());
                        } else {
                            sender.sendMessage(Reference.PREFIX + ChatColor.RED + "Player does not exist or is not online!");
                        }
                    } else {
                        if (sender instanceof Player) {
                            Player player = (Player) sender;
                            player.getInventory().addItem(ItemConstructor.createDrive(size, 0, 0));
                        } else {
                            sender.sendMessage(Reference.PREFIX + ChatColor.RED + "Supply a player to run this command!");
                            sender.sendMessage(generateCommandUsage(args));
                        }
                    }
                } else {
                    sender.sendMessage(generateCommandUsage(args));
                }

                break;
        }

        return true;
    }
}
