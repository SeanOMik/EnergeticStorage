package net.seanomik.energeticstorage.tasks;

import net.seanomik.energeticstorage.EnergeticStorage;
import net.seanomik.energeticstorage.files.PlayersFile;
import net.seanomik.energeticstorage.objects.ESSystem;
import net.seanomik.energeticstorage.utils.Reference;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.libs.jline.internal.Nullable;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class HopperTask extends BukkitRunnable {
    private boolean isHopperInputting(org.bukkit.material.Hopper hopper, BlockFace hopperRelative) {
        BlockFace hopperFacing = hopper.getFacing();

        switch (hopperRelative) {
            case UP:
                return hopperFacing == BlockFace.DOWN;
            case DOWN:
                return hopperFacing == BlockFace.UP;
            case EAST:
                return hopperFacing == BlockFace.WEST;
            case WEST:
                return hopperFacing == BlockFace.EAST;
            case SOUTH:
                return hopperFacing == BlockFace.NORTH;
            case NORTH:
                return hopperFacing == BlockFace.SOUTH;
        }

        return false;
    }

    private Map<BlockFace, Block> getRelativeHoppers(Block block) {
        // relative face, block
        Map<BlockFace, Block> hoppers = new HashMap<>();

        Block upBlock = block.getRelative(BlockFace.UP);
        Block downBlock = block.getRelative(BlockFace.DOWN);
        Block eastBlock = block.getRelative(BlockFace.EAST);
        Block westBlock = block.getRelative(BlockFace.WEST);
        Block northBlock = block.getRelative(BlockFace.NORTH);
        Block southBlock = block.getRelative(BlockFace.SOUTH);

        if (upBlock.getType() == Material.HOPPER) {
            hoppers.put(BlockFace.UP, upBlock);
        }

        if (downBlock.getType() == Material.HOPPER) {
            hoppers.put(BlockFace.DOWN, downBlock);
        }

        if (eastBlock.getType() == Material.HOPPER) {
            hoppers.put(BlockFace.EAST, eastBlock);
        }

        if (westBlock.getType() == Material.HOPPER) {
            hoppers.put(BlockFace.WEST, westBlock);
        }

        if (northBlock.getType() == Material.HOPPER) {
            hoppers.put(BlockFace.NORTH, northBlock);
        }

        if (southBlock.getType() == Material.HOPPER) {
            hoppers.put(BlockFace.SOUTH, southBlock);
        }

        return hoppers;
    }

    @Nullable
    private ItemStack getFirstItemStack(Inventory inventory) {
        ItemStack[] items = inventory.getContents();
        for (ItemStack item : items) {
            if (item != null && item.getType() != Material.AIR) {
                return item;
            }
        }

        return null;
    }

    @Override
    public void run() {
        for (Map.Entry<UUID, List<ESSystem>> systemEntry : Reference.ES_SYSTEMS.entrySet()) {
            for (ESSystem system : systemEntry.getValue()) {
                Block systemBlock = system.getLocation().getBlock();

                // Get all relative hoppers touching the system.
                Map<BlockFace, Block> hoppers = getRelativeHoppers(systemBlock);
                for (Map.Entry<BlockFace, Block> entry : hoppers.entrySet()) {
                    Bukkit.getScheduler().runTask(EnergeticStorage.getPlugin(), () -> {

                        try {
                            org.bukkit.block.Hopper hopper = (org.bukkit.block.Hopper) entry.getValue().getState();
                            org.bukkit.material.Hopper hopperType = (org.bukkit.material.Hopper) hopper.getData();
                            // Check if the hopper is facing towards the system
                            if (isHopperInputting(hopperType, entry.getKey())) {

                                // Find the first non-null item in the hopper inventory and add it
                                ItemStack firstItem = getFirstItemStack(hopper.getInventory());
                                if (firstItem != null) {
                                    ItemStack clonedItem = firstItem.clone();
                                    clonedItem.setAmount(1);

                                    firstItem.setAmount(firstItem.getAmount() - 1);

                                    system.addItem(clonedItem);
                                }
                            }
                        } catch (ClassCastException exception) {
                            // Ignore exception. These exceptions are only thrown in rare occasions
                            // that the hopper is destroyed during during this task.
                        }
                    });
                }
            }
        }
    }

    @Override
    public synchronized void cancel() throws IllegalStateException {
        for (Map.Entry<UUID, List<ESSystem>> systemEntry : Reference.ES_SYSTEMS.entrySet()) {
            PlayersFile.savePlayerSystems(systemEntry.getValue());
        }

        Bukkit.getScheduler().cancelTask(this.getTaskId());
    }
}
