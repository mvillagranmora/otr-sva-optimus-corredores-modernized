SET SCHEMA "BSCL";

DELETE FROM "BSCL.Plataforma.BD::BAT.TipoPrograma" WHERE "IdTipoPrograma"='CargaDJ';
DELETE FROM "BSCL.Plataforma.BD::BAT.TipoPrograma" WHERE "IdTipoPrograma"='CuadraturaDJ';
DELETE FROM "BSCL.Plataforma.BD::BAT.TipoPrograma" WHERE "IdTipoPrograma"='ExportacionDJ';
DELETE FROM "BSCL.Plataforma.BD::BAT.TipoPrograma" WHERE "IdTipoPrograma"='GeneracionDJ';
DELETE FROM "BSCL.Plataforma.BD::BAT.TipoPrograma" WHERE "IdTipoPrograma"='ValidacionDJ';
DELETE FROM "BSCL.Plataforma.BD::BAT.TipoPrograma" WHERE "IdTipoPrograma"='ASIG_CRONO';
DELETE FROM "BSCL.Plataforma.BD::BAT.TipoPrograma" WHERE "IdTipoPrograma"='ASIG_CRONO_AUTOMATIC';
DELETE FROM "BSCL.Plataforma.BD::BAT.TipoPrograma" WHERE "IdTipoPrograma"='VALORIZADOR';

INSERT INTO "BSCL.Plataforma.BD::BAT.TipoPrograma" VALUES('CargaDJ', 'Carga de datos DJ', 1, 'Tributario', 'cl.bcs.normativa.jobs.BcsJobCarga', 'JAVA', '');
INSERT INTO "BSCL.Plataforma.BD::BAT.TipoPrograma" VALUES('ProcesamientoAutomaticoTransacciones', 'Asignacion y Facturacion Automatico de Transacciones', 1, 'Ordenes', 'cl.bcs.ProcesadorTransaccionesJob', 'JAVA', '');
INSERT INTO "BSCL.Plataforma.BD::BAT.TipoPrograma" VALUES('CuadraturaDJ', 'Cuadratura de datos DJ', 1, 'Tributario', 'cl.bcs.normativa.jobs.BcsJobCuadratura', 'JAVA', '');
INSERT INTO "BSCL.Plataforma.BD::BAT.TipoPrograma" VALUES('ExportacionDJ', 'Exportacion de DJ', 1, 'Tributario', 'cl.bcs.normativa.jobs.BcsJobExportacion', 'JAVA', '');
INSERT INTO "BSCL.Plataforma.BD::BAT.TipoPrograma" VALUES('GeneracionDJ', 'Generacion de DJ', 1, 'Tributario', 'cl.bcs.normativa.jobs.BcsJobGeneracion', 'JAVA', '');
INSERT INTO "BSCL.Plataforma.BD::BAT.TipoPrograma" VALUES('ValidacionDJ', 'Validacion de datos DJ', 1, 'Tributario', 'cl.bcs.normativa.jobs.BcsJobValidacion', 'JAVA', '');
INSERT INTO "BSCL.Plataforma.BD::BAT.TipoPrograma" VALUES('ASIG_CRONO_AUTOMATIC', 'Asignación crónologica automatica', 3,'Ordenes','cl.bcs.scheduler.orders.bcsjob.BcsJobOrderChronologiAllocationReader','JAVA','');
INSERT INTO "BSCL.Plataforma.BD::BAT.TipoPrograma" VALUES('ASIG_CRONO', 'Asignación crónologica', 3,'Ordenes','cl.bcs.scheduler.orders.bcsjob.BcsJobOrderChronologicAllocation','JAVA','');
INSERT INTO "BSCL.Plataforma.BD::BAT.TipoPrograma" VALUES('VALORIZADOR', 'Valorizador', 1,'Plataforma','cl.bcs.plataforma.jobs.valorizador.Valoriza','JAVA','');
INSERT INTO "BSCL.Plataforma.BD::BAT.TipoPrograma" VALUES('VENCIMIENTO_DOC', 'Vencimiento de Documentos', 1, 'Plataforma', 'cl.bcs.plataforma.jobs.VencimientoDocumentos', 'JAVA', '');
INSERT INTO "BSCL.Plataforma.BD::BAT.TipoPrograma" VALUES('DOCUMENTOSVENCLI', 'Notificar documentos vencidos/a vencer de clientes', 1,'SOC','cl.bcs.plataforma.jobs.DocumentosClientePorVencerJob','JAVA','');
INSERT INTO "BSCL.Plataforma.BD::BAT.TipoPrograma" VALUES('RESUMEN_FACTURA_CLI', 'Notificar al cliente sus movimientos facturador cada dia', 1,'FAC','cl.bcs.facturacion.ResumenFacturasClienteJob','JAVA','');
INSERT INTO "BSCL.Plataforma.BD::BAT.TipoPrograma" VALUES('UPD_PARAMS_FINANCIEROS_OPEN', 'Actualizar parametros financieros y paridades en apertura', 1,'MER','cl.bcs.plataforma.jobs.ActualizarParametrosFinancieros','JAVA','','N');
INSERT INTO "BSCL.Plataforma.BD::BAT.TipoPrograma" VALUES('UPD_PARAMS_FINANCIEROS_CLOSE', 'Actualizar parametros financieros y paridades en cierre', 1,'MER','cl.bcs.plataforma.jobs.ActualizarParametrosFinancieros','JAVA','','N');

