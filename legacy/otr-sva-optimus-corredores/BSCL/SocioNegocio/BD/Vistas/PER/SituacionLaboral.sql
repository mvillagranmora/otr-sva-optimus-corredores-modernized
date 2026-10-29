SET SCHEMA "BSCL";
DROP VIEW "BSCL.SocioNegocio.BD.Vistas::PER.SituacionLaboral"; 
CREATE VIEW "BSCL.SocioNegocio.BD.Vistas::PER.SituacionLaboral"
AS
SELECT 
    SL."IdSituacionLaboral", 
    SL."CodigoSituacionLaboral", 
    SL."SituacionLaboral", 
    SL."Descripcion"
FROM "BSCL.SocioNegocio.BD::PER.SituacionLaboral" AS SL;
GRANT SELECT ON "BSCL.SocioNegocio.BD.Vistas::PER.SituacionLaboral" TO BCS_DESARROLLADOR WITH GRANT OPTION;