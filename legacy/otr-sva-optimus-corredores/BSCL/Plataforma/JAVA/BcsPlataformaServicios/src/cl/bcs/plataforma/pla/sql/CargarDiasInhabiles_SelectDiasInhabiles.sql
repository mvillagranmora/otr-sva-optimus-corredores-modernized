SELECT
	Fer."IdFeriado", 
	Fer."Tipo",
    Fer."Dia",
    Fer."Fecha",
    Fer."IdPais",
    Fer."DescripcionDia",
    Fer."IdEstado",
    Fer."AgnoCarga"
FROM "BSCL.Plataforma.BD::REG.DiaFeriado" AS Fer
WHERE Fer."Tipo" = 'INHABIL' AND Fer."Dia" IN (5, 6) AND Fer."AgnoCarga" = :agnoCarga AND Fer."IdPais" = :idPais;