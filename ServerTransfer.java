/*********************************************************************************
CEFET-MG  2017/1
Disciplinas: 
			 Laboratório de Redes de Computadores I  
			 Redes de Computadores I

TP Implementação - Camada 1: Java

Grupo 4/8:
	Larissa Bicalho - 201512040304 
	Libério Afonso - 201522040544
	Raphael Chagas - 201522040234
	Roberto Almeida Gontijo - 201322040133	
**********************************************************************************/

	import java.io.File;
	import java.io.FileInputStream;
	import java.io.IOException;
	import java.io.OutputStream;
	import java.net.ServerSocket;
	import java.net.*;
	import java.util.Random;
	import java.io.*; 
import java.nio.ByteBuffer;
import java.util.Arrays;
	/**@author Lucas iorio - http://www.byiorio.com
	 * 
	 * @author Lucas iorio - http://www.byiorio.com
	 *
	 */

public class ServerTransfer {
	//Main
	public static void main(String[] args) throws SocketException, UnknownHostException { 
		// Criando servidor
	    ServerTransfer server = new ServerTransfer();
	 	// Aguardar conexao de cliente para transferia
	    server.waitForClient();         
	}
	 
	private static String getMacAddress(NetworkInterface netInter) throws SocketException, UnknownHostException {
		//NetworkInterface netInter = NetworkInterface.getByInetAddress(InetAddress.getLocalHost());
		byte[] macAddressBytes = netInter.getHardwareAddress();
		String macAddress = String.format("%1$02x-%2$02x-%3$02x-%4$02x-%5$02x-%6$02x",
		        macAddressBytes[0], macAddressBytes[1],
		        macAddressBytes[2], macAddressBytes[3],
		        macAddressBytes[4], macAddressBytes[5]).toUpperCase();
		return macAddress;
	}

	public byte[] getByteArrayFromInteger(int intValue ,int byteArrSize) {
            ByteBuffer wrapped = ByteBuffer.allocate(byteArrSize);
            wrapped.putInt(intValue);
            byte[] byteArray = wrapped.array();
            return byteArray;
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
		    InetAddress endereco = sock.getInetAddress(); 
		    System.out.println("Conectado à máquina: " + endereco + 
                                       " Tamanho Buffer: " + tam +
                                       " Mac Servidor: " + getMacAddress(NetworkInterface.getByInetAddress(endereco)));
			
			// MAC do server	
			// Mostrar como descobriu o MAC address do host.
		    String mac_server = getMacAddress(NetworkInterface.getByInetAddress(endereco));

			//Negociar TMQ - Esperando tamanho do pacote pelo cliente
			System.out.println("Esperando solicitação de tamanho do pacote pelo cliente\n");
			String clientSentence; 
	        String capitalizedSentence; 
			BufferedReader inFromClient = new BufferedReader(new InputStreamReader(sock.getInputStream())); 
			DataOutputStream  outToClient = new DataOutputStream(sock.getOutputStream()); 
	        clientSentence = inFromClient.readLine(); 
    	    outToClient.writeBytes(clientSentence); 

// Mostrar a negociação do tamanho do quadro (Uma camada pergunta a outra qual o tamanho do quadro que pode enviar),
			
			// Mostrar como descobriu o MAC address do cliente.	
				//Obter Mac do cliente e definir tamanho de quadro
			String texto[] = clientSentence.split("___");
			System.out.println("Tamanho do quadro:"+texto[0]+" MAC Cliente: "+texto[1]);
			String mac_cliente = texto[1]; 
			
			int tam_quadro_pedido = Integer.parseInt(texto[0]); //Pedido pelo cliente		
			int tam_quadro = tam_quadro_pedido;	//Definido pelo servidor

			if(tam_quadro_pedido>=1518){ // 2 bytes representam tamanho máximo de 65535 bytes para um quadro
				tam_quadro = 1518; // Entretanto na prática só cabe 1500 bytes em um quadro + cabeçalho
			}
			else if(tam_quadro_pedido<=18){
				tam_quadro = 19; // Tamanho mínimo do quadro é 18 bytes (6 MAC DEST + 6 MAC ORIG + 2 TAM + 4 CDE) + 1
			}

			// Criando tamanho de leitura
			byte[] cbuffer = new byte[tam_quadro-18]; //Só dados
			
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
			
			//Remover traços do MAC
			mac_server="24";
			byte[] Bmac_server = mac_server.getBytes();
			byte[] Bmac_client = mac_cliente.getBytes();

		    byte[] Btam = getByteArrayFromInteger(tam_quadro, 4); // 4 bytes - não é o formato do protocolo
			byte[] Btam2 = new byte[2]; // 2 bytes somente
				Btam2[0] = Btam[3];//Menos significativo
				Btam2[1] = Btam[2];//Mais significativo
				System.out.printf("0x%02X%02X\n", Btam2[1],Btam2[0]);
			byte[] Bctr = new byte[4] ;
				Bctr[0]=0; Bctr[1]=0; Bctr[2]=0; Bctr[3]=0;
			
			//System.out.println("Btam: " + Arrays.toString(Btam));
//			Btam[0]=0;
//			Btam[1]=0;
				
			while ((bytesRead = fileIn.read(cbuffer)) != -1) {
				int rand = gerador.nextInt(100);

				// Verificar a colisão de pacotes
				while(rand < 20){
					System.out.println("Conflito. Reenviado Pedaço.");			    
					rand = gerador.nextInt(100);
				}
				
				socketOut.write(cbuffer, 0, bytesRead);			
                socketOut.flush();
            }

//Falta
		// Mostrar a PDU
		// Mostrar o quadro ethernet (deve mostrar o txt com os bits que o formam)
		// Mostrar se há perdas de pacotes
	 
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

