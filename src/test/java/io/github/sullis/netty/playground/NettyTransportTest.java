package io.github.sullis.netty.playground;

import io.netty.channel.IoHandlerFactory;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.uring.IoUring;
import io.netty.channel.uring.IoUringServerSocketChannel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
        assertTrue(IoUring.isAvailable());
        assertTrue(NettyTransport.IO_URING.isAvailable());
    }

    @Test
    @EnabledOnOs(value = { OS.LINUX } )
    public void linuxTransports() {
        assertEquals(Set.of(NettyTransport.NIO, NettyTransport.IO_URING, NettyTransport.EPOLL),
                NettyTransport.availableTransports().collect(Collectors.toSet()));
    }

    @Test
    @EnabledOnOs(value = { OS.MAC } )
    public void macTransports() {
        assertEquals(Set.of(NettyTransport.NIO),
                NettyTransport.availableTransports().collect(Collectors.toSet()));
    }

    @Test
    public void nioServerSocketChannelClass() {
        assertEquals(NioServerSocketChannel.class, NettyTransport.NIO.getServerSocketChannelClass());
    }

    @Test
    @EnabledOnOs(value = { OS.LINUX })
    public void epollServerSocketChannelClass() {
        assertEquals(EpollServerSocketChannel.class, NettyTransport.EPOLL.getServerSocketChannelClass());
    }

    @Test
    @EnabledOnOs(value = { OS.LINUX })
    public void ioUringServerSocketChannelClass() {
        assertEquals(IoUringServerSocketChannel.class, NettyTransport.IO_URING.getServerSocketChannelClass());
    }

    @Test
    public void nioCreateIoHandlerFactory() {
        IoHandlerFactory factory = NettyTransport.NIO.createIoHandlerFactory();
        assertNotNull(factory);
    }

    @Test
    @EnabledOnOs(value = { OS.LINUX })
    public void epollCreateIoHandlerFactory() {
        IoHandlerFactory factory = NettyTransport.EPOLL.createIoHandlerFactory();
        assertNotNull(factory);
    }

    @Test
    @EnabledOnOs(value = { OS.LINUX })
    public void ioUringCreateIoHandlerFactory() {
        IoHandlerFactory factory = NettyTransport.IO_URING.createIoHandlerFactory();
        assertNotNull(factory);
    }
}