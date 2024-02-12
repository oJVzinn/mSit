package me.meiallu.msit;

import org.bukkit.*;
import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.spigotmc.event.entity.EntityDismountEvent;

public final class MSit extends JavaPlugin implements Listener, CommandExecutor {
    String got_up = "&cVocê levantou do chão.";
    String got_down = "&aVocê sentou no chão.";
    String in_vehicle = "&c&lERRO! &cSaia do seu veículo para fazer isso.";
    String on_air = "&c&lERRO! &cVocê não pode fazer isso no ar.";
    String by_console = "&c&lERRO! &cApenas jogadores conseguem executar isso.";
    String wrong_world = "&c&lERRO! &cVocê não pode fazer isso aqui.";

    @Override
    public void onEnable() {
        this.getCommand("sit");
        getServer().getPluginManager().registerEvents(this, this);
    }

    public String printMsg(String msg) {
        return ChatColor.translateAlternateColorCodes('&', msg);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player) {
            Player p = (Player) sender;
            if( p.isInsideVehicle() ) {
                if (p.getVehicle() instanceof ArmorStand) {
                    ArmorStand as = (ArmorStand) p.getVehicle();
                    Location loc = p.getLocation();
                    loc.setY(loc.getY() + 0.575);
                    as.remove();

                    Bukkit.getScheduler().runTaskLater(this, () -> {
                        p.teleport(loc);
                    }, 1);

                    p.sendMessage( paintMsg(got_up) );
                } else { p.sendMessage( paintMsg(in_vehicle) ); }
            } else {
                if ( p.getWorld().getName().equals("build") || p.getWorld().getName().equals("lobby") ) {
                    Material b = p.getLocation().getBlock().getRelative(BlockFace.DOWN).getType();
                    if ( b.isSolid() && p.getLocation().getY() == Math.round( p.getLocation().getY() )) {
                        Location loc = p.getLocation();
                        if (b == Material.STEP || b == Material.WOOD_STEP || b == Material.STONE_SLAB2) {
                            if (b.getData() == org.bukkit.material.WoodenStep.class) {
                                loc.setY(loc.getY() - 0.175);
                            } else { loc.setY(loc.getY() - 0.675); }
                        } else { loc.setY(loc.getY() - 0.175); }
                        ArmorStand as = p.getWorld().spawn(loc, ArmorStand.class);
                        as.setVisible(false); as.setMarker(true); as.setSmall(true);
                        as.setPassenger(p);
                        p.sendMessage( paintMsg(got_down) );
                    } else { p.sendMessage( paintMsg(on_air) ); }
                } else { p.sendMessage( paintMsg(wrong_world) ); }
            }
        } else { sender.sendMessage( paintMsg(by_console) ); }
        return true;
    }

    @EventHandler
    public void onExit(EntityDismountEvent e) {
        if (e.getDismounted() instanceof ArmorStand) {
            ArmorStand as = (ArmorStand) e.getDismounted();
            Location loc = e.getEntity().getLocation();
            loc.setY(loc.getY() + 0.575);
            as.remove();

            Bukkit.getScheduler().runTaskLater(this, () -> {
                if ( as.getWorld() == e.getEntity().getWorld() ) {
                    e.getEntity().teleport(loc);
                }
            }, 1);

            e.getEntity().sendMessage(ChatColor.RED + "Você levantou do chão.");
        }
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent e) {
        if(e.getPlayer().isInsideVehicle())
            if (e.getPlayer().getVehicle() instanceof ArmorStand)
                e.getPlayer().getVehicle().remove();
    }

    @Override
    public void onDisable() {
        for (World w : Bukkit.getWorlds())
            for (Entity as : w.getEntities())
                if (as instanceof ArmorStand) as.remove();
    }
}
