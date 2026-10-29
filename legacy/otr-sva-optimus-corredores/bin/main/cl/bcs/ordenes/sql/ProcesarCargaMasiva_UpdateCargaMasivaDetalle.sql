
UPDATE "BSCL.Orden.BD::CAR.CargaMasivaDetalle"
SET 
	"IdEstado" = 'PRO',							  
	"IdOperacionGenerada" 		= :IdOperacionGenerada,
	"ObservacionValidacion" 	= :Mensaje
WHERE "IdCargaMasivaDetalle" 	= :IdInterno;