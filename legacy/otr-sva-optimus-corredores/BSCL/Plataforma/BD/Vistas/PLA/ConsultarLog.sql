SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Plataforma.BD.Vistas::PLA.ConsultarLog"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::PLA.ConsultarLog"  
AS
SELECT 	"Log"."IdLog" 								as "IdLog"						,
		"Log"."IdTipoLog" 							as "IdTipoLog"					, 
		"Modulo"."Nombre" 							as "NombreModulo"				, 
		"Usuario"."CodigoUsuario" 					as "CodigoUsuario"				,
		"Log"."IdEmpresa"							as "IdEmpresa"					,
		"Empresa"."Nombre" 							as "NombreEmpresa"				, 
		"Log"."FechaLog" 							as "FechaLog"					, 
		TO_VARCHAR("Log"."FechaRegistro",'yyyy-mm') as "PeriodoLog"					,
		"Log"."FechaRegistro"						as "FechaRegistro"				,
		"UsuarioAtendido"."CodigoUsuario" 			as "CodigoUsuarioAtendido"		, 
		"Log"."IdPersona" 							as "IdPersona"					, 
		"Log"."IdCliente" 							as "IdCliente"					, 
		"Log"."EstadoCliente" 						as "EstadoCliente"				, 
		"Log"."NombreCliente" 						as "NombreCliente"				, 
		"Log"."IdTipoDocumentoIdentidad" 			as "IdTipoDocumentoIdentidad"	, 
		"Log"."IdDocumentoIdentidad" 				as "IdDocumentoIdentidad"		, 
		"Log"."IdPortafolio" 						as "IdPortafolio"				,
		"Portafolio"."CodigoPortafolio"				as "CodigoPortafolio"			, 
		"Log"."EstadoPortafolio" 					as "EstadoPortafolio"			, 
		"Log"."IdRegistro" 							as "IdRegistro"					, 
		"Log"."EstadoRegistro" 						as "EstadoRegistro"				, 
		"Log"."IdTipoRegistro" 						as "IdTipoRegistro"				, 
		"Log"."IdTipoOperacion" 					as "IdTipoOperacion"			, 
		"Log"."Folio" 								as "Folio"						, 
		"Log"."Cantidad" 							as "Cantidad"					, 
		"Log"."PrecioTasa" 							as "PrecioTasa"					, 
		"Log"."Monto" 								as "Monto"						, 
		"Log"."IdMonedaOrigen"						as "IdMonedaOrigen"				, 
		"Log"."IdMonedaLiquidacion"					as "IdMonedaLiquidacion"		, 
		"Log"."TipoCambio"							as "TipoCambio"					, 
		"Log"."Nemo" 								as "Nemo"						, 
		"Log"."IdProceso" 							as "IdProceso"					, 
		"Log"."EstadoProceso"  						as "EstadoProceso"				, 
		"Log"."NombreProceso" 						as "NombreProceso"
FROM "BSCL.Plataforma.BD::PLA.Log" AS "Log"
LEFT JoIN "BSCL.Plataforma.BD::PLA.Modulo" AS "Modulo" ON "Log"."IdModulo" = "Modulo"."IdModulo"
LEFT JoIN "BSCL.Plataforma.BD::ACC.Usuario" AS "Usuario" ON "Log"."IdUsuario" = "Usuario"."IdUsuario" 
LEFT JoIN "BSCL.Plataforma.BD::PLA.Empresa" AS "Empresa" ON "Log"."IdEmpresa" = "Empresa"."IdEmpresa"
LEFT JoIN "BSCL.Plataforma.BD::ACC.Usuario" AS "UsuarioAtendido" ON "Log"."IdUsuarioAtendido" = "UsuarioAtendido"."IdUsuario"
LEFT JoIN "BSCL.Portafolio.BD::POR.Portafolio" AS "Portafolio" ON "Log"."IdPortafolio" = "Portafolio"."IdPortafolio";

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::PLA.ConsultarLog" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 

