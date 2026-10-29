SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Plataforma.BD.Vistas::TRA.ConsultarTipoInstrumentoTransaccion"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::TRA.ConsultarTipoInstrumentoTransaccion"
AS

SELECT
	TITR."IdTipoInstrumento",
	TINS."Nombre" as "NombreTipoInstrumento",
	TITR."IdEstado"
FROM 
	"BSCL.Plataforma.BD::TRA.TipoInstrumentoTransaccion" as TITR 
	INNER JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" TINS
		ON (TINS."IdTipoInstrumento" = TITR."IdTipoInstrumento") ;

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::TRA.ConsultarTipoInstrumentoTransaccion" TO BCS_DESARROLLADOR WITH GRANT OPTION; 
