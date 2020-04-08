package net.seanomik.energeticstorage.utils;

import net.seanomik.energeticstorage.EnergeticStorage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;

public class ItemRecipies {
    public static void registerRecipes() {
        registerBlockRecipes();
        registerDriveRecipes();
    }

    private static  void registerBlockRecipes() {
        try {
            // Register system block
            ItemStack esSystem = ItemConstructor.createSystemBlock();
            ShapedRecipe systemRecipe = new ShapedRecipe(new NamespacedKey(EnergeticStorage.getPlugin(), "es_system"), esSystem);
            systemRecipe.shape(
                    "III",
                    "RGR",
                    "DID");
            systemRecipe.setIngredient('I', Material.IRON_INGOT);
            systemRecipe.setIngredient('G', Material.GLOWSTONE_DUST);
            systemRecipe.setIngredient('R', Material.REDSTONE);
            systemRecipe.setIngredient('D', Material.DIAMOND);
            Bukkit.getServer().addRecipe(systemRecipe);
        } catch (Exception e) {

        }
    }

    private static void registerDriveRecipes() {
        try { // If the plugin was reloaded, a exception will be thrown.
            // Register Drive 1k
            ItemStack drive1k = ItemConstructor.createDrive(1024, 0, 0);

            ShapedRecipe drv1k = new ShapedRecipe(new NamespacedKey(EnergeticStorage.getPlugin(), "es_drive_1k"), drive1k);
            drv1k.shape(
                    "RCR",
                    "CRC",
                    "III");
            drv1k.setIngredient('I', Material.IRON_INGOT);
            drv1k.setIngredient('C', Material.CLAY);
            drv1k.setIngredient('R', Material.REDSTONE);
            Bukkit.getServer().addRecipe(drv1k);

            // Register Drive 4k
            ItemStack drive4k = ItemConstructor.createDrive(4096, 0, 0);

            ShapedRecipe drv4k = new ShapedRecipe(new NamespacedKey(EnergeticStorage.getPlugin(), "es_drive_4k"), drive4k);
            drv4k.shape(
                    "RBR",
                    "BRB",
                    "III");
            drv4k.setIngredient('I', Material.IRON_INGOT);
            drv4k.setIngredient('B', Material.BRICK);
            drv4k.setIngredient('R', Material.REDSTONE);
            Bukkit.getServer().addRecipe(drv4k);

            // Register Drive 16k
            ItemStack drive16k = ItemConstructor.createDrive(16384, 0, 0);

            ShapedRecipe drv16k = new ShapedRecipe(new NamespacedKey(EnergeticStorage.getPlugin(), "es_drive_16k"), drive16k);
            drv16k.shape(
                    "RGR",
                    "GRG",
                    "III");
            drv16k.setIngredient('I', Material.IRON_INGOT);
            drv16k.setIngredient('G', Material.GOLD_INGOT);
            drv16k.setIngredient('R', Material.REDSTONE);
            Bukkit.getServer().addRecipe(drv16k);

            // Register Drive 64k
            ItemStack drive64k = ItemConstructor.createDrive(65536, 0, 0);

            ShapedRecipe drv64k = new ShapedRecipe(new NamespacedKey(EnergeticStorage.getPlugin(), "es_drive_64k"), drive64k);
            drv64k.shape(
                    "RDR",
                    "DRD",
                    "III");
            drv64k.setIngredient('I', Material.IRON_INGOT);
            drv64k.setIngredient('D', Material.DIAMOND);
            drv64k.setIngredient('R', Material.REDSTONE);
            Bukkit.getServer().addRecipe(drv64k);
        } catch (Exception e) {

        }
    }
}
