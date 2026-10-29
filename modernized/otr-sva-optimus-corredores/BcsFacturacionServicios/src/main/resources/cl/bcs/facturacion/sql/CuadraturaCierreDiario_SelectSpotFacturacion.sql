SELECT	OperacionSpot."IdOperacionSpotOrigen"   		AS "FolioTransaccion",
        Max(OperacionSpot."IdTipoOperacion")            AS "IdTipoOperacion",
		Max(TipoOperacion."Nombre")						AS "TipoOperacion",		 
		Max(OperacionSpot."IdOperacionSpotVigente")     AS "IdOperacionSpotVigente", 
		Max(TO_DATE(OperacionSpot."FechaOperacion")) 	AS "FechaHoraTransaccion", 
		Max(OperacionSpot."TipoComprobanteFacturacion") AS "TipoComprobanteFacturacion", 
		Max(OperacionSpot."IdComprobanteFacturacion")   AS "IdComprobanteFacturacion", 
		Max(OperacionSpot."Estado")						AS "EstadoSpot", 
		Max(MovComprobante."IdEstadoFacturacion")		AS "EstadoFacturacion",
		Max(MovComprobante."CodigoBolsa")				AS "CodigoBolsa",
		Max(MovComprobante."IdMoneda")					AS "IdMoneda",
		Max(MovComprobante."NemotecnicoInstrumento")	AS "NemotecnicoInstrumento",
		
		Max(CASE 
			WHEN  MovComprobante."EsFungible" = 'S' AND  MovComprobante."IdClasificacionTipoInstrumento" = 'IF' THEN  MovComprobante."ValorRescate" 
			ELSE  MovComprobante."Cantidad" 
		END) AS  "Cantidad",
		
		Max(MovComprobante."Precio")                    AS "Precio",		 	
		Max(Coalesce("BSCL.Plataforma.BD.Utilitarios::FormatearNumero"( MovComprobante."Monto", FormMvto."MontoDecimales", FormMvto."MontoTrucaRedondea"), MovComprobante."Monto")) As "Monto",
		Coalesce(Sum(Case 
	            When MovComprobante."IdTipoComprobante" in ('FAC', 'FAE', 'POL') Then Cmbte."Monto"
				Else 0
				End) - (Sum(Case 
					            When MovComprobante."IdTipoComprobante" in ('NCR', 'NCE') Then Cmbte."Monto"
								Else 0
							End)),0) As "MontoFacturado", 
		Max(ROUND(OperacionSpot."MontoMonedaSecundaria", 0)) - Coalesce(Sum(Case 
		                                                                     	When MovComprobante."IdTipoComprobante" in ('FAC', 'FAE', 'POL') Then Cmbte."Monto"
													                        	Else 0
													                        End) - (Sum(Case 
													                                      	When MovComprobante."IdTipoComprobante" in ('NCR', 'NCE') Then Cmbte."Monto"
																                        	Else 0
																                        End)),0) As "MontoPorFacturar",
																                        
	    TO_DECIMAL(0)                                                       AS "CantidadAsignada",
        TO_DECIMAL(0)														AS "CantidadPorAsignar",
        TO_DECIMAL(0)														AS "CantidadEnviada",
        TO_DECIMAL(0)														AS "CantidadFacturada",
        TO_DECIMAL(0)														AS "CantidadPorEnviar",
        TO_DECIMAL(0)														AS "CantidadPorFacturar"
FROM "BSCL.Mesa.BD::SPT.OperacionSpot" AS OperacionSpot
INNER JOIN "BSCL.Facturacion.BD::FAC.MovimientoComprobante" AS  MovComprobante
	ON MovComprobante."IdTransaccion"= OperacionSpot."IdOperacionSpot"
	   AND MovComprobante."IdClasificacionTipoInstrumento"	= 'SP'
LEFT JOIN "BSCL.Facturacion.BD::FAC.MovimientoComprobanteDetalle" AS Cmbte
	   ON Cmbte."IdMovimientoComprobante" = MovComprobante."IdMovimientoComprobante"
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion"						AS TipoOperacion
	    ON TipoOperacion."IdTipoOperacion" = OperacionSpot."IdTipoOperacion"
LEFT JOIN "BSCL.Plataforma.BD::PLA.MonedaFormato" As FormMvto 
      	On FormMvto."IdMoneda" = MovComprobante."IdMoneda"
     	And FormMvto."IdTipoOperacion" = ''
     	And FormMvto."IdTipoInstrumento" = ''
     	And FormMvto."IdUsoMonedaFormato" = 'GEN'
WHERE OperacionSpot."Estado" = 'VIG'
  AND OperacionSpot."IdTipoOperacion" = :IdOperacion
  AND TO_DATE(OperacionSpot."FechaOperacion") = :FechaInicio
GROUP BY OperacionSpot."IdOperacionSpotOrigen";

