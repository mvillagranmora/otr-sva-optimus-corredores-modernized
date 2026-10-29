SET SCHEMA "BSCL";
GO
DROP PROCEDURE "BSCL.Normativa.BD.Servicios.NOR::ConsultarOperaciones";
GO
CREATE PROCEDURE 
--
--  Proposito: Servicio que permite obtener las operaciones desde los distintos modulos en el sistema.
--  Autor: CFLORES - BCS
--  Fecha Creacion: 14/09/2016

"BSCL.Normativa.BD.Servicios.NOR::ConsultarOperaciones" ( 
    --VARIABLES DEL PROCESO
	IN p_IdEmpresa					  BIGINT,
 	IN p_IdNormativa 		  		  NVARCHAR(15),
 	IN p_IdTipoDocumento	  		  NVARCHAR(10) DEFAULT '',
	IN p_tablaModulo     		  	  TABLE("IdModulo" nvarchar(3)),
	IN p_registroParametro		  	  TABLE("Codigo" 					NVARCHAR(40), 
											"Valor" 					NVARCHAR(100))-- parametro estandar
	,IN p_nominal NVARCHAR(1) DEFAULT 'N'
	,IN p_paramAnulacionNC			  NVARCHAR(2) DEFAULT ''
    --VARIABLES DE SALIDA 
	,OUT p_out_TablaSalida	"BSCL.Normativa.BD::NOR.SALIDA_OPERACIONBASE" --Se debe llamar siempre p_out_TablaSalida
)
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
    READS SQL DATA WITH RESULT VIEW "BSCL.Normativa.BD.Vistas::NOR.SALIDA_OPERACIONBASE" AS
