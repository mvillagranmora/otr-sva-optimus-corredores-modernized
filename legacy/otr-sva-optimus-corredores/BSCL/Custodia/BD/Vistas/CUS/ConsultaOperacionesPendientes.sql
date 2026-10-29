SET SCHEMA "BSCL";
GO
DROP VIEW "BSCL.Custodia.BD.Vistas::CUS.ConsultaOperacionesPendientes"; 
GO
CREATE VIEW "BSCL.Custodia.BD.Vistas::CUS.ConsultaOperacionesPendientes"
AS
-- INGRESO DE GARANTIAS
WITH OP_RELACIONADO AS (
	SELECT * FROM (
		SELECT rank() OVER (PARTITION BY OPE."IdEmpresa", OPE."FolioOperacion", OPE."IdTipo" ORDER BY OPE."IdInterno" DESC) AS DENSE_RANK
			, OPE."IdInterno",OPE."IdGestion", OPE."IdEstado", OPE."IndDcv", OPE."EstadoCclv", OPE."EstadoCclvDesc", OPE."IdOrden"
			, OPE."Nemo" , OPE."FechaIngresoRegistro", OPE."DocumentoIdentidad"
			,  OPE."FolioOperacion" ,  OPE."IdTipo" ,OPE."IdEmpresa", OPE."TipoOperacion"
		FROM "BSCL.Custodia.BD::CUS.EnvioOperacionesPendientes"				AS  OPE
	) AS OP_MAX
	WHERE OP_MAX.DENSE_RANK = 1
)
, OPEGAR AS (
SELECT  ROW_NUMBER() OVER (PARTITION BY GAR."FolioComprobanteGarantia" ORDER BY GAR."FolioGarantia" ASC) AS "rn"
	, GAR."FolioGarantia"
	, GAR."FolioComprobanteGarantia"
	, SUM(GAR."CantidadGarantia") OVER (PARTITION BY GAR."FolioComprobanteGarantia") AS "Cantidad"
	, SUM(GAR."MontoValorizadoMonedaInstrumento") OVER (PARTITION BY GAR."FolioComprobanteGarantia") AS "Monto"
	, GAR."IdTipoProductoOperacionOrigen"
    , GAR."IdTipoInstrumento"
    , GAR."IdOrigenGarantia"
    , GAR."Nemotecnico"
    , GAR."IdClasificacionTipoInstrumento"
    , GAR."IdCondicionLiquidacion"
    , GAR."FechaOperacion"
    , GAR."FechaVencimientoInstrumento"
    , GAR."IdPortafolio"
    , GAR."CodigoPortafolio"
    , GAR."IdAgente"
    , GAR."IdTipoReajuste"
    , GAR."IdMonedaInstrumento"
    , GAR."IdTipoOperacionOperacionOrigen"
    , GAR."IdConcepto"
    , GAR."IdEmisor"
    , GAR."EsFungible"
    , GAR."NemoSVS"
    , GAR."IdEmpresa"
    , GAR."IdInstrumentoCarteraGarantizada"
    , GAR."IdCliente"
    , GAR."IdEstado"
    ,GAR."PrecioGarantia"																
    ,GAR."TasaCompraOperacionOrigen"						
FROM "BSCL.Custodia.BD.Vistas::GTA.ConsultarGarantias" AS GAR
)
SELECT "IdInterno","IdGestion","IdTipo","Producto","IdTipoProducto","FolioOperacion","IdRegistro","TipoOperacion","IdTipoOperacion","NombreOperacion" ,"IdTipoInstrumento","Nemo","IdClasificacionTipoInstrumento","IdCondicionLiquidacion","IdOperacion","Accion","OperadorDirecto","FechaIngresoRegistro"   ,"FechaVencimiento","IdTipoDocumentoIdentidad","DocumentoIdentidad","IdPortafolio","CodigoPortafolio","FolioComprobante","FolioComprobanteReferencia","IdOrden","Cantidad" ,"Precio" ,"Tasa" ,"Monto"  ,"MontoComprom","IdEstado","DescEstado","IdAgente","IdMoneda","IdTipoGarantia","DescripcionTipoGarantia"  ,"FondoAfp","IdTipoComprobante","CodigoTipoProducto","IdBolsa","DocumentoIdentidadEntidad","IndDcv","FolioTransaccion","DescEstadoCclv","UsoGarantia","BlockEnvio","IdConcepto","NombreConcepto","IndicadorEnvio","IdEmisor","EsFungible","NemoSVS","IdTipoReajuste","Categoria","IdCliente","Nombre" ,"CodCorr","IdEstadoCustodia", "EstadoCclv"  
FROM (
	SELECT
			rank() OVER (PARTITION BY OPE."IdInterno" ORDER BY OPE."IdInterno" DESC) AS DENSE_RANK,
			CASE IFNULL(OPE."IdInterno", 0)
				WHEN 0 THEN 0
				ELSE OPE."IdInterno"
			END													AS "IdInterno"
			,OPE."IdGestion"									AS "IdGestion"
			,'GTIA'												AS "IdTipo"	
			, CONC."Nombre"										AS "Producto"
			,OPEGAR."IdTipoProductoOperacionOrigen"				AS "IdTipoProducto"	-- PENDIENTE CGO
			,OPEGAR."FolioGarantia" AS "FolioOperacion"
            ,OPEGAR."FolioGarantia" AS "IdRegistro" 
			,'I'												AS "TipoOperacion"
			,''													AS "IdTipoOperacion"
			,'INGRESO GARANTIA'									AS "NombreOperacion"
			,OPEGAR."IdTipoInstrumento"							AS "IdTipoInstrumento"						
			
			,CASE OPEGAR."IdOrigenGarantia"
				WHEN 'CTA' 	THEN CONCAT('CASH' ,OPEGAR."IdMonedaInstrumento")
				when 'CUS'	THEN OPEGAR."Nemotecnico"
				ELSE OPEGAR."Nemotecnico"									
			END 												AS "Nemo"	
											
			,OPEGAR."IdClasificacionTipoInstrumento"				AS "IdClasificacionTipoInstrumento"		
			,OPEGAR."IdCondicionLiquidacion"						AS "IdCondicionLiquidacion"	
			,'I'												AS "IdOperacion"			
			,'INGRESO'											AS "Accion"								
			,'N'												AS "OperadorDirecto"					
			,OPEGAR."FechaOperacion"							AS "FechaIngresoRegistro"
			,OPEGAR."FechaVencimientoInstrumento"					AS "FechaVencimiento"						
			,'R'												AS "IdTipoDocumentoIdentidad"				
			,Cli."DocumentoIdentidad"							AS "DocumentoIdentidad"					
			,OPEGAR."IdPortafolio"									AS "IdPortafolio"
			,OPEGAR."CodigoPortafolio"								AS "CodigoPortafolio"
			,OPEGAR."FolioComprobanteGarantia"						AS "FolioComprobante"						
			,0													AS "FolioComprobanteReferencia"
			,0													AS "IdOrden"								
			,OPEGAR."Cantidad" 									AS "Cantidad"
			,OPEGAR."PrecioGarantia"								AS "Precio"								
			,OPEGAR."TasaCompraOperacionOrigen"					AS "Tasa"	
			,OPEGAR."Monto" AS "Monto"
			,0													AS "MontoComprom"							
			,CASE IFNULL(OPE."IdInterno", 0)
				WHEN 0 THEN ''
				ELSE OPE."IdEstado"
			END													AS "IdEstado"
			,CASE IFNULL(OPE."IdInterno", 0)
				WHEN 0 THEN 'DISPONIBLE' --CGO
				ELSE CASE OPE."IdEstado"						
						WHEN 'C'	THEN 'ENVIADO'
						WHEN 'R'	THEN 'RECHAZADO'
						WHEN 'M'	THEN 'COMPLETADO'
						WHEN 'P'	THEN 'PENDIENTE'
						ELSE 'NO DISPONIBLE PARA ENVIO'
						END 
			END													AS "DescEstado"
			
			,OPEGAR."IdAgente"										AS "IdAgente"								
			,CASE OPEGAR."IdClasificacionTipoInstrumento"
				WHEN 'RF'	THEN OPEGAR."IdTipoReajuste"
				WHEN 'IF'	THEN OPEGAR."IdTipoReajuste"
				ELSE OPEGAR."IdMonedaInstrumento"
			END					AS "IdMoneda"								
			,IFNULL(TCT."IdTipoCuenta",'00')					AS "IdTipoGarantia"
			,IFNULL(TCT."Nombre",'NO EXISTE TIPO CUENTA')		AS "DescripcionTipoGarantia"
			,''													AS "FondoAfp"								
			,''													AS "IdTipoComprobante"						

			,CASE
				WHEN OPEGAR."IdTipoOperacionOperacionOrigen" IN ('INGGTA','IT06','INGASI') THEN 'INSI'
				ELSE ''
			END	AS "CodigoTipoProducto"
			
			,EMP."IdBolsa"										AS "IdBolsa"
			,'F'												AS "DocumentoIdentidadEntidad"					
			,CASE IFNULL(OPE."IdInterno", 0)
				WHEN 0 THEN ''
				ELSE OPE."IndDcv"
			END													AS "IndDcv"
			,'0'												AS "FolioTransaccion"						
			,IFNULL(OPE."EstadoCclvDesc",'')					AS "DescEstadoCclv"						
			,''													AS "UsoGarantia"							
			,''													AS "BlockEnvio"							
			,OPEGAR."IdConcepto"									AS "IdConcepto"
			,CON."Descripcion"									AS "NombreConcepto"
			,''													AS "IndicadorEnvio"						
			,OPEGAR."IdEmisor"										AS "IdEmisor"								
			,OPEGAR."EsFungible"									AS "EsFungible"							
			,OPEGAR."NemoSVS"										AS "NemoSVS"		
			,OPEGAR."IdTipoReajuste"								AS "IdTipoReajuste"
			,CASE WHEN OPEGAR."IdClasificacionTipoInstrumento" = 'RV' THEN 
					CASE WHEN OPEGAR."IdTipoInstrumento" IN ('ACN', 'ETN') THEN CASE INS."IdTipoInstrumentoOrigen" WHEN 'OSA' THEN 'OSA' ELSE 'AC' END
					WHEN OPEGAR."IdTipoInstrumento" IN ('MIL', 'ETX', 'ACX') THEN 'OS'
					WHEN OPEGAR."IdTipoInstrumento" = 'CFX' THEN 'CFI'
					WHEN OPEGAR."IdTipoInstrumento" = 'MON' THEN 'MO'
					WHEN OPEGAR."IdTipoInstrumento" = 'OSA' THEN CASE INS."IdTipoInstrumentoOrigen" WHEN 'CFI' THEN 'OSACFI' ELSE 'OSA' END
					ELSE OPEGAR."IdTipoInstrumento" END 
			WHEN OPEGAR."IdClasificacionTipoInstrumento" = 'RF' THEN 
				CASE WHEN OPEGAR."IdTipoInstrumento" IN ('BCD', 'BTU', 'BTP', 'BCP', 'BVL', 'ORF', 'PCX', 'PRD', 'PRC', 'BCU') THEN 'BC'
				WHEN OPEGAR."IdTipoInstrumento" = 'LTH' THEN 'LH'
				ELSE 'BB/BE' END
			ELSE OPEGAR."IdClasificacionTipoInstrumento" END AS "Categoria"	
			,CLI."IdCliente"									AS "IdCliente"	
			,IFNULL(CLI."NombreCompleto",'')	   		        AS "Nombre"
			,EMP."CodigoCorredora"								AS "CodCorr"
			,COALESCE(ESTCUS."IdEstado",'')						AS "IdEstadoCustodia"
			,COALESCE(OPE."EstadoCclv",'')					AS "EstadoCclv"
		FROM OPEGAR
		LEFT JOIN OP_RELACIONADO				AS  OPE
			ON (OPEGAR."IdEmpresa" = OPE."IdEmpresa" AND OPEGAR."FolioGarantia" = OPE."FolioOperacion" AND OPE."IdTipo" = 'GTIA' AND ope."TipoOperacion" = 'I')

			LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento"				AS	INS
			ON (INS."IdInstrumento" = OPEGAR."IdInstrumentoCarteraGarantizada")
						
		INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" 					AS 	CLI 
			ON (CLI."IdEmpresa" = OPEGAR."IdEmpresa" 				AND 
				CLI."IdCliente" = OPEGAR."IdCliente")
		INNER JOIN "BSCL.Custodia.BD::CUS.ConceptoCustodia"								AS CON
			ON (OPEGAR."IdConcepto" = CON."IdConcepto"	AND
				CON."NombreEntidad" = 'CUS.GARANTIA')

		INNER JOIN "BSCL.Plataforma.BD::PLA.Concepto"									AS CONC
		ON (OPEGAR."IdConcepto" = CONC."IdConcepto")
		
		LEFT JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarTipoCuenta"					AS TCT
		ON (TCT."IdModulo" 		= 'CUS' and 
			TCT."IdTipoCuenta" 	= CON."IdTipoCuenta")
			
		INNER JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarCorredoraEmpresa"			AS EMP
		ON (EMP."IdEmpresa"		=  OPEGAR."IdEmpresa"	and EMP."IdBolsa" = 'BCS')
		LEFT JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarEstadosCustodia" ESTCUS
			ON (ESTCUS."NombreEntidad" = 'CUSTODIA.OPERACIONES' AND
				ESTCUS."IdEstado" = MAP(OPE."IdEstado",'C','ENV','R','REC','M','COM','P','PEN',''))
		WHERE OPEGAR."rn" = 1 AND OPEGAR."IdEstado" = 'VIG' AND OPEGAR."IdOrigenGarantia" IN ('CUS','CTA')
) AS IngresosGarantiasPendientes  WHERE  "FolioOperacion" IS NOT NULL OR "IdInterno" = 0
		
