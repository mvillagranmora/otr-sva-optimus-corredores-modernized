SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.Plataforma.BD.Vistas::INS.ConsultarTipoRelacionInstrumento"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::INS.ConsultarTipoRelacionInstrumento"  
AS
SELECT 
	"IdTipoRelacionInstrumento",
	"TipoRelacionInstrumento",
	"IdEstado"
FROM "BSCL.Plataforma.BD::INS.TipoRelacionInstrumento";

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::INS.ConsultarTipoRelacionInstrumento" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
