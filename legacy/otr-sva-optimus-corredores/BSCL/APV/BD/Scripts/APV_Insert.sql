set schema "BSCL";

/*==============================================================*/
/* Insert Table Servicio:                                       */
/*==============================================================*/

INSERT INTO "BSCL.Plataforma.BD::PLA.Modulo"
("IdModulo","Codigo","Nombre","IdEstado")
VALUES(
'APV'
,'APV'
,'AHORRO PREVISIONAL VOLUNTARIO'
,'ACT'
);


INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" )  
VALUES(1806,'BSCL.SocioNegocio.BD.Vistas::SOC.UIBlotterGestionEnrolamiento',2,'Q','');


INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN","TablasTempOUT")
VALUES(
6000
,'BSCL.SocioNegocio.BD.Vistas::PER.ConsultarTipoTrabajador'
,0
,'Q'
,''
,''
,''
);

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN","TablasTempOUT")
VALUES(
6001
,'BSCL.APV.BD.Vistas::APV.ConsultarEmpleadorAPV'
,0
,'Q'
,''
,''
,''
);

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN","TablasTempOUT")
VALUES(
6002
,'BSCL.APV.BD.Vistas::SCS.ConsultarFolioContratoSAAPV'
,0
,'Q'
,''
,''
,''
);

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN","TablasTempOUT")
VALUES(
6003
,'BSCL.APV.BD.Vistas::SCS.ConsultarEstadoContratoAPV'
,0
,'Q'
,''
,''
,''
);

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN","TablasTempOUT")
VALUES(
6004
,'BSCL.APV.BD.Vistas::AFI.ConsultarGestionContratosSAAPV'
,0
,'Q'
,''
,''
,''
);

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN","TablasTempOUT")
VALUES(
6005
,'BSCL.APV.BD.Vistas::SCS.ConsultarEntidadRecaudadora'
,0
,'Q'
,''
,''
,''
);

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN","TablasTempOUT")
VALUES(
6006
,'PLUGIN_APV.jar@cl.bcs.APV.GrabarContratoSAAPV'
,0
,'J'
,''
,''
,''
);


INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN","TablasTempOUT")
VALUES(
6007
,'BSCL.APV.CrystalReports.Servicios::NotificacionSAAPVCliente'
,0
,'P'
,'CONSULTA REPORTE NOTIFICACION CLIENTE SAAPV'
,''
,''
);

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN","TablasTempOUT")
VALUES(
6008
,'BSCL.APV.CrystalReports.Servicio::SCS.ConsultaNotificacionEmpleadorSAAPV'
,0
,'P'
,'CONSULTA REPORTE NOTIFICACION EMPLEADOR SAAPV'
,''
,''
);

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN","TablasTempOUT")
VALUES(6009,'PLUGIN_APV.jar@cl.bcs.APV.CambiarEstadoContratosSAAPV',0,'J','','','');


INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN","TablasTempOUT")
VALUES(
6010
,'BSCL.APV.BD.Vistas::SCS.ConsultarDistribucionInversion'
,0
,'Q'
,'CONSULTA DISTRIBUCION INVERCION'
,''
,''
);

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN","TablasTempOUT")
VALUES(6011,'PLUGIN_APV.jar@cl.bcs.APV.ValidaContratoSAAPV',0,'J','','','');

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN","TablasTempOUT")
VALUES(6012,'BSCL.APV.BD.Vistas::APV.ConsultarTipoCargaArchivo',0,'Q','','','');

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN","TablasTempOUT")
VALUES(6013,'BSCL.APV.BD.Vistas::APV.ConsultarGestionCargaMasiva',0,'Q','','','');

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN","TablasTempOUT")
VALUES(6014,'PLUGIN_APV.jar@cl.bcs.APV.CargaMasivaArchivo',0,'J','','','');

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN","TablasTempOUT")
VALUES(6015,'PLUGIN_APV.jar@cl.bcs.APV.CrearRecaudacionDeposito',0,'J','','','');

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN","TablasTempOUT")
VALUES(6016,'BSCL.APV.BD.Vistas::REC.ConsultarRecaudacion',0,'Q','','','');

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN","TablasTempOUT")
VALUES(6017,'BSCL.Tesoreria.BD.Vistas::ConsultarSucursalesBancarias',0,'Q','','','');


--BASTHIAN POR FAVOR AGREGAR EL SERVICIO 6018
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN","TablasTempOUT")
VALUES(6018,'PLUGIN_APV.jar@cl.bcs.APV.CargaRecaudacion',0,'J','','','');
--AHÃ� TENÃ� EL SERVICIO

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN","TablasTempOUT")
VALUES(6019,'BSCL.Portafolio.BD.Vistas::ConsultarPortafolioMatrizAgrupador',0,'Q','','','');

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN","TablasTempOUT")
VALUES(6020,'BSCL.APV.BD.Vistas::REC.ConsultarRegimenTributario',0,'Q','','','');

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN","TablasTempOUT")
VALUES(6021,'BSCL.APV.BD.Vistas::REC.ConsultarEmpleadorRecaudacion',0,'Q','','','');

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN","TablasTempOUT")
VALUES(6022,'BSCL.APV.BD.Vistas::REC.ConsultarClienteRecaudacion',0,'Q','','','');

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN","TablasTempOUT")
VALUES(6023,'BSCL.Portafolio.BD.Vistas::ConsultaContratoPortafolio',0,'Q','','','');

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN","TablasTempOUT")
VALUES(6024,'BSCL.APV.BD.Vistas::REC.ConsultarRecaudadoDistribuido',0,'Q','','','');

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN","TablasTempOUT")
VALUES(6026,'BSCL.APV.BD.Vistas::APV.ConsultarEstado',0,'Q','','','');

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN","TablasTempOUT")
VALUES(6027,'BSCL.APV.BD.Vistas::REC.ConsultarPersonaJuridica',0,'Q','','','');

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN","TablasTempOUT")
VALUES(6028,'PLUGIN_APV.jar@cl.bcs.APV.CrearRecaudacionEmpleador',0,'J','','','');

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN","TablasTempOUT")
VALUES(6029,'PLUGIN_APV.jar@cl.bcs.APV.CrearRecaudacionCliente',0,'J','','','');

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN","TablasTempOUT")
VALUES(6030,'PLUGIN_APV.jar@cl.bcs.APV.ConsultarTotalesDistribucionRecaudacion',0,'J','Trae Los Totales de la Distribucion en la racaudacion','','');

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN","TablasTempOUT")
VALUES(6031,'BSCL.Portafolio.BD.Vistas::ConsultarMatrizAgrupador',0,'Q','','','');

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN","TablasTempOUT")
VALUES(6032,'BSCL.Portafolio.BD.Vistas::ConsultarPortafolioMatrizDisponible',0,'Q','','','');

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN","TablasTempOUT")
VALUES(6033,'PLUGIN_APV.jar@cl.bcs.APV.ConciliarDeposito',0,'J','Cambia estado de depósito a  Conciliado','','');

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN","TablasTempOUT")
VALUES(6034,'BSCL.APV.BD.Vistas::REC.ConsultarEstadoTransicion',0,'Q','','','');

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN","TablasTempOUT")
VALUES(6035,'BSCL.APV.BD.Vistas::REC.ConsultarOrigen',0,'Q','','','');

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN","TablasTempOUT")
VALUES(6036,'PLUGIN_APV.jar@cl.bcs.APV.ConsultarTotalesOrigen',0,'J','Obtienes los totales de los depósitos según su origen','','');

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN","TablasTempOUT")
VALUES(6037,'PLUGIN_APV.jar@cl.bcs.APV.ProcesarDistribucion',0,'J','Vuelve a procesar recaudaciones en estado conciliado y rezagado.','','');

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN","TablasTempOUT")
VALUES(6038,'PLUGIN_APV.jar@cl.bcs.APV.AsignarDepositoEmpleador',0,'J','Asignar Depósito con Empleadores.','','');

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN","TablasTempOUT")
VALUES(6039,'PLUGIN_APV.jar@cl.bcs.APV.CargaMasivaPrevired',0,'J','Carga Masiva Previred','','');

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN","TablasTempOUT")
VALUES(6040,'BSCL.APV.BD.Vistas::ConsultarProductoAgrupador',0,'Q','','','');

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN","TablasTempOUT")
VALUES(6041,'BSCL.APV.BD.Vistas::ConsultarPlanTipoInversion',0,'Q','','','');

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN","TablasTempOUT")
VALUES(6042,'BSCL.APV.BD.Vistas::ConsultarContratoPortafolio',0,'Q','','','');

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN","TablasTempOUT")
VALUES(6043,'PLUGIN_APV.jar@cl.bcs.APV.CargaMasivaBanco',0,'J','Carga Masiva Banco','','');


INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN","TablasTempOUT")
VALUES(6044,'BSCL.APV.CrystalReports.Servicios::ContratoSAAPV',0,'P','CONSULTA REPORTE NOTIFICACION CLIENTE SAAPV','','');


INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN","TablasTempOUT")
VALUES(6045,'BSCL.CuentaInversion.BD.Servicios::ConsultarSaldoCuentaInversionCliente',0,'P','','','');

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN","TablasTempOUT")
VALUES(6046,'PLUGIN_APV.jar@cl.bcs.APV.CargaMasivaPAC',0,'J','Carga Masiva PAC','','');

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN","TablasTempOUT")
VALUES(6047,'BSCL.APV.BD.Vistas::APV.ConsultarTraspasosAPV',0,'Q','','','');

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" 
("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN","TablasTempOUT")
VALUES (6048,'BSCL.APV.BD.Vistas::AFI.ConsultarContratoSAAPV',0,'Q','Vista de Contratos SAAPV','','');

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" 
("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN","TablasTempOUT")
VALUES (6049,'BSCL.APV.BD.Vistas::APV.ConsultarIdContratoSAAPV',0,'Q','Busca id de Contrato SAAPV','','');

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN","TablasTempOUT","RegistraUso")
VALUES (6114,'BSCL.APV.BD.Vistas::APV.ConsultarIdContratoDC',0,'Q','Busca id de Contrato DC','','','S');

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" 
("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN","TablasTempOUT")
VALUES (6050,'BSCL.APV.BD.Vistas::APV.ConsultarDetalleTraspasosAPV',0,'Q','Consulta detalle de traspasos APV.','','');

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" 
("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN","TablasTempOUT")
VALUES (6051,'PLUGIN_APV.jar@cl.bcs.APV.GrabarTraspaso',0,'J','Ingresa Detalle de Traspaso','','');

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" 
("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN","TablasTempOUT")
VALUES (6052,'PLUGIN_APV.jar@cl.bcs.APV.GrabarRecaudacionTraspaso',0,'J','Genera Recaudacion Deposito y Cliente desde el Traspaso.','','');

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" 
("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN","TablasTempOUT")
VALUES (6053,'PLUGIN_APV.jar@cl.bcs.APV.AsignarContratoARecaudacion',0,'J','Asigna contrato a recaudacion.','','');

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN","TablasTempOUT")
VALUES(6054,'BSCL.Custodia.BD.Servicios::CUS.ConsultarCarteraAPV',0,'P','','','');

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN","TablasTempOUT")
VALUES (6055,'BSCL.APV.BD.Vistas::AFI.ConsultarTrabajadorSAAPV',0,'Q','Busca al trabajador','','');

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN","TablasTempOUT")
VALUES (6056,'BSCL.APV.BD.Vistas::AFI.ConsultarEmpleadorSAAPV',0,'Q','Busca al empleador','','');

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" 
("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN","TablasTempOUT")
VALUES (6057,'PLUGIN_APV.jar@cl.bcs.APV.ModificarContratoSAAPV',0,'J','Modifica Contratos SAAPV.','','');

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" 
("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN","TablasTempOUT")
VALUES (6058,'PLUGIN_APV.jar@cl.bcs.APV.CalculoMontosDesinversion',0,'J','Calcula los Montos y los devuelve para llenar la grilla.','','');

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN","TablasTempOUT")
VALUES (6059,'BSCL.Portafolio.BD.Vistas::ConsultarPortafolioMatrizAgrupadorDetalle',0,'Q','','','');

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN","TablasTempOUT")
VALUES (6060,'BSCL.APV.BD.Servicios::ConsultarCustodiaPortafolioAPV',0,'P','','','');

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN","TablasTempOUT")
VALUES (6061,'PLUGIN_APV.jar@cl.bcs.APV.DevolucionPagoRezagos',0,'J','','','');

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN","TablasTempOUT")
VALUES (6062,'BSCL.APV.BD.Vistas::APV.ConsultarRangoDiasTraspaso',0,'Q','','','');

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN","TablasTempOUT")
VALUES (6063,'BSCL.APV.BD.Vistas::AFI.ConsultarDiasDescuento',0,'Q','','','');

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN","TablasTempOUT")
VALUES (6066,'BSCL.APV.BD.Servicios::ConsultarGestionContratosSAAPV',0,'P','','','');
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion")
VALUES (6069,'BSCL.Plataforma.BD.Vistas::COM.ConsultarGrupoActivoProductos',0,'Q','Consulta de Grupo de Activos por Productos');

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio" ("IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion","TablasTempIN","TablasTempOUT")
VALUES (6071,'PLUGIN_APV.jar@cl.bcs.APV.CrearAnotacionRecaudacion',0,'J','Crea un registro de archivo de Recaudacion','','');
/*==============================================================*/
/* INSERT TABLA SucursalesBancarias:                             */
/*==============================================================*/

