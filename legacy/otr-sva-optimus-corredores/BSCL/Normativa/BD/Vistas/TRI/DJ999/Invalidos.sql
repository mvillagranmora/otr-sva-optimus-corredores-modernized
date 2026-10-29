SET SCHEMA "BSCL";

DROP VIEW "BSCL.Normativa.BD.Vistas::TRI.DJ999.Invalidos";

CREATE VIEW "BSCL.Normativa.BD.Vistas::TRI.DJ999.Invalidos"  as
SELECT b."CodigoValidacion", a.*
    FROM "BSCL.Normativa.BD.Vistas::TRI.DJ999.Datos" a
    INNER JOIN "BSCL.Normativa.BD::TRI.ResumenValidacion" b
    ON a."IdDocumento" = b."IdDocumento"
    INNER JOIN "BSCL.Normativa.BD::TRI.DetalleValidacion" c
    ON b."ID" = c."IdResumenValidacion"
    WHERE a."ID" = c."IdDetalleDocumento"
WITH READ ONLY;

GRANT SELECT ON "BSCL.Normativa.BD.Vistas::TRI.DJ999.Invalidos" TO BCS_DESARROLLADOR WITH GRANT OPTION;