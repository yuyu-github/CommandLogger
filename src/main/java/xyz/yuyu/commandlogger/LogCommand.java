package xyz.yuyu.commandlogger;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

public class LogCommand implements Listener {
  public void log(CommandSender sender, String command) {
    String name = sender.getName();
    long time = new Date().getTime();
    String timeString = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date());
    boolean hasLocation = true;
    double x = 0;
    double y = 0;
    double z = 0;

    String type = "";
    if (sender instanceof ConsoleCommandSender) type = "Console";
    else if (sender instanceof Player) type = "Player";

    switch (type) {
      case "Player":
        Location location = ((Player) sender).getLocation();
        x = location.getX();
        y = location.getY();
        z = location.getZ();
        break;
      default:
        hasLocation = false;
    }

    if (!type.equals("")) {
      Config logfile = new Config(CommandLogger.plugin,
          "logs/" + new SimpleDateFormat("yyyy-MM-dd").format(new Date()) + ".yml");
      FileConfiguration log = logfile.getConfig();
      String uuid = UUID.randomUUID().toString();
      log.set(uuid + ".command", command);
      log.set(uuid + ".type", type);
      log.set(uuid + ".name", name);
      log.set(uuid + ".time", time);
      log.set(uuid + ".time-string", timeString);

      if (hasLocation) {
        log.set(uuid + ".location.x", x);
        log.set(uuid + ".location.y", y);
        log.set(uuid + ".location.z", z);
      }

      logfile.saveConfig();
    }
  }

  @EventHandler
  public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent e) {
    log(e.getPlayer(), e.getMessage());
  }

  @EventHandler
  public void onServerCommand(ServerCommandEvent e) {
    log(e.getSender(), "/" + e.getCommand());
  }
}
