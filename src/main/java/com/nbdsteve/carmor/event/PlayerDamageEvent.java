package com.nbdsteve.carmor.event;

import com.nbdsteve.carmor.Carmor;
import com.nbdsteve.carmor.file.LoadCarmorFiles;
import com.nbdsteve.carmor.method.GetSetNumber;
import com.nbdsteve.carmor.method.InventoryArmorCheck;
import com.nbdsteve.carmor.method.modifier.DamageModifiers;
import com.nbdsteve.carmor.method.specialattack.LightningAttack;
import com.nbdsteve.carmor.method.specialattack.PotionEffectAttack;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.plugin.Plugin;

/**
 * Class containing code for the pvp event
 */
public class PlayerDamageEvent implements Listener {
    //Register the main class
    private Plugin pl = Carmor.getPlugin(Carmor.class);
    //Get the files for the plugin
    private LoadCarmorFiles lcf = ((Carmor) pl).getFiles();

    /**
     * All code for the event is contained in this method
     *
     * @param e the event, cannot be null
     */
    @EventHandler
    public void playerDamageEvent(EntityDamageByEntityEvent e) {
        String setNumber;
        Player p;
        if (e.getDamager() instanceof Player) {
            p = (Player) e.getDamager();
            if (InventoryArmorCheck.checkArmor(p, lcf)) {
                setNumber = GetSetNumber.setNumber(p.getInventory().getHelmet().getItemMeta().getLore(), lcf);
                //Apply the additional damage modifier to the player
                DamageModifiers.applyDamageModifiers(lcf.getArmor().getStringList(setNumber + ".modifiers"), p, true, e);
            }
        }
        if (e.getEntity() instanceof Player) {
            p = (Player) e.getEntity();
            if (InventoryArmorCheck.checkArmor(p, lcf)) {
                setNumber = GetSetNumber.setNumber(p.getInventory().getHelmet().getItemMeta().getLore(), lcf);
                //Apply the reduced damage mod to the player
                DamageModifiers.applyDamageModifiers(lcf.getArmor().getStringList(setNumber + ".modifiers"), p, false, e);
                //Get the list of bonus attacks associated with the armor set
                for (String bonusAttack : lcf.getArmor().getStringList(setNumber + ".bonus-attacks")) {
                    //Generate a proc chance
                    double chance = Math.random() * 1;
                    //Split up the attack sequence
                    String[] attack = bonusAttack.split(":");
                    if (attack[0].equalsIgnoreCase("lightning")) {
                        if (chance < Double.parseDouble(attack[3])) {
                            new LightningAttack(p, Boolean.parseBoolean(attack[1]), Double.parseDouble(attack[2]), lcf);
                        }
                    } else if (attack[0].equalsIgnoreCase("potion")) {
                        if (chance < Double.parseDouble(attack[6])) {
                            new PotionEffectAttack(attack[1], Integer.parseInt(attack[2]), Integer.parseInt(attack[3]), p, Boolean.parseBoolean(attack[1]), Double.parseDouble(attack[2]), lcf);
                        }
                    }
                }
            }
        }
    }
}