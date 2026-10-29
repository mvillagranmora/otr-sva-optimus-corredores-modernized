SET SCHEMA "BSCL";
GO

DROP PROCEDURE "BSCL.Custodia.BD.Servicios::PRT.CalcularPremiosAnticipo";
GO

CREATE PROCEDURE "BSCL.Custodia.BD.Servicios::PRT.CalcularPremiosAnticipo"(
    --VARIABLES DE ENTRADA OBLIGATORIAS.
    IN p_IdEmpresa					BIGINT,
    IN p_IdUsuario					BIGINT,
    IN p_IdModulo					NVARCHAR(3),
	IN p_IdPais						BIGINT,
 	IN p_FolioPrestamo 				BIGINT,
 	IN p_FechaAnticipo 				DATE,
	IN p_Precio						DECIMAL(26,8) DEFAULT null,
	IN p_CantidadAnticipo			DECIMAL(26,8) DEFAULT null,
	
	OUT p_out_TotalPrima 			DECIMAL(26,8),
	OUT P_TBLDETALLE 				TABLE ("FolioEmprestito" BIGINT, "ValorPremio" DECIMAL(26,8), "TasaPremio" DECIMAL(26,8), "Plazo" BIGINT)

)
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER
DEFAULT SCHEMA BSCL
AS
BEGIN
	--------------------
	--DECLARE Manejo de Errores
	--------------------	

	DECLARE SALIDA_CONTROLADA CONDITION;
	DECLARE v_MontoDecimales BIGINT := 0;
	DECLARE v_UsaFechaLiquidacionEnVencimiento NVARCHAR(5);

	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA	 
	BEGIN
		SELECT 0 INTO p_out_TotalPrima FROM DUMMY; 
		SELECT 0 AS "FolioEmprestito", 0 AS "ValorPremio", 0 AS "TasaPremio" FROM dummy WHERE 1=2;
	END;


	BEGIN
		SELECT prm."Valor" INTO v_UsaFechaLiquidacionEnVencimiento
		FROM "BSCL.Plataforma.BD::PLA.Configuracion" prm
		WHERE prm."CodigoParametro" = 'CUS.UsaFechaLiquidacionEnVencimiento';
		
		v_tblConsPlazo =
			SELECT
				  "FolioEmprestito" AS "IdInterno"
				, CASE WHEN v_UsaFechaLiquidacionEnVencimiento = 'S' AND "FechaLiquidacion" > "FechaEmprestito" THEN "FechaLiquidacion" ELSE "FechaEmprestito" END AS "FechaDesde"
				, CASE WHEN p_FechaAnticipo < (CASE WHEN v_UsaFechaLiquidacionEnVencimiento = 'S' AND "FechaLiquidacion" > "FechaEmprestito" THEN "FechaLiquidacion" ELSE "FechaEmprestito" END)
					THEN CASE WHEN v_UsaFechaLiquidacionEnVencimiento = 'S' AND "FechaLiquidacion" > "FechaEmprestito" THEN "FechaLiquidacion" ELSE "FechaEmprestito" END
					ELSE p_FechaAnticipo 
				  END AS "FechaHasta"
				, p_IdPais AS "IdPais"
				, "EsDiaHabilPlazoPrestamo" AS "SoloDiasHabiles"
			FROM "BSCL.Custodia.BD.Vistas::PRT.ConsultarResumenEmprestitoVigente"
			WHERE "FolioPrestamo" = p_FolioPrestamo;
						
		CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerPlazoMasivo" (
			p_tbl 			=> :v_tblConsPlazo,
			p_out_Plazos 	=> :v_tblRptaPlazo);
			
		v_tblResumenEmprestitoVigente = 
		SELECT "INGCantidad", "INGMontoCompraActualMonedaInstrumento", "INGTasaEmprestito", "FolioEmprestito", "IdMonedaSubyacente"
		FROM "BSCL.Custodia.BD.Vistas::PRT.ConsultarResumenEmprestitoVigente" emp
		INNER JOIN :v_tblRptaPlazo AS p ON p."IdInterno" = emp."FolioEmprestito";
		
		p_tblDetalle = 
			SELECT a."FolioEmprestito", 
				ROUND( ( ( a."INGMontoCompraActualMonedaInstrumento" * ( b."CantidadProporcionalAnticipar" / a."INGCantidad" ) ) * ( 1 + ( a."INGTasaEmprestito" / 100 ) * ( b."Plazo" / CASE a."IdMonedaSubyacente" WHEN 'CLP' THEN 30 ELSE 360 END) ) ) - ( a."INGMontoCompraActualMonedaInstrumento" * ( b."CantidadProporcionalAnticipar" / a."INGCantidad" ) ), v_MontoDecimales ) AS "ValorPremio",
				a."INGTasaEmprestito" AS "TasaPremio",
				b."Plazo"
			FROM :v_tblResumenEmprestitoVigente AS a
			INNER JOIN (
				SELECT emp."FolioEmprestito" 
						, CASE WHEN p_CantidadAnticipo - ( SUM(empAcumulado."INGCantidad") - emp."INGCantidad" ) >= emp."INGCantidad"
									THEN emp."INGCantidad"
								WHEN p_CantidadAnticipo - ( SUM(empAcumulado."INGCantidad") - emp."INGCantidad" ) < emp."INGCantidad" AND p_CantidadAnticipo - ( SUM(empAcumulado."INGCantidad") - emp."INGCantidad" ) > 0
									THEN emp."INGCantidad" - ( emp."INGCantidad" - ( p_CantidadAnticipo - ( SUM(empAcumulado."INGCantidad") - emp."INGCantidad" ) ) ) 
								ELSE 
									0
							END AS "CantidadProporcionalAnticipar"
						, p."Plazo"
				FROM :v_tblResumenEmprestitoVigente AS emp
				INNER JOIN :v_tblRptaPlazo AS p ON p."IdInterno" = emp."FolioEmprestito"
				INNER JOIN :v_tblResumenEmprestitoVigente AS empAcumulado ON emp."FolioEmprestito" >= empAcumulado."FolioEmprestito" 
							AND EXISTS ( SELECT 1 FROM :v_tblRptaPlazo r WHERE r."IdInterno" = empAcumulado."FolioEmprestito" )
				GROUP BY emp."FolioEmprestito", emp."INGCantidad", p."Plazo"
				ORDER BY emp."FolioEmprestito" 
			) AS b ON b."FolioEmprestito" = a."FolioEmprestito";
	
		p_out_TotalPrima := 0;
		SELECT IFNULL(SUM("ValorPremio"),0) INTO p_out_TotalPrima FROM :p_tblDetalle;		

	END;

END;
GO