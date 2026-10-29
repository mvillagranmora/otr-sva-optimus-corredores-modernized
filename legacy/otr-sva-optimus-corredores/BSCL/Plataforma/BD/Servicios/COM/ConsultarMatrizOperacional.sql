SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Plataforma.BD.Servicios::COM.ConsultarMatrizOperacional";

CREATE PROCEDURE
-- 
--  Proposito: Consulta Matriz Operacional
--  Autor: - JG
--  Fecha Creacion: 26/04/2016
--   
"BSCL.Plataforma.BD.Servicios::COM.ConsultarMatrizOperacional" (

IN p_IdEmpresa				BIGINT,
IN p_IdTipoEntidad			NVARCHAR(40)	DEFAULT 'PORTAFOLIO', 	--- PORTAFOLIO	 / MODELO				/ PREVISUALIZAR
IN p_IdEntidad				NVARCHAR(40)	DEFAULT '',  			--- IdPortafolio / IdPerfilComercial
-- CASO DE PREVISUALIZAR!
IN p_IdPlantillaCobros		NVARCHAR(40)	DEFAULT '',  
IN p_IdPlantillaProductos	NVARCHAR(40)	DEFAULT '',  

OUT p_out_MatrizOperacional table (
	"IdBolsa" 					NVARCHAR(10),
	"IdTipoProducto"			NVARCHAR(4),
	"TipoProducto"				NVARCHAR(100),
	"IdTipoOperacion"			NVARCHAR(6),
	"TipoOperacion"				NVARCHAR(100),
	--"ProductoIdTipoOperacion"		NVARCHAR(6),
	--"CobroIdTipoOperacion"		NVARCHAR(6),
	"IdCanalIngreso"			NVARCHAR(4),
	"CanalIngreso"				NVARCHAR(100),
	--"ProductoIdCanalIngreso"		NVARCHAR(4),
	--"CobroIdCanalIngreso"		NVARCHAR(4),
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
	"IdEmpresa" 				BIGINT
	)
	
)

LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER 
DEFAULT SCHEMA "BSCL"
READS SQL DATA AS


