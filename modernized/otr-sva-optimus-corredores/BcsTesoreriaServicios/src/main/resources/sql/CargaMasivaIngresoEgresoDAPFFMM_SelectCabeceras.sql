SELECT TOP 1
	 "IdCabecera"
	,"IdCodigoTipo"
	,"IdEstado"
	,"FechaCreacion"
	,"IdUsuario"
	,"NombreArchivo"
FROM "BSCL.Plataforma.BD::FTP.Cabecera"
WHERE "IdCodigoTipo" IN (:IdCodigoTipo)
AND "NombreArchivo" = ('CDB_IngresoEgreso_' || :TipoArchivo || TO_NVARCHAR(:FechaEjecucion, '_YYYYMMDD') || '.csv')
AND "IdEstado" = :IdEstado
ORDER BY "IdCabecera";