SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::ReplicaSocioNegocio_PersonaTelefono"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::ReplicaSocioNegocio_PersonaTelefono" AS

        Select WST."IdStack" AS "IDSTACK", 
				  "IdTelefono"  AS TABLE_KEY,
				  "Action" AS ACTION,
                  "IdTelefono"  AS "IDTELEFONO", -- BIGINT
                  "IdPais"  AS "IDPAIS", -- BIGINT
                  "Numero"  AS "NUMERO", -- VARCHAR(40)
                  "Anexo"  AS "ANEXO", -- VARCHAR(40)
                  "CodigoProposito"  AS "CODIGOPROPOSITO", -- VARCHAR(40)
                  "Comentario"  AS "COMENTARIO", -- VARCHAR(100)
                  "IdEstado"  AS "IDESTADO", -- VARCHAR(3)
                  "IdPersona"  AS "IDPERSONA", -- BIGINT
                  "NombreTipoTelefono"  AS "NOMBRETIPOTELEFONO", -- VARCHAR(100)
                  "IdTipoTelefono"  AS "IDTIPOTELEFONO", -- VARCHAR(40)
                  "DescripcionProposito"  AS "DESCRIPCIONPROPOSITO" -- VARCHAR(100)

        from "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaTelefonoAnalitica"

        LEFT JOIN "BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaActualizacion" WST ON  WST."Table" = 'SocioNegocio_PersonaTelefono' AND WST."Key" = "IdTelefono"

WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::ReplicaSocioNegocio_PersonaTelefono" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
                                    
