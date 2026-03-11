package anar4732.quasar.client.mixin;

import anar4732.quasar.client.QCompanionMod;
import net.minecraft.client.multiplayer.ClientPacketListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public class ClientPacketListenerMixin {
	@Inject(at = @At("TAIL"), method = "handleLogin")
	public void handleLoginInjected(CallbackInfo cir) {
		QCompanionMod.onLogin();
	}
}