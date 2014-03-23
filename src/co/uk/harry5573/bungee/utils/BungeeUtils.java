/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package co.uk.harry5573.bungee.utils;

import co.uk.harry5573.bungee.utils.commands.CommandAddServer;
import co.uk.harry5573.bungee.utils.commands.CommandListServers;
import co.uk.harry5573.bungee.utils.commands.CommandLobby;
import co.uk.harry5573.bungee.utils.listener.BungeeListener;
import co.uk.harry5573.bungee.utils.commands.CommandMaintenance;
import co.uk.harry5573.bungee.utils.commands.CommandReload;
import co.uk.harry5573.bungee.utils.commands.CommandRemoveServer;
import co.uk.harry5573.bungee.utils.commands.CommandServer;
import co.uk.harry5573.bungee.utils.enumerations.EnumMessage;
import co.uk.harry5573.bungee.utils.util.MessageUtil;
import com.google.common.collect.Maps;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;
import net.craftminecraft.bungee.bungeeyaml.bukkitapi.file.FileConfiguration;
import net.craftminecraft.bungee.bungeeyaml.bukkitapi.file.YamlConfiguration;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ServerPing.PlayerInfo;
import net.md_5.bungee.api.plugin.PluginManager;

/**
 *
 * @author Harry5573
 */
public class BungeeUtils extends Plugin implements Listener {

    public static BungeeUtils plugin;
    
    public boolean maintenanceEnabled = false;

    public String defaultServerName;
    
    public PlayerInfo[] serverHoverPlayerListDefault;
    public PlayerInfo[] serverHoverPlayerListMaintenance;

    public ChatColor tabDefaultColor;
    public ChatColor tabStaffColor;
    public HashMap<EnumMessage, String> messages = Maps.newHashMap();

    public int peakPlayers = 0;
    public int currentMaxPlayers = 0;
    public int currentOnlinePlayers = 0;

    FileConfiguration config = null;

    @Override
    public void onEnable() {
        plugin = this;
        this.log("BungeeUtils by Harry5573 starting up!");

        File configFile = new File(this.getDataFolder(), "sampleinfo.txt");
        this.loadConfigs();

        ProxyServer.getInstance().getPluginManager().registerListener(this, new BungeeListener(this));

        this.registerCommands();

        this.log("BungeeUtils by Harry5573 started!");

        this.getProxy().getScheduler().schedule(this, new Runnable() {
            @Override
            public void run() {
                List<String> hoverList = config.getStringList("hoverplayerlist");
                PlayerInfo[] info = new PlayerInfo[hoverList.size()];
                for (int i = 0; i < info.length; i++) {
                    String line = MessageUtil.translateToColorCode(hoverList.get(i).replace("[peakplayers]", String.valueOf(peakPlayers)).replace("[online]", String.valueOf(currentOnlinePlayers)).replace("[max]", String.valueOf(currentMaxPlayers)));
                    info[i] = new PlayerInfo(line.length() > 0 ? line : "§r", "");
                }
                serverHoverPlayerListDefault = info;

                saveConfig("config.yml", config);
            }
        }, 1, 5, TimeUnit.SECONDS);
    }

    @Override
    public void onDisable() {
        log("BungeeUtils by Harry5573 Disabled");
    }

