UPDATE "BSCL.Custodia.BD::CUS.Cartera" CarteraForward
SET
	CarteraForward."IdEstado" = :EstadoPosteriorAutorizacion
WHERE CarteraForward."IdEstado" = 'PEN' AND CarteraForward."IdOperacion" = :IdOperacion;