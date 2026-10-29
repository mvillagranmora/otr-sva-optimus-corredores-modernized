						
SELECT 
	  EVE."IdEventoJuntaAccionista"
	, EVE."IdEmpresa"
	, EVE."IdEmisor"
	, CAST(EVE."FechaRealizacion" AS NVARCHAR(10)) AS "FechaRealizacion" 
	, EVE."IdEstadoJunta"
	, EVE."IdEstadoCustodia"
	, EVE."Instruccion"

FROM "BSCL.Custodia.BD::CUS.EventoJuntaAccionista" AS EVE 
WHERE EVE."IdEventoJuntaAccionista" IN (:IdEventoJuntaAccionista)
AND   EVE."IdEmpresa" =  :IdEmpresa;
							