INSERT INTO "BSCL.Tesoreria.BD::TES.SucursalesBancarias"("IdSucursal","NombreSucursal", "IdBanco", "IdEstado")VALUES(1, 'Las Condes', 12, 'ACT');

INSERT INTO "BSCL.Tesoreria.BD::TES.SucursalesBancarias"("IdSucursal","NombreSucursal", "IdBanco", "IdEstado")VALUES(2, 'Santiago', 12, 'ACT');

INSERT INTO "BSCL.Tesoreria.BD::TES.SucursalesBancarias"("IdSucursal","NombreSucursal", "IdBanco", "IdEstado")VALUES(3, 'Huechuraba', 12, 'ACT');

INSERT INTO "BSCL.Tesoreria.BD::TES.SucursalesBancarias"("IdSucursal","NombreSucursal", "IdBanco", "IdEstado")VALUES(4, 'Las Condes', 17, 'ACT');

INSERT INTO "BSCL.Tesoreria.BD::TES.SucursalesBancarias"("IdSucursal","NombreSucursal", "IdBanco", "IdEstado")VALUES(5, 'Santiago', 17, 'ACT');

INSERT INTO "BSCL.Tesoreria.BD::TES.SucursalesBancarias"("IdSucursal","NombreSucursal", "IdBanco", "IdEstado")VALUES(6, 'Huechuraba', 17, 'ACT');

INSERT INTO "BSCL.Tesoreria.BD::TES.SucursalesBancarias"("IdSucursal","NombreSucursal", "IdEstado")VALUES(7, 'Banca en línea', 'ACT');

/*==============================================================*/
/* INSERT TABLA CodigoBancoPrevired:                             */
/*==============================================================*/

INSERT INTO "BSCL.APV.BD::REC.CodigoBancoPrevired"("IdBanco","CodigoBancoPrevired")VALUES(17, 'BancoChile');

/*==============================================================*/
/* INSERT TABLA EntidadRecaudadora:                             */
/*==============================================================*/

INSERT INTO "BSCL.APV.BD::SCS.EntidadRecaudadora"
("IdEntidadRecaudadora","IdPersona","Codigo","Nombre","Estado","IdTipoEntidadRecaudadora")
VALUES(1, 1765434, 1033, 'CAPITAL', 'VIGENTE', 1);

INSERT INTO "BSCL.APV.BD::SCS.EntidadRecaudadora"
("IdEntidadRecaudadora","IdPersona","Codigo","Nombre","Estado","IdTipoEntidadRecaudadora")
VALUES(2, 1765272, 1003, 'CUPRUM', 'VIGENTE', 1);

INSERT INTO "BSCL.APV.BD::SCS.EntidadRecaudadora" 
("IdEntidadRecaudadora","IdPersona","Codigo","Nombre","Estado","IdTipoEntidadRecaudadora")
VALUES(3, 1766439, 1005, 'HABITAT', 'VIGENTE', 1);

INSERT INTO "BSCL.APV.BD::SCS.EntidadRecaudadora" 
("IdEntidadRecaudadora","IdPersona","Codigo","Nombre","Estado","IdTipoEntidadRecaudadora")
VALUES(4, 1774178, 1032, 'MODELO', 'VIGENTE', 1);

INSERT INTO "BSCL.APV.BD::SCS.EntidadRecaudadora" 
("IdEntidadRecaudadora","IdPersona","Codigo","Nombre","Estado","IdTipoEntidadRecaudadora")
VALUES(5, 1765331, 1008, 'PLANVITAL', 'VIGENTE', 1);

INSERT INTO "BSCL.APV.BD::SCS.EntidadRecaudadora" 
("IdEntidadRecaudadora","IdPersona","Codigo","Nombre","Estado","IdTipoEntidadRecaudadora")
VALUES(6, 1765550, 1034, 'PROVIDA', 'VIGENTE', 1);

/*==============================================================*/
/* INSERT TABLA TipoEntidadRecaudadora:                         */
/*==============================================================*/

INSERT INTO "BSCL.APV.BD::SCS.TipoEntidadRecaudadora"
("IdTipoEntidadRecaudadora","Nombre","Estado")
VALUES(1, 'AFP', 'VIGENTE');

INSERT INTO "BSCL.APV.BD::SCS.TipoEntidadRecaudadora"
("IdTipoEntidadRecaudadora","Nombre","Estado")
VALUES(2, 'CORREDORA', 'VIGENTE');

INSERT INTO "BSCL.APV.BD::SCS.TipoEntidadRecaudadora"
("IdTipoEntidadRecaudadora","Nombre","Estado")
VALUES(3, 'ASEGURADORA', 'VIGENTE');

INSERT INTO "BSCL.APV.BD::SCS.TipoEntidadRecaudadora"
("IdTipoEntidadRecaudadora","Nombre","Estado")
VALUES(4, 'AGENTE DE VALORES', 'VIGENTE');

INSERT INTO "BSCL.APV.BD::SCS.TipoEntidadRecaudadora"
("IdTipoEntidadRecaudadora","Nombre","Estado")
VALUES(5, 'ADMINISTRADORA DE FONDOS', 'VIGENTE');

INSERT INTO "BSCL.APV.BD::SCS.TipoEntidadRecaudadora"
("IdTipoEntidadRecaudadora","Nombre","Estado")
VALUES(6, 'ADMINISTRADORA DE VIVIENDA Y FFMM', 'VIGENTE');

/*==============================================================*/
/* INSERT TABLA Estado:                       			        */
/*==============================================================*/

INSERT INTO "BSCL.APV.BD::APV.Estado" ( "IdEstado","Codigo","Nombre","NombreEntidad" )  VALUES(1,'COM', 'COMPLETADO','CONTRATO');
INSERT INTO "BSCL.APV.BD::APV.Estado" ( "IdEstado","Codigo","Nombre","NombreEntidad" )  VALUES(2,'CER', 'CERRADO','CONTRATO');
INSERT INTO "BSCL.APV.BD::APV.Estado" ( "IdEstado","Codigo","Nombre","NombreEntidad" )  VALUES(3,'PDI', 'PENDIENTE DE INGRESO','CONTRATO');
INSERT INTO "BSCL.APV.BD::APV.Estado" ( "IdEstado","Codigo","Nombre","NombreEntidad" )  VALUES(4,'REV', 'REVOCADO','CONTRATO');
INSERT INTO "BSCL.APV.BD::APV.Estado" ( "IdEstado","Codigo","Nombre","NombreEntidad" )  VALUES(5,'VIG', 'VIGENTE','CONTRATO');
INSERT INTO "BSCL.APV.BD::APV.Estado" ( "IdEstado","Codigo","Nombre","NombreEntidad" )  VALUES(6,'RZD', 'RECHAZADO','CONTRATO');
INSERT INTO "BSCL.APV.BD::APV.Estado" ( "IdEstado","Codigo","Nombre","NombreEntidad" )  VALUES(7,'PDS', 'PENDIENTE DE SOLICITUD','CONTRATO');
INSERT INTO "BSCL.APV.BD::APV.Estado" ( "IdEstado","Codigo","Nombre","NombreEntidad" )  VALUES(8,'ENV', 'ENVIADO','CONTRATO');
INSERT INTO "BSCL.APV.BD::APV.Estado" ( "IdEstado","Codigo","Nombre","NombreEntidad" )  VALUES(9,'NEV', 'NO ENVIADO','CONTRATO');

INSERT INTO "BSCL.APV.BD::APV.Estado" ( "IdEstado","Codigo","Nombre","NombreEntidad" )  VALUES(10,'ING', 'INGRESADO','RECAUDACION');
INSERT INTO "BSCL.APV.BD::APV.Estado" ( "IdEstado","Codigo","Nombre","NombreEntidad" )  VALUES(11,'CON', 'CONCILIADO','RECAUDACION');
INSERT INTO "BSCL.APV.BD::APV.Estado" ( "IdEstado","Codigo","Nombre","NombreEntidad" )  VALUES(12,'NCO', 'NO CONCILIADO','RECAUDACION');
INSERT INTO "BSCL.APV.BD::APV.Estado" ( "IdEstado","Codigo","Nombre","NombreEntidad" )  VALUES(13,'DIS', 'DISTRIBUIDO','RECAUDACION');
INSERT INTO "BSCL.APV.BD::APV.Estado" ( "IdEstado","Codigo","Nombre","NombreEntidad" )  VALUES(14,'REZ', 'REZAGO','RECAUDACION');
INSERT INTO "BSCL.APV.BD::APV.Estado" ( "IdEstado","Codigo","Nombre","NombreEntidad" )  VALUES(15,'TRA', 'TRASPASADO','RECAUDACION');
INSERT INTO "BSCL.APV.BD::APV.Estado" ( "IdEstado","Codigo","Nombre","NombreEntidad" )  VALUES(16,'DEV', 'DEVUELTO','RECAUDACION');

INSERT INTO "BSCL.APV.BD::APV.Estado" ( "IdEstado","Codigo","Nombre","NombreEntidad" )  VALUES(17,'CDC', 'CON DETALLE CLIENTE','EMPLEADOR');
INSERT INTO "BSCL.APV.BD::APV.Estado" ( "IdEstado","Codigo","Nombre","NombreEntidad" )  VALUES(18,'SDC', 'SIN DETALLE CLIENTE','EMPLEADOR');

INSERT INTO "BSCL.APV.BD::APV.Estado" ( "IdEstado","Codigo","Nombre","NombreEntidad" )  VALUES(19,'PDA', 'PENDIENTE DE APROBACION','CONTRATO');
INSERT INTO "BSCL.APV.BD::APV.Estado" ( "IdEstado","Codigo","Nombre","NombreEntidad" )  VALUES(20,'APR', 'APROBADO','CONTRATO');
INSERT INTO "BSCL.APV.BD::APV.Estado" ( "IdEstado","Codigo","Nombre","NombreEntidad" )  VALUES(21,'ANU', 'ANULADO','CONTRATO');

INSERT INTO "BSCL.APV.BD::APV.Parametro" ("IdEmpresa","IdParametro","TipoParametro","NombreParametro","Descripcion","ValorParametro")
VALUES(1,1,'CONTRATOSAAPV','AUTORIZACION_GESTION_CONTRATO','Envia autorizar Blotter Gestion Contratos SAAPV','N'); --Envia autorizar Blotter Gestion Contratos SAAPV
INSERT INTO "BSCL.APV.BD::APV.Parametro" ("IdEmpresa","IdParametro","TipoParametro","NombreParametro","Descripcion","ValorParametro")
VALUES(1,2,'VALIDACIONSAAPV','EMPLEADOR','Valida que el Contrato SAAPV y la recaudación tengan el mismo Empleador','S'); --Valida que el Contrato SAAPV y la recaudación tengan el mismo Empleador
INSERT INTO "BSCL.APV.BD::APV.Parametro" ("IdEmpresa","IdParametro","TipoParametro","NombreParametro","Descripcion","ValorParametro")
VALUES(1,3,'VALIDACIONSAAPV','VIGENCIA_CONTRATO','Valida que el Contrato SAAPV se encuentre vigente','S'); --Valida que el Contrato SAAPV se encuentre vigente
INSERT INTO "BSCL.APV.BD::APV.Parametro" ("IdEmpresa","IdParametro","TipoParametro","NombreParametro","Descripcion","ValorParametro")
VALUES(1,4,'VALIDACIONSAAPV','FORMA_PAGO','Valida que el Contrato SAAPV y la recaudación tengan la misma forma de pago','S'); --Valida que el Contrato SAAPV y la recaudación tengan la misma forma de pago
INSERT INTO "BSCL.APV.BD::APV.Parametro" ("IdEmpresa","IdParametro","TipoParametro","NombreParametro","Descripcion","ValorParametro")
VALUES(1,5,'VALIDACIONSAAPV','MONTO','Valida que el Contrato SAAPV y la recaudación tengan el mismo monto','S'); --Valida que el Contrato SAAPV y la recaudación tengan el mismo monto
INSERT INTO "BSCL.APV.BD::APV.Parametro" ("IdEmpresa","IdParametro","TipoParametro","NombreParametro","Descripcion","ValorParametro")
VALUES(1,6,'VALIDACIONSAAPV','REGIMEN','Valida que el Contrato SAAPV y la recaudación tengan el mismo régimen','S'); --Valida que el Contrato SAAPV y la recaudación tengan el mismo régimen
INSERT INTO  BSCL."BSCL.APV.BD::APV.Parametro" ("IdEmpresa","IdParametro","TipoParametro","NombreParametro","Descripcion","ValorParametro")
VALUES(1,7,'CONTRATOSAAPV','DIA_DESCUENTO','Corresponde al dia de Descuento de un PAC',5);
INSERT INTO  BSCL."BSCL.APV.BD::APV.Parametro" ("IdEmpresa","IdParametro","TipoParametro","NombreParametro","Descripcion","ValorParametro")
VALUES(1,8,'CONTRATOSAAPV','DIA_DESCUENTO','Corresponde al dia de Descuento de un PAC',25);

INSERT INTO bscl."BSCL.Plataforma.BD::PLA.Configuracion" ( "IdEmpresa","CodigoParametro","Descripcion","Valor" )  
VALUES(1,'APV.AutorizaGestionContrato','Envia autorizar Blotter Gestion Contratos SAAPV','N'); --COMPRA,MERCADO,CORREDORA

INSERT INTO bscl."BSCL.Plataforma.BD::PLA.Configuracion" ( "IdEmpresa","CodigoParametro","Descripcion","Valor" )  
VALUES(1,'APV.Habilitado','Indica si tiene habilitado producto APV','S'); 

