--CONCILIACION DE MONTOS
--NO SPOT
select cte."IdComprobante",cte."Monto",cte."MontoCobro",
CASE cam."IdTipoMovimiento" WHEN 'MOVABN' THEN cte."MontoComprobanteFinal" WHEN 'MOVCAR' THEN -1*cte."MontoComprobanteFinal" END as "MontoComprobante",cte."IdMoneda" as "IdMonedaComprobante",
cam."IdCargoAbonoMovimiento",cam."IdTipoMovimiento",cam."Monto" as "MontoCargoAbono",cta."IdMoneda" as "IdMonedaCuentaInversion" 
from "BSCL.Facturacion.BD::FAC.Comprobante" cte
left join "BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento" cam on cam."IdOperacion" = cte."IdComprobante"
left join "BSCL.CuentaInversion.BD::CTA.CuentaInversion" cta on cam."IdCuentaInversion" = cta."IdCuentaInversion"
where cte."IdCarga" = 0 and cte."IdTipoProducto" != 'MEME'
and cam."Monto" != CASE cam."IdTipoMovimiento" WHEN 'MOVABN' THEN cte."MontoComprobante" WHEN 'MOVCAR' THEN -1*cte."MontoComprobante" END


