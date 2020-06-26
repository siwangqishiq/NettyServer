package panyi.xyz.nettyserver.simple;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TaskQueueServer {
    public static final int PORT = 6668;

    public static final Logger logger = LoggerFactory.getLogger(TaskQueueServer.class);

    public static void main(String[] args) throws InterruptedException {
        NioEventLoopGroup bossGroup = new NioEventLoopGroup();//boss线程组
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();// worker线程组

        try{
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup , workerGroup)//
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG , 128) //每个线程的最大连接数
                    .childOption(ChannelOption.SO_KEEPALIVE , true)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline().addLast(new NettyServerHandler());
                        }
                    });

            logger.debug("启动服务 port : %d ..." , PORT);
            //System.out.println("启动服务 port : " + PORT + " ...");
            //绑定端口  启动服务
            ChannelFuture cf = bootstrap.bind(PORT).sync();

            //结束
            cf.channel().closeFuture().sync();
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    //handlers

    /**
     *
     */
    private static class NettyServerHandler extends ChannelInboundHandlerAdapter {

        /**
         * 读取客户端发送的消息
         * @param ctx
         * @param msg  客户端发来的数据
         * @throws Exception
         */
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            if(msg instanceof ByteBuf){
                //耗时任务 提交到eventLoop中调度运行
                ctx.channel().eventLoop().execute(()->{
                    try{
                        Thread.sleep(10 * 1000);
                        ByteBuf msgBuf = (ByteBuf)msg;
                        System.out.println(ctx.channel().remoteAddress() + " say : " + msgBuf.toString(CharsetUtil.UTF_8));

                        ctx.writeAndFlush(Unpooled.copiedBuffer("Hello My name is Netty Server 你好!".getBytes()));
                    } catch (Exception e){
                        e.printStackTrace();
                    }
                });


            }
        }

        @Override
        public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
            ctx.writeAndFlush(Unpooled.copiedBuffer("Hello My name is Netty Server 你好!".getBytes()));
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            ctx.close();
        }


    }//end inner class
}
