package cl.bcs.loaderapp.factorys;

import cl.bcs.loaderapp.concrete.GenericFileLoader;
import cl.bcs.loaderapp.concrete.entidadmercado.InstrumentoFileLoader;
import cl.bcs.loaderapp.concrete.portafolio.PortafolioFileLoader;
import cl.bcs.loaderapp.concrete.portafolio.cuentacustodio.CuentaCustodioFileLoader;
import cl.bcs.loaderapp.concrete.posiciones.cartera.CarteraFileLoader;
import cl.bcs.loaderapp.concrete.socionegocio.anotacion.AnotacionFileLoader;
import cl.bcs.loaderapp.concrete.socionegocio.cliente.ClienteFileLoader;
import cl.bcs.loaderapp.concrete.socionegocio.documento.DocumentoFileLoader;
import cl.bcs.loaderapp.concrete.socionegocio.emisor.EmisorFileLoader;
import cl.bcs.loaderapp.concrete.socionegocio.fatca.FatcaFileLoader;
import cl.bcs.loaderapp.concrete.socionegocio.origenfondos.OrigenFondoFileLoader;
import cl.bcs.loaderapp.concrete.socionegocio.persona.PersonaFileLoader;
import cl.bcs.loaderapp.concrete.socionegocio.proveedor.ProveedorFileLoader;
import cl.bcs.loaderapp.concrete.socionegocio.relacionpersona.RelacionPersonaFileLoader;
import cl.bcs.loaderapp.concrete.socionegocio.relacionpersonaextra.RelacionPersonaExtraFileLoader;
import cl.bcs.loaderapp.interfaces.FileLoader;

public class FileLoaderFactory {
	String entidad;

	public FileLoaderFactory(String entidad) {
		super();
		this.entidad = entidad;
	}
	
	public FileLoader getConcrete(){
		FileLoader loader = null;
		switch (entidad) {
		case "CLIENTE":
			loader = new ClienteFileLoader();
			break;
		case "EMISOR":
			loader = new EmisorFileLoader();
			break;
		case "DOCUMENTO":
			loader = new DocumentoFileLoader();
			break;
		case "ANOTACION":
			loader = new AnotacionFileLoader();
			break;
		case "FATCA":
			loader = new FatcaFileLoader();
			break;
		case "ORIGENFONDO":
			loader = new OrigenFondoFileLoader();
			break;
		case "CUENTACUSTODIO":
			loader = new CuentaCustodioFileLoader();
			break;
		case "PORTAFOLIO":
			loader = new PortafolioFileLoader();
			break;
		case "INSTRUMENTO":
			loader = new InstrumentoFileLoader();
			break;
			
			
		case "PERSONA":
			loader = new PersonaFileLoader();
			break;
		case "RELACIONPERSONA":
			loader = new RelacionPersonaFileLoader();
			break;
		case "RELACIONEXTRA":
			loader = new RelacionPersonaExtraFileLoader();
			break;
		case "PROVEEDOR":
			loader = new ProveedorFileLoader();
			break;
			
		case "GARANTIA":
		case "POSICION":
		case "REPORTE":
			loader = new CarteraFileLoader();
			break;	
			//SUBYACTENTE, CARTERA
		default:
			loader = new GenericFileLoader();
			break;
		}
		return loader;
	}

}
