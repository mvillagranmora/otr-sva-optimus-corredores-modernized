package cl.bcs.contabilidad;


import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpException;
import java.io.IOException;
import java.util.Vector;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class SFTPConnector {

    
	/**
     * Sesion SFTP establecida.
     */
    private Session session;
 
    /**
     * Establece una conexion SFTP.
     *
     * @param username Nombre de usuario.
     * @param password Contrasena.
     * @param host     Host a conectar.
     * @param port     Puerto del Host.
     *
     * @throws JSchException          Cualquier error al establecer
     *                                conexión SFTP.
     * @throws IllegalAccessException Indica que ya existe una conexion
     *                                SFTP establecida.
     */
    public void connect(String username, String password, String host, int port)
        throws JSchException, IllegalAccessException {
        if (this.session == null || !this.session.isConnected()) {
            JSch jsch = new JSch();
 
            this.session = jsch.getSession(username, host, port);
            this.session.setPassword(password);
            this.session.setTimeout(500000);
            // Parametro para no validar key de conexion.
            this.session.setConfig("StrictHostKeyChecking", "no");
 
            this.session.connect();
        } else {
            throw new IllegalAccessException("Sesion SFTP ya iniciada.");
        }
    }
 
    /**
     * Añade un archivo al directorio FTP usando el protocolo SFTP.
     *
     * @param ftpPath  Path del FTP donde se agregará el archivo.
     * @param filePath Directorio donde se encuentra el archivo a subir en
     *                 disco.
     * @param fileName Nombre que tendra el archivo en el destino.
     *
     * @throws IllegalAccessException Excepción lanzada cuando no hay
     *                                conexión establecida.
     * @throws JSchException          Excepción lanzada por algún
     *                                error en la ejecución del comando
     *                                SFTP.
     * @throws SftpException          Error al utilizar comandos SFTP.
     * @throws IOException            Excepción al leer el texto arrojado
     *                                luego de la ejecución del comando
     *                                SFTP.
     */
    public final void addFile(String ftpPath, String filePath,
        String fileName) throws IllegalAccessException, IOException,
        SftpException, JSchException {
        if (this.session != null && this.session.isConnected()) {
 
            // Abrimos un canal SFTP. Es como abrir una consola.
            ChannelSftp channelSftp = (ChannelSftp) this.session.
                openChannel("sftp");
 
            // Nos ubicamos en el directorio del FTP.
            //channelSftp.cd(ftpPath);
            channelSftp.connect();
 
            System.out.println(String.format("Creando archivo %s en el " +
                "directorio %s", fileName, ftpPath));
            channelSftp.put(filePath, fileName);
 
            System.out.println("Archivo subido exitosamente");
 
            channelSftp.exit();
            channelSftp.disconnect();
        } else {
            throw new IllegalAccessException("No existe sesion SFTP iniciada.");
        }
    }
    
    public final void addMultipleFile(String sftpDirectory, String fileDirectory) throws IllegalAccessException, IOException,
            SftpException, JSchException {
            if (this.session != null && this.session.isConnected()) {
     
                // Abrimos un canal SFTP. Es como abrir una consola.
                ChannelSftp channelSftp = (ChannelSftp) this.session.
                    openChannel("sftp");
     
                // Nos ubicamos en el directorio del FTP.
                //channelSftp.cd(sftpDirectory);
                channelSftp.connect();
     
                //System.out.println(String.format("Creando archivo %s en el " +
                  //  "directorio %s", sftpDirectory));
                
                
                File directory = new File(fileDirectory);
                File[] fList = directory.listFiles();

                for (File file : fList){           
                    if (file.isFile()){
                        String filename=file.getAbsolutePath();
                        channelSftp.put(filename, sftpDirectory, ChannelSftp.OVERWRITE);
                        System.out.println(filename + " transferred to " + sftpDirectory );
                    }
                }
     
                System.out.println("Archivos subidos exitosamente");
     
                channelSftp.exit();
                channelSftp.disconnect();
            }
            else {
                throw new IllegalAccessException("No existe sesion SFTP iniciada.");
            }
        }
    
    public void downloadDir(String sftpDirectory, String fileDirectory) throws IllegalAccessException, IOException,
    SftpException, JSchException, FileNotFoundException {
    if (this.session != null && this.session.isConnected()) {

        // Abrimos un canal SFTP. Es como abrir una consola.
        ChannelSftp channelSftp = (ChannelSftp) this.session.
            openChannel("sftp");

        // Nos ubicamos en el directorio del FTP.
        //channelSftp.cd(sftpDirectory);
        channelSftp.connect();
        
        lsFolderCopy(fileDirectory,  sftpDirectory, channelSftp);

              
        //System.out.println(String.format("Creando archivo %s en el " +
          //  "directorio %s", sftpDirectory));
              

        System.out.println("Archivos subidos exitosamente");

        channelSftp.exit();
        channelSftp.disconnect();
    } else {
        throw new IllegalAccessException("No existe sesion SFTP iniciada.");
    }
}
    private void lsFolderCopy(String sourceDirectory , String destPath, ChannelSftp clientChannel) throws SftpException, FileNotFoundException{
    	SftpATTRS attrs;
    	File localFile = new File(sourceDirectory);
    	  	
        
    	if(localFile.isDirectory()){
    		
    		try {
    			destPath = destPath + "/" + localFile.getName();
                attrs = clientChannel.stat(destPath);
            }
    		catch (Exception e) 
    		{
            	clientChannel.mkdir(localFile.getName());
            }
    		 /*
    		 if(clientChannel.stat(destPath).isDir())
    		 {
    			 clientChannel.mkdir(localFile.getName());
    		 }
				*/		
			//destPath = destPath + "/" + localFile.getName();
			clientChannel.cd(destPath);
			
			for(File file: localFile.listFiles()){
				lsFolderCopy(file.getPath(), destPath, clientChannel);
			}
			clientChannel.cd(destPath.substring(0, destPath.lastIndexOf('/')));
		} 
    	else {
			
			clientChannel.put(new FileInputStream(localFile), localFile.getName(),ChannelSftp.OVERWRITE);
		}

	}        
    public void removeDir(String sftpDirectory, String fileDirectory) throws IllegalAccessException, IOException,
    SftpException, JSchException, FileNotFoundException {
    	
    	SftpATTRS attrs;
    	if (this.session != null && this.session.isConnected()) {

            // Abrimos un canal SFTP. Es como abrir una consola.
            ChannelSftp clientChannel = (ChannelSftp) this.session.
                openChannel("sftp");

            // Nos ubicamos en el directorio del FTP.
            //channelSftp.cd(sftpDirectory);
            clientChannel.connect();
            
            File localFile = new File(fileDirectory);
            
            
            try {
            	sftpDirectory = sftpDirectory  + localFile.getName();
                attrs = clientChannel.stat(sftpDirectory);
                //clientChannel.rm(sftpDirectory);
                delete(sftpDirectory, clientChannel);
            }
    		catch (Exception e) 
    		{

            }
        	


            clientChannel.exit();
            clientChannel.disconnect();
        } else {
            throw new IllegalAccessException("No existe sesion SFTP iniciada.");
        }
}
    
    public void delete(String sftpDirectory, ChannelSftp sftpChannel) throws IllegalAccessException, IOException,
    SftpException, JSchException, FileNotFoundException {
    if (this.session != null && this.session.isConnected()) {

        // Abrimos un canal SFTP. Es como abrir una consola.

        if(sftpChannel.stat(sftpDirectory).isDir()){
			@SuppressWarnings("unchecked")
			Vector<LsEntry> fileList = sftpChannel.ls(sftpDirectory);
			sftpChannel.cd(sftpDirectory);
			int size = fileList.size();
			for(int i = 0; i < size; i++){
				if(!fileList.get(i).getFilename().startsWith(".")){
					delete(fileList.get(i).getFilename(), sftpChannel);
				}
			}
			sftpChannel.cd("..");
			sftpChannel.rmdir(sftpDirectory);
		} else {
			sftpChannel.rm(sftpDirectory.toString());
		}
		
        /*
        sftpChannel.rmdir(sftpDirectory); // Finally remove the required dir.
        sftpChannel.exit();
        sftpChannel.disconnect();
        */
    } else {
        throw new IllegalAccessException("No existe sesion SFTP iniciada.");
    }
}

    /**
     * Cierra la sesion SFTP.
     */
    public final void disconnect() {
        this.session.disconnect();
    }
}
