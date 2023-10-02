package io.github.sullis.netty.playground;

import io.netty.channel.epoll.Epoll;
import io.netty.incubator.channel.uring.IOUring;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class NettyTransportTest {
    @Test
    public void nioIsAvailable() {
        assertTrue(NettyTransport.NIO.isAvailable());
    }

    @Test
    @EnabledOnOs(value = { OS.LINUX } )
    public void epollIsAvailableOnLinux() {
        assertTrue(Epoll.isAvailable());
        assertTrue(NettyTransport.EPOLL.isAvailable());
    }

    @Test
    @EnabledOnOs(value = { OS.LINUX } )
    public void ioUringIsAvailableOnLinux() {
        assertTrue(IOUring.isAvailable());
        assertTrue(NettyTransport.IO_URING.isAvailable());
    }
}