package com.k2view.broadway.actors.masking;

import com.k2view.broadway.actors.masking.format.FormatPreservingEncryption;
import com.k2view.broadway.model.Context;
import com.k2view.broadway.model.Data;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class FormatPreservingEncryptionTest {

    @Test
    public void basicTest() throws Exception {
        FormatPreservingEncryption encryptor = new FormatPreservingEncryption();

        String whiteListCharacters = "0123456789abcd";
        String value = "+1 (234) 456-789";

        Data input = Data.from("value", value, "action", "encrypt", "whiteListCharacters", whiteListCharacters);
        Data output = Data.from();
        Context context = MaskingTestUtils.getMockContext();

        encryptor.action(input, output, context);
        String valueEncrypted = output.string("value");

        assertNotEquals(value, output.get("value"));
        assertEquals("+", output.string("value").substring(0, 1));
        assertEquals(" (", output.string("value").substring(2, 4));
        assertEquals(") ", output.string("value").substring(7, 9));
        assertEquals("-", output.string("value").substring(12, 13));

        input.put("value", valueEncrypted, "action", "decrypt");
        encryptor.action(input, output, context);
        assertEquals(value, output.string("value"));
    }

}