-- CARTOLAS
DELETE FROM "BSCL.Plataforma.BD::BAT.TipoPrograma" WHERE "IdTipoPrograma"='ProcesoCartolas';
INSERT INTO "BSCL.Plataforma.BD::BAT.TipoPrograma" VALUES('ProcesoCartolas', 'Proceso de cartolas', 1, 'Cartolas', 'cl.bcs.normativa.cartolas.jobs.BcsJobCartolas', 'JAVA', '');

--NORMATIVA


DELETE FROM "BSCL.Plataforma.BD::BAT.TipoPrograma" WHERE "IdTipoPrograma"='EXPORTACIONCARTA2ART171';
DELETE FROM "BSCL.Plataforma.BD::BAT.TipoPrograma" WHERE "IdTipoPrograma"='EXPORTACIONCARTA1ART171';
DELETE FROM "BSCL.Plataforma.BD::BAT.TipoPrograma" WHERE "IdTipoPrograma"='EXPORTACIONCI63NOMINA';
DELETE FROM "BSCL.Plataforma.BD::BAT.TipoPrograma" WHERE "IdTipoPrograma"='EXPORTACIONCI63TRANSACCION';
DELETE FROM "BSCL.Plataforma.BD::BAT.TipoPrograma" WHERE "IdTipoPrograma"='PROCESONOMINACIRCULARIZACION';
DELETE FROM "BSCL.Plataforma.BD::BAT.TipoPrograma" WHERE "IdTipoPrograma"='PROCESOMASIVOCARTACIRCULARIZACION';
DELETE FROM "BSCL.Plataforma.BD::BAT.TipoPrograma" WHERE "IdTipoPrograma"='GRABARCARTACIRCULARIZACIONMASIVO';
DELETE FROM "BSCL.Plataforma.BD::BAT.TipoPrograma" WHERE "IdTipoPrograma"='EXPORTACIONUAFROE';
DELETE FROM "BSCL.Plataforma.BD::BAT.TipoPrograma" WHERE "IdTipoPrograma"='EXPORTACIONUAFDOF';
DELETE FROM "BSCL.Plataforma.BD::BAT.TipoPrograma" WHERE "IdTipoPrograma"='PROCESOEVALUACIONMONTOSOI';
DELETE FROM "BSCL.Plataforma.BD::BAT.TipoPrograma" WHERE "IdTipoPrograma"='EXPORTACIONINFTRASEIL';
DELETE FROM "BSCL.Plataforma.BD::BAT.TipoPrograma" WHERE "IdTipoPrograma"='EXPORTACIONINFTRABCS';
DELETE FROM "BSCL.Plataforma.BD::BAT.TipoPrograma" WHERE "IdTipoPrograma"='EXPORTACIONUAF57';




