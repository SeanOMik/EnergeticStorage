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
import net.seanomik.energeticstorage.utils.ItemRecipes;
import net.seanomik.energeticstorage.utils.Reference;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldSaveEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public final class EnergeticStorage extends JavaPlugin implements Listener {
    private static EnergeticStorage plugin;
    private static HopperTask hopperTask;

    @Override
    public void onEnable() {
        plugin = this;

        registerCommands();
        registerListener();
        ItemRecipes.registerRecipes();

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

    /**
     * Saves all player systems.
     */
    private void savePlayerSystems() {
        for (Map.Entry<UUID, List<ESSystem>> systemEntry : Reference.ES_SYSTEMS.entrySet()) {
            PlayersFile.savePlayerSystems(systemEntry.getValue());
        }
    }

    @EventHandler
    public void onWorldSaveEvent(WorldSaveEvent event) {
        // Save the player systems when the world is saved
        savePlayerSystems();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        // Save the player systems on shutdown to prevent item loss
        savePlayerSystems();
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
