package net.seanomik.energeticstorage.listeners;

import de.tr7zw.changeme.nbtapi.NBTTileEntity;
import net.seanomik.energeticstorage.Skulls;
import net.seanomik.energeticstorage.objects.ESSystem;
import net.seanomik.energeticstorage.utils.PermissionChecks;
import net.seanomik.energeticstorage.utils.Reference;
import net.seanomik.energeticstorage.utils.Utils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

public class PlayerInteractListener implements Listener {

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getHand() == EquipmentSlot.HAND) {
            if (event.getClickedBlock().getType() == Material.PLAYER_HEAD) {
                Block block = event.getClickedBlock();
                Player player = event.getPlayer();

                NBTTileEntity blockNBT = new NBTTileEntity(block.getState());

                if (blockNBT.getCompound("Owner").getCompound("Properties").getCompoundList("textures").get(0).getString("Value").equals(Skulls.Computer.getTexture())) {
                    event.setCancelled(true);

                    ESSystem esSystem = Utils.findSystemAtLocation(block.getLocation());
                    if (esSystem != null) {
                        if (esSystem.isPlayerTrusted(player) || esSystem.isPublic() || esSystem.getOwner().equals(player.getUniqueId()) || PermissionChecks.canOpenUntrustedSystem(player)) {
                            Reference.ES_SYSTEM_GUI.initializeItems(player, esSystem);
                            Reference.ES_SYSTEM_GUI.openInventory(player, esSystem);
                        } else {
                            player.sendMessage(Reference.PREFIX + ChatColor.RED + "You are not trusted to this system!");
                        }
                    } else {
                        player.sendMessage(Reference.PREFIX + ChatColor.RED + "You are not trusted to this system!");
                    }
                }
/*
// You probably do not need an atomic reference IntelliJ just recommended this to me.
AtomicReference<Double> lastIncrement = new AtomicReference<>((double) 0);
Bukkit.getScheduler().runTaskTimer(plugin, ()-> {
    Location a = new Location(Bukkit.getWorld(""), 0,0,0,);
    Location b = new Location(Bukkit.getWorld(""), 0,0,0,);
    Vector d = b.subtract(a).toVector();

    lastIncrement.updateAndGet(v -> (v + 0.1));
    Location line = a.add(d).multiply(lastIncrement.get());

    // Spawns 1 heart particle
    line.getWorld().spawnParticle(Particle.HEART, line, 1);
}, (long) 20, (long) 15); // 20 = delay, 15 = timer
*/
            }
        }
    }
}
