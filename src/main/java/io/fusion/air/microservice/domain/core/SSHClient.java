package io.fusion.air.microservice.domain.core;

import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.channel.ClientChannel;
import org.apache.sshd.client.channel.ClientChannelEvent;
import org.apache.sshd.client.session.ClientSession;
import org.apache.sshd.common.channel.Channel;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import java.io.ByteArrayOutputStream;
/**
 * @author: Araf Karsh Hamid
 * @version:
 * @date:
 */
public class SSHClient {

    private final String username;
    private final String password;
    private final int port;
    private final String host;
    private final long defaultTimeoutSeconds;

    private String command;

    // Store Results
    private ArrayList<CommandResults> resultArray;

    // SSH Client Objects --------------------------------------------
    private SshClient client;
    private ClientSession clientSession;
    private ByteArrayOutputStream responseStream;
    private ClientChannel channel;

    /**
     * Create the SSHClient Object
     *
     * @param _username
     * @param _password
     * @param _host
     * @param _port
     * @param _defaultTimeoutSeconds
     */
    public SSHClient(String _username, String _password,
                     String _host, int _port, long _defaultTimeoutSeconds) {
        username = _username;
        password = _password;
        host     = _host;
        port     = _port;
        defaultTimeoutSeconds = _defaultTimeoutSeconds;
        resultArray = new ArrayList<CommandResults>();
    }

    /**
     * Start the Client
     */
    public SshClient clientStart() {
        if(client == null) {
            System.out.println("Setting SSH Default Client");
            client = SshClient.setUpDefaultClient();
            client.start();
        }
        return client;
    }

    /**
     * Stop the Client
     */
    public void clientStop() {
        if(client != null) {
            closeChannel();
            closeSession();
            client.stop();
            client = null;
        }
    }

    /**
     * Returns ClientSession
     * @return
     * @throws IOException
     */
    public ClientSession getSession() throws IOException {
        return (clientSession == null) ? createSession() : clientSession;
    }

    /**
     * Create SSHClient Session
     * @return
     * @throws IOException
     */
    public ClientSession createSession() throws IOException {
        clientStart();
        clientSession = client.connect(username, getHost(), getPort())
                     .verify(defaultTimeoutSeconds, TimeUnit.SECONDS)
                     .getSession();
        clientSession.addPasswordIdentity(password);
        clientSession.auth().verify(defaultTimeoutSeconds, TimeUnit.SECONDS);
        return clientSession;
    }

