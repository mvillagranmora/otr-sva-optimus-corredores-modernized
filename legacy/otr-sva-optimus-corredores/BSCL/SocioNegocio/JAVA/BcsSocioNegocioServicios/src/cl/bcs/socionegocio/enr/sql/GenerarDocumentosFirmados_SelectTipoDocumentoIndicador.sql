SELECT TDI."IdTipoIndicador"
     , TI."CodigoTipoIndicador"
     , TI."IdTipoPersona"   
     , TDI."IdTipoDocumento"
FROM "BSCL.Plataforma.BD::DOC.TipoDocumentoIndicador" TDI
     LEFT JOIN "BSCL.Plataforma.BD::IND.TipoIndicador" TI ON (TI."IdTipoIndicador" = TDI."IdTipoIndicador")
WHERE TDI."IdEmpresa" = :IdEmpresa AND TDI."IdTipoDocumento" IN(:IdTipoDocumentos);