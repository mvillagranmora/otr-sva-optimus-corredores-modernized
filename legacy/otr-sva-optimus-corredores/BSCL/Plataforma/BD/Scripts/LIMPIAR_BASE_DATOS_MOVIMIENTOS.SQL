SET SCHEMA "BSCL";

---------------------------------
-- FACTURACION
---------------------------------
Delete from "BSCL.Facturacion.BD::FAC.MovimientoComprobante";
Delete from "BSCL.Facturacion.BD::FAC.MovimientoComprobanteFormaDePago";
Delete from "BSCL.Facturacion.BD::FAC.MovimientoComprobanteCobro";
Delete from "BSCL.Facturacion.BD::FAC.Comprobante";
Delete from "BSCL.Facturacion.BD::FAC.ComprobanteFormaDePago";
Delete from "BSCL.Facturacion.BD::FAC.ComprobanteCobro";
Delete from "BSCL.Facturacion.BD::FAC.ComprobanteDetalle";
Delete from "BSCL.Facturacion.BD::FAC.MovimientoComprobanteDetalle";
Delete from "BSCL.Facturacion.BD::FAC.CobroPeriodico";
Delete from "BSCL.Facturacion.BD::FAC.CobroPeriodicoDetalle";
Delete from "BSCL.Facturacion.BD::FAC.ContratoCobroPeriodico";
Delete from "BSCL.Facturacion.BD::FAC.ContratoCobroPeriodicoDetalle";

---------------------------------
-- FFMM
---------------------------------
DELETE FROM "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate";
DELETE FROM "BSCL.FondoMutuo.BD::FMM.LogAperturaCierreBloqueo";
DELETE FROM "BSCL.FondoMutuo.BD::FMM.LogCertificacionAporteRescate";
DELETE FROM "BSCL.FondoMutuo.BD::FMM.DetalleCargaSolicitudesAporteRescate";
DELETE FROM "BSCL.FondoMutuo.BD::FMM.ValorCuota";
DELETE FROM "BSCL.FondoMutuo.BD::FMM.ValorCuotaHistorico";
DELETE FROM "BSCL.FondoMutuo.BD::FMM.ValorTipoCambio";
DELETE FROM "BSCL.FondoMutuo.BD::FMM.ValorTipoCambioHistorico";


---------------------------------
-- ORDENES
---------------------------------
DELETE FROM "BSCL.Orden.BD::ORD.Asignacion";
DELETE FROM "BSCL.Orden.BD::ORD.CobroAsignacion";
DELETE FROM "BSCL.Orden.BD::ORD.CobroOrden";
DELETE FROM "BSCL.Orden.BD::ORD.EventoBitacora";
DELETE FROM "BSCL.Orden.BD::ORD.Orden";
DELETE FROM "BSCL.Orden.BD::ORD.Transaccion";
DELETE FROM "BSCL.Orden.BD::ORD.AsignacionResultado";
DELETE FROM "BSCL.Orden.BD::ORD.CargaTransaccionesLog";
DELETE FROM "BSCL.Orden.BD::ORD.DebugCrearAsignaciones";
DELETE FROM "BSCL.Orden.BD::ORD.DebugCrearOrdenes";
DELETE FROM "BSCL.Orden.BD::ORD.DebugCrearOrdenesPostreras";
DELETE FROM "BSCL.Orden.BD::ORD.DebugCrearTransacciones";
DELETE FROM "BSCL.Orden.BD::ORD.OrdenPaso";
DELETE FROM "BSCL.Orden.BD::ORD.TransaccionPaso";

---------------------------------
-- NORMATIVA/TRIBUTARIO
---------------------------------
DELETE FROM "BSCL.Normativa.BD::NOR.SolicitudParametros";
DELETE FROM "BSCL.Normativa.BD::NOR.SolicitudCircular";
DELETE FROM "BSCL.Normativa.BD::NOR.NCG380RegistroHistoricoCliente";
DELETE FROM "BSCL.Normativa.BD::NOR.DatosCI1720";
DELETE FROM "BSCL.Normativa.BD::TRI.DatosDJ1885";
DELETE FROM "BSCL.Normativa.BD::TRI.DatosDJ1891";

