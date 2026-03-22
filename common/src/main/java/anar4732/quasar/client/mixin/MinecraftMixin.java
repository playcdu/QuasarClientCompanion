package anar4732.quasar.client.mixin;

import anar4732.quasar.client.QCompanionMod;
import anar4732.quasar.client.gui.QChatScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
	@Shadow
	public abstract void setScreen(@Nullable Screen guiScreen);
	
	@Inject(at = @At("HEAD"), method = "openChatScreen", cancellable = true)
	private void openChatScreen(String defaultText, CallbackInfo ci) {
		if (QCompanionMod.shouldUseQChat()) {
			this.setScreen(new QChatScreen(defaultText));
			ci.cancel();
		}
		// this.minecraft.isBlocked(chatMessage.sender())
	}
}