package cockpit.motherNode.services;

import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class AppService {

    private ExecutorService executorService;
    private volatile boolean isConnected = false;

    public void connectToApp(String ipAddress, int port) {
        if (!isConnected) {
            // Connect to the app using the provided IP address and port
            // You can use a library like Apache Commons Net or Java NIO for socket communication

            // Start the threads for sending info and receiving commands
            executorService = Executors.newFixedThreadPool(3);
            executorService.submit(this::startInfoThread1);
            executorService.submit(this::startInfoThread2);
            executorService.submit(this::startCommandThread);

            isConnected = true;
        }
    }

    public void disconnectFromApp() {
        if (isConnected) {
            // Stop the threads and disconnect from the app
            executorService.shutdownNow();

            // Perform any necessary cleanup or closing of resources

            isConnected = false;
        }
    }

    private void startInfoThread1() {
        while (!Thread.currentThread().isInterrupted()) {
            // Retrieve the info from the app
//            String info = retrieveInfo1FromApp();

            // Send the info to the frontend
//            messagingTemplate.convertAndSend("/topic/info1", info);

            // Add a delay if needed
            try {
                Thread.sleep(1000);
            }
            catch (InterruptedException exception){
                //TODO
            }
        }
    }

    private void startInfoThread2() {
        // Similar to startInfoThread1()
    }

    private void startCommandThread() {
        while (!Thread.currentThread().isInterrupted()) {
            // Receive the command from the frontend
//            String command = receiveCommandFromFrontend();

            // Send the command to the app and receive the response
//            String response = sendCommandToApp(command);

            // Send the response back to the frontend
//            messagingTemplate.convertAndSend("/topic/command-response", response);
        }
    }

    // Implement the methods for retrieving info from the app, receiving commands from the frontend, and sending commands to the app
    // ...
}
