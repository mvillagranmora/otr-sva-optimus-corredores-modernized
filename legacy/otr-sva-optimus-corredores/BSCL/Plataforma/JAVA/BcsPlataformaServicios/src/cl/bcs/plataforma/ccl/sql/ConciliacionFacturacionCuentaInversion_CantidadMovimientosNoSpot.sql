--NO SPOT (1 MOVIMIENTO)
select cte."IdComprobante",count(cam."IdCargoAbonoMovimiento") as "CantidadCargoAbono"
from "BSCL.Facturacion.BD::FAC.Comprobante" cte
left join "BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento" cam on cam."IdOperacion" = cte."IdComprobante"
where cte."IdCarga" = 0 and cte."IdTipoProducto" != 'MEME'
group by cte."IdComprobante"
having count(cam."IdCargoAbonoMovimiento") > 1