INSERT INTO bscl."BSCL.Plataforma.BD::PLA.Configuracion" ( "IdEmpresa","CodigoParametro","Descripcion","Valor" )  
VALUES(1,'APV.ContratoCLI_IdTipoClienteDefecto','Valor de TipoCliente que se asigna por defecto al crear un cliente','FAMILYOFFICE'); --COMPRA,MERCADO,CORREDORA
/*
INSERT INTO bscl."BSCL.Plataforma.BD::PLA.Configuracion" ( "IdEmpresa","CodigoParametro","Descripcion","Valor" )  
VALUES(1,'APV.ContratoCLI_IdEmpleadoEjecutivoDefecto','Valor de IdEmpleadoEjecutivo que se asigna por defecto al crear un cliente','FAMILYOFFICE'); --COMPRA,MERCADO,CORREDORA
*/
INSERT INTO bscl."BSCL.Plataforma.BD::PLA.Configuracion" ( "IdEmpresa","CodigoParametro","Descripcion","Valor" )  
VALUES(1,'APV.ContratoFechaFinRelacionDefecto','Valor de FechaFinRelacion que se asigna por defecto al crear un cliente','2199-01-01'); --COMPRA,MERCADO,CORREDORA


INSERT INTO "BSCL.Plataforma.BD::AUT.TipoEvento"("IdTipoEvento", "IdEmpresa", "IdModulo", "Codigo", "Nombre", "HorasVigencia", "EtapasSecuenciales", "IdTipoProducto", "IdTipoOperacion", "IdUsuarioSuperAutorizador", "IdRolSuperAutorizador", "IdEstado", "SPConfirma", "SPRechaza", "SPFormato", "SPCampoAutorizacion", "SPCampoNegocio")
VALUES('VIGENT', 1, 'APV', 'AUT_CONTRATO_VIGENTE', 'CONTRATO SAAPV VIGENTE', 24.00, 'S', '', '', 0, 0, 'VIG', 'BSCL.APV.BD.Servicios::ConfirmarAutorizacionIngresoContratoSAAPV', 'BSCL.APV.BD.Servicios::RechazaAutorizacionIngresoContratoSAAPV', 4, 'NumeroOperacionConcepto', 'FolioContratoSAAPV');

INSERT INTO "BSCL.Plataforma.BD::AUT.TipoEvento"("IdTipoEvento", "IdEmpresa", "IdModulo", "Codigo", "Nombre", "HorasVigencia", "EtapasSecuenciales", "IdTipoProducto", "IdTipoOperacion", "IdUsuarioSuperAutorizador", "IdRolSuperAutorizador", "IdEstado", "SPConfirma", "SPRechaza", "SPFormato", "SPCampoAutorizacion", "SPCampoNegocio")
VALUES('COMPLE', 1, 'APV', 'AUT_COMPLETA_CONTRATO', 'COMPLETA CONTRATO SAAPV', 24.00, 'S', '', '', 0, 0, 'VIG', 'BSCL.APV.BD.Servicios::ConfirmarAutorizacionCompletarContratoSAAPV', 'BSCL.APV.BD.Servicios::RechazaAutorizacionIngresoContratoSAAPV', 4, 'NumeroOperacionConcepto', 'FolioContratoSAAPV');

INSERT INTO "BSCL.Plataforma.BD::AUT.TipoEvento"("IdTipoEvento", "IdEmpresa", "IdModulo", "Codigo", "Nombre", "HorasVigencia", "EtapasSecuenciales", "IdTipoProducto", "IdTipoOperacion", "IdUsuarioSuperAutorizador", "IdRolSuperAutorizador", "IdEstado", "SPConfirma", "SPRechaza", "SPFormato", "SPCampoAutorizacion", "SPCampoNegocio")
VALUES('REVOCA', 1, 'APV', 'AUT_REVOCACION_CONTRATO', 'REVOCACION CONTRATO SAAPV', 24.00, 'S', '', '', 0, 0, 'VIG', 'BSCL.APV.BD.Servicios::ConfirmarAutorizacionRevocarContratoSAAPV', 'BSCL.APV.BD.Servicios::RechazaAutorizacionIngresoContratoSAAPV', 4, 'NumeroOperacionConcepto', 'FolioContratoSAAPV');

INSERT INTO "BSCL.Plataforma.BD::AUT.TipoEvento"("IdTipoEvento", "IdEmpresa", "IdModulo", "Codigo", "Nombre", "HorasVigencia", "EtapasSecuenciales", "IdTipoProducto", "IdTipoOperacion", "IdUsuarioSuperAutorizador", "IdRolSuperAutorizador", "IdEstado", "SPConfirma", "SPRechaza", "SPFormato", "SPCampoAutorizacion", "SPCampoNegocio")
VALUES('CERRAR', 1, 'APV', 'AUT_CIERRE_CONTRATO', 'CIERRE CONTRATO SAAPV', 24.00, 'S', '', '', 0, 0, 'VIG', 'BSCL.APV.BD.Servicios::ConfirmarAutorizacionCerrarContratoSAAPV', 'BSCL.APV.BD.Servicios::RechazaAutorizacionIngresoContratoSAAPV', 4, 'NumeroOperacionConcepto', 'FolioContratoSAAPV');


INSERT INTO "BSCL.Plataforma.BD::AUT.Etapa" ("IdEtapa","IdTipoEvento","Codigo","Nombre","Orden","TipoAutorizacion","MinimoAutorizan","IdEstado","IdEmpresa")
VALUES(65900,'REVOCA','ETAPA1','ETAPA1',1,'T',0,'VIG',1);

INSERT INTO "BSCL.Plataforma.BD::AUT.Etapa" ("IdEtapa","IdTipoEvento","Codigo","Nombre","Orden","TipoAutorizacion","MinimoAutorizan","IdEstado","IdEmpresa")
VALUES(65901,'CERRAR','ETAPA1','ETAPA1',1,'T',0,'VIG',1);

INSERT INTO "BSCL.Plataforma.BD::AUT.Etapa" ("IdEtapa","IdTipoEvento","Codigo","Nombre","Orden","TipoAutorizacion","MinimoAutorizan","IdEstado","IdEmpresa")
VALUES(65902,'COMPLE','ETAPA1','ETAPA1',1,'T',0,'VIG',1); 

INSERT INTO "BSCL.Plataforma.BD::AUT.Etapa" ("IdEtapa","IdTipoEvento","Codigo","Nombre","Orden","TipoAutorizacion","MinimoAutorizan","IdEstado","IdEmpresa")
VALUES(65903,'VIGENT','ETAPA1','ETAPA1',1,'T',0,'VIG',1); 

INSERT INTO "BSCL.APV.BD::APV.TipoCarga"(
 "IdEmpresa"				--BIGINT	NOT NULL,
,"IdTipoCarga"		    --BIGINT	NOT NULL,
,"NombreTipoCarga"		--NVARCHAR(100) NOT NULL,
,"CodigoTipoCarga"		--NVARCHAR(3) NOT NULL,
,"IdEstado"				--NVARCHAR(3) NOT NULL,
)VALUES(
 1
,1
,'CARGA CONTRATOS SAAPV'
,'CCS'
,'VIG'
);

INSERT INTO "BSCL.APV.BD::APV.TipoCarga"(
"IdEmpresa"				--BIGINT	NOT NULL,
,"IdTipoCarga"		    --BIGINT	NOT NULL,
,"NombreTipoCarga"		--NVARCHAR(100) NOT NULL,
,"CodigoTipoCarga"		--NVARCHAR(3) NOT NULL,
,"IdEstado"				--NVARCHAR(3) NOT NULL,
)VALUES(
1
,2
,'CARGA RECAUDACION'
,'CRE'
,'VIG'
);

INSERT INTO "BSCL.APV.BD::APV.TipoCarga"(
"IdEmpresa"				--BIGINT	NOT NULL,
,"IdTipoCarga"		    --BIGINT	NOT NULL,
,"NombreTipoCarga"		--NVARCHAR(100) NOT NULL,
,"CodigoTipoCarga"		--NVARCHAR(3) NOT NULL,
,"IdEstado"				--NVARCHAR(3) NOT NULL,
)VALUES(
1
,3
,'CARGA CARTOLA'
,'CAR'
,'VIG'
);

INSERT INTO "BSCL.APV.BD::APV.TipoCarga"(
"IdEmpresa"				--BIGINT	NOT NULL,
,"IdTipoCarga"		    --BIGINT	NOT NULL,
,"NombreTipoCarga"		--NVARCHAR(100) NOT NULL,
,"CodigoTipoCarga"		--NVARCHAR(3) NOT NULL,
,"IdEstado"				--NVARCHAR(3) NOT NULL,
)VALUES(
1
,4
,'CARGA DEPOSITOS'
,'DEP'
,'VIG'
);


/*==================================================*/
/*      REGIMENES TRIBUTARIOS   					*/
/*==================================================*/

INSERT INTO "BSCL.APV.BD::REC.RegimenTributario" ("IdRegimenTributario","Codigo","IdEstado")
VALUES(1,'APV-AN','ANU');

INSERT INTO "BSCL.APV.BD::REC.RegimenTributario" ("IdRegimenTributario","Codigo","IdEstado")
VALUES(2,'APV-A','VIG');

INSERT INTO "BSCL.APV.BD::REC.RegimenTributario" ("IdRegimenTributario","Codigo","IdEstado")
VALUES(3,'APV-B','VIG');

INSERT INTO "BSCL.APV.BD::REC.RegimenTributario" ("IdRegimenTributario","Codigo","IdEstado")
VALUES(4,'DC-AL','ANU');

INSERT INTO "BSCL.APV.BD::REC.RegimenTributario" ("IdRegimenTributario","Codigo","IdEstado")
VALUES(5,'DC-DL','ANU');

INSERT INTO "BSCL.APV.BD::REC.RegimenTributario" ("IdRegimenTributario","Codigo","IdEstado")
VALUES(6,'DC-R','VIG');

INSERT INTO "BSCL.APV.BD::REC.RegimenTributario" ("IdRegimenTributario","Codigo","IdEstado")
VALUES(7,'BF','ANU');


/*==================================================================*/
/*      CONFIGURACION  CARGA PAC				  					*/
/*==================================================================*/

--Registro de archivos
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Archivo"
("IdArchivo", "IdEmpresa","CodigoArchivo", "TieneSeparador", "SeparadorColumnas","TieneCabecera","IdModulo")
VALUES(49,1,'CARGA_PAC','N','','N','APV');

--Formatos de archivo PAC

INSERT INTO "BSCL.Plataforma.BD::PLA.Formato" 
("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales")
VALUES(815,'CodigoBanco','STRING',1,3,'S','','','N',0);

INSERT INTO "BSCL.Plataforma.BD::PLA.Formato" 
("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales")
VALUES(816,'CodigoEmpresa','STRING',4,3,'S','','','N',0);

INSERT INTO "BSCL.Plataforma.BD::PLA.Formato" 
("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales")
VALUES(817,'CodigoConvenio','STRING',7,3,'S','','','N',0);

INSERT INTO "BSCL.Plataforma.BD::PLA.Formato" 
("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales")
VALUES(818,'TipoRegistro','STRING',10,1,'S','','','N',0);

INSERT INTO "BSCL.Plataforma.BD::PLA.Formato" 
("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales")
VALUES(819,'IdServicioEmpresa','STRING',11,22,'S','','','N',0);

INSERT INTO "BSCL.Plataforma.BD::PLA.Formato" 
("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales")
VALUES(820,'InformacionAdicional','STRING',34,10,'N','','','N',0);

INSERT INTO "BSCL.Plataforma.BD::PLA.Formato" 
("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales")
VALUES(821,'MontoCargo','NUMBER',44,11,'S','','','N',0);

INSERT INTO "BSCL.Plataforma.BD::PLA.Formato" 
("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales")
VALUES(822,'FechaFacturacion','STRING',55,8,'S','','','N',0);

INSERT INTO "BSCL.Plataforma.BD::PLA.Formato" 
("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales")
VALUES(823,'FechaVencimiento','STRING',63,8,'S','','','N',0);

--Relaciones archivo con separacion y sus formatos Carga PAC.

INSERT INTO "BSCL.Plataforma.BD::PLA.ArchivoFormato"
("IdArchivoFormato","IdArchivo","IdFormato")
VALUES(978,49,815);

INSERT INTO "BSCL.Plataforma.BD::PLA.ArchivoFormato"
("IdArchivoFormato","IdArchivo","IdFormato")
VALUES(979,49,816);

INSERT INTO "BSCL.Plataforma.BD::PLA.ArchivoFormato"
("IdArchivoFormato","IdArchivo","IdFormato")
VALUES(980,49,817);

INSERT INTO "BSCL.Plataforma.BD::PLA.ArchivoFormato"
("IdArchivoFormato","IdArchivo","IdFormato")
VALUES(981,49,818);

INSERT INTO "BSCL.Plataforma.BD::PLA.ArchivoFormato"
("IdArchivoFormato","IdArchivo","IdFormato")
VALUES(982,49,819);

INSERT INTO "BSCL.Plataforma.BD::PLA.ArchivoFormato"
("IdArchivoFormato","IdArchivo","IdFormato")
VALUES(983,49,820);

INSERT INTO "BSCL.Plataforma.BD::PLA.ArchivoFormato"
("IdArchivoFormato","IdArchivo","IdFormato")
VALUES(984,49,821);

