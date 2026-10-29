
UPDATE  DOCI 
SET     DOCI."Borrado" = COALESCE(:Borrado, DOCI."Borrado"),
		DOCI."FechaActualizacion" = :FechaActualizacion
FROM    "BSCL.Orden.BD::ORD.DocumentoInstruccion" AS DOCI
WHERE  DOCI."IdDocumentoInstruccion" IN (:IdDocumentoInstruccion);