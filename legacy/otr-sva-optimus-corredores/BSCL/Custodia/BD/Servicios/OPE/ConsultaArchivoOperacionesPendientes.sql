SET SCHEMA "BSCL";
GO
--* --------------------------------------------------------------------------------------   
DROP PROCEDURE  "BSCL.Custodia.BD.Servicios::OPE.ConsultaArchivoOperacionesPendientes";
GO

--* 
--* Proposito .....: Consultar operaciones pendintes de para envio al DCV
--* Autor .........: MSH
--* Fecha Creacion : Noviembre 2016
--* Mofidicaciones
--* Autor .........: CGO
--* Fecha Creacion : Mayo 2019
--* Jira		   : CELCONDOR-434 Permitir realizar Traspasos y Transferencia de TÃ­tulos al DCV
--* Autor .........: RAM
--* Fecha Creacion : Octubre 2019
--* Jira		   : CELCONDOR-659 Transferencia de Títulos a Depósito de Valores
--* --------------------------------------------------------------------------------------
CREATE PROCEDURE "BSCL.Custodia.BD.Servicios::OPE.ConsultaArchivoOperacionesPendientes"(
    --VARIABLES DE ENTRADA OBLIGATORIAS
    IN p_IdEmpresa  BIGINT,
    IN p_IdUsuario  BIGINT,
    IN p_IdModulo   NVARCHAR(4),
    IN p_IdPais     BIGINT,
 
    --* Variables del SP
    IN p_Fecha              DATE,
    IN p_IdSocioNegocio     BIGINT,
    IN p_IdMercado          NVARCHAR(3),
    IN p_IdEstado           NVARCHAR(3),    
    IN p_IdTipoOperacion    NVARCHAR(3),
    IN p_IdTipoComprobante  NVARCHAR(3),
    IN p_IdTipoCuenta       NVARCHAR(3),    
    
    OUT p_out_Salida        TABLE (
        "IdInterno"                         BIGINT
        ,"FolioOperacion"                   BIGINT
        ,"IdTipoOperacion"                  NVARCHAR(6)
        ,"NombreOperacion"                  NVARCHAR(100)
        ,"IdTipoInstrumento"                NVARCHAR(3)
        ,"Nemo"                             NVARCHAR(40)
        ,"IdClasificacionTipoInstrumento"   NVARCHAR(3)
        ,"IdCondicionLiquidacion"           NVARCHAR(40)
        ,"Accion"                           NVARCHAR(6)
        ,"OperadorDirecto"                  NVARCHAR(1)
        ,"FechaIngresoRegistro"             TIMESTAMP
        ,"FechaVencimiento"                 TIMESTAMP
        ,"IdTipoDocumentoIdentidad"         NVARCHAR(1)
        ,"DocumentoIdentidad"               NVARCHAR(40)
        ,"IdPortafolio"                     BIGINT
        ,"IdCliente"                        BIGINT      
        ,"FolioComprobante"                 BIGINT
        ,"FolioComprobanteReferencia"       BIGINT
        ,"FolioComprobanteRelacionado"      BIGINT
        ,"IdOrden"                          BIGINT
        ,"Cantidad"                         DECIMAL(26,8)
        ,"Precio"                           DECIMAL(26,8)
        ,"Tasa"                             DECIMAL(26,8)
        ,"Monto"                            DECIMAL(26,8)
        ,"MontoComprom"                     DECIMAL(26,8)
        ,"IdEstado"                         NVARCHAR(3)
        ,"IdAgente"                         BIGINT
        ,"IdMoneda"                         NVARCHAR(3)
        ,"TipoGarantia"                     NVARCHAR(5000)
        ,"FondoAfp"                         NVARCHAR(2)
        ,"IdTipoComprobante"                NVARCHAR(3)
        ,"CodigoTipoProducto"               NVARCHAR(40)
        ,"NombreProducto"                   NVARCHAR(100)
        ,"IdBolsa"                          NVARCHAR(4)
        ,"IdBoveda"                         NVARCHAR(4)
        ,"FolioTransaccion"                 NVARCHAR(40)
        ,"DescEstadoCclv"                   NVARCHAR(300)
        ,"UsoGarantia"                      NVARCHAR(1)
        ,"TipoOpePendiente"                 NVARCHAR(1)
        ,"BlockEnvio"                       NVARCHAR(1)
        ,"IdConcepto"                       NVARCHAR(6)
        ,"NombreConcepto"                   NVARCHAR(100)
        ,"IndicadorEnvio"                   NVARCHAR(1)
        ,"IdEmisor"                         NVARCHAR(40)
        ,"EsFungible"                       NVARCHAR(1)
        ,"NemoSVS"                          NVARCHAR(40)
        ,"IdTipoReajuste"                   NVARCHAR(3)
        ,"Categoria"                        NVARCHAR(3)
        ,"TipoOperacionArchivo"				NVARCHAR(2)
        ,"Relacion"                         NVARCHAR(3)  --[CELCONDOR-659]
        ,"NombreCliente"                    NVARCHAR(60) --[CELCONDOR-659]
        ,"TipoSocioNegocio"                 NVARCHAR(20) --[CELCONDOR-659]
        ,"CuentaClienteDCVDepositante"      NVARCHAR(520) --[CELCONDOR-659]
        ,"IdOperacionPendiente"             BIGINT
        ,"IdEstadoProceso"                  NVARCHAR(3)
        ,"Clave"                            NVARCHAR(15)
        ,"CodigoPortafolio"                 NVARCHAR(20)        
        ,"CuentaClienteDCV"                 NVARCHAR(10)
    ),

     --Salidas para mensajes de advertencias y errores
	OUT p_out_ErroresContador 	INT,
	OUT p_out_ErroresDetalle 	TABLE (	
									"IdInterno" NVARCHAR(40), 
									"Codigo" NVARCHAR(203),
									"Descripcion" NVARCHAR(520) )
) 
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER 
DEFAULT SCHEMA BSCL
AS
BEGIN


    DECLARE v_NroAdvertencias INT;
    DECLARE v_NroErrores INT;
    DECLARE v_IdModulo NVARCHAR(3);
    DECLARE v_IdCliente INT;
	DECLARE	v_FechaOperacionSecondate	SECONDDATE;	
	--[CELCONDOR-659]
    DECLARE v_DocumentoIdentidadCorredora NVARCHAR(40);
	DECLARE v_CuentaDCVCuentaRelacionado  NVARCHAR(520);
    DECLARE v_CuentaDCVCuentaPropia       NVARCHAR(520);
    DECLARE v_CuentaDCVCuentaTerceros     NVARCHAR(520);
    DECLARE v_CuentaDCVCuentaLiquidacion  NVARCHAR(520);
    
    SELECT "DocumentoIdentidad" INTO v_DocumentoIdentidadCorredora 
    FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa" WHERE "IdEmpresa" = p_IdEmpresa;
   
   	SELECT "Valor"  INTO v_CuentaDCVCuentaRelacionado
    FROM  "BSCL.Custodia.BD::CUS.Configuracion"	Config WHERE Config."IdEmpresa" = 1 AND 
	Config."CodigoParametro" = 'CuentaDCVCuentaRelacionado';
    
	SELECT "Valor"  INTO v_CuentaDCVCuentaPropia
    FROM  "BSCL.Custodia.BD::CUS.Configuracion"	Config WHERE Config."IdEmpresa" = 1 AND 
	Config."CodigoParametro" = 'CuentaDCVCuentaPropia';
    
	SELECT "Valor"  INTO v_CuentaDCVCuentaTerceros
    FROM  "BSCL.Custodia.BD::CUS.Configuracion"	Config WHERE Config."IdEmpresa" = 1 AND 
	Config."CodigoParametro" = 'CuentaDCVCuentaTerceros';
    
	SELECT "Valor"  INTO v_CuentaDCVCuentaLiquidacion
    FROM  "BSCL.Custodia.BD::CUS.Configuracion"	Config WHERE Config."IdEmpresa" = 1 AND 
	Config."CodigoParametro" = 'CuentaDCVCuentaLiquidacion';
	--[CELCONDOR-659]
	
    v_IdModulo = LEFT(p_IdModulo,3);
	CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema" 
		( :p_IdEmpresa, v_FechaOperacionSecondate );				

	--* Para el filtro de Socio de Negocio
	v_IdCliente = 0;
	IF p_IdSocioNegocio > 0 THEN
		SELECT	"IdCliente" INTO v_IdCliente
		FROM	"BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente"
		WHERE	"IdSocioDeNegocio" = p_IdSocioNegocio;
	END IF;
	v_IdCliente = COALESCE( v_IdCliente, 0 );

	--* 
	--* Para el caso de las facturas cuando es TC se genera automaticamente la clave con el 
	--* Nro. de la factura.
	tbl_FD =
        SELECT  
                ROW_NUMBER() OVER (ORDER BY  DCOMP."IdMovimientoComprobante")       AS "IdInterno"                          
                ,DCOMP."IdMovimientoComprobante"                AS "FolioOperacion"                 
                ,DCOMP."IdTipoComprobante"                      AS "IdTipoOperacion"                    
                ,TOPER."Nombre"                                 AS "NombreOperacion"                    
                ,DCOMP."IdTipoInstrumento"                      AS "IdTipoInstrumento"                  
                ,CASE WHEN DCOMP."IdTipoProducto" = 'INSI' 
                      THEN DCOMP."NemotecnicoSubyacente" 
                      ELSE DCOMP."NemotecnicoInstrumento"  END  AS "Nemo"                               
                
                ,DCOMP."IdClasificacionTipoInstrumento"         AS "IdClasificacionTipoInstrumento" 
                ,DCOMP."CodigoCondicionDeLiquidacion"           AS "IdCondicionLiquidacion"         
                ,DCOMP."IdTipoOperacion"                        AS "Accion"                         
                ,'N'                                            AS "OperadorDirecto"                    
                ,DCOMP."FechaMovimientoComprobante"             AS "FechaIngresoRegistro"               
                ,DCOMP."FechaMovimientoComprobante"             AS "FechaVencimiento"                   
                ,DCOMP."TipoDocumentoIdentidad"                 AS "IdTipoDocumentoIdentidad"           
                ,DCOMP."DocumentoIdentidad"                     AS "DocumentoIdentidad" 
                ,DCOMP."IdPortafolio"                           AS "IdPortafolio"                       
                ,DCOMP."IdCliente"                              AS "IdCliente"                          
                ,DCOMP."FolioComprobante"                       AS "FolioComprobante"                   
                ,DCOMP."FolioComprobanteReferencia"             AS "FolioComprobanteReferencia"     
                ,0                                              AS "FolioComprobanteRelacionado"        
                ,DCOMP."IdOrden"                                AS "IdOrden"                            
                ,DCOMP."Cantidad"                               AS "Cantidad"                           
                ,DCOMP."Precio"                                 AS "Precio"                         
                ,DCOMP."Tasa"                                   AS "Tasa"                               
                ,DCOMP."Monto"                                  AS "Monto"                              
                ,DCOMP."MontoMovimientoComprobante"             AS "MontoComprom"                       
                ,DCOMP."IdEstadoComprobante"                    AS "IdEstado"                           
                ,DCOMP."IdEmpleadoEjecutivoPortafolio"          AS "IdAgente"                           
                ,DCOMP."IdMoneda"                               AS "IdMoneda"                           
                ,''                                             AS "TipoGarantia"                       
                ,CHAR(2)                                        AS "FondoAfp"                           
                ,DCOMP."IdTipoComprobante"                      AS "IdTipoComprobante"                  
                ,DCOMP."CodigoTipoProducto"                     AS "CodigoTipoProducto"             
                ,PRODU."Nombre"                                 AS "NombreProducto"                 
                ,DCOMP."IdBolsa"                                AS "IdBolsa"                            
                ,DCOMP."IdBoveda"                               AS "IdBoveda"                           
                ,DCOMP."FolioTransaccion"                       AS "FolioTransaccion"                   
                ,' '                                            AS "DescEstadoCclv"                 
                ,' '                                            AS "UsoGarantia"                        
                ,' '                                            AS "TipoOpePendiente"                   
                ,' '                                            AS "BlockEnvio"                     
                ,DCOMP."IdConceptoMovimiento"                   AS "IdConcepto"                     
                ,CONCE."Nombre"                                 AS "NombreConcepto"                 
                ,' '                                            AS "IndicadorEnvio"                 
                ,DCOMP."IdEmisor"                               AS "IdEmisor"                           
                ,DCOMP."EsFungible"                             AS "EsFungible"                     
                ,' '                                            AS "NemoSVS"                            
                ,DCOMP."IdTipoReajuste"                         AS "IdTipoReajuste"                 
                ,'FD'                                           AS "Categoria"  
				,CASE WHEN DCOMP."IdFormaCustodia" = 'CUS' 
				 	  THEN 'TC' ELSE 'CV' END 					AS "TipoOperacionArchivo"
				--[CELCONDOR-659] 	  
				,CASE 
	    			  WHEN DCOMP."DocumentoIdentidad" = v_DocumentoIdentidadCorredora  THEN 'CP'
					  WHEN IndCli."ValorIndicador" = 'S'   		                       THEN 'RE'
					  ELSE    'NR'					  							       END  AS "Relacion"					  
				,LEFT(Cli."NombreCompleto",60)                          					AS "NombreCliente"
				,CASE 
	    			  WHEN Contra."IdContraparte" IS NOT NULL THEN 'CONTRAPARTE'  
					  WHEN Cli."IdCliente" IS NOT NULL THEN 'CLIENTE' END  					AS "TipoSocioNegocio"
				,CASE 
	    			  WHEN DCOMP."DocumentoIdentidad" = v_DocumentoIdentidadCorredora  THEN v_CuentaDCVCuentaPropia
					  WHEN IndCli."ValorIndicador" = 'S'   		                       THEN v_CuentaDCVCuentaRelacionado
					  ELSE v_CuentaDCVCuentaTerceros	  							   END  AS "CuentaClienteDCVDepositante"
				--[CELCONDOR-659]
				 	  
        FROM    "BSCL.Facturacion.BD.Vistas::ConsultarDetalleMovimientoDeComprobante"    AS DCOMP
                INNER JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarTipoOperacion"              AS TOPER 
                ON (DCOMP."IdTipoOperacion" = TOPER."IdTipoOperacion" AND DCOMP."IdTipoInstrumento" = TOPER."IdTipoInstrumento")                
                INNER JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarConcepto"                   AS CONCE
                ON (DCOMP."IdConceptoMovimiento" = CONCE."IdConcepto")                      
                INNER JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarTipoProducto"               AS PRODU
                ON (DCOMP."CodigoTipoProducto" = PRODU."IdTipoProducto")
                --[CELCONDOR-659]
                LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente"          AS IndCli
				ON  DCOMP."IdCliente" = IndCli."IdCliente" AND IndCli."CodigoTipoIndicador"  = 'RELCORR'				
				LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente"                   AS Cli 
				ON Cli."IdCliente" =  IndCli."IdCliente"
				LEFT JOIN "BSCL.SocioNegocio.BD::CNT.Contraparte"   							AS Contra
				ON Contra."IdPersona" = Cli."IdPersona"
				--[CELCONDOR-659]
                
        WHERE   (   TO_VARCHAR("FechaLiquidacion", 'YYYY-MM-DD') = TO_VARCHAR(p_Fecha, 'YYYY-MM-DD') OR 
                    TO_VARCHAR(p_Fecha, 'YYYY-MM-DD') = '1900-01-01' )
                AND DCOMP."IdEstadoComprobante" IN ('FAC', 'ENV')
                AND DCOMP."IdTipoProducto" IN ('INIF', 'INRF', 'INAC', 'INSI')
                AND ( p_IdTipoComprobante = 'FD' OR p_IdTipoComprobante = '' );


	tbl_GTIA = 
        SELECT                         
				 ROW_NUMBER() OVER (ORDER BY  GARAN."FolioComprobante") AS "IdInterno"
				,GARAN."FolioComprobante"                     		AS "FolioOperacion"
                ,CASE WHEN GARAN."IdEstado" = 'LIQ' 
                	  THEN 'RETGTA'
                 	  ELSE 'INGGTA' END								AS "IdTipoOperacion"                        
                ,TOPER."Nombre"                                     AS "NombreOperacion"                        
                ,GARAN."IdTipoInstrumento"                          AS "IdTipoInstrumento"                      
                ,GARAN."Nemotecnico"                                AS "Nemo"                                   
                ,GARAN."IdClasificacionTipoInstrumento"             AS "IdClasificacionTipoInstrumento"     
                ,GARAN."IdCondicionLiquidacion"                     AS "IdCondicionLiquidacion"             
                ,''                                                 AS "Accion"                             
                ,'N'                                                AS "OperadorDirecto"                    
                ,GARAN."FechaIngresoRegistro"                       AS "FechaIngresoRegistro"                   
                ,GARAN."FechaVencimientoOperacion"                  AS "FechaVencimiento"                       
                ,'R'                                                AS "IdTipoDocumentoIdentidad"               
                ,Cli."DocumentoIdentidad"                           AS "DocumentoIdentidad"     
                ,GARAN."IdPortafolio"                               AS "IdPortafolio"           
                ,GARAN."IdCliente"                                  AS "IdCliente"                                          
                ,0                                                  AS "FolioComprobante"                       
                ,0                                                  AS "FolioComprobanteReferencia"         
                ,0                                                  AS "FolioComprobanteRelacionado"            
                ,0                                                  AS "IdOrden"                                
				,SUM(GARAN."Cantidad")                              AS "Cantidad"
                ,GARAN."PrecioGarantia"                             AS "Precio"                             
                ,GARAN."TasaGarantia"                               AS "Tasa"                                   
                ,GARAN."MontoGarantia"                              AS "Monto"                                  
                ,0                                                  AS "MontoComprom"                           
                ,GARAN."IdEstado"                                   AS "IdEstado"                               
                ,GARAN."IdAgente"                                   AS "IdAgente"                               
                ,GARAN."IdMonedaInstrumento"                        AS "IdMoneda"                               
                ,''                                                 AS "TipoGranatia"                           
                ,CHAR(2)                                            AS "FondoAfp"                               
                ,''                                                 AS "IdTipoComprobante"                      
                ,'CUST'                                             AS "CodigoTipoProducto"
                ,PRODU."Nombre"                                     AS "NombreProducto"                 