INSERT INTO "BSCL.Plataforma.BD::PLA.ArchivoFormato"
("IdArchivoFormato","IdArchivo","IdFormato")
VALUES(985,49,822);

INSERT INTO "BSCL.Plataforma.BD::PLA.ArchivoFormato"
("IdArchivoFormato","IdArchivo","IdFormato")
VALUES(986,49,823);

/*==================================================================*/
/*      CONFIGURACION  CARGA TOTAL PAC				  					*/
/*==================================================================*/

--Registro de archivos
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Archivo"
("IdArchivo", "IdEmpresa","CodigoArchivo", "TieneSeparador", "SeparadorColumnas","TieneCabecera","IdModulo")
VALUES(53,1,'CARGA_PAC_TOTAL','N','','N','APV');

--Formatos de archivo PAC

INSERT INTO "BSCL.Plataforma.BD::PLA.Formato" 
("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales")
VALUES(854,'CodigoBanco','STRING',1,3,'S','','','N',0);

INSERT INTO "BSCL.Plataforma.BD::PLA.Formato" 
("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales")
VALUES(855,'CodigoEmpresa','STRING',4,3,'S','','','N',0);

INSERT INTO "BSCL.Plataforma.BD::PLA.Formato" 
("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales")
VALUES(856,'CodigoConvenio','STRING',7,3,'S','','','N',0);

INSERT INTO "BSCL.Plataforma.BD::PLA.Formato" 
("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales")
VALUES(857,'TipoRegistro','STRING',10,1,'S','','','N',0);

INSERT INTO "BSCL.Plataforma.BD::PLA.Formato" 
("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales")
VALUES(858,'MontoTotalCargo','NUMBER',44,11,'S','','','N',0);

INSERT INTO "BSCL.Plataforma.BD::PLA.Formato" 
("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales")
VALUES(859,'NumeroDeRegistros','STRING',55,6,'S','','','N',0);

--Relaciones archivo con separacion y sus formatos Carga PAC.

INSERT INTO "BSCL.Plataforma.BD::PLA.ArchivoFormato"
("IdArchivoFormato","IdArchivo","IdFormato")
VALUES(1017,53,854);

INSERT INTO "BSCL.Plataforma.BD::PLA.ArchivoFormato"
("IdArchivoFormato","IdArchivo","IdFormato")
VALUES(1018,53,855);

INSERT INTO "BSCL.Plataforma.BD::PLA.ArchivoFormato"
("IdArchivoFormato","IdArchivo","IdFormato")
VALUES(1019,53,856);

INSERT INTO "BSCL.Plataforma.BD::PLA.ArchivoFormato"
("IdArchivoFormato","IdArchivo","IdFormato")
VALUES(1020,53,857);

INSERT INTO "BSCL.Plataforma.BD::PLA.ArchivoFormato"
("IdArchivoFormato","IdArchivo","IdFormato")
VALUES(1021,53,858);

INSERT INTO "BSCL.Plataforma.BD::PLA.ArchivoFormato"
("IdArchivoFormato","IdArchivo","IdFormato")
VALUES(1022,53,859);

/*==================================================================*/
/*      CONFIGURACION  CARGA CONTRATOS SAAPV    					*/
/*==================================================================*/

--Registro de archivos
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Archivo"("IdArchivo", "IdEmpresa","CodigoArchivo", "TieneSeparador", "SeparadorColumnas","TieneCabecera","IdModulo")
VALUES(40,1,'CARGA_CONTRATOS_SAAPV','S',';','N','APV');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Archivo"("IdArchivo", "IdEmpresa","CodigoArchivo", "TieneSeparador", "SeparadorColumnas","TieneCabecera","IdModulo")
VALUES(43,1,'CARGA_CARTOLAS_APV','N','','N','APV');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Archivo"("IdArchivo", "IdEmpresa","CodigoArchivo", "TieneSeparador", "SeparadorColumnas","TieneCabecera","IdModulo")
VALUES(44,1,'CARGA_RECAUDACION_APV','S',';','N','APV');


--Formatos de archivo Contrato SAAPV
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato"("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales")
VALUES(900,'FolioContratoSAAPV','STRING',0,0,'N','','','N',0);
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato"("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales")
VALUES(901,'NombrePlan','STRING',0,0,'N','','','N',0);
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato"("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales")
VALUES(902,'FechaSuscripcion','STRING',0,0,'N','','','N',0);
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato"("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales")
VALUES(903,'FechaCreacion','STRING',0,0,'N','','','N',0);
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato"("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales")
VALUES(904,'FechaAprobacion','STRING',0,0,'N','','','N',0);
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato"("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales")
VALUES(905,'FechaNotificacion','STRING',0,0,'N','','','N',0);
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato"("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales")
VALUES(906,'FechaRecepcion','STRING',0,0,'N','','','N',0);
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato"("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales")
VALUES(907,'Folio','STRING',0,0,'N','','','N',0);
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato"("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales")
VALUES(909,'IdCategoriaInversionista','STRING',0,0,'N','','','N',0);
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato"("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales")
VALUES(910,'IdPerfilComercial','STRING',0,0,'N','','','N',0);
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato"("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales")
VALUES(911,'NombreCliente','STRING',0,0,'N','','','N',0);
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato"("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales")
VALUES(912,'ApellidoPaternoCliente','STRING',0,0,'N','','','N',0);
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato"("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales")
VALUES(913,'ApellidoMaternoCliente','STRING',0,0,'N','','','N',0);
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato"("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales")
VALUES(914,'DocumentoIdentidadCliente','STRING',0,0,'N','','','N',0);
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato"("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales")
VALUES(915,'FechaNacimientoCliente','STRING',0,0,'N','','','N',0);
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato"("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales")
VALUES(916,'EstadoCivil','STRING',0,0,'N','','','N',0);
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato"("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales")
VALUES(917,'IdTipoGenero','STRING',0,0,'N','','','N',0);
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato"("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales")
VALUES(918,'IdPaisResidenciaCliente','NUMBER',0,0,'N','','','N',0);
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato"("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales")
VALUES(919,'IdCiudadCliente','NUMBER',0,0,'N','','','N',0);
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato"("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales")
VALUES(920,'DireccionCliente','STRING',0,0,'N','','','N',0);
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato"("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales")
VALUES(921,'NumeroDireccionCliente','STRING',0,0,'N','','','N',0);
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato"("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales")
VALUES(922,'IdTipoCargoPersona','NUMBER',0,0,'N','','','N',0);
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato"("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales")
VALUES(923,'TelefonoCliente','STRING',0,0,'N','','','N',0);
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato"("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales")
VALUES(924,'TelefonoComercialCliente','STRING',0,0,'N','','','N',0);
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato"("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales")
VALUES(925,'TelefonoCelularCliente','STRING',0,0,'N','','','N',0);
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato"("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales")
VALUES(926,'EmailCliente','STRING',0,0,'N','','','N',0);
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato"("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales")
VALUES(927,'RazonSocialEmpleador','STRING',0,0,'N','','','N',0);
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato"("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales")
VALUES(928,'DocumentoIdentidadEmpleador','STRING',0,0,'N','','','N',0);
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato"("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales")
VALUES(929,'DireccionComercialEmpleador','STRING',0,0,'N','','','N',0);
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato"("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales")
VALUES(930,'IdComunaEmpleador','NUMBER',0,0,'N','','','N',0);
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato"("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales")
VALUES(931,'TelefonoEmpleador','STRING',0,0,'N','','','N',0);
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato"("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales")
VALUES(932,'NombreEncargado','STRING',0,0,'N','','','N',0);
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato"("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales")
VALUES(933,'EmailEncargado','STRING',0,0,'N','','','N',0);
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato"("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales")
VALUES(934,'TelefonoEncargado','STRING',0,0,'N','','','N',0);
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato"("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales")
VALUES(935,'IdMedioRecaudacionTrabajador','NUMBER',0,0,'N','','','N',0);
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato"("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales")
VALUES(936,'IdEntidadRecaudadora','NUMBER',0,0,'N','','','N',0);
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato"("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales")
VALUES(937,'CodigoPrevision','NUMBER',0,0,'N','','','N',0);
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato"("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales")
VALUES(938,'Pensionado','STRING',0,0,'N','','','N',0);
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato"("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales")
VALUES(939,'Trabajador','STRING',0,0,'N','','','N',0);
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato"("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales")
VALUES(940,'CodigoAgrupador','STRING',0,0,'N','','','N',0);
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato"("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales")
VALUES(941,'IdMatrizAgrupador','NUMBER',0,0,'N','','','N',0);
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato"("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales")
VALUES(942,'OrigenAhorro','STRING',0,0,'N','','','N',0);
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato"("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales")
VALUES(943,'IdPortafolio','NUMBER',0,0,'N','','','N',0);
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato"("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales")
VALUES(944,'IdPropositoPortafolio','STRING',0,0,'N','','','N',0);
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato"("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales")
VALUES(945,'MesInicio','STRING',0,0,'N','','','N',0);
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato"("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales")
VALUES(946,'MesTermino','STRING',0,0,'N','','','N',0);
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato"("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales")
VALUES(947,'MontoPesos','DOUBLE',0,0,'N','','','N',8);
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato"("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales")
VALUES(948,'MontoUF','DOUBLE',0,0,'N','','','N',8);
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato"("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales")
VALUES(949,'PorcentajeRegimenImponible','DOUBLE',0,0,'N','','','N',8);
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato"("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales")
VALUES(950,'Pago','STRING',0,0,'N','','','N',0);
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato"("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales")
VALUES(951,'PagoNombre','STRING',0,0,'N','','','N',0);
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato"("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales")
VALUES(952,'Producto','STRING',0,0,'N','','','N',0);
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato"("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales")
VALUES(953,'Regimen','STRING',0,0,'N','','','N',0);
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato"("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales")
VALUES(954,'CodigoTipoSolicitud','STRING',0,0,'N','','','N',0);
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato"("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales")
VALUES(955,'FormaDePago','STRING',0,0,'N','','','N',0);
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato"("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales")
VALUES(956,'IdMedioRecaudacionFormaPago','NUMBER',0,0,'N','','','N',0);
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato"("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales")
VALUES(957,'DiaDescuento ','NUMBER',0,0,'N','','','N',0);
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato"("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales")
VALUES(958,'Institucion ','STRING',0,0,'N','','','N',0);
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato"("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales")
VALUES(959,'ComisionTransferencia ','NUMBER',0,0,'N','','','S',8);
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato"("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales")
VALUES(960,'NombreEntidadOrigen ','STRING',0,0,'N','','','N',0);
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato"("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales")
VALUES(961,'RutEntidadOrigen','STRING',0,0,'N','','','N',0);
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato"("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales")
VALUES(962,'TipoDeAhorro ','STRING',0,0,'N','','','N',0);
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato"("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales")
VALUES(963,'FondoDeOrigen ','STRING',0,0,'N','','','N',0);
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato"("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales")
VALUES(964,'TipoTraspaso','STRING',0,0,'N','','','N',0);
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato"("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales")
VALUES(965,'MontoTraspaso ','DOUBLE',0,0,'N','','','S',8);
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato"("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales")
VALUES(966,'UFTraspaso','DOUBLE',0,0,'N','','','S',8);
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato"("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales")
VALUES(967,'PorcentajeTraspaso','DOUBLE',0,0,'N','','','S',8);
/*Distribucion Inversion N°1*/
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato"("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales")
VALUES(968,'D1_Instrumento','STRING',0,0,'N','','','N',0);
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato"("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales")
VALUES(969,'D1_Serie','STRING',0,0,'N','','','N',0);
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato"("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales")
VALUES(970,'D1_MontoPesos','DOUBLE',0,0,'N','','','S',8);
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato"("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales")
VALUES(971,'D1_Porcentaje','DOUBLE',0,0,'N','','','S',8);
/*Distribucion Inversion N°2*/
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato"("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales")
VALUES(972,'D2_Instrumento','STRING',0,0,'N','','','N',0);
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato"("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales")
VALUES(973,'D2_Serie','STRING',0,0,'N','','','N',0);
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato"("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales")
VALUES(974,'D2_MontoPesos','DOUBLE',0,0,'N','','','S',8);
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato"("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales")
VALUES(975,'D2_Porcentaje','DOUBLE',0,0,'N','','','S',8);
/*Distribucion Inversion N°3*/
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato"("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales")
VALUES(976,'D3_Instrumento','STRING',0,0,'N','','','N',0);
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato"("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales")
VALUES(977,'D3_Serie','STRING',0,0,'N','','','N',0);
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato"("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales")
VALUES(978,'D3_MontoPesos','DOUBLE',0,0,'N','','','S',8);
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato"("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales")
VALUES(979,'D3_Porcentaje','DOUBLE',0,0,'N','','','S',8);
/*Distribucion Inversion N°4*/
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato"("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales")
VALUES(980,'D4_Instrumento','STRING',0,0,'N','','','N',0);
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato"("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales")
VALUES(981,'D4_Serie','STRING',0,0,'N','','','N',0);
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato"("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales")
VALUES(982,'D4_MontoPesos','DOUBLE',0,0,'N','','','S',8);
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato"("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales")
VALUES(983,'D4_Porcentaje','DOUBLE',0,0,'N','','','S',8);
/*Distribucion Inversion N°5*/
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato"("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales")
VALUES(984,'D5_Instrumento','STRING',0,0,'N','','','N',0);
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato"("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales")
VALUES(985,'D5_Serie','STRING',0,0,'N','','','N',0);
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato"("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales")
VALUES(986,'D5_MontoPesos','DOUBLE',0,0,'N','','','S',8);
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato"("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales")
VALUES(987,'D5_Porcentaje','DOUBLE',0,0,'N','','','S',8);
/*Distribucion Inversion N°6*/
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato"("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales")
VALUES(988,'D6_Instrumento','STRING',0,0,'N','','','N',0);
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato"("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales")
VALUES(989,'D6_Serie','STRING',0,0,'N','','','N',0);
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato"("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales")
VALUES(990,'D6_MontoPesos','DOUBLE',0,0,'N','','','S',8);
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato"("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales")
VALUES(991,'D6_Porcentaje','DOUBLE',0,0,'N','','','S',8);
/*Distribucion Inversion N°7*/
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato"("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales")
VALUES(992,'D7_Instrumento','STRING',0,0,'N','','','N',0);
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato"("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales")
VALUES(993,'D7_Serie','STRING',0,0,'N','','','N',0);
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato"("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales")
VALUES(994,'D7_MontoPesos','DOUBLE',0,0,'N','','','S',8);
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato"("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales")
VALUES(995,'D7_Porcentaje','DOUBLE',0,0,'N','','','S',8);
/*Distribucion Inversion N°8*/
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato"("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales")
VALUES(996,'D8_Instrumento','STRING',0,0,'N','','','N',0);
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato"("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales")
VALUES(997,'D8_Serie','STRING',0,0,'N','','','N',0);
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato"("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales")
VALUES(998,'D8_MontoPesos','DOUBLE',0,0,'N','','','S',8);
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato"("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales")
VALUES(999,'D8_Porcentaje','DOUBLE',0,0,'N','','','S',8);
/*Distribucion Inversion N°9*/
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato"("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales")
VALUES(1000,'D9_Instrumento','STRING',0,0,'N','','','N',0);
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato"("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales")
VALUES(1001,'D9_Serie','STRING',0,0,'N','','','N',0);
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato"("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales")
VALUES(1002,'D9_MontoPesos','DOUBLE',0,0,'N','','','S',8);
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato"("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales")
VALUES(1003,'D9_Porcentaje','DOUBLE',0,0,'N','','','S',8);
/*Distribucion Inversion N°10*/
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato"("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales")
VALUES(1004,'D10_Instrumento','STRING',0,0,'N','','','N',0);
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato"("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales")
VALUES(1005,'D10_Serie','STRING',0,0,'N','','','N',0);
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato"("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales")
VALUES(1006,'D10_MontoPesos','DOUBLE',0,0,'N','','','S',8);
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato"("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales")
VALUES(1007,'D10_Porcentaje','DOUBLE',0,0,'N','','','S',8);



  --Cartola

