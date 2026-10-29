SET SCHEMA "BSCL";
GO
DROP PROCEDURE "BSCL.Plataforma.BD.Servicios::CrearExigibilidad";
GO
CREATE PROCEDURE
-- 
--  Proposito: crea el servicio de exigibilidad
--  Autor: - EAR
--  Fecha Creacion: 20/07/2015
--  el servicio permite la insercion en la tabla de exigibilidad de garantia desde el mantenedor 
-- de exigibilidad de garantia de los porcentaje de exigibilidad para los tipos instrumentos

"BSCL.Plataforma.BD.Servicios::CrearExigibilidad" (
    --VARIABLES OBLIGATORIAS
    IN p_IdEmpresa          	          	BIGINT,      -- identificador de la empresa de la orden 
    IN p_IdUsuario	                        BIGINT,      -- usuario que ingresa la orden 
    IN p_IdModulo                           NVARCHAR(3), -- modulo de la orden 
    IN p_simularOperacion					NVARCHAR(1), -- S/N se usa para simular operacion (algunos casos)
	IN p_UsaReglasNegocio	 				NVARCHAR(1), -- S/N se usa en caso de servicios anidados.
    IN p_TableExigibilidad			     	TABLE("IdTipoInstrumento" NVARCHAR(3), "IdBolsa" NVARCHAR(3), "IdTipoInstrumentoSubyacente" NVARCHAR(3),"IdTipoClasificacionRiesgo" NVARCHAR(4), "Nemotecnico" NVARCHAR(40),"ExigibilidadEntidad" DECIMAL(5,2),"ExigibilidadCorredora" DECIMAL(5,2), "IdExigibilidadGarantia" BIGINT,"IdInstrumento" BIGINT,"IdEntidadClasificadoraRiesgo" NVARCHAR(10),"ClasificacionRiesgo" NVARCHAR(20)),
    --OUT p_out_REF_ID                      TABLE("IdExigibilidad" BIGINT),
	-- Salidas para mensajes de advertencias y errores
	OUT p_out_AdvertenciasContador			INT,
	OUT p_out_AdvertenciasDetalle			TABLE ("Codigo" NVARCHAR(100),"RequiereAutorizacion" NVARCHAR(40),"Descripcion" NVARCHAR(520)),	
	OUT p_out_ErroresContador 				INT,
	OUT p_out_ErroresDetalle 				TABLE ("Codigo" NVARCHAR(100),"Descripcion" NVARCHAR(520))
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
	-------------------------
    -- VARIABLES DEL SISTEMA 
    -------------------------

    DECLARE v_resultadoRegla NVARCHAR(3); 
    	        	
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
		-----------------------------------------------------
		--Declaraciones Variables
		-----------------------------------------------------
       	
		--------------------
		--Negocio Aqui
		--------------------
        -- primero actualizo la exigibilidad de los registros que existen en la exigibilidad
        update ExigibilidadGarantia set ExigibilidadGarantia."ExigibilidadEntidad"      = tablaExigibilidadUsuario."ExigibilidadEntidad"
                                        ,ExigibilidadGarantia."ExigibilidadCorredora"   = tablaExigibilidadUsuario."ExigibilidadCorredora"	
        from "BSCL.Plataforma.BD::GTA.ExigibilidadGarantia" AS ExigibilidadGarantia
        inner join :p_TableExigibilidad as tablaExigibilidadUsuario 
        on tablaExigibilidadUsuario."IdExigibilidadGarantia" = ExigibilidadGarantia."IdExigibilidadGarantia";
        
        -- inserta aquellos registros que no esten en la tabla de exigibilidad de garantia
       insert into "BSCL.Plataforma.BD::GTA.ExigibilidadGarantia" (
                                                                 "IdExigibilidadGarantia"
                                                                ,"IdBolsa"
                                                                ,"IdTipoInstrumento"
                                                                ,"IdTipoInstrumentoSubyacente"
                                                                ,"Nemo"
                                                                ,"IdTipoClasificacionRiesgo"
                                                                ,"PlazoTope"
                                                                ,"UnidadPlazo"
                                                                ,"ExigibilidadEntidad"
                                                                ,"ExigibilidadCorredora"
                                                                ,"IdEstado"
                                                                ,"IdInstrumento"
                                                                ,"IdEntidadClasificadoraRiesgo"
                                                                ,"ClasificacionRiesgo")
		select "BSCL.Plataforma.BD.Secuencias::ExigibilidadGarantia".NEXTVAL as "IdExigibilidadGarantia"
		      ,"IdBolsa"
		      ,"IdTipoInstrumento"
		      ,"IdTipoInstrumentoSubyacente"
		      ,COALESCE("Nemotecnico", '')
		      ,"IdTipoClasificacionRiesgo" 
			  ,0 as "PlazoTope"--no se ha especificado este valor 
              ,'' as "UnidadPlazo"--no se ha especificado este valor
              ,"ExigibilidadEntidad"
		      ,"ExigibilidadCorredora"
		      ,'ACT' as "IdEstado"
		      ,"IdInstrumento"
		      ,"IdEntidadClasificadoraRiesgo"
		      ,"ClasificacionRiesgo"
		from :p_TableExigibilidad 
		where "IdExigibilidadGarantia" = 0
		and ("ExigibilidadEntidad" > 0 or "ExigibilidadCorredora" > 0) ;
		--------------------
		--FIN SP 
		--------------------
	END;	
END; 

GO