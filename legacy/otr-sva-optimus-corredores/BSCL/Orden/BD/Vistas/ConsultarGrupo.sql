SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Orden.BD.Vistas::ConsultarGrupo"; 
 
CREATE VIEW "BSCL.Orden.BD.Vistas::ConsultarGrupo"
AS  
WITH Agrupados AS (
    SELECT 
        GRU."IdGrupo",
        COUNT(DISTINCT CLO."NombreLibroOrdenes") AS DistintosLibros
    FROM 
        "BSCL.Orden.BD::ORD.Grupo" GRU
    INNER JOIN 
        "BSCL.Orden.BD::ORD.GrupoPortafolio" GRUPOR ON GRUPOR."IdGrupo" = GRU."IdGrupo"
    INNER JOIN 
        "BSCL.Orden.BD.Vistas::ConsultarClientesLibroOrdenes" CLO ON GRUPOR."IdPortafolio" = CLO."IdPortafolio"
    GROUP BY 
        GRU."IdGrupo"
),
Resultados AS (
    SELECT
        GRU."IdGrupo",
        MAX(GRU."IdEmpresa") AS "IdEmpresa",
        MAX(GRU."Nombre") AS "Nombre",
        MAX(GRU."IdEstado") AS "IdEstado",
        MAX(CRGP."NombreEstado") AS "EstadoGrupo",
        MAX(GRU."Codigo") AS "Codigo",
        CASE 
            WHEN A.DistintosLibros > 1 THEN 'NO VALIDO'
            ELSE MAX(CLO."NombreLibroOrdenes")
        END AS "NombreLibroOrdenes"
    FROM 
        "BSCL.Orden.BD::ORD.Grupo" GRU
    INNER JOIN 
        "BSCL.Orden.BD.Vistas::ConsultarEstadoGrupoPortafolio" CRGP ON GRU."IdEstado" = CRGP."IdEstado"
    INNER JOIN 
        "BSCL.Orden.BD::ORD.GrupoPortafolio" GRUPOR ON GRUPOR."IdGrupo" = GRU."IdGrupo"
    INNER JOIN 
         "BSCL.Orden.BD.Vistas::ConsultarClientesLibroOrdenes" CLO ON GRUPOR."IdPortafolio" = CLO."IdPortafolio"
    INNER JOIN
         Agrupados A ON GRU."IdGrupo" = A."IdGrupo"
    GROUP BY 
        GRU."IdGrupo", A.DistintosLibros
)
SELECT * FROM Resultados;

GRANT SELECT ON "BSCL.Orden.BD.Vistas::ConsultarGrupo" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