INSERT INTO "BSCL.Plataforma.BD::BAT.TipoPrograma" VALUES('EXPORTACIONCARTA2ART171', 'Exportación Carta 2 Art171 SVS', 1, 'Normativa', 'cl.bcs.normativa.jobs.BcsJobExportacionServ', 'JAVA', '');
INSERT INTO "BSCL.Plataforma.BD::BAT.TipoPrograma" VALUES('EXPORTACIONCARTA1ART171', 'Exportación Carta 1 Art171 BCS', 1, 'Normativa', 'cl.bcs.normativa.jobs.BcsJobExportacionServ', 'JAVA', '');
INSERT INTO "BSCL.Plataforma.BD::BAT.TipoPrograma" VALUES('EXPORTACIONCI63NOMINA', 'Exportación Nomina de Personas Cir 63', 1, 'Normativa', 'cl.bcs.normativa.jobs.BcsJobExportacionServ', 'JAVA', '');
INSERT INTO "BSCL.Plataforma.BD::BAT.TipoPrograma" VALUES('EXPORTACIONCI63TRANSACCION', 'Exportación transacciones de relacionados Cir 63', 1, 'Normativa', 'cl.bcs.normativa.jobs.BcsJobExportacionServ', 'JAVA', '');
INSERT INTO "BSCL.Plataforma.BD::BAT.TipoPrograma" VALUES('PROCESONOMINACIRCULARIZACION', 'Proceso Nomina de Circularizacion', 1, 'Normativa', 'cl.bcs.normativa.jobs.BcsJobExportacionServ', 'JAVA', '');
INSERT INTO "BSCL.Plataforma.BD::BAT.TipoPrograma" VALUES('PROCESOMASIVOCARTACIRCULARIZACION', 'Generacion Carta de Circularizacion PDF Masivo', 1, 'Normativa', 'cl.bcs.normativa.jobs.BcsJobCartaCircularizacionMasivo', 'JAVA', '');
INSERT INTO "BSCL.Plataforma.BD::BAT.TipoPrograma" VALUES('GRABARCARTACIRCULARIZACIONMASIVO', 'Graba la Carta de Circularizacion para PDF Masivo', 1, 'Normativa', 'cl.bcs.normativa.jobs.BcsJobGrabarCertificadoCliente', 'JAVA', '');
INSERT INTO "BSCL.Plataforma.BD::BAT.TipoPrograma" VALUES('EXPORTACIONUAFROE', 'Exportación Reporte Operaciones en Efectivo - UAF', 1, 'Normativa', 'cl.bcs.normativa.jobs.BcsJobExportacionServ', 'JAVA', '');
INSERT INTO "BSCL.Plataforma.BD::BAT.TipoPrograma" VALUES('EXPORTACIONUAFDOF', 'Exportación Transacciones con declaración origen de fondos - UAF', 1, 'Normativa', 'cl.bcs.normativa.jobs.BcsJobExportacionServ', 'JAVA', '');
INSERT INTO "BSCL.Plataforma.BD::BAT.TipoPrograma" VALUES('INFDERECH', 'Informe Derechos', 1, 'Normativa', 'cl.bcs.normativa.jobs.BcsJobExportacionServ', 'JAVA', '','N');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::BAT.TipoPrograma" ("IdTipoPrograma", "Descripcion", "MaximoIntentos", "Modulo", "NombreClasePrograma","TipoClasePrograma", "EsPauta")
VALUES('PROCESOEVALUACIONMONTOSOI', 'Proceso de Evaluacion Monto Operado - SOI', 1, 'Normativa', 'cl.bcs.normativa.jobs.BcsJobProcesarServ', 'JAVA', 'N');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::BAT.TipoPrograma" ("IdTipoPrograma", "Descripcion", "MaximoIntentos", "Modulo", "NombreClasePrograma","TipoClasePrograma", "EsPauta")
VALUES('EXPORTACIONUAF57', 'Exportación UAF57', 1, 'Normativa', 'cl.bcs.normativa.jobs.BcsJobGenerarCircularUAF57', 'JAVA', 'N');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::BAT.TipoPrograma" ("IdTipoPrograma", "Descripcion", "MaximoIntentos", "Modulo", "NombreClasePrograma","TipoClasePrograma", "EsPauta")
VALUES('EXPORTACIONINFTRASEIL', 'Exportación Transacciones por Oficio SVS - SEIL', 1, 'Normativa', 'cl.bcs.normativa.jobs.BcsJobExportacionServ', 'JAVA', 'N');

