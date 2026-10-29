SELECT 
	COL."DATA_TYPE_NAME",
	COL."COLUMN_NAME"
FROM SYS.TABLE_COLUMNS COL
WHERE (COL."TABLE_NAME" = 'BSCL.Plataforma.BD::COM.DetallePerfilCobros'
       OR (COL."TABLE_NAME" = 'BSCL.Plataforma.BD::COM.ValorCobro' 
           AND COL."COLUMN_NAME" IN ('EsPersonalizado','IdCobro','ValorCobro','ValorTopeMinimoOrden')))
	AND COL."COLUMN_NAME" = :CampoModificado
