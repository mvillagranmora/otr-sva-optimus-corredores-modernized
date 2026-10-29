UPDATE CMD
SET CMD."IdEstado" = 'PRO'
FROM 	"BSCL.Plataforma.BD::PLA.CargaMasivaDetalle" AS CMD
WHERE CMD."IdCargaMasiva" = :IdCargaMasiva;
