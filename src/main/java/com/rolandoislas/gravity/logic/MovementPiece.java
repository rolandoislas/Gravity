package com.rolandoislas.gravity.logic;

import java.util.Random;

/**
 * @author Rolando.
 */
public class MovementPiece {

    private final String alphabet = "abcdefghijklmnopqrstuvwxyz";
    private String alphaPrefix;
    private Types type;
    private int movementPlaces;

    public Types getType() {
        return type;
    }

    public int getPlaces() {
        return movementPlaces;
    }

    public String getAlphaPrefix() {
        return alphaPrefix;
    }

    public enum Types {
        REPULSE("r"),
        ATTRACT("a"),
        SHIFT("s");
        private String shortCode;
        private Types(String shortCode) {
            this.shortCode = shortCode;
        }
    }

    public MovementPiece(String code) {
        String alphaPrefix = code.substring(0, 2);
        int places = Integer.parseInt(code.substring(2, 4));
        String type = code.substring(4, 5);
        MovementPiece tmp = new MovementPiece(type, places, alphaPrefix);
        this.type = tmp.getType();
        this.movementPlaces = tmp.getPlaces();
        this.alphaPrefix = tmp.getAlphaPrefix();
    }

    public MovementPiece(String type, int movementPlaces, String alphaPrefix) {
        // Set type
        for(Types typeEnum : Types.values()) {
            if(typeEnum.shortCode.equalsIgnoreCase(type)) {
                this.type = typeEnum;
                break;
            }
        }
        // set movement places
        this.movementPlaces = movementPlaces;
        // set alpha prefix
        this.alphaPrefix = alphaPrefix;
    }

    public MovementPiece() {
        Random random = new Random();
        // Select random type
        int rTypeInt = random.nextInt(100);
        if(rTypeInt < 5) {
            this.type = Types.SHIFT;
        } else if(rTypeInt < 15) {
            this.type = Types.REPULSE;
        } else {
            this.type = Types.ATTRACT;
        }

        // Select random spaces to move
        int rMovementPlaces = random.nextInt(9) + 1;
        this.movementPlaces = rMovementPlaces;
        // Create random alpha prefix
        this.alphaPrefix = (alphabet.charAt(random.nextInt(alphabet.length())) + "").toUpperCase() + alphabet.charAt(random.nextInt(alphabet.length())) + "";
    }

    public String getCode() {
        String typeShortCode = type.shortCode;
        String places = String.format("%02d", movementPlaces);
        return alphaPrefix + places + typeShortCode;
    }

}
