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
import ru.unicorns.objects.StatStorage;
import ru.unicorns.objects.Stats;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class SwordCraftingDaemon implements Listener {

    public SwordCraftingDaemon()
    {
        Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.GREEN+"SwordCraftingDaemon..............✅");
    }

    private Enchantment[] SwordEnchantments = {
            Enchantment.BINDING_CURSE,
            Enchantment.CHANNELING,
            Enchantment.DAMAGE_ALL,
            Enchantment.DAMAGE_UNDEAD,
            Enchantment.DURABILITY,
            Enchantment.FIRE_ASPECT,
            Enchantment.IMPALING,
            Enchantment.LOOT_BONUS_MOBS,
            Enchantment.VANISHING_CURSE,
            Enchantment.KNOCKBACK
    };

    @EventHandler
    public void SwordCrafting(PrepareItemCraftEvent event)
    {
        if (event.getRecipe()==null) return;
        if (event.getRecipe().getResult().getType().name().contains("SWORD"))
        {
            Bukkit.getServer().getConsoleSender().sendMessage("Found sword crafting");
            ItemStack item = event.getRecipe().getResult();
            ItemMeta meta = item.getItemMeta();
            meta.displayName(Component.text(ChatColor.DARK_PURPLE+"???"));
            meta.lore(Collections.singletonList(Component.text("Вы скрафтите с некоторым шансом уникальное оружие")));
            item.setItemMeta(meta);
            event.getInventory().setResult(item);
        }
    }

    @EventHandler
    public void SwordOutput(CraftItemEvent event)
    {
        if (event.getCurrentItem().getType().name().contains("SWORD"))
        {
            if (!(event.getWhoClicked() instanceof Player)) return;
            Player player = (Player)event.getWhoClicked();
            Stats stats = StatStorage.getStatsByPlayer(player);

            ItemStack sword = event.getCurrentItem();


            int crafts = stats.WeaponCrafting; //количество крафтов
            int Level = crafts/20; //уровень навыка
            float Chances = (float)Level/10; //шансы, для 11 уровня шанс равен 1.1
            int Garants = (int) Chances/1; //гаранты на баф
            float AddonChance = (float)Chances%1; //остаточный шанс получения доп бафа
            Bukkit.getLogger().info("[UnicRPG] Sword info:"+"\nCrafts:"+crafts+"\nLevel: "+Level+"\nChances: "+Chances+"\nGarants: "+Garants + "\nNon Garants: "+AddonChance);
            Random rnd = new Random();
            int AdditonBaf=0; //доп бафы
            if (rnd.nextInt(0,10)<=AddonChance*10) AdditonBaf=1;
            int TotalBafs = Garants+AdditonBaf;
            Bukkit.getLogger().info("[UnicRPG] Total Bafs: "+TotalBafs);

            if (TotalBafs == 0)
            {
                event.setCurrentItem(new ItemStack(event.getCurrentItem().getType()));
            }

            for (int i=0; i<TotalBafs;i++)
            {
                Random gen = new Random();
                int step = gen.nextInt(0,3);
                switch (step)
                {
                    case 0:
                        Bukkit.getLogger().info("[UnicRPG] Assigned Enchant");
                        sword = GenerateEnchant(sword,Level);
                        break;

                    case 1:
                        Bukkit.getLogger().info("[UnicRPG] Assigned Durability");
                        sword = GenerateAdditionDurability(sword,Level);
                        break;

                    case 2:
                        Bukkit.getLogger().info("[UnicRPG] Assigned Damage");
                        sword = GenerateAdditionDamage(sword,Level);
                        break;
                }
            }
            ItemMeta meta = sword.getItemMeta();
            meta.displayName(Component.text(GenerateName()));

            if (Level<5)meta.lore(Collections.singletonList(Component.text(ChatColor.AQUA+"Редкое оружие")));
            else if (Level<15) meta.lore(Collections.singletonList(Component.text(ChatColor.LIGHT_PURPLE+"Эпическое оружие")));
            else if (Level<25) meta.lore(Collections.singletonList(Component.text(ChatColor.GOLD+"Легендарное оружие")));
            else meta.lore(Collections.singletonList(Component.text(ChatColor.RED+"Мифическое оружие")));

            sword.setItemMeta(meta);
            event.setCurrentItem(sword);
            stats.WeaponCrafting++;
            StatStorage.UpdateStats(stats);
        }
    }

    private ItemStack GenerateAdditionDamage(ItemStack sword, int level) {
        ItemMeta met = sword.getItemMeta();
        int addition = level/5;
        AttributeModifier modifier = new AttributeModifier(UUID.randomUUID(), "generic.attackSpeed", addition, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.HAND);
        met.addAttributeModifier(Attribute.GENERIC_ATTACK_DAMAGE,modifier);
        sword.setItemMeta(met);
        return sword;
    }

    private ItemStack GenerateAdditionDurability(ItemStack sword, int level) {
        Random rnd = new Random();
        int additionalDurability=1;
        if (level/5!=1) additionalDurability = rnd.nextInt(1,level/5);
        sword.addUnsafeEnchantment(Enchantment.DURABILITY,additionalDurability);
        return sword;
    }

    private ItemStack GenerateEnchant(ItemStack sword, int level) {
        Random rnd = new Random();
        int MaxEnchantLevel = 1;
        if ((int)level/5!=1) MaxEnchantLevel = rnd.nextInt(1,level/5);
        ItemMeta meta = sword.getItemMeta();

        Random rndech = new Random();
        Enchantment ench = SwordEnchantments[rndech.nextInt(0,SwordEnchantments.length)];
        int EnchantLevel = 1;
        if (MaxEnchantLevel!=1) EnchantLevel = rndech.nextInt(1,MaxEnchantLevel);
        Bukkit.getLogger().info("[UnicRPG] Enchant: "+ench.toString() +" "+ EnchantLevel);

        meta.addEnchant(ench,EnchantLevel,true);
        sword.setItemMeta(meta);
        return sword;
    }

    private String GenerateName() {
        Random rnd = new Random();
        String adjective = NameDictionary.adjective.get(rnd.nextInt(0, NameDictionary.adjective.size()));
        String noun = NameDictionary.SwordNouns.get(rnd.nextInt(0, NameDictionary.SwordNouns.size()));
        adjective = adjective.substring(0,1).toUpperCase()+adjective.substring(1);
        return adjective+" "+noun;
    }

    public void Dispose()
    {

    }
}
