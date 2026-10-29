SELECT
	CIR."SecuenciaSolicitud",
    CIR."IdPlanTipoInversion",
    CIR."Codigo",
    CIR."IdPortafolio",
    CIR."DocumentoIdentidad",
    CIR."TipoMovimiento",
    CASE WHEN SOL."TipoSolicitud" = 'APO' THEN EntidadRecaudadora."Nombre"
         ELSE TRA."NombreEntidad"
    END AS "NombreEntidadRecaudadora",
    CASE WHEN SOL."TipoSolicitud" = 'APO' THEN DocumentoIdentidad."DocumentoIdentidad"
         ELSE TRA."DocumentoEntidad"
    END AS "DocumentoIdentidadRecaudadora",
    SOL."IdSolicitudAporteRescate",
    SOL."IdSerieFondo",
    TO_VARCHAR(SOL."FechaCertificacion",'YYYY-MM-DD') AS "FechaCertificacion",
    TO_VARCHAR(SOL."FechaLiquidacion",'YYYY-MM-DD') AS "FechaLiquidacion",
    TO_VARCHAR(SOL."FechaValorCuota",'YYYY-MM-DD') AS "FechaValorCuota",
    TO_VARCHAR(SOL."FechaAceptacion",'YYYY-MM-DD') AS "FechaAceptacion"
FROM "BSCL.Normativa.BD::CIR.DatosEstadisticaCircular1981" AS CIR
LEFT JOIN "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" AS SOL
    ON SOL."IdSolicitudAporteRescate" = CIR."IdTraspaso"
LEFT JOIN "BSCL.APV.BD::TRA.Traspaso" AS TRA
    ON TRA."IdTraspaso" = SOL."FolioContrato"
LEFT JOIN "BSCL.APV.BD::SCS.ContratoSAAPV" AS SAAPV
    ON SAAPV."FolioContratoSAAPV" = SOL."FolioContratoOperacion"
LEFT JOIN "BSCL.APV.BD::SCS.EntidadRecaudadora" AS EntidadRecaudadora
    ON EntidadRecaudadora."IdEntidadRecaudadora" = SAAPV."IdEntidadRecaudadora"
INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS por
    ON por."IdPortafolio" = CIR."IdPortafolio" AND por."IdEstado" = 'ACT'
LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS DocumentoIdentidad
    ON DocumentoIdentidad."IdPersona" = EntidadRecaudadora."IdPersona"
WHERE CIR."EsTraspaso" = 'S' AND CIR."SecuenciaSolicitud" = :SecuenciaSolicitud;