WITH
   CuentaInversion AS
    (
      SELECT
      cuentainversion."IdCliente"
      ,cuentainversion."IdPortafolio"
      ,cuentainversion."IdMoneda"
      ,CASE WHEN cargoabonomov."IdTipoMovimiento" = 'MOVABN' THEN cargoabonomov."Monto" ELSE -1 * cargoabonomov."Monto" END AS "Monto"
      FROM "BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento" AS cargoabonomov
	  INNER JOIN "BSCL.CuentaInversion.BD::CTA.CuentaInversion" AS cuentainversion ON  cuentainversion."IdCuentaInversion" = cargoabonomov."IdCuentaInversion"
	  INNER JOIN "BSCL.Normativa.BD::CAR.PortafolioPaso" P ON cuentainversion."IdPortafolio" = P."IdPortafolio"
	  INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS portafolio ON portafolio."IdPortafolio" = cuentainversion."IdPortafolio"
	  LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" AS matrizAgrupador ON matrizAgrupador."IdMatrizAgrupadorAPV" = portafolio."IdMatrizAgrupadorAPV"
      WHERE 
      cargoabonomov."IdEstado" = 'CRT' 
      AND cargoabonomov."FechaSistema" <= :FechaHasta 
      AND cargoabonomov."FechaEfectivaMovimiento" > :FechaHasta 
      AND cargoabonomov."IdEmpresa" = :IdEmpresa
      AND (:IdProductoAgrupador IS NULL OR MAP(MatrizAgrupador."IdProductoAgrupador",NULL,0,MatrizAgrupador."IdProductoAgrupador") = :IdProductoAgrupador)
      AND P."IdTrabajo" = :IdTrabajo
    )
SELECT 
"IdCliente"
,"IdPortafolio"
,'OPL' AS "Grupo"
,CONCAT('Saldo Retenido ', "IdMoneda") AS "Item"
,"IdMoneda" AS "IdMonedaOperacion"
,CAST(SUM("Monto") AS DECIMAL(26,8)) AS "MontoMonedaOperacion"
FROM CuentaInversion
GROUP BY "IdCliente","IdPortafolio","IdMoneda";