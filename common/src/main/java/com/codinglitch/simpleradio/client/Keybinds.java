package com.codinglitch.simpleradio.client;

import com.codinglitch.simpleradio.CommonSimpleRadio;
import com.codinglitch.simpleradio.CompatCore;
import com.codinglitch.simpleradio.compat.AccessoryCompat;
import com.codinglitch.simpleradio.core.networking.packets.ServerboundRadioUpdatePacket;
import com.codinglitch.simpleradio.core.networking.packets.ServerboundUseHandheldPacket;
import com.codinglitch.simpleradio.core.registry.items.TransceiverItem;
import com.codinglitch.simpleradio.platform.ClientServices;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class Keybinds {

	public static final List<Binding> BINDINGS = new ArrayList<>();

	public enum State {
		DOWN,
		UP,
		CLICK
	}

	public static class Binding {
		public final KeyMapping mapping;
		public final Consumer<State> handler;

		public boolean isDown = false;

		public Binding(KeyMapping mapping, Consumer<State> handler) {
			this.mapping = mapping;
			this.handler = handler;
		}

		public void down() {
			if (isDown) return;
			isDown = true;

			handler.accept(State.DOWN);
		}
		public void up() {
			if (!isDown) return;
			isDown = false;

			handler.accept(State.UP);
		}
		public void click() {
			handler.accept(State.CLICK);
		}
	}

	private static void handleHandheld(State state) {
		if (state == State.CLICK) return;
		if (!CompatCore.TRINKETS.isLoaded && !CompatCore.CURIOS.isLoaded) return;

		LocalPlayer player = Minecraft.getInstance().player;
		if (player == null) return;

		AccessoryCompat.setHandheld(player, state == State.DOWN);
		ClientServices.NETWORKING.sendToServer(new ServerboundUseHandheldPacket(state == State.DOWN));
	}

	// -------- Bindings -------- \\

	public static Binding HANDHELD = bind(new KeyMapping(
			"key.simpleradio.use_handheld",
			InputConstants.Type.KEYSYM,
			GLFW.GLFW_KEY_B,
			"key.categories.simpleradio.simpleradio"
	), Keybinds::handleHandheld);

	// -------- Internal -------- \\

	private static Binding bind(KeyMapping mapping, Consumer<State> handler) {
		Binding binding = new Binding(mapping, handler);
		BINDINGS.add(binding);
		return binding;
	}

	public static void register(Consumer<KeyMapping> registry) {
		if (CompatCore.TRINKETS.isLoaded() || CompatCore.CURIOS.isLoaded()) {
			registry.accept(HANDHELD.mapping);
		}
	}

	public static void processBinding(Binding binding) {
		KeyMapping mapping = binding.mapping;

		while (mapping.consumeClick()) binding.click();
		if (mapping.isDown()) {
			binding.down();
		} else {
			binding.up();
		}
	}
}
