package gg.steve.mc.ap.armor;

import gg.steve.mc.ap.armorequipevent.ArmorType;
import gg.steve.mc.ap.data.BasicSetData;
import gg.steve.mc.ap.data.HandItemData;
import gg.steve.mc.ap.data.SetData;
import gg.steve.mc.ap.data.types.*;
import gg.steve.mc.ap.message.MessageType;
import gg.steve.mc.ap.nbt.NBTItem;
import gg.steve.mc.ap.utils.CommandUtil;
import gg.steve.mc.ap.utils.GuiItemUtil;
import gg.steve.mc.ap.utils.SoundUtil;
import gg.steve.mc.ap.utils.YamlFileUtil;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Set {
    private String name;
    private YamlFileUtil fileUtil;
    private YamlConfiguration config;
    private List<SetData> data;
    private HandItemData handData;
    private Map<Piece, ItemStack> setPieces;
    private SetGui gui;

    public Set(String name, YamlFileUtil fileUtil) {
        this.name = name;
        this.fileUtil = fileUtil;
        this.config = this.fileUtil.get();
        this.setPieces = new HashMap<>();
        this.data = new ArrayList<>();
        ConfigurationSection dataTypes = this.config.getConfigurationSection("set-data");
        for (String entry : dataTypes.getKeys(false)) {
            switch (dataTypes.getString(entry + ".type")) {
                case "basic":
                    this.data.add(new BasicSetData(dataTypes, entry, this));
                    break;
                case "lightning":
                    this.data.add(new LightningSetData(dataTypes, entry));
                    break;
                case "shift":
                    this.data.add(new ShiftSetData(dataTypes, entry));
                    break;
                case "potion":
                    this.data.add(new PotionSetData(dataTypes, entry, this));
                    break;
                case "fall":
                    this.data.add(new FallSetData(dataTypes, entry));
                    break;
                case "hunger":
                    this.data.add(new HungerSetData(dataTypes, entry));
                    break;
                case "traveller":
                    this.data.add(new TravellerSetData(dataTypes, entry));
                    break;
                case "hand":
                    this.handData = new HandSetData(dataTypes, entry, this);
                    break;
                case "fairy":
                    this.data.add(new FairySetData(dataTypes, entry));
                    break;
                case "engineer":
                    this.data.add(new EngineerSetData(dataTypes, entry));
                    break;
                case "color-way":
                    this.data.add(new ColorWaySetData(dataTypes, entry, this));
                    break;
                case "experience":
                    this.data.add(new ExperienceSetData(dataTypes, entry, this));
                    break;
            }
        }
        loadPieces();
    }

    public void loadPieces() {
        ConfigurationSection section = this.config.getConfigurationSection("set-pieces");
        for (String entry : section.getKeys(false)) {
            Piece piece = Piece.valueOf(entry.toUpperCase());
            this.setPieces.put(piece, GuiItemUtil.createItem(section, entry, this));
        }
    }

    public void loadPiece(Piece piece) {
        this.setPieces.put(piece, GuiItemUtil.createItem(this.config.getConfigurationSection("set-pieces"), piece.name().toLowerCase(), this));
    }

    public boolean isWearingSet(Player player, ArmorType type, ItemStack changedItem) {
        if (changedItem != null && !verifyPiece(changedItem)) return false;
        for (Map.Entry item : this.setPieces.entrySet()) {
            if (item.getKey().toString().equalsIgnoreCase("HAND")) continue;
            NBTItem nbtItem = null;
            switch (item.getKey().toString()) {
                case "HELMET":
                    if (type != null) {
                        if (type.toString().equalsIgnoreCase("HELMET")) continue;
                    }
                    if (player.getInventory().getHelmet() == null || player.getInventory().getHelmet().getType().equals(Material.AIR))
                        return false;
                    nbtItem = new NBTItem(player.getInventory().getHelmet());
                    break;
                case "CHESTPLATE":
                    if (type != null) {
                        if (type.toString().equalsIgnoreCase("CHESTPLATE")) continue;
                    }
                    if (player.getInventory().getChestplate() == null || player.getInventory().getChestplate().getType().equals(Material.AIR))
                        return false;
                    nbtItem = new NBTItem(player.getInventory().getChestplate());
                    break;
                case "LEGGINGS":
                    if (type != null) {
                        if (type.toString().equalsIgnoreCase("LEGGINGS")) continue;
                    }
                    if (player.getInventory().getLeggings() == null || player.getInventory().getLeggings().getType().equals(Material.AIR))
                        return false;
                    nbtItem = new NBTItem(player.getInventory().getLeggings());
                    break;
                case "BOOTS":
                    if (type != null) {
                        if (type.toString().equalsIgnoreCase("BOOTS")) continue;
                    }
                    if (player.getInventory().getBoots() == null || player.getInventory().getBoots().getType().equals(Material.AIR))
                        return false;
                    nbtItem = new NBTItem(player.getInventory().getBoots());
                    break;
            }
            if (nbtItem.getString("armor+.set").equalsIgnoreCase("")) return false;
            if (!nbtItem.getString("armor+.set").equalsIgnoreCase(this.name)) return false;
        }
        return true;
    }

    public boolean verifyPiece(ItemStack oldItem) {
        if (oldItem == null || oldItem.getType().equals(Material.AIR)) return false;
        NBTItem nbtItem = new NBTItem(oldItem);
        if (nbtItem.getString("armor+.set").equalsIgnoreCase("")) return false;
        return nbtItem.getString("armor+.set").equalsIgnoreCase(this.name);
    }

    public void notifyPlayer(String method, Player player) {
        if (this.config.getBoolean(method + ".message.enabled")) {
            MessageType.doMessage(player, this.config.getStringList(method + ".message.text"));
        }
        SoundUtil.playSound(config, method, player);
        if (config.getBoolean(method + ".commands.enabled")) {
            CommandUtil.execute(this.config.getStringList(method + ".commands.list"), player);
        }
    }

    public void apply(Player player) {
        notifyPlayer("apply", player);
        for (SetData setData : this.data) {
            setData.onApply(player);
        }
    }

    public void remove(Player player) {
        notifyPlayer("remove", player);
        for (SetData setData : this.data) {
            setData.onRemoval(player);
        }
    }

    public void onHit(EntityDamageByEntityEvent event, Player damager) {
        for (SetData setData : this.data) {
            setData.onHit(event, damager);
        }
    }

    public void onDamage(EntityDamageByEntityEvent event) {
        for (SetData setData : this.data) {
            setData.onDamage(event);
        }
    }

    public void onFall(EntityDamageEvent event) {
        for (SetData setData : this.data) {
            setData.onFall(event);
        }
    }

    public void onHungerDeplete(FoodLevelChangeEvent event) {
        for (SetData setData : this.data) {
            setData.onHungerDeplete(event);
        }
    }

    public void onTargetDeath(EntityDeathEvent event, Player killer) {
        for (SetData setData : this.data) {
            setData.onTargetDeath(event, killer);
        }
    }

    public BasicSetData getBasicData() {
        return (BasicSetData) this.data.get(0);
    }

    public void openGui(Player player) {
        if (this.gui == null) {
            this.gui = new SetGui(config.getConfigurationSection("gui"), this);
        } else {
            this.gui.refresh();
        }
        this.gui.open(player);
    }

    public ItemStack getPiece(Piece piece) {
        loadPiece(piece);
        return this.setPieces.get(piece);
    }

    // <-- Getters and Setters from this point on -->
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public YamlFileUtil getFileUtil() {
        return fileUtil;
    }

    public void setFileUtil(YamlFileUtil fileUtil) {
        this.fileUtil = fileUtil;
    }

    public YamlConfiguration getConfig() {
        return config;
    }

    public void setConfig(YamlConfiguration config) {
        this.config = config;
    }

    public List<SetData> getData() {
        return data;
    }

    public void setData(List<SetData> data) {
        this.data = data;
    }

    public Map<Piece, ItemStack> getSetPieces() {
        return setPieces;
    }

    public void setSetPieces(Map<Piece, ItemStack> setPieces) {
        this.setPieces = setPieces;
    }

    public SetGui getGui() {
        return gui;
    }

    public void setGui(SetGui gui) {
        this.gui = gui;
    }

    public HandItemData getHandData() {
        return handData;
    }

    public void setHandData(HandItemData handData) {
        this.handData = handData;
    }
}