package cockpit.motherNode.services;

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
    private CommandService commandService;
    private CpuUsageThread cpuUsageThread;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    public boolean initiateConnection(String ipAddress, int port) {
        executorService = Executors.newFixedThreadPool(1);

        commandService = new CommandService(ipAddress, port);
        commandService.connect();
        cpuUsageThread = new CpuUsageThread(ipAddress, port, messagingTemplate);

        executorService.submit(cpuUsageThread);
        System.out.println(commandService.isSuccess() + " " + cpuUsageThread.isSuccess());
        try {
            Thread.sleep(1);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return commandService.isSuccess() && cpuUsageThread.isSuccess();
    }

    public void disconnect() {
        if (commandService.getSocket() != null) {
            commandService.stop();
        }
        if (cpuUsageThread != null) {
            cpuUsageThread.stop();
        }
        if (executorService != null) {
            executorService.shutdownNow();
        }
    }

}
