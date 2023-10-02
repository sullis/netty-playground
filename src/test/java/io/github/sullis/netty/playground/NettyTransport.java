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

import java.util.function.Supplier;

public enum NettyTransport {

    NIO(() -> true, () -> new NioEventLoopGroup(), NioServerSocketChannel.class),
    EPOLL(() -> Epoll.isAvailable(), () -> new EpollEventLoopGroup(), EpollServerSocketChannel.class),
    IO_URING(() -> IOUring.isAvailable(), () -> new IOUringEventLoopGroup(), IOUringServerSocketChannel.class);

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
}