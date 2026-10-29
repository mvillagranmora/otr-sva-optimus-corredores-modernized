
SET SCHEMA "BSCL"; 
DROP PROCEDURE "BSCL.Mesa.BD.Servicios::SPT.ModificarCodigoComercio";

CREATE PROCEDURE "BSCL.Mesa.BD.Servicios::SPT.ModificarCodigoComercio"
  ( 
     IN p_IdEmpresa             		BIGINT,
     IN p_IdUsuario             		BIGINT,
     IN p_IdModulo                      NVARCHAR(3),
     IN p_UsaReglasNegocio	            NVARCHAR(1),
     IN p_DatosOperacion     			TABLE (   
     											  "NumeroOperacion"               NVARCHAR(64)     
												, "CodigoComercio"                BIGINT        
												),
     -- Salida Id Generado
    --OUT p_out_IdOperacionSpot 			    TABLE ("IdInterno" NVARCHAR(40),
	--											    "IdOperacionSpot" BIGINT,
    --												"IdDatatec" NVARCHAR(40),
    -- 												"Estado" NVARCHAR(3)
    --												),
     -- Salidas para mensajes de advertencias y errores
	OUT p_out_AdvertenciasContador			INT,
	OUT p_out_AdvertenciasDetalle			TABLE ("IdInterno"	NVARCHAR(40), "IdDatatec" NVARCHAR(40), "Codigo" NVARCHAR(100),"RequiereAutorizacion" NVARCHAR(40),"Descripcion" NVARCHAR(520)),	
	OUT p_out_ErroresContador 				INT,
	OUT p_out_ErroresDetalle 				TABLE ("IdInterno"	NVARCHAR(40), "IdDatatec" NVARCHAR(40), "Codigo" NVARCHAR(100),"Descripcion" NVARCHAR(520))
   )
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER  
	DEFAULT SCHEMA "BSCL"
	--READS SQL DATA   
AS
 BEGIN
	DECLARE varMonedaLocal NVARCHAR(3);
	DECLARE varIdClienteCtaPropia BIGINT;
	DECLARE varMonedaEquivalente NVARCHAR(3) = 'USD';
	DECLARE varPuntaBidPrueba DECIMAL(26,8) = 615.12; --Borrar
	DECLARE varPuntaAskPrueba DECIMAL(26,8) = 618.45; --Borrar
	DECLARE varParidadBidPrueba DECIMAL(26,8) = 1.32; --Borrar
	DECLARE varParidadAskPrueba DECIMAL(26,8) = 1.42; --Borrar
	DECLARE varValorDolarObservado DECIMAL(26,8) = 598; --Borrar
	DECLARE varValorTCSpot DECIMAL(26,8) = 600; --Borrar
	DECLARE varParidadMercado DECIMAL(26,8) = 1.12; --Mant paridades Borrar
	DECLARE varValorTCCierre DECIMAL(26,8) = 0;
	--------------------
	--DECLARE Manejo de Errores
	--------------------	
	DECLARE SALIDA_CONTROLADA CONDITION; 
	DECLARE v_errorhashmsg table ("Codigo" nvarchar(100),"Valor" nvarchar(520));
	DECLARE v_erroroutmsg  nvarchar(520);	        	
	DECLARE v_out_AdvertenciasDetalle  TABLE ("IdInterno" NVARCHAR(40), "IdDatatec" NVARCHAR(40), "Codigo" nvarchar(100),"RequiereAutorizacion" NVARCHAR(1),"ValorParametro" nvarchar(100),"NombreParametro" NVARCHAR(40))
								DEFAULT SELECT 0  AS "IdInterno" 
                                    , ''  AS "IdDatatec"
                                    , ''  AS "Codigo"
                                    , ''  AS "RequiereAutorizacion"
                                    , ''  AS "ValorParametro" 
                                    , ''  AS "NombreParametro" 
                               FROM dummy 
                               WHERE 1=2;
	DECLARE v_out_ErroresDetalle  		TABLE ("IdInterno" NVARCHAR(40), "IdDatatec" NVARCHAR(40), "Codigo" nvarchar(100),"ValorParametro" nvarchar(100),"NombreParametro" NVARCHAR(40))
							DEFAULT SELECT 0  AS "IdInterno" 
                                    , ''  AS "IdDatatec"
                                    , ''  AS "Codigo"
                                    , ''  AS "ValorParametro" 
                                    , ''  AS "NombreParametro" 
                               FROM dummy 
                               WHERE 1=2;
                               
	DECLARE v_count_operaciones				BIGINT;
	DECLARE v_count_pendientes				BIGINT;
	DECLARE v_count_validos  				BIGINT;
	
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA	 
	BEGIN
		--salida/error/advertencia controlado por este servicio
		select count(1) into p_out_ErroresContador 
		from :v_out_ErroresDetalle;
		select count(1) into p_out_AdvertenciasContador 
		from :v_out_AdvertenciasDetalle; 	
		
		v_TablaCodigosMensajes = SELECT "Codigo" AS "CodigoMensaje",
								"NombreParametro" as "CodigoDato",
								"ValorParametro" as "ValorDato" 
								FROM :v_out_ErroresDetalle
								UNION ALL
								SELECT "Codigo" AS "CodigoMensaje",
								"NombreParametro" as "CodigoDato",
								"ValorParametro" as "ValorDato" 
								FROM :v_out_AdvertenciasDetalle;
		
		CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionario" (:v_TablaCodigosMensajes,v_out_ErroresDetalleMensajes);
		
		p_out_ErroresDetalle = 	SELECT errores."IdInterno",
							 		   errores."IdDatatec",
									errores."Codigo",
									--errores."Codigo"||' - '||"ValorParametro" as "Codigo",
									msj."Descripcion"
								FROM :v_out_ErroresDetalle errores
								INNER JOIN :v_out_ErroresDetalleMensajes msj on errores."Codigo" = msj."Codigo";
		
		p_out_AdvertenciasDetalle = SELECT adv."IdInterno",
							 		adv."IdDatatec",
									adv."Codigo",
									--adv."Codigo"||' - '||"ValorParametro" as "Codigo",
									adv."RequiereAutorizacion",
									msj."Descripcion"
								FROM :v_out_AdvertenciasDetalle adv
								INNER JOIN :v_out_ErroresDetalleMensajes msj on adv."Codigo" = msj."Codigo";
				 						 	
	END;
	
	p_out_AdvertenciasContador	:= 0;
	p_out_ErroresContador		:= 0;
	v_out_AdvertenciasDetalle = SELECT 0 as "IdInterno" , '' as "IdDatatec", '' as "Codigo",'N' as "RequiereAutorizacion",'' as "ValorParametro",'' as "NombreParametro" from dummy where 1=2;
	v_out_ErroresDetalle = SELECT 0 as "IdInterno" , '' as "IdDatatec", '' as "Codigo",'' as "ValorParametro",'' as "NombreParametro" from dummy where 1=2;
	
	BEGIN
   	--Guarda configuracion de sistema en variables
	    UPDATE Datos_Originales
	    SET 
			 Datos_Originales."CodigoComercio"              = Datos_Nuevos."CodigoComercio"         
	   	FROM "BSCL.Mesa.BD::SPT.OperacionSpot" Datos_Originales
	   	INNER JOIN :p_DatosOperacion Datos_Nuevos
	   	ON Datos_Nuevos."NumeroOperacion" = Datos_Originales."IdOperacionSpot";
	   	
	
    END;
 END;