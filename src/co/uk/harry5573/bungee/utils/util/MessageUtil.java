/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package co.uk.harry5573.bungee.utils.util;

import net.md_5.bungee.api.ChatColor;

/**
 *
 * @author devan_000
 */
public class MessageUtil {

    public static String translateToColorCode(String msg) {
        return ChatColor.translateAlternateColorCodes('&', msg);
    }

}
