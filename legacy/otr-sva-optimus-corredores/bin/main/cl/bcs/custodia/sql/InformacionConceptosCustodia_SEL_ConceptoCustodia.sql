/********************************************************** 
Proposito	: Script para obtener los conceptos de custodia
autor		: NRM
fecha		: 03/05/2017
***********************************************************/


SELECT 	DISTINCT 
	CC."IdConcepto",
	CC."ConCertificadoAutomatico",
	CC."GeneraMovimientoEnTransito",
	CC."GeneraTransferenciaBoveda" AS "EsTransferenciaBoveda",
	CC."EsTransferenciaCliente",
	CC."EsTransferenciaPortafolio",
	CC."EsGarantiaPrendaria",	
	CC."RetieneDividendos",
	CC."RetieneAmortizacion",
	CC."RetieneIntereses",
	CC."FolioObligatorio"
	
FROM "BSCL.Custodia.BD::CUS.ConceptoCustodia" AS CC
WHERE CC."IdConcepto" IN (:IdConcepto)  
  AND (CC."IdTipoOperacion" = '-' OR (CC."IdTipoOperacion" IN (:IdTipoOperacion) OR 1 = :FiltroTipoOperacionValido))  
  AND (CC."IdTipoInstrumento" = '' OR (CC."IdTipoInstrumento" IN (:IdTipoInstrumento) OR 1 = :FiltroTipoInstrumentoValido))
  AND (CC."IdClasificacionTipoInstrumento" = '-' OR (CC."IdClasificacionTipoInstrumento" IN (:IdClasificacionTipoInstrumento) OR 1 = :FiltroClasificacionTipoInstrumentoValido))
  AND (CC."IdInstrumento" = 0 OR (CC."IdInstrumento" IN (:IdInstrumento) OR 1 = :FiltroInstrumentoValido))
  AND CC."NombreEntidad" IN (:IdNombreEntidad);