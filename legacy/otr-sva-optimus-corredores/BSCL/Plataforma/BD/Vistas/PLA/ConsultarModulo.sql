SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Plataforma.BD.Vistas::PLA.ConsultarModulo"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::PLA.ConsultarModulo" AS
SELECT "IdModulo",
       "Codigo",
       "Nombre",
       "IdEstado"
FROM "BSCL.Plataforma.BD::PLA.Modulo";

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::PLA.ConsultarModulo" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
