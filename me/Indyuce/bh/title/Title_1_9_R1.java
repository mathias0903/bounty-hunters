package me.Indyuce.bh.title;

import org.bukkit.craftbukkit.v1_9_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

import net.minecraft.server.v1_9_R1.IChatBaseComponent;
import net.minecraft.server.v1_9_R1.IChatBaseComponent.ChatSerializer;
import net.minecraft.server.v1_9_R1.PacketPlayOutChat;
import net.minecraft.server.v1_9_R1.PacketPlayOutTitle;
import net.minecraft.server.v1_9_R1.PacketPlayOutTitle.EnumTitleAction;

public class Title_1_9_R1 implements Title
{
	@Override
    public void sendTitle(Player player, String msgTitle, String msgSubTitle, int ticks)
    {
        IChatBaseComponent chatTitle = ChatSerializer.a("{\"text\": \"" + msgTitle + "\"}");
        IChatBaseComponent chatSubTitle = ChatSerializer.a("{\"text\": \"" + msgSubTitle + "\"}");
        PacketPlayOutTitle p = new PacketPlayOutTitle(EnumTitleAction.TITLE, chatTitle);
        PacketPlayOutTitle p2 = new PacketPlayOutTitle(EnumTitleAction.SUBTITLE, chatSubTitle);
        ((CraftPlayer)player).getHandle().playerConnection.sendPacket(p);
        ((CraftPlayer)player).getHandle().playerConnection.sendPacket(p2);
        sendTime(player, ticks);
    }
    private void sendTime(Player player, int ticks)
    {
    	PacketPlayOutTitle p = new PacketPlayOutTitle(EnumTitleAction.TIMES, null, 20, ticks, 20);
    	((CraftPlayer)player).getHandle().playerConnection.sendPacket(p);
    }
	@Override
    public void sendActionBar(Player player, String message)
    {
    	IChatBaseComponent cbc = ChatSerializer.a("{\"text\": \"" + message + "\"}");
    	PacketPlayOutChat ppoc = new PacketPlayOutChat(cbc, (byte) 2);
    	((CraftPlayer)player).getHandle().playerConnection.sendPacket(ppoc);
    }
}