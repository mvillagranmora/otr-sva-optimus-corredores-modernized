UPDATE "BSCL.Mesa.BD::MES.Operacion" OperacionMesa
SET
	OperacionMesa."IdEstado" = :EstadoPosteriorAutorizacion
WHERE OperacionMesa."IdEstado" = 'PDA' AND OperacionMesa."IdOperacion" = :IdOperacion;