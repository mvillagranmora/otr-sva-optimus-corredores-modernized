SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::ReplicaPlataforma_Concepto"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::ReplicaPlataforma_Concepto" AS

        Select WST."IdStack" AS "IDSTACK", 
				  "IdConcepto"  AS TABLE_KEY,
				  "Action" AS ACTION,
                  "IdConcepto"  AS "IDCONCEPTO", -- VARCHAR(6)
                  "NombreConcepto"  AS "NOMBRECONCEPTO", -- VARCHAR(100)
                  "TipoConcepto"  AS "TIPOCONCEPTO", -- VARCHAR(10)
                  "IdEstado"  AS "IDESTADO", -- VARCHAR(3)
                  "NombreEstado"  AS "NOMBREESTADO", -- VARCHAR(100)
				  "ConceptoAbonoReplica" AS "CONCEPTOABONOREPLICA", --VARCHAR(2)
				  "ConceptoCargoReplica" AS "CONCEPTOCARGOREPLICA" --VARCHAR(2)
        from "BSCL.Plataforma.BD.Vistas::PLA.ConsultarConceptoAnalitica"

        LEFT JOIN "BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaActualizacion" WST ON  WST."Table" = 'Plataforma_Concepto' AND WST."Key" = "IdConcepto"

WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::ReplicaPlataforma_Concepto" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
                                    
