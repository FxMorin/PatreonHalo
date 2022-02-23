package ca.fxco.patreonhalo;

import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public final class PatreonHalo extends JavaPlugin implements CommandExecutor {

    //  (ง^ᗜ^)ง  Look mom I put everything in one file!    ಠ_ಠ You WHAT!!  (╯ಠ▃ಠ)╯︵ ┻━┻

    private final FileConfiguration config = getConfig();
    private BukkitRunnable runnable = null;
    private BukkitTask task = null;
    private Set<String> tierNames;
    private final List<Tier> tiers = new ArrayList<>();
    private final static String PREFIX = "§8[§cP§eH§8]§r ";

    private void createTask() {
        int updateTicks = config.getInt("update_th_tick", 2);
        runnable = new BukkitRunnable() {
            @Override
            public void run() {
                for(Player p : Bukkit.getServer().getOnlinePlayers()) {
                    for (Tier tier : tiers) {
                        if (p.hasPermission(tier.permission)) {
                            World world = p.getWorld();
                            Location loc = p.getEyeLocation();
                            loc.add(0, tier.yOffset, 0);
                            for (int i = 0; i < tier.particleCount; i++) {
                                double angle = 2 * Math.PI * i / tier.particleCount;
                                double x = Math.cos(angle) * tier.radius, z = Math.sin(angle) * tier.radius;
                                loc.add(x, 0, z);
                                world.spawnParticle(
                                        Particle.REDSTONE,
                                        loc,
                                        1,
                                        0,
                                        0,
                                        0,
                                        tier.data
                                );
                                loc.subtract(x, 0, z);
                            }
                            break;
                        }
                    }
                }
            }
        };
        task = runnable.runTaskTimer(this, 0L, updateTicks);
    }


    @Override
    public void onEnable() {
        ConfigurationSection tiersSection = config.getConfigurationSection("Tiers");
        if(tiersSection == null) tiersSection = config.createSection("Tiers");
        config.addDefault("update_th_tick", 2);
        config.options().copyDefaults(true);
        saveConfig();
        tierNames = tiersSection.getKeys(false);
        for (String name : tierNames) {
            tiers.add(new Tier(
                    name,
                    config.getDouble("Tiers." + name + ".halo_y_offset", 0.5D),
                    config.getInt("Tiers." + name + ".particle_count", 20),
                    (float) config.getDouble("Tiers." + name + ".halo_radius", 0.3D),
                    new Particle.DustOptions(
                            config.getColor("Tiers." + name + ".color", Color.YELLOW),
                            (float) config.getDouble("Tiers." + name + ".particle_size", 0.7D)
                    )
            ));
        }
        createTask();
        this.getCommand("patreonhalo").setExecutor(this);
    }


    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (player.hasPermission("PatreonHalo.command")) {
                if (args.length >= 1) {
                    if (Objects.equals(args[0], "update_th_tick")) {
                        if (args.length == 2) {
                            try {
                                int updateThTicks = Integer.parseInt(args[1]);
                                if (updateThTicks <= 0 || updateThTicks > 60) {
                                    sender.sendMessage(PREFIX+"Delay must be between 0-60");
                                } else {
                                    config.set("update_th_tick",updateThTicks);
                                    runnable.cancel();
                                    task.cancel();
                                    runnable = null;
                                    task = null;
                                    createTask();
                                    sender.sendMessage(PREFIX+"Changed update_th_tick to: "+updateThTicks);
                                    return true;
                                }
                            } catch(NumberFormatException err) {
                                sender.sendMessage(PREFIX+"Please ender a valid number");
                            }
                        } else {
                            sender.sendMessage(PREFIX+"Command Usage: /patreonhalo update_th_tick <delayInTicks>");
                        }
                        return false;
                    } else {
                        for (Tier tier : tiers) {
                            if (Objects.equals(args[0], tier.name)) {
                                if (args.length == 3) {
                                    boolean success = true;
                                    boolean setDefault = Objects.equals(args[2], "default");
                                    try {
                                        switch (args[1]) {
                                            case "halo_y_offset":
                                                tier.yOffset = setDefault ? 0.5D : Double.parseDouble(args[2]);
                                                config.set("Tiers."+tier.name+".halo_y_offset",tier.yOffset);
                                                break;
                                            case "halo_radius":
                                                tier.radius = setDefault ? 0.3F : Float.parseFloat(args[2]);
                                                config.set("Tiers."+tier.name+".halo_radius",tier.radius);
                                                break;
                                            case "particle_size":
                                                float size = setDefault ? 0.7F : Float.parseFloat(args[2]);
                                                tier.setData(tier.data.getColor(),size);
                                                config.set("Tiers."+tier.name+".particle_size",size);
                                                break;
                                            case "particle_count":
                                                tier.particleCount = setDefault ? 20 : Integer.parseInt(args[2]);
                                                config.set("Tiers."+tier.name+".particle_count",tier.particleCount);
                                                break;
                                            case "color":
                                                Color col = setDefault ?
                                                        Color.YELLOW :
                                                        Color.fromRGB(Integer.parseInt(args[2]));
                                                tier.setData(col,tier.data.getSize());
                                                config.set("Tiers."+tier.name+".color",col);
                                                break;
                                            default:
                                                success = false;
                                                break;
                                        }
                                    } catch(NumberFormatException err) {
                                        sender.sendMessage(PREFIX+"Please ender a valid number");
                                        success = false;
                                    }
                                    if (success) {
                                        sender.sendMessage(
                                                PREFIX+"Changed "+tier.name+"."+args[1]+" value to: "+args[2]
                                        );
                                        saveConfig();
                                        return true;
                                    }
                                }
                                sender.sendMessage(
                                        PREFIX+"Command Usage: /patreonhalo "+tier.name+" <option> <value>"
                                );
                                return false;
                            }
                        }
                    }
                }
                return true;
            }
            return false;
        }
        return true;
    }


    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        List<String> matches = new ArrayList<>();
        if (args.length == 1) {
            matches.add("update_th_tick");
            matches.addAll(tierNames);
        } else if (args.length == 2) {
            if (Objects.equals(args[0], "update_th_tick")) {
                matches.add("1");
                matches.add("2");
                matches.add("4");
            } else if (tierNames.contains(args[0])) {
                matches.add("halo_y_offset");
                matches.add("halo_radius");
                matches.add("particle_size");
                matches.add("particle_count");
                matches.add("color");
            }
        } else if (args.length == 3) {
            boolean valid = true;
            switch (args[1]) {
                case "halo_y_offset":
                    matches.add("0.25");
                    matches.add("0.5");
                    matches.add("0.75");
                    break;
                case "halo_radius":
                    matches.add("0.15");
                    matches.add("0.3");
                    matches.add("0.45");
                    break;
                case "particle_size":
                    matches.add("0.5");
                    matches.add("0.7");
                    matches.add("0.9");
                    break;
                case "particle_count":
                    matches.add("15");
                    matches.add("20");
                    matches.add("25");
                    break;
                case "color":
                    matches.add("0xFFFFFF");
                    break;
                default:
                    valid = false;
                    break;
            }
            if (valid) matches.add("default");
        }
        return matches;
    }


    @Override
    public void onDisable() {
        if (task != null) task.cancel();
    }


    public static class Tier {

        public final String name;
        public final String permission;
        public double yOffset;
        public int particleCount;
        public float radius;
        public final Particle.DustOptions data;

        public Tier(String tierName, double yOffset, int particleCount, float radius, Particle.DustOptions data) {
            this.name = tierName;
            this.permission = "PatreonHalo."+tierName;
            this.yOffset = yOffset;
            this.particleCount = particleCount;
            this.radius = radius;
            this.data = data;
        }

        public void setData(Color col, float size) {
            new Particle.DustOptions(col,size);
        }
    }
}
