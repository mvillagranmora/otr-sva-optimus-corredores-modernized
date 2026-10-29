-- Marcelo Yévenes
-- CELBUHOPRO-998
-- Script para insertar parametros extras para ser usado en calificaciones tributarias



INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
SELECT 'OrigenCalificacion', 'BCS', 1, 'Bolsa', 'S', '', ''
FROM DUMMY WHERE
NOT EXISTS(SELECT 1 FROM BSCL."BSCL.Normativa.BD::DOC.Diccionario" WHERE "Tipo"='OrigenCalificacion' AND "Codigo"='BCS' AND "Orden"=1 AND "Descripcion"='Bolsa');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
SELECT 'OrigenCalificacion', 'COR', 2, 'Corredor', 'S', '', ''
FROM DUMMY WHERE
NOT EXISTS(SELECT 1 FROM BSCL."BSCL.Normativa.BD::DOC.Diccionario" WHERE "Tipo"='OrigenCalificacion' AND "Codigo"='COR' AND "Orden"=2 AND "Descripcion"='Corredor');
