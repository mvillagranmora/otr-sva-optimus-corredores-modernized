SET SCHEMA "BSCL";


DROP VIEW "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarSolicitudesFFMM";


CREATE VIEW "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarSolicitudesFFMM" AS
SELECT
	SolicitudAporteRescate."IdCliente"																									AS "IdCliente"
	,CASE 	WHEN SolicitudAporteRescate."IdTipoMovimientoFondoMutuo" = 'RESREINV' 	THEN 'REV'
			WHEN SolicitudAporteRescate."IdTipoMovimientoFondoMutuo" = 'RESCOM'		THEN 'REC'
    ELSE SolicitudAporteRescate."TipoSolicitud"
    END 																																AS "TipoSolicitud"
    ,TO_NVARCHAR(TO_DATE(SolicitudAporteRescate."FechaIngresoRegistro"))                                                                AS "FechaConsulta"
    ,CASE WHEN COALESCE(Portafolio."IdMatrizAgrupadorAPV",0) = 0 THEN 'NOAPV'
     ELSE 'APV'
     END 																																AS "Origen"
    ,SolicitudAporteRescate."IdSolicitudAporteRescate"																					AS "NumSolicitud"
    ,CASE WHEN SolicitudAporteRescate."IdTipoMovimientoFondoMutuo" = 'RESREINV' THEN
   		'REINVERSION'
    ELSE CASE WHEN SolicitudAporteRescate."TipoSolicitud" = 'APO' THEN
    	'APORTE'
    	ELSE 
    	'RESCATE'
    	END
    END 																																AS "DetSolicitud"
    ,CASE WHEN (SolicitudAporteRescate."Estado" = 'ANU') THEN 'ANULADO'
    	 WHEN (SolicitudAporteRescate."TipoSolicitud" = 'APO' AND SolicitudAporteRescate."IdMoneda" = 'CLP' AND SolicitudAporteRescate."Estado" = 'ING' AND SolicitudAporteRescate."FolioOmnibus" = 0) THEN 'INGRESADO'
   		 WHEN (SolicitudAporteRescate."TipoSolicitud" = 'APO' AND SolicitudAporteRescate."IdMoneda" != 'CLP' AND SolicitudAporteRescate."Estado" = 'PEN' AND SolicitudAporteRescate."FolioOmnibus" = 0) THEN 'INGRESADO'
   		 WHEN (SolicitudAporteRescate."TipoSolicitud" = 'RES' AND SolicitudAporteRescate."Estado" = 'ING' AND SolicitudAporteRescate."FolioOmnibus" = 0) THEN 'INGRESADO'
   		 WHEN (SolicitudAporteRescate."TipoSolicitud" = 'APO' AND SolicitudAporteRescate."IdMoneda" != 'CLP' AND SolicitudAporteRescate."Estado" = 'ING' AND SolicitudAporteRescate."FolioOmnibus" = 0) THEN 'EN PROCESO'
   		 WHEN (SolicitudAporteRescate."Estado" != 'CRT' AND SolicitudAporteRescate."FolioOmnibus" > 0) THEN 'EN PROCESO'
   		 WHEN (SolicitudAporteRescate."IdAgrupador" > 0 AND Reinversion."EstadoAporte" != 'CRT' AND SolicitudAporteRescate."FolioOmnibus" > 0) THEN 'EN PROCESO'
   		 WHEN (SolicitudAporteRescate."IdAgrupador" > 0 AND Reinversion."EstadoAporte" != 'CRT' AND SolicitudAporteRescate."Estado" = 'CRT') THEN 'EN PROCESO'
   		 WHEN (SolicitudAporteRescate."Estado" = 'CRT' AND Reinversion."EstadoAporte" = 'CRT' AND SolicitudAporteRescate."IdAgrupador" > 0) THEN 'TERMINADO'
   		 WHEN (SolicitudAporteRescate."Estado" = 'CRT' AND SolicitudAporteRescate."IdAgrupador" = 0) THEN 'TERMINADO'
          WHEN (SolicitudAporteRescate."IdCarga" != 0 AND SolicitudAporteRescate."Estado" = 'ING') THEN 'INGRESADO'
          WHEN (SolicitudAporteRescate."IdCarga" != 0 AND SolicitudAporteRescate."Estado" = 'CRT') THEN 'TERMINADO'
   		 ELSE ''
    END																																	AS "EstadoDetalle"
    ,SolicitudAporteRescate."IdSolicitudAporteRescate"																					AS "Rellamada"
    ,SolicitudAporteRescate."IdTipoCanalIngreso"																						AS "OrigenSolicitud"
    ,TO_NVARCHAR(TO_DATE(SolicitudAporteRescate."FechaIngresoRegistro"))																AS "FechaSolicitud"
    ,TO_NVARCHAR(TO_TIME(SolicitudAporteRescate."FechaIngresoRegistro"))																AS "HoraSolicitud"
    ,TO_NVARCHAR(TO_DATE(Fondo."FechaCierre"))																							AS "FechaCierre"
    ,CASE WHEN SolicitudAporteRescate."CalculoPorMontoCuota" = 'T' THEN 'TOTAL'
    ELSE 'PARCIAL'
    END		                                                                                                                            AS "TipoTraspaso"
    ,''																																	AS "MotivoRechazo"
    ,CASE WHEN COALESCE(SolicitudAporteRescate."TipoContratoOperacion",'') = '' 
    		   THEN TRIM(TO_VARCHAR(COALESCE(SolicitudAporteRescate."FolioContrato",0)))
     ELSE TRIM(SolicitudAporteRescate."FolioContratoOperacion")
     END 																																AS "Folio"
    ,COALESCE(SolicitudAporteRescate."IdAgrupador",TO_BIGINT(0))																		AS "FolioRelacionado"
    ,ConsultarCliente."DocumentoIdentidad"																								AS "RutCliente"
    ,SerieDeFondo."IdInstrumento"																										AS "CodFondoOrigen"
    ,Fondo."NombreFondo"																												AS "NombreFondoOrigen"
    ,SerieDeFondo."CodigoSerie"																											AS "SerieFondoOrigen"
    ,RegimenTributario."Descripcion"																									AS "RegimenFondoOrigen"
    ,ROUND(SolicitudAporteRescate."MontoMonedaSistema",0)																				AS "MontoPesosOrigen"
    ,SolicitudAporteRescate."NumeroCuotas"																								AS "MontoCuotasOrigen"
    ,SolicitudAporteRescate."ValorCuota"																								AS "ValorCuotaOrigen"
    ,CASE WHEN SolicitudAporteRescate."IdTipoMovimientoFondoMutuo" = 'RESREINV' THEN COALESCE(SerieDeFondoDes."IdInstrumento",TO_BIGINT(0))
    ELSE TO_BIGINT(0)
    END																																	AS "CodFondoDestino_ori"
    ,CASE WHEN SolicitudAporteRescate."IdTipoMovimientoFondoMutuo" = 'RESREINV' THEN COALESCE(SerieDeFondoDes."IdInstrumento",TO_BIGINT(0))
    ELSE TO_BIGINT(0)
    END																																	AS "CodFondoDestino"
    ,CASE WHEN SolicitudAporteRescate."IdTipoMovimientoFondoMutuo" = 'RESREINV' THEN COALESCE(FondoDes."NombreFondo",'')
    ELSE ''
    END																																	AS "NombreFondoDestino"
    ,CASE WHEN SolicitudAporteRescate."IdTipoMovimientoFondoMutuo" = 'RESREINV' THEN COALESCE(SerieDeFondoDes."CodigoSerie",'')
    ELSE ''
    END																																	AS "SerieFondoDestino"
    ,CASE WHEN RegimenTributario."GrupoRegimen" IN ('APV-A', 'APV-B') THEN 'AHORRO PREVISIONAL VOLUNTARIO'
    WHEN RegimenTributario."GrupoRegimen" = 'DC' THEN 'DEPOSITO CONVENIDO'
    ELSE 'REGIMEN NORMAL'
    END																																	AS "RegimenFondoDestino"
    ,CASE WHEN SolicitudAporteRescate."IdTipoMovimientoFondoMutuo" = 'RESREINV' AND Reinversion."IdSolicitudAporte" IS NULL AND COALESCE(Reinversion."Porcentaje",0) != 0 THEN (SolicitudAporteRescate."MontoMonedaSistema"*Reinversion."Porcentaje")/100
    WHEN SolicitudAporteRescate."IdTipoMovimientoFondoMutuo" = 'RESREINV' AND Reinversion."IdSolicitudAporte" IS NOT NULL THEN Reinversion."MontoMonedaSistemaAporte"
    ELSE TO_DECIMAL(0,26,8)
    END																																	AS "MontoPesosDestino"
    ,CASE WHEN SolicitudAporteRescate."IdTipoMovimientoFondoMutuo" = 'RESREINV' THEN COALESCE(Reinversion."NumeroCuotasAporte",TO_DECIMAL(0,26,8))
    ELSE TO_DECIMAL(0,26,8)
    END																																	AS "MontoCuotasDestino"
    ,CASE WHEN SolicitudAporteRescate."IdTipoMovimientoFondoMutuo" = 'RESREINV' THEN COALESCE(Reinversion."ValorCuotaAporte",TO_DECIMAL(0,26,8))
    ELSE TO_DECIMAL(0,26,8)
    END																																	AS "ValorCuotaDestino"
    ,SolicitudAporteRescate."MontoComision"																								AS "MontoCuotasComision"
    ,SolicitudAporteRescate."Porcentaje"																								AS "PorcentajeOrigen"
    ,Reinversion."Porcentaje"																											AS "PorcentajeDestino"
    ,ConsultarCliente."NombreCompleto"																									AS "NombreCliente"
    ,SolicitudAporteRescate."IndicadorMLT"																								AS "ClasificacionMLT"
    ,CASE WHEN RegimenTributario."IdRegimenFondoMutuo" IN ('APV-A', 'APV-B', 'APV-BAN', 'BF', 'APV-AN') THEN 'COTIZACION VOLUNTARIA'
    WHEN RegimenTributario."GrupoRegimen" = 'DC' THEN 'DEPOSITO CONVENIDO'
    WHEN RegimenTributario."IdRegimenFondoMutuo" IN ('57B', '57T') THEN '57 BIS'
    ELSE 'REGIMEN NORMAL'
    END																																	AS "DescripcionTipoAhorro"
    ,CASE WHEN RegimenTributario."IdRegimenFondoMutuo" IN ('18Q', '18T') THEN 'REGIMEN NORMAL'								
   	ELSE RegimenTributario."Descripcion"	
   	END 																																AS "DescripcionTipoCuenta"
    ,TO_NVARCHAR(SolicitudAporteRescate."FechaValorCuota")																				AS "FechaValorCuota"
    ,SolicitudAporteRescate."NumeroCuotas"																								AS "MontoCuotas"
    ,SolicitudAporteRescate."MontoMonedaSistema"																						AS "MontoPesos"
    ,ConsultarEmisor."NombreCompleto"																									AS "NombreAdministradora"
    ,TO_DECIMAL(0,26,8)																													AS "OrigenValorCuota"
    ,CASE WHEN SolicitudAporteRescate."IdRegimenTributario"	IN ('BF', 'APV-A') THEN 'A'
    WHEN SolicitudAporteRescate."IdRegimenTributario" IN ('APV-BAN','APV-AN', 'APV-B') THEN 'B'
    WHEN RegimenTributario."GrupoRegimen" = 'DC' THEN ''
    ELSE SolicitudAporteRescate."IdRegimenTributario"
    END																																	AS "RegimenTributario"
    ,SerieDeFondo."CodigoSerie"																											AS "SerieFondo"
    ,CASE WHEN SolicitudAporteRescate."IdRegimenTributario" = '57B' OR SolicitudAporteRescate."IdRegimenTributario" = '57T' THEN '57B'
    WHEN RegimenTributario."GrupoRegimen" IN ('APV-A', 'APV-B') THEN 'CV'
    WHEN RegimenTributario."GrupoRegimen" = 'DC' THEN 'DC'
    ELSE 'RTN'	
    END																																	AS "TipoAhorro"
    ,SolicitudAporteRescate."IdRegimenTributario"																						AS "TipoCuenta"
    ,CASE WHEN SolicitudAporteRescate."IdTipoMovimientoFondoMutuo" = 'RESREINV' THEN
   		'REV'
    ELSE SolicitudAporteRescate."TipoSolicitud"
    END 																																AS "TipoOperacion"
    ,CASE WHEN SolicitudAporteRescate."Estado" != 'CRT' THEN ValorCuota."ValorCuota"
   	ELSE SolicitudAporteRescate."ValorCuota" 
   	END																																	AS "ValorCuota"
   	,SolicitudAporteRescate."IdTipoMovimientoFondoMutuo"																				AS "TipoMovimiento"
   	,CASE WHEN COALESCE(Portafolio."IdMatrizAgrupadorAPV",TO_BIGINT(0)) != 0 THEN COALESCE(Portafolio."IdMatrizAgrupadorAPV",TO_BIGINT(0)) 
   	ELSE COALESCE(MAT_NOAPV."IdMatrizAgrupadorNOAPV",TO_BIGINT(0)) 
   	END 																																AS "IdMatrizAgrupador"
   	,CASE WHEN COALESCE(Portafolio."IdMatrizAgrupadorAPV",0) != 0 THEN COALESCE(MatrizAgrupador."EsADC",'N') 
   	ELSE COALESCE(MAT_NOAPV."EsADC",'N') 
   	END																																	AS "EsADC"
   	,TO_TIMESTAMP((SolicitudAporteRescate."FechaIngresoRegistro"), 'YYYY-MM-DD HH24:MI:SS.FF7')											AS "FechaIngreso"
   	,TO_NVARCHAR(TO_DATE(COALESCE(Reinversion."FechaCertificacionAporte",SolicitudAporteRescate."FechaCertificacion")))                 AS "FechaCertificacion"
   	,TO_NVARCHAR(TO_DATE(COALESCE(Reinversion."FechaLiquidacionAporte",SolicitudAporteRescate."FechaLiquidacion")))                     AS "FechaLiquidacion"
   	,TO_NVARCHAR(TO_DATE(SolicitudAporteRescate."FechaAceptacion"))                                                                     AS "FechaCurse"
   	,TO_NVARCHAR(TO_DATE(SolicitudAporteRescate."FechaValorCuota"))                                                                     AS "FechaValorCuotaOrigen"
   	,TO_NVARCHAR(TO_DATE(COALESCE(Reinversion."FechaValorCuotaAporte",'0001-01-01')))                                                   AS "FechaValorCuotaDestino"
   	,SolicitudAporteRescate."IdSerieFondo"																								AS "IdSerieFondo"
   	,SerieDeFondoDes."IdSerieFondo"																										AS "IdSerieFondoDestino"
   	,SolicitudAporteRescate."IngresoAutomatico"																							AS "IngresoAutomatico"
   	,COALESCE(SolicitudAporteRescate."FolioContrato",0)																					AS "FolioContrato"
   	,COALESCE(SolicitudAporteRescate."IdTipoCanalIngreso",'')																			AS "Canal"
   	,COALESCE(TipoCanalIngreso."Nombre",'')																								AS "DescripcionCanal"
   	,COALESCE(SolicitudAporteRescate."IdMedioDePago", '')																				AS "IdMedioDePago"
   	,COALESCE(MedioDePago."Nombre",'')																									AS "DescripcionMedioPago"
   	,CASE SolicitudAporteRescate."TipoContratoOperacion" 
   	WHEN 'SAAPV' THEN COALESCE(SAAPV."IdContratoSAAPV",TO_BIGINT(0))
   	WHEN 'CSC' THEN COALESCE(CSC."IdFolioCSC",TO_BIGINT(0))
   	ELSE TO_BIGINT(0)
   	END																																	AS "IdContrato"
      ,TO_NVARCHAR(TO_DATE(COALESCE(Reinversion."FechaFinalReinversion",'0001-01-01')))                                                   AS "FechaFinalReinversion"
    ,SolicitudAporteRescate."IdIngresoEgreso"
    ,SolicitudAporteRescate."IdPortafolio"
    , UTCTOLOCAL(SolicitudAporteRescate."FechaIngresoRegistro")																			AS "FechaIngresoLocal"
