SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Plataforma.BD.Vistas::COM.ConsultarMatrizComercialCorredoraAnalitica"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::COM.ConsultarMatrizComercialCorredoraAnalitica" AS
SELECT DISTINCT
	 	 COBROS."IdEmpresa",
	 	 CASE WHEN COBROS."IdPerfilComercialBase" = '' THEN NULL ELSE CAST(COBROS."IdPerfilComercialBase" AS NVARCHAR(40)) END AS "IdPerfilComercialBase",
		 CAST(COBROS."DescripcionPerfilComercialBase" AS NVARCHAR(100)) AS "DescripcionPerfilComercialBase",
		 CASE WHEN PRODUCTOS."IdCategoriaInversionista" = '' THEN NULL ELSE CAST(PRODUCTOS."IdCategoriaInversionista" AS NVARCHAR(40)) END AS "IdCategoriaInversionista",
		 CASE WHEN PRODUCTOS."IdTipoProducto" = '' THEN NULL ELSE PRODUCTOS."IdTipoProducto" END AS "IdTipoProducto",
		 CAST(PRODUCTOS."TipoProducto" AS NVARCHAR(100)) AS "TipoProducto",
		 CASE WHEN COBROS."IdBolsa" = '' THEN NULL ELSE COBROS."IdBolsa" END AS "IdBolsa",
		 CASE 	WHEN PRODUCTOS."IdTipoOperacion"=COBROS."IdTipoOperacion" THEN PRODUCTOS."IdTipoOperacion"
		 		WHEN PRODUCTOS."IdTipoOperacion" = '0' THEN COBROS."IdTipoOperacion"
		 ELSE PRODUCTOS."IdTipoOperacion" END 								AS "IdTipoOperacion",
		 CASE WHEN PRODUCTOS."IdTipoOperacion"=COBROS."IdTipoOperacion" THEN CAST(PRODUCTOS."TipoOperacion" AS NVARCHAR(100))
		 		WHEN PRODUCTOS."IdTipoOperacion" = '0' THEN CAST(COBROS."TipoOperacion" AS NVARCHAR(100))
		 ELSE CAST(PRODUCTOS."TipoOperacion" AS NVARCHAR(100)) END 								AS "TipoOperacion",
		 CASE WHEN PRODUCTOS."IdCanalIngreso"=COBROS."IdCanalIngreso" THEN PRODUCTOS."IdCanalIngreso"
		 	WHEN PRODUCTOS."IdCanalIngreso" = '0' THEN COBROS."IdCanalIngreso"
		 ELSE PRODUCTOS."IdCanalIngreso" END 								AS "IdCanalIngreso",
		 CASE WHEN PRODUCTOS."IdCanalIngreso"=COBROS."IdCanalIngreso" THEN CAST(PRODUCTOS."CanalIngreso" AS NVARCHAR(100))
		 	WHEN PRODUCTOS."IdCanalIngreso" = '0' THEN CAST(COBROS."CanalIngreso" AS NVARCHAR(100))
		 ELSE CAST(PRODUCTOS."CanalIngreso" AS NVARCHAR(100)) END 									AS "CanalIngreso",
		 CASE WHEN COBROS."IdCobroClasificacion" = 0 THEN NULL ELSE COBROS."IdCobroClasificacion" END AS "IdCobroClasificacion",
		 CAST(COBROS."CodigoCobroClasificacion" AS NVARCHAR(40)) AS "CodigoCobroClasificacion",
		 CASE WHEN COBROS."IdCobro" = '' THEN NULL ELSE COBROS."IdCobro" END AS "IdCobro",
		 CAST(COBROS."NombreCobro" AS NVARCHAR(100)) AS "NombreCobro",
		 COBROS."ValorCobro",-- ej:si es variable 19 es 19% , si es fijo 19 es 19 pesos/uf/etc...
		 COBROS."FormaCobro",
		 CASE WHEN COBROS."IdMoneda" = '' THEN NULL ELSE COBROS."IdMoneda" END AS "IdMoneda",
		 COBROS."ValorTopeMinimoOrden",
		 coalesce(COBROS."Prioridad",0) as "Prioridad",
		 COBROS."DiasPlazo" 
		FROM --PlantillaCobrosConsolidada 
	  (
	 	SELECT
		 	 BASE."IdPerfilComercialBase",
		 	 BASE."DescripcionPerfilComercialBase",
			 IFNULL(EXTRA."IdInternal",BASE."IdInternal")         						as "IdInternal",
			 IFNULL(EXTRA."Prioridad",BASE."Prioridad")          						as "Prioridad",
			 IFNULL(EXTRA."IdPerfilCobros",BASE."IdPerfilCobros")       				as "IdPerfilCobros",
			 IFNULL(EXTRA."DescripcionPerfilCobros",BASE."DescripcionPerfilCobros")   	as "DescripcionPerfilCobros",
			 IFNULL(EXTRA."IdEmpresa",BASE."IdEmpresa")          						as "IdEmpresa",
			 IFNULL(EXTRA."EstadoPerfilCobros",BASE."EstadoPerfilCobros")     			as "EstadoPerfilCobros",
			 IFNULL(EXTRA."NombreEstadoPerfilCobros",BASE."NombreEstadoPerfilCobros")  	as "NombreEstadoPerfilCobros",
			 IFNULL(EXTRA."EsPerfilPersonalizado",BASE."EsPerfilPersonalizado")    		as "EsPerfilPersonalizado",
			 IFNULL(EXTRA."IdDetallePerfilCobros",BASE."IdDetallePerfilCobros")    		as "IdDetallePerfilCobros",
			 IFNULL(EXTRA."IdBolsa",BASE."IdBolsa")           							as "IdBolsa",
			 IFNULL(EXTRA."IdTipoProducto",BASE."IdTipoProducto")       				as "IdTipoProducto",
			 IFNULL(EXTRA."TipoProducto",BASE."TipoProducto")        					as "TipoProducto",
			 IFNULL(EXTRA."IdTipoOperacion",BASE."IdTipoOperacion")       				as "IdTipoOperacion",
			 IFNULL(EXTRA."TipoOperacion",BASE."TipoOperacion")        					as "TipoOperacion",
			 IFNULL(EXTRA."IdCanalIngreso",BASE."IdCanalIngreso")       				as "IdCanalIngreso",
			 IFNULL(EXTRA."CanalIngreso",BASE."CanalIngreso")        					as "CanalIngreso",
			 IFNULL(EXTRA."DiasPlazo",BASE."DiasPlazo")          						as "DiasPlazo",
			 IFNULL(EXTRA."EstadoDetalle",BASE."EstadoDetalle")        					as "EstadoDetalle",
			 IFNULL(EXTRA."NombreEstadoDetalle",BASE."NombreEstadoDetalle")     		as "NombreEstadoDetalle",
			 IFNULL(EXTRA."IdValorCobro",BASE."IdValorCobro")        					as "IdValorCobro",
			 IFNULL(EXTRA."DescripcionValorCobro",BASE."DescripcionValorCobro")    		as "DescripcionValorCobro",
			 IFNULL(EXTRA."IdCobro",BASE."IdCobro")           							as "IdCobro",
			 IFNULL(EXTRA."ValorCobro",BASE."ValorCobro")         						as "ValorCobro",
			 IFNULL(EXTRA."ValorCobroTexto",BASE."ValorCobroTexto")       				as "ValorCobroTexto",
			 IFNULL(EXTRA."ValorTopeMinimoOrden",BASE."ValorTopeMinimoOrden")    		as "ValorTopeMinimoOrden",
			 IFNULL(EXTRA."ValorTopeMinimoOrdenTexto",BASE."ValorTopeMinimoOrdenTexto") as "ValorTopeMinimoOrdenTexto",
			 IFNULL(EXTRA."IdMoneda",BASE."IdMoneda")          							as "IdMoneda",
			 IFNULL(EXTRA."EstadoValorCobro",BASE."EstadoValorCobro")      				as "EstadoValorCobro",
			 IFNULL(EXTRA."NombreCobro",BASE."NombreCobro")         					as "NombreCobro",
			 IFNULL(EXTRA."IdCobroClasificacion",BASE."IdCobroClasificacion")    		as "IdCobroClasificacion",
			 IFNULL(EXTRA."FormaCobro",BASE."FormaCobro")         						as "FormaCobro",
			 IFNULL(EXTRA."PermiteCambiarValor",BASE."PermiteCambiarValor")     		as "PermiteCambiarValor",
			 IFNULL(EXTRA."CodigoCobroClasificacion",BASE."CodigoCobroClasificacion")  	as "CodigoCobroClasificacion",
			 IFNULL(EXTRA."CobroCalculado",BASE."CobroCalculado")       				as "CobroCalculado"
		 FROM
		 	(SELECT P."DescripcionPerfilComercialBase", P."IdPerfilComercialBase", PCOB.*
			 FROM "BSCL.Plataforma.BD::COM.PerfilComercialBase" as P
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
		 (SELECT '' as "DescripcionPerfilComercialBase",PCOB.*
			 FROM "BSCL.Plataforma.BD.Vistas::COM.ConsultarDetallePerfilCobros" 		as PCOB
			 LEFT JOIN "BSCL.Plataforma.BD.Vistas::COM.ConsultarDetallePerfilCobros" 	as PCOB2
			 ON ( PCOB."IdPerfilCobros" = PCOB2."IdPerfilCobros"
			 AND PCOB."IdBolsa" = PCOB2."IdBolsa"
			 AND PCOB."IdTipoProducto" = PCOB2."IdTipoProducto"
			 AND (PCOB."IdTipoOperacion" = PCOB2."IdTipoOperacion")
			 AND PCOB."IdCanalIngreso" = PCOB2."IdCanalIngreso"
			 AND PCOB."CodigoCobroClasificacion"  = PCOB2."CodigoCobroClasificacion"
			 AND PCOB."Prioridad">PCOB2."Prioridad")
			 WHERE
			 PCOB2."Prioridad" IS NULL
			 AND PCOB."IdPerfilCobros" = ''
			 AND PCOB."EstadoPerfilCobros" = 'ACT'
			 ) as EXTRA
		 ON
		 	 EXTRA."IdBolsa" = BASE."IdBolsa"
		 AND EXTRA."IdEmpresa" 			= BASE."IdEmpresa"
		 AND (EXTRA."IdTipoProducto"    = BASE."IdTipoProducto")
		 AND (EXTRA."IdTipoOperacion"   = BASE."IdTipoOperacion")
		 AND (EXTRA."IdCanalIngreso"    = BASE."IdCanalIngreso")
		 AND (EXTRA."CodigoCobroClasificacion"  = BASE."CodigoCobroClasificacion")
		 WHERE
		 IFNULL(EXTRA."EstadoDetalle",BASE."EstadoDetalle") = 'ACT'
	  ) COBROS
	 INNER JOIN --PlantillaProductosConsolidada 
	 (
	  SELECT
	 	 BASE."DescripcionPerfilComercialBase"											as "DescripcionPerfilComercialBase",
		 IFNULL(EXTRA."IdPerfilProductos",BASE."IdPerfilProductos") 					as "IdPerfilProductos",
		 IFNULL(EXTRA."DescripcionPerfilProductos",BASE."DescripcionPerfilProductos") 	as "DescripcionPerfilProductos",
		 IFNULL(EXTRA."IdCategoriaInversionista",BASE."IdCategoriaInversionista") 		as "IdCategoriaInversionista",
		 IFNULL(EXTRA."FechaCreacion",BASE."FechaCreacion") 							as "FechaCreacion",
		 IFNULL(EXTRA."FechaModificacion",BASE."FechaModificacion") 					as "FechaModificacion",
		 IFNULL(EXTRA."EstadoPerfilProductos",BASE."EstadoPerfilProductos") 			as "EstadoPerfilProductos",
		 IFNULL(EXTRA."PorDefecto",BASE."PorDefecto") 									as "PorDefecto",
		 IFNULL(EXTRA."IdEmpresa",BASE."IdEmpresa") 									as "IdEmpresa",
		 IFNULL(EXTRA."EsPerfilPersonalizado",BASE."EsPerfilPersonalizado") 			as "EsPerfilPersonalizado",
		 IFNULL(EXTRA."IdTipoProducto",BASE."IdTipoProducto") 							as "IdTipoProducto",
		 IFNULL(EXTRA."TipoProducto",BASE."TipoProducto") 								as "TipoProducto",
		 IFNULL(EXTRA."IdTipoOperacion",BASE."IdTipoOperacion") 						as "IdTipoOperacion",
		 IFNULL(EXTRA."TipoOperacion",BASE."TipoOperacion") 							as "TipoOperacion",
		 IFNULL(EXTRA."IdCanalIngreso",BASE."IdCanalIngreso") 							as "IdCanalIngreso",
		 IFNULL(EXTRA."CanalIngreso",BASE."CanalIngreso") 								as "CanalIngreso",
		 IFNULL(EXTRA."IdMoneda",BASE."IdMoneda") 										as "IdMoneda",
		 IFNULL(EXTRA."IdGrupoActivos",BASE."IdGrupoActivos") 							as "IdGrupoActivos",
		 IFNULL(EXTRA."EstadoProducto",BASE."EstadoProducto") 							as "EstadoProducto",
		 IFNULL(EXTRA."NombreEstadoProducto",BASE."NombreEstadoProducto") 				as "NombreEstadoProducto"
	 FROM
	 (SELECT P."DescripcionPerfilComercialBase",C.* FROM
		 "BSCL.Plataforma.BD::COM.PerfilComercialBase" P
		 JOIN "BSCL.Plataforma.BD.Vistas::COM.ConsultarDetallePerfilProductos" C ON C."IdPerfilProductos" = P."IdPerfilProductos"
		 ) AS BASE
	 FULL JOIN
	 (SELECT * FROM "BSCL.Plataforma.BD.Vistas::COM.ConsultarDetallePerfilProductos"
		 WHERE "IdPerfilProductos" = '' 
	    ) AS EXTRA
	 ON
	     BASE."IdEmpresa" 			= EXTRA."IdEmpresa" 
	 AND (BASE."IdTipoProducto" 	= EXTRA."IdTipoProducto")
	 AND (BASE."IdTipoOperacion" 	= EXTRA."IdTipoOperacion")
	 AND (BASE."IdCanalIngreso" 	= EXTRA."IdCanalIngreso")
	 )
	 PRODUCTOS
	  	 ON  COBROS."IdTipoProducto" = PRODUCTOS."IdTipoProducto"
		 AND COBROS."DescripcionPerfilComercialBase" = PRODUCTOS."DescripcionPerfilComercialBase"
		 AND (COBROS."IdTipoOperacion" = PRODUCTOS."IdTipoOperacion"
		 OR (COBROS."IdTipoOperacion" = '0' AND IFNULL(PRODUCTOS."IdTipoOperacion",'') != '')
		 OR (PRODUCTOS."IdTipoOperacion" = '0' AND IFNULL(COBROS."IdTipoOperacion",'') != '')
		 )
		 AND (COBROS."IdCanalIngreso" = PRODUCTOS."IdCanalIngreso"
		 OR (COBROS."IdCanalIngreso" = '0' AND IFNULL(PRODUCTOS."IdCanalIngreso",'') != '')
		 OR (PRODUCTOS."IdCanalIngreso" = '0' AND IFNULL(COBROS."IdCanalIngreso",'') != '')
		 )
		 ORDER BY  
		 "DescripcionPerfilComercialBase",
		 "IdTipoProducto",
		 "IdBolsa",
		 "IdTipoOperacion",
		 "IdCanalIngreso",
		 "IdCobro",
		 "IdMoneda";

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::COM.ConsultarMatrizComercialCorredoraAnalitica" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
