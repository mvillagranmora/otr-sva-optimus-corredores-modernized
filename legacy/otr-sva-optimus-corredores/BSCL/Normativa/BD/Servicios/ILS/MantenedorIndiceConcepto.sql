SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Normativa.BD.Servicios::ILS.CrearIndiceConcepto";

CREATE PROCEDURE "BSCL.Normativa.BD.Servicios::ILS.CrearIndiceConcepto" (
	-- Parametros entrada comunes
	IN p_IdEmpresa			BIGINT,			--ID Empresa destino
	IN p_IdUsuario			BIGINT,			--ID del usuario que solicita el servicio
	IN p_IdModulo			NVARCHAR(3),  	--ID del Modulo que lo invoca
	IN p_UsaReglasNegocio	NVARCHAR(1),	--S/N se usa en caso de servicios anidados.
		  
	-- Parametros entrada
	IN p_tablaIndiceConcepto TABLE(
										 "IdIndice" BIGINT,
										 "IdConcepto" BIGINT,
										 "Posicion" NVARCHAR(1),
										 "Factor" DECIMAL(4,2),
										 "Operacion" NVARCHAR(1) ,
										 "Estado" NVARCHAR(3)
									 ),
    
	-- Salidas para mensajes de advertencias y errores
    OUT p_out_IdIndiceConcepto TABLE (
    								     "IdIndice" BIGINT,
    								     "IdConcepto" BIGINT,
									     "Estado" NVARCHAR(3)
									 ),
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
        

		--**Crear tabla de paso
		   	v_tblIndiceConcepto_ = 
		   	SELECT '0' as "IdIndice",
					 "IdConcepto",
					 "IdEmpresa",
					 "Posicion",
					 "Factor",
					 "Operacion",
					 "Estado"
		    FROM "BSCL.Normativa.BD::ILS.IndiceConcepto" WHERE 1=2;
	
		
	     	v_tblIndiceConcepto_ = 
			SELECT     "IdIndice" 
			         , "IdConcepto" 
					 , p_IdEmpresa    as   "IdEmpresa"
					 ,"Posicion"
					 ,"Factor"
					 ,"Operacion"
					 ,"Estado"
			FROM :p_tablaIndiceConcepto; 
	
		
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
    	
			INSERT INTO "BSCL.Normativa.BD::ILS.IndiceConcepto"
			(
				  "IdIndice" 
				 ,"IdConcepto" 
				 ,"IdEmpresa" 	 
				 ,"Posicion"
				 ,"Factor"
				 ,"Operacion"
				 ,"Estado" 		
			)
			SELECT
			  "IdIndice"
			 ,"IdConcepto" 
			 ,"IdEmpresa" 	 
			 ,"Posicion"
			 ,"Factor"
			 ,"Operacion"
			 ,"Estado"   
			FROM :v_tblIndiceConcepto_;
			
			
		   --DEVUELVE MOVIMIENTOS AGREGADOS
			p_out_IdIndiceConcepto = 
			SELECT 
			       "BSCL.Normativa.BD::ILS.IndiceConcepto"."IdIndice",  
			       "BSCL.Normativa.BD::ILS.IndiceConcepto"."IdConcepto",  
			       "BSCL.Normativa.BD::ILS.IndiceConcepto"."Estado"    
			FROM :v_tblIndiceConcepto_ IndiceConcepto
			INNER JOIN  "BSCL.Normativa.BD::ILS.IndiceConcepto" 
			ON IndiceConcepto."IdIndice"  = "BSCL.Normativa.BD::ILS.IndiceConcepto"."IdIndice"
			AND IndiceConcepto."IdConcepto"  = "BSCL.Normativa.BD::ILS.IndiceConcepto"."IdConcepto";
			  			
			
		END;		
	END;	
END;
