package io.github.sullis.netty.playground;

import io.netty.channel.IoHandlerFactory;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollIoHandler;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioIoHandler;
import io.netty.channel.socket.ServerSocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.uring.IoUring;
import io.netty.channel.uring.IoUringIoHandler;
import io.netty.channel.uring.IoUringServerSocketChannel;

import java.util.Arrays;
import java.util.function.Supplier;
import java.util.stream.Stream;

public enum NettyTransport {
    NIO(() -> true, NioIoHandler::newFactory, NioServerSocketChannel.class),
    EPOLL(Epoll::isAvailable, EpollIoHandler::newFactory, EpollServerSocketChannel.class),
    IO_URING(IoUring::isAvailable, IoUringIoHandler::newFactory, IoUringServerSocketChannel.class);

    private static final NettyTransport[] ALL_VALUES = values();

    private final Supplier<Boolean> isAvailableSupplier;
    private final Supplier<IoHandlerFactory> ioHandlerFactorySupplier;
    private final Class<? extends ServerSocketChannel> serverSocketChannelClass;

    NettyTransport(Supplier<Boolean> isAvailable, Supplier<IoHandlerFactory> ioHandlerFactory, Class<? extends ServerSocketChannel> serverSocketClass) {
        this.isAvailableSupplier = isAvailable;
        this.ioHandlerFactorySupplier= ioHandlerFactory;
        this.serverSocketChannelClass = serverSocketClass;
    }

    public boolean isAvailable() {
        return isAvailableSupplier.get();
    }

    public Class<? extends ServerSocketChannel> getServerSocketChannelClass() {
        return this.serverSocketChannelClass;
    }

    public IoHandlerFactory createIoHandlerFactory() {
        return this.ioHandlerFactorySupplier.get();
    }

    public static Stream<NettyTransport> availableTransports() {
        return Arrays.stream(ALL_VALUES)
                .filter(NettyTransport::isAvailable);
    }
}