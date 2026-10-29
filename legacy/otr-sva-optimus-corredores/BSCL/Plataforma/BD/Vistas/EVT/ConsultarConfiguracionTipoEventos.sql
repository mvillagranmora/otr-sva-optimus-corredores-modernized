SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Plataforma.BD.Vistas::EVT.ConsultarConfiguracionTipoEventos"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::EVT.ConsultarConfiguracionTipoEventos"  
AS
select DISTINCT
CUTE."IdConfiguracionTipoEvento",
"IdEmpresa",
"CodigoTipoEvento",
"NotificaTodosUsuarios"
 from "BSCL.Plataforma.BD::EVT.ConfiguracionTipoEvento" ctevt
 INNER JOIN "BSCL.Plataforma.BD::EVT.ConfiguracionUsuarioTipoEvento" cute
 ON cute."IdConfiguracionTipoEvento" = ctevt."IdConfiguracionTipoEvento";


GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::EVT.ConsultarConfiguracionTipoEventos" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 


 