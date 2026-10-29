-- Actualiza los campos de la tarjeta bancaria de la Persona
UPDATE  TJB 
SET     TJB."IdPersona" 			= COALESCE(:IdPersona, TJB."IdPersona"),
		TJB."IdBanco" 				= COALESCE(:IdBanco, TJB."IdBanco"),
		TJB."IdTipoTarjetaBancaria" = COALESCE(:IdTipoTarjetaBancaria, TJB."IdTipoTarjetaBancaria"),
		TJB."NumeroTarjetaBancaria" = COALESCE(:NumeroTarjetaBancaria, TJB."NumeroTarjetaBancaria"),
		TJB."FechaVencimiento" 		= COALESCE(:FechaVencimiento, TJB."FechaVencimiento"),
		TJB."CodigoVerificacion" 	= COALESCE(:CodigoVerificacion, TJB."CodigoVerificacion"),
		TJB."IdEstado" 				= COALESCE(:IdEstado, TJB."IdEstado"),
		TJB."FechaModificacion" 	= CURRENT_UTCTIMESTAMP
FROM	"BSCL.SocioNegocio.BD::PER.TarjetaBancaria" TJB
WHERE "IdTarjetaBancaria" 			= :IdTarjetaBancaria;