SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::ReplicaSocioNegocio_PersonaDireccionElectronica"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::ReplicaSocioNegocio_PersonaDireccionElectronica" AS

        Select WST."IdStack" AS "IDSTACK", 
        		  "IdDireccionElectronica"  AS TABLE_KEY,
        		  "Action" AS ACTION,
                  "IdDireccionElectronica"  AS "IDDIRECCIONELECTRONICA", -- BIGINT
                  "Direccion"  AS "DIRECCION", -- VARCHAR(100)
                  "CodigoProposito"  AS "CODIGOPROPOSITO", -- VARCHAR(40)
                  "Comentario"  AS "COMENTARIO", -- VARCHAR(100)
                  "IdEstado"  AS "IDESTADO", -- VARCHAR(3)
                  "IdPersona"  AS "IDPERSONA", -- BIGINT
                  "IdUsuarioCreador"  AS "IDUSUARIOCREADOR", -- BIGINT
                  "DescripcionProposito"  AS "DESCRIPCIONPROPOSITO", -- VARCHAR(100)
                  "Predeterminado" AS "PREDETERMINADO" -- VARCHAR(1)


        from "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionElectronicaAnalitica"

        LEFT JOIN "BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaActualizacion" WST ON  WST."Table" = 'SocioNegocio_PersonaDireccionElectronica' AND WST."Key" = "IdDireccionElectronica"

WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::ReplicaSocioNegocio_PersonaDireccionElectronica" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
                                    
