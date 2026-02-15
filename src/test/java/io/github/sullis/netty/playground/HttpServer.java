/*
 * Copyright 2012 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package io.github.sullis.netty.playground;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.IoHandlerFactory;
import io.netty.channel.MultiThreadIoEventLoopGroup;
import io.netty.channel.socket.ServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.Arrays;

import static io.github.sullis.netty.playground.HttpUtil.NETTYLOG_NAME;

/**
 * An HTTP server that sends back the content of the received HTTP request
 * in a pretty plaintext form.
 */
public final class HttpServer {
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpServer.class);

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private int port = -1;
    private final NettyTransport transport;

    public static void main(String[] args) throws Exception {
        HttpServer server = new HttpServer();
        server.start();
    }

    public HttpServer() {
        this(NettyTransport.NIO);
    }

    public HttpServer(final NettyTransport... transports) {
        this.transport = Arrays.stream(transports)
                .filter(NettyTransport::isAvailable)
                .findFirst()
                .get();
    }

    public int getPort() {
        return this.port;
    }

    public NettyTransport getNettyTransport() {
        return this.transport;
    }

    public void start() throws Exception {
        LOGGER.info("starting server, with NettyTransport=" + this.transport);

        final SslContext sslCtx = HttpUtil.buildNettySslContext();

        final IoHandlerFactory ioHandlerFactory = this.transport.createIoHandlerFactory();

        // Configure the server.
        bossGroup = new MultiThreadIoEventLoopGroup(ioHandlerFactory);
        workerGroup = new MultiThreadIoEventLoopGroup(ioHandlerFactory);
        ServerBootstrap b = new ServerBootstrap();
        b.option(ChannelOption.SO_BACKLOG, 1024);
        b.group(bossGroup, workerGroup)
         .channel(this.transport.getServerSocketChannelClass())
         .handler(new LoggingHandler(NETTYLOG_NAME, LogLevel.INFO))
         .childHandler(new HttpServerInitializer(sslCtx));

        Channel ch = b.bind(0).sync().channel();

        InetSocketAddress localAddress = ((ServerSocketChannel) ch).localAddress();
        this.port = localAddress.getPort();

        // ch.closeFuture().sync();
    }

    public String getDefaultUrl() {
        return "https://127.0.0.1:" + this.getPort() + '/';
    }

    public void stop() {
        if (bossGroup != null) {
            bossGroup.shutdownGracefully();
        }
        if (workerGroup != null) {
            workerGroup.shutdownGracefully();
        }
    }
}
