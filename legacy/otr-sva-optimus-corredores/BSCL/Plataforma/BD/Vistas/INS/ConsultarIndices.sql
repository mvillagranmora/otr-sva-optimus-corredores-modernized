SET SCHEMA "BSCL"; 
GO
DROP VIEW "BSCL.Plataforma.BD.Vistas::INS.ConsultarIndices"; 
GO
CREATE VIEW "BSCL.Plataforma.BD.Vistas::INS.ConsultarIndices"
AS
SELECT "IdIndiceFinanciero"    AS "Indice" FROM "BSCL.Plataforma.BD::MER.DefinicionIndiceFinanciero"      --RALCA
UNION 
SELECT "IdParametroFinanciero" AS "Indice" FROM "BSCL.Plataforma.BD::MER.DefinicionParametroFinanciero"   -- BOLSA
GO
GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::INS.ConsultarIndices" TO BCS_DESARROLLADOR WITH GRANT OPTION; 