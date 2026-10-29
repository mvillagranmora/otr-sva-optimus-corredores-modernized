-- Marcelo Yévenes
-- CELBUHOPRO-998
-- Script para insertar parametros extras para ser usado en calificaciones tributarias



INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('OrigenCalificacion', 'BCS', 1, 'Bolsa', 'S', '', '');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('OrigenCalificacion', 'COR', 2, 'Corredor', 'S', '', '');

