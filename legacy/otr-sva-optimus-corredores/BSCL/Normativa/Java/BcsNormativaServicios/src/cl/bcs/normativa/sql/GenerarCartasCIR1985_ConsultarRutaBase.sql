SELECT 
     MAX(CASE WHEN UCASE("CodigoParametro") = UCASE('SubRutaArchivosModulo') THEN "Valor" ELSE NULL END) AS "RutaCircular"
    ,(MAX(CASE WHEN UCASE("CodigoParametro") = 'PLA.RUTABASEARCHIVOS'        THEN "Valor" ELSE NULL END) ||
      MAX(CASE WHEN UCASE("CodigoParametro") = UCASE('SubRutaArchivosModulo') THEN "Valor" ELSE NULL END)) AS "RutaArchivo"
    ,MAX(CASE WHEN UCASE("CodigoParametro") = UCASE('NOR.RUTACIRCULAR1985')       THEN "Valor" ELSE NULL END) AS "RutaSFTP"
FROM (
    SELECT UCASE("CodigoParametro") AS "CodigoParametro", "Valor"
    FROM "BSCL.Normativa.BD::NOR.Configuracion"
    WHERE UCASE("CodigoParametro") IN (UCASE('SubRutaArchivosModulo'), UCASE('NOR.RUTACIRCULAR1985'))
      AND "IdEmpresa" = :IdEmpresa
    UNION ALL
    SELECT UCASE("CodigoParametro") AS "CodigoParametro", "Valor"
    FROM "BSCL.Plataforma.BD::PLA.Configuracion"
    WHERE UCASE("CodigoParametro") = 'PLA.RUTABASEARCHIVOS'
      AND "IdEmpresa" = :IdEmpresa
)