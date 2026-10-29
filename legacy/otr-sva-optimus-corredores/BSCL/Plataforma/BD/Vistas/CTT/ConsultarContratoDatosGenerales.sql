SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Plataforma.BD.Vistas::CTT.ConsultarContratoDatosGenerales";
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::CTT.ConsultarContratoDatosGenerales"  
AS
select
"IdEmpresa",
"RazonSocial",
"RUT",
"Direccion",
"Telefono",
"Email",
"Fax",
"Apoderado1",
"DocumentoApoderado1",
"Apoderado2",
"DocumentoApoderado2",
"IdPersonaApoderado1",
"IdPersonaApoderado2"
from "BSCL.Plataforma.BD::CTT.ContratoDatosGenerales";

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::CTT.ConsultarContratoDatosGenerales" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
