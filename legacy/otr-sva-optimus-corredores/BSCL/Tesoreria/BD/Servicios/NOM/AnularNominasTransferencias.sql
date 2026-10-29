SET SCHEMA "BSCL";  
DROP PROCEDURE "BSCL.Tesoreria.BD.Servicios.NOM::AnularNominasTransferencias";

CREATE PROCEDURE 
-- 
--  Proposito: Indicar ya sea que la nómina o el archivo de transferencia fue recepcionado exitosamente
--				por la entidad destino (Banco). Una vez recepcionado no se permitira modificar el detalle de la
--				nómina.
--  Autor: NRM - BCS
--  Fecha Creacion: 10/03/2016
--   
"BSCL.Tesoreria.BD.Servicios.NOM::AnularNominasTransferencias" (
    -- Parametros de Validacion
    IN p_IdEmpresa 						BIGINT,  
    IN p_IdUsuario 						BIGINT, 
    IN p_IdModulo 						NVARCHAR(3),    
    IN p_UsaReglasNegocio 				NVARCHAR(1),	-- S/N se usa en caso de servicios anidados.   
   
    -- Parametros de Entrada
	IN p_FechaMovimiento				NVARCHAR(10),
    IN p_RegistrosAAnular 				TABLE ("Id" BIGINT, "Entidad" NVARCHAR(300)),
          
    -- Parametros de Salida
	OUT p_out_ResultadoProceso			TABLE ("Campo" VARCHAR(40),"Id" BIGINT, "Servicio" VARCHAR(250), "Resultado" VARCHAR(40)),

    --Salidas para mensajes de advertencias y errores
    OUT p_out_AdvertenciasContador 		INT,	  
    OUT p_out_AdvertenciasDetalle  		TABLE ("Codigo" NVARCHAR(100),"RequiereAutorizacion" NVARCHAR(40),"Descripcion" NVARCHAR(520)),	
  	OUT p_out_ErroresContador      		INT,
    OUT p_out_ErroresDetalle       		TABLE ("Codigo" NVARCHAR(100),"Descripcion" NVARCHAR(5000))
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
	DECLARE v_errorcuenta 				INT DEFAULT 0;  
	DECLARE v_errorhashmsg 				TABLE ("Codigo" NVARCHAR(100),"Valor" NVARCHAR(520));
	DECLARE v_erroroutmsg  				NVARCHAR(520);		
	DECLARE v_ErrorNoData  				NVARCHAR(100);
	
	DECLARE v_resultadoRegla 			NVARCHAR(40);
	DECLARE v_ErroresDetalle			TABLE ("Codigo" NVARCHAR(40),"Descripcion" NVARCHAR(520));	        	
	DECLARE v_RechazadosPorValidacion	TABLE ("Id" BIGINT, "Entidad" NVARCHAR(300));
	        	
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
		SELECT COUNT(1) INTO p_out_ErroresContador
		FROM :p_out_ErroresDetalle;
	END;
	DECLARE EXIT HANDLER FOR SQLEXCEPTION
	BEGIN
		declare v_errorExt NVARCHAR(40);
		IF ::SQL_ERROR_CODE > 10000 THEN 
			--error pseudo-controlado pero desde un Sub-SP
			call "BSCL.Plataforma.BD.Servicios::ConsultarCodigoMensajeError" (::SQL_ERROR_CODE,v_errorExt);			
			p_out_ErroresDetalle = SELECT * FROM :p_out_ErroresDetalle UNION ALL  
								   SELECT v_errorExt,::SQL_ERROR_MESSAGE FROM dummy;				
			SELECT count(1) into p_out_ErroresContador FROM :p_out_ErroresDetalle;										
		ELSE
			--Error interno no manejado  (El log será captado por la capa de XS control)		
			RESIGNAL; 
		END IF;	
	END;
	
	p_out_AdvertenciasContador		:= 0;
	p_out_ErroresContador			:= 0;
	p_out_AdvertenciasDetalle		= SELECT '' as "Codigo",'' as "RequiereAutorizacion",'' as "Descripcion" FROM dummy where 1=2;
	p_out_ErroresDetalle			= SELECT '' as "Codigo",'' as "Descripcion" FROM dummy where 1=2; 	 	
    p_out_ResultadoProceso			= SELECT '' as "Campo", 0 as "Id", '' as "Servicio", '' as "Resultado" FROM dummy where 1=2;
    
	v_ErroresDetalle				= SELECT '' as "Codigo",'' as "Descripcion" FROM dummy where 1=2;
	v_RechazadosPorValidacion		= SELECT 0 as "Id", '' as "Entidad" FROM dummy where 1=2;
	
	--------------------
	--VALIDACIONES
	--------------------	   
	IF p_UsaReglasNegocio = 'S' THEN
	BEGIN		
		CALL "BSCL.Plataforma.BD.Reglas::ServicioCumpleRequisitosBasicos"(:p_IdEmpresa,:p_IdUsuario,:p_IdModulo,v_Errores,:v_resultadoRegla);
		IF :v_resultadoRegla = 'RECH' THEN
			 SIGNAL SALIDA_CONTROLADA;
		END IF;
		
		CALL "BSCL.Tesoreria.BD.Reglas::ServicioCumpleRequisitosAnularNominaTransferencia"(:p_FechaMovimiento,:p_RegistrosAAnular,:v_RechazadosPorValidacion,:v_ErroresDetalle,:v_resultadoRegla);
		IF :v_resultadoRegla = 'RECH' THEN				
			p_out_ErroresDetalle = SELECT "Codigo","Descripcion" FROM :v_ErroresDetalle;
			SIGNAL SALIDA_CONTROLADA; 	
		ELSEIF :v_resultadoRegla = 'ADVE' THEN
			--LLamada al servicio de autorizacion si se requiere
			p_out_AdvertenciasDetalle = SELECT "Codigo",'N' as "RequiereAutorizacion", "Descripcion" FROM :v_ErroresDetalle;
			SELECT count(1) into p_out_AdvertenciasContador FROM :p_out_AdvertenciasDetalle;
		END IF;
	END; 
	END IF;
    
    
    --------------------
	--LOGICA
	--------------------
	BEGIN
		DECLARE EXIT HANDLER FOR SQL_ERROR_CODE 1299
		BEGIN
			v_ErrorNoData := 'DATA_NO_EXISTE';
			SIGNAL SALIDA_CONTROLADA_NO_DATA;
		END;
	 
	 	--Quitar registros rechazados por validacion
		v_RegistrosAProcesar =
	 	SELECT "Id","Entidad"
	 	  FROM :p_RegistrosAAnular
	 	 WHERE "Id" NOT IN (SELECT "Id" FROM :v_RechazadosPorValidacion);
	 	 	
	 	--********************************
		--* Anulación Total de Nominas   *
		--********************************
		UPDATE 	NTD
		   SET	NTD."IdEstado" = 'ANU'
		  FROM "BSCL.Tesoreria.BD::TES.NominaTransferenciaDetalle" as NTD
		 INNER JOIN :v_RegistrosAProcesar as RP ON RP."Id" = NTD."IdNominaTransferencia"
		 WHERE RP."Entidad" = 'TES.NominaTransferencia';
		
		UPDATE NT
		   SET NT."IdEstado" = 'ANU'
		  FROM "BSCL.Tesoreria.BD::TES.NominaTransferencia" as NT
		 INNER JOIN :v_RegistrosAProcesar as RP ON RP."Id" = NT."IdNominaTransferencia"
		 WHERE RP."Entidad" = 'TES.NominaTransferencia';
		
		
		--********************************
		--* Anulación Parcial de Nominas *
		--********************************
		UPDATE 	NTD
		   SET	NTD."IdEstado" = 'ANU'
		  FROM "BSCL.Tesoreria.BD::TES.NominaTransferenciaDetalle" as NTD
		 INNER JOIN :v_RegistrosAProcesar as RP ON RP."Id" = NTD."IdNominaTransferenciaDetalle"
		 WHERE RP."Entidad" = 'TES.NominaTransferenciaDetalle';

		v_NominaTransferencia = 
		SELECT DISTINCT NT."IdNominaTransferencia"
		  FROM "BSCL.Tesoreria.BD::TES.NominaTransferenciaDetalle" as NTD
		 INNER JOIN :v_RegistrosAProcesar as RP ON RP."Id" = NTD."IdNominaTransferenciaDetalle"
		 INNER JOIN "BSCL.Tesoreria.BD::TES.NominaTransferencia" as NT ON NT."IdNominaTransferencia" = NTD."IdNominaTransferencia"
		 WHERE RP."Entidad" = 'TES.NominaTransferenciaDetalle';

		v_NominaTransferenciaDetalleSinAnular =
		SELECT  NT."IdNominaTransferencia",
				SUM( CASE WHEN NTD."IdEstado" = 'ANU' THEN CFP."Monto" ELSE 0 END ) as "MontoAnulado",
				(COUNT(NTD."IdNominaTransferenciaDetalle") - SUM( CASE WHEN NTD."IdEstado" = 'ANU' THEN 1 ELSE 0 END )) as "NominaTransferenciaSinAnular"				
		  FROM "BSCL.Tesoreria.BD::TES.NominaTransferencia" as NT
		 INNER JOIN :v_NominaTransferencia as NTs ON NTs."IdNominaTransferencia" = NT."IdNominaTransferencia"
		 INNER JOIN "BSCL.Tesoreria.BD::TES.NominaTransferenciaDetalle" as NTD ON NTD."IdNominaTransferencia" = NT."IdNominaTransferencia"
		 INNER JOIN "BSCL.Tesoreria.BD::TES.ComprobanteFormaDePago" as CFP ON CFP."IdComprobanteFormaDePago" = NTD."IdComprobanteFormaDePago"
		 GROUP BY NT."IdNominaTransferencia";
		
		UPDATE NT
		   SET NT."IdEstado" = CASE 
					   			WHEN NTDSA."NominaTransferenciaSinAnular" > 0 THEN 'ING'
					   			WHEN NTDSA."NominaTransferenciaSinAnular" = 0 THEN 'ANU'
					   	      END,
			   NT."Monto" 	= CASE 
					   			WHEN NTDSA."NominaTransferenciaSinAnular" > 0 THEN (NT."Monto" - NTDSA."MontoAnulado")
					   			WHEN NTDSA."NominaTransferenciaSinAnular" = 0 THEN (NT."Monto")
					   	      END					   	      	
		  FROM :v_NominaTransferenciaDetalleSinAnular as NTDSA
		 INNER JOIN "BSCL.Tesoreria.BD::TES.NominaTransferencia" as NT ON NT."IdNominaTransferencia" = NTDSA."IdNominaTransferencia";
		
		
		--*********************************
		--* Actualización de Comprobantes *
		--*********************************
		UPDATE 	CFP
		   SET	CFP."IdEstado" = 'ING',
		   		CFP."IdComprobantePartida" = null
		  FROM "BSCL.Tesoreria.BD::TES.NominaTransferenciaDetalle" as NTD
		 INNER JOIN :v_RegistrosAProcesar as RP ON RP."Id" = CASE 
		 														WHEN (RP."Entidad" = 'TES.NominaTransferencia') THEN NTD."IdNominaTransferencia"
		 														ELSE NTD."IdNominaTransferenciaDetalle"
		 													 END 
		 INNER JOIN "BSCL.Tesoreria.BD::TES.ComprobanteFormaDePago" as CFP ON CFP."IdComprobanteFormaDePago" = NTD."IdComprobanteFormaDePago";
		
		
		v_Comprobantes =
		SELECT DISTINCT C."IdComprobante", (CASE WHEN NT."EsNomina" = 'S' THEN 'LIQ' WHEN NT."EsTransferencia" = 'S' THEN 'GIR' END) AS "Estado"
		  FROM "BSCL.Tesoreria.BD::TES.NominaTransferenciaDetalle" as NTD
		 INNER JOIN :v_RegistrosAProcesar as RP ON RP."Id" = CASE 
		 														WHEN (RP."Entidad" = 'TES.NominaTransferencia') THEN NTD."IdNominaTransferencia"
		 														ELSE NTD."IdNominaTransferenciaDetalle"
		 													 END
		 INNER JOIN "BSCL.Tesoreria.BD::TES.NominaTransferencia" as NT ON NT."IdNominaTransferencia" = NTD."IdNominaTransferencia"
		 INNER JOIN "BSCL.Tesoreria.BD::TES.ComprobanteFormaDePago" as CFP ON CFP."IdComprobanteFormaDePago" = NTD."IdComprobanteFormaDePago"
		 INNER JOIN "BSCL.Tesoreria.BD::TES.Comprobante" as C ON C."IdComprobante" = CFP."IdComprobante";
		
		v_ComprobantesSinGirar = 
		SELECT  C."IdComprobante", Cs."Estado",
				( COUNT(CFP."IdComprobanteFormaDePago")  -  
				  SUM( CASE WHEN CFP."IdEstado" <> Cs."Estado" THEN 1 ELSE 0 END )
				) as "ComprobantesSinGirar"
		  FROM "BSCL.Tesoreria.BD::TES.Comprobante" as C
		 INNER JOIN :v_Comprobantes as Cs ON Cs."IdComprobante" = C."IdComprobante"
		 INNER JOIN "BSCL.Tesoreria.BD::TES.ComprobanteFormaDePago" as CFP ON CFP."IdComprobante" = C."IdComprobante"
		 WHERE CFP."IdEstado" NOT IN ('ANU')
		 GROUP BY C."IdComprobante",Cs."Estado";
		
		
		UPDATE C
		   SET C."IdEstado" = CASE 
					   			WHEN "ComprobantesSinGirar" > 0 THEN (CASE WHEN CSG."Estado" = 'LIQ' THEN 'LIP' ELSE 'GIP'END)
					   			WHEN "ComprobantesSinGirar" = 0 THEN 'ING'
					   	      END
		  FROM :v_ComprobantesSinGirar as CSG
		 INNER JOIN "BSCL.Tesoreria.BD::TES.Comprobante" as C ON C."IdComprobante" = CSG."IdComprobante";
		
		
		--Actualización Partida
		SELECT C.*
		  FROM "BSCL.Tesoreria.BD::TES.NominaTransferenciaDetalle" as NTD
		 INNER JOIN "BSCL.Tesoreria.BD::TES.NominaTransferencia" as NT ON NT."IdNominaTransferencia" = NTD."IdNominaTransferencia"
		 INNER JOIN :v_RegistrosAProcesar as RP ON RP."Id" = CASE 
		 														WHEN (RP."Entidad" = 'TES.NominaTransferencia') THEN NTD."IdNominaTransferencia"
		 														ELSE NTD."IdNominaTransferenciaDetalle"
		 													 END
		 INNER JOIN "BSCL.Tesoreria.BD::TES.Comprobante" as C ON C."IdComprobante" = NT."IdComprobantePartidaEgreso";

		
		--********************************
		--* Actualizar Saldos            *
		--********************************
		/*v_CuentaBanco =
		SELECT DISTINCT "IdCuentaBancariaCorredora" as "IdCuentaBanco"
		  FROM "BSCL.Tesoreria.BD::TES.ComprobanteFormaDePago" CFP
		 INNER JOIN :v_ComprobantesALiquidar AS CAL ON (CFP."IdComprobante" = CAL."IdComprobante")
		 WHERE COALESCE(CFP."IdCuentaBancariaCorredora",0) > 0;

		CALL "BSCL.Tesoreria.BD.Servicios.BAN::ActualizarSaldosCuentaBanco" (
			p_FechaMovimiento 	=> :p_FechaMovimiento,
			p_CuentaBanco		=> :v_CuentaBanco
		);*/
		 
		--********************************
		--* Resultado del proceso *
		--********************************
		p_out_ResultadoProceso =
		SELECT  'IdNominaTransferencia' AS "Campo",
				"Id" as "Id",
				'BSCL.Tesoreria.BD.Servicios::AnularNominasTransferencias' as "Servicio",
				'ANU' as "Resultado"
		  FROM :v_RegistrosAProcesar as RP
		 UNION ALL
		SELECT  'IdNominaTransferencia' AS "Campo",
				"Id" as "Id",
				'BSCL.Tesoreria.BD.Servicios::AnularNominasTransferencias' as "Servicio",
				'RECH' as "Resultado"
		  FROM :v_RechazadosPorValidacion as RV
		 UNION ALL
		SELECT "Campo","Id","Servicio","Resultado" FROM :p_out_ResultadoProceso;
	END;
END;