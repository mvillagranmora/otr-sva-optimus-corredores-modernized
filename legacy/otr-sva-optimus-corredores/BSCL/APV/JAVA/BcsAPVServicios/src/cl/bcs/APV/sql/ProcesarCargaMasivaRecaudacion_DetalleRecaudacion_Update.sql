UPDATE "BSCL.APV.BD::APV.CargaMasivaDetalleRecaudacion"
SET 
	  "MensajeValidacion" = :MensajeValidacion
	, "EstadoRendicion"   = :EstadoRendicion
	, "EstadoProceso"     = :EstadoProceso
WHERE "FolioCartola" = :FolioCartola
AND   "FolioLineaCartola" = :FolioLineaCartola
AND   "RUTEmpleador" = :RUTEmpleador
AND   "RUTCliente" = :RUTCliente