BEGIN
	
	DECLARE v_IdPlantillaCobros			NVARCHAR(40) := '';
	DECLARE v_IdPlantillaCobrosExtra	NVARCHAR(40) := '';
	DECLARE v_IdPlantillaProductos		NVARCHAR(40) := '';
	DECLARE v_IdPlantillaProductosExtra	NVARCHAR(40) := '';
	DECLARE v_CountRegistroPlantilla	BIGINT := 0;
	
	DECLARE v_PlantillaCobrosConsolidada TABLE (
		"IdInternal" BIGINT,
		"Prioridad" BIGINT,
		"IdPerfilCobros" NVARCHAR(40),
		"DescripcionPerfilCobros" NVARCHAR(100),
		"IdEmpresa" BIGINT,
		"EstadoPerfilCobros" NVARCHAR(3),
		"NombreEstadoPerfilCobros" NVARCHAR(100),
		"EsPerfilPersonalizado" VARCHAR(1),
		"IdDetallePerfilCobros" BIGINT,
		"IdBolsa" NVARCHAR(10),
		"IdTipoProducto" NVARCHAR(4),
		"TipoProducto" NVARCHAR(100),
		"IdTipoOperacion" NVARCHAR(6),
		"TipoOperacion" NVARCHAR(100),
		"IdCanalIngreso" VARCHAR(3),
		"CanalIngreso" NVARCHAR(100),
		"DiasPlazo" BIGINT,
		"EstadoDetalle" NVARCHAR(3),
		"NombreEstadoDetalle" NVARCHAR(100),
		"IdValorCobro" NVARCHAR(40),
		"DescripcionValorCobro" VARCHAR(100),
		"IdCobro" NVARCHAR(10),
		"ValorCobro" DECIMAL(26,8),
		"ValorCobroTexto" VARCHAR(5000),
		"ValorTopeMinimoOrden" DECIMAL(26,8),
		"ValorTopeMinimoOrdenTexto" VARCHAR(5000),
		"IdMoneda" VARCHAR(3),
		"EstadoValorCobro" NVARCHAR(3),
		"NombreCobro" NVARCHAR(100),
		"IdCobroClasificacion" BIGINT,
		"FormaCobro" NVARCHAR(1),
		"PermiteCambiarValor" NVARCHAR(1),
		"CodigoCobroClasificacion" NVARCHAR(40),
		"CobroCalculado" INTEGER);
	
	DECLARE v_PlantillaProductosConsolidada TABLE (
		"IdPerfilProductos" NVARCHAR(40),
		"DescripcionPerfilProductos" NVARCHAR(100),
		"IdCategoriaInversionista" NVARCHAR(40),
		"FechaCreacion" DATE,
		"FechaModificacion" DATE,
		"EstadoPerfilProductos" NVARCHAR(3),
		"PorDefecto" NVARCHAR(1),
		"IdEmpresa" BIGINT,
		"EsPerfilPersonalizado" VARCHAR(1),
		"IdTipoProducto" NVARCHAR(4),
		"TipoProducto" NVARCHAR(100),
		"IdTipoOperacion" NVARCHAR(6),
		"TipoOperacion" NVARCHAR(100),
		"IdCanalIngreso" VARCHAR(3),
		"CanalIngreso" NVARCHAR(100),
		"IdMoneda" VARCHAR(3),
		"IdGrupoActivos" NVARCHAR(40),
		"EstadoProducto" NVARCHAR(3),
		"NombreEstadoProducto" NVARCHAR(100));
		
 /*************************** 
   			LOGICA
 ****************************/
		
	-- OBTENCION DE ID DE PLANTILLAS	
	IF p_IdTipoEntidad = 'PORTAFOLIO' THEN
		SELECT COUNT("IdPortafolio") into v_CountRegistroPlantilla FROM "BSCL.Portafolio.BD::POR.Portafolio" WHERE "IdPortafolio" = CAST(p_IdEntidad as BIGINT);
		IF v_CountRegistroPlantilla>0 THEN
			SELECT IFNULL("IdPerfilCobros",''), IFNULL("IdPerfilCobrosExtra",''), IFNULL("IdPerfilProductos",''),IFNULL("IdPerfilProductosExtra",'') 
			INTO v_IdPlantillaCobros, v_IdPlantillaCobrosExtra, v_IdPlantillaProductos, v_IdPlantillaProductosExtra
			FROM "BSCL.Portafolio.BD::POR.Portafolio" WHERE "IdPortafolio" = CAST(p_IdEntidad as BIGINT);
		END IF;
	ELSEIF p_IdTipoEntidad = 'PREVISUALIZAR' THEN
		v_IdPlantillaCobros 	:= p_IdPlantillaCobros;
		v_IdPlantillaProductos 	:= p_IdPlantillaProductos;
	ELSE 
		SELECT COUNT("IdPerfilComercialBase") into v_CountRegistroPlantilla FROM "BSCL.Plataforma.BD::COM.PerfilComercialBase" WHERE "IdPerfilComercialBase" = :p_IdEntidad;
		IF v_CountRegistroPlantilla>0 THEN
			SELECT IFNULL("IdPerfilCobros",''), '', IFNULL("IdPerfilProductos",'') ,''
			INTO v_IdPlantillaCobros, v_IdPlantillaCobrosExtra, v_IdPlantillaProductos, v_IdPlantillaProductosExtra
			FROM "BSCL.Plataforma.BD::COM.PerfilComercialBase" WHERE "IdPerfilComercialBase" = p_IdEntidad;
		END IF;
	END IF;
	
	-- GENERACION DE PLANTILLAS CONSOLIDADAS
	v_PlantillaCobrosConsolidada = 
		SELECT
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
			IFNULL(EXTRA."CobroCalculado",BASE."CobroCalculado") 						as "CobroCalculado"
		FROM 
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
						AND PCOB."IdPerfilCobros" = v_IdPlantillaCobros
						AND PCOB."IdEmpresa" = p_IdEmpresa
						AND PCOB."EstadoPerfilCobros" = 'ACT'
					) as BASE 
				FULL JOIN
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
						AND PCOB."IdPerfilCobros" = v_IdPlantillaCobrosExtra
						AND PCOB."IdEmpresa" = p_IdEmpresa
						AND PCOB."EstadoPerfilCobros" = 'ACT'
					) as EXTRA	
			ON  
				EXTRA."IdBolsa" = BASE."IdBolsa"  
				AND (EXTRA."IdTipoProducto" 			= BASE."IdTipoProducto")
				AND (EXTRA."IdTipoOperacion" 			= BASE."IdTipoOperacion")
				AND (EXTRA."IdCanalIngreso" 			= BASE."IdCanalIngreso")
				AND (EXTRA."CodigoCobroClasificacion" 	= BASE."CodigoCobroClasificacion")
		WHERE 
			IFNULL(EXTRA."EstadoDetalle",BASE."EstadoDetalle") = 'ACT'
		;
	
	v_PlantillaProductosConsolidada = 
		SELECT 
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
			IFNULL(EXTRA."NombreEstadoProducto",BASE."NombreEstadoProducto") as "NombreEstadoProducto"
		FROM
			(SELECT * FROM "BSCL.Plataforma.BD.Vistas::COM.ConsultarDetallePerfilProductos"
			WHERE "IdPerfilProductos" = v_IdPlantillaProductos AND "IdEmpresa" = p_IdEmpresa) AS BASE
			FULL JOIN 
				(SELECT * FROM "BSCL.Plataforma.BD.Vistas::COM.ConsultarDetallePerfilProductos"
				WHERE "IdPerfilProductos" = v_IdPlantillaProductosExtra AND "IdEmpresa" = p_IdEmpresa) AS EXTRA
			ON   	
				(BASE."IdTipoProducto" = EXTRA."IdTipoProducto")
				AND (BASE."IdTipoOperacion" = EXTRA."IdTipoOperacion")
				AND (BASE."IdCanalIngreso" = EXTRA."IdCanalIngreso")
		;
		
		-- INTERSECCION DE MATRICES CONSOLIDADAS.
		p_out_MatrizOperacional = 
			SELECT DISTINCT
				COBROS."IdBolsa",
				PRODUCTOS."IdTipoProducto",
				PRODUCTOS."TipoProducto",
				CASE WHEN PRODUCTOS."IdTipoOperacion"=COBROS."IdTipoOperacion" THEN PRODUCTOS."IdTipoOperacion"
					WHEN PRODUCTOS."IdTipoOperacion" = '0' THEN COBROS."IdTipoOperacion"
					ELSE PRODUCTOS."IdTipoOperacion" END AS "IdTipoOperacion",
				CASE WHEN PRODUCTOS."IdTipoOperacion"=COBROS."IdTipoOperacion" THEN PRODUCTOS."TipoOperacion"
					WHEN PRODUCTOS."IdTipoOperacion" = '0' THEN COBROS."TipoOperacion"
					ELSE PRODUCTOS."TipoOperacion" END AS "TipoOperacion",
				--PRODUCTOS."IdTipoOperacion"AS "ProductoIdTipoOperacion",
				--COBROS."IdTipoOperacion" AS "CobroIdTipoOperacion",
				CASE WHEN PRODUCTOS."IdCanalIngreso"=COBROS."IdCanalIngreso" THEN PRODUCTOS."IdCanalIngreso"
					WHEN PRODUCTOS."IdCanalIngreso" = '0' THEN COBROS."IdCanalIngreso"
					ELSE PRODUCTOS."IdCanalIngreso" END AS "IdCanalIngreso",
				CASE WHEN PRODUCTOS."IdCanalIngreso"=COBROS."IdCanalIngreso" THEN PRODUCTOS."CanalIngreso"
					WHEN PRODUCTOS."IdCanalIngreso" = '0' THEN COBROS."CanalIngreso"
					ELSE PRODUCTOS."CanalIngreso" END AS "CanalIngreso",
				--PRODUCTOS."IdCanalIngreso" AS "ProductoIdCanalIngreso",
				--COBROS."IdCanalIngreso" AS "CobroIdCanalIngreso",
				COBROS."IdCobro",
				COBROS."ValorCobro",-- ej:si es variable 19 es 19% , si es fijo 19 es 19 pesos/uf/etc...
				COBROS."FormaCobro",
				COBROS."ValorTopeMinimoOrden",
				COBROS."IdMoneda",
				COBROS."Prioridad",
				COBROS."DiasPlazo",
				COBROS."NombreCobro",
				COBROS."IdCobroClasificacion",
				COBROS."CodigoCobroClasificacion",
				PRODUCTOS."IdEmpresa"
			FROM :v_PlantillaCobrosConsolidada COBROS
			INNER JOIN :v_PlantillaProductosConsolidada PRODUCTOS
				ON 	COBROS."IdTipoProducto" = PRODUCTOS."IdTipoProducto"
					AND (COBROS."IdTipoOperacion" = PRODUCTOS."IdTipoOperacion" 
						OR (COBROS."IdTipoOperacion" = '0' AND IFNULL(PRODUCTOS."IdTipoOperacion",'') != '') 
						OR (PRODUCTOS."IdTipoOperacion" = '0' AND IFNULL(COBROS."IdTipoOperacion",'') != '') 
					)
					AND (COBROS."IdCanalIngreso" = PRODUCTOS."IdCanalIngreso"
						OR (COBROS."IdCanalIngreso" = '0' AND IFNULL(PRODUCTOS."IdCanalIngreso",'') != '') 
						OR (PRODUCTOS."IdCanalIngreso" = '0' AND IFNULL(COBROS."IdCanalIngreso",'') != '') 
					);


END;
