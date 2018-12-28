package com.nbdsteve.carmor.gui;

import com.nbdsteve.carmor.file.LoadCarmorFiles;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * Class to generate an armor set for the specified gui
 */
class GenerateArmorSet {

    /**
     * Constructor to create the armor set
     *
     * @param setNumber the armor set to create
     * @param lcf       LoadCarmorFiles instance
     * @param setGui    The gui to add the armor pieces to
     */
    GenerateArmorSet(String setNumber, LoadCarmorFiles lcf, Inventory setGui) {
        //Run this loop for each piece of armor
        for (int i = 0; i <= 4; i++) {
            String armorPiece = null;
            if (i == 0) {
                armorPiece = setNumber + ".helmet";
            } else if (i == 1) {
                armorPiece = setNumber + ".chestplate";
            } else if (i == 2) {
                armorPiece = setNumber + ".leggings";
            } else if (i == 3) {
                armorPiece = setNumber + ".boots";
            } else {
                return;
            }
            //Store the information about the armor set
            String[] armorPieceParts = lcf.getArmorGui().getString(armorPiece).split(":");
            //Create the item
            ItemStack piece = new ItemStack(Material.valueOf(armorPieceParts[0].toUpperCase()));
            //Store the item meta
            ItemMeta pieceMeta = piece.getItemMeta();
            //Create a new array list to create the lore
            List<String> pieceLore = new ArrayList<>();
            //Set the display name of the item
            pieceMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&',
                    lcf.getArmor().getString(armorPiece + ".name")));
            //Add the regular lore
            for (String lore : lcf.getArmor().getStringList(armorPiece + ".lore")) {
                pieceLore.add(ChatColor.translateAlternateColorCodes('&', lore));
            }
            //Add the special gui lore
            for (String lore : lcf.getArmorGui().getStringList(setNumber + ".gui-lore")) {
                pieceLore.add(ChatColor.translateAlternateColorCodes('&', lore).replace("%Price",
                        armorPieceParts[2]));
            }
            //Add the regular enchantments
            for (String ench : lcf.getArmor().getStringList(armorPiece + ".enchantments")) {
                String[] parts = ench.split(":");
                pieceMeta.addEnchant(Enchantment.getByName(parts[0].toUpperCase()),
                        Integer.parseInt(parts[1]),
                        true);
            }
            //Set the new lore
            pieceMeta.setLore(pieceLore);
            //Set the new item meta
            piece.setItemMeta(pieceMeta);
            //Add it to the gui
            setGui.setItem(Integer.parseInt(armorPieceParts[1]), piece);
        }
    }
}