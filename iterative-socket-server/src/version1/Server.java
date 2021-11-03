package version1;

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
        String preferredPort = "";

        System.out.println("\nWelcome to the iterative socket server!");
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

        label:
        while(true) {
            System.out.println("Waiting for client connection...");
            try (
                    Socket client = serverSocket.accept();
                    PrintWriter out = new PrintWriter(client.getOutputStream(), true);
                    BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                    ){


                System.out.println("Server connected to a client!");

                RequestType request = RequestType.valueOf(in.readLine());

                if (request == RequestType.Quit) break;

                System.out.println("Incoming " + request.toString() + " request from " + client.getInetAddress());

                switch (request.toString()) {
                    case "DateTime":
                        out.println(getDateAndTime());
                        break;
                    case "Uptime":
                        out.println(getUptime());
                        break;
                    case "MemoryUse":
                        out.println(getMemoryUse());
                        break;
                    case "Netstat":
                        out.println(getNetStat());
                        break;
                    case "CurrentUsers":
                        out.println(getCurrentUsers());
                        break;
                    case "RunningProcesses":
                        out.println(getRunningProcesses());
                        break;
                    case "Quit":
                        out.println("You chose to quit. Have a great day!\n");
                        break label;
                    default:
                        out.println("The option you entered is invalid, please try again.");
                        break;
                }
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

    public static String getDateAndTime() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        return dtf.format(now);
    }

    public static String getUptime() {
        return runCommandOnRuntime("uptime", "get current server uptime");
    }

//    public static String getUptime(long startTime) {
//        long endTime = System.nanoTime();
//        long timeElapsed = endTime - startTime;
//
//        return  "The connection has been active for " + timeElapsed / 1000000 + " milliseconds.";
//    }

    public static String getMemoryUse() {
        DecimalFormat df = new DecimalFormat("##.##");
        double totalMemory = Runtime.getRuntime().totalMemory();
        double freeMemory = Runtime.getRuntime().freeMemory();
        return "Total memory is: " + df.format(totalMemory / 1024) + " KB\n" + "Current use of memory is: " + df.format((totalMemory - freeMemory) / 1024) + " KB.";
    }

    public static String getNetStat() {
//        String result;
//
//        if(usingPortOtherThanDefault) {
//            result = runCommandOnRuntime("netstat -atn | grep ESTABLISHED | grep 127.0.0.1." + port, "list network connections");
//        } else {
//            result = runCommandOnRuntime("netstat -atn | grep ESTABLISHED | grep 127.0.0.1." + DEFAULT_PORT, "list network connections");
//        }
//
//        return result;
        return runCommandOnRuntime("netstat -a", "list network connections");
    }

    public static String getCurrentUsers() {
        return runCommandOnRuntime("who","list current users");
    }

    public static String getRunningProcesses() {
        return runCommandOnRuntime("ps -l", "list running processes");
    }

    public static String runCommandOnRuntime(String command, String activity) {
        Process process;
        String cmd = command, line = "", acc = "";

        try {
            process = Runtime.getRuntime().exec(cmd);
            BufferedReader commandLineReader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            while((line = commandLineReader.readLine()) != null) acc += line + "\n";

            process.destroy();
        } catch (IOException e) {
            System.out.println("Unable to run command to " + activity + " .\n");
        }

        return acc;
    }
}
