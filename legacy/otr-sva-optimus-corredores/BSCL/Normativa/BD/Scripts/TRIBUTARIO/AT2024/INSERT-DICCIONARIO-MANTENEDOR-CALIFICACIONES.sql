SET SCHEMA "BSCL";

UPSERT BSCL."BSCL.Plataforma.BD::PLA.Diccionario"
VALUES('UI_COPIAR', 'CL', 'Copiar', 'Copiar')
WHERE "CodDiccionario"='UI_COPIAR' AND "CodLenguaje"='CL';

UPSERT BSCL."BSCL.Plataforma.BD::PLA.Diccionario"
VALUES('UI_NEMO_NO_INFORMADO', 'CL', 'Nemo no informado - Revisar Nemo, Mercado, Fecha pago y Ejercicio', '')
WHERE "CodDiccionario"='UI_NEMO_NO_INFORMADO' AND "CodLenguaje"='CL';

UPSERT BSCL."BSCL.Plataforma.BD::PLA.Diccionario"
VALUES('UI_COPIAR_CALIFICACION_TRIBUTARIA', 'CL', '¿Copiar Calificacion Tributaria?', '')
WHERE "CodDiccionario"='UI_COPIAR_CALIFICACION_TRIBUTARIA' AND "CodLenguaje"='CL';

UPSERT BSCL."BSCL.Plataforma.BD::PLA.Diccionario"
VALUES('UI_OK_MODIFICAR_CALIFICACION_TRIBUTARIA', 'CL', 'Calificacion Tributaria Modificada', '')
WHERE "CodDiccionario"='UI_OK_MODIFICAR_CALIFICACION_TRIBUTARIA' AND "CodLenguaje"='CL';

UPSERT BSCL."BSCL.Plataforma.BD::PLA.Diccionario"
VALUES('UI_ELIMINAR_CALIFICACION_TRIBUTARIA', 'CL', 'Eliminar Calificacion Tributaria?', '')
WHERE "CodDiccionario"='UI_ELIMINAR_CALIFICACION_TRIBUTARIA' AND "CodLenguaje"='CL';

UPSERT BSCL."BSCL.Plataforma.BD::PLA.Diccionario"
VALUES('UI_INGRESO_CALIFICACIONES_TRIBUTARIAS', 'CL', 'Ingresar Factores', '')
WHERE "CodDiccionario"='UI_INGRESO_CALIFICACIONES_TRIBUTARIAS' AND "CodLenguaje"='CL';
 
/*
    UI_FACTORES_DESCRIPCION
*/

-- 1
UPSERT BSCL."BSCL.Plataforma.BD::PLA.Diccionario"
VALUES('UI_CALIFICACION_TITULO_1', 'CL', '1. Afectos a los impuestos Global Complementario y/o Impuesto Adicional.', '')
WHERE "CodDiccionario"='UI_CALIFICACION_TITULO_1' AND "CodLenguaje"='CL';

-- 2
UPSERT BSCL."BSCL.Plataforma.BD::PLA.Diccionario"
VALUES('UI_CALIFICACION_TITULO_2', 'CL', '2. Rentas Exentas e Ingresos No Contitutivos de Renta (REX).', '')
WHERE "CodDiccionario"='UI_CALIFICACION_TITULO_2' AND "CodLenguaje"='CL';
-- 2.1
UPSERT BSCL."BSCL.Plataforma.BD::PLA.Diccionario"
VALUES('UI_CALIFICACION_TITULO_2_1', 'CL', '2.1 Rentas Con Tributación Cumplida.', '')
WHERE "CodDiccionario"='UI_CALIFICACION_TITULO_2_1' AND "CodLenguaje"='CL';
-- 2.2
UPSERT BSCL."BSCL.Plataforma.BD::PLA.Diccionario"
VALUES('UI_CALIFICACION_TITULO_2_2', 'CL', '2.2 Rentas Exentas', '')
WHERE "CodDiccionario"='UI_CALIFICACION_TITULO_2_2' AND "CodLenguaje"='CL';

