INSERT INTO	"BSCL.Plataforma.BD::REG.DiaFeriado"(
"IdFeriado",
"Tipo",
"Fecha",
"Dia",
"DescripcionDia",
"AgnoCarga",
"IdPais",
"IdEstado")
VALUES (
(SELECT COALESCE(MAX("IdFeriado"), 0) FROM "BSCL.Plataforma.BD::REG.DiaFeriado") + :Numero,
:Tipo,
:Fecha,
:Dia,
:DescripcionDia,
:AgnoCarga,
:IdPais,
:IdEstado
)