SET SCHEMA "BSCL";
GO
DROP PROCEDURE "BSCL.Custodia.BD.Servicios::EVC.ProcesarEventoCapitalCanje";
GO
CREATE PROCEDURE "BSCL.Custodia.BD.Servicios::EVC.ProcesarEventoCapitalCanje" (


--VARIABLES DE ENTRADA OBLIGATORIAS
    IN p_IdEmpresa          				BIGINT 			default 0,
    IN p_IdUsuario          				BIGINT 			default 0,
    IN p_IdModulo           				NVARCHAR(3) 	default '',  
	
	IN p_IdEventoCapital					BIGINT     		default 0,
	IN p_IdTipoEventoCapital				NVARCHAR(2) 	default '',
	IN p_Secuencia							BIGINT 			default 0,
	IN p_IdClasificacionTipoInstrumento		NVARCHAR(3) 	default '',
	IN p_IdTipoInstrumento					NVARCHAR(3) 	default '',  
	IN p_IdInstrumento						BIGINT 			default 0,
	IN p_Nemo								NVARCHAR(40) 	default '',
	IN p_Factor								DECIMAL(26,8) 	default 0,
	IN p_Paridad							DECIMAL(26,8) 	DEFAULT 0,
	IN p_IdMoneda							NVARCHAR(3) 	default '',
	IN p_IdInstrumentoNuevo					BIGINT 			default 0,
	IN p_NemoNuevo							NVARCHAR(40) 	default '',
	IN p_FactorNuevo						DECIMAL(26,8) 	default 0,
	IN p_FechaLimite						NVARCHAR(10) 	default '0001-01-01',     
	IN p_FechaPago							NVARCHAR(10) 	default '0001-01-01',     
	IN p_IdEstado							NVARCHAR(3) 	default '',
	IN p_NumeroDividendo					BIGINT 			default 0,
	
-- Parametros de Salida
	OUT p_out_InformacionContador  		INT,
	OUT p_out_InformacionDetalle   		TABLE ("Codigo" NVARCHAR(100),"Descripcion" NVARCHAR(5000)),
	
	OUT p_out_AdvertenciasContador 		int,
	OUT p_out_AdvertenciasDetalle 		table ("Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(5000)),	
	
	OUT p_out_ErroresContador 			int,   
	OUT p_out_ErroresDetalle 			table ("Codigo" nvarchar(40),"Descripcion" nvarchar(5000))
	 )   
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
	AS
	
BEGIN SEQUENTIAL EXECUTION
	--------------------
	--DECLARE Manejo de Errores
	--------------------	
	DECLARE abort int default 0;	
	DECLARE SALIDA_CONTROLADA CONDITION; 
	DECLARE v_errorcuenta INT DEFAULT 0;  
	DECLARE v_errorhashmsg table ("Codigo" nvarchar(40),"Valor" nvarchar(520));
	DECLARE v_erroroutmsg  nvarchar(520);	        	
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA
		 
	BEGIN
		--salida/error/advertencia controlado por este servicio
		select count(1) into p_out_ErroresContador 
		from :p_out_ErroresDetalle;
	END;


	DECLARE EXIT HANDLER FOR SQLEXCEPTION
	BEGIN
		declare v_errorExt nvarchar(40);
		IF ::SQL_ERROR_CODE > 10000 THEN
			--error pseudo-controlado pero desde un Sub-SP

			call "BSCL.Plataforma.BD.Servicios::ConsultarCodigoMensajeError" (::SQL_ERROR_CODE,v_errorExt);			
			p_out_ErroresDetalle = select * from :p_out_ErroresDetalle UNION ALL  
								   select v_errorExt,::SQL_ERROR_MESSAGE from dummy;				
			select count(1) into p_out_ErroresContador 
			from :p_out_ErroresDetalle;										
		ELSE
			--Error interno no manejado  (El log sera captado por la capa de XS control)		
			RESIGNAL; 
		END IF;		
	END;

	p_out_AdvertenciasContador	:= 0;
	p_out_ErroresContador		:= 0;
	p_out_AdvertenciasDetalle	= SELECT '' AS "Codigo",'' AS "RequiereAutorizacion",'' AS "Descripcion" FROM dummy WHERE 1=2;
	p_out_ErroresDetalle		= SELECT '' AS "Codigo",'' AS "Descripcion" 	FROM dummy WHERE 1=2;

	BEGIN
		-----------------------------------------------------
		--Declaraciones Variables
		-----------------------------------------------------
	
	    DECLARE	v_Instrumentos 			TABLE ( 
												"IdClasificacionTipoInstrumento"	NVARCHAR(3),
												"IdTipoInstrumento"					NVARCHAR(3),
												"IdInstrumento"						BIGINT ,
												"IdEmisor"							NVARCHAR(40) ,
												"FechaVencimiento"					DATE ,
												"IdTipoReajuste"					NVARCHAR(3),
												"EsFungible"						NVARCHAR(1),
												"IdBoveda"							NVARCHAR(3)
											);

	    DECLARE v_Carteras 				TABLE( 
												"FolioCartera"						BIGINT, --TAMBIEN FOLIO CARTERA RELACIONADA EN OTRAS TABLAS
												"FolioGarantia"						BIGINT,
												"FolioSubyacente"					BIGINT,
												"IdCliente"							BIGINT,
												"IdPortafolio"						BIGINT
											);
		
		--variables de proceso
		DECLARE v_FechaSistema 			TIMESTAMP;
		DECLARE v_FactorMayor			NVARCHAR;
		
		
		v_FactorMayor 	:= 'S';
		
		p_out_TblId = SELECT 0 as "Ids" from dummy where 1=2;

		-- Campos segun linea de Negocio (no se obtienen desde la tabla movimiento en transito)
		p_TblErrores = SELECT '' as "Codigo", '' as "Descripcion" FROM DUMMY WHERE 1=2;
		v_TablaErrores = SELECT '' as "Codigo", '' as "Descripcion" FROM DUMMY WHERE 1=2;
		   
	    CALL  "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaSistema" (p_IdEmpresa,v_FechaSistema);
	             
		-----------------------------------------------------
		--Validaciones basicas (Codigos,Existencias,etc)
		-----------------------------------------------------
		BEGIN
			IF (p_Factor > p_FactorNuevo) THEN
	
				v_FactorMayor 	:= 'N';			
	
				SELECT COUNT(1) INTO p_out_ErroresContador
			  	FROM "BSCL.Orden.BD.Vistas::ConsultarOrden" 
				WHERE
					"IdInstrumento"	= p_IdInstrumento	and
					"Instrumento"		= p_Nemo 		and
					"OrdenVigente" = 'S'; 
					
			  
				IF (p_out_ErroresContador > 0) THEN				
		           	v_TablaCodigosMensajes =	Select 	p_NumeroDividendo 							As "IdInterno", 
													'UI_EVENTO_CAPITAL_Y_O_SECUENCIA_YA_EXISTE'		As "Codigo",
													'p_NumeroDividendo' 							As "CodigoDato1", 
													Cast(p_NumeroDividendo As NVARCHAR(520)) 		as "ValorDato1",
													'p_Secuencia' 									As "CodigoDato2", 
													Cast(p_Secuencia As NVARCHAR(520)) 				As "ValorDato2" 
											From dummy;
				                        
					CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionarioMasivo" (:v_TablaCodigosMensajes,:v_out_hash);
					
					p_out_ErroresDetalle = 	Select "Codigo", "Descripcion" 
											From :v_out_hash;	
			 	END IF;
			 	
				--Si existen errores en las validaciones debe salir
				SELECT COUNT(1) into p_out_ErroresContador FROM :p_out_ErroresDetalle; 
				
				IF :p_out_ErroresContador > 0 THEN
					SIGNAL SALIDA_CONTROLADA;
				END IF;				
	
				-- Se valida que no existan Simultaneas en Vigentes.
	
				SELECT count(1) INTO p_out_ErroresContador
				FROM "BSCL.Custodia.BD.Vistas::SIM.ConsultarCarteraSimultaneaVigentes"
				WHERE 
					"FechaVencimientoOperacion" = v_FechaSistema	and
					"suby_IdInstrumento"		= p_IdInstrumento				and
					"suby_Nemotecnico"			= p_Nemo ;
	
				  
				IF (p_out_ErroresContador > 0) THEN				
		           	v_TablaCodigosMensajes =	SELECT 	p_NumeroDividendo 						As "IdInterno", 
													'UI_EVENTO_CAPITAL_Y_O_SECUENCIA_YA_EXISTE'	As "Codigo",
													'p_NumeroDividendo' 						As "CodigoDato1", 
													Cast(p_NumeroDividendo As NVARCHAR(520)) 	as "ValorDato1",
													'p_Secuencia' 								As "CodigoDato2", 
													Cast(p_Secuencia As NVARCHAR(520)) 			As "ValorDato2" 
												FROM dummy;
				                        
					CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionarioMasivo" (:v_TablaCodigosMensajes,:v_out_hash);
					
					p_out_ErroresDetalle = 	Select "Codigo", "Descripcion" 
											From :v_out_hash;
				END IF;
			 	
				--Si existen errores en las validaciones debe salir
				SELECT COUNT(1) into p_out_ErroresContador 
				FROM :p_out_ErroresDetalle; 
				
				IF :p_out_ErroresContador > 0 THEN
					SIGNAL SALIDA_CONTROLADA;
				END IF;			
		
				-- Se valida que no existan Prestamos Vigentes.
		
				SELECT count(1) INTO p_out_ErroresContador
			  	FROM "BSCL.Custodia.BD.Vistas::PRT.ConsultarResumenPrestamoVigente"
			  	WHERE
			  		"IdInstrumentoSubyacente"	= p_IdInstrumento				and
					"NemoSubyacente"			= p_Nemo ;
				  
				IF (p_out_ErroresContador > 0) THEN				
					p_out_ErroresDetalle = SELECT 'Prestamos' as "Codigo", 'Existen Prestamos Vigentes' as "Descripcion" FROM DUMMY;
			 	END IF;
				
			END IF;
		END;
		-----------------------------------------------------
		--fin Validaciones basicas (Codigos,Existencias,etc)
		-----------------------------------------------------

		-----------------------------------------------------
		--proceso de Negocio
		---------------------------------------------------
		BEGIN

		DECLARE v_TipoSocioNegocio			NVARCHAR(1):='';
		DECLARE v_NombreAgente				NVARCHAR(100):='';
		DECLARE v_CodigoSucursal			NVARCHAR(40):='';
		DECLARE v_CodigoUsuarioAgente		NVARCHAR(40):='';
		DECLARE v_count_errores_custodia 	BIGINT;
		DECLARE v_KeyFlujoCustodia			BIGINT;


--------------------------------------------------------------------------------------------------------
--- EVENTO A FAVOR BOVEDA
--- Inserta registros en tabla "BSCL.Custodia.BD::CUS.EventoCapitalCliente" a partir de resultado 
--- obtenido del servicio anterior ("BSCL.Custodia.BD.Servicios::ConsultarFlujoCustodia")
--------------------------------------------------------------------------------------------------------    
	v_carteras_activas = SELECT
      CAR."IdCartera"                AS "IdCartera"
    , CAR."FolioCartera"              AS "FolioCartera"
    , CAR."IdEmpresa"                AS "IdEmpresa"
    , CAR."FechaOperacion"              AS "FechaOperacion"
    , CAR."FechaVencimientoOperacion"        AS "FechaVencimientoOperacion"
    , CAR."FechaLiquidacion"            AS "FechaLiquidacion"
    , CAR."IdCliente"                AS "IdCliente"
    , CAR."IdPortafolio"              AS "IdPortafolio"
    ,CASE 
      WHEN CAR."IdTipoInstrumento" = 'SIM' or CAR."IdTipoInstrumento" = 'PRT' or CAR."IdTipoInstrumento" = 'EMP' then
        SUB."Nemotecnico"
      ELSE CAR."Nemotecnico"
    END                       AS "Nemotecnico"
    , CASE CAR."IdTipoInstrumento"
        WHEN 'SIM' THEN
          CASE CAR."Nemotecnico"   
            WHEN 'SIMULTANEA VENTA'   THEN 'SIV'
            WHEN 'SIMULTANEA COMPRA'  THEN 'SIC'
            ELSE 'SIM'
          END
            
        
        WHEN 'PRT' THEN 'PRT'
        WHEN 'EMP' THEN 'EMP'
        ELSE 'CUS'
      END                      AS "TipoCartera"
    , CAR."IdTipoInstrumento"            AS "IdTipoInstrumento"
    , CAR."INGCantidad"                AS "Cantidad"
    , CAR."IdBoveda"                AS "IdBoveda"
    , CAR."IdBolsa"                  AS "IdBolsa"
    , CAR."IdConcepto"                AS "IdConcepto"
    , CAR."IdEstado"                AS "IdEstado"
    , CAR."PrecioCompra"              AS "PrecioCompra"
  
  FROM "BSCL.Custodia.BD::CUS.Cartera" AS CAR
  LEFT JOIN (
    SELECT "FolioCarteraRelacionada","Nemotecnico","NroFila" FROM (
      SELECT  "IdSubyacente","FolioCarteraRelacionada","Nemotecnico","IdEstado", row_number() over(partition by "FolioCarteraRelacionada" ORDER BY "IdSubyacente" ) as "NroFila" 
      FROM "BSCL.Custodia.BD::CUS.Subyacente" AS miSub WHERE "IdEstado" = 'VIG' 
    ) WHERE "NroFila" = 1
  ) AS SUB ON SUB."FolioCarteraRelacionada" = CAR."FolioCartera"
  WHERE "IdEstado" = 'VIG'; 
	INSERT INTO "BSCL.Custodia.BD::CUS.EventoCapitalCliente"
   	(
		"IdEventoCapitalCliente",			
		"IdEventoCapital",			
		"IdTipoEventoCapital",		
		"Secuencia",			
		"IdSocioNegocio",					
		"TipoSocioNegocio",			   		
		"IdPortafolio",				
		"IdTipoDocumentoIdentidad",	
		"DocumentoIdentidad",	
		"NombreCliente",				
		"IdClasificacionTipoInstrumento",	
		"IdTipoInstrumento",		
		"IdInstrumento",			
		"Nemo",					
		"IdInstrumentoNuevo",
		"NemoNuevo",						
		"FechaCierre",						
		"FechaPago",				
		"CantidadDerecho",		
		"TipoCartera",						
		"CantidadNueva",					
		"MontoCapital",
		"Precio",						
		"PrecioNuevo",						
		"MontoInteres",				
		"Total",				
		"IdMoneda",						
		"SecuenciaCupon",
		"IdEmpleadoEjecutivoPortafolio",
		"CodigoUsuarioAgente",				
		"NombreAgente",				
		"CodigoSucursal",		
		"FechaHoraIngresoRegistro",
		"FechaHoraUltimaActualizacion",		
		"IdUsuario",						
		"IdEstado",
		"IdBoveda",
		"IdBolsa",
		"NumeroDividendo",
		"Origen")
	SELECT
	    "BSCL.Custodia.BD.Secuencias::EventoCapitalCliente".NEXTVAL,			--	IdEventoCapitalCliente
		p_IdEventoCapital,														--	IdEventoCapital
		p_IdTipoEventoCapital,													--	IdTipoEventoCapital
		p_Secuencia,															--	Secuencia
		IFNULL(SOC."IdSocioDeNegocio",0),										--	IdSocioNegocio
		v_TipoSocioNegocio,														--	TipoSocioNegocio
		IFNULL(FT."IdPortafolio",0),											--	IdPortafolio
		'R',--																	--	IdTipoDocumentoIdentidad
		IFNULL(CL."DocumentoIdentidad",''),										--	DocumentoIdentidad			
		IFNULL(CL."NombreCompleto",''),											--	NombreCliente
		p_IdClasificacionTipoInstrumento,										--	IdClasificacionTipoInstrumento
		p_IdTipoInstrumento,													--  IdTipoInstrumento
		p_IdInstrumento, 														--	IdInstrumento
		p_Nemo,																	--	Nemo
		p_IdInstrumentoNuevo,													--	IdInstrumentoNuevo		
		p_NemoNuevo,															--  NemoNuevo
		TO_DATE(p_FechaLimite, 'YYYY-MM-DD'),									--	FechaCierre
		TO_DATE(p_FechaPago, 'YYYY-MM-DD'),										--	FechaPago
		SUM(IFNULL(FT."Cantidad",0)),											-- Cantidad Derecho
		FT."TipoCartera",
		CASE v_FactorMayor
			WHEN 'N' THEN 	(SUM(IFNULL(FT."Cantidad",0)) / p_Factor)
			ELSE 		  	(SUM(IFNULL(FT."Cantidad",0)) * p_FactorNuevo)
		END,																		--	CantidadNueva		
		
		0 ,																			--	MontoCapital

		IFNULL(FT."PrecioCompra",0),												-- 	Precio
		CASE v_FactorMayor
			WHEN 'N' THEN 	(IFNULL(FT."PrecioCompra",0) * p_Factor)
			ELSE 			(IFNULL(FT."PrecioCompra",0) / p_FactorNuevo)
		END, 																		-- 	Precio Nuevo

		0,																			--	MontoInteres

		CASE v_FactorMayor
			WHEN 'N' THEN 	(SUM(IFNULL(FT."Cantidad",0)) / p_Factor) * (FT."PrecioCompra" * p_Factor)
			ELSE 			(SUM(IFNULL(FT."Cantidad",0)) * p_FactorNuevo) * (FT."PrecioCompra" / p_FactorNuevo)
		END,																		--	Total

		p_IdMoneda,																	--	IdMoneda
		p_Secuencia,																--	SecuenciaCupon
--		v_CodigoUsuarioAgente,														--	CodigoUsuarioAgente
--		v_NombreAgente,																--	NombreAgente
--		v_CodigoSucursal,															--	CodigoSucursal
		COALESCE(EJE."IdEmpleadoEjecutivoPortafolio",0),						
		COALESCE(EJE."CodigoEmpleado",''),										--	"CodigoUsuarioAgente"
		COALESCE(EJE."NombreCompletoEjecutivo",''),								--	"NombreAgente"
		COALESCE(EJE."CodigoSucursal",''),										--	"CodigoSucursal"
		Current_UTCTIMESTAMP,														--	FechaHoraIngresoRegistro
		Current_UTCTIMESTAMP,														--	FechaHoraUltimaActualizacion
		p_IdUsuario,																--	IdUsuario
		p_IdEstado,																	--	IdEstado
		CASE FT."IdBoveda"
			WHEN '' THEN 'CIA'
			ELSE FT."IdBoveda"
		END,																		--  IdBoveda
		CASE FT."IdBolsa" 
			when '' then 'BCS'
			ELSE IFNULL(FT."IdBolsa",'BCS')											--  IdBolsa
		END,
		p_NumeroDividendo,
		'SIS' 
	--FROM :v_out_FlujoDetallado 													AS FT
		FROM  :v_carteras_activas	AS FT
			LEFT JOIN 	"BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente"				AS CL
				ON (FT."IdCliente" = CL."IdCliente")
			LEFT JOIN 	"BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio" 		AS SOC
				ON (CL."IdCliente" = SOC."IdCliente")
			LEFT JOIN 	"BSCL.SocioNegocio.BD.Vistas::EJE.ConsultarEjecutivoPortafolio"	AS EJE
				ON (FT."IdPortafolio" = EJE."IdPortafolio")
		WHERE
				FT."IdEmpresa" 		= p_IdEmpresa  
			AND FT."Nemotecnico"	= p_Nemo
			AND FT."IdCliente" 		> 0
			AND IFNULL(SOC."IdSocioDeNegocio",0) > 0
			AND FT."FechaOperacion" <= p_FechaLimite
			AND FT."IdEstado" = 'VIG'
		
	GROUP BY
		p_IdEventoCapital,
		p_IdTipoEventoCapital,
		p_Secuencia,
		SOC."IdSocioDeNegocio",
		v_TipoSocioNegocio,
		FT."IdPortafolio",
		CL."DocumentoIdentidad",
		CL."NombreCompleto",
		p_IdClasificacionTipoInstrumento,
		p_IdTipoInstrumento,
		p_IdInstrumento,
		p_Nemo,
		p_IdInstrumentoNuevo,
		p_NemoNuevo,
		p_FechaPago,
		--FT."ItemOrigen",
		FT."TipoCartera",
		--FT."ItemCalculo",
		FT."PrecioCompra",
		COALESCE(EJE."IdEmpleadoEjecutivoPortafolio",0),
		COALESCE(EJE."CodigoEmpleado",''),
		COALESCE(EJE."NombreCompletoEjecutivo",''),
		COALESCE(EJE."CodigoSucursal",''),		
		CASE FT."IdBoveda"
			WHEN '' THEN 'CIA'
			ELSE FT."IdBoveda"
		END,
		CASE FT."IdBolsa" 
			WHEN '' then 'BCS'
			ELSE IFNULL(FT."IdBolsa",'BCS')										
		END;


	UPDATE "BSCL.Custodia.BD::CUS.EventoCapital"
	SET "IdEstado" = 'PRO'
	WHERE "IdEventoCapital" = p_IdEventoCapital;
	
	

	v_TablaCodigosMensajes =	Select 	p_NumeroDividendo 					As "IdInterno", 
											'UI_OPERACION_PROCESAR_EXITO'	As "Codigo",
											'' 								As "CodigoDato1", 
											'' 								as "ValorDato1",
											'' 								As "CodigoDato2", 
											'' 								As "ValorDato2" 
									From dummy;
		                        
	CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionarioMasivo" (:v_TablaCodigosMensajes,:v_out_hash);
	
	p_out_InformacionContador = 1;
	
	p_out_InformacionDetalle = 	Select "Codigo", "Descripcion" 
								From :v_out_hash;	
									
    if p_out_ErroresContador > 0 then 
		SIGNAL SALIDA_CONTROLADA;
	end if;
	
	
    
	END;
		
		-----------------------------------------------------
		--fin proceso de Negocio
		---------------------------------------------------

	END;
END; 
GO