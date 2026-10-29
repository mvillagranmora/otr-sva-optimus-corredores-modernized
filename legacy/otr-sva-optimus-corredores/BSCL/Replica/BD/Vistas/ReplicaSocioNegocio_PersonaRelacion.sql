SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::ReplicaSocioNegocio_PersonaRelacion"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::ReplicaSocioNegocio_PersonaRelacion" AS

        Select WST."IdStack" AS "IDSTACK", 
        		  "IdPersonaOrigen" || ':' || "IdPersonaDestino" || ':' || "IdTipoRelacion"  AS TABLE_KEY,
        		  "Action" AS ACTION,
                  "IdPersonaOrigen"  AS "IDPERSONAORIGEN", -- BIGINT
                  "IdRelacionPersona"  AS "IDRELACIONPERSONA", -- BIGINT
                  "IdPersonaDestino"  AS "IDPERSONADESTINO", -- BIGINT
                  "FechaNacimiento"  AS "FECHANACIMIENTO", -- VARCHAR(10)
                  "IdPaisResidencia"  AS "IDPAISRESIDENCIA", -- BIGINT
                  "IdEmpresa"  AS "IDEMPRESA", -- BIGINT
                  "Administracion"  AS "ADMINISTRACION", -- VARCHAR(40)
                  "IdTipoRelacion"  AS "IDTIPORELACION", -- VARCHAR(6)
                  "NombreTipoRelacion"  AS "NOMBRETIPORELACION", -- VARCHAR(100)
                  "FechaInicioRelacion"  AS "FECHAINICIORELACION", -- VARCHAR(19)
                  "IdEstado"  AS "IDESTADO", -- VARCHAR(3)
                  "FechaFinRelacion"  AS "FECHAFINRELACION", -- VARCHAR(19)
                  "NombreEstado"  AS "NOMBREESTADO", -- VARCHAR(100)
                  "CodigoTipoPersona"  AS "CODIGOTIPOPERSONA", -- VARCHAR(2)
                  "DocumentoIdentidadDefecto"  AS "DOCUMENTOIDENTIDADDEFECTO", -- VARCHAR(40)
                  "NombreCompleto"  AS "NOMBRECOMPLETO", -- VARCHAR(302)
                  "RazonSocial"  AS "RAZONSOCIAL", -- VARCHAR(100)
                  "Alias"  AS "ALIAS", -- VARCHAR(100)
                  "Nombres"  AS "NOMBRES", -- VARCHAR(100)
                  "ApellidoPaterno"  AS "APELLIDOPATERNO", -- VARCHAR(100)
                  "ApellidoMaterno"  AS "APELLIDOMATERNO", -- VARCHAR(100)
                  "IdTipoDocumentoIdentidadDefecto"  AS "IDTIPODOCUMENTOIDENTIDADDEFECTO", -- VARCHAR(3)
                  "CondicionesRelacion" AS "CONDICIONESRELACION" --VARCHAR(255)

        from "BSCL.SocioNegocio.BD.Vistas::REL.ConsultarPersonaRelacionAnalitica"

        LEFT JOIN "BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaActualizacion" WST ON  WST."Table" = 'SocioNegocio_PersonaRelacion' AND WST."Key" = ("IdPersonaOrigen" || ':' || "IdPersonaDestino" || ':' || "IdTipoRelacion")

WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::ReplicaSocioNegocio_PersonaRelacion" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
                                    
