import java.io.*;
import java.net.Socket;

public class RequestContext {
    private final String request;
    private final Socket clientSocket;
    private final DataInputStream proxyInputStream;
    private final DataOutputStream proxyOutputStream;
    private final DataOutputStream responseOutputStream;

    private RequestContext(Builder builder) {
        this.request = builder.request;
        this.clientSocket = builder.clientSocket;
        this.proxyInputStream = builder.proxyInputStream;
        this.proxyOutputStream = builder.proxyOutputStream;
        this.responseOutputStream = builder.responseOutputStream;
    }

    public String getRequest() {
        return request;
    }

    public Socket getClientSocket() {
        return clientSocket;
    }

    public DataInputStream getProxyInputStream() {
        return proxyInputStream;
    }

    public DataOutputStream getProxyOutputStream() {
        return proxyOutputStream;
    }

    public DataOutputStream getResponseOutputStream() {
        return responseOutputStream;
    }

    public static class Builder {
        private String request;
        private Socket clientSocket;
        private DataInputStream proxyInputStream;
        private DataOutputStream proxyOutputStream;
        private DataOutputStream responseOutputStream;

        public Builder withRequest(String request) {
            this.request = request;
            return this;
        }

        public Builder withClientSocket(Socket clientSocket) {
            this.clientSocket = clientSocket;
            return this;
        }

        public Builder withProxyInputStream(DataInputStream proxyInputStream) {
            this.proxyInputStream = proxyInputStream;
            return this;
        }

        public Builder withProxyOutputStream(DataOutputStream proxyOutputStream) {
            this.proxyOutputStream = proxyOutputStream;
            return this;
        }

        public Builder withResponseOutputStream(DataOutputStream responseOutputStream) {
            this.responseOutputStream = responseOutputStream;
            return this;
        }

        public RequestContext build() {
            return new RequestContext(this);
        }
    }
}