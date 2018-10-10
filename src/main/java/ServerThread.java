import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

//https://github.com/yhassanzadeh13/java-tcp-socket-programming/blob/master/multi_thread_server-master/src/ServerThread.java
class ServerThread extends Thread
{
    protected BufferedReader is_command;
    protected DataOutputStream os_command;
    protected Socket commandSocket, dataSocket;
    private String line = new String();

    /**
     * Creates a server thread on the input socket
     *
     * @param commandSocket input socket to create a thread on
     */
    public ServerThread(Socket commandSocket)
    {
        this.commandSocket = commandSocket;
    }

    /**
     * The server thread, echos the client until it receives the QUIT string from the client
     */
    public void run()
    {
        System.out.println("New server thread is started");
        try
        {
            is_command = new BufferedReader(new InputStreamReader(commandSocket.getInputStream()));
            os_command = new DataOutputStream(commandSocket.getOutputStream());
            //Socket dataSocket = new Socket("local host", DriveQuickstart.DATA_PORT);

            while(true){
                line = is_command.readLine();
                System.out.println("Client " + commandSocket.getRemoteSocketAddress() + " sent : " + line);
                os_command.writeBytes("Hello, I am master\n");
            }



        }
        catch (IOException e) {
           e.printStackTrace();
        }

    }
}