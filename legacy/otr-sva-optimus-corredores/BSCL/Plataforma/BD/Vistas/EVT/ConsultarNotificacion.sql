SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Plataforma.BD.Vistas::EVT.ConsultarNotificacion"; 

CREATE VIEW "BSCL.Plataforma.BD.Vistas::EVT.ConsultarNotificacion"  
AS
select distinct
cfe."IdEmpresa",
"IdNotificacion",
cnotif."IdConfiguracionEvento",
cnotif."FechaCreacion",
cfe."FormaDespacho" as "FormaDespachoSrv",
cfe."TiempoDespacho" as "TiempoDespachoSrv",
CASE cfe."FormaDespacho" WHEN 'U' THEN 'UI_UI' WHEN 'C' THEN 'UI_CORREO' WHEN 'T' THEN 'UI_AMBOS' END as "FormaDespacho" ,
CASE cfe."TiempoDespacho" WHEN 'M' THEN 'UI_MANUAL' WHEN 'H' THEN 'UI_HORA' WHEN 'I' THEN 'UI_INMEDIATO' END as "TiempoDespacho",
"FechaDespachoOrginal",
"FechaDespachoRecordatorio",
"Enviado" as "EnviadoSrv",
"Enviar" as "EnviarSrv",
CASE "Enviar" WHEN 'S' THEN 'UI_SI' WHEN 'N' THEN 'UI_NO' END as "Enviar",
CASE "Enviado" WHEN 'S' THEN 'UI_SI' WHEN 'N' THEN 'UI_NO' END as "Enviado",
"FechaEnviado",
"IdUsuarioEnviado",
"ReEnviar" as "ReEnviarSrv",
CASE "ReEnviar" WHEN 'S' THEN 'UI_SI' WHEN 'N' THEN 'UI_NO' END as "ReEnviar",
CASE "ReEnviado" WHEN 'S' THEN 'UI_SI' WHEN 'N' THEN 'UI_NO' END as "ReEnviado",
"FechaReEnviado",
"IdUsuarioReEnviado",
CASE cfe."ConfirmacionDespacho" WHEN 'S' THEN 'UI_SI' WHEN 'N' THEN 'UI_NO' END as "ConfirmacionDespacho" ,
"TokenConfirmacion",
CASE "Confirmado" WHEN 'S' THEN 'UI_SI' WHEN 'N' THEN 'UI_NO' END as "Confirmado",
"FechaConfirmado",
CASE "AccionConfirmada" 
	WHEN 'PEN' THEN 'UI_PENDIENTE'	
	WHEN 'RZD' THEN 'UI_RECHAZADA' 
	WHEN 'APD' THEN 'UI_APROBADA' 
	WHEN 'VCD' THEN 'UI_VENCIDA'
END as "AccionConfirmada",
"IdUsuarioConfimado",
cfe."FechaVencimiento",
cfe."FechaRecordatorio",
CASE "Observacion" WHEN 'PEN' THEN 'UI_PENDIENTE' END as "Observacion",
CASE "Descartado" WHEN 'S' THEN 'UI_SI' WHEN 'N' THEN 'UI_NO' END as "Descartado",
"IdUsuarioDescarte",
"FechaDescarte",
"Leida",
"DireccionAlternativa",
"Descripcion",
"RecordatorioEnviado",
"RecordarDespacho"
from "BSCL.Plataforma.BD::EVT.Notificacion" cnotif
inner join "BSCL.Plataforma.BD::EVT.ConfiguracionEvento" cfe On cfe."IdConfiguracionEvento" = cnotif."IdConfiguracionEvento"; 

 
GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::EVT.ConsultarNotificacion" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 


