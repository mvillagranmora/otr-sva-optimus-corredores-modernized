WITH
   CuentaInversion AS
    (
      SELECT
      cuentainversion."IdPortafolio"
      ,cuentainversion."IdMoneda"
      ,cargoabonomov."Monto" AS "Monto"
      ,TO_DATE(:FechaDesde) AS "FechaLiquidacion"
      ,TO_DECIMAL(0,26,8) AS "MontoCompra"
      ,TO_DECIMAL(0,26,8) AS "MontoVenta"
      ,'ABONOS_RETENIDOS_POR_LIQUIDAR' AS "ItemCalculo"
      FROM "BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento" AS cargoabonomov
	  INNER JOIN "BSCL.CuentaInversion.BD::CTA.CuentaInversion" AS cuentainversion ON  cuentainversion."IdCuentaInversion" = cargoabonomov."IdCuentaInversion"

	  
	  INNER JOIN "BSCL.Plataforma.BD::GBL.SaldoFlujoCaja_Proceso_Paso" portafolio ON 
	  									    (portafolio."IdPortafolio" = cuentainversion."IdPortafolio" 
	  									    AND portafolio."IdMoneda" = cuentainversion."IdMoneda")

	  WHERE  cargoabonomov."IdEstado" = 'CRT' AND cargoabonomov."FechaSistema" <= :FechaHasta AND cargoabonomov."FechaEfectivaMovimiento" > :FechaDesde 
	  AND cargoabonomov."IdEmpresa" = :IdEmpresa AND cargoabonomov."IdTipoOperacion"<>''
	  AND "IdTipoMovimiento" <> 'MOVCAR'
    )
SELECT "IdPortafolio" || '_' || "IdMoneda" AS "Id","FechaLiquidacion","IdPortafolio","IdMoneda",SUM("Monto") as "Monto",SUM("MontoCompra") as "MontoCompra",SUM("MontoVenta") as "MontoVenta","ItemCalculo"
,:TipoCalculoFlujoCaja AS "TipoCalculoFlujoCaja"
FROM CuentaInversion
GROUP BY "FechaLiquidacion","IdPortafolio","IdMoneda","ItemCalculo";