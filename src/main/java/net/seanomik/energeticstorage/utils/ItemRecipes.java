package net.seanomik.energeticstorage.utils;

import net.seanomik.energeticstorage.EnergeticStorage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;

public class ItemRecipes {
    public static void registerRecipes() {
        registerBlockRecipes();
        registerDriveRecipes();
    }

    private static  void registerBlockRecipes() {
        try {
            // Register system block
            ItemStack esSystem = ItemConstructor.createSystemBlock();
            ShapedRecipe systemRecipe = new ShapedRecipe(new NamespacedKey(EnergeticStorage.getPlugin(), "es_system"), esSystem)
                    .shape("III",
                           "RGR",
                           "DID")
                    .setIngredient('I', Material.IRON_INGOT)
                    .setIngredient('G', Material.GLOWSTONE_DUST)
                    .setIngredient('R', Material.REDSTONE)
                    .setIngredient('D', Material.DIAMOND);

            Bukkit.getServer().addRecipe(systemRecipe);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void registerDriveRecipes() {
        try { // If the plugin was reloaded, a exception will be thrown.
            // Register Drive 1k
            ItemStack drive1k = ItemConstructor.createDrive(1024, 0, 0);

            ShapedRecipe drv1k = new ShapedRecipe(new NamespacedKey(EnergeticStorage.getPlugin(), "es_drive_1k"), drive1k)
                    .shape("RCR",
                           "CRC",
                           "III")
                    .setIngredient('I', Material.IRON_INGOT)
                    .setIngredient('C', Material.CLAY)
                    .setIngredient('R', Material.REDSTONE);
            Bukkit.getServer().addRecipe(drv1k);

            // Register Drive 4k
            ItemStack drive4k = ItemConstructor.createDrive(4096, 0, 0);

            ShapedRecipe drv4k = new ShapedRecipe(new NamespacedKey(EnergeticStorage.getPlugin(), "es_drive_4k"), drive4k)
                    .shape("RBR",
                           "BRB",
                           "III")
                    .setIngredient('I', Material.IRON_INGOT)
                    .setIngredient('B', Material.BRICK)
                    .setIngredient('R', Material.REDSTONE);
            Bukkit.getServer().addRecipe(drv4k);

            // Register Drive 16k
            ItemStack drive16k = ItemConstructor.createDrive(16384, 0, 0);

            ShapedRecipe drv16k = new ShapedRecipe(new NamespacedKey(EnergeticStorage.getPlugin(), "es_drive_16k"), drive16k)
                    .shape("RGR",
                           "GRG",
                           "III")
                    .setIngredient('I', Material.IRON_INGOT)
                    .setIngredient('G', Material.GOLD_INGOT)
                    .setIngredient('R', Material.REDSTONE);
            Bukkit.getServer().addRecipe(drv16k);

            // Register Drive 64k
            ItemStack drive64k = ItemConstructor.createDrive(65536, 0, 0);

            ShapedRecipe drv64k = new ShapedRecipe(new NamespacedKey(EnergeticStorage.getPlugin(), "es_drive_64k"), drive64k)
                    .shape("RDR",
                           "DRD",
                           "III")
                    .setIngredient('I', Material.IRON_INGOT)
                    .setIngredient('D', Material.DIAMOND)
                    .setIngredient('R', Material.REDSTONE);
            Bukkit.getServer().addRecipe(drv64k);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
