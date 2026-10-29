SET SCHEMA "BSCL";

DROP VIEW "BSCL.Custodia.BD.Vistas::CUS.ConsultarUsoFlujoCustodia";
 
CREATE VIEW "BSCL.Custodia.BD.Vistas::CUS.ConsultarUsoFlujoCustodia" 
AS
SELECT 
  "IdEmpresa"
,"UsoTipoOperacion"
,tip."Nombre" as "NombreUsoFlujoCustodia"
,cus."BusquedaEspecifica"
,cus."UsoTipoOperacion" || ' - ' || CASE cus."BusquedaEspecifica" WHEN '' THEN COALESCE(tip."Nombre",'') ELSE REPLACE(cus."BusquedaEspecifica",'_',' ') END AS "NombreCompleto"
FROM "BSCL.Custodia.BD::CUS.UsoFlujoCustodia" AS cus
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" tip on tip."IdTipoOperacion" = cus."UsoTipoOperacion";

GRANT SELECT ON "BSCL.Custodia.BD.Vistas::CUS.ConsultarUsoFlujoCustodia" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
