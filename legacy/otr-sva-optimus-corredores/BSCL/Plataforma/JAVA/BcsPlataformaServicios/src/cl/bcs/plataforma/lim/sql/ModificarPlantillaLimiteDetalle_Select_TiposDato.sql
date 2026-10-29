SELECT 
	"COLUMN_NAME" AS "CampoModificado",
	"DATA_TYPE_NAME" AS "TipoDato"
FROM SYS.TABLE_COLUMNS
WHERE "TABLE_NAME" = 'BSCL.Plataforma.BD::LIM.Limite'
	AND "COLUMN_NAME" IN ('Nombre','IdMesa','IdCanalIngreso','IdTipoInstrumento','IdTipoOperacion',
		'IdTipoProducto','IdInstrumento','UnidadPlazo','Plazo','IdTipoReajuste','CodigoOperador',
		'MontoTotal','MontoIntraday','MontoOvernight','IdMoneda','IdMonedaCalculo','ClasificacionRiesgoMinima',
		'IdEstado','EsPersonalizada','EsTotalizador','IdAgente','IdClasificacionTipoInstrumento','Porcentaje')
