package version2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class Server {

    private static final int DEFAULT_PORT = 3400;
    private static ArrayList<ClientHandler> clients = new ArrayList<>();

    public static void main(String[] args) throws IOException {
        BufferedReader keyboard = new BufferedReader(new InputStreamReader(System.in)); // reads user keyboard input
        ServerSocket serverSocket;
        String preferredPort = "", preferredAddress = "";

        System.out.println("\nWelcome to the concurrent socket server!");

        System.out.print("Please insert the port you'd like to connect to (enter -1 to connect to the default port): ");

        try {
            preferredPort = keyboard.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            if (Integer.parseInt(preferredPort) == -1) {
                serverSocket = new ServerSocket(DEFAULT_PORT);
            } else {
                serverSocket = new ServerSocket(Integer.parseInt(preferredPort));
            }

            System.out.println();
        } catch (NumberFormatException ex) {
            System.out.println("\nUnable to connect to the specified port, connecting to the default port...");
            serverSocket = new ServerSocket(DEFAULT_PORT);
        }

        while (true) {
            System.out.println("Waiting for client connection...");

            try {
                Socket client = serverSocket.accept();
                BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                System.out.println("Server connected to a client!");

                RequestType request = RequestType.valueOf(in.readLine());

                if (request == RequestType.Quit) break;

                new ServerHandler(client, in, request).start();
            } catch (IOException e) {
                System.err.println("IO Exception caught.");
                System.out.println(e.getMessage());
            } catch (NullPointerException e) {
                System.err.println("NullPointerException caught.");
                System.out.println(e.getMessage());
            }
        }
        serverSocket.close();
    }
}

