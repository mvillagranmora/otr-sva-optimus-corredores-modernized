SET SCHEMA BSCL;

INSERT INTO "BSCL.Normativa.BD::NOR.Configuracion"
("IdEmpresa", "CodigoParametro", "Descripcion", "Valor", "IdNormativa", "EsModificableUsuario", "Uso", "ValorDefecto", "ValoresPosibles", "ModificablePorUsuario", "ModificablePostProduccion", "CreadorParametro", "FechaCreacion", "ModificadorParametro", "FechaModificacion", "Observaciones")
SELECT 1 AS "IdEmpresa",'TRI_CTACONTABLE_DJ1829_POS' AS "CodigoParametro",'Cuenta contable para resultado de Ejercicio Positivo en DJ1829' AS "Descripcion"
,'411490300000' AS "Valor"
,'DJ1829' AS  "IdNormativa"
,'S' AS "EsModificableUsuario"
,'Carga inicial DJ' AS "Uso"
,'0'  AS "ValorDefecto"
,'0' AS "ValoresPosibles",'S' AS "ModificablePorUsuario",'' AS  "ModificablePostProduccion", '' AS  "CreadorParametro", CURRENT_UTCTIMESTAMP AS "FechaCreacion"
,'' AS "ModificadorParametro", CURRENT_UTCTIMESTAMP AS "FechaModificacion", '' AS "Observaciones"
FROM DUMMY
where NOT EXISTS (SELECT 1 FROM "BSCL.Normativa.BD::NOR.Configuracion" N WHERE N."CodigoParametro"='TRI_CTACONTABLE_DJ1829_POS'  AND N."IdNormativa"='DJ1829');  


INSERT INTO "BSCL.Normativa.BD::NOR.Configuracion"
("IdEmpresa", "CodigoParametro", "Descripcion", "Valor", "IdNormativa", "EsModificableUsuario", "Uso", "ValorDefecto", "ValoresPosibles", "ModificablePorUsuario", "ModificablePostProduccion", "CreadorParametro", "FechaCreacion", "ModificadorParametro", "FechaModificacion", "Observaciones")
SELECT 1 AS "IdEmpresa",'TRI_CTACONTABLE_DJ1829_NEG' AS "CodigoParametro",'Cuenta contable para resultado de Ejercicio Negativo en DJ1829' AS "Descripcion"
,'412390500000' AS "Valor"
,'DJ1829' AS  "IdNormativa"
,'S' AS "EsModificableUsuario"
,'Carga inicial DJ' AS "Uso"
,'0'  AS "ValorDefecto"
,'0' AS "ValoresPosibles",'S' AS "ModificablePorUsuario",'' AS  "ModificablePostProduccion", '' AS  "CreadorParametro", CURRENT_UTCTIMESTAMP AS "FechaCreacion"
,'' AS "ModificadorParametro", CURRENT_UTCTIMESTAMP AS "FechaModificacion", '' AS "Observaciones"
FROM DUMMY
where NOT EXISTS (SELECT 1 FROM "BSCL.Normativa.BD::NOR.Configuracion" N WHERE N."CodigoParametro"='TRI_CTACONTABLE_DJ1829_NEG'  AND N."IdNormativa"='DJ1829');  



COMMIT;