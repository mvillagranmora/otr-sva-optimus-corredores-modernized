SELECT 
	"COLUMN_NAME" AS "CampoModificado",
	"DATA_TYPE_NAME" AS "TipoDato"
FROM SYS.TABLE_COLUMNS
WHERE "TABLE_NAME" = 'BSCL.Plataforma.BD::LIM.PlantillaLimite'
	AND "COLUMN_NAME" IN ('DescripcionPlantillaLimite', 'IdEstado')
