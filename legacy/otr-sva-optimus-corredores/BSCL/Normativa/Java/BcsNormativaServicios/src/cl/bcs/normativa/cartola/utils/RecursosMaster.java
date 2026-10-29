package cl.bcs.normativa.cartola.utils;

import java.util.ArrayList;
import java.util.List;

public class RecursosMaster {
	
	List<RecursosDef> recursos;
	
	public static class RecursosDef {
		public String	Tipo;
		public String	Nombre;
		public String	NombreArchivo;
	    public String	Descripcion;
	    public String	Grupo;
	    public String	Editable;
	    public String	Valor;
	    public String 	Key;
	};
	
	public List<RecursosDef> getRecursosMaster () {
		return recursos;
	};
	
	public RecursosMaster () {
		recursos = new ArrayList<RecursosDef>();
		
		RecursosDef LogoCorredora = new RecursosDef();
		LogoCorredora.Tipo			= "IMAGEN";
		LogoCorredora.Nombre		= "LogoCorredora";
		LogoCorredora.NombreArchivo	= "LogoCorredora.png";
		LogoCorredora.Descripcion	= "Logo de la corredora";
		LogoCorredora.Grupo			= "SISTEMA";
		LogoCorredora.Editable		= "SI";
		LogoCorredora.Key			= "LOGO_CORREDOR";
		recursos.add(LogoCorredora);

		RecursosDef LogoBCS = new RecursosDef();
		LogoBCS.Tipo				= "IMAGEN";
		LogoBCS.Nombre				= "STI_logotipo";
		//LogoBCS.NombreArchivo		= "logo_mbi.png";
		LogoBCS.Descripcion			= "Logotipo del encabezado";
		LogoBCS.Grupo				= "SISTEMA";
		LogoBCS.Editable			= "SI";
		LogoBCS.Key					= "LOGO_CORREDOR";
		recursos.add(LogoBCS);
		
		RecursosDef Banner = new RecursosDef();
		Banner.Tipo					= "IMAGEN";
		Banner.Nombre				= "BAN_imagen";
		Banner.NombreArchivo		= "banner.png";
		Banner.Descripcion			= "Banner de la portada";
		Banner.Grupo				= "SISTEMA";
		Banner.Editable				= "SI";
		Banner.Key					= "BANNER_CARTOLA_CORREDOR";
		recursos.add(Banner);
	};
};
