SELECT 
	     OPE."IdOperacion"
	    ,OPE."IdTipoOperacion"
	    ,OPE."IdCliente"
	    ,OPE."IdPortafolio"
	    ,OPE."IdClienteCorredor"
	    ,OPE."IdPortafolioCorredor"
	    ,OPE."FolioCartera" AS "FolioCarteraPacto"
	    ,OPE."IdInstrumento" AS "IdInstrumentoPacto"
	    ,OPE."PlazoEnDias"
	    ,OPE."IdTipoProducto"	 
	    ,RES."IdReserva"
	    ,RES."IdInstrumento" 	  AS "IdInstrumentoDetalle"
	    ,RES."IngCantidad" 		  AS "CantidadDetalle"
	    ,RES."IngValorRescate"    AS "ValorRescateDetalle"
	    ,RES."IngMontoValorizado" AS "MontoValorizadoDetalle"
	    ,RES."FolioCarteraOrigen" AS "FolioCarteraOrigenDetalle"
	    ,RES."IdCarteraOrigen" 	  AS "IdCarteraOrigenDetalle"
	    ,RES."IdEmisor"
	    ,CAR."IdCartera"  AS "IdCarteraDestinoDetalle"
	    ,RES."FolioCarteraCustodia" AS "FolioCarteraDestinoDetalle"	  
	    ,CAR."INGCantidad" 		  AS "CantidadCarteraDestinoDetalle"
	    ,CAR."INGValorRescate" 	  AS "ValorRescateDestinoDetalle"
	    ,CAR."FechaLiquidacion"
	    ,RES."EsFungible"
	    ,CAR."IdTipoReajuste"
	    ,CAR."IdMonedaEmision"
	    ,CAR."IdMonedaInstrumento"
	    ,RES."Tasa" AS "TasaOperacion"
	    ,RES."Precio" 
	    ,OPE."IdTipoReajuste" AS "IdTipoReajusteOperacion"
FROM "BSCL.Mesa.BD::MES.Operacion" AS ope
INNER JOIN "BSCL.Custodia.BD::CUS.Reserva" AS res ON ope."IdOperacion"= res."IdOperacion" 
INNER JOIN "BSCL.Custodia.BD::CUS.Cartera" AS car ON car."IdOperacion" = res."IdReserva"
WHERE res."IdOperacion" IN (:IdOperacion) AND car."IdTipoProducto"='MERF'
AND ope."IdEstado"='VIG'
AND car."IdEstado"='VIG'
AND ope."FechaVencimiento"<=(:FechaVencimiento)
AND car."IdTipoOperacion" = :TipoOperacion
and car."IdTipoInstrumento" <> 'PAC';

   