package net.anar4732.quasarClient.fabric;

import net.anar4732.quasarClient.QCompanionMod;
import net.fabricmc.api.ClientModInitializer;

public final class QCompanionModFabric implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
	    QCompanionMod.init();
    }
}