SELECT pro."IdInterno",pro."Nemotecnico",ins."IdInstrumento",ins."IdTipoInstrumento",tope."IdTipoOperacion",instr."CorteMinimo",pro."IdTipoReajuste"
FROM "BSCL.Orden.BD::ORD.CrearTransaccionesMarketDataBEC_Proceso" pro
INNER JOIN "BSCL.Plataforma.BD::INS.Instrumento" ins ON pro."Nemotecnico" = ins."Nemotecnico"
INNER JOIN "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" toti ON toti."IdTipoInstrumento" = ins."IdTipoInstrumento"
INNER JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" tope ON toti."IdTipoOperacion" = tope."IdTipoOperacion" AND pro."AccionOrden" = tope."AccionOrden" AND tope."IdTipoOperacion" not in ('VCRF')
INNER JOIN "BSCL.Orden.BD::ORD.ConceptoOrden" cto ON cto."IdTipoOperacion" = tope."IdTipoOperacion" AND cto."Uso" = 'INGRESOORDEN'
LEFT JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumentoTipoReajuste" instr on instr."IdInstrumento" = ins."IdInstrumento" and instr."IdTipoReajuste" = pro."IdTipoReajuste"
;
