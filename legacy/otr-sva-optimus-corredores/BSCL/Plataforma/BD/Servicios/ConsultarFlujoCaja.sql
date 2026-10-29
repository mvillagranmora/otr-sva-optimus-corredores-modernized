SET SCHEMA "BSCL";
DROP PROCEDURE "BSCL.Plataforma.BD.Servicios::ConsultarFlujoCaja";

CREATE PROCEDURE
--  
--  Proposito: LLamar al flujo de caja desde la UI
--  Autor: NRM - BCS 
--  Fecha Creacion: 31/08/2016
-------------------------------------------
"BSCL.Plataforma.BD.Servicios::ConsultarFlujoCaja" (
	-- Parametros entrada comunes en todos los servicios publicos
    IN  p_IdEmpresa                     BIGINT,
    IN  p_IdUsuario                     BIGINT,
    IN  p_IdModulo                      NVARCHAR(3),
   	IN  p_UsaReglasNegocio	 		    NVARCHAR(1),	-- S/N se usa en caso de servicios anidados.

    -- Parametros entrada del servicio publico
	IN  p_FechaDesde  					NVARCHAR(10),
    IN  p_FechaHasta  					NVARCHAR(10),
	IN  p_Uso                      		NVARCHAR(40) 	DEFAULT null,	-- Uso del flujo de caja.
	IN  p_DesplegarEnUI					NVARCHAR(1) 	DEFAULT 'S', -- N: Muestra todos los items del flujo; S: Muestra solo los items para UI
	IN	p_AgruparPorOrigen				NVARCHAR(1) 	DEFAULT 'N', -- S: agrupa por origen items; N: agrupa por items
    IN  p_PortafolioAConsultar			TABLE("IdPortafolio"	BIGINT,			
    										  "IdMoneda"		NVARCHAR(3)),	-- Parámetro opcional


    -- Parametros salida del servicio publico	
    OUT p_out_FlujoCaja                 TABLE("NombreUso"					NVARCHAR(100),
    										  "FechaSaldo"					NVARCHAR(10), --Fecha liquidacion del saldo
    										  "IdPortafolio"				BIGINT,                                              
                                              "IdMoneda"					NVARCHAR(3), 
                                              "Saldo"						DECIMAL(26,8)),
                                              
	OUT p_out_FlujoCajaDetallado		TABLE("FechaSaldo"					NVARCHAR(10), --Fecha liquidacion del saldo
    										  "IdPortafolio"				BIGINT,
                                              "IdMoneda"					NVARCHAR(3),
                                              "IdUsoFlujoCaja"				BIGINT,
                                              "NombreUso"					NVARCHAR(100),
                                              "IdConfiguracionFlujoCaja"	BIGINT,
                                              "ItemCalculo"					NVARCHAR(100),
                                              "Monto"						DECIMAL(26,8)),
	
	-- Salidas para mensajes de advertencias y errores
	OUT p_out_InformacionContador  		INT,
	OUT p_out_InformacionDetalle   		TABLE ("Codigo" NVARCHAR(100),"Descripcion" NVARCHAR(5000)),
	OUT p_out_AdvertenciasContador      INT,
	OUT p_out_AdvertenciasDetalle       TABLE ("Codigo" nvarchar(100),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(5000)),	
	OUT p_out_ErroresContador           INT,
	OUT p_out_ErroresDetalle            TABLE ("Codigo" nvarchar(100),"Descripcion" nvarchar(5000))
) 
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
	--READS SQL DATA
