package anar4732.quasar.client;

import dev.architectury.injectables.annotations.ExpectPlatform;

public class QExpectPlatform {
	@ExpectPlatform
	public static boolean shouldSendPacket() {
		throw new AssertionError();
	}
}