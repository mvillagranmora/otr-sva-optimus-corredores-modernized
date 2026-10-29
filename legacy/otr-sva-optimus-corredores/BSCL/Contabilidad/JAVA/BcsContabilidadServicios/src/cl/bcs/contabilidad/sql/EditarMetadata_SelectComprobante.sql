SELECT DISTINCT "IdTipoContabilidad","DebeHaber","Ejercicio","Formato" 
FROM "BSCL.Contabilidad.BD::ITZ.ContabilidadDefinicionAsiento" 
WHERE "IdMetadataCuentaContable" = :IdMetadataCuentaContable 