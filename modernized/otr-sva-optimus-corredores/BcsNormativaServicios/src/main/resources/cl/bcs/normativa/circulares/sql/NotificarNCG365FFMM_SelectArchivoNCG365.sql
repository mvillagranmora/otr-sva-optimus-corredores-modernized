SELECT ANC1."IdCliente", ANC1."NombreArchivo", ANC1."UbicacionZip", ANC1."FechaCertificacion", ANC1."Archivo" FROM "BSCL.Normativa.BD::CIR.ArchivoNCG365" ANC1
JOIN (SELECT "IdCliente", max("IdArchivoNCG365") AS "IdArchivoNCG365" FROM "BSCL.Normativa.BD::CIR.ArchivoNCG365"
		WHERE "FechaCertificacion" = :FechaCertificacion AND "UbicacionZip" LIKE '%NCG365FM_________.zip'
		GROUP BY "IdCliente") ANC2 ON ANC1."IdArchivoNCG365"=ANC2."IdArchivoNCG365";