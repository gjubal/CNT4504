package version1;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static version1.Server.*;

public class ClientHandler extends Thread {
    private Socket client;
    private ArrayList<ClientHandler> clients;
    private RequestType requestType;
    private BufferedReader in;
    private PrintWriter out;
    private String preferredPort;
    private boolean usingPortOtherThanDefault;

    public static ArrayList<Long> requestTotals = new ArrayList<Long>();

    public ClientHandler(String serverIp, String port, RequestType requestType, boolean isNotDefaultPort, int clientNumber) throws IOException {
        this.client = new Socket(serverIp, Integer.parseInt(port));
        this.requestType = requestType;
        in = new BufferedReader(new InputStreamReader(client.getInputStream()));
        out = new PrintWriter(client.getOutputStream(), true);
        preferredPort = port;
        usingPortOtherThanDefault = isNotDefaultPort;
        requestTotals = new ArrayList<Long>();
        this.setName("Client #" + clientNumber);
    }

    public void run() {
        long startTime = System.nanoTime(), totalTime;
        out.println(requestType.name());

        try {
            String line = "";

            if (requestType != RequestType.Quit) {
                String result = this.getName() + "%n%n";
                while((line = in.readLine()) != null) {
                    if (line.isEmpty()) continue;
                    result += String.format("%s %n", line);
                }
                System.out.printf(result + "%n");
            }

        }  catch (IOException e) {
            System.err.println("IO exception in client handler");
            System.out.println(e.getStackTrace());
        } finally {
            out.close();
            try {
                in.close();
                client.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        totalTime = (System.nanoTime() - startTime) / 1000000; // Total time to run thread in milliseconds
        requestTotals.add(totalTime);
    }
}