package panyi.xyz.nettyserver.heartbeat;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import panyi.xyz.nettyserver.simple.HelloServer;

public class HeartbeatServer {
    private int port;

    public HeartbeatServer(int p){
        this.port = p;
    }


    public void runLoop(){
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try{
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup , workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG , 128) //每个线程的最大连接数
                    .childOption(ChannelOption.SO_KEEPALIVE , true)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline().addLast("idleChecker" ,
                                    new IdleStateHandler(5,5,10));
                            socketChannel.pipeline().addLast("custom" , new IdleStateCustomHandler());
                        }
                    });

            ChannelFuture channelFuture = bootstrap.bind(port).sync();
            System.out.println("启动服务 port : " + port + " ...");
            channelFuture.channel().closeFuture().sync();
        }catch (Exception e){
            e.printStackTrace();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    private static class IdleStateCustomHandler extends ChannelInboundHandlerAdapter{
        @Override
        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
            System.out.println("userEventTriggered evt = " + evt);
        }
    }//end inner class

    public static void main(String[] args){
        new HeartbeatServer(7000).runLoop();
    }

}//end class
