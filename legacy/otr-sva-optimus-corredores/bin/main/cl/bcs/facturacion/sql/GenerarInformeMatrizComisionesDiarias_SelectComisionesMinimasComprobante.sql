SELECT DISTINCT
comp."IdComprobante",
val."ValorTopeMinimoOrden" as "ComisionMinima"
FROM "BSCL.Facturacion.BD::FAC.Comprobante" AS comp
LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" cli on cli."IdCliente" = comp."IdCliente"
LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS port ON port."IdPortafolio" = comp."IdPortafolio"
LEFT JOIN "BSCL.Plataforma.BD::COM.PerfilCobros" pp ON port."IdPerfilCobros" = pp."IdPerfilCobros"
LEFT JOIN "BSCL.Plataforma.BD::COM.DetallePerfilCobros" det on det."IdPerfilCobros" = pp."IdPerfilCobros"
LEFT JOIN "BSCL.Plataforma.BD::COM.ValorCobro" val on val."IdValorCobro" = det."IdValorCobro" AND val."IdTipoProducto" = comp."IdTipoProducto"
LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS inst ON inst."IdInstrumento" = comp."IdInstrumento"
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" tipinst ON tipinst."IdTipoInstrumento" = inst."IdTipoInstrumento" 
WHERE comp."IdComprobante" IN (:IdsComprobantes)
AND tipinst."IdClasificacionTipoInstrumento" = 'RV'
AND val."IdCobro" = 'COVA'
AND det."Borrado" = 'N'
AND val."ValorTopeMinimoOrden" NOT IN(0);