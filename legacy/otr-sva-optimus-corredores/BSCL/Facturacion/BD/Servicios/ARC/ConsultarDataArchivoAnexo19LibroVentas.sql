SET SCHEMA "BSCL";
GO 
DROP PROCEDURE "BSCL.Facturacion.BD.Servicios::ARC.ConsultarDataArchivoAnexo19LibroVentas";
GO
CREATE PROCEDURE
-- 
--  Proposito: Obtiene la data para la generación del archivo Anexo 19 Libro de Ventas Electrónico
--           * No usa reglas de negocio ya que las validaciones se realizan en la solicitud de generación del archivo
--           * Los campos de la data de salida deben tener el mismo nombre que los campos del formato del archivo
--           * El Job que dibuja el arhivo formatea y ajusta los largos de campos de acuerdo al formato del archivo y tipo de registro
--           * El campo "TipoRegistroOptimus" debe ir en todas las tablas de salida y concordar con el formato configurado
--  Autor: JVILLAGRAN - BCS
--  Fecha Creacion: 12/01/2018
--
"BSCL.Facturacion.BD.Servicios::ARC.ConsultarDataArchivoAnexo19LibroVentas" (

     IN p_IdEmpresa 					BIGINT,
     IN p_IdUsuario						BIGINT,
     IN p_CodigoFormatoArchivo 			NVARCHAR(40),
     IN p_IdPrograma 					BIGINT,
     -- El resto de los parametros de entrada los obtiene de "BSCL.Plataforma.BD::BAT.ParametrosPrograma"
     
	 -- Datos para generar el archivo (TipoRegistro 1)
	 OUT p_out_Data_TipoRegistro1       TABLE (
	 										"TipoRegistroOptimus"   		INTEGER,  	 	-- Este campo debe ir en todas las tablas de salida y su valor concordar con el formato configurado(TipoRegistro)
	 										"Periodo"						NVARCHAR(8),
	 										"CodigoUnicoOperacion"  		NVARCHAR(40),
	 										"NumeroAsientoContable" 		NVARCHAR(10),	 										
											"FechaEmisionComprobante" 		NVARCHAR(10),    -- DATE    |10 | Fecha de emisión del comprobante de pago                                                                                                            
											"FechaVencimientoPago"    		NVARCHAR(10),    -- DATE    |10 | Fecha de Vencimiento o Fecha de Pago                                                                                                                
											"TipoComprobantePago"     		NVARCHAR(2),     -- CHAR    |2  | Tipo de comprobante de pago o Documento                                                                                                             
											"NumeroSerieComprobantePago" 	NVARCHAR(20),	 -- CHAR    |20 | Número serie del comprobante de pago o documento o número de serie de la maquina registradora                                                       
											"NumeroComprobantePago"      	NVARCHAR(20),	 -- CHAR    |20 | Número del comprobante de pago o documento. En caso de optar por anotar el importe total de las operaciones realizadas, registrar el número inicial
											
											"ImporteTotalFinal" 			INTEGER, 	     -- NUM    	|20| En caso de optar por anotar el importe total de las operaciones, registrar el número final
											"TipoDocumentoIdentidad" 		NVARCHAR(1), 	 -- CHAR   	|1| Tipo de Documento de Identidad del cliente
											"NumeroDocumentoIdentidad" 		NVARCHAR(40), 	 -- CHAR 	|15| Número de Documento de Identidad del cliente
											"NombreCliente" 				NVARCHAR(100), 	 -- CHAR 	|100| Nombre o Razón Social del Cliente
											"ValorFacturado" 				DECIMAL(26,8), 	 -- DEC 	|14,2| Valor facturado de la exportación
											"BaseImponible"  				DECIMAL(26,8), 	 -- DEC 	|14,2| Base imponible de la operación gravada
											"DescuentoBaseImponible" 		DECIMAL(26,8), 	 -- DEC 	|14,2| Descuento de la Base Imponible
											
											"ImpuestoGeneralVentas" 		 DECIMAL(26,8),  -- DEC	|14,2| Impuesto General a las Ventas y/o Impuesto de Promoción Municipal
											"DescuentoImpuestoGeneralVentas" DECIMAL(26,8),	 -- DEC	|14,2| Descuento del Impuesto General a las Ventas y/o Impuesto de Promoción Municipal	Importe total de la operación exonerada
											"ImporteOperacionExonerada"		DECIMAL(26,8),   -- DEC	|14,2| ImporteOperacionExonerada
											"ImporteOperacionInafecta"		DECIMAL(26,8),   -- DEC	|14,2| ImporteOperacionInafecta											
											"ImpuestoSelectivoConsumo" 		DECIMAL(26,8), 	 -- DEC	|14,2| Impuesto Selectivo al Consumo, de ser el caso
											"BaseImponibleOperacion" 		DECIMAL(26,8), 	 -- DEC	|14,2| Base imponible de la operación gravada con el Impuesto a las Ventas del Arroz Pilado
											"ImpuestoVentasArrozPilado" 	DECIMAL(26,8), 	 -- DEC	|14,2| Impuesto a las Ventas del Arroz Pilado
											"OtrosConceptosNoBaseImponible" DECIMAL(26,8), 	 -- DEC	|14,2| Otros conceptos, tributos y cargos que no forman parte de la base imponible
											"ImporteTotalComprobantePago" 	DECIMAL(26,8), 	 -- DEC	|14,2| Importe total del comprobante de pago

											"CodigoMoneda" 							NVARCHAR(30), 	-- CHAR | 3  | Código de la Moneda','')
											"TipoCambio" 							DECIMAL(26,8), 	-- DEC  | 4,3| Tipo de cambio','')
											"FechaEmisionComprobanteModificado" 	NVARCHAR(10), 	-- DATE | 10 | Fecha de emisión del comprobante de pago o documento original que se modifica','dd/MM/yyyy')
											"TipoComprobantePagoModificado" 		NVARCHAR(2), 	-- CHAR | 2  | Tipo del comprobante de pago que se modifica','')
											"NumeroSerieComprobantePagoModificado" 	NVARCHAR(20), 	-- CHAR | 20 | Número de serie del comprobante de pago que se modifica','')
											"NumeroComprobantePagoModificado" 		NVARCHAR(20), 	-- CHAR | 20 | Número del comprobante de pago que se modifica (6) o Número de la DUA,','')
											"IdentificacionContrato" 				NVARCHAR(12),  	-- CHAR | 12 | Identificación del Contrato o del proyecto en el caso de los Operadores de las sociedades irregulares, consorcios, joint ventures u otras…','')
											"Error1InconsistenciaTipoCambio"  		NVARCHAR(30), 	-- CHAR | 30 | Error tipo 1: inconsistencia en el tipo de cambio','')
											"ComprobantesCanceladosConMediosPago" 	NVARCHAR(30), 	-- CHAR | 30 | Indicador de Comprobantes de pago cancelados con medios de pago','')
											"EstadoOportunidadAnotacion" 			INTEGER 		-- NUM  | 1  | Estado que identifica la oportunidad de anotación','')

											)

 )
 
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL" 
     	
