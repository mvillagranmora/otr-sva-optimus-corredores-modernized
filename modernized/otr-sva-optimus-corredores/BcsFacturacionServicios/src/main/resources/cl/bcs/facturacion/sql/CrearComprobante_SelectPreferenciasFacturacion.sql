SELECT  "IdPortafolio", MAX(Case When "Codigo"='FAC_FLFPPR' THEN  "Valor" ELSE NULL END ) AS "FusionaComprobanteDetallePorPrecio",
   						MAX(Case When "Codigo"='FAC_SFPINS' THEN  "Valor" ELSE NULL END ) AS "SeparaComprobantePorInstrumento",
                        MAX(Case When "Codigo"='FAC_SLFPTR' THEN  "Valor" ELSE NULL END ) AS "SeparaComprobanteDetallePorTransaccion",
						MAX(Case When "Codigo"='FAC_MODFAC' THEN  "Valor" ELSE NULL END ) AS "ModoFacturacion",
						MAX(CASE WHEN "Codigo"='FAC_FFVC'   THEN  "CodigoValor" ELSE NULL END ) AS "FusionFacturaVentaCorta",
						MAX(CASE WHEN "Codigo"='FAC_FFPPRV' THEN  "Valor" ELSE NULL END ) AS "FusionFacturaPrecioPromedioRV"
FROM "BSCL.Portafolio.BD.Vistas::ConsultarPreferenciasFacturacion" ConsPrefFac
WHERE  "Codigo" IN ('FAC_MODFAC','FAC_FLFPPR','FAC_SFPINS','FAC_SLFPTR', 'FAC_FFVC','FAC_FFPPRV')
AND "IdPortafolio" IN (:idsPortafolio)
GROUP BY "IdPortafolio";
