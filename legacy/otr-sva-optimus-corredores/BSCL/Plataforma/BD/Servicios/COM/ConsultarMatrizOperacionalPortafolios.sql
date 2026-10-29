SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Plataforma.BD.Servicios::COM.ConsultarMatrizOperacionalPortafolios";

CREATE PROCEDURE
--  
--  Proposito: Consulta Matriz Operacional Para N Portafolios.
--  Autor: - JG
--  Fecha Creacion: 02/06/2016
--   
"BSCL.Plataforma.BD.Servicios::COM.ConsultarMatrizOperacionalPortafolios" (

	--IN p_Portafolios	TABLE("IdPortafolio" bigint),

	IN  p_IdEmpresa					bigint,			--ID Empresa destino
	IN  p_IdUsuario					bigint,			--ID del usuario que solicita el servicio
	IN  p_IdModulo					nvarchar(3),  	--ID del Modulo que lo invoca
	
	IN p_Portafolios	TABLE("IdPortafolio" bigint, "IdTipoProducto" NVARCHAR(4), "IdTipoOperacion" NVARCHAR(6), "IdCanalIngreso" NVARCHAR(4),"IdBolsa" NVARCHAR(3)),
	
	OUT p_out_MatrizOperacional table (
		"IdPortafolio"				BIGINT,
		"IdBolsa" 					NVARCHAR(10),
		"IdTipoProducto"			NVARCHAR(4),
		"IdTipoOperacion"			NVARCHAR(6),
		"IdCanalIngreso"			NVARCHAR(4),
		"IdCobro"					NVARCHAR(10),
		"ValorCobro"				DECIMAL(26,8), -- ej:si es variable 19 es 19% , si es fijo 19 es 19 pesos/uf/etc...
		"FormaCobro"				NVARCHAR(1),
		"ValorTopeMinimoOrden"		DECIMAL(26,8),
		"IdMoneda"					NVARCHAR(3),
		"Prioridad" 				BIGINT,
		"DiasPlazo"					BIGINT,
		"NombreCobro"				NVARCHAR(100),
		"IdCobroClasificacion"		BIGINT,
		"CodigoCobroClasificacion"	NVARCHAR(40),
		"PermiteCambiarValor"		NVARCHAR(1),
		"IdEmpresa" 				BIGINT
		),
		
		
	-- Salidas para mensajes de advertencias y errores
	OUT p_out_AdvertenciasContador  	 	int,
	OUT p_out_AdvertenciasDetalle  	 	 	table ("Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(5000)),
	OUT p_out_ErroresContador  	 	 	 	int,
	OUT p_out_ErroresDetalle  	 	 	 	table ("Codigo" nvarchar(40),"Descripcion" nvarchar(5000))
)

LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER 
DEFAULT SCHEMA "BSCL"
READS SQL DATA AS


BEGIN

	--------------------
	--DECLARE Manejo de Errores
	--------------------
	DECLARE SALIDA_CONTROLADA CONDITION;
	DECLARE v_errorcuenta INT DEFAULT 0;
	DECLARE v_errorhashmsg table ("Codigo" nvarchar(40),"Valor" nvarchar(520));
	DECLARE v_erroroutmsg  nvarchar(520);
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA
	BEGIN
		--salida/error/advertencia controlado por este servicio
		select count(1) into p_out_ErroresContador
		from :p_out_ErroresDetalle;
		select count(1) into p_out_AdvertenciasContador
		from :p_out_AdvertenciasDetalle;
	END;

	p_out_AdvertenciasContador	:= 0;
	p_out_ErroresContador		:= 0;
	p_out_AdvertenciasDetalle	= select '' as "Codigo",'' as "RequiereAutorizacion",'' as "Descripcion" from dummy where 1=2;
	p_out_ErroresDetalle		= select '' as "Codigo",'' as "Descripcion" from dummy where 1=2;	

	-- GENERACION DE LOS DATOS
	
	p_out_MatrizOperacional = 
	SELECT DISTINCT 
		MATRIZOPERACIONAL."IdPortafolio",
		MATRIZOPERACIONAL."IdBolsa",
		INPORTAFOLIOS."IdTipoProducto",
		INPORTAFOLIOS."IdTipoOperacion",
		INPORTAFOLIOS."IdCanalIngreso",
		MATRIZOPERACIONAL."IdCobro",
		MATRIZOPERACIONAL."ValorCobro",
		MATRIZOPERACIONAL."FormaCobro",
		MATRIZOPERACIONAL."ValorTopeMinimoOrden",
		MATRIZOPERACIONAL."IdMoneda",
		MATRIZOPERACIONAL."Prioridad",
		MATRIZOPERACIONAL."DiasPlazo",
		MATRIZOPERACIONAL."NombreCobro",
		MATRIZOPERACIONAL."IdCobroClasificacion",
		MATRIZOPERACIONAL."CodigoCobroClasificacion",
		MATRIZOPERACIONAL."PermiteCambiarValor",
		MATRIZOPERACIONAL."IdEmpresa"
	FROM
		:p_Portafolios as INPORTAFOLIOS
		INNER JOIN
		(SELECT 
			COBROSPORTAFOLIO."IdPortafolio",
			COBROSPORTAFOLIO."IdBolsa",
			CASE WHEN PRODUCTOSPORTAFOLIO."IdTipoProducto"=COBROSPORTAFOLIO."IdTipoProducto" THEN PRODUCTOSPORTAFOLIO."IdTipoProducto"
				ELSE PRODUCTOSPORTAFOLIO."IdTipoProducto" END AS "IdTipoProducto",
			/*CASE WHEN PRODUCTOSPORTAFOLIO."IdTipoProducto"=COBROSPORTAFOLIO."IdTipoProducto" THEN PRODUCTOSPORTAFOLIO."TipoProducto"
				ELSE PRODUCTOSPORTAFOLIO."TipoProducto" END AS "TipoProducto",*/
			CASE WHEN PRODUCTOSPORTAFOLIO."IdTipoOperacion"=COBROSPORTAFOLIO."IdTipoOperacion" THEN PRODUCTOSPORTAFOLIO."IdTipoOperacion"
				WHEN PRODUCTOSPORTAFOLIO."IdTipoOperacion" = '0' THEN COBROSPORTAFOLIO."IdTipoOperacion"
				ELSE PRODUCTOSPORTAFOLIO."IdTipoOperacion" END AS "IdTipoOperacion",
			/*CASE WHEN PRODUCTOSPORTAFOLIO."IdTipoOperacion"=COBROSPORTAFOLIO."IdTipoOperacion" THEN PRODUCTOSPORTAFOLIO."TipoOperacion"
				WHEN PRODUCTOSPORTAFOLIO."IdTipoOperacion" = '0' THEN COBROSPORTAFOLIO."TipoOperacion"
				ELSE PRODUCTOSPORTAFOLIO."TipoOperacion" END AS "TipoOperacion",*/
			CASE WHEN PRODUCTOSPORTAFOLIO."IdCanalIngreso"=COBROSPORTAFOLIO."IdCanalIngreso" THEN PRODUCTOSPORTAFOLIO."IdCanalIngreso"
				WHEN PRODUCTOSPORTAFOLIO."IdCanalIngreso" = '0' THEN COBROSPORTAFOLIO."IdCanalIngreso"
				ELSE PRODUCTOSPORTAFOLIO."IdCanalIngreso" END AS "IdCanalIngreso",
			/*CASE WHEN PRODUCTOSPORTAFOLIO."IdCanalIngreso"=COBROSPORTAFOLIO."IdCanalIngreso" THEN PRODUCTOSPORTAFOLIO."CanalIngreso"
				WHEN PRODUCTOSPORTAFOLIO."IdCanalIngreso" = '0' THEN COBROSPORTAFOLIO."CanalIngreso"
				ELSE PRODUCTOSPORTAFOLIO."CanalIngreso" END AS "CanalIngreso",*/
			COBROSPORTAFOLIO."IdCobro",
			COBROSPORTAFOLIO."ValorCobro", -- ej:si es variable 19 es 19% , si es fijo 19 es 19 pesos/uf/etc...
			COBROSPORTAFOLIO."FormaCobro",
			COBROSPORTAFOLIO."ValorTopeMinimoOrden",
			COBROSPORTAFOLIO."IdMoneda",
			COBROSPORTAFOLIO."Prioridad",
			COBROSPORTAFOLIO."DiasPlazo",
			COBROSPORTAFOLIO."NombreCobro",
			COBROSPORTAFOLIO."IdCobroClasificacion",
			COBROSPORTAFOLIO."CodigoCobroClasificacion",
			COBROSPORTAFOLIO."PermiteCambiarValor",
			PRODUCTOSPORTAFOLIO."IdEmpresa"
		FROM 
			(SELECT 
				IFNULL(PORTPRODEXTRA."IdPortafolio",PORTPRODBASE."IdPortafolio") 			as "IdPortafolio",
				IFNULL(PORTPRODEXTRA."IdPerfilProductos",PORTPRODBASE."IdPerfilProductos") 	as "IdPerfilProductos",
				IFNULL(PORTPRODEXTRA."IdTipoProducto",PORTPRODBASE."IdTipoProducto") 		as "IdTipoProducto",
				--IFNULL(PORTPRODEXTRA."TipoProducto",PORTPRODBASE."TipoProducto") 			as "TipoProducto",
				IFNULL(PORTPRODEXTRA."IdTipoOperacion",PORTPRODBASE."IdTipoOperacion") 		as "IdTipoOperacion",
				--IFNULL(PORTPRODEXTRA."TipoOperacion",PORTPRODBASE."TipoOperacion") 			as "TipoOperacion",
				IFNULL(PORTPRODEXTRA."IdCanalIngreso",PORTPRODBASE."IdCanalIngreso")	 	as "IdCanalIngreso",
				--IFNULL(PORTPRODEXTRA."CanalIngreso",PORTPRODBASE."CanalIngreso")	 		as "CanalIngreso",
				IFNULL(PORTPRODEXTRA."IdMoneda",PORTPRODBASE."IdMoneda") 					as "IdMoneda",
				IFNULL(PORTPRODEXTRA."IdGrupoActivos",PORTPRODBASE."IdGrupoActivos") 		as "IdGrupoActivos",
				IFNULL(PORTPRODEXTRA."IdEmpresa",PORTPRODBASE."IdEmpresa") 					as "IdEmpresa"
			FROM
				(SELECT POR."IdPortafolio",BASE.*
				FROM "BSCL.Portafolio.BD::POR.Portafolio" as POR
					INNER JOIN :p_Portafolios INPOR ON POR."IdPortafolio" = INPOR."IdPortafolio"
					INNER JOIN "BSCL.Plataforma.BD.Vistas::COM.ConsultarDetallePerfilProductos" AS BASE
					ON (BASE."IdPerfilProductos" = POR."IdPerfilProductos")
				WHERE BASE."EstadoProducto"= 'ACT' AND BASE."EstadoPerfilProductos" = 'ACT')  AS PORTPRODBASE
			FULL JOIN 
				(SELECT POR."IdPortafolio",EXTRA.*
				FROM "BSCL.Portafolio.BD::POR.Portafolio" as POR
					INNER JOIN :p_Portafolios INPOR ON POR."IdPortafolio" = INPOR."IdPortafolio"
					INNER JOIN "BSCL.Plataforma.BD.Vistas::COM.ConsultarDetallePerfilProductos" as EXTRA
					ON (EXTRA."IdPerfilProductos" = POR."IdPerfilProductosExtra") 
				WHERE EXTRA."EstadoProducto"= 'ACT' AND EXTRA."EstadoPerfilProductos" = 'ACT') AS PORTPRODEXTRA
				ON	
					(PORTPRODBASE."IdPortafolio" 		= PORTPRODEXTRA."IdPortafolio") 
					AND (PORTPRODBASE."IdTipoProducto" 	= PORTPRODEXTRA."IdTipoProducto")
					AND (PORTPRODBASE."IdTipoOperacion" = PORTPRODEXTRA."IdTipoOperacion")
					AND (PORTPRODBASE."IdCanalIngreso" 	= PORTPRODEXTRA."IdCanalIngreso")
			) AS PRODUCTOSPORTAFOLIO
		INNER JOIN
			(SELECT
				IFNULL(PORTCOBEXTRA."IdPortafolio",PORTCOBBASE."IdPortafolio") 								as "IdPortafolio",
				IFNULL(PORTCOBEXTRA."Prioridad",PORTCOBBASE."Prioridad") 									as "Prioridad",
				IFNULL(PORTCOBEXTRA."IdEmpresa",PORTCOBBASE."IdEmpresa") 									as "IdEmpresa",
				IFNULL(PORTCOBEXTRA."IdBolsa",PORTCOBBASE."IdBolsa") 										as "IdBolsa",
				IFNULL(PORTCOBEXTRA."IdTipoProducto",PORTCOBBASE."IdTipoProducto") 							as "IdTipoProducto",
				--IFNULL(PORTCOBEXTRA."TipoProducto",PORTCOBBASE."TipoProducto") 								as "TipoProducto",
				IFNULL(PORTCOBEXTRA."IdTipoOperacion",PORTCOBBASE."IdTipoOperacion") 						as "IdTipoOperacion",
				--IFNULL(PORTCOBEXTRA."TipoOperacion",PORTCOBBASE."TipoOperacion") 							as "TipoOperacion",
				IFNULL(PORTCOBEXTRA."IdCanalIngreso",PORTCOBBASE."IdCanalIngreso") 							as "IdCanalIngreso",
				--IFNULL(PORTCOBEXTRA."CanalIngreso",PORTCOBBASE."CanalIngreso") 								as "CanalIngreso",
				IFNULL(PORTCOBEXTRA."DiasPlazo",PORTCOBBASE."DiasPlazo") 									as "DiasPlazo",
				IFNULL(PORTCOBEXTRA."EstadoDetalle",PORTCOBBASE."EstadoDetalle") 							as "EstadoDetalle",
				IFNULL(PORTCOBEXTRA."IdValorCobro",PORTCOBBASE."IdValorCobro") 								as "IdValorCobro",
				IFNULL(PORTCOBEXTRA."IdCobro",PORTCOBBASE."IdCobro") 										as "IdCobro",
				IFNULL(PORTCOBEXTRA."ValorCobro",PORTCOBBASE."ValorCobro") 									as "ValorCobro",
				IFNULL(PORTCOBEXTRA."ValorTopeMinimoOrden",PORTCOBBASE."ValorTopeMinimoOrden") 				as "ValorTopeMinimoOrden",
				IFNULL(PORTCOBEXTRA."IdMoneda",PORTCOBBASE."IdMoneda") 										as "IdMoneda",
				IFNULL(PORTCOBEXTRA."NombreCobro",PORTCOBBASE."NombreCobro") 								as "NombreCobro",
				IFNULL(PORTCOBEXTRA."IdCobroClasificacion",PORTCOBBASE."IdCobroClasificacion") 				as "IdCobroClasificacion",
				IFNULL(PORTCOBEXTRA."FormaCobro",PORTCOBBASE."FormaCobro") 									as "FormaCobro",
				IFNULL(PORTCOBEXTRA."CodigoCobroClasificacion",PORTCOBBASE."CodigoCobroClasificacion") 		as "CodigoCobroClasificacion",
				IFNULL(PORTCOBEXTRA."PermiteCambiarValor",PORTCOBBASE."PermiteCambiarValor") 				as "PermiteCambiarValor"
			FROM 
				(SELECT POR."IdPortafolio",BASE.* 
				FROM "BSCL.Portafolio.BD::POR.Portafolio" as POR
					INNER JOIN :p_Portafolios INPOR ON POR."IdPortafolio" = INPOR."IdPortafolio"
					INNER JOIN 
					(SELECT PCOB.* 
						FROM "BSCL.Plataforma.BD.Vistas::COM.ConsultarDetallePerfilCobros" as PCOB
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
							AND PCOB."EstadoDetalle" = 'ACT'
						) as BASE 
					ON (BASE."IdPerfilCobros" = POR."IdPerfilCobros")	
					) AS PORTCOBBASE
				FULL JOIN
					(SELECT POR."IdPortafolio",EXTRA.*
					FROM "BSCL.Portafolio.BD::POR.Portafolio" as POR
						INNER JOIN :p_Portafolios INPOR ON POR."IdPortafolio" = INPOR."IdPortafolio"
						INNER JOIN 
						(SELECT PCOB.* 
						FROM "BSCL.Plataforma.BD.Vistas::COM.ConsultarDetallePerfilCobros" as PCOB
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
							AND PCOB."EstadoDetalle" = 'ACT'
						) as EXTRA	
					ON(EXTRA."IdPerfilCobros" = POR."IdPerfilCobrosExtra")	
					) AS PORTCOBEXTRA
				ON  
					(PORTCOBEXTRA."IdPortafolio" 				= PORTCOBBASE."IdPortafolio")  
					AND (PORTCOBEXTRA."IdBolsa" 				= PORTCOBBASE."IdBolsa")  
					AND (PORTCOBEXTRA."IdEmpresa"		 		= PORTCOBBASE."IdEmpresa")
					AND (PORTCOBEXTRA."IdTipoProducto" 			= PORTCOBBASE."IdTipoProducto")
					AND (PORTCOBEXTRA."IdTipoOperacion" 		= PORTCOBBASE."IdTipoOperacion")
					AND (PORTCOBEXTRA."IdCanalIngreso" 			= PORTCOBBASE."IdCanalIngreso")
					AND (PORTCOBEXTRA."CodigoCobroClasificacion"= PORTCOBBASE."CodigoCobroClasificacion")	
			) AS COBROSPORTAFOLIO
		ON 
			PRODUCTOSPORTAFOLIO."IdPortafolio" 		 = COBROSPORTAFOLIO."IdPortafolio"
			AND 
				(PRODUCTOSPORTAFOLIO."IdTipoProducto" 	 = COBROSPORTAFOLIO."IdTipoProducto" 
				OR COBROSPORTAFOLIO."IdTipoProducto" 	= '0'
			) 
			AND (PRODUCTOSPORTAFOLIO."IdTipoOperacion" 	 = COBROSPORTAFOLIO."IdTipoOperacion" 
				OR COBROSPORTAFOLIO."IdTipoOperacion" 	 = '0'
				OR PRODUCTOSPORTAFOLIO."IdTipoOperacion" = '0'
			)
			AND (PRODUCTOSPORTAFOLIO."IdCanalIngreso" 	= COBROSPORTAFOLIO."IdCanalIngreso"
				OR COBROSPORTAFOLIO."IdCanalIngreso" 	= '0' 
				OR PRODUCTOSPORTAFOLIO."IdCanalIngreso" = '0'
			)
		) as MATRIZOPERACIONAL
		ON INPORTAFOLIOS."IdPortafolio" = MATRIZOPERACIONAL."IdPortafolio"
		
	WHERE 
		(INPORTAFOLIOS."IdTipoProducto" = MATRIZOPERACIONAL."IdTipoProducto"
			OR MATRIZOPERACIONAL."IdTipoProducto" = '0')
		AND (INPORTAFOLIOS."IdTipoOperacion" = MATRIZOPERACIONAL."IdTipoOperacion"
			OR MATRIZOPERACIONAL."IdTipoOperacion" = '0')
		AND (INPORTAFOLIOS."IdCanalIngreso" = MATRIZOPERACIONAL."IdCanalIngreso"
			OR MATRIZOPERACIONAL."IdCanalIngreso" = '0')
		AND (INPORTAFOLIOS."IdBolsa" = MATRIZOPERACIONAL."IdBolsa")
	;

		
	END;