INSERT INTO "BSCL"."BSCL.Plataforma.BD::BAT.TipoPrograma" ("IdTipoPrograma", "Descripcion", "MaximoIntentos", "Modulo", "NombreClasePrograma","TipoClasePrograma", "EsPauta")
VALUES('EXPORTACIONINFTRABCS', 'Exportación Transacciones por Oficio  - BCS', 1, 'Normativa', 'cl.bcs.normativa.jobs.BcsJobExportacionServ', 'JAVA', 'N');
	 	 
	 


INSERT INTO "BSCL.Plataforma.BD::BAT.TipoPrograma" VALUES('EXPORTACIONSEIL', 'Exportacion SEIL', 1, 'Normativa', 'cl.bcs.normativa.jobs.BcsJobExportacionServ', 'JAVA', '');

--FFMM
DELETE FROM "BSCL.Plataforma.BD::BAT.TipoPrograma" WHERE "IdTipoPrograma"='CARGAMASIVASOLICITUDESFFMM';
DELETE FROM "BSCL.Plataforma.BD::BAT.TipoPrograma" WHERE "IdTipoPrograma"='CERTIFICACIONMASIVASOLICITUDESFFMM';
DELETE FROM "BSCL.Plataforma.BD::BAT.TipoPrograma" WHERE "IdTipoPrograma"='CargaOnlineAGF';
DELETE FROM "BSCL.Plataforma.BD::BAT.TipoPrograma" WHERE "IdTipoPrograma"='BLOQUEOCIERRESERIESFFMM';
DELETE FROM "BSCL.Plataforma.BD::BAT.TipoPrograma" WHERE "IdTipoPrograma"='GENERARARCHIVOOPERACIONESAGF';

INSERT INTO "BSCL.Plataforma.BD::BAT.TipoPrograma" VALUES('CARGAMASIVASOLICITUDESFFMM','Carga masiva de solicitudes de fondos mutuo',1,'FondoMutuo','cl.bcs.fondomutuo.cargamasiva.BcsJobCargaMasivaSolicitudes','JAVA','','N');
INSERT INTO "BSCL.Plataforma.BD::BAT.TipoPrograma" VALUES('CERTIFICACIONMASIVASOLICITUDESFFMM','Certificacion masiva de solicitudes de fondos mutuos',1,'FondoMutuo','cl.bcs.fondomutuo.certificacionmasiva.BcsJobCertificacionMasivaSolicitudes','JAVA','','N');
INSERT INTO "BSCL.Plataforma.BD::BAT.TipoPrograma" VALUES('CargaOnlineAGF','Carga de solicitudes online para la AGF',1,'FondoMutuo','cl.bcs.fondomutuo.cargaonlineagf.BcsJobCargaOnlineAGF','JAVA','','N');
INSERT INTO "BSCL.Plataforma.BD::BAT.TipoPrograma" VALUES('BLOQUEOCIERRESERIESFFMM','Bloqueo y cierre de series de FFMM',1,'FondoMutuo','cl.bcs.fondomutuo.bloqueocierreseries.BcsJobBloqueoCierreSeries','JAVA','','N');
INSERT INTO "BSCL.Plataforma.BD::BAT.TipoPrograma" VALUES('GENERARARCHIVOOPERACIONESAGF','Genera archivo de operaciones para la AGF FFMM',1,'FondoMutuo','cl.bcs.fondomutuo.generaarchivooperacionesagf.GenerarArchivo','JAVA','','N');

--FIN FFMM


UPDATE bscl."BSCL.Plataforma.BD::BAT.TipoPrograma" SET "Nodo"='' WHERE "IdTipoPrograma"='NOTIFY';


--Insert Tipo Programa consolidacli

INSERT INTO "BSCL"."BSCL.Plataforma.BD::BAT.TipoPrograma"
SELECT 
'ConsolidacionCli' as "IdTipoPrograma",
'Consolidacion info clientes para llenado de tabla historica' as "Descripcion",
1 as "MaximoIntentos",
'Normativa' as "Modulo",
'cl.bcs.normativa.jobs.BcsJobConsolidaCli' as "NombreClasePrograma",
'JAVA' as "TipoClasePrograma",
'' as "Nodo"
from dummy;
 

