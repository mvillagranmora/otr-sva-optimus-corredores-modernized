WITH
   CuentaInversion AS
    (
      SELECT
      cuentainversion."IdCliente"
      ,cuentainversion."IdPortafolio"
      ,cuentainversion."IdMoneda"
      ,CASE WHEN cargoabonomov."IdTipoMovimiento" = 'MOVABN' THEN cargoabonomov."Monto" ELSE -1 * cargoabonomov."Monto" END AS "Monto"
      ,:p_FechaDesde AS "FechaSaldo"
      FROM "BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento" AS cargoabonomov
	  INNER JOIN "BSCL.CuentaInversion.BD::CTA.CuentaInversion" AS cuentainversion ON  cuentainversion."IdCuentaInversion" = cargoabonomov."IdCuentaInversion"

	  
	  INNER JOIN "BSCL.CuentaInversion.BD::GBL.ConsultarSaldoCuentaInversion_Cuenta_Paso" portafolio ON 
	  									    (portafolio."IdPortafolio" = cuentainversion."IdPortafolio" 
	  									    AND portafolio."IdMoneda" = cuentainversion."IdMoneda")

	  WHERE portafolio."IdTrabajo" = :IdTrabajo AND cargoabonomov."IdEstado" = 'CRT' AND cargoabonomov."FechaSistema" <= :p_FechaDesde AND cargoabonomov."FechaEfectivaMovimiento" > :p_FechaDesde AND cargoabonomov."IdEmpresa" = :p_IdEmpresa
    )
SELECT "IdCliente","IdPortafolio","IdMoneda",CAST(SUM("Monto") AS DECIMAL(26,8)) AS "Monto","FechaSaldo"
FROM CuentaInversion
GROUP BY "IdCliente","IdPortafolio","FechaSaldo","IdMoneda";


