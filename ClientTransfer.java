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

	import java.io.*;
	import java.io.FileOutputStream;
	import java.io.IOException;
	import java.io.InputStream;
	import java.net.*;
	 
   	public class ClientTransfer {
		//Main
		public static void main(String[] args) throws SocketException, UnknownHostException {
            //Criando Classe cliente para receber arquivo
	        ClientTransfer cliente = new ClientTransfer();
	 
	        //Solicitando arquivo
	        cliente.getFileFromServeR();
	    }

		private static byte[] getMacAddress() throws SocketException, UnknownHostException {
	       	    NetworkInterface netInter = NetworkInterface.getByInetAddress(
        	    InetAddress.getByName(InetAddress.getLocalHost().getHostName()+".local"));
		    return netInter.getHardwareAddress();
    		}
		private static String getMacMask(byte[] macAddressBytes){
		    String macAddress = String.format("%1$02x-%2$02x-%3$02x-%4$02x-%5$02x-%6$02x",
		    macAddressBytes[0], macAddressBytes[1],
		    macAddressBytes[2], macAddressBytes[3],
		    macAddressBytes[4], macAddressBytes[5]).toUpperCase();
                    return macAddress;
		}
	 
	    private void getFileFromServeR() {
	        Socket sockServer = null;
	        FileOutputStream fos = null;
	        InputStream is = null;
	 
			String str = "/home/roberto/Área de Trabalho/Redes_TCP_Layers/";
	
	        try {
	            // Criando conexão com o servidor
	            System.out.println("Conectando com Servidor porta 13267");
                InetAddress address = InetAddress.getByName(InetAddress.getLocalHost()
                            	.getHostName()+".local");//.getHostAddress()
				
				String mac_cliente = getMacAddress(NetworkInterface.getByInetAddress(address));				
				System.out.println("MAC Cliente: " + mac_cliente);
 

	            sockServer = new Socket(address, 13267);
	            is = sockServer.getInputStream();

				//Negociar TMQ - Solicitar tamanho do pacote pelo cliente
				String sentence; 
		        String modifiedSentence; 
		        BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in)); 
		        DataOutputStream outToServer = new DataOutputStream(sockServer.getOutputStream()); 
				BufferedReader inFromServer = new BufferedReader(new InputStreamReader(sockServer.getInputStream())); 
				System.out.println("Cliente - Insira o tamanho de pacote desejado: ");
        		sentence = inFromUser.readLine(); 
        		outToServer.writeBytes(sentence + "___" + mac_cliente + '\n'); 
        		modifiedSentence = inFromServer.readLine();
        		System.out.println("FROM SERVER: " + modifiedSentence); 

	            // Cria arquivo local no cliente
	            fos = new FileOutputStream(new File(str+"ArqRecebido"));
	            System.out.println(str+"ArqRecebido");
	             
	            // Prepara variáveis para transferência
				int tam_quadro_pedido = Integer.parseInt(sentence); //Pedido pelo cliente
				int tam_quadro = tam_quadro_pedido;	

				if(tam_quadro_pedido>=1518){ // 2 bytes representam tamanho máximo de 65535 bytes para um quadro
					tam_quadro = 1518; // Entretanto na prática só cabe 1500 bytes em um quadro + cabeçalho
				}
				else if(tam_quadro_pedido<=18){
					tam_quadro = 19; // Tamanho mínimo do quadro é 18 bytes (6 MAC DEST + 6 MAC ORIG + 2 TAM + 4 CDE) + 1
				}	
			
	            byte[] cbuffer = new byte[tam_quadro-18];
	            int bytesRead;

				byte[] arquivo = new byte [4000];	
				int k=0;	 
				for(k=0;k<4000;k++){
					arquivo[k] = (byte) 0;
				}   

				int contP = 3995, contB = 0;		    	 	        
				int size = 0;    
				// Copia conteudo do canal	            
				System.out.println("Recebendo arquivo...");	            
				while ((bytesRead = is.read(cbuffer)) != -1) {	                
					fos.write(cbuffer, 0, bytesRead);			
					arquivo[contP] = cbuffer[contB];			
					contB++;			
					contP--;	        
			        size++;
					fos.flush();	            
				}		
				byte[] arqfinal = new byte [size];
				for(k=0;k<=size;k++){
					arqfinal[k]=arquivo[size-k];
				}
 
				FileOutputStream ar = null;		
				ar = new FileOutputStream(new File(str+"Arquivo"));		
				while ((bytesRead = is.read(arqfinal)) != -1) {	               
					ar.write(arqfinal, 0, bytesRead);				                
					ar.flush();	            
				}		
			
	            System.out.println("Arquivo recebido!");
	        } catch (Exception e) {
	            e.printStackTrace();
	        } finally {
	            if (sockServer != null) {
	                try {
	                    sockServer.close();
	                } catch (IOException e1) {
	                    e1.printStackTrace();
	                }
	            }
	 
	            if (fos != null) {
	                try {
	                    fos.close();
	                } catch (IOException e1) {
	                    e1.printStackTrace();
	                }
	            }
	 
	            if (is != null) {
	                try {
	                    is.close();
	                } catch (IOException e1) {
	                    e1.printStackTrace();
	                }
	            }
	        }
	 
	    }
	}

