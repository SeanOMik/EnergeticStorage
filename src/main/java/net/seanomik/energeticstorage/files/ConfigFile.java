package net.seanomik.energeticstorage.files;

import net.seanomik.energeticstorage.EnergeticStorage;
import net.seanomik.energeticstorage.tasks.HopperTask;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class ConfigFile extends YamlConfiguration {
    private static ConfigFile config;
    private EnergeticStorage plugin;
    private File configFile;

    public static ConfigFile getConfig() {
        if (ConfigFile.config == null) {
            ConfigFile.config = new ConfigFile();
        }
        return ConfigFile.config;
    }

    public ConfigFile() {
        this.plugin = (EnergeticStorage) EnergeticStorage.getPlugin((Class) EnergeticStorage.class);
        this.configFile = new File(this.plugin.getDataFolder(), "config.yml");
        this.saveDefault();
        this.reload();
    }
    
    public void reload() {
        try {
            super.load(this.configFile);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void save() {
        try {
            super.save(this.configFile);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void saveDefault() {
        this.plugin.saveResource("config.yml", false);
    }
    
    public void saveConfig() {
        try {
            super.save(this.configFile);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void reloadConfig() {
        try {
            super.load(this.configFile);

            // Check if hopper input is not enabled and if the hopperTask is running. If it is, disable it.
            // else, we enable the task.
            if (!isHopperInputEnabled() && !EnergeticStorage.getHopperTask().isCancelled()) {
                EnergeticStorage.getHopperTask().cancel();
            } else if (isHopperInputEnabled() && EnergeticStorage.getHopperTask().isCancelled()) {
                EnergeticStorage.setHopperTask(new HopperTask());
                EnergeticStorage.getHopperTask().runTaskTimerAsynchronously(EnergeticStorage.getPlugin(), 0L, 8L);
                //EnergeticStorage.setHopperTask(new HopperTask().runTaskTimerAsynchronously(EnergeticStorage.getPlugin(), 0L, 8L));
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void saveDefaultConfig() {
        try {
            this.plugin.saveDefaultConfig();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Auto replace alternate color codes.
    @Override
    public String getString(String path) {
        // Only attempt to translate if the text is not empty.
        return (super.getString(path).isEmpty()) ? super.getString(path) : ChatColor.translateAlternateColorCodes('&', super.getString(path));
    }

    public static int getMaxTypes() {
        return getConfig().getInt("driveMaxTypes");
    }

    public static boolean isHopperInputEnabled(){return !getConfig().contains("allowHopperInput") || getConfig().getBoolean("allowHopperInput");}
}

	