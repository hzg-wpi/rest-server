package org.tango.web.server;

import com.google.common.base.Objects;
import fr.esrf.Tango.DevFailed;
import fr.esrf.Tango.DevVarLongStringArray;
import fr.esrf.TangoApi.Database;
import fr.esrf.TangoApi.DeviceInfo;
import fr.esrf.TangoApi.DeviceProxy;
import org.tango.client.ez.proxy.NoSuchCommandException;
import org.tango.client.ez.proxy.TangoProxy;
import org.tango.client.ez.proxy.TangoProxyException;

import javax.annotation.concurrent.ThreadSafe;
import java.util.Arrays;
import java.util.List;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 23.05.14
 */
@ThreadSafe
public class DatabaseDs {
    public static final String TANGO_DB = "tango.db";

    private final String host;
    private final String port;
    private final String tangoHost;
    private final TangoProxy proxy;

    public DatabaseDs(String host, String port, TangoProxy dbProxy) throws TangoProxyException{
        this.host = host;
        this.port = port;
        this.tangoHost = host + ":" + port;
        this.proxy = dbProxy;
    }

    public DeviceInfo getDeviceInfo(String devname) throws TangoProxyException {
        try {
            DevVarLongStringArray info = proxy.executeCommand("DbGetDeviceInfo", devname);
            DeviceInfo deviceInfo = new DeviceInfo(info);
            return deviceInfo;
        } catch (NoSuchCommandException e) {
            throw new AssertionError("Can not find DbGetDeviceInfo");
        }
    }

    public String getDeviceAddress(String devname) throws TangoProxyException {
        DeviceInfo info = getDeviceInfo(devname);
        return "tango://" + tangoHost + "/" + info.name;
    }

    public List<String> getDeviceList() throws TangoProxyException, NoSuchCommandException {
        String[] result = proxy.executeCommand("DbGetDeviceWideList", "*");
        return Arrays.asList(result);
    }

    public List<String> getDomainsList(String wildcard) throws TangoProxyException, NoSuchCommandException {
        String[] result = proxy.executeCommand("DbGetDeviceDomainList", wildcard);
        return Arrays.asList(result);
    }

    public List<String> getFamiliesList(String domain, String wildcard) throws TangoProxyException, NoSuchCommandException {
        String[] result = proxy.executeCommand("DbGetDeviceFamilyList", domain + "/" + wildcard);
        return Arrays.asList(result);
    }

    public List<String> getMembersList(String domain, String family, String wildcard) throws TangoProxyException, NoSuchCommandException {
        String[] result = proxy.executeCommand("DbGetDeviceMemberList", domain + "/" + family + "/" + wildcard);
        return Arrays.asList(result);
    }

    public List<String> getDeviceList(String wildcard) throws TangoProxyException, NoSuchCommandException {
        String[] result = proxy.executeCommand("DbGetDeviceWideList", wildcard);
        return Arrays.asList(result);
    }

    public String[] getInfo() throws TangoProxyException, NoSuchCommandException {
        return proxy.executeCommand("DbInfo");
    }

    public String getDbURL(){
        return "tango://" + tangoHost + "/" + proxy.getName();
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("proxy", proxy)
                .add("tangoHost", tangoHost)
                .toString();
    }

    public DeviceProxy toDeviceProxy(){
        return proxy.toDeviceProxy();
    }

    public String getName() {
        return proxy.getName();
    }

    public String getPort() {
        return port;
    }

    public String getHost() {
        return host;
    }

    public String getFullTangoHost() {
        return tangoHost;
    }

    public Database asDatabase() {
        return db;
    }
}
