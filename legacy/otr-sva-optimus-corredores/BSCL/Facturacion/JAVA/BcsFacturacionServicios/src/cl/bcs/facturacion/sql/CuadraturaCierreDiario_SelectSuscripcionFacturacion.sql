Select 
		EvCli."IdEventoCapitalCliente"					AS "IdTransaccion",
		EvCli."IdEventoCapitalCliente"					AS "FolioTransaccion",
		Max(TO_DATE(EvCptal."FechaHoraIngresoRegistro")) 	AS "FechaTransaccion", 
		Max(MovComprobante."FechaHoraMovimientoComprobante") 			AS "FechaHoraTransaccion",
        --Max(MovComprobante."IdTipoOperacion")            AS "IdTipoOperacion", 
		--Max(EvCptal."IdEventoCapital")     AS "IdEventoCapitalVigente", 
		Max(EvCli."IdClasificacionTipoInstrumento")		AS "IdClasificacionTipoInstrumento",
		Max(EvCli."IdTipoInstrumento") 					AS "IdTipoInstrumento",
		Max(Bo."Nombre")								AS "NombreBolsa",	
		Max(TipoOperacion."Nombre")						AS "TipoOperacion",
		Max(MovComprobante."IdCondicionLiquidacion") 	AS "IdCondicionLiquidacion",
		--Max(EvCli."IdMoneda"),
		Max(MovComprobante."IdMoneda")                  AS "IdMoneda",
		Max(MovComprobante."NombreTipoInstrumento")    	AS "NombreTipoInstrumento",
		Max(MovComprobante."NemotecnicoInstrumento")    AS "NemotecnicoInstrumento",
		Max(MovComprobante."Cantidad")                  AS "Cantidad",
		Max(MovComprobante."Precio")                    AS "Precio",
		Max(MovComprobante."Monto")                     AS "Monto",	
		--Max(MovComprobante."IdTipoComprobante") AS "TipoComprobanteFacturacion", 
		--Max(MovComprobante."IdComprobante")   AS "IdComprobanteFacturacion",
		Max(MovComprobante."Cantidad") 						AS "CantidadAsignada",
		Max(CASE WHEN (EvCli."CantidadNueva" - MovComprobante."Cantidad")  > 0 THEN MovComprobante."Cantidad"  
		ELSE 0 END) 										AS "CantidadPorAsignar",
		Max(MovComprobante."Cantidad") 						AS "CantidadEnviada",
		Max(CASE WHEN (EvCli."CantidadNueva" - MovComprobante."Cantidad")  > 0 THEN MovComprobante."Cantidad"  
		ELSE 0 END) 										AS "CantidadPorEnviar",
		Max(MovComprobante."Cantidad") 						AS "CantidadFacturada",
		Max(CASE WHEN (EvCli."CantidadNueva" - MovComprobante."Cantidad")  > 0 THEN MovComprobante."Cantidad"  
		ELSE 0 END) 										AS "CantidadPorFacturar",
		Max(EvCptal."IdEstado")						AS "EstadoEventoCapital", 
		Max(MovComprobante."IdEstadoFacturacion") 		AS "EstadoFacturacion",		
		Max(MovComprobante."CodigoBolsa")               AS "NombreBolsa",
		Coalesce(Sum(Case 
	                   When MovComprobante."IdTipoComprobante" in ('FAC', 'FAE', 'POL') Then Cmbte."Monto"
					 Else 0
					 End) - (Sum(Case 
					               When MovComprobante."IdTipoComprobante" in ('NCR', 'NCE') Then Cmbte."Monto"
								 Else 0
								 End)),0) 				As "MontoFacturado", 
		Max(ROUND(EvCli."MontoCapital", 0)) - Coalesce(Sum(Case 
		                                                      When MovComprobante."IdTipoComprobante" in ('FAC', 'FAE', 'POL') Then Cmbte."Monto"
													          Else 0
													          End) - (Sum(Case 
													                      When MovComprobante."IdTipoComprobante" in ('NCR', 'NCE') Then Cmbte."Monto"
																          Else 0 End)),0) As "MontoPorFacturar"	
	FROM "BSCL.Custodia.BD::CUS.EventoCapitalCliente"  AS EvCli 
  	INNER JOIN "BSCL.Custodia.BD::CUS.EventoCapital"   AS EvCptal
    		ON EvCli."IdEventoCapital" = EvCptal."IdEventoCapital" AND  EvCli."IdTipoEventoCapital" = 'SU'
    LEFT JOIN "BSCL.Plataforma.BD::INS.Bolsa" AS Bo ON (EvCli."IdBolsa" = Bo."IdBolsa" )
	INNER JOIN "BSCL.Facturacion.BD::FAC.MovimientoComprobante" AS MovComprobante
		ON MovComprobante."IdOperacion" = EvCli."IdEventoCapitalCliente"
	LEFT JOIN "BSCL.Facturacion.BD::FAC.MovimientoComprobanteDetalle" AS Cmbte
	   ON Cmbte."IdMovimientoComprobante" = MovComprobante."IdMovimientoComprobante"
	LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" AS TipoOperacion
	    ON TipoOperacion."IdTipoOperacion" = MovComprobante."IdTipoOperacion"
WHERE EvCptal."IdEstado" = 'VIG'
  AND EvCli."IdTipoEventoCapital" = 'SU'
  AND MovComprobante."IdTipoOperacion" = :IdOperacion
  AND TO_DATE(MovComprobante."FechaEmision") = :FechaInicio
GROUP BY EvCli."IdEventoCapitalCliente";

