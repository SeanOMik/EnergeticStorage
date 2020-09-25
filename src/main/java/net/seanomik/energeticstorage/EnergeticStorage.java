package net.seanomik.energeticstorage;

import net.seanomik.energeticstorage.commands.ESGiveCommand;
import net.seanomik.energeticstorage.commands.ESReloadCommand;
import net.seanomik.energeticstorage.files.PlayersFile;
import net.seanomik.energeticstorage.listeners.BlockBreakListener;
import net.seanomik.energeticstorage.listeners.BlockPlaceListener;
import net.seanomik.energeticstorage.listeners.PlayerInteractListener;
import net.seanomik.energeticstorage.utils.ItemRecipies;
import net.seanomik.energeticstorage.utils.Reference;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

// @TODO: Add more config options
/*
 * Change Log:
 *  - Fix /esgive command runner from getting kicked with "Illegal Characters" error.
 *  - Fix systems that were placed on the walls.
 */
public final class EnergeticStorage extends JavaPlugin implements Listener {
    private static EnergeticStorage plugin;

    @Override
    public void onEnable() {
        plugin = this;

        registerCommands();
        registerListener();
        ItemRecipies.registerRecipes();

        PlayersFile.getConfig().saveDefaultConfig();

        Reference.ES_SYSTEMS = PlayersFile.getAllSystems();
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
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        //cachePlayersSystems(player);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public static EnergeticStorage getPlugin() {
        return plugin;
    }
}
