UPDATE CMD
SET CMD."IdEstado" = 'PRO'
FROM 	"BSCL.Custodia.BD::CUS.CargaMasivaDetalle" AS CMD
WHERE CMD."IdCargaMasiva" = :IdCargaMasiva;