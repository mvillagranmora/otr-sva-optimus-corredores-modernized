SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Plataforma.BD.Vistas::EVT.ConsultarConfiguracionUsuarioTipoEvento"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::EVT.ConsultarConfiguracionUsuarioTipoEvento"  
AS
select 
"IdConfiguracionUsuarioTipoEvento",
"IdConfiguracionTipoEvento",
"TipoUsuario",
"IdUsuarioRol"
 from "BSCL.Plataforma.BD::EVT.ConfiguracionUsuarioTipoEvento";


GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::EVT.ConsultarConfiguracionUsuarioTipoEvento" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 


 