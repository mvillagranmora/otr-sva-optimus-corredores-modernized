SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.Plataforma.BD.Vistas::INS.ConsultarBolsa"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::INS.ConsultarBolsa"  
AS
SELECT bol."IdBolsa" 
, bol."Codigo" 
, bol."Nombre" 
, bol."IdPais" 
, bol."IdEstado" 
, boltip."IdTipoInstrumento" 
, boltip."IdEstado" AS "EstadoBolsaTipoInstrumento" 
, boltip."ExigeGarantia"
, pais."NombrePais" as "NombrePais"
, bol."EsExtranjera"
FROM "BSCL.Plataforma.BD::INS.Bolsa" AS bol 
LEFT OUTER JOIN "BSCL.Plataforma.BD::INS.BolsaTipoInstrumento" AS boltip ON boltip."IdBolsa" = bol."IdBolsa"
LEFT OUTER JOIN "BSCL.Plataforma.BD::REG.Pais" AS pais ON pais."IdPais" = bol."IdPais";

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::INS.ConsultarBolsa" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 