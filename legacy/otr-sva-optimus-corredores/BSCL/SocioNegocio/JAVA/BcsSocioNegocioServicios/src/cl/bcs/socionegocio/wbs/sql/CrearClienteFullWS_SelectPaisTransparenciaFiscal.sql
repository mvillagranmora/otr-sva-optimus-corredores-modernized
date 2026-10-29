SELECT
"IdPais",
"NombrePais",
"CodigoPais",
"UsaDivisionPais",
"NombreDivisionPais",
"EsOcde",
"EsPaisUs"
FROM "BSCL.Plataforma.BD.Vistas::REG.ConsultarPais"
WHERE "IdPais" = :IdPais;