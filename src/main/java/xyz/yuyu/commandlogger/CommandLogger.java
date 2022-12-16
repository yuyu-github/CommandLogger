package xyz.yuyu.commandlogger;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

public final class CommandLogger extends JavaPlugin {
  static Plugin plugin;

  Config config;

  @Override
  public void onEnable() {
    plugin = this;

    Bukkit.getServer().getPluginManager().registerEvents(new LogCommand(), this);

    config = new Config(this);

    getLogger().info("プラグインが起動しました");
  }

  @Override
  public void onDisable() {
  }
}
