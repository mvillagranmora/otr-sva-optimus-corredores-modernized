SET SCHEMA "BSCL";
DROP VIEW "BSCL.CuentaInversion.BD.Vistas::ConsultaConfiguracionCuentaInversion";


CREATE VIEW "BSCL.CuentaInversion.BD.Vistas::ConsultaConfiguracionCuentaInversion" AS
SELECT  "IdEmpresa",
		MAX(CASE WHEN "CodigoParametro" = 'PERMITEANULARMOVIMIENTO' THEN "Valor" ELSE 'N' END) AS "PERMITEANULARMOVIMIENTO",
		MAX(CASE WHEN "CodigoParametro" = 'ANULACIONREQUIEREAUTORIZACION' THEN "Valor" ELSE 'N' END) AS "ANULACIONREQUIEREAUTORIZACION",
		MAX(CASE WHEN "CodigoParametro" = 'LIQUIDASALDOGENERACOMPROBANTETESORERIA' THEN "Valor" ELSE 'N' END) AS "LIQUIDASALDOGENERACOMPROBANTETESORERIA"
  FROM (
		SELECT "CodigoParametro","IdEmpresa","Valor"
		  FROM "BSCL.CuentaInversion.BD::CTA.ConfiguracionCuentaInversion"
		)
 GROUP BY "IdEmpresa";
GRANT SELECT ON "BSCL.CuentaInversion.BD.Vistas::ConsultaConfiguracionCuentaInversion" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
