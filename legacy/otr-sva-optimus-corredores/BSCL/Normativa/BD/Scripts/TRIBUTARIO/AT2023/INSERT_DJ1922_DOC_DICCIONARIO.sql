

DELETE FROM BSCL."BSCL.Normativa.BD::DOC.Diccionario" WHERE "Tipo" = 'TributaSa1922';

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('TributaSa1922', '1', 1, 'SI', 'S', '', '');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('TributaSa1922', '2', 2, 'NO', 'S', '', '');