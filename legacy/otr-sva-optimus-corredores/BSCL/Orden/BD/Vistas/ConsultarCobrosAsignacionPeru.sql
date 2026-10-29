SET SCHEMA "BSCL"; 
GO
DROP VIEW "BSCL.Orden.BD.Vistas::ConsultarCobrosAsignacionPeru";
GO 
CREATE VIEW "BSCL.Orden.BD.Vistas::ConsultarCobrosAsignacionPeru"
AS 
SELECT	"IdAsignacion",
		COALESCE(MAX(CASE WHEN "IdCobro" = 'IGV' THEN "MontoCobroCalculadoLiquidacion" END),0) AS "Impuesto",
		COALESCE(MAX(CASE WHEN "IdCobro" = 'COMISION BVL' THEN "MontoCobroCalculadoLiquidacion" END),0) AS "ComisBVL",
		COALESCE(MAX(CASE WHEN "IdCobro" = 'COMISION CAVALI' THEN "MontoCobroCalculadoLiquidacion" END),0) AS "ComisCavali",
		COALESCE(MAX(CASE WHEN "IdCobro" = 'FONDO GARANTIA' THEN "MontoCobroCalculadoLiquidacion" END),0) AS "ComisGarantia",
		COALESCE(MAX(CASE WHEN "IdCobro" = 'COMISION_SAB' THEN "MontoCobroCalculadoLiquidacion" END),0) AS "ComisSAB",
		COALESCE(MAX(CASE WHEN "IdCobro" = 'COMISION SMV' THEN "MontoCobroCalculadoLiquidacion" END),0) AS "ComisSMV",
		COALESCE(MAX(CASE WHEN "IdCobro" = 'COMISION_SAB' THEN "ValorCobroFinal" END),0) AS "PorcentajeComisSAB"
FROM (
	SELECT "IdAsignacion", "IdCobro", "MontoCobroCalculadoLiquidacion", "ValorCobroFinal"  
	FROM "BSCL.Orden.BD::ORD.CobroAsignacion" CA
	INNER JOIN "BSCL.Plataforma.BD::COM_2.DefinicionCobro" DC ON CA."IdCobro" = DC."CodigoDefinicionCobro"
) 
GROUP BY "IdAsignacion";
GO
GRANT SELECT ON "BSCL.Orden.BD.Vistas::ConsultarCobrosAsignacionPeru" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 