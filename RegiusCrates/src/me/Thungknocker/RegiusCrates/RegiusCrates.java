package me.Thungknocker.RegiusCrates;

import java.io.File;
import java.io.IOException;
import java.util.*;

import com.evilmidget38.UUIDFetcher;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

public class RegiusCrates extends JavaPlugin {
    public HashMap<String, Prize> prizes = new HashMap<>();
    File p = new File("plugins/RegiusCrates/players.yml");
    YamlConfiguration players = YamlConfiguration.loadConfiguration(this.p);
    Random rand = new Random();

    public void onDisable() {
        saveConfig();

        PluginDescriptionFile pdfFile = getDescription();
        getLogger().info(pdfFile.getName() + " has been disabled");
    }

    public void onEnable() {
        getLogger().info("RegiusCrates has been enabled!");

        this.saveDefaultConfig();

        if (!this.p.exists()) {
            getLogger().info("No player.yml file found! Generating a new one...");

            try {
                this.players.save(this.p);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        ConfigurationSection prizes = getConfig().getConfigurationSection("Prizes");
        if (prizes != null) {
            for (String number : prizes.getKeys(false)) {
                ConfigurationSection prizeConfig = prizes.getConfigurationSection(number);
                if (prizeConfig != null) {
                    this.prizes.put(number, new Prize(getConfig().getString("Prizes." + number + ".playermessage"), getConfig().getStringList("Prizes." + number + ".commands")));
                }
            }
        }
    }

    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        String playername = sender.getName();
        List labels = Arrays.asList("crate", "crates", "regiuscrates");

        if (labels.contains(commandLabel)) {
            if (args.length == 0) {
                String defaultborder = getConfig().getString("Messages.defaultborder").replaceAll("&", "§");
                String defaultmessage1 = getConfig().getString("Messages.defaultmessage1").replaceAll("&", "§");

                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    if (this.players.contains(player.getUniqueId().toString())) {
                        String amount = Integer.toString(this.players.getInt(player.getUniqueId().toString() + ".crates"));
                        defaultmessage1 = defaultmessage1.replace("{amount}", amount);

                        if (!this.players.get(player.getUniqueId().toString() + ".lastName").equals(player.getName())) {
                            this.players.set(player.getUniqueId().toString() + ".lastName", player.getName());
                        }
                    } else {
                        defaultmessage1 = defaultmessage1.replace("{amount}", "0");
                        this.players.set(player.getUniqueId().toString() + ".crates", "0");
                        this.players.set(player.getUniqueId().toString() + ".lastName", player.getName());
                    }
                } else {
                    defaultmessage1 = defaultmessage1.replace("{amount}", "0");
                }

                String defaultmessage2 = getConfig().getString("Messages.defaultmessage2").replaceAll("&", "§");
                String defaultmessage3 = getConfig().getString("Messages.defaultmessage3").replaceAll("&", "§");
                String defaultmessage4 = getConfig().getString("Messages.defaultmessage4").replaceAll("&", "§");

                sender.sendMessage(defaultborder);
                sender.sendMessage(defaultmessage1);
                sender.sendMessage(defaultmessage2);
                sender.sendMessage(defaultmessage3);
                sender.sendMessage(defaultmessage4);
                sender.sendMessage(defaultborder);

                try {
                    this.players.save(this.p);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                String RewardMSG;

                if ((args[0].equals("rewards")) || (args[0].equals("reward"))) {
                    String border = getConfig().getString("Messages.rewardsborder").replaceAll("&", "§");

                    sender.sendMessage(border);

                    for (int i = 1; i < 100; i++) {
                        if (getConfig().getString("Rewards." + i) == null) {
                            break;
                        }

                        RewardMSG = getConfig().getString("Rewards." + i);
                        String RewardMSG2 = RewardMSG.replaceAll("&", "§");
                        sender.sendMessage(RewardMSG2);
                    }

                    sender.sendMessage(border);
                } else if (args[0].equals("open")) {
                    if (sender instanceof Player) {
                        Player player = (Player) sender;
                        if (this.players.contains(player.getUniqueId().toString())) {
                            if (this.players.getInt(player.getUniqueId().toString() + ".crates") > 0) {
                                String rewardmessage = getConfig().getString("Messages.rewardmessage").replaceAll("&", "§");
                                sender.sendMessage(rewardmessage);

                                for (Map.Entry entry : this.prizes.entrySet()) {
                                    String key = (String) entry.getKey();
                                    int intkey = Integer.parseInt(key);
                                    if (this.rand.nextInt(intkey) == 0) {
                                        String PrizeMSG = this.prizes.get(key).message;
                                        String PrizeMSG2 = PrizeMSG.replaceAll("&", "§");
                                        sender.sendMessage(PrizeMSG2);
                                        List<String> PrizeCMD = this.prizes.get(key).commands;
                                        for (String command : PrizeCMD) {
                                            command = command.replace("/", "");
                                            command = command.replace("{username}", playername);
                                            getServer().dispatchCommand(getServer().getConsoleSender(), command);
                                        }
                                    }
                                }

                                int newvalue = this.players.getInt(player.getUniqueId().toString() + ".crates") - 1;
                                this.players.set(player.getUniqueId().toString() + ".crates", Integer.valueOf(newvalue));

                                if (!this.players.get(player.getUniqueId().toString() + ".lastName").equals(player.getName())) {
                                    this.players.set(player.getUniqueId().toString() + ".lastName", player.getName());
                                }

                                try {
                                    this.players.save(this.p);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                String nocrates = getConfig().getString("Messages.nocrates").replaceAll("&", "§");
                                sender.sendMessage(nocrates);
                            }
                        } else {
                            String nocrates = getConfig().getString("Messages.nocrates").replaceAll("&", "§");
                            sender.sendMessage(nocrates);
                        }
                    } else {
                        sender.sendMessage("§4Only players can use this command.");
                    }
                } else if (args[0].equals("bal")) {
                    if (sender.isOp()) {
                        if (args.length == 2) {
                            String uuid;
                            Player target = Bukkit.getServer().getPlayer(args[1]);

                            if (target != null) {
                                uuid = target.getUniqueId().toString();
                            } else {
                                UUIDFetcher fetcher = new UUIDFetcher(Arrays.asList(args[1]));

                                Map<String, UUID> response = null;
                                try {
                                    response = fetcher.call();
                                } catch (Exception e) {
                                    getLogger().warning("Exception while running UUIDFetcher");
                                    e.printStackTrace();
                                }

                                uuid = response.get(args[1]).toString();
                            }

                            int amount;

                            if (this.players.contains(uuid)) {
                                amount = this.players.getInt(uuid + ".crates");
                            } else {
                                amount = 0;
                            }

                            sender.sendMessage(ChatColor.YELLOW + args[1] + " has " + amount + " crates left.");
                        } else {
                            sender.sendMessage("§4Incorrect command usage! Type /crates for help.");
                        }
                    } else {
                        sender.sendMessage("§cYou do not have permission to use this command.");
                    }
                } else if (args[0].equals("give")) {
                    if (sender.isOp()) {
                        if (args.length == 3) {
                            int amount = Integer.parseInt(args[2]);

                            String uuid;
                            Player target = Bukkit.getServer().getPlayer(args[1]);

                            if (target != null) {
                                uuid = target.getUniqueId().toString();
                            } else {
                                UUIDFetcher fetcher = new UUIDFetcher(Arrays.asList(args[1]));

                                Map<String, UUID> response = null;
                                try {
                                    response = fetcher.call();
                                } catch (Exception e) {
                                    getLogger().warning("Exception while running UUIDFetcher");
                                    e.printStackTrace();
                                }

                                uuid = response.get(args[1]).toString();
                            }

                            if (!this.players.contains(uuid)) {
                                String stringamount = Integer.toString(amount);
                                this.players.set(uuid + ".crates", Integer.valueOf(amount));
                                this.players.set(uuid + ".lastName", args[1]);
                                sender.sendMessage(ChatColor.YELLOW + "You gave " + amount + " crates to " + args[1] + ".");
                                String receivecrates = getConfig().getString("Messages.receivecrates").replaceAll("&", "§");
                                receivecrates = receivecrates.replace("{amount}", stringamount);
                                if (target != null) target.sendMessage(receivecrates);

                                try {
                                    this.players.save(this.p);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                String stringamount = Integer.toString(amount);
                                int newamount = this.players.getInt(uuid + ".crates") + amount;
                                this.players.set(uuid + ".crates", Integer.valueOf(newamount));
                                sender.sendMessage(ChatColor.YELLOW + "You gave " + amount + " crates to " + args[1] + ".");
                                String receivecrates = getConfig().getString("Messages.receivecrates").replaceAll("&", "§");
                                receivecrates = receivecrates.replace("{amount}", stringamount);
                                if (target != null) target.sendMessage(receivecrates);
                                try {
                                    this.players.save(this.p);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        } else {
                            sender.sendMessage("§cIncorrect usage! Proper usage: §a/crates give {user} {amount}.");
                        }
                    } else {
                        sender.sendMessage("§cYou do not have permission to use this command.");
                    }
                } else if (args[0].equals("set")) {
                    if (sender.isOp()) {
                        if (args.length == 3) {
                            String uuid;

                            int amount = Integer.parseInt(args[2]);
                            Player target = Bukkit.getServer().getPlayer(args[1]);

                            if (target != null) {
                                uuid = target.getUniqueId().toString();
                            } else {
                                UUIDFetcher fetcher = new UUIDFetcher(Arrays.asList(args[1]));

                                Map<String, UUID> response = null;
                                try {
                                    response = fetcher.call();
                                } catch (Exception e) {
                                    getLogger().warning("Exception while running UUIDFetcher");
                                    e.printStackTrace();
                                }

                                uuid = response.get(args[1]).toString();
                            }

                            if (!this.players.contains(uuid)) {
                                this.players.set(uuid + ".crates", Integer.valueOf(amount));
                                this.players.set(uuid + ".lastName", args[1]);

                                sender.sendMessage(ChatColor.YELLOW + "You set " + args[1] + "'s crate balance to " + amount + ".");

                                try {
                                    this.players.save(this.p);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                            } else {
                                this.players.set(uuid + ".crates", Integer.valueOf(amount));

                                sender.sendMessage(ChatColor.YELLOW + "You set " + args[1] + "'s crate balance to " + amount + ".");

                                try {
                                    this.players.save(this.p);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        } else {
                            sender.sendMessage("§cIncorrect usage! Proper usage: §a/crates give {user} {amount}.");
                        }
                    } else {
                        sender.sendMessage("§cYou do not have permission to use this command.");
                    }
                } else if (this.players.contains(playername)) {
                    String defaultborder = getConfig().getString("Messages.defaultborder").replaceAll("&", "§");
                    String defaultmessage1 = getConfig().getString("Messages.defaultmessage1").replaceAll("&", "§");

                    if (sender instanceof Player) {
                        Player player = (Player) sender;
                        if (this.players.contains(player.getUniqueId().toString())) {
                            String amount = Integer.toString(this.players.getInt(player.getUniqueId().toString() + ".crates"));
                            defaultmessage1 = defaultmessage1.replace("{amount}", amount);

                            if (!this.players.get(player.getUniqueId().toString() + ".lastName").equals(player.getName())) {
                                this.players.set(player.getUniqueId().toString() + ".lastName", player.getName());
                            }
                        } else {
                            defaultmessage1 = defaultmessage1.replace("{amount}", "0");
                            this.players.set(player.getUniqueId().toString() + ".crates", "0");
                            this.players.set(player.getUniqueId().toString() + ".lastName", player.getName());
                        }
                    } else {
                        defaultmessage1 = defaultmessage1.replace("{amount}", "0");
                    }

                    String defaultmessage2 = getConfig().getString("Messages.defaultmessage2").replaceAll("&", "§");
                    String defaultmessage3 = getConfig().getString("Messages.defaultmessage3").replaceAll("&", "§");
                    String defaultmessage4 = getConfig().getString("Messages.defaultmessage4").replaceAll("&", "§");

                    sender.sendMessage(defaultborder);
                    sender.sendMessage(defaultmessage1);
                    sender.sendMessage(defaultmessage2);
                    sender.sendMessage(defaultmessage3);
                    sender.sendMessage(defaultmessage4);
                    sender.sendMessage(defaultborder);

                    try {
                        this.players.save(this.p);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return false;
    }
}