-- Marcelo Rojas
-- CELBUHOPRO-1096
-- Script para insertar Diccionario

SET SCHEMA "BSCL";

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Diccionario" ("CodDiccionario", "CodLenguaje", "Descripcion", "Abreviado")
SELECT 'UI_VISOR_DATOS', 'CL', 'Visor Datos', ''
FROM DUMMY 
WHERE NOT EXISTS (
    SELECT 1 
    FROM BSCL."BSCL.Plataforma.BD::PLA.Diccionario" 
    WHERE "CodDiccionario"='UI_VISOR_DATOS' AND "CodLenguaje"='CL'
);

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Diccionario" ("CodDiccionario", "CodLenguaje", "Descripcion", "Abreviado")
SELECT 'UI_SITUACION_INSTRUMENTO', 'CL', 'SituacionInstrumento', ''
FROM DUMMY 
WHERE NOT EXISTS (
    SELECT 1 
    FROM BSCL."BSCL.Plataforma.BD::PLA.Diccionario" 
    WHERE "CodDiccionario"='UI_SITUACION_INSTRUMENTO' AND "CodLenguaje"='CL'
);

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Diccionario" ("CodDiccionario", "CodLenguaje", "Descripcion", "Abreviado")
SELECT 'UI_DIGITO', 'CL', 'Digito', ''
FROM DUMMY 
WHERE NOT EXISTS (
    SELECT 1 
    FROM BSCL."BSCL.Plataforma.BD::PLA.Diccionario" 
    WHERE "CodDiccionario"='UI_DIGITO' AND "CodLenguaje"='CL'
);


INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Diccionario" ("CodDiccionario", "CodLenguaje", "Descripcion", "Abreviado")
SELECT 'UI_CANTIDAD_UNIDADES', 'CL', 'CantidadUnidades', ''
FROM DUMMY 
WHERE NOT EXISTS (
    SELECT 1 
    FROM BSCL."BSCL.Plataforma.BD::PLA.Diccionario" 
    WHERE "CodDiccionario"='UI_CANTIDAD_UNIDADES' AND "CodLenguaje"='CL'
);

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Diccionario" ("CodDiccionario", "CodLenguaje", "Descripcion", "Abreviado")
SELECT 'UI_C._VALORIZACION', 'CL', 'CodigoValorizacion', ''
FROM DUMMY 
WHERE NOT EXISTS (
    SELECT 1 
    FROM BSCL."BSCL.Plataforma.BD::PLA.Diccionario" 
    WHERE "CodDiccionario"='UI_C._VALORIZACION' AND "CodLenguaje"='CL'
);

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Diccionario" ("CodDiccionario", "CodLenguaje", "Descripcion", "Abreviado")
SELECT 'UI_VAL._CIERRE', 'CL', 'ValorizacionCierre', ''
FROM DUMMY 
WHERE NOT EXISTS (
    SELECT 1 
    FROM BSCL."BSCL.Plataforma.BD::PLA.Diccionario" 
    WHERE "CodDiccionario"='UI_VAL._CIERRE' AND "CodLenguaje"='CL'
);

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Diccionario" ("CodDiccionario", "CodLenguaje", "Descripcion", "Abreviado")
SELECT 'UI_C._PAIS_EMISION', 'CL', 'CodigoPaisEmisionInstrumento', ''
FROM DUMMY 
WHERE NOT EXISTS (
    SELECT 1 
    FROM BSCL."BSCL.Plataforma.BD::PLA.Diccionario" 
    WHERE "CodDiccionario"='UI_C._PAIS_EMISION' AND "CodLenguaje"='CL'
);



INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Diccionario" ("CodDiccionario", "CodLenguaje", "Descripcion", "Abreviado")
SELECT 'UI_FUENTE_PRECIOS', 'CL', 'FuentePrecios', ''
FROM DUMMY 
WHERE NOT EXISTS (
    SELECT 1 
    FROM BSCL."BSCL.Plataforma.BD::PLA.Diccionario" 
    WHERE "CodDiccionario"='UI_FUENTE_PRECIOS' AND "CodLenguaje"='CL'
);

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Diccionario"
("CodDiccionario", "CodLenguaje", "Descripcion", "Abreviado")
SELECT 'UI_TENDENCIA_CLASIFICACION_RIESGO', 'CL', 'Tendencia Clasificacion Riesgo', ''
FROM DUMMY 
WHERE NOT EXISTS (
    SELECT 1 
    FROM BSCL."BSCL.Plataforma.BD::PLA.Diccionario" 
    WHERE "CodDiccionario"='UI_TENDENCIA_CLASIFICACION_RIESGO' AND "CodLenguaje"='CL'
);

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Diccionario"
("CodDiccionario", "CodLenguaje", "Descripcion", "Abreviado")
SELECT 'UI_CIRCULAR_CARTERA_CMF', 'CL', 'Circular Cartera CMF', ''
FROM DUMMY 
WHERE NOT EXISTS (
    SELECT 1 
    FROM BSCL."BSCL.Plataforma.BD::PLA.Diccionario" 
    WHERE "CodDiccionario"='UI_CIRCULAR_CARTERA_CMF' AND "CodLenguaje"='CL'
);



COMMIT ;
