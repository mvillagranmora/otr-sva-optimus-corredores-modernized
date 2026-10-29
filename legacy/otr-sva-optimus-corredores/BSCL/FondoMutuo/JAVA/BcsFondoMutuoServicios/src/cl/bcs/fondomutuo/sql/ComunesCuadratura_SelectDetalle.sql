SELECT
"IdDetalle",
"IdCabecera",
"Linea"
FROM "BSCL.Plataforma.BD::FTP.Detalle"
WHERE "IdCabecera" = :IdCabecera
ORDER BY "IdDetalle"