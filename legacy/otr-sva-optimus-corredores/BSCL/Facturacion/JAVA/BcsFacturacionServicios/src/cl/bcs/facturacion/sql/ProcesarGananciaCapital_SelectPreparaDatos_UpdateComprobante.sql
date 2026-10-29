SELECT  CA."MmValor" AS "MayorMenorValor",CA."Retencion" AS "MontoRetencionGananciaCap",(CASE WHEN CA."ValorIndicador"='S' THEN 'C' ELSE 'R' END) AS "CargoGananciaCap",CA."IdComprobante"
FROM "BSCL.Facturacion.BD::FAC.Comprobante" CM
INNER JOIN (
		SELECT sum(cal."MayorMenorValor") AS "MmValor",sum(cal."MontoRetencionGananciaCap") AS "Retencion",cal."IdComprobante",cal."ValorIndicador"
		FROM (
			SELECT
			 cdt."IdComprobante"
			,cdt."MayorMenorValor"
			,cdt."MontoRetencionGananciaCap"
			,indRes."ValorIndicador"
			FROM "BSCL.Facturacion.BD::FAC.ComprobanteDetalle"cdt
			INNER JOIN
			(
				SELECT cdtr."IdComprobante" FROM  "BSCL.Facturacion.BD::FAC.MovimientoComprobanteDetalle" mcdt
				INNER JOIN "BSCL.Facturacion.BD::FAC.ComprobanteDetalle" cdtr ON (mcdt."IdComprobanteDetalle"=cdtr."IdComprobanteDetalle")
				WHERE mcdt."IdMovimientoComprobanteDetalle" IN (:IdMovimientoComprobanteDetalle)
				GROUP BY cdtr."IdComprobante"
			) com ON (com."IdComprobante"=cdt."IdComprobante")
			INNER JOIN  "BSCL.Facturacion.BD::FAC.Comprobante" cab ON (com."IdComprobante"=cab."IdComprobante") 
			INNER JOIN	"BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" soc ON (soc."IdCliente"=cab."IdCliente")
							INNER JOIN "BSCL.SocioNegocio.BD::PER.Persona" PER ON (PER."IdPersona"=soc."IdPersona")
							LEFT JOIN 
							(
								SELECT ind."IdCliente",tin."IdTipoPersona",ind."ValorIndicador"
								FROM "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente" ind 
								INNER JOIN  "BSCL.Plataforma.BD.Vistas::IND.ConsultarTipoIndicador" tin ON  ( ind."CodigoTipoIndicador"= tin."CodigoTipoIndicador" AND ind."CodigoTipoIndicador" IN  ('NRESNODOM','NRESNODOMPJ'))
							) AS indRes ON (indRes."IdCliente"=soc."IdCliente" AND indRes."IdTipoPersona"=PER."Tipo")
		)AS cal GROUP BY cal."IdComprobante",cal."ValorIndicador"
) CA ON (CA."IdComprobante"=CM."IdComprobante")