-- 3
UPSERT BSCL."BSCL.Plataforma.BD::PLA.Diccionario"
VALUES('UI_CALIFICACION_TITULO_3', 'CL', '3. Acumulados a Contar del 01.01.2017', '')
WHERE "CodDiccionario"='UI_CALIFICACION_TITULO_3' AND "CodLenguaje"='CL';
-- 3.1
UPSERT BSCL."BSCL.Plataforma.BD::PLA.Diccionario"
VALUES('UI_CALIFICACION_TITULO_3_1', 'CL', '3.1 Asociados a Rentas Afectas', '')
WHERE "CodDiccionario"='UI_CALIFICACION_TITULO_3_1' AND "CodLenguaje"='CL';
-- 3.1.1
UPSERT BSCL."BSCL.Plataforma.BD::PLA.Diccionario"
VALUES('UI_CALIFICACION_TITULO_3_1_1', 'CL', '3.1.1 No Sujetos a Restitucion generados hasta el 31.12.2019', '')
WHERE "CodDiccionario"='UI_CALIFICACION_TITULO_3_1_1' AND "CodLenguaje"='CL';
-- 3.1.2
UPSERT BSCL."BSCL.Plataforma.BD::PLA.Diccionario"
VALUES('UI_CALIFICACION_TITULO_3_1_2', 'CL', '3.1.2 No Sujetos a Restitucion generados a contar del 01.01.2020', '')
WHERE "CodDiccionario"='UI_CALIFICACION_TITULO_3_1_2' AND "CodLenguaje"='CL';
-- 3.1.3
UPSERT BSCL."BSCL.Plataforma.BD::PLA.Diccionario"
VALUES('UI_CALIFICACION_TITULO_3_1_3', 'CL', '3.1.3 Sujetos a Restitucion', '')
WHERE "CodDiccionario"='UI_CALIFICACION_TITULO_3_1_3' AND "CodLenguaje"='CL';
-- 3.2
UPSERT BSCL."BSCL.Plataforma.BD::PLA.Diccionario"
VALUES('UI_CALIFICACION_TITULO_3_2', 'CL', '3.2 Asociados a Rentas Exentas', '')
WHERE "CodDiccionario"='UI_CALIFICACION_TITULO_3_2' AND "CodLenguaje"='CL';
-- 3.2.1
UPSERT BSCL."BSCL.Plataforma.BD::PLA.Diccionario"
VALUES('UI_CALIFICACION_TITULO_3_2_1', 'CL', '3.2.1 Sujetos a Restitucion', '')
WHERE "CodDiccionario"='UI_CALIFICACION_TITULO_3_2_1' AND "CodLenguaje"='CL';

-- 4
UPSERT BSCL."BSCL.Plataforma.BD::PLA.Diccionario"
VALUES('UI_CALIFICACION_TITULO_4', 'CL', '4. Acumulados hasta el 31.12.2016', '')
WHERE "CodDiccionario"='UI_CALIFICACION_TITULO_4' AND "CodLenguaje"='CL';
-- 4.1
UPSERT BSCL."BSCL.Plataforma.BD::PLA.Diccionario"
VALUES('UI_CALIFICACION_TITULO_4_1', 'CL', '4.1 Asociados a Rentas Afectas', '')
WHERE "CodDiccionario"='UI_CALIFICACION_TITULO_4_1' AND "CodLenguaje"='CL';
-- 4.2
UPSERT BSCL."BSCL.Plataforma.BD::PLA.Diccionario"
VALUES('UI_CALIFICACION_TITULO_4_2', 'CL', '4.2 Asociados a Rentas Exentas (Articulo 11, Ley 18.401)', '')
WHERE "CodDiccionario"='UI_CALIFICACION_TITULO_4_2' AND "CodLenguaje"='CL';




UPSERT BSCL."BSCL.Plataforma.BD::PLA.Diccionario"
VALUES('UI_FACTOR_08_DESCRIPCION', 'CL', 'Factor-08: Con crédito por IDPC generados a contar del 01.01.2017', '')
WHERE "CodDiccionario"='UI_FACTOR_08_DESCRIPCION' AND "CodLenguaje"='CL';

UPSERT BSCL."BSCL.Plataforma.BD::PLA.Diccionario"
VALUES('UI_FACTOR_09_DESCRIPCION', 'CL', 'Factor-09: Con crédito por IDPC acumulados hasta el 31.12.2016', '')
WHERE "CodDiccionario"='UI_FACTOR_09_DESCRIPCION' AND "CodLenguaje"='CL';

UPSERT BSCL."BSCL.Plataforma.BD::PLA.Diccionario"
VALUES('UI_FACTOR_10_DESCRIPCION', 'CL', 'Factor-10: Con derecho a crédito por pago IDPC Voluntario', '')
WHERE "CodDiccionario"='UI_FACTOR_10_DESCRIPCION' AND "CodLenguaje"='CL';

UPSERT BSCL."BSCL.Plataforma.BD::PLA.Diccionario"
VALUES('UI_FACTOR_11_DESCRIPCION', 'CL', 'Factor-11: Sin derecho a credito', '')
WHERE "CodDiccionario"='UI_FACTOR_11_DESCRIPCION' AND "CodLenguaje"='CL';



