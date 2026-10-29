SELECT PAI."IdPais" AS "c_idPais"
FROM "BSCL.Plataforma.BD::PLA.Configuracion" AS CON LEFT JOIN "BSCL.Plataforma.BD::REG.Pais" AS PAI ON CON."Valor" = PAI."CodigoPais"
WHERE CON."IdEmpresa" =:p_IdEmpresa AND CON."CodigoParametro" = 'PLA.LocalizacionSistema' AND CON."Valor" = PAI."CodigoPais";