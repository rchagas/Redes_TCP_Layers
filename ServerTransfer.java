	import java.io.File;
	import java.io.FileInputStream;
	import java.io.IOException;
	import java.io.OutputStream;
	import java.net.ServerSocket;
	import java.net.*;

	import java.io.BinaryReader;

	 
	/**@author Lucas iorio - http://www.byiorio.com
	 * 
	 * @author Lucas iorio - http://www.byiorio.com
	 *
	 */
	public class ServerTransfer {
	    public static void main(String[] args) {
	 
	        // Criando servidor
	        ServerTransfer server = new ServerTransfer();
	 
	        // Aguardar conexao de cliente para transferia
	        server.waitForClient();
	         
	    }
	 
	    public void waitForClient() {
	        // Checa se a transferencia foi completada com sucesso
	        OutputStream socketOut = null;
	        ServerSocket servsock = null;
	        FileInputStream fileIn = null;
	 
	        try {
	            // Abrindo porta para conexao de clients
	            servsock = new ServerSocket(13267);
	            System.out.println("Porta de conexao aberta 13267");		    
	 
	 
	            // Cliente conectado
	            Socket sock = servsock.accept();
	            System.out.println("Conexao recebida pelo cliente");
	
		    int tam = sock.getSendBufferSize();//Pega tamanho do buffer de envio 			
		    InetAddress endereco = sock.getInetAddress( );//pega o endereço do host
		    System.out.println("Conectado à máquina: " + endereco + " Tamanho Buffer: " + tam);
		    
	            // Criando tamanho de leitura
	            byte[] cbuffer = new byte[100];
	            int bytesRead;
	 
	            // Criando arquivo que sera transferido pelo servidor
	            File file = new File("teste.txt");
	            fileIn = new FileInputStream(file);
		    BinaryReader reader = new BinaryReader(file);
	             
	            // Criando canal de transferencia
	            socketOut = sock.getOutputStream();
	 
	            // Lendo arquivo criado e enviado para o canal de transferencia
	            System.out.println("Enviando Arquivo...");
	            while ((bytesRead = fileIn.read(cbuffer)) != -1) {
	                socketOut.write(cbuffer, 0, bytesRead);
	                socketOut.flush();
	            }
	 
	            System.out.println("Arquivo Enviado!");
	        } catch (Exception e) {
	            // Mostra erro no console
	            e.printStackTrace();
	        } finally {
	            if (socketOut != null) {
	                try {
	                    socketOut.close();
	                } catch (IOException e) {
	                    e.printStackTrace();
	                }
	            }
	 
	            if (servsock != null) {
	                try {
	                    servsock.close();
	                } catch (IOException e) {
	                    e.printStackTrace();
	                }
	            }
	 
	            if (fileIn != null) {
	                try {
	                    fileIn.close();
	                } catch (IOException e) {
	                    e.printStackTrace();
	                }
	            }
	        }
	    }
	}