-- OG
UPSERT BSCL."BSCL.Plataforma.BD::PLA.Diccionario"
VALUES('UI_FACTOR_18_DESCRIPCION', 'CL', 'Factor-18: Rentas Exentas de Impto. GC Y/O Impto Adicional', '')
WHERE "CodDiccionario"='UI_FACTOR_18_DESCRIPCION' AND "CodLenguaje"='CL';

-- FACTOR_19
UPSERT BSCL."BSCL.Plataforma.BD::PLA.Diccionario"
VALUES('UI_FACTOR_19_DESCRIPCION', 'CL', 'Factor-19: Ingreso no Constitutivos de Renta', '')
WHERE "CodDiccionario"='UI_FACTOR_19_DESCRIPCION' AND "CodLenguaje"='CL';

-- 3
UPSERT BSCL."BSCL.Plataforma.BD::PLA.Diccionario"
VALUES('UI_CALIFICACION_TITULO_3', 'CL', '3. Acumulados a Contar del 01.01.2017', '')
WHERE "CodDiccionario"='UI_CALIFICACION_TITULO_3' AND "CodLenguaje"='CL';
-- 3.1
UPSERT BSCL."BSCL.Plataforma.BD::PLA.Diccionario"
VALUES('UI_CALIFICACION_TITULO_3_1', 'CL', '3.1 Asociados a Rentas Afectas', '')
WHERE "CodDiccionario"='UI_CALIFICACION_TITULO_3_1' AND "CodLenguaje"='CL';
-- 3.1.1
UPSERT BSCL."BSCL.Plataforma.BD::PLA.Diccionario"
VALUES('UI_CALIFICACION_TITULO_3_1_1', 'CL', '3.1.1 No Sujetos a Restitucion generados hasta el 31.12.2019', '')
WHERE "CodDiccionario"='UI_CALIFICACION_TITULO_3_1_1' AND "CodLenguaje"='CL';
-- 3.1.2
UPSERT BSCL."BSCL.Plataforma.BD::PLA.Diccionario"
VALUES('UI_CALIFICACION_TITULO_3_1_2', 'CL', '3.1.2 No Sujetos a Restitucion generados a contar del 01.01.2020', '')
WHERE "CodDiccionario"='UI_CALIFICACION_TITULO_3_1_2' AND "CodLenguaje"='CL';
-- 3.1.3
UPSERT BSCL."BSCL.Plataforma.BD::PLA.Diccionario"
VALUES('UI_FACTOR_20_DESCRIPCION', 'CL', 'Factor-20: Sin Derecho a Devolucion', '')
WHERE "CodDiccionario"='UI_FACTOR_20_DESCRIPCION' AND "CodLenguaje"='CL';

UPSERT BSCL."BSCL.Plataforma.BD::PLA.Diccionario"
VALUES('UI_FACTOR_21_DESCRIPCION', 'CL', 'Factor-21: Con Derecho a Devolucion', '')
WHERE "CodDiccionario"='UI_FACTOR_21_DESCRIPCION' AND "CodLenguaje"='CL';

UPSERT BSCL."BSCL.Plataforma.BD::PLA.Diccionario"
VALUES('UI_FACTOR_22_DESCRIPCION', 'CL', 'Factor-22: Sin Derecho a Devolucion', '')
WHERE "CodDiccionario"='UI_FACTOR_22_DESCRIPCION' AND "CodLenguaje"='CL';

UPSERT BSCL."BSCL.Plataforma.BD::PLA.Diccionario"
VALUES('UI_FACTOR_23_DESCRIPCION', 'CL', 'Factor-23: Con Derecho a Devolucion', '')
WHERE "CodDiccionario"='UI_FACTOR_23_DESCRIPCION' AND "CodLenguaje"='CL';

UPSERT BSCL."BSCL.Plataforma.BD::PLA.Diccionario"
VALUES('UI_FACTOR_24_DESCRIPCION', 'CL', 'Factor-24: Sin Derecho a Devolucion', '')
WHERE "CodDiccionario"='UI_FACTOR_24_DESCRIPCION' AND "CodLenguaje"='CL';

UPSERT BSCL."BSCL.Plataforma.BD::PLA.Diccionario"
VALUES('UI_FACTOR_25_DESCRIPCION', 'CL', 'Factor-25: Con Derecho a Devolucion', '')
WHERE "CodDiccionario"='UI_FACTOR_25_DESCRIPCION' AND "CodLenguaje"='CL';

