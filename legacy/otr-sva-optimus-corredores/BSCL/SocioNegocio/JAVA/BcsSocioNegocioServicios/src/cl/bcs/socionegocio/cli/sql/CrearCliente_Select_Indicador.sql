SELECT TIND."IdTipoIndicador"
FROM "BSCL.Plataforma.BD::IND.TipoIndicador" TIND
WHERE "CodigoTipoIndicador" = :CodigoTipoIndicador AND TIND."IdEmpresa" = :IdEmpresa