}

SET SCHEMA "BSCL";

/*==============================================================*/
/* Insert Table:                                                */
/*==============================================================*/

 
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio"("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion") VALUES(1737,'BSCL.Tesoreria.BD.Vistas::CAR.ConsultarTipoCargaMasiva',0,'Q','Consultar Tipo de Carga Masiva');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio"("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion") VALUES(1738,'BSCL.Tesoreria.BD.Vistas::CAR.ConsultarCargaMasiva',0,'Q','Consultar Carga Masiva');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio"("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion") VALUES(1739,'BSCL.Tesoreria.BD.Vistas::CAR.ConsultarCargaMasivaDetalle',0,'Q','Consultar Carga Masiva Detalle');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio"("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion") VALUES(1740,'BSCL.Tesoreria.BD.Servicios.CAR::CrearCargaMasiva',0,'P','Servicio Crear Carga Masiva');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio"("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion") VALUES(1748,'BSCL.Tesoreria.BD.Servicios.CAR::EliminarCargaMasiva',0,'P','Servicio Eliminar Carga Masiva');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio"("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion") VALUES(1749,'BSCL.Tesoreria.BD.Servicios.CAR::ProcesarCargaMasiva',0,'P','Servicio Procesar Carga Masiva');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio"("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion") VALUES(1756, 'BSCL.Plataforma.BD.Vistas::BAN.ConsultarTipoBanco',0,'Q','Consulta tipo de banco');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio"("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion") VALUES(1757, 'BSCL.Plataforma.BD.Vistas::BAN.ConsultarPlaza',0,'Q','Consulta plaza');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio"("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion") VALUES(1762, 'BSCL.Custodia.BD.Servicios::ConsultarCustodiaGeneral',0,'P','Consultar custodia general(oficina)');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion") VALUES(1763,'BSCL.Custodia.BD.Servicios::GTA.ConsultarCoberturaGarantiaResumen',0,'P','Consultar Cobertura Garantia Resumen');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion") VALUES(1765,'BSCL.Custodia.BD.Vistas::GTA.ConsultarCoberturaGarantiaOperaciones',0,'Q','Consultar Cobertura Garantias Operaciones');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion") VALUES(1766,'BSCL.Custodia.BD.Vistas::GTA.ConsultarCoberturaGarantiaDetalle',0,'Q','Consultar Cobertura Garantias Detalles');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion") VALUES(1768,'BSCL.Custodia.BD.Vistas::GTA.ConsultarEntidadDestinoGarantia',0,'Q','Consultar Entidad Destino Garantia');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN","TablasTempOUT") VALUES(1775, 'BSCL.Tesoreria.BD.Servicios.BAN::ConsultarLibretaBanco', 0, 'P', '', '', '');

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN","TablasTempOUT")
VALUES (1776, 'BSCL.Tesoreria.BD.Vistas::DOC.ConsultarEnvioDocumento', 0, 'Q', 'Tesoreria: Vista ConsultarEnvioDocumento', '', '');
INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN","TablasTempOUT")
VALUES (1777, 'BSCL.Tesoreria.BD.Vistas::DOC.ConsultarEnvioDocumentoDetalle', 0, 'Q', 'Tesoreria: Vista ConsultarEnvioDocumentoDetalle', '', '');
INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN","TablasTempOUT")
VALUES (1779, 'BSCL.Tesoreria.BD.Servicios.DOC::GestionEnvioDocumentos', 0, 'P', '', '', '');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio"("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN","TablasTempOUT") 
VALUES(1782,'BSCL.Tesoreria.BD.Servicios.CIE::CierreDiario',0,'P','Revision cierre diario tesorería','','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio"("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN","TablasTempOUT") 
VALUES(1783,'BSCL.Tesoreria.BD.Servicios.CIE::ConsultarResumenCierreDiario',0,'P','Consultar Resumen Cierre Diario para Cierre de Modulo','','');








INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )
VALUES(3121,'BSCL.Plataforma.BD.Vistas::CAM.ConsultarGrupoCamara',0,'Q','Consulta grupo de camaras del CCLV');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(3045,'BSCL.Orden.BD.Vistas::ConsultarConfiguracionCanalDistribucion',0,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(3046,'BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarCargaSolicitudesAporteRescate',0,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(3047,'BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarResumenCargaSolicitudesAporteRescate',0,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(3052,'BSCL.FondoMutuo.BD.Servicios::ConsultarComprobante',0,'P','Comprobante de Solicitud de Aporte/Rescate Fondos Mutuos');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(3053,'BSCL.FondoMutuo.BD.Servicios::ConsultarCartaLiquidacion',0,'P','Comprobante Carta de Liquidacion de Fondos Mutuos');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(3057,'BSCL.Facturacion.BD.Vistas::ConsultarFormatoContratoCobroPeriodico',0,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(3058,'BSCL.FondoMutuo.BD.Servicios::AnulacionCargaSolicitudAporteRescate',0,'P','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(3060,'BSCL.Orden.BD.Vistas::ConsultarTBSOLICITUDES',0,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(3061,'BSCL.Orden.BD.Vistas::ConsultarTBRESPUESTAS',0,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(3062,'BSCL.Orden.BD.Servicios::ProcesarMensajesNegociacion',0,'P','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(3101,'BSCL.Normativa.BD.Vistas::CAR.ConsultarNominas',0,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(3102,'BSCL.Normativa.BD.Vistas::CAR.ConsultarPlantillas',0,'Q','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(3103,'BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarTipoMovimientoFondoMutuoCarga',0,'Q','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(3104,'BSCL.Normativa.BD.Servicios::CAR.GrabarNomina',0,'P','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(3105,'BSCL.Normativa.BD.Servicios::CAR.CopiarPlantilla',0,'P','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(3106,'BSCL.Facturacion.BD.Servicios::CrearContratoCobroPeriodico',0,'P','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(3107,'BSCL.Normativa.BD.Vistas::CAR.ConsultarParametros',0,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(3108,'BSCL.Normativa.BD.Vistas::CAR.ConsultarCartolas',0,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(3109,'BSCL.FondoMutuo.BD.Servicios::CrearProgramaCargaBatch',0,'P','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(3110,'BSCL.Normativa.BD.Vistas::CAR.ConsultarRecursos',0,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(3111,'BSCL.Normativa.BD.Servicios::CAR.GrabarRecurso',0,'P','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(3112,'BSCL.FondoMutuo.BD.Servicios::CrearProgramaCertificacionBatch',0,'P','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(3113,'BSCL.Normativa.BD.Servicios::CAR.GrabarConfig',0,'P','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(3114,'BSCL.FondoMutuo.BD.Servicios::ConsultarInformeLiquidacion',0,'P','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(3115,'BSCL.Normativa.BD.Servicios::CAR.ActualizarVigenciaPlantilla',0,'P','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(3116,'BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarCartaLiquidacion',0,'Q','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(3117,'BSCL.Normativa.BD.Servicios::CAR.ActualizarValorRecurso',0,'P','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(3118,'BSCL.Orden.BD.Servicios::ConsultarInformacionEnvioFacturacionMasivo',0,'P','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(3119,'BSCL.Orden.BD.Servicios::EnvioFacturacionMasivo',0,'P','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(3120,'BSCL.Normativa.BD.Servicios::CAR.ActualizarReproceso',0,'P','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(3122,'BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarSolicitudAgf',0,'Q','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(3123,'BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarEstadoInterfazAgf',0,'Q','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(3124,'BSCL.FondoMutuo.BD.Servicios::CambiarEstadoSolicitudAgf',0,'P','');


INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(3125,'BSCL.CanalAgente.BD.Servicio::ConsultarHistorialMovimientosCustodia',0,'P','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(3126,'BSCL.CanalAgente.BD.Servicio::ConsultarHistorialMovimientosCuentaInversion',0,'P','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(3128,'BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarTipoRegistro',0,'Q','');


INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(3129,'BSCL.Plataforma.BD.Vistas::INS.ConsultarCorredora',0,'Q','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(3130,'BSCL.Plataforma.BD.Vistas::INS.ConsultarCorredoraCodigos',0,'Q','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(3131,'BSCL.Normativa.BD.Vistas::CAR.ConsultarConfig',0,'Q','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(3132,'BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaGrupoYDireccionElectronica',0,'Q','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES (3135,'BSCL.Custodia.BD.Servicios::MRF.AsignacionAutomatica' , 1,  'P', '');


INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN","TablasTempOUT" ) 
VALUES(3136,'BSCL.Custodia.BD.Servicios::VAL.ValorizarCustodiaMasiva',0,'P','',
'p_Instrumentos=BSCL.Custodia.BD::VAL.ValorizarCustodiaMasiva_Entrada_Instrumentos;p_Carteras=BSCL.Custodia.BD::VAL.ValorizarCustodiaMasiva_Entrada_Carteras;',
'p_Out_Depuracion=BSCL.Plataforma.BD::PLA.Depuracion_Entrada_Depuracion;');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(263,'BSCL.Orden.BD.Servicios::CuadraturaVencimientoOrdenes',0,'P','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(3051,'BSCL.Orden.BD.Servicios::AperturaModuloOrdenes',0,'P','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(3133,'BSCL.SocioNegocio.CrystalReports.Servicios::ContratoConvenioUsoPlataformaFirmaElectronicaPN',0,'P','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(3134,'BSCL.SocioNegocio.CrystalReports.Servicios::ContratoConvenioUsoPlataformaFirmaElectronicaPJ',0,'P','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(3137,'BSCL.FondoMutuo.BD.Utilitarios::CalcularMontoNumeroCuota',0,'P','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(3138,'BSCL.Plataforma.BD.Vistas::BAT.ConsultarAgenda',0,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(3139,'BSCL.Plataforma.BD.Vistas::BAT.UIConsultarAgenda',0,'Q','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(3140,'BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona',0,'Q','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(3141,'BSCL.Plataforma.BD.Vistas::PAU.ConsultarPauta',0,'Q','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(3142,'BSCL.Plataforma.BD.Vistas::PAU.ConsultarControlPauta',0,'Q','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(3143,'BSCL.FondoMutuo.BD.Servicios::ConsultarFlujoCustodiaFondoMutuo',0,'P','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(3144,'BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarConfiguracionFondoAdministracionLiquidez',0,'Q','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(3145,'BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarConfiguracionPortafolioAdministracionLiquidez',0,'Q','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(3146,'BSCL.FondoMutuo.BD.Servicios::CrearProgramarGeneraArchivoOperacionesAGF',0,'P','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(3147,'BSCL.FondoMutuo.BD.Servicios::ConfigurarFondoAdministracionLiquidez',0,'P','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(3148,'BSCL.FondoMutuo.BD.Servicios::ConfigurarPortafolioAdministracionLiquidez',0,'P','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(3149,'BSCL.Plataforma.BD.Vistas::PLA.ConsultarUsoFlujoCaja',0,'Q','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(3150,'BSCL.CanalAgente.BD.Servicios::ConsultaAvanzadaClientes',0,'P','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN","TablasTempOUT" ) 
VALUES(3151,'BSCL.Orden.BD.Servicios::CrearOrdenes',0,'P','',
'p_Ordenes=BSCL.Orden.BD::ORD.CrearOrdenes_Entrada_Ordenes;p_CobrosModificados=BSCL.Orden.BD::ORD.CrearOrdenes_Entrada_CobrosModificados;p_Garantias=BSCL.Orden.BD::ORD.CrearOrdenes_Entrada_Garantias;',
'P_OUT_RESULTADO=BSCL.Orden.BD::ORD.CrearOrdenes_Salida_Resultado;P_OUT_COBROS=BSCL.Orden.BD::ORD.CrearOrdenes_Salida_Cobros;P_OUT_GARANTIAS=BSCL.Orden.BD::ORD.CrearOrdenes_Salida_Garantias;P_OUT_INFORMACIONDETALLE=BSCL.Orden.BD::ORD.CrearOrdenes_Salida_InformacionDetalle;P_OUT_ADVERTENCIASDETALLE=BSCL.Orden.BD::ORD.CrearOrdenes_Salida_AdvertenciasDetalle;P_OUT_ERRORESDETALLE=BSCL.Orden.BD::ORD.CrearOrdenes_Salida_ErroresDetalle');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(3152,'BSCL.Facturacion.BD.Servicios::DteActualizarEstadoComprobante',0,'P','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(3153,'BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarLogAdministracionLiquidez',0,'Q','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(3154,'BSCL.Custodia.BD.Servicios::VAL.ValorizarCustodiaMasivaUI',0,'P','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN","TablasTempOUT")  
VALUES(3155,'BSCL.Plataforma.BD.Servicios::MER.CalcularParidadYTipoCambio',3,'P',' ', 
'p_Entrada=BSCL.Plataforma.BD::MER.CalcularParidadYTipoCambio_Entrada_Entrada', 
'P_OUT_SALIDA=BSCL.Plataforma.BD::MER.CalcularParidadYTipoCambio_Salida_Salida');


INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(3156,'BSCL.FondoMutuo.BD.Servicios::AperturaAutomaticoFondos',0,'P','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion") 
VALUES (3160,'BSCL.Mesa.BD.Servicios::MRF.ValidaArchivoMasivoRF' , 1,  'P', '');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion") 
VALUES (3161,'BSCL.Plataforma.BD.Vistas::MER.ConsultarEmisor' , 0,  'Q', '');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion") 
VALUES (3162,'BSCL.Facturacion.BD.Servicios::AgruparMovimientoComprobanteCobrosCostos' , 0,  'P', '');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(3163,'BSCL.FondoMutuo.BD.Servicios::CertificarPorFiltrosSolicitudAporteRescate',0,'P','');


INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion") 
VALUES (3164,'BSCL.Mesa.BD.Vistas::MRF.ConsultarCargaMasivaOperacionesRF' , 1,  'Q', '');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion") 
VALUES (3165,'BSCL.Mesa.BD.Servicios::MRF.GrabarCargaMasivaOperacionesRF' , 1,  'P', '');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion") 
VALUES (3166,'BSCL.FondoMutuo.BD.Servicios::ReaperturaFondos' , 0,  'P', '');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion") 
VALUES (3167,'BSCL.Normativa.BD.Vistas::ConsultarTipoJuntaAccionistas' , 1,  'Q', '');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion") 
VALUES (3168,'BSCL.Custodia.BD.Vistas::CUS.ConsultarJuntaAccionistas' , 1,  'Q', '');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion") 
VALUES (3169,'BSCL.Normativa.BD.Vistas::ConsultarEstadoJuntaAccionistas' , 1,  'Q', '');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion") 
VALUES (3170,'BSCL.Normativa.BD.Vistas::ConsultarCustodiaCierreJuntaAccionista' , 1,  'Q', '');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion") 
VALUES (3171,'BSCL.Normativa.BD.Vistas::ConsultarTipoInstruccionJuntaAccionista' , 1,  'Q', '');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion") 
VALUES (3172,'BSCL.Normativa.BD.Vistas::ConsultarNominasJuntaAccionista' , 1,  'Q', '');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion") 
VALUES (3173,'BSCL.Custodia.BD.Servicios::EVC.CrearEventoJuntaAccionista' , 1,  'P', '');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion") 
VALUES (3175,'BSCL.Mesa.BD.Servicios::MRF.ProcesarCargaMasivaOperacionesRF' , 1,  'P', '');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion") 
VALUES (3176,'BSCL.Custodia.BD.Servicios::EVC.ConsultarInstructivoVotacionJuntaAccionistas' , 1,  'P', '');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion") 
VALUES (3177,'BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPatrimonio' , 1,  'Q', '');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion") 
VALUES (3178,'BSCL.Custodia.BD.Vistas::CUS.ConsultarArchivoCustodia' , 1,  'Q', '');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion") 
VALUES (3179,'BSCL.Custodia.BD.Servicios::EVC.ModificarEventoJuntaAccionista' , 1,  'P', '');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion") 
VALUES (3180,'BSCL.Custodia.BD.Servicios::EVC.EliminarJuntaAccionista' , 1,  'P', '');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion") 
VALUES (3181,'BSCL.Custodia.BD.Servicios::EVC.CerrarJuntaAccionista' , 1,  'P', '');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion") 
VALUES (3182,'BSCL.Custodia.BD.Vistas::CUS.ConsultarTiposJuntaAccionista' , 1,  'Q', '');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion") 
VALUES (3183,'BSCL.CanalAgente.BD.Servicios::ConsultarFlujoInstrumento' , 1,  'P', '');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion") 
VALUES (3190,'BSCL.Orden.BD.Servicios::ReasignacionFidessa' , 1,  'P', '');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion") 
VALUES (3191,'BSCL.Custodia.BD.Servicios::EVC.NotificarJuntaAccionistas' , 1,  'P', '');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion") 
VALUES (3192,'BSCL.Plataforma.BD.Vistas::COM_2.ConsultarClaseCobro' , 1,  'Q', '');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion") 
VALUES (3193,'BSCL.Plataforma.BD.Vistas::COM_2.ConsultarTipoCobro' , 1,  'Q', '');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion") 
VALUES (3194,'BSCL.Plataforma.BD.Vistas::COM_2.ConsultarEstadoCobro' , 1,  'Q', '');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion") 
VALUES (3195,'BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarFondoCombobox' , 1,  'Q', '');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion") 
VALUES (3196,'BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarHorarioFondo' , 1,  'Q', '');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion") 
VALUES (3198,'BSCL.Plataforma.BD.Vistas::COM_2.ConsultarBaseTasaCobro' , 1,  'Q', '');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion") 
VALUES (3199,'BSCL.Plataforma.BD.Vistas::COM_2.ConsultarTipoRangoCobro' , 1,  'Q', '');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion") 
VALUES (3203,'BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarRegimenSerieDeFondo' , 1,  'Q', '');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion") 
VALUES (3207,'BSCL.Orden.BD.Servicios::ConsultarCustodiaGarantizableSimultanea' , 1,  'P', '');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion") 
VALUES (3215,'BSCL.Facturacion.BD.Vistas::ConsultarLibroDteComprobante' , 1,  'Q', '');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN", "TablasTempOUT" ) 
VALUES(3224,'BSCL.Facturacion.BD.Servicios::DteEnviarLibro',0,'P','','P_COMPROBANTE=BSCL.Facturacion.BD::GBL.DteEnviarLibro_IN_Comprobante','P_OUT_INFORMACIONDETALLE=BSCL.Facturacion.BD::GBL.DteEnviarLibro_OUT_InformacionDetalle;P_OUT_RESULTADOPROCESO=BSCL.Facturacion.BD::GBL.DteEnviarLibro_OUT_ResultadoProceso;P_OUT_ADVERTENCIASDETALLE=BSCL.Facturacion.BD::GBL.DteEnviarLibro_OUT_AdvertenciasDetalle;P_OUT_ERRORESDETALLE=BSCL.Facturacion.BD::GBL.DteEnviarLibro_OUT_ErroresDetalle');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN", "TablasTempOUT" ) 
VALUES(3225,'BSCL.Plataforma.BD.Vistas::COM.ConsultarClasificacionCobro',0,'Q','','','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN", "TablasTempOUT" ) 
VALUES(3226,'BSCL.Normativa.BD.Vistas::DOC.ConsultarUltimaVersionEsquema',0,'Q','','','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN", "TablasTempOUT" ) 
VALUES(3227,'BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarEstadoRegimenFondoMutuo',0,'Q','','','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN", "TablasTempOUT" ) 
VALUES(3228,'BSCL.FondoMutuo.BD.Servicios::ModificarRegimenFondoMutuo',0,'P','','','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN", "TablasTempOUT" ) 
VALUES(3229,'BSCL.Facturacion.BD.Servicios::CalcularMovimientoComprobanteCobrosCostos',0,'P','','P_MOVIMIENTO=BSCL.Facturacion.BD::GBL.CalcularMovimientoComprobanteCobrosCostos_IN_MovimientoComprobante;P_COBRO=BSCL.Facturacion.BD::GBL.CalcularMovimientoComprobanteCobrosCostos_IN_Cobro','P_OUT_INFORMACIONDETALLE=BSCL.Facturacion.BD::GBL.CalcularMovimientoComprobanteCobrosCostos_OUT_InformacionDetalle;P_OUT_RESULTADOPROCESO=BSCL.Facturacion.BD::GBL.CalcularMovimientoComprobanteCobrosCostos_OUT_ResultadoProceso;P_OUT_ADVERTENCIASDETALLE=BSCL.Facturacion.BD::GBL.CalcularMovimientoComprobanteCobrosCostos_OUT_AdvertenciasDetalle;P_OUT_ERRORESDETALLE=BSCL.Facturacion.BD::GBL.CalcularMovimientoComprobanteCobrosCostos_OUT_ErroresDetalle');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN", "TablasTempOUT" ) 
VALUES(3231,'BSCL.Plataforma.BD.Vistas::INS.ConsultarEntidadClasificadoraRiesgo',0,'Q','','','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN", "TablasTempOUT" ) 
VALUES(3232,'BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumentoValorClasificacionRiesgo',0,'Q','','','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN", "TablasTempOUT" ) 
VALUES(3237,'BSCL.Orden.BD.Vistas::ConsultarClientesLibroOrdenesGrupo',0,'Q','','','');


INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN", "TablasTempOUT" ) 
VALUES(3238,'BSCL.Orden.BD.Servicios::ConsultarTransaccionesAnuladasModificadas',0,'P','','','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN", "TablasTempOUT" ) 
VALUES(3240,'BSCL.Custodia.BD::OPE.ClaveOperacionesPendientes',0,'P','','','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN", "TablasTempOUT" ) 
VALUES(3242,'BSCL.Normativa.BD.Servicios::TRI.DJ1919.ObtenerTotales',0,'P','','','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN", "TablasTempOUT" ) 
VALUES(3243,'BSCL.Normativa.BD.Vistas::TRI.DJ1919.Datos',0,'Q','','','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN", "TablasTempOUT" ) 
VALUES(3244,'BSCL.Normativa.BD.Vistas::TRI.DJ1919.Invalidos',0,'Q','','','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN", "TablasTempOUT" ) 
VALUES(3245,'BSCL.Normativa.BD.Servicios::TRI.DJ1919.GrabarDetalle',0,'P','','','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN", "TablasTempOUT" ) 
VALUES(3251,'BSCL.Plataforma.BD.Vistas::INS.UI.ConsultarInstrumento',0,'Q','','','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN", "TablasTempOUT" ) 
VALUES(3253,'BSCL.SocioNegocio.BD.Vistas::PER.ConsultarTipoCargoPersona',0,'Q','','','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN", "TablasTempOUT" ) 
VALUES(3254,'BSCL.Normativa.BD.Servicios::TRI.DJ1823.ObtenerTotales',0,'P','','','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN", "TablasTempOUT" ) 
VALUES(3256,'BSCL.Normativa.BD.Vistas::TRI.DJ1823.Invalidos',0,'Q','','','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN", "TablasTempOUT" ) 
VALUES(3257,'BSCL.Normativa.BD.Servicios::TRI.DJ1823.GrabarDetalle',0,'P','','','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN", "TablasTempOUT" ) 
VALUES(3258,'BSCL.Normativa.BD.Vistas::TRI.DJ1823.Datos',0,'Q','','','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN", "TablasTempOUT" ) 
VALUES(3259,'BSCL.Normativa.BD.Vistas::TRI.DJ1829.Datos',0,'Q','','','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN", "TablasTempOUT" ) 
VALUES(3260,'BSCL.Normativa.BD.Vistas::TRI.DJ1829.Invalidos',0,'Q','','','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN", "TablasTempOUT" ) 
VALUES(3261,'BSCL.Normativa.BD.Servicios::TRI.DJ1829.EliminarDetalles',0,'P','','','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN", "TablasTempOUT" ) 
VALUES(3262,'BSCL.Normativa.BD.Servicios::TRI.DJ1829.ObtenerTotales',0,'P','','','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN", "TablasTempOUT" ) 
VALUES(3263,'BSCL.Normativa.BD.Servicios::TRI.DJ1829.GrabarDetalle',0,'P','','','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN", "TablasTempOUT" ) 
VALUES(3264,'BSCL.Normativa.BD.Vistas::TRI.DJ1839.Datos',0,'Q','','','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN", "TablasTempOUT" ) 
VALUES(3265,'BSCL.Normativa.BD.Vistas::TRI.DJ1839.Invalidos',0,'Q','','','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN", "TablasTempOUT" ) 
VALUES(3266,'BSCL.Custodia.BD.Vistas::CUS.ConsultarUsoFlujoCustodia',0,'Q','','','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN", "TablasTempOUT" ) 
VALUES(3267,'BSCL.Normativa.BD.Servicios::TRI.DJ1839.ObtenerTotales',0,'P','','','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN", "TablasTempOUT" ) 
VALUES(3268,'BSCL.Normativa.BD.Servicios::TRI.DJ1839.GrabarDetalle',0,'P','','','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN", "TablasTempOUT" ) 
VALUES(3269,'BSCL.Normativa.BD.Servicios::TRI.DJ1839.EliminarDetalles',0,'P','','','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN", "TablasTempOUT" ) 
VALUES(3270,'BSCL.Normativa.BD.Vistas::TRI.DJ1848.Datos',0,'Q','','','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN", "TablasTempOUT" ) 
VALUES(3271,'BSCL.Normativa.BD.Vistas::TRI.DJ1848.Invalidos',0,'Q','','','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN", "TablasTempOUT" ) 
VALUES(3272,'BSCL.Normativa.BD.Servicios::TRI.DJ1848.EliminarDetalles',0,'P','','','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN", "TablasTempOUT" ) 
VALUES(3273,'BSCL.Normativa.BD.Servicios::TRI.DJ1848.GrabarDetalle',0,'P','','','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN", "TablasTempOUT" ) 
VALUES(3274,'BSCL.Normativa.BD.Servicios::TRI.DJ1848.ObtenerTotales',0,'P','','','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN", "TablasTempOUT" ) 
VALUES(3275,'BSCL.Normativa.BD.Vistas::TRI.DJ1849.Datos',0,'Q','','','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN", "TablasTempOUT" ) 
VALUES(3276,'BSCL.Normativa.BD.Vistas::TRI.DJ1849.Invalidos',0,'Q','','','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN", "TablasTempOUT" ) 
VALUES(3277,'BSCL.Normativa.BD.Servicios::TRI.DJ1849.EliminarDetalles',0,'P','','','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN", "TablasTempOUT" ) 
VALUES(3278,'BSCL.Normativa.BD.Servicios::TRI.DJ1849.GrabarDetalle',0,'P','','','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN", "TablasTempOUT" ) 
VALUES(3279,'BSCL.Normativa.BD.Servicios::TRI.DJ1849.ObtenerTotales',0,'P','','','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN", "TablasTempOUT" ) 
VALUES(3283,'BSCL.Normativa.BD.Vistas::TRI.DJ1870.Datos',0,'Q','','','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN", "TablasTempOUT" ) 
VALUES(3284,'BSCL.Normativa.BD.Vistas::TRI.DJ1870.Invalidos',0,'Q','','','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN", "TablasTempOUT" ) 
VALUES(3285,'BSCL.Normativa.BD.Servicios::TRI.DJ1870.EliminarDetalles',0,'P','','','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN", "TablasTempOUT" ) 
VALUES(3286,'BSCL.Normativa.BD.Servicios::TRI.DJ1870.GrabarDetalle',0,'P','','','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN", "TablasTempOUT" ) 
VALUES(3287,'BSCL.Normativa.BD.Servicios::TRI.DJ1870.ObtenerTotales',0,'P','','','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN", "TablasTempOUT" ) 
VALUES(3288,'BSCL.Normativa.BD.Vistas::TRI.DJ1874.Datos',0,'Q','','','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN", "TablasTempOUT" ) 
VALUES(3289,'BSCL.Normativa.BD.Vistas::TRI.DJ1874.Invalidos',0,'Q','','','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN", "TablasTempOUT" ) 
VALUES(3290,'BSCL.Normativa.BD.Servicios::TRI.DJ1874.EliminarDetalles',0,'P','','','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN", "TablasTempOUT" ) 
VALUES(3291,'BSCL.Normativa.BD.Servicios::TRI.DJ1874.GrabarDetalle',0,'P','','','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN", "TablasTempOUT" ) 
VALUES(3292,'BSCL.Normativa.BD.Servicios::TRI.DJ1874.ObtenerTotales',0,'P','','','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN", "TablasTempOUT" ) 
VALUES(3293,'BSCL.Normativa.BD.Vistas::TRI.DJ1890.Datos',0,'Q','','','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN", "TablasTempOUT" ) 
VALUES(3294,'BSCL.Normativa.BD.Vistas::TRI.DJ1890.Invalidos',0,'Q','','','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN", "TablasTempOUT" ) 
VALUES(3295,'BSCL.Normativa.BD.Servicios::TRI.DJ1890.EliminarDetalles',0,'P','','','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN", "TablasTempOUT" ) 
VALUES(3296,'BSCL.Normativa.BD.Servicios::TRI.DJ1890.GrabarDetalle',0,'P','','','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN", "TablasTempOUT" ) 
VALUES(3297,'BSCL.Normativa.BD.Servicios::TRI.DJ1890.ObtenerTotales',0,'P','','','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN", "TablasTempOUT" ) 
VALUES(3298,'BSCL.Normativa.BD.Vistas::TRI.DJ1862.Datos',0,'Q','','','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN", "TablasTempOUT" ) 
VALUES(3299,'BSCL.Normativa.BD.Vistas::TRI.DJ1862.Invalidos',0,'Q','','','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN", "TablasTempOUT" ) 
VALUES(3300,'BSCL.Normativa.BD.Servicios::TRI.DJ1862.ObtenerTotales',0,'P','','','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN", "TablasTempOUT" ) 
VALUES(3301,'BSCL.Normativa.BD.Servicios::TRI.DJ1862.GrabarDetalle',0,'P','','','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN", "TablasTempOUT" ) 
VALUES(3302,'BSCL.Normativa.BD.Servicios::TRI.DJ1862.EliminarDetalles',0,'P','','','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN", "TablasTempOUT" ) 
VALUES(3303,'BSCL.Normativa.BD.Vistas::TRI.DJ1894.Invalidos',0,'Q','','','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN", "TablasTempOUT" ) 
VALUES(3304,'BSCL.Normativa.BD.Servicios::TRI.DJ1894.EliminarDetalles',0,'P','','','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN", "TablasTempOUT" ) 
VALUES(3305,'BSCL.Normativa.BD.Servicios::TRI.DJ1894.GrabarDetalle',0,'P','','','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN", "TablasTempOUT" ) 
VALUES(3073,'BSCL.Normativa.BD.Servicios::TRI.DJ1894.ObtenerTotales',0,'P','','','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN", "TablasTempOUT" ) 
VALUES(3306,'BSCL.Normativa.BD.Vistas::TRI.DJ1922.Datos',0,'Q','','','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN", "TablasTempOUT" ) 
VALUES(3307,'BSCL.Normativa.BD.Vistas::TRI.DJ1922.Invalidos',0,'Q','','','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN", "TablasTempOUT" ) 
VALUES(3308,'BSCL.Normativa.BD.Servicios::TRI.DJ1922.EliminarDetalles',0,'P','','','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN", "TablasTempOUT" ) 
VALUES(3309,'BSCL.Normativa.BD.Servicios::TRI.DJ1922.GrabarDetalle',0,'P','','','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN", "TablasTempOUT" ) 
VALUES(3310,'BSCL.Normativa.BD.Servicios::TRI.DJ1922.ObtenerTotales',0,'P','','','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN", "TablasTempOUT" ) 
VALUES(3311,'BSCL.Normativa.BD.Vistas::TRI.DJ1932.Datos',0,'Q','','','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN", "TablasTempOUT" ) 
VALUES(3312,'BSCL.Normativa.BD.Vistas::TRI.DJ1932.Invalidos',0,'Q','','','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN", "TablasTempOUT" ) 
VALUES(3313,'BSCL.Normativa.BD.Servicios::TRI.DJ1932.EliminarDetalles',0,'P','','','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN", "TablasTempOUT" ) 
VALUES(3314,'BSCL.Normativa.BD.Servicios::TRI.DJ1932.GrabarDetalle',0,'P','','','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN", "TablasTempOUT" ) 
VALUES(3315,'BSCL.Normativa.BD.Servicios::TRI.DJ1932.ObtenerTotales',0,'P','','','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN", "TablasTempOUT" ) 
VALUES(3316,'BSCL.Normativa.BD.Vistas::TRI.DJ1914.Datos',0,'Q','','','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN", "TablasTempOUT" ) 
VALUES(3317,'BSCL.Normativa.BD.Vistas::TRI.DJ1914.Invalidos',0,'Q','','','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN", "TablasTempOUT" ) 
VALUES(3318,'BSCL.Normativa.BD.Servicios::TRI.DJ1914.EliminarDetalles',0,'P','','','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN", "TablasTempOUT" ) 
VALUES(3319,'BSCL.Normativa.BD.Servicios::TRI.DJ1914.GrabarDetalle',0,'P','','','');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN", "TablasTempOUT" ) 
VALUES(3320,'BSCL.Normativa.BD.Servicios::TRI.DJ1914.ObtenerTotales',0,'P','','','');
--JAVAS
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN", "TablasTempOUT" ) 
VALUES(20008,'PLUGIN_ORD.jar@cl.bcs.ordenes.PrevisualizarDesasignacion',0,'J','','','');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN", "TablasTempOUT" ) 
VALUES(20009,'PLUGIN_ORD.jar@cl.bcs.ordenes.EjecutarDesasignacion',0,'J','','','');


INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN", "TablasTempOUT" ) 
VALUES(20010,'BSCL.Facturacion.BD.Servicios::GenerarInterfazAfp',0,'P','','P_MOVIMIENTO=BSCL.Facturacion.BD::GBL.GenerarInterfazAfp_IN_Movimiento','P_OUT_MOVIMIENTO=BSCL.Facturacion.BD::GBL.GenerarInterfazAfp_OUT_Movimiento;P_OUT_INFORMACIONDETALLE=BSCL.Facturacion.BD::GBL.GenerarInterfazAfp_OUT_InformacionDetalle;P_OUT_RESULTADOPROCESO=BSCL.Facturacion.BD::GBL.GenerarInterfazAfp_OUT_ResultadoProceso;P_OUT_ADVERTENCIASDETALLE=BSCL.Facturacion.BD::GBL.GenerarInterfazAfp_OUT_AdvertenciasDetalle;P_OUT_ERRORESDETALLE=BSCL.Facturacion.BD::GBL.GenerarInterfazAfp_OUT_ErroresDetalle');
/*==============================================================*/
/* Commit:                                                      */
/*==============================================================*/
commit;
