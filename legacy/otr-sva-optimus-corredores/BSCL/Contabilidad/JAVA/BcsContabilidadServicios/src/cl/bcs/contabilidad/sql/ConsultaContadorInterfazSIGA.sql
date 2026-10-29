SELECT (COUNT('A') + 1) AS "Cont" FROM "BSCL.Plataforma.BD::BAT.Programa" 
WHERE "IdTipoPrograma" IN ('InterfazSIGADemanda', 'InterfazSIGADiaria') AND "fechaSistema" = :FechaEjecucion