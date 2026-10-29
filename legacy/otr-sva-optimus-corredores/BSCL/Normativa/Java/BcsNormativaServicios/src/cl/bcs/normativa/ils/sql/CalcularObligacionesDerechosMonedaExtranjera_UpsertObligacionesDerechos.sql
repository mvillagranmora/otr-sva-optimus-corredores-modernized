UPSERT "BSCL.Mesa.BD::MRF.ObligacionesDerechosMonedaExtranjera"
("FechaContable",
"ObligacionesTasaPapel",
"DerechosTasaPapel",
"DerechosTasaPactos",
"ObligacionesTasaPactos",
"ObligacionesTasaPapelMenores8Dias",
"DerechosTasaPapelMenores8Dias",
"DerechosTasaPactosMenores8Dias",
"ObligacionesTasaPactosMenores8Dias",
"IdEmpresa") 
VALUES(
:FechaContable,
:ObligacionesTasaPapel,
:DerechosTasaPapel,
:DerechosTasaPactos,
:ObligacionesTasaPactos,
:ObligacionesTasaPapelMenores8Dias,
:DerechosTasaPapelMenores8Dias,
:DerechosTasaPactosMenores8Dias,
:ObligacionesTasaPactosMenores8Dias,
:IdEmpresa) 
WHERE "FechaContable"=:FechaContable;