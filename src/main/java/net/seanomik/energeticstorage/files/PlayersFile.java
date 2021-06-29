package net.seanomik.energeticstorage.files;

import com.google.gson.JsonParser;
import net.seanomik.energeticstorage.EnergeticStorage;
import net.seanomik.energeticstorage.objects.ESDrive;
import net.seanomik.energeticstorage.objects.ESSystem;
import net.seanomik.energeticstorage.utils.ItemSerialization;
import net.seanomik.energeticstorage.utils.Utils;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
//import org.bukkit.craftbukkit.libs.org.apache.commons.lang3.StringEscapeUtils;
import org.bukkit.inventory.ItemStack;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.util.*;

public class PlayersFile extends YamlConfiguration {
    private static PlayersFile config;
    private EnergeticStorage plugin;
    private File configFile;

    public static PlayersFile getConfig() {
        if (PlayersFile.config == null) {
            PlayersFile.config = new PlayersFile();
        }
        return PlayersFile.config;
    }

    public PlayersFile() {
        this.plugin = (EnergeticStorage) EnergeticStorage.getPlugin((Class) EnergeticStorage.class);
        this.configFile = new File(this.plugin.getDataFolder(), "players.yml");
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
        this.plugin.saveResource("players.yml", false);
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
        return (super.getString(path) == null || super.getString(path).isEmpty()) ? super.getString(path) : ChatColor.translateAlternateColorCodes('&', super.getString(path));
    }

    public static boolean doesPlayerHaveSystem(UUID uuid) {
        return getConfig().contains("players." + uuid + ".systems");
    }

    public static Map<UUID, List<ESSystem>> getAllSystems() {
        Map<UUID, List<ESSystem>> allSystems = new HashMap<>();

        for (String playerUUIDStr : getConfig().getConfigurationSection("players").getKeys(false)) {
            UUID playerUUID = UUID.fromString(playerUUIDStr);
            allSystems.put(playerUUID, getPlayersSystems(playerUUID));
        }

        return allSystems;
    }

    public static List<ESSystem> getPlayersSystems(UUID uuid) {
        List<ESSystem> systems = new ArrayList<>();
        for (String systemUUID : getConfig().getConfigurationSection("players." + uuid + ".systems").getKeys(false)) {
            String systemPath = "players." + uuid + ".systems." + systemUUID + ".";
            List<ESDrive> drives = new ArrayList<>();

            if (getConfig().contains(systemPath + "drives")) {
                for (String driveUUID : getConfig().getConfigurationSection(systemPath + "drives").getKeys(false)) {

                    Map<ItemStack, Integer> items = new HashMap();
                    if (getConfig().contains(systemPath + "drives." + driveUUID + ".items")) {
                        try {
                            JSONParser jsonParser = new JSONParser();
                            JSONArray itemJsonArray = (JSONArray) jsonParser.parse(getConfig().getString(systemPath + "drives." + driveUUID + ".items"));

                            for (int i = 0; i < itemJsonArray.size(); i++) {
                                JSONObject itemObject = (JSONObject) itemJsonArray.get(i);

                                Map.Entry<ItemStack, Integer> item = ItemSerialization.deserializeItem((String) itemObject.get("itemYAML"));

                                items.put(item.getKey(), item.getValue());
                            }
                        } catch (ParseException | InvalidConfigurationException e) {
                            e.printStackTrace();
                        }
                    }

                    int size = getConfig().getInt(systemPath + "drives." + driveUUID + ".size");

                    drives.add(new ESDrive(size, items));
                }
            }

            List<UUID> trustedUUIDs = new ArrayList<>();
            if (getConfig().contains(systemPath + "trustedUUIDs")) {
                try {
                    JSONArray trustedJson = (JSONArray) new JSONParser().parse(getConfig().getString(systemPath + "trustedUUIDs"));
                    for (int i = 0; i < trustedJson.size(); i++) {
                        JSONObject object = (JSONObject) trustedJson.get(i);

                        trustedUUIDs.add(UUID.fromString((String) object.get("UUID")));
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }

            boolean isPublic = getConfig().getBoolean(systemPath + "public");
            ESSystem.SortOrder sortOrder = ESSystem.SortOrder.valueOf(getConfig().getString(systemPath + "sortOrder"));

            Location loc = Utils.convertStringToLocation(getConfig().getString(systemPath + "loc"));
            systems.add(new ESSystem(uuid, UUID.fromString(systemUUID), loc, drives, trustedUUIDs, isPublic, sortOrder));
        }

        return systems;
    }

    public static void savePlayerSystem(ESSystem esSystem) {
        String systemPath = "players." + esSystem.getOwner() + ".systems." + esSystem.getUUID() + ".";

        getConfig().set(systemPath + "loc", Utils.convertLocationToString(esSystem.getLocation()));
        getConfig().set(systemPath + "public", esSystem.isPublic());
        getConfig().set(systemPath + "sortOrder", esSystem.getSortOrder().toString());

        try {
            JSONArray jsonArray = new JSONArray();
            for (UUID uuid : esSystem.getTrustedPlayers()) {
                String object = "{\"UUID\":\"" + uuid.toString() + "\"}";
                JSONObject uuidJSON = (JSONObject) new JSONParser().parse(object);

                jsonArray.add(uuidJSON);
            }

            getConfig().set(systemPath + "trustedUUIDs", jsonArray.toJSONString());
        } catch (ParseException e) {
            e.printStackTrace();
        }

        getConfig().set(systemPath + "drives", null);

        for (ESDrive drive : esSystem.getESDrives()) {
            if (drive == null) continue;
            getConfig().set(systemPath + "drives." + drive.getUUID() + ".size", drive.getSize());

            JSONArray itemsJson = new JSONArray();
            for (Map.Entry<ItemStack, Integer> entry : drive.getItems().entrySet()) {
                try {
                    String object = "{\"itemYAML\":\"" + ItemSerialization.serializeItem(entry.getKey(), entry.getValue()).replace("\"", "\\\"") + "\"}";
                    JSONObject itemJSON = (JSONObject) new JSONParser().parse(object);

                    itemsJson.add(itemJSON);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }

            getConfig().set(systemPath + "drives." + drive.getUUID() + ".items", itemsJson.toJSONString());
        }

        getConfig().saveConfig();
    }

    public static void removePlayerSystem(UUID player, UUID uuid) {
        getConfig().set("players." + player + ".systems." + uuid, null);

        // Check if the config for the player is now empty, and if it is, then just remove their UUID from the config.
        if (getConfig().getConfigurationSection("players." + player + ".systems").getKeys(false).size() == 0) {
            getConfig().set("players." + player, null);
        }

        getConfig().saveConfig();
    }

    public static void savePlayerSystems(List<ESSystem> esSystems) {
        assert esSystems != null;
        for (ESSystem esSystem : esSystems) {
            savePlayerSystem(esSystem);
        }
    }
}

	