package net.seanomik.energeticstorage.utils;

import org.bukkit.command.CommandSender;

public class PermissionChecks {
    public static boolean canDestroyUntrustedSystems(CommandSender sender) {
        return sender.hasPermission("energeticstorage.system.destroy.untrusted");
    }

    public static boolean canOpenUntrustedSystem(CommandSender sender) {
        return sender.hasPermission("energeticstorage.system.open.untrusted");
    }

    public static boolean canESGive(CommandSender sender) {
        return sender.hasPermission("energeticstorage.esgive");
    }

    public static boolean canESGiveOthers(CommandSender sender) {
        return sender.hasPermission("energeticstorage.esgive.others");
    }

    public static boolean canCreateSystem(CommandSender sender) {
        return sender.hasPermission("energeticstorage.system.create");
    }
}
