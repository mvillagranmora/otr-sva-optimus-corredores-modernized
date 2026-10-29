SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::ReplicaPortafolio_Portafolio"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::ReplicaPortafolio_Portafolio" AS

        Select WST."IdStack" AS "IDSTACK", 
        		  "IdPortafolio"  AS TABLE_KEY,
        		  "Action" AS ACTION,
                  "IdPortafolio"  AS "IDPORTAFOLIO", -- BIGINT
                  "Proposito"  AS "PROPOSITO", -- VARCHAR(520)
                  "IdCliente"  AS "IDCLIENTE", -- BIGINT
                  "IdEstado"  AS "IDESTADO", -- VARCHAR(3)
                  "FechaBloqueo"  AS "FECHABLOQUEO", -- VARCHAR(19)
                  "ComentarioBloqueo"  AS "COMENTARIOBLOQUEO", -- VARCHAR(520)
                  "IdTipoBloqueo"  AS "IDTIPOBLOQUEO", -- VARCHAR(40)
                  "FechaCreacion"  AS "FECHACREACION", -- VARCHAR(19)
                  "FechaIngreso"  AS "FECHAINGRESO", -- VARCHAR(19)
                  "IdPerfilComercialBaseOrigen"  AS "IDPERFILCOMERCIALBASEORIGEN", -- VARCHAR(40)
                  "IdPerfilProductos"  AS "IDPERFILPRODUCTOS", -- VARCHAR(40)
                  "IdPerfilCobros"  AS "IDPERFILCOBROS", -- VARCHAR(40)
                  "IdPerfilLimites"  AS "IDPERFILLIMITES", -- VARCHAR(40)
                  "DescripcionPortafolio"  AS "DESCRIPCIONPORTAFOLIO", -- VARCHAR(100)
                  "FechaModificacion"  AS "FECHAMODIFICACION", -- VARCHAR(19)
                  "IdTipoDesbloqueo"  AS "IDTIPODESBLOQUEO", -- VARCHAR(40)
                  "FechaDesbloqueo"  AS "FECHADESBLOQUEO", -- VARCHAR(19)
                  "ComentarioDesbloqueo"  AS "COMENTARIODESBLOQUEO", -- VARCHAR(520)
                  "CodigoPortafolio"  AS "CODIGOPORTAFOLIO", -- VARCHAR(100)
                  "IdTelefonoContacto"  AS "IDTELEFONOCONTACTO", -- BIGINT
                  "IdDireccionEnvioCorrespondencia"  AS "IDDIRECCIONENVIOCORRESPONDENCIA", -- BIGINT
                  "Nombres"  AS "NOMBRES", -- VARCHAR(100)
                  "ApellidoPaterno"  AS "APELLIDOPATERNO", -- VARCHAR(100)
                  "ApellidoMaterno"  AS "APELLIDOMATERNO", -- VARCHAR(100)
                  "RazonSocial" AS "RAZONSOCIAL", --VARCHAR(100)
                  "IdPersona"  AS "IDPERSONA", -- BIGINT
                  "DocumentoIdentidad"  AS "DOCUMENTOIDENTIDAD", -- VARCHAR(40)
                  "FechaCambioPerfil"  AS "FECHACAMBIOPERFIL", -- VARCHAR(10)
                  "IdUsuarioModificacionPerfil"  AS "IDUSUARIOMODIFICACIONPERFIL", -- BIGINT
                  "ALIASGRUPO"  AS "ALIASGRUPO", -- VARCHAR(100)
                  "IdEjecutivoPortafolio"  AS "IDEJECUTIVOPORTAFOLIO", -- BIGINT
                  "IdMatrizAgrupador" AS "IDMATRIZAGRUPADOR", --BIGINT
                  "IdEjecutivoComercial" AS "IDEJECUTIVOCOMERCIAL" --BIGINT

        from  "BSCL.Portafolio.BD.Vistas::ConsultarPortafolioAnalitica"

        LEFT JOIN "BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaActualizacion" WST ON  WST."Table" = 'Portafolio_Portafolio' AND WST."Key" = "IdPortafolio"

WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::ReplicaPortafolio_Portafolio" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
                                    
