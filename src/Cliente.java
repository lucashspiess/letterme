import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class Cliente extends JFrame implements Runnable{    
    private static final String ENDERECO_SERVIDOR = "192.168.1.3";
    private ClienteSocket clienteSocket;
    private Scanner scanner;
    DefaultListModel modelo = new DefaultListModel<>();

    JList usuarios = new JList<>(modelo);

    JPanel usuariosPane;

    JLabel usuariosOnline;
    int verticalScrollBarMaximumValue;
    int registros;
    JPanel painelFundo;
    JScrollPane barraRolagem;
    JTextArea jtchat;
    JButton benviar;
    JTextField jtmensagem;
    JDialog usuario;
    static String login;
    JTextField nomeUsuario = new JTextField(10);

    public Cliente(){
        scanner = new Scanner(System.in);
    }

    public void start() throws IOException{
        try{
            clienteSocket = new ClienteSocket(new Socket(ENDERECO_SERVIDOR, 4000));
            System.out.println("Cliente conectado ao servidor em " + ENDERECO_SERVIDOR + ":" + 4000);
            popUp(null);
            new Thread(this).start();
            mensagemLoop();
        } finally {
            clienteSocket.close();
        }
    }

    @Override
    public void run(){
        String msg;
        int i;
        int j = 0;
        while((msg = clienteSocket.getMessage()) != null){
            if(!msg.startsWith("01")){
                if(msg.startsWith("02")){
                    String [] usuario = msg.split("02");
                    for(i=0;i<modelo.getSize();i++){
                        if(modelo.getElementAt(i).equals(usuario[1])){
                            modelo.remove(i);
                            usuariosPane.removeAll();
                            usuariosOnline = new JLabel("Usuários online ("+modelo.getSize()+")");
                            usuariosPane.add(usuariosOnline);
                            usuariosPane.add(usuarios);
                            usuariosPane.revalidate();
                        }
                    } 
                j=0;
                }
                else{
                    jtchat.append("\n"+msg); 
                }      
            }
            else{
                String [] usuario = msg.split("01");
                if(modelo.getSize()==0){
                    modelo.addElement(usuario[1]);
                    usuariosPane.removeAll();
                    usuariosOnline = new JLabel("Usuários online ("+modelo.getSize()+")");
                    usuariosPane.add(usuariosOnline);
                    usuariosPane.add(usuarios);
                    usuariosPane.revalidate();
                }
                else{
                    for(i=0;i<modelo.getSize();i++){
                        if(modelo.getElementAt(i).equals(usuario[1])){
                            j=1;
                        }
                    }
                    if(j==0){
                        modelo.addElement(usuario[1]);
                        usuariosPane.removeAll();
                        usuariosOnline = new JLabel("Usuários online ("+modelo.getSize()+")");
                        usuariosPane.add(usuariosOnline);
                        usuariosPane.add(usuarios);
                        usuariosPane.revalidate();
                    } 
                }
                j=0;
            }
        }
    }

    private void mensagemLoop() throws IOException{
        String msg;
        do{
            msg = scanner.nextLine();
            clienteSocket.sendMsg(msg);
        }while(true);
    }

    public void popUp(JFrame janela){
        JLabel jnomeusuario = new JLabel("Nome de usuário");
        JButton ok = new JButton("Escolher");
        usuario = new JDialog(janela, true);
        usuario.setSize(200,200);
        usuario.setLocation(500,100);
        usuario.setLayout(new FlowLayout());
        nomeUsuario.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e1) {
                if(e1.getKeyCode() == KeyEvent.VK_ENTER) { 
                    login = nomeUsuario.getText();
                    clienteSocket.sendMsg(login);
                    usuario.setVisible(false);
                }
            }
        });
        usuario.add(jnomeusuario);
        usuario.add(nomeUsuario);

        Ok acaousuario = new Ok();
        ok.addActionListener(acaousuario);

        usuario.add(ok);
        usuario.setVisible(true);
    }

    private class Ok implements ActionListener{
        Ok(){

        }

        public void actionPerformed(ActionEvent evento){
            login = nomeUsuario.getText();
            clienteSocket.sendMsg(login);
            usuario.setVisible(false);
        }
    }


    public void telaGeral(){
        JPanel imagem = new JPanel();
        JLabel agenda = new JLabel("LetterMe");
        Font f = new Font("Old English Text MT", Font.PLAIN, 100);

        usuariosOnline = new JLabel("Usuários online ("+modelo.getSize()+")");
        JPanel chat;

        usuariosPane = new JPanel();
        usuariosPane.setLayout(new BoxLayout(usuariosPane, BoxLayout.Y_AXIS));
        usuariosPane.add(usuariosOnline);
        usuariosPane.add(usuarios);


        Color minhaCor = new Color(238, 238, 238);

        benviar = new JButton("Enviar");


        chat = new JPanel();
        chat.setLayout(new BoxLayout(chat, BoxLayout.Y_AXIS));
        jtchat = new JTextArea();

        JPanel jpchatflow = new JPanel();
        barraRolagem = new JScrollPane(jtchat);

        jtchat.setRows(20);
        chat.add(barraRolagem);
        jtchat.setWrapStyleWord(true);
        barraRolagem.setBounds(10,60,780,500);
        barraRolagem.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        verticalScrollBarMaximumValue = barraRolagem.getVerticalScrollBar().getMaximum();
        barraRolagem.getVerticalScrollBar().addAdjustmentListener(
            e -> {
                if ((verticalScrollBarMaximumValue - e.getAdjustable().getMaximum()) == 0)
                    return;
                e.getAdjustable().setValue(e.getAdjustable().getMaximum());
                verticalScrollBarMaximumValue = barraRolagem.getVerticalScrollBar().getMaximum();
            }); 

        JPanel jpatributos = new JPanel();
        jpatributos.setLayout(new FlowLayout(FlowLayout.CENTER));
        jtmensagem = new JTextField(65);

        jtmensagem.addKeyListener(new KeyAdapter() {
            
            public void keyPressed(KeyEvent e) {
                if(e.getKeyCode() == KeyEvent.VK_ENTER) {
                    String msg;
                msg = jtmensagem.getText();
                if(!msg.isEmpty()){
                    jtchat.append("\n"+"você: "+msg);
                    clienteSocket.sendMsg(msg); 
                    jtmensagem.setText("");
                }
                }
            }
        });
        jpatributos.add(jtmensagem);
        jpatributos.add(benviar);

        jpchatflow.add(new JLabel("chat"));
        chat.add(jpchatflow);
        chat.add(barraRolagem);
        chat.add(jpatributos);

        Enviar acaoenviar = new Enviar();

        benviar.addActionListener(acaoenviar);

        jtchat.setText("");
        jtchat.setEditable(false);
        jtchat.setLineWrap(true);

        agenda.setForeground(new Color(0,0,0));
        agenda.setFont(f);

        imagem.setLayout(new FlowLayout(FlowLayout.CENTER));
        imagem.add(agenda);

        Image    iconeTitulo = Toolkit.getDefaultToolkit().getImage("letterme.png");
        setIconImage(iconeTitulo);

        this.setTitle("LetterMe");
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(imagem, BorderLayout.PAGE_START);
        getContentPane().add("West",usuariosPane);
        getContentPane().add("Center",chat);

        setBackground(minhaCor);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocation(75,25);
        setSize(900,500);
        setResizable(false);
        setVisible(true);
    }

    private class Enviar implements ActionListener{
        private Enviar(){

        }

        public void actionPerformed(ActionEvent evento){
            String msg;
            msg = jtmensagem.getText();
            if(!msg.isEmpty()){
                jtchat.append("\n"+"você: "+msg);
                clienteSocket.sendMsg(msg);
                jtmensagem.setText("");
            }
        }
    }

    public static void main(String[] args){
        try{
            Cliente cliente = new Cliente();
            cliente.telaGeral();
            cliente.start();
            
        } catch (IOException ex){
            System.out.println("Erro ao iniciar o cliente "+ ex.getMessage());
        }
        System.out.println("Cliente finalizado");
    }  
}