    public void loadConfigs() {
        this.saveResource("config.yml", false);
        config = this.getConfig("config.yml");

        this.defaultServerName = config.getString("defaultserver");
        this.messages.put(EnumMessage.NOPERM, MessageUtil.translateToColorCode(config.getString("messages.noperm")));
        this.messages.put(EnumMessage.KICKMAINTENANCE, MessageUtil.translateToColorCode(config.getString("messages.kickmaintenance")));
        this.messages.put(EnumMessage.MOTDMAINTENANCE, MessageUtil.translateToColorCode(config.getString("motd.maintenance")));
        this.messages.put(EnumMessage.MOTDUNKNOWNPLAYER, MessageUtil.translateToColorCode(config.getString("motd.unknownplayer")));
        this.messages.put(EnumMessage.MOTDKNOWNPLAYER, MessageUtil.translateToColorCode(config.getString("motd.player")));
        this.tabDefaultColor = ChatColor.valueOf(config.getString("tabcolor.default"));
        this.tabStaffColor = ChatColor.valueOf(config.getString("tabcolor.staff"));
        this.peakPlayers = config.getInt("peakplayers");

        List<String> hoverList = config.getStringList("hoverplayerlist");
        PlayerInfo[] info = new PlayerInfo[hoverList.size()];
        for (int i = 0; i < info.length; i++) {
            String line = MessageUtil.translateToColorCode(hoverList.get(i).replace("[peakplayers]", String.valueOf(this.peakPlayers)));
            info[i] = new PlayerInfo(line.length() > 0 ? line : "§r", "");
        }
        this.serverHoverPlayerListDefault = info;

        List<String> hoverListMaintenance = config.getStringList("hoverplayerlistmaintenance");
        PlayerInfo[] infoMaintenance = new PlayerInfo[hoverListMaintenance.size()];
        for (int i = 0; i < infoMaintenance.length; i++) {
            String line = MessageUtil.translateToColorCode(hoverListMaintenance.get(i).replace("[peakplayers]", String.valueOf(this.peakPlayers)));
            infoMaintenance[i] = new PlayerInfo(line.length() > 0 ? line : "§r", "");
        }
        this.serverHoverPlayerListMaintenance = infoMaintenance;
    }
    
    public void log(String msg) {
        this.getLogger().info(msg);
    }

    public void registerCommands() {
        PluginManager pm = ProxyServer.getInstance().getPluginManager();

        pm.registerCommand(this, new CommandMaintenance(this, "maintenance", "", new String[]{"maint"}));
        pm.registerCommand(this, new CommandLobby(this, "hub", "", new String[]{"lobby"}));
        pm.registerCommand(this, new CommandServer(this, "server", "", new String[]{"listservers"}));
        pm.registerCommand(this, new CommandListServers(this, "listservers", "", new String[]{"serverlist"}));
        pm.registerCommand(this, new CommandReload(this, "bureload", "", new String[]{"abreload"}));
        pm.registerCommand(this, new CommandAddServer(this, "addserver", "", new String[]{"add"}));
        pm.registerCommand(this, new CommandRemoveServer(this, "removeserver", "", new String[]{"delserver"}));
    }

    public InputStream getResource(String filename) {
        if (filename == null) {
            throw new IllegalArgumentException("Filename cannot be null");
        }

        try {
            URL url = this.getClass().getClassLoader().getResource(filename);

            if (url == null) {
                return null;
            }

            URLConnection connection = url.openConnection();
            connection.setUseCaches(false);
            return connection.getInputStream();
        } catch (IOException ex) {
            return null;
        }
    }

    public void saveResource(String resourcePath, boolean replace) {
        if (resourcePath == null || resourcePath.equals("")) {
            throw new IllegalArgumentException("ResourcePath cannot be null or empty");
        }

        resourcePath = resourcePath.replace('\\', '/');
        InputStream in = getResource(resourcePath);
        if (in == null) {
            throw new IllegalArgumentException("The embedded resource '" + resourcePath + "' cannot be found");
        }

        File outFile = new File(getDataFolder(), resourcePath);
        int lastIndex = resourcePath.lastIndexOf('/');
        File outDir = new File(getDataFolder(), resourcePath.substring(0, lastIndex >= 0 ? lastIndex : 0));

        if (!outDir.exists()) {
            outDir.mkdirs();
        }

        try {
            if (!outFile.exists() || replace) {
                OutputStream out = new FileOutputStream(outFile);
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                out.close();
                in.close();
            } else {
            }
        } catch (IOException ex) {
        }
    }

    public FileConfiguration getConfig(String configName) {
        return YamlConfiguration.loadConfiguration(new File(getDataFolder() + File.separator + configName));
    }

    public void saveConfig(String configName, FileConfiguration config) {
        try {
            config.save(new File(getDataFolder() + File.separator + configName));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public boolean configExists(String configName) {
        return new File(getDataFolder() + File.separator + configName).exists();
    }

    public String clearifyIP(String input) {
        return input.substring(1).split(":")[0];
    }
}
