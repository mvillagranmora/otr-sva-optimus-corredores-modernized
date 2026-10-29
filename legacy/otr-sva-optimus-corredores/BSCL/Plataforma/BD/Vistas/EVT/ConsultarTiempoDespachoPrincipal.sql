SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Plataforma.BD.Vistas::EVT.ConsultarTiempoDespachoPrincipal"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::EVT.ConsultarTiempoDespachoPrincipal"  
AS

 
select 
 "IdTiempoDespacho",
 "CodigoTiempoDespacho",
 "Nombre"
from "BSCL.Plataforma.BD::EVT.TiempoDespacho";


GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::EVT.ConsultarTiempoDespachoPrincipal" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 

