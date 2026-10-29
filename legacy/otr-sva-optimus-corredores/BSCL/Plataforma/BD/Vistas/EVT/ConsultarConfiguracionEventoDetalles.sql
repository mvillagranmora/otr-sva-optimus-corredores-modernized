SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Plataforma.BD.Vistas::EVT.ConsultarConfiguracionEventoDetalles"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::EVT.ConsultarConfiguracionEventoDetalles"  
AS
select 
"IdConfiguracionEventoDetalle",
"IdConfiguracionEvento",
"IdUsuario",
"FechaCreacion",
"CodigoTipoEvento",
"IdEstado"
 from "BSCL.Plataforma.BD::EVT.ConfiguracionEventoDetalle";


GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::EVT.ConsultarConfiguracionEventoDetalles" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 


 