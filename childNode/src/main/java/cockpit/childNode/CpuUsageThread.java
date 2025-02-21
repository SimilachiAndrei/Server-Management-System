package cockpit.childNode;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.lang.management.ManagementFactory;
import com.sun.management.OperatingSystemMXBean;
import org.json.JSONException;
import org.json.JSONObject;

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

                File root = new File("/");
                long totalDiskSpace = root.getTotalSpace();
                long freeDiskSpace = root.getFreeSpace();
                long usedDiskSpace = totalDiskSpace - freeDiskSpace;

                // Calculate Disk usage percentage
                double diskUsage = (double) usedDiskSpace / totalDiskSpace * 100;

                // Format the data to be sent over the socket
                JSONObject json = new JSONObject();
                json.put("CPU Load",cpuLoad * 100);
                json.put("RAM usage",ramUsage);
                json.put("Total RAM", formatMemorySize(totalMemory));
                json.put("Used RAM",formatMemorySize(usedMemory));
                json.put("Free RAM", formatMemorySize(freeMemory));

                json.put("Disk Usage", diskUsage);
                json.put("Total Disk Space", formatMemorySize(totalDiskSpace));
                json.put("Used Disk Space", formatMemorySize(usedDiskSpace));
                json.put("Free Disk Space", formatMemorySize(freeDiskSpace));


                byte[] response = json.toString().getBytes();
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
        } catch (JSONException e) {
            throw new RuntimeException(e);
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