---------------------------------
-- CARTERA
---------------------------------
delete from "BSCL.Custodia.BD::CUS.GenerarMovimientoCustodiaLog";
delete from "BSCL.Custodia.BD::CUS.Subyacente";
delete from "BSCL.Custodia.BD::CUS.Cartera";
delete from "BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito";
delete from "BSCL.Custodia.BD::GTA.Garantia";
DELETE FROM "BSCL.Custodia.BD::CUS.Cartera";
DELETE FROM "BSCL.Custodia.BD::CUS.Operacion";
DELETE FROM "BSCL.Custodia.BD::PAR.ActualizarMovimientoCustodiaCarterasVigentes";
DELETE FROM "BSCL.Custodia.BD::PAR.ActualizarMovimientoCustodiaDatosEntrada";
DELETE FROM "BSCL.Custodia.BD::PAR.ActualizarMovimientoCustodiaFoliosOUT";
DELETE FROM "BSCL.Custodia.BD::PAR.ActualizarMovimientoCustodiaInsPorOperacion";
DELETE FROM "BSCL.Custodia.BD::PAR.ActualizarMovimientoCustodiaTablaDesarrollo";
DELETE FROM "BSCL.Custodia.BD::PAR.AplicarConfiguracionDatosEntradaMovimientoCustodia";
DELETE FROM "BSCL.Custodia.BD::PAR.AplicarConfiguracionDatosEntradaMovimientoCustodiaOUT";
DELETE FROM "BSCL.Custodia.BD::PAR.CrearMovimientoCustodiaConfiguracion";
DELETE FROM "BSCL.Custodia.BD::PAR.CrearMovimientoCustodiaConfiguracionOUT";
DELETE FROM "BSCL.Custodia.BD::PAR.CrearMovimientoCustodiaDatosEntrada";
DELETE FROM "BSCL.Custodia.BD::PAR.GenerarMovimientoCustodiaDatosEntrada";
DELETE FROM "BSCL.Custodia.BD::PAR.GenerarMovimientoCustodiaParametrosEntrada";
DELETE FROM "BSCL.Custodia.BD::PAR.OrdenEjecucionMovimientoCustodia";
DELETE FROM "BSCL.Custodia.BD::PAR.OrdenEjecucionMovimientoCustodiaOUT";
DELETE FROM "BSCL.Custodia.BD::PAR.ProcesarMovimientoCustodiaCarterasVigentes";
DELETE FROM "BSCL.Custodia.BD::PAR.ProcesarMovimientoCustodiaCarterasVigentesParaUtilizar";
DELETE FROM "BSCL.Custodia.BD::PAR.ProcesarMovimientoCustodiaInsPorOperacion";
DELETE FROM "BSCL.Custodia.BD::PAR.ProcesarMovimientoCustodiaOperaciones";
DELETE FROM "BSCL.Custodia.BD::PAR.ProcesarMovimientoCustodiaOrdenEjecucion";
DELETE FROM "BSCL.Custodia.BD::PAR.ProcesarMovimientoCustodiaTablaDesarrolloOUT";

delete from "BSCL.Custodia.BD::CUS.EventoCapitalClienteFormaDePago";
delete from "BSCL.Custodia.BD::CUS.EventoCapitalCliente";
delete from "BSCL.Custodia.BD::CUS.EventoCapital";


---------------------------------
-- MESA RF - FWD - SPOT
---------------------------------
Delete from "BSCL.Custodia.BD::CUS.Reserva";
Delete from "BSCL.Custodia.BD::CUS.Operacion";
Delete from "BSCL.Mesa.BD::SPT.OperacionSpot";
DELETE FROM "BSCL.Mesa.BD::MES.CargaMasivaDepositos";
DELETE FROM "BSCL.Mesa.BD::SPT.OperacionSpot";
DELETE FROM "BSCL.Mesa.BD::SPT.PosicionInicialMesa";
DELETE FROM "BSCL.Mesa.BD::SPT.Puntas";
DELETE FROM "BSCL.Mesa.BD::SPT.PuntasPorTramo";

