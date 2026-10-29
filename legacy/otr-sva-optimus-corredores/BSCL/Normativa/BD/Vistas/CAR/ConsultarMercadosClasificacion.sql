SET SCHEMA "BSCL";

DROP VIEW "BSCL.Normativa.BD.Vistas::CAR.ConsultarMercadosClasificacion";

CREATE VIEW "BSCL.Normativa.BD.Vistas::CAR.ConsultarMercadosClasificacion" AS
    SELECT
        Arbol."IdArbolClasificacionActivo" AS "IdMercado",
        Arbol."Descripcion" AS "DescripcionMercado" 
    FROM "BSCL.Plataforma.BD::INS.ArbolClasificacionActivos" Arbol
    WHERE Arbol."Padre" = 0;

GRANT SELECT ON "BSCL.Normativa.BD.Vistas::CAR.ConsultarMercadosClasificacion" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
