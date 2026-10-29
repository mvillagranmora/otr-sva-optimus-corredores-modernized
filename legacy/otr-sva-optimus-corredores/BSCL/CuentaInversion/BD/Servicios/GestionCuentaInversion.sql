SET SCHEMA "BSCL";
GO
DROP PROCEDURE "BSCL.CuentaInversion.BD.Servicios::GestionCuentaInversion";
GO
CREATE PROCEDURE 
-- 
--  Proposito: Crear Cuenta de Inversion
--  Autor: NRM - BCS
--  Fecha Creacion: 24/05/2016
--  
"BSCL.CuentaInversion.BD.Servicios::GestionCuentaInversion" (	
	-- Parametros entrada comunes en todos los servicios publicos
	IN  p_IdEmpresa                 BIGINT,
    IN  p_IdUsuario                 BIGINT, 
    IN  p_IdModulo                  NVARCHAR(3),
	IN  p_UsaReglasNegocio	 		NVARCHAR(1),	-- S/N se usa en caso de servicios anidados.
	
	-- Parametros entrada del servicio publico
	IN  p_EsEjecucionManual			NVARCHAR(1)		DEFAULT 'N',	--S: Ejecucion manual, N: Ejecución por sistema
	IN  p_CuentaInversion			TABLE("IdCliente"					BIGINT,
										  "IdPortafolio"				BIGINT,
										  "IdMoneda"					NVARCHAR(3),
										  "IdCuentaInversion"			BIGINT,
										  "Nombre"						NVARCHAR(250),
										  "CuentaInversionPrincipal"	NVARCHAR(1),
										  "Accion"						NVARCHAR(1)), 	-- I: Ingresar cuenta inversion
																						-- D: Desactivar cuenta inversion
																						-- A: Activar cuenta inversion
																						-- M: Modificar Cuenta Inversion
    
    -- Parametros salida del servicio publico	
	OUT p_out_ResultadoProceso		TABLE("Campo" VARCHAR(40),"Id" BIGINT, "Servicio" VARCHAR(250), "Resultado" VARCHAR(40)),
	
	-- Salidas para mensajes de advertencias y errores
	OUT p_out_InformacionContador  	INT,
	OUT p_out_InformacionDetalle   	TABLE("Codigo" NVARCHAR(100),"Descripcion" NVARCHAR(5000)),	
	OUT p_out_AdvertenciasContador  INT,
	OUT p_out_AdvertenciasDetalle   TABLE("Codigo" nvarchar(100),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(5000)),	
	OUT p_out_ErroresContador       INT,
	OUT p_out_ErroresDetalle        TABLE("Codigo" nvarchar(100),"Descripcion" nvarchar(5000))	
)
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
AS 
BEGIN
	--------------------
	--DECLARE Manejo de Errores
	--------------------
	DECLARE SALIDA_EXITOSA				CONDITION;
	DECLARE SALIDA_CONTROLADA 			CONDITION;
	DECLARE SALIDA_CONTROLADA_NO_DATA 	CONDITION;
	DECLARE v_resultadoRegla 			NVARCHAR(40);	
	DECLARE v_ErrorNoData  				NVARCHAR(100);
	DECLARE v_ErroresDetalle 			TABLE("Codigo" NVARCHAR(100),"Descripcion" NVARCHAR(5000));
	DECLARE v_RechazadosPorValidacion	TABLE("IdInterno" BIGINT);
	DECLARE v_TablaCodigosMensajes		TABLE("IdInterno" BIGINT,"Codigo" NVARCHAR(100),"CodigoDato1" NVARCHAR(100),"ValorDato1" NVARCHAR(520),"CodigoDato2" NVARCHAR(100),"ValorDato2" NVARCHAR(520));
	
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA	 
	BEGIN
		--salida/error/advertencia controlado por este servicio
		SELECT count(1) into p_out_ErroresContador FROM :p_out_ErroresDetalle;
		SELECT count(1) into p_out_AdvertenciasContador FROM :p_out_AdvertenciasDetalle;
	END;
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA_NO_DATA     
	BEGIN
		v_TablaCodigosMensajes= SELECT 0 as "IdInterno",:v_ErrorNoData as "Codigo",'' as "CodigoDato1",'' as "ValorDato1",'' as "CodigoDato2",'' as "ValorDato2" FROM DUMMY;	
		CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionarioMasivo" (:v_TablaCodigosMensajes,:v_erroroutmsg);		
		p_out_ErroresDetalle =  SELECT "Codigo","Descripcion" FROM :v_erroroutmsg;
		SELECT COUNT(1) INTO p_out_ErroresContador FROM :p_out_ErroresDetalle;		
	END;
	DECLARE EXIT HANDLER FOR SALIDA_EXITOSA     
	BEGIN
		CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionarioMasivo" (:v_TablaCodigosMensajes,:v_erroroutmsg);
		p_out_InformacionDetalle = SELECT "Codigo", "Descripcion" FROM :v_erroroutmsg;
		SELECT COUNT("Codigo") INTO p_out_InformacionContador FROM :p_out_InformacionDetalle;		
	END;
	DECLARE EXIT HANDLER FOR SQLEXCEPTION
	BEGIN
		declare v_errorExt nvarchar(40);

		IF ::SQL_ERROR_CODE > 10000 THEN
			--error pseudo-controlado pero desde un Sub-SP
			call "BSCL.Plataforma.BD.Servicios::ConsultarCodigoMensajeError" (::SQL_ERROR_CODE,v_errorExt);			
			p_out_ErroresDetalle = SELECT * FROM :p_out_ErroresDetalle UNION ALL  
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
	p_out_AdvertenciasDetalle	= SELECT '' as "Codigo",'' as "RequiereAutorizacion",'' as "Descripcion" FROM dummy where 1=2;
	p_out_ErroresDetalle		= SELECT '' as "Codigo",'' as "Descripcion" FROM dummy where 1=2;
	p_out_ResultadoProceso		= SELECT '' AS "Campo", 0 as "Id", '' as "Servicio", '' as "Resultado" FROM dummy where 1=2;
	
	v_RechazadosPorValidacion	= SELECT 0 AS "IdInterno" FROM dummy WHERE 1=2;
	
	
	v_CuentaInversion =
	 SELECT ROW_NUMBER() over (ORDER BY "IdCliente","IdPortafolio","IdMoneda","IdCuentaInversion","Nombre","CuentaInversionPrincipal","Accion") as "IdInterno",
	 		"IdCliente",
			"IdPortafolio",
			"IdMoneda",
			"IdCuentaInversion",
			"Nombre",
			"CuentaInversionPrincipal",
			"Accion"
	   FROM (SELECT DISTINCT
			 		"IdCliente",
					"IdPortafolio",
					"IdMoneda",
					"IdCuentaInversion",
					"Nombre",
					"CuentaInversionPrincipal",
					"Accion"
			   FROM :p_CuentaInversion
			) as T;
	
	--------------------
	--VALIDACIONES
	--------------------	   
	IF p_UsaReglasNegocio = 'S' THEN
	BEGIN
		CALL "BSCL.Plataforma.BD.Reglas::ServicioCumpleRequisitosBasicos"(:p_IdEmpresa,:p_IdUsuario,:p_IdModulo,v_Errores,:v_resultadoRegla);
		IF :v_resultadoRegla = 'RECH' THEN
			 p_out_ErroresDetalle = SELECT "Codigo","Descripcion" FROM :v_Errores;
			 SIGNAL SALIDA_CONTROLADA; 	
		END IF;
		
		
		CALL "BSCL.CuentaInversion.BD.Reglas::ServicioCumpleRequisitoGestionCuentaInversion"(:v_CuentaInversion,:v_RechazadosPorValidacion,:v_ErroresDetalle,:v_resultadoRegla);
		IF :v_resultadoRegla = 'RECH' THEN				
			p_out_ErroresDetalle = SELECT "Codigo","Descripcion" FROM :v_ErroresDetalle;
			SIGNAL SALIDA_CONTROLADA;
		ELSEIF :v_resultadoRegla = 'ADVE' THEN
		
			v_CuentaInversion = 
			 SELECT "IdInterno","IdCliente","IdPortafolio","IdMoneda","IdCuentaInversion","Nombre","CuentaInversionPrincipal","Accion"
			   FROM :v_CuentaInversion 
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
		DECLARE EXIT HANDLER FOR SQL_ERROR_CODE 1299
		BEGIN
			v_ErrorNoData := 'DATA_NO_EXISTE';
			SIGNAL SALIDA_CONTROLADA_NO_DATA;
		END;
		
		------------------------------------
		--INGRESAR CUENTA INVERSION
		------------------------------------
		v_Secuencias =
		SELECT "BSCL.CuentaInversion.BD.Secuencias::CuentaInversion".NEXTVAL as "IdCuentaInversion",
				CI."IdPortafolio",
				CI."IdMoneda"
		  FROM :v_CuentaInversion as CI
		  WHERE CI."Accion" = 'I';
		
		
		INSERT INTO "BSCL.CuentaInversion.BD::CTA.CuentaInversion"(
		    "IdCuentaInversion",
		    "IdEmpresa",
		    "IdCliente", 
			"IdPortafolio",
			"IdMoneda",
			"Codigo",
			"Nombre",
			"CuentaInversionPrincipal",
			"FechaIngresoRegistro",
			"IdUsuario",
			"IdEstado"	 
		)
		 SELECT  SEC."IdCuentaInversion"
				,:p_IdEmpresa
				,CI."IdCliente"
				,CI."IdPortafolio"
				,CI."IdMoneda"
				,'CTA' ||'-'|| CI."IdMoneda"
				,COALESCE(DIC."Descripcion",'') ||' '|| MON."Nombre"
				,CASE WHEN CI."IdMoneda" = CFG."PLA.MonedaSistema" THEN 'S' ELSE 'N' END
				,CURRENT_UTCTIMESTAMP
				,:p_IdUsuario
				,'ACT'
		   FROM :v_CuentaInversion as CI
		  INNER JOIN :v_Secuencias as SEC
		  		  ON SEC."IdPortafolio" = CI."IdPortafolio"
		  		 AND SEC."IdMoneda" = CI."IdMoneda"
		   LEFT JOIN "BSCL.Plataforma.BD::PLA.Moneda" as MON
		   		  ON MON."IdMoneda" = CI."IdMoneda"
		   LEFT JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarConfiguracionPivot" as CFG
		   		  ON CFG."IdEmpresa" = :p_IdEmpresa
		   LEFT JOIN "BSCL.Plataforma.BD::PLA.Diccionario" as DIC
		   		  ON DIC."CodDiccionario" = 'SP_NOMBRE_CUENTA_INVERSION'
		   		 AND DIC."CodLenguaje" = CFG."PLA.IdiomaSistema"
		  WHERE CI."Accion" = 'I';
		  
		p_out_ResultadoProceso = 
		 SELECT 'IdCuentaInversion' as "Campo",
				"IdCuentaInversion" as "Id",
				'BSCL.CuentaInversion.BD.Servicios::GestionCuentaInversion' as "Servicio",
				'ING' as "Resultado"
		   FROM :v_Secuencias
		  UNION ALL SELECT "Campo","Id","Servicio","Resultado" FROM :p_out_ResultadoProceso;

		
		----------------------------------------
		--DESACTIVAR / ACTIVAR CUENTA INVERSION
		----------------------------------------
		UPDATE CI
		   SET CI."IdEstado" 			= CASE WHEN CII."Accion" = 'D' THEN 'INA' WHEN CII."Accion" = 'A' THEN 'ACT' END,
		   	   CI."BloqueadaEdicion" 	= CASE WHEN :p_EsEjecucionManual = 'S' AND CII."Accion" = 'D' THEN 'S' ELSE 'N' END
		  FROM :v_CuentaInversion as CII
		 INNER JOIN "BSCL.CuentaInversion.BD::CTA.CuentaInversion" as CI
		  		 ON CI."IdCuentaInversion" = CII."IdCuentaInversion"
		  		AND CI."BloqueadaEdicion" = CASE WHEN :p_EsEjecucionManual = 'N' THEN 'N' ELSE CI."BloqueadaEdicion" END
		 WHERE CII."Accion" IN ('D','A');

		p_out_ResultadoProceso = 
		 SELECT 'IdCuentaInversion' as "Campo",
				"IdCuentaInversion" as "Id",
				'BSCL.CuentaInversion.BD.Servicios::GestionCuentaInversion' as "Servicio",
				CASE WHEN "Accion" = 'D' THEN 'INA' WHEN "Accion" = 'A' THEN 'ACT' END as "Resultado"
		   FROM :v_CuentaInversion
		  WHERE "Accion" IN ('D','A')
		  UNION ALL SELECT "Campo","Id","Servicio","Resultado" FROM :p_out_ResultadoProceso;
				
		------------------------------------
		--MODIFICAR CUENTA INVERSION
		------------------------------------
		--1) si se define una nueva cuenta principal para el portafolio se setea el resto de las cuentas con 'N'
		 v_PortafoliosAActualizar =  
		  SELECT CII."IdPortafolio",
		  		 COUNT(CASE WHEN CI."CuentaInversionPrincipal" = 'S' AND CII."CuentaInversionPrincipal" = 'S' THEN 1 END) as "Count"
		    FROM :v_CuentaInversion as CII
		   INNER JOIN "BSCL.CuentaInversion.BD::CTA.CuentaInversion" as CI
		  		   ON CI."IdPortafolio" = CII."IdPortafolio"
		   WHERE CII."Accion" = 'M'
		   GROUP BY CII."IdPortafolio"
		  HAVING COUNT(CASE WHEN CI."CuentaInversionPrincipal" = 'S' AND CII."CuentaInversionPrincipal" = 'S' THEN 1 END) > 0;
				
		UPDATE CI
		   SET CI."CuentaInversionPrincipal" = 'N'
		  FROM :v_PortafoliosAActualizar as PA
		 INNER JOIN "BSCL.CuentaInversion.BD::CTA.CuentaInversion" as CI
		  		 ON CI."IdPortafolio" = PA."IdPortafolio"; 
		
		--2) se actualiza la cuenta de inversión con el valor indicado
		UPDATE CI
		   SET CI."CuentaInversionPrincipal" = CII."CuentaInversionPrincipal"
		  FROM :v_CuentaInversion as CII
		 INNER JOIN "BSCL.CuentaInversion.BD::CTA.CuentaInversion" as CI
		  		 ON CI."IdCuentaInversion" = CII."IdCuentaInversion"
		 WHERE CII."Accion" = 'M';
		 
		 --3) se verifica que exista al menos una cuenta como principal sino es así se deja como principal la asociada a la moneda de sistema
		v_PortafoliosAActualizar =
		  SELECT CII."IdPortafolio",
		  		 COUNT(CASE WHEN CI."CuentaInversionPrincipal" = 'S' THEN 1 END) as "Count"
		    FROM :v_CuentaInversion as CII
		   INNER JOIN "BSCL.CuentaInversion.BD::CTA.CuentaInversion" as CI
		  		   ON CI."IdPortafolio" = CII."IdPortafolio"
		   WHERE CII."Accion" = 'M'
		   GROUP BY CII."IdPortafolio"
		  HAVING COUNT(CASE WHEN CI."CuentaInversionPrincipal" = 'S' THEN 1 END) = 0;

		UPDATE CI
		   SET CI."CuentaInversionPrincipal" = CASE WHEN CI."IdMoneda" = CFG."PLA.MonedaSistema" THEN 'S' ELSE 'N' END
		  FROM :v_PortafoliosAActualizar as PA
		 INNER JOIN "BSCL.CuentaInversion.BD::CTA.CuentaInversion" as CI
		  		 ON CI."IdPortafolio" = PA."IdPortafolio"
		 INNER JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarConfiguracionPivot" as CFG
		   		  ON CFG."IdEmpresa" = :p_IdEmpresa; 
		

		 
		 p_out_ResultadoProceso = 
		 SELECT 'IdCuentaInversion' as "Campo",
				"IdCuentaInversion" as "Id",
				'BSCL.CuentaInversion.BD.Servicios::GestionCuentaInversion' as "Servicio",
				'MOD' as "Resultado"
		   FROM :v_CuentaInversion
		  WHERE "Accion" = 'M'
		  UNION ALL SELECT "Campo","Id","Servicio","Resultado" FROM :p_out_ResultadoProceso;
		  
		  
		------------------------------------
		--RETORNA INFORMACION DE EXITO 
		------------------------------------
		v_TablaCodigosMensajes =
		 SELECT "Id" as "IdInterno"
			   ,CASE WHEN "Resultado" = 'ING' THEN 'INGRESO_EXITOSO_CUENTA_INVERSION'
			   		 WHEN "Resultado" = 'INA' THEN 'CUENTA_INVERSION_DESACTIVADA'
			   		 WHEN "Resultado" = 'ACT' THEN 'CUENTA_INVERSION_ACTIVADA'
			   		 WHEN "Resultado" = 'MOD' THEN 'CUENTA_INVERSION_MODIFICADA'
			     END as "Codigo"
			   ,'NombreCliente' as "CodigoDato1"
			   ,"NombreCompleto" as "ValorDato1"
			   ,'NombreMoneda' as "CodigoDato2"
			   ,"NombreMoneda" as "ValorDato2" 
		  FROM :p_out_ResultadoProceso as RP
		 INNER JOIN "BSCL.CuentaInversion.BD.Vistas::ConsultarCuentaInversion" as CI ON CI."IdCuentaInversion" = RP."Id"
		 INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" as CLI ON CLI."IdCliente" = CI."IdCliente";
		SIGNAL SALIDA_EXITOSA;
	END;
END;
GO