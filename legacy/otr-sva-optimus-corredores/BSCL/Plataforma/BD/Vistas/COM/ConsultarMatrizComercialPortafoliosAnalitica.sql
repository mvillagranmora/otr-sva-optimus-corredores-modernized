SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Plataforma.BD.Vistas::COM.ConsultarMatrizComercialPortafoliosAnalitica"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::COM.ConsultarMatrizComercialPortafoliosAnalitica" AS
SELECT DISTINCT
				PRODUCTOS."IdEmpresa",
				CASE WHEN COBROS."IdCliente" = 0 THEN NULL ELSE COBROS."IdCliente" END AS "IdCliente",
				CAST(COBROS."NombreCliente" AS NVARCHAR(302)) AS "NombreCliente",
				CAST(PRODUCTOS."DocumentoIdentidad" AS NVARCHAR(40)) AS "DocumentoIdentidad",
				CASE WHEN PRODUCTOS."IdPortafolio" = 0 THEN NULL ELSE PRODUCTOS."IdPortafolio" END AS "IdPortafolio",
				CAST(PRODUCTOS."CodigoPortafolio" AS NVARCHAR(100)) AS "CodigoPortafolio",
				CASE WHEN PRODUCTOS."IdCategoriaInversionista" = '' THEN NULL ELSE CAST(PRODUCTOS."IdCategoriaInversionista" AS NVARCHAR(40))END AS "IdCategoriaInversionista",
				CASE WHEN PRODUCTOS."IdTipoProducto" = '' THEN NULL ELSE PRODUCTOS."IdTipoProducto" END AS "IdTipoProducto",
				CAST(PRODUCTOS."TipoProducto" AS NVARCHAR(100)) AS "TipoProducto",
				CASE WHEN COBROS."IdBolsa" = '' THEN NULL ELSE COBROS."IdBolsa" END AS "IdBolsa",
				COBROS."EsExtra" as "EsExtraCobros",
				PRODUCTOS."EsExtra" as "EsExtraProductos",
				CASE WHEN PRODUCTOS."IdTipoOperacion"=COBROS."IdTipoOperacion" THEN PRODUCTOS."IdTipoOperacion"
					WHEN PRODUCTOS."IdTipoOperacion" = '0' THEN COBROS."IdTipoOperacion"
					ELSE PRODUCTOS."IdTipoOperacion" END AS "IdTipoOperacion",
				CASE WHEN PRODUCTOS."IdTipoOperacion"=COBROS."IdTipoOperacion" THEN CAST(PRODUCTOS."TipoOperacion" AS NVARCHAR(100)) 
					WHEN PRODUCTOS."IdTipoOperacion" = '0' THEN CAST(COBROS."TipoOperacion" AS NVARCHAR(100))
					ELSE CAST(PRODUCTOS."TipoOperacion" AS NVARCHAR(100)) END AS "TipoOperacion",
				CASE WHEN PRODUCTOS."IdCanalIngreso"=COBROS."IdCanalIngreso" THEN PRODUCTOS."IdCanalIngreso"
					WHEN PRODUCTOS."IdCanalIngreso" = '0' THEN COBROS."IdCanalIngreso"
					ELSE PRODUCTOS."IdCanalIngreso" END AS "IdCanalIngreso",
				CASE WHEN PRODUCTOS."IdCanalIngreso"=COBROS."IdCanalIngreso" THEN CAST(PRODUCTOS."CanalIngreso" AS NVARCHAR(100))
					WHEN PRODUCTOS."IdCanalIngreso" = '0' THEN CAST(COBROS."CanalIngreso" AS NVARCHAR(100))
					ELSE CAST(PRODUCTOS."CanalIngreso" AS NVARCHAR(100)) END AS "CanalIngreso",
				CASE WHEN COBROS."IdCobroClasificacion" = 0 THEN NULL ELSE COBROS."IdCobroClasificacion" END AS "IdCobroClasificacion",
				coalesce(CAST(COBROS."CodigoCobroClasificacion" AS NVARCHAR(40)),'') AS "CodigoCobroClasificacion",
				CASE WHEN COBROS."IdCobro" = '' THEN NULL ELSE COBROS."IdCobro" END AS "IdCobro",
				coalesce(CAST(COBROS."NombreCobro" AS NVARCHAR(100)),'') AS "NombreCobro",
				COBROS."ValorCobro",-- ej:si es variable 19 es 19% , si es fijo 19 es 19 pesos/uf/etc...
				COBROS."FormaCobro",
				CASE WHEN COBROS."IdMoneda" = '' THEN NULL ELSE COBROS."IdMoneda" END AS "IdMoneda",
				COBROS."ValorTopeMinimoOrden",
				coalesce(COBROS."Prioridad",0) as "Prioridad",
				COBROS."DiasPlazo",
				COBROS."IdValorCobro"
			FROM --PlantillaCobrosConsolidada 
				(
					SELECT
						BASE."IdCliente",
						LEFT(BASE."NombreCompleto",100) as "NombreCliente",
						BASE."DocumentoIdentidad",
						BASE."IdPortafolio",
						BASE."CodigoPortafolio",
						IFNULL(EXTRA."IdInternal",BASE."IdInternal") 								as "IdInternal",
						IFNULL(EXTRA."Prioridad",BASE."Prioridad") 									as "Prioridad",
						IFNULL(EXTRA."IdPerfilCobros",BASE."IdPerfilCobros") 						as "IdPerfilCobros",
						IFNULL(EXTRA."DescripcionPerfilCobros",BASE."DescripcionPerfilCobros") 		as "DescripcionPerfilCobros",
						IFNULL(EXTRA."IdEmpresa",BASE."IdEmpresa") 									as "IdEmpresa",
						IFNULL(EXTRA."EstadoPerfilCobros",BASE."EstadoPerfilCobros") 				as "EstadoPerfilCobros",
						IFNULL(EXTRA."NombreEstadoPerfilCobros",BASE."NombreEstadoPerfilCobros") 	as "NombreEstadoPerfilCobros",
						IFNULL(EXTRA."EsPerfilPersonalizado",BASE."EsPerfilPersonalizado") 			as "EsPerfilPersonalizado",
						IFNULL(EXTRA."IdDetallePerfilCobros",BASE."IdDetallePerfilCobros") 			as "IdDetallePerfilCobros",
						IFNULL(EXTRA."IdBolsa",BASE."IdBolsa") 										as "IdBolsa",
						IFNULL(EXTRA."IdTipoProducto",BASE."IdTipoProducto") 						as "IdTipoProducto",
						IFNULL(EXTRA."TipoProducto",BASE."TipoProducto") 							as "TipoProducto",
						IFNULL(EXTRA."IdTipoOperacion",BASE."IdTipoOperacion") 						as "IdTipoOperacion",
						IFNULL(EXTRA."TipoOperacion",BASE."TipoOperacion") 							as "TipoOperacion",
						IFNULL(EXTRA."IdCanalIngreso",BASE."IdCanalIngreso") 						as "IdCanalIngreso",
						IFNULL(EXTRA."CanalIngreso",BASE."CanalIngreso") 							as "CanalIngreso",
						IFNULL(EXTRA."DiasPlazo",BASE."DiasPlazo") 									as "DiasPlazo",
						IFNULL(EXTRA."EstadoDetalle",BASE."EstadoDetalle") 							as "EstadoDetalle",
						IFNULL(EXTRA."NombreEstadoDetalle",BASE."NombreEstadoDetalle") 				as "NombreEstadoDetalle",
						IFNULL(EXTRA."IdValorCobro",BASE."IdValorCobro") 							as "IdValorCobro",
						IFNULL(EXTRA."DescripcionValorCobro",BASE."DescripcionValorCobro") 			as "DescripcionValorCobro",
						IFNULL(EXTRA."IdCobro",BASE."IdCobro") 										as "IdCobro",
						IFNULL(EXTRA."ValorCobro",BASE."ValorCobro") 								as "ValorCobro",
						IFNULL(EXTRA."ValorCobroTexto",BASE."ValorCobroTexto") 						as "ValorCobroTexto",
						IFNULL(EXTRA."ValorTopeMinimoOrden",BASE."ValorTopeMinimoOrden") 			as "ValorTopeMinimoOrden",
						IFNULL(EXTRA."ValorTopeMinimoOrdenTexto",BASE."ValorTopeMinimoOrdenTexto") 	as "ValorTopeMinimoOrdenTexto",
						IFNULL(EXTRA."IdMoneda",BASE."IdMoneda") 									as "IdMoneda",
						IFNULL(EXTRA."EstadoValorCobro",BASE."EstadoValorCobro") 					as "EstadoValorCobro",
						IFNULL(EXTRA."NombreCobro",BASE."NombreCobro") 								as "NombreCobro",
						IFNULL(EXTRA."IdCobroClasificacion",BASE."IdCobroClasificacion") 			as "IdCobroClasificacion",
						IFNULL(EXTRA."FormaCobro",BASE."FormaCobro") 								as "FormaCobro",
						IFNULL(EXTRA."PermiteCambiarValor",BASE."PermiteCambiarValor") 				as "PermiteCambiarValor",
						IFNULL(EXTRA."CodigoCobroClasificacion",BASE."CodigoCobroClasificacion") 	as "CodigoCobroClasificacion",
						IFNULL(EXTRA."CobroCalculado",BASE."CobroCalculado") 						as "CobroCalculado",
						IFNULL(EXTRA."EsExtra",BASE."EsExtra") 										as "EsExtra"
					FROM 
							(SELECT P."IdPortafolio",P."CodigoPortafolio",CLI."IdCliente", 
								CASE "PER"."Tipo" WHEN 'N' THEN "PER"."Nombres" ||' '|| "PER"."ApellidoPaterno" || ' ' ||"PER"."ApellidoMaterno" 
								ELSE "PER"."RazonSocial" END AS "NombreCompleto",DOC."DocumentoIdentidad",PCOB.*, 'N' as "EsExtra" 
								--FROM "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" CLI
								FROM "BSCL.SocioNegocio.BD::CLI.Cliente" CLI
								JOIN "BSCL.SocioNegocio.BD::PER.Persona" PER ON CLI."IdPersona" = PER."IdPersona"
								JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" DOC ON PER."IdDocumentoIdentidad" = DOC."IdDocumentoIdentidad"
								JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolioDatosPlantilla" P ON CLI."IdCliente" = P."IdCliente"
								JOIN "BSCL.Plataforma.BD.Vistas::COM.ConsultarDetallePerfilCobros" as PCOB ON PCOB."IdPerfilCobros" = P."IdPerfilCobros"
								LEFT JOIN "BSCL.Plataforma.BD.Vistas::COM.ConsultarDetallePerfilCobros" as PCOB2
									ON ( PCOB."IdPerfilCobros" = PCOB2."IdPerfilCobros"
									AND PCOB."IdBolsa" = PCOB2."IdBolsa"  
									AND PCOB."IdTipoProducto" = PCOB2."IdTipoProducto"
									AND (PCOB."IdTipoOperacion" = PCOB2."IdTipoOperacion")
									AND PCOB."IdCanalIngreso" = PCOB2."IdCanalIngreso"
									AND PCOB."CodigoCobroClasificacion"  = PCOB2."CodigoCobroClasificacion"
									AND PCOB."Prioridad">PCOB2."Prioridad")
								WHERE 
									PCOB2."Prioridad" IS NULL
									AND PCOB."EstadoPerfilCobros" = 'ACT'
								) as BASE 
							FULL JOIN
								(SELECT P."IdPortafolio",P."CodigoPortafolio",
								CASE "PER"."Tipo" WHEN 'N' THEN "PER"."Nombres" ||' '|| "PER"."ApellidoPaterno" || ' ' ||"PER"."ApellidoMaterno" 
								ELSE "PER"."RazonSocial" END AS "NombreCompleto",DOC."DocumentoIdentidad",PCOB.*, 'S' as "EsExtra" 
								--FROM "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" CLI
								FROM "BSCL.SocioNegocio.BD::CLI.Cliente" CLI
								JOIN "BSCL.SocioNegocio.BD::PER.Persona" PER ON CLI."IdPersona" = PER."IdPersona"
								JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" DOC ON PER."IdDocumentoIdentidad" = DOC."IdDocumentoIdentidad"
								JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolioDatosPlantilla" P ON CLI."IdCliente" = P."IdCliente"
								JOIN "BSCL.Plataforma.BD.Vistas::COM.ConsultarDetallePerfilCobros" as PCOB ON PCOB."IdPerfilCobros" = IFNULL(P."IdPerfilCobrosExtra",'')
								LEFT JOIN "BSCL.Plataforma.BD.Vistas::COM.ConsultarDetallePerfilCobros" as PCOB2
									ON ( PCOB."IdPerfilCobros" = PCOB2."IdPerfilCobros"
									AND PCOB."IdBolsa" = PCOB2."IdBolsa"  
									AND PCOB."IdTipoProducto" = PCOB2."IdTipoProducto"
									AND (PCOB."IdTipoOperacion" = PCOB2."IdTipoOperacion")
									AND PCOB."IdCanalIngreso" = PCOB2."IdCanalIngreso"
									AND PCOB."CodigoCobroClasificacion"  = PCOB2."CodigoCobroClasificacion"
									AND PCOB."Prioridad">PCOB2."Prioridad")
								WHERE 
									PCOB2."Prioridad" IS NULL
									AND PCOB."EstadoPerfilCobros" = 'ACT'
									
								) as EXTRA	
						ON  
							    EXTRA."IdEmpresa" 	= BASE."IdEmpresa"
							AND EXTRA."IdBolsa" = BASE."IdBolsa"
							AND (EXTRA."IdPortafolio"	 			= BASE."IdPortafolio") 
							AND (EXTRA."IdTipoProducto" 			= BASE."IdTipoProducto")
							AND (EXTRA."IdTipoOperacion" 			= BASE."IdTipoOperacion")
							AND (EXTRA."IdCanalIngreso" 			= BASE."IdCanalIngreso")
							AND (EXTRA."CodigoCobroClasificacion" 	= BASE."CodigoCobroClasificacion")
					WHERE 
						IFNULL(EXTRA."EstadoDetalle",BASE."EstadoDetalle") = 'ACT'	
				
				) COBROS
			INNER JOIN --PlantillaProductosConsolidada 
				(
					SELECT 
						BASE."IdPortafolio",BASE."CodigoPortafolio",BASE."NombreCompleto",BASE."DocumentoIdentidad",
						IFNULL(EXTRA."IdPerfilProductos",BASE."IdPerfilProductos") as "IdPerfilProductos",
						IFNULL(EXTRA."DescripcionPerfilProductos",BASE."DescripcionPerfilProductos") as "DescripcionPerfilProductos",
						IFNULL(EXTRA."IdCategoriaInversionista",BASE."IdCategoriaInversionista") as "IdCategoriaInversionista",
						IFNULL(EXTRA."FechaCreacion",BASE."FechaCreacion") as "FechaCreacion",
						IFNULL(EXTRA."FechaModificacion",BASE."FechaModificacion") as "FechaModificacion",
						IFNULL(EXTRA."EstadoPerfilProductos",BASE."EstadoPerfilProductos") as "EstadoPerfilProductos",
						IFNULL(EXTRA."PorDefecto",BASE."PorDefecto") as "PorDefecto",
						IFNULL(EXTRA."IdEmpresa",BASE."IdEmpresa") as "IdEmpresa",
						IFNULL(EXTRA."EsPerfilPersonalizado",BASE."EsPerfilPersonalizado") as "EsPerfilPersonalizado",
						IFNULL(EXTRA."IdTipoProducto",BASE."IdTipoProducto") as "IdTipoProducto",
						IFNULL(EXTRA."TipoProducto",BASE."TipoProducto") as "TipoProducto",
						IFNULL(EXTRA."IdTipoOperacion",BASE."IdTipoOperacion") as "IdTipoOperacion",
						IFNULL(EXTRA."TipoOperacion",BASE."TipoOperacion") as "TipoOperacion",
						IFNULL(EXTRA."IdCanalIngreso",BASE."IdCanalIngreso") as "IdCanalIngreso",
						IFNULL(EXTRA."CanalIngreso",BASE."CanalIngreso") as "CanalIngreso",
						IFNULL(EXTRA."IdMoneda",BASE."IdMoneda") as "IdMoneda",
						IFNULL(EXTRA."IdGrupoActivos",BASE."IdGrupoActivos") as "IdGrupoActivos",
						IFNULL(EXTRA."EstadoProducto",BASE."EstadoProducto") as "EstadoProducto",
						IFNULL(EXTRA."NombreEstadoProducto",BASE."NombreEstadoProducto") as "NombreEstadoProducto",
						IFNULL(EXTRA."EsExtra",BASE."EsExtra") as "EsExtra"
					FROM
						(SELECT P."IdPortafolio",P."CodigoPortafolio",
						CASE "PER"."Tipo" WHEN 'N' THEN "PER"."Nombres" ||' '|| "PER"."ApellidoPaterno" || ' ' ||"PER"."ApellidoMaterno" 
								ELSE "PER"."RazonSocial" END AS "NombreCompleto",DOC."DocumentoIdentidad", C.*, 'N' as "EsExtra"  
						--FROM "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" CLI
						FROM "BSCL.SocioNegocio.BD::CLI.Cliente" CLI
						JOIN "BSCL.SocioNegocio.BD::PER.Persona" PER ON CLI."IdPersona" = PER."IdPersona"
						JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" DOC ON PER."IdDocumentoIdentidad" = DOC."IdDocumentoIdentidad"
						JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolioDatosPlantilla" P ON CLI."IdCliente" = P."IdCliente" 
						JOIN "BSCL.Plataforma.BD.Vistas::COM.ConsultarDetallePerfilProductos" C ON C."IdPerfilProductos" = P."IdPerfilProductos"
						) 
								AS BASE
						FULL JOIN 
							(SELECT P."IdPortafolio",P."CodigoPortafolio",CLI."IdCliente",
							CASE "PER"."Tipo" WHEN 'N' THEN "PER"."Nombres" ||' '|| "PER"."ApellidoPaterno" || ' ' ||"PER"."ApellidoMaterno" 
								ELSE "PER"."RazonSocial" END AS "NombreCompleto",DOC."DocumentoIdentidad", C.*, 'S' as "EsExtra"  
							--FROM "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" CLI
							FROM "BSCL.SocioNegocio.BD::CLI.Cliente" CLI
							JOIN "BSCL.SocioNegocio.BD::PER.Persona" PER ON CLI."IdPersona" = PER."IdPersona"
							JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" DOC ON PER."IdDocumentoIdentidad" = DOC."IdDocumentoIdentidad"
							JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolioDatosPlantilla" P ON CLI."IdCliente" = P."IdCliente"
							JOIN "BSCL.Plataforma.BD.Vistas::COM.ConsultarDetallePerfilProductos" C ON IFNULL(P."IdPerfilProductosExtra",'') = C."IdPerfilProductos" 
							) AS EXTRA
						ON   
							     BASE."IdEmpresa" 	 	= EXTRA."IdEmpresa" 	
							AND (BASE."IdPortafolio" 	= EXTRA."IdPortafolio")
							AND (BASE."IdTipoProducto" 	= EXTRA."IdTipoProducto")
							AND (BASE."IdTipoOperacion" = EXTRA."IdTipoOperacion")
							AND (BASE."IdCanalIngreso" 	= EXTRA."IdCanalIngreso")
				)
				PRODUCTOS
				ON 	
					COBROS."IdPortafolio" = PRODUCTOS."IdPortafolio"
					AND (COBROS."IdTipoProducto" = PRODUCTOS."IdTipoProducto"
					)
					AND (COBROS."IdTipoOperacion" = PRODUCTOS."IdTipoOperacion" 
						OR (COBROS."IdTipoOperacion" = '0' AND IFNULL(PRODUCTOS."IdTipoOperacion",'') != '') 
						OR (PRODUCTOS."IdTipoOperacion" = '0' AND IFNULL(COBROS."IdTipoOperacion",'') != '') 
					)
					AND (COBROS."IdCanalIngreso" = PRODUCTOS."IdCanalIngreso"
						OR (COBROS."IdCanalIngreso" = '0' AND IFNULL(PRODUCTOS."IdCanalIngreso",'') != '') 
						OR (PRODUCTOS."IdCanalIngreso" = '0' AND IFNULL(COBROS."IdCanalIngreso",'') != '') 
					)
			ORDER BY 
				PRODUCTOS."IdEmpresa",
				COBROS."IdCliente",
				COBROS."NombreCliente",
				PRODUCTOS."DocumentoIdentidad",
				PRODUCTOS."IdPortafolio",
				PRODUCTOS."CodigoPortafolio",
				PRODUCTOS."IdCategoriaInversionista",
				PRODUCTOS."IdTipoProducto",
				PRODUCTOS."TipoProducto",
				COBROS."IdBolsa",
				PRODUCTOS."IdTipoOperacion",
				COBROS."IdCobro",
				COBROS."IdMoneda"; 

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::COM.ConsultarMatrizComercialPortafoliosAnalitica" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
