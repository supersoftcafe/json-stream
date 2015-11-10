package com.supersoftcafe.json_stream;


import com.fasterxml.jackson.databind.ObjectReader;
import com.supersoftcafe.json_stream.impl.ParserImpl;


public class ParserFactory {
    public static Parser fromJackson(ObjectReader objectReader) {
        return new ParserImpl(objectReader);
    }
}
