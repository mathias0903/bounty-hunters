package me.Indyuce.bh.json;

import org.bukkit.craftbukkit.v1_10_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

import net.minecraft.server.v1_10_R1.PacketPlayOutChat;
import net.minecraft.server.v1_10_R1.PlayerConnection;
import net.minecraft.server.v1_10_R1.IChatBaseComponent.ChatSerializer;

public class Json_1_10_R1 implements Json {
	@Override
    public void sendMsg(Player p, String msg) {
		PacketPlayOutChat packet = new PacketPlayOutChat(ChatSerializer.a(msg));
		PlayerConnection co = ((CraftPlayer) p).getHandle().playerConnection;
		co.sendPacket(packet);
    }
}