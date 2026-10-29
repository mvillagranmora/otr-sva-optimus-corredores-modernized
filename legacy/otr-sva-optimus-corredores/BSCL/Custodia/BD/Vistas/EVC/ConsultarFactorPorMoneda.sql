SET SCHEMA "BSCL"; 
GO 
DROP VIEW "BSCL.Custodia.BD.Vistas::EVC.ConsultarFactorPorMoneda"; 
GO
CREATE VIEW "BSCL.Custodia.BD.Vistas::EVC.ConsultarFactorPorMoneda" AS 

WITH VCL AS (
	SELECT
		RANK() OVER ( PARTITION BY "Moneda", "Secuencia", "Nemo" ORDER BY "HoraAuditoria" DESC) AS "Prioridad",
		"HoraAuditoria", 
		"Secuencia",
		CASE 
			WHEN "Moneda" = '$' THEN 'CLP' 
			WHEN "Moneda" = 'US$' THEN 'USD' 
			ELSE 'CLP' 
		END AS "IdMoneda",
		"Nemo",
		"ValorAccion"
	FROM "BSCL.Custodia.BD::CUS.VariacionCapitalLog"
) 
SELECT 
	VCL."Prioridad",
	VCL."HoraAuditoria",
	VCL."Secuencia",
	VCL."IdMoneda",
	VCL."Nemo" AS "Nemotecnico",
	VCL."ValorAccion" AS "Factor"
FROM VCL
WHERE VCL."Prioridad" = 1

GO	 	
GRANT SELECT ON "BSCL.Custodia.BD.Vistas::EVC.ConsultarPreferenciaPagoEventoCapital" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
GO

