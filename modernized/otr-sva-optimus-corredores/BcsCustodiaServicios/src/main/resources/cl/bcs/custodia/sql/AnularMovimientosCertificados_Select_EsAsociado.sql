SELECT C."IdCartera", M."IdMovimientoCarteraEnTransito", M."PermiteCertificacionPorUsuario",
	CASE 
		WHEN (M."IdClasificacionTipoInstrumento" = 'IF') THEN	
			CASE 
				WHEN (M."ValorRescate" != C."INGValorRescate" OR M."Cantidad" != C."INGCantidad") 	-- Para los casos de IF 
				THEN 'S' ELSE 'N'
			END
		ELSE
			CASE 
				WHEN (M."Cantidad" != C."INGCantidad") 												-- Para los casos de RV - RF
				THEN 'S' ELSE 'N'
			END			
		END 																						AS "EsAsociado"
	
FROM 		"BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito" M
INNER JOIN	"BSCL.Custodia.BD::CUS.Cartera"						C ON (M."IdMovimientoCarteraEnTransito" = C."IdMovimientoCarteraEnTransito" OR M."IdMovimientoCarteraEnTransito" = C."EGRIdOperacion")

WHERE 	M."IdMovimientoCarteraEnTransito" 	IN (:IdMovimientoCarteraEnTransito)
AND 	C."IdEstado"						= 'VIG'