UNION ALL

-- RETIRO DE GARANTIAS
SELECT "IdInterno","IdGestion","IdTipo","Producto","IdTipoProducto","FolioOperacion","IdRegistro","TipoOperacion","IdTipoOperacion","NombreOperacion" ,"IdTipoInstrumento","Nemo","IdClasificacionTipoInstrumento","IdCondicionLiquidacion","IdOperacion","Accion","OperadorDirecto","FechaIngresoRegistro"   ,"FechaVencimiento","IdTipoDocumentoIdentidad","DocumentoIdentidad","IdPortafolio","CodigoPortafolio","FolioComprobante","FolioComprobanteReferencia","IdOrden","Cantidad" ,"Precio" ,"Tasa" ,"Monto"  ,"MontoComprom","IdEstado","DescEstado","IdAgente","IdMoneda","IdTipoGarantia","DescripcionTipoGarantia"  ,"FondoAfp","IdTipoComprobante","CodigoTipoProducto","IdBolsa","DocumentoIdentidadEntidad","IndDcv","FolioTransaccion","DescEstadoCclv","UsoGarantia","BlockEnvio","IdConcepto","NombreConcepto","IndicadorEnvio","IdEmisor","EsFungible","NemoSVS","IdTipoReajuste","Categoria","IdCliente","Nombre" ,"CodCorr","IdEstadoCustodia", "EstadoCclv"  
FROM (
	SELECT
			rank() OVER (PARTITION BY OPE."IdInterno" ORDER BY OPE."IdInterno") AS DENSE_RANK,
			CASE IFNULL(OPE."IdInterno", 0)
				WHEN 0 THEN 0
				ELSE OPE."IdInterno"
			END													AS "IdInterno"
			,OPE."IdGestion"									AS "IdGestion"
			,'GTIA'												AS "IdTipo"
			, GAR."NombreConcepto"								AS "Producto"
			,GAR."IdTipoProductoOperacionOrigen"				AS "IdTipoProducto"	-- PENDIENTE CGO
			,GAR."FolioGarantia"                                AS "FolioOperacion"
			,GAR."FolioGarantia"   								AS "IdRegistro"
			,'E'												AS "TipoOperacion"
			,''													AS "IdTipoOperacion"			
			,'RETIRO GARANTIA' 									AS "NombreOperacion"
			,GAR."IdTipoInstrumento"							AS "IdTipoInstrumento"						
			
			,CASE GAR."IdOrigenGarantia"
				WHEN 'CTA' 	THEN CONCAT('CASH' ,"IdMonedaInstrumento")
				when 'CUS'	THEN GAR."Nemotecnico"
				ELSE GAR."Nemotecnico"									
			END 												AS "Nemo"	
											
			,GAR."IdClasificacionTipoInstrumento"				AS "IdClasificacionTipoInstrumento"		
			,GAR."IdCondicionLiquidacion"						AS "IdCondicionLiquidacion"	
			,'E'												AS "IdOperacion"			
			,'RETIRO'											AS "Accion"								
			,'N'												AS "OperadorDirecto"					
			,GAR."FechaOperacion"							AS "FechaIngresoRegistro"
			,GAR."FechaVencimientoInstrumento"					AS "FechaVencimiento"						
			,'R'												AS "IdTipoDocumentoIdentidad"				
			,Cli."DocumentoIdentidad"							AS "DocumentoIdentidad"
			,GAR."IdPortafolio"									AS "IdPortafolio"
			,GAR."CodigoPortafolio"								AS "CodigoPortafolio"
			,GAR."FolioComprobanteGarantia"						AS "FolioComprobante"
			,0													AS "FolioComprobanteReferencia"
			,0													AS "IdOrden"								
			,CASE GAR."IdOrigenGarantia"
				when 'CUS'	THEN GAR."EGRCantidad"
				ELSE 0									
			END 												AS "Cantidad"								
			,GAR."PrecioGarantia"								AS "Precio"								
			,GAR."TasaCompraOperacionOrigen"					AS "Tasa"									
			,CASE GAR."IdOrigenGarantia"
				when 'CUS'	THEN GAR."EGRMonto"
				ELSE GAR."EGRCantidad"									
			END 												AS "Monto"									
			,0													AS "MontoComprom"							
			,CASE IFNULL(OPE."IdInterno", 0)
				WHEN 0 THEN ''
				ELSE OPE."IdEstado"
			END													AS "IdEstado"
			,CASE IFNULL(OPE."IdInterno", 0)
				WHEN 0 THEN 'DISPONIBLE'
				ELSE CASE OPE."IdEstado"						
						WHEN 'C'	THEN 'ENVIADO'
						WHEN 'R'	THEN 'RECHAZADO'
						WHEN 'M'	THEN 'COMPLETADO'
						WHEN 'P'	THEN 'PENDIENTE'					
						ELSE 'NO DISPONIBLE PARA ENVIO'
						END 
			END													AS "DescEstado"
			
			,GAR."IdAgente"										AS "IdAgente"								
			,CASE GAR."IdClasificacionTipoInstrumento"
				WHEN 'RF'	THEN GAR."IdTipoReajuste"
				WHEN 'IF'	THEN GAR."IdTipoReajuste"
				ELSE GAR."IdMonedaInstrumento"
			END					AS "IdMoneda"
			,IFNULL(TCT."IdTipoCuenta",'00')					AS "IdTipoGarantia"
			,IFNULL(TCT."Nombre",'NO EXISTE TIPO CUENTA')		AS "DescripcionTipoGarantia"
			,''													AS "FondoAfp"								
			,''													AS "IdTipoComprobante"						
				
			,CASE
				WHEN GAR."IdTipoOperacionOperacionOrigen" IN ('INGGTA','IT06','INGASI') THEN 'INSI'
				ELSE ''
			END	AS "CodigoTipoProducto"

			,EMP."IdBolsa"										AS "IdBolsa"
			,'F'												AS "DocumentoIdentidadEntidad"					
			,CASE IFNULL(OPE."IdInterno", 0)
				WHEN 0 THEN ''
				ELSE OPE."IndDcv"
			END													AS "IndDcv"
			,'0'												AS "FolioTransaccion"						
			,IFNULL(OPE."EstadoCclvDesc",'')					AS "DescEstadoCclv"
			,''													AS "UsoGarantia"							
			,''													AS "BlockEnvio"							
			,GAR."IdConcepto"									AS "IdConcepto"
			,CON."Descripcion"									AS "NombreConcepto"						
			,''													AS "IndicadorEnvio"						
			,GAR."IdEmisor"										AS "IdEmisor"								
			,GAR."EsFungible"									AS "EsFungible"							
			,GAR."NemoSVS"										AS "NemoSVS"	
			,GAR."IdTipoReajuste"								AS "IdTipoReajuste"
			,CASE WHEN GAR."IdClasificacionTipoInstrumento" = 'RV' THEN 
					CASE WHEN GAR."IdTipoInstrumento" IN ('ACN', 'ETN') THEN CASE INS."IdTipoInstrumentoOrigen" WHEN 'OSA' THEN 'OSA' ELSE 'AC' END
					WHEN GAR."IdTipoInstrumento" IN ('MIL', 'ETX', 'ACX') THEN 'OS'
					WHEN GAR."IdTipoInstrumento" = 'CFX' THEN 'CFI'
					WHEN GAR."IdTipoInstrumento" = 'MON' THEN 'MO'
					WHEN GAR."IdTipoInstrumento" = 'OSA' THEN CASE INS."IdTipoInstrumentoOrigen" WHEN 'CFI' THEN 'OSACFI' ELSE 'OSA' END
					ELSE GAR."IdTipoInstrumento" END 
			WHEN GAR."IdClasificacionTipoInstrumento" = 'RF' THEN 
				CASE WHEN GAR."IdTipoInstrumento" IN ('BCD', 'BTU', 'BTP', 'BCP', 'BVL', 'ORF', 'PCX', 'PRD', 'PRC', 'BCU') THEN 'BC'
				WHEN GAR."IdTipoInstrumento" IN ('LTH') THEN 'LH'
				ELSE 'BB/BE' END
			ELSE GAR."IdClasificacionTipoInstrumento" END AS "Categoria"		
			,CLI."IdCliente"									AS "IdCliente"	
			,IFNULL(CLI."NombreCompleto",'')			        AS "Nombre"
			,EMP."CodigoCorredora"								AS "CodCorr"
			,COALESCE(ESTCUS."IdEstado",'')						AS "IdEstadoCustodia"
			,COALESCE(OPE."EstadoCclv",'')					AS "EstadoCclv"
		FROM "BSCL.Custodia.BD.Vistas::GTA.ConsultarGarantias"							AS	GAR
		LEFT JOIN OP_RELACIONADO				AS  OPE
			ON (GAR."IdEmpresa" = OPE."IdEmpresa" AND GAR."FolioGarantia" = OPE."FolioOperacion" AND OPE."IdTipo" = 'GTIA' AND ope."TipoOperacion" = 'E')

		LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento"				AS	INS
			ON (INS."IdInstrumento" = GAR."IdInstrumentoCarteraGarantizada")
						
		INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" 					AS 	CLI 
			ON (CLI."IdEmpresa" = GAR."IdEmpresa" 				AND 
				CLI."IdCliente" = GAR."IdCliente")
		INNER JOIN "BSCL.Custodia.BD::CUS.ConceptoCustodia"								AS CON
			ON (GAR."IdConcepto" = CON."IdConcepto"	AND
				CON."NombreEntidad" = 'CUS.GARANTIA')
		LEFT JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarTipoCuenta"					AS TCT
		ON (TCT."IdModulo" = 'CUS' and 
			TCT."IdTipoCuenta" = CON."IdTipoCuenta")
		INNER JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarCorredoraEmpresa"			AS EMP
		ON (EMP."IdEmpresa"		=  GAR."IdEmpresa"	and EMP."IdBolsa" = 'BCS')
		LEFT JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarEstadosCustodia" ESTCUS
			ON (ESTCUS."NombreEntidad" = 'CUSTODIA.OPERACIONES' AND
				ESTCUS."IdEstado" = MAP(OPE."IdEstado",'C','ENV','R','REC','M','COM','P','PEN',''))
		WHERE GAR."IdEstado" in ('LIQ', 'RPA')
		  AND GAR."EGRCantidad" <> 0
) AS RetirosGarantiasPendientes  WHERE (RetirosGarantiasPendientes.DENSE_RANK = 1 AND "IdInterno" <> 0) OR "IdInterno" = 0