--              Pendiente...
--              ,GARAN."IdBolsaOperacionOrigen"                     AS "IdBolsa"                                                
                ,'BCS'                                              AS "IdBolsa"                                                                
--              Pendiente...                
--              ,GARAN."IdBovedaOperacionOrigen"                    AS "IdBoveda"                                               
                ,'CORR'                                             AS "IdBoveda"                                               
                ,'0'                                                AS "FolioTransaccion"                       
                ,' '                                                AS "DescEstadoCclv"                     
                ,' '                                                AS "UsoGarantia"                            
                ,' '                                                AS "TipoOpePendiente"                       
                ,' '                                                AS "BlockEnvio"                         
                ,GARAN."IdConcepto"                                 AS "IdConcepto"                         
                ,Concepto."Nombre"                                  AS "NombreConcepto"                     
                ,' '                                                AS "IndicadorEnvio"                     
                ,GARAN."IdEmisor"                                   AS "IdEmisor"                               
                ,GARAN."EsFungible"                                 AS "EsFungible"                         
                ,GARAN."NemoSVS"                                    AS "NemoSVS"                                
                ,GARAN."IdTipoReajuste"                             AS "IdTipoReajuste"
                ,'GT'                                               AS "Categoria"  
                ,'TC'												AS "TipoOperacionArchivo"
                --[CELCONDOR-659] 	  
				,CASE 
	    			  WHEN Cli."DocumentoIdentidad" = v_DocumentoIdentidadCorredora    THEN 'CP'
					  WHEN IndCli."ValorIndicador" = 'S'   		                       THEN 'RE'
					  ELSE    'NR'					  							       END  AS "Relacion"	  
				
				,LEFT(Cli."NombreCompleto",60)                      AS "NombreCliente"
				,CASE 
	    			  WHEN Contra."IdContraparte" IS NOT NULL THEN 'CONTRAPARTE'  
					  WHEN Cli."IdCliente" IS NOT NULL THEN 'CLIENTE' END  AS "TipoSocioNegocio"
				,CASE 
	    			  WHEN Cli."DocumentoIdentidad" = v_DocumentoIdentidadCorredora  THEN v_CuentaDCVCuentaPropia
					  WHEN IndCli."ValorIndicador" = 'S'   		                       THEN v_CuentaDCVCuentaRelacionado
					  ELSE v_CuentaDCVCuentaTerceros	  							   END  AS "CuentaClienteDCVDepositante"
				--[CELCONDOR-659]
				
        FROM    "BSCL.Custodia.BD.Vistas::GTA.ConsultarTablaGarantia"                       AS GARAN
                INNER JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarTipoOperacion"          AS TOPER 
                ON ( CASE WHEN GARAN."IdEstado" = 'LIQ' 
                	  THEN 'RETGTA' ELSE 'INGGTA' END = TOPER."IdTipoOperacion" )
                LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" 				AS Cli 
                ON (Cli."IdEmpresa" = GARAN."IdEmpresa" and Cli."IdCliente" = GARAN."IdCliente")
                INNER JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarTipoProducto"       PRODU
                ON ('CUST' = PRODU."IdTipoProducto")                
                LEFT JOIN "BSCL.Plataforma.BD::PLA.Concepto" 								AS Concepto 
                ON (GARAN."IdConcepto" = Concepto."IdConcepto")
                
                --[CELCONDOR-659]
                LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente"      AS IndCli
				ON IndCli."IdCliente" =  GARAN."IdCliente" AND IndCli."CodigoTipoIndicador"  = 'RELCORR'
				LEFT JOIN "BSCL.SocioNegocio.BD::CNT.Contraparte"   							AS Contra
				ON Contra."IdPersona" = Cli."IdPersona"
				--[CELCONDOR-659]
                
        WHERE   (	(   TO_VARCHAR(GARAN."INGFechaLiquidacion", 'YYYY-MM-DD') = TO_VARCHAR(p_Fecha, 'YYYY-MM-DD')  
				     	AND GARAN."IdEstado" = 'VIG' )
                	OR
					(   TO_VARCHAR(GARAN."EGRFechaLiquidacion", 'YYYY-MM-DD') = TO_VARCHAR(p_Fecha, 'YYYY-MM-DD')  
                		AND GARAN."IdEstado" = 'LIQ' )                	
                	OR
					(   TO_VARCHAR(p_Fecha, 'YYYY-MM-DD') = '1900-01-01'
                		AND GARAN."IdEstado" IN ('VIG','LIQ' )  )
                )
                AND GARAN."IdClasificacionTipoInstrumento" IN ('IF', 'RF', 'RV')
                AND ( p_IdTipoComprobante = 'GT' OR p_IdTipoComprobante = '' )
		GROUP BY
				GARAN."FolioComprobante",TOPER."Nombre",GARAN."IdTipoInstrumento",GARAN."Nemotecnico",
				GARAN."IdClasificacionTipoInstrumento",GARAN."IdCondicionLiquidacion",GARAN."FechaIngresoRegistro",
				GARAN."FechaVencimientoOperacion",Cli."DocumentoIdentidad",GARAN."IdPortafolio",GARAN."IdCliente",
				GARAN."PrecioGarantia",GARAN."TasaGarantia",GARAN."MontoGarantia",GARAN."IdEstado",GARAN."IdAgente",
				GARAN."IdMonedaInstrumento",PRODU."Nombre",GARAN."IdConcepto",Concepto."Nombre",GARAN."IdEmisor",
				GARAN."EsFungible",GARAN."NemoSVS",GARAN."IdTipoReajuste",IndCli."ValorIndicador",CLI."NombreCompleto", --[CELCONDOR-659]
				CLI."IdCliente",Contra."IdContraparte"; --[CELCONDOR-659]
	
	/*----------------MOVIMIENTO DE TITULOS------------*/				
