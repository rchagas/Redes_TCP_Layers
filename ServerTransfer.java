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
/* Referência de partida para desenvolvimento do código	@author Lucas iorio - http://www.byiorio.com */

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
	import java.io.File;
	import java.io.FileOutputStream;
	import java.io.IOException;
	import java.io.File;
	import java.io.FileWriter;
	import java.io.IOException; 

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
		String macAddress = String.format("%1$02x:%2$02x:%3$02x:%4$02x:%5$02x:%6$02x",
		        macAddressBytes[0], macAddressBytes[1],
		        macAddressBytes[2], macAddressBytes[3],
		        macAddressBytes[4], macAddressBytes[5]).toUpperCase(); // "%1$02x-%2$02x-%3$02x-%4$02x-%5$02x-%6$02x"
		return macAddress;
	}

	public byte[] getByteArrayFromInteger(int intValue ,int byteArrSize) {
            ByteBuffer wrapped = ByteBuffer.allocate(byteArrSize);
            wrapped.putInt(intValue);
            byte[] byteArray = wrapped.array();
            return byteArray;
	}

	public static byte[] hexStringToByteArray(String s) {
		int len = s.length();
		byte[] data = new byte[len / 2];
		byte[] invdata = new byte[len / 2];
		
		for (int i = 0; i < len; i += 2) {
		    data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
		                         + Character.digit(s.charAt(i+1), 16));
		}

		int size = data.length;
		for (int i = 0; i < invdata.length; i++) {
 		   size--;
 		   invdata[i] = data[size];
		}	
		return invdata;
	}

	public byte[] constroiQuadro(byte[] Bmac_client, byte[] Bmac_server, byte[] Btam2, byte[] cbuffer, byte[] Bctr, int tam_quadro){
		byte[] quadro = new byte[tam_quadro];
		int i;
	
		//Preenche mac destino
		for(i=0;i<=5;i++){
			quadro[i] = Bmac_client[5-i];	
		}				
	
		//Preenche mac origem
		for(i=6;i<=11;i++){
			quadro[i] = Bmac_server[11-i];	
		}
		
		//Preenche tamanho do pacote
		quadro[12]=Btam2[1];
		quadro[13]=Btam2[0];
		
		//Dados
		int numdados = cbuffer.length;
		//System.out.println("numdados: " + numdados +"\n");
		i=14;
		for(i=14; i<(numdados+14); i++){
			quadro[i] = cbuffer[i-14];	
			//quadro[i] = cbuffer[numdados+14-i-1];	
		}
		int j = i;
		
		//Controle
		quadro[j]=0; quadro[j+1]=0; quadro[j+2]=0; quadro[j+3]=0;	
		
		return quadro;
	}

	//Imprime PDU em hexadecimal
	void imprimirPDU(byte[] quadro, String s){
			System.out.println("\n\n\n\nImprimindo PDU (Em hexadecimal) " + s);
			System.out.println("----------------------------------------------------------------");
		
			//Preenche mac destino
			System.out.printf("MAC ADDRESS CLIENTE: %1$02x:%2$02x:%3$02x:%4$02x:%5$02x:%6$02x\n", quadro[0],quadro[1], quadro[2], quadro[3],quadro[4], quadro[5]); 	
			System.out.printf("MAC ADDRESS SERVIDOR: %1$02x:%2$02x:%3$02x:%4$02x:%5$02x:%6$02x\n", quadro[6],quadro[7], quadro[8], quadro[9],quadro[10], quadro[11]); 	
			System.out.printf("TAMANHO: 0x%1$02x%2$02x\n",quadro[12], quadro[13]);
			System.out.printf("DADOS: 0x");

			//Imprimo dados em hexadecimal
			for(int i=14; i<quadro.length-4;i++){
				System.out.printf("%1$02x",quadro[i]);	
			}

			System.out.printf("\nBits Controle: 0x%1$02x%2$02x%2$02x%2$02x\n", quadro[quadro.length-4], quadro[quadro.length-3], quadro[quadro.length-2], quadro[quadro.length-1]);
			System.out.printf("---------------------------------------------------------------------------");
	}

	//Gera txt em bits com o quadro
	void txtQuadro(byte[] quadro) throws IOException{
		FileWriter arq = new FileWriter("primeiroQuadro_comDados.txt");    
		PrintWriter gravarArq = new PrintWriter(arq);     
		int i=0;
		for (byte b : quadro) {
			String s1 = String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0');
			//System.out.println(s1); // 10000001
			gravarArq.printf(s1);

			//String esc = Integer.toBinaryString(b & 255 | 256).substring(1);
			//if(i<8)gravarArq.printf(esc);
			//i++;
		}
		arq.close();

	}

	int fromByteArray(byte[] bytes) {
		return bytes[0] << 24 | (bytes[1] & 0xFF) << 16 | (bytes[2] & 0xFF) << 8 | (bytes[3] & 0xFF);
	}

// O outputStream utiliza o mesmo canal que o DataOutputStream. Como resultado, somente conseguimos enviar uma mensagem para o cliente por execução do código do host. Após o cliente solicitar a conexão, o host retorna com uma única mensagem informando o número de pacotes a serem enviados para o cliente e o arquivo a ser enviado.

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
			System.out.println("Esperando solicitação de tamanho do pacote pelo cliente: ");
			String clientSentence; 
	        String capitalizedSentence; 
			BufferedReader inFromClient = new BufferedReader(new InputStreamReader(sock.getInputStream())); 
			clientSentence = inFromClient.readLine(); 
