set schema "BSCL";
--Creacion 04-01-2024
--Mrojas-CELBUHOPRO-1116

DROP VIEW "BSCL.Custodia.BD.Vistas::CUS.ConsultarHomologacionCMF";

CREATE VIEW "BSCL.Custodia.BD.Vistas::CUS.ConsultarHomologacionCMF" AS
SELECT 
    HOMOLOGACION."Esquema",
    HOMOLOGACION."Tipo",
    HOMOLOGACION."Descripcion",
    HOMOLOGACION."ValorOrigen",
    HOMOLOGACION."ValorDestino",
    HOMOLOGACION."IdEstado"
FROM 
    "BSCL.Normativa.BD::DOC.Homologacion" AS HOMOLOGACION
WHERE 
     HOMOLOGACION."Esquema" = 'CMF';