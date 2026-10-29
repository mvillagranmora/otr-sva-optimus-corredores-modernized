SELECT
	EventoCapital."IdEventoCapital",
	SUM(CarteraEventoCapital."Cantidad") AS "CantidadTotalFondo"
FROM 
	"BSCL.Custodia.BD.Vistas::CUS.ConsultarCarteraEventoCapital" CarteraEventoCapital
	LEFT JOIN "BSCL.Custodia.BD::CUS.EventoCapital" AS EventoCapital ON (EventoCapital."IdEventoCapital" in (:IdEventoCapital))
	LEFT JOIN 	"BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio" AS Socio ON (CarteraEventoCapital."IdCliente" = Socio."IdCliente")
WHERE 
	CarteraEventoCapital."Nemotecnico" = :Nemotecnico
	AND CarteraEventoCapital."IdEmpresa" = :IdEmpresa 
	AND CarteraEventoCapital."IdCliente" > 0
	AND IFNULL(Socio."IdSocioDeNegocio",0) > 0
	AND CarteraEventoCapital."FechaOperacion" <= TO_DATE(EventoCapital."FechaLimite", 'YYYY-MM-DD')
	AND CarteraEventoCapital."IdEstado" = 'VIG'
	AND EventoCapital."IdTipoInstrumento" = :IdTipoInstrumento
	AND CarteraEventoCapital."IdTipoInstrumento" = :IdTipoInstrumento
GROUP BY
	EventoCapital."IdEventoCapital"