/********************************************************** 
Proposito	: Tabla de paso para no desbordar variables
autor		: MCHAVEZ
fecha		: 03/01/2023
***********************************************************/

INSERT INTO"BSCL.Contabilidad.BD::GBL.CentralizaTesoreria_ComprobantesAsociados" 
SELECT "IdComprobanteAsociado"
FROM "BSCL.Tesoreria.BD.Vistas::ConsultarComprobanteOrigen" x
WHERE x."IdComprobante" IN ( 
		SELECT DISTINCT
						Comprobante."IdComprobante"
		FROM "BSCL.Tesoreria.BD::TES.Comprobante" 			AS	Comprobante
		LEFT  JOIN "BSCL.Tesoreria.BD.Vistas::ConsultarComprobanteFormaDePago" 	AS	ComprobanteFormaDePago 
		ON (ComprobanteFormaDePago."IdEmpresa" 		= Comprobante."IdEmpresa" 		AND
			ComprobanteFormaDePago."IdComprobante" 	= Comprobante."IdComprobante" 	AND 
			ComprobanteFormaDePago."IdEstado" IN ('LIQ','ANU'))
		WHERE 
			Comprobante."IdEmpresa"	= :IdEmpresa			AND 
			Comprobante."IdEstado" IN ('LIQ','ANU')			AND  
			ComprobanteFormaDePago."FechaLiquidacion" = :FechaContable AND 
			Comprobante."IdConcepto" IN ('PAREGR', 'PARING')
);