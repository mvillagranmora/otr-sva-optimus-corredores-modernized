SELECT * 
FROM "BSCL.Plataforma.BD::FTP.Cabecera" 
WHERE "IdCodigoTipo" IN (:IdCodigoTipo)
AND "IdEstado" = 'PEN'
ORDER BY "IdCabecera"
