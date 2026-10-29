SET SCHEMA "BSCL";
GO
DROP PROCEDURE "BSCL.CuentaInversion.BD.Servicios::AgregarProgramaCargaInicialSaldosReporteAntiguedad";
GO
CREATE PROCEDURE "BSCL.CuentaInversion.BD.Servicios::AgregarProgramaCargaInicialSaldosReporteAntiguedad" (
	-- Parametros de Entrada
	IN  p_IdEmpresa         BIGINT,
    IN  p_IdUsuario         BIGINT,
    	
	--Salidas para mensajes de advertencias y errores
	OUT p_out_AdvertenciasContador 	INT,
	OUT p_out_AdvertenciasDetalle  	TABLE ("Codigo" NVARCHAR(100),"RequiereAutorizacion" NVARCHAR(40),"Descripcion" NVARCHAR(5000)),
	OUT p_out_ErroresContador      	INT,
	OUT p_out_ErroresDetalle       	TABLE ("Codigo" NVARCHAR(100),"Descripcion" NVARCHAR(5000))
)
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL" 
AS
  BEGIN
  	--------------------
	--DECLARE Manejo de Errores
	--------------------	  
	DECLARE SALIDA_CONTROLADA 			CONDITION; 
	DECLARE SALIDA_CONTROLADA_NO_DATA 	CONDITION;
	DECLARE v_errorhashmsg 				TABLE ("Codigo" NVARCHAR(40),"Valor" NVARCHAR(520));
	DECLARE v_erroroutmsg  				NVARCHAR(520);		
	DECLARE v_ErrorNoData  				NVARCHAR(100);
	DECLARE v_AdvertenciasDetalle		TABLE ("Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(5000));
	DECLARE v_ErroresDetalle			TABLE ("Codigo" nvarchar(40),"Descripcion" nvarchar(5000));
	DECLARE v_AdvertenciasContador		INT;
	DECLARE v_ErroresContador			INT;
	DECLARE v_out_IdPrograma			TABLE("IdPrograma" BIGINT);
	DECLARE v_Count						BIGINT := 0;
	DECLARE v_ContadorSaldosDiferentes	BIGINT := 0;
	DECLARE v_FechaSistema				NVARCHAR(10);
	DECLARE v_TabCuentaInversion 		TABLE("IdCuentaInversion" BIGINT);
	
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA
	
	BEGIN
		--salida/error/advertencia controlado por este servicio
		SELECT count(1) into p_out_ErroresContador FROM :p_out_ErroresDetalle;
		SELECT count(1) into p_out_AdvertenciasContador FROM :p_out_AdvertenciasDetalle;
	END;
	
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA_NO_DATA
	
	BEGIN
		v_hash= SELECT '' as "Codigo",'' as "Valor" FROM DUMMY;	
		CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajeDiccionario" (:v_ErrorNoData,:v_hash,:v_erroroutmsg);
		p_out_ErroresDetalle =  SELECT :v_ErrorNoData  as "Codigo",:v_erroroutmsg as "Descripcion" FROM dummy UNION ALL
								SELECT * FROM :p_out_ErroresDetalle;
		SELECT COUNT(1) INTO p_out_ErroresContador FROM :p_out_ErroresDetalle;
	END;
	
	DECLARE EXIT HANDLER FOR SQLEXCEPTION
	
	BEGIN
		DECLARE v_errorExt NVARCHAR(40);
		IF ::SQL_ERROR_CODE > 10000 THEN 
			call "BSCL.Plataforma.BD.Servicios::ConsultarCodigoMensajeError" (::SQL_ERROR_CODE,v_errorExt);			
			p_out_ErroresDetalle = SELECT * FROM :p_out_ErroresDetalle UNION ALL  
								   SELECT v_errorExt,::SQL_ERROR_MESSAGE FROM dummy;				
			SELECT count(1) into p_out_ErroresContador FROM :p_out_ErroresDetalle;										
		ELSE
			--Error interno no manejado	
			RESIGNAL; 
		END IF;		
	END;
	
	p_out_AdvertenciasContador	:= 0;
	p_out_ErroresContador		:= 0;
	p_out_AdvertenciasDetalle	= SELECT '' as "Codigo",'' as "RequiereAutorizacion",'' as "Descripcion" FROM dummy where 1=2;
	p_out_ErroresDetalle		= SELECT '' as "Codigo",'' as "Descripcion" FROM dummy where 1=2;  
	
	v_AdvertenciasContador		:= 0;
	v_ErroresContador			:= 0;
	v_AdvertenciasDetalle		= SELECT '' as "Codigo",'' as "RequiereAutorizacion",'' as "Descripcion" FROM dummy where 1=2;
	v_ErroresDetalle			= SELECT '' as "Codigo",'' as "Descripcion" FROM dummy where 1=2;
	
	v_out_IdPrograma			= SELECT 0 as "IdPrograma" FROM dummy where 1=2;
	
	BEGIN
		
		--------------------
		--VALIDACIONES
		--------------------
		SELECT COUNT(1) INTO v_Count FROM "BSCL.CuentaInversion.BD::CTA.SaldosReporteAntiguedad";
		
		IF(v_Count > 0) THEN
			p_out_ErroresDetalle = select 'ERROR_CARGA_INICIAL_YA_REALIZADA' as "Codigo",:v_erroroutmsg as "Descripcion" from dummy;
			SIGNAL SALIDA_CONTROLADA;
		END IF;
		
		--------------------
		--LOGICA
		--------------------
		v_saldosInicialesDiferentesDeCero = WITH SALDOS AS (
			SELECT "IdCuentaInversion","FechaEfectivaMovimiento","Saldo",AUX."Fecha",
			    row_number() OVER (PARTITION  BY "IdCuentaInversion" order BY "FechaEfectivaMovimiento" ASC,"FechaCertificacion" ASC, "IdCargoAbonoMovimiento" ASC) AS "Row" 
			    FROM (
			    		SELECT m."IdCuentaInversion",
			    				m."FechaEfectivaMovimiento",
			    				CASE 
									WHEN m."IdTipoMovimiento" = 'MOVCAR' THEN COALESCE(m."Saldo",0) + COALESCE(m."Monto",0)
									ELSE
										COALESCE(m."Saldo",0) - COALESCE(m."Monto",0)
									END AS "Saldo"
								,
			    				m."IdCargoAbonoMovimiento",
			    				m."FechaCertificacion"
			    		FROM "BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento"  AS m
						WHERE m."IdEstado" = 'CRT' ORDER BY m."IdCuentaInversion", m."FechaEfectivaMovimiento",m."FechaCertificacion"
					)
					LEFT JOIN (
					SELECT "FechaSistema" AS "Fecha" FROM "BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento" ORDER BY "FechaSistema" ASC LIMIT 1
					) AS AUX ON AUX."Fecha" <> TO_DATE('0001-01-01')
			)
			
			SELECT * FROM SALDOS WHERE "Row" = 1 AND "Saldo" != 0;
		
		SELECT count(1) INTO v_ContadorSaldosDiferentes FROM :v_saldosInicialesDiferentesDeCero;
		
		IF(v_ContadorSaldosDiferentes > 0) THEN
			v_TabCuentaInversion = SELECT SALDOS."IdCuentaInversion" FROM :v_saldosInicialesDiferentesDeCero AS SALDOS;		
			
			CALL "BSCL.CuentaInversion.BD.Servicios::ActualizarSaldoCargoAbono" (
				p_IdEmpresa			=> :p_IdEmpresa,
			    p_IdUsuario			=> :p_IdUsuario,
				p_FechaMovimiento	=> '0001-01-01',
				p_CuentaInversion	=> :v_TabCuentaInversion
			);
		END IF;
		
		v_saldoInicial =  WITH SALDOSINICIALES AS (
			SELECT "IdCuentaInversion","FechaEfectivaMovimiento","Saldo",AUX."Fecha",ADD_DAYS(AUX."Fecha",-1) AS "FechaAnterior",--"IdCargoAbonoMovimiento",
    		row_number() OVER (PARTITION  BY "IdCuentaInversion" order BY "FechaEfectivaMovimiento" ASC,"FechaCertificacion" ASC, "IdCargoAbonoMovimiento" ASC) AS "Row" 
    		FROM (
    			SELECT m."IdCuentaInversion",
    				m."FechaEfectivaMovimiento",
    				CASE 
						WHEN m."IdTipoMovimiento" = 'MOVCAR' THEN COALESCE(m."Saldo",0) + COALESCE(m."Monto",0)
						ELSE
							COALESCE(m."Saldo",0) - COALESCE(m."Monto",0)
						END AS "Saldo"
					,
    				m."IdCargoAbonoMovimiento",
    				m."FechaCertificacion"
    			FROM "BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento"  AS m
				WHERE m."IdEstado" = 'CRT' ORDER BY m."IdCuentaInversion", m."FechaEfectivaMovimiento",m."FechaCertificacion"
			)
			LEFT JOIN (
			SELECT "FechaSistema" AS "Fecha" FROM "BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento" ORDER BY "FechaSistema" ASC LIMIT 1
			) AS AUX ON AUX."Fecha" <> TO_DATE('0001-01-01')
		)
		SELECT * FROM SALDOSINICIALES WHERE "Row" = 1;
		
		v_saldoInicialIntermediacion = SELECT 
				CASE WHEN TO_DATE(config."Valor") = TO_DATE('0001-01-01') THEN s."FechaAnterior"
					WHEN TO_DATE(config."Valor") <> TO_DATE('0001-01-01') AND s."FechaAnterior" <= ADD_DAYS(TO_DATE(config."Valor"),-1) THEN s."FechaAnterior"
					ELSE ADD_DAYS(TO_DATE(config."Valor"),-1)
					END AS "FechaSistema",
					ctainver."IdCliente",ctainver."IdPortafolio", ctainver."IdMoneda", 'I' AS "TipoSaldo", s."Saldo"
 					FROM :v_saldoInicial AS s
					INNER JOIN "BSCL.CuentaInversion.BD::CTA.CuentaInversion" AS ctainver
						ON ctainver."IdCuentaInversion" = s."IdCuentaInversion"
					LEFT JOIN "BSCL.Plataforma.BD::PLA.Configuracion" AS config
						ON config."IdEmpresa" = 1 AND "CodigoParametro" = 'PLA.FechaGoLive'
					ORDER BY ctainver."IdCliente" ASC, ctainver."IdPortafolio" ASC;
		
		v_saldoInicialGeneral = SELECT 
				CASE WHEN TO_DATE(config."Valor") = TO_DATE('0001-01-01') THEN s."FechaAnterior"
					WHEN TO_DATE(config."Valor") <> TO_DATE('0001-01-01') AND s."FechaAnterior" <= ADD_DAYS(TO_DATE(config."Valor"),-1) THEN s."FechaAnterior"
					ELSE ADD_DAYS(TO_DATE(config."Valor"),-1)
					END AS "FechaSistema",
					ctainver."IdCliente",ctainver."IdPortafolio", ctainver."IdMoneda", 'G' AS "TipoSaldo", 0 AS "Saldo"
 					FROM :v_saldoInicial AS s
					INNER JOIN "BSCL.CuentaInversion.BD::CTA.CuentaInversion" AS ctainver
						ON ctainver."IdCuentaInversion" = s."IdCuentaInversion"
					LEFT JOIN "BSCL.Plataforma.BD::PLA.Configuracion" AS config
						ON config."IdEmpresa" = 1 AND "CodigoParametro" = 'PLA.FechaGoLive'
					ORDER BY ctainver."IdCliente" ASC, ctainver."IdPortafolio" ASC;
					
		v_saldoInicialFinal = SELECT * from :v_saldoInicialIntermediacion
								UNION ALL
								SELECT * from :v_saldoInicialGeneral;
								
		--Insertar saldo inicial
		INSERT INTO "BSCL.CuentaInversion.BD::CTA.SaldosReporteAntiguedad"
		SELECT * FROM :v_saldoInicialFinal;
		
		--Obtener fecha del sistema
		CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaSistema" (:p_IdEmpresa,v_FechaSistema);
		
		--Parametros de entrada para el Job
 	 	v_TablaParametros = 
 	 		SELECT 'IdEmpresa'			AS "NombreParametro", TO_NVARCHAR(p_IdEmpresa)			AS "Valor" FROM DUMMY
 	 		UNION ALL
 	 		SELECT 'IdUsuario'			AS "NombreParametro", TO_NVARCHAR(p_IdUsuario)			AS "Valor" FROM DUMMY
 	 		UNION ALL
 	 		SELECT 'FechaSistema'		AS "NombreParametro", TO_NVARCHAR(v_FechaSistema)		AS "Valor" FROM DUMMY
 	 		;
	
		CALL "BSCL.Plataforma.BD.Servicios::BAT.AgregarPrograma"    
				(   
				     p_IdEmpresa     			=> :p_IdEmpresa
					,p_IdUsuario    			=> :p_IdUsuario
					,p_IdModulo    				=> 'CTA'					
				 	,p_IdAgenda	    			=> 0
					,p_IdTipoTarea				=> 'CARGA_INICIAL_SALDOS_REPORTE_ANTIGUEDAD'  -- Job a ejecutar
					,p_Segundos	    			=> 0     -- En cuantos segundos mas se ejecuta
				    ,p_TablaParametros 			=> :v_TablaParametros
				 	,p_CreaParam				=> 'S'		       -- S/N indica si vienen parametros
					,p_out_IdPrograma 			=> :v_out_IdPrograma    -- Id el job creado
					,p_out_AdvertenciasContador => v_AdvertenciasContador
					,p_out_AdvertenciasDetalle 	=> v_AdvertenciasDetalle
					,p_out_ErroresContador 		=> v_ErroresContador
					,p_out_ErroresDetalle 		=> v_ErroresDetalle
				);

		p_out_AdvertenciasDetalle 	= SELECT * FROM :v_AdvertenciasDetalle UNION ALL SELECT * FROM :p_out_AdvertenciasDetalle;
		p_out_ErroresDetalle 		= SELECT * FROM :v_ErroresDetalle UNION ALL SELECT * FROM :p_out_ErroresDetalle;
		
		IF (:v_ErroresContador > 0) THEN
			SIGNAL SALIDA_CONTROLADA;
		END IF;
	
  	END;
END;