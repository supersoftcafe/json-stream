package com.supersoftcafe.json_stream;

import java.io.IOException;
import java.io.InputStream;

@FunctionalInterface
public interface SupplyInputStream<T extends InputStream> {
    T get() throws IOException;
}
