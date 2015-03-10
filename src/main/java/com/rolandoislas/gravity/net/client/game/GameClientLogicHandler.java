package com.rolandoislas.gravity.net.client.game;

import com.rolandoislas.gravity.logic.MovementPiece;
import com.rolandoislas.gravity.net.common.NetUtil;
import com.rolandoislas.gravity.state.Game;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Rolando.
 */
public class GameClientLogicHandler extends ChannelInboundHandlerAdapter {

    private final Game game;

    public GameClientLogicHandler(Game game) {
        this.game = game;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        String message = NetUtil.byteBufToString((ByteBuf) msg);
        System.out.println("Client L: " + message);
        String code = message.equals("") ? "" : message.substring(0, 2);
        switch(code) {
            case GameClientDecoder.CODE_MOVEMNET_PIECES :
                setPlayerMovementPieces(message);
                break;
            case GameClientDecoder.CODE_TURN_START :
                startTurn();
                break;
            case GameClientDecoder.CODE_MOVE_PLAYER :
                movePlayer(message);
                break;
            case GameClientDecoder.CODE_NEUTRAL_SHIP_MOVE :
                moveNeutralShip(message);
                break;
            case GameClientDecoder.CODE_END_GAME :
                endGame(message);
                break;
            default :
                ctx.fireChannelRead(NetUtil.stringToByteBuf(message));
                break;
        }
    }

    private void endGame(String message) {
        int winningPlayer = Integer.parseInt(message.substring(2, 4));
        game.doEndGame(winningPlayer);
    }

    private void moveNeutralShip(String message) {
        int ship = Integer.parseInt(message.substring(2, 4));
        int location = Integer.parseInt(message.substring(4, 6));
        game.moveNeutralShip(ship, location);
    }

    private void movePlayer(String message) {
        int player = Integer.parseInt(message.substring(2, 4));
        int location = Integer.parseInt(message.substring(4, 6));
        game.movePlayer(player, location);
    }

    private void startTurn() {
        game.setAllPlayersTurning();
        game.setTurn(true);
    }

    private void setPlayerMovementPieces(String message) {
        List<MovementPiece> movementPieces = new ArrayList<>();
        for(int i = 0; i < 6; i++) {
            String code = message.substring(2 + i * 5, 7 + i * 5);
            MovementPiece mp = new MovementPiece(code);
            movementPieces.add(mp);
        }
        game.setPlayerMovementPieces(movementPieces);
    }

}
