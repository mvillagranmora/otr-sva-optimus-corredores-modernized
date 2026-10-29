SELECT CASE
WHEN OPE."IdTipoOperacion" = 'COMSPT' THEN 'VENSPT'
WHEN OPE."IdTipoOperacion" = 'VENSPT' THEN 'COMSPT'
END AS "IdTipoOperacion", OPE."IdTipoCanalIngreso", OPE."IdMonedaPrincipal", SUM(OPE."MontoMonedaPrincipal") AS "MontoMonedaPrincipal", OPE."IdMonedaSecundaria", SUM(OPE."MontoMonedaSecundaria") AS "MontoMonedaSecundaria"
FROM "BSCL.Mesa.BD::SPT.OperacionSpot" OPE
LEFT JOIN "BSCL.Facturacion.BD::FAC.ComprobanteDetalle"  MOV ON CAST(OPE."IdOperacionSpot" AS nvarchar) = CAST(MOV."IdTransaccion" AS nvarchar)
LEFT JOIN "BSCL.Mesa.BD::MES.ComprobantePagoOperacion" CPO ON CPO."IdOperacion" = OPE."IdOperacionSpot"
WHERE OPE."IdEmpresa" = :IdEmpresa
AND OPE."IdCliente" = :IdCliente
AND OPE."IdPortafolio" = :IdPortafolio
AND OPE."IdTipoOperacion" in ('VENSPT','COMSPT')
AND OPE."Estado" = 'VIG'
AND MOV."IdComprobante" IS NULL
AND CPO."IdFormaDePago" LIKE '%_CIN_%'
AND (OPE."IdTipoOperacion" = 'COMSPT' AND CPO."IdFormaDePago" like 'VENSPT%' OR OPE."IdTipoOperacion" = 'VENSPT' AND CPO."IdFormaDePago" like 'COMSPT%')
AND COALESCE(MOV."IdEstadoComprobanteDetalle",'') <> 'ANU'
GROUP BY OPE."IdTipoCanalIngreso", OPE."IdTipoOperacion", OPE."IdMonedaPrincipal", OPE."IdMonedaSecundaria";