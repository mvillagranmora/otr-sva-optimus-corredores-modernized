SELECT DISTINCT EVENTO."IdTipoEvento",EVENTO."Operador"
FROM "BSCL.Contabilidad.BD::ITZ.ContabilidadDefinicionTipoEvento" EVENTO
INNER JOIN "BSCL.Contabilidad.BD::ITZ.ContabilidadDefinicionAsiento" ASIENTO ON (ASIENTO."IdDefinicionAsiento" = EVENTO."IdDefinicionAsiento")
WHERE ASIENTO."IdMetadataCuentaContable" = :IdMetadataCuentaContable AND ASIENTO."IdTipoContabilidad" = :IdTipoContabilidad