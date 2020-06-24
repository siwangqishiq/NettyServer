package panyi.xyz.nettyserver;

import io.netty.util.NettyRuntime;

public class Hello {
    public static void main(String args[]){
        System.out.println(NettyRuntime.availableProcessors() );
    }
}
