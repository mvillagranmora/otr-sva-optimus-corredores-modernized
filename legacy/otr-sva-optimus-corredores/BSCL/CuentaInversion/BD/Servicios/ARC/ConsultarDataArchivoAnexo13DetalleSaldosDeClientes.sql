SET SCHEMA "BSCL";
GO
DROP PROCEDURE "BSCL.CuentaInversion.BD.Servicios::ARC.ConsultarDataArchivoAnexo13DetalleSaldosDeClientes";
GO
CREATE PROCEDURE
--
--  Proposito: Obtiene la data para la generación del archivo Anexo 13 Detalle de Saldos de clientes
--           * No usa reglas de negocio ya que las validaciones se realizan en la solicitud de generación del archivo
--           * Los campos de la data de salida deben tener el mismo nombre que los campos del formato del archivo
--           * El Job que dibuja el arhivo formatea y ajusta los largos de campos de acuerdo al formato del archivo y tipo de registro
--           * El campo "TipoRegistroOptimus" debe ir en todas las tablas de salida y concordar con el formato configurado
--  Autor: JVILLAGRAN - BCS
--  Fecha Creacion: 30/01/2018
--
"BSCL.CuentaInversion.BD.Servicios::ARC.ConsultarDataArchivoAnexo13DetalleSaldosDeClientes" (

	 -- Parametros fijos de entrada 
     IN p_IdEmpresa 					BIGINT,
     IN p_IdUsuario 					BIGINT,
     IN p_CodigoFormatoArchivo 			NVARCHAR(40),     
     IN p_IdPrograma 					BIGINT,
     -- El resto de los parametros de entrada son variables (Obtiener de "BSCL.Plataforma.BD::BAT.ParametrosPrograma")
     
	 -- Datos para generar el archivo (TipoRegistro 1)
	 OUT p_out_Data_TipoRegistro1       TABLE (
	 										"TipoRegistroOptimus"   		INTEGER,  	 		-- Este campo debe ir en todas las tablas de salida y su valor concordar con el formato configurado(TipoRegistro)
	 										"Periodo"						NVARCHAR(8),
	 										"IdCuentaInversion"				BIGINT,
	 										"Correlativo"					NVARCHAR(20),
	 										"TipoSocioNegocio"         		INTEGER, 			-- NUM    |1    | 0: Proveedores   1:Personal Administrativo   6: Socio de negocio
	 										"NumeroDocumentoIdentidad"  	NVARCHAR(40), 		-- CHAR   |40   | RUC/DNI/RUT asociado al tipo
											"NombreCompleto"   				NVARCHAR(100), 		-- CHAR   |100  | Apellidos y nombres, denominación o Razón social
											"FechaEmision"      			NVARCHAR(10), 		-- DATE   |10   | Fecha de inicio deuda(negativo). Fecha última posición saldo positivo
											"MontoPendientePago"      		DECIMAL(26,8), 		-- DEC    |17,2 | Monto pendiente de pago. Saldo a favor o en contra
											"EstadoOperacion"				INTEGER							
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
	 
		
		DECLARE v_FechaEfectivaMovimiento 	NVARCHAR(10); 
		DECLARE v_IdCliente 				BIGINT;
		DECLARE v_IdEmpleado 				BIGINT;
		DECLARE v_IdEstado 					NVARCHAR(3); 
		DECLARE v_IdPortafolio 				BIGINT;
		DECLARE v_IdTipoMovimiento 			NVARCHAR(6);
		DECLARE v_SaldoLibroInventario 		DECIMAL(16,8);
		
		DECLARE v_ParametrosEntrada TABLE(  "FechaEfectivaMovimiento" 	NVARCHAR(10),
											"IdCliente"					BIGINT,
											"IdEmpleado"				BIGINT,
											"IdEstado"					NVARCHAR(3),
											"IdPortafolio"				BIGINT,
											"IdTipoMovimiento"			NVARCHAR(6),
											"SaldoLibroInventario"		DECIMAL(26,8)
										  );
										  
		v_ParametrosEntrada = SELECT '' AS "FechaEfectivaMovimiento", 0 AS "IdCliente", 0 AS "IdEmpleado",
									 '' AS "IdEstado", 0 AS "IdPortafolio", '' AS "IdTipoMovimiento", 0 AS "SaldoLibroInventario"
							  FROM DUMMY WHERE 1=2;
										
		
							  
		------------------------------------------
		-- OBTIENE PARAMETROS DE ENTRADA 
		------------------------------------------
		v_ParametrosEntrada =
		SELECT
			MAX(CASE WHEN "NombreParametro" = 'FechaEfectivaMovimiento' THEN "Valor" ELSE NULL END) "FechaEfectivaMovimiento",
			MAX(CASE WHEN "NombreParametro" = 'IdCliente' THEN "Valor" ELSE NULL END) "IdCliente",
			MAX(CASE WHEN "NombreParametro" = 'IdEmpleado' THEN "Valor" ELSE NULL END) "IdEmpleado",
			MAX(CASE WHEN "NombreParametro" = 'IdEstado' THEN "Valor" ELSE NULL END) "IdEstado",			
			MAX(CASE WHEN "NombreParametro" = 'IdPortafolio' THEN "Valor" ELSE NULL END) "IdPortafolio",
			MAX(CASE WHEN "NombreParametro" = 'IdTipoMovimiento' THEN "Valor" ELSE NULL END) "IdTipoMovimiento",
			MAX(CASE WHEN "NombreParametro" = 'SaldoLibroInventario' THEN "Valor" ELSE NULL END) "SaldoLibroInventario"
		FROM
		(
			SELECT "NombreParametro", "Valor" 
			FROM "BSCL.Plataforma.BD::BAT.ParametrosPrograma"  
			WHERE "IdPrograma" = :p_IdPrograma
		);
		--SELECT 'v_ParametrosEntrada', * FROM :v_ParametrosEntrada; -- REVISAR (activar solo a nivel local)

		
		
		SELECT			
			COALESCE("FechaEfectivaMovimiento",'0001-01-01') AS "FechaEfectivaMovimiento",
			(CASE WHEN("IdCliente" = 0) THEN NULL ELSE "IdCliente" END) AS "IdCliente",  -- si no se indica cliente debe ser null
			"IdEmpleado",			
			"IdEstado",
			(CASE WHEN("IdPortafolio" = 0) THEN NULL ELSE "IdPortafolio" END) AS "IdPortafolio",   -- si no se indica portafolio debe ser null
			COALESCE("IdTipoMovimiento", '') AS "IdTipoMovimiento",
			"SaldoLibroInventario"
		INTO v_FechaEfectivaMovimiento, v_IdCliente, v_IdEmpleado, v_IdEstado, v_IdPortafolio, v_IdTipoMovimiento, v_SaldoLibroInventario
		FROM :v_ParametrosEntrada;		
		--SELECT 'Parametros', :v_FechaEfectivaMovimiento, :v_IdCliente, :v_IdEmpleado, :v_IdEstado, :v_IdPortafolio, :v_IdTipoMovimiento, :v_SaldoLibroInventario  FROM DUMMY;  -- REVISION
		
		
		

		----------------------------------------------------------
		-- OBTIENE CUENTAS POR PAGAR Y COBRAR (CUENTA INVERSION) 
		----------------------------------------------------------
		CALL "BSCL.CuentaInversion.BD.Servicios::ConsultarCuentasPorPagarCobrarUI" 
		(
		     p_IdEmpresa				 => :p_IdEmpresa
		    ,p_IdUsuario				 => :p_IdUsuario
		    ,p_IdModulo					 => 'CTA'
		    ,p_IdEmpleado				 => :v_IdEmpleado
		    ,p_SaldoLibroInventario 	 => :v_SaldoLibroInventario
		    ,p_IdEstado					 => :v_IdEstado
		    ,p_FechaEfectivaMovimiento 	 => :v_FechaEfectivaMovimiento
		    ,p_IdCliente				 => :v_IdCliente
		    ,p_IdPortafolio				 => :v_IdPortafolio
		    ,p_IdTipoMovimiento			 => :v_IdTipoMovimiento  -- 'MOVCAR'|'MOVABN'|''
			,p_OUT_CuentasPorPagarCobrar => v_out_CuentasPorPagarCobrar
		);
	   --SELECT 'v_out_CuentasPorPagarCobrar', * FROM :v_out_CuentasPorPagarCobrar;  -- REVISION

		

		----------------------------------------------------------
		-- PREPARA SALIDA DEL SERVICIO 
		----------------------------------------------------------
		
		-- Si la data requiere algún procesamiento se hace acá 
		v_DetalleSaldoClientes = WITH DetSalCli AS 
		(		
				SELECT "IdCuentaInversion","NombreCliente", "DocumentoIdentidadCliente", "IdMoneda", "Monto", "FechaEfectivaMovimiento"
				FROM :v_out_CuentasPorPagarCobrar
		)	
		SELECT "IdCuentaInversion","NombreCliente", "DocumentoIdentidadCliente", "IdMoneda", "Monto", "FechaEfectivaMovimiento"
		FROM DetSalCli;		
		--SELECT 'v_DetalleSaldoClientes', * FROM :v_DetalleSaldoClientes; -- REVISION

		
		/***********  SALIDA REAL  ***********/		
		p_out_Data_TipoRegistro1  =
		SELECT
			1 			 					 AS "TipoRegistroOptimus",   	 -- Este campo debe ir en todas las tablas de salida y su valor concordar con el formato configurado(TipoRegistro)
			TO_NVARCHAR("FechaEfectivaMovimiento",'YYYYMMDD')	AS "Periodo",
			"IdCuentaInversion" 			 AS "IdCuentaInversion",
			'M' || row_number() over(ORDER BY "IdCuentaInversion" ASC) AS "Correlativo",
			6 			 					 AS "TipoSocioNegocio",          -- NUM    |1    | 0: Proveedores   1:Personal Administrativo   6: Socio de negocio			
			"DocumentoIdentidadCliente" 	 AS "NumeroDocumentoIdentidad", -- CHAR   |40   | RUC/DNI/RUT asociado al tipo
			"NombreCliente" 				 AS "NombreCompleto",  		 -- CHAR   |100  | Apellidos y nombres, denominación o Razón social
			"FechaEfectivaMovimiento" 		 AS "FechaEmision", -- DATE   |10   | Fecha de inicio deuda(negativo). Fecha última posición saldo positivo
			"Monto" 		 				 AS "MontoPendientePago",     -- DEC    |17,2 | Monto pendiente de pago. Saldo a favor o en contra
			CASE WHEN "FechaEfectivaMovimiento" = :v_FechaEfectivaMovimiento THEN 
				1
			ELSE
				9
			END 							 AS "EstadoOperacion"
		FROM :v_DetalleSaldoClientes;
		/************************************/
		
					
		/*** SALIDA EN DURO PARA PRUEBAS  ****
		p_out_Data_TipoRegistro1  =
		SELECT
			1 			 AS "TipoRegistroOptimus",   	-- Este campo debe ir en todas las tablas de salida y su valor concordar con el formato configurado(TipoRegistro)
			6 			 AS "TipoSocioNegocio",         -- NUM    |1    | 0: Proveedores   1:Personal Administrativo   6: Socio de negocio			
			'12969673' 	 AS "NumeroDocumentoIdentidad", -- CHAR   |40   | RUC/DNI/RUT asociado al tipo			
			'JUANCHO PEREZ' AS "NombreCompleto",  		-- CHAR   |100  | Apellidos y nombres, denominación o Razón social
			'2018-01-30' AS "FechaEmision",      		-- DATE   |10   | Fecha de inicio deuda(negativo). Fecha última posición saldo positivo
			777.99 		 AS "MontoPendientePago"      	-- DEC    |17,2 | Monto pendiente de pago. Saldo a favor o en contra
		FROM DUMMY;
		*************************************/		
		
		
  	END;    
END;