package org.cobra.utils;

import org.cobra.networks.CobraSelector;
import org.junit.jupiter.api.Assertions;

import java.io.IOException;
import java.util.Random;
import java.util.function.Supplier;

public class TestUtils {
    private static final long DEFAULT_POOL_INTERVAL_MS = 100;
    public static final String LETTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    public static final String DIGITS = "0123456789";
    public static final String LETTERS_AND_DIGITS = LETTERS + DIGITS;

    public static String randString(int len) {
        Random rand = new Random();
        int boundRand = LETTERS_AND_DIGITS.length();
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < len; i++) {
            sb.append(LETTERS_AND_DIGITS.charAt(rand.nextInt(boundRand)));
        }
        return sb.toString();
    }

    public static void waitChannelReady(final CobraSelector selector, final String channel) throws IOException {
        int secLeft = 30;
        while (!selector.isReadyChannel(channel) && secLeft-- > 0)
            selector.poll(1_000L);

        Assertions.assertTrue(selector.isReadyChannel(channel), String.format("channel %s is not ready", channel));
    }

    public static void waitForCondition(final TestCondition conditional, long timeoutMs, String detail) throws Exception {
        waitForCondition(conditional, timeoutMs, () -> detail);
    }

    public static void waitForCondition(final TestCondition conditional, long timeoutMs,
                                        Supplier<String> conditionalDetailer) throws Exception {
        waitForCondition(conditional, timeoutMs, DEFAULT_POOL_INTERVAL_MS, conditionalDetailer);
    }

    public static void waitForCondition(final TestCondition conditional, long timeoutMs, long pollIntervalMs,
                                        Supplier<String> conditionalDetailer) throws Exception {
        retryOnExceptionWithTimeout(timeoutMs, pollIntervalMs, () -> {
            String conditionalDetail = conditionalDetailer != null ? conditionalDetailer.get() : null;
            Assertions.assertTrue(conditional.reachCondition(), String.format("condition not met; timeout = %s; " +
                    "detail = %s", timeoutMs, conditionalDetail));
        });
    }

    public static void retryOnExceptionWithTimeout(
            final long timoutMs,
            long pollIntervalMs,
            final UnitCallable callable) throws InterruptedException {
        long expectedEnd = System.currentTimeMillis() + timoutMs;

        while (true) {
            try {
                callable.call();
                return;
            } catch (AssertionError e) {
                if (expectedEnd <= System.currentTimeMillis()) {
                    throw e;
                }
            } catch (Exception e) {
                if (expectedEnd <= System.currentTimeMillis()) {
                    throw new AssertionError(String.format("assertion failed with an exception after %s ms", timoutMs));
                }
                // ignore, silently continue until timeout
            }
            Thread.sleep(Math.min(pollIntervalMs, timoutMs));
        }
    }
}
