SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Plataforma.BD.Vistas::AUT.ConsultarTipoAutorizacion";

CREATE VIEW "BSCL.Plataforma.BD.Vistas::AUT.ConsultarTipoAutorizacion" as

SELECT *
FROM "BSCL.Plataforma.BD::AUT.TipoAutorizacion";


GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::AUT.ConsultarTipoAutorizacion" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
 