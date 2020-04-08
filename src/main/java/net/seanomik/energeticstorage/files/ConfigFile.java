package net.seanomik.energeticstorage.files;

import net.seanomik.energeticstorage.EnergeticStorage;
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
}

	