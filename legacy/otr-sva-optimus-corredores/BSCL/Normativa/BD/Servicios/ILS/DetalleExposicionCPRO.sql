SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Normativa.BD.Servicios::ILS.DetalleExposicionCPRO";

CREATE PROCEDURE "BSCL.Normativa.BD.Servicios::ILS.DetalleExposicionCPRO" (
	-- Parametros entrada comunes
	IN p_IdEmpresa			BIGINT,			
	IN p_IdUsuario			BIGINT,			
	IN p_IdModulo			NVARCHAR(3),  
	IN p_UsaReglasNegocio	NVARCHAR(1),	
		  
	-- Parametros entrada
	IN p_IdTipoInforme		BIGINT,
	IN p_FechaContable		NVARCHAR(10),
	OUT p_tablaResultadoExposicionCPRO TABLE(
												"Nemotecnico" NVARCHAR(1), 
												"DelDia" NVARCHAR(1), 
												"EnGarantia" NVARCHAR(1), 
												"Contable" NVARCHAR(1), 
												"PorCompromisos" NVARCHAR(1), 
												"TotalTenencia" NVARCHAR(1), 
												"DeudasValores" NVARCHAR(1), 
												"Precio" NVARCHAR(1), 
												"FactorAseguramiento" NVARCHAR(1), 
												"ContableValorizada" NVARCHAR(1), 
												"PorCompromisosValorizada" NVARCHAR(1), 
												"DeudasValoresValorizada" NVARCHAR(1), 
												"TotalValorizado" NVARCHAR(1), 
												"ValoresEnCirculacion" NVARCHAR(1), 
												"PorcentajeTenencia" NVARCHAR(1), 
												"FactorPonderacion" NVARCHAR(1), 
												"TenenciaPonderada" NVARCHAR(1)
									 		),    
	-- Salidas para mensajes de advertencias y errores
	OUT p_out_AdvertenciasContador	INT,
	OUT p_out_AdvertenciasDetalle	TABLE ("Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(5000))DEFAULT EMPTY,	
	OUT p_out_ErroresContador		INT,
	OUT p_out_ErroresDetalle		TABLE ("Codigo" nvarchar(40),"Descripcion" nvarchar(5000))DEFAULT EMPTY
) 
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER 
DEFAULT SCHEMA "BSCL"
AS
BEGIN

	--------------------
	--DECLARE Manejo de Errores
	--------------------	
	DECLARE SALIDA_CONTROLADA CONDITION; 
	DECLARE v_errorcuenta INT DEFAULT 0;  
	DECLARE v_errorhashmsg table ("Codigo" nvarchar(40),"Valor" nvarchar(520));
	DECLARE v_erroroutmsg  nvarchar(520);	        	
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA	 
	BEGIN
		--salida/error/advertencia controlado por este servicio
		select count(1) into p_out_ErroresContador 
		from :p_out_ErroresDetalle;
		select count(1) into p_out_AdvertenciasContador 
		from :p_out_AdvertenciasDetalle;		 					 	
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
			--Error interno no manejado  (El log será captado por la capa de XS control)		
			RESIGNAL; 
		END IF;		
	END;	
	p_out_AdvertenciasContador	:= 0;
	p_out_ErroresContador		:= 0;
	p_out_AdvertenciasDetalle	= select '' as "Codigo",'' as "RequiereAutorizacion",'' as "Descripcion" from dummy where 1=2;
	p_out_ErroresDetalle		= select '' as "Codigo",'' as "Descripcion" from dummy where 1=2;
	
	BEGIN
		--------------------
		--Declare Variables
		--------------------
	    DECLARE v_out_AdvertenciasContador, v_out_ErroresContador INT;
	    DECLARE v_out_ErroresDetalle		TABLE ("Codigo" nvarchar(40),"Descripcion" nvarchar(520));
        DECLARE v_out_AdvertenciasDetalle	TABLE ("Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(520));	
        

		
		--------------------
		--Validaciones
		--------------------
		
		
		--------------------
		--Reglas Negocio
		--------------------
		
		--------------------
		--Lógica de Negocio 
		--------------------
				
		BEGIN
			
			p_tablaResultadoExposicionCPRO  = 
						SELECT '' as "Nemotecnico", 
						       '' as "DelDia", 
						       '' as "EnGarantia", 
						       '' as "Contable",
						       '' as "PorCompromisos",
						       '' as "TotalTenencia",
						       '' as "DeudasValores",
						       '' as "Precio",
						       '' as "FactorAseguramiento",
						       '' as "ContableValorizada",
						       '' as "PorCompromisosValorizada",
						       '' as "DeudasValoresValorizada",
						       '' as "TotalValorizado",
						       '' as "ValoresEnCirculacion",
						       '' as "PorcentajeTenencia",
						       '' as "FactorPonderacion",
						       '' as "TenenciaPonderada" FROM DUMMY;
					
		END;		
	END;	
END;
