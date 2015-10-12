package com.supersoftcafe.json_stream;

import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import static org.junit.Assert.*;


public class BatteryTests {
    private InputStream open() throws IOException {
        return BatteryTests.class.getResourceAsStream("/test.json");
    }

    @Test
    public void testGetOne() throws Exception {
        // Given
        String json = "{\"field1\": 1, \"field2\": 2}";
        StringReader in = new StringReader(json);

        // When
        Long value = Parser.getOne(in, Long.class, "$.field2");

        // Then
        assertNotNull(value);
        assertEquals(2l, value.longValue());
    }

    @Test
    public void batteryOfTests() throws Exception {
        Parser.create()
                .when("$.metadata", new TypeRef<Map<String, Object>>(){}, value -> System.out.printf("Got map %s\n", value))
                .when("$.*[1:2].someAttr", TestBean.class, value -> System.out.printf("Got one %s %d\n", value.getValue1(), value.getValue2()))
                .when("$..puff", String.class, value -> System.out.printf("Puff %s\n", value))
                .parse(open());

        Map<String, Object> metadata = Parser.getOne(open(), new TypeRef<Map<String, Object>>(){}, "$.metadata");
        System.out.printf("Metadata %s\n", metadata);

        Parser.stream(open(), TestBean.class, "$.*[1,3,7].someAttr")
                .forEach(value -> System.out.printf("Second %s %d\n", value.getValue1(), value.getValue2()));
    }


    

    private InputStream openBig() throws IOException {
        return new GZIPInputStream(BatteryTests.class.getResourceAsStream("/big.json.gz"));
    }

    @Test @Ignore
    public void bigCountCards1() throws Exception {
        TypeRef<?> typeRef = new TypeRef<Map<String, Object>>() {};
        long count = Parser.stream(openBig(), typeRef, "$..cards[*]").count();
        System.out.printf("Count is %d\n", count);
    }

    @Test @Ignore
    public void bigCountCards2() throws Exception {
        TypeRef<?> typeRef = new TypeRef<Map<String, Object>>() {};
        long count = Parser.stream(openBig(), typeRef, "$..cards[*]").count();
        System.out.printf("Count is %d\n", count);
    }

    @Test @Ignore
    public void bigCountCards3() throws Exception {
        TypeRef<?> typeRef = new TypeRef<Map<String, Object>>() {};
        long count = Parser.stream(openBig(), typeRef, "$..cards[*]").count();
        System.out.printf("Count is %d\n", count);
    }
}