UPDATE "BSCL.CuentaInversion.BD::CAR.CargaMasivaDetalle"
SET "IdEstado"			   =:p_IdEstado,
	"ObservacionValidacion"=:p_ObservacionValidacion,
	"Descripcion"		   =:p_Descripcion
WHERE "IdCargaMasivaDetalle"=:p_IdCargaMasivaDetalle;