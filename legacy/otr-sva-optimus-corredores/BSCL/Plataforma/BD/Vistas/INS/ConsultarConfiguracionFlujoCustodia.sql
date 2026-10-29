SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Plataforma.BD.Vistas::INS.ConsultarConfiguracionFlujoCustodia"; 

CREATE VIEW "BSCL.Plataforma.BD.Vistas::INS.ConsultarConfiguracionFlujoCustodia"(
	"IdTipoInstrumentoFlujoCustodia"
	,"IdTipoInstrumento"
	,"NombreTipoInstrumento"
	,"IdBolsa"
	,"NombreBolsa"
	,"IdEstadoFlujoCustodia"
	,"NombreEstadoFlujoCustodia"
	,"IdFlujoCustodia" 
	,"IdItemFlujoCustodia"
	,"NombreFlujoCustodia"
	,"ItemFlujoCustodia"
	,"IdEstadoItemFlujoCustodia"
	,"NombreEstadoItemFlujoCustodia"
	) AS select
	tflu."IdTipoInstrumentoFlujoCustodia"
	,tflu."IdTipoInstrumento"
	,cti."Nombre" as "NombreTipoInstrumento"
	,tflu."IdBolsa"
	,bolsa."Nombre" as "NombreBolsa"
	,tflu."IdEstado" as "IdEstadoFlujoCustodia"
	,est."Nombre" as "NombreEstadoFlujoCustodia"
	,tflu."IdFlujoCustodia" 
	,iflu."IdItemFlujoCustodia"
	,iflu."NombreFlujoCustodia"
	,iflu."ItemFlujoCustodia"
	,iflu."IdEstado" as "IdEstadoItemFlujoCustodia"
	,estI."Nombre" as "NombreEstadoItemFlujoCustodia" 

	from "BSCL.Plataforma.BD::INS.TipoInstrumentoFlujoCustodia" tflu 
		inner join "BSCL.Plataforma.BD::INS.ItemFlujoCustodia" iflu on iflu."IdFlujoCustodia" = tflu."IdFlujoCustodia"
		inner join "BSCL.Plataforma.BD::INS.TipoInstrumento" cti  on cti."IdTipoInstrumento" = tflu."IdTipoInstrumento" --and cti."IdBolsa" = tflu."IdBolsa" 
		inner join "BSCL.Plataforma.BD::INS.Bolsa" bolsa on bolsa."IdBolsa" = tflu."IdBolsa"
		inner join "BSCL.Plataforma.BD::PLA.Estado" est on est."IdEstado" = tflu."IdEstado"
		inner join "BSCL.Plataforma.BD::PLA.Estado" estI on estI."IdEstado" = iflu."IdEstado"
	
WITH READ ONLY;
     
GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::INS.ConsultarConfiguracionFlujoCustodia" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
     
     
     
