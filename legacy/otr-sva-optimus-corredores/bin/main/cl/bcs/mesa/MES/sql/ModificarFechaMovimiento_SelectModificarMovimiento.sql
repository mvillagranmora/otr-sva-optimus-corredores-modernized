SELECT SPT."IdOperacionSpot", SPT."IdTipoOperacion", SPT."IdCliente", INS."IdInstrumento", INS."Nemotecnico", 
       SPT."MontoMonedaPrincipal", SPT."IdMonedaPrincipal", SPT."TipoCambioCierre", SPT."ParidadCierre"
FROM "BSCL.Mesa.BD::SPT.OperacionSpot" AS SPT
  INNER JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS INS
	ON SPT."IdInstrumento" = INS."IdInstrumento"
WHERE SPT."IdOperacionSpot" = :idsOperacion