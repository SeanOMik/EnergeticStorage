package net.seanomik.energeticstorage.listeners;

import net.seanomik.energeticstorage.EnergeticStorage;
import net.seanomik.energeticstorage.files.PlayersFile;
import net.seanomik.energeticstorage.objects.ESDrive;
import net.seanomik.energeticstorage.objects.ESSystem;
import net.seanomik.energeticstorage.utils.ItemConstructor;
import net.seanomik.energeticstorage.utils.PermissionChecks;
import net.seanomik.energeticstorage.utils.Reference;
import net.seanomik.energeticstorage.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import java.util.LinkedList;
import java.util.List;

public class BlockBreakListener implements Listener {

    @EventHandler
    public void onBlockBreakListener(BlockBreakEvent event) {
        if (event.getBlock().getType() == Material.PLAYER_HEAD || event.getBlock().getType() == Material.PLAYER_WALL_HEAD) {
            Block block = event.getBlock();
            Player player = event.getPlayer();

            if (Utils.isBlockASystem(block)) {
                ESSystem esSystem = Utils.findSystemAtLocation(block.getLocation());

                if (esSystem != null) {
                    if (esSystem.isPlayerTrusted(player) || esSystem.getOwner().equals(player.getUniqueId()) || PermissionChecks.canDestroyUntrustedSystems(player)) {
                        for (ESDrive drive : esSystem.getESDrives()) {
                            block.getLocation().getWorld().dropItemNaturally(block.getLocation(), drive.getDriveItem());
                        }

                        // Remove the system from cache and storage
                        Bukkit.getScheduler().runTaskAsynchronously(EnergeticStorage.getPlugin(), () -> {
                            PlayersFile.removePlayerSystem(player.getUniqueId(), esSystem.getUUID());

                            List<ESSystem> systems = new LinkedList<>(Reference.ES_SYSTEMS.get(player.getUniqueId()));
                            systems.removeIf(esSystem::equals);
                            Reference.ES_SYSTEMS.replace(player.getUniqueId(), systems);
                        });

                        // Only drop the system if they're not in creative.
                        event.setDropItems(false);
                        if (player.getGameMode() != GameMode.CREATIVE) {
                            event.getBlock().getLocation().getWorld().dropItemNaturally(event.getBlock().getLocation(), ItemConstructor.createSystemBlock());
                        }
                    } else {
                        event.setCancelled(true);
                        player.sendMessage(Reference.PREFIX + ChatColor.RED + "You are not trusted to this system!");
                    }
                }
            }
        }
    }
}
