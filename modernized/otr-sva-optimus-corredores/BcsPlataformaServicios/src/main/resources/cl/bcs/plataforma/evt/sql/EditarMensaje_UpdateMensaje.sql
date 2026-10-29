UPDATE "BSCL.Plataforma.BD::EVT.Mensaje"
SET "IdTipoMensaje" = :IdTipoMensaje
,"IdTipoEntidadDestinatario" = :IdTipoEntidadDestinatario
,"Titulo" = :Titulo
,"MensajeCorto" = :MensajeCorto
,"MensajeLargo" = :MensajeLargo
,"FechaVigenciaDesde" = :FechaVigenciaDesde
,"FechaVigenciaHasta" = :FechaVigenciaHasta
,"Habilitado" = :Habilitado
,"MuestraMensajeCorto" = :MuestraMensajeCorto
,"UrlAviso" = :UrlAviso
,"IdTipoCanal" = :IdTipoCanal
WHERE "IdMensaje" = :IdMensaje;