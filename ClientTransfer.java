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

	import java.net.*;
	import java.nio.*;
	import java.util.*;
	import java.io.*;
	
   	public class ClientTransfer {
		//Main
		public static void main(String[] args) throws SocketException, UnknownHostException {
            //Criando Classe cliente para receber arquivo
	        ClientTransfer cliente = new ClientTransfer();
	 
	        //Solicitando arquivo
	        cliente.getFileFromServeR();
	    }
        
        /*private static byte[] getBytesMacAddress() throws SocketException, UnknownHostException {	       	    
            NetworkInterface netInter = NetworkInterface.getByInetAddress(InetAddress.getByName(InetAddress.getLocalHost().getHostName()+".local"));		    
            return netInter.getHardwareAddress();
        }*/	
        
		private static String getMacAddress(NetworkInterface netInter) throws SocketException, UnknownHostException {
			//NetworkInterface netInter = NetworkInterface.getByInetAddress(InetAddress.getLocalHost());
			byte[] macAddressBytes = netInter.getHardwareAddress();
			String macAddress = String.format("%1$02x:%2$02x:%3$02x:%4$02x:%5$02x:%6$02x",
		    macAddressBytes[0], macAddressBytes[1],
		    macAddressBytes[2], macAddressBytes[3],
		    macAddressBytes[4], macAddressBytes[5]).toUpperCase();//"%1$02x-%2$02x-%3$02x-%4$02x-%5$02x-%6$02x"
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
		    System.out.println("\n\n\n\nImprimindo PDU (Em hexadecimal): " + s);
		    System.out.println("-------------------------------------------------------------------------");

		    //Preenche mac destino
		    System.out.printf("MAC ADDRESS CLIENTE: %1$02x:%2$02x:%3$02x:%4$02x:%5$02x:%6$02x\n", quadro[0],quadro[1], quadro[2], quadro[3],quadro[4], quadro[5]); 	
		    System.out.printf("MAC ADDRESS SERVIDOR: %1$02x:%2$02x:%3$02x:%4$02x:%5$02x:%6$02x\n", quadro[6], quadro[7], quadro[8], quadro[9], quadro[10], quadro[11]); 	
		    System.out.printf("TAMANHO: 0x%1$02x%2$02x\n",quadro[12], quadro[13]);
		    System.out.printf("DADOS: 0x");

		    //Imprimo dados em hexadecimal
		    for(int i=14; i<quadro.length-4;i++){
			    System.out.printf("%1$02x",quadro[i]);	
		    }

		    System.out.printf("\nBits Controle: 0x%1$02x%2$02x%2$02x%2$02x\n", quadro[quadro.length-4], quadro[quadro.length-3], quadro[quadro.length-2], quadro[quadro.length-1]);
		    System.out.printf("-----------------------------------------------------------------------------\n\n\n");
    }

    int fromByteArray(byte[] bytes) {	
        return bytes[0] << 24 | (bytes[1] & 0xFF) << 16 | (bytes[2] & 0xFF) << 8 | (bytes[3] & 0xFF);
    }

    //Gera txt em bits com o quadro
    void txtQuadro(byte[] quadro) throws IOException{
	    FileWriter arq = new FileWriter("Quadro1_Cliente.txt");    
	    PrintWriter gravarArq = new PrintWriter(arq);     
	    int i=0;
	    for (byte b : quadro) {
	        String esc = Integer.toBinaryString(b & 255 | 256).substring(1);
		    if(i<8)gravarArq.printf(esc);
		    i++;
	    }
	    arq.close();

    }

	private void getFileFromServeR() {
		Socket sockServer = null;
		FileOutputStream fos = null;
		InputStream is = null;
	 
		String str = "/home/roberto/Área de Trabalho/Redes_TCP_Layers/";
	
		try {
			// Criando conexão com o servidor
			System.out.println("\n\n\n\nCÓDIGO CLIENTE");
			System.out.println("Conectando com Servidor porta 13267");
			InetAddress address = InetAddress.getByName(InetAddress.getLocalHost()
								.getHostName()+".local");//.getHostAddress()
				
			String mac_cliente = getMacAddress(NetworkInterface.getByInetAddress(address));				
			// System.out.println("MAC Cliente: " + mac_cliente);
			
            sockServer = new Socket(address, 13267);
            is = sockServer.getInputStream();

			//Negociar TMQ - Solicitar tamanho do pacote pelo cliente
			String sentence;
	        BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in)); 
	        DataOutputStream outToServer = new DataOutputStream(sockServer.getOutputStream()); 
			BufferedReader inFromServer = new BufferedReader(new InputStreamReader(sockServer.getInputStream())); 
			System.out.println("Cliente - Insira o tamanho de pacote desejado [>=18]: ");
       		sentence = inFromUser.readLine(); 
       		outToServer.writeBytes(sentence + "___" + mac_cliente + '\n'); 	
                	
            // Cria arquivo local no cliente
            fos = new FileOutputStream(new File(str+"allbin_pdu"));
			// System.out.println(str+"ArqRecebido");
	             
            // Prepara variáveis para transferência
			int tam_quadro_pedido = Integer.parseInt(sentence); //Pedido pelo cliente
			int tam_quadro = tam_quadro_pedido;	

			if(tam_quadro_pedido>=1518){ // 2 bytes representam tamanho máximo de 65535 bytes para um quadro
				tam_quadro = 1518; // Entretanto na prática só cabe 1500 bytes em um quadro + cabeçalho
			}
			else if(tam_quadro_pedido<=18){
				tam_quadro = 18; // Tamanho mínimo do quadro é 18 bytes (6 MAC DEST + 6 MAC ORIG + 2 TAM + 4 CDE) + 1
			}	
			
			byte[] Bpdu = new byte[tam_quadro];

			int k=0;	
 				                
			for(k=0;k < tam_quadro;k++){
				Bpdu[k] = (byte) 0;
			}   

			int bytesRead;
			byte[] arquivo = new byte [4000];	
			for(k=0;k<4000;k++){
				arquivo[k] = (byte) 0;
			}   

			int contP = 0;		    	 	        
			int npac_chegou = 0;
    
			// Copia conteudo do canal	            
			// System.out.println("Recebendo arquivo...");	

            byte[] Bnumpac = new byte[4]; //Chega um vetor de bytes

            //Primeiros 4 bytes se referem ao número de pacotes a enviar   
            int temp=0;
			int numpac = 0;
            while ((bytesRead = is.read(Bpdu)) != -1) {  //bytesRead: número de bytes da Bpdu      
				fos.write(Bpdu, 0, bytesRead);
                //System.out.println("bytesRead: " + bytesRead +"\n");
				//Número de pacotes enviados
				if(temp==0){
					Bnumpac[0] = Bpdu[0];
					Bnumpac[1] = Bpdu[1];
					Bnumpac[2] = Bpdu[2];
					Bnumpac[3] = Bpdu[3];

					numpac = fromByteArray(Bnumpac);
					//System.out.printf("Bnumpac: %1$02x:%2$02x:%3$02x:%4$02x ", Bnumpac[0], Bnumpac[1], Bnumpac[2], Bnumpac[3]);	
				    //System.out.println("numpac: " + numpac + "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
				    temp = 1;
				}
				//Arquivo transmitido
				else{
                    //Copia parte com dados e coloca em arquivo
                    int i=14;
                    for(i = 14; i < Bpdu.length - 4; i++){
					    arquivo[contP+i-14] = Bpdu[i];                      
                    }
					contP = contP+i-14;	        
			    	npac_chegou++;
					imprimirPDU(Bpdu, "Cliente");
				}
				fos.flush();	            
				//System.out.println("ok");
            }  
				            
            // System.out.println("contP: "+contP + " contB: " + contB);
	        // System.out.println("npac_chegou: " + npac_chegou);

			byte[] arqfinal = new byte [contP];//npac_chegou
			for(k=0; k < contP; k++){
				//arqfinal[k]=arquivo[contP+k+1];
				arqfinal[k] = arquivo[k];
				//System.out.println(arqfinal[k]); //Imprimindo  
			}

            String texto = new String(arqfinal);           
			//System.out.println("texto: " + texto);			
			//for(int i = 0; i < npac_chegou; i++){
            
			BufferedWriter out = new BufferedWriter(new FileWriter("cliente_ArqReceb.txt"));	
            out.write(texto);
            out.close();
			    
			if(numpac != npac_chegou){
				System.out.println("Arquivo não chegou com sucesso!");
                //if(tam_quadro==18) numpac = tam_quadro - 18;
                //System.out.printf("Pacotes faltantes %d\n", numpac-(npac_chegou-1));
			}
			else{
				System.out.println("\nNão houve perdas de pacotes!\nArquivo recebido com sucesso!\n");
			}

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

