/********************************************************** 
Proposito	: Script para obtener los conceptos de custodia
autor		: NRM
fecha		: 03/05/2017
***********************************************************/


SELECT 	
	CC."IdConcepto",
	CC."ConCertificadoAutomatico",
	CC."GeneraMovimientoEnTransito"
FROM "BSCL.Custodia.BD::CUS.ConceptoCustodia" AS CC
WHERE CC."NombreEntidad" 	= 'CUS.MOVIMIENTOCUSTODIA';
