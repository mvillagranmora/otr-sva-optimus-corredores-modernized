UPDATE IEFP
SET "IdEstado" = 'ANU'
FROM "BSCL.Tesoreria.BD::TES.IngresoEgresoFormaDePago" AS IEFP
INNER JOIN  #AnularRegistros_p_RegistrosAProcesar AS RP ON (IEFP."IdIngresoEgreso" = RP."Id")
WHERE RP."Entidad" = 'TES.IngresoEgreso';