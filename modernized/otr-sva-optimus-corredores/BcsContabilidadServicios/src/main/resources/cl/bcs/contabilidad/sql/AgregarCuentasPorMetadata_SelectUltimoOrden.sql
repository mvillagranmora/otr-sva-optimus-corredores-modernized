SELECT MAX("Orden") AS "Max" FROM "BSCL.Contabilidad.BD::ITZ.ContabilidadDefinicionAsiento"
WHERE "IdTipoContabilidad" = :IdTipoContabilidad;