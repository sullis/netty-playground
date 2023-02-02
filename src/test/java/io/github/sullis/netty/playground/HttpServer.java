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
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;

import static io.github.sullis.netty.playground.HttpServerUtil.NETTYLOG_NAME;

/**
 * An HTTP server that sends back the content of the received HTTP request
 * in a pretty plaintext form.
 */
public final class HttpServer {

    static final boolean SSL = System.getProperty("ssl") != null;

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private int port = -1;

    public static void main(String[] args) throws Exception {
        HttpServer server = new HttpServer();
        server.start();
    }

    public int getPort() {
        return this.port;
    }

    public void start() throws Exception {
        // Configure SSL.
        final SslContext sslCtx = HttpServerUtil.buildSslContext();

        // Configure the server.
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        ServerBootstrap b = new ServerBootstrap();
        b.option(ChannelOption.SO_BACKLOG, 1024);
        b.group(bossGroup, workerGroup)
         .channel(NioServerSocketChannel.class)
         .handler(new LoggingHandler(NETTYLOG_NAME, LogLevel.INFO))
         .childHandler(new HttpServerInitializer(sslCtx));

        Channel ch = b.bind(0).sync().channel();

        port = ((NioServerSocketChannel) ch).localAddress().getPort();

        System.err.println("Server: " +
                (SSL? "https" : "http") + "://127.0.0.1:" + this.port + '/');

        // ch.closeFuture().sync();
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
