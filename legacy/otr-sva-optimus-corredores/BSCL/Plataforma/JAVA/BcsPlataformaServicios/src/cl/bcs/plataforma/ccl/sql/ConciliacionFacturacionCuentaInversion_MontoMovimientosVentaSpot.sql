select * from (
	select cte."IdComprobante",cte."Monto",cte."MontoCobro",cte."MontoComprobanteFinal" as "MontoComprobante",cte."IdMoneda"  as "IdMonedaComprobante",
	cam."IdCargoAbonoMovimiento",cam."IdTipoMovimiento",cam."Monto" as "MontoCargoAbono",cta."IdMoneda" as "IdMonedaCuentaInversion",cte."ValorTipoCambio",
	
	ROUND (
	CASE 	WHEN cam."IdTipoMovimiento" = 'MOVCAR' and cta."IdMoneda" = 'CLP' THEN cte."MontoComprobante" - cam."Monto" 
			WHEN cam."IdTipoMovimiento" = 'MOVABN' THEN cte."MontoComprobante"/ROUND(cte."ValorTipoCambio",8,ROUND_CEILING) - cam."Monto" END
	,4,ROUND_CEILING)
	as "Diferencia"
	
	from "BSCL.Facturacion.BD::FAC.Comprobante" cte
	left join "BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento" cam on cam."IdOperacion" = cte."IdComprobante"
	left join "BSCL.CuentaInversion.BD::CTA.CuentaInversion" cta on cam."IdCuentaInversion" = cta."IdCuentaInversion"
	where cte."IdCarga" = 0 and cte."IdTipoProducto" = 'MEME' and cte."IdTipoOperacion" = 'VENSPT'
	order by cte."IdComprobante"
)
where "Diferencia" > 0.01