INSERT INTO "BSCL.Plataforma.BD::PLA.Formato" ("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales") VALUES(624,'TipoFila','NUMBER',1,1,'S','','','N',0);
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato" ("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales") VALUES(625,'Folio','NUMBER',2,16,'S','','','N',0);
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato" ("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales") VALUES(626,'FechaOperacion','STRING',18,8,'S','','','N',0);
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato" ("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales") VALUES(627,'HoraOperacion','STRING',26,6,'S','','','N',0);
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato" ("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales") VALUES(628,'IdBanco','STRING',32,16,'S','','','N',0);
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato" ("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales") VALUES(629,'TipoInstitucion','STRING',48,10,'S','','','N',0);
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato" ("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales") VALUES(630,'Institucion','STRING',58,30,'S','','','N',0);
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato" ("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales") VALUES(631,'FechaAbono','STRING',88,8,'S','','','N',0);
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato" ("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales") VALUES(632,'HoraAbono','STRING',96,6,'S','','','N',0);
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato" ("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales") VALUES(633,'PeriodoPago','STRING',102,6,'S','','','N',0);
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato" ("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales") VALUES(634,'PeriodoPagoDesde','STRING',108,8,'S','','','N',0);
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato" ("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales") VALUES(635,'PeriodoPagoHasta','STRING',116,8,'S','','','N',0);
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato" ("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales") VALUES(636,'RutPagador','STRING',124,10,'S','','','N',0);
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato" ("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales") VALUES(637,'CuentaCorrientePagador','STRING',134,30,'S','','','N',0);
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato" ("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales") VALUES(638,'NumeroAfiliado','STRING',164,5,'S','','','N',0);
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato" ("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales") VALUES(639,'NumeroContrato','STRING',169,2,'S','','','N',0);
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato" ("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales") VALUES(640,'MontoAbono','STRING',171,12,'S','','','N',0);
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato" ("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales") VALUES(641,'CuentaCorrienteInstitucion','STRING',183,30,'S','','','N',0);

  --Deposito
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato" 
("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales")
VALUES(616,'FechaDeposito','STRING',0,0,'S','','','N',0);

INSERT INTO "BSCL.Plataforma.BD::PLA.Formato" 
("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales")
VALUES(617,'TipoDeposito','STRING',0,0,'S','','','N',0);

INSERT INTO "BSCL.Plataforma.BD::PLA.Formato" 
("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales")
VALUES(618,'ConceptoRecaudacion','STRING',0,0,'S','','','N',0);

INSERT INTO "BSCL.Plataforma.BD::PLA.Formato" 
("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales")
VALUES(619,'MontoDeposito','NUMBER',0,0,'S','','','S',0);

INSERT INTO "BSCL.Plataforma.BD::PLA.Formato" 
("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales")
VALUES(620,'Banco','STRING',0,0,'S','','','N',0);

INSERT INTO "BSCL.Plataforma.BD::PLA.Formato" 
("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales")
VALUES(621,'CuentaCorriente','NUMBER',0,0,'S','','','N',0);

INSERT INTO "BSCL.Plataforma.BD::PLA.Formato" 
("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales")
VALUES(622,'Sucursal','STRING',0,0,'S','','','N',0);

INSERT INTO "BSCL.Plataforma.BD::PLA.Formato" 
("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales")
VALUES(623,'NumeroDocumento','STRING',0,0,'S','','','N',0);

--Relaciones archivo con separacion y sus formatos
--CONTRATOSAAPV
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato"("IdArchivoFormato","IdArchivo","IdFormato")
VALUES(1070,40,900);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato"("IdArchivoFormato","IdArchivo","IdFormato")
VALUES(1071,40,901);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato"("IdArchivoFormato","IdArchivo","IdFormato")
VALUES(1072,40,902);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato"("IdArchivoFormato","IdArchivo","IdFormato")
VALUES(1073,40,903);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato"("IdArchivoFormato","IdArchivo","IdFormato")
VALUES(1074,40,904);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato"("IdArchivoFormato","IdArchivo","IdFormato")
VALUES(1075,40,905);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato"("IdArchivoFormato","IdArchivo","IdFormato")
VALUES(1076,40,906);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato"("IdArchivoFormato","IdArchivo","IdFormato")
VALUES(1077,40,907);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato"("IdArchivoFormato","IdArchivo","IdFormato")
VALUES(1078,40,908);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato"("IdArchivoFormato","IdArchivo","IdFormato")
VALUES(1079,40,909);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato"("IdArchivoFormato","IdArchivo","IdFormato")
VALUES(1080,40,910);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato"("IdArchivoFormato","IdArchivo","IdFormato")
VALUES(1081,40,911);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato"("IdArchivoFormato","IdArchivo","IdFormato")
VALUES(1082,40,912);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato"("IdArchivoFormato","IdArchivo","IdFormato")
VALUES(1083,40,913);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato"("IdArchivoFormato","IdArchivo","IdFormato")
VALUES(1084,40,914);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato"("IdArchivoFormato","IdArchivo","IdFormato")
VALUES(1085,40,915);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato"("IdArchivoFormato","IdArchivo","IdFormato")
VALUES(1086,40,916);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato"("IdArchivoFormato","IdArchivo","IdFormato")
VALUES(1087,40,917);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato"("IdArchivoFormato","IdArchivo","IdFormato")
VALUES(1088,40,918);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato"("IdArchivoFormato","IdArchivo","IdFormato")
VALUES(1089,40,919);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato"("IdArchivoFormato","IdArchivo","IdFormato")
VALUES(1090,40,920);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato"("IdArchivoFormato","IdArchivo","IdFormato")
VALUES(1091,40,921);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato"("IdArchivoFormato","IdArchivo","IdFormato")
VALUES(1092,40,922);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato"("IdArchivoFormato","IdArchivo","IdFormato")
VALUES(1093,40,923);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato"("IdArchivoFormato","IdArchivo","IdFormato")
VALUES(1094,40,924);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato"("IdArchivoFormato","IdArchivo","IdFormato")
VALUES(1095,40,925);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato"("IdArchivoFormato","IdArchivo","IdFormato")
VALUES(1096,40,926);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato"("IdArchivoFormato","IdArchivo","IdFormato")
VALUES(1097,40,927);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato"("IdArchivoFormato","IdArchivo","IdFormato")
VALUES(1098,40,928);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato"("IdArchivoFormato","IdArchivo","IdFormato")
VALUES(1099,40,929);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato"("IdArchivoFormato","IdArchivo","IdFormato")
VALUES(1100,40,930);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato"("IdArchivoFormato","IdArchivo","IdFormato")
VALUES(1101,40,931);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato"("IdArchivoFormato","IdArchivo","IdFormato")
VALUES(1102,40,932);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato"("IdArchivoFormato","IdArchivo","IdFormato")
VALUES(1103,40,933);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato"("IdArchivoFormato","IdArchivo","IdFormato")
VALUES(1104,40,934);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato"("IdArchivoFormato","IdArchivo","IdFormato")
VALUES(1105,40,935);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato"("IdArchivoFormato","IdArchivo","IdFormato")
VALUES(1106,40,936);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato"("IdArchivoFormato","IdArchivo","IdFormato")
VALUES(1107,40,937);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato"("IdArchivoFormato","IdArchivo","IdFormato")
VALUES(1108,40,938);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato"("IdArchivoFormato","IdArchivo","IdFormato")
VALUES(1109,40,939);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato"("IdArchivoFormato","IdArchivo","IdFormato")
VALUES(1110,40,940);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato"("IdArchivoFormato","IdArchivo","IdFormato")
VALUES(1111,40,941);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato"("IdArchivoFormato","IdArchivo","IdFormato")
VALUES(1112,40,942);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato"("IdArchivoFormato","IdArchivo","IdFormato")
VALUES(1113,40,943);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato"("IdArchivoFormato","IdArchivo","IdFormato")
VALUES(1114,40,944);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato"("IdArchivoFormato","IdArchivo","IdFormato")
VALUES(1115,40,945);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato"("IdArchivoFormato","IdArchivo","IdFormato")
VALUES(1116,40,946);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato"("IdArchivoFormato","IdArchivo","IdFormato")
VALUES(1117,40,947);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato"("IdArchivoFormato","IdArchivo","IdFormato")
VALUES(1118,40,948);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato"("IdArchivoFormato","IdArchivo","IdFormato")
VALUES(1119,40,949);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato"("IdArchivoFormato","IdArchivo","IdFormato")
VALUES(1120,40,950);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato"("IdArchivoFormato","IdArchivo","IdFormato")
VALUES(1121,40,951);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato"("IdArchivoFormato","IdArchivo","IdFormato")
VALUES(1122,40,952);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato"("IdArchivoFormato","IdArchivo","IdFormato")
VALUES(1123,40,953);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato"("IdArchivoFormato","IdArchivo","IdFormato")
VALUES(1124,40,954);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato"("IdArchivoFormato","IdArchivo","IdFormato")
VALUES(1125,40,955);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato"("IdArchivoFormato","IdArchivo","IdFormato")
VALUES(1126,40,956);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato"("IdArchivoFormato","IdArchivo","IdFormato")
VALUES(1127,40,957);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato"("IdArchivoFormato","IdArchivo","IdFormato")
VALUES(1128,40,958);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato"("IdArchivoFormato","IdArchivo","IdFormato")
VALUES(1129,40,959);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato"("IdArchivoFormato","IdArchivo","IdFormato")
VALUES(1130,40,960);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato"("IdArchivoFormato","IdArchivo","IdFormato")
VALUES(1131,40,961);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato"("IdArchivoFormato","IdArchivo","IdFormato")
VALUES(1132,40,962);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato"("IdArchivoFormato","IdArchivo","IdFormato")
VALUES(1133,40,963);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato"("IdArchivoFormato","IdArchivo","IdFormato")
VALUES(1134,40,964);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato"("IdArchivoFormato","IdArchivo","IdFormato")
VALUES(1135,40,965);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato"("IdArchivoFormato","IdArchivo","IdFormato")
VALUES(1136,40,966);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato"("IdArchivoFormato","IdArchivo","IdFormato")
VALUES(1137,40,967);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato"("IdArchivoFormato","IdArchivo","IdFormato")
VALUES(1138,40,968);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato"("IdArchivoFormato","IdArchivo","IdFormato")
VALUES(1139,40,969);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato"("IdArchivoFormato","IdArchivo","IdFormato")
VALUES(1140,40,970);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato"("IdArchivoFormato","IdArchivo","IdFormato")
VALUES(1141,40,971);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato"("IdArchivoFormato","IdArchivo","IdFormato")
VALUES(1142,40,972);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato"("IdArchivoFormato","IdArchivo","IdFormato")
VALUES(1143,40,973);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato"("IdArchivoFormato","IdArchivo","IdFormato")
VALUES(1144,40,974);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato"("IdArchivoFormato","IdArchivo","IdFormato")
VALUES(1145,40,975);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato"("IdArchivoFormato","IdArchivo","IdFormato")
VALUES(1146,40,976);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato"("IdArchivoFormato","IdArchivo","IdFormato")
VALUES(1147,40,977);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato"("IdArchivoFormato","IdArchivo","IdFormato")
VALUES(1148,40,978);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato"("IdArchivoFormato","IdArchivo","IdFormato")
VALUES(1149,40,979);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato"("IdArchivoFormato","IdArchivo","IdFormato")
VALUES(1150,40,980);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato"("IdArchivoFormato","IdArchivo","IdFormato")
VALUES(1151,40,981);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato"("IdArchivoFormato","IdArchivo","IdFormato")
VALUES(1152,40,982);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato"("IdArchivoFormato","IdArchivo","IdFormato")
VALUES(1153,40,983);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato"("IdArchivoFormato","IdArchivo","IdFormato")
VALUES(1154,40,984);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato"("IdArchivoFormato","IdArchivo","IdFormato")
VALUES(1155,40,985);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato"("IdArchivoFormato","IdArchivo","IdFormato")
VALUES(1156,40,986);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato"("IdArchivoFormato","IdArchivo","IdFormato")
VALUES(1157,40,987);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato"("IdArchivoFormato","IdArchivo","IdFormato")
VALUES(1158,40,988);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato"("IdArchivoFormato","IdArchivo","IdFormato")
VALUES(1159,40,989);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato"("IdArchivoFormato","IdArchivo","IdFormato")
VALUES(1160,40,990);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato"("IdArchivoFormato","IdArchivo","IdFormato")
VALUES(1161,40,991);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato"("IdArchivoFormato","IdArchivo","IdFormato")
VALUES(1162,40,992);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato"("IdArchivoFormato","IdArchivo","IdFormato")
VALUES(1163,40,993);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato"("IdArchivoFormato","IdArchivo","IdFormato")
VALUES(1164,40,994);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato"("IdArchivoFormato","IdArchivo","IdFormato")
VALUES(1165,40,995);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato"("IdArchivoFormato","IdArchivo","IdFormato")
VALUES(1166,40,996);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato"("IdArchivoFormato","IdArchivo","IdFormato")
VALUES(1167,40,997);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato"("IdArchivoFormato","IdArchivo","IdFormato")
VALUES(1168,40,998);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato"("IdArchivoFormato","IdArchivo","IdFormato")
VALUES(1169,40,999);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato"("IdArchivoFormato","IdArchivo","IdFormato")
VALUES(1170,40,1000);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato"("IdArchivoFormato","IdArchivo","IdFormato")
VALUES(1171,40,1001);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato"("IdArchivoFormato","IdArchivo","IdFormato")
VALUES(1172,40,1002);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato"("IdArchivoFormato","IdArchivo","IdFormato")
VALUES(1173,40,1003);

