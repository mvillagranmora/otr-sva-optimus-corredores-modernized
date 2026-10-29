SELECT 
	COL."DATA_TYPE_NAME",
	COL."COLUMN_NAME",
	:CampoModificado AS "CampoModificado",
	'' AS "ValorNuevo"
FROM SYS.TABLE_COLUMNS COL
WHERE COL."TABLE_NAME" = 'BSCL.Plataforma.BD::COM.DetalleGrupoActivos'
  AND COL."COLUMN_NAME" = :CampoModificado