UNION ALL

-- GARANTIAS INGRESADAS CON ESTADO LIQUIDADO
SELECT "IdInterno","IdGestion","IdTipo","Producto","IdTipoProducto","FolioOperacion","IdRegistro","TipoOperacion","IdTipoOperacion","NombreOperacion" ,"IdTipoInstrumento","Nemo","IdClasificacionTipoInstrumento","IdCondicionLiquidacion","IdOperacion","Accion","OperadorDirecto","FechaIngresoRegistro"   ,"FechaVencimiento","IdTipoDocumentoIdentidad","DocumentoIdentidad","IdPortafolio","CodigoPortafolio","FolioComprobante","FolioComprobanteReferencia","IdOrden","Cantidad" ,"Precio" ,"Tasa" ,"Monto"  ,"MontoComprom","IdEstado","DescEstado","IdAgente","IdMoneda","IdTipoGarantia","DescripcionTipoGarantia"  ,"FondoAfp","IdTipoComprobante","CodigoTipoProducto","IdBolsa","DocumentoIdentidadEntidad","IndDcv","FolioTransaccion","DescEstadoCclv","UsoGarantia","BlockEnvio","IdConcepto","NombreConcepto","IndicadorEnvio","IdEmisor","EsFungible","NemoSVS","IdTipoReajuste","Categoria","IdCliente","Nombre" ,"CodCorr","IdEstadoCustodia", "EstadoCclv"   
FROM (
	SELECT
			rank() OVER (PARTITION BY OPE."IdInterno" ORDER BY OPE."IdInterno") AS DENSE_RANK,
			CASE IFNULL(OPE."IdInterno", 0)
				WHEN 0 THEN 0
				ELSE OPE."IdInterno"
			END													AS "IdInterno"
			,OPE."IdGestion"									AS "IdGestion"
			,'GTIA'												AS "IdTipo"
			, GAR."NombreConcepto"								AS "Producto"
			,GAR."IdTipoProductoOperacionOrigen"				AS "IdTipoProducto"	-- PENDIENTE CGO
			,GAR."FolioGarantia"                                AS "FolioOperacion"
			,GAR."FolioGarantia"   								AS "IdRegistro"
			,'I'												AS "TipoOperacion"
			,''													AS "IdTipoOperacion"			
			,'INGRESO GARANTIA' 								AS "NombreOperacion"
			,GAR."IdTipoInstrumento"							AS "IdTipoInstrumento"						
			
			,CASE GAR."IdOrigenGarantia"
				WHEN 'CTA' 	THEN CONCAT('CASH' ,"IdMonedaInstrumento")
				when 'CUS'	THEN GAR."Nemotecnico"
				ELSE GAR."Nemotecnico"									
			END 												AS "Nemo"	
											
			,GAR."IdClasificacionTipoInstrumento"				AS "IdClasificacionTipoInstrumento"		
			,GAR."IdCondicionLiquidacion"						AS "IdCondicionLiquidacion"	
			,'E'												AS "IdOperacion"			
			,'RETIRO'											AS "Accion"								
			,'N'												AS "OperadorDirecto"					
			,GAR."FechaOperacion"								AS "FechaIngresoRegistro"
			,GAR."FechaVencimientoInstrumento"					AS "FechaVencimiento"						
			,'R'												AS "IdTipoDocumentoIdentidad"				
			,Cli."DocumentoIdentidad"							AS "DocumentoIdentidad"
			,GAR."IdPortafolio"									AS "IdPortafolio"
			,GAR."CodigoPortafolio"								AS "CodigoPortafolio"
			,GAR."FolioComprobanteGarantia"						AS "FolioComprobante"
			,0													AS "FolioComprobanteReferencia"
			,0													AS "IdOrden"								
			,GAR."CantidadGarantia"								AS "Cantidad"
			,GAR."PrecioGarantia"								AS "Precio"								
			,GAR."TasaCompraOperacionOrigen"					AS "Tasa"									
			,GAR."MontoValorizadoMonedaInstrumento" 			AS "Monto"
			,0													AS "MontoComprom"							
			,CASE IFNULL(OPE."IdInterno", 0)
				WHEN 0 THEN ''
				ELSE OPE."IdEstado"
			END													AS "IdEstado"
			,CASE IFNULL(OPE."IdInterno", 0)
				WHEN 0 THEN 'DISPONIBLE'
				ELSE CASE OPE."IdEstado"						
						WHEN 'C'	THEN 'ENVIADO'
						WHEN 'R'	THEN 'RECHAZADO'
						WHEN 'M'	THEN 'COMPLETADO'
						WHEN 'P'	THEN 'PENDIENTE'					
						ELSE 'NO DISPONIBLE PARA ENVIO'
						END 
			END													AS "DescEstado"
			
			,GAR."IdAgente"										AS "IdAgente"								
			,CASE GAR."IdClasificacionTipoInstrumento"
				WHEN 'RF'	THEN GAR."IdTipoReajuste"
				WHEN 'IF'	THEN GAR."IdTipoReajuste"
				ELSE GAR."IdMonedaInstrumento"
			END					AS "IdMoneda"
			,IFNULL(TCT."IdTipoCuenta",'00')					AS "IdTipoGarantia"
			,IFNULL(TCT."Nombre",'NO EXISTE TIPO CUENTA')		AS "DescripcionTipoGarantia"
			,''													AS "FondoAfp"								
			,''													AS "IdTipoComprobante"						
				
			,CASE
				WHEN GAR."IdTipoOperacionOperacionOrigen" IN ('INGGTA','IT06','INGASI') THEN 'INSI'
				ELSE ''
			END	AS "CodigoTipoProducto"

			,EMP."IdBolsa"										AS "IdBolsa"
			,'F'												AS "DocumentoIdentidadEntidad"					
			,CASE IFNULL(OPE."IdInterno", 0)
				WHEN 0 THEN ''
				ELSE OPE."IndDcv"
			END													AS "IndDcv"
			,'0'												AS "FolioTransaccion"						
			,IFNULL(OPE."EstadoCclvDesc",'')					AS "DescEstadoCclv"
			,''													AS "UsoGarantia"							
			,''													AS "BlockEnvio"							
			,GAR."IdConcepto"									AS "IdConcepto"
			,CON."Descripcion"									AS "NombreConcepto"						
			,''													AS "IndicadorEnvio"						
			,GAR."IdEmisor"										AS "IdEmisor"								
			,GAR."EsFungible"									AS "EsFungible"							
			,GAR."NemoSVS"										AS "NemoSVS"	
			,GAR."IdTipoReajuste"								AS "IdTipoReajuste"
			,CASE WHEN GAR."IdClasificacionTipoInstrumento" = 'RV' THEN 
					CASE WHEN GAR."IdTipoInstrumento" IN ('ACN', 'ETN') THEN CASE INS."IdTipoInstrumentoOrigen" WHEN 'OSA' THEN 'OSA' ELSE 'AC' END
					WHEN GAR."IdTipoInstrumento" IN ('MIL', 'ETX', 'ACX') THEN 'OS'
					WHEN GAR."IdTipoInstrumento" = 'CFX' THEN 'CFI'
					WHEN GAR."IdTipoInstrumento" = 'MON' THEN 'MO'
					WHEN GAR."IdTipoInstrumento" = 'OSA' THEN CASE INS."IdTipoInstrumentoOrigen" WHEN 'CFI' THEN 'OSACFI' ELSE 'OSA' END
					ELSE GAR."IdTipoInstrumento" END 
			WHEN GAR."IdClasificacionTipoInstrumento" = 'RF' THEN 
				CASE WHEN GAR."IdTipoInstrumento" IN ('BCD', 'BTU', 'BTP', 'BCP', 'BVL', 'ORF', 'PCX', 'PRD', 'PRC', 'BCU') THEN 'BC'
				WHEN GAR."IdTipoInstrumento" IN ('LTH') THEN 'LH'
				ELSE 'BB/BE' END
			ELSE GAR."IdClasificacionTipoInstrumento" END AS "Categoria"		
			,CLI."IdCliente"									AS "IdCliente"	
			,IFNULL(CLI."NombreCompleto",'')			        AS "Nombre"
			,EMP."CodigoCorredora"								AS "CodCorr"
			,COALESCE(ESTCUS."IdEstado",'')						AS "IdEstadoCustodia"
			,COALESCE(OPE."EstadoCclv",'')					AS "EstadoCclv"
		FROM "BSCL.Custodia.BD.Vistas::GTA.ConsultarGarantias"							AS	GAR
		LEFT JOIN OP_RELACIONADO				AS  OPE
			ON (GAR."IdEmpresa" = OPE."IdEmpresa" AND GAR."FolioGarantia" = OPE."FolioOperacion" AND OPE."IdTipo" = 'GTIA' AND ope."TipoOperacion" = 'I')
					
		LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento"				AS	INS
			ON (INS."IdInstrumento" = GAR."IdInstrumentoCarteraGarantizada")
						
		INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" 					AS 	CLI 
			ON (CLI."IdEmpresa" = GAR."IdEmpresa" 				AND 
				CLI."IdCliente" = GAR."IdCliente")
		INNER JOIN "BSCL.Custodia.BD::CUS.ConceptoCustodia"								AS CON
			ON (GAR."IdConcepto" = CON."IdConcepto"	AND
				CON."NombreEntidad" = 'CUS.GARANTIA')
		LEFT JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarTipoCuenta"					AS TCT
		ON (TCT."IdModulo" = 'CUS' and 
			TCT."IdTipoCuenta" = CON."IdTipoCuenta")
		INNER JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarCorredoraEmpresa"			AS EMP
		ON (EMP."IdEmpresa"		=  GAR."IdEmpresa"	and EMP."IdBolsa" = 'BCS')
		LEFT JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarEstadosCustodia" ESTCUS
			ON (ESTCUS."NombreEntidad" = 'CUSTODIA.OPERACIONES' AND
				ESTCUS."IdEstado" = MAP(OPE."IdEstado",'C','ENV','R','REC','M','COM','P','PEN',''))
		WHERE GAR."IdEstado" in ('LIQ')
		  AND GAR."EGRCantidad" <> 0
) AS GarantiasLiquidadasPendientes  WHERE (GarantiasLiquidadasPendientes.DENSE_RANK = 1 AND "IdInterno" <> 0) OR "IdInterno" = 0
UNION ALL 

