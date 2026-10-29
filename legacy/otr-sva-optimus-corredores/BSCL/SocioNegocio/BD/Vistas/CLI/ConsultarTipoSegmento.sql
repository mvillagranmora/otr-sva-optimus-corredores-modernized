SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarTipoSegmento"; 
 
CREATE VIEW "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarTipoSegmento"  AS 

SELECT * 
FROM  "BSCL.SocioNegocio.BD::CLI.TipoSegmento"
WHERE "Borrado"='N';

GRANT SELECT ON "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarTipoSegmento" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
