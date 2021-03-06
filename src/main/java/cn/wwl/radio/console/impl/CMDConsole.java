package cn.wwl.radio.console.impl;

import cn.wwl.radio.console.GameConsole;
import cn.wwl.radio.network.SocketTransfer;

import java.util.Scanner;

public class CMDConsole implements GameConsole {

    @Override
    public void init() {
        System.out.println("System > CMDConsole Init.");
    }

    @Override
    public void printToConsole(String data) {
        System.out.println("System > " + data);
    }

    @Override
    public void printError(String data) {
        System.err.println("Error > " + data);
    }

    @Override
    public void redirectGameConsole(String data) {
        System.out.println("Game > " + data);
    }

    @Override
    public void startConsole() {
        printToConsole("CSGO Virtual Console.");
        printToConsole("Console ready. Enter everything will redirect to console.");
        Scanner scanner = new Scanner(System.in).useDelimiter("\n");
        while (!Thread.currentThread().isInterrupted()) {
            String next;
            try {
                next = scanner.next();
            } catch (Exception e) { //Ignore any Scanner exception, go Next;
                continue;
            }


            if (next.trim().isEmpty()) {
                continue;
            }

            SocketTransfer.getInstance().pushToConsole(next);
        }
    }

    @Override
    public void clear() {
        for (int i = 0; i < 10000; i++) {
            System.out.println("\t");
        }
    }

    @Override
    public void printException(Exception e) {
        e.printStackTrace();
    }
}