    /**
     * Close Client Session
     * @throws IOException
     */
    public void closeSession() {
        System.out.println("Closing the Client Session...");
        if(clientSession != null) {
            try {
                clientSession.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            clientSession = null;
        }
    }

    /**
     * Get Client Channel
     * @return
     * @throws IOException
     */
    public ClientChannel getChannel() throws IOException {
        return (channel == null) ? createChannel() : channel;
    }

    /**
     * Create Client Channel
     * @return
     * @throws IOException
     */
    public ClientChannel createChannel() throws IOException {
        responseStream = new ByteArrayOutputStream();
        channel = getSession().createChannel(Channel.CHANNEL_SHELL);
        System.out.println("Create Client Channel");
        channel.setOut(responseStream);
        channel.open().verify(defaultTimeoutSeconds, TimeUnit.SECONDS);
        return channel;
    }

    public void closeChannel() {
        System.out.println("Closing the Client Channel...");
        if(channel != null) {
            try {
                channel.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            channel = null;
            try {
                responseStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            responseStream = null;
        }
    }

    /**
     * Execute the Command using SSHClient
     * @param command
     * @return
     * @throws IOException
     */
    public String executeCommand(String command) throws IOException {
        String outputString = "";
        System.out.println("Setting SSH Session "+username+"@"+ getHost() +":"+ getPort() +"/");
        ClientSession session = getSession();
        ClientChannel channel = getChannel();
        Date dt = new Date();
        OutputStream sendCmdStream = channel.getInvertedIn();
        String cmd = command + "\n";
        sendCmdStream.write(cmd.getBytes());
        sendCmdStream.flush();
        channel.waitFor(EnumSet.of(ClientChannelEvent.CLOSED),
                TimeUnit.SECONDS.toMillis(defaultTimeoutSeconds));
        outputString = responseStream.toString();
        resultArray.add(new CommandResults(command, outputString, dt));
        return outputString;
    }

    /**
     * For Testing ONLY
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {

        // testSSHClient1();
        testSSHClient2();
        // testSSHClient3();
    }

    public static void testSSHClient1() throws IOException {
        String command = "ls -la\n";
        SSHClient cl = new SSHClient("demo", "password", "test.rebex.net", 22, 10);
        cl.clientStart();
        String result = cl.executeCommand(command);
        cl.clientStop();
        System.out.println("Command Executed "+command);
        System.out.println("==Result=========================================================");
        System.out.println(result);
        System.out.println("=================================================================");
    }

    public static void testSSHClient2() throws IOException {
        // String[] commands = { "ls -la", "pwd", "ls -la readme.txt", "help"};
        String[] commands = { "ls -la"};
        SSHClient cl = new SSHClient("demo", "password", "test.rebex.net", 22, 10);
        for(String command : commands){
            cl.clientStart();
            String result = cl.executeCommand(command);
            cl.clientStop();
            /**
            System.out.println("Command Executed " + command);
            System.out.println("==Result=========================================================");
            System.out.println(result);
            System.out.println("=================================================================");
             */
        }
        int x = 1;
        for(CommandResults cr : cl.getResultArray()) {
            System.out.println("=================================================================");
            System.out.println(x+"> "+cr.getStartTime()+"|Command Executed " + cr.getCommand());
            System.out.println("==Result=========================================================");
            System.out.println(cr.getResult());
            System.out.println("==Elapsed Time = "+cr.getElapsedTime()+" (ms)===============================");
            System.out.println(cr.toJSONString());
            x++;
        }
    }
    /**
     * Test the SSH Client
     * @throws IOException
     */
    public static void testSSHClient3() throws IOException {
        String username = "demo";
        String password = "password";
        String host = "test.rebex.net";
        int port = 22;
        long defaultTimeoutSeconds = 10l;
        String command = "ls -la\n";
        System.out.println("Connecting to SSD Server");
        testCommand(username, password, host, port, defaultTimeoutSeconds, command);
        System.out.println("Done!");
    }

    /**
     * Execute the Command in the remote system
     * @param username
     * @param password
     * @param host
     * @param port
     * @param defaultTimeoutSeconds
     * @param command
     * @throws IOException
     */
    public static void testCommand(String username, String password,
                                   String host, int port, long defaultTimeoutSeconds, String command) throws IOException {

        System.out.println("Setting SSH Default Client");
        SshClient client = SshClient.setUpDefaultClient();
        client.start();

        System.out.println("Setting SSH Session "+username+"@"+host+":"+port+"/"+password);
        try (ClientSession session = client.connect(username, host, port)
                .verify(defaultTimeoutSeconds, TimeUnit.SECONDS).getSession()) {
            session.addPasswordIdentity(password);
            session.auth().verify(defaultTimeoutSeconds, TimeUnit.SECONDS);

            System.out.println("Setting Response Stream");
            try (ByteArrayOutputStream responseStream = new ByteArrayOutputStream();
                 ClientChannel channel = session.createChannel(Channel.CHANNEL_SHELL)) {
                System.out.println("Create Client Channel");
                channel.setOut(responseStream);
                try {
                    channel.open().verify(defaultTimeoutSeconds, TimeUnit.SECONDS);
                    try (OutputStream pipedIn = channel.getInvertedIn()) {
                        pipedIn.write(command.getBytes());
                        pipedIn.flush();
                    }

                    channel.waitFor(EnumSet.of(ClientChannelEvent.CLOSED),
                            TimeUnit.SECONDS.toMillis(defaultTimeoutSeconds));
                    String responseString = new String(responseStream.toByteArray());
                    System.out.println("=================================================");
                    System.out.println(responseString);
                    System.out.println("=================================================");
                } finally {
                    channel.close(false);
                }
            }
        } finally {
            client.stop();
        }
    }

    /**
     * Return Port Number
     * @return
     */
    public int getPort() {
        return port;
    }

    /**
     * Return the Host Name
     * @return
     */
    public String getHost() {
        return host;
    }

    /**
     * Returns the Results
     * @return
     */
    public ArrayList<CommandResults> getResultArray() {
        return resultArray;
    }
}
