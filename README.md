Gravity
===

Gravity is a work in progress game about escaping from a black hole.

# Download

[Release](http:///data.rolandoislas.com/gravity/release/Gravity.jar)
[Dev](http:///data.rolandoislas.com/gravity/release/Gravity-dev.jar)

Requires Java 8 or greater.

Port 48051 must be forwarded for multiplayer support.

# Screenshots

[Gameboard](http://data.rolandoislas.com/gravity/image/screenshot/gameboard.png)

# How to Play

## Movement Pieces

Movement pieces are what determine how the player will move in a turn. Each player will start with 6 movement pieces 
and will choose one to play every turn.

### Types of Movement Pieces

Movement pieces have a string value in the following format "xx00y", where x is two randomly generated alphabetic 
characters, the zeros make up an integer, and y denotes the type of movement.

#### Types

- A : Attract : Moves the player to the closest ship
- R : Repulse : Moves the player away from the closest ship
- S : Shift : Moves all ships towards the player

## Turns

All turns are taken simultaneously, with players choosing one movement piece per turn. Once all players have chosen 
their movement, the movements will be executed alphabetically based on the first two characters of the movement piece.

### Rounds

After six rounds the player furthest from the black hole wins.

# Compiling from Source

A Gradle wrapper is included in the repo. Simply run `gradlew build` to get a jar in `./build/libs`

# Contributing

Want to contribute? Submit a pull request to the dev branch.
