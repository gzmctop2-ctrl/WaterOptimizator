package com.wateroptimizer;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;

public class WaterOptimizerClient implements ClientModInitializer {
    private enum Phase { IDLE, LOOKING, SWITCHING, PLACING }

    private static Phase phase = Phase.IDLE;
    private static int waitTicks = 0;
    private static boolean menuKeyDown = false;
    private static boolean actionKeyDown = false;

    @Override
    public void onInitializeClient() {
        Config.load();
        ClientTickEvents.END_CLIENT_TICK.register(WaterOptimizerClient::tick);
    }

    private static void tick(MinecraftClient client) {
        ClientPlayerEntity player = client.player;
        if (player == null || client.world == null || client.interactionManager == null) {
            phase = Phase.IDLE;
            return;
        }

        long window = client.getWindow().getHandle();
        boolean menuNow = InputUtil.isKeyPressed(window, Config.menuKey);
        boolean actionNow = InputUtil.isKeyPressed(window, Config.actionKey);
        boolean free = client.currentScreen == null;

        if (free && menuNow && !menuKeyDown) {
            client.setScreen(new WaterScreen());
        } else if (free && actionNow && !actionKeyDown) {
            if (phase == Phase.IDLE) {
                phase = Phase.LOOKING;
            } else {
                phase = Phase.IDLE; // press again = cancel
            }
        }
        menuKeyDown = menuNow;
        actionKeyDown = actionNow;

        switch (phase) {
            case LOOKING -> {
                float pitch = player.getPitch();
                pitch = Math.min(90f, pitch + Config.degreesPerTick());
                player.setPitch(pitch);
                if (pitch >= 90f) {
                    phase = Phase.SWITCHING;
                }
            }
            case SWITCHING -> {
                int slot = findWaterBucket(player);
                if (slot < 0) {
                    player.sendMessage(Text.literal("WaterOptimizer: no water bucket in hotbar"), true);
                    phase = Phase.IDLE;
                    return;
                }
                if (player.getInventory().getSelectedSlot() != slot) {
                    player.getInventory().setSelectedSlot(slot);
                }
                waitTicks = 2; // let the slot change sync to the server
                phase = Phase.PLACING;
            }
            case PLACING -> {
                if (--waitTicks > 0) return;
                if (player.getMainHandStack().isOf(Items.WATER_BUCKET)) {
                    client.interactionManager.interactItem(player, Hand.MAIN_HAND);
                    player.swingHand(Hand.MAIN_HAND);
                }
                phase = Phase.IDLE;
            }
            default -> { }
        }
    }

    private static int findWaterBucket(ClientPlayerEntity player) {
        int current = player.getInventory().getSelectedSlot();
        if (player.getInventory().getStack(current).isOf(Items.WATER_BUCKET)) return current;
        for (int i = 0; i < 9; i++) {
            if (player.getInventory().getStack(i).isOf(Items.WATER_BUCKET)) return i;
        }
        return -1;
    }
}