UPSERT BSCL."BSCL.Plataforma.BD::PLA.Diccionario"
VALUES('UI_FACTOR_26_DESCRIPCION', 'CL', 'Factor-26: Sin Derecho a Devolucion', '')
WHERE "CodDiccionario"='UI_FACTOR_26_DESCRIPCION' AND "CodLenguaje"='CL';

UPSERT BSCL."BSCL.Plataforma.BD::PLA.Diccionario"
VALUES('UI_FACTOR_27_DESCRIPCION', 'CL', 'Factor-27: Con Derecho a Devolucion', '')
WHERE "CodDiccionario"='UI_FACTOR_27_DESCRIPCION' AND "CodLenguaje"='CL';

UPSERT BSCL."BSCL.Plataforma.BD::PLA.Diccionario"
VALUES('UI_FACTOR_28_DESCRIPCION', 'CL', 'Factor-28: Credito por IPE', '')
WHERE "CodDiccionario"='UI_FACTOR_28_DESCRIPCION' AND "CodLenguaje"='CL';

UPSERT BSCL."BSCL.Plataforma.BD::PLA.Diccionario"
VALUES('UI_FACTOR_29_DESCRIPCION', 'CL', 'Factor-29: Sin Derecho a Devolucion', '')
WHERE "CodDiccionario"='UI_FACTOR_29_DESCRIPCION' AND "CodLenguaje"='CL';

UPSERT BSCL."BSCL.Plataforma.BD::PLA.Diccionario"
VALUES('UI_FACTOR_30_DESCRIPCION', 'CL', 'Factor-30: Con Derecho a Devolucion', '')
WHERE "CodDiccionario"='UI_FACTOR_30_DESCRIPCION' AND "CodLenguaje"='CL';

UPSERT BSCL."BSCL.Plataforma.BD::PLA.Diccionario"
VALUES('UI_FACTOR_31_DESCRIPCION', 'CL', 'Factor-31: Sin Derecho a Devolucion', '')
WHERE "CodDiccionario"='UI_FACTOR_31_DESCRIPCION' AND "CodLenguaje"='CL';

UPSERT BSCL."BSCL.Plataforma.BD::PLA.Diccionario"
VALUES('UI_FACTOR_32_DESCRIPCION', 'CL', 'Factor-32: Con Derecho a Devolucion', '')
WHERE "CodDiccionario"='UI_FACTOR_32_DESCRIPCION' AND "CodLenguaje"='CL';

UPSERT BSCL."BSCL.Plataforma.BD::PLA.Diccionario"
VALUES('UI_FACTOR_33_DESCRIPCION', 'CL', 'Factor-33: Credito por IPE', '')
WHERE "CodDiccionario"='UI_FACTOR_33_DESCRIPCION' AND "CodLenguaje"='CL';

UPSERT BSCL."BSCL.Plataforma.BD::PLA.Diccionario"
VALUES('UI_FACTOR_34_DESCRIPCION', 'CL', 'Factor-34: Cred. Por Impto. Tasa Adicional, Ex Art. 21 LIR', '')
WHERE "CodDiccionario"='UI_FACTOR_34_DESCRIPCION' AND "CodLenguaje"='CL';

UPSERT BSCL."BSCL.Plataforma.BD::PLA.Diccionario"
VALUES('UI_FACTOR_35_DESCRIPCION', 'CL', 'Factor-35: Tasa Efectiva Del Cred. Del FUT (TEF)', '')
WHERE "CodDiccionario"='UI_FACTOR_35_DESCRIPCION' AND "CodLenguaje"='CL';

UPSERT BSCL."BSCL.Plataforma.BD::PLA.Diccionario"
VALUES('UI_FACTOR_36_DESCRIPCION', 'CL', 'Factor-36: TASA EFECTIVA DEL CRED. DEL FUNT (TEX)', '')
WHERE "CodDiccionario"='UI_FACTOR_36_DESCRIPCION' AND "CodLenguaje"='CL';

UPSERT BSCL."BSCL.Plataforma.BD::PLA.Diccionario"
VALUES('UI_FACTOR_37_DESCRIPCION', 'CL', 'Factor-37: DEVOLUCION DE CAPITAL ART. 17 NUM 7 LIR', '')
WHERE "CodDiccionario"='UI_FACTOR_37_DESCRIPCION' AND "CodLenguaje"='CL';

-- FACTOR_38 (FACTOR_19B)
UPSERT BSCL."BSCL.Plataforma.BD::PLA.Diccionario"
VALUES('UI_FACTOR_38_DESCRIPCION', 'CL', 'Factor-19B: Ingreso no Constitutivos de Renta', '')
WHERE "CodDiccionario"='UI_FACTOR_38_DESCRIPCION' AND "CodLenguaje"='CL';

COMMIT;