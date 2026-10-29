SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::ReplicaFondoMutuo_RegimenSerieFondo"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::ReplicaFondoMutuo_RegimenSerieFondo" AS

        Select WST."IdStack" AS "IDSTACK", 
        		"IdRegimenFondoMutuo" || ':' || "IdSerieFondo"  AS TABLE_KEY,
        		"Action" AS ACTION,
                "IdRegimenFondoMutuo" AS "IDREGIMENFONDOMUTUO",
				"IdSerieFondo" AS "IDSERIEFONDO",
				"IdFondo" AS "IDFONDO",
				"DescripcionRegimen" AS "DESCRIPCIONREGIMEN",
				"IdEstadoRegimenSerieDeFondo" AS "IDESTADOREGIMENSERIEDEFONDO",
				"IdEstadoRegimenFondoMutuo" AS "IDESTADOREGIMENFONDOMUTUO",
				"GrupoRegimen" AS "GRUPOREGIMEN",
				"PermiteOtrosGrupos" AS "PERMITEOTROSGRUPOS",
				"PermiteRescate" AS "PERMITERESCATE",
				"PermiteAporte" AS "PERMITEAPORTE",
				"PermiteCambioGrupo" AS "PERMITECAMBIOGRUPO"

        from "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarRegimenSerieFondoAnalitica"

        LEFT JOIN "BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaActualizacion" WST ON  WST."Table" = 'FondoMutuo_RegimenSerieFondo' AND WST."Key" = ("IdRegimenFondoMutuo" || ':' || "IdSerieFondo")

WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::ReplicaFondoMutuo_RegimenSerieFondo" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
                                    
