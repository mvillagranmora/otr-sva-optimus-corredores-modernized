SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::ReplicaFondoMutuo_ValorCuotaHistorico"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::ReplicaFondoMutuo_ValorCuotaHistorico" AS

        SELECT WST."IdStack" AS "IDSTACK", 
        		TO_VARCHAR("IdValorCuotaHistorico")  AS TABLE_KEY,
        		"Action" AS ACTION,
        		"IdValorCuotaHistorico" AS "IDVALORCUOTAHISTORICO",
				"IdSerieFondo" AS "IDSERIEFONDO",
				"FechaValorCuota" AS "FECHAVALORCUOTA",
				"ValorCuota" AS "VALORCUOTA",
				"IdFondo" AS "IDFONDO",
				"IdMoneda" AS "IDMONEDA",
				"NombreSerieFondo" AS "NOMBRESERIEFONDO",
				"CodigoSerie" AS "CODIGOSERIE",
				"FechaCarga" as "FECHACARGA",    					--NVARCHAR(10)
				"FechaIngresoRegistro" AS "FECHAINGRESOREGISTRO",	--NVARCHAR(19)
				"EsUltimaCargaDiaria" AS "ESULTIMACARGADIARIA"		--NVARCHAR(1)

        from "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarValorCuotaHistoricoAnalitica"

        LEFT JOIN "BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaActualizacion" WST ON  WST."Table" = 'FondoMutuo_ValorCuotaHistorico' AND WST."Key" = "IdValorCuotaHistorico"

WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::ReplicaFondoMutuo_ValorCuotaHistorico" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
                                    
