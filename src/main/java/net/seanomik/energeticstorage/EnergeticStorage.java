package net.seanomik.energeticstorage;

import net.seanomik.energeticstorage.commands.ESGiveCommand;
import net.seanomik.energeticstorage.commands.ESReloadCommand;
import net.seanomik.energeticstorage.files.ConfigFile;
import net.seanomik.energeticstorage.files.PlayersFile;
import net.seanomik.energeticstorage.listeners.BlockBreakListener;
import net.seanomik.energeticstorage.listeners.BlockPlaceListener;
import net.seanomik.energeticstorage.listeners.PlayerInteractListener;
import net.seanomik.energeticstorage.objects.ESSystem;
import net.seanomik.energeticstorage.tasks.HopperTask;
import net.seanomik.energeticstorage.utils.ItemRecipies;
import net.seanomik.energeticstorage.utils.Reference;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Chest;
import org.bukkit.block.data.type.Hopper;
import org.bukkit.craftbukkit.libs.jline.internal.Nullable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.world.WorldSaveEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public final class EnergeticStorage extends JavaPlugin implements Listener {
    private static EnergeticStorage plugin;
    private static HopperTask hopperTask;

    @Override
    public void onEnable() {
        plugin = this;

        registerCommands();
        registerListener();
        ItemRecipies.registerRecipes();

        ConfigFile.getConfig().saveDefaultConfig();
        PlayersFile.getConfig().saveDefaultConfig();

        Reference.ES_SYSTEMS = PlayersFile.getAllSystems();

        if (ConfigFile.isHopperInputEnabled()) {
            hopperTask = new HopperTask();
            hopperTask.runTaskTimerAsynchronously(this, 0L, 8L);
        }
    }

    private void registerCommands() {
        getCommand("esgive").setExecutor(new ESGiveCommand());
        getCommand("esreload").setExecutor(new ESReloadCommand());
    }

    private void registerListener() {
        getServer().getPluginManager().registerEvents(Reference.ES_TERMINAL_GUI, this);
        getServer().getPluginManager().registerEvents(Reference.ES_DRIVE_GUI, this);
        getServer().getPluginManager().registerEvents(Reference.ES_SYSTEM_SECURITY_GUI, this);
        getServer().getPluginManager().registerEvents(new PlayerInteractListener(), this);
        getServer().getPluginManager().registerEvents(new BlockBreakListener(), this);
        getServer().getPluginManager().registerEvents(new BlockPlaceListener(), this);
        getServer().getPluginManager().registerEvents(this, this);
    }

    /*public void cachePlayersSystems(Player player) {
        if (PlayersFile.doesPlayerHaveSystem(player.getUniqueId())) {
            Reference.ES_SYSTEMS.put(player.getUniqueId(), PlayersFile.getPlayersSystems(player.getUniqueId()));
        }
    }*/

    @EventHandler
    public void onWorldSaveEvent(WorldSaveEvent event) {
        for (Map.Entry<UUID, List<ESSystem>> systemEntry : Reference.ES_SYSTEMS.entrySet()) {
            PlayersFile.savePlayerSystems(systemEntry.getValue());
        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public static EnergeticStorage getPlugin() {
        return plugin;
    }

    public static HopperTask getHopperTask() {
        return hopperTask;
    }

    public static void setHopperTask(HopperTask hopperTask) {
        EnergeticStorage.hopperTask = hopperTask;
    }
}
