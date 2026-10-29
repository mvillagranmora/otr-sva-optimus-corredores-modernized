package cl.bcs.normativa.cartola;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cl.bcs.normativa.cartola.ResumenInversionBase.Portafolio;
import cl.bcs.plataforma.pla.utils.FechaDB;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.Log;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CodigoPDF417 implements BcsCartola{
	
	public final Log log = new Log("NOR.CodigoPDF417");
	
	private final String SQL_SELECT_DATOS_CLIENTE = loadResourceToString("cl/bcs/normativa/cartola/sql/CodigoPDF417_SelectDatosCliente.sql");
	private final String SQL_INSERT_PORTAFOLIOS_LEGACY = loadResourceToString("cl/bcs/normativa/cartola/sql/CartolaGenerico_InsertPortafoliosLegacy.sql");
	private final String SQL_DELETE_PORTAFOLIOS_LEGACY = loadResourceToString("cl/bcs/normativa/cartola/sql/CartolaGenerico_DeletePortafoliosLegacy.sql");
	
	public static class SalidaCodigoPDF417{
		public Long IdCliente;
        public Long IdPortafolio;
        public Timestamp fecha;
        public String pais;
        public String complemento;
        public String ciudadCodBarra;
        public String direccionCodBarra;
        public String nombre;
        public String cuenta;
        public String comunaCodBarra;
        public String numeroCodBarra;
        public String rut;
                
        public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class DatoCliente{
		public Long IdCliente;
		public Long IdPortafolio;
		public String NombrePais;
		public String Departamento;
		public String NombreCiudad;
		public String Direccion;
		public String NombreCompleto;
		public String CodigoPortafolio;
		public String NombreDivisionPais;
		public String Numero;
		public String DocumentoIdentidad;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class CargoAbono{
		public Long IdCliente;
		public Long IdPortafolio;
		public BigDecimal Cargo;
		public BigDecimal Abono;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	
	public SalidaCartola procesar(Connection con,EntradaCartola entrada) throws Exception{
		log.write("INICIO::CodigoPDF417");
		SQL sql = new SQL();
		SalidaCartola out = new SalidaCartola();
		
		Long idTrabajo = new Double(Math.random()*10000000000L).longValue();
		List<Portafolio> portObj = new ArrayList<>();
		List<Long> portafolios = new ArrayList<Long>();
		for(Dato dato : entrada.p_Datos){
			portafolios.add(dato.IdPortafolio);
			
			Portafolio por = new Portafolio();
			por.IdTrabajo = idTrabajo;
			por.IdPortafolio = dato.IdPortafolio;
			portObj.add(por);
			
		}
		
		sql.batch(con, SQL_INSERT_PORTAFOLIOS_LEGACY, portObj, Portafolio.class);
		
		
		
		
		HashMap<String,Object> params = new HashMap<String,Object>();
		
		
		
		
		List<DatoCliente> datosCliente = sql.query(con, SQL_SELECT_DATOS_CLIENTE, params, DatoCliente.class);
		Timestamp fechaTS = FechaDB.ObtenerFechaHoraSistemaTs(con, entrada.p_IdEmpresa);
		List<SalidaCodigoPDF417> salidas = new ArrayList<SalidaCodigoPDF417>();
		for(DatoCliente datoCliente : datosCliente) {
			SalidaCodigoPDF417 salidaCodigoPDF417 = new SalidaCodigoPDF417();
			salidaCodigoPDF417.IdCliente = datoCliente.IdCliente;
			salidaCodigoPDF417.IdPortafolio = datoCliente.IdPortafolio;
			salidaCodigoPDF417.pais = datoCliente.NombrePais;
			salidaCodigoPDF417.complemento = datoCliente.Departamento;
			salidaCodigoPDF417.ciudadCodBarra = datoCliente.NombreCiudad;
			salidaCodigoPDF417.direccionCodBarra = datoCliente.Direccion;
			salidaCodigoPDF417.nombre = datoCliente.NombreCompleto;
			salidaCodigoPDF417.cuenta = datoCliente.CodigoPortafolio;
			salidaCodigoPDF417.comunaCodBarra = datoCliente.NombreDivisionPais;
			salidaCodigoPDF417.numeroCodBarra = datoCliente.Numero;
			salidaCodigoPDF417.rut = datoCliente.DocumentoIdentidad;
			salidaCodigoPDF417.fecha = fechaTS;
			salidas.add(salidaCodigoPDF417);
		}
				
		List<HashMap<String,Object>> resultadoCodigoPDF417 = Utils.List2ListHashGenericObject(salidas);
		SQL.exec(con, SQL_DELETE_PORTAFOLIOS_LEGACY.replace(":IdTrabajo", idTrabajo+""));

		out.P_OUT_SALIDA.put("codigoPDF417", resultadoCodigoPDF417);
		
		log.write("FIN::CodigoPDF417");
		return out;
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getDESAConnection()) {
		EntradaCartola Objin = new EntradaCartola();
		Objin.p_IdEmpresa = 1L;
		Objin.p_IdModulo = "NOR";
		Objin.p_IdUsuario = 21L;
		Objin.p_FechaDesde = Fecha.toDate("2018-07-01");
		Objin.p_FechaHasta = Fecha.toDate("2018-07-31");
		Dato dato = new Dato();
		dato.IdCliente = 524891L;
		dato.IdPortafolio = 512341L;
		Objin.p_Datos.add(dato);
		CodigoPDF417  demo= new CodigoPDF417();				
		try {
			SalidaCartola ObjOut=demo.procesar(con, Objin);
			con.commit();
			System.out.println(ObjOut.toString());
		}
		catch(Exception er) {
			er.printStackTrace();	
			con.rollback();
		}
			con.rollback(); 			
    	}
		System.out.println("== END ==");
	}

}
