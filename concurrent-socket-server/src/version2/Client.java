package version2;

import java.io.*;
import java.net.Socket;
import java.text.DecimalFormat;
import java.util.ArrayList;

public class Client {
    private static final int SERVER_PORT = 3400;

    public static void main(String[] args) throws IOException, InterruptedException {
        BufferedReader keyboard = new BufferedReader(new InputStreamReader((System.in))); // reads user keyboard input
        Socket socket;
        DecimalFormat df = new DecimalFormat("##.##");
        String preferredPort = "", command, preferredAddress = "";
        int nThreads, operationsRan = 0;

        System.out.println("\nWelcome to the concurrent socket client!");
        System.out.print("Please insert the address you'd like to connect to: ");

        try {
            preferredAddress = keyboard.readLine();
        } catch( IOException e) {
            e.printStackTrace();
        }

        System.out.print("Please insert the server port you'd like to connect to (enter -1 to connect to the default port): ");

        try {
            preferredPort = keyboard.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            if (Integer.parseInt(preferredPort) == -1) {
                socket = new Socket(preferredAddress, SERVER_PORT);
                preferredPort = "3400";
            } else {
                socket = new Socket(preferredAddress, Integer.parseInt(preferredPort));
            }

            System.out.println();
        } catch (NumberFormatException ex) {
            System.out.println("\nUnable to connect to the specified port, connecting to the default port...");
            socket = new Socket(preferredAddress, SERVER_PORT);
            preferredPort = "3400";
        }

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

           System.out.println();

           try {
               if(Integer.parseInt(command) > 0 && Integer.parseInt(command) < 8) {
                   RequestType reqType = RequestType.None;

                           switch (Integer.parseInt(command)) {
                               case 1:
                                   reqType = RequestType.DateTime;
                                   break;
                               case 2:
                                   reqType = RequestType.Uptime;
                                   break;
                               case 3:
                                   reqType = RequestType.MemoryUse;
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
                               default:
                                   reqType = RequestType.None;
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
                       threads[i] = new ClientHandler(preferredAddress, preferredPort, reqType, Integer.parseInt(preferredPort) != 8080, i + 1);
                   }

                   for(ClientHandler thread: threads) thread.start();

                   for(ClientHandler thread: threads) thread.join();

                   ArrayList<String> columns = new ArrayList<>();
                   double averageTime;

                   columns.add(reqType.name());

                   long turnAroundTime = 0;

                   for(int i = 0; i < ClientHandler.requestTotals.size(); i++) {
                       String currentThreadTime = ClientHandler.requestTotals.get(i).toString();

                       columns.add(currentThreadTime);
                       System.out.println("Time for request #" + (i+1) + ": " + currentThreadTime);
                       turnAroundTime += Long.parseLong(currentThreadTime);
                   }

                   averageTime = turnAroundTime / (double)nThreads;

                   columns.add("");
                   columns.add(String.valueOf(averageTime));
                   columns.add(String.valueOf(turnAroundTime));

                   operationsRan++;

                   // save data in .txt file
                   try (PrintWriter fileOut = new PrintWriter(String.format("%s-%s-%sThreads.txt", reqType.name(), operationsRan, nThreads))){
                       StringBuilder output = new StringBuilder();

                       for(int i = 0; i < columns.size(); i++) {
                           if(i == 0) output.append(String.format("%s %n", columns.get(i)));

                           int lastThree = columns.size() - 4;

                           if(i > 0 && i < lastThree) output.append(String.format("%s ", columns.get(i)));
                           if(i == lastThree) output.deleteCharAt(output.length() - 1);
                           if(i == columns.size() - 3) output.append("\n");
                           if(i == columns.size() - 2) output.append(String.format("Average response time: %sms.%n", columns.get(i)));
                           if(i == columns.size() - 1) output.append(String.format("Total turn-around time: %sms.%n", columns.get(i)));
                       }

                       fileOut.write(output.toString());
                   } catch(FileNotFoundException e) {
                       System.out.println("Couldn't create file: " + e.getMessage());
                   }

                   ClientHandler.requestTotals.clear();
                   System.out.println("\nTotal turn-around time: " + df.format(turnAroundTime) + "ms.");
                   System.out.println("Average response time: " + df.format(averageTime) + "ms.");
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
