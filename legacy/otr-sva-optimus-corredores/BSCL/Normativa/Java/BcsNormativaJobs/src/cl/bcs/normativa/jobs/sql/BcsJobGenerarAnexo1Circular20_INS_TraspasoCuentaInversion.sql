INSERT INTO "BSCL.Normativa.BD::CIR.DatosEstadisticaCircular1981"
( 
    "IdDatosEstadisticaCircular1981", 
    --parametros entrada
    "IdEmpresa","Periodo","FechaEjecucion","FechaInicioBusqueda","FechaFinBusqueda",
    "SecuenciaSolicitud","IdUsuario",
    -----------------------
    "EsTraspaso","IdTraspaso","TipoMovimiento","IdContratoSAAPV","IdCliente",
    "IdPortafolio","IdTipoMovimiento",
    "IdConcepto","FechaCertificacion","FechaCertificacionHora",
    "Monto","IdPersona","IdMatrizAgrupadorAPV","IdPlanTipoInversion","NombrePlan",
    "Codigo","TipoDocumentoIdentidad","IdDocumentoIdentidad","DocumentoIdentidad","NombreCompleto",
    "IdTipoPersona","Sexo","FechaNacimiento")
SELECT "BSCL.Normativa.BD.Secuencias::CIR.DatosEstadisticaCircular1981".NextVal,
	    --parametros entrada
	    :IdEmpresa,SUBSTRING(:FechaEjecucion,1,7),:FechaEjecucion,
	    :FechaInicioBusqueda,:FechaFinBusqueda,:SecuenciaSolicitud,:IdUsuario,
	    ----------
	    * FROM (
SELECT
		CASE WHEN S."IdTipoMovimientoFondoMutuo" IN ('APOTRASP') THEN 'S'
			ELSE 'N' END AS "EsTraspaso",
		CASE WHEN S."IdTipoMovimientoFondoMutuo" IN ('APOTRASP') THEN S."IdSolicitudAporteRescate"
			ELSE 0 END AS "IdTraspaso",
		'INGRESO' AS "TipoMovimiento",
		CASE WHEN S."IdTipoMovimientoFondoMutuo" IN ('APONORM') THEN S."IdSolicitudAporteRescate"
			ELSE 0 END AS "IdContratoSAAPV",
		S."IdCliente",	S."IdPortafolio",
		'MOVABN' AS "IdTipoMovimiento",
	    CASE WHEN S."IdRegimenTributario" = 'BF' THEN 'ABOBON'
	    	ELSE '' END AS "IdConcepto",
		S."FechaCertificacion",TO_CHAR(S."FechaCertificacion", 'YYYY-MM-DD HH24:MI:SS')  AS "FechaCertificacionHora",
		S."MontoMonedaSistema" AS "Monto",DI."IdPersona",P."IdMatrizAgrupadorAPV",
		CMA."IdPlanTipoInversion",CMA."NombrePlan", 
		CASE WHEN S."IdRegimenTributario" = 'APV-AN' THEN 'APV-B'
			 WHEN S."IdRegimenTributario" IN ('DC-DL','DC-AL','DC-R') THEN 'DC'
	    	 ELSE S."IdRegimenTributario" END AS "Codigo",
		PER."TipoDocumentoIdentidad",PER."IdDocumentoIdentidad",PER."DocumentoIdentidad",PER."NombreCompleto",
		PER."IdTipoPersona",PER."Sexo",PER."FechaNacimiento"
	FROM "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" AS S
	INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" 		PER ON (S."IdCliente" = PER."IdCliente")
	INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" 					P ON (S."IdPortafolio" = P."IdPortafolio") 
	INNER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarMatrizAgrupador" 	CMA ON (CMA."IdMatrizAgrupadorAPV" = P."IdMatrizAgrupadorAPV")
	LEFT JOIN  "BSCL.APV.BD::SCS.ContratoSAAPV" CSAAPV ON (CSAAPV."FolioContratoSAAPV" = S."FolioContratoOperacion" AND S."TipoContratoOperacion" = 'SAAPV' AND S."IdTipoMovimientoFondoMutuo" = 'APOTRASP') 
	LEFT JOIN  "BSCL.APV.BD::TRA.Traspaso" T ON (T."IdContratoSAAPV" = CSAAPV."IdContratoSAAPV")
	LEFT JOIN  "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" DI ON (DI."DocumentoIdentidad" = T."DocumentoEntidad")
	WHERE
		S."FechaValorCuota" BETWEEN :FechaInicioBusqueda AND :FechaFinBusqueda AND
		S."IdRegimenTributario" IN ('APV-A','APV-B','BF','DC-R','APV-AN','DC-DL','DC-AL') AND
		S."Estado" NOT IN ('ANU','ING','PEN') AND
		S."IdTipoMovimientoFondoMutuo" IN ('APONORM','APOTRASP')
UNION
	SELECT
		CASE WHEN S."IdTipoMovimientoFondoMutuo" IN ('RESTRASP') THEN 'S'
			ELSE 'N' END AS "EsTraspaso",
		CASE WHEN S."IdTipoMovimientoFondoMutuo" IN ('RESTRASP') THEN S."IdSolicitudAporteRescate"
			ELSE 0 END AS "IdTraspaso",
		'EGRESO' AS "TipoMovimiento",
		CASE WHEN S."IdTipoMovimientoFondoMutuo" IN ('RESNORM') THEN S."IdSolicitudAporteRescate"
			ELSE 0 END AS "IdContratoSAAPV",
		S."IdCliente",	S."IdPortafolio",
		'MOVCAR' AS "IdTipoMovimiento",
	    CASE WHEN S."IdRegimenTributario" = 'BF' THEN 'ABOBON'
	    	ELSE '' END AS "IdConcepto",
		S."FechaCertificacion",TO_CHAR(S."FechaCertificacion", 'YYYY-MM-DD HH24:MI:SS')  AS "FechaCertificacionHora",
		S."MontoMonedaSistema" AS "Monto",DI."IdPersona",P."IdMatrizAgrupadorAPV",
		CMA."IdPlanTipoInversion",CMA."NombrePlan", 
		CASE WHEN S."IdRegimenTributario" = 'APV-AN' THEN 'APV-B'
			 WHEN S."IdRegimenTributario" IN ('DC-DL','DC-AL','DC-R') THEN 'DC'
	    	 ELSE S."IdRegimenTributario" END AS "Codigo",
		PER."TipoDocumentoIdentidad",PER."IdDocumentoIdentidad",PER."DocumentoIdentidad",PER."NombreCompleto",
		PER."IdTipoPersona",PER."Sexo",PER."FechaNacimiento"
	FROM "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" AS S
	INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" 		PER ON (S."IdCliente" = PER."IdCliente")
	INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" 					P ON (S."IdPortafolio" = P."IdPortafolio") 
	INNER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarMatrizAgrupador" 	CMA ON (CMA."IdMatrizAgrupadorAPV" = P."IdMatrizAgrupadorAPV")
	LEFT JOIN "BSCL.APV.BD::TRA.OrdenTraspaso" OT ON (S."IdSolicitudAporteRescate"=OT."IdSolicitudAporteRescate" AND S."IdTipoMovimientoFondoMutuo" = 'RESTRASP')
	LEFT JOIN  "BSCL.APV.BD::TRA.Traspaso" T ON (OT."IdTraspaso" = T."IdTraspaso"  OR  T."IdTraspaso" = S."FolioContrato")
	LEFT JOIN  "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" DI ON (DI."DocumentoIdentidad" = T."DocumentoEntidad")
	WHERE
		S."FechaValorCuota" BETWEEN :FechaInicioBusqueda AND :FechaFinBusqueda AND
		S."IdRegimenTributario" IN ('APV-A','APV-B','BF','DC-R','APV-AN','DC-DL','DC-AL') AND
		S."Estado" NOT IN ('ANU','ING','PEN') AND
		S."IdTipoMovimientoFondoMutuo" IN ('RESNORM','RESTRASP')
);