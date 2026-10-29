
INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )
SELECT  
'UI_AGREGAR_ESQUEMA',
'CL',
'Agregar esquema',
'AGRESQ'
FROM DUMMY 
WHERE NOT EXISTS (SELECT 1 FROM  BSCL."BSCL.Plataforma.BD::PLA.Diccionario" WHERE  "CodDiccionario"='UI_AGREGAR_ESQUEMA');



INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )
SELECT  
'UI_INGRESAR_PERIODO',
'CL',
'Ingresar periodo',
'INRPER'
FROM DUMMY 
WHERE NOT EXISTS (SELECT 1 FROM  BSCL."BSCL.Plataforma.BD::PLA.Diccionario" WHERE  "CodDiccionario"='UI_INGRESAR_PERIODO');




INSERT INTO BSCL."BSCL.Normativa.BD::NOR.Configuracion"
("IdEmpresa", "CodigoParametro", "Descripcion", "Valor", "IdNormativa", "EsModificableUsuario", "Uso", "ValorDefecto", "ValoresPosibles", "ModificablePorUsuario", "ModificablePostProduccion", "CreadorParametro", "FechaCreacion", "ModificadorParametro", "FechaModificacion", "Observaciones")
SELECT 1 AS "IdEmpresa",
'PERIODO_TRIBUTARIO' AS "CodigoParametro"
, 'Periodo Tributario para las DJ' AS "Descripcion"
, '2020' AS "Valor"
, '' AS "IdNormativa"
, 'N' AS "EsModificableUsuario"
, ' ' AS "Uso"
, ' ' AS "ValorDefecto"
, ' ' AS "ValoresPosibles"
, ' ' AS "ModificablePorUsuario"
, ' ' AS "ModificablePostProduccion"
, ' ' AS "CreadorParametro"
, '2017-07-18 22:47:59.000' AS "FechaCreacion"
, ' ' AS "ModificadorParametro"
, '2017-07-18 22:48:07.000' AS "FechaModificacion"
, ' ' AS  "Observaciones" 
FROM  dummy 
WHERE NOT EXISTS (SELECT 1 FROM  BSCL."BSCL.Normativa.BD::NOR.Configuracion" WHERE  "CodigoParametro"='PERIODO_TRIBUTARIO');















