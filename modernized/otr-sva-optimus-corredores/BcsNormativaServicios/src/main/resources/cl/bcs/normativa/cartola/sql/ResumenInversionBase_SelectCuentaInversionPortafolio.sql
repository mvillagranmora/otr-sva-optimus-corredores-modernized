WITH 
MovimientosCuentaInversion AS
(
SELECT
	cuentainversion."IdCuentaInversion"
   ,cuentainversion."IdCliente"
   ,COALESCE(cuentainversion."IdPortafolio",0) AS "IdPortafolio"
   ,cuentainversion."IdMoneda"
   ,cargoabonomov."SaldoPorFechaMovimiento"
   ,cargoabonomov."FechaSistema"
   ,RANK() over (PARTITION BY cargoabonomov."IdCuentaInversion" 
   ORDER BY cargoabonomov."FechaSistema",cargoabonomov."IdCargoAbonoMovimiento") as "RankFechaMovimiento"

FROM  "BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento" AS cargoabonomov
LEFT OUTER JOIN "BSCL.CuentaInversion.BD::CTA.CuentaInversion" AS cuentainversion ON  cuentainversion."IdCuentaInversion" = cargoabonomov."IdCuentaInversion"
INNER JOIN "BSCL.Normativa.BD::CAR.PortafolioPaso" P ON cuentainversion."IdPortafolio" = P."IdPortafolio"
LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio ON Portafolio."IdPortafolio" = cuentainversion."IdPortafolio" AND Portafolio."IdCliente" = cuentainversion."IdCliente"
LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" AS MatrizAgrupador ON MatrizAgrupador."IdMatrizAgrupadorAPV" = Portafolio."IdMatrizAgrupadorAPV"
WHERE cargoabonomov."IdEstado" = 'CRT'
AND cuentainversion."IdMoneda" IN (:IdMoneda)
 	AND cargoabonomov."FechaEfectivaMovimiento" <= :FechaHasta
AND (:IdProductoAgrupador IS NULL OR MAP(MatrizAgrupador."IdProductoAgrupador",NULL,0,MatrizAgrupador."IdProductoAgrupador") = :IdProductoAgrupador)
AND P."IdTrabajo" = :IdTrabajo
)
SELECT
	MOV."IdCliente"
   ,MOV."IdPortafolio"
   ,'CAJA' AS "Grupo"
   ,CASE WHEN "SaldoPorFechaMovimiento" > 0 THEN CONCAT('Caja Disponible ',  MOV."IdMoneda") ELSE CONCAT('Deuda ', MOV."IdMoneda") END as "Item"
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