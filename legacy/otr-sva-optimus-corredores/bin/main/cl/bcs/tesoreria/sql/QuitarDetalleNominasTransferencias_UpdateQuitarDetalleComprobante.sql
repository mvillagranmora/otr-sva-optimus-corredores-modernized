UPDATE C
SET C."IdEstado" = 'ING'
FROM "BSCL.Tesoreria.BD::TES.Comprobante" AS C
    INNER JOIN #QuitarDetalle_p_RegistrosAProcesar AS RP ON C."IdComprobante" = RP."IdComprobante";