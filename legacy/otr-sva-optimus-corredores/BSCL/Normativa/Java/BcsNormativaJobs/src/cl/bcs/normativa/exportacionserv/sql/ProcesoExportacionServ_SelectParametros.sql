SELECT SOLPAR."IdSolicitudCircular", SOLPAR."TipoParametro", TIPPAR."Nombre", SOLPAR."Valor"
FROM "BSCL.Normativa.BD::NOR.SolicitudParametros" AS SOLPAR
INNER JOIN "BSCL.Normativa.BD::NOR.TipoParametros" AS TIPPAR ON (SOLPAR."TipoParametro" = TIPPAR."TipoParametro")
WHERE SOLPAR."IdSolicitudCircular" IN (:IdSolicitudCircular) 
AND SOLPAR."Valor" != '' AND SOLPAR."Valor" IS NOT NULL AND TIPPAR."EsMasivo" = 'N' AND TIPPAR."EsInterno" = 'N'
AND TIPPAR."TipoParametro" NOT IN  ('EMP', 'NOMSAL', 'EXTSAL', 'COMSAL', 'NOMCOLSAL', 'PAUTA_NCG380', 'PAUTA_NCG380_NUM', 'EsPauta');