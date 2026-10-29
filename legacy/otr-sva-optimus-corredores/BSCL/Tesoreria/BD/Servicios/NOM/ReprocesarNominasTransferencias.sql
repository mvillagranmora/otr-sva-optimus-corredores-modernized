SET SCHEMA "BSCL";  
DROP PROCEDURE "BSCL.Tesoreria.BD.Servicios.NOM::ReprocesarNominasTransferencias";

CREATE PROCEDURE 
-- 
--  Proposito: Indicar ya sea que la nómina o el archivo de transferencia fue recepcionado exitosamente
--				por la entidad destino (Banco). Una vez recepcionado no se permitira modificar el detalle de la
--				nómina.
--  Autor: NRM - BCS
--  Fecha Creacion: 10/03/2016
--   
"BSCL.Tesoreria.BD.Servicios.NOM::ReprocesarNominasTransferencias" (
    -- Parametros de Validacion
    IN p_IdEmpresa 						BIGINT,  
    IN p_IdUsuario 						BIGINT, 
    IN p_IdModulo 						NVARCHAR(3),    
    IN p_UsaReglasNegocio 				NVARCHAR(1),	-- S/N se usa en caso de servicios anidados.   
   
    -- Parametros de Entrada
	IN p_FechaMovimiento				NVARCHAR(10),
    IN p_RegistrosAReprocesar 			TABLE (	 "Id" 						BIGINT --Puede ser IdNominaTransferencia o IdNominaTransferenciaDetalle
    										
    											--INFO CORREDORA
												,"IdChequera"				BIGINT
												,"NumeroChequeDesde"		BIGINT
												,"CantidadCheques"			BIGINT
												
												--INFO SOCIO
    											,"IdCuentaBancariaSocio"	BIGINT
												,"IdTarjetaBancariaSocio" 	BIGINT
												,"NumeroSerieSocio"			NVARCHAR(40)
												,"NumeroDocumentoSocio"   	BIGINT
												
												--INFO CLAVE
												,"ClaveDCV" 				NVARCHAR(50)
												),          
          
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
	DECLARE v_RechazadosPorValidacion	TABLE ("Id" 	BIGINT);
	        	
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
	v_RechazadosPorValidacion		= SELECT  0 as "Id" FROM dummy WHERE 1=2; --SELECT  0 as "Id",0 as "IdChequera",0 as "NumeroChequeDesde",0 as "CantidadCheques",0 as "IdCuentaBancariaSocio",
									  --		  0 as "IdTarjetaBancariaSocio",'' as "NumeroSerieSocio",'' as "NumeroChequeSocio" FROM dummy where 1=2;
	
	--------------------
	--VALIDACIONES
	--------------------	   
	IF p_UsaReglasNegocio = 'S' THEN
	BEGIN		
		CALL "BSCL.Plataforma.BD.Reglas::ServicioCumpleRequisitosBasicos"(:p_IdEmpresa,:p_IdUsuario,:p_IdModulo,v_Errores,:v_resultadoRegla);
		IF :v_resultadoRegla = 'RECH' THEN
			 SIGNAL SALIDA_CONTROLADA;
		END IF;
		
		CALL "BSCL.Tesoreria.BD.Reglas::ServicioCumpleRequisitosReprocesarNominaTransferencia"(:p_FechaMovimiento,:p_RegistrosAReprocesar,:v_RechazadosPorValidacion,:v_ErroresDetalle,:v_resultadoRegla);
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
	 	SELECT   "Id"
				,"IdChequera"
				,"NumeroChequeDesde"
				,"CantidadCheques"
    			,"IdCuentaBancariaSocio"
				,"IdTarjetaBancariaSocio"
				,"NumeroSerieSocio"
				,"NumeroDocumentoSocio"
				,"ClaveDCV"
	 	  FROM :p_RegistrosAReprocesar
	 	 WHERE "Id" NOT IN (SELECT "Id" FROM :v_RechazadosPorValidacion);

		--******************************************************
		--* Actualización de la forma de pago del Comprobantes *
		--******************************************************
		v_Registros = 
		SELECT 	--ROW_NUMBER() over (partition by NTD."IdNominaTransferencia",RP."IdChequera") as "RowNumber",
				NTD."IdNominaTransferencia",
				NTD."IdNominaTransferenciaDetalle",
				CFP."IdComprobanteFormaDePago",

		   		RP."IdCuentaBancariaSocio",
				CBS."IdTipoCuentaBancaria",
				CBS."NombreCuentaBancariaFull",
				CBS."NombreTipoCuentaBancaria",
	            CBS."NumeroCuentaBancaria",
				RP."IdTarjetaBancariaSocio",				
				CPTB."IdTipoTarjetaBancaria",
				CPTB."NombreTipoTarjetaBancaria",
 				CPTB."NumeroTarjetaBancaria",
 				CPTB."FechaVencimiento",
 				RP."NumeroSerieSocio",
				RP."NumeroDocumentoSocio",
				
				RP."CantidadCheques",
				RP."IdChequera",
				RP."NumeroChequeDesde",
				(RP."NumeroChequeDesde" -1 + RP."CantidadCheques") as "NumeroChequeHasta",
				CFP."NumeroDocumentoCorredora",
				RP."ClaveDCV"
		  FROM "BSCL.Tesoreria.BD::TES.NominaTransferenciaDetalle" as NTD
		 INNER JOIN :v_RegistrosAProcesar as RP ON RP."Id" = CASE WHEN (RP."IdChequera" > 0) THEN NTD."IdNominaTransferencia" ELSE NTD."IdNominaTransferenciaDetalle" END
		 INNER JOIN "BSCL.Tesoreria.BD::TES.ComprobanteFormaDePago" as CFP ON CFP."IdComprobanteFormaDePago" = NTD."IdComprobanteFormaDePago"
		  LEFT OUTER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaCuentasBancarias" AS CBS ON (CBS."IdCuentaBancaria" = RP."IdCuentaBancariaSocio")
		  LEFT OUTER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaTarjetasBancarias" AS CPTB ON CPTB."IdTarjetaBancaria" = RP."IdTarjetaBancariaSocio";
		
		
		v_TalonarioCheques = 
		 SELECT RP."IdComprobanteFormaDePago",
		 		RP."IdChequera",
				RP."NumeroChequeDesde" - 1 + ROW_NUMBER() over (partition by RP."IdNominaTransferencia",RP."IdChequera") as "NumeroCheque",
				RP."NumeroChequeHasta", 
				p_FechaMovimiento as "FechaEmisionCheque"
		  FROM :v_Registros as RP
		 WHERE RP."NumeroDocumentoCorredora" = 0
		 ORDER BY RP."IdComprobanteFormaDePago";
		 
		v_TalonarioCheques = 
		SELECT RP."IdComprobanteFormaDePago",
		 		RP."IdChequera",
				RP."NumeroCheque",
				RP."NumeroChequeHasta", 
				RP."FechaEmisionCheque"
		  FROM :v_TalonarioCheques as RP WHERE RP."NumeroCheque" <= RP."NumeroChequeHasta";

		UPDATE 	CFP		
		   SET  CFP."IdChequera"					= COALESCE(TC."IdChequera",CFP."IdChequera"),
		   		CFP."NumeroDocumentoCorredora"		= COALESCE(TC."NumeroCheque",CFP."NumeroDocumentoCorredora"),
		   		CFP."FechaDocumentoPagoCorredora"	= COALESCE(TC."FechaEmisionCheque",CFP."FechaDocumentoPagoCorredora"),
		   		CFP."IdCuentaBancariaSocio"			= RP."IdCuentaBancariaSocio",
				CFP."IdTipoCuentaBancariaSocio"		= COALESCE(RP."IdTipoCuentaBancaria",''),
			 	CFP."NombreCuentaBancariaSocio"		= COALESCE(RP."NombreCuentaBancariaFull",''),
				CFP."NombreTipoCuentaBancariaSocio" = COALESCE(RP."NombreTipoCuentaBancaria",''),
	            CFP."NumeroCuentaBancariaSocio"		= COALESCE(RP."NumeroCuentaBancaria",''),
				CFP."IdTarjetaBancariaSocio"		= RP."IdTarjetaBancariaSocio",				
				CFP."IdTipoTarjetaBancariaSocio"	= COALESCE(RP."IdTipoTarjetaBancaria",0),
				CFP."NombreTipoTarjetaBancariaSocio"= COALESCE(RP."NombreTipoTarjetaBancaria",''),
 				CFP."NumeroTarjetaBancariaSocio"	= COALESCE(RP."NumeroTarjetaBancaria",''), 				
 				CFP."NumeroSerieSocio"				= RP."NumeroSerieSocio",
				CFP."NumeroDocumentoSocio"			= RP."NumeroDocumentoSocio",
				CFP."ClaveDCV"						= RP."ClaveDCV"
		  FROM :v_Registros as RP
		  LEFT JOIN :v_TalonarioCheques as TC ON TC."IdComprobanteFormaDePago" = RP."IdComprobanteFormaDePago"
		 INNER JOIN "BSCL.Tesoreria.BD::TES.ComprobanteFormaDePago" as CFP ON CFP."IdComprobanteFormaDePago" = RP."IdComprobanteFormaDePago";


		--************************************************************
		--* Actualización Chequera (en el caso de nómina de cheques) *
		--************************************************************
		
		UPDATE C
		   SET	C."NumeroChequeActual"	= (C."NumeroChequeActual" + RP."CantidadCheques"),
		   	   	C."Remanente"			= (C."Remanente" - RP."CantidadCheques")
		  FROM "BSCL.Tesoreria.BD::TES.Chequera" as C
		 INNER JOIN :v_RegistrosAProcesar as RP ON RP."IdChequera" = C."IdChequera"
		 WHERE RP."IdChequera" > 0;
		
		--***************************************************
		--* Realizar impresión de la nomina o transferencia *
		--***************************************************
		
		
		--********************************
		--* Resultado del proceso *
		--********************************
		p_out_ResultadoProceso =
		SELECT  CASE WHEN (RP."IdChequera" > 0) THEN 'IdNominaTransferencia' ELSE 'IdNominaTransferenciaDetalle' END AS "Campo",
				RP."Id" AS "Id",
				'BSCL.Tesoreria.BD.Servicios::ReprocesarNominasTransferencias' as "Servicio",
				'OK' as "Resultado"
		  FROM :v_RegistrosAProcesar as RP;
		/*UNION ALL
		SELECT  CASE WHEN (RV."IdChequera" > 0) THEN 'IdNominaTransferencia' ELSE 'IdNominaTransferenciaDetalle' END AS "Campo",
				RV."Id" AS "Id",
				'BSCL.Tesoreria.BD.Servicios::ReprocesarNominasTransferencias' as "Servicio",
				'RECH' as "Resultado"
		  FROM :v_RechazadosPorValidacion as RV;*/
		
		  
		--********************************
		--*             LOG              *
		--********************************		  
	END;
END;