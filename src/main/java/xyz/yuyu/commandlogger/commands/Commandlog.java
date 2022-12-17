package xyz.yuyu.commandlogger.commands;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.util.ChatPaginator;
import xyz.yuyu.commandlogger.CommandLogger;
import xyz.yuyu.commandlogger.Config;

import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Commandlog implements CommandExecutor, TabCompleter {
  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    switch (args[0]) {
      case "search":
        Map<String, List<String>> conditions = new HashMap<>();
        String type = "";
        for (int i = 1; i < args.length; i++) {
          if (args[i].contains("=")) {
            if (args[i].endsWith("=")) type = args[i].substring(0, args[i].length() - 1);
            else {
              int split_index = args[i].indexOf("=");
              type = args[i].substring(0, split_index);
              String value = args[i].substring(split_index + 1);

              if (!conditions.containsKey(type)) conditions.put(type, new ArrayList<>());
              conditions.get(type).add(value);

              type = "";
            }
          } else if (!type.equals("")) {
            if (!conditions.containsKey(type)) conditions.put(type, new ArrayList<>());
            conditions.get(type).add(args[i]);
           }
        }

        List<String> results = new ArrayList<>();

        int day = 5;
        if (conditions.containsKey("time")) {
          String timeCondition = conditions.get("time").get(0);
          if (timeCondition.endsWith("d")) {
            Matcher m = Pattern.compile("^[0-9]+").matcher(timeCondition);
            if (m.find()) day = Integer.parseInt(m.group());
          }
        }

        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.add(Calendar.DAY_OF_MONTH, -(day - 1));
        for (; day > 0; day--, cal.add(Calendar.DAY_OF_MONTH, 1)) {
          Config logfile = new Config(CommandLogger.plugin,
              "logs/" + new SimpleDateFormat("yyyy-MM-dd").format(cal.getTime()) + ".yml");
          FileConfiguration log = logfile.getConfig();
          for (String key : log.getKeys(false)) {
            String command_ = log.getString(key + ".command");
            String type_ = log.getString(key + ".type");
            String name = log.getString(key + ".name");

            if (conditions.containsKey("command")) {
              boolean match = false;
              for (String commandCond : conditions.get("command")) {
                if (!commandCond.startsWith("/")) commandCond = "/" + commandCond;
                if (command_ != null && (command_.startsWith(commandCond + " ") || command_.equals(commandCond))) {
                  match = true;
                  break;
                }
              }
              if (!match) continue;
            }

            if (conditions.containsKey("type") && !conditions.get("type").contains(type_ != null ? type_.toLowerCase() : null)) continue;
            if (conditions.containsKey("name") && !conditions.get("name").contains(name)) continue;

            long time = log.getLong(key + ".time");

            boolean hasLocation = log.contains(key + ".location");
            double x = 0, y = 0, z = 0;
            String world = "";
            if (hasLocation) {
              x = log.getDouble(key + ".location.x");
              y = log.getDouble(key + ".location.y");
              z = log.getDouble(key + ".location.z");
              if (log.contains(key + ".location.world")) world = log.getString(key + ".location.world");
            }

            String str = "";
            str += ChatColor.AQUA + name;
            str += " " + ChatColor.WHITE + command_;
            str += " " + ChatColor.LIGHT_PURPLE + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(time));
            if (hasLocation) {
              str += ChatColor.GREEN;
              str += " X:" + String.format("%.2f", x);
              str += " Y:" + String.format("%.2f", y);
              str += " Z:" + String.format("%.2f", z);
              if (!world.equals("")) str += " " + world;
            }
            results.add(str);
          }
        }

        int linesPerPage = CommandLogger.plugin.config.getConfig().getInt("lines-per-page");
        if (linesPerPage <= 0) linesPerPage = 10;
        int maxPage = (int)Math.ceil((double)results.size() / linesPerPage);
        int page = 1;
        if (conditions.containsKey("page")) try { page = Integer.parseInt(conditions.get("page").get(0)); } catch (Exception e) {}
        if (page < 1) page = 1;
        if (page > maxPage) page = maxPage;

        sender.sendMessage(results.subList(Math.max(0, results.size() - page * linesPerPage), Math.max(0, results.size() - (page - 1) * linesPerPage)).toArray(new String[0]));
        ComponentBuilder msg = new ComponentBuilder("                    ");
        if (page > 1) msg.append("←").event(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
            "/commandlog search page=" + (page - 1) + " " + (args.length > 1 ?
                String.join(" ", Arrays.copyOfRange(args, 1, args.length)).replaceAll("(^| )page= ?[^ ]+($| )", " ") : "")));
        else msg.append(ChatColor.GRAY + "←");
        msg.append(" " + page + "/" + maxPage + " ").reset();
        if (page < maxPage) msg.append("→").event(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
            "/commandlog search page=" + (page + 1) + " " + (args.length > 1 ?
                String.join(" ", Arrays.copyOfRange(args, 1, args.length)).replaceAll("(^| )page= ?[^ ]+($| )", " ") : "")));
        else msg.append(ChatColor.GRAY + "→");
        sender.spigot().sendMessage(msg.create());
        return true;
      default:
        return false;
    }
  }

  List<String> matches(List<String> list, String value) { return matches(list, value, ""); }
  List<String> matches(List<String> list, String value, String prefix) { return matches(list.toArray(new String[0]), value, prefix); }
  List<String> matches(String[] list, String value) { return matches(list, value, ""); }
  List<String> matches(String[] list, String value, String prefix) {
    if (!prefix.equals("")) {
      for (int i = 0; i < list.length; i++) {
        list[i] = prefix + list[i];
      }
    }

    List<String> result = new ArrayList<>();
    if (value.matches(" *")) result = Arrays.asList(list);
    else {
      for (String item : list) {
        if (item.startsWith(value)) result.add(item);
      }
    }
    return result;
  }

  @Override
  public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
    List<String> result = new ArrayList<>();
    if (args.length == 1) {
      result = matches(new String[]{"search"}, args[0]);
    } else {
      switch (args[0]) {
        case "search":
          if (!(args[args.length - 1].contains("=") || args[args.length - 2].endsWith("="))) {
            result = matches(new String[]{"command=", "type=", "name=", "time=", "page="}, args[args.length - 1]);
          } else {
            String type;
            boolean space;
            if (args[args.length - 1].contains("=")) {
              int split_index = args[args.length - 1].indexOf("=");
              type = args[args.length - 1].substring(0, split_index);
              space = false;
            } else {
              type = args[args.length - 2].substring(0, args[args.length - 2].length() - 1);
              space = true;
            }

            switch (type) {
              case "type":
                result = matches(new String[]{"player", "console"}, args[args.length - 1], space ? "" : "type=");
                break;
              case "name":
                List<String> list = Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
                list.add("CONSOLE");
                result = matches(list, args[args.length - 1], space ? "" : "name=");
                break;
              case "time":
                if (args[args.length - 1].equals("") || args[args.length - 1].endsWith("="))
                  result = matches(new String[]{"1","2","3","4","5","6","7","8","9"}, args[args.length - 1], args[args.length - 1]);
                else {
                  Matcher m = Pattern.compile("^([^=]+=)?[0-9]+").matcher(args[args.length - 1]);
                  result = matches(new String[]{"d"}, args[args.length - 1],
                      m.find() ? m.group() : "");
                }
                break;
              case "page":
                if (args[args.length - 1].equals("") || args[args.length - 1].endsWith("="))
                  result = matches(new String[]{"1","2","3","4","5","6","7","8","9"}, args[args.length - 1], args[args.length - 1]);
            }
          }
          break;
      }
    }
    return result;
  }
}
