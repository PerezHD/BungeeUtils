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
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.plugin.PluginManager;

/**
 *
 * @author Harry5573
 */
public class BungeeUtils extends Plugin implements Listener {

    public static BungeeUtils plugin;
    public boolean isMaintOn = false;
    private Path sampleConfigPath;
    private List<String> sampleLines;
    public String messagePermissionDenied;
    public String messageServerKickMaintenance;
    public String messageMOTDMaintenance;
    public String defaultServerName;
    public ServerPing.PlayerInfo[] serverPingInfo;
    public ChatColor tabChatColor;
    public ChatColor tabStaffChatColor;

    @Override
    public void onEnable() {
        plugin = this;
        this.log("BungeeUtils by Harry5573 starting up!");

        File configFile = new File(this.getDataFolder(), "sampleinfo.txt");
        this.sampleConfigPath = configFile.toPath();
        this.loadConfigs();

        ProxyServer.getInstance().getPluginManager().registerListener(this, new BungeeListener(this));

        this.registerCommands();
        /**
         * Command maintenance
         */
        this.log("BungeeUtils by Harry5573 started!");
    }

    @Override
    public void onDisable() {
        log("BungeeUtils by Harry5573 Disabled");
    }

    /**
     * Colours a message
     *
     * @param messages
     * @return
     */
    private List<String> colorize(List<String> messages) {
        List<String> newList = new ArrayList<>(messages.size());
        for (String msg : messages) {
            newList.add(this.colorMessage(msg));
        }
        return newList;
    }

    private String colorMessage(String msg) {
        return ChatColor.translateAlternateColorCodes('&', msg);
    }

    public void loadConfigs() {
        File folder = new File("plugins/" + this.getDescription().getName());
        String filename = "/config.ini";

        Properties config = new Properties();
        try {
            if (!folder.exists()) {
                folder.mkdirs();
            }
            File file = new File(folder + filename);
            if (!file.exists()) {
                file.createNewFile();
                config.load(new FileReader(file));
                config.setProperty("messageNoPerm", "&cPermission Denied");
                config.setProperty("messageMaintenanceKick", "&cYou have been kicked due to &bMaintenance being enabled. &cJoin back soon");
                config.setProperty("motdMaintenance", "&cServer in &bMaintenance &cmode!");
                config.setProperty("defaultServer", "hub");
                config.setProperty("tabColor", "YELLOW");
                config.setProperty("tabStaffColor", "LIGHT_PURPLE");
                config.store(new FileWriter(file), getDescription().getName() + " Configuration");
            } else {
                config.load(new FileReader(file));
            }
            this.messagePermissionDenied = this.colorMessage(config.getProperty("messageNoPerm", "cPermission Denied"));
            this.messageServerKickMaintenance = this.colorMessage(config.getProperty("messageMaintenanceKick", "&cYou have been kicked due to &bMaintenance being enabled. &cJoin back soon"));
            this.messageMOTDMaintenance = this.colorMessage(config.getProperty("motdMaintenance", "&cServer in &bMaintenance &cmode!"));
            this.defaultServerName = config.getProperty("defaultServer", "hub");
            this.tabChatColor = ChatColor.valueOf(config.getProperty("tabColor", "YELLOW"));
            this.tabStaffChatColor = ChatColor.valueOf(config.getProperty("tabStaffColor", "LIGHT_PURPLE"));
        } catch (Exception e) {
            ProxyServer.getInstance().getLogger().severe("Unable to load configuration file, please make sure it exists!  Using default values");
        }

        if (Files.notExists(this.sampleConfigPath)) {
            try {
                Files.createDirectories(this.sampleConfigPath.getParent());
                Files.createFile(this.sampleConfigPath);
                this.sampleLines = new ArrayList<>();
            } catch (IOException ex) {
            }
        } else {
            try {
                this.sampleLines = this.colorize(Files.readAllLines(this.sampleConfigPath, StandardCharsets.UTF_8));
            } catch (IOException ex) {
            }
        }

        this.loadServerListSample(false);
    }

    public void log(String msg) {
        this.getLogger().info(msg);
    }

    public void loadServerListSample(boolean maintenanceEnable) {
        if (maintenanceEnable) {
            List<String> linesMaintenance = new ArrayList<>();
            linesMaintenance.add(ChatColor.RED + "The server is currently in maintenance mode!");
            linesMaintenance.add(ChatColor.LIGHT_PURPLE + "We will be back soon!");

            ServerPing.PlayerInfo[] info = new ServerPing.PlayerInfo[linesMaintenance.size()];

            for (int i = 0; i < info.length; i++) {
                String line = (String) linesMaintenance.get(i);
                info[i] = new ServerPing.PlayerInfo(line.length() > 0 ? line : "§r", "");
            }
            this.serverPingInfo = info;
        } else {
            List<String> linesNoMaintenance = new ArrayList<>();

            for (String s : this.sampleLines) {
                linesNoMaintenance.add(s);
            }

            ServerPing.PlayerInfo[] info = new ServerPing.PlayerInfo[linesNoMaintenance.size()];
            for (int i = 0; i < info.length; i++) {
                String line = (String) linesNoMaintenance.get(i);
                info[i] = new ServerPing.PlayerInfo(line.length() > 0 ? line : "§r", "");
            }
            this.serverPingInfo = info;
        }
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
}