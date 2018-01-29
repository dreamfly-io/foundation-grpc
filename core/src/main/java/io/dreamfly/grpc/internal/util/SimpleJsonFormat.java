package io.dreamfly.grpc.internal.util;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message.Builder;
import com.google.protobuf.MessageOrBuilder;
import com.google.protobuf.util.JsonFormat;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Utility classes to convert protobuf messages to/from JSON format.
 * <p>
 * This class warps the JsonFormat with default configuration and wraps the
 * InvalidProtocolBufferException as RuntimeException.
 */
public class SimpleJsonFormat {

    private static final JsonFormat.Parser defaultParser = JsonFormat.parser();
    private static final JsonFormat.Printer defaultPrinter = JsonFormat.printer();

    private SimpleJsonFormat() {
        // no instance
    }

    /**
     * convert json to message.
     *
     * @param jsonContent content in json format
     * @param message message builder
     */
    public static void json2message(String jsonContent, Builder message) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(jsonContent),
                "jsonContent should not be null or empty");
        checkNotNull(message, "message should not be null");
        try {
            defaultParser.merge(jsonContent, message);
        } catch (InvalidProtocolBufferException e) {
            throw new IllegalArgumentException("fail to parse json", e);
        }
    }

    /**
     * convert message to json.
     *
     * @param message message contains content
     * @return content in json format
     */
    public static String message2Json(MessageOrBuilder message) {
        checkNotNull(message, "message should not be null");
        try {
            return defaultPrinter.includingDefaultValueFields().print(message);
        } catch (InvalidProtocolBufferException e) {
            throw new IllegalArgumentException("fail to generate json", e);
        }
    }

}
