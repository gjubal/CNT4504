package version2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ServerHandler extends Thread {
    private final Socket client;
    private final BufferedReader in;
    private  final RequestType requestType;

    public ServerHandler(Socket client, BufferedReader in, RequestType requestType) throws IOException {
        this.client = client;
        this.in = in;
        this.requestType = requestType;
    }

    public void run() {
        try(
            PrintWriter out = new PrintWriter(client.getOutputStream(), true)
        ) {
            System.out.println("Incoming " + requestType.toString() + " request.");

            switch (requestType.toString()) {
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
                    break;
                default:
                    out.println("The option you entered is invalid, please try again.");
                    break;
            }

            in.close();
        } catch (IOException e) {
            System.err.println("IO exception in server handler.");
            e.printStackTrace();
        }
    }

    public static String getDateAndTime() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        return dtf.format(now);
    }

    public static String getUptime() {
        return runCommandOnRuntime("uptime", "get current server uptime");
    }


    public static String getMemoryUse() {
        DecimalFormat df = new DecimalFormat("##.##");
        double totalMemory = Runtime.getRuntime().totalMemory();
        double freeMemory = Runtime.getRuntime().freeMemory();
        return "Total memory is: " + df.format(totalMemory / 1024) + " KB\n" + "Current use of memory is: " + df.format((totalMemory - freeMemory) / 1024) + " KB.";
    }

    public static String getNetStat() {
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
        String line, acc = "";

        try {
            process = Runtime.getRuntime().exec(command);
            BufferedReader commandLineReader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            while((line = commandLineReader.readLine()) != null) acc += line + "\n";

            process.destroy();
        } catch (IOException e) {
            System.out.println("Unable to run command to " + activity + " .\n");
        }

        return acc;
    }
}