-- COMPROBANTES DE FACTURAS
	SELECT	 DISTINCT
			 CASE IFNULL(OPE."IdInterno", 0)
					WHEN 0 THEN 0
					ELSE OPE."IdInterno"
			 END												AS "IdInterno"
			,OPE."IdGestion"									AS "IdGestion"
			,'ASIG'												AS "IdTipo"	 								-- ??		
			,INS."RazonSocial"									AS "Producto"
			,''													AS "IdTipoProducto"							-- ??
			,FAC."FolioTransaccion"                             AS "FolioOperacion"
			,FAC."FolioTransaccion"    							AS "IdRegistro"
			,CASE TOPE."AccionOperacion"
				WHEN 'COMPRA' THEN 'C'
				WHEN 'VENTA'  THEN 'V'
			END													AS "TipoOperacion"							-- ??
			,TOPE."AccionOperacion"								AS "IdTipoOperacion"
			,TCOMP."Nombre"										AS "NombreOperacion"						-- ??
			,COALESCE(FAC."IdTipoInstrumentoSubyacente", FAC."IdTipoInstrumento")				AS "IdTipoInstrumento"						
			,COALESCE(FAC."NemotecnicoSubyacente", FAC."NemotecnicoInstrumento")	AS "Nemo"	
											
			,FAC."IdClasificacionTipoInstrumento"				AS "IdClasificacionTipoInstrumento"		
			,FAC."IdCondicionLiquidacion"						AS "IdCondicionLiquidacion"	
			,'C'												AS "IdOperacion"							-- ??			
			,'INGRESO'											AS "Accion"									-- ??
			,'N'												AS "OperadorDirecto"						-- ??				
			,FAC."FechaSistema"     							AS "FechaIngresoRegistro"
			,FAC."FechaVencimiento"								AS "FechaVencimiento"						-- ??						
			,'R'												AS "IdTipoDocumentoIdentidad"				-- ??						
			,Cli."DocumentoIdentidad"							AS "DocumentoIdentidad"
			,FAC."IdPortafolio"									AS "IdPortafolio"
			,PORT."CodigoPortafolio"							AS "CodigoPortafolio"							
			,FAC."IdComprobante"								AS "FolioComprobante"						
			,0													AS "FolioComprobanteReferencia"
			,0													AS "IdOrden"								
			,FAC."Cantidad"										AS "Cantidad"								
			,FAC."Precio"										AS "Precio"								
			,FAC."Tasa"											AS "Tasa"									
			,0													AS "Monto"									
			,FAC."Monto"										AS "MontoComprom"							
			,CASE IFNULL(OPE."IdInterno", 0)
				WHEN 0 THEN ''
				ELSE OPE."IdEstado"
			END													AS "IdEstado"
			,CASE IFNULL(OPE."IdInterno", 0)
				WHEN 0 THEN 'DISPONIBLE'
				ELSE CASE OPE."IdEstado"						
						WHEN 'C'	THEN 'ENVIADO'
						WHEN 'R'	THEN 'RECHAZADO'
						WHEN 'M'	THEN 'COMPLETADO'
						WHEN 'P'	THEN 'PENDIENTE'
						ELSE 'NO DISPONIBLE PARA ENVIO'
						END 
			END													AS "DescEstado"
			
			,0													AS "IdAgente"								-- ??								
			,CASE FAC."IdClasificacionTipoInstrumento"
				WHEN 'RF'	THEN FAC."IdTipoReajuste"
				WHEN 'IF'	THEN FAC."IdTipoReajuste"
				ELSE FAC."IdMoneda"
			END					AS "IdMoneda"								
			,IFNULL(TCT."IdTipoCuenta",'00')					AS "IdTipoGarantia"
			,IFNULL(TCT."Nombre",'')							AS "DescripcionTipoGarantia"
			,''													AS "FondoAfp"								-- ??
			,''													AS "IdTipoComprobante"						-- ??						

			,CASE
				WHEN FAC."IdTipoOperacion" IN ('INGGTA','IT06','INGASI') THEN 'INSI'
				ELSE ''
			END	AS "CodigoTipoProducto"
			
			,EMP."IdBolsa"										AS "IdBolsa"
			,'F'												AS "DocumentoIdentidadEntidad"					
			,CASE IFNULL(OPE."IdInterno", 0)
				WHEN 0 THEN ''
				ELSE OPE."IndDcv"
			END													AS "IndDcv"
			,'0'												AS "FolioTransaccion"						
			,IFNULL(OPE."EstadoCclvDesc",'')					AS "DescEstadoCclv"						
			,''													AS "UsoGarantia"							
			,''													AS "BlockEnvio"							
			,FAC."IdConceptoMovimiento"							AS "IdConcepto"
			,CON."Descripcion"									AS "NombreConcepto"
			,''													AS "IndicadorEnvio"						
			,FAC."IdEmisor"										AS "IdEmisor"								
			,FAC."EsFungible"									AS "EsFungible"							
			,FAC."NemoSVS"										AS "NemoSVS"		
			,FAC."IdTipoReajuste"								AS "IdTipoReajuste"
				, CASE FAC."IdTipoInstrumento"
					WHEN 'ACN'	THEN 'AC'
					WHEN 'CFI' THEN  'CFI'
					ELSE FAC."IdClasificacionTipoInstrumento"
				END												AS "Categoria"								
			
					
			,CLI."IdCliente"									AS "IdCliente"	
			,IFNULL(CLI."NombreCompleto",'')	   		        AS "Nombre"
			,EMP."CodigoCorredora"								AS "CodCorr"
			,COALESCE(ESTCUS."IdEstado",'')						AS "IdEstadoCustodia"
			,COALESCE(OPE."EstadoCclv",'')					AS "EstadoCclv"
	FROM "BSCL.Facturacion.BD::FAC.MovimientoComprobante"									AS  FAC
	LEFT JOIN   OP_RELACIONADO							AS  OPE
	ON (FAC."IdEmpresa" = OPE."IdEmpresa" AND FAC."FolioTransaccion" = CAST (OPE."FolioOperacion"AS VARCHAR)
	AND FAC."NemotecnicoSubyacente" = OPE."Nemo" AND FAC."FechaSistema"=OPE."FechaIngresoRegistro" AND FAC."DocumentoIdentidad" = OPE."DocumentoIdentidad")
	
	
	INNER JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento"						AS	INS
	ON (INS."IdInstrumento" = FAC."IdInstrumento")
	
	
	INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" 							AS 	CLI 
	ON (CLI."IdEmpresa" = FAC."IdEmpresa" 				AND 
		CLI."IdCliente" = FAC."IdCliente")
		
	INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" 										AS PORT
	ON (PORT."IdPortafolio" = FAC."IdPortafolio")
	
	INNER JOIN "BSCL.Custodia.BD::CUS.ConceptoCustodia"										AS CON
		ON (FAC."IdConceptoMovimiento" = CON."IdConcepto"	)
					
	LEFT JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarTipoCuenta"							AS TCT
		ON (TCT."IdModulo" 		= 'CUS' and 
			TCT."IdTipoCuenta" 	= CON."IdTipoCuenta")
	
			INNER JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarCorredoraEmpresa"			AS EMP
			ON (EMP."IdEmpresa"	=  FAC."IdEmpresa"	AND  
				EMP."IdBolsa" 	= 'BCS')		
			
	LEFT JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarEstadosCustodia" ESTCUS
				ON (ESTCUS."NombreEntidad" = 'CUSTODIA.OPERACIONES' AND
					ESTCUS."IdEstado" = MAP(OPE."IdEstado",'C','ENV','R','REC','M','COM','P','PEN',''))		
	
	LEFT JOIN "BSCL.Facturacion.BD::FAC.TipoComprobante"								AS TCOMP
	ON (TCOMP."IdTipoComprobante" = FAC."IdTipoComprobante")
	
	LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion"									AS TOPE
	ON (TOPE."IdTipoOperacion" = FAC."IdTipoOperacion")
	
	
	WHERE 
		FAC."IdTipoInstrumento" IN ('FDO','FIP','FBC','FIC','FUF','IOA','IOE','IOI','IOD','IOB','IOC','IOU','IOM','IOF','FBO','SIM') 
	AND FAC."IdEstadoFacturacion" =  'FAC'
	AND FAC."IdCondicionLiquidacion" =  'TP'	
		
