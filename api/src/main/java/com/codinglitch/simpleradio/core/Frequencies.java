package com.codinglitch.simpleradio.core;

import com.codinglitch.simpleradio.central.Frequency;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.Nullable;
import oshi.util.tuples.Pair;

import java.util.List;

public interface Frequencies {
    List<Frequency> get();

    Frequency get(String frequency, Frequency.Modulation modulation);
    Frequency getOrCreate(String frequency, Frequency.Modulation modulation);

    Frequency tryParse(String string);

    void add(Frequency frequency);
    void remove(Frequency frequency);

    @Nullable
    Frequency.Modulation modulationOf(String shorthand);

    boolean check(String frequency);

    String incrementFrequency(String frequency, int amount);

    List<Pair<Integer, Frequency>> getWithin(String frequency, Frequency.Modulation modulation, int distance);

    List<Integer> within(String frequency, Frequency.Modulation modulation, int distance);

    @Nullable
    Frequency fromTag(CompoundTag tag);
}
