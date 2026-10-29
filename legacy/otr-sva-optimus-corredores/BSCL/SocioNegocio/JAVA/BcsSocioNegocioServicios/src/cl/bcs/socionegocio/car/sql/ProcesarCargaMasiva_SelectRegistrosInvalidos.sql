SELECT
	"IdCargaMasivaDetalle"
FROM "BSCL.SocioNegocio.BD::CAR.CargaMasivaDetalle" CMD
WHERE CMD."IdEstado"='INV' AND CMD."IdCargaMasiva"=:IdCargaMasiva;