--Deposito
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato"
("IdArchivoFormato","IdArchivo","IdFormato")
VALUES(779,44,616);

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato"
("IdArchivoFormato","IdArchivo","IdFormato")
VALUES(780,44,617);

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato"
("IdArchivoFormato","IdArchivo","IdFormato")
VALUES(781,44,618);

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato"
("IdArchivoFormato","IdArchivo","IdFormato")
VALUES(782,44,619);

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato"
("IdArchivoFormato","IdArchivo","IdFormato")
VALUES(783,44,620);

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato"
("IdArchivoFormato","IdArchivo","IdFormato")
VALUES(784,44,621);

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato"
("IdArchivoFormato","IdArchivo","IdFormato")
VALUES(785,44,622);

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato"
("IdArchivoFormato","IdArchivo","IdFormato")
VALUES(786,44,623);

 --Cartola
 
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato" ("IdArchivoFormato","IdArchivo","IdFormato") VALUES(787,43,624);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato" ("IdArchivoFormato","IdArchivo","IdFormato") VALUES(788,43,625);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato" ("IdArchivoFormato","IdArchivo","IdFormato") VALUES(789,43,626);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato" ("IdArchivoFormato","IdArchivo","IdFormato") VALUES(790,43,627);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato" ("IdArchivoFormato","IdArchivo","IdFormato") VALUES(791,43,628);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato" ("IdArchivoFormato","IdArchivo","IdFormato") VALUES(792,43,629);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato" ("IdArchivoFormato","IdArchivo","IdFormato") VALUES(793,43,630);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato" ("IdArchivoFormato","IdArchivo","IdFormato") VALUES(794,43,631);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato" ("IdArchivoFormato","IdArchivo","IdFormato") VALUES(795,43,632);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato" ("IdArchivoFormato","IdArchivo","IdFormato") VALUES(796,43,633);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato" ("IdArchivoFormato","IdArchivo","IdFormato") VALUES(797,43,634);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato" ("IdArchivoFormato","IdArchivo","IdFormato") VALUES(798,43,635);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato" ("IdArchivoFormato","IdArchivo","IdFormato") VALUES(799,43,636);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato" ("IdArchivoFormato","IdArchivo","IdFormato") VALUES(800,43,637);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato" ("IdArchivoFormato","IdArchivo","IdFormato") VALUES(801,43,638);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato" ("IdArchivoFormato","IdArchivo","IdFormato") VALUES(802,43,639);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato" ("IdArchivoFormato","IdArchivo","IdFormato") VALUES(803,43,640);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato" ("IdArchivoFormato","IdArchivo","IdFormato") VALUES(804,43,641);

/*==================================================================*/
/*      CONFIGURACION  CARGA DISTRIBUCION CONTRATOS SAAPV   		*/
/*==================================================================*/

--Registro de archivos
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Archivo"("IdArchivo", "IdEmpresa","CodigoArchivo", "TieneSeparador", "SeparadorColumnas","TieneCabecera")
VALUES(41,1,'CARGA_DISTRIBUCION,CONTRATOS_SAAPV','S',';','N');

--Formatos de archivo

INSERT INTO "BSCL.Plataforma.BD::PLA.Formato" 
("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales")
VALUES(579,'FolioContratoSAAPV','STRING',0,0,'S','','','N',0);
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato" 
("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales")
VALUES(580,'IdInstrumento','NUMBER',0,0,'S','','','N',0);
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato" 
("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales")
VALUES(581,'Nemotecnico','STRING',0,0,'S','','','N',0);
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato" 
("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales")
VALUES(582,'MontoPesos','STRING',0,0,'S','','','S',8);
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato" 
("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales")
VALUES(583,'Porcentaje','STRING',0,0,'S','','','S',8);


-- Relaciones archivo con separacion y sus formatos

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato"
("IdArchivoFormato","IdArchivo","IdFormato")
VALUES(739,41,579);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato"
("IdArchivoFormato","IdArchivo","IdFormato")
VALUES(740,41,580);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato"
("IdArchivoFormato","IdArchivo","IdFormato")
VALUES(741,41,581);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato"
("IdArchivoFormato","IdArchivo","IdFormato")
VALUES(742,41,582);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato"
("IdArchivoFormato","IdArchivo","IdFormato")
VALUES(743,41,583);

/*==================================================================*/
/*      CONFIGURACION  CARGA PREVIRED						 		*/
/*==================================================================*/

/**			Registro de archivo			**/
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Archivo"("IdArchivo", "IdEmpresa","CodigoArchivo", "TieneSeparador", "SeparadorColumnas","TieneCabecera","IdModulo")
VALUES(45,1,'CARGA_PREVIRED','N','','N','APV');


/*       Registro de Formato de Archivos*/

INSERT INTO "BSCL.Plataforma.BD::PLA.Formato" 
("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales")
VALUES(644,'fila1','NUMBER',1,1,'S','','','N',0);

INSERT INTO "BSCL.Plataforma.BD::PLA.Formato" 
("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales")
VALUES(645,'Folio','NUMBER',2,16,'S','','','N',0);

INSERT INTO "BSCL.Plataforma.BD::PLA.Formato" 
("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales")
VALUES(646,'Tipo_Pag','NUMBER',18,1,'S','','','N',0);

INSERT INTO "BSCL.Plataforma.BD::PLA.Formato" 
("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales")
VALUES(647,'FechaOperacion','NUMBER',19,8,'S','','','N',0);

INSERT INTO "BSCL.Plataforma.BD::PLA.Formato" 
("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales")
VALUES(648,'HoraOperacion','NUMBER',25,6,'S','','','N',0);

INSERT INTO "BSCL.Plataforma.BD::PLA.Formato" 
("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales")
VALUES(649,'INS_APV','NUMBER',32,4,'S','','','N',0);

INSERT INTO "BSCL.Plataforma.BD::PLA.Formato" 
("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales")
VALUES(650,'Rut_Empleador','STRING',37,10,'S','','','N',0);

INSERT INTO "BSCL.Plataforma.BD::PLA.Formato" 
("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales")
VALUES(651,'Tipo_Empleador','STRING',46,1,'S','','','N',0);

INSERT INTO "BSCL.Plataforma.BD::PLA.Formato" 
("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales")
VALUES(652,'Email_Empleador','STRING',47,50,'S','','','N',0);

INSERT INTO "BSCL.Plataforma.BD::PLA.Formato" 
("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales")
VALUES(653,'fila2','NUMBER',96,1,'S','','','N',0);

INSERT INTO "BSCL.Plataforma.BD::PLA.Formato" 
("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales")
VALUES(654,'RazonSocial_Empleador','STRING',97,50,'S','','','N',0);

INSERT INTO "BSCL.Plataforma.BD::PLA.Formato" 
("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales")
VALUES(655,'Ruta_Empleador1','STRING',145,10,'S','','','N',0);

INSERT INTO "BSCL.Plataforma.BD::PLA.Formato" 
("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales")
VALUES(656,'Act_Econ_Empleador','STRING',154,6,'S','','','N',0);

INSERT INTO "BSCL.Plataforma.BD::PLA.Formato" 
("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales")
VALUES(657,'Direccion_Empleador','STRING',159,50,'S','','','N',0);

INSERT INTO "BSCL.Plataforma.BD::PLA.Formato" 
("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales")
VALUES(658,'Nro_Direccion_Empleador','STRING',208,10,'S','','','N',0);

INSERT INTO "BSCL.Plataforma.BD::PLA.Formato" 
("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales")
VALUES(659,'Departamento_Empleador','STRING',217,20,'S','','','N',0);

INSERT INTO "BSCL.Plataforma.BD::PLA.Formato" 
("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales")
VALUES(660,'Comuna_Empleador','STRING',236,30,'S','','','N',0);

INSERT INTO "BSCL.Plataforma.BD::PLA.Formato" 
("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales")
VALUES(661,'Ciudad_Empleador','STRING',265,30,'S','','','N',0);

INSERT INTO "BSCL.Plataforma.BD::PLA.Formato" 
("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales")
VALUES(662,'Region_Empleador','STRING',294,2,'S','','','N',0);

INSERT INTO "BSCL.Plataforma.BD::PLA.Formato" 
("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales")
VALUES(663,'Telefono_Empleador','STRING',296,20,'S','','','N',0);

INSERT INTO "BSCL.Plataforma.BD::PLA.Formato" 
("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales")
VALUES(664,'Nom_Rep_Legal_Empleador','STRING',315,50,'S','','','N',0);

INSERT INTO "BSCL.Plataforma.BD::PLA.Formato" 
("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales")
VALUES(665,'Rut_Rep_Legal_Empleador','STRING',364,10,'S','','','N',0);

INSERT INTO "BSCL.Plataforma.BD::PLA.Formato" 
("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales")
VALUES(666,'fila3','NUMBER',373,1,'S','','','N',0);

INSERT INTO "BSCL.Plataforma.BD::PLA.Formato" 
("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales")
VALUES(667,'Rut_Trabajador','STRING',374,10,'S','','','N',0);

INSERT INTO "BSCL.Plataforma.BD::PLA.Formato" 
("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales")
VALUES(668,'Ape_Pat_Trabajador','STRING',383,30,'S','','','N',0);

INSERT INTO "BSCL.Plataforma.BD::PLA.Formato" 
("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales")
VALUES(669,'Ape_Mat_Trabajador','STRING',412,30,'S','','','N',0);

INSERT INTO "BSCL.Plataforma.BD::PLA.Formato" 
("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales")
VALUES(670,'Nom_Trabajador','STRING',441,30,'S','','','N',0);

INSERT INTO "BSCL.Plataforma.BD::PLA.Formato" 
("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales")
VALUES(671,'Sexo_Trabajador','STRING',470,1,'S','','','N',0);

