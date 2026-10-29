SELECT 
	MvtoCmbteCobro."IdMovimientoComprobanteCobro" AS "IdMovimientoComprobanteCobroOld", 
	MvtoCmbteCobro."IdCobro", 
	MvtoCmbteCobro."IdMovimientoComprobante" AS "NumeroMvtoCmbte",
	MvtoCmbteCobro."IdMovimientoComprobanteCobro" AS "NumeroMvtoCmbteCobro",
	MvtoCmbteCobro."IdMonedaCobro",
	CASE 
        WHEN MvtoCmbteCobro."IdCobro" = 'GFFA' 
            THEN CC."TotalGastoCmbte" 
        ELSE MvtoCmbteCobro."MontoCobro" 
    END AS "MontoCobro", 
	MvtoCmbteCobro."Porcentaje", 
	MvtoCmbteCobro."FechaSistema", 
	MvtoCmbteCobro."IdEstado", 
	MvtoCmbteCobro."IdMonedaLiquidacionCobro", 
	MvtoCmbteCobro."IdMonedaMovimientoComprobanteCobro", 
	MvtoCmbteCobro."MontoMovimientoComprobanteCobro",
	MvtoCmbteCobro."MontoLiquidacionCobro", 
	MvtoCmbteCobro."Monto", 
	MvtoCmbteCobro."MontoMovimientoComprobante", 
	MvtoCmbteCobro."MontoLiquidacion"
FROM "BSCL.Facturacion.BD::FAC.MovimientoComprobanteCobro" MvtoCmbteCobro
LEFT JOIN "BSCL.Facturacion.BD::FAC.MovimientoComprobante"  MC
	ON MC."IdMovimientoComprobante" = MvtoCmbteCobro."IdMovimientoComprobante" 
LEFT JOIN (
			SELECT  "IdComprobante",
                    MAX(CASE WHEN "CodigoCobroClasificacion" = 'COMISION' THEN "MontoLocalCobro" ELSE 0 END) AS "TotalComisCmbte",
                    MAX(CASE WHEN "CodigoCobroClasificacion" = 'DERECHO' THEN "MontoLocalCobro" ELSE 0 END) AS "TotalDereCmbte",
                    MAX(CASE WHEN "CodigoCobroClasificacion" = 'GASTO' THEN "MontoLocalCobro" ELSE 0 END) AS "TotalGastoCmbte",
                    MAX(CASE WHEN "CodigoCobroClasificacion" = 'IMPUESTO' THEN "MontoLocalCobro" ELSE 0 END) AS "TotalImptoCmbte",
                    MAX(CASE WHEN "CodigoCobroClasificacion" = 'RETENCION' THEN "MontoLocalCobro" ELSE 0 END) AS "TotalRetenCmbte",
                    MAX("IdMonedaComprobanteCobro") AS "IdMonedaComprobanteCobro"
			 FROM (
					SELECT  CmbteCobro."IdComprobante",
						Cobro."IdCobroClasificacion", 
						CobroClasificacion."CodigoCobroClasificacion",
						IFNULL(SUM(CmbteCobro."MontoComprobanteCobro"),0) as "MontoLocalCobro",
						CmbteCobro."IdMonedaComprobanteCobro"
				  FROM "BSCL.Facturacion.BD::FAC.ComprobanteCobro" CmbteCobro
				  LEFT OUTER JOIN "BSCL.Plataforma.BD::COM.Cobro" As Cobro On Cobro."IdCobro" = CmbteCobro."IdCobro"
				  LEFT OUTER JOIN "BSCL.Plataforma.BD::COM.CobroClasificacion" As CobroClasificacion On CobroClasificacion."IdCobroClasificacion" = Cobro."IdCobroClasificacion"
				GROUP BY CmbteCobro."IdComprobante", Cobro."IdCobroClasificacion",CobroClasificacion."CodigoCobroClasificacion", CmbteCobro."IdMonedaComprobanteCobro"
				ORDER BY Cobro."IdCobroClasificacion"
			 )
			 WHERE "IdComprobante" IN (:idsComprobante)
			 group by "IdComprobante"
		) AS CC
		ON CC."IdComprobante" = MC."IdComprobante" 
WHERE MvtoCmbteCobro."IdMovimientoComprobante" IN (:idsMovimientoComprobanteOld)
AND MvtoCmbteCobro."IdEstado" = 'VIG';