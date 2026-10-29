WITH 
MovimientosTesoreria AS
(
	SELECT
		IE."IdClienteTitularPago" AS "IdCliente"
	   ,COALESCE(IE."IdPortafolioTitularPago",0) AS "IdPortafolio"
	   ,IE."IdMoneda"
	   ,CASE WHEN IE."IdTipoMovimiento" = 'MOVEGR' THEN -1*IE."Monto" ELSE IE."Monto" END "MontoMonedaOperacion"
	FROM "BSCL.Tesoreria.BD.Vistas::ConsultarIngresoEgresoCartola" AS IE
	LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio ON Portafolio."IdPortafolio" = IE."IdPortafolioTitularPago" AND Portafolio."IdCliente" = IE."IdClienteTitularPago"
	LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" AS MatrizAgrupador ON MatrizAgrupador."IdMatrizAgrupadorAPV" = Portafolio."IdMatrizAgrupadorAPV"
	WHERE
	IE."IdPortafolioTitularPago" IN (:IdPortafolio)
	AND	IE."IdEstado" = 'ING'
	AND (IE."FechaLiquidacion" <= :FechaHasta OR IE."FechaLiquidacion" >= :FechaHasta)
	AND IE."IdEmpresa" = :IdEmpresa
	AND (:IdProductoAgrupador IS NULL OR MAP(MatrizAgrupador."IdProductoAgrupador",NULL,0,MatrizAgrupador."IdProductoAgrupador") = :IdProductoAgrupador)
),
ComprobanteTesoreria AS
(
	SELECT
		COM."IdClienteTitularPago" AS "IdCliente"
	   ,COALESCE(COM."IdPortafolioTitularPago",0) AS "IdPortafolio"
	   ,COM."IdMoneda"
	   ,CASE WHEN COM."IdTipoMovimiento" = 'MOVEGR' THEN -1*COM."Monto" ELSE COM."Monto" END "MontoMonedaOperacion"
	FROM "BSCL.Tesoreria.BD.Vistas::ConsultarComprobanteCartola" AS COM
	LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio ON Portafolio."IdPortafolio" = COM."IdPortafolioTitularPago" AND Portafolio."IdCliente" = COM."IdClienteTitularPago"
	LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" AS MatrizAgrupador ON MatrizAgrupador."IdMatrizAgrupadorAPV" = Portafolio."IdMatrizAgrupadorAPV"
	WHERE
	COM."IdPortafolioTitularPago" IN (:IdPortafolio)
	AND	COM."IdEstado" NOT IN ('ANU','MOD','LIQ')
	AND (COM."FechaLiquidacion" <= :FechaHasta OR COM."FechaLiquidacion" >= :FechaHasta)
	AND COM."IdEmpresa" = :IdEmpresa
	AND (:IdProductoAgrupador IS NULL OR MAP(MatrizAgrupador."IdProductoAgrupador",NULL,0,MatrizAgrupador."IdProductoAgrupador") = :IdProductoAgrupador)
)
SELECT
   "IdCliente"
   ,"IdPortafolio"
   ,'OPL' AS "Grupo"
   ,CONCAT('Saldo Retenido ', "IdMoneda") AS "Item"
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
