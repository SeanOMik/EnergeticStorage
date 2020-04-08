package net.seanomik.energeticstorage.objects;

import net.seanomik.energeticstorage.utils.Utils;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class ESSystem implements Cloneable {
    private UUID owner;
    private UUID uuid;
    private Location location;
    private List<ESDrive> esDrives = new ArrayList<>();
    private List<UUID> trustedPlayers = new ArrayList<>();
    private boolean isPublic;

    public ESSystem(UUID owner, UUID uuid, Location location) {
        this.owner = owner;
        this.uuid = uuid;
        this.location = location;
    }

    public ESSystem(UUID owner, UUID uuid, Location location, List<ESDrive> esDrives, List<UUID> trustedPlayers, boolean isPublic) {
        this(owner, uuid, location);

        this.esDrives = esDrives;
        this.trustedPlayers = trustedPlayers;
        this.isPublic = isPublic;
    }

    public void setEsDrives(List<ESDrive> esDrives) {
        this.esDrives = esDrives;
    }

    public List<ESDrive> getEsDrives() {
        return esDrives;
    }

    public void setTrustedPlayers(List<UUID> trustedPlayers) {
        this.trustedPlayers = trustedPlayers;
    }

    public List<UUID> getTrustedPlayers() {
        return trustedPlayers;
    }

    public boolean isPlayerTrusted(UUID uuid) {
        return trustedPlayers.contains(uuid) || isPublic;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public void setPublic(boolean aPublic) {
        isPublic = aPublic;
    }

    public boolean isPlayerTrusted(Player player) {
        return trustedPlayers.contains(player.getUniqueId());
    }

    public void addTrustedPlayer(UUID uuid) {
        trustedPlayers.add(uuid);
    }

    public void addTrustedPlayer(Player player) {
        trustedPlayers.add(player.getUniqueId());
    }

    public void removeTrustedPlayer(UUID uuid) {
        trustedPlayers.remove(uuid);
    }

    public void removeTrustedPlayer(Player player) {
        trustedPlayers.remove(player.getUniqueId());
    }

    public UUID getUUID() {
        return uuid;
    }

    public UUID getOwner() {
        return owner;
    }

    public Location getLocation() {
        return location;
    }

    public List<ESDrive> getESDrives() {
        return esDrives;
    }

    public void setUUID(UUID id) {
        this.uuid = uuid;
    }

    public void setOwner(UUID owner) {
        this.owner = owner;
    }

    public void setESDrives(List<ESDrive> esDrives) {
        this.esDrives = esDrives;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public ESSystem clone() {
        try {
            ESSystem system = (ESSystem) super.clone();
            if (this.esDrives != null) {
                esDrives = new ArrayList<>(esDrives);
            }

            return system;
        } catch (CloneNotSupportedException var2) {
            throw new Error(var2);
        }
    }

    public boolean equals(Object other) {
        assert other != null;
        if (other instanceof ESSystem) {
            ESSystem otherSystem = (ESSystem) other;
            return otherSystem.getUUID() == uuid;
        }

        return false;
    }

    public ESDrive getNextAvailableDrive() {
        for (ESDrive drive : esDrives) {
            if (drive.isAvailable(null)) {
                return drive;
            }
        }

        return null;
    }

    public ESDrive findItemInAvailableDrive(ItemStack item) {
        for (ESDrive drive : esDrives) {
            for (ItemStack itemStack : drive.getItems().keySet()) {
                if (item.isSimilar(itemStack) && drive.isAvailable(item)) {
                    return drive;
                }
            }
        }

        return null;
    }

    public Map<ItemStack, Integer> getAllItems() {
        Map<ItemStack, Integer> items = new HashMap<>();

        for (ESDrive drive : esDrives) {
            for (Map.Entry<ItemStack, Integer> entry : drive.getItems().entrySet()) {
                if (items.containsKey(entry.getKey())) {
                    // Set the ItemStack amount to the already existing item's amount + this amount
                    entry.getKey().setAmount(Math.min(entry.getValue(), 64));
                    items.remove(entry.getKey());
                }

                items.put(entry.getKey(), entry.getValue());
            }
        }

        return items;
    }

public boolean addItem(ItemStack item) {
    ESDrive drive = findItemInAvailableDrive(item);

    // If we failed to find the item in the next available drive, then find another drive.
    if (drive == null) {
        drive = getNextAvailableDrive();

        if (drive == null) {
            return false;
        }
    }

    boolean addReturn = drive.addItem(item);

    return addReturn;
}

    public ItemStack removeItem(ItemStack item) {
        // Find a drive that has this item to remove from.
        ESDrive drive = null;
        for (ESDrive esDrive : esDrives) {
            for (ItemStack itemStack : esDrive.getItems().keySet()) {
                if (Utils.removeAmountFromLore(item).isSimilar(Utils.removeAmountFromLore(itemStack))) {
                    drive = esDrive;
                }
            }
        }

        // If we failed to find the item in the next available drive, then find another drive.
        if (drive == null) {
            return null;
        }

        return drive.removeItem(item);
    }
}
