SET SCHEMA "BSCL";

DROP VIEW  "BSCL.Normativa.BD.Vistas::DOC.ConsultarHomologacion";

CREATE VIEW "BSCL.Normativa.BD.Vistas::DOC.ConsultarHomologacion"  
(
	"Tipo",
	"Descripcion",
	"ValorOrigen",
	"ValorDestino",
	"Esquema",
	"Version",
	"FechaDatosDesde",
	"FechaDatosHasta" 
)
AS

	SELECT HOM."Tipo",HOM."Descripcion",HOM."ValorOrigen",HOM."ValorDestino",HOM."Esquema",HOM."Version",ESQ."FechaDatosDesde",ESQ."FechaDatosHasta" 
		FROM   "BSCL.Normativa.BD::DOC.Homologacion" HOM
		INNER JOIN "BSCL.Normativa.BD::DOC.Esquema" ESQ
		ON (ESQ."Esquema"=HOM."Esquema" AND ESQ."Version"=HOM."Version" );

GRANT SELECT ON "BSCL.Normativa.BD.Vistas::DOC.ConsultarHomologacion" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
