SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Mesa.BD.Vistas::ConsultarInstrumentoUI"; 
 
CREATE VIEW "BSCL.Mesa.BD.Vistas::ConsultarInstrumentoUI" AS
	SELECT	*,
			"IdTipoInstrumento" AS "IdTipoInstrumentoUI"
	FROM	"BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento"
	WHERE	"IdTipoInstrumento" IN ('SPT', 'FWD');	
GRANT SELECT ON "BSCL.Mesa.BD.Vistas::ConsultarInstrumentoUI" TO BCS_DESARROLLADOR WITH GRANT OPTION;