tbl_MOVTIT = 
        SELECT                         
				 ROW_NUMBER() OVER (ORDER BY  GARAN."FolioComprobante") AS "IdInterno"
				,GARAN."FolioComprobante"                     		AS "FolioOperacion"
                ,CASE WHEN GARAN."IdEstado" = 'LIQ' 
                	  THEN 'RETGTA'
                 	  ELSE 'INGGTA' END								AS "IdTipoOperacion"                        
                ,TOPER."Nombre"                                     AS "NombreOperacion"                        
                ,GARAN."IdTipoInstrumento"                          AS "IdTipoInstrumento"                      
                ,GARAN."Nemotecnico"                                AS "Nemo"                                   
                ,GARAN."IdClasificacionTipoInstrumento"             AS "IdClasificacionTipoInstrumento"     
                ,GARAN."IdCondicionLiquidacion"                     AS "IdCondicionLiquidacion"             
                ,''                                                 AS "Accion"                             
                ,'N'                                                AS "OperadorDirecto"                    
                ,GARAN."FechaIngresoRegistro"                       AS "FechaIngresoRegistro"                   
                ,GARAN."FechaVencimientoOperacion"                  AS "FechaVencimiento"                       
                ,'R'                                                AS "IdTipoDocumentoIdentidad"               
                ,Cli."DocumentoIdentidad"                           AS "DocumentoIdentidad"     
                ,GARAN."IdPortafolio"                               AS "IdPortafolio"           
                ,GARAN."IdCliente"                                  AS "IdCliente"                                          
                ,0                                                  AS "FolioComprobante"                       
                ,0                                                  AS "FolioComprobanteReferencia"         
                ,0                                                  AS "FolioComprobanteRelacionado"            
                ,0                                                  AS "IdOrden"                                
				,SUM(GARAN."Cantidad")                              AS "Cantidad"
                ,GARAN."PrecioGarantia"                             AS "Precio"                             
                ,GARAN."TasaGarantia"                               AS "Tasa"                                   
                ,GARAN."MontoGarantia"                              AS "Monto"                                  
                ,0                                                  AS "MontoComprom"                           
                ,GARAN."IdEstado"                                   AS "IdEstado"                               
                ,GARAN."IdAgente"                                   AS "IdAgente"                               
                ,GARAN."IdMonedaInstrumento"                        AS "IdMoneda"                               
                ,''                                                 AS "TipoGranatia"                           
                ,CHAR(2)                                            AS "FondoAfp"                               
                ,''                                                 AS "IdTipoComprobante"                      
                ,'CUST'                                             AS "CodigoTipoProducto"
                ,PRODU."Nombre"                                     AS "NombreProducto"                 

