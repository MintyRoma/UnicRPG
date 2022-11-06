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
import ru.unicorns.objects.*;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Random;
import java.util.UUID;

import static ru.unicorns.daemons.WeaponEnchants.*;
import static ru.unicorns.objects.NameDictionary.*;

public class WeaponCraftingDaemon implements Listener {

    /**
     * Конструктор демона уведомляющий о запуске
     */
    public WeaponCraftingDaemon()
    {
        Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.GREEN+"WeaponCraftingDaemon\t✅");
    }

    /**
     * Демон подменяющий результат крафта для отображения того, что игрок может получить уникальный предмет с некоторым шансом
     * @param event событие PrepareItemCraftEvent
     */
    @EventHandler
    public void WeaponCrafting(PrepareItemCraftEvent event)
    {
        if (event.getRecipe()==null) return;
        String type = event.getRecipe().getResult().getType().name();
        WeaponType ResItem = DefineWeaponType(type);
        if (ResItem!= WeaponType.nonWeapon)
        {
            ItemStack item = event.getRecipe().getResult();
            ItemMeta meta = item.getItemMeta();
            meta.displayName(Component.text(ChatColor.DARK_PURPLE+"???"));
            meta.lore(Collections.singletonList(Component.text("Вы скрафтите с некоторым шансом уникальное оружие")));
            item.setItemMeta(meta);
            event.getInventory().setResult(item);
        }
    }



    /**
     * Демон подбора предмета в результате крафта. Модифицирует предмет.
     * @param event Событие CraftItemEvent
     */
    @EventHandler
    public void WeaponOutput(CraftItemEvent event)
    {
        String type = event.getRecipe().getResult().getType().name();
        WeaponType ResItem = DefineWeaponType(type);
        if (ResItem== WeaponType.nonWeapon) return;
        if (!(event.getWhoClicked() instanceof Player)) return;

        Player player = ((Player)event.getWhoClicked());
        int Level = StatStorage.getLevel(StatType.WeaponCrafting,player);
        ItemStack result = event.getRecipe().getResult();
        int TotalBafs = CalculateBuffs(Level);
        ItemMeta i_meta = result.getItemMeta();


        i_meta = GenerateBuffs(TotalBafs,Level,i_meta,ResItem);
        if (i_meta==null)
        {
            i_meta = new ItemStack(result.getType()).getItemMeta();

        }
        else
        {
            i_meta = GenerateLegend(Level,i_meta);
            i_meta = GenerateName(ResItem,i_meta);
        }

        result.setItemMeta(i_meta);
        Stats stats = StatStorage.getStatsByPlayer(player) ;
        stats.WeaponCrafting++;
        StatStorage.UpdateStats(stats);
        event.setCurrentItem(result);
    }

    /**
     *
     * @param Level уровень навыка
     * @param meta мета информация предмета
     * @return ItemMeta с откредактированным лором
     */
    private ItemMeta GenerateLegend(int Level,ItemMeta meta) {
        if (Level<5)meta.lore(Collections.singletonList(Component.text(ChatColor.GREEN+"Редкое оружие")));
        else if (Level<15) meta.lore(Collections.singletonList(Component.text(ChatColor.AQUA+"Эпическое оружие")));
        else if (Level<25) meta.lore(Collections.singletonList(Component.text(ChatColor.GOLD+"Легендарное оружие")));
        else meta.lore(Collections.singletonList(Component.text(ChatColor.LIGHT_PURPLE+"Мифическое оружие")));
        return meta;
    }

    /**
     * Накладывает повышенный урон, повышенную прочность и эффекты из общего количества бафов.
     * Может с шансом 25% не наложить 1 баф.
     * В случае если количество бафов равно 0, то вернет null
     * @param TotalBuffs количество бафов
     * @param Level уровень навыка
     * @param itemMeta мета информация предмета
     * @param type тип предмета из WeaponType
     * @return ItemMeta если установлены бафы;
     * null если бафы не установлены
     */
    private @Nullable ItemMeta GenerateBuffs(int TotalBuffs, int Level, ItemMeta itemMeta, WeaponType type) {
        if (TotalBuffs == 0)
        {
            return null;
        }

        for (int i=0; i<TotalBuffs;i++)
        {
            Random gen = new Random();
            int step = gen.nextInt(0,3);
            switch (step)
            {
                case 0:
                    Bukkit.getLogger().info("[UnicRPG] Assigned Enchant");
                    itemMeta = GenerateEnchant(itemMeta,Level,type);
                    break;

                case 1:
                    Bukkit.getLogger().info("[UnicRPG] Assigned Durability");
                    itemMeta = GenerateAdditionDurability(itemMeta,Level);
                    break;

                case 2:
                    Bukkit.getLogger().info("[UnicRPG] Assigned Damage");
                    itemMeta = GenerateAdditionAttribute(itemMeta,Level);
                    break;
                default:
                    break;
            }
        }
        return itemMeta;
    }

    /**
     * Рассчитывает количество бафов опираясь на текущий уровень навыка. Шанс получить бафы составляет Level/10
     * @param Level уровень навыка
     * @return int - количество бафов
     */
    private int CalculateBuffs(int Level)
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

    /**
     * Добавляет случайный повышенный урон в зависимости от уровня
     * @param meta мета информация предмета
     * @param level уровень навыка
     * @return ItemMeta с добавленными атрибутами к урону
     */
    private ItemMeta GenerateAdditionAttribute(ItemMeta meta, int level) {
        Random rnd = new Random();
        float addition = (float)level/10;
        if (level>1)
        {
            addition = (float)rnd.nextInt(1,level)/10;
        }
        int atr = rnd.nextInt(0,3);
        switch (atr)
        {
            case 0:
                AttributeModifier modifier = new AttributeModifier(UUID.randomUUID(), "generic.attackDamage", addition, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.HAND);
                meta.addAttributeModifier(Attribute.GENERIC_ATTACK_DAMAGE,modifier);
                break;
            case 1:
                AttributeModifier mod = new AttributeModifier(UUID.randomUUID(), "generic.attackSpeed", addition, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.HAND);
                meta.addAttributeModifier(Attribute.GENERIC_ATTACK_SPEED,mod);
                break;
            case 2:
                AttributeModifier mod1 = new AttributeModifier(UUID.randomUUID(), "generic.attackKnockback", addition, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.HAND);
                meta.addAttributeModifier(Attribute.GENERIC_ATTACK_KNOCKBACK,mod1);
                break;
        }
        return meta;
    }

    /**
     * Добавляет случайное зачарование прочности в зависимости от уровня
     * @param meta мета информация предмета
     * @param level уровень навыка
     * @return ItemMeta с добавленным зачарованием
     */
    private ItemMeta GenerateAdditionDurability(ItemMeta meta, int level) {
        Random rnd = new Random();
        int additionalDurability=1;
        if (level>20)additionalDurability= rnd.nextInt(1,level/10);
        meta.addEnchant(Enchantment.DURABILITY,additionalDurability,true);
        return meta;
    }

    /**
     * Генерирует зачарования от зависимости от типа предмета и уровня навыка.
     * @param meta мета информация предмета
     * @param level уровень навыка
     * @param type тип предмета по WeaponType
     * @return ItemMeta с добавленными зачарованиями
     */
    private ItemMeta GenerateEnchant(ItemMeta meta, int level, WeaponType type) {
        Random rnd = new Random();
        int MaxEnchantLevel = 1;
        if (level>20)MaxEnchantLevel= rnd.nextInt(1,level/10);
        Random rndech = new Random();

        Enchantment ench;
        switch (type)
        {
            case sword:
                ench = SwordEnchantments[rndech.nextInt(0,SwordEnchantments.length)];
                break;
            case pickaxe:
                ench = PickaxeEnchantments[rndech.nextInt(0,PickaxeEnchantments.length)];
                break;
            case axe:
                ench = AxeEnchantments[rndech.nextInt(0,AxeEnchantments.length)];
                break;
            case hoe:
                ench = HoeEnchantments[rndech.nextInt(0,HoeEnchantments.length)];
                break;
            case shovel:
                ench = ShovelEnchantments[rndech.nextInt(0,ShovelEnchantments.length)];
                break;
            default:
                return meta;
        }
        int EnchantLevel = 1;
        if (MaxEnchantLevel!=1) EnchantLevel = rndech.nextInt(1,MaxEnchantLevel);
        Bukkit.getLogger().info("[UnicRPG] Enchant: "+ench.toString() +" "+ EnchantLevel);

        meta.addEnchant(ench,EnchantLevel,true);
        return meta;
    }

    /**
     * Генерирует классное имя по словарю прилагательных+существительных из NameDictionary
     * @param type Тип предмета по WeaponType
     * @param meta мета информация предмета
     * @return ItemMeta с новым именем предмета
     */
    private ItemMeta GenerateName(WeaponType type, ItemMeta meta) {
        String adjective = "";
        switch (type)
        {
            case sword:
            case axe:
                adjective = GetAdjective(false);
                break;
            case pickaxe:
            case hoe:
            case shovel:
                adjective = GetAdjective(true);
                break;
        }
        meta.displayName(Component.text(adjective+" "+GetWeaponNoun(type)));
        return meta;
    }

    /**
     * Определяет тип предмета по WeaponType на основе названия предмета
     * @param type тип предмета из ItemStack
     * @return тип по WeaponType
     */
    private WeaponType DefineWeaponType(String type) {
        if (type == null) {
            return WeaponType.nonWeapon;
        }
        if (type.contains("SWORD")) return  WeaponType.sword;
        else if (type.contains("AXE")) return WeaponType.axe;
        else if (type.contains("PICKAXE")) return WeaponType.pickaxe;
        else if (type.contains("HOE")) return WeaponType.hoe;
        else if (type.contains("BOW")) return WeaponType.bow;
        else if (type.contains("SHOVEL")) return WeaponType.shovel;
        else return WeaponType.nonWeapon;
    }

}