INSERT INTO "BSCL.Plataforma.BD::PLA.Formato" 
("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales")
VALUES(672,'Renta_Imponible','NUMBER',471,12,'S','','','N',0);

INSERT INTO "BSCL.Plataforma.BD::PLA.Formato" 
("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales")
VALUES(673,'Monto_APV','NUMBER',482,12,'S','','','N',0);

INSERT INTO "BSCL.Plataforma.BD::PLA.Formato" 
("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales")
VALUES(674,'Monto_DC','NUMBER',493,12,'S','','','N',0);

INSERT INTO "BSCL.Plataforma.BD::PLA.Formato" 
("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales")
VALUES(675,'Monto_Total','NUMBER',504,12,'S','','','N',0);

INSERT INTO "BSCL.Plataforma.BD::PLA.Formato" 
("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales")
VALUES(676,'Periodo_Pago','STRING',515,6,'S','','','N',0);

INSERT INTO "BSCL.Plataforma.BD::PLA.Formato" 
("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales")
VALUES(677,'fila4','NUMBER',520,1,'S','','','N',0);

INSERT INTO "BSCL.Plataforma.BD::PLA.Formato" 
("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales")
VALUES(678,'Monto_Total_APV','NUMBER',521,12,'S','','','N',0);

INSERT INTO "BSCL.Plataforma.BD::PLA.Formato" 
("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales")
VALUES(679,'Monto_Total_DC','NUMBER',531,12,'S','','','N',0);

INSERT INTO "BSCL.Plataforma.BD::PLA.Formato" 
("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales")
VALUES(680,'Monto_Total1','NUMBER',542,12,'S','','','N',0);

INSERT INTO "BSCL.Plataforma.BD::PLA.Formato" 
("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales")
VALUES(681,'Periodo_Pago1','NUMBER',553,6,'S','','','N',0);

INSERT INTO "BSCL.Plataforma.BD::PLA.Formato" 
("IdFormato","CodigoCampo","TipoDato","Posicion","LargoCampo","EsObligatorio","Validacion","MensajeValidacion","IncluyeDecimales","CantidadDecimales")
VALUES(682,'Num_Afiliados','NUMBER',558,7,'S','','','N',0);

/*       RELACION ENTRE ARCHIVO Y FORMATOS      */


INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato"
("IdArchivoFormato","IdArchivo","IdFormato")
VALUES(805,45,642);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato"
("IdArchivoFormato","IdArchivo","IdFormato")
VALUES(806,45,643);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato"
("IdArchivoFormato","IdArchivo","IdFormato")
VALUES(807,45,644);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato"
("IdArchivoFormato","IdArchivo","IdFormato")
VALUES(808,45,645);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato"
("IdArchivoFormato","IdArchivo","IdFormato")
VALUES(809,45,646);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato"
("IdArchivoFormato","IdArchivo","IdFormato")
VALUES(810,45,647);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato"
("IdArchivoFormato","IdArchivo","IdFormato")
VALUES(811,45,648);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato"
("IdArchivoFormato","IdArchivo","IdFormato")
VALUES(812,45,649);

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato"
("IdArchivoFormato","IdArchivo","IdFormato")
VALUES(813,45,650);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato"
("IdArchivoFormato","IdArchivo","IdFormato")
VALUES(814,45,651);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato"
("IdArchivoFormato","IdArchivo","IdFormato")
VALUES(815,45,652);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato"
("IdArchivoFormato","IdArchivo","IdFormato")
VALUES(816,45,653);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato"
("IdArchivoFormato","IdArchivo","IdFormato")
VALUES(817,45,654);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato"
("IdArchivoFormato","IdArchivo","IdFormato")
VALUES(818,45,655);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato"
("IdArchivoFormato","IdArchivo","IdFormato")
VALUES(819,45,656);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato"
("IdArchivoFormato","IdArchivo","IdFormato")
VALUES(820,45,657);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato"
("IdArchivoFormato","IdArchivo","IdFormato")
VALUES(821,45,658);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato"
("IdArchivoFormato","IdArchivo","IdFormato")
VALUES(822,45,659);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato"
("IdArchivoFormato","IdArchivo","IdFormato")
VALUES(823,45,660);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato"
("IdArchivoFormato","IdArchivo","IdFormato")
VALUES(824,45,661);

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato"
("IdArchivoFormato","IdArchivo","IdFormato")
VALUES(825,45,662);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato"
("IdArchivoFormato","IdArchivo","IdFormato")
VALUES(826,45,663);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato"
("IdArchivoFormato","IdArchivo","IdFormato")
VALUES(827,45,664);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato"
("IdArchivoFormato","IdArchivo","IdFormato")
VALUES(828,45,665);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato"
("IdArchivoFormato","IdArchivo","IdFormato")
VALUES(829,45,666);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato"
("IdArchivoFormato","IdArchivo","IdFormato")
VALUES(830,45,667);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato"
("IdArchivoFormato","IdArchivo","IdFormato")
VALUES(831,45,668);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato"
("IdArchivoFormato","IdArchivo","IdFormato")
VALUES(832,45,669);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato"
("IdArchivoFormato","IdArchivo","IdFormato")
VALUES(833,45,670);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato"
("IdArchivoFormato","IdArchivo","IdFormato")
VALUES(834,45,671);

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato"
("IdArchivoFormato","IdArchivo","IdFormato")
VALUES(835,45,672);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato"
("IdArchivoFormato","IdArchivo","IdFormato")
VALUES(836,45,673);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato"
("IdArchivoFormato","IdArchivo","IdFormato")
VALUES(837,45,674);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato"
("IdArchivoFormato","IdArchivo","IdFormato")
VALUES(838,45,675);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato"
("IdArchivoFormato","IdArchivo","IdFormato")
VALUES(839,45,676);

INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato"
("IdArchivoFormato","IdArchivo","IdFormato")
VALUES(840,45,677);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato"
("IdArchivoFormato","IdArchivo","IdFormato")
VALUES(841,45,678);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato"
("IdArchivoFormato","IdArchivo","IdFormato")
VALUES(842,45,679);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato"
("IdArchivoFormato","IdArchivo","IdFormato")
VALUES(843,45,680);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato"
("IdArchivoFormato","IdArchivo","IdFormato")
VALUES(844,45,681);
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato"
("IdArchivoFormato","IdArchivo","IdFormato")
VALUES(845,45,682);

/*==================================================================*/
/*      INSERT GRUPOS PORTAFOLIO							   		*/
/*==================================================================*/


INSERT INTO "BSCL.APV.BD::SCS.PlanTipoInversion"
("IdPlanTipoInversion","NombrePlanTipoInversion","IdEstado")
VALUES(1,'A. ABIERTO','VIG');

INSERT INTO "BSCL.APV.BD::SCS.PlanTipoInversion" 
("IdPlanTipoInversion","NombrePlanTipoInversion","IdEstado")
VALUES(2,'I. ACTIVO','VIG');

INSERT INTO "BSCL.APV.BD::APV.ProductoAgrupador" ("IdProductoAgrupador","NombreProductoAgrupador","IdPlanTipoInversion","IdEstado")
VALUES(1,'APV Fondos Mutuos',1,'VIG'); 

INSERT INTO "BSCL.APV.BD::APV.ProductoAgrupador" ("IdProductoAgrupador","NombreProductoAgrupador","IdPlanTipoInversion","IdEstado") 
VALUES(2,'APV Acciones',2,'VIG');

INSERT INTO "BSCL.APV.BD::APV.ProductoAgrupador" ("IdProductoAgrupador","NombreProductoAgrupador","IdPlanTipoInversion","IdEstado") 
VALUES(3,'APV Administración de Inversiones',2,'VIG');


INSERT INTO "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV"(
    "IdMatrizAgrupadorAPV"	,
    "IdPropositoPortafolio" ,
	"IdPlanTipoInversion"	,
	"IdProductoAgrupador"	,
	"IdRegimenTributario"	,
	"NombreAgrupador"		,
	"CodigoAgrupador"		,
	"IdEstado"				)
VALUES(1,	'PROPOSITOAPV',	1,	1,	1,	'CUENTA ABIERTA FFMM'	,'GRUPO 1','VIG');
INSERT INTO "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV"(
    "IdMatrizAgrupadorAPV"	,
    "IdPropositoPortafolio" ,
	"IdPlanTipoInversion"	,
	"IdProductoAgrupador"	,
	"IdRegimenTributario"	,
	"NombreAgrupador"		,
	"CodigoAgrupador"		,
	"IdEstado"				)
VALUES(2,	'PROPOSITOAPV',	1,	1,	2,	'CUENTA ABIERTA FFMM'	,'GRUPO 1','VIG');
INSERT INTO "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV"(
    "IdMatrizAgrupadorAPV"	,
    "IdPropositoPortafolio" ,
	"IdPlanTipoInversion"	,
	"IdProductoAgrupador"	,
	"IdRegimenTributario"	,
	"NombreAgrupador"		,
	"CodigoAgrupador"		,
	"IdEstado"				)
VALUES(3,	'PROPOSITOAPV',	1,	1,	3,	'CUENTA ABIERTA FFMM'	,'GRUPO 1','VIG');
INSERT INTO "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV"(
    "IdMatrizAgrupadorAPV"	,
    "IdPropositoPortafolio" ,
	"IdPlanTipoInversion"	,
	"IdProductoAgrupador"	,
	"IdRegimenTributario"	,
	"NombreAgrupador"		,
	"CodigoAgrupador"		,
	"IdEstado"				)
VALUES(4,	'PROPOSITOAPV',	1,	1,	4,	'CUENTA ABIERTA FFMM'	,'GRUPO 1','VIG');
INSERT INTO "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV"(
    "IdMatrizAgrupadorAPV"	,
    "IdPropositoPortafolio" ,
	"IdPlanTipoInversion"	,
	"IdProductoAgrupador"	,
	"IdRegimenTributario"	,
	"NombreAgrupador"		,
	"CodigoAgrupador"		,
	"IdEstado"				)
VALUES(5,	'PROPOSITOAPV',	1,	1,	5,	'CUENTA ABIERTA FFMM'	,'GRUPO 1','VIG');
INSERT INTO "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV"(
    "IdMatrizAgrupadorAPV"	,
    "IdPropositoPortafolio" ,
	"IdPlanTipoInversion"	,
	"IdProductoAgrupador"	,
	"IdRegimenTributario"	,
	"NombreAgrupador"		,
	"CodigoAgrupador"		,
	"IdEstado"				)
VALUES(6,	'PROPOSITOAPV',	1,	1,	6,	'CUENTA ABIERTA FFMM'	,'GRUPO 1','VIG');
INSERT INTO "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV"(
    "IdMatrizAgrupadorAPV"	,
    "IdPropositoPortafolio" ,
	"IdPlanTipoInversion"	,
	"IdProductoAgrupador"	,
	"IdRegimenTributario"	,
	"NombreAgrupador"		,
	"CodigoAgrupador"		,
	"IdEstado"				)
VALUES(7,	'PROPOSITOAPV',	2,	2,	1,	'CUENTA ACTIVA ACCIONES'	,'GRUPO 2','VIG');
INSERT INTO "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV"(
    "IdMatrizAgrupadorAPV"	,
    "IdPropositoPortafolio" ,
	"IdPlanTipoInversion"	,
	"IdProductoAgrupador"	,
	"IdRegimenTributario"	,
	"NombreAgrupador"		,
	"CodigoAgrupador"		,
	"IdEstado"				)
VALUES(8,	'PROPOSITOAPV',	2,	2,	2,	'CUENTA ACTIVA ACCIONES'	,'GRUPO 2','VIG');
INSERT INTO "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV"(
    "IdMatrizAgrupadorAPV"	,
    "IdPropositoPortafolio" ,
	"IdPlanTipoInversion"	,
	"IdProductoAgrupador"	,
	"IdRegimenTributario"	,
	"NombreAgrupador"		,
	"CodigoAgrupador"		,
	"IdEstado"				)
VALUES(9,	'PROPOSITOAPV',	2,	2,	3,	'CUENTA ACTIVA ACCIONES'	,'GRUPO 2','VIG');
INSERT INTO "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV"(
    "IdMatrizAgrupadorAPV"	,
    "IdPropositoPortafolio" ,
	"IdPlanTipoInversion"	,
	"IdProductoAgrupador"	,
	"IdRegimenTributario"	,
	"NombreAgrupador"		,
	"CodigoAgrupador"		,
	"IdEstado"				)
VALUES(10,	'PROPOSITOAPV',	2,	2,	4,	'CUENTA ACTIVA ACCIONES'	,'GRUPO 2','VIG');
INSERT INTO "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV"(
    "IdMatrizAgrupadorAPV"	,
    "IdPropositoPortafolio" ,
	"IdPlanTipoInversion"	,
	"IdProductoAgrupador"	,
	"IdRegimenTributario"	,
	"NombreAgrupador"		,
	"CodigoAgrupador"		,
	"IdEstado"				)
VALUES(11, 'PROPOSITOAPV',	2,	2,	5,	'CUENTA ACTIVA ACCIONES'	,'GRUPO 2','VIG');
INSERT INTO "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV"(
    "IdMatrizAgrupadorAPV"	,
    "IdPropositoPortafolio" ,
	"IdPlanTipoInversion"	,
	"IdProductoAgrupador"	,
	"IdRegimenTributario"	,
	"NombreAgrupador"		,
	"CodigoAgrupador"		,
	"IdEstado"				)
