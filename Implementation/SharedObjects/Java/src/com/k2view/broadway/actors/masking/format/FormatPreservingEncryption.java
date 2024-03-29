package com.k2view.broadway.actors.masking.format;


import com.k2view.broadway.model.Actor;
import com.k2view.broadway.model.Context;
import com.k2view.broadway.model.Data;
import com.k2view.broadway.util.CalculateOnChange;
import com.k2view.fabric.common.Util;

import java.lang.reflect.Method;
import java.util.Base64;

public class FormatPreservingEncryption implements Actor {

    private static final String FF3_TWEAK = "CBD09280979564";
    private static final String FF3_KEY;
    static {
        try {
            Class<?> hasherClass = Class.forName("com.k2view.fabric.common.encryption.Hasher");

            Method getHasherMethod = hasherClass.getDeclaredMethod("strongest", boolean.class);
            Object hasher = getHasherMethod.invoke(hasherClass, true);

            Method hashMethod = hasherClass.getDeclaredMethod("hash", String[].class);
            Object hash = hashMethod.invoke(hasher, new Object[] {new String[] {FF3_TWEAK}});

            FF3_KEY = Base64.getEncoder().encodeToString((byte[]) hash).substring(0, 32);
        } catch (Exception e) {
            throw new RuntimeException("Can't create FF3 key using Fabric hasher.", e);
        }

    }

    private static final String ALPHABET = ("0123456789" +
            "abcdefghijklmnopqrstuvwxyz" +
            "ABCDEFGHIJKLMNOPQRSTUVWXYZ" +
            "\"'!?@#$%^&*(),.-_=+[]{}`~:;|\\/ ");

    private final transient CalculateOnChange<String, FF3Cipher> cipherCache = new CalculateOnChange<>();

    private FF3Cipher getCipher(String alphabet) throws Exception {
        return cipherCache.get(alphabet, a -> {
            if (Util.isEmpty(alphabet)) {
                return new FF3Cipher(FF3_KEY, FF3_TWEAK, ALPHABET);
            } else {
                return new FF3Cipher(FF3_KEY, FF3_TWEAK, alphabet);
            }
        });
    }

    @Override
    public void action(Data input, Data output, Context context) throws Exception {
        String value = input.string("value");
        String whiteList = input.string("whiteListCharacters");
        FF3Cipher cipher = getCipher(whiteList);

        String outputValue;
        FormatUtils.FormatAndValue unwrapped = FormatUtils.unwrap(whiteList, value);
        if ("encrypt".equals(input.string("action"))) {
            outputValue = cipher.encrypt(unwrapped.value());
        } else if ("decrypt".equals(input.string("action"))) {
            outputValue = cipher.decrypt(unwrapped.value());
        } else {
            throw new IllegalArgumentException("No action specified, the parameter 'action' is mandatory and should be 'encrypt' or 'decrypt'.");
        }

        output.put("value", FormatUtils.wrap(unwrapped.format(), outputValue));
        output.put("format", unwrapped.format());
    }
}
