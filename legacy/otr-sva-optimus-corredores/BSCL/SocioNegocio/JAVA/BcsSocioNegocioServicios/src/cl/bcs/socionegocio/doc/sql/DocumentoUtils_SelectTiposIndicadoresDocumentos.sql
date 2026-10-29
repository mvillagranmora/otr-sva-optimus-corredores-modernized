SELECT TI."IdTipoIndicador"
     , TDI."IdTipoDocumentoIndicador"
     , TDI."IdTipoDocumento"
     , TDI."IdEmpresa"
FROM "BSCL.Plataforma.BD::DOC.TipoDocumentoIndicador" TDI
     LEFT JOIN "BSCL.Plataforma.BD::IND.TipoIndicador" TI ON (TI."IdTipoIndicador" = TDI."IdTipoIndicador" AND TI."IdEmpresa" = TDI."IdEmpresa")
WHERE TDI."IdEmpresa" = :IdEmpresa
  AND TDI."IdTipoDocumento" IN (:IdsTiposDocumentos);