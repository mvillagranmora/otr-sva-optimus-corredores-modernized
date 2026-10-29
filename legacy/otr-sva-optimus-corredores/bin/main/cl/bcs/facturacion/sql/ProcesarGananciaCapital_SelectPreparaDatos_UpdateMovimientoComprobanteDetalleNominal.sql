SELECT   mcdt."Monto",mcdt."IdMovimientoComprobanteDetalle"	,(CASE WHEN indRes."ValorIndicador" ='S' THEN ( mcdt."Monto" * COALESCE(nres."Valor",1) /100 ) ELSE 0 END) AS "MontoRetencionGananciaCap",indRes."ValorIndicador"
		FROM "BSCL.Facturacion.BD::FAC.MovimientoComprobanteDetalle" mcdt
		INNER JOIN "BSCL.Facturacion.BD::FAC.MovimientoComprobante"  mct	ON (mcdt."IdMovimientoComprobante"=mct."IdMovimientoComprobante")
		INNER JOIN "BSCL.Facturacion.BD::FAC.ComprobanteDetalle" cdt ON (cdt."IdComprobanteDetalle"=mcdt."IdComprobanteDetalle")
		INNER JOIN	"BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" soc ON (soc."IdCliente"=mct."IdCliente")
		INNER JOIN "BSCL.SocioNegocio.BD::PER.Persona" PER ON (PER."IdPersona"=soc."IdPersona")
		LEFT JOIN 
		(
			SELECT ind."IdCliente",tin."IdTipoPersona",LTRIM(RTRIM(COALESCE (ind."ValorIndicador",'N'))) AS "ValorIndicador"
			FROM "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente" ind 
			INNER JOIN  "BSCL.Plataforma.BD.Vistas::IND.ConsultarTipoIndicador" tin ON  ( ind."CodigoTipoIndicador"= tin."CodigoTipoIndicador" AND ind."CodigoTipoIndicador" IN  ('NRESNODOM','NRESNODOMPJ'))
		) AS indRes ON (indRes."IdCliente"=soc."IdCliente" AND indRes."IdTipoPersona"=PER."Tipo")
		LEFT OUTER JOIN "BSCL.Facturacion.BD::FAC.ConfiguracionFacturacion" nres  on (nres."CodigoParametro"='IGK_POR_RETEN_NORESIDENTE')
		WHERE mcdt."IdMovimientoComprobanteDetalle" IN (:IdMovimientoComprobanteDetalle)
		AND mct."IdFormaCustodia" <> 'CUS' AND  mct."EsVentaCorta"='S';