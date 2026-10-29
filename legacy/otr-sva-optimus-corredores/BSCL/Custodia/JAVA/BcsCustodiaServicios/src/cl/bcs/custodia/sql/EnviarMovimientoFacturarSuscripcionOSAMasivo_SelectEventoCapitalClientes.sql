select 
	ConPortafolio."IdCliente" 											as "IdCliente", 
	EvCapCli."IdPortafolio" 											as "IdPortafolio",
	EvCapCli."IdInstrumento"											as "IdInstrumento",
	EvCapCli."IdEstado"                                                 as "IdEstadoEventoCliente",
	RelInstrumento."IdInstrumentoRelacionado"							as "IdInstrumentoRelacionado",
--	RelInstrumento."IdInstrumentoRelacionado"							as "IdInstrumento",
--	EvCapCli."IdInstrumento"											as "IdInstrumentoRelacionado",
	EvCapCli."IdMoneda" 												as "IdMoneda",
	EvCapCli."IdBoveda" 												as "IdBoveda",
	EvCapCli."IdBolsa"													as "IdBolsa",
	Instrumento."IdEmisor" 												as "IdEmisor",
	EvCapCli."CantidadNueva"											as "CantidadNueva",
	EvCapCli."Precio" 													as "Precio",
	EvCapCli."IdEventoCapitalCliente" 									as "IdEventoCapitalCliente",
	IND."ValorIndicador" 												as "OperaCuentaInversion",
 	COALESCE(VCPP."IdPreferenciaPago",'')  								AS "IdPreferenciaPago",
	COALESCE(VCPP."PorDefecto",'N') 									aS "EsPreferenciaPagoPorDefecto",
	COALESCE(VCPP."IdEstado",'')										AS "IdEstadoPreferenciaPago",
	COALESCE(VCPP."IdMedioDePago",'')									AS "IdMedioDePago" ,
	COALESCE(VCPP."IdTipoOperacion",'')									AS "IdTipoOperacion" 
FROM "BSCL.Custodia.BD::CUS.EventoCapital"									AS EvCap
INNER JOIN "BSCL.Custodia.BD::CUS.EventoCapitalCliente" 					AS EvCapCli
	ON EvCap."IdEventoCapital" = EvCapCli."IdEventoCapital" 
INNER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" 				AS ConPortafolio
	ON EvCapCli."IdPortafolio" = ConPortafolio."IdPortafolio"
INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" 				AS ConCliente
	ON ConPortafolio."IdCliente" = ConCliente."IdCliente"
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEmpleado" 				AS ConEmpleado
	ON ConCliente."IdPersonaEjecutivo" = ConEmpleado."IdPersona"
LEFT JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento"				AS Instrumento
	ON EvCapCli."IdInstrumento" = Instrumento."IdInstrumento"
LEFT JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarSucursal"				AS Sucursal
	ON ConEmpleado."IdSucursal" = Sucursal."IdSucursal"
INNER JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarRelacionInstrumento"	as RelInstrumento
	ON EvCapCli."IdInstrumento" = RelInstrumento."IdInstrumento"
LEFT JOIN "BSCL.Plataforma.BD::IND.Indicador" 								AS IND
	ON IND."IdEntidad" = ConPortafolio."IdPortafolio" 
LEFT JOIN "BSCL.Plataforma.BD::IND.TipoIndicador" 							AS TIN 
	ON TIN."IdTipoIndicador" = IND."IdTipoIndicador"
LEFT JOIN  "BSCL.Portafolio.BD.Vistas::ConsultarPreferenciaPagoPortafolio" AS VCPP
	ON VCPP."IdPortafolio" = ConPortafolio."IdPortafolio" 
	AND VCPP."IdTipoOperacion" = 'INGSUS'
  	AND VCPP."IdEstado" = 'ACT'
WHERE EvCapCli."IdEventoCapitalCliente" in (:ListaIdEventoCapitalCliente) 
  AND EvCapCli."IdTipoEventoCapital" = 'SU'
  AND EvCapCli."CantidadNueva" > 0
  AND RelInstrumento."IdEstado"= 'VIG'
  AND TIN."CodigoTipoIndicador" = 'GEN_OCCINV'