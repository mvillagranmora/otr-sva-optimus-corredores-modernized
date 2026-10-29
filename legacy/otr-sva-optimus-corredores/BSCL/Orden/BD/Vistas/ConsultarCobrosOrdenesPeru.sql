SET SCHEMA "BSCL"; 
GO
DROP VIEW "BSCL.Orden.BD.Vistas::ConsultarCobrosOrdenesPeru";
GO 
CREATE VIEW "BSCL.Orden.BD.Vistas::ConsultarCobrosOrdenesPeru"
AS 
SELECT	"IdOrden",
		COALESCE(MAX(CASE WHEN "IdCobro" = 'IGV' THEN "MontoCobroCalculadoLiquidacion" END),0) AS "Impuesto",
		COALESCE(MAX(CASE WHEN "IdCobro" = 'COMISION BVL' THEN "MontoCobroCalculadoLiquidacion" END),0) AS "ComisBVL",
		COALESCE(MAX(CASE WHEN "IdCobro" = 'COMISION CAVALI' THEN "MontoCobroCalculadoLiquidacion" END),0) AS "ComisCavali",
		COALESCE(MAX(CASE WHEN "IdCobro" = 'FONDO GARANTIA' THEN "MontoCobroCalculadoLiquidacion" END),0) AS "ComisGarantia",
		COALESCE(MAX(CASE WHEN "IdCobro" = 'COMISION_SAB' THEN "MontoCobroCalculadoLiquidacion" END),0) AS "ComisSAB",
		COALESCE(MAX(CASE WHEN "IdCobro" = 'COMISION SMV' THEN "MontoCobroCalculadoLiquidacion" END),0) AS "ComisSMV",
		COALESCE(MAX(CASE WHEN "IdCobro" = 'COMISION_SAB' THEN "ValorCobroFinal" END),0) AS "PorcentajeComisSAB"
FROM (
	SELECT "IdOrden", "IdCobro", "MontoCobroCalculadoLiquidacion", "ValorCobroFinal"  
	FROM "BSCL.Orden.BD::ORD.CobroOrden" CO
	INNER JOIN "BSCL.Plataforma.BD::COM_2.DefinicionCobro" DC ON CO."IdCobro" = DC."CodigoDefinicionCobro"
) 
GROUP BY "IdOrden";
GO
GRANT SELECT ON "BSCL.Orden.BD.Vistas::ConsultarCobrosOrdenesPeru" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 