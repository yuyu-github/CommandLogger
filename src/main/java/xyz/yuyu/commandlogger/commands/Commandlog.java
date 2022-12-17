package xyz.yuyu.commandlogger.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.sql.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Commandlog implements CommandExecutor, TabCompleter {
  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    return true;
  }

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
  };

  @Override
  public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
    List<String> result = new ArrayList<>();
    if (args.length == 1) {
      result = matches(new String[]{"search"}, args[0]);
    } else {
      switch (args[0]) {
        case "search":
          if (!(args[args.length - 1].contains("=") || args[args.length - 2].endsWith("="))) {
            result = matches(new String[]{"command=", "type=", "name=", "time="}, args[args.length - 1]);
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
                result = new ArrayList<String>(Bukkit.getOnlinePlayers().stream().map(i -> i.getName()).collect(Collectors.toList()));
                result.add("CONSOLE");
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
            }
          }
          break;
      }
    }
    return result;
  }
}
