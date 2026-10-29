SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Portafolio.BD.Vistas::ConsultarTipoAutorizado"; 
 
CREATE VIEW "BSCL.Portafolio.BD.Vistas::ConsultarTipoAutorizado" AS 
SELECT 
"IdEmpresa",
"IdTipoAutorizado",
"NombreTipoAutorizado",
"IdTipoRelacion"
FROM "BSCL.Portafolio.BD::POR.TipoAutorizado";


GRANT SELECT ON "BSCL.Portafolio.BD.Vistas::ConsultarTipoAutorizado" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
