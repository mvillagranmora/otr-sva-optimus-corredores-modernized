SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Plataforma.BD.Vistas::EVT.ConsultarTiempoDespacho"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::EVT.ConsultarTiempoDespacho"  
AS
select 
 "IdTiempoDespacho",
 "CodigoTiempoDespacho",
 "Nombre"
from "BSCL.Plataforma.BD::EVT.TiempoDespacho";


GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::EVT.ConsultarTiempoDespacho" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 