--Insert Tipo Programa Exporta


INSERT INTO "BSCL"."BSCL.Plataforma.BD::BAT.TipoPrograma"
SELECT 
'ExportaNCG380' as "IdTipoPrograma", --cambiar por 'ExportaNor' as "IdTipoPrograma",
'Exporta datos NCG380' as "Descripcion", --cambiar por  'Exporta datos solicitud normativa' as "Descripcion",
1 as "MaximoIntentos",
'Normativa' as "Modulo",
'cl.bcs.normativa.jobs.BcsJobExportacionServ' as "NombreClasePrograma", 
'JAVA' as "TipoClasePrograma",
'' as "Nodo"
from dummy;
  
--Insert Tipo Programa CIRCULAR 1064


INSERT INTO "BSCL"."BSCL.Plataforma.BD::BAT.TipoPrograma"
SELECT 
'GeneraCIR1064' as "IdTipoPrograma", --cambiar por 'ExportaNor' as "IdTipoPrograma",
'Genera CIRCULAR 1064' as "Descripcion", --cambiar por  'Exporta datos solicitud normativa' as "Descripcion",
1 as "MaximoIntentos",
'Normativa' as "Modulo",
'cl.bcs.normativa.jobs.BcsJobExportacionServ' as "NombreClasePrograma", 
'JAVA' as "TipoClasePrograma",
'' as "Nodo"
from dummy;


INSERT INTO "BSCL"."BSCL.Plataforma.BD::BAT.TipoPrograma"
SELECT 
'GeneraCIR1064R' as "IdTipoPrograma", --cambiar por 'ExportaNor' as "IdTipoPrograma",
'Genera CIRCULAR 1064 Rueda' as "Descripcion", --cambiar por  'Exporta datos solicitud normativa' as "Descripcion",
1 as "MaximoIntentos",
'Normativa' as "Modulo",
'cl.bcs.normativa.jobs.BcsJobExportacionServ' as "NombreClasePrograma", 
'JAVA' as "TipoClasePrograma",
'' as "Nodo"
from dummy;


INSERT INTO "BSCL"."BSCL.Plataforma.BD::BAT.TipoPrograma"
SELECT 
'GeneraCIR1064FR' as "IdTipoPrograma", --cambiar por 'ExportaNor' as "IdTipoPrograma",
'Genera CIRCULAR 1064 Fuera Rueda' as "Descripcion", --cambiar por  'Exporta datos solicitud normativa' as "Descripcion",
1 as "MaximoIntentos",
'Normativa' as "Modulo",
'cl.bcs.normativa.jobs.BcsJobExportacionServ' as "NombreClasePrograma", 
'JAVA' as "TipoClasePrograma",
'' as "Nodo"
from dummy;


insert into "BSCL.Plataforma.BD::BAT.TipoPrograma" ("IdTipoPrograma", "Descripcion", "MaximoIntentos", "Modulo", "NombreClasePrograma", "TipoClasePrograma", "Nodo")
values('DTE', 'Envia comprobantes a DTE', 1, 'Facturacion', 'cl.bcs.plataforma.jobs.dte.Dte', 'JAVA', 'A');

--******************************************************

--AGENDAS
SET SCHEMA "BSCL";
DROP TABLE #param;
create local temporary table #param ("NombreParametro" nvarchar(100) , "Valor" nvarchar(100));
insert into #param values('IdEmpresa','1');
CALL "BSCL"."BSCL.Plataforma.BD.Servicios::BAT.AgregarAgenda"
(
1,
3,
'PLA',
                            
'VENCIMIENTOCLAVES',
'N',   
'**',
'*',
'**',
'09',
'00',
'S',
'N',
'N',
'S', 
#param,
                  
?,
?,
?,
?,
?,
?);

DROP TABLE #param;
create local temporary table #param ("NombreParametro" nvarchar(100) , "Valor" nvarchar(100));
insert into #param values('IdEmpresa','1');

