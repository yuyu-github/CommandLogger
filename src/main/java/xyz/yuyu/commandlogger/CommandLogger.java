package xyz.yuyu.commandlogger;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public final class CommandLogger extends JavaPlugin {

  @Override
  public void onEnable() {
    getLogger().info("プラグインが起動しました");
  }

  @Override
  public void onDisable() {
  }
}
