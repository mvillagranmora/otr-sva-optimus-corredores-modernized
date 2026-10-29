SET SCHEMA "BSCL"; 
GO 
DROP VIEW "BSCL.Plataforma.BD.Vistas::PLA.ConsultarUsoGlosaTipoInstrumento"; 
GO
CREATE VIEW "BSCL.Plataforma.BD.Vistas::PLA.ConsultarUsoGlosaTipoInstrumento"
 AS
SELECT
GLOSA."IdGlosa" ,
GLOSA."IdTipoInstrumento" ,
GLOSA."Prioridad" ,
COALESCE(DEFINICION."Nombre",'') AS "Nombre"
FROM "BSCL.Plataforma.BD::PLA.UsoGlosaTipoInstrumento" GLOSA 
LEFT JOIN "BSCL.Plataforma.BD::PLA.DefinicionGlosaTipoInstrumento" DEFINICION ON (GLOSA."IdGlosa" = DEFINICION."IdGlosaTipoInstrumento");
GO
GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::PLA.ConsultarUsoGlosaTipoInstrumento" TO  BCS_DESARROLLADOR WITH GRANT OPTION;