SELECT DISTINCT SUM(comp."Cantidad")AS "CantidadTotal",INDIC."ValorIndicador" AS "SeparaPorInstrumento",comp."IdComprobante",comp."IdComprobanteReferencia",comp."IdCliente",comp."IdPortafolio",comp."DocumentoIdentidad",por."CodigoPortafolio",
comp."NombreLargoCliente" AS "NombreCompleto",comp."IdEstadoComprobante",comp."MontoCobroComprobante",comp."IdCondicionLiquidacion" AS "IdCondicionDeLiquidacion",
comp."IdMoneda", comp."IdTipoInstrumento",
comp."FechaLiquidacion",comp."IdConceptoMovimiento",comp."IdBolsa",comp."IdTipoComprobante",comp."IdMonedaLiquidacion", comp."IdClasificacionTipoInstrumento",
comp."NemotecnicoInstrumentoComprobante",
case 
	when INDIC."ValorIndicador" = 'S' then 
		case 
			when SUM(comp."Cantidad") = 0 then 0
			else (SUM(comp."Monto")/SUM(comp."Cantidad"))
		end
	else 0
end AS "PPP"

FROM "BSCL.Facturacion.BD.Vistas::ConsultarDetalleMovimientoDeComprobante" AS comp
LEFT JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" por ON por."IdPortafolio" = comp."IdPortafolio"

LEFT OUTER JOIN "BSCL.Plataforma.BD::IND.TipoIndicador" AS TIPINDIC ON (TIPINDIC."IdEmpresa" =comp."IdEmpresa" AND TIPINDIC."CodigoTipoIndicador"='FAC_SFPINS')
LEFT OUTER JOIN "BSCL.Plataforma.BD::IND.Indicador" AS INDIC ON(INDIC."IdTipoIndicador"=TIPINDIC."IdTipoIndicador" AND INDIC."IdTipoEntidad"='PORTAFOLIO' AND INDIC."IdEntidad"=comp."IdPortafolio")

WHERE (
		"IdComprobante" IN (:idsComprobante) 
		OR "IdMovimientoComprobante" IN (:idsMovimientoComprobante)
		OR "IdAsignacion" IN (:idsAsignacion)
		OR "IdTransaccion" IN (:idsTransaccion)
)

GROUP BY comp."IdComprobante",comp."IdComprobanteReferencia",comp."IdCliente",comp."IdPortafolio",
comp."DocumentoIdentidad",por."CodigoPortafolio",
comp."NombreLargoCliente",comp."IdEstadoComprobante",comp."MontoCobroComprobante",comp."IdCondicionLiquidacion",
comp."FechaLiquidacion",comp."IdConceptoMovimiento",comp."IdBolsa",comp."IdTipoComprobante",comp."IdMonedaLiquidacion", 
comp."IdClasificacionTipoInstrumento",INDIC."ValorIndicador",comp."IdTipoInstrumento",comp."IdMoneda",comp."NemotecnicoInstrumentoComprobante"