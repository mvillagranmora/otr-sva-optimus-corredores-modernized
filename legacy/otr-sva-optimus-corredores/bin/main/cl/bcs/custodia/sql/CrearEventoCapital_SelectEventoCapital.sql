SELECT "NumeroDividendo","Secuencia"-- COUNT("Secuencia") as "CantidadSecuencia"
FROM "BSCL.Custodia.BD::CUS.EventoCapital"
WHERE "IdEmpresa"           = :p_IdEmpresa
    --AND "Secuencia"         = :p_Secuencia
    AND "NumeroDividendo"   IN (:p_NumeroDividendo)
    AND "IdTipoEventoCapital" IN (:p_IdTipoEventoCapital)
--GROUP BY "NumeroDividendo"

/*
SELECT  "NumeroDividendo","Secuencia"--, count("Secuencia")
FROM "BSCL.Custodia.BD::CUS.EventoCapital"
WHERE "NumeroDividendo"=123 AND "Secuencia"=1
ORDER BY "NumeroDividendo"
GROUP BY "NumeroDividendo","Secuencia"
*/
    /*
SELECT "NumeroDividendo", COUNT("Secuencia") as "CantidadSecuencia"
FROM "BSCL.Custodia.BD::CUS.EventoCapital"
WHERE  "NumeroDividendo"   = 123
GROUP BY "NumeroDividendo"*/