SET SCHEMA "BSCL";

DROP VIEW  "BSCL.Normativa.BD.Vistas::TRI.DJ1.Homologacion";

CREATE VIEW "BSCL.Normativa.BD.Vistas::TRI.DJ1.Homologacion"  
(
	"CodigoTipoIndicador",
	"Codigo",
	"Tipo",
	"ValorDestino",
	"Esquema",
	"Version",
	"FechaDatosDesde",
	"FechaDatosHasta" 
)
AS
	---CODIGO RELACION
	SELECT TREL."CodigoTipoIndicador",TREL."Codigo",HOM."Tipo",HOM."ValorDestino",HOM."Esquema",HOM."Version",ESQ."FechaDatosDesde",ESQ."FechaDatosHasta" 
		FROM  "BSCL.Plataforma.BD.Vistas::IND.ConsultarDetalleTipoIndicador" TREL
		LEFT OUTER JOIN "BSCL.Normativa.BD::DOC.Homologacion" HOM
		ON (TREL."Codigo"=HOM."ValorOrigen" AND HOM."Tipo"='DJ1Relacion' AND TREL."CodigoTipoIndicador"='DJ1')
		INNER JOIN "BSCL.Normativa.BD::DOC.Esquema" ESQ
		ON (ESQ."Esquema"=HOM."Esquema" AND ESQ."Version"=HOM."Version" );

GRANT SELECT ON "BSCL.Normativa.BD.Vistas::TRI.DJ1.Homologacion" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
