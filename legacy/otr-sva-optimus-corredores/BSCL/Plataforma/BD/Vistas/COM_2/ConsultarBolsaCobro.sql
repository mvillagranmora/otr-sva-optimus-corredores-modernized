SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Plataforma.BD.Vistas::COM_2.ConsultarBolsaCobro"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::COM_2.ConsultarBolsaCobro" 
AS
SELECT null as "IdBolsa",'TODAS' AS "NombreBolsa",0 as "IdPais",'' as "NombrePais"
FROM "BSCL.Plataforma.BD::INS.Bolsa"
UNION
SELECT "IdBolsa","Nombre" AS "NombreBolsa",bolsa."IdPais","NombrePais"
FROM "BSCL.Plataforma.BD::INS.Bolsa" bolsa
LEFT JOIN "BSCL.Plataforma.BD::REG.Pais" pais on bolsa."IdPais" = pais."IdPais"
ORDER BY "IdPais";

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::COM_2.ConsultarBolsaCobro" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 

