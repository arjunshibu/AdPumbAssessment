import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

public class CommandFactory {

    // Factory method for creating ServerCommand
    public static Command createServerCommand(Socket socket) {
        return new ServerCommand(socket);
    }

    // Factory method for creating ClientCommand
    public static Command createClientCommand(Socket clientSocket, DataInputStream offshoreIn,
            DataOutputStream offshoreOut) {
        return new ClientCommand(clientSocket, offshoreIn, offshoreOut);
    }
}