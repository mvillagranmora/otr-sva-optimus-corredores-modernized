SELECT "ContratosAbiertosDia", "ContratosEjercidos", "ContratosSorteados", "Sorteado"
FROM "BSCL.Custodia.BD.Vistas::DER.ConsultaSorteoContratos"
WHERE "Serie" = :p_Nemotecnico
AND "Fecha" = :p_FechaOperacion
AND "IdPortafolio" = :p_IdPortafolio;