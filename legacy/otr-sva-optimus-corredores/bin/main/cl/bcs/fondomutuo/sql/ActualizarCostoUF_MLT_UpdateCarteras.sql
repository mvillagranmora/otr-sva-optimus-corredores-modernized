	UPDATE c
	SET c."CostoInversionEnFactorAjuste"    = CASE WHEN MLT."TipoSolicitud"='APO' THEN MLT."PRO_CostoInversionEnFactorAjuste" ELSE c."CostoInversionEnFactorAjuste" END
	   ,c."EGRCostoInversionEnFactorAjuste" = CASE WHEN MLT."TipoSolicitud"='RES' THEN MLT."PRO_EGRCostoInversionEnFactorAjuste" ELSE c."EGRCostoInversionEnFactorAjuste" END
	FROM "BSCL.Custodia.BD::CUS.Cartera" c
	INNER JOIN
	
	(
		SELECT m."TipoSolicitud",m."IdCartera",m."PRO_EGRCostoInversionEnFactorAjuste",m."PRO_CostoInversionEnFactorAjuste"
		FROM "BSCL.Normativa.BD::SIS.ProcesoMandatos" m
		WHERE 
			m."IdCartera" =:IdCartera
			AND m."IdSolicitudAporteRescate"=:IdSolicitudAporteRescate
			AND m."TipoSolicitud"=:TipoSolicitud
			AND m."IdPortafolio"=:IdPortafolio
			AND m."IdEmpresa"=:IdEmpresa
			AND m."IdEstado" ='ING'
	)MLT	
	ON (MLT."IdCartera"=c."IdCartera")
	WHERE c."IdClasificacionTipoInstrumento" = 'FM'	;