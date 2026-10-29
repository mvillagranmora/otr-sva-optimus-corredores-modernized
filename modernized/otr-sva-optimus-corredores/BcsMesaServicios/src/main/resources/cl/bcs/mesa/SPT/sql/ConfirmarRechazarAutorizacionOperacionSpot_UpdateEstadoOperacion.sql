UPDATE "BSCL.Mesa.BD::SPT.OperacionSpot" OperacionSpot
SET
	OperacionSpot."Estado" = :EstadoPosteriorAutorizacion
WHERE OperacionSpot."Estado" = 'PEN' AND OperacionSpot."IdOperacionSpot" = :IdOperacionSpot;