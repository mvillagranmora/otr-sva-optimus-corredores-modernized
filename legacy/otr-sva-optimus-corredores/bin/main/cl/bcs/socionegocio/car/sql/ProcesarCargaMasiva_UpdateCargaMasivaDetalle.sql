UPDATE CMD
SET CMD."IdEstado" = 'PRO'
FROM "BSCL.SocioNegocio.BD::CAR.CargaMasivaDetalle" AS CMD
WHERE CMD."IdCargaMasivaDetalle" = (:IdCargaMasivaDetalle) AND CMD."IdEstado" = 'ING';