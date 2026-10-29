SELECT 
	CarteraEventoCapital."IdCliente",
	CarteraEventoCapital."IdPortafolio",
	CarteraEventoCapital."IdBoveda",
	EventoCapital."IdEventoCapital",
	EventoCapital."IdTipoEventoCapital",
	EventoCapital."Secuencia",	
	Socio."IdSocioDeNegocio",
	ConsultarCliente."DocumentoIdentidad",
	ConsultarCliente."NombreCompleto",
	EventoCapital."IdClasificacionTipoInstrumento",
	EventoCapital."IdTipoInstrumento",
	EventoCapital."IdInstrumento",
	EventoCapital."Nemo",
	EventoCapital."IdMoneda",
	EventoCapital."Monto",
	EventoCapital."CantidadCuotas",
	EventoCapital."NumeroDividendo",
	CarteraEventoCapital."TipoCarteraNormativa",
	ConsultarCliente."TipoDocumentoIdentidad",
	CarteraEventoCapital."IdRegimenFondoMutuo",
	SUM(CarteraEventoCapital."Cantidad") AS "Cantidad"
	
FROM 
	"BSCL.Custodia.BD.Vistas::CUS.ConsultarCarteraEventoCapital" CarteraEventoCapital
	LEFT JOIN "BSCL.Custodia.BD::CUS.EventoCapital" AS EventoCapital ON (EventoCapital."IdEventoCapital" in (:IdEventoCapital))
	LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio" AS Socio ON (CarteraEventoCapital."IdCliente" = Socio."IdCliente")
	LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS ConsultarCliente ON (CarteraEventoCapital."IdCliente" = ConsultarCliente."IdCliente")
	
WHERE 
	CarteraEventoCapital."IdEmpresa" =:IdEmpresa
	AND CarteraEventoCapital."Nemotecnico" =:Nemotecnico
	AND CarteraEventoCapital."IdCliente" > 0
	AND IFNULL(Socio."IdSocioDeNegocio",0) > 0
	AND CarteraEventoCapital."FechaOperacion" <= TO_DATE(EventoCapital."FechaLimite", 'YYYY-MM-DD')
	AND CarteraEventoCapital."IdEstado" = 'VIG'
	AND EventoCapital."IdTipoInstrumento" =:IdTipoInstrumento
	AND CarteraEventoCapital."IdTipoInstrumento" =:IdTipoInstrumento
	
GROUP BY 
	CarteraEventoCapital."IdCliente",
	CarteraEventoCapital."IdPortafolio",
	CarteraEventoCapital."IdBoveda",
	EventoCapital."IdEventoCapital",
	EventoCapital."IdTipoEventoCapital",
	EventoCapital."Secuencia",
	Socio."IdSocioDeNegocio",
	ConsultarCliente."DocumentoIdentidad",
	ConsultarCliente."NombreCompleto",
	EventoCapital."IdClasificacionTipoInstrumento",
	EventoCapital."IdTipoInstrumento",
	EventoCapital."IdInstrumento",
	EventoCapital."Nemo",
	EventoCapital."IdMoneda",
	EventoCapital."Monto",
	EventoCapital."CantidadCuotas",
	EventoCapital."NumeroDividendo",
	CarteraEventoCapital."TipoCarteraNormativa",
	ConsultarCliente."TipoDocumentoIdentidad",
	CarteraEventoCapital."IdRegimenFondoMutuo";