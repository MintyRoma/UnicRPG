package ru.unicorns;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;
import ru.unicorns.commands.UnicStatCommand;
import ru.unicorns.daemons.PlayerStatsDaemon;
import ru.unicorns.daemons.WeaponCraftingDaemon;

public final class UnicRPG extends JavaPlugin {

    /**
     * Демон крафта инструмнентов
     */
    private WeaponCraftingDaemon weaponCraftingDaemon;
    /**
     * Демон конфига
     */
    private PlayerStatsDaemon playerStatsDaemon;

    /**
     * При запуске, наследуется от JavaPlugin
     */
    @Override
    public void onEnable() {
        Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.WHITE+"UnicRPG v." + getDescription().getVersion()+" started");
        Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.WHITE+"Starting up daemons:");

        playerStatsDaemon = new PlayerStatsDaemon();
        this.getServer().getPluginManager().registerEvents(this.playerStatsDaemon, this);

        weaponCraftingDaemon = new WeaponCraftingDaemon();
        this.getServer().getPluginManager().registerEvents(this.weaponCraftingDaemon, this);

        Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.WHITE+"Daemons succesfully started");
        Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.WHITE+"Registering commands:");
        this.getCommand("unicstat").setExecutor(new UnicStatCommand());
    }

    /**
     * Действия при отключении плагина, наследуется от JavaPlugin
     */
    @Override
    public void onDisable() {
        this.getLogger().info("Killing daemons");
        weaponCraftingDaemon.Dispose();
        //Other daemons here

        playerStatsDaemon.Dispose();
        this.getLogger().info("Daemons killed");
        this.getLogger().info("Plugin disabled");
    }

}
