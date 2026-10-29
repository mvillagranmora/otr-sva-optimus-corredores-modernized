WITH CTE_PERFILES AS (
		SELECT	
			Portafolio."IdPerfilProductos"				AS "IdPerfilProductos"
			, Portafolio."IdPerfilCobros"				AS "IdPerfilCobros"
			, Portafolio."IdPerfilProductosExtra"		AS "IdPerfilProductosExtra"
			, Portafolio."IdPerfilCobrosExtra"			AS "IdPerfilCobrosExtra"
		FROM "BSCL.Portafolio.BD::POR.Portafolio" Portafolio
		WHERE Portafolio."IdEstado" = 'ACT' AND Portafolio."IdPortafolio" = :IdPortafolio
	),
	CTE_PERFIL_PRODUCTOS AS (
		SELECT 
			ROW_NUMBER() OVER (
					PARTITION BY DetalleProductos."IdTipoProducto", DetalleProductos."IdTipoOperacion", DetalleProductos."IdCanalIngreso"
					ORDER BY DetalleProductos."IdTipoOperacion" ASC
				)														AS "OrdenRegistro"
			   , DetalleProductos."IdTipoProducto"						AS "IdTipoProducto"
			   , DetalleProductos."NombreEstadoProducto"				AS "NombreEstadoProductos"
			   , DetalleProductos."IdTipoOperacion"						AS "IdTipoOperacion"
			   , DetalleProductos."IdCanalIngreso"						AS "IdCanalIngreso"
		FROM CTE_PERFILES CTEPerfiles
			LEFT JOIN "BSCL.Plataforma.BD.Vistas::COM.ConsultarDetallePerfilProductos" DetalleProductos ON (DetalleProductos."IdPerfilProductos" = CTEPerfiles."IdPerfilProductos")
	),
	CTE_PERFIL_PRODUCTOS_EXTRA AS (
		SELECT 
			ROW_NUMBER() OVER (
				PARTITION BY DetalleProductosExtra."IdTipoProducto", DetalleProductosExtra."IdTipoOperacion", DetalleProductosExtra."IdCanalIngreso"
				ORDER BY DetalleProductosExtra."IdTipoOperacion" ASC
			)															AS "OrdenRegistro"
		   , DetalleProductosExtra."IdTipoProducto"						AS "IdTipoProducto"
		   , DetalleProductosExtra."NombreEstadoProducto"				AS "NombreEstadoProductos"
		   , DetalleProductosExtra."IdTipoOperacion"					AS "IdTipoOperacion"
		   , DetalleProductosExtra."IdCanalIngreso"						AS "IdCanalIngreso"
		FROM CTE_PERFILES CTEPerfiles
			LEFT JOIN "BSCL.Plataforma.BD.Vistas::COM.ConsultarDetallePerfilProductos" DetalleProductosExtra ON (DetalleProductosExtra."IdPerfilProductos" = CTEPerfiles."IdPerfilProductosExtra")
	),
	CTE_PERFIL_COBROS AS (
		SELECT 
			ROW_NUMBER() OVER (
				PARTITION BY DetalleCobros."IdTipoProducto", DetalleCobros."CodigoCobroClasificacion", DetalleCobros."IdTipoOperacion", DetalleCobros."IdCanalIngreso" 
				ORDER BY DetalleCobros."IdTipoOperacion" ASC
			)																						AS "OrdenRegistro"
		   , DetalleCobros."IdTipoProducto"															AS "IdTipoProducto"
		   , DetalleCobros."IdTipoOperacion"														AS "IdTipoOperacion"
		   , DetalleCobros."IdCanalIngreso"															AS "IdCanalIngreso"
		   , DetalleCobros."ValorCobro"																AS "ValorCobro"
		   , DetalleCobros."CodigoCobroClasificacion"												AS "CodigoCobroClasificacion"
		   , DetalleCobros."FormaCobro"																AS "FormaCobro"
		FROM CTE_PERFILES CTEPerfiles
			LEFT JOIN "BSCL.Plataforma.BD.Vistas::COM.ConsultarDetallePerfilCobros" DetalleCobros ON (DetalleCobros."IdPerfilCobros" = CTEPerfiles."IdPerfilCobros")
	),
	CTE_PERFIL_COBROS_EXTRA AS (
		SELECT 
			ROW_NUMBER() OVER (
				PARTITION BY DetalleCobrosExtra."IdTipoProducto", DetalleCobrosExtra."CodigoCobroClasificacion", DetalleCobrosExtra."IdTipoOperacion", DetalleCobrosExtra."IdCanalIngreso" 
				ORDER BY DetalleCobrosExtra."IdTipoOperacion" ASC
			)																					AS "OrdenRegistro"
		   , DetalleCobrosExtra."IdTipoProducto"												AS "IdTipoProducto"
		   , DetalleCobrosExtra."IdTipoOperacion"												AS "IdTipoOperacion"
		   , DetalleCobrosExtra."IdCanalIngreso"												AS "IdCanalIngreso"
		   , DetalleCobrosExtra."ValorCobro"													AS "ValorCobro"
		   , DetalleCobrosExtra."CodigoCobroClasificacion"										AS "CodigoCobroClasificacion"
		   , DetalleCobrosExtra."FormaCobro"													AS "FormaCobro" 
		FROM CTE_PERFILES CTEPerfiles
			LEFT JOIN "BSCL.Plataforma.BD.Vistas::COM.ConsultarDetallePerfilCobros" DetalleCobrosExtra ON (DetalleCobrosExtra."IdPerfilCobros" = CTEPerfiles."IdPerfilCobrosExtra")
	)
	SELECT 
		CTEPerfilProductos."IdTipoProducto"																									AS "IdTipoProducto"
		, COALESCE(CTEPerfilProductosExtra."NombreEstadoProductos", CTEPerfilProductos."NombreEstadoProductos", '')							AS "NombreEstadoProductos"
		, COALESCE(CTEPerfilProductosExtra."IdTipoOperacion", CTEPerfilProductos."IdTipoOperacion", '')										AS "IdTipoOperacion"
		, COALESCE(CTEPerfilProductosExtra."IdCanalIngreso", CTEPerfilProductos."IdCanalIngreso", '')										AS "IdCanalIngreso"
		, COALESCE(CTEPerfilCobrosExtraComision."CodigoCobroClasificacion", CTEPerfilCobrosComision."CodigoCobroClasificacion", '')			AS "CobroComisionCodigoCobroClasificacion"
		, COALESCE(CTEPerfilCobrosExtraComision."ValorCobro", CTEPerfilCobrosComision."ValorCobro", 0)										AS "CobroComisionValorCobro"
		, COALESCE(CTEPerfilCobrosExtraComision."FormaCobro", CTEPerfilCobrosComision."FormaCobro", '')										AS "CobroComisionFormaCobro"
		, COALESCE(CTEPerfilCobrosExtraGasto."CodigoCobroClasificacion", CTEPerfilCobrosGasto."CodigoCobroClasificacion", '')				AS "CobroGastoCodigoCobroClasificacion"
		, COALESCE(CTEPerfilCobrosExtraGasto."ValorCobro", CTEPerfilCobrosGasto."ValorCobro", 0)											AS "CobroGastoValorCobro"
		, COALESCE(CTEPerfilCobrosExtraGasto."FormaCobro", CTEPerfilCobrosGasto."FormaCobro", '')											AS "CobroGastoFormaCobro"
		, COALESCE(CTEPerfilCobrosExtraDerecho."CodigoCobroClasificacion", CTEPerfilCobrosDerecho."CodigoCobroClasificacion", '')			AS "CobroDerechoCodigoCobroClasificacion"
		, COALESCE(CTEPerfilCobrosExtraDerecho."ValorCobro", CTEPerfilCobrosDerecho."ValorCobro", 0)										AS "CobroDerechoValorCobro"
		, COALESCE(CTEPerfilCobrosExtraDerecho."FormaCobro", CTEPerfilCobrosDerecho."FormaCobro", '')										AS "CobroDerechoFormaCobro"
	FROM CTE_PERFIL_PRODUCTOS CTEPerfilProductos
		LEFT JOIN CTE_PERFIL_PRODUCTOS_EXTRA CTEPerfilProductosExtra ON (CTEPerfilProductosExtra."IdTipoProducto" = CTEPerfilProductos."IdTipoProducto" AND CTEPerfilProductosExtra."IdTipoOperacion" = CTEPerfilProductos."IdTipoOperacion" AND CTEPerfilProductosExtra."OrdenRegistro" = 1)
		LEFT JOIN CTE_PERFIL_COBROS CTEPerfilCobrosComision ON (CTEPerfilCobrosComision."IdTipoProducto" = CTEPerfilProductos."IdTipoProducto" AND CTEPerfilCobrosComision."IdTipoOperacion" = CTEPerfilProductos."IdTipoOperacion" AND CTEPerfilCobrosComision."OrdenRegistro" = 1 AND CTEPerfilCobrosComision."CodigoCobroClasificacion" = 'COMISION')
		LEFT JOIN CTE_PERFIL_COBROS_EXTRA CTEPerfilCobrosExtraComision ON (CTEPerfilCobrosExtraComision."IdTipoProducto" = CTEPerfilProductos."IdTipoProducto" AND CTEPerfilCobrosExtraComision."IdTipoOperacion" = CTEPerfilProductos."IdTipoOperacion" AND CTEPerfilCobrosExtraComision."OrdenRegistro" = 1 AND CTEPerfilCobrosExtraComision."CodigoCobroClasificacion" = 'COMISION')
		LEFT JOIN CTE_PERFIL_COBROS CTEPerfilCobrosGasto ON (CTEPerfilCobrosGasto."IdTipoProducto" = CTEPerfilProductos."IdTipoProducto" AND CTEPerfilCobrosGasto."IdTipoOperacion" = CTEPerfilProductos."IdTipoOperacion" AND CTEPerfilCobrosGasto."OrdenRegistro" = 1 AND CTEPerfilCobrosGasto."CodigoCobroClasificacion" = 'GASTO')
		LEFT JOIN CTE_PERFIL_COBROS_EXTRA CTEPerfilCobrosExtraGasto ON (CTEPerfilCobrosExtraGasto."IdTipoProducto" = CTEPerfilProductos."IdTipoProducto" AND CTEPerfilCobrosExtraGasto."IdTipoOperacion" = CTEPerfilProductos."IdTipoOperacion" AND CTEPerfilCobrosExtraGasto."OrdenRegistro" = 1 AND CTEPerfilCobrosExtraGasto."CodigoCobroClasificacion" = 'GASTO')
		LEFT JOIN CTE_PERFIL_COBROS CTEPerfilCobrosDerecho ON (CTEPerfilCobrosDerecho."IdTipoProducto" = CTEPerfilProductos."IdTipoProducto" AND CTEPerfilCobrosDerecho."IdTipoOperacion" = CTEPerfilProductos."IdTipoOperacion" AND CTEPerfilCobrosDerecho."OrdenRegistro" = 1 AND CTEPerfilCobrosDerecho."CodigoCobroClasificacion" = 'DERECHO')
		LEFT JOIN CTE_PERFIL_COBROS_EXTRA CTEPerfilCobrosExtraDerecho ON (CTEPerfilCobrosExtraDerecho."IdTipoProducto" = CTEPerfilProductos."IdTipoProducto" AND CTEPerfilCobrosExtraDerecho."IdTipoOperacion" = CTEPerfilProductos."IdTipoOperacion" AND CTEPerfilCobrosExtraDerecho."OrdenRegistro" = 1 AND CTEPerfilCobrosExtraDerecho."CodigoCobroClasificacion" = 'DERECHO')
	WHERE CTEPerfilProductos."OrdenRegistro" = 1;