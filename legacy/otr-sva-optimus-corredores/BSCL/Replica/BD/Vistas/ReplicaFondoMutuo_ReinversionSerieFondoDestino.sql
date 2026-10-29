SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::ReplicaFondoMutuo_ReinversionSerieFondoDestino"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::ReplicaFondoMutuo_ReinversionSerieFondoDestino" AS

        Select WST."IdStack" AS "IDSTACK", 
        		TO_VARCHAR("IdSolicitudRescate")  AS TABLE_KEY,
        		"Action" AS ACTION,
                "IdSolicitudRescate" AS "IDSOLICITUDRESCATE",
				"IdSolicitudAporte" AS "IDSOLICITUDAPORTE",
				"IdTipoMovimientoRescate" AS "IDTIPOMOVIMIENTORESCATE",
				"IdTipoMovimientoAporte" AS "IDTIPOMOVIMIENTOAPORTE",
				"IdSerieFondoDestino" AS "IDSERIEFONDODESTINO",
				"NombreSerieFondo" AS "NOMBRESERIEFONDO",
				"Porcentaje" AS "PORCENTAJE",
				"MontoRescate" AS "MONTORESCATE",
				"MontoAporte" AS "MONTOAPORTE",
				"IdRegimenTributario" AS "IDREGIMENTRIBUTARIO",
				"IdReinversionSerieFondo" AS "IDREINVERSIONSERIEFONDO"

        FROM "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarReinversionSerieFondoDestinoAnalitica"

        LEFT JOIN "BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaActualizacion" WST ON  WST."Table" = 'FondoMutuo_ReinversionSerieFondoDestino' AND WST."Key" = "IdSolicitudRescate"

WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::ReplicaFondoMutuo_ReinversionSerieFondoDestino" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
                                    
