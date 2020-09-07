package net.seanomik.energeticstorage.utils;

import net.seanomik.energeticstorage.files.ConfigFile;
import net.seanomik.energeticstorage.gui.ESDriveGUI;
import net.seanomik.energeticstorage.gui.ESSystemSecurityGUI;
import net.seanomik.energeticstorage.gui.ESTerminalGUI;
import net.seanomik.energeticstorage.objects.ESSystem;
import org.bukkit.ChatColor;

import java.util.*;

public class Reference {

    public static String PREFIX = ChatColor.AQUA + "" + ChatColor.ITALIC + "[Energetic Storage] " + ChatColor.RESET;

    public static ESTerminalGUI ES_TERMINAL_GUI = new ESTerminalGUI();
    public static ESDriveGUI ES_DRIVE_GUI = new ESDriveGUI();
    public static ESSystemSecurityGUI ES_SYSTEM_SECURITY_GUI = new ESSystemSecurityGUI();

    public static Map<UUID, List<ESSystem>> ES_SYSTEMS = new HashMap<>();

    public static int MAX_DRIVE_TYPES = ConfigFile.getMaxTypes();
}
