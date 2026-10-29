SELECT "FolioCartera", 
        "AccionOperacion",
        "AccionOperacionPlazo",
        "FechaInversion",
        "FechaVencimientoOperacion",
        "MontoCompra",
        "MontoAPlazo",
        "EGRFechaOperacion",
        "INGCantidad",
        "INGPrecioCompra",
        "INGTasaCompra",       
        ROUND("INGMontoATasaCompraActual",0) as "INGMontoATasaCompraActual",
        "INGMontoAPlazo",
        "INGFechaLiquidacion",
        "EGRFechaLiquidacion",
        "suby_Nemotecnico",
        "FolioComprobante",
        "suby_NombreTipoInstrumento",
        "Cliente",
        "Proposito",
        "CodigoPortafolio",
        "FechaLiquidacion",
        SIMUL."IdEstado",
        "PaloNormal",
        "PaloPlazo",
        "NombreCorredora",
        "IdCorredoraContraparte",
        "IdBolsa",
        "NombreEjecutivo",
        "FechaLiquidacionMovtoContado",
        "PrecioInicial",
        "PrecioAPlazo",
        "suby_INGPrecioMercadoActual",
        "PrecioActualCalculado",
        "NombreLargo",
        "IdCondicionLiquidacion",
        "IdTipoComprobante",
        "IdTipoInstrumentoOperacion",
        "IdTipoOperacion",
        "IdTipoProducto",
        "DocumentoIdentidad",
        "EGRCantidad",
        "EGRCostoRetiroenFactorAjuste",
        "EGRFechaComprobante",
        "suby_IdMonedaInstrumento",
        "INGMontoInteresCompraAnterior",
        "INGMontoInteresCompraAnteriorMonedaInstrumento",
        "INGMontoInteresCompraAnteriorMonedaAdicional",
        "INGMontoInteresCorridoCompraActual",
        "INGMontoInteresCorridoCompraActualMonedaInstrumento",
        "INGMontoInteresCorridoCompraActualMonedaAdicional",
        "INGMontoInteresCompraProximo",
        "INGMontoInteresCompraProximoMonedaInstrumento",
        "INGMontoInteresCompraProximoMonedaAdicional",
        "INGMontoInteresMercadoAnterior",
        "INGMontoInteresMercadoAnteriorMonedaInstrumento",
        "INGMontoInteresMercadoAnteriorMonedaAdicional",
        "INGMontoInteresCorridoMercadoActual",
        "INGMontoInteresCorridoMercadoActualMonedaInstrumento",
        "INGMontoInteresCorridoMercadoActualMonedaAdicional",
        "INGMontoInteresMercadoProximo",
        "INGMontoInteresMercadoProximoMonedaInstrumento",
        "INGMontoInteresMercadoProximoMonedaAdicional",
        CASE 
	        WHEN "FechaLiquidacionMovtoContado" IS NULL OR "FechaVencimientoOperacion" IS NULL THEN '-'
	        ELSE TO_VARCHAR(DAYS_BETWEEN("FechaLiquidacionMovtoContado", :INGFechaOperacion)) 
	             || '/' || TO_VARCHAR(DAYS_BETWEEN("FechaLiquidacionMovtoContado", "FechaVencimientoOperacion"))
    	END AS "NumeroDias",
        COALESCE(DETERIORO."FlagDeterioro",'N') AS "FlagDeterioro",
        COALESCE(DETERIORO."PorcentajeDeterioro",0 ) AS "PorcentajeDeterioro",
        CASE DETERIORO."FlagDeterioro" WHEN 'S' 
            THEN CAST((COALESCE(DETERIORO."PorcentajeDeterioro" ,0)/100) * COALESCE("INGCantidad",0) AS DECIMAL(26,8))
            ELSE 0 
        END AS "Deterioro",
        COALESCE(DETERIORO."Fase",0) AS "Fase",
        "NombreLargoCliente"
        FROM "BSCL.Custodia.BD.Vistas::SIM.ConsultarCarteraSimultanea" AS SIMUL
        LEFT JOIN "BSCL.Plataforma.BD::MER.DeterioroEmisor" AS DETERIORO ON DETERIORO."Emisor" = "suby_IdEmisor" AND DETERIORO."IdEstado" = 'ACT'
        WHERE 	("FechaVencimientoOperacion"	> TO_DATE(:FechaVencimientoOperacion))
			AND ("INGFechaOperacion"			<= TO_DATE(:INGFechaOperacion))
			AND (SIMUL."IdEstado"						= :IdEstado)
			AND ("EGRCantidad"					= :EGRCantidad)
			AND ("IdCliente"					IN (:IdCliente)					OR 1 = :FiltroCliente)
			AND ("IdPortafolio"					IN (:IdPortafolio)				OR 1 = :FiltroPortafolio)
			AND ("IdTipoOperacion"				IN (:IdTipoOperacion)			OR 1 = :FiltroTipoOperacion)
			AND ("suby_IdInstrumento"			IN (:IdInstrumentoSubyacente)	OR 1 = :FiltroInstrumentoSubyacente)
			AND ("PaloNormal"					IN (:PaloNormal)				OR 
				"PaloPlazo"						IN (:PaloPlazo) 				OR 1 = :FiltroPalo)
			