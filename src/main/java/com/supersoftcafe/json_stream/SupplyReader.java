package com.supersoftcafe.json_stream;

import java.io.IOException;
import java.io.Reader;

@FunctionalInterface
public interface SupplyReader<T extends Reader> {
    T get() throws IOException;
}
