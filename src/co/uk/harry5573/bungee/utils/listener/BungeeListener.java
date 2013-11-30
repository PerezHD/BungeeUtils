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
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.ServerPing.Players;
import net.md_5.bungee.api.config.ServerInfo;
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

    BungeeUtils plugin;

    public BungeeListener(BungeeUtils instance) {
        this.plugin = instance;
    }

    @EventHandler
    public void onPing(ProxyPingEvent e) {
        if ((e.getConnection() == null) || (e.getConnection().getVirtualHost() == null) || (e.getConnection().getVirtualHost().getHostName() == null)) {
            return;
        }

        ServerPing serverPing = new ServerPing();
        serverPing.setFavicon(e.getResponse().getFavicon());
        serverPing.setVersion(e.getResponse().getVersion());

        Players players = e.getResponse().getPlayers();

        if (plugin.isMaintOn) {
            serverPing.setDescription(plugin.messageMOTDMaintenance);
            players.setMax(0);
            players.setOnline(0);
            serverPing.setPlayers(players);
        } else {
            serverPing.setDescription(e.getResponse().getDescription());
            serverPing.setPlayers(players);
        }

        serverPing.getPlayers().setSample(plugin.serverPingInfo);
        e.setResponse(serverPing);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onLogin(PostLoginEvent e) {
        ProxiedPlayer p = e.getPlayer();
        if (plugin.isMaintOn && !p.hasPermission("bungeeutils.bypassmaintenance")) {
            p.disconnect(plugin.messageServerKickMaintenance);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onLoginServer(ServerConnectEvent e) {
        ProxiedPlayer p = e.getPlayer();
        if (plugin.isMaintOn && !p.hasPermission("bungeeutils.bypassmaintenance")) {
            p.disconnect(plugin.messageServerKickMaintenance);
            return;
        }

        String name = p.getName();
        String newname = null;

        if (p.hasPermission("bungeeutils.staffcolor")) {
            newname = plugin.tabStaffChatColor + name;
        } else {
            newname = plugin.tabChatColor + name;
        }

        if (newname.length() > 16) {
            p.setDisplayName(newname.substring(0, 16));
        } else {
            p.setDisplayName(newname);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onKickServer(ServerKickEvent e) {
        ProxiedPlayer p = e.getPlayer();
        if (plugin.isMaintOn && !p.hasPermission("bungeeutils.bypassmaintenance")) {
            p.disconnect(plugin.messageServerKickMaintenance);
            return;
        }

        if (!e.getPlayer().getServer().getInfo().getName().equals(ProxyServer.getInstance().getServers().get("default").getName())) {
            String kickreason = e.getKickReason();
            if (!kickreason.startsWith("§0")) {
                e.setCancelled(true);
                p.sendMessage(ChatColor.AQUA + "You were disconnected for: " + ChatColor.RED + kickreason);
            }
        }
    }
}
