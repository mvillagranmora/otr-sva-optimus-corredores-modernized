SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::ReplicaSocioNegocio_TipoRelacion"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::ReplicaSocioNegocio_TipoRelacion" AS

        Select WST."IdStack" AS "IDSTACK", 
        		  "IdTipoRelacion"  AS TABLE_KEY,
        		  "Action" AS ACTION,
                  "IdTipoRelacion"  AS "IDTIPORELACION", -- VARCHAR(6)
                  "EsPersonaNatural"  AS "ESPERSONANATURAL", -- VARCHAR(1)
                  "IdTipoEntidadOrigen"  AS "IDTIPOENTIDADORIGEN", -- VARCHAR(100)
                  "Administracion"  AS "ADMINISTRACION", -- VARCHAR(40)
                  "NombreTipoRelacion"  AS "NOMBRETIPORELACION", -- VARCHAR(100)
                  "IdTipoRelacionDestino"  AS "IDTIPORELACIONDESTINO", -- VARCHAR(6)
                  "EsIndefinida"  AS "ESINDEFINIDA", -- VARCHAR(1)
                  "IdEstado"  AS "IDESTADO", -- VARCHAR(3)
                  "IdTipoEntidadDestino"  AS "IDTIPOENTIDADDESTINO", -- VARCHAR(100)
                  "IdEmpresa"  AS "IDEMPRESA", -- BIGINT
                  "UsaPorcentajeParticipacion"  AS "USAPORCENTAJEPARTICIPACION", -- VARCHAR(1)
                  "TextoUsaPorcentajeParticipacion"  AS "TEXTOUSAPORCENTAJEPARTICIPACION" -- VARCHAR(2)

        from "BSCL.SocioNegocio.BD.Vistas::REL.ConsultarTipoRelacionAnalitica"

        LEFT JOIN "BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaActualizacion" WST ON  WST."Table" = 'SocioNegocio_TipoRelacion' AND WST."Key" = "IdTipoRelacion"

WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::ReplicaSocioNegocio_TipoRelacion" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
                                    
