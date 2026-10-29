SELECT * 
FROM "BSCL.Plataforma.BD::FTP.Detalle"
WHERE "IdCabecera" IN (:IdCabecera)
ORDER BY "IdDetalle"