SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::ReplicaSocioNegocio_Cliente"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::ReplicaSocioNegocio_Cliente" AS

        Select WST."IdStack" AS "IDSTACK", 
        		  "IdPersona"  AS TABLE_KEY,
        		  "Action" AS ACTION,
                  "IdCliente"  AS "IDCLIENTE", -- BIGINT
                  "IdPersona"  AS "IDPERSONA", -- BIGINT
                  "IdEmpresa"  AS "IDEMPRESA", -- BIGINT
                  "IdEstado"  AS "IDESTADO", -- VARCHAR(3)
                  "IdSegmentoCliente"  AS "IDSEGMENTOCLIENTE", -- BIGINT
                  "IdCategoriaInversionista"  AS "IDCATEGORIAINVERSIONISTA", -- VARCHAR(40)
                  "IdTipoBloqueo"  AS "IDTIPOBLOQUEO", -- VARCHAR(40)
                  "FechaBloqueo"  AS "FECHABLOQUEO", -- VARCHAR(19)
                  "ComentarioBloqueo"  AS "COMENTARIOBLOQUEO", -- VARCHAR(520)
                  "FechaIngreso"  AS "FECHAINGRESO", -- VARCHAR(19)
                  "FechaCreacion"  AS "FECHACREACION", -- VARCHAR(19)
                  "FechaModificacion"  AS "FECHAMODIFICACION", -- VARCHAR(19)
                  "IdUsuarioCreador"  AS "IDUSUARIOCREADOR", -- BIGINT
                  "IdPortafolioDefecto"  AS "IDPORTAFOLIODEFECTO", -- BIGINT
                  "IdTipoDesbloqueo"  AS "IDTIPODESBLOQUEO", -- VARCHAR(40)
                  "FechaDesbloqueo"  AS "FECHADESBLOQUEO", -- VARCHAR(19)
                  "ComentarioDesbloqueo"  AS "COMENTARIODESBLOQUEO", -- VARCHAR(520)
                  "IdPersonaEjecutivo"  AS "IDPERSONAEJECUTIVO", -- BIGINT
                  "CodigoEjecutivoExterno"  AS "CODIGOEJECUTIVOEXTERNO", -- VARCHAR(40)
                  "IdTipoCliente"  AS "IDTIPOCLIENTE", -- VARCHAR(40)
                  "IdDocumentoIdentidad"  AS "IDDOCUMENTOIDENTIDAD", -- BIGINT
                  "Tipo"  AS "TIPO", -- VARCHAR(2)
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
                  "EsEnrolado"  AS "ESENROLADO", -- VARCHAR(1)
                  "IdCarga"  AS "IDCARGA", -- BIGINT
                  "Alias"  AS "ALIAS", -- VARCHAR(100)
                  "IdOrigen"  AS "IDORIGEN", -- BIGINT
                  "Borrado"  AS "BORRADO", -- VARCHAR(1)
                  "IdPersonaConyuge"  AS "IDPERSONACONYUGE", -- BIGINT
                  "IdPersonaRepresentante"  AS "IDPERSONAREPRESENTANTE", -- BIGINT
                  "IdPaisNacionalidadPrincipal"  AS "IDPAISNACIONALIDADPRINCIPAL", -- BIGINT
                  "GlobalLegalEntity"  AS "GLOBALLEGALENTITY", -- VARCHAR(520)
                  "IdPersonaEmpleador"  AS "IDPERSONAEMPLEADOR", -- BIGINT
                  "FechaInicioConyuge"  AS "FECHAINICIOCONYUGE", -- VARCHAR(19)
                  "FechaInicioRepresentante"  AS "FECHAINICIOREPRESENTANTE", -- VARCHAR(19)
                  "DetalleCargo"  AS "DETALLECARGO", -- VARCHAR(40)
                  "IdNombreFantasiaPrincipal"  AS "IDNOMBREFANTASIAPRINCIPAL", -- BIGINT
                  "IdEmpleado"  AS "IDEMPLEADO", -- BIGINT
                  "CodigoEmpleado"  AS "CODIGOEMPLEADO", -- VARCHAR(40)
                  "CodigoTipoCargoEmpleado"  AS "CODIGOTIPOCARGOEMPLEADO", -- VARCHAR(6)
                  "DescripcionSegmento"  AS "DESCRIPCIONSEGMENTO" -- VARCHAR(40)

        from "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarClienteAnalitica"

        LEFT JOIN "BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaActualizacion" WST ON  WST."Table" = 'SocioNegocio_Cliente' AND WST."Key" = "IdPersona"

WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::ReplicaSocioNegocio_Cliente" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
                                    