UNION ALL 

--	ANTICIPO SIMULTANEAS
	SELECT   DISTINCT
			 CASE IFNULL(OPE."IdInterno", 0)
		   		WHEN 0 THEN 0
		   		ELSE OPE."IdInterno"
		     END											AS "IdInterno"
			,OPE."IdGestion"								AS "IdGestion"
			,'ANTC'	 										AS "IdTipo"
			,INS."RazonSocial"								AS "Producto"
			,ANT."IdTipoProducto"							AS "IdTipoProducto"
			,ANT."FolioCartera"								AS "FolioOperacion"
			,ANT."FolioCartera"   							AS "IdRegistro"
			,CASE ANT."AccionOperacion"
				WHEN 'COMPRA' THEN 'C'
				WHEN 'VENTA'  THEN 'V'
			END												AS "TipoOperacion"
			,ANT."AccionOperacion"							AS "IdTipoOperacion"
			,TOPE."Nombre"									AS "NombreOperacion"
			,ANT."IdTipoInstrumentoOperacion"				AS "IdTipoInstrumento"
			,ANT."suby_Nemotecnico"							AS "Nemo"
			,INS."IdClasificacionTipoInstrumento"			AS "IdClasificacionTipoInstrumento"
			,ANT."IdCondicionLiquidacion"					AS "IdCondicionLiquidacion"
			,CASE ANT."AccionOperacion"
				WHEN 'COMPRA' THEN 'C'
				WHEN 'VENTA'  THEN 'V'
			END												AS "IdOperacion"
			,ANT."AccionOperacion"							AS "Accion"			
			,'N'											AS "OperadorDirecto"
			,ANT."EGRFechaOperacion"						AS "FechaIngresoRegistro"
			,ANT."FechaVencimientoOperacion"				AS "FechaVencimiento"
			,'R'											AS "IdTipoDocumentoIdentidad"
			,ANT."DocumentoIdentidadDefecto"				AS "DocumentoIdentidad"
			,ANT."IdPortafolio"								AS "IdPortafolio"
			,ANT."CodigoPortafolio"							AS "CodigoPortafolio"
			,ANT."FolioComprobante"							AS "FolioComprobante"
			,0												AS "FolioComprobanteReferencia"
			,COALESCE(OPE."IdOrden",0)						AS "IdOrden"
			,ANT."EGRCantidad"								AS "Cantidad"
			,ANT."EGRPrecio"								AS "Precio"
			,ANT."EGRTasa"									AS "Tasa"
			,ANT."EGRMonto"									AS "Monto"
			,0												AS "MontoComprom"
			,CASE IFNULL(OPE."IdInterno", 0)
				WHEN 0 THEN ''
				ELSE OPE."IdEstado"
			END												AS "IdEstado"
			,CASE IFNULL(OPE."IdInterno", 0)
				WHEN 0 THEN 'DISPONIBLE'
				ELSE CASE OPE."IdEstado"						
						WHEN 'C'	THEN 'ENVIADO'
						WHEN 'R'	THEN 'RECHAZADO'
						WHEN 'M'	THEN 'COMPLETADO'
						WHEN 'P'	THEN 'PENDIENTE'
						ELSE 'NO DISPONIBLE PARA ENVIO'
						END
			END												AS "DescEstado"
			
			,ANT."IdAgente"									AS "IdAgente"
			,ANT."IdMonedaInstrumento"						AS "IdMoneda"
			,'00'											AS "IdTipoGarantia"
			,ANT."EGRGlosa"									AS "DescripcionTipoGarantia"
			,''												AS "FondoAfp"
			,ANT."IdTipoComprobante"						AS "IdTipoComprobante"
			,ANT."IdTipoProducto"							AS "CodigoTipoProducto"
			,ANT."IdBolsa"									AS "IdBolsa"
			,'F'											AS "DocumentoIdentidadEntidad"
			,''												AS "IndDcv"
			,'0'											AS "FolioTransaccion"
			,IFNULL(OPE."EstadoCclvDesc",'')				AS "DescEstadoCclv"
			,''												AS "UsoGarantia"
			,''												AS "BlockEnvio"
			,ANT."AccionOperacion"							AS "IdConcepto"
			,ANT."NombreOperacion"							AS "NombreConcepto"
			,''												AS "IndicadorEnvio"
			,ANT."IdEmisor"									AS "IdEmisor"
			,ANT."EsFungible"								AS "EsFungible"
			,ANT."NemoSVS"									AS "NemoSVS"
			,''												AS "IdTipoReajuste"
			,CASE ANT."IdTipoInstrumentoOperacion"
				WHEN 'ACN'	THEN 'AC'
				WHEN 'CFI' THEN  'CFI'
				ELSE INS."IdClasificacionTipoInstrumento"
			END												AS "Categoria"
			,ANT."IdCliente"								AS "IdCliente"
			,ANT."NombreLargoCliente"						AS "IdCliente"
			,EMP."CodigoCorredora"							AS "CodCorr"
			,COALESCE(ESTCUS."IdEstado",'')					AS "IdEstadoCustodia"
			,COALESCE(OPE."EstadoCclv",'')				AS "EstadoCclv"
  FROM "BSCL.Custodia.BD.Vistas::SIM.ConsultarAnticipos" AS ANT
 INNER JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" AS INS ON (INS."IdInstrumento" = ANT."IdInstrumentoOperacion")
  LEFT JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarCorredoraEmpresa" AS EMP ON (EMP."IdEmpresa" =  ANT."IdEmpresa" AND EMP."IdBolsa" = 'BCS')
  LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" AS TOPE ON (TOPE."IdTipoOperacion" = ANT."EGRIdTipoOperacion")
  LEFT JOIN OP_RELACIONADO AS OPE ON (ANT."IdEmpresa" = OPE."IdEmpresa" AND OPE."FolioOperacion" = ANT."FolioCartera")  
  LEFT JOIN "BSCL.Plataforma.BD::PLA.Estado" ESTADO ON ESTADO."IdEstado" = ANT."IdEstado"
  LEFT JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarEstadosCustodia" ESTCUS ON (ESTCUS."NombreEntidad" = 'CUSTODIA.OPERACIONES' 
   AND ESTCUS."IdEstado" = MAP(OPE."IdEstado",'C','ENV','R','REC','M','COM','P','PEN','')) 
   
