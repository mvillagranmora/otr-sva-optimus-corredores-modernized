SELECT pro."IdInterno",pro."Nemotecnico",ins."IdInstrumento",ins."IdTipoInstrumento",pro."IdCondicionDeLiquidacionContado",CAST(liq."PlazoEnDias" AS BIGINT)AS "DiasLiquidacionContado",tope."IdTipoOperacion",instr."CorteMinimo",pro."IdTipoReajuste"
FROM "BSCL.Orden.BD::ORD.CrearTransaccionesBolsaElectronica_Proceso" pro
INNER JOIN "BSCL.Plataforma.BD::INS.Instrumento" ins ON pro."Nemotecnico" = ins."Nemotecnico"
INNER JOIN "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" toti ON toti."IdTipoInstrumento" = ins."IdTipoInstrumento"
INNER JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" tope ON toti."IdTipoOperacion" = tope."IdTipoOperacion" AND pro."AccionOrden" = tope."AccionOrden"
INNER JOIN "BSCL.Orden.BD::ORD.ConceptoOrden" cto ON cto."IdTipoOperacion" = tope."IdTipoOperacion" AND cto."Uso" = 'INGRESOORDEN'
INNER JOIN "BSCL.Plataforma.BD::PLA.CondicionDeLiquidacion" liq ON liq."IdCondicionDeLiquidacion" = pro."IdCondicionDeLiquidacionContado"
LEFT JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumentoTipoReajuste" instr on instr."IdInstrumento" = ins."IdInstrumento" and instr."IdTipoReajuste" = pro."IdTipoReajuste"
;

--INSERT INTO "BSCL.Orden.BD::ORD.CrearTransaccionesBolsaElectronica_Proceso"
--("IdInterno","Nemotecnico","AccionOrden","IdCondicionDeLiquidacion")
--VALUES
--(0,'PAGARE NR','COMPRA','T0');