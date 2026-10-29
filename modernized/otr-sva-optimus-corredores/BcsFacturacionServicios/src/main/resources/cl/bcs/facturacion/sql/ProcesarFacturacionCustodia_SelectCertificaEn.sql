SELECT "IdTipoOperacion","IdAccionOperacion","PoseeInstrumentoSubyacente","PoseeInstrumentoRelacionado","CertificaEn"
FROM "BSCL.Custodia.BD::CUS.ConfiguracionMovimientoCustodia"
WHERE "IdTipoOperacion" IN (:idsTipoOperacion)