--              Pendiente...
--              ,GARAN."IdBolsaOperacionOrigen"                     AS "IdBolsa"                                                
                ,'BCS'                                              AS "IdBolsa"                                                                
--              Pendiente...                
--              ,GARAN."IdBovedaOperacionOrigen"                    AS "IdBoveda"                                               
                ,'CORR'                                             AS "IdBoveda"                                               
                ,'0'                                                AS "FolioTransaccion"                       
                ,' '                                                AS "DescEstadoCclv"                     
                ,' '                                                AS "UsoGarantia"                            
                ,' '                                                AS "TipoOpePendiente"                       
                ,' '                                                AS "BlockEnvio"                         
                ,GARAN."IdConcepto"                                 AS "IdConcepto"                         
                ,Concepto."Nombre"                                  AS "NombreConcepto"                     
                ,' '                                                AS "IndicadorEnvio"                     
                ,GARAN."IdEmisor"                                   AS "IdEmisor"                               
                ,GARAN."EsFungible"                                 AS "EsFungible"                         
                ,GARAN."NemoSVS"                                    AS "NemoSVS"                                
                ,GARAN."IdTipoReajuste"                             AS "IdTipoReajuste"
                ,'GT'                                               AS "Categoria"  
                ,'TC'												AS "TipoOperacionArchivo"
                --[CELCONDOR-659] 	  
				,CASE 
	    			  WHEN Cli."DocumentoIdentidad" = v_DocumentoIdentidadCorredora    THEN 'CP'
					  WHEN IndCli."ValorIndicador" = 'S'   		                       THEN 'RE'
					  ELSE    'NR'					  							       END  AS "Relacion"	  
				
				,LEFT(Cli."NombreCompleto",60)                      AS "NombreCliente"
				,CASE 
	    			  WHEN Contra."IdContraparte" IS NOT NULL THEN 'CONTRAPARTE'  
					  WHEN Cli."IdCliente" IS NOT NULL THEN 'CLIENTE' END  AS "TipoSocioNegocio"
				,CASE 
	    			  WHEN Cli."DocumentoIdentidad" = v_DocumentoIdentidadCorredora  THEN v_CuentaDCVCuentaPropia
					  WHEN IndCli."ValorIndicador" = 'S'   		                       THEN v_CuentaDCVCuentaRelacionado
					  ELSE v_CuentaDCVCuentaTerceros	  							   END  AS CuentaClienteDCVDepositante		  
				--[CELCONDOR-659]
				
        FROM    "BSCL.Custodia.BD.Vistas::GTA.ConsultarTablaGarantia"                       AS GARAN
                INNER JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarTipoOperacion"          AS TOPER 
                ON ( CASE WHEN GARAN."IdEstado" = 'LIQ' 
                	  THEN 'RETGTA' ELSE 'INGGTA' END = TOPER."IdTipoOperacion" )
                LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" 				AS Cli 
                ON (Cli."IdEmpresa" = GARAN."IdEmpresa" and Cli."IdCliente" = GARAN."IdCliente")
                INNER JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarTipoProducto"       PRODU
                ON ('CUST' = PRODU."IdTipoProducto")                
                LEFT JOIN "BSCL.Plataforma.BD::PLA.Concepto" 								AS Concepto 
                ON (GARAN."IdConcepto" = Concepto."IdConcepto")
                
                --[CELCONDOR-659]
                LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente"      AS IndCli
				ON IndCli."IdCliente" =  GARAN."IdCliente" AND IndCli."CodigoTipoIndicador"  = 'RELCORR'
				LEFT JOIN "BSCL.SocioNegocio.BD::CNT.Contraparte"   							AS Contra
				ON Contra."IdPersona" = Cli."IdPersona"
				--[CELCONDOR-659]
                
        WHERE   ((TO_VARCHAR(GARAN."INGFechaLiquidacion", 'YYYY-MM-DD') = TO_VARCHAR(p_Fecha, 'YYYY-MM-DD')  
				     	AND GARAN."IdEstado" = 'VIG' )
                	OR
					(   TO_VARCHAR(GARAN."EGRFechaLiquidacion", 'YYYY-MM-DD') = TO_VARCHAR(p_Fecha, 'YYYY-MM-DD')  
                		AND GARAN."IdEstado" = 'LIQ' )                	
                	OR
					(   TO_VARCHAR(p_Fecha, 'YYYY-MM-DD') = '1900-01-01'
                		AND GARAN."IdEstado" IN ('VIG','LIQ' )  )
                )
                AND GARAN."IdClasificacionTipoInstrumento" IN ('IF', 'RF', 'RV')
                AND ( p_IdTipoComprobante = 'GT' OR p_IdTipoComprobante = '' )
		GROUP BY
				GARAN."FolioComprobante",TOPER."Nombre",GARAN."IdTipoInstrumento",GARAN."Nemotecnico",
				GARAN."IdClasificacionTipoInstrumento",GARAN."IdCondicionLiquidacion",GARAN."FechaIngresoRegistro",
				GARAN."FechaVencimientoOperacion",Cli."DocumentoIdentidad",GARAN."IdPortafolio",GARAN."IdCliente",
				GARAN."PrecioGarantia",GARAN."TasaGarantia",GARAN."MontoGarantia",GARAN."IdEstado",GARAN."IdAgente",
				GARAN."IdMonedaInstrumento",PRODU."Nombre",GARAN."IdConcepto",Concepto."Nombre",GARAN."IdEmisor",
				GARAN."EsFungible",GARAN."NemoSVS",GARAN."IdTipoReajuste",IndCli."ValorIndicador",CLI."NombreCompleto", --[CELCONDOR-659]
				CLI."IdCliente",Contra."IdContraparte"; --[CELCONDOR-659]
				
				