AS
  BEGIN
   	
	
	
	--------------------
	--LOGICA
	--------------------
	BEGIN

		DECLARE v_ParametrosEntrada TABLE(	"IdCliente"  							BIGINT,
											"IdPortafolio" 							BIGINT,
											"IdTipoComprobante" 					NVARCHAR(3),
											"IdMoneda" 								NVARCHAR(3),
											"IdEstadoComprobante" 					NVARCHAR(3),
											"IdClasificacionTipoInstrumento" 		NVARCHAR(3),
											"IdTipoInstrumento" 					NVARCHAR(3),
											"IdSucursalEmpleadoEjecutivoPortafolio" BIGINT,
											"IdEmpleadoEjecutivoPortafolio"  		BIGINT,
											"FechaComprobanteDesde"	 				NVARCHAR(10),
											"FechaComprobanteHasta" 				NVARCHAR(10),
											"FolioComprobanteDesde"  				BIGINT,
											"FolioComprobanteHasta"					BIGINT);
											

		v_ParametrosEntrada = SELECT 0 AS "IdCliente", 0 AS "IdPortafolio", '' AS "IdTipoComprobante",
									'' AS "IdMoneda", '' AS "IdEstadoComprobante", '' AS "IdClasificacionTipoInstrumento",
									'' AS "IdTipoInstrumento", 0 AS "IdSucursalEmpleadoEjecutivoPortafolio", 0 AS "IdEmpleadoEjecutivoPortafolio",
									'' AS "FechaComprobanteDesde", '' AS "FechaComprobanteHasta", 0 AS "FolioComprobanteDesde",0 AS "FolioComprobanteHasta"
							  FROM DUMMY WHERE 1=2;
										
		
		------------------------------------------
		-- OPTIENE PARAMETROS DE ENTRADA 
		------------------------------------------
		v_ParametrosEntrada =
		SELECT
			MAX(CASE WHEN "NombreParametro" = 'IdCliente' THEN "Valor" ELSE NULL END) "IdCliente",
			MAX(CASE WHEN "NombreParametro" = 'IdPortafolio' THEN "Valor" ELSE NULL END) "IdPortafolio",
			MAX(CASE WHEN "NombreParametro" = 'IdTipoComprobante' THEN "Valor" ELSE NULL END) "IdTipoComprobante",
			MAX(CASE WHEN "NombreParametro" = 'IdMoneda' THEN "Valor" ELSE NULL END) "IdMoneda",
			MAX(CASE WHEN "NombreParametro" = 'IdEstadoComprobante' THEN "Valor" ELSE NULL END) "IdEstadoComprobante",
			MAX(CASE WHEN "NombreParametro" = 'IdClasificacionTipoInstrumento' THEN "Valor" ELSE NULL END) "IdClasificacionTipoInstrumento",
			MAX(CASE WHEN "NombreParametro" = 'IdTipoInstrumento' THEN "Valor" ELSE NULL END) "IdTipoInstrumento",
			MAX(CASE WHEN "NombreParametro" = 'IdSucursalEmpleadoEjecutivoPortafolio' THEN "Valor" ELSE NULL END) "IdSucursalEmpleadoEjecutivoPortafolio",
			MAX(CASE WHEN "NombreParametro" = 'IdEmpleadoEjecutivoPortafolio' THEN "Valor" ELSE NULL END) "IdEmpleadoEjecutivoPortafolio",
			MAX(CASE WHEN "NombreParametro" = 'FechaComprobanteDesde' THEN "Valor" ELSE NULL END) "FechaComprobanteDesde",		
			MAX(CASE WHEN "NombreParametro" = 'FechaComprobanteHasta' THEN "Valor" ELSE NULL END) "FechaComprobanteHasta",
			MAX(CASE WHEN "NombreParametro" = 'FolioComprobanteDesde' THEN "Valor" ELSE NULL END) "FolioComprobanteDesde",
			MAX(CASE WHEN "NombreParametro" = 'FolioComprobanteHasta' THEN "Valor" ELSE NULL END) "FolioComprobanteHasta"
		FROM
		(
			SELECT "NombreParametro", "Valor" 
			FROM "BSCL.Plataforma.BD::BAT.ParametrosPrograma"  
			WHERE "IdPrograma" = :p_IdPrograma
		);
		--SELECT 'v_ParametrosEntrada', * FROM :v_ParametrosEntrada; -- REVISAR (activar solo a nivel local)
				

		
		----------------------------------------------------------
		-- OBTIENE COMPROBANTES DE FAC SEGUN PARAMETROS 
		----------------------------------------------------------
		v_ComprobanteFacturacion = 
		SELECT 
			COM.*
			,CAST(year(COM."FechaComprobante") AS NVARCHAR(4)) AS "PeriodoAnno"
			,(CASE
				WHEN month(COM."FechaComprobante") < 10 THEN '0' || cast(month(COM."FechaComprobante") AS NVARCHAR(1))
				ELSE cast(month(COM."FechaComprobante") AS NVARCHAR(2))
			 END)  AS "PeriodoMes"			
			
		FROM "BSCL.Facturacion.BD.Vistas::ConsultarComprobante" AS COM
		INNER JOIN :v_ParametrosEntrada AS PAR ON (1=1)
		WHERE COM."IdCliente" = (CASE WHEN COALESCE(PAR."IdCliente",0) <> 0 THEN PAR."IdCliente" ELSE COM."IdCliente" END)		 
		AND COM."IdPortafolio" = (CASE WHEN COALESCE(PAR."IdPortafolio",0) <> 0 THEN PAR."IdPortafolio" ELSE COM."IdPortafolio" END)
		AND COM."IdTipoComprobante" = (CASE WHEN COALESCE(PAR."IdTipoComprobante",'') <> '' THEN PAR."IdTipoComprobante" ELSE COM."IdTipoComprobante" END)
		AND COM."IdMoneda" = (CASE WHEN COALESCE(PAR."IdMoneda",'') <> '' THEN PAR."IdMoneda" ELSE COM."IdMoneda" END)
		AND COM."IdEstadoComprobante" = (CASE WHEN COALESCE(PAR."IdEstadoComprobante",'') <> '' THEN PAR."IdEstadoComprobante" ELSE COM."IdEstadoComprobante" END)
		AND COM."IdClasificacionTipoInstrumento" = (CASE WHEN COALESCE(PAR."IdClasificacionTipoInstrumento",'') <> '' THEN PAR."IdClasificacionTipoInstrumento" ELSE COM."IdClasificacionTipoInstrumento" END)
		AND COM."IdTipoInstrumento" = (CASE WHEN COALESCE(PAR."IdTipoInstrumento",'') <> '' THEN PAR."IdTipoInstrumento" ELSE COM."IdTipoInstrumento" END)
		AND COM."IdSucursalEmpleadoEjecutivoPortafolio" = (CASE WHEN COALESCE(PAR."IdSucursalEmpleadoEjecutivoPortafolio",0) <> 0 THEN PAR."IdSucursalEmpleadoEjecutivoPortafolio" ELSE COM."IdSucursalEmpleadoEjecutivoPortafolio" END)
		AND COM."IdEmpleadoEjecutivoPortafolio" = (CASE WHEN COALESCE(PAR."IdEmpleadoEjecutivoPortafolio",0) <> 0 THEN PAR."IdEmpleadoEjecutivoPortafolio" ELSE COM."IdEmpleadoEjecutivoPortafolio" END)		 
		AND COM."FechaComprobante" >= (CASE WHEN COALESCE(PAR."FechaComprobanteDesde",'') <> '' THEN PAR."FechaComprobanteDesde" ELSE COM."FechaComprobante" END)
		AND COM."FechaComprobante" <= (CASE WHEN COALESCE(PAR."FechaComprobanteHasta",'') <> '' THEN PAR."FechaComprobanteHasta" ELSE COM."FechaComprobante" END)
		AND COM."FolioComprobante" >= (CASE WHEN COALESCE(PAR."FolioComprobanteDesde",0) <> 0 THEN PAR."FolioComprobanteDesde" ELSE COM."FolioComprobante" END)
		AND COM."FolioComprobante" <= (CASE WHEN COALESCE(PAR."FolioComprobanteHasta",0) <> 0 THEN PAR."FolioComprobanteHasta" ELSE COM."FolioComprobante" END)
		AND COM."IdEstadoComprobante" <> 'ANU';
		
		--SELECT 'v_ComprobanteFacturacion', * FROM :v_ComprobanteFacturacion;    -- REVISAR (activar solo a nivel local) 

		
		
		----------------------------------------------------------
		-- PREPARA SALIDA DEL SERVICIO 
		----------------------------------------------------------

		p_out_Data_TipoRegistro1  =		
		SELECT
			/*01*/  1 				AS "TipoRegistroOptimus" 			-- Este campo debe ir en todas las tablas de salida y su valor concordar con el formato configurado(TipoRegistro)
		    /*02*/ ,COM."PeriodoAnno" || COM."PeriodoMes" || '00' 	AS "Periodo"
			/*03*/ ,CAST(COM."IdComprobante" AS NVARCHAR(40)) AS "CodigoUnicoOperacion"
			/*04*/ ,'M004' 		AS "NumeroAsientoContable"            
			/*05*/ ,COM."FechaComprobante" AS "FechaEmisionComprobante"                                                                                                                
			/*06*/ ,COM."FechaLiquidacion" AS "FechaVencimientoPago"
			/*07*/ ,CASE
						WHEN FP."IdMedioDePago" = 'CHG' THEN '03'    -- (VVI) CHEQUE GENERENCIA 		--> 03 OTROS      
						WHEN FP."IdMedioDePago" = 'INT' THEN '01'    -- (TRA) TRANSFERENCIA INTERBANC 	--> 01 EFECTIVO
						WHEN FP."IdMedioDePago" = 'TBC' THEN '01'    -- (TRA) TRANSFERENCIA BCP			--> 01 EFECTIVO
						WHEN FP."IdMedioDePago" = 'GYT' THEN '02'    -- (TRA) GIROS Y TRANSFERENCIAS 	--> 02 DEPÓSITO EN CUENTA
						ELSE '01'
					END AS "TipoComprobantePago"
			
			/*08*/ ,'E001'			AS "NumeroSerieComprobantePago" 	                                                       
			/*09*/ ,CAST(COM."FolioComprobante" AS NVARCHAR(20)) AS "NumeroComprobantePago"			
			/*10*/ ,0 AS "ImporteTotalFinal"
			/*11*/ ,(CASE 
						WHEN PER."Tipo" = 'N' THEN '1' -- 01	DOCUMENTO  NACIONAL DE IDENTIDAD	DNI 
						WHEN PER."Tipo" = 'J' THEN '6' -- 06	REG. ÚNICO DE CONTRIBUYENTES (1)	RUC 
						ELSE '' 
					END) AS "TipoDocumentoIdentidad"
			/*12*/ ,DOI."DocumentoIdentidad" AS "NumeroDocumentoIdentidad"
			/*13*/ ,COM."NombreLargoCliente" AS "NombreCliente"
			/*14*/ ,0.00 AS "ValorFacturado"
			/*15*/ ,0.00 AS "BaseImponible"
			/*16*/ ,0.00 AS "DescuentoBaseImponible"			
			/*17*/ ,0.00 AS "ImpuestoGeneralVentas"
			/*18*/ ,0.00 AS "DescuentoImpuestoGeneralVentas"			 
			/*19*/ ,0.00 AS "ImporteOperacionExonerada"
			/*20*/ ,0.00 AS "ImporteOperacionInafecta"											
			/*21*/ ,0.00 AS "ImpuestoSelectivoConsumo"
			/*22*/ ,CASE 
						WHEN COM."IdTipoComprobante" = 'POL' THEN COM."TotalComis"
						ELSE COM."Monto"
					END  AS "BaseImponibleOperacion"
			/*23*/ ,0.00 AS "ImpuestoVentasArrozPilado"
			/*24*/ ,0.00 AS "OtrosConceptosNoBaseImponible"
			/*25*/ ,COM."Monto" AS "ImporteTotalComprobantePago"
			/*26*/ ,COM."IdMoneda" AS "CodigoMoneda"
			/*27*/ ,CASE 
						WHEN COM."IdMoneda" = 'PEN' THEN 1.000
						ELSE 0.000
					END	 AS "TipoCambio"
			
			/*28*/ ,'0001-01-01' AS "FechaEmisionComprobanteModificado"
			/*29*/ ,'00' AS "TipoComprobantePagoModificado"
			/*30*/ ,'' AS "NumeroSerieComprobantePagoModificado"
			/*31*/ ,'-' AS "NumeroComprobantePagoModificado"			
			/*32*/ ,'' AS "IdentificacionContrato"
			/*33*/ ,'' AS "Error1InconsistenciaTipoCambio"
			/*34*/ ,'' AS "ComprobantesCanceladosConMediosPago"
			/*35*/ ,1 AS "EstadoOportunidadAnotacion"

		FROM :v_ComprobanteFacturacion AS COM
		LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON (CLI."IdCliente" = COM."IdCliente")
		LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS PER ON (PER."IdPersona" = CLI."IdPersona")
		LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS DOI ON (DOI."IdPersona" = PER."IdPersona" AND DOI."IdTipoDocumentoIdentidad" = (CASE WHEN PER."Tipo" = 'N' THEN 'DNI' WHEN PER."Tipo" = 'J' THEN 'RUC' ELSE 'RUC' END))
		LEFT JOIN  (
						SELECT "IdComprobante", MAX("IdMedioDePago") AS "IdMedioDePago"
						FROM "BSCL.Facturacion.BD::FAC.ComprobanteFormaDePago"
						WHERE "IdEstado" = 'VIG'
						GROUP BY "IdComprobante"
				   ) AS FP ON (FP."IdComprobante" = COM."IdComprobante");
				   
		   

  	END;    
END;
