SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Mesa.CrystalReports.Servicios::FormasDePagoEntregamos";

-- 
--  Proposito: 
--  Fecha Creacion: 01/07/2016
--  

CREATE PROCEDURE "BSCL.Mesa.CrystalReports.Servicios::FormasDePagoEntregamos"(
	--Entrada
	IN p_IdEmpresa 		BIGINT,
	IN p_IdOperacion 	BIGINT,
	IN p_IdTipoProducto NVARCHAR(4) DEFAULT ''
)
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER 
DEFAULT SCHEMA "BSCL"
AS
BEGIN
	v_Formas_De_Pago =
		SELECT
			 TOP 5 FPE."NombreFormaDePago" AS "NombreMedioDePago"
			,TO_VARCHAR(FPE."FechaLiquidacionComprobantePagoOperacion",'DD-MM-YYYY') AS "FechaCondicionLiquidacion"
			,FPE."IdMonedaComprobantePago" AS "IdMoneda"
			,FPE."Monto"
			,COALESCE(MON."MontoDecimales",4)	AS "MontoDecimalesEntregamos"
		FROM "BSCL.Mesa.BD.Vistas::ConsultarComprobantePagoOperacion" FPE
		LEFT JOIN "BSCL.Plataforma.BD::PLA.MonedaFormato" MON
			ON (MON."IdUsoMonedaFormato" = 'SPT' AND MON."IdMoneda" = FPE."IdMonedaComprobantePago")
		WHERE FPE."IdOperacion" = p_IdOperacion AND FPE."RecibePaga" = 'P'
		AND (p_IdTipoProducto = '' OR FPE."IdTipoProducto" = p_IdTipoProducto)
		UNION ALL
		
	 	SELECT
			 '' AS "NombreMedioDePago"
			,'' AS "FechaCondicionLiquidacion"
			,'' AS "IdMoneda"
			,0  AS "Monto"
			,4	AS "MontoDecimalesEntregamos"
		FROM DUMMY;
		
	SELECT * FROM :v_Formas_De_Pago;
	
END;