//			System.out.println("clientSentence: " + clientSentence+"\n");
    	    
// Mostrar a negociação do tamanho do quadro (Uma camada pergunta a outra qual o tamanho do quadro que pode enviar),
			
			// Mostrar como descobriu o MAC address do cliente.	
				//Obter Mac do cliente e definir tamanho de quadro
			String texto[] = clientSentence.split("___");
//			System.out.println("Tamanho do quadro:"+texto[0]+" MAC Cliente: "+texto[1]);
			String mac_cliente = texto[1]; 
			
			int tam_quadro_pedido = Integer.parseInt(texto[0]); //Pedido pelo cliente		
			int tam_quadro = tam_quadro_pedido;	//Definido pelo servidor

			if(tam_quadro_pedido>=1518){ // 2 bytes representam tamanho máximo de 65535 bytes para um quadro
				tam_quadro = 1518; // Entretanto na prática só cabe 1500 bytes em um quadro + cabeçalho
			}
			else if(tam_quadro_pedido<=18){
				tam_quadro = 18; // Tamanho mínimo do quadro é 18 bytes (6 MAC DEST + 6 MAC ORIG + 2 TAM + 4 CDE) + 1
			}

			// Criando tamanho de leitura
			byte[] cbuffer = new byte[tam_quadro-18]; //Só dados
			
			int bytesRead;
 
			// Criando arquivo que sera transferido pelo servidor
			File file = new File("input_server.txt");
			fileIn = new FileInputStream(file);
	             
			// Criando canal de transferencia
			socketOut = sock.getOutputStream();
	 
			// Lendo arquivo criado e enviado para o canal de transferencia
//			System.out.println("Enviando Arquivo...");
		    //instância um objeto da classe Random usando o construtor padrão
		    Random gerador = new Random();
						
			//Tira : dos endereços MAC
			String ns_mac_server = mac_server.replace(":","");
			String ns_mac_cliente = mac_cliente.replace(":","");
			//System.out.println("ns_mac_server: "+ ns_mac_server +"string sizes " + ns_mac_server.length());
			
			byte[] Bmac_server = hexStringToByteArray(ns_mac_server);
			byte[] Bmac_client = hexStringToByteArray(ns_mac_cliente);
				//System.out.printf("Teste Mac_server 0x%02X%02X%02X%02X%02X%02X\n", Bmac_server[5], Bmac_server[4], Bmac_server[3], Bmac_server[2] ,Bmac_server[1], Bmac_server[0]); 
				//System.out.printf("Teste Mac_server 0x%02X%02X\n", Bmac_server[1], Bmac_server[0]);
				 
		    byte[] Btam = getByteArrayFromInteger(tam_quadro, 4); // 4 bytes - não é o formato do protocolo
			byte[] Btam2 = new byte[2]; // 2 bytes somente
				Btam2[0] = Btam[3];//Menos significativo
				Btam2[1] = Btam[2];//Mais significativo
//				System.out.printf("0x%02X%02X\n", Btam2[1],Btam2[0]);

			byte[] Bctr = new byte[4] ;
				Bctr[0]=0; Bctr[1]=0; Bctr[2]=0; Bctr[3]=0;
							
			byte[] quadro;
			
			//Número de pacotes enviados
			int npacotes_aEnv=0;
				if(tam_quadro>18) npacotes_aEnv = (int)Math.ceil((double)file.length()/(tam_quadro-18));

			String npac_aEnv = Integer.toString(npacotes_aEnv);
			
			byte[] Bnpac_aEnv = getByteArrayFromInteger(npacotes_aEnv, 4);

			int numpac = fromByteArray(Bnpac_aEnv);
//			System.out.println("npacotes_aEnv: "+npacotes_aEnv +" numpac: "+numpac+"!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");

			//Os 4 primeiros bytes da resposta para o cliente consistem no número de pacotes que o cliente deve esperar
			socketOut.write(Bnpac_aEnv); 
			
			int passadas=0;
			while ((bytesRead = fileIn.read(cbuffer)) != -1) {
				int rand = gerador.nextInt(100);
				quadro = constroiQuadro(Bmac_client, Bmac_server, Btam2, cbuffer, Bctr, tam_quadro);//tam_quadro inclui cabeçalho

				// Mostrar a PDU
				imprimirPDU(quadro, "Servidor");

				// Mostrar o quadro ethernet (deve mostrar o txt com os bits que o formam)
				if(passadas==1){//Escreve o primeiro quadro nesse caso
					txtQuadro(quadro);
				}
				// Verificar a colisão de pacotes
				while(rand < 20){
					System.out.println("\nConflito. Reenviado Pedaço.");			    
					rand = gerador.nextInt(100);
				}
				passadas++;
				socketOut.write(quadro, 0, quadro.length);	//bytesRead		
                socketOut.flush();
				if(tam_quadro==18) break;
            }

			System.out.println("\npassadas: "+passadas);
            System.out.println("\nArquivo Enviado!");
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

// Mostrar se há perdas de pacotes

