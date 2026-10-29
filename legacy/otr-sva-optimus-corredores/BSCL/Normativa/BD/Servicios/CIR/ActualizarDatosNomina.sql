SET SCHEMA "BSCL";

DROP PROCEDURE  "BSCL.Normativa.BD.Servicios::CIR.ActualizarDatosNomina";

CREATE PROCEDURE
-- 
--  Proposito: Servicio que obtiene y evalua informacion de parametros para la Circularizacion, 
--  para ser creado como una solicitud de Cambio.
--  Autor: CFLORES - BCS
--  Fecha Creacion: 27-10-2016
--  
"BSCL.Normativa.BD.Servicios::CIR.ActualizarDatosNomina" (
	-- Parametros entrada comunes en todos los servicios publicos
	IN  p_IdEmpresa									BIGINT,			--ID Empresa destino
	IN  p_IdUsuario									BIGINT,			--ID del usuario que solicita el servicio
	IN  p_IdModulo									NVARCHAR(3),  	--ID del Modulo que lo invoca
	IN  p_UsaReglasNegocio	 						NVARCHAR(1),	-- S/N se usa en caso de servicios anidados.
		  
	-- Parametros entrada del servicio publico
    IN  p_TablaNomina		  		TABLE("IdNomina"							BIGINT),
	
	
	-- Parametros salida del servicio publico
--	OUT p_out_IdSecuencias table ("IdSecuencia" BIGINT)
	OUT p_out_RefId 					BIGINT,	
	-- Salidas para mensajes de advertencias y errores
	OUT p_out_AdvertenciasContador 		int,
	OUT p_out_AdvertenciasDetalle 		table ("Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(5000)),	
	OUT p_out_ErroresContador 			int,
	OUT p_out_ErroresDetalle 			table ("Codigo" nvarchar(40),"Descripcion" nvarchar(5000))
	
) 
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER 
DEFAULT SCHEMA "BSCL"
AS
BEGIN
	--------------------
	--DECLARE Manejo de Errores
	--------------------	
	DECLARE SALIDA_CONTROLADA 		CONDITION; 
	DECLARE v_errorcuenta 			INT DEFAULT 0;  
	DECLARE v_errorhashmsg 			table ("Codigo" nvarchar(40),"Valor" nvarchar(520));
	DECLARE v_erroroutmsg  			nvarchar(520);	        	
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA


	BEGIN
		--salida/error/advertencia controlado por este servicio
	p_out_AdvertenciasContador	:= 0;
	p_out_ErroresContador		:= 0;
	p_out_AdvertenciasDetalle	= select '' as "Codigo",'' as "RequiereAutorizacion",'' as "Descripcion" from dummy where 1=2;
	p_out_ErroresDetalle		= select '' as "Codigo",'' as "Descripcion" from dummy where 1=2;
	
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

	
	BEGIN	
		--------------------
		--Declare Variables
		--------------------


			DECLARE v_count			   					    BIGINT:=0;

		    DECLARE v_out_AdvertenciasContador, v_out_ErroresContador INT;
		    DECLARE v_out_ErroresDetalle           table ("Codigo" nvarchar(40),"Descripcion" nvarchar(520));
	        DECLARE v_out_AdvertenciasDetalle      table ("Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(520));	
       
			DECLARE v_fechaHoraSistema NVARCHAR(40):='';

			--------------------
		--Validaciones
		--------------------
		
		-- Existencia 		
		
		
		--------------------
		--Reglas Negocio
		--------------------		
		
		--------------------
		--Lógica Negocio
		--------------------
		BEGIN
			--Inicializacion de variables:
			
				
			UPDATE N
			SET  N."FormaEnvio"=IND."CodigoValorIndicador",
            	 N."FechaModificacion"=CURRENT_UTCTIMESTAMP
            FROM "BSCL.Normativa.BD::CIR.Nomina" N	  
            INNER JOIN :p_TablaNomina P ON (N."IdNomina"=P."IdNomina")
        	INNER JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPortafolio" IND  --ENVIO  DE CIRCULARIZACION
			ON (IND."IdUso"='IND' AND IND."CodigoTipoIndicador"='CIRCENVIO' 
				AND IND."IdCliente" =N."IdCliente" AND N."IdPortafolio"=IND."IdPortafolio");
            p_out_RefId:=1;


	END;
					
			
		--------------------
		--FIN SP 
		--------------------
	END;	
END;
