SELECT "IdCentralizacion", "Estado", "Nombre" AS "EstadoNombre"
FROM "BSCL.Contabilidad.BD::ITZ.Centralizacion" cen
INNER JOIN "BSCL.Contabilidad.BD::ITZ.EstadoContabilidad" ec ON ec."IdEstadoContabilidad" = cen."Estado" 
INNER JOIN "BSCL.Plataforma.BD::PLA.Estado" e ON e."IdEstado" = ec."IdEstado"
WHERE "IdCentralizacion" = :IdCentralizacion