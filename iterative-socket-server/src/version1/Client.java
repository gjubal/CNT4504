package version1;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;

public class Client {
    private static final String SERVER_IP = "127.0.0.1";
    private static final int SERVER_PORT = 8080;

    public static void main(String[] args) throws IOException, InterruptedException {
        BufferedReader keyboard = new BufferedReader(new InputStreamReader((System.in))); // reads user keyboard input
        Socket socket;
        String preferredPort = "", command = "";
        int nThreads = 1, operationsRan = 0;

        System.out.println("\nWelcome to the iterative socket client!");
        System.out.print("Please insert the server port you'd like to connect to (enter -1 to connect to the default port): ");

        try {
            preferredPort = keyboard.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            if (Integer.parseInt(preferredPort) == -1) {
                socket = new Socket(SERVER_IP, SERVER_PORT);
                preferredPort = "8080";
            } else {
                socket = new Socket(SERVER_IP, Integer.parseInt(preferredPort));
            }

            System.out.println();
        } catch (NumberFormatException ex) {
            System.out.println("\nUnable to connect to the specified port, connecting to the default port...");
            socket = new Socket(SERVER_IP, SERVER_PORT);
            preferredPort = "8080";
        }

        BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

        System.out.println("Successfully connected!\n");

       do {
           System.out.println("1. Get current date and time on the server");
           System.out.println("2. Get server uptime");
           System.out.println("3. Get server memory usage");
           System.out.println("4. List connections to the server");
           System.out.println("5. List users currently connected to the server");
           System.out.println("6. List programs currently running on the server");
           System.out.println("7. Exit\n");
           System.out.print("Enter one of the commands above: ");

           command = keyboard.readLine();

//           out.println(command);
           System.out.println();

           try {
               if(Integer.parseInt(command) > 0 && Integer.parseInt(command) < 8) {
                   RequestType reqType = RequestType.Uptime;

                   switch(Integer.parseInt(command)) {
                       case 1:
                           reqType = RequestType.DateTime;
                           break;
                       case 2:
                           reqType = RequestType.Uptime;
                           break;
                       case 3:
                           reqType = RequestType.Memory;
                           break;
                       case 4:
                           reqType = RequestType.Netstat;
                           break;
                       case 5:
                           reqType = RequestType.CurrentUsers;
                           break;
                       case 6:
                           reqType = RequestType.RunningProcesses;
                           break;
                       case 7:
                           reqType = RequestType.Quit;
                           break;
                   }

                   out.println(reqType);

                   if(reqType == RequestType.Quit) {
                       System.out.println("You chose to quit. Have a great day!");
                       break;
                   }

                   System.out.print("How many client requests? ");
                   nThreads = Integer.parseInt(keyboard.readLine());
                   System.out.println();

                   ClientHandler[] threads = new ClientHandler[nThreads];

                   for (int i = 0; i < nThreads; i++) {
                       threads[i] = new ClientHandler(SERVER_IP, preferredPort, reqType, Integer.parseInt(preferredPort) != 8080, i + 1);
                   }

                   for(ClientHandler thread: threads) thread.start();

                   for(ClientHandler thread: threads) {
                       try {
                           thread.join();
                       } catch(InterruptedException e) {
                           System.out.println("Unable to join threads - " + e.getMessage() + "\n");
                       }
                   }
                   CSVParser parser = new CSVParser();
                   ArrayList<String> column = new ArrayList<String>();
                   double averageTime = 0;

                   column.add(reqType.name());

                   long turnAroundTime = 0;

                   for(int i = 0; i < ClientHandler.requestTotals.size(); i++) {
                       String currentThreadTime = ClientHandler.requestTotals.get(i).toString();

                       column.add(currentThreadTime);
                       System.out.println("Time for client request #" + (i+1) + ": " + currentThreadTime);
                       turnAroundTime += Long.parseLong(currentThreadTime);
                   }

                   averageTime = turnAroundTime / (double)nThreads;

                   column.add("");
                   column.add(String.valueOf(averageTime));
                   column.add(String.valueOf(turnAroundTime));

                   operationsRan++;
                   parser.export(column, String.format("%s-%s-%sThreads", reqType.name(), String.valueOf(operationsRan), String.valueOf(nThreads)));
                   ClientHandler.requestTotals.clear();
                   System.out.println("\nAverage response time: " + averageTime);
                   System.out.println("Total turn-around time: " + turnAroundTime);
                   System.out.println();
               }
           } catch (NumberFormatException e) {
               System.out.println("Incorrect command input.");
           }
       } while(true);

        socket.close();
        System.exit(0);
    }
}
