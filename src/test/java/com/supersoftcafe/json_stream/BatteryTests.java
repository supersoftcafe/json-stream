package com.supersoftcafe.json_stream;


import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.Map;
import java.util.Optional;
import java.util.zip.GZIPInputStream;

import static org.junit.Assert.*;


public class BatteryTests {
    private Parsers parsers;


    @Before public void before() throws Exception {
        this.parsers = Parsers.newInstance();
    }


    private InputStream open() throws IOException {
        return BatteryTests.class.getResourceAsStream("/test.json");
    }

    @Test
    public void testGetOne() throws Exception {
        // Given
        String json = "{\"field1\": 1, \"field2\": 2}";
        StringReader in = new StringReader(json);

        // When
        Long value = parsers.getOne(in, Long.class, "$.field2");

        // Then
        assertNotNull(value);
        assertEquals(2l, value.longValue());
    }

    @Test
    public void testFindFirstNull() throws Exception {
        // Given
        String json = "{\"field2\": null}";
        StringReader in = new StringReader(json);

        // When
        Optional<Boolean> value = parsers.getFirst(in, Boolean.class, "$.field2");

        // Then
        assertNotNull(value);
        assertFalse(value.isPresent());
    }

    @Test
    public void batteryOfTests() throws Exception {
        parsers.parser()
                .when("$.metadata", new TypeRef<Map<String, Object>>(){}, value -> System.out.printf("Got map %s\n", value))
                .when("$.*[1:2].someAttr", TestBean.class, value -> System.out.printf("Got one %s %d\n", value.getValue1(), value.getValue2()))
                .when("$..puff", String.class, value -> System.out.printf("Puff %s\n", value))
                .nestedSubTrees(true)
                .parse(open());

        Map<String, Object> metadata = parsers.getOne(open(), new TypeRef<Map<String, Object>>() {}, "$.metadata");
        System.out.printf("Metadata %s\n", metadata);

        parsers.stream(open(), TestBean.class, "$.*[1,3,7].someAttr")
                .forEach(value -> System.out.printf("Second %s %d\n", value.getValue1(), value.getValue2()));
    }




    private InputStream openBig() throws IOException {
        return new GZIPInputStream(BatteryTests.class.getResourceAsStream("/big.json.gz"));
    }

    @Test @Ignore
    public void bigCountCards1() throws Exception {
        TypeRef<?> typeRef = new TypeRef<Map<String, Object>>() {};
        long count = parsers.stream(openBig(), typeRef, "$..cards[*]").count();
        System.out.printf("Count is %d\n", count);
    }

    @Test @Ignore
    public void bigCountCards2() throws Exception {
        TypeRef<?> typeRef = new TypeRef<Map<String, Object>>() {};
        long count = parsers.stream(openBig(), typeRef, "$..cards[*]").count();
        System.out.printf("Count is %d\n", count);
    }

    @Test @Ignore
    public void bigCountCards3() throws Exception {
        TypeRef<?> typeRef = new TypeRef<Map<String, Object>>() {};
        long count = parsers.stream(openBig(), typeRef, "$..cards[*]").count();
        System.out.printf("Count is %d\n", count);
    }
}