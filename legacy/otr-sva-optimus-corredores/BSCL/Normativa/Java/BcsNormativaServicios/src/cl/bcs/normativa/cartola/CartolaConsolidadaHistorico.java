package cl.bcs.normativa.cartola;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.normativa.cartola.ConsultarCartolaOnline.ConsultarCartolaOnline_Entrada;
import cl.bcs.normativa.cartola.ConsultarCartolaOnline.ConsultarCartolaOnline_Salida;
import cl.bcs.normativa.cartola.ConsultarCartolaOnline.DatosCliente;
import cl.bcs.normativa.cartola.FlujoPatrimonialYRentabilidad.SalidaPauta;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.plataforma.pla.utils.PlataformaUtils.MensajeSalida;
import cl.bcs.utils.JavaCall;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class CartolaConsolidadaHistorico implements JavaCall{
	
	static final Logger logger = LogManager.getLogger(CartolaConsolidadaHistorico.class);
	
	private final String SQL_SELECT_CLIENTES = loadResourceToString("cl/bcs/normativa/cartola/sql/HistoricoCartolaConsolidada_SelectClientes.sql");
	private final String SQL_INSERT_CLIENTES_CARTOLA = loadResourceToString("cl/bcs/normativa/cartola/sql/HistoricoCartolaConsolidada_InsertClientes.sql");
	private final String SQL_INSERT_MERCADOS_CLIENTES_CARTOLA = loadResourceToString("cl/bcs/normativa/cartola/sql/HistoricoCartolaConsolidada_InsertMercadosClientes.sql");
	private final String SQL_INSERT_INSTRUMENTOS_CLIENTES_CARTOLA = loadResourceToString("cl/bcs/normativa/cartola/sql/HistoricoCartolaConsolidada_InsertInstrumentosClientes.sql");
	
	
	private final String SQL_DELETE_CLIENTES_CARTOLA = loadResourceToString("cl/bcs/normativa/cartola/sql/HistoricoCartolaConsolidada_DeleteClientes.sql");
	private final String SQL_DELETE_MERCADOS_CLIENTES_CARTOLA = loadResourceToString("cl/bcs/normativa/cartola/sql/HistoricoCartolaConsolidada_DeleteMercadosClientes.sql");
	private final String SQL_DELETE_INSTRUMENTOS_CLIENTES_CARTOLA = loadResourceToString("cl/bcs/normativa/cartola/sql/HistoricoCartolaConsolidada_DeleteInstrumentosClientes.sql");
	
	
	
	public static class EntradaGuardarCartolaConsolidada{
		public Long p_IdEmpresa;
		public Long p_IdUsuario;
		public String p_IdModulo;
		public String p_Fecha;
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class SalidaGuardarCartolaConsolidada{
		public Long P_OUT_ERRORESCONTADOR;
		public List <MensajeSalida> P_OUT_MENSAJES;
		
		public SalidaGuardarCartolaConsolidada() {
			this.P_OUT_ERRORESCONTADOR=0L;
			this.P_OUT_MENSAJES = new ArrayList<MensajeSalida>();
		}
		
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class Clientes{
		public String NombreCompleto;
		public Long IdCliente;
		public Long IdPersona;
		public String NombreCategoriaInversionista;
		public Long IdEmpleadoEjecutivo;
		public String NombreEmpleadoEjecutivo;
		public Long IdPortafolio;
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ClientesCartolaHistorica{
		public Long IdCliente;
		public String NombreCliente;
		public Long IdAgente;
		public String NombreAgente;
		public String CategoriaInversionista;
		public String Fecha;
		public BigDecimal TotalPatrimonio;
		public BigDecimal RentabilidadMTD;
		public BigDecimal RentabilidadYTD;
		public BigDecimal RentabilidadITD;
		public BigDecimal AbonoIngresoMTD;
		public BigDecimal AbonoIngresoYTD;
		public BigDecimal AbonoIngresoITD;
		public BigDecimal CargoRetiroMTD;
		public BigDecimal CargoRetiroYTD;
		public BigDecimal CargoRetiroITD;
	
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ClientesCartolaHistoricaMercados{
		public Long IdCliente;
		public String NombreCliente;
		public String Fecha;
		public Long IdInstrumento;
		public String Nemotecnico;
		public String NombreMercadoCartola;
		public String NombreClasificacion;
		public BigDecimal Nominales;
		public BigDecimal Pesos;
		public BigDecimal Porcentaje;
		public BigDecimal TotalPesos;
		public BigDecimal TotalNominales;
		public BigDecimal TotalPorcentaje;
		public BigDecimal TotalPatrimonio;
		public BigDecimal MontoTotalMercado;
		public BigDecimal PorcentajeTotalMercado;

		public ClientesCartolaHistoricaMercados() {
			this.IdCliente = 0L;
			this.NombreCliente = "";
			this.Fecha = "";
			this.IdInstrumento = 0L;
			this.Nemotecnico = "";
			this.NombreClasificacion = "";
			this.NombreMercadoCartola = "";
			this.Pesos = BigDecimal.ZERO;
			this.Nominales = BigDecimal.ZERO;
			this.Porcentaje = BigDecimal.ZERO;
			this.TotalPesos = BigDecimal.ZERO;
			this.TotalNominales = BigDecimal.ZERO;
			this.TotalPorcentaje = BigDecimal.ZERO;
			this.TotalPatrimonio = BigDecimal.ZERO;	
			this.MontoTotalMercado = BigDecimal.ZERO;
			this.PorcentajeTotalMercado = BigDecimal.ZERO;
		}
		public ClientesCartolaHistoricaMercados getSumaMercados() {
			ClientesCartolaHistoricaMercados datosClientes = new ClientesCartolaHistoricaMercados();
			datosClientes.TotalPorcentaje = Porcentaje;
			
			return datosClientes;
		}
		public ClientesCartolaHistoricaMercados getDatosCliente(){
			ClientesCartolaHistoricaMercados datosCliente = new ClientesCartolaHistoricaMercados();
			datosCliente.IdCliente = IdCliente;
			datosCliente.NombreCliente = NombreCliente;
			datosCliente.Fecha = Fecha;
			datosCliente.IdInstrumento = IdInstrumento;
			datosCliente.Nemotecnico = Nemotecnico;
			datosCliente.NombreClasificacion = NombreClasificacion;
			datosCliente.NombreMercadoCartola = NombreMercadoCartola;
			datosCliente.Pesos = Pesos;
			datosCliente.Nominales = Nominales;
			datosCliente.Porcentaje = Porcentaje;
			datosCliente.TotalPesos = TotalPesos;
			datosCliente.TotalNominales = TotalNominales;
			datosCliente.TotalPorcentaje = TotalPorcentaje;
			datosCliente.TotalPatrimonio = TotalPatrimonio;	
			return datosCliente;
		}
		public BigDecimal getMontoTotalMercado() {
			return MontoTotalMercado;
		}
		public void setMontoTotalMercado(BigDecimal montoTotalMercado) {
			MontoTotalMercado = montoTotalMercado;
		}
		public BigDecimal getPorcentajeTotalMercado() {
			return PorcentajeTotalMercado;
		}
		public void setPorcentajeTotalMercado(BigDecimal porcentajeTotalMercado) {
			PorcentajeTotalMercado = porcentajeTotalMercado;
		}
		public Long getIdCliente() {
			return IdCliente;
		}
		public void setIdCliente(Long idCliente) {
			IdCliente = idCliente;
		}
		public String getNombreCliente() {
			return NombreCliente;
		}
		public void setNombreCliente(String nombreCliente) {
			NombreCliente = nombreCliente;
		}
		public String getFecha() {
			return Fecha;
		}
		public void setFecha(String fecha) {
			Fecha = fecha;
		}
		public Long getIdInstrumento() {
			return IdInstrumento;
		}
		public void setIdInstrumento(Long idInstrumento) {
			IdInstrumento = idInstrumento;
		}
		public String getNemotecnico() {
			return Nemotecnico;
		}
		public void setNemotecnico(String nemotecnico) {
			Nemotecnico = nemotecnico;
		}
		public String getNombreMercadoCartola() {
			return NombreMercadoCartola;
		}
		public void setNombreMercadoCartola(String nombreMercadoCartola) {
			NombreMercadoCartola = nombreMercadoCartola;
		}
		public String getNombreClasificacion() {
			return NombreClasificacion;
		}
		public void setNombreClasificacion(String nombreClasificacion) {
			NombreClasificacion = nombreClasificacion;
		}
		public BigDecimal getNominales() {
			return Nominales;
		}
		public void setNominales(BigDecimal nominales) {
			Nominales = nominales;
		}
		public BigDecimal getPesos() {
			return Pesos;
		}
		public void setPesos(BigDecimal pesos) {
			Pesos = pesos;
		}
		public BigDecimal getPorcentaje() {
			return Porcentaje;
		}
		public void setPorcentaje(BigDecimal porcentaje) {
			Porcentaje = porcentaje;
		}
		public BigDecimal getTotalPesos() {
			return TotalPesos;
		}
		public void setTotalPesos(BigDecimal totalPesos) {
			TotalPesos = totalPesos;
		}
		public BigDecimal getTotalNominales() {
			return TotalNominales;
		}
		public void setTotalNominales(BigDecimal totalNominales) {
			TotalNominales = totalNominales;
		}
		public BigDecimal getTotalPorcentaje() {
			return TotalPorcentaje;
		}
		public void setTotalPorcentaje(BigDecimal totalPorcentaje) {
			TotalPorcentaje = totalPorcentaje;
		}
		public BigDecimal getTotalPatrimonio() {
			return TotalPatrimonio;
		}
		public void setTotalPatrimonio(BigDecimal totalPatrimonio) {
			TotalPatrimonio = totalPatrimonio;
		}
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	public static class ClientesCartolaHistoricaFlujo{
		public Long IdCliente;
		public BigDecimal SaldoInicialMTD;
		public BigDecimal SaldoInicialYTD;
		public BigDecimal SaldoInicialITD;
		public BigDecimal AportesMTD;
		public BigDecimal AportesYTD;
		public BigDecimal AportesITD;
		public BigDecimal RescatesMTD;
		public BigDecimal RescatesYTD;
		public BigDecimal RescatesITD;
		public BigDecimal UtilidadMTD;
		public BigDecimal UtilidadYTD;
		public BigDecimal UtilidadITD;
		public BigDecimal SaldoFinalMTD;
		public BigDecimal SaldoFinalYTD;
		public BigDecimal SaldoFinalITD;
		public BigDecimal RentabilidadMTD;
		public BigDecimal RentabilidadYTD;
		public BigDecimal RentabilidadITD;
		
		@Override
		public String toString() {
			return Utils.printJson(this, false);
		}
	}
	
	@Override
	public JsonObject call(Connection conn, JsonObject in) throws Exception {
		Gson gson = new Gson();		
		EntradaGuardarCartolaConsolidada jin = gson.fromJson(in.toString(), EntradaGuardarCartolaConsolidada.class);
		SalidaGuardarCartolaConsolidada out = logica(conn,jin);
		
		JsonParser parser = new JsonParser();
        JsonObject dataout = parser.parse(gson.toJson(out)).getAsJsonObject();
		return dataout;
	}
	
	
	
	// ----------------------------------------------------------------
	// LOGICA
	// ----------------------------------------------------------------
	public SalidaGuardarCartolaConsolidada logica(Connection con, EntradaGuardarCartolaConsolidada entrada) throws Exception {
		logger.info("INICIO::EntradaGuardarCartolaConsolidadaHistorico");
		SQL sql=new SQL();
		SalidaGuardarCartolaConsolidada out = new SalidaGuardarCartolaConsolidada();
		String fechaSistema = Utils.getFechaSistema(con, Utils.getSchema(), entrada.p_IdEmpresa);
		LocalDate fechaActual = LocalDate.parse(fechaSistema);
	    LocalDate fechaMTDDate = LocalDate.of(fechaActual.getYear(), fechaActual.getMonth(), 1);
		ConsultarCartolaOnline cartolaConsolidada = new ConsultarCartolaOnline();
		ConsultarCartolaOnline_Entrada datosEntrada = new ConsultarCartolaOnline_Entrada();
		ConsultarCartolaOnline_Salida resultadoCartola = new ConsultarCartolaOnline_Salida();
		Set<Long> listClientes = new HashSet<>();
		List<DatosCliente> listaDatosCliente = new ArrayList<>();
		List<ClientesCartolaHistoricaMercados> listClientesMercados = new ArrayList<>();
		List<ClientesCartolaHistoricaMercados> listClientesInstrumentos = new ArrayList<>();
		List<ClientesCartolaHistoricaMercados> composicionCarteraCliente = new ArrayList<>();
		List<ClientesCartolaHistorica> listClientesCartola = new ArrayList<>();
		List<HashMap<String, Object>> flujoPatrimonialCliente = new ArrayList<>();
		HashMap<Long, BigDecimal> patrimonioCliente = new HashMap<>();
		HashMap<Long, Clientes> datosClientes = new HashMap<>();
		
// 11. OBTIENE LOS DATOS DE PORTAFOLIOS/CLIENTES DE TODOS LOS CLIENTES		
		List<Clientes> listaClientes = sql.query(con, SQL_SELECT_CLIENTES, new HashMap<>(), Clientes.class);
		Long idAnterior = 0L;
		Long cont = 0L;
		Long contTotal = 0L;
	
		
// 22. RECORRE LOS DATOS DE TODOS LOS CLIENTES			
		for(Clientes cliente: listaClientes) {
			contTotal++;
			if(!idAnterior.equals(cliente.IdCliente)) {
				cont++;
				idAnterior = cliente.IdCliente;
			}
			
// 33. ACUMULA DE A 3000 CLIENTES PARA ENVIAR A CONSULTAR LA CARTOLA EN GRUPOS
			if(cont < 3000 ) {
				DatosCliente datosCliente = new DatosCliente();
				datosCliente.IdCliente = cliente.IdCliente;
				datosCliente.IdPortafolio = cliente.IdPortafolio;
				listaDatosCliente.add(datosCliente);
				listClientes.add(cliente.IdCliente);
				datosClientes.put(cliente.IdCliente, cliente);
			} 
			
// 44. CUANDO YA HAY UN GRUPO DE 3000 CLIENTES O SE LLEGA AL MAXIMO SE CONSULTA LA CARTOLA		
			if(cont == 3000 || contTotal ==  listaClientes.size()) {
				cont = 0L;
				datosEntrada.p_IdEmpresa = entrada.p_IdEmpresa;
				datosEntrada.p_IdUsuario = entrada.p_IdUsuario;
				datosEntrada.p_IdModulo = entrada.p_IdModulo;
				datosEntrada.p_FechaDesde  = fechaMTDDate.toString() ;
				datosEntrada.p_FechaHasta = fechaSistema;
				datosEntrada.p_Datos = listaDatosCliente;
				datosEntrada.esPauta = true;
// 44. CONSULTA LA CARTOLA (en grupos de 3000)			
				//consulta a la cartola
				resultadoCartola = cartolaConsolidada.logica(con, datosEntrada);
				out.P_OUT_MENSAJES.add(resultadoCartola.P_OUT_MENSAJES.get(0));
				out.P_OUT_ERRORESCONTADOR += resultadoCartola.P_OUT_ERRORESCONTADOR;
				
				for( HashMap<String, Object> clienteCarteraInversion: resultadoCartola.P_OUT_SALIDA.get("carteraInversiones")) {
					ClientesCartolaHistoricaMercados clienteHistoricoMercados = new ClientesCartolaHistoricaMercados();
						clienteHistoricoMercados.IdCliente = (Long) clienteCarteraInversion.get("idCliente");
						clienteHistoricoMercados.NombreMercadoCartola = clienteCarteraInversion.get("Mercado").toString();
						clienteHistoricoMercados.NombreClasificacion = clienteCarteraInversion.get("Clasificacion").toString();
						clienteHistoricoMercados.IdInstrumento = (Long) clienteCarteraInversion.get("IdInstrumento");
						clienteHistoricoMercados.Nemotecnico = clienteCarteraInversion.get("emisor").toString();
						clienteHistoricoMercados.Nominales = (BigDecimal) clienteCarteraInversion.get("cantidad");
						clienteHistoricoMercados.Porcentaje = (BigDecimal) clienteCarteraInversion.get("valorMercadoCLP");
						clienteHistoricoMercados.TotalPesos = (BigDecimal) clienteCarteraInversion.get("valorMercadoCLP");
						clienteHistoricoMercados.TotalNominales = (BigDecimal) clienteCarteraInversion.get("cantidad");
						composicionCarteraCliente.add(clienteHistoricoMercados);
				}
				
// *** OBTIENE LA SALIDA DE LA CARTOLA PARA "FLUJO PATRIMONIAL"
				flujoPatrimonialCliente.addAll(resultadoCartola.P_OUT_SALIDA.get("flujoPatrimonial"));
				
// *** RECORRE LOS CLIENTES DEL GRUPO DE 3000
				Map<Long, Set<ClientesCartolaHistoricaMercados>> mercadosporCliente = composicionCarteraCliente.stream().collect(Collectors.groupingBy(ClientesCartolaHistoricaMercados::getIdCliente, Collectors.mapping(ClientesCartolaHistoricaMercados::getDatosCliente, Collectors.toSet())));
				listClientes.forEach(idCliente -> {
					Set<ClientesCartolaHistoricaMercados> datosPorCliente = mercadosporCliente.get(idCliente);//saca todos los datos de todos los mercados de ese cliente
					if (datosPorCliente != null) {
						Map<Long, BigDecimal> patrimonioClientes = datosPorCliente.stream().collect(Collectors.groupingBy(ClientesCartolaHistoricaMercados::getIdCliente, Collectors.reducing(BigDecimal.ZERO, ClientesCartolaHistoricaMercados::getPorcentaje, BigDecimal::add)));
					
						//System.out.println("PATRIMONIO: " + patrimonioClientes);
//*** INICIO PROCESO CartolaHistoricaMercados  >> CAR.CartolaHistoricaMercados					
						//POR MERCADO
						Collection<ClientesCartolaHistoricaMercados> ClasificacionMercados = datosPorCliente.stream().map(datosMercado ->{//suma totales por mercados y crea lista de objetos por (cliente y mercado)
							ClientesCartolaHistoricaMercados clienteCartolaMercado = new ClientesCartolaHistoricaMercados();
							clienteCartolaMercado.NombreMercadoCartola = datosMercado.NombreMercadoCartola;
							clienteCartolaMercado.IdCliente = datosMercado.IdCliente;
							clienteCartolaMercado.Fecha = fechaSistema;
							clienteCartolaMercado.Pesos = datosMercado.Porcentaje;
							clienteCartolaMercado.TotalPesos = datosMercado.Porcentaje;
							clienteCartolaMercado.TotalPatrimonio = (patrimonioClientes.get(datosMercado.IdCliente) != null)? patrimonioClientes.get(datosMercado.IdCliente) : BigDecimal.ZERO;
							
					   		return clienteCartolaMercado;
					   	}).collect(Collectors.toMap(ClientesCartolaHistoricaMercados::getNombreMercadoCartola, Function.identity(), (left, rigth)->{
					   		left.setMontoTotalMercado(left.getPesos().add(rigth.getPesos()));
							left.setPesos(left.getPesos().add(rigth.getPesos()));
					   		if(left.getTotalPatrimonio().compareTo(BigDecimal.ZERO) != 0) {
					   			left.setPorcentajeTotalMercado((left.getMontoTotalMercado().multiply(new BigDecimal("100")).divide(left.getTotalPatrimonio(),2, RoundingMode.HALF_UP)));
					   		}
					   		else {
					   			left.setPorcentajeTotalMercado(BigDecimal.ZERO);
					   		}
					   		return left;
					   	})).values();
						List<ClientesCartolaHistoricaMercados> clientesMercados = ClasificacionMercados.stream().collect(Collectors.toList());
						patrimonioCliente.put(idCliente, clientesMercados.get(0).TotalPatrimonio);
						listClientesMercados.addAll(clientesMercados);
						//System.out.println("MERCADOS CARTOLA : " + listClientesMercados);
//*** FIN PROCESO CartolaHistoricaMercados  >> CAR.CartolaHistoricaMercados
						
//*** INICIO PROCESO  >>  CAR.CartolaHistoricaInstrumentos
						//POR INSTRUMENTO
						Collection<ClientesCartolaHistoricaMercados> clasificacionInstrumentos = datosPorCliente.stream().map(datosInstrumento ->{//Suma totales por mercados y crea lista de objetos por (cliente y mercado)
							ClientesCartolaHistoricaMercados clienteCartolaInstrumento = new ClientesCartolaHistoricaMercados();
							clienteCartolaInstrumento.IdCliente = datosInstrumento.IdCliente;
							clienteCartolaInstrumento.Fecha = fechaSistema;
							clienteCartolaInstrumento.NombreCliente = datosClientes.get(datosInstrumento.IdCliente).NombreCompleto;
							clienteCartolaInstrumento.NombreClasificacion = datosInstrumento.NombreClasificacion;
							clienteCartolaInstrumento.IdInstrumento = datosInstrumento.IdInstrumento;
							clienteCartolaInstrumento.Nemotecnico = datosInstrumento.Nemotecnico;
							clienteCartolaInstrumento.TotalPatrimonio = (patrimonioCliente.get(datosInstrumento.IdCliente) != null)? patrimonioCliente.get(datosInstrumento.IdCliente) : BigDecimal.ZERO;//patrimonio
							clienteCartolaInstrumento.Nominales = datosInstrumento.Nominales;//cantidad
							clienteCartolaInstrumento.MontoTotalMercado = datosInstrumento.Porcentaje;//valor
					   		return clienteCartolaInstrumento;
					   	}).collect(Collectors.toMap(ClientesCartolaHistoricaMercados::getIdInstrumento, Function.identity(), (left, rigth)->{
					   		left.setNominales(left.getNominales().add(rigth.getNominales()));
					   		left.setTotalPesos(left.getMontoTotalMercado().add(rigth.getMontoTotalMercado()));
					   		if(left.getTotalPatrimonio().compareTo(BigDecimal.ZERO) != 0)
					   			left.setPorcentaje((left.getTotalPesos().multiply(new BigDecimal("100"))).divide(left.getTotalPatrimonio(),2, RoundingMode.HALF_UP));
					   		else
					   			left.setPorcentaje(BigDecimal.ZERO);
					   		return left;
					   	})).values();
						List<ClientesCartolaHistoricaMercados> listCartolaInstrum = clasificacionInstrumentos.stream().collect(Collectors.toList());
						//System.out.println("INSTRUMENTOS" + listCartolaInstrum);
//*** FIN PROCESO  >>  CAR.CartolaHistoricaInstrumentos
						
						//POR CLASIFICACION
						Collection<ClientesCartolaHistoricaMercados> totalClasificacionInstrumentos = datosPorCliente.stream().map(datosClasificacion ->{//Suma totales por mercados y crea lista de objetos por (cliente y mercado)
							ClientesCartolaHistoricaMercados clienteCartolaClasificacion= new ClientesCartolaHistoricaMercados();
							clienteCartolaClasificacion.IdCliente = datosClasificacion.IdCliente;
							clienteCartolaClasificacion.Fecha = fechaSistema;
							clienteCartolaClasificacion.NombreClasificacion = datosClasificacion.NombreClasificacion;
							clienteCartolaClasificacion.IdInstrumento = datosClasificacion.IdInstrumento;
							clienteCartolaClasificacion.Nemotecnico = datosClasificacion.Nemotecnico;
							clienteCartolaClasificacion.Nominales = datosClasificacion.Nominales;
							clienteCartolaClasificacion.Pesos = datosClasificacion.Porcentaje;
							clienteCartolaClasificacion.TotalPatrimonio = (patrimonioClientes.get(datosClasificacion.IdCliente) != null)? patrimonioClientes.get(datosClasificacion.IdCliente) : BigDecimal.ZERO;
					   		return clienteCartolaClasificacion;
					   	}).collect(Collectors.toMap(ClientesCartolaHistoricaMercados::getNombreClasificacion, Function.identity(), (left, rigth)->{
					   		left.setTotalPesos(left.getPesos().add(rigth.getPesos()));
					   		left.setTotalNominales(left.getNominales().add(rigth.getNominales()));
					   		if(left.getTotalPatrimonio().compareTo(BigDecimal.ZERO) != 0)
					   			left.setTotalPorcentaje((left.getTotalPesos().multiply(new BigDecimal("100"))).divide(left.getTotalPatrimonio(),2, RoundingMode.HALF_UP));
					   		else
					   			left.setTotalPorcentaje(BigDecimal.ZERO);
					   		return left;
					   	})).values();
						List<ClientesCartolaHistoricaMercados> listCartolaclasificacion = totalClasificacionInstrumentos.stream().collect(Collectors.toList());
						//System.out.println("CLASIFICACION" + listCartolaclasificacion);
						
// *** PREPARA DATOS PARA GUARDAR  ClientesInstrumentos  >> CAR.CartolaHistorica						
						for(ClientesCartolaHistoricaMercados inst : listCartolaInstrum) {
							ClientesCartolaHistoricaMercados cartolainstru= new ClientesCartolaHistoricaMercados();
							cartolainstru.IdCliente = inst.IdCliente;
							cartolainstru.NombreCliente = inst.NombreCliente;
							cartolainstru.Fecha = inst.Fecha;
							cartolainstru.IdInstrumento = inst.IdInstrumento;
							cartolainstru.Nemotecnico = inst.Nemotecnico;
							cartolainstru.Nominales = inst.Nominales;
							cartolainstru.Pesos = inst.TotalPatrimonio;
							cartolainstru.Porcentaje = inst.Porcentaje;
							for(ClientesCartolaHistoricaMercados clasificacion : listCartolaclasificacion) {
								if(inst.IdCliente.equals(clasificacion.IdCliente) && inst.NombreClasificacion.equals(clasificacion.NombreClasificacion)) {
								cartolainstru.NombreClasificacion = clasificacion.NombreClasificacion;
								cartolainstru.TotalNominales = clasificacion.TotalNominales;
								cartolainstru.TotalPesos = clasificacion.TotalPesos;
								cartolainstru.TotalPorcentaje = clasificacion.TotalPorcentaje;
								}
							}
							listClientesInstrumentos.add(cartolainstru);
						}
						//System.out.println("INSTRUMENTOS CARTOLA" + listClientesInstrumentos);
					
// *** PREPARA DATOS PARA GUARDAR ClientesCartolaHistorica  >> CAR.CartolaHistorica
						//CLIENTES
						flujoPatrimonialCliente.forEach(flujoP -> {
								ClientesCartolaHistorica datosClienteCartolaHistorica = new ClientesCartolaHistorica();
								SalidaPauta	as = (SalidaPauta) flujoP.get(idCliente);
								datosClienteCartolaHistorica.IdCliente = idCliente;
								datosClienteCartolaHistorica.NombreCliente = datosClientes.get(idCliente).NombreCompleto;
								datosClienteCartolaHistorica.IdAgente = datosClientes.get(idCliente).IdEmpleadoEjecutivo;
								datosClienteCartolaHistorica.NombreAgente = datosClientes.get(idCliente).NombreEmpleadoEjecutivo;
								datosClienteCartolaHistorica.CategoriaInversionista = datosClientes.get(idCliente).NombreCategoriaInversionista; 
								datosClienteCartolaHistorica.Fecha = fechaSistema;
								datosClienteCartolaHistorica.TotalPatrimonio = patrimonioCliente.get(idCliente);
								datosClienteCartolaHistorica.RentabilidadMTD = as.RentabilidadMTD;
								datosClienteCartolaHistorica.RentabilidadYTD = as.RentabilidadYTD;
								datosClienteCartolaHistorica.RentabilidadITD = as.RentabilidadITD;
								datosClienteCartolaHistorica.AbonoIngresoMTD = as.AportesMTD;
								datosClienteCartolaHistorica.AbonoIngresoYTD = as.AportesYTD;
								datosClienteCartolaHistorica.AbonoIngresoITD = as.AportesITD;
								datosClienteCartolaHistorica.CargoRetiroMTD = as.RescatesMTD;
								datosClienteCartolaHistorica.CargoRetiroYTD = as.RescatesYTD;
								datosClienteCartolaHistorica.CargoRetiroITD = as.RescatesITD;
								listClientesCartola.add(datosClienteCartolaHistorica);
						});	
						//System.out.println("CLIENTES CARTOLA : " + listClientesCartola);
					}
					
					
				});
				
// ** GUARDA EN TABLAS HISTORICAS
				try {
					// Limpia las tablas para la fecha procesada

					List<EntradaGuardarCartolaConsolidada> paramDelete = new ArrayList<EntradaGuardarCartolaConsolidada>();
					paramDelete.add(entrada);
					
					sql.batch(con, SQL_DELETE_CLIENTES_CARTOLA, paramDelete, EntradaGuardarCartolaConsolidada.class);
					sql.batch(con, SQL_DELETE_MERCADOS_CLIENTES_CARTOLA, paramDelete, EntradaGuardarCartolaConsolidada.class);
					sql.batch(con, SQL_DELETE_INSTRUMENTOS_CLIENTES_CARTOLA, paramDelete, EntradaGuardarCartolaConsolidada.class);
					
					//Inserta nuevos registros de cartola historica
					sql.batch(con, SQL_INSERT_CLIENTES_CARTOLA, listClientesCartola, ClientesCartolaHistorica.class);
					sql.batch(con, SQL_INSERT_MERCADOS_CLIENTES_CARTOLA, listClientesMercados, ClientesCartolaHistoricaMercados.class);	
					sql.batch(con, SQL_INSERT_INSTRUMENTOS_CLIENTES_CARTOLA, listClientesInstrumentos, ClientesCartolaHistoricaMercados.class);
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
// ** LIMPIA LISTAS EN CADA VUELTA DE LOS 3000 CLIENTES CONSULTADOS
				//SIGUIENTE
				flujoPatrimonialCliente.clear();
				listClientesMercados.clear();
				listClientesInstrumentos.clear();
				listClientesCartola.clear();
				listClientes.clear();
				datosClientes.clear();
				listaDatosCliente.clear();
				DatosCliente datosCliente = new DatosCliente();
				datosCliente.IdCliente = cliente.IdCliente;
				datosCliente.IdPortafolio = cliente.IdPortafolio;
				listaDatosCliente.add(datosCliente);
			}
		}
		logger.info("FIN::EntradaGuardarCartolaConsolidadaHistorico");
		return out;
	}
	
	
	
	// ----------------------------------------------------------------
	// METODO PAR PRUEBAS LOCALES
	// ----------------------------------------------------------------
	public static void main(String[] args) throws Exception {
		System.out.println("== TEST ==");
		try (Connection con = PlataformaUtils.getMBIPREConnection()) { //-->FIJARSE EN QUE ENTORNO SE ESTA PROBANDO<--
			EntradaGuardarCartolaConsolidada Objin = new EntradaGuardarCartolaConsolidada();
		Objin.p_IdEmpresa = 1L;
		Objin.p_IdModulo = "NOR";
		Objin.p_IdUsuario = 0L;
		Objin.p_Fecha = "2021-03-18";
		CartolaConsolidadaHistorico  demo=new CartolaConsolidadaHistorico();				
		try {
			SalidaGuardarCartolaConsolidada ObjOut=demo.logica(con, Objin);
			//con.rollback();
			con.commit();
			System.out.println(ObjOut.toString());
		}
		catch(Exception er) {
			er.printStackTrace();	
			con.rollback();
		}
		
    	}
		System.out.println("== END ==");
	}
}