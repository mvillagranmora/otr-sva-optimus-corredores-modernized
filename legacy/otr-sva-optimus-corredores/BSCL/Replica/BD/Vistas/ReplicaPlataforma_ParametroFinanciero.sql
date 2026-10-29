SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::ReplicaPlataforma_ParametroFinanciero"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::ReplicaPlataforma_ParametroFinanciero" AS

        SELECT WST."IdStack" AS "IDSTACK", 
        		"IdParametroFinanciero" || ':' || "Fecha"  AS TABLE_KEY,
        		"Action" AS ACTION,
               	"IdParametroFinanciero" AS "IDPARAMETROFINANCIERO",
				"Fecha" AS "FECHA",
				"Periodo" AS "PERIODO",
				"IdMonedaParametro" AS "IDMONEDAPARAMETRO",
				"Nombre" AS "NOMBRE",
				"IdMonedaValor" AS "IDMONEDAVALOR",
				"AgnoMes" AS "AGNOMES",
				"Agno" AS "AGNO",
				"Valor" AS "VALOR",
				"FechaActualizacion" AS "FECHAACTUALIZACION"

        from "BSCL.Plataforma.BD.Vistas::MER.ConsultarParametroFinancieroAnalitica"

        LEFT JOIN "BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaActualizacion" WST ON  WST."Table" = 'Plataforma_ParametroFinanciero' AND WST."Key" = ("IdParametroFinanciero" || ':' || "Fecha")

WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::ReplicaPlataforma_ParametroFinanciero" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
                                    
