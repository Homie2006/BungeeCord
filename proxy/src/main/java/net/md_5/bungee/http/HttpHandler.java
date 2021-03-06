package net.md_5.bungee.http;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.LastHttpContent;
import java.nio.charset.Charset;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.Callback;

@RequiredArgsConstructor
public class HttpHandler extends SimpleChannelInboundHandler<HttpObject>
{

    private final Callback<String> callback;
    private final StringBuilder buffer = new StringBuilder();

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception
    {
        try
        {
            callback.done( null, cause );
        } finally
        {
            ctx.channel().close();
        }
    }

    @Override
    protected void messageReceived(ChannelHandlerContext ctx, HttpObject msg) throws Exception
    {
        if ( msg instanceof HttpResponse )
        {
            HttpResponse response = (HttpResponse) msg;
            if ( response.getStatus().code() != 200 )
            {
                throw new IllegalStateException( "Expected HTTP response 200 OK, got " + response.getStatus() );
            }
        }
        if ( msg instanceof HttpContent )
        {
            HttpContent content = (HttpContent) msg;
            buffer.append( content.content().toString( Charset.forName( "UTF-8" ) ) );

            if ( msg instanceof LastHttpContent )
            {
                try
                {
                    callback.done( buffer.toString(), null );
                } finally
                {
                    ctx.channel().close();
                }
            }
        }
    }
}
