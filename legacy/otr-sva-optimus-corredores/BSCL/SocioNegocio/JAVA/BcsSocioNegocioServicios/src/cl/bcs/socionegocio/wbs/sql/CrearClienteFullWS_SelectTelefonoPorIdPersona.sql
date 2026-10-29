SELECT 
		TEL."IdTelefono",
		TEL ."CodigoProposito" AS "IdProposito",
		TEL ."Numero" ,
		TEL ."IdPais",
		TEL ."IdTipoTelefono"  AS "Tipo",
		TEL ."Anexo" ,
		TEL ."Comentario" ,
		TEL ."Predeterminado" 
FROM "BSCL.SocioNegocio.BD::PER.Telefono" AS TEL
WHERE TEL."IdPersona" IN (:IdPersona);