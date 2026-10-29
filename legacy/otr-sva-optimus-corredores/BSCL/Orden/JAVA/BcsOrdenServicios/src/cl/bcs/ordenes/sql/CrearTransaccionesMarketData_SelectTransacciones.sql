SELECT pro."IdInterno",pro."Nemotecnico",ins."IdInstrumento",ins."IdTipoInstrumento",tope."IdTipoOperacion",instr."CorteMinimo",pro."IdTipoReajuste", ins."IdMoneda"
FROM "BSCL.Orden.BD::ORD.CrearTransaccionesMarketData_Proceso" pro
INNER JOIN "BSCL.Plataforma.BD::INS.Instrumento" ins ON pro."Nemotecnico" = ins."Nemotecnico"
INNER JOIN "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" toti ON toti."IdTipoInstrumento" = ins."IdTipoInstrumento"
INNER JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" tope ON toti."IdTipoOperacion" = tope."IdTipoOperacion" AND pro."AccionOrden" = tope."AccionOrden" AND tope."IdTipoOperacion" not in ('VCRF')
INNER JOIN "BSCL.Orden.BD::ORD.ConceptoOrden" cto ON cto."IdTipoOperacion" = tope."IdTipoOperacion" AND cto."Uso" = 'INGRESOORDEN'
LEFT JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumentoTipoReajuste" instr on instr."IdInstrumento" = ins."IdInstrumento" and instr."IdTipoReajuste" = pro."IdTipoReajuste"
;

--INSERT INTO "BSCL.Orden.BD::ORD.CrearTransaccionesMarketData_Proceso"
--("IdInterno","Nemotecnico","AccionOrden","IdTipoReajuste")
--VALUES
--(0,'PAGARE NR','COMPRA','CLP');
