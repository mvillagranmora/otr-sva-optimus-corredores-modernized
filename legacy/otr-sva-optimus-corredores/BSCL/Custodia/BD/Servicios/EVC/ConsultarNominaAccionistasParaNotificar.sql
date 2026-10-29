SET SCHEMA "BSCL"; 
DROP PROCEDURE "BSCL.Custodia.BD.Servicios::EVC.ConsultarNominaAccionistasParaNotificar";
CREATE PROCEDURE "BSCL.Custodia.BD.Servicios::EVC.ConsultarNominaAccionistasParaNotificar" (

    IN  p_IdEmpresa							BIGINT,			
	IN  p_IdUsuario							BIGINT,			
	IN  p_IdModulo							NVARCHAR(3),  	
    IN  p_IdEventoJuntaAccionista           BIGINT,
	OUT p_out_IdEvento         			 	TABLE(
													"IdEvento"        BIGINT
												  ),
	OUT p_out_AdvertenciasContador 			int,
	OUT p_out_AdvertenciasDetalle 			table ("Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(5000)),	
	OUT p_out_ErroresContador 				int,
	OUT p_out_ErroresDetalle 				table ("Codigo" nvarchar(40),"Descripcion" nvarchar(5000))
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
			call "BSCL.Plataforma.BD.Servicios::ConsultarCodigoMensajeError" (::SQL_ERROR_CODE,v_errorExt);			
			p_out_ErroresDetalle = select * from :p_out_ErroresDetalle UNION ALL  
								   select v_errorExt,::SQL_ERROR_MESSAGE from dummy;				
			select count(1) into p_out_ErroresContador 
			from :p_out_ErroresDetalle;										
		ELSE	
			RESIGNAL; 
		END IF;		
	END;	

	
	BEGIN	
		--------------------
		--Declare Variables
		--------------------
		DECLARE v_out_Agregar_Evento TABLE("IdEvento" BIGINT);
		
	
		BEGIN 
		
			v_NominasANotificar = SELECT  "IdClienteNominaJuntaAccionista" AS "IdClienteNominaJuntaAccionista"
			                             ,"IdPersona" AS "IdPersona"
										 ,"IdDireccionElectronicaPrincipal" AS "IdDireccionElectronica"
										 ,"NombreCliente" AS "NombreCliente"
										 ,"CiudadCliente" AS "CiudadCliente"
										 ,"Direccion" AS "EnvioCorrespondencia"
										 ,"FechaRealizacion" AS "FechaRealizacion"
										 ,"HoraRealizacion" AS "HoraRealizacion"
										 ,"Materias" AS "Materias"
										 ,"FechaCierre" AS "FechaCierre"
										 ,"Lugar" AS "Lugar"
								  FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultaClienteNominaJuntaAccionista" 
								  WHERE "IdEventoJuntaAccionista" = :p_IdEventoJuntaAccionista;
		
			v_out_Agregar_Evento = SELECT 0 AS "IdEvento" FROM DUMMY WHERE 1=2;	
					
 			CALL "BSCL.Custodia.BD.Servicios::EVC.NotificarJuntaAccionistas"(
 					p_IdEmpresa => p_IdEmpresa,
 					p_IdUsuario => p_IdUsuario,
 					p_IdModulo => p_IdModulo,
 					p_TablaNotificacion => :v_NominasANotificar,
 					p_out_IdEvento => :v_out_Agregar_Evento,
 					p_out_AdvertenciasContador => :p_out_AdvertenciasContador,
 					p_out_AdvertenciasDetalle => :p_out_AdvertenciasDetalle,
 					p_out_ErroresContador => :p_out_ErroresContador,
 					p_out_ErroresDetalle => :p_out_ErroresDetalle
 				);
	 		
	 		UPDATE "BSCL.Custodia.BD::CUS.ClienteNominaJuntaAccionista"
	 		SET    "IdEstadoNotificacion" = 'OK'
	 		WHERE  "IdEventoJuntaAccionista" = :p_IdEventoJuntaAccionista;
	 		
	 		p_out_IdEvento=SELECT "IdEvento"  FROM :v_out_Agregar_Evento;	
	 		
			
		END;
			
			
		--------------------
		--FIN SP 
		--------------------
	END;	
END;
