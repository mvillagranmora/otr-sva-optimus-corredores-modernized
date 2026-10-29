SET SCHEMA "BSCL";
DROP VIEW "BSCL.Replica.BD.Vistas::ReplicaFondoMutuo_NOAPV";

CREATE VIEW "BSCL.Replica.BD.Vistas::ReplicaFondoMutuo_NOAPV" AS

	SELECT
		 WST."IdStack" AS "IDSTACK", 
        "IdMatrizAgrupadorNOAPV" || ':' || "CodigoPortafolio"  AS TABLE_KEY,
        "Action" AS ACTION,
		"IdMatrizAgrupadorNOAPV" AS "IDMATRIZAGRUPADORNOAPV",
		"CodigoPortafolio" AS "CODIGOPORTAFOLIO",
		"IdEstado" AS "IDESTADO",
		"EsADC" AS "ESADC"
	FROM "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarNOAPVAnalitica"
	LEFT JOIN "BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaActualizacion" WST
	ON  WST."Table" = 'FondoMutuo_NOAPV' AND WST."Key" = ("IdMatrizAgrupadorNOAPV" || ':' || "CodigoPortafolio")

WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::ReplicaFondoMutuo_NOAPV" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
