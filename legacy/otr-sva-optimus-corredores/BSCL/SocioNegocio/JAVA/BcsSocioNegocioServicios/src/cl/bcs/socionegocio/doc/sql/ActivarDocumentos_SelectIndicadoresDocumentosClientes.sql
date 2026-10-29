SELECT IND."IdIndicador"          AS "IdIndicador"
     , CIDC."IdCliente"           AS "IdEntidad"
     , IND."IdTipoEntidad"        AS "IdTipoEntidad"
     , CIDC."IdTipoIndicador"     AS "IdTipoIndicador"
     , IND."ModeloCliente"        AS "ModeloCliente"
     , CIDC."IdEmpresa"           AS "IdEmpresa"
     , IND."ValorIndicador"       AS "ValorIndicador"
     , IND."FechaIngreso"         AS "FechaIngreso"
     , IND."FechaCreacion"        AS "FechaCreacion"
     , IND."FechaModificacion"    AS "FechaModificacion"
     , IND."IdEstadoAutorizacion" AS "IdEstadoAutorizacion"
     , IND."FechaVencimiento"     AS "FechaVencimiento"
     , IND."IdCarga"              AS "IdCarga"
FROM "BSCL.Plataforma.BD::DOC.ConfirmarIndicadorDocumentoCliente" CIDC
     LEFT JOIN "BSCL.Plataforma.BD::IND.Indicador" IND ON (
        IND."IdTipoIndicador" = CIDC."IdTipoIndicador"
        AND IND."IdEmpresa" = CIDC."IdEmpresa"
        AND IND."IdEntidad" = CIDC."IdCliente"
        AND IND."IdTipoEntidad" = 'CLIENTE'
    )
WHERE CIDC."IdTrabajo" = :IdTrabajo;