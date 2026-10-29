SET SCHEMA "BSCL";
GO
DROP PROCEDURE "BSCL.Normativa.BD.Servicios.NOR::LibroVentas";
GO
CREATE PROCEDURE 
--
--  Proposito: Servicio entrega Tabla con Libro de Operaciones .
--  Autor: CESPINOSA - BCS
--  Fecha Creacion: 29/08/2016

"BSCL.Normativa.BD.Servicios.NOR::LibroVentas" (
	--Parametros de Entrada Comunes
	IN p_IdEmpresa		BIGINT,
	--IN p_IdUsuario		BIGINT,
	--IN p_IdModulo		NVARCHAR(3)
	
	--Parametros de Entrada Propios
	IN p_FechaDesde		NVARCHAR(10) DEFAULT NULL,
	IN p_FechaHasta		NVARCHAR(10) DEFAULT NULL,
	IN p_IdCliente		BIGINT DEFAULT NULL,

	IN p_IdBolsa		NVARCHAR(4) DEFAULT NULL,
	IN p_IdClasificacion 	NVARCHAR(3) DEFAULT NULL,
	IN p_IdTipoInstrumento 	NVARCHAR(3) DEFAULT NULL,
	IN p_TipoComprobante	NVARCHAR(3) DEFAULT NULL,
	
	--Salidas
	out p_out_LibroVentas "BSCL.Normativa.BD::NOR.SALIDA_LibroVentas"

)
LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
	
	READS SQL DATA WITH RESULT VIEW "BSCL.Normativa.BD.Vistas::NOR.SALIDA_LibroVentas" AS 

