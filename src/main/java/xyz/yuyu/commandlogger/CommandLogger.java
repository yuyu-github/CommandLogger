package xyz.yuyu.commandlogger;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import xyz.yuyu.commandlogger.commands.Commandlog;

import java.util.Objects;

public final class CommandLogger extends JavaPlugin {
  public static Plugin plugin;

  Config config;

  @Override
  public void onEnable() {
    plugin = this;

    Bukkit.getServer().getPluginManager().registerEvents(new LogCommand(), this);

    Objects.requireNonNull(getCommand("commandlog")).setExecutor(new Commandlog());

    config = new Config(this);

    getLogger().info("プラグインが起動しました");
  }

  @Override
  public void onDisable() {
  }
}
