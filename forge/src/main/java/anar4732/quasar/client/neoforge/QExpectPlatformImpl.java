package anar4732.quasar.client.neoforge;

import anar4732.quasar.api.QCompanionNetworkManager;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.network.registration.NetworkRegistry;

public class QExpectPlatformImpl {
	public static boolean shouldSendPacket() {
		return NetworkRegistry.hasChannel(Minecraft.getInstance().getConnection(), QCompanionNetworkManager.PACKET_ID_C2S);
	}
}