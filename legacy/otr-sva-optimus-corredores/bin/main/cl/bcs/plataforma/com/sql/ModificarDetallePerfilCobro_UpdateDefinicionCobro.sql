UPDATE "BSCL.Plataforma.BD::COM_2.DefinicionCobro"
SET "MontoFijo_MontoCobro"          = :MontoFijo_MontoCobro,
    "PorcentualFijo_ValorCobro"     = :PorcentualFijo_ValorCobro, 
    "PorcentualTasaPlazo_ValorCobro"= :PorcentualTasaPlazo_ValorCobro
WHERE "IdEmpresa"= :IdEmpresa AND "CodigoDefinicionCobro" = :CodigoDefinicionCobro