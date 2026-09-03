package org.example.justdeepfried;

public class Main {
    static void main() {
        Loader loader = new Loader();
        loader.start(Main.class);
        SimpleServer.startServer(loader);
    }
}
