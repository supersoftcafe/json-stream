package com.supersoftcafe.json_stream;


import java.util.List;


public interface Path extends List<Path.Node> {


    interface Node {
        boolean isArray();
        long   getArrayIndex();

        boolean isAttribute();
        String getAttributeName();
    }
}