---------------------------------
-- TESORERIA
---------------------------------
DELETE FROM "BSCL.Tesoreria.BD::TES.NominaTransferenciaDetalle";
DELETE FROM "BSCL.Tesoreria.BD::TES.NominaTransferencia";
DELETE FROM "BSCL.Tesoreria.BD::TES.DepositoDetalle";
DELETE FROM "BSCL.Tesoreria.BD::TES.Deposito";
DELETE FROM "BSCL.Tesoreria.BD::CAR.CargaMasivaDetalle";
DELETE FROM "BSCL.Tesoreria.BD::CAR.CargaMasiva";
DELETE FROM "BSCL.Tesoreria.BD::TES.CajaMoneda";
DELETE FROM "BSCL.Tesoreria.BD::TES.CajaProductoEmpresa";
DELETE FROM "BSCL.Tesoreria.BD::TES.Caja";
DELETE FROM "BSCL.Tesoreria.BD::TES.Chequera";
DELETE FROM "BSCL.Tesoreria.BD::TES.ComprobanteDetalleOrigen";
DELETE FROM "BSCL.Tesoreria.BD::TES.ComprobanteDetalle";
DELETE FROM "BSCL.Tesoreria.BD::TES.ComprobanteFormaDePago";
DELETE FROM "BSCL.Tesoreria.BD::TES.Comprobante";
DELETE FROM "BSCL.Tesoreria.BD::TES.IngresoEgresoFormaDePago";
DELETE FROM "BSCL.Tesoreria.BD::TES.IngresoEgreso";


---------------------------------
-- CUENTA INVERSION
---------------------------------
DELETE FROM "BSCL"."BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento";
DELETE FROM "BSCL.CuentaInversion.BD::CTA.LibroInventarioDeSaldos";

-------------------------
-- MODULO PLATAFORMA
-------------------------

-- AUTORIZACIONES
delete from "BSCL.Plataforma.BD::AUT.AutorizadoEtapaEvento";
delete from "BSCL.Plataforma.BD::AUT.EtapaEvento";
delete from "BSCL.Plataforma.BD::AUT.Evento";
DELETE FROM "BSCL.Plataforma.BD::AUT.AutorizadoEtapa";


--Programas Tareas Programadas
DELETE FROM "BSCL"."BSCL.Plataforma.BD::BAT.LogPrograma";
DELETE FROM  "BSCL.Plataforma.BD::BAT.ParametrosPrograma";
DELETE FROM  "BSCL.Plataforma.BD::BAT.Programa";

--Generales
DELETE FROM "BSCL.Plataforma.BD::PLA.DetalleLog";
DELETE FROM "BSCL.Plataforma.BD::PLA.Log";
DELETE FROM "BSCL.Plataforma.BD::PLA.SolicitudCambio";

--Notificaciones
DELETE FROM "BSCL"."BSCL.Plataforma.BD::EVT.EventoDetalle";
DELETE FROM "BSCL"."BSCL.Plataforma.BD::EVT.Evento";
DELETE FROM "BSCL"."BSCL.Plataforma.BD::EVT.NotificacionDetalle";
DELETE FROM "BSCL"."BSCL.Plataforma.BD::EVT.Notificacion";
DELETE FROM "BSCL"."BSCL.Plataforma.BD::EVT.Destinatario";
DELETE FROM "BSCL"."BSCL.Plataforma.BD::EVT.EventoDirecciones";
DELETE FROM "BSCL"."BSCL.Plataforma.BD::EVT.Mail";
DELETE FROM "BSCL"."BSCL.Plataforma.BD::EVT.EventoArchivoAdjunto";

--Asuntos Pendientes
DELETE from "BSCL.Plataforma.BD::PND.DetalleAsuntoPendiente";

-------------------------
-- ACCESOS & USUARIOS
-------------------------
DELETE FROM "BSCL.Plataforma.BD::ACC.Sesion";

-------------------------
-- PORTAFOLIO
-------------------------


-------------------------
-- CLIENTE
-------------------------

-------------------------
-- PROVEEDOR
-------------------------

-------------------------
-- CONTRAPARTE
-------------------------

-------------------------
-- EMPLEADO
-------------------------


-------------------------
-- PERSONA
-------------------------

-------------------------
-- COMMIT
-------------------------
commit;



