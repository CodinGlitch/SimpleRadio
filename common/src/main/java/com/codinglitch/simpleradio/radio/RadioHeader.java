package com.codinglitch.simpleradio.radio;

import com.codinglitch.simpleradio.api.central.WorldlyPosition;
import com.codinglitch.simpleradio.core.registry.entities.Wire;

import java.util.ArrayList;
import java.util.UUID;

/**
 * A source containing a record of all visited wires.
 */
public class RadioHeader extends RadioSource {
    public ArrayList<UUID> record = new ArrayList<>();

    public boolean willShort(Wire wire) {
        UUID wireUUID = wire.getUUID();
        for (UUID uuid : record) {
            if (wireUUID.equals(uuid)) return true;
        }
        return false;
    }
    public void visit(Wire wire) {
        record.add(wire.getUUID());
    }

    public RadioHeader(WorldlyPosition location) {
        super();

        this.origin = location;
    }
}
