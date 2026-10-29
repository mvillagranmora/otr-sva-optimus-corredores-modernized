SELECT 
	DOCI."IdDocumentoInstruccion",
	DOCI."IdInstruccion",
	DOCI."Borrado",
	DOCI."FechaActualizacion"
FROM "BSCL.Orden.BD::ORD.DocumentoInstruccion" DOCI
WHERE DOCI."IdDocumentoInstruccion" IN (:IdsDocumentosInstruccion);