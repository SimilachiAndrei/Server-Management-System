package cockpit.childNode;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.lang.management.ManagementFactory;
import com.sun.management.OperatingSystemMXBean;

public class CpuUsageThread implements Runnable {
    private Socket socket;

    public CpuUsageThread(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        OperatingSystemMXBean osMXBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();

        try{
            DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());

            while (socket.isConnected()) {
                // Get CPU load
                double cpuLoad = osMXBean.getSystemCpuLoad();

                // Get RAM usage
                long totalMemory = osMXBean.getTotalPhysicalMemorySize();
                long freeMemory = osMXBean.getFreePhysicalMemorySize();
                long usedMemory = totalMemory - freeMemory;

                // Get RAM usage percentage
                double ramUsage = (double) usedMemory / totalMemory * 100;

                // Format the data to be sent over the socket
                String data = String.format(
                        "CPU Load: %.2f%%,%nRAM Usage: %.2f%%,%nTotal RAM: %s,%nUsed RAM: %s,%nFree RAM: %s%n",
                        cpuLoad * 100,
                        ramUsage,
                        formatMemorySize(totalMemory),
                        formatMemorySize(usedMemory),
                        formatMemorySize(freeMemory)
                );

                byte[] response = data.getBytes();
                outputStream.writeInt(response.length);
                outputStream.write(response);
                outputStream.flush();

//                System.out.println(data);

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        } catch (IOException e) {
            System.out.println("Error handling CPU usage: " + e.getMessage());
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                System.out.println("Error closing socket: " + e.getMessage());
            }
        }
    }

    // Helper method to format memory size in a readable format
    private static String formatMemorySize(long size) {
        if (size < 1024) {
            return size + " B";
        } else if (size < 1024 * 1024) {
            return String.format("%.2f", (double) size / 1024) + " KB";
        } else if (size < 1024 * 1024 * 1024) {
            return String.format("%.2f", (double) size / (1024 * 1024)) + " MB";
        } else {
            return String.format("%.2f", (double) size / (1024 * 1024 * 1024)) + " GB";
        }
    }
}