BEGIN
		
	DECLARE v_count,v_countModulo INT:=0;
	DECLARE v_CUS,v_MRF,v_MSP,v_MFW,v_FAC,v_FMM,v_LVE, v_PRE, v_EVC NVARCHAR(1):='';
	DECLARE v_FECHA,v_FECHADESDE,v_FECHAHASTA,v_CLASEFECHA NVARCHAR(50):='';
	DECLARE v_IDCLIENTE BIGINT := 0;
	DECLARE v_IDCLASIFICACION   NVARCHAR(3);
	DECLARE v_IDTIPOINSTRUMENTO NVARCHAR(3);
	DECLARE v_IDTIPOCOMPROBANTE NVARCHAR(3);
	DECLARE v_tableTemp TABLE("IdComprobante" BIGINT, "IdComprobanteReferencia" BIGINT, "IdComprobanteOperacion" BIGINT) DEFAULT SELECT 0 AS "IdComprobante",0 as "IdComprobanteReferencia",0 as "IdComprobanteOperacion" FROM DUMMY WHERE 1=2;
	DECLARE v_fechaHoraSistema 							 NVARCHAR(40):='';
	DECLARE v_IdNormativa NVARCHAR(15):=p_IdNormativa;
	
	IF :p_IdNormativa = 'NCG380Operc' THEN
    	p_IdNormativa := 'NCG380Ope';
	END IF;
	v_CLASEFECHA:= 'MOVIMIENTO';
	call "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema"(
		p_IdEmpresa => p_IdEmpresa,
		p_out_FechaHoraSistema => v_fechaHoraSistema
	);
		
	--Se obtienen operaciones segun Modulo
	SELECT  COUNT(1) INTO v_countModulo FROM :p_tablaModulo;			
	IF v_countModulo>0 THEN
			p_tablaModuloGrupo =
					SELECT 			 MAX( CASE WHEN SOL."IdModulo"='CUS' THEN 'S' ELSE NULL END) AS "CUS" --CUSTODIA
									,MAX( CASE WHEN SOL."IdModulo"='MRF' THEN 'S' ELSE NULL END) AS "MRF" --MESA RF
									,MAX( CASE WHEN SOL."IdModulo"='MSP' THEN 'S' ELSE NULL END) AS "MSP" --MESA SPOT
									,MAX( CASE WHEN SOL."IdModulo"='MFW' THEN 'S' ELSE NULL END) AS "MFW" --MESA FORWARD
									,MAX( CASE WHEN SOL."IdModulo"='FAC' THEN 'S' ELSE NULL END) AS "FAC" --FACTURACION
									,MAX( CASE WHEN SOL."IdModulo"='FMM' THEN 'S' ELSE NULL END) AS "FMM" --FONDOS MUTUOS
									,MAX( CASE WHEN SOL."IdModulo"='LVE' THEN 'S' ELSE NULL END) AS "LVE" --LIBRO VENTAS
									,MAX( CASE WHEN SOL."IdModulo"='PRE' THEN 'S' ELSE NULL END) AS "PRE" --PRESTAMOS
									,MAX( CASE WHEN SOL."IdModulo"='EVC' THEN 'S' ELSE NULL END) AS "EVC"  --EEventos de Capital																											
					FROM :p_tablaModulo AS SOL;
					
			SELECT TOP 1 "CUS","MRF","MSP","MFW","FAC","FMM","LVE", "PRE", "EVC"
				   INTO v_CUS,v_MRF,v_MSP,v_MFW,v_FAC,v_FMM,v_LVE, v_PRE, v_EVC FROM :p_tablaModuloGrupo;
					
	END IF;
		
	--Parametros
	SELECT  COUNT(1) INTO v_count FROM :p_registroParametro;			
	IF v_count>0 THEN
		p_tablaParametrosGrupo =
				SELECT 			 MAX( CASE WHEN SOL."Codigo"='FECHA' 	  		  THEN SOL."Valor" ELSE NULL END) AS "FECHA"
								,MAX( CASE WHEN SOL."Codigo"='FECHADESDE' 		  THEN SOL."Valor" ELSE NULL END) AS "FECHADESDE" 
								,MAX( CASE WHEN SOL."Codigo"='FECHAHASTA' 		  THEN SOL."Valor" ELSE NULL END) AS "FECHAHASTA" 
								,MAX( CASE WHEN SOL."Codigo"='CLASEFECHA' 		  THEN SOL."Valor" ELSE NULL END) AS "CLASEFECHA" --TRANSACCION / MOVIMIENTO (DEFAULT)
								,MAX( CASE WHEN SOL."Codigo"='IDCLIENTE' 		  THEN CAST(SOL."Valor" AS BIGINT) ELSE NULL END) AS "IDCLIENTE"
								,MAX( CASE WHEN SOL."Codigo"='IDCLASIFICACION' 	  THEN SOL."Valor" ELSE NULL END) AS "IDCLASIFICACION"
								,MAX( CASE WHEN SOL."Codigo"='IDTIPOINSTRUMENTO'   THEN SOL."Valor" ELSE NULL END) AS "IDTIPOINSTRUMENTO"
								,MAX( CASE WHEN SOL."Codigo"='IDTIPOCOMPROBANTE'   THEN SOL."Valor" ELSE NULL END) AS "IDTIPOCOMPROBANTE"
				FROM :p_registroParametro AS SOL;
					

		SELECT TOP 1 "FECHA","FECHADESDE","FECHAHASTA",COALESCE("CLASEFECHA",'MOVIMIENTO') AS "CLASEFECHA", "IDCLIENTE","IDCLASIFICACION","IDTIPOINSTRUMENTO","IDTIPOCOMPROBANTE"
			INTO v_FECHA,v_FECHADESDE,v_FECHAHASTA,v_CLASEFECHA,v_IDCLIENTE,v_IDCLASIFICACION, v_IDTIPOINSTRUMENTO, v_IDTIPOCOMPROBANTE  FROM :p_tablaParametrosGrupo;

		
					
		--Valor de FechA
		SELECT COUNT(1) INTO v_count FROM :p_registroParametro WHERE "Codigo" LIKE '%FECHA%';
							
		IF v_count>0 THEN
			v_tablaFecha = SELECT (CASE  WHEN :v_FECHA IS NULL THEN :v_FECHADESDE ELSE  TO_VARCHAR (:v_FECHA, 'YYYY-MM-DD') END)  AS "FECHADESDE",
								  (CASE  WHEN :v_FECHA IS NULL THEN :v_FECHAHASTA ELSE  TO_VARCHAR (:v_FECHA, 'YYYY-MM-DD') END)  AS "FECHAHASTA",
								  COALESCE(:v_CLASEFECHA,'MOVIMIENTO')  AS "CLASEFECHA"
							  FROM DUMMY;
		 ELSE
		  v_tablaFecha = SELECT NULL AS "FECHADESDE", TO_VARCHAR (v_fechaHoraSistema, 'YYYY-MM-DD') AS "FECHAHASTA",:v_CLASEFECHA  AS "CLASEFECHA"  FROM DUMMY;				  
		END IF;		
	ELSE
		v_tablaFecha = SELECT NULL AS "FECHADESDE", TO_VARCHAR (v_fechaHoraSistema, 'YYYY-MM-DD') AS "FECHAHASTA",:v_CLASEFECHA  AS "CLASEFECHA"	  FROM DUMMY;						
	END IF;
		
	--Producto Notmativo
	v_tablaProductos =SELECT
			'' AS "IdTipoProducto",
			'' AS "IdClasificacionTipoInstrumento",
			'' AS "IdTipoInstrumento", 
			'' AS "IdTipoOperacion",
			'' AS "IdNormativa",
			'' AS "IdTipoDocumento",
			'' AS "Uso",
			'' AS "HIdTipoProducto",
			'' AS "HIdClasificacionTipoInstrumento",
			'' AS "HIdTipoInstrumento",
			'' AS "HIdTipoOperacion",
			'' AS "IncluirVentaCorta"
			FROM DUMMY WHERE 1=2;	
		
	--Se obtienen operaciones segun Productos configurados para la Normativa
	IF p_IdNormativa<>'' THEN 
		v_tablaProductos=
			SELECT   NPRO."IdTipoProducto"
					,NPRO."IdClasificacionTipoInstrumento"
					,NPRO."IdTipoInstrumento"
					,NPRO."IdTipoOperacion" 
					,NPRO."IdNormativa"
					,NPRO."IdTipoDocumento"
					,NPRO."Uso"
					,CASE NPRO."HIdTipoProducto" 				 WHEN '' THEN NPRO."IdTipoProducto" 				ELSE NPRO."HIdTipoProducto" 				END AS "HIdTipoProducto"
					,CASE NPRO."HIdClasificacionTipoInstrumento" WHEN '' THEN NPRO."IdClasificacionTipoInstrumento" ELSE NPRO."HIdClasificacionTipoInstrumento" END AS "HIdClasificacionTipoInstrumento"
					,CASE NPRO."HIdTipoInstrumento" 			 WHEN '' THEN NPRO."IdTipoInstrumento" 				ELSE NPRO."HIdTipoInstrumento" 				END AS "HIdTipoInstrumento"
					,CASE NPRO."HIdTipoOperacion" 			 	 WHEN '' THEN NPRO."IdTipoOperacion" 				ELSE NPRO."HIdTipoOperacion" 				END AS "HIdTipoOperacion"
					,NPRO."IncluirVentaCorta"					
			FROM  "BSCL.Normativa.BD::NOR.Producto" NPRO 	--PRODUCTO NORMATIVA
			WHERE NPRO."IdNormativa"=:p_IdNormativa
					AND (NPRO."IdTipoDocumento" =:p_IdTipoDocumento OR p_IdTipoDocumento='');
	ELSE 
		IF v_countModulo>0 THEN
			v_tablaProductos=
				SELECT   NPRO."IdTipoProducto"
						,COALESCE(NPRO."IdClasificacionTipoInstrumento",'') AS "IdClasificacionTipoInstrumento"
						,COALESCE(NPRO."IdTipoInstrumento",'') AS "IdTipoInstrumento"
						,COALESCE(NPRO."IdTipoOperacion",'')  AS "IdTipoOperacion"
						,'' AS "IdNormativa"
						,'' AS "IdTipoDocumento"
						,'' AS "Uso"
						, NPRO."IdTipoProducto"									AS "HIdTipoProducto"
						, COALESCE(NPRO."IdClasificacionTipoInstrumento",'')	AS "HIdClasificacionTipoInstrumento"
						, COALESCE(NPRO."IdTipoInstrumento",'') 				AS "HIdTipoInstrumento"
						, COALESCE(NPRO."IdTipoOperacion",'') 					AS "HIdTipoOperacion"
						,'N' AS "IncluirVentaCorta"					
				FROM  "BSCL.Plataforma.BD.Vistas::INS.ConsultarTipoOperacion" NPRO 	--PRODUCTOS CONFIGURADOS
				WHERE "EstadoTipoOperacionTipoInstrumento"='ACT';
		END IF;		
	END IF;			
		
	--LIBRO DE VENTAS UI
	IF v_LVE = 'S' THEN
		p_out_TablaSalida=
		SELECT 
			---CAMPOS COMUNES
			''												 			AS "FechaCreacion",
			COALESCE(TO_VARCHAR( OPE."FechaComprobante", 'YYYY-MM-DD') || ' ' || TO_VARCHAR (OPE."FechaIngresoRegistro", 'HH24:MI:SS'), '1900-01-01 00:00:00') AS "FechaMovimiento",
			''														 	AS "FolioTransaccion",
			'' 															AS "IdOrden",
			''													 		AS "IdComprobante",
			0							 								AS "IdOperacion",
			COALESCE(OPE."IdTipoOperacion",'') 							AS "IdTipoOperacion",
			COALESCE(OPE."IdCliente",0) 							 	AS "IdCliente",
			COALESCE(OPE."IdPortafolio",0) 								AS "IdPortafolio",
			COALESCE(OPE."IdClasificacionTipoInstrumento",'') 			AS "IdClasificacionTipoInstrumento",
			''															AS "IdEmisor",
			''							 								AS "Nemotecnico",
			0															AS "Cantidad",
			0															AS "Precio",
			COALESCE(OPE."MontoComprobante",0)  			       		AS "Monto",
			0															AS "Tasa",
			''							 								AS "IdMoneda",
			0															AS "IdInstrumento",	
			COALESCE(OPE."IdTipoInstrumento",'') 						AS "IdTipoInstrumento", 
			COALESCE(OPE."IdTipoProducto",'') 							AS "IdTipoProducto",
			COALESCE(OPE."AccionTipoOperacion",'')						AS "AccionOperacion",
			0															AS "MontoCompra",
			0 															AS "MontoVenta",
			0															AS "TasaCompra",
			0 															AS "TasaVenta",
			0 															AS "TotalCompra",
			0															AS "TotalVenta",
			0															AS "IdTransaccion",
			'0001-01-01 00:00:00'								 		AS "FechaTransaccion",
			''															AS "RelacionInstrumento",
			0															AS "IdContraparte",
			0													        AS "IdOperadorContraparte",
			0														    AS "IdOperadorCorredor",
			COALESCE(OPE."IdTipoComprobante",'') 						AS "IdTipoComprobante",
			COALESCE(OPE."NombreTipoComprobante",'') 					AS "TipoComprobante",
			COALESCE(OPE."FolioComprobante",0) 							AS "FolioComprobante",
			0															AS "ValorPresenteCompraInst",
			'0001-01-01 00:00:00'								 		AS "FechaVencimiento",
			COALESCE(OPE."TotalComis",0)							 	AS "Comision",
			COALESCE(OPE."TotalDere",0) 								AS "Derecho",
			COALESCE(OPE."TotalGasto",0) 								AS "Gasto",
			COALESCE(OPE."TotalImpto",0) 								AS "Impuesto",
		
			OPE."IdCondicionLiquidacion"								AS "IdCondicionLiquidacion",
			0															AS "IdDocumentoRelacion",
			'0001-01-01'											    AS "FechaVencimientoPacto",
			0															AS "PrecioPlazo",
			0														    AS "TasaPlazo",
			''															AS "IdMonedaPlazo",
			0														    AS "TotalCompraVencimiento",
			0															AS "TotalVentaVencimiento",
			'' 															AS "Custodia",
			''															AS "IdBolsa",
			''															AS "IdBoveda",
			0															AS "CantidadGarantia",
			''															AS "NemoSVS",
			0															AS "ValorPresente",
			0															AS "UtilidadPerdida",
			'0001-01-01 00:00:00'										AS "FechaOrigenPacto",
			''															AS "DescripcionOperacion",
			''															AS "IdEstadoFactura",
			''															AS "Canal",
			0														    AS "ValorIVA",
			0															AS "FolioComprobanteAsociado",
			''														    AS "ModalidadEjecucion",
			'' 														 	AS "EsFungible",
			0															AS "IdMovimientoRelacionado",
			''															AS "CodigoOperadorContraparte",
			''															AS "CodigoOperadorCorredor",
			'' 															AS "TipoOperacionSecundaria",
			0 															AS "ValorRescateInicial",
			'' 															AS "IdMonedaSecundaria",
			0															AS "IdNormativa",
			''															AS "IdTipoDocumento",
			''															AS "HIdTipoProducto",
			''															AS "HIdClasificacionTipoInstrumento",
			''															AS "HIdTipoInstrumento",
			''															AS "HIdTipoOperacion",
			''															AS "Uso",
			''															AS "EsEnRueda",
			''															AS "EsVentaCorta",
			0														    AS "ParidadCierre",
			0														    AS "PlazoVencimiento",
			OPE."DocumentoIdentidad"									AS "DocumentoIdentidad",	
			OPE."NombreLargoCliente"									AS "NombreCompleto",
			'LVE'														AS "IdModulo",
			''                                                          AS "FechaIngresoRegistro",
			0															AS "IdOperacionOrigenReasignacion",
			''									 						AS "IdSistemaBursatil",
			0 															AS "INGCantidad",
			''															AS "InstrumentoNegociacion",
			0													 		AS "IdComprobanteReferencia",
			0															AS "IdComprobanteOperacion",
			'' 															AS "EsVencimientoPacto",
			0															AS "SecuenciaMovimiento",
			'0001-01-01'												AS "FechaLiquidacion"
		FROM	"BSCL.Facturacion.BD.Vistas::ConsultarComprobante" OPE
		INNER JOIN :v_tablaFecha PFECH	
		
		ON ( 	(PFECH."FECHADESDE" IS NOT NULL AND PFECH."CLASEFECHA" ='MOVIMIENTO' AND (TO_DATE(OPE."FechaComprobante") BETWEEN TO_DATE(PFECH."FECHADESDE") AND TO_DATE(PFECH."FECHAHASTA")))		 
			 OR  											
				(PFECH."FECHADESDE" IS NULL 	AND	PFECH."CLASEFECHA" ='MOVIMIENTO' AND TO_DATE(OPE."FechaComprobante") <= TO_DATE(PFECH."FECHAHASTA"))
			)  		 
		WHERE (OPE."IdCliente" = :v_IDCLIENTE OR :v_IDCLIENTE IS NULL)
		AND   (OPE."IdClasificacionTipoInstrumento" = :v_IDCLASIFICACION OR :v_IDCLASIFICACION IS NULL)
		AND  (OPE."IdTipoInstrumento" = :v_IDTIPOINSTRUMENTO OR :v_IDTIPOINSTRUMENTO IS NULL)
		AND  (OPE."IdTipoComprobante" = :v_IDTIPOCOMPROBANTE OR :v_IDTIPOCOMPROBANTE IS NULL)
		AND  OPE."IdEstadoComprobante" <> 'ANU'
		
		UNION ALL SELECT * FROM :p_out_TablaSalida;	
	END IF; --LIBRO DE VENTAS UI
		
	--FACTURACION
	IF v_FAC ='S' OR (p_IdNormativa<>'' and v_FAC ='') THEN
		--Tabla de Salida
		p_out_TablaSalidaTemp=
		SELECT 
			---CAMPOS COMUNES
			COALESCE(OPE."FechaIngresoRegistro",'')			 			AS "FechaCreacion",
			CASE 
				WHEN v_IdNormativa = 'NCG380Operc' AND CARSIM."IdTipoInstrumento" = 'SIM' AND CARSIM."IdEstado" IN ('VCD', 'ANT') THEN COALESCE(TO_VARCHAR( CARSIM."EGRFechaComprobante", 'YYYY-MM-DD') || ' ' || TO_VARCHAR (OPE."FechaIngresoRegistro", 'HH24:MI:SS'), '1900-01-01 00:00:00')
				WHEN v_IdNormativa = 'NCG380Operc' AND (CARSIM."IdTipoInstrumento" != 'SIM' OR CARSIM."IdTipoInstrumento" IS NULL) THEN 
					CASE 
						WHEN OPE."TipoFechaTransaccion" = 'T' THEN TO_VARCHAR(UTCTOLOCAL(OPE."FechaTransaccion", 'America/Santiago','platform'), 'YYYY-MM-DD') || ' ' || TO_VARCHAR (OPE."FechaTransaccion", 'HH24:MI:SS')
						ELSE COALESCE(TO_VARCHAR( OPE."FechaMovimiento", 'YYYY-MM-DD') || ' ' || TO_VARCHAR (OPE."FechaIngresoRegistro", 'HH24:MI:SS'), '1900-01-01 00:00:00')
					END
				WHEN v_IdNormativa = 'NCG380Operc' AND OPE."TipoFechaTransaccion" = 'T' THEN TO_VARCHAR( UTCTOLOCAL(OPE."FechaTransaccion", 'America/Santiago','platform'), 'YYYY-MM-DD') || ' ' || TO_VARCHAR (OPE."FechaTransaccion", 'HH24:MI:SS')
				ELSE COALESCE(TO_VARCHAR( OPE."FechaTransaccion", 'YYYY-MM-DD') || ' ' || TO_VARCHAR (OPE."FechaIngresoRegistro", 'HH24:MI:SS'), '1900-01-01 00:00:00') 
			END AS "FechaMovimiento",
			COALESCE(CAST(OPE."FolioTransaccion"AS  NVARCHAR(40)),'') 	AS "FolioTransaccion",
			COALESCE(CAST(OPE."IdOrden"AS  NVARCHAR(40)),'') 			AS "IdOrden",
			COALESCE(CAST(OPE."IdComprobante"AS  NVARCHAR(40)),'0') 		AS "IdComprobante",
			COALESCE(OPE."IdOperacion",0) 								AS "IdOperacion",
			CASE 
				WHEN v_IdNormativa = 'NCG380Operc' AND CARSIM."IdTipoInstrumento" = 'SIM' AND CARSIM."IdEstado" IN ('VCD') AND OPE."AccionOperacion" = 'VENTA' THEN 'VESIMV'
				WHEN v_IdNormativa = 'NCG380Operc' AND CARSIM."IdTipoInstrumento" = 'SIM' AND CARSIM."IdEstado" IN ('VCD') AND OPE."AccionOperacion" = 'COMPRA' THEN 'VESIMC'
				WHEN v_IdNormativa = 'NCG380Operc' AND CARSIM."IdTipoInstrumento" = 'SIM' AND CARSIM."IdEstado" IN ('ANT') AND OPE."AccionOperacion" = 'COMPRA' THEN 'ANSIMC'
				WHEN v_IdNormativa = 'NCG380Operc' AND CARSIM."IdTipoInstrumento" = 'SIM' AND CARSIM."IdEstado" IN ('ANT') AND OPE."AccionOperacion" = 'VENTA' THEN 'ANSIMV'
				ELSE COALESCE(OPE."IdTipoOperacion",'') 							
			END AS "IdTipoOperacion",								
			COALESCE(OPE."IdCliente",0) 								AS "IdCliente",
			COALESCE(OPE."IdPortafolio",0) 								AS "IdPortafolio",
			COALESCE(OPE."IdClasificacionTipoInstrumento",'') 			AS "IdClasificacionTipoInstrumento",
			--COALESCE(OPE."Mercado",'') AS "Mercado",
			COALESCE(OPE."IdEmisor",'') 								AS "IdEmisor",
			COALESCE(OPE."Nemotecnico",'') 								AS "Nemotecnico",
			COALESCE(OPE."Cantidad",0) 									AS "Cantidad",
			COALESCE(OPE."Precio",0) 									AS "Precio",
			COALESCE(OPE."MontoMovimientoComprobante",0)         		AS "Monto",
			COALESCE(OPE."Tasa",0) 										AS "Tasa",
			COALESCE(OPE."IdMoneda",'') 								AS "IdMoneda",
			COALESCE(OPE."IdInstrumento",0)								AS "IdInstrumento",	
			COALESCE(OPE."IdTipoInstrumento",'') 						AS "IdTipoInstrumento", 
			COALESCE(OPE."IdTipoProducto",'') 							AS "IdTipoProducto",			
			
			--(Compra/Venta)
			COALESCE(OPE."AccionOperacion",'')							AS "AccionOperacion",
			COALESCE(OPE."MontoCompra",0)								AS "MontoCompra",
			COALESCE(OPE."MontoVenta",0) 								AS "MontoVenta",
			COALESCE(OPE."TasaCompra",0) 								AS "TasaCompra",
			COALESCE(OPE."TasaVenta",0) 								AS "TasaVenta",
			COALESCE(OPE."TotalCompra",0) 								AS "TotalCompra",
			COALESCE(OPE."TotalVenta",0)								AS "TotalVenta",
			
			---ESPECIFICOS: FACTURACION
			COALESCE(OPE."IdTransaccion",0) 							AS "IdTransaccion",
			COALESCE(OPE."FechaTransaccion",'0001-01-01 00:00:00') 		AS "FechaTransaccion",
			COALESCE(OPE."RelacionInstrumento",'') AS "RelacionInstrumento",
			COALESCE(OPE."IdContraparte", 0) AS "IdContraparte",
			COALESCE(OPE."IdOperadorContraparte",0) AS "IdOperadorContraparte",
			COALESCE(OPE."IdOperadorCorredor",0) AS "IdOperadorCorredor",
			COALESCE(OPE."IdTipoComprobante",'') AS "IdTipoComprobante",
			COALESCE(tipocomprobante."Nombre",'') AS "TipoComprobante",
			COALESCE(OPE."FolioComprobante",0) AS "FolioComprobante",
			COALESCE(OPE."ValorPresenteCompraInst",0) AS "ValorPresenteCompraInst",
			COALESCE(OPE."FechaVencimiento",'0001-01-01 00:00:00') AS "FechaVencimiento",

			COALESCE(OPE."Comision",0)					 	AS "Comision",
			COALESCE(OPE."Derecho",0) 						AS "Derecho",
			COALESCE(OPE."Gasto",0) 						AS "Gasto",
			COALESCE(OPE."Impuesto",0) 						AS "Impuesto",

			COALESCE(OPE."IdCondicionLiquidacion",'') AS "IdCondicionLiquidacion",
			COALESCE(OPE."IdDocumentoRelacion",0) AS "IdDocumentoRelacion",
			COALESCE(OPE."FechaVencimientoPacto",'0001-01-01') AS "FechaVencimientoPacto",
			COALESCE(OPE."PrecioPlazo",0) AS "PrecioPlazo",
			COALESCE(OPE."TasaPlazo",0) AS "TasaPlazo",
			COALESCE(OPE."IdMonedaPlazo",'') AS "IdMonedaPlazo",
			COALESCE(OPE."TotalCompraVencimiento",0) AS "TotalCompraVencimiento",
			COALESCE(OPE."TotalVentaVencimiento",0) AS "TotalVentaVencimiento",
			COALESCE(OPE."Custodia",'') AS "Custodia",
			COALESCE(OPE."IdBolsa",'') AS "IdBolsa",
			COALESCE(OPE."IdBoveda",'') AS "IdBoveda",
			COALESCE(OPE."CantidadGarantia",0) AS "CantidadGarantia",
			COALESCE(OPE."NemoSVS",'') AS "NemoSVS",
			COALESCE(OPE."ValorPresente",0) AS "ValorPresente",
			COALESCE(OPE."UtilidadPerdida",0) AS "UtilidadPerdida",
			COALESCE(OPE."FechaOrigenPacto",'0001-01-01 00:00:00') AS "FechaOrigenPacto",
			COALESCE(CAST(OPE."DescripcionOperacion"AS  NVARCHAR(40)),'') AS "DescripcionOperacion",
			COALESCE(OPE."IdEstadoFactura",'') AS "IdEstadoFactura",
			COALESCE(OPE."Canal",'') AS "Canal",
			COALESCE(OPE."ValorIVA",0) AS "ValorIVA",
			COALESCE(OPE."FolioComprobanteAsociado",0) AS "FolioComprobanteAsociado",
			COALESCE(OPE."ModalidadEjecucion", '') AS "ModalidadEjecucion",
			COALESCE(OPE."EsFungible", '') AS "EsFungible",
			COALESCE(OPE."IdMovimientoRelacionado", 0) AS "IdMovimientoRelacionado",
			OPE."CodigoOperadorContraparte" AS "CodigoOperadorContraparte",
			OPE."CodigoOperadorCorredor" AS "CodigoOperadorCorredor", 
			---ESPECIFICOS: MESA RF	
			'' AS "TipoOperacionSecundaria",
			0 AS "ValorRescateInicial",
			---ESPECIFICOS: SPOT
			'' AS "IdMonedaSecundaria",
			NPRO."IdNormativa",
			NPRO."IdTipoDocumento",
			NPRO."HIdTipoProducto",
			NPRO."HIdClasificacionTipoInstrumento",
			NPRO."HIdTipoInstrumento",
			NPRO."HIdTipoOperacion",
			NPRO."Uso",
			OPE."EsEnRueda" 	AS "EsEnRueda",
			OPE."EsVentaCorta" 	AS "EsVentaCorta",
			---ESPECIFICOS: FORWARD
			0														    AS "ParidadCierre",
			0														    AS "PlazoVencimiento",
			OPE."DocumentoIdentidad",
			OPE."NombreCompleto",
			'FAC' AS "IdModulo",
			'' AS "FechaIngresoRegistro",
			0 AS "IdOperacionOrigenReasignacion",
			COALESCE(OPE."IdSistemaBursatil",'') AS "IdSistemaBursatil",
			CASE 				
				WHEN v_IdNormativa = 'NCG380Operc' AND CARSIM."IdTipoInstrumento" = 'SIM' AND CARSIM."IdEstado" IN ('ANT') THEN CARSIM."EGRCantidad"		
				ELSE COALESCE(CAR."INGCantidad",0)
			END	AS "INGCantidad",
			OPE."InstrumentoNegociacion" AS "InstrumentoNegociacion",
			COALESCE(OPE."IdComprobanteReferencia", 0) AS "IdComprobanteReferencia",
			COALESCE(OPE."IdComprobante", 0) AS "IdComprobanteOperacion",
			'N' AS "EsVencimientoPacto",
			COALESCE(OPE."SecuenciaMovimiento",0) AS "SecuenciaMovimiento",
			'0001-01-01'												AS "FechaLiquidacion"
		FROM	"BSCL.Facturacion.BD.Vistas::ConsultarOperacionesTotalizadas" OPE
		LEFT JOIN "BSCL.Custodia.BD::CUS.Cartera" AS CARSIM ON OPE."IdComprobante" = CARSIM."FolioComprobante" AND v_IdNormativa = 'NCG380Operc' AND OPE."IdTipoInstrumento" = 'SIM'
		INNER JOIN :v_tablaProductos NPRO 							 --PRODUCTO NORMATIVA
					  ON ((OPE."IdTipoProducto"= NPRO."IdTipoProducto" OR OPE."IdTipoProducto"='')
					 AND (OPE."IdClasificacionTipoInstrumento"	= (CASE WHEN OPE."IdTipoComprobante" = 'NEO' THEN '' ELSE NPRO."IdClasificacionTipoInstrumento" END) OR NPRO."IdClasificacionTipoInstrumento"='') 
					 AND (OPE."IdTipoInstrumento" 				= (CASE WHEN OPE."IdTipoComprobante" = 'NEO' THEN '' ELSE NPRO."IdTipoInstrumento" END) OR NPRO."IdTipoInstrumento"='') 
					 AND (OPE."IdTipoOperacion" 				= NPRO."IdTipoOperacion" 				OR NPRO."IdTipoOperacion"='' OR  (OPE."IdTipoComprobante" IN ('NCE','NCR','NCO','NEE','NEO') AND OPE."IdTipoOperacionRelacionada" = CASE WHEN OPE."IdTipoInstrumento" <>'SIM' THEN NPRO."IdTipoOperacion" ELSE '' END) ))
			   		 AND OPE."EsVentaCorta" = CASE WHEN NPRO."IncluirVentaCorta" = 'N' THEN 'N' ELSE OPE."EsVentaCorta" END
		INNER JOIN :v_tablaFecha PFECH	
		ON (    (v_IdNormativa = 'NCG380Operc' AND CARSIM."IdTipoInstrumento" = 'SIM' AND CARSIM."IdEstado" IN ('VCD', 'ANT') AND PFECH."FECHADESDE" IS NOT NULL AND PFECH."CLASEFECHA" ='MOVIMIENTO' AND TO_DATE(CARSIM."EGRFechaComprobante") BETWEEN TO_DATE(PFECH."FECHADESDE") AND TO_DATE(PFECH."FECHAHASTA"))
			 OR
			 	(v_IdNormativa = 'NCG380Operc' AND CARSIM."IdTipoInstrumento" = 'SIM' AND CARSIM."IdEstado" IN ('VIG') AND PFECH."FECHADESDE" IS NOT NULL AND PFECH."CLASEFECHA" ='MOVIMIENTO' AND TO_DATE(OPE."FechaMovimiento") BETWEEN TO_DATE(PFECH."FECHADESDE") AND TO_DATE(PFECH."FECHAHASTA"))					
			 OR
			 	(v_IdNormativa = 'NCG380Operc' AND Ope."IdTipoInstrumento" <> 'SIM' AND PFECH."FECHADESDE" IS NOT NULL AND PFECH."CLASEFECHA" ='MOVIMIENTO' AND TO_DATE(CASE WHEN OPE."TipoFechaTransaccion" = 'T' THEN UTCTOLOCAL(OPE."FechaTransaccion", 'America/Santiago','platform') ELSE OPE."FechaMovimiento" END) BETWEEN TO_DATE(PFECH."FECHADESDE") AND TO_DATE(PFECH."FECHAHASTA"))
			 OR
			 	(v_IdNormativa <> 'NCG380Operc' AND PFECH."FECHADESDE" IS NOT NULL AND PFECH."CLASEFECHA" ='MOVIMIENTO' AND TO_DATE(OPE."FechaMovimiento") BETWEEN TO_DATE(PFECH."FECHADESDE") AND TO_DATE(PFECH."FECHAHASTA"))												  
			OR
				(PFECH."FECHADESDE" IS NOT NULL AND PFECH."CLASEFECHA" ='MOVIMIENTO' AND (TO_DATE(OPE."FechaMovimiento") BETWEEN TO_DATE(PFECH."FECHADESDE") AND TO_DATE(PFECH."FECHAHASTA")))		 
			 OR  											
				(PFECH."FECHADESDE" IS NULL 	AND	PFECH."CLASEFECHA" ='MOVIMIENTO' AND TO_DATE(OPE."FechaMovimiento") <= TO_DATE(PFECH."FECHAHASTA"))
			 OR
				(PFECH."FECHADESDE" IS NOT NULL AND PFECH."CLASEFECHA" ='TRANSACCION' AND (TO_DATE(OPE."FechaTransaccion") BETWEEN TO_DATE(PFECH."FECHADESDE") AND TO_DATE(PFECH."FECHAHASTA")))		 
			 OR  
				(PFECH."FECHADESDE" IS NULL 	AND PFECH."CLASEFECHA" ='TRANSACCION' AND TO_DATE(OPE."FechaTransaccion") <= TO_DATE(PFECH."FECHAHASTA"))		 
			)  
		LEFT OUTER JOIN "BSCL.Facturacion.BD::FAC.TipoComprobante" AS tipocomprobante ON tipocomprobante."IdTipoComprobante" = OPE."IdTipoComprobante"	
		LEFT JOIN "BSCL.Custodia.BD::CUS.Cartera" AS CAR ON OPE."IdTipoInstrumento" = 'SIM' AND OPE."IdComprobante" = CAR."EGRFolioComprobante" 
		AND OPE."IdTipoComprobante" = CAR."EGRIdTipoComprobante" AND OPE."IdTipoComprobante" = 'NCR'
		WHERE (OPE."IdCliente" = :v_IDCLIENTE OR :v_IDCLIENTE IS NULL)
		AND   (OPE."IdClasificacionTipoInstrumento" = :v_IDCLASIFICACION OR :v_IDCLASIFICACION IS NULL)
		AND  (OPE."IdTipoInstrumento" = :v_IDTIPOINSTRUMENTO OR :v_IDTIPOINSTRUMENTO IS NULL)
		AND  (OPE."IdTipoComprobante" = :v_IDTIPOCOMPROBANTE OR :v_IDTIPOCOMPROBANTE IS NULL)
		AND OPE."IdTipoInstrumento"!='SPT'
		
		UNION ALL 
		-- VENTA CORTA MERCADO RV
		SELECT 
			---CAMPOS COMUNES
			COALESCE(OPE."FechaIngresoRegistro",'')			 			AS "FechaCreacion",
			COALESCE(TO_VARCHAR( OPE."FechaComprobante", 'YYYY-MM-DD') || ' ' || TO_VARCHAR (OPE."FechaIngresoRegistro", 'HH24:MI:SS'), '1900-01-01 00:00:00') AS "FechaMovimiento",
			COALESCE(CAST(OPE."FolioTransaccion"AS  NVARCHAR(40)),'') 	AS "FolioTransaccion",
			COALESCE(CAST(OPE."IdOrden"AS  NVARCHAR(40)),'') 			AS "IdOrden",
			COALESCE(CAST(OPE."IdComprobante"AS  NVARCHAR(40)),'0') 		AS "IdComprobante",
			COALESCE(OPE."IdOperacion",0) 								AS "IdOperacion",			 
			COALESCE(OPE."IdTipoOperacion",'') 							AS "IdTipoOperacion",											
			COALESCE(OPE."IdCliente",0) 								AS "IdCliente",
			COALESCE(OPE."IdPortafolio",0) 								AS "IdPortafolio",
			COALESCE(OPE."IdClasificacionTipoInstrumento",'') 			AS "IdClasificacionTipoInstrumento",
			--COALESCE(OPE."Mercado",'') AS "Mercado",
			COALESCE(OPE."IdEmisor",'') 								AS "IdEmisor",
			COALESCE(OPE."Nemotecnico",'') 								AS "Nemotecnico",
			COALESCE(OPE."Cantidad",0) 									AS "Cantidad",
			COALESCE(OPE."Precio",0) 									AS "Precio",
			COALESCE(OPE."MontoMovimientoComprobante",0)         		AS "Monto",
			COALESCE(OPE."Tasa",0) 										AS "Tasa",
			COALESCE(OPE."IdMoneda",'') 								AS "IdMoneda",
			COALESCE(OPE."IdInstrumento",0)								AS "IdInstrumento",	
			COALESCE(OPE."IdTipoInstrumento",'') 						AS "IdTipoInstrumento", 
			COALESCE(OPE."IdTipoProducto",'') 							AS "IdTipoProducto",
			
			--(Compra/Venta)
			COALESCE(OPE."AccionOperacion",'')							AS "AccionOperacion",
			COALESCE(OPE."MontoCompra",0)								AS "MontoCompra",
			COALESCE(OPE."MontoVenta",0) 								AS "MontoVenta",
			COALESCE(OPE."TasaCompra",0) 								AS "TasaCompra",
			COALESCE(OPE."TasaVenta",0) 								AS "TasaVenta",
			COALESCE(OPE."TotalCompra",0) 								AS "TotalCompra",
			COALESCE(OPE."TotalVenta",0)								AS "TotalVenta",
			
			---ESPECIFICOS: FACTURACION
			COALESCE(OPE."IdTransaccion",0) 							AS "IdTransaccion",
			COALESCE(OPE."FechaTransaccion",'0001-01-01 00:00:00') 		AS "FechaTransaccion",
			COALESCE(OPE."RelacionInstrumento",'') AS "RelacionInstrumento",
			COALESCE(OPE."IdContraparte", 0) AS "IdContraparte",
			COALESCE(OPE."IdOperadorContraparte",0) AS "IdOperadorContraparte",
			COALESCE(OPE."IdOperadorCorredor",0) AS "IdOperadorCorredor",
			COALESCE(OPE."IdTipoComprobante",'') AS "IdTipoComprobante",
			COALESCE(tipocomprobante."Nombre",'') AS "TipoComprobante",
			COALESCE(OPE."FolioComprobante",0) AS "FolioComprobante",
			COALESCE(OPE."ValorPresenteCompraInst",0) AS "ValorPresenteCompraInst",
			COALESCE(OPE."FechaVencimiento",'0001-01-01 00:00:00') AS "FechaVencimiento",

			COALESCE(OPE."Comision",0)					 	AS "Comision",
			COALESCE(OPE."Derecho",0) 						AS "Derecho",
			COALESCE(OPE."Gasto",0) 						AS "Gasto",
			COALESCE(OPE."Impuesto",0) 						AS "Impuesto",

			COALESCE(OPE."IdCondicionLiquidacion",'') AS "IdCondicionLiquidacion",
			COALESCE(OPE."IdDocumentoRelacion",0) AS "IdDocumentoRelacion",
			COALESCE(OPE."FechaVencimientoPacto",'0001-01-01') AS "FechaVencimientoPacto",
			COALESCE(OPE."PrecioPlazo",0) AS "PrecioPlazo",
			COALESCE(OPE."TasaPlazo",0) AS "TasaPlazo",
			COALESCE(OPE."IdMonedaPlazo",'') AS "IdMonedaPlazo",
			COALESCE(OPE."TotalCompraVencimiento",0) AS "TotalCompraVencimiento",
			COALESCE(OPE."TotalVentaVencimiento",0) AS "TotalVentaVencimiento",
			COALESCE(OPE."Custodia",'') AS "Custodia",
			COALESCE(OPE."IdBolsa",'') AS "IdBolsa",
			COALESCE(OPE."IdBoveda",'') AS "IdBoveda",
			COALESCE(OPE."CantidadGarantia",0) AS "CantidadGarantia",
			COALESCE(OPE."NemoSVS",'') AS "NemoSVS",
			COALESCE(OPE."ValorPresente",0) AS "ValorPresente",
			COALESCE(OPE."UtilidadPerdida",0) AS "UtilidadPerdida",
			COALESCE(OPE."FechaOrigenPacto",'0001-01-01 00:00:00') AS "FechaOrigenPacto",
			COALESCE(CAST(OPE."DescripcionOperacion"AS  NVARCHAR(40)),'') AS "DescripcionOperacion",
			COALESCE(OPE."IdEstadoFactura",'') AS "IdEstadoFactura",
			COALESCE(OPE."Canal",'') AS "Canal",
			COALESCE(OPE."ValorIVA",0) AS "ValorIVA",
			COALESCE(OPE."FolioComprobanteAsociado",0) AS "FolioComprobanteAsociado",
			COALESCE(OPE."ModalidadEjecucion", '') AS "ModalidadEjecucion",
			COALESCE(OPE."EsFungible", '') AS "EsFungible",
			COALESCE(OPE."IdMovimientoRelacionado", 0) AS "IdMovimientoRelacionado",
			OPE."CodigoOperadorContraparte" AS "CodigoOperadorContraparte",
			OPE."CodigoOperadorCorredor" AS "CodigoOperadorCorredor", 
			---ESPECIFICOS: MESA RF	
			'' AS "TipoOperacionSecundaria",
			0 AS "ValorRescateInicial",
			---ESPECIFICOS: SPOT
			'' AS "IdMonedaSecundaria",
			NPRO."IdNormativa",
			NPRO."IdTipoDocumento",
			NPRO."HIdTipoProducto",
			NPRO."HIdClasificacionTipoInstrumento",
			NPRO."HIdTipoInstrumento",
			NPRO."HIdTipoOperacion",
			NPRO."Uso",
			OPE."EsEnRueda" 	AS "EsEnRueda",
			OPE."EsVentaCorta" 	AS "EsVentaCorta",
			---ESPECIFICOS: FORWARD
			0														    AS "ParidadCierre",
			0														    AS "PlazoVencimiento",
			OPE."DocumentoIdentidad",
			OPE."NombreCompleto",
			'FAC' AS "IdModulo",
			'' AS "FechaIngresoRegistro",
			0 AS "IdOperacionOrigenReasignacion",
			COALESCE(OPE."IdSistemaBursatil",'') AS "IdSistemaBursatil",
			0 									 AS "INGCantidad",
			OPE."InstrumentoNegociacion" AS "InstrumentoNegociacion",
			COALESCE(OPE."IdComprobanteReferencia", 0) AS "IdComprobanteReferencia",
			COALESCE(OPE."IdComprobante", 0) AS "IdComprobanteOperacion",
			'N' AS "EsVencimientoPacto",
			COALESCE(OPE."SecuenciaMovimiento",0) AS "SecuenciaMovimiento",
			'0001-01-01'	AS "FechaLiquidacion"
		FROM	"BSCL.Facturacion.BD.Vistas::ConsultarOperacionesTotalizadas" OPE
		LEFT OUTER JOIN "BSCL.Facturacion.BD::FAC.TipoComprobante" AS tipocomprobante ON tipocomprobante."IdTipoComprobante" = OPE."IdTipoComprobante"	
		INNER JOIN :v_tablaProductos NPRO 							 --PRODUCTO NORMATIVA
					  ON ((OPE."IdTipoProducto"= NPRO."IdTipoProducto" OR OPE."IdTipoProducto"='')
					  AND (OPE."IdClasificacionTipoInstrumento"	= (CASE WHEN OPE."IdTipoComprobante" = 'NEO' THEN '' ELSE NPRO."IdClasificacionTipoInstrumento" END) OR NPRO."IdClasificacionTipoInstrumento"='')
					  AND (OPE."IdTipoInstrumento" 				= (CASE WHEN OPE."IdTipoComprobante" = 'NEO' THEN '' ELSE NPRO."IdTipoInstrumento" END) OR NPRO."IdTipoInstrumento"='')
					  AND (OPE."IdTipoOperacion" = NPRO."IdTipoOperacion" OR NPRO."IdTipoOperacion"='' ))
		INNER JOIN :v_tablaFecha PFECH	
			ON ( 	(PFECH."FECHADESDE" IS NOT NULL AND PFECH."CLASEFECHA" ='MOVIMIENTO' AND (TO_DATE(OPE."FechaMovimiento") BETWEEN TO_DATE(PFECH."FECHADESDE") AND TO_DATE(PFECH."FECHAHASTA")))		 
				 OR  											
					(PFECH."FECHADESDE" IS NULL 	AND	PFECH."CLASEFECHA" ='MOVIMIENTO' AND TO_DATE(OPE."FechaMovimiento") <= TO_DATE(PFECH."FECHAHASTA"))
				 OR
					(PFECH."FECHADESDE" IS NOT NULL AND PFECH."CLASEFECHA" ='TRANSACCION' AND (TO_DATE(OPE."FechaTransaccion") BETWEEN TO_DATE(PFECH."FECHADESDE") AND TO_DATE(PFECH."FECHAHASTA")))		 
				 OR  
					(PFECH."FECHADESDE" IS NULL 	AND PFECH."CLASEFECHA" ='TRANSACCION' AND TO_DATE(OPE."FechaTransaccion") <= TO_DATE(PFECH."FECHAHASTA"))		 
				)
		WHERE (OPE."IdCliente" = :v_IDCLIENTE OR :v_IDCLIENTE IS NULL)
		AND   (OPE."IdClasificacionTipoInstrumento" = :v_IDCLASIFICACION OR :v_IDCLASIFICACION IS NULL)
		AND  (OPE."IdTipoInstrumento" = :v_IDTIPOINSTRUMENTO OR :v_IDTIPOINSTRUMENTO IS NULL)
		AND  (OPE."IdTipoComprobante" = :v_IDTIPOCOMPROBANTE OR :v_IDTIPOCOMPROBANTE IS NULL)
		AND OPE."EsVentaCorta" ='S'
		AND v_IdNormativa <> 'NCG380Operc'
		
		UNION ALL 
		-- VENCIMIENTO DE SIMULTANEAS (no tienen un comprobante de facturación asociado)
		---CAMPOS COMUNES
		SELECT 
			COALESCE(CARSIM."FechaIngresoRegistro", '') AS "FechaCreacion",
			CASE 
				WHEN CARSIM."IdTipoInstrumento" = 'SIM' AND CARSIM."IdEstado" IN ('VCD') THEN COALESCE(TO_VARCHAR(CARSIM."EGRFechaComprobante", 'YYYY-MM-DD') || ' ' || TO_VARCHAR (CARSIM."FechaIngresoRegistro", 'HH24:MI:SS'), '1900-01-01 00:00:00') 				
				ELSE COALESCE(TO_VARCHAR(CARSIM."FechaOperacion", 'YYYY-MM-DD') || ' ' || TO_VARCHAR (CARSIM."FechaIngresoRegistro", 'HH24:MI:SS'), '1900-01-01 00:00:00') 
			END AS "FechaMovimiento",
			COALESCE(CARSIM."FolioExternoReferencia",'') 							AS "FolioTransaccion",
			''																		AS "IdOrden",
			'0'																		AS "IdComprobante",
			COALESCE(CARSIM."IdOperacion",0) 										AS "IdOperacion",
			--COALESCE(CARSIM."IdTipoOperacion",'') 									AS "IdTipoOperacion",
			CASE 
				WHEN COALESCE(CARSIM."IdTipoOperacion",'') = 'COMSIM' THEN 'VESIMC'
				WHEN COALESCE(CARSIM."IdTipoOperacion",'') = 'VENSIM' THEN 'VESIMV'
			END AS "IdTipoOperacion",
			COALESCE(CARSIM."IdCliente",0) 											AS "IdCliente",
			COALESCE(CARSIM."IdPortafolio",0) 										AS "IdPortafolio",
			COALESCE(CARSIM."IdClasificacionTipoInstrumento",'') 					AS "IdClasificacionTipoInstrumento",
			--COALESCE(OPE."Mercado",'') AS "Mercado",
			COALESCE(CARSIM."IdEmisor",'') 											AS "IdEmisor",
			COALESCE(SUB."Nemotecnico",'') 											AS "Nemotecnico",
			COALESCE(CARSIM."INGCantidad",0.0) 										AS "Cantidad",
			COALESCE(CARSIM."PrecioAPlazo",0.0) 									AS "Precio",
			COALESCE(CARSIM."MontoAPlazo",0.0)         								AS "Monto",
			0.0 																	AS "Tasa",
			COALESCE(CARSIM."IdMonedaInstrumento",'') 								AS "IdMoneda",
			COALESCE(CARSIM."IdInstrumento",0)										AS "IdInstrumento",	
			COALESCE(SUB."IdTipoInstrumento",'') 									AS "IdTipoInstrumento", 
			COALESCE(CARSIM."IdTipoProducto",'') 									AS "IdTipoProducto",
			--(Compra/Venta)
			''																		AS "AccionOperacion",
			CASE WHEN COALESCE(CARSIM."IdTipoOperacion",'') = 'COMSIM' THEN COALESCE(CARSIM."INGMontoAPlazo", 0.0) ELSE 0.0 END AS "MontoCompra",
			CASE WHEN COALESCE(CARSIM."IdTipoOperacion",'') = 'VENSIM' THEN COALESCE(CARSIM."INGMontoAPlazo", 0.0) ELSE 0.0 END AS "MontoVenta",
			COALESCE(CARSIM."TasaCompra",0.0) 										AS "TasaCompra",
			0.0								 										AS "TasaVenta",
			0.0 																	AS "TotalCompra",
			0.0																		AS "TotalVenta",
			---ESPECIFICOS: FACTURACION
			0																		AS "IdTransaccion",
			'0001-01-01 00:00:00'							 						AS "FechaTransaccion",
			''															    		AS "RelacionInstrumento",
			0															    		AS "IdContraparte",
			0																    	AS "IdOperadorContraparte",
			0															    		AS "IdOperadorCorredor",
			COALESCE(CARSIM."IdTipoComprobante",'') 								AS "IdTipoComprobante",
			COALESCE(tipocomprobante."Nombre",'') 									AS "TipoComprobante",
			COALESCE(CARSIM."FolioComprobante",0) 									AS "FolioComprobante",
			0.0															    		AS "ValorPresenteCompraInst",
			COALESCE(CARSIM."FechaVencimientoOperacion",'0001-01-01 00:00:00') 		AS "FechaVencimiento",
			0.0																	 	AS "Comision",
			0.0												 						AS "Derecho",
			0.0												 						AS "Gasto",
			0.0											 							AS "Impuesto",
			COALESCE(CARSIM."IdCondicionLiquidacion",'') 							AS "IdCondicionLiquidacion",
			0																	    AS "IdDocumentoRelacion",
			'0001-01-01' 															AS "FechaVencimientoPacto",
			COALESCE(CARSIM."PrecioAPlazo",0.0) 									AS "PrecioPlazo",
			0.0																	    AS "TasaPlazo",
			''																	    AS "IdMonedaPlazo",
			0.0								 										AS "TotalCompraVencimiento",
			0.0						 												AS "TotalVentaVencimiento",
			COALESCE(CARSIM."EGRIdTipoComprobante",'') 								AS "Custodia",
			COALESCE(CARSIM."IdBolsa",'') 											AS "IdBolsa",
			COALESCE(CARSIM."IdBoveda",'') 											AS "IdBoveda",
			0.0	 																	AS "CantidadGarantia",
			COALESCE(CARSIM."NemoSVS",'') 											AS "NemoSVS",
			0.0									 									AS "ValorPresente",
			0.0 																	AS "UtilidadPerdida",
			'0001-01-01 00:00:00'	 												AS "FechaOrigenPacto",
			COALESCE(LEFT(CARSIM."EGRGlosa", 100), '') 								AS "DescripcionOperacion",
			'' 																		AS "IdEstadoFactura",
			COALESCE(CARSIM."IdTipoCanalIngreso",'') 								AS "Canal",
			0.0 																	AS "ValorIVA", 
			CASE WHEN v_IdNormativa = 'NCG380Operc' THEN CARSIM."IdOperacion" 
				 ELSE 0 
		    END																		AS "FolioComprobanteAsociado",
			'' 																		AS "ModalidadEjecucion",
			COALESCE(CAST(CARSIM."EsFungible" AS CHAR), 'N') 						AS "EsFungible",
			0	 																	AS "IdMovimientoRelacionado",
			''								 										AS "CodigoOperadorContraparte",
			''																 		AS "CodigoOperadorCorredor", 
			---ESPECIFICOS: MESA RF	
			'' 																		AS "TipoOperacionSecundaria",
			0.0 																	AS "ValorRescateInicial",
			---ESPECIFICOS: SPOT
			COALESCE(CARSIM."IdMonedaSecundaria",'') 	 							AS "IdMonedaSecundaria",
			NPRO."IdNormativa",
			NPRO."IdTipoDocumento",
			NPRO."HIdTipoProducto",
			NPRO."HIdClasificacionTipoInstrumento",
			NPRO."HIdTipoInstrumento",
			NPRO."HIdTipoOperacion",
			NPRO."Uso",
			'' 																		AS "EsEnRueda",
			'' 																		AS "EsVentaCorta", 
			---ESPECIFICOS: FORWARD
			0.0														    			AS "ParidadCierre",
			0														    			AS "PlazoVencimiento",
			CSDN."DocumentoIdentidad",
			CSDN."NombreCompleto",
			'FAC' 																	AS "IdModulo",
			'' 																		AS "FechaIngresoRegistro", -- aqui va bien hasta ahora									 												
			0	 																	AS "IdOperacionOrigenReasignacion",
			'' 																		AS "IdSistemaBursatil",
			COALESCE(CARSIM."INGCantidad", 0.0)										AS "INGCantidad",
			COALESCE(CARSIM."Nemotecnico", '')										AS "InstrumentoNegociacion",
			0																		AS "IdComprobanteReferencia",
			0 																		AS "IdComprobanteOperacion",
			'N'																		AS "EsVencimientoPacto",
			CASE 
				WHEN COALESCE(MOVTRAN."SecuenciaMovimiento", 0) <> 0 THEN MOVTRAN."SecuenciaMovimiento"
				ELSE COALESCE(CARSIM."IdCartera",0) 	
			END																		AS "SecuenciaMovimiento",
			'0001-01-01'															AS "FechaLiquidacion"
		FROM "BSCL.Custodia.BD::CUS.Cartera" as CARSIM
		LEFT OUTER JOIN "BSCL.Facturacion.BD::FAC.TipoComprobante" AS tipocomprobante ON tipocomprobante."IdTipoComprobante" = CARSIM."IdTipoComprobante"
		INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS CSDN ON (CSDN."IdCliente" = CARSIM."IdCliente")
		INNER JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarUltimoMovimientoSubyacente" AS ultSub ON ultSub."FolioCarteraRelacionada" = CARSIM."FolioCartera"
		INNER JOIN "BSCL.Custodia.BD::CUS.Subyacente" AS SUB ON (SUB."IdSubyacente" = ultSub."IdSubyacente")
		LEFT JOIN "BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito" MOVTRAN ON MOVTRAN."IdMovimientoCarteraEnTransito" = SUB."IdMovimientoCarteraEnTransito" AND v_IdNormativa = 'NCG380Operc'
		LEFT JOIN "BSCL.Facturacion.BD.Vistas::ConsultarMovimientoComprobanteSimple" AS MovNormal ON MovNormal."IdEmpresa" = CARSIM."IdEmpresa"	
		AND MovNormal."IdComprobante" = CARSIM."FolioComprobante"
		AND MovNormal."NemotecnicoSubyacente" = SUB."Nemotecnico"
		AND MovNormal."Cantidad" = CARSIM."Cantidad"
		AND trim(MovNormal."FolioTransaccion") = CARSIM."FolioExternoReferencia"
		AND v_IdNormativa = 'NCG380Operc'
		LEFT JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarCorredora" CC ON CC."IdCorredora" = MovNormal."IdCorredoraContraparte" AND v_IdNormativa = 'NCG380Operc'
		INNER JOIN :v_tablaProductos NPRO 							 --PRODUCTO NORMATIVA
					  ON ((CARSIM."IdTipoProducto" = NPRO."IdTipoProducto" OR CARSIM."IdTipoProducto"='')
					 AND (CARSIM."IdClasificacionTipoInstrumento"	= (CASE WHEN CARSIM."IdTipoComprobante" = 'NEO' THEN '' ELSE NPRO."IdClasificacionTipoInstrumento" END) OR NPRO."IdClasificacionTipoInstrumento"='') 
					 AND (CARSIM."IdTipoInstrumento" 				= (CASE WHEN CARSIM."IdTipoComprobante" = 'NEO' THEN '' ELSE NPRO."IdTipoInstrumento" END) OR NPRO."IdTipoInstrumento"='') 
					 AND (CARSIM."IdTipoOperacion" 				= NPRO."IdTipoOperacion" 				OR NPRO."IdTipoOperacion"='' OR  (CARSIM."IdTipoComprobante" IN ('NCE','NCR','NCO','NEE','NEO') AND CARSIM."IdTipoInstrumento" <>'SIM') ))  		 
		INNER JOIN :v_tablaFecha PFECH	
		ON ((PFECH."FECHADESDE" IS NOT NULL AND PFECH."CLASEFECHA" ='MOVIMIENTO' AND (TO_DATE(CARSIM."FechaVencimientoOperacion") BETWEEN TO_DATE(PFECH."FECHADESDE") AND TO_DATE(PFECH."FECHAHASTA")))		 
			 OR  											
				(PFECH."FECHADESDE" IS NULL 	AND	PFECH."CLASEFECHA" ='MOVIMIENTO' AND TO_DATE(CARSIM."FechaVencimientoOperacion") <= TO_DATE(PFECH."FECHAHASTA"))
			 OR
				(PFECH."FECHADESDE" IS NOT NULL AND PFECH."CLASEFECHA" ='TRANSACCION' AND (TO_DATE(CARSIM."FechaVencimientoOperacion") BETWEEN TO_DATE(PFECH."FECHADESDE") AND TO_DATE(PFECH."FECHAHASTA")))		 
			 OR  
				(PFECH."FECHADESDE" IS NULL 	AND PFECH."CLASEFECHA" ='TRANSACCION' AND TO_DATE(CARSIM."FechaVencimientoOperacion") <= TO_DATE(PFECH."FECHAHASTA"))		 
			)	
		WHERE CARSIM."IdTipoInstrumento" = 'SIM' 
		AND (CARSIM."IdCliente" = :v_IDCLIENTE OR :v_IDCLIENTE IS NULL)
		AND  (CARSIM."IdClasificacionTipoInstrumento" = :v_IDCLASIFICACION OR :v_IDCLASIFICACION IS NULL)
		AND  (CARSIM."IdTipoInstrumento" = :v_IDTIPOINSTRUMENTO OR :v_IDTIPOINSTRUMENTO IS NULL)
		AND  (CARSIM."IdTipoComprobante" = :v_IDTIPOCOMPROBANTE OR :v_IDTIPOCOMPROBANTE IS NULL)
		AND CARSIM."IdEstado" = 'VCD'
	
		UNION ALL
		
		SELECT 
			---CAMPOS COMUNES
			COALESCE(OPE."FechaIngresoRegistro",'')			 			AS "FechaCreacion",
			COALESCE(CMO."FechaMovimiento",CMOREF."FechaMovimiento", OPE."FechaMovimiento", '')	AS "FechaMovimiento",
			COALESCE(CAST(OPE."FolioTransaccion"AS  NVARCHAR(40)),'') 	AS "FolioTransaccion",
			COALESCE(CAST(OPE."IdOrden"AS  NVARCHAR(40)),'') 			AS "IdOrden",
			COALESCE(CAST(OPE."IdComprobante"AS  NVARCHAR(40)),'0') 		AS "IdComprobante",
			COALESCE(OPE."IdOperacion",0) 								AS "IdOperacion",
			COALESCE(OPE."IdTipoOperacion",'') 							AS "IdTipoOperacion",
			COALESCE(OPE."IdCliente",0) 								AS "IdCliente",
			COALESCE(OPE."IdPortafolio",0) 								AS "IdPortafolio",
			COALESCE(OPE."IdClasificacionTipoInstrumento",'') 			AS "IdClasificacionTipoInstrumento",
			--COALESCE(OPE."Mercado",'') AS "Mercado",
			COALESCE(OPE."IdEmisor",'') 								AS "IdEmisor",
			COALESCE(OPE."Nemotecnico",'') 								AS "Nemotecnico",
			COALESCE(OPE."Cantidad",0) 									AS "Cantidad",
			COALESCE(OPE."Precio",0) 									AS "Precio",
			COALESCE(OPE."MontoMovimientoComprobante",0)         		AS "Monto",
			COALESCE(OPE."Tasa",0) 										AS "Tasa",
			COALESCE(OPE."IdMoneda",'') 								AS "IdMoneda",
			COALESCE(OPE."IdInstrumento",0)								AS "IdInstrumento",	
			COALESCE(OPE."IdTipoInstrumento",'') 						AS "IdTipoInstrumento", 
			COALESCE(OPE."IdTipoProducto",'') 							AS "IdTipoProducto",
			
			--(Compra/Venta)
			COALESCE(OPE."AccionOperacion",'')							AS "AccionOperacion",
			COALESCE(OPE."MontoCompra",0)								AS "MontoCompra",
			COALESCE(OPE."MontoVenta",0) 								AS "MontoVenta",
			COALESCE(OPE."TasaCompra",0) 								AS "TasaCompra",
			COALESCE(OPE."TasaVenta",0) 								AS "TasaVenta",
			COALESCE(OPE."TotalCompra",0) 								AS "TotalCompra",
			COALESCE(OPE."TotalVenta",0)								AS "TotalVenta",
			
			---ESPECIFICOS: FACTURACION
			COALESCE(OPE."IdTransaccion",0) 							AS "IdTransaccion",
			COALESCE(OPE."FechaTransaccion",'0001-01-01 00:00:00') 		AS "FechaTransaccion",
			COALESCE(OPE."RelacionInstrumento",'') AS "RelacionInstrumento",
			COALESCE(OPE."IdContraparte", 0) AS "IdContraparte",
			COALESCE(OPE."IdOperadorContraparte",0) AS "IdOperadorContraparte",
			COALESCE(OPE."IdOperadorCorredor",0) AS "IdOperadorCorredor",
			COALESCE(OPE."IdTipoComprobante",'') AS "IdTipoComprobante",
			COALESCE(tipocomprobante."Nombre",'') AS "TipoComprobante",
			COALESCE(OPE."FolioComprobante",0) AS "FolioComprobante",
			COALESCE(OPE."ValorPresenteCompraInst",0) AS "ValorPresenteCompraInst",
			COALESCE(OPE."FechaVencimiento",'0001-01-01 00:00:00') AS "FechaVencimiento",

			COALESCE(OPE."Comision",0)					 	AS "Comision",
			COALESCE(OPE."Derecho",0) 						AS "Derecho",
			COALESCE(OPE."Gasto",0) 						AS "Gasto",
			COALESCE(OPE."Impuesto",0) 						AS "Impuesto",

			COALESCE(OPE."IdCondicionLiquidacion",'') AS "IdCondicionLiquidacion",
			COALESCE(OPE."IdDocumentoRelacion",0) AS "IdDocumentoRelacion",
			COALESCE(OPE."FechaVencimientoPacto",'0001-01-01') AS "FechaVencimientoPacto",
			COALESCE(OPE."PrecioPlazo",0) AS "PrecioPlazo",
			COALESCE(OPE."TasaPlazo",0) AS "TasaPlazo",
			COALESCE(OPE."IdMonedaPlazo",'') AS "IdMonedaPlazo",
			COALESCE(OPE."TotalCompraVencimiento",0) AS "TotalCompraVencimiento",
			COALESCE(OPE."TotalVentaVencimiento",0) AS "TotalVentaVencimiento",
			COALESCE(OPE."Custodia",'') AS "Custodia",
			COALESCE(OPE."IdBolsa",'') AS "IdBolsa",
			COALESCE(OPE."IdBoveda",'') AS "IdBoveda",
			COALESCE(OPE."CantidadGarantia",0) AS "CantidadGarantia",
			COALESCE(OPE."NemoSVS",'') AS "NemoSVS",
			COALESCE(OPE."ValorPresente",0) AS "ValorPresente",
			COALESCE(OPE."UtilidadPerdida",0) AS "UtilidadPerdida",
			COALESCE(OPE."FechaOrigenPacto",'0001-01-01 00:00:00') AS "FechaOrigenPacto",
			COALESCE(CAST(OPE."DescripcionOperacion"AS  NVARCHAR(40)),'') AS "DescripcionOperacion",
			COALESCE(OPE."IdEstadoFactura",'') AS "IdEstadoFactura",
			COALESCE(OPE."Canal",'') AS "Canal",
			COALESCE(OPE."ValorIVA",0) AS "ValorIVA",
			COALESCE(OPE."FolioComprobanteAsociado",0) AS "FolioComprobanteAsociado",
			COALESCE(OPE."ModalidadEjecucion", '') AS "ModalidadEjecucion",
			COALESCE(OPE."EsFungible", '') AS "EsFungible",
			COALESCE(OPE."IdMovimientoRelacionado", 0) AS "IdMovimientoRelacionado",
			OPE."CodigoOperadorContraparte" AS "CodigoOperadorContraparte",
			OPE."CodigoOperadorCorredor" AS "CodigoOperadorCorredor", 
			---ESPECIFICOS: MESA RF	
			'' AS "TipoOperacionSecundaria",
			0 AS "ValorRescateInicial",
			---ESPECIFICOS: SPOT
			'' AS "IdMonedaSecundaria",
			NPRO."IdNormativa",
			NPRO."IdTipoDocumento",
			NPRO."HIdTipoProducto",
			NPRO."HIdClasificacionTipoInstrumento",
			NPRO."HIdTipoInstrumento",
			NPRO."HIdTipoOperacion",
			NPRO."Uso",
			OPE."EsEnRueda" 	AS "EsEnRueda",
			OPE."EsVentaCorta" 	AS "EsVentaCorta",
			---ESPECIFICOS: FORWARD
			0														    AS "ParidadCierre",
			0														    AS "PlazoVencimiento",
			OPE."DocumentoIdentidad",
			OPE."NombreCompleto",
			'FAC' AS "IdModulo",
			'' AS "FechaIngresoRegistro",
			0 AS "IdOperacionOrigenReasignacion",
			COALESCE(OPE."IdSistemaBursatil",'') AS "IdSistemaBursatil",
			0 AS "INGCantidad",
			'' AS "InstrumentoNegociacion",
			COALESCE(OPE."IdComprobanteReferencia", 0) AS "IdComprobanteReferencia",
			COALESCE(OPE."IdComprobante", 0) AS "IdComprobanteOperacion",
			'N' AS "EsVencimientoPacto",
			COALESCE(OPE."SecuenciaMovimiento",0) AS "SecuenciaMovimiento",
			'0001-01-01'							AS "FechaLiquidacion"
		FROM	"BSCL.Facturacion.BD.Vistas::ConsultarOperacionesTotalizadas" OPE
		INNER JOIN "BSCL.Mesa.BD.Vistas::SPT.ConsultarOperaciones" AS CMO ON  CMO."IdTransaccion" = OPE."IdTransaccion" 
		AND (CMO."IdTipoInstrumento"='SPT' AND OPE."IdTipoInstrumento"='SPT')
		LEFT  JOIN "BSCL.Mesa.BD.Vistas::SPT.ConsultarOperaciones" AS CMOREF 
		ON CMOREF."IdComprobanteReferencia" = OPE."IdComprobante" 
		AND CMOREF."IdTransaccion" = OPE."IdTransaccion" 
		AND CMOREF."FolioComprobante" > 0
		AND (CMOREF."IdTipoInstrumento"='SPT' AND OPE."IdTipoInstrumento"='SPT')
		LEFT OUTER JOIN "BSCL.Facturacion.BD::FAC.TipoComprobante" AS tipocomprobante ON tipocomprobante."IdTipoComprobante" = OPE."IdTipoComprobante"	
		INNER JOIN :v_tablaProductos NPRO 							 --PRODUCTO NORMATIVA
			  ON ((OPE."IdTipoProducto"= NPRO."IdTipoProducto" OR OPE."IdTipoProducto"='')
			 AND (OPE."IdClasificacionTipoInstrumento"	= (CASE WHEN OPE."IdTipoComprobante" = 'NEO' THEN '' ELSE NPRO."IdClasificacionTipoInstrumento" END) OR NPRO."IdClasificacionTipoInstrumento"='') 
			 AND (OPE."IdTipoInstrumento" 				= (CASE WHEN OPE."IdTipoComprobante" = 'NEO' THEN '' ELSE NPRO."IdTipoInstrumento" END) OR NPRO."IdTipoInstrumento"='') 
			 AND (OPE."IdTipoOperacion" 				= NPRO."IdTipoOperacion" 				OR NPRO."IdTipoOperacion"='' OR  (OPE."IdTipoComprobante" IN ('NCE','NCR','NCO','NEE','NEO') AND OPE."IdTipoOperacionRelacionada" = CASE WHEN OPE."IdTipoInstrumento" <>'SIM' THEN NPRO."IdTipoOperacion" ELSE '' END) ))
	   		 AND OPE."EsVentaCorta" = CASE WHEN NPRO."IncluirVentaCorta" = 'N' THEN 'N' ELSE OPE."EsVentaCorta" END
			
			
		INNER JOIN :v_tablaFecha PFECH	
			ON ( 	(PFECH."FECHADESDE" IS NOT NULL AND PFECH."CLASEFECHA" ='MOVIMIENTO' AND (TO_DATE(OPE."FechaMovimiento") BETWEEN TO_DATE(PFECH."FECHADESDE") AND TO_DATE(PFECH."FECHAHASTA")))		 
				 OR  											
					(PFECH."FECHADESDE" IS NULL 	AND	PFECH."CLASEFECHA" ='MOVIMIENTO' AND TO_DATE(OPE."FechaMovimiento") <= TO_DATE(PFECH."FECHAHASTA"))
				 OR
					(PFECH."FECHADESDE" IS NOT NULL AND PFECH."CLASEFECHA" ='TRANSACCION' AND (TO_DATE(OPE."FechaTransaccion") BETWEEN TO_DATE(PFECH."FECHADESDE") AND TO_DATE(PFECH."FECHAHASTA")))		 
				 OR  
					(PFECH."FECHADESDE" IS NULL 	AND PFECH."CLASEFECHA" ='TRANSACCION' AND TO_DATE(OPE."FechaTransaccion") <= TO_DATE(PFECH."FECHAHASTA"))		 
				)  		 
		WHERE (OPE."IdCliente" = :v_IDCLIENTE OR :v_IDCLIENTE IS NULL)
		AND   (OPE."IdClasificacionTipoInstrumento" = :v_IDCLASIFICACION OR :v_IDCLASIFICACION IS NULL)
		AND  (OPE."IdTipoInstrumento" = :v_IDTIPOINSTRUMENTO OR :v_IDTIPOINSTRUMENTO IS NULL)
		AND  (OPE."IdTipoComprobante" = :v_IDTIPOCOMPROBANTE OR :v_IDTIPOCOMPROBANTE IS NULL);
		

		v_tableTemp = SELECT "IdComprobante","IdComprobanteReferencia", "IdComprobanteOperacion" FROM :p_out_TablaSalidaTemp;
		p_out_TablaSalida=
		SELECT "FechaCreacion", "FechaMovimiento", "FolioTransaccion", "IdOrden", "IdComprobante", "IdOperacion", "IdTipoOperacion", "IdCliente",
					"IdPortafolio", "IdClasificacionTipoInstrumento", "IdEmisor", "Nemotecnico", "Cantidad", "Precio", "Monto", "Tasa", "IdMoneda",
					"IdInstrumento", "IdTipoInstrumento", "IdTipoProducto", "AccionOperacion", "MontoCompra", "MontoVenta", "TasaCompra", "TasaVenta",
					"TotalCompra", "TotalVenta", "IdTransaccion", "FechaTransaccion", "RelacionInstrumento", "IdContraparte", "IdOperadorContraparte",
					"IdOperadorCorredor", "IdTipoComprobante","TipoComprobante", "FolioComprobante", "ValorPresenteCompraInst", "FechaVencimiento",
					"Comision", "Derecho", "Gasto", "Impuesto", "IdCondicionLiquidacion", "IdDocumentoRelacion", "FechaVencimientoPacto",
					"PrecioPlazo", "TasaPlazo", "IdMonedaPlazo", "TotalCompraVencimiento", "TotalVentaVencimiento", "Custodia", "IdBolsa",
					"IdBoveda", "CantidadGarantia", "NemoSVS", "ValorPresente", "UtilidadPerdida", "FechaOrigenPacto", "DescripcionOperacion",
					"IdEstadoFactura", "Canal", "ValorIVA", "FolioComprobanteAsociado", "ModalidadEjecucion", "EsFungible", "IdMovimientoRelacionado",
					"CodigoOperadorContraparte", "CodigoOperadorCorredor", "TipoOperacionSecundaria", "ValorRescateInicial", "IdMonedaSecundaria",
					"IdNormativa", "IdTipoDocumento", "HIdTipoProducto", "HIdClasificacionTipoInstrumento", "HIdTipoInstrumento", "HIdTipoOperacion",
					"Uso", "EsEnRueda", "EsVentaCorta", "ParidadCierre", "PlazoVencimiento", "DocumentoIdentidad", "NombreCompleto", "IdModulo", "FechaIngresoRegistro",
					"IdOperacionOrigenReasignacion", "IdSistemaBursatil", "INGCantidad", "InstrumentoNegociacion", "IdComprobanteReferencia", "IdComprobanteOperacion",
					"EsVencimientoPacto", "SecuenciaMovimiento", "FechaLiquidacion"
		FROM :p_out_TablaSalidaTemp

		UNION ALL SELECT * FROM :p_out_TablaSalida;
				 
	END IF; --FACTURACION

	IF p_paramAnulacionNC = 'S' THEN
		p_out_TablaSalida =
		SELECT  t."FechaCreacion", t."FechaMovimiento", t."FolioTransaccion", t."IdOrden", t."IdComprobante", t."IdOperacion", t."IdTipoOperacion", t."IdCliente",
				t."IdPortafolio", t."IdClasificacionTipoInstrumento", t."IdEmisor", t."Nemotecnico", t."Cantidad", t."Precio", t."Monto", t."Tasa", t."IdMoneda",
				t."IdInstrumento", t."IdTipoInstrumento", t."IdTipoProducto", t."AccionOperacion", t."MontoCompra", t."MontoVenta", t."TasaCompra", t."TasaVenta",
				t."TotalCompra", t."TotalVenta", t."IdTransaccion", t."FechaTransaccion", t."RelacionInstrumento", t."IdContraparte", t."IdOperadorContraparte",
				t."IdOperadorCorredor", t."IdTipoComprobante",t."TipoComprobante", t."FolioComprobante", t."ValorPresenteCompraInst", t."FechaVencimiento",
				t."Comision", t."Derecho", t."Gasto", t."Impuesto", t."IdCondicionLiquidacion", t."IdDocumentoRelacion", t."FechaVencimientoPacto",
				t."PrecioPlazo", t."TasaPlazo", t."IdMonedaPlazo", t."TotalCompraVencimiento", t."TotalVentaVencimiento", t."Custodia", t."IdBolsa",
				t."IdBoveda", t."CantidadGarantia", t."NemoSVS", t."ValorPresente", t."UtilidadPerdida", t."FechaOrigenPacto", t."DescripcionOperacion",
				t."IdEstadoFactura", t."Canal", t."ValorIVA", t."FolioComprobanteAsociado", t."ModalidadEjecucion", t."EsFungible", t."IdMovimientoRelacionado",
				t."CodigoOperadorContraparte", t."CodigoOperadorCorredor", t."TipoOperacionSecundaria", t."ValorRescateInicial", t."IdMonedaSecundaria",
				t."IdNormativa", t."IdTipoDocumento", t."HIdTipoProducto", t."HIdClasificacionTipoInstrumento", t."HIdTipoInstrumento", t."HIdTipoOperacion",
				t."Uso", t."EsEnRueda", t."EsVentaCorta", t."ParidadCierre", t."PlazoVencimiento", t."DocumentoIdentidad", t."NombreCompleto", t."IdModulo", t."FechaIngresoRegistro",
				t."IdOperacionOrigenReasignacion", t."IdSistemaBursatil", t."INGCantidad", t."InstrumentoNegociacion", t."IdComprobanteReferencia", t."IdComprobanteOperacion",
				t."EsVencimientoPacto", t."SecuenciaMovimiento", t."FechaLiquidacion"
		FROM :p_out_TablaSalida t
		LEFT JOIN (
				SELECT tFac."IdComprobante" AS "IdComprobanteFact"
				FROM :p_out_TablaSalidaTemp AS tFac 
				INNER JOIN (
					SELECT tCompr."IdComprobanteReferencia"
					FROM :v_tableTemp AS tCompr
					WHERE (NOT tCompr."IdComprobanteReferencia" = 0) AND (tCompr."IdComprobanteReferencia" IS NOT NULL)
				) tCompbte ON tFac."IdComprobante" = tCompbte."IdComprobanteReferencia"
		) tRefF ON t."IdComprobante" = tRefF."IdComprobanteFact" 
		LEFT JOIN (
				SELECT tNC."IdComprobante"
				FROM :v_tableTemp AS tNC
				WHERE (NOT tNC."IdComprobante" = 0) AND (NOT tNC."IdComprobanteReferencia" = 0) AND (tNC."IdComprobanteReferencia" IS NOT NULL)
		) tRefNC ON t."IdComprobante" = tRefNC."IdComprobante"
		WHERE tRefNC."IdComprobante" IS NULL AND tRefF."IdComprobanteFact" IS NULL;--Solo quedan comprobantes Facturas no Referenciadas ni NC
		
	ELSE 
		IF p_paramAnulacionNC = '' THEN
			p_out_TablaSalida = 
			SELECT  "FechaCreacion", "FechaMovimiento", "FolioTransaccion", "IdOrden", "IdComprobante", "IdOperacion", "IdTipoOperacion", "IdCliente",
				"IdPortafolio", "IdClasificacionTipoInstrumento", "IdEmisor", "Nemotecnico", "Cantidad", "Precio", "Monto", "Tasa", "IdMoneda",
				"IdInstrumento", "IdTipoInstrumento", "IdTipoProducto", "AccionOperacion", "MontoCompra", "MontoVenta", "TasaCompra", "TasaVenta",
				"TotalCompra", "TotalVenta", "IdTransaccion", "FechaTransaccion", "RelacionInstrumento", "IdContraparte", "IdOperadorContraparte",
				"IdOperadorCorredor", "IdTipoComprobante","TipoComprobante", "FolioComprobante", "ValorPresenteCompraInst", "FechaVencimiento",
				"Comision", "Derecho", "Gasto", "Impuesto", "IdCondicionLiquidacion", "IdDocumentoRelacion", "FechaVencimientoPacto",
				"PrecioPlazo", "TasaPlazo", "IdMonedaPlazo", "TotalCompraVencimiento", "TotalVentaVencimiento", "Custodia", "IdBolsa",
				"IdBoveda", "CantidadGarantia", "NemoSVS", "ValorPresente", "UtilidadPerdida", "FechaOrigenPacto", "DescripcionOperacion",
				"IdEstadoFactura", "Canal", "ValorIVA", "FolioComprobanteAsociado", "ModalidadEjecucion", "EsFungible", "IdMovimientoRelacionado",
				"CodigoOperadorContraparte", "CodigoOperadorCorredor", "TipoOperacionSecundaria", "ValorRescateInicial", "IdMonedaSecundaria",
				"IdNormativa", "IdTipoDocumento", "HIdTipoProducto", "HIdClasificacionTipoInstrumento", "HIdTipoInstrumento", "HIdTipoOperacion",
				"Uso", "EsEnRueda", "EsVentaCorta", "ParidadCierre", "PlazoVencimiento", "DocumentoIdentidad", "NombreCompleto", "IdModulo", "FechaIngresoRegistro",
				"IdOperacionOrigenReasignacion", "IdSistemaBursatil", "INGCantidad", "InstrumentoNegociacion", "IdComprobanteReferencia", "IdComprobanteOperacion",
				"EsVencimientoPacto", "SecuenciaMovimiento", "FechaLiquidacion"
			FROM :p_out_TablaSalida;
		END IF;
	END IF;
		
	--MESA RF
	IF v_MRF ='S' OR (p_IdNormativa<>'' and v_MRF ='') THEN

		IF :p_nominal = 'N' THEN
			--Tabla de Salida
			p_out_TablaSalida=
			SELECT

				---CAMPOS COMUNES
				OPE."FechaCreacion"											AS "FechaCreacion",
				COALESCE(TO_VARCHAR( OPE."FechaMovimiento", 'YYYY-MM-DD') || ' ' || TO_VARCHAR (OPE."FechaCreacion", 'HH24:MI:SS'), '1900-01-01 00:00:00') AS "FechaMovimiento",
				COALESCE(CAST(OPE."FolioTransaccion"AS  NVARCHAR(40)),'')	AS "FolioTransaccion",
				'' 															AS "IdOrden",
				''													 		AS "IdComprobante",
				COALESCE(OPE."IdOperacion",0) 								AS "IdOperacion",
				COALESCE(OPE."IdTipoOperacion",'') 							AS "IdTipoOperacion",
				COALESCE(OPE."IdCliente",0) 								AS "IdCliente",
				COALESCE(OPE."IdPortafolio",0) 								AS "IdPortafolio",
				COALESCE(OPE."IdClasificacionTipoInstrumento",'') 			AS "IdClasificacionTipoInstrumento",
				COALESCE(OPE."IdEmisor",'') 								AS "IdEmisor",
				COALESCE(OPE."Nemotecnico",'') 								AS "Nemotecnico",

				COALESCE(OPE."Cantidad",0) 									AS "Cantidad",
				COALESCE(OPE."Precio",0) 									AS "Precio",
				COALESCE(OPE."MontoValorizado",0) 						    AS "Monto",
				COALESCE(OPE."Tasa",0) 										AS "Tasa",
				COALESCE(OPE."IdMoneda",'') 								AS "IdMoneda",
				COALESCE(OPE."IdInstrumento",0)								AS "IdInstrumento",	
				COALESCE(OPE."IdClasificacionTipoInstrumento",'')			AS "IdTipoInstrumento", 				
				COALESCE(OPE."IdTipoProducto",'') 							AS "IdTipoProducto",
					--(Compra/Venta)
				COALESCE(OPE."AccionOperacion",'')							AS "AccionOperacion",
				COALESCE(OPE."MontoCompra",0)								AS "MontoCompra",
				COALESCE(OPE."MontoVenta",0) 								AS "MontoVenta",
				COALESCE(OPE."TasaCompra",0) 								AS "TasaCompra",
				COALESCE(OPE."TasaVenta",0) 								AS "TasaVenta",
				0 															AS "TotalCompra",--No se aplican comisiones 
				0															AS "TotalVenta",
				---ESPECIFICOS: FACTURACION
				0 															AS "IdTransaccion",
				COALESCE(OPE."FechaCreacion", '0001-01-01 00:00:00')		AS "FechaTransaccion",
				'' AS "RelacionInstrumento",
				0 AS "IdContraparte",
				COALESCE(OPE."IdOperadorContraparte",0) AS "IdOperadorContraparte",
				COALESCE(OPE."IdOperadorCorredor",0) AS "IdOperadorCorredor",
				'' AS "IdTipoComprobante",
				'' AS "TipoComprobante",
				COALESCE(OPE."FolioComprobante",0) AS "FolioComprobante",
				COALESCE(OPE."ValorPresenteCompraInst",0) AS "ValorPresenteCompraInst",
				COALESCE(OPE."FechaVencimiento",'0001-01-01 00:00:00') AS "FechaVencimiento",
				--No se aplican comisiones (Directas desde Mesa)
				0 															AS "Comision",
				0 															AS "Derecho",
				0 															AS "Gasto",
				0 															AS "Impuesto",
				COALESCE(OPE."IdCondicionLiquidacion",'') AS "IdCondicionLiquidacion",
				0 AS "IdDocumentoRelacion",
				IFNULL(OPE."FechaVencimientoPacto",'0001-01-01') AS "FechaVencimientoPacto",
				COALESCE(OPE."PrecioPlazo",0) AS "PrecioPlazo",
				COALESCE(OPE."TasaPlazo",0) AS "TasaPlazo",
				COALESCE(OPE."IdMonedaPlazo",'') AS "IdMonedaPlazo",
				COALESCE(OPE."TotalCompraVencimiento",0) AS "TotalCompraVencimiento",
				COALESCE(OPE."TotalVentaVencimiento",0) AS "TotalVentaVencimiento",
				COALESCE(OPE."Custodia",'') AS "Custodia",
				COALESCE(OPE."IdBolsa",'') AS "IdBolsa",
				COALESCE(OPE."IdBoveda",'') AS "IdBoveda",
				COALESCE(OPE."CantidadGarantia",0) AS "CantidadGarantia",
				COALESCE(OPE."NemoSVS",'') AS "NemoSVS",
				COALESCE(OPE."ValorPresente",0) AS "ValorPresente",
				COALESCE(OPE."UtilidadPerdida",0) AS "UtilidadPerdida",
				COALESCE(OPE."FechaOrigenPacto",'0001-01-01 00:00:00') AS "FechaOrigenPacto",
				COALESCE(CAST(OPE."DescripcionOperacion"AS  NVARCHAR(40)),'') AS "DescripcionOperacion",
				--* MSH, IMPCREDOPT-2527
				CASE WHEN OPE."IdEstado" IN ('ANT','LIQ') AND OPE."IdTipoOperacion" IN ('COMPAC','VENPAC','ANTPAC') THEN 'VIG'
					ELSE COALESCE(OPE."IdEstado",'??') END AS "IdEstadoFactura",
				COALESCE(OPE."Canal",'') AS "Canal",
				COALESCE(OPE."ValorIVA",0) AS "ValorIVA",
				COALESCE(OPE."FolioComprobanteAsociado",0) AS "FolioComprobanteAsociado",
				'' AS "ModalidadEjecucion",
				COALESCE(OPE."EsFungible", '') AS "EsFungible",
				0 AS "IdMovimientoRelacionado",
				'' AS "CodigoOperadorContraparte",
				'' AS "CodigoOperadorCorredor", 
				---ESPECIFICOS: MESA RF	
				COALESCE(OPE."TipoOperacionSecundaria", '') AS "TipoOperacionSecundaria",
				COALESCE(OPE."ValorRescateInicial", 0) AS "ValorRescateInicial",
				---ESPECIFICOS: SPOT
				'' AS "IdMonedaSecundaria",
				NPRO."IdNormativa",
				NPRO."IdTipoDocumento",
				NPRO."HIdTipoProducto",
				NPRO."HIdClasificacionTipoInstrumento",
				NPRO."HIdTipoInstrumento",
				NPRO."HIdTipoOperacion"	,
				NPRO."Uso",
				'N'   AS "EsEnRueda",
				'N'   AS "EsVentaCorta",
				---ESPECIFICOS: FORWARD
				0														    AS "ParidadCierre",
				0														    AS "PlazoVencimiento",
				OPE."DocumentoIdentidad",	
				OPE."NombreCompleto",
				'MRF' AS "IdModulo",
				OPE."FechaIngresoRegistro"                                  AS "FechaIngresoRegistro",
				OPE."IdOperacionOrigenReasignacion"							AS "IdOperacionOrigenReasignacion",
				'' 															AS "IdSistemaBursatil", 
				0 AS "INGCantidad",
				'' AS "InstrumentoNegociacion",
				0 AS "IdComprobanteReferencia",
				0 AS "IdComprobanteOperacion",
				'N' AS "EsVencimientoPacto",
				0 AS "SecuenciaMovimiento",
				'0001-01-01'												AS "FechaLiquidacion"
			FROM "BSCL.Mesa.BD.Vistas::MRF.ConsultarOperaciones" OPE
			INNER JOIN :v_tablaProductos NPRO 							 --PRODUCTO NORMATIVA
						  ON ((OPE."IdTipoProducto"= NPRO."IdTipoProducto")
						 AND (OPE."IdClasificacionTipoInstrumento"	= NPRO."IdClasificacionTipoInstrumento" OR NPRO."IdClasificacionTipoInstrumento"='') 
						 AND (OPE."IdTipoInstrumento" 				= NPRO."IdTipoInstrumento"  			OR NPRO."IdTipoInstrumento"='') 
						 AND (OPE."IdTipoOperacion" 				= NPRO."IdTipoOperacion" 				OR NPRO."IdTipoOperacion"=''))
			INNER JOIN :v_tablaFecha PFECH	
			ON ( 	(PFECH."FECHADESDE" IS NOT NULL  AND (TO_DATE(OPE."FechaMovimiento") BETWEEN TO_DATE(PFECH."FECHADESDE") AND TO_DATE(PFECH."FECHAHASTA")))		 
				 OR  											
					(PFECH."FECHADESDE" IS NULL 	 AND TO_DATE(OPE."FechaMovimiento") <= TO_DATE(PFECH."FECHAHASTA"))
				)  
				
			WHERE (OPE."IdCliente" = :v_IDCLIENTE OR :v_IDCLIENTE IS NULL)
			AND   (OPE."IdClasificacionTipoInstrumento" = :v_IDCLASIFICACION OR :v_IDCLASIFICACION IS NULL)
			AND  (OPE."IdTipoInstrumento" = :v_IDTIPOINSTRUMENTO OR :v_IDTIPOINSTRUMENTO IS NULL)
			
			
			UNION ALL SELECT * FROM :p_out_TablaSalida;

		ELSE 
			p_out_TablaSalida=
			SELECT
	
				---CAMPOS COMUNES
				OPE."FechaCreacion"											AS "FechaCreacion",
				COALESCE(TO_VARCHAR( OPE."FechaMovimiento", 'YYYY-MM-DD') || ' ' || TO_VARCHAR (OPE."FechaCreacion", 'HH24:MI:SS'), '1900-01-01 00:00:00') AS "FechaMovimiento",
				COALESCE(CAST(OPE."FolioTransaccion"AS  NVARCHAR(40)),'')	AS "FolioTransaccion",
				'' 															AS "IdOrden",
				''													 		AS "IdComprobante",
				COALESCE(OPE."IdOperacion",0) 								AS "IdOperacion",
				COALESCE(OPE."IdTipoOperacion",'') 							AS "IdTipoOperacion",
				COALESCE(OPE."IdCliente",0) 								AS "IdCliente",
				COALESCE(OPE."IdPortafolio",0) 								AS "IdPortafolio",
				COALESCE(OPE."IdClasificacionTipoInstrumento",'') 			AS "IdClasificacionTipoInstrumento",
				COALESCE(OPE."IdEmisor",'') 								AS "IdEmisor",
				COALESCE(OPE."Nemotecnico",'') 								AS "Nemotecnico",

				COALESCE(OPE."Cantidad",0) 									AS "Cantidad",
				COALESCE(OPE."Precio",0) 									AS "Precio",
				COALESCE(OPE."MontoValorizado",0) 						    AS "Monto",
				COALESCE(OPE."Tasa",0) 										AS "Tasa",
				COALESCE(OPE."IdMoneda",'') 								AS "IdMoneda",
				COALESCE(OPE."IdInstrumento",0)								AS "IdInstrumento",	

				--* MSH 2018
				COALESCE(OPE."IdTipoInstrumento",'') 						AS "IdTipoInstrumento",				
				COALESCE(OPE."IdTipoProducto",'') 							AS "IdTipoProducto",
					--(Compra/Venta)
				COALESCE(OPE."AccionOperacion",'')							AS "AccionOperacion",
				COALESCE(OPE."MontoCompra",0)								AS "MontoCompra",
				COALESCE(OPE."MontoVenta",0) 								AS "MontoVenta",
				COALESCE(OPE."TasaCompra",0) 								AS "TasaCompra",
				COALESCE(OPE."TasaVenta",0) 								AS "TasaVenta",
				0 															AS "TotalCompra",--No se aplican comisiones 
				0															AS "TotalVenta",
				---ESPECIFICOS: FACTURACION
				0 															AS "IdTransaccion",
				COALESCE(OPE."FechaCreacion", '0001-01-01 00:00:00')		AS "FechaTransaccion",
				'' AS "RelacionInstrumento",
				0 AS "IdContraparte",
				COALESCE(OPE."IdOperadorContraparte",0) AS "IdOperadorContraparte",
				COALESCE(OPE."IdOperadorCorredor",0) AS "IdOperadorCorredor",
				'' AS "IdTipoComprobante",
				'' AS "TipoComprobante",
				COALESCE(OPE."FolioComprobante",0) AS "FolioComprobante",
				COALESCE(OPE."ValorPresenteCompraInst",0) AS "ValorPresenteCompraInst",
				COALESCE(OPE."FechaVencimiento",'0001-01-01 00:00:00') AS "FechaVencimiento",
				--No se aplican comisiones (Directas desde Mesa)
				0 															AS "Comision",
				0 															AS "Derecho",
				0 															AS "Gasto",
				0 															AS "Impuesto",
				COALESCE(OPE."IdCondicionLiquidacion",'') AS "IdCondicionLiquidacion",
				0 AS "IdDocumentoRelacion",
				IFNULL(OPE."FechaVencimientoPacto",'0001-01-01') AS "FechaVencimientoPacto",
				COALESCE(OPE."PrecioPlazo",0) AS "PrecioPlazo",
				COALESCE(OPE."TasaPlazo",0) AS "TasaPlazo",
				COALESCE(OPE."IdMonedaPlazo",'') AS "IdMonedaPlazo",
				COALESCE(OPE."TotalCompraVencimiento",0) AS "TotalCompraVencimiento",
				COALESCE(OPE."TotalVentaVencimiento",0) AS "TotalVentaVencimiento",
				COALESCE(OPE."Custodia",'') AS "Custodia",
				COALESCE(OPE."IdBolsa",'') AS "IdBolsa",
				COALESCE(OPE."IdBoveda",'') AS "IdBoveda",
				COALESCE(OPE."CantidadGarantia",0) AS "CantidadGarantia",
				COALESCE(OPE."NemoSVS",'') AS "NemoSVS",
				COALESCE(OPE."ValorPresente",0) AS "ValorPresente",
				COALESCE(OPE."UtilidadPerdida",0) AS "UtilidadPerdida",
				COALESCE(OPE."FechaOrigenPacto",'0001-01-01 00:00:00') AS "FechaOrigenPacto",
				COALESCE(CAST(OPE."DescripcionOperacion"AS  NVARCHAR(40)),'') AS "DescripcionOperacion",
				--* MSH, IMPCREDOPT-2527
				CASE WHEN OPE."IdEstado" IN ('ANT','LIQ') AND OPE."IdTipoOperacion" IN ('COMPAC','VENPAC','ANTPAC') THEN 'VIG'
					ELSE COALESCE(OPE."IdEstado",'??') END AS "IdEstadoFactura",
				COALESCE(OPE."Canal",'') AS "Canal",
				COALESCE(OPE."ValorIVA",0) AS "ValorIVA",
				COALESCE(OPE."FolioComprobanteAsociado",0) AS "FolioComprobanteAsociado",
				'' AS "ModalidadEjecucion",
				COALESCE(OPE."EsFungible", '') AS "EsFungible",
				0 AS "IdMovimientoRelacionado",
				'' AS "CodigoOperadorContraparte",
				'' AS "CodigoOperadorCorredor", 
				---ESPECIFICOS: MESA RF	
				COALESCE(OPE."TipoOperacionSecundaria", '') AS "TipoOperacionSecundaria",
				COALESCE(OPE."ValorRescateInicial", 0) AS "ValorRescateInicial",
				---ESPECIFICOS: SPOT
				'' AS "IdMonedaSecundaria",
				NPRO."IdNormativa",
				NPRO."IdTipoDocumento",
     			NPRO."HIdTipoProducto",
				NPRO."HIdClasificacionTipoInstrumento",
				NPRO."HIdTipoInstrumento",
				NPRO."HIdTipoOperacion"	,
				NPRO."Uso",
				'N'   AS "EsEnRueda",
				'N'   AS "EsVentaCorta",
				---ESPECIFICOS: FORWARD
				0														    AS "ParidadCierre",
				0														    AS "PlazoVencimiento",
				OPE."DocumentoIdentidad",	
				OPE."NombreCompleto",
				'MRF' AS "IdModulo",
				''    AS "FechaIngresoRegistro",
				OPE."IdOperacionOrigenReasignacion"							AS "IdOperacionOrigenReasignacion",
				'' 															AS "IdSistemaBursatil",
				0 AS "INGCantidad",
				'' AS "InstrumentoNegociacion",
				0 AS "IdComprobanteReferencia",
				0 AS "IdComprobanteOperacion",
				'N' AS "EsVencimientoPacto",
				0 AS "SecuenciaMovimiento",
				'0001-01-01'												AS "FechaLiquidacion"
			FROM "BSCL.Mesa.BD.Vistas::MRF.ConsultarLibroOperaciones" OPE
			INNER JOIN :v_tablaProductos NPRO 							 --PRODUCTO NORMATIVA
						  ON ((OPE."IdTipoProducto"= NPRO."IdTipoProducto")
						 AND (OPE."IdClasificacionTipoInstrumento"	= NPRO."IdClasificacionTipoInstrumento" OR NPRO."IdClasificacionTipoInstrumento"='') 
						 AND (OPE."IdTipoInstrumento" 				= NPRO."IdTipoInstrumento"  			OR NPRO."IdTipoInstrumento"='') 
						 AND (OPE."IdTipoOperacion" 				= NPRO."IdTipoOperacion" 				OR NPRO."IdTipoOperacion"=''))
			INNER JOIN :v_tablaFecha PFECH	
			ON ( 	(PFECH."FECHADESDE" IS NOT NULL  AND (TO_DATE(OPE."FechaMovimiento") BETWEEN TO_DATE(PFECH."FECHADESDE") AND TO_DATE(PFECH."FECHAHASTA")))		 
				 OR  											
					(PFECH."FECHADESDE" IS NULL 	 AND TO_DATE(OPE."FechaMovimiento") <= TO_DATE(PFECH."FECHAHASTA"))
				)  
				
			WHERE (OPE."IdCliente" = :v_IDCLIENTE OR :v_IDCLIENTE IS NULL)
			AND   (OPE."IdClasificacionTipoInstrumento" = :v_IDCLASIFICACION OR :v_IDCLASIFICACION IS NULL)
			AND  (OPE."IdTipoInstrumento" = :v_IDTIPOINSTRUMENTO OR :v_IDTIPOINSTRUMENTO IS NULL)
			
			UNION ALL
			--Vencimiento de Pactos
			SELECT
	
				---CAMPOS COMUNES
				OPE."FechaCreacion"											AS "FechaCreacion",
				COALESCE(TO_VARCHAR( OPE."FechaVencimientoPacto", 'YYYY-MM-DD') || ' ' || TO_VARCHAR (OPE."FechaCreacion", 'HH24:MI:SS'), '1900-01-01 00:00:00') AS "FechaMovimiento",
				COALESCE(CAST(OPE."FolioTransaccion"AS  NVARCHAR(40)),'')	AS "FolioTransaccion",
				'' 															AS "IdOrden",
				''													 		AS "IdComprobante",
				COALESCE(OPE."IdOperacion",0) 								AS "IdOperacion",
				COALESCE(OPE."IdTipoOperacion",'') 							AS "IdTipoOperacion",
				COALESCE(OPE."IdCliente",0) 								AS "IdCliente",
				COALESCE(OPE."IdPortafolio",0) 								AS "IdPortafolio",
				COALESCE(OPE."IdClasificacionTipoInstrumento",'') 			AS "IdClasificacionTipoInstrumento",
				COALESCE(OPE."IdEmisor",'') 								AS "IdEmisor",
				COALESCE(OPE."Nemotecnico",'') 								AS "Nemotecnico",

				COALESCE(OPE."Cantidad",0) 									AS "Cantidad",
				COALESCE(OPE."Precio",0) 									AS "Precio",
				COALESCE(OPE."MontoValorizado",0) 						    AS "Monto",
				COALESCE(OPE."Tasa",0) 										AS "Tasa",
				COALESCE(OPE."IdMoneda",'') 								AS "IdMoneda",
				COALESCE(OPE."IdInstrumento",0)								AS "IdInstrumento",	

				--* MSH 2018
				COALESCE(OPE."IdTipoInstrumento",'') 						AS "IdTipoInstrumento",				
				COALESCE(OPE."IdTipoProducto",'') 							AS "IdTipoProducto",
					--(Compra/Venta)
				COALESCE(OPE."AccionOperacion",'')							AS "AccionOperacion",
				COALESCE(OPE."MontoCompra",0)								AS "MontoCompra",
				COALESCE(OPE."MontoVenta",0) 								AS "MontoVenta",
				COALESCE(OPE."TasaCompra",0) 								AS "TasaCompra",
				COALESCE(OPE."TasaVenta",0) 								AS "TasaVenta",
				0 															AS "TotalCompra",--No se aplican comisiones 
				0															AS "TotalVenta",
				---ESPECIFICOS: FACTURACION
				0 															AS "IdTransaccion",
				COALESCE(OPE."FechaCreacion", '0001-01-01 00:00:00')		AS "FechaTransaccion",
				'' AS "RelacionInstrumento",
				0 AS "IdContraparte",
				COALESCE(OPE."IdOperadorContraparte",0) AS "IdOperadorContraparte",
				COALESCE(OPE."IdOperadorCorredor",0) AS "IdOperadorCorredor",
				'' AS "IdTipoComprobante",
				'' AS "TipoComprobante",
				COALESCE(OPE."FolioComprobante",0) AS "FolioComprobante",
				COALESCE(OPE."ValorPresenteCompraInst",0) AS "ValorPresenteCompraInst",
				COALESCE(OPE."FechaVencimiento",'0001-01-01 00:00:00') AS "FechaVencimiento",
				--No se aplican comisiones (Directas desde Mesa)
				0 															AS "Comision",
				0 															AS "Derecho",
				0 															AS "Gasto",
				0 															AS "Impuesto",
				COALESCE(OPE."IdCondicionLiquidacion",'') AS "IdCondicionLiquidacion",
				0 AS "IdDocumentoRelacion",
				IFNULL(OPE."FechaVencimientoPacto",'0001-01-01') AS "FechaVencimientoPacto",
				COALESCE(OPE."PrecioPlazo",0) AS "PrecioPlazo",
				COALESCE(OPE."TasaPlazo",0) AS "TasaPlazo",
				COALESCE(OPE."IdMonedaPlazo",'') AS "IdMonedaPlazo",
				COALESCE(OPE."TotalCompraVencimiento",0) AS "TotalCompraVencimiento",
				COALESCE(OPE."TotalVentaVencimiento",0) AS "TotalVentaVencimiento",
				COALESCE(OPE."Custodia",'') AS "Custodia",
				COALESCE(OPE."IdBolsa",'') AS "IdBolsa",
				COALESCE(OPE."IdBoveda",'') AS "IdBoveda",
				COALESCE(OPE."CantidadGarantia",0) AS "CantidadGarantia",
				COALESCE(OPE."NemoSVS",'') AS "NemoSVS",
				COALESCE(OPE."ValorPresente",0) AS "ValorPresente",
				COALESCE(OPE."UtilidadPerdida",0) AS "UtilidadPerdida",
				COALESCE(OPE."FechaOrigenPacto",'0001-01-01 00:00:00') AS "FechaOrigenPacto",
				COALESCE(CAST(OPE."DescripcionOperacion"AS  NVARCHAR(40)),'') AS "DescripcionOperacion",
				--* MSH, IMPCREDOPT-2527
				CASE WHEN OPE."IdEstado" IN ('ANT','LIQ') AND OPE."IdTipoOperacion" IN ('COMPAC','VENPAC','ANTPAC') THEN 'VIG'
					ELSE COALESCE(OPE."IdEstado",'??') END AS "IdEstadoFactura",
				COALESCE(OPE."Canal",'') AS "Canal",
				COALESCE(OPE."ValorIVA",0) AS "ValorIVA",
				COALESCE(OPE."FolioComprobanteAsociado",0) AS "FolioComprobanteAsociado",
				'' AS "ModalidadEjecucion",
				COALESCE(OPE."EsFungible", '') AS "EsFungible",
				0 AS "IdMovimientoRelacionado",
				'' AS "CodigoOperadorContraparte",
				'' AS "CodigoOperadorCorredor", 
				---ESPECIFICOS: MESA RF	
				COALESCE(OPE."TipoOperacionSecundaria", '') AS "TipoOperacionSecundaria",
				COALESCE(OPE."ValorRescateInicial", 0) AS "ValorRescateInicial",
				---ESPECIFICOS: SPOT
				'' AS "IdMonedaSecundaria",
				NPRO."IdNormativa",
				NPRO."IdTipoDocumento",
     			NPRO."HIdTipoProducto",
				NPRO."HIdClasificacionTipoInstrumento",
				NPRO."HIdTipoInstrumento",
				NPRO."HIdTipoOperacion"	,
				NPRO."Uso",
				'N'   AS "EsEnRueda",
				'N'   AS "EsVentaCorta",
				---ESPECIFICOS: FORWARD
				0														    AS "ParidadCierre",
				0														    AS "PlazoVencimiento",
				OPE."DocumentoIdentidad",	
				OPE."NombreCompleto",
				'MRF' AS "IdModulo",
				''    AS "FechaIngresoRegistro",
				OPE."IdOperacionOrigenReasignacion"							AS "IdOperacionOrigenReasignacion",
				'' 															AS "IdSistemaBursatil",
				0 AS "INGCantidad",
				'' AS "InstrumentoNegociacion",
				0 AS "IdComprobanteReferencia",
				0 AS "IdComprobanteOperacion",
				'S' AS "EsVencimientoPacto",
				0 AS "SecuenciaMovimiento",
				'0001-01-01'												AS "FechaLiquidacion"
			FROM "BSCL.Mesa.BD.Vistas::MRF.ConsultarLibroOperaciones" OPE
			INNER JOIN :v_tablaProductos NPRO 							 --PRODUCTO NORMATIVA
						  ON ((OPE."IdTipoProducto"= NPRO."IdTipoProducto")
						 AND (OPE."IdClasificacionTipoInstrumento"	= NPRO."IdClasificacionTipoInstrumento" OR NPRO."IdClasificacionTipoInstrumento"='') 
						 AND (OPE."IdTipoInstrumento" 				= NPRO."IdTipoInstrumento"  			OR NPRO."IdTipoInstrumento"='') 
						 AND (OPE."IdTipoOperacion" 				= NPRO."IdTipoOperacion" 				OR NPRO."IdTipoOperacion"=''))
			INNER JOIN :v_tablaFecha PFECH	
			ON ((PFECH."FECHADESDE" IS NOT NULL AND (TO_DATE(OPE."FechaVencimientoPacto") BETWEEN TO_DATE(PFECH."FECHADESDE") AND TO_DATE(PFECH."FECHAHASTA"))))  
				
			WHERE (OPE."IdCliente" = :v_IDCLIENTE OR :v_IDCLIENTE IS NULL)
			AND (OPE."IdClasificacionTipoInstrumento" = :v_IDCLASIFICACION OR :v_IDCLASIFICACION IS NULL)
			AND (OPE."IdTipoInstrumento" = :v_IDTIPOINSTRUMENTO OR :v_IDTIPOINSTRUMENTO IS NULL)
			AND (OPE."IdTipoOperacion" IN ('VENPAC','COMPAC'))
			AND (OPE."IdEmisor" <> '')
			AND (v_IdNormativa = 'NCG380Operc')
			AND (OPE."IdEstado" = 'LIQ')
			
			UNION ALL SELECT * FROM :p_out_TablaSalida;
		END IF;
						 
	END IF;	--MESA RF	
		

	--MESA SPOT
	IF v_MSP ='S' OR (p_IdNormativa<>'' and v_MSP ='') THEN
		--Tabla de Salida
		p_out_TablaSalida=
		SELECT
		
			---CAMPOS COMUNES
			OPE."FechaMovimiento"									AS "FechaCreacion",
			OPE."FechaMovimiento" AS "FechaMovimiento",
			COALESCE(CAST(OPE."FolioTransaccion"AS  NVARCHAR(40)),'')		AS "FolioTransaccion",
			'' 															AS "IdOrden",
			COALESCE(CAST(OPE."IdComprobante"AS  NVARCHAR(40)),'') 		AS "IdComprobante",
			COALESCE(OPE."IdOperacion",0) AS "IdOperacion",
			COALESCE(OPE."IdTipoOperacion",'') AS "IdTipoOperacion",
			COALESCE(OPE."IdCliente",0) AS "IdCliente",
			COALESCE(OPE."IdPortafolio",0) AS "IdPortafolio",
			COALESCE(OPE."IdClasificacionTipoInstrumento",'') 			AS "IdClasificacionTipoInstrumento",
			'' 															AS "IdEmisor",
			COALESCE(OPE."Nemotecnico",'') AS "Nemotecnico",
			COALESCE(OPE."Cantidad",0) AS "Cantidad",
			COALESCE(OPE."Precio",0) AS "Precio",
			COALESCE(OPE."MontoMonedaSecundaria",0) AS "Monto",
			0															AS "Tasa",
			COALESCE(OPE."IdMoneda",'') AS "IdMoneda",
			COALESCE(OPE."IdInstrumento",0)	AS "IdInstrumento",	
			COALESCE(OPE."IdTipoInstrumento",'') AS "IdTipoInstrumento", 
			COALESCE(OPE."IdTipoProducto",'') 	AS "IdTipoProducto",
					--(Compra/Venta)
			COALESCE(OPE."AccionOperacion",'')							AS "AccionOperacion",
			COALESCE(OPE."MontoCompra",0)								AS "MontoCompra",
			COALESCE(OPE."MontoVenta",0) 								AS "MontoVenta",
			0															AS "TasaCompra",
			COALESCE(OPE."TasaVenta",0)									AS "TasaVenta",
			0 															AS "TotalCompra", --No se aplican comisiones 
			0															AS "TotalVenta",
			---ESPECIFICOS: FACTURACION
			0 															AS "IdTransaccion",
			COALESCE(OPE."FechaIngresoRegistro",'0001-01-01 00:00:00') 	AS "FechaTransaccion",				
			'' 															AS "RelacionInstrumento",
			COALESCE(OPE."IdContraparte", 0) AS "IdContraparte",
			COALESCE(OPE."IdOperadorContraparte",0) AS "IdOperadorContraparte",
			COALESCE(OPE."IdOperadorCorredor",0) AS "IdOperadorCorredor",
			COALESCE(OPE."IdTipoComprobante",'') AS "IdTipoComprobante",
			COALESCE(tipocomprobante."Nombre",'') AS "TipoComprobante",
			COALESCE(OPE."FolioComprobante",0) AS "FolioComprobante",
			0															AS "ValorPresenteCompraInst",
			'' 															AS "FechaVencimiento",
			--No se aplican comisiones (Directas desde Mesa)
			0 															AS "Comision",
			0 															AS "Derecho",
			0 															AS "Gasto",
			0 															AS "Impuesto",	
			'' AS "IdCondicionLiquidacion",
			0 AS "IdDocumentoRelacion",
			'' AS "FechaVencimientoPacto",
			0 AS "PrecioPlazo",
			0 AS "TasaPlazo",
			'' AS "IdMonedaPlazo",
			0 AS "TotalCompraVencimiento",
			0 AS "TotalVentaVencimiento",
			COALESCE(OPE."Custodia",'') AS "Custodia",
			'BCS' AS "IdBolsa",                                       
			'' AS "IdBoveda",
			0 AS "CantidadGarantia",
			COALESCE(OPE."NemoSVS",'') AS "NemoSVS",
			0 AS "ValorPresente",
			COALESCE(OPE."Utilidad",0)									 AS "UtilidadPerdida",
			'' AS "FechaOrigenPacto",
			COALESCE(CAST(OPE."TipoOperacion" AS  NVARCHAR(40)),'') AS "DescripcionOperacion",
			COALESCE(OPE."IdEstadoOperacion",'') AS "IdEstadoFactura",
			COALESCE(OPE."Canal",'') AS "Canal",
			0 AS "ValorIVA",
			0 AS "FolioComprobanteAsociado",
			'' AS "ModalidadEjecucion",
			'' AS "EsFungible",
			0 AS "IdMovimientoRelacionado",
			'' AS "CodigoOperadorContraparte",
			'' AS "CodigoOperadorCorredor", 
			---ESPECIFICOS: MESA RF	
			'' AS "TipoOperacionSecundaria",
			0 AS "ValorRescateInicial",
			---ESPECIFICOS: SPOT
			COALESCE(OPE."IdMonedaSecundaria", '') AS "IdMonedaSecundaria",
			NPRO."IdNormativa",
			NPRO."IdTipoDocumento",
			NPRO."HIdTipoProducto",
			NPRO."HIdClasificacionTipoInstrumento",
			NPRO."HIdTipoInstrumento",
			NPRO."HIdTipoOperacion",
			NPRO."Uso",
			'N' as "EsEnRueda",
			'N'   AS "EsVentaCorta",
			---ESPECIFICOS: FORWARD
			0														    AS "ParidadCierre",
			0														    AS "PlazoVencimiento",
			OPE."DocumentoIdentidad",	
			OPE."NombreCompleto",
			'MSP' AS "IdModulo",
			''    AS "FechaIngresoRegistro",
			0    AS "IdOperacionOrigenReasignacion",
			'' 	 AS "IdSistemaBursatil",
			0 AS "INGCantidad",
			'' AS "InstrumentoNegociacion",
			0 AS "IdComprobanteReferencia",
			0 AS "IdComprobanteOperacion",
			'N' AS "EsVencimientoPacto",
			0 AS "SecuenciaMovimiento",
			'0001-01-01' AS "FechaLiquidacion"
		FROM "BSCL.Mesa.BD.Vistas::SPT.ConsultarOperaciones" OPE
		LEFT OUTER JOIN "BSCL.Facturacion.BD::FAC.TipoComprobante" AS tipocomprobante ON tipocomprobante."IdTipoComprobante" = OPE."IdTipoComprobante"
		INNER JOIN :v_tablaProductos NPRO 							 --PRODUCTO NORMATIVA
					  ON ((OPE."IdTipoProducto"= NPRO."IdTipoProducto")
					 AND (OPE."IdClasificacionTipoInstrumento"	= NPRO."IdClasificacionTipoInstrumento" OR NPRO."IdClasificacionTipoInstrumento"='') 
					 AND (OPE."IdTipoInstrumento" 				= NPRO."IdTipoInstrumento"  			OR NPRO."IdTipoInstrumento"='') 
					 AND (OPE."IdTipoOperacion" 				= NPRO."IdTipoOperacion" 				OR NPRO."IdTipoOperacion"=''))
		INNER JOIN :v_tablaFecha PFECH	
		ON ( 	(PFECH."FECHADESDE" IS NOT NULL  AND (TO_DATE(OPE."FechaMovimiento") BETWEEN TO_DATE(PFECH."FECHADESDE") AND TO_DATE(PFECH."FECHAHASTA")))		 
			 OR  											
				(PFECH."FECHADESDE" IS NULL 	 AND TO_DATE(OPE."FechaMovimiento") <= TO_DATE(PFECH."FECHAHASTA"))
			)  	
			
		WHERE (OPE."IdCliente" = :v_IDCLIENTE OR :v_IDCLIENTE IS NULL)
		AND   (OPE."IdClasificacionTipoInstrumento" = :v_IDCLASIFICACION OR :v_IDCLASIFICACION IS NULL)
		AND  (OPE."IdTipoInstrumento" = :v_IDTIPOINSTRUMENTO OR :v_IDTIPOINSTRUMENTO IS NULL)
		AND  (OPE."IdTipoComprobante" = :v_IDTIPOCOMPROBANTE OR :v_IDTIPOCOMPROBANTE IS NULL)			
		AND OPE."IdEstadoOperacion" = 'VIG'	
		AND OPE."EstadoConfirmacion" = 'CNF'
		AND OPE."IdMovimientoComprobante" > 0
		AND OPE."IdEstadoMovimientoComprobante" = 'FAC'
										   
		UNION ALL SELECT * FROM :p_out_TablaSalida;	
				 
	END IF; --MESA SPOT

		
	--- MESA FORWARD	
	IF v_MFW ='S' OR (p_IdNormativa<>'' and v_MFW ='') THEN
		--Tabla de Salida
		p_out_TablaSalida=
		SELECT
			---CAMPOS COMUNES
			OPE."FechaCreacion",
			OPE."FechaMovimiento" 					AS "FechaMovimiento",
			COALESCE(CAST(OPE."IdOperacion"AS  NVARCHAR(40)),'')			AS "FolioTransaccion",
			'' 										AS "IdOrden",
			'' 										AS "IdComprobante",
			COALESCE(OPE."IdOperacion",0) 			AS "IdOperacion",
			COALESCE(OPE."IdTipoOperacion",'') 		AS "IdTipoOperacion",
			COALESCE(OPE."IdCliente",0) 			AS "IdCliente",
			COALESCE(OPE."IdPortafolio",0)  		AS "IdPortafolio",
			COALESCE(OPE."IdClasificacionTipoInstrumento",'')   AS "IdClasificacionTipoInstrumento",
			'' 													AS "IdEmisor",
			COALESCE(OPE."Nemotecnico",'') 						AS "Nemotecnico",
			COALESCE(OPE."INGCantidad",0) 						AS "Cantidad",
			COALESCE(OPE."TipoCambioCierre",0) 							AS "Precio",
			COALESCE(OPE."INGMontoMonedaSecundaria",0) AS "Monto",
			0													AS "Tasa",
			COALESCE(OPE."IdMonedaPrincipal",'') 				AS "IdMoneda",
			COALESCE(OPE."IdInstrumento",0)						AS "IdInstrumento",	
			COALESCE(OPE."IdTipoInstrumento",'') 				AS "IdTipoInstrumento", 
			COALESCE(OPE."IdTipoProducto",'') 					AS "IdTipoProducto",

					--(Compra/Venta)
			COALESCE(OPE."AccionOperacion",'')							AS "AccionOperacion",
			COALESCE(OPE."MontoCompra",0)								AS "MontoCompra",
			COALESCE(OPE."MontoVenta",0) 								AS "MontoVenta",
			0							 								AS "TasaCompra",
			0															AS "TasaVenta",
			0 															AS "TotalCompra", --No se aplican comisiones 
			0															AS "TotalVenta",
			
			---ESPECIFICOS: FACTURACION
			0 AS "IdTransaccion",
			"FechaCreacion" AS "FechaTransaccion",
			'' AS "RelacionInstrumento",
			0 AS "IdContraparte",
			0 AS "IdOperadorContraparte",
			0 AS "IdOperadorCorredor",
			'' AS "IdTipoComprobante",
			'' AS "TipoComprobante",
			COALESCE(OPE."IdOperacion",0) AS "FolioComprobante",
			0 AS "ValorPresenteCompraInst",
			OPE."FechaVencimiento" AS "FechaVencimiento",
			--No se aplican comisiones (Directas desde Mesa)
			0 															AS "Comision",
			0 															AS "Derecho",
			0 															AS "Gasto",
			0 															AS "Impuesto",		
			'' AS "IdCondicionLiquidacion",
			0 AS "IdDocumentoRelacion",
			'' AS "FechaVencimientoPacto",
			0 AS "PrecioPlazo",
			0 AS "TasaPlazo",
			'' AS "IdMonedaPlazo",
			0 AS "TotalCompraVencimiento",
			0 AS "TotalVentaVencimiento",
			COALESCE(OPE."Custodia",'') AS "Custodia",
			'BCS' AS "IdBolsa",
			'' AS "IdBoveda",
			0 AS "CantidadGarantia",
			'' AS "NemoSVS",
			0 AS "ValorPresente",
			0 AS "UtilidadPerdida",
			'' AS "FechaOrigenPacto",
			COALESCE(CAST(OPE."TipoOperacion" AS  NVARCHAR(40)),'') AS "DescripcionOperacion",
			OPE."IdEstado" AS "IdEstadoFactura",
			COALESCE(OPE."IdTipoCanalIngreso",'') AS "Canal",
			0 AS "ValorIVA",
			CASE WHEN v_IdNormativa = 'NCG380Operc' AND OPE."TipoOperacion" IN ('COMPRA FORWARD','VENTA FORWARD') THEN OPE."FolioOperacion" 
			ELSE 0 END AS "FolioComprobanteAsociado",
			'' AS "ModalidadEjecucion",
			'' AS "EsFungible",
			0 AS "IdMovimientoRelacionado",
			'' AS "CodigoOperadorContraparte",
			'' AS "CodigoOperadorCorredor", 
			---ESPECIFICOS: MESA RF	
			'' AS "TipoOperacionSecundaria",
			0 AS "ValorRescateInicial",
			---ESPECIFICOS: SPOT
			COALESCE(OPE."IdMonedaSecundaria",'') AS "IdMonedaSecundaria",
			NPRO."IdNormativa",
			NPRO."IdTipoDocumento",
			NPRO."HIdTipoProducto",
			NPRO."HIdClasificacionTipoInstrumento",
			NPRO."HIdTipoInstrumento",
			NPRO."HIdTipoOperacion",
			NPRO."Uso",
			'N' as "EsEnRueda",
			'N'   AS "EsVentaCorta",
			---ESPECIFICOS: FORWARD
			OPE."ParidadCierre"										    AS "ParidadCierre",
			CASE WHEN "FechaVencimiento" > TO_DATE('0001-01-01','YYYY-MM-DD') THEN
				DAYS_BETWEEN("FechaOperacion", "FechaVencimiento")
				ELSE 0 END 														    AS "PlazoVencimiento",
			
			OPE."DocumentoIdentidad",	
			OPE."NombreCompleto",
			'MFW' AS "IdModulo",
			''    AS "FechaIngresoRegistro",
			0	  AS "IdOperacionOrigenReasignacion",
			'' 	  AS "IdSistemaBursatil",
			0 AS "INGCantidad",
			'' AS "InstrumentoNegociacion",
			0 AS "IdComprobanteReferencia",
			0 AS "IdComprobanteOperacion",
			'N' AS "EsVencimientoPacto",
			0 AS "SecuenciaMovimiento",
			COALESCE(OPE."FechaLiquidacion", '0001-01-01') AS "FechaLiquidacion"
		FROM
		"BSCL.Mesa.BD.Vistas::FWD.ConsultarOperaciones" as OPE
		INNER JOIN :v_tablaProductos NPRO 							 --PRODUCTO NORMATIVA
					  ON ((OPE."IdTipoProducto"= NPRO."IdTipoProducto")
					 AND (OPE."IdClasificacionTipoInstrumento"	= NPRO."IdClasificacionTipoInstrumento" OR NPRO."IdClasificacionTipoInstrumento"='') 
					 AND (OPE."IdTipoInstrumento" 				= NPRO."IdTipoInstrumento"  			OR NPRO."IdTipoInstrumento"='') 
					 AND (OPE."IdTipoOperacion" 				= NPRO."IdTipoOperacion" 				OR NPRO."IdTipoOperacion"=''))
		INNER JOIN :v_tablaFecha PFECH	
		ON ( 	(PFECH."FECHADESDE" IS NOT NULL  AND (TO_DATE(OPE."FechaMovimiento") BETWEEN TO_DATE(PFECH."FECHADESDE") AND TO_DATE(PFECH."FECHAHASTA")))		 
			 OR  											
				(PFECH."FECHADESDE" IS NULL 	 AND TO_DATE(OPE."FechaMovimiento") <= TO_DATE(PFECH."FECHAHASTA"))
			) 
			
		WHERE (OPE."IdCliente" = :v_IDCLIENTE OR :v_IDCLIENTE IS NULL)
		AND   (OPE."IdClasificacionTipoInstrumento" = :v_IDCLASIFICACION OR :v_IDCLASIFICACION IS NULL)
		AND  (OPE."IdTipoInstrumento" = :v_IDTIPOINSTRUMENTO OR :v_IDTIPOINSTRUMENTO IS NULL)
		AND OPE."IdEstadoCartera" NOT IN ('ANU','MOD')	
		UNION ALL SELECT * FROM :p_out_TablaSalida;	
	END IF; --- MESA FORWARD

		
	--FMM
	IF v_FMM ='S' OR (p_IdNormativa<>'' and v_FMM ='') THEN
	
		--Tabla de Salida
		p_out_TablaSalida=
		SELECT
		
			---CAMPOS COMUNES
			OPE."FechaCreacion" AS "FechaCreacion",
			COALESCE(TO_VARCHAR( OPE."FechaMovimiento", 'YYYY-MM-DD') || ' ' || TO_VARCHAR (OPE."FechaCreacion", 'HH24:MI:SS'), '1900-01-01 00:00:00') AS "FechaMovimiento",
			COALESCE(CAST(OPE."FolioContrato" AS  NVARCHAR(40)),'') AS "FolioTransaccion",
			'' 																  AS "IdOrden",
			'' 																  AS "IdComprobante",
			COALESCE(OPE."IdSolicitudAporteRescate",0) AS "IdOperacion",
			COALESCE(OPE."IdTipoOperacion",'') AS "IdTipoOperacion",
			COALESCE(OPE."IdCliente",0) AS "IdCliente",
			COALESCE(OPE."IdPortafolio",0) AS "IdPortafolio",
			COALESCE(OPE."IdClasificacionTipoInstrumento",'') AS "IdClasificacionTipoInstrumento",
			--COALESCE(OPE."Mercado",'') AS "Mercado",
			COALESCE(OPE."IdEmisor",'') AS "IdEmisor",
			COALESCE(OPE."Nemotecnico",'') AS "Nemotecnico",
			COALESCE(OPE."Cantidad",0) AS "Cantidad",
			COALESCE(OPE."Precio",0) AS "Precio",
			COALESCE(OPE."Monto",0) AS "Monto",
			0 AS "Tasa",
			COALESCE(OPE."IdMoneda",'') AS "IdMoneda",
			COALESCE(OPE."IdInstrumento",0)	AS "IdInstrumento",	
			COALESCE(OPE."IdTipoInstrumento",'') AS "IdTipoInstrumento", 
			COALESCE(OPE."IdTipoProducto",'') 	AS "IdTipoProducto",

			--(Compra/Venta)
			COALESCE(OPE."AccionOperacion",'')							AS "AccionOperacion",
			COALESCE(OPE."MontoCompra",0) AS "MontoCompra",
			COALESCE(OPE."MontoVenta",0) AS "MontoVenta",
			0 								AS "TasaCompra",
			0								AS "TasaVenta",
			0								AS "TotalCompra",
			0								AS "TotalVenta",
			
			---ESPECIFICOS: FACTURACION
			0 AS "IdTransaccion",
			COALESCE(OPE."FechaCreacion",'0001-01-01 00:00:00') AS "FechaTransaccion",
			'' AS "RelacionInstrumento",
			0 AS "IdContraparte",
			0 AS "IdOperadorContraparte",
			0 AS "IdOperadorCorredor",
			'' AS "IdTipoComprobante",
			'' AS "TipoComprobante",
			COALESCE(OPE."IdSolicitudAporteRescate",0)  AS "FolioComprobante",
			0 AS "ValorPresenteCompraInst",
			'' AS "FechaVencimiento",

			0 AS "Comision",
			0 AS "Derecho",
			0 AS "Gasto",
			0 AS "Impuesto",
			'' AS "IdCondicionLiquidacion",
			0 AS "IdDocumentoRelacion",
			'' AS "FechaVencimientoPacto",
			0 AS "PrecioPlazo",
			0 AS "TasaPlazo",
			'' AS "IdMonedaPlazo",
			0 AS "TotalCompraVencimiento",
			0 AS "TotalVentaVencimiento",
			'' AS "Custodia",
			COALESCE(OPE."Bolsa",'') AS "IdBolsa",
			'' AS "IdBoveda",
			0 AS "CantidadGarantia",
			'' AS "NemoSVS",
			0 AS "ValorPresente",
			0 AS "UtilidadPerdida",
			'' AS "FechaOrigenPacto",
			OPE."TipoOperacion" AS "DescripcionOperacion",
			CASE WHEN OPE."IdEstado" = 'CRT' THEN 'VIG' ELSE OPE."IdEstado" END AS "IdEstadoFactura",
			OPE."Canal" AS "Canal",
			0 AS "ValorIVA",
			0 AS "FolioComprobanteAsociado",
			'' AS "ModalidadEjecucion",
			'' AS "EsFungible",
			0 AS "IdMovimientoRelacionado",
			'' AS "CodigoOperadorContraparte",
			'' AS "CodigoOperadorCorredor", 
			---ESPECIFICOS: MESA RF	
			'' AS "TipoOperacionSecundaria",
			0 AS "ValorRescateInicial",
			---ESPECIFICOS: SPOT
			'' AS "IdMonedaSecundaria",
			NPRO."IdNormativa",
			NPRO."IdTipoDocumento",
			NPRO."HIdTipoProducto",
			NPRO."HIdClasificacionTipoInstrumento",
			NPRO."HIdTipoInstrumento",
			NPRO."HIdTipoOperacion",
			NPRO."Uso",
			'N' as "EsEnRueda",
			'N'   AS "EsVentaCorta",
			---ESPECIFICOS: FORWARD
			0														    AS "ParidadCierre",
			0														    AS "PlazoVencimiento",
			OPE."DocumentoIdentidad",	
			OPE."NombreCompleto",
			'FMM' AS "IdModulo",
			''    AS "FechaIngresoRegistro",
			0    AS "IdOperacionOrigenReasignacion",
			'' 	 AS "IdSistemaBursatil",
			0 AS "INGCantidad",
			'' AS "InstrumentoNegociacion",
			0 AS "IdComprobanteReferencia",
			0 AS "IdComprobanteOperacion",
			'N' AS "EsVencimientoPacto",
			0 AS "SecuenciaMovimiento",
			'0001-01-01' AS "FechaLiquidacion"
		FROM	"BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarOperaciones" OPE
		INNER JOIN :v_tablaProductos NPRO 							 --PRODUCTO NORMATIVA
					  ON ((OPE."IdTipoProducto"= NPRO."IdTipoProducto")
					 AND (OPE."IdClasificacionTipoInstrumento"	= NPRO."IdClasificacionTipoInstrumento" OR NPRO."IdClasificacionTipoInstrumento"='') 
					 AND (OPE."IdTipoInstrumento" 				= NPRO."IdTipoInstrumento"  			OR NPRO."IdTipoInstrumento"='') 
					 AND (OPE."IdTipoOperacion" 				= NPRO."IdTipoOperacion" 				OR NPRO."IdTipoOperacion"=''))
		INNER JOIN :v_tablaFecha PFECH	
		ON ( 	(PFECH."FECHADESDE" IS NOT NULL  AND (TO_DATE(OPE."FechaMovimiento") BETWEEN TO_DATE(PFECH."FECHADESDE") AND TO_DATE(PFECH."FECHAHASTA")))		 
			 OR  											
				(PFECH."FECHADESDE" IS NULL 	 AND TO_DATE(OPE."FechaMovimiento") <= TO_DATE(PFECH."FECHAHASTA"))
			)  	
			
		WHERE (OPE."IdCliente" = :v_IDCLIENTE OR :v_IDCLIENTE IS NULL)
		AND   (OPE."IdClasificacionTipoInstrumento" = :v_IDCLASIFICACION OR :v_IDCLASIFICACION IS NULL)
		AND  (OPE."IdTipoInstrumento" = :v_IDTIPOINSTRUMENTO OR :v_IDTIPOINSTRUMENTO IS NULL)
		
		
		UNION ALL SELECT * FROM :p_out_TablaSalida;	
				 
	END IF; --FMM
	
	
	--PRESTAMOS
	IF v_PRE ='S' OR (p_IdNormativa<>'' and v_PRE ='') THEN
	   
	   p_out_TablaSalida=
	   SELECT
			car."FechaIngresoRegistro" AS "FechaCreacion",
			COALESCE(TO_VARCHAR( car."INGFechaOperacion",'YYYY-MM-DD') || ' ' || TO_VARCHAR (car."FechaIngresoRegistro",'HH24:MI:SS'),'1900-01-01 00:00:00') AS "FechaMovimiento",
			CAST(CAR."FolioPrestamo" AS nvarchar) AS "FolioTransaccion",
			'0' AS "IdOrden",
			CAST(CAR."FolioComprobante" AS nvarchar) AS "IdComprobante",
			CASE WHEN v_IdNormativa = 'NCG380Operc' THEN car."IdCartera" 
				 ELSE 0 
		    END	AS "IdOperacion",
			COALESCE(car."IdTipoOperacion",	'') AS "IdTipoOperacion",
			COALESCE(car."IdCliente",0) AS "IdCliente",
			COALESCE(car."IdPortafolio",0) AS "IdPortafolio",
			car."IdClasificacionTipoInstrumento" AS "IdClasificacionTipoInstrumento",
			car."IdEmisorSubyacente" AS "IdEmisor",
			car."NemoSubyacente" AS "Nemotecnico",
			car."INGCantidad" AS "Cantidad",
			CAR."INGPrecioInstrumento" AS "Precio",
			0 AS "Monto",
			0 AS "Tasa",
			'' AS "IdMoneda",
			car."IdInstrumento" AS "IdInstrumento",
			COALESCE(car."IdTipoInstrumento",'') AS "IdTipoInstrumento",
			COALESCE(car."IdTipoProducto",'') AS "IdTipoProducto",
			'' AS "AccionOperacion",
			CAR."INGMontoTotalPrestamo" AS "MontoCompra",
			0 AS "MontoVenta",
			0 AS "TasaCompra",
			0 AS "TasaVenta",
			0 AS "TotalCompra",
			0 AS "TotalVenta",
			0 AS "IdTransaccion",
			'0001-01-01 00:00:00' AS "FechaTransaccion",
			'' AS "RelacionInstrumento",
			0 AS "IdContraparte",
			0 AS "IdOperadorContraparte",
			0 AS "IdOperadorCorredor",
			COALESCE(car."IdTipoComprobante",'') AS "IdTipoComprobante",
			COALESCE(tipocomprobante."Nombre",'') AS "TipoComprobante",
			COALESCE(CAR."FolioComprobante",0) AS "FolioComprobante",
			0 AS "ValorPresenteCompraInst",
			'0001-01-01 00:00:00' AS "FechaVencimiento",
			0 AS "Comision",
			0 AS "Derecho",
			0 AS "Gasto",
			0 AS "Impuesto",
			car."IdCondicionLiquidacion" AS "IdCondicionLiquidacion",
			0 AS "IdDocumentoRelacion",
			'0001-01-01' AS "FechaVencimientoPacto",
			0 AS "PrecioPlazo",
			0 AS "TasaPlazo",
			'' AS "IdMonedaPlazo",
			0 AS "TotalCompraVencimiento",
			0 AS "TotalVentaVencimiento",
			'' AS "Custodia",
			'' AS "IdBolsa",
			'' AS "IdBoveda",
			0 AS "CantidadGarantia",
			'' AS "NemoSVS",
			0 AS "ValorPresente",
			0 AS "UtilidadPerdida",
			'0001-01-01 00:00:00' AS "FechaOrigenPacto",
			SUBSTRING(car."NemotecnicoPrestamo",0,39)  AS "DescripcionOperacion",
			car."IdEstado" AS "IdEstadoFactura",
			'' AS "Canal",
			0 AS "ValorIVA",
			0 AS "FolioComprobanteAsociado",
			'' AS "ModalidadEjecucion",
			'' AS "EsFungible",
			0 AS "IdMovimientoRelacionado",
			'' AS "CodigoOperadorContraparte",
			'' AS "CodigoOperadorCorredor",
			'' AS "TipoOperacionSecundaria",
			0 AS "ValorRescateInicial",
			'' AS "IdMonedaSecundaria",
			'' AS "IdNormativa",
			'' AS "IdTipoDocumento",
			'' AS "HIdTipoProducto",
			'' AS "HIdClasificacionTipoInstrumento",
			'' AS "HIdTipoInstrumento",
			'' AS "HIdTipoOperacion",
			'' AS "Uso",
			'' AS "EsEnRueda",
			'' AS "EsVentaCorta",
			0 AS "ParidadCierre",
			0 AS "PlazoVencimiento",
			'' AS "DocumentoIdentidad",
			'' AS "NombreCompleto",
			car."IdModulo" AS "IdModulo",
			car."FechaIngresoRegistro" AS "FechaIngresoRegistro",
			0 AS "IdOperacionOrigenReasignacion",
			'' AS "IdSistemaBursatil",
			0 AS "INGCantidad",
			'' AS "InstrumentoNegociacion",
			0 AS "IdComprobanteReferencia",
			0 AS "IdComprobanteOperacion",
			'N' AS "EsVencimientoPacto",
			0 AS "SecuenciaMovimiento",
			'0001-01-01' AS "FechaLiquidacion"
		FROM "BSCL.Custodia.BD.Vistas::PRT.ConsultarResumenPrestamoVigente" CAR	
		LEFT OUTER JOIN "BSCL.Facturacion.BD::FAC.TipoComprobante" AS tipocomprobante ON tipocomprobante."IdTipoComprobante" = CAR."IdTipoComprobante"	
		INNER JOIN :v_tablaFecha PFECH
			ON ( 	(PFECH."FECHADESDE" IS NOT NULL AND PFECH."CLASEFECHA" ='MOVIMIENTO' AND (TO_DATE(car."INGFechaOperacion") BETWEEN TO_DATE(PFECH."FECHADESDE") AND TO_DATE(PFECH."FECHAHASTA")) )	 
				 OR  											
					(PFECH."FECHADESDE" IS NULL 	AND	PFECH."CLASEFECHA" ='MOVIMIENTO' AND TO_DATE(car."INGFechaOperacion") <= TO_DATE(PFECH."FECHAHASTA"))
				)
	   
		UNION ALL 
		
		SELECT
			car."FechaIngresoRegistro" AS "FechaCreacion",
			COALESCE(TO_VARCHAR( car."EGRFechaOperacion",'YYYY-MM-DD') || ' ' || TO_VARCHAR (car."FechaIngresoRegistro",'HH24:MI:SS'),'1900-01-01 00:00:00') AS "FechaMovimiento",
			CAST(CAR."FolioPrestamo" AS nvarchar) AS "FolioTransaccion",
			'0' AS "IdOrden",
			CAST(CAR."FolioComprobante" AS nvarchar) AS "IdComprobante",
			CASE WHEN v_IdNormativa = 'NCG380Operc' THEN car."IdCartera" 
				 ELSE 0 
		    END	AS "IdOperacion",
			COALESCE(car."IdTipoOperacion",	'') AS "IdTipoOperacion",
			COALESCE(car."IdCliente",0) AS "IdCliente",
			COALESCE(car."IdPortafolio",0) AS "IdPortafolio",
			car."IdClasificacionTipoInstrumento" AS "IdClasificacionTipoInstrumento",
			car."IdEmisorSubyacente" AS "IdEmisor",
			car."NemoSubyacente" AS "Nemotecnico",
			car."EGRCantidad" AS "Cantidad",
			CAR."EGRPrecio" AS "Precio",
			0 AS "Monto",
			0 AS "Tasa",
			'' AS "IdMoneda",
			0 AS "IdInstrumento",
			COALESCE(car."IdTipoInstrumento",'') AS "IdTipoInstrumento",
			COALESCE(car."IdTipoProducto",'') AS "IdTipoProducto",
			'' AS "AccionOperacion",
			CAR."EGRMonto" AS "MontoCompra",
			0 AS "MontoVenta",
			0 AS "TasaCompra",
			0 AS "TasaVenta",
			0 AS "TotalCompra",
			0 AS "TotalVenta",
			0 AS "IdTransaccion",
			'0001-01-01 00:00:00' AS "FechaTransaccion",
			'' AS "RelacionInstrumento",
			0 AS "IdContraparte",
			0 AS "IdOperadorContraparte",
			0 AS "IdOperadorCorredor",
			COALESCE(car."IdTipoComprobante",'') AS "IdTipoComprobante",
			COALESCE(tipocomprobante."Nombre",'') AS "TipoComprobante",
			COALESCE(CAR."FolioComprobante",0) AS "FolioComprobante",
			0 AS "ValorPresenteCompraInst",
			'0001-01-01 00:00:00' AS "FechaVencimiento",
			0 AS "Comision",
			0 AS "Derecho",
			0 AS "Gasto",
			0 AS "Impuesto",
			car."IdCondicionLiquidacion" AS "IdCondicionLiquidacion",
			0 AS "IdDocumentoRelacion",
			'0001-01-01' AS "FechaVencimientoPacto",
			0 AS "PrecioPlazo",
			0 AS "TasaPlazo",
			'' AS "IdMonedaPlazo",
			0 AS "TotalCompraVencimiento",
			0 AS "TotalVentaVencimiento",
			'' AS "Custodia",
			'' AS "IdBolsa",
			'' AS "IdBoveda",
			0 AS "CantidadGarantia",
			'' AS "NemoSVS",
			0 AS "ValorPresente",
			0 AS "UtilidadPerdida",
			'0001-01-01 00:00:00' AS "FechaOrigenPacto",
			CASE WHEN CAR."IdEstado" = 'ANT' THEN SUBSTRING('ANT. '|| car."NombreTipoPrestamo",0,39)
				 WHEN CAR."IdEstado" = 'VCD' THEN SUBSTRING('VENC. '|| car."NombreTipoPrestamo",0,39)
				 END
			AS "DescripcionOperacion",
			car."IdEstado" AS "IdEstadoFactura",
			'' AS "Canal",
			0 AS "ValorIVA",
			0 AS "FolioComprobanteAsociado",
			'' AS "ModalidadEjecucion",
			'' AS "EsFungible",
			0 AS "IdMovimientoRelacionado",
			'' AS "CodigoOperadorContraparte",
			'' AS "CodigoOperadorCorredor",
			'' AS "TipoOperacionSecundaria",
			0 AS "ValorRescateInicial",
			'' AS "IdMonedaSecundaria",
			'' AS "IdNormativa",
			'' AS "IdTipoDocumento",
			'' AS "HIdTipoProducto",
			'' AS "HIdClasificacionTipoInstrumento",
			'' AS "HIdTipoInstrumento",
			'' AS "HIdTipoOperacion",
			'' AS "Uso",
			'' AS "EsEnRueda",
			'' AS "EsVentaCorta",
			0 AS "ParidadCierre",
			0 AS "PlazoVencimiento",
			'' AS "DocumentoIdentidad",
			'' AS "NombreCompleto",
			car."IdModulo" AS "IdModulo",
			car."FechaIngresoRegistro" AS "FechaIngresoRegistro",
			0 AS "IdOperacionOrigenReasignacion",
			'' AS "IdSistemaBursatil",
			0 AS "INGCantidad",
			'' AS "InstrumentoNegociacion",
			0 AS "IdComprobanteReferencia",
			0 AS "IdComprobanteOperacion",
			'' AS "EsVencimientoPacto",
			0 AS "SecuenciaMovimiento",
			'0001-01-01' AS "FechaLiquidacion"
		FROM "BSCL.Custodia.BD.Vistas::PRT.ConsultarResumenPrestamoHistorico" AS CAR
		LEFT OUTER JOIN "BSCL.Facturacion.BD::FAC.TipoComprobante" AS tipocomprobante ON tipocomprobante."IdTipoComprobante" = CAR."IdTipoComprobante"	
		INNER JOIN :v_tablaFecha PFECH
			ON ( 	(PFECH."FECHADESDE" IS NOT NULL AND PFECH."CLASEFECHA" ='MOVIMIENTO' AND (TO_DATE(car."EGRFechaOperacion") BETWEEN TO_DATE(PFECH."FECHADESDE") AND TO_DATE(PFECH."FECHAHASTA")) )		 
				 OR  											
					(PFECH."FECHADESDE" IS NULL 	AND	PFECH."CLASEFECHA" ='MOVIMIENTO' AND TO_DATE(car."EGRFechaOperacion") <= TO_DATE(PFECH."FECHAHASTA"))
				)

		UNION ALL SELECT * FROM :p_out_TablaSalida;
	   	
	END IF; --PRESTAMOS

	--EVENTOS DE CAPITAL
	IF v_EVC ='S' OR (p_IdNormativa <> '' and v_EVC = '') THEN
		p_out_TablaSalida =
		SELECT
			COALESCE(OPE."FechaIngresoRegistro",EVCLI."FechaHoraIngresoRegistro",'')	AS "FechaCreacion",
			COALESCE(TO_VARCHAR( OPE."FechaTransaccion", 'YYYY-MM-DD') || ' ' || TO_VARCHAR (OPE."FechaIngresoRegistro", 'HH24:MI:SS'),TO_VARCHAR(EVCLI."FechaPago", 'YYYY-MM-DD') || ' ' || TO_VARCHAR (EVCLI."FechaHoraIngresoRegistro", 'HH24:MI:SS'), '1900-01-01 00:00:00') AS "FechaMovimiento",
			COALESCE(CAST(OPE."FolioTransaccion"AS  NVARCHAR(40)),'') AS "FolioTransaccion",
			COALESCE(CAST(OPE."IdOrden"AS  NVARCHAR(40)),'') AS "IdOrden",
			COALESCE(CAST(OPE."IdComprobante"AS  NVARCHAR(40)),'0') AS "IdComprobante",
			COALESCE(OPE."IdOperacion",0) AS "IdOperacion",
			COALESCE(OPE."IdTipoOperacion",TEC."IdTipoOperacion",'') AS "IdTipoOperacion",
			COALESCE(OPE."IdCliente", SOC."IdCliente", 0) AS "IdCliente",
			COALESCE(OPE."IdPortafolio",POR."IdPortafolio",0) AS "IdPortafolio",
			COALESCE(OPE."IdClasificacionTipoInstrumento",EVCLI."IdClasificacionTipoInstrumento",'') AS "IdClasificacionTipoInstrumento",
			COALESCE(OPE."IdEmisor",INS."IdEmisor",'') AS "IdEmisor",
			COALESCE(OPE."Nemotecnico",INS."Nemotecnico",'') AS "Nemotecnico",
			CASE 
				WHEN EVCLI."IdTipoEventoCapital" = 'RC' THEN
					EVCLI."CantidadDerecho"
				ELSE COALESCE(OPE."Cantidad",EVCLI."CantidadNueva", 0)
			END AS "Cantidad",
			COALESCE(OPE."Precio",EVCLI."Precio",0) AS "Precio",
			COALESCE(OPE."MontoMovimientoComprobante",EVCLI."Total",0) AS "Monto",
			COALESCE(OPE."Tasa",EV."TasaEfectiva",0) AS "Tasa",
			COALESCE(OPE."IdMoneda",EV."IdMoneda",'') AS "IdMoneda",
			COALESCE(OPE."IdInstrumento",INS."IdInstrumento",0) AS "IdInstrumento",	
			COALESCE(OPE."IdTipoInstrumento",EVCLI."IdTipoInstrumento",'') AS "IdTipoInstrumento", 
			COALESCE(OPE."IdTipoProducto",'') AS "IdTipoProducto",
			COALESCE(OPE."AccionOperacion",'') AS "AccionOperacion",
			COALESCE(OPE."MontoCompra",0) AS "MontoCompra",
			COALESCE(OPE."MontoVenta",0) AS "MontoVenta",
			COALESCE(OPE."TasaCompra",0) AS "TasaCompra",
			COALESCE(OPE."TasaVenta",0) AS "TasaVenta",
			COALESCE(OPE."TotalCompra",0) AS "TotalCompra",
			COALESCE(OPE."TotalVenta",0) AS "TotalVenta",
			COALESCE(OPE."IdTransaccion",0) AS "IdTransaccion",
			COALESCE(OPE."FechaTransaccion",'0001-01-01 00:00:00') AS "FechaTransaccion",
			COALESCE(OPE."RelacionInstrumento",'') AS "RelacionInstrumento",
			COALESCE(OPE."IdContraparte", 0) AS "IdContraparte",
			COALESCE(OPE."IdOperadorContraparte",0) AS "IdOperadorContraparte",
			COALESCE(OPE."IdOperadorCorredor",0) AS "IdOperadorCorredor",
			COALESCE(OPE."IdTipoComprobante",'') AS "IdTipoComprobante",
			COALESCE(tipocomprobante."Nombre",'') AS "TipoComprobante",
			COALESCE(OPE."FolioComprobante",0) AS "FolioComprobante",
			COALESCE(OPE."ValorPresenteCompraInst",0) AS "ValorPresenteCompraInst",
			COALESCE(OPE."FechaVencimiento",'0001-01-01 00:00:00') AS "FechaVencimiento",
			COALESCE(OPE."Comision",0) AS "Comision",
			COALESCE(OPE."Derecho",0) AS "Derecho",
			COALESCE(OPE."Gasto",0) AS "Gasto",
			COALESCE(OPE."Impuesto",0) AS "Impuesto",
			COALESCE(OPE."IdCondicionLiquidacion",'') AS "IdCondicionLiquidacion",
			COALESCE(OPE."IdDocumentoRelacion",0) AS "IdDocumentoRelacion",
			COALESCE(OPE."FechaVencimientoPacto",'0001-01-01') AS "FechaVencimientoPacto",
			COALESCE(OPE."PrecioPlazo",0) AS "PrecioPlazo",
			COALESCE(OPE."TasaPlazo",0) AS "TasaPlazo",
			COALESCE(OPE."IdMonedaPlazo",'') AS "IdMonedaPlazo",
			COALESCE(OPE."TotalCompraVencimiento",0) AS "TotalCompraVencimiento",
			COALESCE(OPE."TotalVentaVencimiento",0) AS "TotalVentaVencimiento",
			COALESCE(OPE."Custodia",'') AS "Custodia",
			COALESCE(OPE."IdBolsa",EVCLI."IdBolsa" ,'') AS "IdBolsa",
			COALESCE(OPE."IdBoveda",EVCLI."IdBoveda" ,'') AS "IdBoveda",
			COALESCE(OPE."CantidadGarantia",0) AS "CantidadGarantia",
			COALESCE(OPE."NemoSVS",'') AS "NemoSVS",
			COALESCE(OPE."ValorPresente",0) AS "ValorPresente",
			COALESCE(OPE."UtilidadPerdida",0) AS "UtilidadPerdida",
			COALESCE(OPE."FechaOrigenPacto",'0001-01-01 00:00:00') AS "FechaOrigenPacto",
			COALESCE(CAST(OPE."DescripcionOperacion"AS  NVARCHAR(40)),CAST(TEC."Nombre" AS  NVARCHAR(40)),'') AS "DescripcionOperacion",
			COALESCE(OPE."IdEstadoFactura",'') AS "IdEstadoFactura",
			COALESCE(OPE."Canal",'') AS "Canal",
			COALESCE(OPE."ValorIVA",0) AS "ValorIVA",
			COALESCE(OPE."FolioComprobanteAsociado",0) AS "FolioComprobanteAsociado",
			COALESCE(OPE."ModalidadEjecucion", '') AS "ModalidadEjecucion",
			COALESCE(OPE."EsFungible",'') AS "EsFungible",
			COALESCE(OPE."IdMovimientoRelacionado", 0) AS "IdMovimientoRelacionado",
			OPE."CodigoOperadorContraparte" AS "CodigoOperadorContraparte",
			OPE."CodigoOperadorCorredor" AS "CodigoOperadorCorredor", 
			'' AS "TipoOperacionSecundaria",
			0 AS "ValorRescateInicial",
			'' AS "IdMonedaSecundaria",
			NPRO."IdNormativa",
			NPRO."IdTipoDocumento",
			NPRO."HIdTipoProducto",
			NPRO."HIdClasificacionTipoInstrumento",
			NPRO."HIdTipoInstrumento",
			NPRO."HIdTipoOperacion",
			NPRO."Uso",
			OPE."EsEnRueda" 	AS "EsEnRueda",
			OPE."EsVentaCorta" 	AS "EsVentaCorta",
			0 AS "ParidadCierre",
			0 AS "PlazoVencimiento",
			COALESCE(OPE."DocumentoIdentidad", EVCLI."DocumentoIdentidad", '') AS "DocumentoIdentidad",
			COALESCE(OPE."NombreCompleto", EVCLI."NombreCliente" , '') AS "NombreCompleto",
			'EVC' AS "IdModulo",
			'' AS "FechaIngresoRegistro",
			0 AS "IdOperacionOrigenReasignacion",
			COALESCE(OPE."IdSistemaBursatil",'') AS "IdSistemaBursatil",
			CASE 
				WHEN EVCLI."IdTipoEventoCapital" = 'RC' THEN
					EVCLI."CantidadDerecho"
				ELSE COALESCE(OPE."Cantidad",EVCLI."CantidadNueva", 0)
			END AS "INGCantidad",
			OPE."InstrumentoNegociacion" AS "InstrumentoNegociacion",
			COALESCE(OPE."IdComprobanteReferencia", 0) AS "IdComprobanteReferencia",
			COALESCE(OPE."IdComprobante", 0) AS "IdComprobanteOperacion",
			'N' AS "EsVencimientoPacto",
			COALESCE(OPE."SecuenciaMovimiento",0) AS "SecuenciaMovimiento",
			'0001-01-01' AS "FechaLiquidacion"
		FROM
			"BSCL.Custodia.BD::CUS.EventoCapitalCliente" EVCLI
		INNER JOIN "BSCL.Custodia.BD::CUS.TipoEventoCapital" TEC ON TEC."IdTipoEventoCapital" = EVCLI."IdTipoEventoCapital" 
		INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" POR ON POR."IdPortafolio" = EVCLI."IdPortafolio" 
		INNER JOIN "BSCL.Plataforma.BD::INS.Instrumento" INS ON INS."IdInstrumento" = EVCLI."IdInstrumento" 
		INNER JOIN "BSCL.Custodia.BD::CUS.EventoCapital" EV ON EV."IdEventoCapital" = EVCLI."IdEventoCapital" 
		INNER JOIN "BSCL.Custodia.BD::CUS.TipoEventoCapital" TEV ON TEV."IdTipoEventoCapital" = EV."IdTipoEventoCapital" 
		INNER JOIN "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" SOC ON SOC."IdSocioDeNegocio" = EVCLI."IdSocioNegocio" 
		INNER JOIN "BSCL.SocioNegocio.BD::PER.Persona" PER ON PER."IdPersona" = SOC."IdPersona" 
		LEFT JOIN "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" SDF ON SDF."IdInstrumento" = INS."IdInstrumento" 
		LEFT JOIN "BSCL.Facturacion.BD.Vistas::ConsultarOperacionesTotalizadas" OPE 
			ON OPE."IdComprobante" = EVCLI."IdComprobanteFac" 
			AND OPE."Cantidad" = EVCLI."CantidadNueva"
			AND OPE."IdMovimientoComprobante" = EVCLI."IdMovimientoComprobanteFac"
		LEFT OUTER JOIN "BSCL.Facturacion.BD::FAC.TipoComprobante" AS tipocomprobante ON tipocomprobante."IdTipoComprobante" = OPE."IdTipoComprobante"
		LEFT JOIN :v_tablaProductos NPRO ON (
			(NPRO."IdTipoProducto" = EVCLI."IdTipoEventoCapital")
			AND (EVCLI."IdClasificacionTipoInstrumento" = NPRO."IdClasificacionTipoInstrumento")
			AND (EVCLI."IdTipoInstrumento" = NPRO."IdTipoInstrumento")
		)
		INNER JOIN :v_tablaFecha PFECH ON (
			(PFECH."FECHADESDE" IS NOT NULL AND PFECH."CLASEFECHA" ='MOVIMIENTO' AND (TO_DATE(EVCLI."FechaPago") BETWEEN TO_DATE(PFECH."FECHADESDE") AND TO_DATE(PFECH."FECHAHASTA")))
			OR
			(PFECH."FECHADESDE" IS NULL 	AND	PFECH."CLASEFECHA" ='MOVIMIENTO' AND TO_DATE(EVCLI."FechaPago") <= TO_DATE(PFECH."FECHAHASTA"))
		)
		WHERE (SOC."IdCliente" = :v_IDCLIENTE OR :v_IDCLIENTE IS NULL)
		AND   (EVCLI."IdClasificacionTipoInstrumento" = :v_IDCLASIFICACION OR :v_IDCLASIFICACION IS NULL)
		AND   (EVCLI."IdTipoInstrumento" = :v_IDTIPOINSTRUMENTO OR :v_IDTIPOINSTRUMENTO IS NULL)
		AND EV."IdTipoEventoCapital" IN ('EL', 'DA')
		AND EVCLI."IdEstado" = 'CER'
		UNION ALL 
		SELECT * FROM :p_out_TablaSalida;	
	END IF; --EVENTOS DE CAPITAL
		
END;