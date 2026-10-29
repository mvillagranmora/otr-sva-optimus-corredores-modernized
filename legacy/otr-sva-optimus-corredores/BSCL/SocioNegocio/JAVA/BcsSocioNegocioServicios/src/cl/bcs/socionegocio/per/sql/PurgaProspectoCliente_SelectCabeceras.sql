SELECT TOP 1
	 "IdCabecera"
	,"IdCodigoTipo"
	,"IdEstado"
	,"FechaCreacion"
	,"IdUsuario"
	,"NombreArchivo"
FROM "BSCL.Plataforma.BD::FTP.Cabecera"
WHERE "IdCodigoTipo" IN (:IdCodigoTipo)
AND "NombreArchivo" = ('PURGA_PROSPECTOS_' || TO_NVARCHAR(:FechaEjecucion, 'YYYYMMDD') || '.txt')
AND "IdEstado" = :IdEstado
ORDER BY "IdCabecera";