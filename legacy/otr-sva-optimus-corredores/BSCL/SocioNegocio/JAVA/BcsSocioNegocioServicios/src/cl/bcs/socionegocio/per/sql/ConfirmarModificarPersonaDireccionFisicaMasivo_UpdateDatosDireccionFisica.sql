-- Actualiza los campos de la direccion fisica de la Persona
UPDATE  DIR 
SET     DIR."IdPersona" 		= COALESCE(:IdPersona,DIR."IdPersona"),
		DIR."Calle" 			= COALESCE(:Calle,DIR."Calle"),
		DIR."Numero" 			= COALESCE(:Numero,DIR."Numero"),
		DIR."Departamento" 		= COALESCE(:Departamento,DIR."Departamento"),
		DIR."Sector" 			= COALESCE(:Sector,DIR."Sector"),
		DIR."CodigoPostal" 		= COALESCE(:CodigoPostal,DIR."CodigoPostal"),
		DIR."IdCiudad" 			= COALESCE(:IdCiudad,DIR."IdCiudad"),
		DIR."CodigoProposito" 	= COALESCE(:CodigoProposito,DIR."CodigoProposito"),
		DIR."Comentario" 		= COALESCE(:Comentario,DIR."Comentario"),
		DIR."IdEstado" 			= COALESCE(:IdEstado,DIR."IdEstado"),
		DIR."IdDivisionPais" 	= COALESCE(:IdDivisionPais,DIR."IdDivisionPais"),
		DIR."CodigoCompuesto" 	= COALESCE(:CodigoCompuesto,DIR."CodigoCompuesto"),
		DIR."Predeterminado" 	= COALESCE(:Predeterminado,DIR."Predeterminado"),
		DIR."FechaModificacion" = CURRENT_UTCTIMESTAMP
FROM	"BSCL.SocioNegocio.BD::PER.DireccionFisica" DIR
WHERE "IdDireccionFisica" 		= :IdDireccionFisica;