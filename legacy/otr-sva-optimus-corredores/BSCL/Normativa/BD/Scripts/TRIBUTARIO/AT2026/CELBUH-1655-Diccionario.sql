SET SCHEMA BSCL;

DELETE FROM "BSCL.Normativa.BD::DOC.Diccionario" WHERE "Tipo"='TipoContribuyente1962' AND "Codigo"=1 AND "Descripcion"='Sociedad Administradora De Fondos';

INSERT INTO "BSCL.Normativa.BD::DOC.Diccionario"
("Tipo", "Codigo", "Orden", "Descripcion", "Activo", "Objetivo", "Producto")
VALUES('TipoContribuyente1962', 1, 2, 'Sociedad Administradora De Fondos', 'S', '', '');

COMMIT;