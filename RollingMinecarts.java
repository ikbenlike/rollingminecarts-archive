import java.util.Collection;
import org.bukkit.entity.Entity;
import java.util.ArrayList;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.Chunk;
import java.util.Iterator;
import org.bukkit.event.Event;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.entity.PoweredMinecart;
import org.bukkit.entity.StorageMinecart;
import org.bukkit.entity.Minecart;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

// 
// Decompiled by Procyon v0.5.36
// 

public final class RollingMinecarts extends JavaPlugin implements Runnable, Listener
{
    public final void onEnable() {
        if (this.getConfig().getBoolean("Load chunks")) {
            this.getServer().getScheduler().scheduleSyncRepeatingTask((Plugin)this, (Runnable)this, 6000L, 6000L);
        }
        this.getServer().getPluginManager().registerEvents((Listener)this, (Plugin)this);
        this.getConfig().options().copyDefaults(true);
        this.saveConfig();
    }
    
    @EventHandler
    public final void p(final VehicleMoveEvent vehicleMoveEvent) {
        if (vehicleMoveEvent.getVehicle() instanceof Minecart) {
            if (vehicleMoveEvent.getVehicle() instanceof StorageMinecart) {
                ((Minecart)vehicleMoveEvent.getVehicle()).setSlowWhenEmpty(!this.getConfig().getBoolean("Affect storage carts"));
            }
            if (!(vehicleMoveEvent.getVehicle() instanceof StorageMinecart) && !(vehicleMoveEvent.getVehicle() instanceof PoweredMinecart)) {
                ((Minecart)vehicleMoveEvent.getVehicle()).setSlowWhenEmpty(!this.getConfig().getBoolean("Affect empty carts"));
            }
            if (this.getConfig().getBoolean("Load chunks")) {
                for (int i = -3; i <= 3; ++i) {
                    for (int j = -3; j <= 3; ++j) {
                        vehicleMoveEvent.getTo().getWorld().loadChunk(vehicleMoveEvent.getTo().getChunk().getX() + i, vehicleMoveEvent.getTo().getChunk().getZ() + j);
                    }
                }
            }
        }
    }
    
    public final void run() {
        final Iterator<World> iterator = (Iterator<World>)this.getServer().getWorlds().iterator();
        while (iterator.hasNext()) {
            Chunk[] loadedChunks;
            for (int length = (loadedChunks = iterator.next().getLoadedChunks()).length, i = 0; i < length; ++i) {
                final Chunk chunk = loadedChunks[i];
                final ChunkUnloadEvent chunkUnloadEvent = new ChunkUnloadEvent(chunk);
                this.getServer().getPluginManager().callEvent((Event)chunkUnloadEvent);
                if (!chunkUnloadEvent.isCancelled()) {
                    chunk.unload(true, true);
                }
            }
        }
    }
    
    public final boolean onCommand(final CommandSender commandSender, final Command command, final String s, final String[] array) {
        final ArrayList<Minecart> list = (ArrayList<Minecart>)new ArrayList<Entity>();
        final Iterator<World> iterator = this.getServer().getWorlds().iterator();
        while (iterator.hasNext()) {
            list.addAll((Collection<? extends Entity>)iterator.next().getEntitiesByClass((Class)Minecart.class));
        }
        if (command.getName().equals("countminecarts") && commandSender.hasPermission("rollingminecarts.count")) {
            commandSender.sendMessage(String.valueOf(list.size()) + " Minecart" + ((list.size() != 1) ? "s" : ""));
        }
        if (command.getName().equals("removeminecarts") && commandSender.hasPermission("rollingminecarts.remove")) {
            commandSender.sendMessage(String.valueOf(list.size()) + " Minecart" + ((list.size() != 1) ? "s" : "") + " removed");
            final Iterator<Object> iterator2 = (Iterator<Object>)list.iterator();
            while (iterator2.hasNext()) {
                iterator2.next().remove();
            }
        }
        if (command.getName().equals("removeemptycarts") && commandSender.hasPermission("rollingminecarts.remove")) {
            int i = 0;
            final Iterator<Entity> iterator3 = list.iterator();
            while (iterator3.hasNext()) {
                final Minecart minecart;
                if ((minecart = (Minecart)iterator3.next()).getPassenger() == null && !(minecart instanceof StorageMinecart) && !(minecart instanceof PoweredMinecart)) {
                    minecart.remove();
                    ++i;
                }
            }
            commandSender.sendMessage(String.valueOf(i) + " Minecart" + ((i != 1) ? "s" : "") + " removed");
        }
        return true;
    }
    
    @EventHandler
    public final void P(final ChunkUnloadEvent chunkUnloadEvent) {
        if (this.getConfig().getBoolean("Load chunks")) {
            for (int i = -3; i <= 3; ++i) {
                for (int j = -3; j <= 3; ++j) {
                    final Iterator<Minecart> iterator = chunkUnloadEvent.getWorld().getEntitiesByClass((Class)Minecart.class).iterator();
                    while (iterator.hasNext()) {
                        final Minecart minecart;
                        if (((minecart = iterator.next()).getVelocity().getX() != 0.0 || minecart.getVelocity().getZ() != 0.0) && minecart.getLocation().getChunk().getX() == i + chunkUnloadEvent.getChunk().getX() && minecart.getLocation().getChunk().getZ() == j + chunkUnloadEvent.getChunk().getZ()) {
                            chunkUnloadEvent.setCancelled(true);
                            return;
                        }
                    }
                }
            }
        }
    }
    
    public final void onDisable() {
    }
}
