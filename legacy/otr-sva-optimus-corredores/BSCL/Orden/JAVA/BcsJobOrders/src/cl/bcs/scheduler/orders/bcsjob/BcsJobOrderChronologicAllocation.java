package cl.bcs.scheduler.orders.bcsjob;

import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sql.DataSource;

import cl.bcs.scheduler.BcsJob;
import cl.bcs.scheduler.orders.pojo.Asignacion;
import cl.bcs.scheduler.orders.pojo.Orden;
import cl.bcs.scheduler.orders.pojo.ParametrosDefault;
import cl.bcs.scheduler.orders.pojo.Transaccion;
import cl.bcs.scheduler.orders.util.connectorsorders.ConnectorsOrders;

//toda implementacion debe tener test junit
public class BcsJobOrderChronologicAllocation extends BcsJob {

	Map<String, String> params;
	String nameBookOrder = "";
	DataSource datasource;
	ConnectorsOrders conn = new ConnectorsOrders();
	Connection con;
	private final AtomicInteger count = new AtomicInteger(0);
	ParametrosDefault param = new ParametrosDefault();

	String fecha = "";
	public String getFecha() {
		return fecha;
	}

	public void setFecha(String fecha) {
		this.fecha = fecha;
	}

	public Connection getConnection() {
		return con;
	}

	public void setConnection(Connection con) {
		this.con = con;
	}

	boolean dataExists = false;

 

	public BcsJobOrderChronologicAllocation() {
		super();
		param.setIdEmpresa(1);
		param.setIdUsuario(3);
		param.setModulo("ORD");
	}
 
