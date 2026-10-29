SELECT "TipoParametro", "Valor" FROM "BSCL.Normativa.BD::NOR.SolicitudParametros"
WHERE "TipoParametro" IN ('NOMSAL','PAUTA_NCG380_NUM') AND "IdSolicitudCircular" = :IdSolicitudCircular;