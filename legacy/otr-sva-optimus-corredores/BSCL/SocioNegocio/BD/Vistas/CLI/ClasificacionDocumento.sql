SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.SocioNegocio.BD.Vistas::CLI.ClasificacionDocumento"; 
 		
CREATE VIEW "BSCL.SocioNegocio.BD.Vistas::CLI.ClasificacionDocumento" AS
SELECT 
TPO."Glosa",
DOC."IdCliente"
from 
"BSCL.Plataforma.BD::DOC.Anexo" ANX,
"BSCL.Plataforma.BD::DOC.Documento" DOC,
"BSCL.Plataforma.BD::DOC.TipoDocumento" TPO
WHERE 
ANX."IdDocumento" = DOC."IdDocumento" AND 
(ANX."FechaVigencia" >= CURRENT_UTCTIMESTAMP OR ANX."FechaVigencia" = '0001-01-01')
AND TPO."IdTipoDocumento" = DOC."IdTipoDocumento"
GROUP BY TPO."Glosa", DOC."IdCliente";

GRANT SELECT ON "BSCL.SocioNegocio.BD.Vistas::CLI.ClasificacionDocumento" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 