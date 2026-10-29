SELECT CTA."IdCargoAbonoMovimiento"
	  ,'ANULACION POR COMPROBANTE TESORERIA ANULADO(' || CTA."IdComprobante" || ')' as "GlosaAnulacion"
FROM #AnularRegistros_p_RegistrosAProcesar AS CA
INNER JOIN "BSCL.Tesoreria.BD::TES.Comprobante" AS COM ON (COM."IdComprobante" = CA."Id")
INNER JOIN "BSCL.CuentaInversion.BD.Vistas::ConsultarCargosAbonos" AS CTA ON (CTA."IdComprobante" = CA."Id")
WHERE CTA."IdModulo" = 'TES'
AND CA."Entidad" = 'TES.Comprobante';