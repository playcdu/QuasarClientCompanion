package anar4732.quasar.client.mixin;

import anar4732.quasar.client.gui.QChatComponent;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.components.ChatComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class GuiMixin {
	@Shadow
	public ChatComponent chat;
	
	@Inject(at = @At("TAIL"), method = "<init>")
	private void init(CallbackInfo cir) {
		this.chat = new QChatComponent();
	}
}