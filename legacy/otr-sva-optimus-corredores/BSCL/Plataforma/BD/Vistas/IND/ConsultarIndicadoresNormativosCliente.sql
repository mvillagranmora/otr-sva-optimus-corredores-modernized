SET SCHEMA "BSCL";

DROP VIEW "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresNormativosCliente";

CREATE VIEW "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresNormativosCliente" AS
SELECT
	CIC."IdIndicador" AS "IdIndicador"
	,CIP."CodigoTipoIndicador" AS "CodigoTipoIndicadorPersona"
	,CIC."CodigoTipoIndicador" AS "CodigoTipoIndicadorCliente"
	,CIP."IdPersona"
	,CIC."IdCliente"
	,CIP."IdTipoIndicador" AS "IdTipoIndicadorPersona"
	,CIC."IdTipoIndicador" AS "IdTipoIndicadorCliente"
	,CIP."ValorIndicador" AS "ValorIndicadorPersona"
	,CIC."ValorIndicador" AS "ValorIndicadorCliente"
	,CIC."NombreTipoIndicador"
	,CIC."ClaseDato"
	FROM "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente" CIC
	INNER JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPersona" CIP
		ON CIC."IdPersona" = CIP."IdPersona" AND CIC."NombreTipoIndicador" = CIP."NombreTipoIndicador"
	WHERE
		CIC."GrupoIndicador" = 'NORMATIVOS';
		
GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresNormativosCliente" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 