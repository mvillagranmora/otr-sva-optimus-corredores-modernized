SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::Sebra.HOMOLOGACION"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::Sebra.HOMOLOGACION" AS

SELECT 'COMUNA_SEBRA' AS tipo_valor,c.com_id as com_id_sebra,div."IdDivisionPais" as com_id_optimus,div."IdCiudad" as id_ciudad_optimus 
FROM "BSCL.Replica.BD.Vistas::ReplicaCOMUNA" c 
LEFT JOIN "BSCL.Homologacion.BD::HOM.Homologacion" h ON (h."TipoValor" = 'COMUNA_SEBRA' AND "ValorOrigen" = c.com_id ) 
LEFT JOIN "BSCL.Plataforma.BD::REG.DivisionPais" div on div."IdDivisionPais" = h."ValorDestino"


WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::Sebra.HOMOLOGACION" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
