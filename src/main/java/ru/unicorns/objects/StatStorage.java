package ru.unicorns.objects;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.TitlePart;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class StatStorage {

    public static List<Stats> PlayersStats = new ArrayList<>();

    public static Stats getStatsByPlayer(Player player)
    {
        for(Stats st: PlayersStats)
        {
            if (st.Nickname.equals(player.getName())) return st;
        }
        return new Stats();
    }

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

    private static void CheckForNewLevel(Stats updated) {
        int WeponLevel = updated.WeaponCrafting;

        Player receiver = Bukkit.getServer().getPlayer(updated.Nickname);
        if (WeponLevel%20==0)
        {
            receiver.sendTitlePart(TitlePart.TITLE, Component.text(ChatColor.GOLD+"LEVEL UP!"));
            receiver.sendTitlePart(TitlePart.SUBTITLE, Component.text("Ваш уровень крафта оружия достиг "+(int)WeponLevel/20+" !"));
            receiver.playSound(receiver, Sound.ITEM_TOTEM_USE,1f, 1f);
        }
    }
}