tbl_MOVTRAN =		
		SELECT 
				 ROW_NUMBER() OVER (ORDER BY  TRANSITO."FolioComprobante") 	AS "IdInterno"
				,TRANSITO."FolioComprobante"                     			AS "FolioOperacion"
                ,TRANSITO."IdTipoOperacion"									AS "IdTipoOperacion"                        
                ,TOPER."Nombre"                                     		AS "NombreOperacion"                        
                ,TRANSITO."IdTipoInstrumento"                          		AS "IdTipoInstrumento"                      
                ,TRANSITO."Nemotecnico"                               		AS "Nemo"                                   
                ,TRANSITO."IdClasificacionTipoInstrumento"             		AS "IdClasificacionTipoInstrumento"     
                ,TRANSITO."IdCondicionLiquidacion"                     		AS "IdCondicionLiquidacion"             
                ,TRANSITO."IdTipoOperacion"                               	AS "Accion"                             
                ,'N'                                                		AS "OperadorDirecto"                    
                ,TRANSITO."FechaOperacion"			 						AS "FechaIngresoRegistro"                   
                ,TRANSITO."FechaVencimientoOperacion"						AS "FechaVencimiento"                       
                ,CLI."TipoDocumentoIdentidad"								AS "IdTipoDocumentoIdentidad"               
                ,CLI."DocumentoIdentidad"                           		AS "DocumentoIdentidad"     
                ,TRANSITO."IdPortafolio"                               		AS "IdPortafolio"           
                ,TRANSITO."IdCliente"                                 		AS "IdCliente"                                          
                ,0                                                  		AS "FolioComprobante"                       
                ,0                                                  		AS "FolioComprobanteReferencia"         
                ,0                                                  		AS "FolioComprobanteRelacionado"            
                ,0                                                  		AS "IdOrden"                                      
				,TRANSITO."Cantidad"										AS "Cantidad"
                ,TRANSITO."PrecioCompra"		                            AS "Precio"                             
                ,TRANSITO."TasaCompra"			                            AS "Tasa"                                   
                ,TRANSITO."MontoCompra"										AS "Monto"                            
                ,TRANSITO."MontoCompra"										AS "MontoComprom"                           
                ,TRANSITO."IdEstado"                                   		AS "IdEstado"                               
                ,TRANSITO."IdAgente"                                   		AS "IdAgente"                               
                ,TRANSITO."IdMonedaInstrumento"                        		AS "IdMoneda"                               
                ,''                                                 		AS "TipoGranatia"                           
                ,CHAR(2)                                            		AS "FondoAfp"                               
                ,''                                                 		AS "IdTipoComprobante"                      
                ,'CUST'                                             		AS "CodigoTipoProducto"
                ,PRODU."Nombre"                                     		AS "NombreProducto"                 
--              Pendiente...
--              ,GARAN."IdBolsaOperacionOrigen"                     AS "IdBolsa"                                                
                ,TRANSITO."IdBolsa"											AS "IdBolsa"                                                                
