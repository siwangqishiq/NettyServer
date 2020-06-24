package panyi.xyz.nettyserver.stringcodec;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.CharsetUtil;
import panyi.xyz.nettyserver.simple.HelloServer;

public class StringCodecServer {
    private int port;

    public StringCodecServer(int p){
        this.port = p;
    }

    public void runLoop(){
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try{
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup , workerGroup)//
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG , 128) //每个线程的最大连接数
                    .childOption(ChannelOption.SO_KEEPALIVE , true)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            ChannelPipeline pipeline = socketChannel.pipeline();
                            pipeline.addLast(new StringEncoder());
                            pipeline.addLast(new StringDecoder());
                            pipeline.addLast(new StringBackHandler());
                        }
                    });

            System.out.println("启动服务 port : " + port + " ...");
            //绑定端口  启动服务
            ChannelFuture cf = bootstrap.bind(port).sync();

            //结束
            cf.channel().closeFuture().sync();
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    public static class StringBackHandler extends SimpleChannelInboundHandler {
        @Override
        protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
            System.out.println("received " + msg);
            ctx.writeAndFlush(Unpooled.copiedBuffer("Killer man Fuck Ali 干死阿里".getBytes(CharsetUtil.UTF_8)));
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            System.out.println("on exception : " + cause);
            ctx.close();
        }
    }

    public static void main(String args[]){
        new StringCodecServer(2002).runLoop();
    }

}//end class
