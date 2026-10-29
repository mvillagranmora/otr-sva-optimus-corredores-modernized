SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Mesa.CrystalReports.Servicios::FormasDePagoRecibimos";

-- 
--  Proposito: 
--  Fecha Creacion: 01/07/2016
--  

CREATE PROCEDURE "BSCL.Mesa.CrystalReports.Servicios::FormasDePagoRecibimos"(
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
			 TOP 5 FPR."NombreFormaDePago" AS "NombreMedioDePago"
			,TO_VARCHAR(FPR."FechaLiquidacionComprobantePagoOperacion",'DD-MM-YYYY') AS "FechaCondicionLiquidacion"
			,FPR."IdMonedaComprobantePago" AS "IdMoneda"
			,FPR."Monto"
			,COALESCE(MON."MontoDecimales",4)	AS "MontoDecimalesRecibimos"
		--FROM "BSCL.Mesa.BD.Vistas::ConsultarFormaDePago" FPR
		FROM "BSCL.Mesa.BD.Vistas::ConsultarComprobantePagoOperacion" FPR
		LEFT JOIN "BSCL.Plataforma.BD::PLA.MonedaFormato" MON
			ON (MON."IdUsoMonedaFormato" = 'SPT' AND MON."IdMoneda" = FPR."IdMonedaComprobantePago")
		WHERE FPR."IdOperacion" = p_IdOperacion AND FPR."RecibePaga" = 'R'
		AND (p_IdTipoProducto = '' OR FPR."IdTipoProducto" = p_IdTipoProducto)
		UNION ALL
		
	 	SELECT
			 '' AS "NombreMedioDePago"
			,'' AS "FechaCondicionLiquidacion"
			,'' AS "IdMoneda"
			,0  AS "Monto"
			,4	AS "MontoDecimalesRecibimos"
		FROM DUMMY;
		
	SELECT * FROM :v_Formas_De_Pago;
	
END;


