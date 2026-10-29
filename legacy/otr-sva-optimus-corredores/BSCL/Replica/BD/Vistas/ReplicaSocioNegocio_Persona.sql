SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::ReplicaSocioNegocio_Persona"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::ReplicaSocioNegocio_Persona" AS

        Select WST."IdStack" AS "IDSTACK", 
        		  "IdPersona"  AS TABLE_KEY,
        		  "Action" AS ACTION,
                  "IdPersona"  AS "IDPERSONA", -- BIGINT
                  "IdEstadoPersona"  AS "IDESTADOPERSONA", -- VARCHAR(3)
                  "IdDocumentoIdentidad"  AS "IDDOCUMENTOIDENTIDAD", -- BIGINT
                  "TipoPersona"  AS "TIPOPERSONA", -- VARCHAR(2)
                  "Nombres"  AS "NOMBRES", -- VARCHAR(100)
                  "ApellidoPaterno"  AS "APELLIDOPATERNO", -- VARCHAR(100)
                  "ApellidoMaterno"  AS "APELLIDOMATERNO", -- VARCHAR(100)
                  "RazonSocial"  AS "RAZONSOCIAL", -- VARCHAR(100)
                  "IdGiroPrincipal"  AS "IDGIROPRINCIPAL", -- BIGINT
                  "IdPaisNacimiento"  AS "IDPAISNACIMIENTO", -- BIGINT
                  "IdPaisResidencia"  AS "IDPAISRESIDENCIA", -- BIGINT
                  "FechaNacimiento"  AS "FECHANACIMIENTO", -- VARCHAR(10)
                  "FechaDefuncion"  AS "FECHADEFUNCION", -- VARCHAR(10)
                  "Sexo"  AS "SEXO", -- VARCHAR(1)
                  "EsExtranjero"  AS "ESEXTRANJERO", -- VARCHAR(1)
                  "EstadoCivil"  AS "ESTADOCIVIL", -- VARCHAR(2)
                  "RegimenConyugal"  AS "REGIMENCONYUGAL", -- VARCHAR(2)
                  "Cargo"  AS "CARGO", -- VARCHAR(100)
                  "Empleador"  AS "EMPLEADOR", -- VARCHAR(100)
                  "IdDireccionFisicaPrincipal"  AS "IDDIRECCIONFISICAPRINCIPAL", -- BIGINT
                  "IdDireccionElectronicaPrincipal"  AS "IDDIRECCIONELECTRONICAPRINCIPAL", -- BIGINT
                  "IdTelefonoPrincipal"  AS "IDTELEFONOPRINCIPAL", -- BIGINT
                  "IdProfesion"  AS "IDPROFESION", -- BIGINT
                  "FechaInicioActividad"  AS "FECHAINICIOACTIVIDAD", -- VARCHAR(19)
                  "AliasPersona"  AS "ALIASPERSONA", -- VARCHAR(100)
                  "IdTipoDocumentoIdentidad"  AS "IDTIPODOCUMENTOIDENTIDAD", -- VARCHAR(3)
                  "DocumentoIdentidad"  AS "DOCUMENTOIDENTIDAD", -- VARCHAR(40)
                  "NumeroSerie"  AS "NUMEROSERIE", -- VARCHAR(40)
                  "FechaEmisionDocumentoIdentidad"  AS "FECHAEMISIONDOCUMENTOIDENTIDAD", -- VARCHAR(10)
                  "FechaVencimientoDocumentoIdentidad"  AS "FECHAVENCIMIENTODOCUMENTOIDENTIDAD", -- VARCHAR(10)
                  "IdEstadoDocumentoIdentidad"  AS "IDESTADODOCUMENTOIDENTIDAD", -- VARCHAR(3)
                  "DescripcionTipoPersona"  AS "DESCRIPCIONTIPOPERSONA", -- VARCHAR(100)
                  "EsContraparte"  AS "ESCONTRAPARTE", -- VARCHAR(5)
                  "EsProveedor"  AS "ESPROVEEDOR", -- VARCHAR(5)
                  "EsEmpleado"  AS "ESEMPLEADO", -- VARCHAR(5)
                  "EsCliente"  AS "ESCLIENTE" -- VARCHAR(5)

        from "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaAnalitica"

        LEFT JOIN "BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaActualizacion" WST ON  WST."Table" = 'SocioNegocio_Persona' AND WST."Key" = "IdPersona"

WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::ReplicaSocioNegocio_Persona" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
                                    
