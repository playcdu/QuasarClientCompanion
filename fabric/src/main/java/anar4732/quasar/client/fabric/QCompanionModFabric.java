package anar4732.quasar.client.fabric;

import anar4732.quasar.client.QCompanionMod;
import net.fabricmc.api.ClientModInitializer;

public final class QCompanionModFabric implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
	    QCompanionMod.init();
    }
}