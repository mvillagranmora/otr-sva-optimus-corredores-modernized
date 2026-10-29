package cl.bcs.loaderapp.factorys;

import cl.bcs.loaderapp.concrete.entidadmercado.CuponDbLoader;
import cl.bcs.loaderapp.concrete.entidadmercado.InstrumentoDbLoader;
import cl.bcs.loaderapp.concrete.facturacion.DetalleMovimientoPolizaDbLoader;
import cl.bcs.loaderapp.concrete.facturacion.DetallePolizaDbLoader;
import cl.bcs.loaderapp.concrete.facturacion.MovimientoPolizaDbLoader;
import cl.bcs.loaderapp.concrete.facturacion.PolizaDbLoader;
import cl.bcs.loaderapp.concrete.portafolio.PortafolioDbLoader;
import cl.bcs.loaderapp.concrete.portafolio.cuentacustodio.CuentaCustodioDbLoader;
import cl.bcs.loaderapp.concrete.posiciones.cartera.CarteraDbLoader;
import cl.bcs.loaderapp.concrete.posiciones.cartera.MovimientoCarteraDbLoader;
import cl.bcs.loaderapp.concrete.posiciones.subyacente.SubyacenteDbLoader;
import cl.bcs.loaderapp.concrete.posiciones.transaccion.OpcionDbLoader;
import cl.bcs.loaderapp.concrete.socionegocio.anotacion.AnotacionDbLoader;
import cl.bcs.loaderapp.concrete.socionegocio.banco.BancoDbLoader;
import cl.bcs.loaderapp.concrete.socionegocio.bolsa.BolsaDbLoader;
import cl.bcs.loaderapp.concrete.socionegocio.boveda.BovedaDbLoader;
import cl.bcs.loaderapp.concrete.socionegocio.cliente.ClienteDbLoader;
import cl.bcs.loaderapp.concrete.socionegocio.documento.DocumentoDbLoader;
import cl.bcs.loaderapp.concrete.socionegocio.emisor.EmisorDbLoader;
import cl.bcs.loaderapp.concrete.socionegocio.fatca.FatcaDbLoader;
import cl.bcs.loaderapp.concrete.socionegocio.indicador.IndicadorClienteDbLoader;
import cl.bcs.loaderapp.concrete.socionegocio.indicador.IndicadorPersonaDbLoader;
import cl.bcs.loaderapp.concrete.socionegocio.indicador.PreferenciaPortafolioDbLoader;
import cl.bcs.loaderapp.concrete.socionegocio.indicador.RequisitosClienteDbLoader;
import cl.bcs.loaderapp.concrete.socionegocio.origenfondos.OrigenFondoDbLoader;
import cl.bcs.loaderapp.concrete.socionegocio.persona.PersonaDbLoader;
import cl.bcs.loaderapp.concrete.socionegocio.proveedor.ProveedorDbLoader;
import cl.bcs.loaderapp.concrete.socionegocio.relacionpersona.RelacionPersonaDbLoader;
import cl.bcs.loaderapp.concrete.socionegocio.relacionpersonaextra.RelacionPersonaExtraDbLoader;
import cl.bcs.loaderapp.interfaces.DbLoader;

public class DbLoaderFactory {
	String entidad;

	public DbLoaderFactory(String entidad) {
		super();
		this.entidad = entidad;
	}

	public DbLoader getConcrete() {
		DbLoader loader = null;
		switch (entidad) {
		case "CLIENTE":
			loader = new ClienteDbLoader();
			break;
		case "EMISOR":
			loader = new EmisorDbLoader();
			break;
		case "DOCUMENTO":
			loader = new DocumentoDbLoader();
			break;
		case "ANOTACION":
			loader = new AnotacionDbLoader();
			break;
		case "FATCA":
			loader = new FatcaDbLoader();
			break;
		case "ORIGENFONDO":
			loader = new OrigenFondoDbLoader();
			break;
		case "CUENTACUSTODIO":
			loader = new CuentaCustodioDbLoader();
			break;
		case "PORTAFOLIO":
			loader = new PortafolioDbLoader();
			break;
		case "INSTRUMENTO":
			loader = new InstrumentoDbLoader();
			break;
		
		case "SUBYACENTE":
			loader = new SubyacenteDbLoader();
			break;
		case "MOVIMIENTO_CARTERA":
			loader = new MovimientoCarteraDbLoader();
			break;	
		case "OPCIONES":
			loader = new OpcionDbLoader();
			break;
		case "POLIZA":
			loader = new PolizaDbLoader();
			break;
		case "DETALLEPOLIZA":
			loader = new DetallePolizaDbLoader();
			break;
		case "MOVIMIENTOPOLIZA":
			loader = new MovimientoPolizaDbLoader();
			break;
		case "DETALLEMOVIMIENTOPOLIZA":
			loader = new DetalleMovimientoPolizaDbLoader();
			break;			
			
			
		case "PERSONA":
			loader = new PersonaDbLoader();
			break;
		case "RELACIONPERSONA":
			loader = new RelacionPersonaDbLoader();
			break;
		case "RELACIONEXTRA":
			loader = new RelacionPersonaExtraDbLoader();
			break;
			
		case "PROVEEDOR":
			loader = new ProveedorDbLoader();
			break;
		case "BANCO":
			loader = new BancoDbLoader();
			break;
		case "BOLSA":
			loader = new BolsaDbLoader();
			break;	
		case "BOVEDA":
			loader = new BovedaDbLoader();
			break;
			
		case "REQUISITOSCLIENTE":
			loader = new RequisitosClienteDbLoader();
			break;	
		case "INDICADORESCLIENTE":
			loader = new IndicadorClienteDbLoader();
			break;
		case "INDICADORESPERSONA":
			loader = new IndicadorPersonaDbLoader();
			break;
		case "PREFERENCIASPORTAFOLIO":
			loader = new PreferenciaPortafolioDbLoader();
			break;
		case "CUPONES":
			loader = new CuponDbLoader();
			break;	
		case "GARANTIA":
		case "REPORTE":
		case "POSICION":
			loader = new CarteraDbLoader();
			break;	
			
		default:
			break;
		}
		return loader;
	}
}
