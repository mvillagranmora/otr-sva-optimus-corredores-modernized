UPDATE CD
SET CD."IdEstado" = 'ING'
FROM "BSCL.Tesoreria.BD::TES.ComprobanteDetalle" AS CD
    INNER JOIN #QuitarDetalle_p_RegistrosAProcesar AS RP ON CD."IdComprobante" = RP."IdComprobante";