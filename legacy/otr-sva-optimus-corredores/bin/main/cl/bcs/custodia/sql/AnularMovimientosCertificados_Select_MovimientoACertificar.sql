SELECT  "IdMovimientoCarteraEnTransito","IdMovimientoCarteraEnTransitoRelacionado","IdTipoOperacion", "PermiteCertificacionPorUsuario", "EsNominal", "MueveCustodia" 
FROM "BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito" 
WHERE "IdMovimientoCarteraEnTransito" = :IdMovimientoCarteraEnTransito