SET SCHEMA "BSCL";

DROP PROCEDURE  "BSCL.Normativa.BD.Servicios::CIR1720.EditarDatos";


CREATE PROCEDURE 
--
--  Proposito: Servicio Invocado desde el JOB para cargar datos de la circular, para la edicion.
--  Autor: CFLORES - BCS
--  Fecha Creacion: 07/04/2017

"BSCL.Normativa.BD.Servicios::CIR1720.EditarDatos" ( 

    --VARIABLES DEL PROCESO
	IN p_IdEmpresa					  			BIGINT,	
	IN p_IdCuadratura							BIGINT,
	IN p_CantidadUnidadesNominal				DECIMAL(26,8),
	IN p_Valorizacion							DECIMAL(26,8),
	IN p_CarteraPropia							DECIMAL(26,8),	
	IN p_CustodiaTerceros						DECIMAL(26,8),	
	IN p_CustodiaPropiaDCV						DECIMAL(26,8),
	IN p_CustodiaTercerosDCV 					DECIMAL(26,8),
	IN p_CustodiaSociedadEmisora 				DECIMAL(26,8),
	IN p_CustodiaIntermediario 					DECIMAL(26,8),
	IN p_CustodiaBolsas	 						DECIMAL(26,8), 
	IN p_CustodiaOtros	 						DECIMAL(26,8), 
	IN p_TipoInstrumento						NVARCHAR(10),
	IN p_Habilitado								NVARCHAR(1) DEFAULT 'S',
	IN p_IdUsuario   							BIGINT 	DEFAULT 0,
-- Salidas para mensajes de advertencias y errores
	OUT p_out_AdvertenciasContador 		int,
	OUT p_out_AdvertenciasDetalle 		table ("Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(5000)),	
	OUT p_out_ErroresContador 			int,
	OUT p_out_ErroresDetalle 			table ("Codigo" nvarchar(100),"Descripcion" nvarchar(5000))
	
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
	DECLARE v_errorhashmsg 			table ("Codigo" nvarchar(40),"Valor" nvarchar(5000));
	DECLARE v_erroroutmsg  			nvarchar(5000);	        	
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
	
		----------------------------------------
		--Declare Variables

  		DECLARE v_fechaHoraSistema		 NVARCHAR(40):='';
		DECLARE v_Custodias				 DECIMAL(26,8):=0;
		DECLARE v_Count 				 BIGINT :=0;
		---Parametros


		
		--------------------------------------------------------
		-- Identificar fecha de trabajo
		--------------------------------------------------------

		call "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema"(
				p_IdEmpresa => p_IdEmpresa,
				p_out_FechaHoraSistema => v_fechaHoraSistema
		);
			
		
		v_Custodias = :p_CustodiaPropiaDCV + :p_CustodiaTercerosDCV + :p_CustodiaSociedadEmisora + :p_CustodiaIntermediario + :p_CustodiaBolsas + :p_CustodiaOtros;
	
		IF :p_CantidadUnidadesNominal != :p_CarteraPropia + :p_CustodiaTerceros THEN 
			 p_out_ErroresDetalle = SELECT  'UI_CANT_NOMINAL_DIFERENTE_CART_PROPIA_Y_CUST_TERCERO' AS "Codigo", '' AS "Descripcion" FROM DUMMY;
           	 SIGNAL SALIDA_CONTROLADA;
		END IF;		
		
		IF :p_CantidadUnidadesNominal != v_Custodias THEN 
		 	p_out_ErroresDetalle = SELECT  'UI_CANT_NOMINAL_DIFERENTE_SUMATORIA_CUSTODIAS' AS "Codigo", '' AS "Descripcion" FROM DUMMY;
		    SIGNAL SALIDA_CONTROLADA;
		END IF;
		
		IF :p_Valorizacion = 0 THEN
			 p_out_ErroresDetalle = SELECT  'UI_MONTO_VALORIZACION_DEBE_SER_MAYOR_0' AS "Codigo", '' AS "Descripcion" FROM DUMMY;
				SIGNAL SALIDA_CONTROLADA;	
		END IF;
	
		IF length(:p_TipoInstrumento) < 1 THEN 
			p_out_ErroresDetalle = SELECT  'UI_TIPO_INSTRUMENTO_DEBE_SER_MAYOR_1' AS "Codigo", '' AS "Descripcion" FROM DUMMY;
				SIGNAL SALIDA_CONTROLADA;	
		END IF;
		
		SELECT COUNT(1) INTO v_Count FROM "BSCL.Normativa.BD::NOR.DatosCIR1720"	WHERE "IdCuadratura"=p_IdCuadratura;
		
		IF v_Count>0 THEN	--Existe
			
			UPDATE "BSCL.Normativa.BD::NOR.DatosCIR1720"
			SET 
			"CantidadUnidadesNominal"=:p_CantidadUnidadesNominal,
			"Valorizacion"=:p_Valorizacion,
			"CarteraPropia"=:p_CarteraPropia,	
			"CustodiaTerceros"=:p_CustodiaTerceros,
			"CustodiaPropiaDCV"=:p_CustodiaPropiaDCV,
			"CustodiaTercerosDCV"=:p_CustodiaTercerosDCV,
			"CustodiaSociedadEmisora"=:p_CustodiaSociedadEmisora,
			"CustodiaIntermediario"=:p_CustodiaIntermediario,
			"CustodiaBolsas"=:p_CustodiaBolsas,
			"CustodiaOtros"=:p_CustodiaOtros,
			"TipoInstrumento"=:p_TipoInstrumento,
			"Habilitado" = :p_Habilitado,
			--Auditoria
			"FechaModificacion"= CURRENT_UTCTIMESTAMP,
			"IdUsuarioModificacion" =:p_IdUsuario
			WHERE "IdCuadratura"=p_IdCuadratura;	
							
		ELSE
		
		
		
			INSERT INTO "BSCL.Normativa.BD::NOR.DatosCIR1720"
			("IdCuadratura",
			"FechaCuadraturaCustodia",
			"Nemotecnico",
			"NombreEmisor",
			"TipoInstrumento",
			"CantidadUnidadesNominal",
			"TipoUnidades",
			"Valorizacion",
			"CarteraPropia",
			"CustodiaTerceros",
			"CustodiaPropiaDCV",
			"CustodiaTercerosDCV",
			"CustodiaSociedadEmisora",
			"CustodiaIntermediario",
			"CustodiaBolsas",
			"CustodiaOtros",
			"FechaRegistro",
			"Habilitado",
			--Auditoria
			"FechaIngreso",
			"FechaCreacion",
			"FechaModificacion",
			"IdUsuarioCreador",
			"IdUsuarioModificacion")
			SELECT 
				OPE."IdCuadratura",
				OPE."FechaCuadraturaCustodia",
				OPE."Nemotecnico",
				COALESCE(INS."RazonSocial", TO_VARCHAR('')) AS "NombreEmisor",
				:p_TipoInstrumento AS "TipoInstrumento",
				:p_CantidadUnidadesNominal AS "CantidadUnidadesNominal",
				TO_VARCHAR('') AS "TipoUnidades",
				:p_Valorizacion AS "Valorizacion",
				:p_CarteraPropia AS "CarteraPropia",
				:p_CustodiaTerceros AS  "CustodiaTerceros",
				:p_CustodiaPropiaDCV AS "CustodiaPropiaDCV",
				:p_CustodiaTercerosDCV AS "CustodiaTercerosDCV",
				:p_CustodiaSociedadEmisora AS "CustodiaSociedadEmisora",
				:p_CustodiaIntermediario AS "CustodiaIntermediario",
				:p_CustodiaBolsas AS "CustodiaBolsas",
				:p_CustodiaOtros AS "CustodiaOtros",
				OPE."FechaCuadraturaCustodia" AS  "FechaRegistro",
				:p_Habilitado AS "Habilitado",
				--Auditoria
				:v_fechaHoraSistema AS "FechaIngreso",
				CURRENT_UTCTIMESTAMP AS "FechaCreacion",
				CURRENT_UTCTIMESTAMP AS "FechaModificacion",
				:p_IdUsuario AS "IdUsuarioCreador",
				:p_IdUsuario AS "IdUsuarioModificacion"
			FROM  "BSCL.Normativa.BD::PRO.CuadraturaCustodia" AS OPE
			 LEFT JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" INS ON OPE."IdInstrumento" = INS."IdInstrumento"
			WHERE  OPE."IdCuadratura"=p_IdCuadratura;
	
		
		END IF;		

		
	
	END;
	
	
END; 