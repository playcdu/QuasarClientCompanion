package anar4732.quasar.client.mixin;

import anar4732.quasar.client.QCompanionMod;
import anar4732.quasar.client.gui.CDUHeader;
import anar4732.quasar.client.gui.CDUOnlineServerEntry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.multiplayer.ServerSelectionList;
import net.minecraft.client.multiplayer.ServerData;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerSelectionList.class)
public abstract class ServerSelectionListMixin extends ObjectSelectionList<ServerSelectionList.Entry> {
	@Shadow
	@Final
	private JoinMultiplayerScreen screen;
	
	@Unique
	private static final CDUHeader CDU_HEADER = new CDUHeader();
	
	protected ServerSelectionListMixin(Minecraft minecraft, int i, int j, int k, int l) {
		super(minecraft, i, j, k, l);
	}
	
	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/multiplayer/ServerSelectionList;clearEntries()V", shift = At.Shift.AFTER), method = "refreshEntries")
	public void addQEntries(CallbackInfo cir) {
		this.addEntry(CDU_HEADER);
		QCompanionMod.SERVERS.forEach(serverEntry -> {
			ServerData data = new ServerData(serverEntry.name, serverEntry.address, ServerData.Type.OTHER);
			this.addEntry(new CDUOnlineServerEntry((ServerSelectionList) (Object) this, screen, data));
		});
	}
}