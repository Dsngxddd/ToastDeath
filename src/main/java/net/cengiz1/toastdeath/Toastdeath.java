package net.cengiz1.toastdeath;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Level;

public class Toastdeath extends JavaPlugin implements Listener {

    private Advancement toastAdvancement;

    @Override
    public void onEnable() {
        getLogger().info("Plugin enabled!");
        getServer().getPluginManager().registerEvents(this, this);

        // Başarımları kaydetmek için bir metod çağırabiliriz
        registerAdvancement();
    }

    @Override
    public void onDisable() {
        getLogger().info("Plugin disabled!");
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = event.getEntity().getKiller();

        if (killer != null) {
            sendToastMessage(killer, victim);
            grantAdvancement(killer);
        }
    }

    private void sendToastMessage(Player killer, Player victim) {
        String message = "Öldüren İsmi " + killer.getName() + " ölen ismi " + victim.getName();
        killer.sendMessage(message);
    }

    private void grantAdvancement(Player player) {
        if (toastAdvancement == null) {
            getLogger().warning("Advancement has not been registered yet!");
            return;
        }

        AdvancementProgress progress = player.getAdvancementProgress(toastAdvancement);
        if (!progress.isDone()) {
            for (String criteria : progress.getRemainingCriteria()) {
                progress.awardCriteria(criteria);
            }
            getLogger().info("Advancement granted to player " + player.getName() + ": " + toastAdvancement.getKey());

            // 3 saniye sonra advancement'ı kaldır
            new BukkitRunnable() {
                @Override
                public void run() {
                    revokeAdvancement(player);
                }
            }.runTaskLater(this, 60L);
        } else {
            getLogger().info("Player already has this advancement: " + toastAdvancement.getKey());
        }
    }

    private void revokeAdvancement(Player player) {
        AdvancementProgress progress = player.getAdvancementProgress(toastAdvancement);
        for (String criteria : progress.getAwardedCriteria()) {
            progress.revokeCriteria(criteria);
        }
        getLogger().info("Advancement revoked from player " + player.getName() + ": " + toastAdvancement.getKey());
    }

    private void registerAdvancement() {
        NamespacedKey key = new NamespacedKey(this, "kill_player");

        toastAdvancement = Bukkit.getAdvancement(key);
        if (toastAdvancement != null) {
            getLogger().info("Advancement already registered: " + key);
            return;
        }

        InputStream inputStream = getResource("data/toastdeath/advancements/kill_player.json");
        if (inputStream == null) {
            getLogger().warning("Advancement JSON resource not found!");
            return;
        }

        try (InputStreamReader reader = new InputStreamReader(inputStream)) {
            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
            Bukkit.getUnsafe().loadAdvancement(key, json.toString());
            toastAdvancement = Bukkit.getAdvancement(key);
            if (toastAdvancement != null) {
                getLogger().info("Advancement successfully registered: " + key);
            } else {
                getLogger().warning("Failed to register advancement: " + key);
            }
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Error loading advancement", e);
        }
    }
}
