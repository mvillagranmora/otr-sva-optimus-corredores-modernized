SET SCHEMA "BSCL";
GO
DROP VIEW "BSCL.CuentaInversion.BD.Vistas::ConsultarDetalleReporteAcreedores"; 
GO 
CREATE VIEW "BSCL.CuentaInversion.BD.Vistas::ConsultarDetalleReporteAcreedores" 
AS
SELECT 
	PARAM."IdSecuencia" 							AS "IdDetalleReporte"
	,PRO."IdPrograma" 				
	,REPLACE(PARAM."NombreParametro", 'P_', '')		AS "NombreParametro"
	,CASE 
		WHEN PARAM."Valor" = 'N' THEN 'NO'
		WHEN PARAM."Valor" = 'S' THEN 'SI'
		ELSE PARAM."Valor"
	END "Valor"
FROM "BSCL.Plataforma.BD::BAT.ParametrosPrograma" PARAM
INNER JOIN "BSCL.Plataforma.BD::BAT.Programa" PRO
	ON PARAM."IdPrograma" = PRO."IdPrograma" 
WHERE PRO."IdTipoPrograma" = 'GenerarReporteAcreedores'
AND PARAM."NombreParametro" LIKE_REGEXPR 'P_';
GO
GRANT SELECT ON "BSCL.CuentaInversion.BD.Vistas::ConsultarDetalleReporteAcreedores" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 