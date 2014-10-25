package org.salvagecraft.RegiusChat;

import java.util.*;

import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class RegiusChat extends JavaPlugin implements Listener {
    public HashMap <String, ArrayList<String>> ignore = new HashMap<String, ArrayList<String>>();
    public Permission permission;
    public HashMap<String, String> colors = new HashMap();
    public List<String> disallowed = Arrays.asList("magic", "bold", "strikethrough", "underline", "italic", "reset");
    public List<String> msgAliases = Arrays.asList("msg", "tell", "t", "m", "whisper", "emsg", "etell", "ewhisper");
    public List<String> replyAliases = Arrays.asList("reply", "ereply", "r", "er");

    public HashMap<String, String> previous = new HashMap<String, String>();
    Map.Entry<String,String> entry;
    Iterator<Map.Entry<String,String>> it;

    public void onEnable() {
        getConfig().options().copyDefaults(true);
        saveConfig();
        try {
            ignore = SaveLoad.load("plugins\\RegiusChat\\ignore.bin");
        } catch(Exception e){
            getLogger().info("Generating ignore.bin...");
            try {
                SaveLoad.save(ignore, "plugins\\RegiusChat\\ignore.bin");
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
        fillColors();
        setPermissionMessages();
        this.permission = getServer().getServicesManager().getRegistration(Permission.class).getProvider();
        getServer().getPluginManager().registerEvents(this, this);
    }

    public void onDisable()	{
        try{
            SaveLoad.save(ignore, "plugins\\RegiusChat\\ignore.bin");
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    public void setPermissionMessages() {
        FileConfiguration config = YamlConfiguration.loadConfiguration(getResource("plugin.yml"));
        for (String cmd : config.getConfigurationSection("commands").getKeys(false)) {
            getCommand(cmd).setPermissionMessage(ChatColor.translateAlternateColorCodes('&', getConfig().getString("general.noPermMessage")));
        }
    }

    public void fillColors() {
        for (ChatColor color : ChatColor.values()) {
            this.colors.put(color.name().replace("_", "").toLowerCase(), color.name());
        }
    }

    public ChatColor valueOfChatColor(String color) {
        try {
            return ChatColor.valueOf(color);
        } catch (Exception e) {
        }
        return ChatColor.WHITE;
    }

    public ChatColor getChatColor(Player player) {
        if (getConfig().contains("settings.colors." + player.getUniqueId().toString())) {
            return valueOfChatColor(getConfig().getString("settings.colors." + player.getUniqueId().toString()));
        }
        return valueOfChatColor(getConfig().getString("general.defaultChatColor"));
    }

    public String getMessage(String[] args, int from) {
        StringBuilder builder = new StringBuilder();
        for (int i = from; i < args.length; i++) {
            builder.append(args[i] + " ");
        }
        return builder.toString().trim();
    }

    public void removeFromPrevious(String player) {
        it = previous.entrySet().iterator();
        while (it.hasNext()) {
            entry = it.next();
            if (entry.getValue().equals(player)) {
                this.previous.remove(entry.getKey());
            }
        }
    }

    public void message(Player sender, String target, String[] args, int from) {
        Player p = getServer().getPlayer(target);
        if (p != null) {
            if (ignore.containsKey(p.getUniqueId().toString())) {
                ArrayList<String> arrayList = ignore.get(p.getUniqueId().toString());
                if (arrayList.contains(sender.getUniqueId().toString())) {
                    sender.sendMessage("§cThis person has ignored you - please try again later.");
                    return;
                }
            }

            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', getConfig().getString("general.messageMessage").replace("<receiver>", p.getName()).replace("<message>", getChatColor(sender) + getMessage(args, from))));
            p.sendMessage(ChatColor.translateAlternateColorCodes('&', getConfig().getString("general.messagedMessage").replace("<sender>", sender.getName()).replace("<message>", getChatColor(sender) + getMessage(args, from))));
            this.previous.put(sender.getName(), p.getName());
            this.previous.put(p.getName(), sender.getName());
        } else {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', getConfig().getString("general.notOnlineMessage").replace("<player>", args[0])));
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();

        if (player.hasPermission("regiuschat.usecolors")) {
            event.setFormat(ChatColor.translateAlternateColorCodes('&', getConfig().getString("settings.formats." + this.permission.getPrimaryGroup(player), getConfig().getString("settings.formats.default")).replace("<player>", "%s").replace("<message>", getChatColor(player) + "%s")));
        } else {
            event.setFormat(ChatColor.translateAlternateColorCodes('&', getConfig().getString("settings.formats." + this.permission.getPrimaryGroup(player), getConfig().getString("settings.formats.default"))).replace("<player>", "%s").replace("<message>", getChatColor(player) + "%s"));
        }

        ArrayList<String> avoid = new ArrayList<String>(); //avoid these uuids

        for (Map.Entry entry : ignore.entrySet()) {
            if (((ArrayList<String>) entry.getValue()).contains(player.getUniqueId().toString())) { //Casting Exceptions.
                avoid.add((String) entry.getKey());
            }
        }

        for (Player plr : Bukkit.getOnlinePlayers()) {
            String plrUUID = plr.getUniqueId().toString();

            if (avoid.contains(plrUUID)) {
                event.getRecipients().remove(plr);
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        this.previous.remove(player.getName());
        removeFromPrevious(player.getName());
    }

    public void sendHelpMessage(CommandSender sender) {
        sender.sendMessage("§6§lREGIUSCHAT§f | §7/regiuschat");
        sender.sendMessage("§6Oo-----------------------oOo-----------------------oO");
        sender.sendMessage("§2/regiuschat help §f- §aGet command help");
        sender.sendMessage("§2/regiuschat reload §f- §aReload the plugin");
        sender.sendMessage("§6Oo-----------------------oOo-----------------------oO");
    }

    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        Player player = (Player) sender;
        if (cmd.getName().equalsIgnoreCase("regiuschat")) {
            if (args.length == 0) {
                sendHelpMessage(sender);
            } else if (args[0].equalsIgnoreCase("reload")) {
                if (sender.hasPermission("regiuschat.reload")) {
                    reloadConfig();
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', getConfig().getString("general.reloadMessage")));
                } else {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', getConfig().getString("general.noPermMessage")));
                }
            }
        } else if (this.msgAliases.contains(cmd.getName())) {
            if (args.length < 2) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', getConfig().getString("general.messageHelpMessage")));
            } else {
                message(player, args[0], args, 1);
            }
        } else if (this.replyAliases.contains(cmd.getName())) {
            if (args.length < 1) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', getConfig().getString("general.replyHelpMessage")));
            } else if (this.previous.containsKey(sender.getName())) {
                message(player, this.previous.get(sender.getName()), args, 0);
            } else {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', getConfig().getString("general.cantReplyMessage")));
            }
        } else if (cmd.getName().equalsIgnoreCase("color")) {
            if (args.length == 0) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', getConfig().getString("general.colorHelpMessage")));
            } else if (this.colors.containsKey(args[0].toLowerCase())) {
                if (!this.disallowed.contains(args[0].toLowerCase())) {
                    getConfig().set("settings.colors." + player.getUniqueId().toString(), this.colors.get(args[0].toLowerCase()));
                    saveConfig();
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', getConfig().getString("general.colorMessage")));
                } else {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', getConfig().getString("general.notValidMessage")));
                }
            } else {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', getConfig().getString("general.notValidMessage")));
            }
        } else if (cmd.getName().equals("ignore")) {
            if (args.length == 1) {
                String uuid = player.getUniqueId().toString();
                String targetUUID = Bukkit.getServer().getOfflinePlayer(args[0]).getUniqueId().toString();

                if (ignore.containsKey(uuid)) {
                    ArrayList<String> blocked = ignore.get(uuid);
                    if (!blocked.contains(targetUUID)) {
                        blocked.add(targetUUID);
                        ignore.put(uuid, blocked);
                        player.sendMessage("§aSuccessfully ignored §c" + args[0] + "!");
                    } else {
                        blocked.remove(targetUUID);
                        ignore.put(uuid, blocked);
                        player.sendMessage("§aSuccessfully un-ignored §c" + args[0] + "!");
                    }
                } else {
                    ArrayList<String> blocked = new ArrayList<String>();
                    blocked.add(targetUUID);
                    ignore.put(uuid, blocked);
                    player.sendMessage("§aSuccessfully ignored §c" + args[0] + "!");
                }
            } else {
                player.sendMessage("§cIncorrect usage! /ignore <player>");
            }
        }
        return true;
    }
}