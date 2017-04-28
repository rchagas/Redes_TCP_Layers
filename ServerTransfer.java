	import java.io.File;
	import java.io.FileInputStream;
	import java.io.IOException;
	import java.io.OutputStream;
	import java.net.ServerSocket;
	import java.net.*;
	import java.util.Random;
	 
	/**@author Lucas iorio - http://www.byiorio.com
	 * 
	 * @author Lucas iorio - http://www.byiorio.com
	 *
	 */
	public class ServerTransfer {
		private static String getMacAddress() throws SocketException, UnknownHostException {
			NetworkInterface netInter = NetworkInterface.getByInetAddress(
                InetAddress.getByName(InetAddress.getLocalHost().getHostName()+".local"));
			byte[] macAddressBytes = netInter.getHardwareAddress();
			String macAddress = String.format("%1$02x-%2$02x-%3$02x-%4$02x-%5$02x-%6$02x",
		    macAddressBytes[0], macAddressBytes[1],
		    macAddressBytes[2], macAddressBytes[3],
		    macAddressBytes[4], macAddressBytes[5]).toUpperCase();
			return macAddress;
    }
	    public static void main(String[] args) throws SocketException, UnknownHostException {
	 
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
	            // Abrindo porta para conexao de clientes
	            servsock = new ServerSocket(13267);
	            System.out.println("Porta de conexao aberta 13267");		    
	 
	 
	            // Cliente conectado
	            Socket sock = servsock.accept();
	            System.out.println("Conexao recebida pelo cliente");
	
		    int tam = sock.getSendBufferSize(); //Pega tamanho do buffer de envio 			
		    InetAddress endereco = sock.getInetAddress(); //pega o endereço do host
		    System.out.println("Conectado à máquina: " + endereco + 
                                       " Tamanho Buffer: " + tam +
                                       " Mac Server: " + getMacAddress());

		    //String mac_server = getMacAddress(NetworkInterface.getByInetAddress(endereco));


	            // Criando tamanho de leitura
	            byte[] cbuffer = new byte[1];
	            int bytesRead;
	 
	            // Criando arquivo que sera transferido pelo servidor
	            File file = new File("teste");
	            fileIn = new FileInputStream(file);
	             
	            // Criando canal de transferencia
	            socketOut = sock.getOutputStream();
	 
	            // Lendo arquivo criado e enviado para o canal de transferencia
	            System.out.println("Enviando Arquivo...");
		    //instância um objeto da classe Random usando o construtor padrão
		    Random gerador = new Random();

	            while ((bytesRead = fileIn.read(cbuffer)) != -1) {
	                
			int rand = gerador.nextInt(100);
			while(rand < 50)
			{
				System.out.println("Conflito. Reenviado Pedaço.");			    
				rand = gerador.nextInt(100);
			}		
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

