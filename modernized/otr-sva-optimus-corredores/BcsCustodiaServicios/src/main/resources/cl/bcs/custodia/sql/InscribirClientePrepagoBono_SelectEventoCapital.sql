SELECT	
	EVE."IdEventoCapital",
	EVE."IdTipoEventoCapital",  		
	EVE."Secuencia",
	EVE."IdClasificacionTipoInstrumento",
	EVE."IdTipoInstrumento",						
	EVE."IdInstrumento",
	EVE."Nemo", 								
	EVE."FechaPago",
	EVE."NumeroDividendo",
	BON."CallConfirmado",
	BON."CuponeraVariable", 
	BON."FechaPrepago"
		
FROM "BSCL.Custodia.BD::CUS.EventoCapital" AS EVE
INNER JOIN "BSCL.Plataforma.BD::INS.InstrumentoBono" AS BON ON (BON."IdInstrumento" = EVE."IdInstrumento")
WHERE  EVE."IdEventoCapital" IN (:IdsEventoCapital);
