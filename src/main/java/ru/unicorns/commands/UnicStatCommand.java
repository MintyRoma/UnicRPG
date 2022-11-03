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
            if (args[0]!=null) pl.sendMessage(ConstructDetalization(args[0]));
            else pl.sendMessage(ConstructDetalization());
        }
        else
        {
            ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();
            console.sendMessage("Amount of Storage: "+count);
            if (args[0]!=null) console.sendMessage(ConstructDetalization(args[0]));
            else console.sendMessage(ConstructDetalization());
        }
        return true;
    }

    private String ConstructDetalization() {
        String message = "";
        for (Stats st: StatStorage.PlayersStats)
        {
            message+="\n"+ChatColor.AQUA+"Stats of "+st.Nickname +
                    "\nWeapon Crafting: "+st.WeaponCrafting+
                    "\nArmor Crafting: "+st.ArmorCrafting+
                    "\nFood Crafting: "+st.FoodCrafting+
                    "\nPotion Crafting: "+st.PotionCrafting+
                    "\nKills: "+st.Kills+
                    "\nRunned: "+st.Runer;
        }
        return message;
    }

    private String ConstructDetalization(String nickname) {
        String message = "";
        Stats found = null;
        for (Stats st: StatStorage.PlayersStats) if (st.Nickname.equals(nickname)) {
            found = st;
            break;
        }
        if (found != null) {
            message += "\n" + ChatColor.AQUA + "Stats of " + found.Nickname +
                    "\nWeapon Crafting: " + found.WeaponCrafting +
                    "\nArmor Crafting: " + found.ArmorCrafting +
                    "\nFood Crafting: " + found.FoodCrafting +
                    "\nPotion Crafting: " + found.PotionCrafting +
                    "\nKills: " + found.Kills +
                    "\nRunned: " + found.Runer;
        }
        else message = "Such player was not found";
        return message;
    }
}
