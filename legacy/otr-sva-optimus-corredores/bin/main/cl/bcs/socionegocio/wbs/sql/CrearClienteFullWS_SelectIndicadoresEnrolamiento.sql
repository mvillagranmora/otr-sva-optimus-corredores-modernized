SELECT
"IdIndicador" AS "Id"
,"CodigoTipoIndicador"
FROM "BSCL.SocioNegocio.BD.Vistas::ENR.ConsultarIndicadorEnrolamiento"
WHERE "IdTipoEntidad" = :IdTipoEntidad
AND "IdEmpresa" = :IdEmpresa
AND "IdEnrolamiento" = :IdEnrolamiento
AND "CodigoTipoIndicador" IN (:CodigoTipoIndicador)