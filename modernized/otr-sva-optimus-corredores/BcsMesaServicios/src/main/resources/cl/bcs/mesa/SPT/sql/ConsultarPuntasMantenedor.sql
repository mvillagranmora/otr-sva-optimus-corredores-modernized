SELECT
    TOP 1 COALESCE(PU."IdPuntas", 0) AS "IdPuntas",
    GEN."IdMoneda" AS "IdMonedaPunta",
    CASE
        WHEN COALESCE(PU."IdPuntas", -1) > 0 THEN 'SI'
        ELSE 'NO'
    END AS "ExistePunta",
    PU."IdEmpresa",
    PU."Fecha",
    PU."IdUsuario",
    COALESCE(PU."AperturaPrecio", 0) AS "AperturaPrecio",
    COALESCE(PU."PuntaCompra", 0) AS "PuntaCompra",
    COALESCE(PU."PuntaVenta", 0) AS "PuntaVenta",
    PU."CheckPrecioPorTramo",
    PU."Estado",
    GEN."PrecioDecimales",
    COALESCE(SPT."MontoDecimales", GEN."MontoDecimales") AS "MontoDecimales",
    COALESCE(
        SPT."TipoCambioDecimales",
        GEN."TipoCambioDecimales"
    ) AS "TipoCambioDecimales",
    COALESCE(SPT."ParidadDecimales", GEN."ParidadDecimales") AS "ParidadDecimales",
    COALESCE(SPT."PuntosDecimales", GEN."PuntosDecimales") AS "PuntosDecimales",
    "Origen" AS "SistemaModificador"
FROM
    "BSCL.Plataforma.BD::PLA.MonedaFormato" GEN
    LEFT JOIN "BSCL.Mesa.BD::SPT.Puntas" PU ON PU."IdMonedaPunta" = GEN."IdMoneda"
    AND PU."Estado" = 'VIG'
    AND PU."IdEmpresa" = :IdEmpresa
    LEFT JOIN "BSCL.Plataforma.BD::PLA.MonedaFormato" SPT ON GEN."IdMoneda" = SPT."IdMoneda"
    AND SPT."IdUsoMonedaFormato" = 'SPT'
WHERE
    GEN."IdMoneda" = :IdMoneda
    AND GEN."IdUsoMonedaFormato" = 'GEN'
    AND GEN."IdTipoInstrumento" = ''
ORDER BY
    PU."Fecha" DESC;
