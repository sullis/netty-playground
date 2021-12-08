package io.github.sullis.netty.playground;

import static org.apache.commons.lang3.SystemUtils.IS_OS_LINUX;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.junit.jupiter.api.Test;

import io.netty.incubator.channel.uring.IOUring;

/**
 *
 *  Netty io_uring transport on Linux
 *
 */
public class IoUringTest {
    @Test
    public void testUringTransport() {
        System.out.println("Linux: " + IS_OS_LINUX);
        if (IS_OS_LINUX) {
            final Throwable cause = IOUring.unavailabilityCause();
            if (cause != null) {
                System.err.println("unavailabilityCause: " + ExceptionUtils.getStackTrace(cause));
            }
        }
        System.out.println("IOUring.isAvailable: " + IOUring.isAvailable());
    }
}
