SET SCHEMA "BSCL";
GO
DROP PROCEDURE "BSCL.Normativa.BD.Servicios.NOR::LibroOperaciones";
GO
CREATE PROCEDURE 
--
--  Proposito: Servicio entrega Tabla con Libro de Operaciones .
--  Autor: CESPINOSA - BCS
--  Fecha Creacion: 29/08/2016

"BSCL.Normativa.BD.Servicios.NOR::LibroOperaciones" (
	--Parametros de Entrada Comunes
	IN p_IdEmpresa		BIGINT,
	--IN p_IdUsuario		BIGINT,
	--IN p_IdModulo		NVARCHAR(3)
	
	--Parametros de Entrada Propios
	IN p_FechaDesde		NVARCHAR(10) DEFAULT NULL,
	IN p_FechaHasta		NVARCHAR(10) DEFAULT NULL,
	
	IN p_IdCliente		BIGINT DEFAULT NULL,
	IN p_IdMoneda		NVARCHAR(3) DEFAULT NULL,
	IN p_IdBolsa		NVARCHAR(4) DEFAULT NULL,
	IN p_IdClasificacion 	NVARCHAR(3) DEFAULT NULL,
	IN p_IdTipoInstrumento 	NVARCHAR(3) DEFAULT NULL,
	
	IN p_IdInstrumento	BIGINT DEFAULT NULL,
	IN p_IdTipOperacion NVARCHAR(6) DEFAULT NULL,
	
	--Salidas
	out p_out_LibroOperaciones "BSCL.Normativa.BD::NOR.SALIDA_LIBROOPERACIONES"

	,IN p_MostrarPactos 		NVARCHAR(1) DEFAULT 'S'
	,IN p_MostrarEVC            NVARCHAR(1) DEFAULT 'S'
)
LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
    READS SQL DATA WITH RESULT VIEW "BSCL.Normativa.BD.Vistas::NOR.SALIDA_LIBROOPERACIONES" AS