UNION ALL 

-- VENCIMIENTO SIMULTANEAS
	SELECT  DISTINCT
			CASE IFNULL(OPE."IdInterno", 0)
		   		WHEN 0 THEN 0
		   		ELSE OPE."IdInterno"
		     END											AS "IdInterno"
			,OPE."IdGestion"								AS "IdGestion"
			,'VENC'	 										AS "IdTipo"
			,INS."RazonSocial"								AS "Producto"
			,SIM."IdTipoProducto"							AS "IdTipoProducto"
			,SIM."FolioCartera"								AS "FolioOperacion"
			,SIM."FolioCartera"								AS "IdRegistro"
			,CASE SIM."AccionOperacion"
				WHEN 'COMPRA' THEN 'C'
				WHEN 'VENTA'  THEN 'V'
			END												AS "TipoOperacion"
			,SIM."AccionOperacion"							AS "IdTipoOperacion"
			,SIM."NombreOperacion"							AS "NombreOperacion"
			,SIM."IdTipoInstrumentoOperacion"				AS "IdTipoInstrumento"
			,SIM."suby_Nemotecnico"							AS "Nemo"
			,SIM."IdClasificacionTipoInstrumentoOperacion"	AS "IdClasificacionTipoInstrumento"
			,SIM."IdCondicionLiquidacion"					AS "IdCondicionLiquidacion"
			,CASE SIM."AccionOperacion"
				WHEN 'COMPRA' THEN 'C'
				WHEN 'VENTA'  THEN 'V'
			END												AS "IdOperacion"
			,SIM."AccionOperacion"							AS "Accion"			
			,'N'											AS "OperadorDirecto"
			,SIM."INGFechaOperacion"						AS "FechaIngresoRegistro"
			,SIM."FechaVencimientoOperacion"				AS "FechaVencimiento"
			,'R'											AS "IdTipoDocumentoIdentidad"
			,SIM."DocumentoIdentidad"						AS "DocumentoIdentidad"
			,SIM."IdPortafolio"								AS "IdPortafolio"
			,SIM."CodigoPortafolio"							AS "CodigoPortafolio"
			,SIM."FolioComprobante"							AS "FolioComprobante"
			,0												AS "FolioComprobanteReferencia"
			,COALESCE(OPE."IdOrden",0)						AS "IdOrden"
			,SIM."INGCantidad"								AS "Cantidad"
			,SIM."INGPrecioCompra"							AS "Precio"
			,SIM."INGTasaCompra"							AS "Tasa"
			,SIM."MontoCompra"								AS "Monto"
			,0												AS "MontoComprom"
			,CASE IFNULL(OPE."IdInterno", 0)
				WHEN 0 THEN ''
				ELSE OPE."IdEstado"
			END													AS "IdEstado"
			,CASE IFNULL(OPE."IdInterno", 0)
				WHEN 0 THEN 'DISPONIBLE'
				ELSE CASE OPE."IdEstado"						
						WHEN 'C'	THEN 'ENVIADO'
						WHEN 'R'	THEN 'RECHAZADO'
						WHEN 'M'	THEN 'COMPLETADO'
						WHEN 'P'	THEN 'PENDIENTE'
						ELSE 'NO DISPONIBLE PARA ENVIO'
						END 
			END												AS "DescEstado"			

			,SIM."IdAgente"									AS "IdAgente"
			,SIM."IdMonedaInstrumento"						AS "IdMoneda"
			,'00'											AS "IdTipoGarantia"
			,SIM."NombreOperacion"							AS "DescripcionTipoGarantia"
			,''												AS "FondoAfp"
			,SIM."IdTipoComprobante"						AS "IdTipoComprobante"
			,SIM."IdTipoProducto"							AS "CodigoTipoProducto"
			,SIM."IdBolsa"									AS "IdBolsa"
			,'F'											AS "DocumentoIdentidadEntidad"
			,''												AS "IndDcv"
			,'0'											AS "FolioTransaccion"
			,IFNULL(OPE."EstadoCclvDesc",'')				AS "DescEstadoCclv"
			,''												AS "UsoGarantia"
			,''												AS "BlockEnvio"
			,SIM."AccionOperacion"							AS "IdConcepto"
			,SIM."NombreOperacion"							AS "NombreConcepto"
			,''												AS "IndicadorEnvio"
			,SIM."suby_IdEmisor"							AS "IdEmisor"
			,SIM."suby_EsFungible"							AS "EsFungible"
			,SIM."suby_NemoSVS"								AS "NemoSVS"
			,SIM."suby_IdTipoReajusteSubyacente"			AS "IdTipoReajuste"
			,CASE SIM."IdTipoInstrumentoOperacion"
				WHEN 'ACN'	THEN 'AC'
				WHEN 'CFI' THEN  'CFI'
				ELSE SIM."IdClasificacionTipoInstrumentoOperacion"
			END												AS "Categoria"
			,SIM."IdCliente"								AS "IdCliente"
			,SIM."NombreLargoCliente"						AS "Nombre"
			,EMP."CodigoCorredora"							AS "CodCorr"
			,COALESCE(ESTCUS."IdEstado",'')					AS "IdEstadoCustodia"
			,COALESCE(OPE."EstadoCclv",'')				AS "EstadoCclv"
  FROM "BSCL.Custodia.BD.Vistas::SIM.ConsultarCarteraSimultanea" AS SIM	
 INNER JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" AS INS ON (INS."IdInstrumento" = SIM."IdInstrumentoOperacion")
  LEFT JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarCorredoraEmpresa" AS EMP ON (EMP."IdEmpresa" =  SIM."IdEmpresa" AND EMP."IdBolsa" = 'BCS')
  LEFT JOIN OP_RELACIONADO AS OPE ON (SIM."IdEmpresa" = OPE."IdEmpresa" AND OPE."FolioOperacion" = SIM."FolioCartera")  
  LEFT JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarEstadosCustodia" ESTCUS ON (ESTCUS."NombreEntidad" = 'CUSTODIA.OPERACIONES' 
   AND ESTCUS."IdEstado" = MAP(OPE."IdEstado",'C','ENV','R','REC','M','COM','P','PEN',''))
