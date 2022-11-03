package ru.unicorns.daemons;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.sun.tools.javac.util.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import ru.unicorns.objects.StatStorage;
import ru.unicorns.objects.Stats;

import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class PlayerStatsDaemon implements Listener {
    public PlayerStatsDaemon()
    {
        try
        {
            File theDir = new File("./plugins/UnicRPG/");
            if (!theDir.exists())
            {
                Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.GOLD+"⚠️PlayerStatDaemon: directory not exists. Creating new!");
                theDir.mkdirs();
            }

            File statsfile = new File("./plugins/UnicRPG/stats.dat");
            if (!statsfile.isFile())
            {
                Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.GOLD+"⚠️PlayerStatDaemon: dat file not exists.Creating new!");
                statsfile.createNewFile();
            }
            Path statspath = Paths.get("./plugins/UnicRPG/stats.dat");

            List<String> readedconfig = Files.readAllLines(statspath);
            String rawconfig = String.join("",readedconfig);
            Gson gson = new Gson();
            List<Stats> copy = gson.fromJson(rawconfig, new TypeToken<List<Stats>>(){}.getType());
            if (copy!=null)StatStorage.PlayersStats =copy;
            Bukkit.getServer().getConsoleSender().sendMessage("Loaded Stats");
        }
        catch (Exception ex)
        {
            Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.RED+"Can't import stats. Reason: "+ex.getMessage());
            Bukkit.shutdown();
        }
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(new TimingExport(), 1, 1, TimeUnit.MINUTES);
        Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.GREEN+"PlayerStatDaemon.................✅ ");
    }

    @EventHandler
    public void PlayerImport(PlayerJoinEvent event)
    {
        String nickname = event.getPlayer().getName();
        Boolean foundInStorage = false;
        if (!StatStorage.PlayersStats.isEmpty()) {
            for (Stats st : StatStorage.PlayersStats) {
                if (st.Nickname.equals(nickname)) {
                    Bukkit.getLogger().info("[UnicRPG] Found "+ st.Nickname+ " in StatStorage");
                    foundInStorage = true;
                    break;
                }
            }
        }
        if (foundInStorage==false)
        {
            Stats stat = new Stats();
            stat.Nickname = nickname;
            Bukkit.getLogger().info("[UnicRPG] Adding "+ stat.Nickname+ " to StatStorage");
            StatStorage.PlayersStats.add(stat);
        }
    }

    public void Dispose()
    {

    }

    private class TimingExport implements Runnable
    {

        @Override
        public void run() {
            GsonBuilder builder = new GsonBuilder();
            Gson gson = builder.setPrettyPrinting().create();
            String rawdata = gson.toJson(StatStorage.PlayersStats);
            try
            {
                FileWriter writer =new FileWriter("./plugins/UnicRPG/stats.dat");
                writer.write(rawdata);
                writer.close();
            }
            catch (Exception ex)
            {

            }
        }
    }
}
