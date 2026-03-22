package anar4732.quasar.client.mixin;

import anar4732.quasar.client.gui.CDUHeader;
import anar4732.quasar.client.gui.CDUOnlineServerEntry;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.multiplayer.ServerSelectionList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(JoinMultiplayerScreen.class)
public class JoinMultiplayerScreenMixin {
	@Shadow
	protected ServerSelectionList serverSelectionList;
	
	@Shadow
	private Button selectButton;
	
	@Shadow
	private Button editButton;
	
	@Shadow
	private Button deleteButton;
	
	@Inject(at = @At("TAIL"), method = "onSelectedChange")
	public void onSelectedChangeInject(CallbackInfo cir) {
		ServerSelectionList.Entry entry = this.serverSelectionList.getSelected();
		if (entry instanceof CDUOnlineServerEntry) {
			this.selectButton.active = true;
			this.editButton.active = false;
			this.deleteButton.active = false;
		} else if (entry instanceof CDUHeader) {
			this.selectButton.active = false;
			this.editButton.active = false;
			this.deleteButton.active = false;
		}
	}
}