WHERE SIM."IdEstado" IN ('VIG', 'VCD')

UNION ALL

-- ANTICIPO y LIQUIDACION VENTA CORTA
 SELECT

	CASE IFNULL(OPE."IdInterno", 0)
		WHEN 0 THEN 0
		ELSE OPE."IdInterno"
	 END											AS "IdInterno"
	,OPE."IdGestion"								AS "IdGestion"
	,'VENC'	 										AS "IdTipo"	
	,PRT."NombreTipoPrestamo"						AS "Producto"
	,PRT."IdTipoProducto"							AS "IdTipoProducto"
	,PRT."FolioPrestamo"							AS "FolioOperacion"
	,PRT."FolioPrestamo"								AS "IdRegistro"
	,'VENTA'										AS "TipoOperacion"
	,'VENTA'										AS "IdTipoOperacion"
	,CASE PRT."IdEstado"
		WHEN 'ANT' THEN 'ANTICIPO'
		ELSE  'LIQUIDACION'	
	END												AS "NombreOperacion"
	,PRT."IdTipoInstrumento"						AS "IdTipoInstrumento"
	,PRT."NemoSubyacente"							AS "Nemo"
	,INS."IdClasificacionTipoInstrumento"			AS "IdClasificacionTipoInstrumento"
	,PRT."IdCondicionLiquidacion"					AS "IdCondicionLiquidacion"
	,'V'											AS "IdOperacion"
	,'VENTA'										AS "Accion"			
	,'N'											AS "OperadorDirecto"
	,PRT."FechaPrestamo"							AS "FechaIngresoRegistro"
	,CASE PRT."IdEstado"
		WHEN 'ANT' THEN PRT."EGRFechaOperacion"
		ELSE  PRT."FechaVencimientoPrestamo"	
	END 											AS "FechaVencimiento"
	,'R'											AS "IdTipoDocumentoIdentidad"
	,PRT."DocumentoIdentidad"						AS "DocumentoIdentidad"
	,PRT."IdPortafolio"								AS "IdPortafolio"
	,PRT."CodigoPortafolio"							AS "CodigoPortafolio"
	,PRT."FolioComprobante"							AS "FolioComprobante"
	,0												AS "FolioComprobanteReferencia"
	,COALESCE(OPE."IdOrden",0)						AS "IdOrden"
	,PRT."EGRCantidadPrestamo"						AS "Cantidad"
	,PRT."PrecioInstrumento"						AS "Precio"
	,PRT."TasaPrestamo"								AS "Tasa"
	,PRT."INGMontoTotalPrestamo"					AS "Monto"
	,0												AS "MontoComprom"
	,CASE IFNULL(OPE."IdInterno", 0)
		WHEN 0 THEN ''
		ELSE OPE."IdEstado"
	 END												AS "IdEstado"
	,CASE IFNULL(OPE."IdInterno", 0)
		WHEN 0 THEN 'DISPONIBLE'
		ELSE CASE OPE."IdEstado"						
				WHEN 'C'	THEN 'ENVIADO'
				WHEN 'R'	THEN 'RECHAZADO'
				WHEN 'M'	THEN 'COMPLETADO'
				WHEN 'P'	THEN 'PENDIENTE'
				ELSE 'NO DISPONIBLE PARA ENVIO'
				END
	 END											AS "DescEstado"
			
	,PRT."IdAgente"									AS "IdAgente"
	,PRT."IdMonedaInstrumento"						AS "IdMoneda"
	,'00'											AS "IdTipoGarantia"
	,PRT."INGGlosa"									AS "DescripcionTipoGarantia"
	,''												AS "FondoAfp"
	,PRT."IdTipoComprobante"						AS "IdTipoComprobante"
	,PRT."IdTipoProducto"							AS "CodigoTipoProducto"
	,PRT."IdBolsa"									AS "IdBolsa"
	,'F'											AS "DocumentoIdentidadEntidad"
	,''												AS "IndDcv"
	,'0'											AS "FolioTransaccion"
	,IFNULL(OPE."EstadoCclvDesc",'')				AS "DescEstadoCclv"
	,''												AS "UsoGarantia"
	,''												AS "BlockEnvio"
	,PRT."IdConcepto"								AS "IdConcepto"
	,PRT."NombreConcepto"							AS "NombreConcepto"
	,''												AS "IndicadorEnvio"
	,PRT."IdEmisorSubyacente"						AS "IdEmisor"
	,PRT."EsFungible"								AS "EsFungible"
	,PRT."NemoSVSSubyacente"						AS "NemoSVS"
	,PRT."IdTipoReajuste"							AS "IdTipoReajuste"
	,PRT."IdClasificacionTipoInstrumento"			AS "Categoria"
	,PRT."IdCliente"								AS "IdCliente"
	,PRT."RazonSocial"								AS "Nombre"
	,EMP."CodigoCorredora"							AS "CodCorr"
	,COALESCE(ESTCUS."IdEstado",'')					AS "IdEstadoCustodia"
	,COALESCE(OPE."EstadoCclv",'')					AS "EstadoCclv"
		 
FROM 		"BSCL.Custodia.BD.Vistas::PRT.ConsultarResumenPrestamoVigente"	AS PRT
INNER JOIN 	"BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" 			AS INS 		ON (INS."IdInstrumento" 	= PRT."IdInstrumentoSubyacente")
LEFT JOIN 	"BSCL.Plataforma.BD.Vistas::INS.ConsultarCorredoraEmpresa" 		AS EMP 		ON (EMP."IdEmpresa" 		= PRT."IdEmpresa" 	AND EMP."IdBolsa" = 'BCS')
LEFT JOIN 	OP_RELACIONADO			AS OPE 		ON (PRT."IdEmpresa" 		= OPE."IdEmpresa" 	AND OPE."FolioOperacion" = PRT."FolioPrestamo")  
LEFT JOIN 	"BSCL.Plataforma.BD::PLA.Estado" 								AS EST 		ON EST."IdEstado" 			= PRT."IdEstado"
LEFT JOIN 	"BSCL.Custodia.BD.Vistas::CUS.ConsultarEstadosCustodia" 		AS ESTCUS 	ON (ESTCUS."NombreEntidad" 	= 'CUSTODIA.OPERACIONES' 
   AND ESTCUS."IdEstado" = MAP(OPE."IdEstado",'C','ENV','R','REC','M','COM','P','PEN','')) 

