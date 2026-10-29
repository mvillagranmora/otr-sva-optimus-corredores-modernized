SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::ReplicaPlataforma_Banco"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::ReplicaPlataforma_Banco" AS

        Select WST."IdStack" AS "IDSTACK", 
        		  "IdBanco"  AS TABLE_KEY,
        		  "Action" AS ACTION,
                  "NombreBanco"  AS "NOMBREBANCO", -- VARCHAR(100)
                  "CodigoBanco"  AS "CODIGOBANCO", -- VARCHAR(40)
                  "IdBanco"  AS "IDBANCO", -- BIGINT
                  "IdPaisOperacion"  AS "IDPAISOPERACION", -- BIGINT
                  "IdPersona"  AS "IDPERSONA", -- BIGINT
                  "IdEstado"  AS "IDESTADO", -- VARCHAR(3)
                  "IdEmpresa"  AS "IDEMPRESA", -- BIGINT
                  "IdTipoBanco"  AS "IDTIPOBANCO" -- VARCHAR(3)

        from "BSCL.Plataforma.BD.Vistas::BAN.ConsultarBancoAnalitica"

        LEFT JOIN "BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaActualizacion" WST ON  WST."Table" = 'Plataforma_Banco' AND WST."Key" = "IdBanco"

WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::ReplicaPlataforma_Banco" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
                                    