--              Pendiente...                
--              ,GARAN."IdBovedaOperacionOrigen"                    AS "IdBoveda"                                               
                ,TRANSITO."IdBoveda"                                		AS "IdBoveda"                                               
                ,TRANSITO."FolioExternoReferencia"							AS "FolioTransaccion"                       
                ,' '                                                		AS "DescEstadoCclv"                     
                ,' '                                                		AS "UsoGarantia"                            
                ,' '                                                		AS "TipoOpePendiente"                       
                ,' '                                               			AS "BlockEnvio"                         
                ,TRANSITO."IdConcepto"                                 		AS "IdConcepto"                         
                ,CONCEPTO."Nombre"                                  		AS "NombreConcepto"                     
                ,' '                                                		AS "IndicadorEnvio"                     
                ,TRANSITO."IdEmisor"                                   		AS "IdEmisor"                               
                ,TRANSITO."EsFungible"                                 		AS "EsFungible"                         
                ,TRANSITO."NemoSVS"                                    		AS "NemoSVS"                                
                ,TRANSITO."IdTipoReajuste"                             		AS "IdTipoReajuste"
                ,'TR'                                               		AS "Categoria"  
                ,'TC'														AS "TipoOperacionArchivo"
                --[CELCONDOR-659] 	  
				,CASE 
	    			  WHEN CLI."DocumentoIdentidad" = v_DocumentoIdentidadCorredora    THEN 'CP'
					  WHEN IndCli."ValorIndicador" = 'S'   		                       THEN 'RE'
					  ELSE    'NR'	
					  END  													AS "Relacion"				
				,LEFT(CLI."NombreCompleto",60)                      		AS "NombreCliente"
				,CASE 
	    			  WHEN CONTRA."IdContraparte" IS NOT NULL THEN 'CONTRAPARTE'  
					  WHEN CLI."IdCliente" IS NOT NULL THEN 'CLIENTE' 
					  END  													AS "TipoSocioNegocio"
				,CASE 
	    			  WHEN CLI."DocumentoIdentidad" = v_DocumentoIdentidadCorredora	THEN v_CuentaDCVCuentaPropia
					  WHEN INDCLI."ValorIndicador" = 'S'   		                       		THEN v_CuentaDCVCuentaRelacionado
					  ELSE v_CuentaDCVCuentaTerceros	  							   		
					  END  													AS "CuentaClienteDCVDepositante"
				--[CELCONDOR-659]
			
			FROM	"BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito"      	AS TRANSITO
				INNER JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarTipoOperacion"	AS TOPER 
					ON ( TRANSITO."IdTipoOperacion" = TOPER."IdTipoOperacion" 
						AND TRANSITO."IdClasificacionTipoInstrumento" 	= TOPER."IdClasificacionTipoInstrumento"
			        	AND TRANSITO."IdTipoInstrumento" 				= TOPER."IdTipoInstrumento")
				LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" 		AS CLI 
			        ON (CLI."IdEmpresa" = TRANSITO."IdEmpresa" and CLI."IdCliente" = TRANSITO."IdCliente")
			    INNER JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarTipoProducto"       PRODU
			        ON ('CUST' = PRODU."IdTipoProducto")
			  	LEFT JOIN "BSCL.Plataforma.BD::PLA.Concepto" 						AS CONCEPTO 
			        ON (TRANSITO."IdConcepto" = CONCEPTO."IdConcepto")
				LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente"      	AS INDCLI
					ON IndCli."IdCliente" =  TRANSITO."IdCliente" 	AND INDCLI."CodigoTipoIndicador"  = 'RELCORR'
				LEFT JOIN "BSCL.SocioNegocio.BD::CNT.Contraparte"   							AS CONTRA
					ON CONTRA."IdPersona" = CLI."IdPersona"
			WHERE 	TRANSITO."IdEstado" = 'PEN' 
			 		AND TRANSITO."IdBoveda" = 'DCV' AND TRANSITO."IdTipoOperacion" IN ('RETTIT','INGTIT')
			 		AND TO_VARCHAR(TRANSITO."FechaLiquidacion", 'YYYY-MM-DD') = TO_VARCHAR(:p_Fecha, 'YYYY-MM-DD');
				
