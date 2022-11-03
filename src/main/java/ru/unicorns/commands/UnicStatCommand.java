package ru.unicorns.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.unicorns.objects.StatStorage;
import ru.unicorns.objects.Stats;

public class UnicStatCommand implements CommandExecutor {

    public UnicStatCommand()
    {
        Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.GREEN+"/unicstat.................✅ ");
    }


    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args)
    {
        int count=0;
        if (!StatStorage.PlayersStats.isEmpty()) count = StatStorage.PlayersStats.size();
        if (sender instanceof Player)
        {
            Player pl = (Player) sender;
            pl.sendMessage("Amount of Storage: "+count);
            if (count>0)
            {
                for (Stats st: StatStorage.PlayersStats)
                {
                    pl.sendMessage(ChatColor.AQUA+"Stats of "+st.Nickname);
                    pl.sendMessage("Weapon Crafting: "+st.WeaponCrafting);
                    pl.sendMessage("Armor Crafting: "+st.ArmorCrafting);
                    pl.sendMessage("Food Crafting: "+st.FoodCrafting);
                    pl.sendMessage("Potion Crafting: "+st.PotionCrafting);
                    pl.sendMessage("Kills: "+st.Kills);
                    pl.sendMessage("Runned: "+st.Runer);
                }
            }
        }
        else
        {
            ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();
            console.sendMessage("Amount of Storage: "+count);
            for (Stats st: StatStorage.PlayersStats)
            {
                console.sendMessage(ChatColor.AQUA+"Stats of "+st.Nickname);
                console.sendMessage("Weapon Crafting: "+st.WeaponCrafting);
                console.sendMessage("Armor Crafting: "+st.ArmorCrafting);
                console.sendMessage("Food Crafting: "+st.FoodCrafting);
                console.sendMessage("Potion Crafting: "+st.PotionCrafting);
                console.sendMessage("Kills: "+st.Kills);
                console.sendMessage("Runned: "+st.Runer);
            }
        }
        return true;
    }
}