BEGIN
	
	DECLARE v_registroParametro TABLE ("Codigo"  NVARCHAR(40), "Valor" NVARCHAR(100));
	
	v_TablaModulo = Select 'LVE' as "IdModulo" from dummy;
		
	
	v_registroParametro = SELECT 'FECHADESDE' AS "Codigo", p_FechaDesde AS "Valor" FROM DUMMY
				UNION ALL SELECT 'FECHAHASTA' AS "Codigo", p_FechaHasta AS "Valor" FROM DUMMY
				UNION ALL SELECT 'IDCLIENTE' AS "Codigo",  CAST(p_IdCliente AS NVARCHAR(10))  AS "Valor" FROM DUMMY
				UNION ALL SELECT 'IDCLASIFICACION' AS "Codigo", p_IdClasificacion  AS "Valor" FROM DUMMY				
				UNION ALL SELECT 'IDTIPOINSTRUMENTO' AS "Codigo", p_IdTipoInstrumento AS "Valor" FROM DUMMY
				UNION ALL SELECT 'IDTIPOCOMPROBANTE' AS "Codigo", p_TipoComprobante  AS "Valor" FROM DUMMY;
		
	CALL "BSCL.Normativa.BD.Servicios.NOR::ConsultarOperaciones"
	(
		p_IdEmpresa			=> p_IdEmpresa,
		p_IdNormativa		=> 'NCG380Ope',
		p_tablaModulo		=> :v_TablaModulo,
		p_registroParametro => :v_registroParametro,
		p_out_TablaSalida	=> v_SalidaConsultarOperaciones
	);
	
			
	p_out_LibroVentas =
	Select 				---CAMPOS COMUNES
				OPE."FechaMovimiento" AS "FechaMovimiento",
				OPE."FolioTransaccion" AS "FolioTransaccion",
				OPE."IdOrden" AS "SecuenciaOrden",
				OPE."IdOperacion" AS "IdOperacion",
				OPE."IdCliente" AS "IdCliente",
				OPE."DocumentoIdentidad" AS "RutCliente",
				OPE."NombreCompleto" AS "NombreCliente",
				OPE."IdPortafolio" AS "IdPortafolio",
				POR."CodigoPortafolio",
				OPE."IdClasificacionTipoInstrumento",
				OPE."IdTipoInstrumento",
				OPE."IdTipoOperacion",
				COALESCE(ClasInst."Nombre",'') 					AS "ClasificacionTipoInstrumento",
				COALESCE(TipoInstrumento."Descripcion",'') 		AS "TipoInstrumento",
				COALESCE(TipoOperacion."Nombre",'') 			AS "TipoOperacion",
				TipoOperacion."AccionOperacion",
				CASE WHEN TipoOperacion."AccionOperacion"='COMPRA' THEN 'C'
				 	 WHEN TipoOperacion."AccionOperacion"='VENTA'  THEN 'V' ELSE '' END AS "ClaseOperacion",
				OPE."IdTipoProducto",
				OPE."IdEmisor" 			 AS "IdEmisor",
				OPE."Nemotecnico" 		 AS "Nemotecnico",
				OPE."Cantidad" 			 AS "Cantidad",
				OPE."Precio" 			 AS "Precio",
				OPE."Tasa" 				 AS "Tasa",
				OPE."IdMoneda" 			 AS "IdMoneda",
				OPE."IdTipoComprobante"  AS "IdTipoComprobante",
				TPCOM."Nombre" 			 AS "NombreTipoComprobante",
				OPE."FolioComprobante"	 AS "FolioComprobante",				
				(CASE WHEN (OPE."IdTipoComprobante"  IN ('NCE','NCR','NEE','NEO','NCO')) 
				THEN (-1)* OPE."Monto" 
				ELSE			 
				OPE."Monto" 
				END ) AS "MontoOperado",
				(CASE WHEN (OPE."IdTipoComprobante" IN ('NCE','NCR','NEE','NEO','NCO'))
				THEN (-1)* OPE."Comision" 	
				ELSE
				OPE."Comision"		 
				END ) AS "Comision",
				(CASE WHEN (OPE."IdTipoComprobante"  IN ('NCE','NCR','NEE','NEO','NCO')) 
				THEN (-1)* OPE."Derecho" 
				ELSE			 
				OPE."Derecho" 
				END ) AS "Derecho",
				(CASE WHEN (OPE."IdTipoComprobante"  IN ('NCE','NCR','NEE','NEO','NCO')) 
				THEN (-1)* OPE."Gasto" 
				ELSE
				OPE."Gasto"			 
				END ) AS "CargoFijo",
				(CASE WHEN (OPE."IdTipoComprobante" IN ('NCE','NCR','NEE','NEO','NCO')) 
				THEN (-1)* OPE."Impuesto" 	
				ELSE		 
				OPE."Impuesto" 	
				END ) AS "IVA",
				(CASE WHEN (OPE."IdTipoComprobante" IN ('NCE','NCR','NEE','NEO','NCO')) 
				THEN (-1)* (CASE WHEN TPCOM."AfectoImpuesto" = 'S' AND (OPE."IdTipoOperacion" = 'COMOTC' OR OPE."IdTipoOperacion" = 'VENOTC') THEN OPE."Monto" WHEN OPE."Impuesto" >  0 THEN (OPE."Comision" + OPE."Derecho" +  OPE."Gasto") ELSE 0 END )
				ELSE
				(CASE WHEN TPCOM."AfectoImpuesto" = 'S' AND (OPE."IdTipoOperacion" = 'COMOTC' OR OPE."IdTipoOperacion" = 'VENOTC') THEN OPE."Monto" WHEN OPE."Impuesto" >  0 THEN (OPE."Comision" + OPE."Derecho" +  OPE."Gasto") ELSE 0 END )	
				END ) AS "MontoAfecto",
				(CASE WHEN (OPE."IdTipoComprobante" IN ('NCE','NCR','NEE','NEO','NCO')) 
				THEN (-1)* (CASE WHEN OPE."Impuesto" =  0 and TPCOM."AfectoImpuesto" = 'N' THEN (OPE."Monto") ELSE 0 END )	
				ELSE
				(CASE WHEN OPE."Impuesto" =  0 and TPCOM."AfectoImpuesto" = 'N' THEN (OPE."Monto") ELSE 0 END )	
				END ) AS "MontoExento",
				(CASE WHEN (OPE."IdTipoComprobante" IN ('NCE','NCR','NEE','NEO','NCO')) 
				THEN (-1)* (CASE WHEN (OPE."Comision" + OPE."Derecho" +  OPE."Gasto") >  0 AND TPCOM."AfectoImpuesto" = 'S' THEN (OPE."Comision" + OPE."Derecho" +  OPE."Gasto" + OPE."Impuesto")
					  WHEN (OPE."Comision" + OPE."Derecho" +  OPE."Gasto") = 0 AND TPCOM."AfectoImpuesto" = 'S' AND OPE."Impuesto" >  0 THEN ( OPE."Monto" + OPE."Impuesto")
					  WHEN (OPE."Impuesto" =  0 AND TPCOM."AfectoImpuesto" = 'S') THEN OPE."Monto"
				 ELSE OPE."Monto" END ) 
				 ELSE
				 (CASE WHEN (OPE."Comision" + OPE."Derecho" +  OPE."Gasto") >  0 AND TPCOM."AfectoImpuesto" = 'S' THEN (OPE."Comision" + OPE."Derecho" +  OPE."Gasto" + OPE."Impuesto")
					  WHEN (OPE."Comision" + OPE."Derecho" +  OPE."Gasto") = 0 AND TPCOM."AfectoImpuesto" = 'S' AND OPE."Impuesto" >  0 THEN ( OPE."Monto" + OPE."Impuesto")
					  WHEN (OPE."Impuesto" =  0 AND TPCOM."AfectoImpuesto" = 'S') THEN OPE."Monto"
				 ELSE OPE."Monto" END ) 
				 END ) AS "MontoTotal"
				      
	FROM :v_SalidaConsultarOperaciones OPE
	INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS POR ON (OPE."IdPortafolio" = POR."IdPortafolio")
	LEFT JOIN "BSCL.Facturacion.BD.Vistas::ConsultarTipoComprobante" TPCOM ON OPE."IdTipoComprobante" = TPCOM."IdTipoComprobante"
	LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" as TipoOperacion ON OPE."IdTipoOperacion" = TipoOperacion."IdTipoOperacion"
	LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" as TipoInstrumento ON TipoInstrumento."IdTipoInstrumento" = OPE."IdTipoInstrumento" 
	LEFT JOIN "BSCL.Plataforma.BD::INS.ClasificacionTipoInstrumento" as ClasInst ON ClasInst."IdClasificacionTipoInstrumento" = OPE."IdClasificacionTipoInstrumento"
	where (OPE."IdClasificacionTipoInstrumento" = p_IdClasificacion OR p_IdClasificacion IS NULL)
		AND  (OPE."IdTipoInstrumento" = p_IdTipoInstrumento OR p_IdTipoInstrumento IS NULL)
		AND  (OPE."IdTipoComprobante" = p_TipoComprobante OR p_TipoComprobante IS NULL)
		AND  (OPE."IdCliente" = p_IdCliente OR p_IdCliente IS NULL)
		AND OPE."IdTipoComprobante" != 'POL'
		ORDER BY OPE."FechaMovimiento";
	

END;
