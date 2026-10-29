SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::ReplicaPlataforma_Empresa"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::ReplicaPlataforma_Empresa" AS

        Select WST."IdStack" AS "IDSTACK", 
        		  "IdEmpresa"  AS TABLE_KEY,
        		  "Action" AS ACTION,
                  "IdEmpresa"  AS "IDEMPRESA", -- BIGINT
                  "Codigo"  AS "CODIGO", -- VARCHAR(40)
                  "Nombre"  AS "NOMBRE", -- VARCHAR(100)
                  "IdEstado"  AS "IDESTADO", -- VARCHAR(3)
                  "IdPais"  AS "IDPAIS", -- BIGINT
                  "IdSocioDeNegocio"  AS "IDSOCIODENEGOCIO", -- BIGINT
                  ''  AS "DOCUMENTOIDENTIDAD" -- VARCHAR(50)

        from "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresaAnalitica"

        LEFT JOIN "BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaActualizacion" WST ON  WST."Table" = 'Plataforma_Empresa' AND WST."Key" = "IdEmpresa"

WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::ReplicaPlataforma_Empresa" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
                                    
