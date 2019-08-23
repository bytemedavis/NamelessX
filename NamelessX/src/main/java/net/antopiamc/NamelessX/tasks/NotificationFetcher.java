package net.antopiamc.NamelessX.tasks;

import co.aikar.taskchain.TaskChain;
import net.antopiamc.NamelessX.NamelessPlugin;
import net.antopiamc.NamelessX.api.NamelessException;
import net.antopiamc.NamelessX.api.NamelessPlayer;
import net.antopiamc.NamelessX.api.Notification;
import net.antopiamc.NamelessX.utils.Message;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

public class NotificationFetcher extends BukkitRunnable {

    Player PLAYER;

    public NotificationFetcher(Player player){
        PLAYER = player;
    }

    @Override
    public void run(){
        TaskChain<?> chain = NamelessPlugin.getChainFactory().newChain();
        chain
                .delay(40)
                .async(() -> {
                    try {
                        NamelessPlayer nameless = NamelessPlugin.getInstance().api.getPlayer(PLAYER.getUniqueId());

                        if(!(nameless.exists())) {
                            chain.abort();
                            return;
                        }

                        if (!(nameless.isValidated())) {
                            chain.abort();
                            return;
                        }

                        // TODO Sort notifications by type

                        List<Notification> notifications = nameless.getNotifications();

                        if (notifications.size() == 0) {
                            PLAYER.sendMessage(Message.COMMAND_NOTIFICATIONS_OUTPUT_NONOTIFICATIONS.getMessage());
                            chain.abort();
                            return;
                        }

                        chain.setTaskData("notifications", notifications);
                    } catch (NamelessException e) {
                        PLAYER.sendMessage(Message.COMMAND_NOTIFICATIONS_OUTPUT_FAIL_GENERIC.getMessage());
                        e.printStackTrace();
                        chain.abort();
                        return;
                    }
                })
                .abortIfNull()
                .sync(() -> {
                    List<Notification> notifications = chain.getTaskData("notifications");
                    notifications.forEach((notification) -> {
                        BaseComponent[] message = new ComponentBuilder(notification.getMessage())
                                .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(Message.COMMAND_NOTIFICATIONS_OUTPUT_CLICKTOOPEN.getMessage()).create()))
                                .event(new ClickEvent(ClickEvent.Action.OPEN_URL, notification.getUrl()))
                                .create();
                        PLAYER.spigot().sendMessage(message);
                    });
                })
                .execute();
    }
}
