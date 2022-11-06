package ru.unicorns.daemons;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import ru.unicorns.objects.StatStorage;
import ru.unicorns.objects.Stats;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class PlayerStatsDaemon implements Listener {

    /**
     * Конструктор демона уведомляющий о готовности к работе и загружающий необходимые ресурсы
     */
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
        Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.GREEN+"PlayerStatDaemon\t✅ ");
    }

    /**
     * Проверка, что статы игрока существуют.
     * Если не существуют - генерация нового класса статов
     * @param event PlayerJoinEvent
     */
    @EventHandler
    public void PlayerImport(PlayerJoinEvent event)
    {
        String nickname = event.getPlayer().getName();
        boolean foundInStorage = false;
        if (!StatStorage.PlayersStats.isEmpty()) {
            for (Stats st : StatStorage.PlayersStats) {
                if (st.Nickname.equals(nickname)) {
                    Bukkit.getLogger().info("[UnicRPG] Found "+ st.Nickname+ " in StatStorage");
                    foundInStorage = true;
                    break;
                }
            }
        }
        if (!foundInStorage)
        {
            Stats stat = new Stats();
            stat.Nickname = nickname;
            Bukkit.getLogger().info("[UnicRPG] Adding "+ stat.Nickname+ " to StatStorage");
            StatStorage.PlayersStats.add(stat);
        }
    }

    public void Dispose()
    {
        TimingExport endexport = new TimingExport();
        Thread childTread = new Thread(endexport);
        childTread.run();
        Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.RED+"PlayerStatsDaemon shutted down\t🛑");
    }

    /**
     * Экспорт информации о статах по расписанию в файл stats.dat
     */
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
