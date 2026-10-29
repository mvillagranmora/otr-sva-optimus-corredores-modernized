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
-- COBERTURA DE GARANTIAS
---------------------------------
DELETE FROM "BSCL.Plataforma.BD::GTA.CoberturaGarantia";
DELETE FROM "BSCL.Plataforma.BD::GTA.ExigibilidadGarantia";

---------------------------------
-- MESA RF FWD - SPOT 
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
--del usuario
DELETE FROM "BSCL.Tesoreria.BD::TES.ConfiguracionChequera";
DELETE FROM "BSCL.Tesoreria.BD::TES.CajaUsuario";




---------------------------------
-- CUENTA INVERSION
---------------------------------
DELETE FROM "BSCL"."BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento";
DELETE FROM "BSCL.CuentaInversion.BD::CTA.LibroInventarioDeSaldos";
--del portafolio
DELETE FROM "BSCL.CuentaInversion.BD::CTA.CuentaInversion";


-------------------------
-- MODULO PLATAFORMA
-------------------------

-- AUTORIZACIONES
delete from "BSCL.Plataforma.BD::AUT.AutorizadoEtapaEvento";
delete from "BSCL.Plataforma.BD::AUT.EtapaEvento";
delete from "BSCL.Plataforma.BD::AUT.Evento";
DELETE FROM "BSCL.Plataforma.BD::AUT.AutorizadoEtapa";


--Productos
DELETE FROM "BSCL.Plataforma.BD::COM.DetallePerfilProductos";
DELETE FROM "BSCL.Plataforma.BD::COM.PerfilProductos";

-- Cobros
DELETE FROM "BSCL.Plataforma.BD::COM.DetallePerfilCobros";
DELETE FROM "BSCL.Plataforma.BD::COM.PerfilCobros";

-- Perfil Comercial Base
DELETE FROM "BSCL.Plataforma.BD::COM.PerfilComercialBase";

--Programas Tareas Programadas
DELETE FROM "BSCL"."BSCL.Plataforma.BD::BAT.LogPrograma";
DELETE FROM  "BSCL.Plataforma.BD::BAT.ParametrosPrograma";
DELETE FROM  "BSCL.Plataforma.BD::BAT.Programa";

--Documentos de una entidad - cliente
DELETE FROM "BSCL.Plataforma.BD::DOC.Anexo";
DELETE FROM "BSCL.Plataforma.BD::DOC.Documento";

--Valores de Indicadores para cada identidad
DELETE FROM "BSCL.Plataforma.BD::IND.Indicador" WHERE "IdTipoEntidad" = 'PERSONA';
DELETE FROM "BSCL.Plataforma.BD::IND.Indicador" WHERE "IdTipoEntidad" = 'CLIENTE';
DELETE FROM "BSCL.Plataforma.BD::IND.Indicador" WHERE "IdTipoEntidad" = 'PORTAFOLIO';

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
DELETE FROM  "BSCL.Plataforma.BD::ACC.Sesion";
DELETE FROM  "BSCL.Plataforma.BD::ACC.PerfilAcceso";
DELETE FROM  "BSCL.Plataforma.BD::ACC.RolPerfil"; 
DELETE FROM  "BSCL.Plataforma.BD::ACC.RolUsuario";
DELETE FROM  "BSCL.Plataforma.BD::ACC.Perfil";
DELETE FROM  "BSCL.Plataforma.BD::ACC.Rol";
DELETE FROM  "BSCL.Plataforma.BD::ACC.Usuario"; 

-------------------------
-- PORTAFOLIO
-------------------------
DELETE FROM   "BSCL.Portafolio.BD::POR.Anotacion"                            ;
DELETE FROM   "BSCL.Portafolio.BD::POR.Autorizado"                           ;
DELETE FROM   "BSCL.Portafolio.BD::POR.CuentaCustodioPortafolio"             ;
DELETE FROM   "BSCL.Portafolio.BD::POR.PreferenciaPagoPortafolio"            ;
DELETE FROM   "BSCL.Portafolio.BD::POR.PreferenciaFacturacion"               ;
DELETE FROM   "BSCL.Portafolio.BD::POR.PortafolioClienteUsoMedioContacto"    ;
DELETE FROM   "BSCL.Portafolio.BD::POR.Portafolio"                           ;

