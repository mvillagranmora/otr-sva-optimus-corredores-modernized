SELECT "IdTipoIndicador"
FROM "BSCL.Plataforma.BD::IND.TipoIndicador"
WHERE "CodigoTipoIndicador" = :CodigoTipoIndicador AND "IdEmpresa" = :IdEmpresa AND "IdEstado" = 'ACT';