FROM "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" AS SolicitudAporteRescate
LEFT JOIN "BSCL.FondoMutuo.BD::FMM.TipoMovimientoFondoMutuo" AS TipoMovimientoFondoMutuo ON TipoMovimientoFondoMutuo."IdTipoMovimientoFondoMutuo" = SolicitudAporteRescate."IdTipoMovimientoFondoMutuo"
LEFT JOIN "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" AS SerieDeFondo ON SerieDeFondo."IdSerieFondo" = SolicitudAporteRescate."IdSerieFondo"
LEFT JOIN "BSCL.FondoMutuo.BD::FMM.Fondo" AS Fondo ON Fondo."IdFondo" = SerieDeFondo."IdFondo"
LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio ON Portafolio."IdPortafolio" = SolicitudAporteRescate."IdPortafolio"
LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" AS MatrizAgrupador ON MatrizAgrupador."IdMatrizAgrupadorAPV" = Portafolio."IdMatrizAgrupadorAPV"
LEFT JOIN "BSCL.FondoMutuo.BD::FMM.RegimenFondoMutuo" AS RegimenTributario ON RegimenTributario."IdRegimenFondoMutuo" = SolicitudAporteRescate."IdRegimenTributario"
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS ConsultarCliente ON ConsultarCliente."IdCliente" = SolicitudAporteRescate."IdCliente"
LEFT JOIN "BSCL.Plataforma.BD.Vistas::MER.ConsultarEmisor" AS ConsultarEmisor ON ConsultarEmisor."IdEmisor" = SolicitudAporteRescate."IdEmisorOrigen"
LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorNOAPV" MAT_NOAPV ON MAT_NOAPV."CodigoPortafolio" = Portafolio."CodigoPortafolio" AND Portafolio."IdMatrizAgrupadorAPV" = 0
LEFT JOIN "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarSolicitudAporteRescateDetalleAgrupado" AS Reinversion ON SolicitudAporteRescate."IdSolicitudAporteRescate" = Reinversion."IdSolicitudRescate"
LEFT JOIN "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" AS SerieDeFondoDes ON SerieDeFondoDes."IdSerieFondo" = Reinversion."IdSerieFondo"
LEFT JOIN "BSCL.FondoMutuo.BD::FMM.Fondo" AS FondoDes ON FondoDes."IdFondo" = SerieDeFondoDes."IdFondo"
LEFT JOIN "BSCL.FondoMutuo.BD::FMM.ValorCuota" AS ValorCuota ON ValorCuota."IdSerieFondo" = SerieDeFondo."IdSerieFondo"
LEFT JOIN "BSCL.Plataforma.BD::PLA.TipoCanalIngreso" AS TipoCanalIngreso ON (TipoCanalIngreso."IdTipoCanalIngreso" = SolicitudAporteRescate."IdTipoCanalIngreso") 
LEFT JOIN "BSCL.Plataforma.BD::PAG.MedioDePago" AS MedioDePago ON (MedioDePago."IdMedioDePago" = SolicitudAporteRescate."IdMedioDePago")
LEFT JOIN "BSCL.APV.BD::SCS.ContratoSAAPV" SAAPV ON SAAPV."FolioContratoSAAPV" = SolicitudAporteRescate."FolioContratoOperacion" AND SolicitudAporteRescate."TipoContratoOperacion" = 'SAAPV'
LEFT JOIN "BSCL.FondoMutuo.BD::CSC.ContratoSuscripcionCuotas" CSC ON CSC."FolioCSC" = SolicitudAporteRescate."FolioContratoOperacion" AND SolicitudAporteRescate."TipoContratoOperacion" = 'CSC'
WHERE SolicitudAporteRescate."IdTipoMovimientoFondoMutuo" != 'APOREINV'
ORDER BY
	SolicitudAporteRescate."IdCliente"
   ,SolicitudAporteRescate."FechaEmision" DESC
   ,SolicitudAporteRescate."FolioContrato";
GRANT SELECT ON "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarSolicitudesFFMM" TO BCS_DESARROLLADOR WITH GRANT OPTION;