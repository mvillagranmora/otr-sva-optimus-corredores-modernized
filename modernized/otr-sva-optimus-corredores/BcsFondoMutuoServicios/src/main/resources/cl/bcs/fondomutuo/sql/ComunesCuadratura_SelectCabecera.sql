SELECT 
"IdCabecera"
,"IdCodigoTipo"
,"IdEstado"
,"FechaCreacion"
,"IdUsuario" 
FROM "BSCL.Plataforma.BD::FTP.Cabecera" 
WHERE "IdCodigoTipo" = :IdCodigoTipo
AND "IdEstado" = 'PEN'
ORDER BY "IdCabecera"