	@Override
	public void transaction() throws Throwable {
		DataSource datasource = super.getDataSource();
		
		
		String fechaLibroUltimaEjecucion = "";
		try(Connection con  = datasource.getConnection();)
		{	
			setConnection(con);
			this.con = con;
			con.setAutoCommit(false);
		
		try { 
		
			final String libroOrdenes = super.getParam("IdLibroOrdenes")!=null?super.getParam("IdLibroOrdenes"):"";
  			final int ordersBook = Integer.parseInt(libroOrdenes);
 			nameBookOrder = conn.OrdersBookName(Integer.parseInt(libroOrdenes),con); 
 			fechaLibroUltimaEjecucion = conn.getUltimaFechaLibroOrdenes(ordersBook, con);
 
			conn.addLog(super.getJobId(), "ACT", "Iniciando = " + "Asignación Cronológica" + "  " + nameBookOrder	,con		
					);

			if (libroOrdenes.trim().length()>0) {
				  fecha = conn.getLastExecutionAssignDate(libroOrdenes, con,param);
				  setFecha(fecha);
				int contador = conn.getLastExecutionAssignCount(libroOrdenes, con, param);
				if(contador == 0)
				{
					Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Ejecuto prepareDataForAssign");
					dataExists = conn.prepareDataForAssign(super.getJobId(), libroOrdenes ,con,param);
 				}
				if(contador == 1)
				{
					Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Ejecuto prepareDataForAssignWithDate " );

					dataExists = conn.prepareDataForAssignWithDate(super.getJobId(), libroOrdenes,fecha ,con,param);
					
 				}
			 
				conn.addLog(super.getJobId(), "ACT", "Preparando datos asignacion cronologica",con);
				setDataExists(dataExists);
			}
			
		 
			if (dataExists) {
				
				List<Transaccion> transaccionList = conn.getTransactions(super.getJobId(),con);
				
				List<Orden> ordenList = conn.getOrdenes(super.getJobId(),con);
				
 				List<Asignacion> asignList = new ArrayList<>();
				final int cantOrd = ordenList.size();
				final int cantTrx = transaccionList.size();
				Logger.getLogger(this.getClass().getName()).log(Level.INFO, " Cantidad de ordenes : " + cantOrd);
				Logger.getLogger(this.getClass().getName()).log(Level.INFO, " Cantidad de palos : " + cantTrx);
				conn.lockUnlockOrders("B", super.getJobId(),con, param);
				conn.addLog(super.getJobId(), "ACT", "Bloqueo Ordenes",con);
				conn.lockUnlockTransactions("B", super.getJobId(),con,param);
				conn.addLog(super.getJobId(), "ACT", "Bloqueo transacciones",con);
				conn.addLog(super.getJobId(), "ACT", "Inicio el algoritmo de asignación masivo",con);

				try {
					// Arreglo de ordenes
					for (int oi = 0; oi < ordenList.size(); oi++) {

						/// Arreglo de transacciones
						for (int ti = 0; ti < transaccionList.size(); ti++) {

							// comparamos la fecha si la fecha de orden es menor o
							// igual a la de transaccion = true, de lo contrario
							// false.     
							if (compararFechasConDate(ordenList.get(oi).getFechaOperacion(),
									transaccionList.get(ti).getFechaOperacionTransaccion())) {
 								// comparamos IdClasificacionTipoInstrumento
								if (transaccionList.get(ti).getIdClasificacionTipoInstrumento()
										.equalsIgnoreCase(ordenList.get(oi).getIdClasificacionTipoInstrumento())) {
									// comparamos IdTipoInstrumento
									if(transaccionList.get(ti).getIdInstrumento()==ordenList.get(oi).getIdInstrumento())
									{
										// comparamos IdInstrumento
									if (transaccionList.get(ti).getIdTipoInstrumento()
											.equalsIgnoreCase(ordenList.get(oi).getIdTipoInstrumento())) {

										// comparas la IdBolsa
										if (transaccionList.get(ti).getIdBolsa()
												.equalsIgnoreCase(ordenList.get(oi).getIdBolsa()) || ordenList.get(oi).getEspecificaBolsa()=="N") {

											// comparamos el tipo de operacion
											if (ordenList.get(oi).getIdTipoOperacion()
													.equals(transaccionList.get(ti).getIdTipoOperacion())) {
												// Comparamos la
												// idCondicionDeLiquidacion
												if (transaccionList.get(ti).getIdCondicionDeLiquidacion().equalsIgnoreCase(
														ordenList.get(oi).getIdCondicionDeLiquidacion())) {

													if (ordenList.get(oi).getIdTipoOperacion().contains("COM")) {
														// Si el codigoTipoPrecio es Limite
														// El precio del palo no puede ser mayor que el de la orden
														
														if (ordenList.get(oi).getIdCodigoTipoPrecio().equalsIgnoreCase("L")
																&& ordenList.get(oi).getPrecio() >= transaccionList.get(ti).getPrecio()) {

															// si la
															// cantidadDisponible de
															// la orden es mayor a 0
															// sigo el flujo
															if (ordenList.get(oi).getCantidadDisponible() > 0 && ordenList.get(oi).getPrecio()>0) {

																if (transaccionList.get(ti).getCantidadDisponible() > 0 && transaccionList.get(ti).getPrecio()>0) {
																	
 
																	// Si la cantidadDisponible
																	// de la orden es menor o igual que CantidadDisponible
																	// de la transaccion se opera con  la cantidadDisponible  de orden
																	if (ordenList.get(oi).getCantidadDisponible() <= transaccionList.get(ti).getCantidadDisponible()) {

																		double cantidadDisponibleOrden = ordenList.get(oi).getCantidadDisponible();
																		double cantidadAsignadaOrden = ordenList.get(oi).getCantidadAsignada();
																		double cantidadDisponibleTransaccion = transaccionList.get(ti).getCantidadDisponible();
																		double cantidadAsignadaTransaccion = transaccionList.get(ti).getCantidadAsignada();
																		int montoTotal = (int) ((int) cantidadDisponibleOrden* transaccionList.get(ti).getPrecio());
																		Asignacion asigna = new Asignacion(count.getAndIncrement(), "ANF",
																				(int) ordenList.get(oi).getCantidadDisponible(),
																				transaccionList.get(ti).getFechaOperacionTransaccion(),
																				transaccionList.get(ti).getIdTransaccionPaso(),
																				ordenList.get(oi).getIdOrdenPaso(),
																				montoTotal);
																		asignList.add(asigna);
																		ordenList.get(oi).setCantidadDisponible(cantidadDisponibleOrden- cantidadDisponibleOrden);
																		ordenList.get(oi).setCantidadAsignada(cantidadAsignadaOrden+ cantidadDisponibleOrden);
																		transaccionList.get(ti).setCantidadDisponible(cantidadDisponibleTransaccion- cantidadDisponibleOrden);
																		transaccionList.get(ti).setCantidadAsignada(cantidadAsignadaTransaccion+ cantidadDisponibleOrden);

																	}
																	// Si la
																	// cantidadDisponible
																	// de la orden
																	// es mayor que
																	// CantidadDisponible
																	// de la
																	// transaccion
																	// se opera con
																	// la
																	// cantidadDisponible
																	// de
																	// transaccion
																	if (ordenList.get(oi).getCantidadDisponible() > transaccionList.get(ti).getCantidadDisponible()) {

																	
																		double cantidadDisponibleOrden = ordenList.get(oi).getCantidadDisponible();
																		double cantidadAsignadaOrden = ordenList.get(oi).getCantidadAsignada();
																		double cantidadDisponibleTransaccion = transaccionList.get(ti).getCantidadDisponible();
																		double cantidadAsignadaTransaccion = transaccionList.get(ti).getCantidadAsignada();
																		int montoTotal = (int) ((int) cantidadDisponibleOrden* transaccionList.get(ti).getPrecio());
																		
 																		Asignacion asigna = new Asignacion(count.getAndIncrement(), "ANF",
																				(int) cantidadDisponibleTransaccion,
																				transaccionList.get(ti).getFechaOperacionTransaccion(),
																				transaccionList.get(ti).getIdTransaccionPaso(),
																				ordenList.get(oi).getIdOrdenPaso(),
																				montoTotal);
																		asignList.add(asigna);
																		ordenList.get(oi).setCantidadDisponible(cantidadDisponibleOrden- cantidadDisponibleTransaccion);
																		ordenList.get(oi).setCantidadAsignada(cantidadAsignadaOrden+ cantidadDisponibleTransaccion);
																		transaccionList.get(ti).setCantidadDisponible(cantidadDisponibleTransaccion- cantidadDisponibleTransaccion);
																		transaccionList.get(ti).setCantidadAsignada(cantidadAsignadaTransaccion+ cantidadDisponibleTransaccion);

																	}

																}

															}
														}
															// Si el
															// codigoTipoPrecio es M
															// el precio de la orden
															// es el precio que
															// asigne la transaccion
															if (ordenList.get(oi).getIdCodigoTipoPrecio().equalsIgnoreCase("M")) {
																if (ordenList.get(oi).getCantidadDisponible() > 0 && ordenList.get(oi).getPrecio()>0) {
																	if (transaccionList.get(ti).getCantidadDisponible() > 0 && transaccionList.get(ti).getPrecio()>0) {
																	// Si la
																	// cantidadDisponible
																	// de la orden
																	// es menor o
																	// igual que
																	// CantidadDisponible
																	// de la
																	// transaccion
																	// se opera con
																	// la
																	// cantidadDisponible
																	// de orden
																	if (ordenList.get(oi).getCantidadDisponible() <= transaccionList.get(ti).getCantidadDisponible()) {

																		
																		double cantidadDisponibleOrden = ordenList.get(oi).getCantidadDisponible();
																		double cantidadAsignadaOrden = ordenList.get(oi).getCantidadAsignada();
																		double cantidadDisponibleTransaccion = transaccionList.get(ti).getCantidadDisponible();
																		double cantidadAsignadaTransaccion = transaccionList.get(ti).getCantidadAsignada();
																		int montoTotal = (int) ((int) cantidadDisponibleOrden* transaccionList.get(ti).getPrecio());
 																		Asignacion asigna = new Asignacion(count.getAndIncrement(), "ANF",
																				(int) cantidadDisponibleOrden,
																				transaccionList.get(ti).getFechaOperacionTransaccion(),
																				transaccionList.get(ti).getIdTransaccionPaso(),
																				ordenList.get(oi).getIdOrdenPaso(),
																				montoTotal);
																		asignList.add(asigna);
																		ordenList.get(oi).setCantidadDisponible(cantidadDisponibleOrden- cantidadDisponibleOrden);
																		ordenList.get(oi).setCantidadAsignada(cantidadAsignadaOrden+ cantidadDisponibleOrden);
																		transaccionList.get(ti).setCantidadDisponible(cantidadDisponibleTransaccion- cantidadDisponibleOrden);
																		transaccionList.get(ti).setCantidadAsignada(cantidadAsignadaTransaccion+ cantidadDisponibleOrden);

																	}
																	// Si la
																	// cantidadDisponible
																	// de la orden
																	// es mayor que
																	// CantidadDisponible
																	// de la
																	// transaccion
																	// se opera con
																	// la
																	// cantidadDisponible
																	// de
																	// transaccion
																	if (ordenList.get(oi).getCantidadDisponible() > transaccionList.get(ti).getCantidadDisponible()) {

																	
																		double cantidadDisponibleOrden = ordenList.get(oi).getCantidadDisponible();
																		double cantidadAsignadaOrden = ordenList.get(oi).getCantidadAsignada();
																		double cantidadDisponibleTransaccion = transaccionList.get(ti).getCantidadDisponible();
																		double cantidadAsignadaTransaccion = transaccionList.get(ti).getCantidadAsignada();
																		int montoTotal = (int) ((int) cantidadDisponibleOrden* transaccionList.get(ti).getPrecio());
 																		Asignacion asigna = new Asignacion(count.getAndIncrement(), "ANF",
																				(int) cantidadDisponibleTransaccion,
																				transaccionList.get(ti).getFechaOperacionTransaccion(),
																				transaccionList.get(ti).getIdTransaccionPaso(),
																				ordenList.get(oi).getIdOrdenPaso(),
																				montoTotal);
																		asignList.add(asigna);
																		ordenList.get(oi).setCantidadDisponible(cantidadDisponibleOrden- cantidadDisponibleTransaccion);
																		ordenList.get(oi).setCantidadAsignada(cantidadAsignadaOrden+ cantidadDisponibleTransaccion);
																		transaccionList.get(ti).setCantidadDisponible(cantidadDisponibleTransaccion- cantidadDisponibleTransaccion);
																		transaccionList.get(ti).setCantidadAsignada(cantidadAsignadaTransaccion+ cantidadDisponibleTransaccion);

																	}
																}
															}
															}

														
													}
													if (ordenList.get(oi).getIdTipoOperacion().contains("VEN")) {
														if (ordenList.get(oi).getIdCodigoTipoPrecio().equalsIgnoreCase("L")
																&& ordenList.get(oi).getPrecio() >= transaccionList.get(ti).getPrecio()) {

															// si la
															// cantidadDisponible de
															// la orden es mayor a 0
															// sigo el flujo
															if (ordenList.get(oi).getCantidadDisponible() > 0 && ordenList.get(oi).getPrecio()>0) {

																if (transaccionList.get(ti).getCantidadDisponible() > 0 && transaccionList.get(ti).getPrecio()>0) {

																	// Si la
																	// cantidadDisponible
																	// de la orden
																	// es menor o
																	// igual que
																	// CantidadDisponible
																	// de la
																	// transaccion
																	// se opera con
																	// la
																	// cantidadDisponible
																	// de orden
																	if (ordenList.get(oi).getCantidadDisponible() <= transaccionList.get(ti).getCantidadDisponible()) {

																	
																		double cantidadDisponibleOrden = ordenList.get(oi).getCantidadDisponible();
																		double cantidadAsignadaOrden = ordenList.get(oi).getCantidadAsignada();
																		double cantidadDisponibleTransaccion = transaccionList.get(ti).getCantidadDisponible();
																		double cantidadAsignadaTransaccion = transaccionList.get(ti).getCantidadAsignada();
																		int montoTotal = (int) ((int) cantidadDisponibleOrden* transaccionList.get(ti).getPrecio());
																		
																		Asignacion asigna = new Asignacion(count.getAndIncrement(), "ANF",
																				(int) ordenList.get(oi)
																						.getCantidadDisponible(),
																				transaccionList.get(ti).getFechaOperacionTransaccion(),
																				transaccionList.get(ti).getIdTransaccionPaso(),
																				ordenList.get(oi).getIdOrdenPaso(),
																				montoTotal);
																		asignList.add(asigna);
																		
																		
																		ordenList.get(oi).setCantidadDisponible(cantidadDisponibleOrden- cantidadDisponibleOrden);
																		ordenList.get(oi).setCantidadAsignada(cantidadAsignadaOrden+ cantidadDisponibleOrden);
																		transaccionList.get(ti).setCantidadDisponible(cantidadDisponibleTransaccion- cantidadDisponibleOrden);
																		transaccionList.get(ti).setCantidadAsignada(cantidadAsignadaTransaccion+ cantidadDisponibleOrden);
																		
																		

																	}
																	// Si la
																	// cantidadDisponible
																	// de la orden
																	// es mayor que
																	// CantidadDisponible
																	// de la
																	// transaccion
																	// se opera con
																	// la
																	// cantidadDisponible
																	// de
																	// transaccion
																	if (ordenList.get(oi).getCantidadDisponible() > transaccionList.get(ti).getCantidadDisponible()) {

																		
																		double cantidadDisponibleOrden = ordenList.get(oi).getCantidadDisponible();
																		double cantidadAsignadaOrden = ordenList.get(oi).getCantidadAsignada();
																		double cantidadDisponibleTransaccion = transaccionList.get(ti).getCantidadDisponible();
																		double cantidadAsignadaTransaccion = transaccionList.get(ti).getCantidadAsignada();
																		int montoTotal = (int) ((int) cantidadDisponibleOrden* transaccionList.get(ti).getPrecio());
 																		
																		Asignacion asigna = new Asignacion(count.getAndIncrement(), "ANF",
																				(int) cantidadDisponibleTransaccion,
																				transaccionList.get(ti).getFechaOperacionTransaccion(),
																				transaccionList.get(ti).getIdTransaccionPaso(),
																				ordenList.get(oi).getIdOrdenPaso(),
																				montoTotal);
																		asignList.add(asigna);
																		
																		ordenList.get(oi).setCantidadDisponible(cantidadDisponibleOrden- cantidadDisponibleTransaccion);
																		ordenList.get(oi).setCantidadAsignada(cantidadAsignadaOrden+ cantidadDisponibleTransaccion);
																		transaccionList.get(ti).setCantidadDisponible(cantidadDisponibleTransaccion- cantidadDisponibleTransaccion);
																		transaccionList.get(ti).setCantidadAsignada(cantidadAsignadaTransaccion+ cantidadDisponibleTransaccion);
																	

																	}

																}

															}
														}
															// Si el
															// codigoTipoPrecio es M
															// el precio de la orden
															// es el precio que
															// asigne la transaccion
															if (ordenList.get(oi).getIdCodigoTipoPrecio().equalsIgnoreCase("M")) {

																if (ordenList.get(oi).getCantidadDisponible() > 0 && ordenList.get(oi).getPrecio()>0
																	&&	transaccionList.get(ti).getCantidadDisponible()>0 && transaccionList.get(ti).getPrecio()>0	) {
																	// Si la
																	// cantidadDisponible
																	// de la orden
																	// es menor o
																	// igual que
																	// CantidadDisponible
																	// de la
																	// transaccion
																	// se opera con
																	// la
																	// cantidadDisponible
																	// de orden
																	if (ordenList.get(oi).getCantidadDisponible() <= transaccionList.get(ti).getCantidadDisponible()) {

																		
																		double cantidadDisponibleOrden = ordenList.get(oi).getCantidadDisponible();
																		double cantidadAsignadaOrden = ordenList.get(oi).getCantidadAsignada();
																		double cantidadDisponibleTransaccion = transaccionList.get(ti).getCantidadDisponible();
																		double cantidadAsignadaTransaccion = transaccionList.get(ti).getCantidadAsignada();
																		int montoTotal = (int) ((int) cantidadDisponibleOrden* transaccionList.get(ti).getPrecio());
																		Asignacion asigna = new Asignacion(count.getAndIncrement(), "ANF",
																				(int) cantidadDisponibleOrden,
																				transaccionList.get(ti).getFechaOperacionTransaccion(),
																				transaccionList.get(ti).getIdTransaccionPaso(),
																				ordenList.get(oi).getIdOrdenPaso(),
																				montoTotal);
																		asignList.add(asigna);
																		ordenList.get(oi).setCantidadDisponible(cantidadDisponibleOrden- cantidadDisponibleOrden);
																		ordenList.get(oi).setCantidadAsignada(cantidadAsignadaOrden+ cantidadDisponibleOrden);
																		transaccionList.get(ti).setCantidadDisponible(cantidadDisponibleTransaccion- cantidadDisponibleOrden);
																		transaccionList.get(ti).setCantidadAsignada(cantidadAsignadaTransaccion+ cantidadDisponibleOrden);
																		

																	}
																	// Si la
																	// cantidadDisponible
																	// de la orden
																	// es mayor que
																	// CantidadDisponible
																	// de la
																	// transaccion
																	// se opera con
																	// la
																	// cantidadDisponible
																	// de
																	// transaccion
																	if (ordenList.get(oi).getCantidadDisponible() > transaccionList.get(ti).getCantidadDisponible()) {

																		
																		double cantidadDisponibleOrden = ordenList.get(oi).getCantidadDisponible();
																		double cantidadAsignadaOrden = ordenList.get(oi).getCantidadAsignada();
																		double cantidadDisponibleTransaccion = transaccionList.get(ti).getCantidadDisponible();
																		double cantidadAsignadaTransaccion = transaccionList.get(ti).getCantidadAsignada();
																		Asignacion asigna = new Asignacion(count.getAndIncrement(), "ANF",
																				(int) cantidadDisponibleTransaccion,
																				transaccionList.get(ti).getFechaOperacionTransaccion(),
																				transaccionList.get(ti).getIdTransaccionPaso(),
																				ordenList.get(oi).getIdOrdenPaso());
																		asignList.add(asigna);
 																		
																		ordenList.get(oi).setCantidadDisponible(cantidadDisponibleOrden- cantidadDisponibleTransaccion);
																		ordenList.get(oi).setCantidadAsignada(cantidadAsignadaOrden+ cantidadDisponibleTransaccion);
																		transaccionList.get(ti).setCantidadDisponible(cantidadDisponibleTransaccion- cantidadDisponibleTransaccion);
																		transaccionList.get(ti).setCantidadAsignada(cantidadAsignadaTransaccion+ cantidadDisponibleTransaccion);
																		

																	}
																}
															}
														}

														
													}

												}
											}
										}
									}

								}
							}
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
					Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, e.getMessage(), e.getCause());
					conn.lockUnlockTransactions("D", super.getJobId(),con,param);
					conn.lockUnlockOrders("D", super.getJobId(),con,param);
					throw new Throwable(e.getMessage());
				}

				
				conn.addLog(super.getJobId(), "ACT", "Termino el algoritmo de asignacion",con);
				Logger.getLogger(this.getClass().getName()).log(Level.INFO,
						" Cantidad de Asignaciones : " + asignList.size() + "Programa = " + super.getJobId());
				

				conn.nullifyMassively(super.getJobId(),con,param);
				conn.deleteMassively(libroOrdenes,con);
				conn.assignMassively(asignList,super.getJobId(),con,param);
				conn.addLog(super.getJobId(), "ACT", "Creación masiva de asignaciones",con);
				conn.createAndInsertTableTemporallyAssign(super.getJobId(),ordersBook,con);
				
				conn.addLog(super.getJobId(), "ACT", "Anulación masiva de asignaciones",con);
				con.commit();
			}

		} catch (Throwable e) {
			Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, e.getMessage(), e.getCause());
			conn.lockUnlockTransactions("D", super.getJobId(),con,param);
			conn.lockUnlockOrders("D", super.getJobId(),con,param);
			conn.addLog(super.getJobId(), "Err", e.getMessage(),con);
		    getConnection().rollback();
			throw new Throwable(e.getMessage());
          
		} finally {
			if (dataExists) {
 			 
				conn.lockUnlockTransactions("D", super.getJobId(),con,param);
				conn.lockUnlockOrders("D", super.getJobId(),con,param);
 				conn.dropTable("#orden_in_" + super.getJobId(),con);
				conn.dropTable("#created_asignacion_" + super.getJobId(),con);
 				conn.dropTable("#transaccion_in_" + super.getJobId(),con);
				conn.addLog(super.getJobId(), "ACT", "Termino proceso de asignación del libro = " + nameBookOrder,con
						);
				con.commit();
			}

		}
		}catch (Exception e) {
			conn.lockUnlockTransactions("D", super.getJobId(),con,param);
			conn.lockUnlockOrders("D", super.getJobId(),con,param);
			conn.setFechaUltimaEjecucion(fechaLibroUltimaEjecucion, con);
			Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, e.getMessage(), e.getCause());
			throw new Throwable(e.getMessage());
		}
	 
			

 
	}

	@Override
	public void onError(Throwable t) {
		// si ocurre un problema se debe dejar todo tal cual como estaba
		try {
 			if (this.dataExists) {
 				
 				conn.updateStateAssign(fecha, super.getParam("IdLibroOrdenes"), getConnection());
				conn.updateProgramm(super.getJobId(), -1, t.getMessage().substring(0, 20),getConnection());
				conn.addLog(super.getJobId(), "ERR", t.getMessage().substring(0, 99), getConnection());
				conn.lockUnlockOrders("D", super.getJobId(),getConnection(),param);
				conn.addLog(super.getJobId(), "ACT", "Desbloqueo Ordenes",getConnection());
				conn.lockUnlockTransactions("D", super.getJobId(),getConnection(),param);
				conn.addLog(super.getJobId(), "ACT", "Desbloqueo transacciones",getConnection());
				datasource.getConnection().rollback();
				conn.addLog(super.getJobId(), "ACT", "rollback",getConnection());
				conn.getConnection().commit();
			}
		 

		} catch (Throwable e) {
			// TODO Auto-generated catch block
			Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, e.getMessage(), e.getCause());
		}
		// loggear
	}

	public boolean isDataExists() {
		return dataExists;
	}

	public void setDataExists(boolean dataExists) {
		this.dataExists = dataExists;
	}

	public Map<String, String> getParams() {
		return params;
	}

	public void setParams(Map<String, String> params) {
		this.params = params;
	}

	long jobId;

	public void setJobId(long jobId) {
		this.jobId = jobId;
	}

	@Override
	public long getJobId() {
		return super.getJobId(); // To change body of generated methods, choose
									// Tools | Templates.
	}

	public static boolean compararFechasConDate(String fecha1, String fechaActual) {

		try {
			boolean resultado = false;

			SimpleDateFormat formateador = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Date fechaDate1 = formateador.parse(fecha1);
			Date fechaDate2 = formateador.parse(fechaActual);

			if (fechaDate1.before(fechaDate2)) {
				resultado = true;
			} else {
				if (fechaDate2.before(fechaDate1)) {
					resultado = false;
				} else {
					resultado = true;
				}
			}
			return resultado;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub
		
	}

}
