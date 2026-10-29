-- Marcelo Yévenes
-- CELBUHOPRO-1016
-- Script para insertar un nuevo campo en la UI

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Diccionario"
("CodDiccionario", "CodLenguaje", "Descripcion", "Abreviado")
SELECT 'UI_CALIFICACION_PENDIENTE', 'CL', 'Calificación Pendiente', ''
FROM DUMMY 
WHERE NOT EXISTS (SELECT 1 FROM BSCL."BSCL.Plataforma.BD::PLA.Diccionario" N WHERE N."CodDiccionario"='UI_CALIFICACION_PENDIENTE' AND N."CodLenguaje"='CL');


