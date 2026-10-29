SET SCHEMA "BSCL";
DROP VIEW "BSCL.CuentaInversion.BD.Vistas::ConsultarIndicadoresPortafolioCuentaInversion";

CREATE VIEW "BSCL.CuentaInversion.BD.Vistas::ConsultarIndicadoresPortafolioCuentaInversion" AS
SELECT	"IdPortafolio"
		,MAX(CASE WHEN "IdUso" = 'PRE' AND "CodigoTipoIndicador" = 'GEN_SOBCTA' THEN "ValorIndicador" ELSE null END) AS "OperaLineaSobregiro"
		,MAX(CASE WHEN "IdUso" = 'PRE' AND "CodigoTipoIndicador" = 'GEN_OCCINV' THEN "ValorIndicador" ELSE null END) AS "OperaCuentaInversion"
FROM (

	SELECT "IdPortafolio","IdUso","CodigoTipoIndicador","IdIndicador","ValorIndicador"
	FROM "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPortafolio"
) 
GROUP BY "IdPortafolio"
WITH READ ONLY;

GRANT SELECT ON "BSCL.CuentaInversion.BD.Vistas::ConsultarIndicadoresPortafolioCuentaInversion" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
