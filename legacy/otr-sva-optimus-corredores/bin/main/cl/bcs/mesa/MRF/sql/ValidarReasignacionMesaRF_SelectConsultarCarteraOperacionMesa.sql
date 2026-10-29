SELECT 	"FolioCarteraCustodiaDetalle",
		"NemotecnicoDetalle",
		"IdClasificacionTipoInstrumentoDetalle",
		"TasaCompraDetalle",
		"CantidadReservadaDetalle",
		"ValorRescateDetalle",
		"CorteMinimoDetalle",
		CAST ("FechaOperacion" AS DATE) AS "FechaOperacion"
FROM "BSCL.Mesa.BD.Vistas::MRF.ConsultarDetalleOperacionesMesaRF"
WHERE "FolioOperacion" = :FolioOperacion AND "FolioCarteraCustodiaDetalle" = :FolioCartera