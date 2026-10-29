SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::ReplicaSocioNegocio_PersonaDireccionFisica"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::ReplicaSocioNegocio_PersonaDireccionFisica" AS

        Select WST."IdStack" AS "IDSTACK", 
        		  "IdDireccionFisica"  AS TABLE_KEY,
        		  "Action" AS ACTION,
                  "Calle"  AS "CALLE", -- VARCHAR(100)
                  "Numero"  AS "NUMERO", -- VARCHAR(40)
                  "Departamento"  AS "DEPARTAMENTO", -- VARCHAR(40)
                  "Sector"  AS "SECTOR", -- VARCHAR(100)
                  "CodigoPostal"  AS "CODIGOPOSTAL", -- VARCHAR(100)
                  "CodigoProposito"  AS "CODIGOPROPOSITO", -- VARCHAR(40)
                  "Comentario"  AS "COMENTARIO", -- VARCHAR(100)
                  "IdEstado"  AS "IDESTADO", -- VARCHAR(3)
                  "DescripcionProposito"  AS "DESCRIPCIONPROPOSITO", -- VARCHAR(100)
                  "IdDireccionFisica"  AS "IDDIRECCIONFISICA", -- BIGINT
                  "IdCiudad"  AS "IDCIUDAD", -- BIGINT
                  "IdPersona"  AS "IDPERSONA", -- BIGINT
                  "NombreCiudad"  AS "NOMBRECIUDAD", -- VARCHAR(100)
                  "IdDivisionPais"  AS "IDDIVISIONPAIS" -- BIGINT

        from "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionFisicaAnalitica"

        LEFT JOIN "BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaActualizacion" WST ON  WST."Table" = 'SocioNegocio_PersonaDireccionFisica' AND WST."Key" = "IdDireccionFisica"

WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::ReplicaSocioNegocio_PersonaDireccionFisica" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
                                    
