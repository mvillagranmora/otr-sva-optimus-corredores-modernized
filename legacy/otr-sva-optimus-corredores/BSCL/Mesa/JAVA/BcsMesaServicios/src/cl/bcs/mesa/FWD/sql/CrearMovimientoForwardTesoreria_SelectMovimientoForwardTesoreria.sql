SELECT
 CPO."IdOperacion"
,INGEGR."IdCartera"
,INGEGR."FolioCartera"
,INGEGR."IdEstado"
,CPO."IdModalidadPago"
,CPO."IdFormaDePago"
,CPO."IdTipoProducto"
,CPO."Monto" AS "MontoFormaDePagoForward"
,CPO."IdMoneda" AS "MonedaFormaDePagoForward"
,FWD."MontoMonedaPrincipal"
,FWD."MontoMonedaSecundaria"
,INGEGR."MontoOperacion"
,INGEGR."IdMonedaLiquidacion"
,FWD."IdPortafolio"
,FWD."IdTipoCanalIngreso"
,fPag."IdMedioDePago"
,fPag."IdTipoOperacion"
,FWD."IdMonedaPrincipal"
FROM "BSCL.Mesa.BD::MES.ComprobantePagoOperacion" CPO
INNER JOIN "BSCL.Mesa.BD::GBL.CrearMovimientoForwardTesoreria_Proceso_Paso" AS INGEGR ON (CPO."IdOperacion" = INGEGR."IdCartera" AND CPO."IdEmpresa" = :IdEmpresa)
INNER JOIN "BSCL.Mesa.BD.Vistas::FWD.ConsultarMovimientosFWDVencimiento" AS FWD ON (INGEGR."IdCartera" = FWD."IdCartera" AND INGEGR."FolioCartera"	= FWD."IdOperacion" AND INGEGR."IdEstado" = FWD."IdEstado")
LEFT JOIN "BSCL.Mesa.BD.Vistas::ConsultarFormasDePagoMesa" fPag ON (CPO."IdFormaDePago" = fPag."IdFormaDePago" AND CPO."IdTipoProducto" = fPag."IdTipoProducto" AND fPag."IdEstado"='ACT')
WHERE CPO."AfectarTesoreria" = 'S'
AND FWD."ModalidadLiquidacionValor" = 'COMPENSADA'
AND CPO."IdTipoProducto" = 'MEFW';