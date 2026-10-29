WITH 
MovimientosCuentaInversion AS
(
SELECT
	B."IdCuentaInversion"
   ,B."IdCliente"
   ,COALESCE(B."IdPortafolio",0) AS "IdPortafolio"
   ,B."IdMoneda"
   ,B."SaldoPorFechaMovimiento"
   ,B."FechaSistema"
   ,RANK() over (PARTITION BY B."IdCuentaInversion" ORDER BY B."FechaSistema",B."FechaCertificacion",B."IdCargoAbonoMovimiento") as "RankFechaMovimiento"
FROM "BSCL.CuentaInversion.BD.Vistas::ConsultarCartolaDeMovimientos" AS B
LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio ON Portafolio."IdPortafolio" = B."IdPortafolio" AND Portafolio."IdCliente" = B."IdCliente"
LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" AS MatrizAgrupador ON MatrizAgrupador."IdMatrizAgrupadorAPV" = Portafolio."IdMatrizAgrupadorAPV"
WHERE
B."IdCliente" IN (:IdCliente)
AND	B."IdEstado" = 'CRT'
AND B."IdMoneda" IN (:IdMoneda)
 	AND B."FechaEfectivaMovimiento" <= :FechaHasta
AND (:IdProductoAgrupador IS NULL OR MAP(MatrizAgrupador."IdProductoAgrupador",NULL,0,MatrizAgrupador."IdProductoAgrupador") = :IdProductoAgrupador)
)
SELECT
	MOV."IdCliente"
   ,MOV."IdPortafolio"
   ,'CAJA' AS "Grupo"
   ,CASE WHEN "SaldoPorFechaMovimiento" > 0 THEN CONCAT('Caja Disponible ', MOV."IdMoneda") ELSE CONCAT('Deuda ',  MOV."IdMoneda") END as "Item"
   ,MOV."IdMoneda" AS "IdMonedaOperacion"
   ,MOV."SaldoPorFechaMovimiento" AS "MontoMonedaOperacion"
FROM MovimientosCuentaInversion AS MOV,
(
	SELECT
		"IdCuentaInversion"
	   ,MAX("RankFechaMovimiento") AS "RankMovimientoAnterior"
	   ,"IdMoneda"
	FROM MovimientosCuentaInversion
	GROUP BY
		"IdCuentaInversion"
	   ,"IdMoneda"
) FMAX
WHERE
FMAX."IdCuentaInversion" = MOV."IdCuentaInversion"
AND FMAX."RankMovimientoAnterior" = MOV."RankFechaMovimiento"
AND FMAX."IdMoneda" = MOV."IdMoneda";