UPDATE CMD SET CMD."MayorMenorValor"=CA."MmValor",CMD."MontoRetencionGananciaCap"= (CASE WHEN CA."ValorIndicador" ='S' THEN ( CMD."Monto" * COALESCE(nres."Valor",1) /100 ) ELSE CA."Retencion" END)
FROM "BSCL.Facturacion.BD::FAC.MovimientoComprobanteDetalle" CMD
INNER JOIN (

	SELECT sum(cal."MmValor") AS "MmValor",sum(cal."Retencion") AS "Retencion",cal."IdMovimientoComprobanteDetalle",CASE WHEN LTRIM(RTRIM(COALESCE (cal."ValorIndicador",'N'))) ='' THEN 'N' ELSE LTRIM(RTRIM(COALESCE (cal."ValorIndicador",'N'))) END AS "ValorIndicador"
		FROM (
				SELECT   mcdt."Monto",
					mcdt."IdMovimientoComprobanteDetalle"
					,(car."EGRCantidad" * car."EGRPrecio") - (car."EGRCantidad" * car."PrecioCompra") AS "MmValor"
					, (((car."EGRCantidad" * car."EGRPrecio") - (car."EGRCantidad" * car."PrecioCompra")) * 	COALESCE(res."Valor",10) /100)AS "Retencion"
					,LTRIM(RTRIM(COALESCE (indRes."ValorIndicador",'N'))) AS "ValorIndicador"
					FROM "BSCL.Facturacion.BD::FAC.MovimientoComprobanteDetalle" mcdt
					INNER JOIN "BSCL.Facturacion.BD::FAC.MovimientoComprobante"  mct	ON (mcdt."IdMovimientoComprobante"=mct."IdMovimientoComprobante")
					INNER JOIN "BSCL.Facturacion.BD::FAC.ComprobanteDetalle" cdt ON (cdt."IdComprobanteDetalle"=mcdt."IdComprobanteDetalle")
					INNER JOIN "BSCL.Custodia.BD::CUS.Cartera" car ON ( car."EGRFolioComprobante"=cdt."IdComprobante" AND mcdt."NemotecnicoInstrumento"=car."Nemotecnico")
					INNER JOIN	"BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" soc ON (soc."IdCliente"=mct."IdCliente")
					INNER JOIN "BSCL.SocioNegocio.BD::PER.Persona" PER ON (PER."IdPersona"=soc."IdPersona")
					LEFT JOIN 
					(
						SELECT ind."IdCliente",tin."IdTipoPersona",ind."ValorIndicador"
						FROM "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente" ind 
						INNER JOIN  "BSCL.Plataforma.BD.Vistas::IND.ConsultarTipoIndicador" tin ON  ( ind."CodigoTipoIndicador"= tin."CodigoTipoIndicador" AND ind."CodigoTipoIndicador" IN  ('NRESNODOM','NRESNODOMPJ'))
					) AS indRes ON (indRes."IdCliente"=soc."IdCliente" AND indRes."IdTipoPersona"=PER."Tipo")
					LEFT OUTER JOIN "BSCL.Facturacion.BD::FAC.ConfiguracionFacturacion" res  on (res."CodigoParametro"='IGK_POR_RETEN_RESIDENTE')
					WHERE mcdt."IdMovimientoComprobanteDetalle" IN (:IdMovimientoComprobanteDetalle)
     		)AS cal GROUP BY cal."IdMovimientoComprobanteDetalle",cal."ValorIndicador"

) CA ON (CA."IdMovimientoComprobanteDetalle"=CMD."IdMovimientoComprobanteDetalle")
LEFT OUTER JOIN "BSCL.Facturacion.BD::FAC.ConfiguracionFacturacion" nres  on (nres."CodigoParametro"='IGK_POR_RETEN_NORESIDENTE');