SET SCHEMA "BSCL"; 
GO

DROP VIEW "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumentoEliminar"; 
GO
CREATE VIEW "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumentoEliminar"  
AS
SELECT ARB."IdRelacionClasificacion", 
	   ARB."IdArbolClasificacionActivo",	
	   ARB."IdTipoInstrumento", 
	   ARB."IdInstrumento",
	   TINS."Nemotecnico"
FROM "BSCL.Plataforma.BD::INS.RelacionClasificacionActivos" ARB 
INNER JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS TINS ON TINS."IdInstrumento" = ARB."IdInstrumento"	
GO

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumentoEliminar" TO  BCS_DESARROLLADOR WITH GRANT OPTION;