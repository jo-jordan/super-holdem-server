# Super Holdem Server
## Description
This is a server for the Super Holdem game. It is written in Kotlin 1.18.2. It designed to be run in a Docker container.

## How to run

### Docker
The easiest way to run the server is to use Docker. You can build the image with the following command:
```
docker build -t super-holdem-server .
```
Then you can run the server with the following command:
```
docker run -p 8887:8887 -p 8888:8888 super-holdem-server
```
The auth server will be available at http://localhost:8887.

websockets will be available at ws://localhost:8888.

## Bugs

1. check bet equal has no effect sometimes