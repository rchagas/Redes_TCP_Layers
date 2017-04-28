	import java.io.*;
	import java.io.FileOutputStream;
	import java.io.IOException;
	import java.io.InputStream;
	import java.net.*;
	 
	public class ClientTransfer {
	private static String getMacAddress() throws SocketException, UnknownHostException {
        NetworkInterface netInter = NetworkInterface.getByInetAddress(InetAddress.getLocalHost());
        byte[] macAddressBytes = netInter.getHardwareAddress();
        String macAddress = String.format("%1$02x-%2$02x-%3$02x-%4$02x-%5$02x-%6$02x",
                macAddressBytes[0], macAddressBytes[1],
                macAddressBytes[2], macAddressBytes[3],
                macAddressBytes[4], macAddressBytes[5]).toUpperCase();
        return macAddress;
    }
	    public static void main(String[] args) {
	 
	        //Criando Classe cliente para receber arquivo
	        ClientTransfer cliente = new ClientTransfer();
	 
	        //Solicitando arquivo
	        cliente.getFileFromServeR();
	    }
	 
	    private void getFileFromServeR() {
	        Socket sockServer = null;
	        FileOutputStream fos = null;
	        InputStream is = null;
	 
	        try {
	            // Criando conexão com o servidor
	            System.out.println("Conectando com Servidor porta 13267");
	            sockServer = new Socket("127.0.0.1", 13267);
	            is = sockServer.getInputStream();

		   //=========
		   InetAddress address = InetAddress.getLocalHost();  
		   NetworkInterface ni = NetworkInterface.getByInetAddress(address);  
		   byte[] mac = ni.getHardwareAddress();
		   String macAddress = "";
		   for (int i = 0; i < mac.length; i++) {             
		       macAddress += (String.format("%02X-", mac[i]));  
		   }
		   System.out.println(macAddress.substring(0, macAddress.length()-1));
		    //========
		    
		    
	 
	            // Cria arquivo local no cliente
	            fos = new FileOutputStream(new File("/home/liberio/Downloads/ArqRecebido"));
	            System.out.println("/home/liberio/Downloads/ArqRecebido");
	             
	            // Prepara variaveis para transferencia
	            byte[] cbuffer = new byte[1];
	            int bytesRead;
	 
	            // Copia conteudo do canal
	            System.out.println("Recebendo arquivo...");
	            while ((bytesRead = is.read(cbuffer)) != -1) {
	                fos.write(cbuffer, 0, bytesRead);
	                fos.flush();
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

