SELECT "IdTipoCanalDistribucion","IdTipoCanalIngreso","IdTipoInstrumento","IdBolsa","GeneraMovimientoFacturacionAutomatico",
"GeneraComprobanteFacturacionAutomatico"
FROM "BSCL.Orden.BD::ORD.ConfiguracionCanalDistribucion"
WHERE "IdEmpresa" = :IdEmpresa
