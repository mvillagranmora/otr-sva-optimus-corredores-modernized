/********************************************************** 
Proposito	: Script para obtener plantilla custodia
autor		: NRM
fecha		: 18/04/2017
***********************************************************/

SELECT 
	 "IdPlantillaCustodia"
	,"IdAccionOperacionLlave"
	,"IdTipoProductoLlave"
	,"IdTipoOperacionLlave"
	,"IdClasificacionTipoInstrumentoLlave"
	,"IdTipoInstrumentoLlave"
	,"IdInstrumentoLlave"
	,"IdOrigenInstrumentoLlave"
	,"EsGarantiaLlave"
	-------------------------------------------------------------
	,"IdEmisor"
	,"Nemotecnico"
	,"NemoSVS"
	,"IdTipoReajuste"
	,"IdMonedaEmision"
	,"IdMonedaInstrumento"	
	,"EsFungible"
	,"FechaEmisionInstrumento"
	,"FechaVencimientoInstrumento"
	,"Tamano"
	,"ISIN"
	,"TieneCorteDeCupon"
	,"IndArt104"
	,"TasaFiscal"					
	,"TipoValorizacion"					
	,"ValorizaPor"
	-------------------------------------------------------------
	,"IdBolsa"
	,"IdBoveda"
FROM "BSCL.Custodia.BD::CUS.PlantillaCustodia"
WHERE "IdEstado" = 'ACT';