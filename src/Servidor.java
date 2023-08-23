 import java.io.IOException;
import java.net.ServerSocket;
import java.util.List;
import java.util.LinkedList;
import java.util.Iterator;

public class Servidor{
    public static final int PORT = 4000;
    private ServerSocket serverSocket;
    private final List<ClienteSocket> clientes = new LinkedList<>();
    String usuario;

    public void start() throws IOException{
        serverSocket = new ServerSocket(PORT);
        System.out.println("Servidor iniciado na porta " + PORT);
        clienteLoop();
    }

    public void clienteLoop(){
        String usuario;
        while(true){
            try{
                ClienteSocket clienteSocket = new ClienteSocket(serverSocket.accept());
                clientes.add(clienteSocket);
                usuario = clienteSocket.getMessage();
                clienteSocket.cadastraUsuario(usuario);
                clienteSocket.sendMsg("01"+usuario);
                
                new Thread(() -> clienteMessageLoop(clienteSocket)).start();
            } catch (IOException ex){
                System.out.println("Erro "+ ex.getMessage());
            }
        }
    }

    private void clienteMessageLoop(ClienteSocket clienteSocket){
        String msg;
        try{
            while((msg = clienteSocket.getMessage())!= null){
                System.out.printf("Mensagem recebida do cliente %s: %s\n", clienteSocket.getUsuario(), msg);
                sendMsgToAll(clienteSocket, msg);
                if("sair".equalsIgnoreCase(msg)){
                    return;
                }    
            }
        } finally {
            sendMsgToAll(clienteSocket, "02"+clienteSocket.getUsuario());
            clienteSocket.close();
        }
    }

    private void sendMsgToAll(ClienteSocket sender, String msg){
        Iterator<ClienteSocket> iterator = clientes.iterator();
        while(iterator.hasNext()){
            ClienteSocket clienteSocket = iterator.next();
            if(msg.startsWith("02")){
                clienteSocket.sendMsg(msg);
            }
            else{
                if(msg.startsWith("@")){
                    if(msg.startsWith("@"+clienteSocket.getUsuario())){
                        clienteSocket.sendMsg("@"+sender.getUsuario()+": "+msg);    
                    }
                }
                else{
                    if(!sender.equals(clienteSocket)){
                        if(!clienteSocket.sendMsg("@"+sender.getUsuario()+": "+msg));  
                        }
                    }
                    clienteSocket.sendMsg("01"+sender.getUsuario());
                }
            }        
        }       

    public static void main(String[] args){
        try{
            Servidor server = new Servidor();
            server.start();
        } catch (IOException ex){
            System.out.println("Erro ao iniciar o servidor " + ex.getMessage());
        }
        System.out.println("Servidor finalizado");
    }
}