/*----------------MOVIMIENTO DE TITULOS------------*/
	
	tbl_MRF = 
        SELECT  DISTINCT
        		--ROW_NUMBER() OVER (ORDER BY  oper."IdOperacion")    AS "IdInterno"                          
				1 														AS "IdInterno"                          
                ,OPER."FolioOperacion"  								AS "FolioOperacion"                     
                ,OPER."IdTipoOperacion" 								AS "IdTipoOperacion"                        
                ,T_Ope."Nombre" 										AS "NombreOperacion"                            
                ,DET."IdTipoInstrumento" 								AS "IdTipoInstrumento" 
                ,CAR."Nemotecnico" 										AS "Nemo"                                    
                ,DET."IdClasificacionTipoInstrumento" 					AS "IdClasificacionTipoInstrumento"       
                ,OPER."IdCondicionLiquidacion" 							AS "IdCondicionLiquidacion"
                ,' ' 													AS "Accion"                
                ,'N' 													AS "OperadorDirecto"                   
                ,OPER."FechaCreacion" 									AS "FechaIngresoRegistro"                 
                ,OPER."FechaVencimiento" 								AS "FechaVencimiento"                      
                ,'R' 													AS "IdTipoDocumentoIdentidad"              
                ,Cli."DocumentoIdentidad" 								AS "DocumentoIdentidad"                                                   
                ,OPER."IdPortafolio" 									AS "IdPortafolio"                          
                ,OPER."IdCliente" 										AS "IdCliente"                
                ,0 AS "FolioComprobante"                        
                ,0 AS "FolioComprobanteReferencia"          
                ,0 AS "FolioComprobanteRelacionado"         
                ,0 AS "IdOrden"                             
                ,DET."IngCantidad" 										AS "Cantidad"                                
                ,DET."Precio" 											AS "Precio"                               
                ,DET."Tasa" 											AS "Tasa"                                   
                ,DET."IngMontoValorizado" 								AS "Monto"                                    
                ,0 AS "MontoComprom"                
                ,OPER."IdEstado" 										AS "IdEstado"                              
                ,OPER."IdAgente" 										AS "IdAgente"                              
                ,Ins."IdTipoReajuste" 									AS "IdMoneda"                             
                ,' ' 													AS "TipoGranatia"                          
                ,' '    												AS "FondoAfp"                               
                ,'MA'   												AS "IdTipoComprobante"                      
                ,OPER."IdTipoProducto" 									AS "CodigoTipoProducto"
                ,PRODU."Nombre" 										AS "NombreProducto"     
                ,'-BCS' 												AS "IdBolsa"                                                
                ,'-DCV' 												AS "IdBoveda"                               
                ,'0' 													AS "FolioTransaccion"                      
                ,' ' 													AS "DescEstadoCclv"                        
                ,' '    												AS "UsoGarantia"                            
                ,' '    												AS "TipoOpePendiente"                       
                ,' '    												AS "BlockEnvio"                         
                ,CONCEPTO_PLA."IdConcepto"  							AS "IdConcepto" 
                ,CONCEPTO_PLA."Nombre" 									AS "NombreConcepto"                      
                ,' '    												AS "IndicadorEnvio"                     
                ,DET."IdEmisor" 										AS "IdEmisor"                               
                ,DET."EsFungible" 										AS "EsFungible"                           
                ,COALESCE(CAR."NemoSVS", DET."Nemotecnico") 			AS "NemoSVS"                             
                ,DET."IdTipoReajuste" 									AS "IdTipoReajuste"       
                ,'MA'    												AS "Categoria"              
				,CASE WHEN OPER."AfectaCustodia" = 'S' 
					  THEN 'TC' ELSE 'CV' END 							AS "TipoOperacionArchivo"
				--[CELCONDOR-659]
					  ,CASE 
	    			  WHEN Cli."DocumentoIdentidad" = v_DocumentoIdentidadCorredora    THEN 'CP'
					  WHEN IndCli."ValorIndicador" = 'S'   		                       THEN 'RE'
					  ELSE    'NR'					  			   END  AS "Relacion"	  
				
				,LEFT(Cli."NombreCompleto",60)                          AS "NombreCliente"
				,CASE 
	    			  WHEN Contra."IdContraparte" IS NOT NULL THEN 'CONTRAPARTE'  
					  WHEN Cli."IdCliente" IS NOT NULL THEN 'CLIENTE' END  AS "TipoSocioNegocio"
				,CASE 
	    			  WHEN Cli."DocumentoIdentidad" = v_DocumentoIdentidadCorredora  THEN v_CuentaDCVCuentaPropia
					  WHEN IndCli."ValorIndicador" = 'S'   		                       THEN v_CuentaDCVCuentaRelacionado
					  ELSE v_CuentaDCVCuentaTerceros	  							   END  AS "CuentaClienteDCVDepositante"
				--[CELCONDOR-659]					  
					  
					  
        FROM    "BSCL.Mesa.BD::MES.Operacion" 													AS OPER
                LEFT JOIN "BSCL.Custodia.BD::CUS.Reserva" 										AS DET
                ON OPER."IdOperacion" = DET."IdOperacion"
                LEFT JOIN "BSCL.Custodia.BD::CUS.Cartera" 										AS CAR
                ON CAR."FolioCartera" = DET."FolioCarteraCustodia"              
                LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" 					AS Cli 
                ON (Cli."IdEmpresa" = OPER."IdEmpresa" AND Cli."IdCliente" = OPER."IdCliente")
                LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" 								AS Ins 
                ON (DET."IdInstrumento" = Ins."IdInstrumento")
                LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" 								AS T_Ope
                ON OPER."IdTipoProducto" = T_Ope."IdTipoProducto" AND OPER."IdTipoOperacion" = T_Ope."IdTipoOperacion"
                LEFT JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarTipoProducto" 				AS PRODU
                ON (OPER."IdTipoProducto" = PRODU."IdTipoProducto")
                LEFT JOIN "BSCL.Mesa.BD::MES.ConceptoMesa" 								AS CONCEPTO_MES
                ON OPER."IdTipoOperacion" = CONCEPTO_MES."IdTipoOperacion" AND 
                OPER."IdTipoProducto" = CONCEPTO_MES."IdTipoProducto" AND 
                CONCEPTO_MES."NombreEntidad" = 'INGRESO'
                LEFT JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarConcepto" 					AS CONCEPTO_PLA
                ON CONCEPTO_MES."IdConcepto" = CONCEPTO_PLA."IdConcepto"                 
                --[CELCONDOR-659]
                LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente"      	AS IndCli
				ON IndCli."IdCliente" =  OPER."IdCliente" 	AND IndCli."CodigoTipoIndicador"  = 'RELCORR'
				LEFT JOIN "BSCL.SocioNegocio.BD::CNT.Contraparte"   							AS Contra
				ON Contra."IdPersona" = Cli."IdPersona"				
                
        WHERE   OPER."IdTipoOperacion" IN ('COMTER', 'VENTER', 'COMPAC', 'VENPAC') AND OPER."IdEstado" IN ('VIG', 'ANT', 'VCD')  AND
                (   TO_VARCHAR(OPER."FechaCondicionLiquidacion", 'YYYY-MM-DD') = TO_VARCHAR(p_Fecha, 'YYYY-MM-DD') OR 
                    TO_VARCHAR(p_Fecha, 'YYYY-MM-DD') = '1900-01-01' )
                AND ( p_IdTipoComprobante = 'MA' OR p_IdTipoComprobante = '' );   




	--* Las que tienen IdOp.Pend = 0 son las que no tienen clave y son TC 
	--* ------------------------------------------------------------------
	tbl_SIN_Clave =
		SELECT	FD."FolioOperacion"
				,FD."IdTipoOperacion"
                ,COALESCE(OP_Pend."IdOperacionPendiente", 0)    AS "IdOperacionPendiente"
		FROM	:tbl_FD AS FD
                LEFT JOIN "BSCL.Custodia.BD::CUS.OperacionesPendientes" OP_Pend
                ON 	FD."FolioOperacion" = OP_Pend."FolioOperacion" 
                	AND FD."IdTipoOperacion" = OP_Pend."IdTipoOperacion"				
		WHERE	"TipoOperacionArchivo" = 'TC'
		--* ---------------------------------------------------------------------------------
		UNION ALL
		--* ---------------------------------------------------------------------------------
		SELECT	GTIA."FolioOperacion"
				,GTIA."IdTipoOperacion"
                ,COALESCE(OP_Pend."IdOperacionPendiente", 0)    AS "IdOperacionPendiente"
		FROM	:tbl_GTIA AS GTIA
                LEFT JOIN "BSCL.Custodia.BD::CUS.OperacionesPendientes" OP_Pend
                ON 	GTIA."FolioOperacion" = OP_Pend."FolioOperacion" 
                	AND GTIA."IdTipoOperacion" = OP_Pend."IdTipoOperacion"
        --* ---------------------------------------------------------------------------------
		UNION ALL
		--* ---------------------------------------------------------------------------------
		SELECT	TIT."FolioOperacion"
				,TIT."IdTipoOperacion"
                ,COALESCE(OP_Pend."IdOperacionPendiente", 0)    AS "IdOperacionPendiente"
		FROM	:tbl_MOVTIT AS TIT
                LEFT JOIN "BSCL.Custodia.BD::CUS.OperacionesPendientes" OP_Pend
                ON 	TIT."FolioOperacion" = OP_Pend."FolioOperacion" 
                	AND TIT."IdTipoOperacion" = OP_Pend."IdTipoOperacion"
        --* ---------------------------------------------------------------------------------
		UNION ALL
		--* ---------------------------------------------------------------------------------
		SELECT	TRAN."FolioOperacion"
				,TRAN."IdTipoOperacion"
                ,COALESCE(OP_Pend."IdOperacionPendiente", 0)    AS "IdOperacionPendiente"
		FROM	:tbl_MOVTRAN AS TRAN
                LEFT JOIN "BSCL.Custodia.BD::CUS.OperacionesPendientes" OP_Pend
                ON 	TRAN."FolioOperacion" = OP_Pend."FolioOperacion" 
                	AND TRAN."IdTipoOperacion" = OP_Pend."IdTipoOperacion"
		--* ---------------------------------------------------------------------------------
		UNION ALL
		--* ---------------------------------------------------------------------------------
		SELECT	MRF."FolioOperacion"
				,MRF."IdTipoOperacion"
                ,COALESCE(OP_Pend."IdOperacionPendiente", 0)    AS "IdOperacionPendiente"
		FROM	:tbl_MRF AS MRF
                LEFT JOIN "BSCL.Custodia.BD::CUS.OperacionesPendientes" OP_Pend
                ON 	MRF."FolioOperacion" = OP_Pend."FolioOperacion" 
                	AND MRF."IdTipoOperacion" = OP_Pend."IdTipoOperacion"				
		WHERE	"TipoOperacionArchivo" = 'TC';

	--* Inserto las que no tienen clave....
	--* -----------------------------------
	INSERT	INTO "BSCL.Custodia.BD::CUS.OperacionesPendientes"
			( 	"IdOperacionPendiente", "IdTipoOperacion", "FolioOperacion", "Clave"		
				,"IdEstado", "IdUsuario", "FechaEstado"	)
	SELECT	"BSCL.Custodia.BD.Secuencias::IdOperacionPendiente".NEXTVAL
				,"IdTipoOperacion" ,"FolioOperacion", TRIM("FolioOperacion")
				,'PEN', p_IdUsuario, v_FechaOperacionSecondate
	FROM	:tbl_SIN_Clave
	WHERE	"IdOperacionPendiente" = 0;		


    tbl_Paso =          
    	SELECT	FD.*
                ,COALESCE(OP_Pend."IdOperacionPendiente", 0)    AS "IdOperacionPendiente"
                ,COALESCE(OP_Pend."IdEstado", 'PEN' )           AS "IdEstadoProceso"
                ,COALESCE(OP_Pend."Clave", '' )                 AS "Clave"    	
    	FROM	:tbl_FD AS FD
                LEFT JOIN "BSCL.Custodia.BD::CUS.OperacionesPendientes" OP_Pend
                ON 	FD."FolioOperacion" = OP_Pend."FolioOperacion" 
                	AND FD."IdTipoOperacion" = OP_Pend."IdTipoOperacion"
        --* ---------------------------------------------------------------------------
        UNION ALL                           
        --* ---------------------------------------------------------------------------     
    	SELECT	GTIA.*
                ,COALESCE(OP_Pend."IdOperacionPendiente", 0)    AS "IdOperacionPendiente"
                ,COALESCE(OP_Pend."IdEstado", 'PEN' )           AS "IdEstadoProceso"
                ,COALESCE(OP_Pend."Clave", '' )                 AS "Clave"    	
    	FROM	:tbl_GTIA AS GTIA
                LEFT JOIN "BSCL.Custodia.BD::CUS.OperacionesPendientes" OP_Pend
                ON 	GTIA."FolioOperacion" = OP_Pend."FolioOperacion" 
               		AND GTIA."IdTipoOperacion" = OP_Pend."IdTipoOperacion"  
        --* ---------------------------------------------------------------------------
        UNION ALL                           
        --* ---------------------------------------------------------------------------     
    	SELECT	TIT.*
                ,COALESCE(OP_Pend."IdOperacionPendiente", 0)    AS "IdOperacionPendiente"
                ,COALESCE(OP_Pend."IdEstado", 'PEN' )           AS "IdEstadoProceso"
                ,COALESCE(OP_Pend."Clave", '' )                 AS "Clave"    	
    	FROM	:tbl_MOVTIT AS TIT
                LEFT JOIN "BSCL.Custodia.BD::CUS.OperacionesPendientes" OP_Pend
                ON 	TIT."FolioOperacion" = OP_Pend."FolioOperacion" 
               		AND TIT."IdTipoOperacion" = OP_Pend."IdTipoOperacion"
        --* ---------------------------------------------------------------------------
        UNION ALL                           
        --* ---------------------------------------------------------------------------     
    	SELECT	TRAN.*
                ,COALESCE(OP_Pend."IdOperacionPendiente", 0)    AS "IdOperacionPendiente"
                ,COALESCE(OP_Pend."IdEstado", 'PEN' )           AS "IdEstadoProceso"
                ,COALESCE(OP_Pend."Clave", '' )                 AS "Clave"    	
    	FROM	:tbl_MOVTRAN AS TRAN
                LEFT JOIN "BSCL.Custodia.BD::CUS.OperacionesPendientes" OP_Pend
                ON 	TRAN."FolioOperacion" = OP_Pend."FolioOperacion" 
               		AND TRAN."IdTipoOperacion" = OP_Pend."IdTipoOperacion"
        --* ---------------------------------------------------------------------------
        UNION ALL                           
        --* ---------------------------------------------------------------------------     
    	SELECT	MRF.*
                ,COALESCE(OP_Pend."IdOperacionPendiente", 0)    AS "IdOperacionPendiente"
                ,COALESCE(OP_Pend."IdEstado", 'PEN' )           AS "IdEstadoProceso"
                ,COALESCE(OP_Pend."Clave", '' )                 AS "Clave"    	
    	FROM	:tbl_MRF AS MRF
                LEFT JOIN "BSCL.Custodia.BD::CUS.OperacionesPendientes" OP_Pend
                ON 	MRF."FolioOperacion" = OP_Pend."FolioOperacion" 
               		AND MRF."IdTipoOperacion" = OP_Pend."IdTipoOperacion";                                   



    tbl_Ctas = 
        SELECT  ROW_NUMBER() OVER (PARTITION BY CLI."IdPortafolio") AS "IdCuenta",
        		CLI."IdCliente", 
                CLI."IdPortafolio",
                POR."CodigoPortafolio",
                IND."ValorIndicador" AS "EsRelacionado", -- "N" > No relacionado => Cta 3ros.., "S" > Relacionado => Cta Relacionados
                COALESCE(CTA."Codigo", '') AS "Cuenta3ro",
                CASE WHEN COALESCE(CTA."Codigo", '') > '' THEN CTA."Codigo"
                     WHEN IND."ValorIndicador" = 'S' THEN ''    --'CTA. Relacionado'
                     ELSE ''    --'CTA. 3ro'
                END AS "CuentaCli_DCV"
        FROM    :tbl_Paso CLI
                LEFT JOIN "BSCL.Portafolio.BD.Vistas::ConsultarCuentaCustodioPortafolio" CTA
                ON  CLI."IdPortafolio" = CTA."IdPortafolio" 
                    AND CTA."IdBoveda" = 'DCV' AND CTA."IdEstado" = 'ACT' AND CTA."IdEstadoAutorizacion" = 'AUT'                        
                LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente" IND                  
                ON  IND."IdUso" = 'IND' AND CLI."IdCliente" = IND."IdCliente" AND IND."GrupoIndicador" = 'NORMATIVOS' 
                    AND "CodigoTipoIndicador" = 'RELCORR'
                LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" POR
                ON  CLI."IdCliente" = POR."IdCliente" AND CLI."IdPortafolio" = POR."IdPortafolio";


    p_out_Salida = 
        SELECT  DISTINCT 
                Paso.*,
                Cta."CodigoPortafolio",
                COALESCE(Cta."CuentaCli_DCV", '') AS "CuentaClienteDCV"
        FROM    :tbl_Paso Paso
                LEFT JOIN :tbl_Ctas Cta 
                ON Paso."IdCliente" = Cta."IdCliente" AND Paso."IdPortafolio" = Cta."IdPortafolio" AND
                   Cta."IdCuenta" = 1
        WHERE	( v_IdCliente = Paso."IdCliente" OR v_IdCliente = 0 ) AND
    			( p_IdMercado = "IdClasificacionTipoInstrumento" OR p_IdMercado = '' ) AND
    			( p_IdEstado = "IdEstadoProceso" OR p_IdEstado = '' ) AND
    			( p_IdTipoCuenta = "Categoria" OR p_IdTipoCuenta = '' ); 



                                    
	--* Para que haga Commit desde la UI
	p_out_ErroresContador = 0;
	p_out_ErroresDetalle =
		SELECT * FROM :p_out_ErroresDetalle WHERE 1 = 2;
                                
END;

GO