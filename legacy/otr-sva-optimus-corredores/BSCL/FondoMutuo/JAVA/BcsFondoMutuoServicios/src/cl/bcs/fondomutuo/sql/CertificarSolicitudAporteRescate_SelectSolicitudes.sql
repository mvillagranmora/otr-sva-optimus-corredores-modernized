SELECT 
s."IdSolicitudAporteRescate"
,s."IdSolicitudAporteRescateRelacionado"
,s."FechaCertificacion"
,s."TipoSolicitud"
,s."IdSerieFondo"
,s."IdInstrumento"
,s."IdTipoInstrumento"
,s."IdTipoMovimientoFondoMutuo"
,s."IdEmisor"
,s."IdCliente"
,s."IdPortafolio"
,s."IdMoneda"
,s."IdMonedaPago"
,s."IdMonedaFondo"
,s."CostoMonedaLocal"
,s."Monto"
,s."ValorCuota"
,s."NumeroCuotas"
,s."ParidadFechaCertificacion"
,s."FechaLiquidacion"
,s."FechaAceptacion"
,s."IdUsuario"
,s."IdPersona"
,s."IdMedioDePago"
,s."MueveCta"
,s."CalculoPorMontoCuota"
,s."MontoComision"
,s."MontoPago"
,s."MontoComisionLocal"
,s."MontoPagoLocal"
,s."ValorUFFechaInversion"
,s."CostoUF"
,s."IndicadorMLT"
,s."Overnight"
,s."Glosa"
,s."IdSerieFondoDestino"
,s."IdEmisorDestino"
,s."SolicitudBatch"
,s."OrigenOperacion"
,s."ComisionInformada"
,s."PropositoPortafolio"


,s."IdRegimenTributario"  -- Ahora los rescates totales tambien especifican regimen tributario

,CASE s."PropositoPortafolio"
	WHEN 'PROPOSITOAPV' THEN
		CASE s."IdRegimenTributario"
			WHEN '' THEN s."CodigoPortafolio"
			ELSE s."IdRegimenTributario"
		END
	ELSE ''
END AS "IdRegimenTributarioAPV"
,s."TipoCarteraNormativa"
,s."IdRegimenTributarioDestino"
,s."TipoCarteraNormativaDestino"
,s."Spread"
,s."IdPreferenciaPagoPortafolio"
,s."IdTipoCanalIngreso"
,s."EstadoSolicitud"
,s."IdMonedaSistema"
,s."MontoMonedaSistema"
,s."EsPorMontoLiquido"
,s."MontoLiquido"
,s."MontoLiquidoLocal"
,s."EstadoLiquidacion"
,s."FolioCartaLiquidacion"
,s."EstadoSolicitud" AS "Estado"
,sr."Estado" AS "EstadoRelacionado"
,sr."EstadoLiquidacion" AS "EstadoLiquidacionRelacionado"
,s."IdEmpleadoEjecutivo"
,s."IdIngresoEgreso"
,s."IdOperacionSpot"
,CASE s."TipoSolicitud" 
	WHEN 'APO' THEN 
    	CASE 
    		WHEN s."IdTipoMovimientoFondoMutuo" = 'APOREINV' 	THEN 'APOREI'
        	WHEN s."IdTipoMovimientoFondoMutuo" = 'APODIV'	 	THEN 'APODIV'
         	WHEN s."IdTipoMovimientoFondoMutuo" = 'APOOVER'	 	THEN 'APOOVE'
         	WHEN s."IdTipoMovimientoFondoMutuo" = 'APORESPRO' 	THEN 'APOPRO'
         	WHEN s."IdTipoMovimientoFondoMutuo" = 'APOTRASP'	THEN 'APOTRS'
         	WHEN s."IdTipoMovimientoFondoMutuo" = 'APODIVCFI'	THEN 'APOCFI'
		 	ELSE 'APORFM'
    	END
	WHEN 'RES' THEN 
    	CASE 
    		WHEN s."IdTipoMovimientoFondoMutuo" = 'RESREINV' THEN 'RESREI'
        	WHEN s."IdTipoMovimientoFondoMutuo" = 'RESCOM'	THEN 'RESCOM'
         	WHEN s."IdTipoMovimientoFondoMutuo" = 'RESTRASP' THEN 'RESTRS'
         	ELSE 'RESCFM'
        END
END AS "IdConcepto"
,s."IdCartera"
,s."IdAgrupador"
,'N' as "RelacionTraspaso"
,s."Switch"
,CASE 
	WHEN t."RazonRetiro" = 'TRA' THEN COALESCE (t."IdPersona",0)
	ELSE COALESCE (ed."IdPersona",0) 
END AS "IdPersonaEmisorDestino"
,s."IdFamiliaFondo"
,s."FechaOriginalInversion"
,s."FechaValorCuota"
,s."IngresoAutomatico"
,COALESCE (t."RazonRetiro",'') AS "RazonRetiro"
,s."FolioContrato"
,s."IdOmnibus"
,reg."CertificaSinAsociado"
,se."EsAPV"
,s."DocumentoIdentidad"
,s."CodigoPortafolio"
,tp."GeneraOmnibus"
,c."TipoComision"
,CASE 
	WHEN COALESCE(solRelNN."IdTipoMovimientoFondoMutuo",'') = 'RESCNN' THEN 'S'
	ELSE 'N'
END AS "TieneComisionNNRelacionada"
FROM "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarSolicitudAporteRescate" AS s
LEFT OUTER JOIN "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" sr ON (s."IdSolicitudAporteRescateRelacionado" = sr."IdSolicitudAporteRescate")
LEFT OUTER JOIN "BSCL.Plataforma.BD::MER.Emisor"	AS ed ON (ed."IdEmisor"=s."IdEmisorDestino" )
LEFT OUTER JOIN "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" AS se ON (se."IdSerieFondo" = s."IdSerieFondo")
LEFT OUTER JOIN "BSCL.APV.BD::TRA.OrdenTraspaso" AS ot ON (ot."IdSolicitudAporteRescate" = s."IdSolicitudAporteRescate")
LEFT OUTER JOIN "BSCL.APV.BD::TRA.Traspaso" AS t ON (ot."IdTraspaso"= t."IdTraspaso")
LEFT OUTER JOIN "BSCL.FondoMutuo.BD::FMM.RegimenFondoMutuo" AS reg ON reg."IdRegimenFondoMutuo" = s."IdRegimenTributario"
LEFT OUTER JOIN "BSCL.FondoMutuo.BD::FMM.TipoMovimientoFondoMutuo" AS tp ON tp."IdTipoMovimientoFondoMutuo" = s."IdTipoMovimientoFondoMutuo"
LEFT JOIN "BSCL.FondoMutuo.BD::FMM.Comision" AS c ON s."IdSerieFondo" = c."IdSerieFondo" AND c."TipoComision" = 'PER'
LEFT OUTER JOIN "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" solRelNN ON (solRelNN."IdSolicitudAporteRescateRelacionado" = s."IdSolicitudAporteRescate" AND solRelNN."IdTipoMovimientoFondoMutuo" = 'RESCNN')
WHERE s."IdSolicitudAporteRescate" IN (:IdSolicitudAporteRescate) AND s."IdEmpresa" = :IdEmpresa
ORDER BY s."TipoSolicitud" ASC;