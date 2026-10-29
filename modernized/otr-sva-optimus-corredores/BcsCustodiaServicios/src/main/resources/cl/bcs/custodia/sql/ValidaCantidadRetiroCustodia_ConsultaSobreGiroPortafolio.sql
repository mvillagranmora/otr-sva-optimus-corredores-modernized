SELECT LIM."IdPortafolio" AS "IdRegistro", LIM."IdMoneda" AS "Codigo", LIM."IdEstado" AS "Valor"
FROM "BSCL.Custodia.BD::GBL.ValidaCantidadRetiroCustodia_RegistrosAValidar" REG
LEFT JOIN "BSCL.Plataforma.BD.Vistas::LIM.ConsultarLimitePortafolio" LIM
ON (LIM."IdTipoLimite" = 'LIMPORTSOBREGIRO'
AND LIM."IdPortafolio" = REG."IdPortafolio"
)