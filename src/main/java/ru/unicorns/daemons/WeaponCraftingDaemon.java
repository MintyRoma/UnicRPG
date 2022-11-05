package ru.unicorns.daemons;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import ru.unicorns.objects.ItemType;
import ru.unicorns.objects.StatStorage;
import ru.unicorns.objects.StatType;
import ru.unicorns.objects.Stats;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Random;
import java.util.UUID;

import static ru.unicorns.daemons.WeaponEnchants.SwordEnchantments;

public class WeaponCraftingDaemon implements Listener {

    public WeaponCraftingDaemon()
    {
        Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.GREEN+"WeaponCraftingDaemon\t✅");
    }

    @EventHandler
    public void WeaponCrafting(PrepareItemCraftEvent event)
    {
        if (event.getRecipe()==null) return;
        String type = event.getRecipe().getResult().getType().name();
        ItemType ResItem = DefineWeaponType(type);
        if (ResItem!=ItemType.nonWeapon)
        {
            ItemStack item = event.getRecipe().getResult();
            ItemMeta meta = item.getItemMeta();
            meta.displayName(Component.text(ChatColor.DARK_PURPLE+"???"));
            meta.lore(Collections.singletonList(Component.text("Вы скрафтите с некоторым шансом уникальное оружие")));
            item.setItemMeta(meta);
            event.getInventory().setResult(item);
        }
    }

    private ItemType DefineWeaponType(String type) {
        if (type == null) {
            return ItemType.nonWeapon;
        }
        if (type.contains("SWORD")) return  ItemType.sword;
        else if (type.contains("AXE")) return ItemType.axe;
        else if (type.contains("PICKAXE")) return ItemType.pickaxe;
        else if (type.contains("HOE")) return ItemType.hoe;
        else if (type.contains("BOW")) return ItemType.bow;
        else if (type.contains("SHOVEL")) return ItemType.shovel;
        else return ItemType.nonWeapon;
    }

    @EventHandler
    public void WeaponOutput(CraftItemEvent event)
    {
        String type = event.getRecipe().getResult().getType().name();
        ItemType ResItem = DefineWeaponType(type);
        if (!(event.getWhoClicked() instanceof Player)) return;

        Player player = ((Player)event.getWhoClicked());
        int Level = StatStorage.getLevel(StatType.WeaponCrafting,player);
        ItemStack result = event.getRecipe().getResult();
        int TotalBafs = CalculateBafs(Level);
        switch (ResItem)
        {
            case sword:
                ItemMeta bafs = GenerateSwordBufs(TotalBafs,Level,result.getItemMeta());
                if (bafs==null)
                {
                    result.setItemMeta(new ItemStack(result.getType()).getItemMeta());
                    break;
                }
                result.setItemMeta(bafs);
                result.setItemMeta(GenerateLegend(Level, result.getItemMeta()));
                result.setItemMeta(GenerateName(ItemType.sword,result.getItemMeta()));
                break;
            default:
                return;
        }
        Stats stats = StatStorage.getStatsByPlayer(player) ;
        stats.WeaponCrafting++;
        StatStorage.UpdateStats(stats);
        event.setCurrentItem(result);
    }

    private ItemMeta GenerateLegend(int Level,ItemMeta meta) {
        if (Level<5)meta.lore(Collections.singletonList(Component.text(ChatColor.GREEN+"Редкое оружие")));
        else if (Level<15) meta.lore(Collections.singletonList(Component.text(ChatColor.AQUA+"Эпическое оружие")));
        else if (Level<25) meta.lore(Collections.singletonList(Component.text(ChatColor.GOLD+"Легендарное оружие")));
        else meta.lore(Collections.singletonList(Component.text(ChatColor.LIGHT_PURPLE+"Мифическое оружие")));
        return meta;
    }

    private @Nullable ItemMeta GenerateSwordBufs(int TotalBafs,int Level, ItemMeta itemMeta) {
        if (TotalBafs == 0)
        {
            return null;
        }

        for (int i=0; i<TotalBafs;i++)
        {
            Random gen = new Random();
            int step = gen.nextInt(0,3);
            switch (step)
            {
                case 0:
                    Bukkit.getLogger().info("[UnicRPG] Assigned Enchant");
                    itemMeta = GenerateEnchant(itemMeta,Level);
                    break;

                case 1:
                    Bukkit.getLogger().info("[UnicRPG] Assigned Durability");
                    itemMeta = GenerateAdditionDurability(itemMeta,Level);
                    break;

                case 2:
                    Bukkit.getLogger().info("[UnicRPG] Assigned Damage");
                    itemMeta = GenerateAdditionDamage(itemMeta,Level);
                    break;
            }
        }
        return itemMeta;
    }

    private int CalculateBafs(int Level)
    {
        float Chances = (float)Level/10; //шансы, для 11 уровня шанс равен 1.1
        int Garants = (int) Chances; //гаранты на баф
        float AddonChance = Chances%1; //остаточный шанс получения доп бафа
        //Bukkit.getLogger().info("[UnicRPG] Sword info:"+"\nCrafts:"+crafts+"\nLevel: "+Level+"\nChances: "+Chances+"\nGarants: "+Garants + "\nNon Garants: "+AddonChance);
        Random rnd = new Random();
        int AdditonBaf=0; //доп бафы
        if (rnd.nextInt(0,10)<=AddonChance*10) AdditonBaf=1;
        return Garants+AdditonBaf;
    }

    private ItemMeta GenerateAdditionDamage(ItemMeta meta, int level) {
        int addition = level/5;
        AttributeModifier modifier = new AttributeModifier(UUID.randomUUID(), "generic.attackSpeed", addition, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.HAND);
        meta.addAttributeModifier(Attribute.GENERIC_ATTACK_DAMAGE,modifier);
        return meta;
    }

    private ItemMeta GenerateAdditionDurability(ItemMeta meta, int level) {
        Random rnd = new Random();
        int additionalDurability=1;
        if (level>20)additionalDurability= rnd.nextInt(1,level/10);
        meta.addEnchant(Enchantment.DURABILITY,additionalDurability,true);
        return meta;
    }


    private ItemMeta GenerateEnchant(ItemMeta meta, int level) {
        Random rnd = new Random();
        int MaxEnchantLevel = 1;
        if (level>20)MaxEnchantLevel= rnd.nextInt(1,level/10);
        Random rndech = new Random();
        Enchantment ench = SwordEnchantments[rndech.nextInt(0,SwordEnchantments.length)];
        int EnchantLevel = 1;
        if (MaxEnchantLevel!=1) EnchantLevel = rndech.nextInt(1,MaxEnchantLevel);
        Bukkit.getLogger().info("[UnicRPG] Enchant: "+ench.toString() +" "+ EnchantLevel);

        meta.addEnchant(ench,EnchantLevel,true);
        return meta;
    }

    private ItemMeta GenerateName(ItemType type, ItemMeta meta) {
        Random rnd = new Random();
        String adjective = NameDictionary.adjective.get(rnd.nextInt(0, NameDictionary.adjective.size()));
        String noun="";
        switch (type)
        {
            case sword:
                noun=NameDictionary.SwordNouns.get(rnd.nextInt(0, NameDictionary.SwordNouns.size()));
                break;
        }
        adjective = adjective.substring(0,1).toUpperCase()+adjective.substring(1);
        meta.displayName(Component.text(adjective+" "+noun));
        return meta;
    }
}