BEGIN
	
	
	DECLARE v_IdPersonaCtaPropia						 BIGINT:= 0;
	DECLARE	v_IdClienteCtaPropia 						 BIGINT:=0;
 	DECLARE v_CodigoCtaPropia 							 NVARCHAR(3):= '';
 	DECLARE v_DocumentoIdentidadCtaPropia  				 NVARCHAR(40) DEFAULT '';
 	DECLARE v_NombreCtaPropia  						 	 NVARCHAR(200) DEFAULT '';
 		
 		
 		
	v_TablaModulo = 
	Select 'FAC' as "IdModulo" from dummy
	union all 
	Select 'MRF' as "IdModulo" from dummy
	union all
	Select 'MFW' as "IdModulo" from dummy
	union all 	
	Select 'CUS' as "IdModulo" from dummy
	union all 
	Select 'FMM' as "IdModulo" from dummy;
	
	v_registroParametro = SELECT 'FECHADESDE' AS "Codigo", p_FechaDesde AS "Valor" FROM DUMMY
				UNION ALL SELECT 'FECHAHASTA' AS "Codigo", p_FechaHasta AS "Valor" FROM DUMMY;

	IF p_MostrarEVC = 'S' THEN 
		v_TablaModulo = 
		SELECT "IdModulo" FROM :v_TablaModulo
		UNION ALL 
		Select 'EVC' as "IdModulo" from dummy;
	END IF;


	CALL "BSCL.Normativa.BD.Servicios.NOR::ConsultarOperaciones"
	(
		p_IdEmpresa			=> p_IdEmpresa,
		p_IdNormativa		=> 'NCG380Ope',
		p_tablaModulo		=> :v_TablaModulo,
		p_registroParametro => :v_registroParametro,
		p_nominal => 'S',
		p_out_TablaSalida	=> v_SalidaConsultarOperaciones
	);



			---Valores de la cuenta propia	
	SELECT TOP 1 CLI."IdCliente", CLI."DocumentoIdentidad",COR."CodigoCorredora",  COD."NombreCorto" INTO v_IdClienteCtaPropia,v_DocumentoIdentidadCtaPropia,v_CodigoCtaPropia, v_NombreCtaPropia
	FROM "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" CLI
	--INNER JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa" EMP ON (CLI."IdPersona"=EMP."IdPersona")
	INNER JOIN "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio" as SOC ON CLI."IdPersona" =SOC."IdPersona"		
	INNER JOIN "BSCL.Plataforma.BD::PLA.Empresa" EMP ON (SOC."IdSocioDeNegocio"=EMP."IdSocioDeNegocio")	
	INNER JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarCorredora" COD ON COD."IdPersona" =SOC."IdPersona"
	INNER JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarCorredoraCodigos" COR ON COD."IdCorredora" =COR."IdCorredora"
	WHERE EMP."IdEmpresa" = p_IdEmpresa
	AND CLI."IdEstado"='ACT';
		
		
	p_out_LibroOperaciones =     
	           Select 				---CAMPOS COMUNES
				--TO_DATE ('2016-01-01') AS "FechaMovimiento",
				OPE."FechaMovimiento" AS "FechaMovimiento",
				OPE."FolioTransaccion" AS "FolioTransaccion",
				OPE."IdOrden" AS "IdOrden",
				CASE WHEN OPE."IdTipoInstrumento" = 'PAC' THEN CAST (OPE."IdOperacion" AS nvarchar(40) ) 
					 ELSE  OPE."IdComprobante" 
				END AS "IdComprobante",
				OPE."IdOperacion" AS "IdOperacion",
				OPE."IdCliente" AS "IdCliente",
				PER."DocumentoIdentidad" AS "RutCliente",
				COALESCE(NULLIF(TRIM(POR."Alias"), ''), PER."NombreCompleto") AS "NombreCliente",
				OPE."IdPortafolio" AS "IdPortafolio",
				POR."CodigoPortafolio",
				CASE WHEN AREA."Descripcion" IS NULL THEN 'POR DEFINIR' ELSE AREA."Descripcion" END AS "AreaCliente",
				OPE."IdClasificacionTipoInstrumento",
				OPE."IdTipoInstrumento",
                OPE."IdTipoOperacion",               
				COALESCE(ClasInst."Nombre",'') 					AS "ClasificacionTipoInstrumento",
				COALESCE(TipoInstrumento."Descripcion",'') 		AS "TipoInstrumento",
                COALESCE(TipoOperacion."Nombre",'')             AS "TipoOperacion",	
				OPE."AccionOperacion",
				(CASE COALESCE(RELNOR."RELACION",'NOVINCULADO') 
						WHEN 'NOVINCULADO' THEN 'NO'
						WHEN 'NOINFORMAR'  THEN 'NO'
						ELSE 'SI'
				END )AS "Relacion",
				COALESCE(RELNOR."RELACION",'NOVINCULADO') AS "CodigoTipoRelacion",
				COALESCE(RELNOR."NombreValorIndicador",'NO VINCULADO') AS "TipoRelacion",
				CASE WHEN OPE."IdTipoInstrumento" =  'PAC' THEN RES."IdEmisor"
					ELSE OPE."IdEmisor" 
				END AS "IdEmisor",
				CASE WHEN OPE."IdTipoInstrumento" = 'SPT' THEN 
							CASE COM."IdMonedaSpot"
								WHEN 'BRL' THEN 'REAL'
								WHEN 'CAD' THEN 'DOLAR'
								WHEN 'USD' THEN 'DOLAR'
								WHEN 'PEN' THEN 'SOL'
								WHEN 'AUD' THEN 'DOLAR'
								WHEN 'RMB' THEN 'RENMINBI'
								WHEN 'DKK' THEN 'CORONA'
								WHEN 'JPY' THEN 'YEN'
								WHEN 'CHF' THEN 'FRANCO'
								WHEN 'NOK' THEN 'CORONA'
								WHEN 'NZD' THEN 'DOLAR'
								WHEN 'GBP' THEN 'LIBRA'
								WHEN 'SEK' THEN 'CORONA'
								WHEN 'HKD' THEN 'DOLAR'
								WHEN 'MXN' THEN 'PESO'
								WHEN 'EUR' THEN 'EURO'
								WHEN 'CLP' THEN 'DOLAR'
								WHEN 'COP' THEN 'PESO'
								WHEN 'CUP' THEN 'PESO'
								WHEN 'UF'  THEN 'UNIDAD'
								WHEN 'DO'  THEN 'DOLAR'			
							ELSE 'DOLAR' END
			    WHEN OPE."IdTipoInstrumento" = 'PAC' THEN
			    	CASE
						WHEN carr."NemoSVS" != '' OR carr."NemoSVS" IS NOT NULL  THEN carr."NemoSVS"
					ELSE res."Nemotecnico" END 
				ELSE OPE."Nemotecnico" END AS "Nemotecnico",
				CASE WHEN OPE."IdTipoInstrumento" = 'PAC' THEN 
					CASE WHEN RES."IdClasificacionTipoInstrumento" = 'IF'THEN RES."ValorRescateInicial" 
						ELSE RES."CantidadInicial"
					END
					ELSE OPE."Cantidad" 
				END AS "Cantidad",
				OPE."Precio" AS "Precio",
				OPE."Tasa" AS "Tasa",
				OPE."IdMoneda" AS "IdMoneda",
				OPE."IdMonedaSecundaria" AS "IdMonedaSecundaria",
				---ESPECIFICOS: FACTURACION
				OPE."TasaCompra" AS "TasaCompra",
				OPE."RelacionInstrumento" AS "RelacionInstrumento",
				OPE."IdOperadorContraparte" AS "IdOperadorContraparte",
				OPE."IdOperadorCorredor" AS "IdOperadorCorredor",
				OPE."IdTipoComprobante" AS "IdTipoComprobante",
				TPCOM."Nombre" AS "NombreTipoComprobante",
				OPE."FolioComprobante" AS "FolioComprobante",
				CASE WHEN OPE."IdTipoInstrumento" =  'PAC' THEN RES."MontoLiquidacionOperacion"
				    ELSE OPE."ValorPresenteCompraInst"
				END  AS "ValorPresenteCompraInst",
				OPE."FechaVencimiento" AS "FechaVencimiento",
				CASE WHEN OPE."IdTipoInstrumento" =  'PAC' THEN RES."EgrMontoValorizado"
				    ELSE OPE."MontoCompra" 
				END AS "MontoCompra",
				CASE WHEN OPE."IdTipoInstrumento" =  'PAC' THEN RES."IngMontoValorizado"
				    ELSE OPE."MontoVenta" 
		    	END AS "MontoVenta",
				OPE."Comision" AS "Comision",
				OPE."Derecho" AS "Derecho",
				OPE."Gasto" AS "CargoFijo",
				OPE."Impuesto" AS "IVA",
				OPE."TotalCompra",
				OPE."TotalVenta",
				OPE."IdCondicionLiquidacion" AS "IdCondicionLiquidacion",
				OPE."IdDocumentoRelacion" AS "IdDocumentoRelacion",
				OPE."FechaVencimientoPacto" AS "FechaVencimientoPacto",
				CASE WHEN OPE."IdTipoInstrumento" =  'PAC' THEN RES."Tasa"
				    ELSE OPE."PrecioPlazo" 
				END AS "PrecioPlazo",
				OPE."TasaPlazo" AS "TasaPlazo",
				OPE."IdMonedaPlazo" AS "IdMonedaPlazo",
				OPE."TotalCompraVencimiento" AS "TotalCompraVencimiento",
				CASE WHEN OPE."IdTipoInstrumento" =  'PAC' THEN RES."INGMontoAPlazo"
				    ELSE OPE."TotalVentaVencimiento" 
				END AS "TotalVentaVencimiento",
				OPE."Custodia" AS "Custodia",
				OPE."IdBoveda" AS "IdBoveda",
				OPE."CantidadGarantia" AS "CantidadGarantia",
				OPE."NemoSVS" AS "NemoSVS",
				CASE WHEN OPE."IdTipoInstrumento" =  'PAC' THEN RES."Tasa"
				    ELSE OPE."TasaVenta" 
				END  AS "TasaVenta",
				CASE WHEN OPE."IdTipoInstrumento" =  'PAC' THEN RES."MontoLiquidacionOperacion"
				    ELSE OPE."ValorPresente" 
				END AS "ValorPresente",
				OPE."UtilidadPerdida" AS "UtilidadPerdida",
				OPE."FechaOrigenPacto" AS "FechaOrigenPacto",
				OPE."IdEstadoFactura" AS "IdEstadoFactura",
				OPE."Canal" AS "Canal",
				OPE."ValorIVA" AS "ValorIVA",
				OPE."FolioComprobanteAsociado" AS "FolioComprobanteAsociado",
				/*
				CASE WHEN  OPE."AccionOperacion" ='COMPRA' THEN COALESCE(CNC."NombreCorto",'') ELSE v_NombreCtaPropia END AS "NombreComprador",
				
				CASE WHEN  OPE."AccionOperacion" ='VENTA'  THEN COALESCE(CNC."NombreCorto",'') ELSE v_NombreCtaPropia END AS "NombreVendedor",
				
				CASE WHEN  OPE."AccionOperacion" ='COMPRA' THEN COALESCE(CNC."DocumentoIdentidad",'') ELSE v_DocumentoIdentidadCtaPropia END AS "RutComprador",
				
				CASE WHEN  OPE."AccionOperacion" ='VENTA'  THEN COALESCE(CNC."DocumentoIdentidad",'') ELSE v_DocumentoIdentidadCtaPropia END AS "RutVendedor",
				*/
				COALESCE(CNC."NombreCorto",v_NombreCtaPropia) AS "NombreComprador",
				'' AS "NombreVendedor",
				COALESCE(CNC."DocumentoIdentidad",v_DocumentoIdentidadCtaPropia) AS "RutComprador",
				''AS "RutVendedor",
				OPE."IdBolsa",
				CASE WHEN 	OPE."IdCliente"=v_IdClienteCtaPropia 					THEN '1'
	  				      ELSE (
	  				      		CASE  WHEN	COALESCE(RELNOR."RELACION",'NOVINCULADO')='NOVINCULADO' THEN '2'
	  				      			  WHEN	COALESCE(RELNOR."RELACION",'NOVINCULADO')<>'NOVINCULADO' AND COALESCE(IND."ValorIndicador",'N')='N' THEN '3'
	  				      			  WHEN	COALESCE(RELNOR."RELACION",'NOVINCULADO')<>'NOVINCULADO' AND COALESCE(IND."ValorIndicador",'N')='S'	THEN '4'
	  				      		END
	  				      )
	  				      
	  			END AS "Proceso",
	  			OPE."EsVentaCorta" AS "EsVentaCorta",
	  			CASE OPE."EsVentaCorta" WHEN 'S' THEN 'SI' WHEN 'N' THEN 'NO' ELSE '' END as "EsVentaCortaUI",
	  			CASE OPE."IdModulo" WHEN 'FAC' THEN COALESCE(CAST( OPE."FolioComprobante" AS  NVARCHAR(40)),'')
	  								WHEN 'MSP' THEN COALESCE(CAST( OPE."FolioComprobante" AS  NVARCHAR(40)),'')
	  								 ELSE '' END AS "FolioDTE",
	  								 
			    OPE."ParidadCierre",
			    OPE."PlazoVencimiento",
			    REA."IdOperacionRelacionada",
			    REA."IdOperacionOrigenReasignacion",
				OPE."InstrumentoNegociacion"
				
	FROM :v_SalidaConsultarOperaciones OPE 
	--INNER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" POR ON (OPE."IdPortafolio"=POR."IdPortafolio") --Se actualiza por demora en tiempo de respuesta
	INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" POR ON (OPE."IdPortafolio"=POR."IdPortafolio")
	--LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" CLI ON OPE."IdCliente" = CLI."IdCliente" AND CLI."TipoDocumentoIdentidad" = 'R' --Se actualiza por demora en tiempo de respuesta
	INNER JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON CLI."IdCliente" = OPE."IdCliente"
	LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" AS PER ON PER."IdPersona" = CLI."IdPersona" AND PER."TipoDocumentoIdentidad" = 'R'
	LEFT JOIN "BSCL.Facturacion.BD.Vistas::ConsultarTipoComprobante" TPCOM ON OPE."IdTipoComprobante" = TPCOM."IdTipoComprobante"
	LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" as TipoOperacion ON OPE."IdTipoOperacion" = TipoOperacion."IdTipoOperacion" 
	LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" as TipoInstrumento ON TipoInstrumento."IdTipoInstrumento" = OPE."IdTipoInstrumento" 
	LEFT JOIN "BSCL.Plataforma.BD::INS.ClasificacionTipoInstrumento" as ClasInst ON ClasInst."IdClasificacionTipoInstrumento" = OPE."IdClasificacionTipoInstrumento"
	---CONTRAPARTE CORREDOR
	LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Area" AS AREA ON (AREA."Codigo" = POR."AreaCliente")
	LEFT JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarCorredora"		  CNC ON (CNC."IdCorredora"=OPE."IdContraparte")
	LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente" IND  --INFORMACION PRIVILEGIADA
				ON ( IND."IdUso"='IND' AND IND."CodigoTipoIndicador"='INFPRV' AND IND."ValorIndicador"='S'
					AND IND."IdCliente" =OPE."IdCliente")
 	LEFT OUTER JOIN ---RELACIONADO
 			(SELECT COALESCE(REL."CodigoValorIndicador" ,'NOVINCULADO') AS "RELACION",REL."IdCliente",COALESCE(REL."NombreValorIndicador",'NO VINCULADO') AS "NombreValorIndicador"
     			 FROM "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente" REL
				--LEFT OUTER JOIN "BSCL.Normativa.BD::DOC.Homologacion" RELHOM
	     		--	ON ((CAST (REL."CodigoValorIndicador" AS  nvarchar(100))= RELHOM."ValorOrigen" AND RELHOM."Tipo"='RELACION' AND RELHOM."Esquema"=v_Esquema AND RELHOM."Version"=v_Version))
			 	 WHERE (REL."IdUso"='IND' AND REL."CodigoTipoIndicador"='1064PROP' AND REL."CodigoValorIndicador" <>'')
 		) RELNOR ON (RELNOR."IdCliente" =OPE."IdCliente")
 	LEFT JOIN "BSCL.Mesa.BD::MES.Operacion" AS REA ON OPE."IdOperacion" = REA."IdOperacion"
 	LEFT JOIN "BSCL.Facturacion.BD::FAC.Comprobante" AS COM ON CASE WHEN OPE."IdComprobante" = '' THEN 0 ELSE OPE."IdComprobante" END = COM."IdComprobante"
   ---PACTOS
   LEFT JOIN "BSCL.Custodia.BD::CUS.Reserva" RES ON REA."IdOperacion" = RES."IdOperacion" AND OPE."IdTipoInstrumento" = 'PAC' 
   LEFT JOIN "BSCL.Custodia.BD::CUS.Cartera" CARR ON   carr."IdCartera" = Res."IdCarteraOrigen"
   where (OPE."IdClasificacionTipoInstrumento" = p_IdClasificacion OR p_IdClasificacion IS NULL)
		AND (OPE."IdTipoInstrumento" = p_IdTipoInstrumento OR p_IdTipoInstrumento IS NULL)
		AND (OPE."IdMoneda" = p_IdMoneda OR p_IdMoneda IS NULL)
		AND (OPE."IdBolsa" = p_IdBolsa OR p_IdBolsa IS NULL)
		AND (OPE."IdCliente" = p_IdCliente OR p_IdCliente IS NULL)
		AND (OPE."IdInstrumento" = p_IdInstrumento OR p_IdInstrumento IS NULL)
		--* MSH, IMPCREDOPT-2527
		AND ( ( OPE."IdEstadoFactura" IN ('VIG','ANU', 'LIQ', 'ENV', 'FAC', 'RGN', 'EMI') AND OPE."IdTipoProducto" IN ('MERF', 'MEME') ) OR (OPE."IdTipoProducto" NOT IN ('MERF', 'MEME') AND OPE."IdEstadoFactura"<>'ANU' ) )
		--AND OPE."IdTipoOperacion"  = 'VENACN'--p_IdTipoOperacion
		AND (
			p_MostrarPactos = 'S' OR
			(
				p_MostrarPactos = 'N' AND OPE."IdTipoOperacion" NOT IN ('COMPAC', 'VENPAC', 'ANTPAC','VCDPAC')
			)
		)
		AND  ((OPE."IdTipoInstrumento" <> 'PAC') OR (OPE."IdTipoInstrumento" = 'PAC' AND  OPE."IdEmisor"='')) 
	    AND (( OPE."IdTipoOperacion" IN ( 'VENPAC','COMPAC') AND REA."IdEstado" NOT IN ('ANT','LIQ')) 
              OR (OPE."IdTipoOperacion" IN ( 'ANTPAC','VCDPAC') AND REA."IdEstado"  IN ('ANT','LIQ'))
              OR (OPE."IdTipoOperacion" NOT IN ( 'ANTPAC','VCDPAC')) )
	    AND (OPE."IdTipoOperacion"  = :p_IdTipOperacion OR :p_IdTipOperacion IS NULL)	

	ORDER BY OPE."FechaMovimiento";


END;