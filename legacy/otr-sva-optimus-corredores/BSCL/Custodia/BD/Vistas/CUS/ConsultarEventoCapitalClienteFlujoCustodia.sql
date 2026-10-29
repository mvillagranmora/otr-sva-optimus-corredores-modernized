SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Custodia.BD.Vistas::CUS.ConsultarEventoCapitalClienteFlujoCustodia";

CREATE VIEW "BSCL.Custodia.BD.Vistas::CUS.ConsultarEventoCapitalClienteFlujoCustodia"
AS 
	SELECT  EVC."IdTipoEventoCapital"				AS "IdTipoEventoCapital",
			EVC."IdEventoCapitalCliente" 			AS "IdRegistro",
	        EVC."IdEventoCapital" 					AS "FolioRegistro",
			EVC."IdClasificacionTipoInstrumento"	AS "IdClasificacionTipoInstrumento",
	        EVC."IdTipoInstrumento"					AS "IdTipoInstrumento",
	        EVC."IdInstrumento"						AS "IdInstrumento",        
	        EVC."Nemo"		 						AS "Nemotecnico",
	        EVC."IdInstrumentoNuevo"				AS "IdInstrumentoNuevo",
			EVC."NemoNuevo"							AS "NemoNuevo",
	        EVC."IdMoneda" 							AS "IdMonedaInstrumento",
	        EVC."IdBoveda"							AS "IdBoveda",
	        EVC."FechaCierre" 						AS "FechaLiquidacion",
	        EVC."CantidadNueva" 					AS "Cantidad",
	        EVC."Precio" 							AS "Precio",           
	        EVC."MontoCapital"						AS "Monto",    
	        Portafolio."IdCliente"					AS "IdCliente",
	        EVC."IdPortafolio"						AS "IdPortafolio",
	        EVC."IdBolsa"							AS "IdBolsa",
	        EVC."IdEstado"							AS "IdEstado",
	        EVC."IdMoneda"							AS "IdMoneda",
	        EVC."TipoCartera"						AS "TipoCartera",
	        EvCptal."IdEmpresa",
	        EVC."IdRegimenFondoMutuo"
	FROM "BSCL.Custodia.BD::CUS.EventoCapitalCliente" AS EVC
		INNER JOIN "BSCL.Custodia.BD::CUS.EventoCapital" AS EvCptal
    			ON EVC."IdEventoCapital" = EvCptal."IdEventoCapital"
	  	 LEFT JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" AS Portafolio
			    ON EVC."IdPortafolio" = Portafolio."IdPortafolio";

GRANT SELECT ON "BSCL.Custodia.BD.Vistas::CUS.ConsultarEventoCapitalClienteFlujoCustodia" TO BCS_DESARROLLADOR WITH GRANT OPTION;
