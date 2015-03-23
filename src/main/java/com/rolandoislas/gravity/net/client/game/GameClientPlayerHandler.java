package com.rolandoislas.gravity.net.client.game;

import com.rolandoislas.gravity.gui.PlayerPanelGame;
import com.rolandoislas.gravity.net.common.NetUtil;
import com.rolandoislas.gravity.state.Game;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * @author Rolando.
 */
public class GameClientPlayerHandler extends ChannelInboundHandlerAdapter {


    private Game game;

    public GameClientPlayerHandler(Game game) {
        this.game = game;
    }

    @Override
     public void channelRead(ChannelHandlerContext ctx, Object msg) {
        String message = NetUtil.byteBufToString((ByteBuf)(msg));
        System.out.println("Client: " + message);
        String code = message.equals("") ? "" : message.substring(0, 2);
        switch(code) {
            case GameClientDecoder.CODE_CONNECTION :
                setPlayerNumber(message);
                break;
            case GameClientDecoder.CODE_GAMESTATE :
                setGameState(message);
                break;
            case GameClientDecoder.CODE_STATUS_CHANGE :
                setStatus(message);
                break;
			case GameClientDecoder.CODE_ERROR :
				doError(message);
				break;
            default :
                ctx.fireChannelRead(NetUtil.stringToByteBuf(message));
                break;
        }
    }

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		cause.printStackTrace();
		switch (cause.getMessage()) {
			case "An existing connection was forcibly closed by the remote host" :
				ctx.close();
				game.doError("Lost connection to host.");
				break;
		}
	}

	private void doError(String message) {
		String errorMessage = null;
		for (GameClientDecoder.ERROR_CODE errorCode : GameClientDecoder.ERROR_CODE.values()) {
			if (message.substring(2, 4).equals(errorCode.code)) {
				errorMessage = errorCode.message;
				break;
			}
		}
		game.doError(errorMessage);
	}

	private void setStatus(String message) {
        int player = Integer.parseInt(message.substring(2, 4));
        int numberCode = Integer.parseInt(message.substring(4, 6));
        PlayerPanelGame.STATUSCODE code = null;
        for(PlayerPanelGame.STATUSCODE statusCode : PlayerPanelGame.STATUSCODE.values()) {
            if(statusCode.code == numberCode) {
                code = statusCode;
                break;
            }
        }
        game.setPlayerStatus(player, code);
    }

    private void setGameState(String message) {
        for(int i = 0; i < game.getTotalPlayers(); i++) {
            int player = Integer.parseInt(message.substring(2 + 4 * i, 4 + 4 * i));
            boolean status = Integer.parseInt(message.substring(4 + 4 * i, 6 + 4 * i)) != 0;
            PlayerPanelGame.STATUSCODE statusCode = status ? PlayerPanelGame.STATUSCODE.CONNECTED : PlayerPanelGame.STATUSCODE.CONNECTING;
            game.setPlayerStatus(player, statusCode);
        }
    }

    private void setPlayerNumber(String message) {
        game.setPlayerNumber(Integer.parseInt(message.substring(2, 4)));
    }

}
