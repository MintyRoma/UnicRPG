package ru.unicorns;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import ru.unicorns.commands.UnicStatCommand;
import ru.unicorns.daemons.PlayerStatsDaemon;
import ru.unicorns.daemons.SwordCraftingDaemon;

public final class UnicRPG extends JavaPlugin {

    private SwordCraftingDaemon swordCraftingDaemon;
    private PlayerStatsDaemon playerStatsDaemon;
    @Override
    public void onEnable() {
        Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.WHITE+"UnicRPG started");
        Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.WHITE+"Starting up daemons:");

        playerStatsDaemon = new PlayerStatsDaemon();
        this.getServer().getPluginManager().registerEvents((Listener)this.playerStatsDaemon,(Plugin) this);

        swordCraftingDaemon = new SwordCraftingDaemon();
        this.getServer().getPluginManager().registerEvents((Listener) this.swordCraftingDaemon, (Plugin) this);

        Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.WHITE+"Daemons succesfully started");
        Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.WHITE+"Registering commands:");
        this.getCommand("unicstat").setExecutor(new UnicStatCommand());
    }

    @Override
    public void onDisable() {
        this.getLogger().info("Killing daemons");
        this.getLogger().info("Daemons killed");
        this.getLogger().info("Plugin disabled");
    }

}
