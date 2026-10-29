UPDATE "BSCL.Facturacion.BD::FAC.MovimientoComprobante"
SET "NemotecnicoSubyacente" = :p_NemoNuevo,
	"IdInstrumentoSubyacente" = :p_IdInstrumentoNuevo

WHERE "IdMovimientoComprobante" IN 
(
	SELECT "IdMovimientoComprobante" 
	FROM  "BSCL.Custodia.BD.Vistas::EVC.ConsultarSimultaneaVigentes" 
	WHERE "IdInstrumentoSubyacente" = :p_IdInstrumento
	
	)