CALL "BSCL"."BSCL.Plataforma.BD.Servicios::BAT.AgregarAgenda"
(
1,
0,
'FAC',
                            
'RESUMEN_FACTURA_CLI',
'N',   
'**',
'*',
'**',
'10',
'00',
'S',
'N',
'N',
'S', 
#param,
                  
?,
?,
?,
?,
?,
?);



---AGENDA : ESTADISTICA OPERACION CLIENTE -Cflores
DELETE FROM "BSCL.Plataforma.BD::BAT.TipoPrograma" WHERE "IdTipoPrograma"='ESTADISTICAOPERACIONCLIENTE' AND "Modulo"= 'Normativa';

INSERT INTO "BSCL"."BSCL.Plataforma.BD::BAT.TipoPrograma" ("IdTipoPrograma", "Descripcion", "MaximoIntentos", "Modulo", "NombreClasePrograma","TipoClasePrograma", "EsPauta")
VALUES('ESTADISTICAOPERACIONCLIENTE', 'Proceso estadistica de operaciones por Cliente - SOI', 1, 'Normativa', 'cl.bcs.normativa.jobs.BcsJobProcesarServ', 'JAVA', 'N');
	 

--AGENDA
		delete from "BSCL.Plataforma.BD::BAT.ParametrosAgenda" 
		where "IdAgenda" in (select "IdAgenda" from "BSCL.Plataforma.BD::BAT.Agenda" where "IdTipoPrograma"='ESTADISTICAOPERACIONCLIENTE');
		delete from "BSCL.Plataforma.BD::BAT.Agenda" where "IdTipoPrograma"='ESTADISTICAOPERACIONCLIENTE';
		
		drop table #param;
		create local temporary table #param ("NombreParametro" nvarchar(100) , "Valor" nvarchar(100));
			insert into #param values('IdEmpresa','1');
			insert into #param values('IdTipoTarea','ESTADISTICAOPERACIONCLIENTE');
			insert into #param values('IdSolicitudCircular','0');
		call "BSCL"."BSCL.Plataforma.BD.Servicios::BAT.AgregarAgenda"
		(
			p_IdEmpresa			 		=> 1,
			p_IdUsuario 				=> 0,
			p_IdModulo 					=>'NOR',                         
			p_IdTipoTarea 				=> 'ESTADISTICAOPERACIONCLIENTE',
			p_EsDemonio 				=> 'N',   
			p_DiaDeMes 					=> '**',
			p_DiaDeSemana 				=> '*',
			p_Mes 						=> '**',
			p_Hora 						=> '10',--'**',
			p_Minuto 					=> '30',
			p_SoloDiasHabiales 			=> 'S',
			p_SoloPrincipioMes 			=> 'N',
			p_SoloFinDeMes 				=> 'N',
			p_habilitado 				=> 'S', 
			p_TablaParametros 			=> #param,                
			p_IdAgenda 					=> ?,
			p_IdPrograma 				=> ?,
			p_out_AdvertenciasContador 	=> ?,
			p_out_AdvertenciasDetalle 	=> ?,
			p_out_ErroresContador 		=> ?,
			p_out_ErroresDetalle 		=> ?
		);	 
	 

--AGENDA Actualizacion parametros financieros y paridades tipo cambio
DROP TABLE #param;
create local temporary table #param ("NombreParametro" nvarchar(100) , "Valor" nvarchar(100));
insert into #param values('IdEmpresa','1');

CALL "BSCL"."BSCL.Plataforma.BD.Servicios::BAT.AgregarAgenda"
(
1,
3,
'MER',
                            
'UPD_PARAMS_FINANCIEROS_OPEN',
'N',   
'**',
'*',
'**',
'10',
'**',
'S',
'N',
'N',
'S', 
#param,
                  
?,
?,
?,
?,
?,
?);
CALL "BSCL"."BSCL.Plataforma.BD.Servicios::BAT.AgregarAgenda"
(
1,
3,
'MER',
                            
'UPD_PARAMS_FINANCIEROS_CLOSE',
'N',   
'**',
'*',
'**',
'18',
'**',
'S',
'N',
'N',
'S', 
#param,
                  
?,
?,
?,
?,
?,
?);

commit;

