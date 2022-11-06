package ru.unicorns.objects;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.TitlePart;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;


/**
 * Хранилище статов
 */
public class StatStorage {

    //Global config
    /**
     * Затраты для получения уровня
     */
    public static int WeaponCraftsForLevel = 1000;
    public static int ArmorCraftsForLevel = 1000;
    public static int KillsForLevel = 1000;
    public static int RunForLevel = 1000;
    public static int FoodCraftsForLevel = 1000;
    public static int PotionCraftsForLevel = 1000;


    /**
     * Лист статов игроков
     */
    public static List<Stats> PlayersStats = new ArrayList<>();

    /**
     * Получение класса статов игроково
     * @param player класс Player игрока
     * @return Stats статы игрока
     */
    public static Stats getStatsByPlayer(Player player)
    {
        for(Stats st: PlayersStats)
        {
            if (st.Nickname.equals(player.getName())) return st;
        }
        return new Stats();
    }

    /**
     * Получение уровня навыка
     * @param stat тип навыка по StatType
     * @param player класс Player игрока
     * @return int текущий уровень
     */
    public static int getLevel(StatType stat, Player player)
    {
        Stats st = getStatsByPlayer(player);
        switch (stat)
        {
            case WeaponCrafting:
                return st.WeaponCrafting/WeaponCraftsForLevel;
            case ArmorCrafting:
                return st.ArmorCrafting/ArmorCraftsForLevel;
            case PotionCrafting:
                return st.PotionCrafting/PotionCraftsForLevel;
            case FoodCrafting:
                return st.FoodCrafting/FoodCraftsForLevel;
            case Kills:
                return st.Kills/KillsForLevel;
            case Runer:
                return st.Runer/RunForLevel;
        }
        return 0;
    }

    /**
     * Обновляет статы игрока в базе
     * @param updated Stats игрока
     */
    public static void UpdateStats(Stats updated)
    {
        Stats old = null;
        for (Stats st: PlayersStats)
        {
            if (st.Nickname.equals(updated.Nickname))
            {
                old = st;
                break;
            }
        }
        if (old!=null)
        {
            PlayersStats.remove(old);
            PlayersStats.add(updated);
        }
        CheckForNewLevel(updated);
    }

    /**
     * Проверяет на изменение статов игрока для отображения анимации
     * @param updated Stats игрока
     */
    private static void CheckForNewLevel(Stats updated) {
        int WeponLevel = updated.WeaponCrafting;

        Player receiver = Bukkit.getServer().getPlayer(updated.Nickname);
        if (WeponLevel%WeaponCraftsForLevel==0)
        {
            receiver.sendTitlePart(TitlePart.TITLE, Component.text(ChatColor.GOLD+"LEVEL UP!"));
            receiver.sendTitlePart(TitlePart.SUBTITLE, Component.text("Ваш уровень крафта оружия достиг "+ WeponLevel /WeaponCraftsForLevel+" !"));
            receiver.playSound(receiver, Sound.ITEM_TOTEM_USE,1f, 1f);
        }
    }

}
