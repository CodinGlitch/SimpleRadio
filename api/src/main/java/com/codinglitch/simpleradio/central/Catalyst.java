package com.codinglitch.simpleradio.central;

import com.codinglitch.simpleradio.radio.Message;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

public record Catalyst(ResourceLocation location, Item associate, float efficiency) {
    public Message acceptSource(Message message) {
        message.addPower(message.getPower() * (efficiency - 1));
        return message;
    }
}
