/*Copyright (C) Harry5573 2013-14

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.*/
package co.uk.harry5573.bungee.utils.listener;

import co.uk.harry5573.bungee.utils.BungeeUtils;
import co.uk.harry5573.bungee.utils.enumerations.EnumMessage;
import com.google.common.collect.Maps;
import java.util.HashMap;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ServerPing.Protocol;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.event.ProxyPingEvent;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.event.ServerKickEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;
/**
 *
 * @author Harry5573
 */
public class BungeeListener implements Listener {

    private BungeeUtils plugin;

    public BungeeListener(BungeeUtils instance) {
        this.plugin = instance;
    }

    public HashMap<String, String> knownClientIPS = Maps.newHashMap();

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPing(ProxyPingEvent e) {
        PendingConnection connection = e.getConnection();
        if (connection == null || connection.getVirtualHost() == null || connection.getVirtualHost().getHostName() == null) {
            return;
        }

        if (plugin.maintenanceEnabled) {
            e.getResponse().setDescription(plugin.messages.get(EnumMessage.MOTDMAINTENANCE));
            e.getResponse().getPlayers().setMax(0);
            e.getResponse().getPlayers().setOnline(0);
            e.getResponse().getPlayers().setSample(plugin.serverHoverPlayerListMaintenance);
            e.getResponse().setVersion(new Protocol(ChatColor.YELLOW + "Maintenance Mode", 99));
        } else {
            e.getResponse().getPlayers().setSample(plugin.serverHoverPlayerListDefault);
            String ip = this.plugin.clearifyIP(e.getConnection().getAddress().toString());
            if (this.knownClientIPS.containsKey(ip)) {
                e.getResponse().setDescription(plugin.messages.get(EnumMessage.MOTDKNOWNPLAYER).replace("[player]", this.knownClientIPS.get(ip)));
            } else {
                e.getResponse().setDescription(plugin.messages.get(EnumMessage.MOTDUNKNOWNPLAYER));
            }
        }

        int online = e.getResponse().getPlayers().getOnline();

        plugin.currentMaxPlayers = e.getResponse().getPlayers().getMax();
        plugin.currentOnlinePlayers = online;
        if (online > plugin.peakPlayers) {
            plugin.peakPlayers = online;
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onLogin(PostLoginEvent e) {
        ProxiedPlayer p = e.getPlayer();
        if (!this.knownClientIPS.containsKey(p.getName())) {
            this.knownClientIPS.put(plugin.clearifyIP(p.getAddress().toString()), p.getName());
        }
        if (plugin.maintenanceEnabled && !p.hasPermission("bungeeutils.bypassmaintenance")) {
            p.disconnect(new ComponentBuilder("").append(plugin.messages.get(EnumMessage.KICKMAINTENANCE)).create());
            return;
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onLoginServer(ServerConnectEvent e) {
        ProxiedPlayer p = e.getPlayer();
        if (plugin.maintenanceEnabled && !p.hasPermission("bungeeutils.bypassmaintenance")) {
            p.disconnect(new ComponentBuilder("").append(plugin.messages.get(EnumMessage.KICKMAINTENANCE)).create());
            return;
        }

        String name = p.getName();

        if (p.hasPermission("bungeeutils.staffcolor")) {
            name = plugin.tabStaffColor + name;
        } else {
            name = plugin.tabDefaultColor + name;
        }

        if (name.length() > 16) {
            p.setDisplayName(name.substring(0, 16));
        } else {
            p.setDisplayName(name);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onKickServer(ServerKickEvent e) {
        ProxiedPlayer p = e.getPlayer();
        if (plugin.maintenanceEnabled && !p.hasPermission("bungeeutils.bypassmaintenance")) {
            p.disconnect(new ComponentBuilder("").append(plugin.messages.get(EnumMessage.KICKMAINTENANCE)).create());
            return;
        }

        ServerInfo info = plugin.getProxy().getServerInfo(plugin.defaultServerName);
        if (p.getServer() != info) {
            p.connect(info);
            e.setCancelled(true);
            p.sendMessage(new ComponentBuilder("").append(ChatColor.AQUA + "You were disconnected for: " + ChatColor.RED + e.getKickReason()).create());
            return;
        }
    }
}
