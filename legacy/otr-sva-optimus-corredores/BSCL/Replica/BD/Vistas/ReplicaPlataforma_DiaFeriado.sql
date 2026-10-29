SET SCHEMA "BSCL";

DROP VIEW "BSCL.Replica.BD.Vistas::ReplicaPlataforma_DiaFeriado";

CREATE VIEW "BSCL.Replica.BD.Vistas::ReplicaPlataforma_DiaFeriado" AS

SELECT 
WST."IdStack" AS "IDSTACK"
,"IdFeriado"  AS TABLE_KEY
,WST."Action" AS ACTION
,FERIADO."IdFeriado" AS "IDFERIADO" --BIGINT
,FERIADO."Tipo" AS "TIPO" --NVARCHAR(40)
,FERIADO."Fecha" AS "FECHA" --NVARCHAR(10)
,FERIADO."Dia" AS "DIA" --INTEGER
,FERIADO."DescripcionDia" AS "DESCRIPCIONDIA" --NVARCHAR(100)
,FERIADO."AgnoCarga" AS "AGNOCARGA" --INTEGER
,FERIADO."IdPais" AS "IDPAIS" --BIGINT
,FERIADO."IdEstado" AS "IDESTADO" --NVARCHAR(3)

FROM "BSCL.Plataforma.BD.Vistas::REG.ConsultarDiaFeriadoAnalitica" FERIADO
LEFT JOIN "BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaActualizacion" WST ON  WST."Table" = 'Plataforma_DiaFeriado' AND WST."Key" = FERIADO."IdFeriado"

WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::ReplicaPlataforma_DiaFeriado" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
