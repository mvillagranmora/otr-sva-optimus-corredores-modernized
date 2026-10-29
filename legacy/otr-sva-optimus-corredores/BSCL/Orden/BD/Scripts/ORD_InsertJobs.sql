
 INSERT INTO  "BSCL"."BSCL.Plataforma.BD::BAT.TipoPrograma"
select 'ControlMercado','Control de Mensajes de Mercado',1,'Orden','cl.bcs.ordenes.jobs.ControlMercadoJob','JAVA','D','N'
 from dummy 

 
INSERT INTO  "BSCL"."BSCL.Plataforma.BD::BAT.TipoPrograma"
select 'EXTRACTOR_ARCHIVOS','Extractor de Archivos',1,'Plataforma','cl.bcs.plataforma.jobs.ExtractorArchivosJob','JAVA','','N'
 from dummy 


 

INSERT INTO  "BSCL"."BSCL.Plataforma.BD::BAT.TipoPrograma"
select 'PROCESADOR_ARCHIVOS_RUEBAS','Procesador Archivos Ruebas',1,'Plataforma','cl.bcs.plataforma.jobs.ProcesadorArchivosJob','JAVA','','N'
 from dummy 


INSERT INTO  "BSCL"."BSCL.Plataforma.BD::BAT.TipoPrograma"
select 'ProcesamientoAutomaticoTransacciones','Asignacion y Facturacion Automatico de Transacciones',1,'Ordenes','cl.bcs.ordenes.jobs.ProcesadorTransaccionesJob','JAVA','','N'
 from dummy 
