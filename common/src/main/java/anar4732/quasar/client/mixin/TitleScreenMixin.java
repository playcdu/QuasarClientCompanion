package anar4732.quasar.client.mixin;

import anar4732.quasar.client.QCompanionMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TitleScreen.class)
public class TitleScreenMixin {
	@Inject(at = @At("HEAD"), method = "mouseClicked")
	public void firstInteractionDetection(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
		if (QCompanionMod.firstInteraction) {
			QCompanionMod.firstInteraction = false;
			String title = QCompanionMod.CONFIG.modPackConfig.welcomeTitle;
			String message = QCompanionMod.CONFIG.modPackConfig.welcomeMessage;
			if (!title.isEmpty()) {
				QCompanionMod.LOGGER.info("Displaying welcome message with title '{}' and message '{}'", title, message);
				Minecraft mc = Minecraft.getInstance();
				mc.execute(() -> SystemToast.add(mc.getToasts(), SystemToast.SystemToastIds.PERIODIC_NOTIFICATION, Component.literal(title), Component.literal(message)));
			}
		}
	}
}