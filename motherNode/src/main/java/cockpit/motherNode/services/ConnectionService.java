package cockpit.motherNode.services;

import cockpit.motherNode.concurrency.CommandThread;
import cockpit.motherNode.concurrency.CpuUsageThread;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
@Getter
public class ConnectionService {

    private ExecutorService executorService;
    private CommandThread commandThread;
    private CpuUsageThread cpuUsageThread;

    private final SimpMessagingTemplate messagingTemplate;

    @Autowired
    public ConnectionService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public boolean initiateConnection(String ipAddress, int port, String name, String jwt) {
        executorService = Executors.newFixedThreadPool(2);

        commandThread = new CommandThread(ipAddress, port, name, jwt, messagingTemplate);
        cpuUsageThread = new CpuUsageThread(ipAddress, port, name, jwt, messagingTemplate);

        executorService.submit(commandThread);
        executorService.submit(cpuUsageThread);

        System.out.println(commandThread.isSuccess() + " " + cpuUsageThread.isSuccess());
        try {
            Thread.sleep(3);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return commandThread.isSuccess() && cpuUsageThread.isSuccess();
    }

    public void disconnect() {
        if (commandThread.getSocket() != null) {
            commandThread.stop();
        }
        if (cpuUsageThread != null) {
            cpuUsageThread.stop();
        }
        if (executorService != null) {
            executorService.shutdownNow();
        }
    }}
