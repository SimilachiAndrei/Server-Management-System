package cockpit.motherNode.Utility;
import java.net.InetAddress;
import java.net.UnknownHostException;

public final class IpAddressUtil {

    private IpAddressUtil() {
    }

    // Convert String to InetAddress
    public static InetAddress stringToInetAddress(String ip) throws UnknownHostException {
        return InetAddress.getByName(ip);
    }

    // Convert InetAddress to String
    public static String inetAddressToString(InetAddress address) {
        return address.getHostAddress();
    }
}

