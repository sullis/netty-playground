package io.github.sullis.netty.playground;

import io.github.nettyplus.leakdetector.junit.NettyLeakDetectorExtension;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.junit.jupiter.api.Test;

import io.netty.channel.uring.IoUring;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;

/**
 *
 *  Netty io_uring transport on Linux
 *
 */
@ExtendWith(NettyLeakDetectorExtension.class)
public class IoUringTest {
    @Test
    @EnabledOnOs(value = OS.LINUX)
    public void ioUringIsAvailableOnLinux() {
        final Throwable cause = IoUring.unavailabilityCause();
        assertThat(cause).isNull();
        assertThat(IoUring.isAvailable()).isTrue();
    }
}
