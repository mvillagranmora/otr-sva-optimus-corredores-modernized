SELECT "Valor1" AS "FechaContable", "Valor2" AS "NombreReporte"
FROM "BSCL.Plataforma.BD::PLA.LogServicio"
WHERE "Valor2" IN ('CoberturaPatrimonial','CalcularObligacionesDerechosMonedaExtranjera')
AND "Valor1" = :p_FechaConsulta;