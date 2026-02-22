package com.codinglitch.simpleradio.compat.etched;

import gg.moonflower.etched.api.record.PlayableRecord;
import gg.moonflower.etched.api.record.TrackData;
import net.minecraft.world.item.ItemStack;

public class EtchedCompat {
    public static String getSound(ItemStack record) {
        TrackData[] tracks = PlayableRecord.getStackMusic(record).orElse(null);
        if (tracks == null) return null;

        return tracks[0].url();
    }
}
