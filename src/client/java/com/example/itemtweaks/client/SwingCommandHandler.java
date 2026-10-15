package com.example.itemtweaks.client;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommands.literal;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.Minecraft;

/** {@code /swing} opens the in-game settings screen on the next client tick. */
public final class SwingCommandHandler {
	private SwingCommandHandler() {}

	public static void register() {
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
				dispatcher.register(literal("swing").executes(ctx -> {
					Minecraft client = ctx.getSource().getClient();
					client.execute(() -> client.gui.setScreen(new SwingSettingsScreen()));
					return 1;
				})));
	}
}
