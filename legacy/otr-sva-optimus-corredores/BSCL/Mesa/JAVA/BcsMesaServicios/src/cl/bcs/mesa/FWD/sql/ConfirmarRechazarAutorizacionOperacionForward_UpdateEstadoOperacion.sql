UPDATE "BSCL.Mesa.BD::MES.Operacion" OperacionForward
SET
	OperacionForward."IdEstado" = :EstadoPosteriorAutorizacion
WHERE OperacionForward."IdEstado" = 'PEN' AND OperacionForward."IdOperacion" = :IdOperacion;