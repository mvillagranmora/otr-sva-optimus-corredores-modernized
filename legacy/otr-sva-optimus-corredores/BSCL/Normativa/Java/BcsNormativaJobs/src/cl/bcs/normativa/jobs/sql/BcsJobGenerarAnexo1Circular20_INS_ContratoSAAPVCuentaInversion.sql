INSERT INTO "BSCL.Normativa.BD::CIR.DatosEstadisticaCircular1981"
( 
    "IdDatosEstadisticaCircular1981", 
    --parametros entrada
    "IdEmpresa","Periodo","FechaEjecucion","FechaInicioBusqueda","FechaFinBusqueda",
    "SecuenciaSolicitud","IdUsuario",
    -----------------------
    "EsTraspaso","IdContratoSAAPV","IdPortafolioContratoSAAPV","IdEstado",
    "IdMatrizAgrupadorAPV","IdPlanTipoInversion","NombrePlan","Codigo",
    "IdCliente","IdPersona","IdPortafolio","TipoDocumentoIdentidad",
    "IdDocumentoIdentidad","DocumentoIdentidad","NombreCompleto","IdTipoPersona",
    "Sexo","FechaNacimiento","IdCargoAbonoMovimiento","IdTipoMovimiento",
    "IdConcepto","FechaCertificacion","FechaCertificacionHora","Monto")
SELECT
    "BSCL.Normativa.BD.Secuencias::CIR.DatosEstadisticaCircular1981".NextVal,
    --parametros entrada
    :IdEmpresa,SUBSTRING(:FechaEjecucion,1,7),:FechaEjecucion,
    :FechaInicioBusqueda,:FechaFinBusqueda,:SecuenciaSolicitud,:IdUsuario,
    ----------------------
    'N' AS "EsTraspaso",ccsci."IdContratoSAAPV",ccsci."IdPortafolioContratoSAAPV",ccsci."IdEstado",
    ccsci."IdMatrizAgrupadorAPV",ccsci."IdPlanTipoInversion",ccsci."NombrePlan",ccsci."Codigo",
    ccsci."IdCliente",ccsci."IdPersona",ccsci."IdPortafolio",ccsci."TipoDocumentoIdentidad",
    ccsci."IdDocumentoIdentidad",ccsci."DocumentoIdentidad",ccsci."NombreCompleto",ccsci."IdTipoPersona",
    ccsci."Sexo",ccsci."FechaNacimiento",ccsci."IdCargoAbonoMovimiento",
    CASE WHEN COALESCE(ccsci."IdTipoMovimiento",'') = '' AND sa."TipoSolicitud" = 'APO' THEN 'MOVCAR'
    	 WHEN COALESCE(ccsci."IdTipoMovimiento",'') = '' AND sa."TipoSolicitud" = 'RES' THEN 'MOVABN'
    	 ELSE ccsci."IdTipoMovimiento" END AS "IdTipoMovimiento",
    ccsci."IdConcepto",
    TO_CHAR(COALESCE(ccsci."FechaCertificacion",sa."FechaCertificacion"), 'YYYY-MM-DD') AS "FechaCertificacion",
    COALESCE(ccsci."FechaCertificacion",TO_SECONDDATE(sa."FechaCertificacion", 'YYYY-MM-DD')) AS "FechaCertificacionHora" ,
    CASE WHEN COALESCE(ccsci."Monto",0) = 0 THEN sa."Monto" ELSE ccsci."Monto" END AS "Monto"
FROM "BSCL.APV.BD.Vistas::SCS.ConsultarContratoSAAPVCuentaInversion" ccsci
LEFT JOIN "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" AS sa ON (sa."IdPortafolio" = ccsci."IdPortafolio" AND 
																	sa."FolioContrato" = ccsci."IdContratoSAAPV" AND 
																	sa."IdCliente" = ccsci."IdCliente" AND
																	sa."FechaCertificacion" >= :FechaInicioBusqueda AND sa."FechaCertificacion" <= :FechaFinBusqueda AND
																	sa."FechaLiquidacion" >= :FechaInicioBusqueda AND sa."FechaLiquidacion" <= :FechaFinBusqueda)
WHERE ccsci."IdEmpresa"  = :IdEmpresa
      AND ccsci."IdEstado" = 'VIG'
      AND ccsci."IdTipoPersona" = 'N'
      AND (ccsci."Codigo" = 'APV-A' OR ccsci."Codigo" = 'APV-B' OR ccsci."Codigo" = 'DC-R')
      AND ccsci."IdCargoAbonoMovimiento" NOT IN (SELECT "IdCargoAbonoMovimiento"
                                           FROM "BSCL.Normativa.BD::CIR.DatosEstadisticaCircular1981"
                                           WHERE "SecuenciaSolicitud" = :SecuenciaSolicitud
                                             AND "EsTraspaso" = 'S'); --no debe considerar los traspasos