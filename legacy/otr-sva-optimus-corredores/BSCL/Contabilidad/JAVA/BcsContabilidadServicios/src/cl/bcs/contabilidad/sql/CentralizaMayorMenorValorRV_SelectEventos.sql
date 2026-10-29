WITH v_TabEventosPaso AS (
    SELECT
    	TO_BIGINT(:p_IdTipoCentralizacion)    		AS "IdTipoCentralizacion"
		,CVIFRF."IdValorizacion"					AS "IdMovimiento"                              
        ,ROUND(CVIFRF."MontoCompra",0)				AS "MontoCompra"                 
        ,ROUND(CVIFRF."MontoActual",0)				AS "MontoActual"
        ,ROUND(CVIFRF."MontoProximo",0)				AS "MontoProximo"
        ,CVIFRF."MayorValorMonedaSistema" 
        ,CVIFRF."MenorValorMonedaSistema" 
        ,CVIFRF."IdTipoOperacion"     				AS "Nemotecnico"
        ,CVIFRF."IdMonedaInstrumento"
    FROM "BSCL.Custodia.BD::VAL.CarteraValorizadaIFRF" CVIFRF
    WHERE CVIFRF."IdEmpresa" = :p_IdEmpresa
	  AND CVIFRF."IdInforme" = :p_IdInforme
)
SELECT 
	"IdMovimiento",
	"IdTipoCentralizacion",
	'MONTO_COMPRA' 			AS "IdEvento",
	'Monto Compra' 			AS "Nombre",
	"MontoCompra" 			AS "MontoEvento",
	"IdMonedaInstrumento" 	AS "CodigoMoneda" 
FROM v_TabEventosPaso
UNION ALL
SELECT 
	"IdMovimiento",
	"IdTipoCentralizacion",
	'MONTO_PRESENTE'		AS "IdEvento",
	'Monto Presente' 		AS "Nombre",
	"MontoActual" 			AS "MontoEvento",
	"IdMonedaInstrumento" 	AS "CodigoMoneda" 
FROM v_TabEventosPaso
UNION ALL
SELECT 
	"IdMovimiento",
	"IdTipoCentralizacion",
	'MONTO_MERCADO'			AS "IdEvento",
	'Monto Mercado' 		AS "Nombre",
	"MontoProximo" 			AS "MontoEvento",
	"IdMonedaInstrumento" 	AS "CodigoMoneda" 
FROM v_TabEventosPaso
UNION ALL
SELECT 
	"IdMovimiento",
	"IdTipoCentralizacion",
	'MAYOR_VALOR_COMPRA' 	AS "IdEvento",
	'Mayor Valor Compra' 	AS "Nombre",
	"MayorValorMonedaSistema" AS "MontoEvento",
	"IdMonedaInstrumento" 	AS "CodigoMoneda" 
FROM v_TabEventosPaso
UNION ALL
SELECT 
	"IdMovimiento",
	"IdTipoCentralizacion",
	'MENOR_VALOR_COMPRA' 	AS "IdEvento",
	'Menor Valor Compra' 	AS "Nombre",
	"MenorValorMonedaSistema"					AS "MontoEvento",
	"IdMonedaInstrumento" 	AS "CodigoMoneda" 
FROM v_TabEventosPaso;