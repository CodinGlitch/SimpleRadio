package com.codinglitch.simpleradio.central;

import java.util.Optional;

public interface ConfigHolder {
    <T> Optional<T> getEntry(String name);
}
