UPDATE "BSCL.Custodia.BD::CUS.Cartera" 
SET 
    "IdEstado"				= 'MOD' 
WHERE "IdOperacion" IN (:IdOperacion) 
	AND "IdTipoProducto" = 'MEFW'
	AND "IdEstado"='PEN';