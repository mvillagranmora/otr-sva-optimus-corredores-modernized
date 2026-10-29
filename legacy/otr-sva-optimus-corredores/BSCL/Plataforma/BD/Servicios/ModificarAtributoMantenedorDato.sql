SET SCHEMA "BSCL";
GO
DROP PROCEDURE "BSCL.Plataforma.BD.Servicios::ModificarAtributoMantenedorDato";
GO
CREATE PROCEDURE
-- 
--  Proposito: Modificar registro de mantenedor dinamico
--  Autor: Natalia Silva Muñoz
--  Fecha Creacion: 02/11/2015 
--
"BSCL.Plataforma.BD.Servicios::ModificarAtributoMantenedorDato"
  (
  	IN p_IdEmpresa      			BIGINT,
  	IN p_IdUsuario      			BIGINT,
  	IN p_IdModulo       			NVARCHAR(3),    
  	IN p_UsaReglasNegocio			NVARCHAR(1),  
    IN p_Mantenedor 				NVARCHAR(6),    
    IN p_Dato table ("NumFila" BIGINT, "Campo" NVARCHAR(40), "Valor" NVARCHAR(520)),
    
    --OUT p_OUT_ID NVARCHAR(1000)
    --Salidas para mensajes de advertencias y errores
     --OUT p_out_AdvertenciasContador INT,	  
     --OUT p_out_AdvertenciasDetalle  TABLE ("Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(520)),	
  	 OUT p_out_ErroresContador      INT,
     OUT p_out_ErroresDetalle       TABLE ("Codigo" nvarchar(40),"Descripcion" nvarchar(520))
  )
As
Begin
   Declare v_Dato table ("NumFila" BIGINT, "Campo" NVARCHAR(40), "Valor" NVARCHAR(520));
     
   --Moneda
   Declare ValorCodigo, ValorSimbolo, ValorMultiplicaODivide NVARCHAR(40);
   Declare ValorCodigoNumerico, ValorDecimales, ValorOrden Integer;
   Declare ValorNombre NVARCHAR(100);
   Declare ValorRangoSuperiorParidad, ValorRangoInferiorParidad, ValorPorcentajeSuperiorParidad, ValorPorcentajeInferiorParidad,
           ValorRangoSuperiorTipoCambio, ValorRangoInferiorTipoCambio, ValorPorcentajeSuperiorTipoCambio,
           ValorPorcentajeInferiorTipoCambio  Decimal(26,8);
   Declare ValorIdPais BIGINT;  
   Declare ValorIdMoneda, ValorIdEstado NVARCHAR(3);
   Declare v_ExisteMoneda BIGINT:= 0;
   Declare v_EstadoMoneda NVARCHAR(3):= '';
   
   
   --Configuracion
   Declare ValorIdEmpresa BIGINT;
   Declare ValorCodigoParametro NVARCHAR(40);
   Declare ValorDescripcion, ValorValor NVARCHAR(520);
   
   --Proposito
   Declare ValorIdPropositoPortafolio NVARCHAR(40);
   
   --Requiere Autorizacion
   Declare ValorIdConcepto NVARCHAR(6);
   Declare ValorRequiereAutorizacion NVARCHAR(1); 
   
   --Custodia
   Declare ValorIdTipoPrestamo  NVARCHAR(6);
   Declare ValorCobraPrimaAlInicio NVARCHAR(1); 
   Declare ValorMinimoDiasPrestamo BIGINT;
   Declare ValorMaximoDiasPrestamo BIGINT;
   Declare ValorSoloCuentaPropia NVARCHAR(1);
   Declare ValorPlazoSugerido BIGINT;
   Declare ValorMinimoDiasAnticipo BIGINT;
    
   Declare ValorCodigoParametroCUS  NVARCHAR(40);
   Declare ValorParametroValorCUS NVARCHAR(520);    
      
   p_out_ErroresContador := 0;
   v_Dato = Select "NumFila", "Campo", "Valor" from :p_Dato;
   p_out_ErroresDetalle = SELECT '' As "Codigo", '' As "Descripcion"  FROM DUMMY WHERE 1=2;

	-- MANTENEDOR DE MONEDAS
	IF UPPER(:p_Mantenedor) = 'MONEDA' THEN
		v_DatosMoneda =
		SELECT 
			COALESCE(MAX(CASE WHEN "Campo" = 'IdMoneda' 			THEN "Valor" ELSE null END),'')  AS "IdMoneda",				    
			COALESCE(MAX(CASE WHEN "Campo" = 'Nombre' 				THEN "Valor" ELSE null END),'')  AS "Nombre",				    
			COALESCE(MAX(CASE WHEN "Campo" = 'Simbolo' 				THEN "Valor" ELSE null END),'')  AS "Simbolo",				    
			COALESCE(MAX(CASE WHEN "Campo" = 'IdPais' 				THEN "Valor" ELSE null END),0)   AS "IdPais",				    
			COALESCE(MAX(CASE WHEN "Campo" = 'IdEstado' 			THEN "Valor" ELSE null END),'')  AS "IdEstado",				    												
			COALESCE(MAX(CASE WHEN "Campo" = 'IdTipoMoneda' 		THEN "Valor" ELSE null END),'')  AS "IdTipoMoneda",				    																	    
			COALESCE(MAX(CASE WHEN "Campo" = 'MonedaSMV' 			THEN "Valor" ELSE null END),0)   AS "MonedaSMV"
		FROM :v_Dato;

		UPDATE MON
		SET  			
       		"Nombre" 		= DAT."Nombre",
          	"Simbolo" 		= DAT."Simbolo",
          	"IdPais" 		= DAT."IdPais",
          	"IdEstado" 		= DAT."IdEstado",
          	"IdTipoMoneda" 	= DAT."IdTipoMoneda",
          	"MonedaSMV"		= DAT."MonedaSMV"
		FROM "BSCL.Plataforma.BD::PLA.Moneda" AS MON
		INNER JOIN :v_DatosMoneda AS DAT ON (DAT."IdMoneda" = MON."IdMoneda");
		
		SELECT COUNT(1) INTO v_ExisteMoneda
			FROM "BSCL.Plataforma.BD::MER.DefinicionParametroFinanciero" AS IDPF
			WHERE IDPF."IdParametroFinanciero" = (SELECT "IdMoneda" FROM :v_DatosMoneda);
				
		SELECT "IdEstado" INTO v_EstadoMoneda FROM :v_DatosMoneda;
				
		IF (v_ExisteMoneda = 0 AND v_EstadoMoneda = 'ACT') THEN	
			INSERT INTO "BSCL.Plataforma.BD::MER.DefinicionParametroFinanciero"
			SELECT 
				DATOS."IdMoneda", --IdParametroFinanciero NVARCHAR(40)
				'DI',             --Periodo NVARCHAR(2)
				DATOS."Nombre",   --Nombre NVARCHAR(100)
				DATOS."Nombre",   --Descripcion NVARCHAR(500)
				DATOS."IdMoneda", --IdMonedaParametro NVARCHAR(3)
				'USD',            --IdMonedaValor NVARCHAR(3)
				''                --CodigoSerieBancoCentral NVARCHAR(100)
			FROM :v_DatosMoneda AS DATOS;
		END IF;	
		
	END IF;	  -- IF UPPER(:p_Mantenedor) = 'MONEDA'


   
   	IF UPPER(:p_Mantenedor) = 'CONFIG' THEN
        Select "Valor"
        Into ValorCodigoParametro
        From :v_Dato
        Where "Campo" = 'CodigoParametro';
                          
        Select "Valor"
        Into ValorDescripcion
        From :v_Dato
        Where "Campo" = 'Descripcion';
        
        Select "Valor"
        Into ValorValor
        From :v_Dato
        Where "Campo" = 'Valor';
        
        Update "BSCL.Plataforma.BD::PLA.Configuracion"
        Set "Valor" = ValorValor
        Where "IdEmpresa" = :p_IdEmpresa
           And "CodigoParametro" = ValorCodigoParametro;    
	END IF; -- IF UPPER(:p_Mantenedor) = 'CONFIG'
   
   
    -- MANTENEDOR DE PROPOSITO PORTAFOLIO
   	IF UPPER(:p_Mantenedor) = 'PROPOS' THEN
     		Select "Valor" Into ValorIdPropositoPortafolio	From :v_Dato Where "Campo" = 'IdPropositoPortafolio';
	        Select "Valor" Into ValorDescripcion			From :v_Dato Where "Campo" = 'Descripcion';
	        Select "Valor" Into ValorIdEstado				From :v_Dato Where "Campo" = 'IdEstado';
	        
	        Update "BSCL.Portafolio.BD::POR.PropositoPortafolio"
	        Set "Descripcion" = ValorDescripcion,
	            "IdEstado" = ValorIdEstado
	        Where "IdPropositoPortafolio" = ValorIdPropositoPortafolio;
	END IF; -- IF UPPER(:p_Mantenedor) = 'CONFIG'


	-- MANTENEDOR DE AUTORIZACION
   	IF UPPER(:p_Mantenedor) = 'PORTAF' THEN
 		Select "Valor" Into ValorIdConcepto	From :v_Dato Where "Campo" = 'IdConcepto';
        Select "Valor" Into ValorRequiereAutorizacion	From :v_Dato Where "Campo" = 'RequiereAutorizacion';
        
        Update "BSCL.Plataforma.BD::PLA.RequiereAutorizacion"
        Set "RequiereAutorizacion" = ValorRequiereAutorizacion
        Where "IdConcepto" = ValorIdConcepto;
	END IF; -- IF UPPER(:p_Mantenedor) = 'PORTAF'
		
		
	--	MANTENEDOR DE TIPO PRESTAMO
    IF UPPER(:p_Mantenedor) = 'PRESTA' THEN
         Select "Valor"
         Into ValorIdTipoPrestamo
         From :v_Dato
         Where "Campo" = 'IdTipoPrestamo';     
         
         Select "Valor"
         Into ValorCobraPrimaAlInicio
         From :v_Dato
         Where "Campo" = 'CobraPrimaAlInicio';     
         
         Select "Valor"
         Into ValorMinimoDiasPrestamo
         From :v_Dato
         Where "Campo" = 'MinimoDiasPrestamo';     
         
         Select "Valor"
         Into ValorMaximoDiasPrestamo
         From :v_Dato
         Where "Campo" = 'MaximoDiasPrestamo';     
         
         Select "Valor"
         Into ValorSoloCuentaPropia
         From :v_Dato
         Where "Campo" = 'SoloCuentaPropia';     
         
         Select "Valor"
         Into ValorPlazoSugerido
         From :v_Dato
         Where "Campo" = 'PlazoSugerido';     
         
         Select "Valor"
         Into ValorMinimoDiasAnticipo
         From :v_Dato
         Where "Campo" = 'MinimoDiasAnticipo';     
        
        
         Update "BSCL.Custodia.BD::PRT.TipoPrestamo"
         Set "CobraPrimaAlInicio" = ValorCobraPrimaAlInicio
         	, "MinimoDiasPrestamo" = ValorMinimoDiasPrestamo
         	, "MaximoDiasPrestamo" = ValorMaximoDiasPrestamo
         	, "SoloCuentaPropia" = ValorSoloCuentaPropia
         	, "PlazoSugerido" = ValorPlazoSugerido
         	, "MinimoDiasAnticipo" = ValorMinimoDiasAnticipo
         	
         Where "IdTipoPrestamo" = ValorIdTipoPrestamo;
	END IF;   -- IF UPPER(:p_Mantenedor) = 'PRESTA'
		         	
	
	-- MANTENEDOR DE CONFIGURACION CUSTODIA	         	
    IF UPPER(:p_Mantenedor) = 'CTADCV' THEN
         Select "Valor" Into ValorCodigoParametroCUS
         From :v_Dato
         Where "Campo" = 'CodigoParametro';     
         
         Select "Valor" Into ValorParametroValorCUS
         From :v_Dato
         Where "Campo" = 'Valor';     
        
         Update "BSCL.Custodia.BD::CUS.Configuracion"
         Set "Valor" = ValorParametroValorCUS
         Where "CodigoParametro" = ValorCodigoParametroCUS;
	END IF;   -- IF UPPER(:p_Mantenedor) = 'CTADCV'

	
   		
	
	-- MANTENEDOR DE BANCOS
	IF UPPER(:p_Mantenedor) = 'BANCO' THEN
		v_DatosBanco =		 
		SELECT 
		    COALESCE(MAX(CASE WHEN "Campo" = 'IdBanco' 			THEN "Valor" ELSE null END),0)  AS "IdBanco",
		    COALESCE(MAX(CASE WHEN "Campo" = 'NombreBanco' 		THEN "Valor" ELSE null END),'') AS "NombreBanco",
		    COALESCE(MAX(CASE WHEN "Campo" = 'CodigoBanco' 		THEN "Valor" ELSE null END),'') AS "CodigoBanco",		    
		    COALESCE(MAX(CASE WHEN "Campo" = 'IdContraparte'	THEN "Valor" ELSE null END),0)  AS "IdContraparte",
		    COALESCE(MAX(CASE WHEN "Campo" = 'IdEstado' 		THEN "Valor" ELSE null END),'') AS "IdEstado",
		    COALESCE(MAX(CASE WHEN "Campo" = 'IdPaisOperacion' 	THEN "Valor" ELSE null END),0)  AS "IdPaisOperacion",
		    COALESCE(MAX(CASE WHEN "Campo" = 'IdTipoBanco' 		THEN "Valor" ELSE null END),'') AS "IdTipoBanco",
		    COALESCE(MAX(CASE WHEN "Campo" = 'BancoReferenciaForward' 		THEN "Valor" ELSE null END),'') AS "BancoReferenciaForward",
		    COALESCE(MAX(CASE WHEN "Campo" = 'IdSocioDeNegocio' THEN "Valor" ELSE null END),0) AS "IdSocioDeNegocio"
		FROM :v_Dato;
		
		
		UPDATE BAN
		SET  "NombreBanco" 		= DAT."NombreBanco",
		     "CodigoBanco" 		= DAT."CodigoBanco",
		   	 "IdPaisOperacion"  = DAT."IdPaisOperacion",
		   	 "IdPersona" 		= COALESCE(SOC."IdPersona",CON."IdPersona", 0),
		   	 "IdEstado"    		= DAT."IdEstado",
		   	 "BancoReferenciaForward"    		= DAT."BancoReferenciaForward",
		   	 "IdTipoBanco" 		= DAT."IdTipoBanco"
		FROM "BSCL.Plataforma.BD::BAN.Banco" AS BAN	
		INNER JOIN :v_DatosBanco AS DAT ON (DAT."IdBanco" = BAN."IdBanco")		
		LEFT JOIN "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" AS SOC ON (SOC."IdSocioDeNegocio" = DAT."IdSocioDeNegocio")
		LEFT JOIN "BSCL.SocioNegocio.BD::CNT.Contraparte" AS CON ON (CON."IdContraparte" = DAT."IdContraparte");		
		
	END IF;	-- IF UPPER(:p_Mantenedor) = 'BANCO'
	
	IF (UPPER(:p_Mantenedor) = 'BOLSA') THEN
		v_DatosBolsa =
		SELECT 
			COALESCE(MAX(CASE WHEN "Campo" = 'IdBolsa'  THEN "Valor" ELSE null END),'') AS "IdBolsa",
			COALESCE(MAX(CASE WHEN "Campo" = 'Nombre'   THEN "Valor" ELSE null END),'') AS "Nombre",
			COALESCE(MAX(CASE WHEN "Campo" = 'IdPais'   THEN "Valor" ELSE null END),'') AS "IdPais",
			COALESCE(MAX(CASE WHEN "Campo" = 'IdEstado' THEN "Valor" ELSE null END),'') AS "IdEstado"
		FROM :v_Dato;
		
		UPDATE BOL
		SET "Nombre"   = DAT."Nombre",
			"IdPais"   = DAT."IdPais",
			"IdEstado" = DAT."IdEstado"
		FROM "BSCL.Plataforma.BD::INS.Bolsa" BOL
		INNER JOIN :v_DatosBolsa AS DAT ON (DAT."IdBolsa" = BOL."IdBolsa");

	END IF;     	    
	
	-- MANTENEDOR DE PLAZAS
	IF UPPER(:p_Mantenedor) = 'PLAZA' THEN	
		v_DatosPlaza =
		SELECT 
			COALESCE(MAX(CASE WHEN "Campo" = 'IdPlaza' 						THEN "Valor" ELSE null END),0)  AS "IdPlaza",				    
		    COALESCE(MAX(CASE WHEN "Campo" = 'Codigo' 						THEN "Valor" ELSE null END),'') AS "Codigo",
		    COALESCE(MAX(CASE WHEN "Campo" = 'NombrePlaza' 					THEN "Valor" ELSE null END),'') AS "NombrePlaza",
		    COALESCE(MAX(CASE WHEN "Campo" = 'PlazoRetencionMismoBanco' 	THEN "Valor" ELSE null END),0)  AS "PlazoRetencionMismoBanco",
		    COALESCE(MAX(CASE WHEN "Campo" = 'PlazoRetencionOtroBanco' 		THEN "Valor" ELSE null END),0)  AS "PlazoRetencionOtroBanco",
		    COALESCE(MAX(CASE WHEN "Campo" = 'PlazoMaximoCobroMismoBanco'	THEN "Valor" ELSE null END),0)  AS "PlazoMaximoCobroMismoBanco",
		    COALESCE(MAX(CASE WHEN "Campo" = 'PlazoMaximoCobroOtroBanco' 	THEN "Valor" ELSE null END),0)  AS "PlazoMaximoCobroOtroBanco",
		    COALESCE(MAX(CASE WHEN "Campo" = 'IdPais' 						THEN "Valor" ELSE null END),0)  AS "IdPais",
		    COALESCE(MAX(CASE WHEN "Campo" = 'IdEstado' 					THEN "Valor" ELSE null END),'') AS "IdEstado",
		    COALESCE(MAX(CASE WHEN "Campo" = 'IdCiudad' 					THEN "Valor" ELSE null END),0)  AS "IdCiudad"
		FROM :v_Dato;

		UPDATE PLA
		SET  
			"Codigo" 					= DAT."Codigo",
			"NombrePlaza" 				= DAT."NombrePlaza",
			"PlazoRetencionMismoBanco" 	= DAT."PlazoRetencionMismoBanco",
			"PlazoRetencionOtroBanco" 	= DAT."PlazoRetencionOtroBanco",
			"PlazoMaximoCobroMismoBanco"= DAT."PlazoMaximoCobroMismoBanco",
			"PlazoMaximoCobroOtroBanco" = DAT."PlazoMaximoCobroOtroBanco",
			"IdPais" 					= DAT."IdPais",
			"IdEstado" 					= DAT."IdEstado",
			"IdCiudad" 					= DAT."IdCiudad"		    		   	 
		FROM "BSCL.Plataforma.BD::BAN.Plaza" AS PLA
		INNER JOIN :v_DatosPlaza AS DAT ON (DAT."IdPlaza" = PLA."IdPlaza");
			
	END IF;	-- IF UPPER(:p_Mantenedor) = 'PLAZA'


	--- ESPECIES EN GARANTIA
	IF UPPER(:p_Mantenedor) = 'ESPGTA' THEN	
		v_DatosEG =
		SELECT 
			COALESCE(MAX(CASE WHEN "Campo" = 'IdEspecieGarantia' 	THEN CAST("Valor" as NVARCHAR(5)) ELSE null END),'')  AS "IdEspecieGarantia",				    
		    COALESCE(MAX(CASE WHEN "Campo" = 'Codigo' 				THEN "Valor" ELSE null END),'') AS "Codigo",
		    COALESCE(MAX(CASE WHEN "Campo" = 'Descripcion' 			THEN "Valor" ELSE null END),'') AS "Descripcion",
		    COALESCE(MAX(CASE WHEN "Campo" = 'IdEstado' 			THEN "Valor" ELSE null END),'') AS "IdEstado"
		FROM :v_Dato;

		UPDATE EG
		SET  
			"Codigo" 					= DAT."Codigo",
			"Descripcion" 				= DAT."Descripcion",
			"IdEstado" 					= DAT."IdEstado"
		FROM "BSCL.Custodia.BD::GTA.EspecieEnGarantia" AS EG
		INNER JOIN :v_DatosEG AS DAT ON (CAST(DAT."IdEspecieGarantia" as BIGINT) = EG."IdEspecieGarantia");
			
	END IF;	-- IF UPPER(:p_Mantenedor) = 'ESPGTA'

	
   	IF UPPER(:p_Mantenedor) = 'CONAPV' THEN
        Select "Valor"
        Into ValorIdEmpresa
        From :v_Dato
        Where "Campo" = 'IdEmpresa';
      
        Select "Valor"
        Into ValorCodigoParametro
        From :v_Dato
        Where "Campo" = 'CodigoParametro';
                                  
        Select "Valor"
        Into ValorValor
        From :v_Dato
        Where "Campo" = 'Valor';
        
        Update "BSCL.APV.BD::PLA.ConfiguracionAPV"
        Set "Valor" = ValorValor
        Where "IdEmpresa" = ValorIdEmpresa 
           And "CodigoParametro" = ValorCodigoParametro;    
	END IF; -- IF UPPER(:p_Mantenedor) = 'CONFIG'
	
	-- MANTENEDOR DE MERCADO
	IF UPPER(:p_Mantenedor) = 'MERCAD' THEN
		v_DatosMercado =
		SELECT 
			COALESCE(MAX(CASE WHEN "Campo" = 'IdMercado' 		THEN "Valor" ELSE null END),'')  AS "IdMercado",
			COALESCE(MAX(CASE WHEN "Campo" = 'Nombre' 			THEN "Valor" ELSE null END),'')  AS "Nombre",
			COALESCE(MAX(CASE WHEN "Campo" = 'Descripcion' 		THEN "Valor" ELSE null END),'')  AS "Descripcion",
			COALESCE(MAX(CASE WHEN "Campo" = 'IdEstado' 		THEN "Valor" ELSE null END),'')  AS "IdEstado"
		FROM :v_Dato;

		UPDATE MER
		SET  			
       		"Nombre" 		= DAT."Nombre",
          	"Descripcion" 	= DAT."Descripcion",
          	"IdEstado" 		= DAT."IdEstado"
		FROM "BSCL.Plataforma.BD::INS.Mercado" AS MER
		INNER JOIN :v_DatosMercado AS DAT ON (DAT."IdMercado" = MER."IdMercado");
			
	END IF;	  -- IF UPPER(:p_Mantenedor) = 'MERCAD'
	
	-- MANTENEDOR DE PAIS
	IF UPPER(:p_Mantenedor) = 'PAIS' THEN
		
		v_MensajesError_Pais = SELECT 'ERROR_CAMPO_ES_OCDE_INVALIDO' AS "CodigoMensaje", '' AS "CodigoDato", '' AS "ValorDato"
							   FROM :v_Dato WHERE "Campo" = 'EsOcde' AND "Valor" NOT IN ('S','N')
							   UNION ALL
							   SELECT 'ERROR_CAMPO_ES_PAIS_US_INVALIDO' AS "CodigoMensaje", '' AS "CodigoDato", '' AS "ValorDato"
							   FROM :v_Dato WHERE "Campo" = 'EsPaisUs' AND "Valor" NOT IN ('S','N');
							   
		CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionario" (:v_MensajesError_Pais, :v_out_ErroresDetalleMensajes);
		
		p_out_ErroresDetalle = SELECT "Codigo", "Descripcion" FROM :v_out_ErroresDetalleMensajes;
		
		SELECT COUNT(1) INTO  p_out_ErroresContador FROM :p_out_ErroresDetalle;
		
		v_DatosPais =
		SELECT
			COALESCE(MAX(CASE WHEN "Campo" = 'IdPais' 			THEN "Valor" ELSE null END),'')  AS "IdPais",
			COALESCE(MAX(CASE WHEN "Campo" = 'NombrePais' 		THEN "Valor" ELSE null END),'')  AS "NombrePais",
			COALESCE(MAX(CASE WHEN "Campo" = 'EsOcde' 			THEN "Valor" ELSE null END),'')  AS "EsOcde",
			COALESCE(MAX(CASE WHEN "Campo" = 'EsPaisUs' 		THEN "Valor" ELSE null END),'')  AS "EsPaisUs"
		FROM :v_Dato;
		
		IF p_out_ErroresContador = 0 THEN 
			UPDATE PAIS
			SET
				"NombrePais" 	= DAT."NombrePais",
				"EsOcde"		= DAT."EsOcde",
				"EsPaisUs"		= DAT."EsPaisUs"
			FROM "BSCL.Plataforma.BD::REG.Pais" AS PAIS
			INNER JOIN :v_DatosPais AS DAT ON (DAT."IdPais" = PAIS."IdPais");
		END IF;
		
	END IF;   -- IF UPPER(:p_Mantenedor) = 'PAIS'
	
	IF (UPPER(:p_Mantenedor) = 'DERIVA') THEN
		v_DatosDerivado =
		SELECT 
			:p_IdEmpresa AS "IdEmpresa"
			,COALESCE(MAX(CASE WHEN "Campo" = 'ModalidadEjercicio' 		THEN "Valor" ELSE null END),'') AS "ModalidadEjercicio"
			,COALESCE(MAX(CASE WHEN "Campo" = 'TipoLiquidacion' 			THEN "Valor" ELSE null END),'')  AS "TipoLiquidacion"
			,COALESCE(MAX(CASE WHEN "Campo" = 'ParametroEjercicio' 		THEN "Valor" ELSE null END),'') AS "ParametroEjercicio"
			,COALESCE(MAX(CASE WHEN "Campo" = 'ParametroEjercicioValor' 	THEN "Valor" ELSE null END),'') AS "ParametroEjercicioValor" --unico campo updateable
		FROM :v_Dato;
		
		UPDATE CONF
		SET	CONF."ParametroEjercicioValor" = DAT."ParametroEjercicioValor"
		FROM "BSCL.Custodia.BD::DER.Configuracion" CONF
			INNER JOIN :v_DatosDerivado AS DAT ON (
					DAT."IdEmpresa" = :p_IdEmpresa 
				AND DAT."ModalidadEjercicio" 	= CONF."ModalidadEjercicio"
				AND DAT."TipoLiquidacion" 		= CONF."TipoLiquidacion"
				AND DAT."ParametroEjercicio" 	= CONF."ParametroEjercicio");
		
	END IF;	   

	IF (UPPER(:p_Mantenedor) = 'TIPREL') THEN
		v_DatosTipRel =
		SELECT 
			:p_IdEmpresa AS "IdEmpresa"
			,COALESCE(MAX(CASE WHEN "Campo" = 'CodigoTipoRelacion' 		THEN "Valor" ELSE null END),'') AS "CodigoTipoRelacion"
			,COALESCE(MAX(CASE WHEN "Campo" = 'DescripcionTipoRelacion' 			THEN "Valor" ELSE null END),'')  AS "DescripcionTipoRelacion"
		FROM :v_Dato;
		
		UPDATE CONF
		SET	CONF."DescripcionTipoRelacion" = DAT."DescripcionTipoRelacion"
		FROM "BSCL.Plataforma.BD::REL.CodigoTipoRelacion" CONF
			INNER JOIN :v_DatosTipRel AS DAT ON (
					DAT."IdEmpresa" = :p_IdEmpresa 
				AND DAT."CodigoTipoRelacion" 	= CONF."CodigoTipoRelacion");
		UPDATE CONF
		SET "NombreTipoRelacion" = DAT."DescripcionTipoRelacion"
		FROM "BSCL.SocioNegocio.BD::REL.TipoRelacion" CONF
			INNER JOIN :v_DatosTipRel AS DAT ON (
					DAT."IdEmpresa" = :p_IdEmpresa 
				AND DAT."CodigoTipoRelacion" 	= CONF."IdTipoRelacion");
		
	END IF;	
                           
End; 
