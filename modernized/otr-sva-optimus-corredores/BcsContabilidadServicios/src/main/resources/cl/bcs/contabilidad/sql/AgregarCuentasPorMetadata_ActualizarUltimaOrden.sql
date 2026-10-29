UPDATE "BSCL.Contabilidad.BD::ITZ.ContabilidadDefinicionAsiento"
SET "Orden" = "Orden" + 1 + :Index
WHERE "Orden" >= :Orden
AND "Estado" NOT IN ('A','E');