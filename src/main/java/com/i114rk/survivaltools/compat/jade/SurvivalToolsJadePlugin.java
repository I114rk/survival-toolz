package com.i114rk.survivaltools.compat.jade;

import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

/**
 * Optional Jade integration. Jade finds this class through the {@code jade} entrypoint, so nothing
 * here is ever loaded when Jade is missing.
 */
@WailaPlugin
public class SurvivalToolsJadePlugin implements IWailaPlugin {
	@Override
	public void register(IWailaCommonRegistration registration) {
		registration.registerItemStorage(StoredMobItemStorage.INSTANCE, Object.class);
	}

	@Override
	public void registerClient(IWailaClientRegistration registration) {
		registration.registerItemStorageClient(StoredMobItemStorageClient.INSTANCE);
	}
}
