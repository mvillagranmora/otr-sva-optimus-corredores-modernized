/********************************************************** 
Proposito	: 
autor		: NRM
fecha		: 03/09/2019
***********************************************************/
WITH 
MovimientosTesoreria AS
(
	SELECT
		Portafolio."IdCliente" AS "IdCliente"
	   ,COALESCE(IE."IdPortafolioTitularPago",0) AS "IdPortafolio"
	   ,IE."IdMoneda"
	   ,CASE WHEN IE."IdTipoMovimiento" = 'MOVEGR' THEN -1*IE."Monto" ELSE IE."Monto" END "MontoMonedaOperacion"
	FROM "BSCL.Tesoreria.BD::TES.IngresoEgreso" AS IE
	INNER JOIN "BSCL.Normativa.BD::CAR.PortafolioPaso" P ON IE."IdPortafolioTitularPago" = P."IdPortafolio"
	LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio ON Portafolio."IdPortafolio" = IE."IdPortafolioTitularPago" --AND Portafolio."IdCliente" = IE."IdCliente"
	LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" AS MatrizAgrupador ON MatrizAgrupador."IdMatrizAgrupadorAPV" = Portafolio."IdMatrizAgrupadorAPV"
	WHERE IE."IdEstado" = 'ING'
	AND (IE."FechaLiquidacion" <= :FechaHasta OR IE."FechaLiquidacion" >= :FechaHasta)
	AND IE."IdEmpresa" = :IdEmpresa
	AND (:IdProductoAgrupador IS NULL OR MAP(MatrizAgrupador."IdProductoAgrupador",NULL,0,MatrizAgrupador."IdProductoAgrupador") = :IdProductoAgrupador)
	AND P."IdTrabajo" = :IdTrabajo
),
ComprobanteTesoreria AS
(
	SELECT
		Portafolio."IdCliente" AS "IdCliente"
	   ,COALESCE(COM."IdPortafolioTitularPago",0) AS "IdPortafolio"
	   ,COM."IdMoneda"
	   ,CASE WHEN COM."IdTipoMovimiento" = 'MOVEGR' THEN -1*COM."Monto" ELSE COM."Monto" END "MontoMonedaOperacion"
	FROM "BSCL.Tesoreria.BD::TES.Comprobante" AS COM
	INNER JOIN "BSCL.Normativa.BD::CAR.PortafolioPaso" P ON COM."IdPortafolioTitularPago" = P."IdPortafolio"
	LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio ON Portafolio."IdPortafolio" = COM."IdPortafolioTitularPago" --AND Portafolio."IdCliente" = COM."IdClienteTitularPago"
	LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" AS MatrizAgrupador ON MatrizAgrupador."IdMatrizAgrupadorAPV" = Portafolio."IdMatrizAgrupadorAPV"
	WHERE 	COM."IdEstado" NOT IN ('ANU','MOD','LIQ')
	AND (COM."FechaLiquidacion" <= :FechaHasta OR COM."FechaLiquidacion" >= :FechaHasta)
	AND COM."IdEmpresa" = :IdEmpresa
	AND (:IdProductoAgrupador IS NULL OR MAP(MatrizAgrupador."IdProductoAgrupador",NULL,0,MatrizAgrupador."IdProductoAgrupador") = :IdProductoAgrupador)
	AND P."IdTrabajo" = :IdTrabajo
)
SELECT
   "IdCliente"
   ,"IdPortafolio"
   ,'OPL' AS "Grupo"
   ,CONCAT('Fondos en Canje ', "IdMoneda") AS "Item"
   ,"IdMoneda" AS "IdMonedaOperacion"
   ,SUM("MontoMonedaOperacion") AS "MontoMonedaOperacion"
FROM
(
	SELECT
		"IdCliente"
	   ,"IdPortafolio"
	   ,"IdMoneda"
	   ,"MontoMonedaOperacion"
	FROM MovimientosTesoreria
	UNION ALL
	SELECT
		"IdCliente"
	   ,"IdPortafolio"
	   ,"IdMoneda"
	   ,"MontoMonedaOperacion"
	FROM ComprobanteTesoreria
)
GROUP BY
	"IdCliente"
   ,"IdPortafolio"
   ,"IdMoneda";