AS
BEGIN
	
	--------------------
	--Manejo de Errores 
	--------------------	
	DECLARE SALIDA_CONTROLADA 			CONDITION;
	DECLARE SALIDA_CONTROLADA_NO_DATA 	CONDITION;
	DECLARE v_errorExt	 				NVARCHAR(40);
	DECLARE v_resultadoRegla			NVARCHAR(40);
	
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA	 
	BEGIN
		SELECT count(1) into p_out_ErroresContador FROM :p_out_ErroresDetalle;
		SELECT count(1) into p_out_AdvertenciasContador FROM :p_out_AdvertenciasDetalle;
	END;
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA_NO_DATA     
	BEGIN
		v_hash= SELECT 0 as "IdInterno",'DATA_NO_EXISTE' as "Codigo",'' as "CodigoDato1",'' as "ValorDato1",'' as "CodigoDato2",'' as "ValorDato2" FROM DUMMY;	
		CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionarioMasivo" (:v_hash,:v_erroroutmsg);		
		p_out_ErroresDetalle =  SELECT "Codigo","Descripcion" FROM :v_erroroutmsg;
		SELECT COUNT(1) INTO p_out_ErroresContador FROM :p_out_ErroresDetalle;
	END;
	DECLARE EXIT HANDLER FOR SQLEXCEPTION
	BEGIN
		IF ::SQL_ERROR_CODE > 10000 THEN
			--error pseudo-controlado pero desde un Sub-SP
			CALL "BSCL.Plataforma.BD.Servicios::ConsultarCodigoMensajeError" (::SQL_ERROR_CODE,v_errorExt);			
			p_out_ErroresDetalle = SELECT "Codigo", "Descripcion" FROM :p_out_ErroresDetalle UNION ALL  
								   SELECT v_errorExt,::SQL_ERROR_MESSAGE FROM dummy;				
			SELECT count(1) into p_out_ErroresContador FROM :p_out_ErroresDetalle;
		ELSE
			--Error interno no manejado  (El log ser? captado por la capa de XS control)		
			RESIGNAL;
		END IF;
	END;
	
	p_out_InformacionContador	:= 0;
	p_out_AdvertenciasContador	:= 0;
	p_out_ErroresContador		:= 0;
	p_out_InformacionDetalle	= SELECT '' as "Codigo",'' as "Descripcion" from dummy where 1=2;
	p_out_AdvertenciasDetalle	= SELECT '' as "Codigo",'' as "RequiereAutorizacion",'' as "Descripcion" FROM dummy WHERE 1=2;
	p_out_ErroresDetalle		= SELECT '' as "Codigo",'' as "Descripcion" FROM dummy WHERE 1=2;
	p_out_FlujoCaja 			= SELECT '' as "NombreUso",'0001-01-01' as "FechaSaldo",0 as "IdPortafolio",'' as "IdMoneda",0  as "Saldo" FROM DUMMY WHERE 1=2;
	p_out_FlujoCajaDetallado	= SELECT '0001-01-01' as "FechaSaldo",0 as "IdPortafolio",'' as "IdMoneda",0  as "IdUsoFlujoCaja",'' as "NombreUso",
										 0 as "IdConfiguracionFlujoCaja",'' as "ItemCalculo",0 as "Monto" FROM DUMMY WHERE 1=2;
	
	
	v_PortafolioAConsultar =
	 SELECT DISTINCT
	 		 CAST(ROW_NUMBER() over (order by "IdPortafolio","IdMoneda") as BIGINT) as "IdInterno"
	 		,"IdPortafolio"
	 		,"IdMoneda"
       FROM :p_PortafolioAConsultar;
	
	--------------------
	--Validaciones
	--------------------
	IF :p_UsaReglasNegocio = 'S' THEN
	BEGIN
		CALL "BSCL.Plataforma.BD.Reglas::ServicioCumpleRequisitosBasicos"(:p_IdEmpresa,:p_IdUsuario,:p_IdModulo,v_Errores,:v_resultadoRegla);
		IF :v_resultadoRegla = 'RECH' THEN
			 SIGNAL SALIDA_CONTROLADA;
		END IF;
		
		CALL "BSCL.Plataforma.BD.Reglas::ServicioCumpleRequisitoConsultarFlujoCaja"(
			 p_IdEmpresa					=> :p_IdEmpresa
	        ,p_Uso       					=> :p_Uso
			,p_FechaDesde					=> :p_FechaDesde
			,p_FechaHasta					=> :p_FechaHasta
			,p_PortafolioAConsultar	        => :v_PortafolioAConsultar
			,p_out_MovimientosRech			=> :v_RechazadosPorValidacion
			,p_out_ErroresDetalle			=> :v_ErroresDetalle
			,p_out_resultadoRegla			=> :v_resultadoRegla
		);
			
		IF :v_resultadoRegla = 'RECH' THEN
			p_out_ErroresDetalle = SELECT "Codigo","Descripcion" FROM :v_ErroresDetalle;
			SIGNAL SALIDA_CONTROLADA;
		ELSEIF :v_resultadoRegla = 'ADVE' THEN
		
			------------------------------------
			-- ELIMINAR REGISTROS RECHAZADOS
			------------------------------------
			v_PortafolioAConsultar = 
			 SELECT "IdInterno","IdPortafolio","IdMoneda"
			   FROM :v_PortafolioAConsultar 
			  WHERE "IdInterno" NOT IN (SELECT "IdInterno" FROM :v_RechazadosPorValidacion);
		
			
			p_out_AdvertenciasDetalle = SELECT "Codigo",'N' as "RequiereAutorizacion", "Descripcion" FROM :v_ErroresDetalle;
			SELECT count(1) into p_out_AdvertenciasContador FROM :p_out_AdvertenciasDetalle;
		END IF;
	END;
	END IF;
	
	--------------------
	--Negocio Aqui
	--------------------
	BEGIN
		
		------------------------------------
		-- OBTENER SALDO FLUJO CAJA
		------------------------------------
		v_PortafoliosAConsultar = 
		SELECT  POR."IdPortafolio"
		 		,MON."IdMoneda"
		FROM :p_PortafolioAConsultar as PC
		INNER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" as POR  ON POR."IdPortafolio" = PC."IdPortafolio"
		INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" as CLI  ON CLI."IdCliente" = POR."IdCliente"
		INNER JOIN "BSCL.Plataforma.BD::PLA.Moneda" as MON  ON (PC."IdMoneda" IS NULL OR MON."IdMoneda" = PC."IdMoneda")
		GROUP BY POR."IdPortafolio",MON."IdMoneda";
		--SELECT 'v_PortafoliosAConsultar_V1', * FROM :v_PortafoliosAConsultar;  -- REVISAR 
		
		-- Para cuenta inversión, debe filtra sólo las monedas que el cliente usa en cuenta inversión
		IF (:p_Uso = 'CUENTA_INVERSION') THEN
			v_PortafoliosAConsultar =
			SELECT  PC."IdPortafolio", 
					CTA."IdMoneda"		 		
			FROM :v_PortafoliosAConsultar AS PC
			INNER JOIN "BSCL.CuentaInversion.BD::CTA.CuentaInversion" AS CTA ON (CTA."IdPortafolio" = PC."IdPortafolio" AND CTA."IdMoneda" = PC."IdMoneda");
			--SELECT 'v_PortafoliosAConsultar_V2', * FROM :v_PortafoliosAConsultar;  -- REVISAR 
		END IF;
		
		 
		CALL "BSCL.Plataforma.BD.Servicios::SaldoFlujoCaja" 
		(
		     p_IdEmpresa				=>	:p_IdEmpresa
			,p_Uso						=>	:p_Uso
			,p_DesplegarEnUI			=>  :p_DesplegarEnUI
			,p_AgruparPorOrigen			=>  :p_AgruparPorOrigen
			,p_FechaDesde				=>	:p_FechaDesde
		    ,p_FechaHasta				=>	:p_FechaHasta
			,p_PortafolioAConsultar		=>	:v_PortafoliosAConsultar
		    ,p_out_FlujoCaja			=> 	p_out_FlujoCaja
		    ,p_out_FlujoCajaDetallado	=>	p_out_FlujoCajaDetallado
			,p_out_ErroresContador		=>	p_out_ErroresContador
			,p_out_ErroresDetalle		=>	p_out_ErroresDetalle
		);
		
	END;
END;