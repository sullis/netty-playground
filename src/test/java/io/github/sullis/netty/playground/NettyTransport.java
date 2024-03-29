package io.github.sullis.netty.playground;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.ServerSocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.incubator.channel.uring.IOUring;
import io.netty.incubator.channel.uring.IOUringEventLoopGroup;
import io.netty.incubator.channel.uring.IOUringServerSocketChannel;

import java.util.Arrays;
import java.util.function.Supplier;
import java.util.stream.Stream;

public enum NettyTransport {
    NIO(() -> true, NioEventLoopGroup::new, NioServerSocketChannel.class),
    EPOLL(Epoll::isAvailable, EpollEventLoopGroup::new, EpollServerSocketChannel.class),
    IO_URING(IOUring::isAvailable, IOUringEventLoopGroup::new, IOUringServerSocketChannel.class);

    private static final NettyTransport[] ALL_VALUES = values();

    private final Supplier<Boolean> isAvailableSupplier;
    private final Supplier<EventLoopGroup> eventLoopGroupSupplier;
    private final Class<? extends ServerSocketChannel> serverSocketChannelClass;

    NettyTransport(Supplier<Boolean> isAvailable, Supplier<EventLoopGroup> eventLoopGroup, Class<? extends ServerSocketChannel> serverSocketClass) {
        this.isAvailableSupplier = isAvailable;
        this.eventLoopGroupSupplier = eventLoopGroup;
        this.serverSocketChannelClass = serverSocketClass;
    }

    public boolean isAvailable() {
        return isAvailableSupplier.get();
    }

    public Class<? extends ServerSocketChannel> getServerSocketChannelClass() {
        return this.serverSocketChannelClass;
    }

    public EventLoopGroup createEventLoopGroup() {
        return this.eventLoopGroupSupplier.get();
    }

    public static Stream<NettyTransport> availableTransports() {
        return Arrays.stream(ALL_VALUES)
                .filter(NettyTransport::isAvailable);
    }
}