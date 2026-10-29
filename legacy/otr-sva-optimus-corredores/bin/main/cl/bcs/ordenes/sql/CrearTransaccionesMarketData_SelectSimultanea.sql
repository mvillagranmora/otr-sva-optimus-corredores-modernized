SELECT pro."IdInterno",pro."Nemotecnico",toti."IdInstrumento",tope."IdTipoOperacion"
FROM "BSCL.Orden.BD::ORD.CrearTransaccionesMarketData_Proceso" pro
INNER JOIN "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" toti ON toti."IdTipoInstrumento" = 'SIM'
INNER JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" tope ON toti."IdTipoOperacion" = tope."IdTipoOperacion" AND pro."AccionOrden" = tope."AccionOrden"
INNER JOIN "BSCL.Orden.BD::ORD.ConceptoOrden" cto ON cto."IdTipoOperacion" = tope."IdTipoOperacion" AND cto."Uso" = 'INGRESOORDEN'
;