VALUES(12,	'PROPOSITOAPV',	2,	2,	6,	'CUENTA ACTIVA ACCIONES'	,'GRUPO 2','VIG');
INSERT INTO "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV"(
    "IdMatrizAgrupadorAPV"	,
    "IdPropositoPortafolio" ,
	"IdPlanTipoInversion"	,
	"IdProductoAgrupador"	,
	"IdRegimenTributario"	,
	"NombreAgrupador"		,
	"CodigoAgrupador"		,
	"IdEstado"				)
VALUES(13,	'PROPOSITOAPV',	2,	3,	1,	'CUENTA ACTIVA ADMINISTRACION INVERSIONES'	,'GRUPO 3','VIG');
INSERT INTO "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV"(
    "IdMatrizAgrupadorAPV"	,
    "IdPropositoPortafolio" ,
	"IdPlanTipoInversion"	,
	"IdProductoAgrupador"	,
	"IdRegimenTributario"	,
	"NombreAgrupador"		,
	"CodigoAgrupador"		,
	"IdEstado"				)
VALUES(14,	'PROPOSITOAPV',	2,	3,	2,	'CUENTA ACTIVA ADMINISTRACION INVERSIONES'	,'GRUPO 3','VIG');
INSERT INTO "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV"(
    "IdMatrizAgrupadorAPV"	,
    "IdPropositoPortafolio" ,
	"IdPlanTipoInversion"	,
	"IdProductoAgrupador"	,
	"IdRegimenTributario"	,
	"NombreAgrupador"		,
	"CodigoAgrupador"		,
	"IdEstado"				)
VALUES(15,	'PROPOSITOAPV',	2,	3,	3,	'CUENTA ACTIVA ADMINISTRACION INVERSIONES'	,'GRUPO 3','VIG');
INSERT INTO "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV"(
    "IdMatrizAgrupadorAPV"	,
    "IdPropositoPortafolio" ,
	"IdPlanTipoInversion"	,
	"IdProductoAgrupador"	,
	"IdRegimenTributario"	,
	"NombreAgrupador"		,
	"CodigoAgrupador"		,
	"IdEstado"				)
VALUES(16,	'PROPOSITOAPV',	2,	3,	4,	'CUENTA ACTIVA ADMINISTRACION INVERSIONES'	,'GRUPO 3','VIG');
INSERT INTO "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV"(
    "IdMatrizAgrupadorAPV"	,
    "IdPropositoPortafolio" ,
	"IdPlanTipoInversion"	,
	"IdProductoAgrupador"	,
	"IdRegimenTributario"	,
	"NombreAgrupador"		,
	"CodigoAgrupador"		,
	"IdEstado"				)
VALUES(17,	'PROPOSITOAPV',	2,	3,	5,	'CUENTA ACTIVA ADMINISTRACION INVERSIONES'	,'GRUPO 3','VIG');
INSERT INTO "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV"(
    "IdMatrizAgrupadorAPV"	,
    "IdPropositoPortafolio" ,
	"IdPlanTipoInversion"	,
	"IdProductoAgrupador"	,
	"IdRegimenTributario"	,
	"NombreAgrupador"		,
	"CodigoAgrupador"		,
	"IdEstado"				)
VALUES(18,	'PROPOSITOAPV',	2,	3,	6,	'CUENTA ACTIVA ADMINISTRACION INVERSIONES'	,'GRUPO 3','VIG');
 

/*==============================================================*/
/*TEST Bitacora Estados Dist.                                   */
/*==============================================================*/

INSERT INTO "BSCL.APV.BD::APV.EstadoTransicion"
("IdEstadoTransicion","Tipo","IdRegistro","FechaTransicion","EstadoAnterior","EstadoNuevo","Observacion","Usuario")
VALUES(1,'Recaudacion',76,'2017-11-13',' ','ING','OK','BDUARTE');

INSERT INTO "BSCL.APV.BD::APV.EstadoTransicion"
("IdEstadoTransicion","Tipo","IdRegistro","FechaTransicion","EstadoAnterior","EstadoNuevo","Observacion","Usuario")
VALUES(2,'Recaudacion',76,'2017-11-14',' ','CON','OK','BDUARTE');

INSERT INTO "BSCL.APV.BD::APV.EstadoTransicion"
("IdEstadoTransicion","Tipo","IdRegistro","FechaTransicion","EstadoAnterior","EstadoNuevo","Observacion","Usuario")
VALUES(3,'Recaudacion',76,'2017-11-15',' ','REZ','CONTRATO NO ESTA VIGENTE','JCASTRO');

INSERT INTO "BSCL.APV.BD::APV.EstadoTransicion"
("IdEstadoTransicion","Tipo","IdRegistro","FechaTransicion","EstadoAnterior","EstadoNuevo","Observacion","Usuario")
VALUES(4,'Recaudacion',73,'2017-11-15',' ','ING','CONTRATO NO ESTA VIGENTE','JCASTRO');

/*==============================================================*/
/*Origen Depósitos Recaudación                                   */
/*==============================================================*/

INSERT INTO "BSCL.APV.BD::REC.Origen"
("IdOrigen","Codigo","Nombre","Tipo","IdEstado")
VALUES(1,'CAJSURA', 'CAJA SURA', 'MANUAL', 'ACT');

INSERT INTO "BSCL.APV.BD::REC.Origen" 
("IdOrigen","Codigo","Nombre","Tipo","IdEstado")
VALUES(2,'CAJLAND', 'CAJA LOS ANDES', 'MANUAL', 'ACT');

INSERT INTO "BSCL.APV.BD::REC.Origen" 
("IdOrigen","Codigo","Nombre","Tipo","IdEstado")
VALUES(3,'SANTAND', 'SANTANDER', 'MANUAL', 'ACT');

INSERT INTO "BSCL.APV.BD::REC.Origen" 
("IdOrigen","Codigo","Nombre","Tipo","IdEstado")
VALUES(4,'PREVRED', 'PREVIRED', 'CARGA', 'ACT');

INSERT INTO "BSCL.APV.BD::REC.Origen" 
("IdOrigen","Codigo","Nombre","Tipo","IdEstado")
VALUES(5,'PAC', 'PAC', 'CARGA', 'ACT');

INSERT INTO "BSCL.APV.BD::REC.Origen" 
("IdOrigen","Codigo","Nombre","Tipo","IdEstado")
VALUES(6,'TRASPA', 'TRASPASO', 'MANUAL', 'ACT');
/*==============================================================*/
/*Conceptos Tesoreria,Plataforma,Cuenta Inversion               */
/*==============================================================*/

INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto"
("IdConcepto", "Nombre", "IdEstado") 
SELECT 'INGAPV','INGRESO POR APV','VIG' FROM DUMMY;

INSERT INTO "BSCL.Tesoreria.BD::TES.ConceptoTesoreria" 
("IdConcepto","NombreEntidad","MultipleConcepto","Tipo","DesplegarEnComboBox","DisponibleIngresoManual","VisiblePorEjecutivo","Origen")
SELECT 'INGAPV','TES.IngresoEgreso','N','IN','S','N','N','APV' FROM DUMMY; 
  
INSERT INTO "BSCL"."BSCL.CuentaInversion.BD::CTA.ConceptoCuentaInversion" 
("IdConcepto","TipoConcepto","DesplegarEnCombobox","DisponibleIngresoManual","VisiblePorEjecutivo","NombreEntidad","Origen") 
SELECT 'INGAPV','AB','S','N','S','CTA.CARGOABONOMOVIMIENTO','APV' FROM DUMMY;

INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto"
("IdConcepto", "Nombre", "IdEstado") 
SELECT 'APVPAC','INGRESO CARGA PAC APV','VIG' FROM DUMMY;

INSERT INTO "BSCL.Tesoreria.BD::TES.ConceptoTesoreria" 
("IdConcepto","NombreEntidad","MultipleConcepto","Tipo","DesplegarEnComboBox","DisponibleIngresoManual","VisiblePorEjecutivo","Origen")
SELECT 'APVPAC','TES.IngresoEgreso','N','IN','N','N','N','APV' FROM DUMMY; 

INSERT INTO "BSCL.Tesoreria.BD::TES.PreferenciaPagoCorredor"("IdPreferenciasPagoCorredor","IdTipoMovimiento","IdConcepto","IdMedioDePago","IdCuentaBanco","UnicaOpcionDePago","IdEmpresa","IdEstado")
VALUES (34, 'MOVING', 'APVPAC', 'TRA', 1, 'S', 1,'ACT');
/*==============================================================*/
/*INDICADORES									                */
/*==============================================================*/

insert into "BSCL.Plataforma.BD::IND.TipoIndicador" ( "IdTipoIndicador", "IdTipoEntidad",
  "IdEmpresa", "ValorIndicadorDefecto", "ClaseDato", "NombreTipoIndicador", "CodigoTipoIndicador","IdTipoPersona", 
  "GrupoIndicador", "IdUso", "IdEstado", "CantidadDecimales", "LongitudMinima", "LongitudMaxima", "RequiereAutorizacion" ) 
VALUES (10021, 'PERSONA', 1, '', 'NVARCHAR', 'Nombre de encargado de RRHH','NOMBRE_ENCARGADO_RRHH','J', 'DATOS EMPLEADOR', 'IND', 'ACT', 0, 1, 40, 'N' );

insert into "BSCL.Plataforma.BD::IND.TipoIndicador" ( "IdTipoIndicador", "IdTipoEntidad",
  "IdEmpresa", "ValorIndicadorDefecto", "ClaseDato", "NombreTipoIndicador", "CodigoTipoIndicador","IdTipoPersona" ,
  "GrupoIndicador", "IdUso", "IdEstado", "CantidadDecimales", "LongitudMinima", "LongitudMaxima", "RequiereAutorizacion" ) 
VALUES (10028, 'PERSONA', 1, '', 'NVARCHAR', 'Mail de encargado de RRHH','MAIL_ENCARGADO_RRHH','J', 'DATOS EMPLEADOR', 'IND', 'ACT', 0, 1, 40, 'N' );

insert into "BSCL.Plataforma.BD::IND.TipoIndicador" ( "IdTipoIndicador", "IdTipoEntidad",
  "IdEmpresa", "ValorIndicadorDefecto", "ClaseDato", "NombreTipoIndicador", "CodigoTipoIndicador","IdTipoPersona" ,
  "GrupoIndicador", "IdUso", "IdEstado", "CantidadDecimales", "LongitudMinima", "LongitudMaxima", "RequiereAutorizacion" ) 
VALUES (10029, 'PERSONA', 1, '', 'NVARCHAR', 'Teléfono de encargado de RRHH','TELEFONO_ENCARGADO_RRHH','J', 'DATOS EMPLEADOR', 'IND', 'ACT', 0, 1, 40, 'N' );

/*==============================================================*/
/*				Direccion Fisica Proposito APV                  */
/*==============================================================*/

/* 05-02-2018 */
INSERT INTO BSCL."BSCL.SocioNegocio.BD::PER.PropositoMedioContacto"
("IdPropositoMedioContacto", "Descripcion")
VALUES('PREV', 'APV');


/*==============================================================*/
/*			 			Perfil Comercial                  	    */
/*==============================================================*/

INSERT INTO "BSCL.Plataforma.BD::COM.PerfilComercialBase" values('PERF','PERF','BALANCEADO','A','SELENIUMBALANCEADO',0,'PLANTILLA002','2016-03-28','2016-03-28','ACT','N',1,5,'');	



/*==============================================================*/
/*			 			OrigenDestinoTransicion            	    */
/*==============================================================*/
--INSERT INTO "BSCL.APV.BD::APV.OrigenDestinoTransicion" VALUES (1, 1, 'DIS', 'S', 'N', 110, 100 ,129 , 'ACT'); --DESA
INSERT INTO "BSCL.APV.BD::APV.OrigenDestinoTransicion" VALUES (1, 1, 'DIS', 'S', 'N', 110, 164 ,111 , 'ACT'); --QA


/*==============================================================*/
/*			 			MedioRecaudacion			       	    */
/*==============================================================*/	
INSERT INTO "BSCL.APV.BD::SCS.MedioRecaudacion" ("IdMedioRecaudacion","CodigoMedioRecaudacion","NombreMedioRecaudacion","TipoMedioRecaudacion")
VALUES(1,'DIREMP','DIRECTAMENTE EL TRABAJADOR','DIRECTA');	 	
INSERT INTO "BSCL.APV.BD::SCS.MedioRecaudacion" ("IdMedioRecaudacion","CodigoMedioRecaudacion","NombreMedioRecaudacion","TipoMedioRecaudacion")
VALUES(2,'ATREMP','|A TRAVES DEL EMPLEADOR','DIRECTA');	 	
INSERT INTO "BSCL.APV.BD::SCS.MedioRecaudacion" ("IdMedioRecaudacion","CodigoMedioRecaudacion","NombreMedioRecaudacion","TipoMedioRecaudacion")
VALUES(3,'PAC','PAC','DIRECTA');	 	
INSERT INTO "BSCL.APV.BD::SCS.MedioRecaudacion" ("IdMedioRecaudacion","CodigoMedioRecaudacion","NombreMedioRecaudacion","TipoMedioRecaudacion")
VALUES(4,'IPS','IPS - EX INP','INDIRECTA');	 	
INSERT INTO "BSCL.APV.BD::SCS.MedioRecaudacion" ("IdMedioRecaudacion","CodigoMedioRecaudacion","NombreMedioRecaudacion","TipoMedioRecaudacion")
VALUES(5,'AFP','AFP','INDIRECTA');	 	
	 	
