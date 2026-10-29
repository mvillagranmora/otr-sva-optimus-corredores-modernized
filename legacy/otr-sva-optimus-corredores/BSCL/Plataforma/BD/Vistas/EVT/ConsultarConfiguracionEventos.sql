SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Plataforma.BD.Vistas::EVT.ConsultarConfiguracionEventos"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::EVT.ConsultarConfiguracionEventos"  
AS
select
config."IdEmpresa",
config."IdConfiguracionEvento",
config."Descripcion",
config."IdUsuario",
config."FechaCreacion",
config."IdEstado",
MAP(config."IdEstado",'ACT','UI_ACT','UI_INA') as "IdEstadoUI",
config."FormaDespacho",
config."TiempoDespacho",
config."FechaDespacho",
MAP(config."PermitePersonalizar",'S','UI_SI','UI_NO') as "PermitePersonalizar",
forma."Nombre" as "NombreFormaDespacho",
tiempo."Nombre" as "NombreTiempoDespacho",
CASE forma."CodigoFormaDespacho" WHEN 'U' THEN 'UI_UI' WHEN 'C' THEN 'UI_CORREO' WHEN 'T' THEN 'UI_AMBOS' END  as "NombreFormaDespachoUI",
CASE tiempo."CodigoTiempoDespacho" WHEN 'M' THEN 'UI_MANUAL' WHEN 'H' THEN 'UI_HORA' WHEN 'I' THEN 'UI_INMEDIATO' END as "NombreTiempoDespachoUI",
config."ConfirmacionDespacho",
config."RecordarDespacho",
MAP(config."ConfirmacionDespacho",'S','UI_SI','UI_NO') as "ConfirmacionDespachoUI",
config."FechaVencimiento",
config."FechaRecordatorio",
"DiasVencimiento" as "FechaVencimientoDias",
"DiasRecordatorio" as "FechaRecordatorioDias",
config."IdPlantilla",
config."IdPlantillaRecordatorio",
config."Agrupa",
"NotificaTodosUsuarios",
"ExcluyeDestinatario",
"CopiarRemitente",
"IdUsuarioRol",
"CopiasAdicionales",
"DirEvt",
"ControlProceso"
from "BSCL.Plataforma.BD::EVT.ConfiguracionEvento" config
LEFT JOIN "BSCL.Plataforma.BD::EVT.FormaDespacho" forma ON forma."CodigoFormaDespacho" = config."FormaDespacho"
LEFT JOIN "BSCL.Plataforma.BD::EVT.TiempoDespacho" tiempo ON tiempo."CodigoTiempoDespacho" = config."TiempoDespacho"
LEFT JOIN "BSCL.Plataforma.BD::EVT.ConfiguracionEventoCodigoTipoEvento" cecte ON cecte."IdConfiguracionEvento" = config."IdConfiguracionEvento"
LEFT JOIN "BSCL.Plataforma.BD.Vistas::EVT.ConsultarTipoEventosAsociadosAConfiguracion" cteac ON cteac."IdConfiguracionEvento" = config."IdConfiguracionEvento";

 
GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::EVT.ConsultarConfiguracionEventos" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 

 