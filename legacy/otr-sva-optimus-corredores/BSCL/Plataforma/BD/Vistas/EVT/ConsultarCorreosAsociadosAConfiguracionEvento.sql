SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Plataforma.BD.Vistas::EVT.ConsultarCorreosAsociadosAConfiguracionEvento"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::EVT.ConsultarCorreosAsociadosAConfiguracionEvento"  
AS
select distinct
"IdConfiguracionEventoCopiasCorreo"       ,  
correo."IdConfiguracionEvento"                   ,   
"Correo"								  ,   
correo."IdUsuario"                         	  ,   
correo."FechaCreacion"                        
from "BSCL.Plataforma.BD::EVT.ConfiguracionEventoCopiasCorreo" correo
LEFT JOIN  "BSCL.Plataforma.BD::EVT.ConfiguracionEvento" config
ON correo."IdConfiguracionEvento" = config."IdConfiguracionEvento";


GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::EVT.ConsultarCorreosAsociadosAConfiguracionEvento" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 


 