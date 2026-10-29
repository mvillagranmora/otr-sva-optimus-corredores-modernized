SELECT 
"IdInstrumento",
"Nemotecnico",
"NombreCompletoEmisor",
"RazonSocial",
"IdClasificacionTipoInstrumento",
"IdTipoInstrumento",
"IdEstado",
"Rentabilidad7Dias", 
"Rentabilidad30Dias",
"Rentabilidad52Sem",
"EstaInscrito",
"EsAutorizado",
"EsInstrumentoCalificado",
"MonedaDeReajuste" ,
"MonedaDePago"
FROM "BSCL.Plataforma.BD.Vistas::INS.WS.ConsultarInstrumento"
WHERE  (:FiltraIdInstrumento = 'N' OR "IdInstrumento" IN (:IdInstrumento))
AND (:FiltraIdClasificacionTipoInstrumento = 'N' OR "IdClasificacionTipoInstrumento" IN (:IdClasificacionTipoInstrumento))
AND (:FiltraIdTipoInstrumento = 'N' OR "IdTipoInstrumento" IN (:IdTipoInstrumento))
AND (:FiltraNemotecnico = 'N' OR "Nemotecnico" IN (:Nemotecnico))
AND (:FiltraEsInscrito = 'N' OR "EstaInscrito" IN (:EstaInscrito))
AND (:FiltraEsAutorizado = 'N' OR "EsAutorizado" IN (:EsAutorizado))
AND (:FiltraEsInstrumentoCalificado = 'N' OR "EsInstrumentoCalificado" IN (:EsInstrumentoCalificado))
AND (:FiltraIdEstado = 'N' OR "IdEstado" IN (:IdEstado))
AND ("IdClasificacionTipoInstrumento" != 'RV' OR :IncluyeNemoNegociacion = 'S' OR "IdInstrumento" IN (SELECT "IdInstrumento" FROM "BSCL.Plataforma.BD::INS.Instrumento" WHERE "IdInstrumentoRaiz" IS NULL OR "IdInstrumentoRaiz" = 0));