-------------------------
-- CLIENTE
-------------------------
DELETE  FROM "BSCL.SocioNegocio.BD::CLI.DetalleDeclaracionOrigenFondos"; 
DELETE  FROM "BSCL.SocioNegocio.BD::CLI.DeclaracionOrigenFondos";        
DELETE  FROM "BSCL.SocioNegocio.BD::CLI.Cliente";    
DELETE FROM "BSCL.SocioNegocio.BD::SOC.Enrolamiento" WHERE "IdTipoEntidad" = 'CLIENTE';

-------------------------
-- PROVEEDOR
-------------------------
DELETE FROM "BSCL.SocioNegocio.BD::PRO.Proveedor"
WHERE "IdEmpleado" NOT IN (SELECT SOC."IdEmpleado" FROM "BSCL.Plataforma.BD::PLA.Empresa" EMP 
JOIN "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" SOC ON (SOC."IdSocioDeNegocio" = EMP."IdSocioDeNegocio"));


-------------------------
-- CONTRAPARTE
-------------------------
DELETE FROM "BSCL.SocioNegocio.BD::CNT.Contraparte"
WHERE "IdEmpleado" NOT IN (SELECT SOC."IdContraparte" FROM "BSCL.Plataforma.BD::PLA.Empresa" EMP 
JOIN "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" SOC ON (SOC."IdSocioDeNegocio" = EMP."IdSocioDeNegocio"));

DELETE FROM "BSCL.SocioNegocio.BD::SOC.Enrolamiento" WHERE "IdTipoEntidad" = 'CONTRAPARTE' 
AND "IdEntidadEnrolada" NOT IN (SELECT SOC."IdContraparte" FROM "BSCL.Plataforma.BD::PLA.Empresa" EMP 
JOIN "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" SOC ON (SOC."IdSocioDeNegocio" = EMP."IdSocioDeNegocio"));

-------------------------
-- EMPLEADO
-------------------------
DELETE FROM "BSCL.SocioNegocio.BD::EMP.Empleado"
WHERE "IdEmpleado" NOT IN (SELECT SOC."IdEmpleado" FROM "BSCL.Plataforma.BD::PLA.Empresa" EMP 
JOIN "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" SOC ON (SOC."IdSocioDeNegocio" = EMP."IdSocioDeNegocio"));

DELETE FROM "BSCL.SocioNegocio.BD::SOC.Enrolamiento" WHERE "IdTipoEntidad" = 'EMPLEADO' 
AND "IdEntidadEnrolada" NOT IN (SELECT SOC."IdEmpleado" FROM "BSCL.Plataforma.BD::PLA.Empresa" EMP 
JOIN "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" SOC ON (SOC."IdSocioDeNegocio" = EMP."IdSocioDeNegocio"));

-------------------------
-- PERSONA
-------------------------
DELETE FROM "BSCL.SocioNegocio.BD::PER.DireccionFisica" 
WHERE "IdPersona" NOT IN (SELECT SOC."IdPersona" FROM "BSCL.Plataforma.BD::PLA.Empresa" EMP 
JOIN "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" SOC ON (SOC."IdSocioDeNegocio" = EMP."IdSocioDeNegocio"));

DELETE FROM "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad"
WHERE "IdPersona" NOT IN (SELECT SOC."IdPersona" FROM "BSCL.Plataforma.BD::PLA.Empresa" EMP 
JOIN "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" SOC ON (SOC."IdSocioDeNegocio" = EMP."IdSocioDeNegocio"));

DELETE FROM "BSCL.SocioNegocio.BD::PER.DireccionElectronica"
WHERE "IdPersona" NOT IN (SELECT SOC."IdPersona" FROM "BSCL.Plataforma.BD::PLA.Empresa" EMP 
JOIN "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" SOC ON (SOC."IdSocioDeNegocio" = EMP."IdSocioDeNegocio"));