WHERE 
   	PRT."IdEstado" IN ( 'ANT', 'LIQ')
AND PRT."IdTipoPrestamo" IN ('PRTVTC', 'PRVCRF')

UNION ALL
--- FACTURAS VENTA CORTA
SELECT	 DISTINCT
			CASE IFNULL(OPE."IdInterno", 0)
					WHEN 0 THEN 0
					ELSE OPE."IdInterno"
			 END												AS "IdInterno"
			,OPE."IdGestion"									AS "IdGestion"
			,'ASIG'												AS "IdTipo"	 								-- ??		
			,'VENTA CORTA'										AS "Producto"
			,''													AS "IdTipoProducto"							-- ??
			,FAC."FolioTransaccion"                             AS "FolioOperacion"
			,FAC."FolioTransaccion"                             AS "FolioOperacion"
			,'V'												AS "TipoOperacion"							-- ??
			,TOPE."AccionOperacion"								AS "IdTipoOperacion"
			,TCOMP."Nombre"										AS "NombreOperacion"						-- ??
			,FAC."IdTipoInstrumento"							AS "IdTipoInstrumento"						
			
			,FAC."NemotecnicoInstrumento"						AS "Nemo"	
											
			,FAC."IdClasificacionTipoInstrumento"				AS "IdClasificacionTipoInstrumento"		
			,FAC."IdCondicionLiquidacion"						AS "IdCondicionLiquidacion"	
			,'C'												AS "IdOperacion"							-- ??			
			,'INGRESO'											AS "Accion"									-- ??
			,'N'												AS "OperadorDirecto"						-- ??				
			,FAC."FechaSistema"									AS "FechaIngresoRegistro"
			,FAC."FechaVencimiento"								AS "FechaVencimiento"						-- ??						
			,'R'												AS "IdTipoDocumentoIdentidad"				-- ??						
			,Cli."DocumentoIdentidad"							AS "DocumentoIdentidad"
			,FAC."IdPortafolio"									AS "IdPortafolio"
			,PORT."CodigoPortafolio"							AS "CodigoPortafolio"							
			,FAC."IdComprobante"								AS "FolioComprobante"						
			,0													AS "FolioComprobanteReferencia"
			,0													AS "IdOrden"								
			,FAC."Cantidad"										AS "Cantidad"								
			,FAC."Precio"										AS "Precio"								
			,FAC."Tasa"											AS "Tasa"									
			,0													AS "Monto"									
			,FAC."Monto"										AS "MontoComprom"							
			,CASE IFNULL(OPE."IdInterno", 0)
				WHEN 0 THEN ''
				ELSE OPE."IdEstado"
			END													AS "IdEstado"
			,CASE IFNULL(OPE."IdInterno", 0)
				WHEN 0 THEN 'DISPONIBLE'
				ELSE CASE OPE."IdEstado"						
						WHEN 'C'	THEN 'ENVIADO'
						WHEN 'R'	THEN 'RECHAZADO'
						WHEN 'M'	THEN 'COMPLETADO'
						WHEN 'P'	THEN 'PENDIENTE'
						ELSE 'NO DISPONIBLE PARA ENVIO'
						END 
			END													AS "DescEstado"
			
			,0													AS "IdAgente"								-- ??								
			,CASE FAC."IdClasificacionTipoInstrumento"
				WHEN 'RF'	THEN FAC."IdTipoReajuste"
				WHEN 'IF'	THEN FAC."IdTipoReajuste"
				ELSE FAC."IdMoneda"
			END					AS "IdMoneda"								
			,IFNULL(TCT."IdTipoCuenta",'00')					AS "IdTipoGarantia"
			,IFNULL(TCT."Nombre",'')							AS "DescripcionTipoGarantia"
			,''													AS "FondoAfp"								-- ??
			,''													AS "IdTipoComprobante"						-- ??						

			,CASE
				WHEN FAC."IdTipoOperacion" IN ('INGGTA','IT06','INGASI') THEN 'INSI'
				ELSE ''
			END	AS "CodigoTipoProducto"
			
			,EMP."IdBolsa"										AS "IdBolsa"
			,'F'												AS "DocumentoIdentidadEntidad"					
			,CASE IFNULL(OPE."IdInterno", 0)
				WHEN 0 THEN ''
				ELSE OPE."IndDcv"
			END													AS "IndDcv"
			,'0'												AS "FolioTransaccion"						
			,IFNULL(OPE."EstadoCclvDesc",'')					AS "DescEstadoCclv"						
			,''													AS "UsoGarantia"							
			,''													AS "BlockEnvio"							
			,FAC."IdConceptoMovimiento"							AS "IdConcepto"
			,CON."Descripcion"									AS "NombreConcepto"
			,''													AS "IndicadorEnvio"						
			,FAC."IdEmisor"										AS "IdEmisor"								
			,FAC."EsFungible"									AS "EsFungible"							
			,FAC."NemoSVS"										AS "NemoSVS"		
			,FAC."IdTipoReajuste"								AS "IdTipoReajuste"
				, CASE FAC."IdTipoInstrumento"
					WHEN 'ACN'	THEN 'AC'
					WHEN 'CFI' THEN  'CFI'
					ELSE FAC."IdClasificacionTipoInstrumento"
				END												AS "Categoria"								
			
					
			,CLI."IdCliente"									AS "IdCliente"	
			,IFNULL(CLI."NombreCompleto",'')	   		        AS "Nombre"
			,EMP."CodigoCorredora"								AS "CodCorr"
			,COALESCE(ESTCUS."IdEstado",'')						AS "IdEstadoCustodia"
			,COALESCE(OPE."EstadoCclv",'')						AS "EstadoCclv"
	FROM "BSCL.Facturacion.BD::FAC.MovimientoComprobante"									AS  FAC
	LEFT JOIN  OP_RELACIONADO							AS  OPE
	ON (FAC."IdEmpresa" = OPE."IdEmpresa" AND FAC."FolioTransaccion" = OPE."FolioOperacion" )
	
	INNER JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento"						AS	INS
	ON (INS."IdInstrumento" = FAC."IdInstrumento")
	
	
	INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" 							AS 	CLI 
	ON (CLI."IdEmpresa" = FAC."IdEmpresa" 				AND 
		CLI."IdCliente" = FAC."IdCliente")
		
	INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" 										AS PORT
	ON (PORT."IdPortafolio" = FAC."IdPortafolio")
	
	INNER JOIN "BSCL.Custodia.BD::CUS.ConceptoCustodia"										AS CON
		ON (FAC."IdConceptoMovimiento" = CON."IdConcepto"	)
					
	LEFT JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarTipoCuenta"							AS TCT
		ON (TCT."IdModulo" 		= 'CUS' and 
			TCT."IdTipoCuenta" 	= CON."IdTipoCuenta")
	
			INNER JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarCorredoraEmpresa"			AS EMP
			ON (EMP."IdEmpresa"	=  FAC."IdEmpresa"	AND  
				EMP."IdBolsa" 	= 'BCS')		
			
	LEFT JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarEstadosCustodia" ESTCUS
				ON (ESTCUS."NombreEntidad" = 'CUSTODIA.OPERACIONES' AND
					ESTCUS."IdEstado" = MAP(OPE."IdEstado",'C','ENV','R','REC','M','COM','P','PEN',''))		
	
	LEFT JOIN "BSCL.Facturacion.BD::FAC.TipoComprobante"								AS TCOMP
	ON (TCOMP."IdTipoComprobante" = FAC."IdTipoComprobante")
	
	LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion"									AS TOPE
	ON (TOPE."IdTipoOperacion" = FAC."IdTipoOperacion")
	
	
	WHERE 
		 FAC."IdEstadoFacturacion" =  'FAC'
	AND FAC."IdCondicionLiquidacion" !=  'TP'	
	AND FAC."EsVentaCorta" = 'S'
ORDER BY "FolioOperacion" DESC
	
WITH READ ONLY;
GO
GRANT SELECT ON "BSCL.Custodia.BD.Vistas::CUS.ConsultaOperacionesPendientes"  TO  BCS_DESARROLLADOR WITH GRANT OPTION;
GO

