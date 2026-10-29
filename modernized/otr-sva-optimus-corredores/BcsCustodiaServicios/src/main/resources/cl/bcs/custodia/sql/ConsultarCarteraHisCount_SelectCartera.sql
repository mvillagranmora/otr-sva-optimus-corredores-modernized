SELECT ("FechaRespaldo") AS "CarteraCount" 
FROM "BSCL.Custodia.BD::HIS.Cartera"
WHERE "FechaRespaldo" = :p_FechaConsulta
AND "IdCliente" = :p_IdCliente