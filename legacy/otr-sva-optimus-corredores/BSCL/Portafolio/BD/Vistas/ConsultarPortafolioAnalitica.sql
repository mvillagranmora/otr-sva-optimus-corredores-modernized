SET SCHEMA "BSCL";

DROP VIEW "BSCL.Portafolio.BD.Vistas::ConsultarPortafolioAnalitica";

CREATE VIEW "BSCL.Portafolio.BD.Vistas::ConsultarPortafolioAnalitica" AS 
select 				  
				 POR."IdPortafolio"
				,CAST(POR."Proposito" AS NVARCHAR(520)) AS "Proposito"
				,CASE WHEN POR."IdCliente" = 0 THEN NULL ELSE POR."IdCliente" END AS "IdCliente" 
				,CASE WHEN POR."IdEstado" = '' THEN NULL ELSE POR."IdEstado" END AS "IdEstado"  
				,CASE WHEN IFNULL(POR."FechaBloqueo",'0001-01-01')= '0001-01-01' THEN '0001-01-01 00:00:00' 
 	                              ELSE  
 	                                  CASE WHEN  POR."FechaBloqueo" IN ('0001-01-01 00:00:00','1900-01-01 00:00:00.0') THEN '0001-01-01 00:00:00' 
 	                                  	ELSE LEFT(TO_CHAR(ADD_SECONDS (POR."FechaBloqueo", CONFIGURACION."Valor")),19)
 	                                  END 
 	                  END AS "FechaBloqueo"
				,CAST(POR."ComentarioBloqueo" AS NVARCHAR(520)) AS "ComentarioBloqueo"
				,CAST(POR."IdTipoBloqueo" AS NVARCHAR(40))  AS "IdTipoBloqueo"
				,CASE WHEN IFNULL(POR."FechaCreacion",'0001-01-01')= '0001-01-01' THEN '0001-01-01 00:00:00' 
 	                              ELSE  
 	                                  CASE WHEN  POR."FechaCreacion" IN ('0001-01-01 00:00:00','1900-01-01 00:00:00.0') THEN '0001-01-01 00:00:00' 
 	                                  	ELSE LEFT(TO_CHAR(ADD_SECONDS (POR."FechaCreacion", CONFIGURACION."Valor")),19)
 	                                  END 
 	             END AS "FechaCreacion"
				,CASE WHEN IFNULL(POR."FechaIngreso",'0001-01-01')= '0001-01-01' THEN '0001-01-01 00:00:00' 
 	                              ELSE  
 	                                  CASE WHEN  POR."FechaIngreso" IN ('0001-01-01 00:00:00','1900-01-01 00:00:00.0') THEN '0001-01-01 00:00:00' 
 	                                  	ELSE LEFT(TO_CHAR(ADD_SECONDS (POR."FechaIngreso", CONFIGURACION."Valor")),19)
 	                                  END 
 	             END AS "FechaIngreso"
				,CAST(POR."IdPerfilComercialBaseOrigen" AS NVARCHAR(40))  AS "IdPerfilComercialBaseOrigen"
				,CAST(POR."IdPerfilProductos" AS NVARCHAR (40))  AS "IdPerfilProductos"
				,CAST(POR."IdPerfilCobros" AS NVARCHAR(40))  AS "IdPerfilCobros"
				,CAST(POR."IdPerfilLimites" AS NVARCHAR(40))  AS "IdPerfilLimites"
				,CAST(POR."DescripcionPortafolio" AS NVARCHAR(100)) AS "DescripcionPortafolio"
				,CASE WHEN IFNULL(POR."FechaModificacion",'0001-01-01')= '0001-01-01' THEN '0001-01-01 00:00:00' 
 	                              ELSE  
 	                                  CASE WHEN  POR."FechaModificacion" IN ('0001-01-01 00:00:00','1900-01-01 00:00:00.0') THEN '0001-01-01 00:00:00' 
 	                                  	ELSE LEFT(TO_CHAR(ADD_SECONDS (POR."FechaModificacion", CONFIGURACION."Valor")),19)
 	                                  END 
 	             END AS "FechaModificacion"
				,CAST(POR."IdTipoDesbloqueo" AS NVARCHAR(40))  AS "IdTipoDesbloqueo"
				,CASE WHEN IFNULL(POR."FechaDesbloqueo",'0001-01-01')= '0001-01-01' THEN '0001-01-01 00:00:00' 
 	                              ELSE  
 	                                  CASE WHEN  POR."FechaDesbloqueo" IN ('0001-01-01 00:00:00','1900-01-01 00:00:00.0') THEN '0001-01-01 00:00:00' 
 	                                  	ELSE LEFT(TO_CHAR(ADD_SECONDS (POR."FechaDesbloqueo", CONFIGURACION."Valor")),19)
 	                                  END 
 	             END AS "FechaDesbloqueo"
				,CAST(POR."ComentarioDesbloqueo" AS NVARCHAR(520)) AS "ComentarioDesbloqueo"
				,CASE WHEN POR."CodigoPortafolio" = '' THEN NULL ELSE CAST(POR."CodigoPortafolio" AS NVARCHAR(100)) END AS "CodigoPortafolio"
				,CASE WHEN POR."IdTelefonoContacto" = 0 THEN NULL ELSE POR."IdTelefonoContacto" END AS "IdTelefonoContacto" 
				,CASE WHEN POR."IdDireccionEnvioCorrespondencia" = 0 THEN NULL ELSE POR."IdDireccionEnvioCorrespondencia" END AS "IdDireccionEnvioCorrespondencia" 
				,CAST(PER."Nombres" AS NVARCHAR(100)) AS "Nombres"
				,CAST(PER."ApellidoPaterno" AS NVARCHAR(100)) AS "ApellidoPaterno"
				,CAST(PER."ApellidoMaterno" AS NVARCHAR(100)) AS "ApellidoMaterno"
				,CAST(PER."RazonSocial" AS NVARCHAR(100)) AS "RazonSocial"
				,CASE WHEN PER."IdPersona" = 0 THEN NULL ELSE PER."IdPersona" END AS "IdPersona" 
				,CAST(DOC."DocumentoIdentidad"  AS NVARCHAR(40)) AS "DocumentoIdentidad"
				,TO_CHAR(COALESCE(MODPERFIL."FechaCambioPerfil",'0001-01-01')) as "FechaCambioPerfil"
				,CASE WHEN MODPERFIL."IdUsuario" = 0 THEN NULL ELSE MODPERFIL."IdUsuario" END  as "IdUsuarioModificacionPerfil"
				,' ' as "ALIASGRUPO" -- grupo al qe pertenece el portafolio
				,CASE WHEN POR."IdEmpleadoEjecutivo" = 0 THEN NULL ELSE POR."IdEmpleadoEjecutivo" END  as "IdEjecutivoPortafolio"
				,CASE WHEN COALESCE(POR."IdMatrizAgrupadorAPV",0) != 0 THEN COALESCE(POR."IdMatrizAgrupadorAPV",0) ELSE COALESCE(MAT_NOAPV."IdMatrizAgrupadorNOAPV",0) END AS "IdMatrizAgrupador"
				,CASE WHEN POR."IdEmpleadoEjecutivoComercial" = 0 THEN NULL ELSE POR."IdEmpleadoEjecutivoComercial" END  as "IdEjecutivoComercial"
				from "BSCL.Portafolio.BD::POR.Portafolio"  as POR
				LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" as CLI ON CLI."IdCliente" = POR."IdCliente"
				LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" as PER ON CLI."IdPersona" = PER."IdPersona"
				LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorNOAPV" MAT_NOAPV ON MAT_NOAPV."CodigoPortafolio" = POR."CodigoPortafolio" AND  POR."IdMatrizAgrupadorAPV" = 0
				LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as DOC ON PER."IdDocumentoIdentidad" = DOC."IdDocumentoIdentidad"
				LEFT JOIN "BSCL.Plataforma.BD::PLA.Configuracion" AS CONFIGURACION on "CodigoParametro" = 'PLA.SegundosZonaHoraria'
				LEFT JOIN (			       		 
							select	 
									RANK() over (partition by  "IdRegistro"  order by L."IdLog" DESC) as RANK
									, L."IdRegistro"
									, L."IdUsuario"
									, L."FechaLog" as "FechaCambioPerfil"
									, L."IdLog"
									, L."IdPortafolio"
									from "BSCL.Plataforma.BD::PLA.Log" as L
				            			JOIN "BSCL.Plataforma.BD::PLA.DetalleLog" as DL ON L."IdLog" = DL."IdLog"
				            		where L."IdTipoRegistro" = 'PORTAFOLIO' 
				            		   and upper(DL."CampoRegistro") = 'IDPERFILCOMERCIALBASEORIGEN'
						 ) AS MODPERFIL
				    ON MODPERFIL."IdPortafolio" = POR."IdPortafolio"
				    AND RANK = 1;

GRANT SELECT ON "BSCL.Portafolio.BD.Vistas::ConsultarPortafolioAnalitica" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
