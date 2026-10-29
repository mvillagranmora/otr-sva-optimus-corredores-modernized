-- Actualiza los campos de la cuenta bancaria de la Persona
UPDATE  CTB 
SET     CTB."IdPersona" 				= COALESCE(:IdPersona, CTB."IdPersona"),
		CTB."EsPac" 					= COALESCE(:EsPac, CTB."EsPac"),
		CTB."IdBanco" 					= COALESCE(:IdBanco, CTB."IdBanco"),
		CTB."IdPlaza" 					= COALESCE(:IdPlaza, CTB."IdPlaza"),
		CTB."IdMoneda" 					= COALESCE(:IdMoneda, CTB."IdMoneda"),
		CTB."IdEstado" 					= COALESCE(:IdEstado, CTB."IdEstado"),
		CTB."CodigoAba" 				= COALESCE(:CodigoAba, CTB."CodigoAba"),
		CTB."CodigoIban" 				= COALESCE(:CodigoIban, CTB."CodigoIban"),
		CTB."IdentificadorCuenta" 		= COALESCE(:IdentificadorCuenta, CTB."IdentificadorCuenta"),
		CTB."IdPersonaRelacionada" 		= COALESCE(:IdPersonaRelacionada, CTB."IdPersonaRelacionada"),
		CTB."IdTipoCuentaBancaria" 		= COALESCE(:IdTipoCuentaBancaria, CTB."IdTipoCuentaBancaria"),
		CTB."NumeroCuentaBancaria" 		= COALESCE(:NumeroCuentaBancaria, CTB."NumeroCuentaBancaria"),
		CTB."CodigoCuentaInterbancario" = COALESCE(:CodigoCuentaInterbancario, CTB."CodigoCuentaInterbancario"),
		CTB."IdTipoIdentificadorCuenta" = COALESCE(:IdTipoIdentificadorCuenta, CTB."IdTipoIdentificadorCuenta"),
		CTB."OtroCodigoBancoExtranjero" = COALESCE(:OtroCodigoBancoExtranjero, CTB."OtroCodigoBancoExtranjero"),
		CTB."OtroCodigoCuentaExtranjero" = COALESCE(:OtroCodigoCuentaExtranjero, CTB."OtroCodigoCuentaExtranjero"),
		CTB."FechaModificacion" 		= CURRENT_UTCTIMESTAMP
FROM	"BSCL.SocioNegocio.BD::PER.CuentaBancaria" CTB
WHERE "IdCuentaBancaria" 				= :IdCuentaBancaria;

