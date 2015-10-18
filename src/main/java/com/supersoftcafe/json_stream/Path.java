package com.supersoftcafe.json_stream;


import java.util.List;


public interface Path extends List<Path.Node> {


    interface Node {
        boolean isArray();
        boolean isObject();
        String getName();
        long getIndex();
    }
}
