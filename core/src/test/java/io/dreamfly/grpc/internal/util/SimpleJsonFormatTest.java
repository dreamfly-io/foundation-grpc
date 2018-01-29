package io.dreamfly.grpc.internal.util;


import com.google.rpc.DebugInfo;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SimpleJsonFormatTest {

    @Test
    public void test() {
        // message to json
        DebugInfo debugInfo = DebugInfo.newBuilder().setDetail("detail-1234567").build();
        String json = SimpleJsonFormat.message2Json(debugInfo);
        System.out.printf("json=" + json);

        // json to message
        DebugInfo.Builder builder = DebugInfo.newBuilder();
        SimpleJsonFormat.json2message(json, builder);
        assertThat(builder.getDetail()).isEqualTo("detail-1234567");
    }

}