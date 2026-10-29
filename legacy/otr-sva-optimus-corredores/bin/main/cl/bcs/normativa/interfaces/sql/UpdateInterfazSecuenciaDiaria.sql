UPDATE "BSCL.Normativa.BD::InterfazSecuenciaDiaria"
SET "Secuencia" = :Secuencia
WHERE "IdInterfaz" = :IdInterfaz
AND "Fecha" = :Fecha