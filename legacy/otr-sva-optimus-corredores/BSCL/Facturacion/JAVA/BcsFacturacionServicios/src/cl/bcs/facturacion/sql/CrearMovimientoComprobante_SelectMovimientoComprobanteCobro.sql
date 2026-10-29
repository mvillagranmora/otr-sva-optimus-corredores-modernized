SELECT MVTOCOB."NumeroMvtoCmbte",MVTOCOB."NumeroMvtoCmbteCobro",MVTOCOB."IdCobro",MVTOCOB."IdMonedaCobro",MVTOCOB."IdMonedaLiquidacionCobro",

COB."IdCobro" AS "IdCobroReal",
DEFCOB."CodigoDefinicionCobro" AS "CodigoDefinicionCobroReal",
MON."IdMoneda" AS "IdMonedaReal",
MONLIQ."IdMoneda" AS "IdMonedaLiquidacionReal",

"BSCL.Facturacion.BD.Secuencias::MovimientoComprobanteCobro".NEXTVAL AS "IdMovimientoComprobanteCobro"

FROM "BSCL.Facturacion.BD::FAC.CrearMovimientoComprobanteCobro_Proceso" MVTOCOB
LEFT JOIN "BSCL.Plataforma.BD::COM.Cobro" AS COB ON COB."IdCobro" = MVTOCOB."IdCobro"
LEFT JOIN "BSCL.Plataforma.BD::COM_2.DefinicionCobro" AS DEFCOB ON DEFCOB."CodigoDefinicionCobro" = MVTOCOB."IdCobro"
LEFT JOIN "BSCL.Plataforma.BD::PLA.Moneda" AS MON ON MON."IdMoneda" = MVTOCOB."IdMonedaCobro"
LEFT JOIN "BSCL.Plataforma.BD::PLA.Moneda" AS MONLIQ ON MONLIQ."IdMoneda" = MVTOCOB."IdMonedaLiquidacionCobro";