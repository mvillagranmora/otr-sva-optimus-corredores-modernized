package cl.bcs.plataforma.services.filemanager;

import cl.bcs.plataforma.services.filemanager.impl.AnotacionClienteConcrete;
import cl.bcs.plataforma.services.filemanager.impl.AnotacionPortafolioConcrete;
import cl.bcs.plataforma.services.filemanager.impl.AnotacionRecaudacionAPV;

public class FileBlobManagerFactory{
	
	public static FileBlobManager getManager(String uploader){
		if(uploader.equals("ANOTACION_PORTAFOLIO")){
			return new AnotacionPortafolioConcrete();
		}else if(uploader.equals("ANOTACION_CLIENTE")){
			return new AnotacionClienteConcrete();
		}else if(uploader.equals("ANOTACION_RECAUDACION_APV")){
			return new AnotacionRecaudacionAPV();
		}
		return null;
	}

}
