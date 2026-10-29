SET SCHEMA "BSCL"; 
GO

DROP VIEW "BSCL.Orden.BD.Vistas::ConsultarValorIndicesRV"; 
GO 

CREATE VIEW "BSCL.Orden.BD.Vistas::ConsultarValorIndicesRV"  AS 
    SELECT "indice" AS "Indice"
       ,"price" AS "Valor"
       ,"variation" AS "Variacion"
    FROM "BSCL.Orden.BD::MRV.INDICES_RV"
    WITH READ ONLY;
GO

GRANT SELECT ON "BSCL.Orden.BD.Vistas::ConsultarValorIndicesRV" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
GO
