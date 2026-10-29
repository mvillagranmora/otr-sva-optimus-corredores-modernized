SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Plataforma.BD.Vistas::EVT.ConsultarFormaDespacho"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::EVT.ConsultarFormaDespacho"  
AS
select 
 "IdFormaDespacho",
 "CodigoFormaDespacho",
 "Nombre"
from "BSCL.Plataforma.BD::EVT.FormaDespacho";

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::EVT.ConsultarFormaDespacho" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 

