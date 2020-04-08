package net.seanomik.energeticstorage.listeners;

import de.tr7zw.changeme.nbtapi.NBTTileEntity;
import net.seanomik.energeticstorage.Skulls;
import net.seanomik.energeticstorage.files.PlayersFile;
import net.seanomik.energeticstorage.objects.ESSystem;
import net.seanomik.energeticstorage.utils.PermissionChecks;
import net.seanomik.energeticstorage.utils.Reference;
import net.seanomik.energeticstorage.utils.Utils;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public class BlockPlaceListener implements Listener {

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (event.getBlock().getType() == Material.PLAYER_HEAD) {
            Block block = event.getBlock();
            Player player = event.getPlayer();

            NBTTileEntity blockNBT = new NBTTileEntity(block.getState());

            if (blockNBT.getCompound("Owner").getCompound("Properties").getCompoundList("textures").get(0).getString("Value").equals(Skulls.Computer.getTexture())) {
                if (PermissionChecks.canCreateSystem(player)) {
                    ESSystem newSystem = new ESSystem(player.getUniqueId(), UUID.randomUUID(), block.getLocation());
                    PlayersFile.savePlayerSystem(newSystem);

                    // If the player already has a system then add it to their cached systems, else just add it.
                    if (Reference.ES_SYSTEMS.containsKey(player.getUniqueId())) {
                        List<ESSystem> playerESSystems = new LinkedList<>(Reference.ES_SYSTEMS.get(player.getUniqueId()));
                        playerESSystems.add(newSystem);

                        Reference.ES_SYSTEMS.replace(player.getUniqueId(), playerESSystems);
                    } else {
                        Reference.ES_SYSTEMS.put(player.getUniqueId(), Arrays.asList(newSystem));
                    }
                }
            }
        }
    }
}
