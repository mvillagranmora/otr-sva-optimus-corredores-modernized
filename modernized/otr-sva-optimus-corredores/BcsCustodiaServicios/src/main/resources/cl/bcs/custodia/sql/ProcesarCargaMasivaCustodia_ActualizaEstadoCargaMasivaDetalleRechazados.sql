UPDATE CMD
SET CMD."IdEstado" = 'RZD', 
	CMD."ObservacionValidacion" = :ObservacionValidacion
FROM "BSCL.Custodia.BD::CUS.CargaMasivaDetalle" AS CMD
WHERE CMD."IdCargaMasiva" = :IdCargaMasiva 
	AND CMD."IdCargaMasivaDetalle" = :IdCargaMasivaDetalle;