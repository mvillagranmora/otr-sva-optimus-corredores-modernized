SELECT CTA."IdCargoAbonoMovimiento"
	  ,'ANULACION POR COMPROBANTE TESORERIA ANULADO(' || CTA."IdComprobante" || ')' as "GlosaAnulacion"
FROM "BSCL.CuentaInversion.BD.Vistas::ConsultarCargosAbonos" AS CTA
INNER JOIN #QuitarDetalle_p_RegistrosAProcesar as RP ON RP."IdComprobante" = CTA."IdComprobante"
WHERE CTA."IdModulo" = 'TES';