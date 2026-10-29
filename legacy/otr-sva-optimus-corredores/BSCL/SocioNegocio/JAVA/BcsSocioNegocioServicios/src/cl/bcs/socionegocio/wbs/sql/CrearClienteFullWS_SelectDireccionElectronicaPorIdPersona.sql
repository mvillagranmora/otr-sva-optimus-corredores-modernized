SELECT 
	DIRE ."IdDireccionElectronica",
	DIRE ."Comentario" ,
	DIRE ."CodigoProposito" AS "IdProposito",
	DIRE ."Direccion" ,
	DIRE ."Predeterminado" 
FROM "BSCL.SocioNegocio.BD::PER.DireccionElectronica" AS DIRE
WHERE DIRE ."IdPersona"  IN (:IdPersona);