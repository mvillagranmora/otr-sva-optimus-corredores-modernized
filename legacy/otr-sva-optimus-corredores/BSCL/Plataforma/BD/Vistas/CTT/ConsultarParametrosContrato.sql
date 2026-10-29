SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Plataforma.BD.Vistas::CTT.ConsultarParametrosContrato";
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::CTT.ConsultarParametrosContrato"  
AS
select "IdParametroContrato",
"CodigoArchivo",
"Nombre",
"Tipo",
"Descripcion",
"IdentificaPersona"
from "BSCL.Plataforma.BD::CTT.ParametroContratos";

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::CTT.ConsultarParametrosContrato" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
