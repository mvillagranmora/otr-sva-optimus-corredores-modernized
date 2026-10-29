SET SCHEMA "BSCL"; 
GO 
DROP VIEW "BSCL.Plataforma.BD.Vistas::PLA.ConsultarDefinicionGlosaTipoInstrumento"; 
GO
CREATE VIEW "BSCL.Plataforma.BD.Vistas::PLA.ConsultarDefinicionGlosaTipoInstrumento" 
AS
SELECT
"IdGlosaTipoInstrumento",
"Nombre",
"Descripcion"
FROM "BSCL.Plataforma.BD::PLA.DefinicionGlosaTipoInstrumento";
GO
GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::PLA.ConsultarDefinicionGlosaTipoInstrumento" TO  BCS_DESARROLLADOR WITH GRANT OPTION;