DELETE FROM "BSCL.SocioNegocio.BD::PER.Telefono"
WHERE "IdPersona" NOT IN (SELECT SOC."IdPersona" FROM "BSCL.Plataforma.BD::PLA.Empresa" EMP 
JOIN "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" SOC ON (SOC."IdSocioDeNegocio" = EMP."IdSocioDeNegocio"));

DELETE FROM "BSCL.SocioNegocio.BD::PER.GrupoDireccion"
WHERE "IdPersona" NOT IN (SELECT SOC."IdPersona" FROM "BSCL.Plataforma.BD::PLA.Empresa" EMP 
JOIN "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" SOC ON (SOC."IdSocioDeNegocio" = EMP."IdSocioDeNegocio"));

DELETE FROM "BSCL.SocioNegocio.BD::PER.GrupoDireccionDetalle";

DELETE FROM "BSCL.SocioNegocio.BD::PER.CuentaBancaria"
WHERE "IdPersona" NOT IN (SELECT SOC."IdPersona" FROM "BSCL.Plataforma.BD::PLA.Empresa" EMP 
JOIN "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" SOC ON (SOC."IdSocioDeNegocio" = EMP."IdSocioDeNegocio"));

DELETE FROM "BSCL.SocioNegocio.BD::PER.TarjetaBancaria"
WHERE "IdPersona" NOT IN (SELECT SOC."IdPersona" FROM "BSCL.Plataforma.BD::PLA.Empresa" EMP 
JOIN "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" SOC ON (SOC."IdSocioDeNegocio" = EMP."IdSocioDeNegocio"));

DELETE FROM "BSCL.SocioNegocio.BD::PER.NombreFantasia"
WHERE "IdPersona" NOT IN (SELECT SOC."IdPersona" FROM "BSCL.Plataforma.BD::PLA.Empresa" EMP 
JOIN "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" SOC ON (SOC."IdSocioDeNegocio" = EMP."IdSocioDeNegocio"));

DELETE FROM "BSCL.SocioNegocio.BD::REL.RelacionPersona"
WHERE "IdPersonaOrigen" NOT IN (SELECT SOC."IdPersona" FROM "BSCL.Plataforma.BD::PLA.Empresa" EMP 
JOIN "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" SOC ON (SOC."IdSocioDeNegocio" = EMP."IdSocioDeNegocio"));


DELETE FROM "BSCL.SocioNegocio.BD::PER.Fatca"
WHERE "IdPersona" NOT IN (SELECT SOC."IdPersona" FROM "BSCL.Plataforma.BD::PLA.Empresa" EMP 
JOIN "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" SOC ON (SOC."IdSocioDeNegocio" = EMP."IdSocioDeNegocio"));


DELETE FROM "BSCL.SocioNegocio.BD::PER.Persona"
WHERE "IdPersona" NOT IN (SELECT SOC."IdPersona" FROM "BSCL.Plataforma.BD::PLA.Empresa" EMP 
JOIN "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" SOC ON (SOC."IdSocioDeNegocio" = EMP."IdSocioDeNegocio"));

DELETE FROM "BSCL.SocioNegocio.BD::SOC.Enrolamiento" WHERE "IdTipoEntidad" = 'PERSONA' 
AND "IdEntidadEnrolada" NOT IN (SELECT SOC."IdPersona" FROM "BSCL.Plataforma.BD::PLA.Empresa" EMP 
JOIN "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" SOC ON (SOC."IdSocioDeNegocio" = EMP."IdSocioDeNegocio"));

DELETE FROM "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio"
WHERE "IdSocioDeNegocio" NOT IN (SELECT SOC."IdSocioDeNegocio" FROM "BSCL.Plataforma.BD::PLA.Empresa" EMP 
JOIN "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" SOC ON (SOC."IdSocioDeNegocio" = EMP."IdSocioDeNegocio"));


-------------------------
-- COMMIT
-------------------------
commit;


