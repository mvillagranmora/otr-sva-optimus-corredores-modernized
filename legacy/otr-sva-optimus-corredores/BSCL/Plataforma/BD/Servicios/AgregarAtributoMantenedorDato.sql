SET SCHEMA "BSCL";
GO 
DROP PROCEDURE "BSCL.Plataforma.BD.Servicios::AgregarAtributoMantenedorDato";
GO 
CREATE PROCEDURE
-- 
--  Proposito: Agrega un registro del mantenedor general dinamico
--  Autor: JVILLAGRAN
--  Fecha Creacion: 29/03/2017 
--
"BSCL.Plataforma.BD.Servicios::AgregarAtributoMantenedorDato"
(
  	IN p_IdEmpresa      			BIGINT,
  	IN p_IdUsuario      			BIGINT,
  	IN p_IdModulo       			NVARCHAR(3),    
  	IN p_UsaReglasNegocio			NVARCHAR(1),    
    IN p_Mantenedor 				NVARCHAR(6),
    IN p_Dato 						TABLE ("NumFila" BIGINT, "Campo" NVARCHAR(40), "Valor" NVARCHAR(520)),
    
    OUT p_out_Id 					NVARCHAR(50),  -- Id del registro generado
    OUT p_out_AdvertenciasContador 	INT,	  
    OUT p_out_AdvertenciasDetalle  	TABLE ("Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(5000)),	
  	OUT p_out_ErroresContador      	INT,
    OUT p_out_ErroresDetalle       	TABLE ("Codigo" nvarchar(40),"Descripcion" nvarchar(5000))
)
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"   
AS
BEGIN


  	--------------------------------
	--DECLARE Manejo de Errores
	--------------------------------

	DECLARE SALIDA_EXITOSA					CONDITION;
	DECLARE SALIDA_CONTROLADA 				CONDITION; 
	DECLARE v_resultadoRegla 				NVARCHAR(40);
    DECLARE v_IdRegistro					BIGINT;   -- Id del registro generado

    DECLARE v_Dato 							TABLE ("NumFila" BIGINT, "Campo" NVARCHAR(40), "Valor" NVARCHAR(520));
	DECLARE v_TablaCodigosMensajes			TABLE("IdInterno" BIGINT,"Codigo" NVARCHAR(100),"CodigoDato1" NVARCHAR(100),"ValorDato1" NVARCHAR(520),"CodigoDato2" NVARCHAR(100),"ValorDato2" NVARCHAR(520))
											DEFAULT SELECT 0  as "IdInterno",'' as "Codigo",'' as "CodigoDato1",'' as "ValorDato1",'' as "CodigoDato2",'' as "ValorDato2" FROM DUMMY WHERE 1=2;
	--* BOLSA
	DECLARE v_PaisSistema 					NVARCHAR(10);
	DECLARE v_PaisBoveda 					BIGINT;
	DECLARE v_EsBovedaExtranjera			NVARCHAR(10);
    	DECLARE v_IdBolsa						NVARCHAR(40);
	DECLARE v_ExisteCodigo					BIGINT;
	DECLARE v_MensajeDiccionario			TABLE( "CodigoMensaje" NVARCHAR(100),"CodigoDato" NVARCHAR(100),"ValorDato" NVARCHAR(520));

	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA	 
	BEGIN
		SELECT count(1) into p_out_ErroresContador FROM :p_out_ErroresDetalle;
		SELECT count(1) into p_out_AdvertenciasContador FROM :p_out_AdvertenciasDetalle;
	END;

	DECLARE EXIT HANDLER FOR SQLEXCEPTION
	BEGIN
		DECLARE v_errorExt 	NVARCHAR(40);	
		IF ::SQL_ERROR_CODE > 10000 THEN 
		
			--error pseudo-controlado pero desde un Sub-SP
			CALL "BSCL.Plataforma.BD.Servicios::ConsultarCodigoMensajeError" (::SQL_ERROR_CODE, v_errorExt);			
			
			p_out_ErroresDetalle = SELECT * FROM :p_out_ErroresDetalle UNION ALL  
								   SELECT v_errorExt,::SQL_ERROR_MESSAGE FROM dummy;
			SELECT count(1) into p_out_ErroresContador FROM :p_out_ErroresDetalle;										
		ELSE
			--Error interno no manejado  (El log será captado por la capa de XS control)		
			RESIGNAL;
		END IF;
	END;

	p_out_AdvertenciasContador		:= 0;
	p_out_ErroresContador			:= 0;  	
  	p_out_AdvertenciasDetalle       = SELECT '' As "Codigo", '' As "RequiereAutorizacion", '' As "Descripcion" FROM DUMMY WHERE 1=2;
  	p_out_ErroresDetalle            = SELECT '' As "Codigo", '' As "Descripcion"  FROM DUMMY WHERE 1=2;

  	
	----------------------------------------
	-- VALIDACIONES
	----------------------------------------

	IF p_UsaReglasNegocio = 'S' THEN
	BEGIN
		--validacion invalidante configuración básica del sistema
		CALL "BSCL.Plataforma.BD.Reglas::ServicioCumpleRequisitosBasicos"(:p_IdEmpresa,:p_IdUsuario,:p_IdModulo,v_out_ErroresDetalle,:v_resultadoRegla);

		p_out_ErroresDetalle = SELECT "Codigo", "Descripcion" FROM :v_out_ErroresDetalle UNION ALL 
							   SELECT * from :p_out_ErroresDetalle;

		IF :v_resultadoRegla = 'RECH' THEN
			SIGNAL SALIDA_CONTROLADA; 	
		END IF;
	END;
	END IF;

		
	----------------------------------------
	-- NEGOCIO
	----------------------------------------

   	IF UPPER(:p_Mantenedor) NOT IN ('BANCO', 'PLAZA', 'ESPGTA', 'PROPOS', 'MONEDA', 'MERCAD', 'PAIS', 'DERIVA', 'TIPREL', 'BOLSA') THEN
		INSERT INTO :v_MensajeDiccionario VALUES ('TIPO_TRANSACCION_NO_EXISTE', 'IdTipoTransaccion', :p_Mantenedor);   	
		CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionario" (	:v_MensajeDiccionario,	p_out_ErroresDetalle );
		SIGNAL SALIDA_CONTROLADA;
   	END IF;


	v_Dato = SELECT "NumFila", "Campo", "Valor" FROM :p_Dato;
 
    -- MANTENEDOR DE BANCOS
   	IF UPPER(:p_Mantenedor) = 'BANCO' THEN   
   	
   		SELECT "BSCL.Plataforma.BD.Secuencias::BAN.Banco".NEXTVAL  INTO v_IdRegistro  FROM DUMMY;
   		
	   	v_DatoBanco =
		SELECT 
			:v_IdRegistro AS "IdBanco",
		    COALESCE(MAX(CASE WHEN "Campo" = 'NombreBanco' 		THEN "Valor" ELSE null END),'') AS "NombreBanco",
		    COALESCE(MAX(CASE WHEN "Campo" = 'CodigoBanco' 		THEN "Valor" ELSE null END),'') AS "CodigoBanco",		    
		    COALESCE(MAX(CASE WHEN "Campo" = 'IdContraparte'	THEN "Valor" ELSE null END),0)  AS "IdContraparte",
		    COALESCE(MAX(CASE WHEN "Campo" = 'IdEstado' 		THEN "Valor" ELSE null END),'') AS "IdEstado",
		    COALESCE(MAX(CASE WHEN "Campo" = 'BancoReferenciaForward' 		THEN "Valor" ELSE null END),'') AS "BancoReferenciaForward",
			:p_IdEmpresa AS "IdEmpresa",
		    COALESCE(MAX(CASE WHEN "Campo" = 'IdPaisOperacion' 	THEN "Valor" ELSE null END),0) AS "IdPaisOperacion",
		    COALESCE(MAX(CASE WHEN "Campo" = 'IdTipoBanco' 		THEN "Valor" ELSE null END),'') AS "IdTipoBanco",
		    COALESCE(MAX(CASE WHEN "Campo" = 'IdSocioDeNegocio' THEN "Valor" ELSE NULL END),0) AS "IdSocioDeNegocio"
		FROM :v_Dato;


		INSERT INTO "BSCL.Plataforma.BD::BAN.Banco" ("IdBanco", "NombreBanco", "BancoReferenciaForward", "CodigoBanco", "IdPersona", "IdEstado", "IdEmpresa", "IdPaisOperacion", "IdTipoBanco")		
		SELECT 
			BAN."IdBanco",
		    BAN."NombreBanco",
		    BAN."BancoReferenciaForward",
		    BAN."CodigoBanco",			
			COALESCE(SOC."IdPersona",CON."IdPersona", 0) AS "IdPersona",
			BAN."IdEstado",
			BAN."IdEmpresa",
		    BAN."IdPaisOperacion",
		    BAN."IdTipoBanco"
		FROM :v_DatoBanco AS BAN		
		LEFT JOIN "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" AS SOC ON (SOC."IdSocioDeNegocio" = BAN."IdSocioDeNegocio")
		LEFT JOIN "BSCL.SocioNegocio.BD::CNT.Contraparte" AS CON ON (CON."IdContraparte" = BAN."IdContraparte");		
   	END IF; -- IF UPPER(:p_Mantenedor) = 'BANCO'



    -- MANTENEDOR DE PLAZAS
   	IF UPPER(:p_Mantenedor) = 'PLAZA' THEN   
		SELECT "BSCL.Plataforma.BD.Secuencias::BAN.Plaza".NEXTVAL  INTO v_IdRegistro  FROM DUMMY;

		INSERT INTO "BSCL.Plataforma.BD::BAN.Plaza" ("IdPlaza", "Codigo", "NombrePlaza", "PlazoRetencionMismoBanco", "PlazoRetencionOtroBanco", "PlazoMaximoCobroMismoBanco", "PlazoMaximoCobroOtroBanco", "IdPais", "IdEstado", "IdCiudad")		
		SELECT 
			:v_IdRegistro AS "IdPlaza",
		    COALESCE(MAX(CASE WHEN "Campo" = 'Codigo' 		THEN "Valor" ELSE null END),'') AS "Codigo",		    			
		    COALESCE(MAX(CASE WHEN "Campo" = 'NombrePlaza' 		THEN "Valor" ELSE null END),'') AS "NombrePlaza",
		    COALESCE(MAX(CASE WHEN "Campo" = 'PlazoRetencionMismoBanco' 		THEN "Valor" ELSE null END),'') AS "PlazoRetencionMismoBanco",
		    COALESCE(MAX(CASE WHEN "Campo" = 'PlazoRetencionOtroBanco' 		THEN "Valor" ELSE null END),'') AS "PlazoRetencionOtroBanco",
		    COALESCE(MAX(CASE WHEN "Campo" = 'PlazoMaximoCobroMismoBanco' 		THEN "Valor" ELSE null END),'') AS "PlazoMaximoCobroMismoBanco",
		    COALESCE(MAX(CASE WHEN "Campo" = 'PlazoMaximoCobroOtroBanco' 		THEN "Valor" ELSE null END),0) AS "PlazoMaximoCobroOtroBanco",
		    COALESCE(MAX(CASE WHEN "Campo" = 'IdPais' 		THEN "Valor" ELSE null END),'') AS "IdPais",
		    COALESCE(MAX(CASE WHEN "Campo" = 'IdEstado' 		THEN "Valor" ELSE null END),'') AS "IdEstado",
		    COALESCE(MAX(CASE WHEN "Campo" = 'IdCiudad' 	THEN "Valor" ELSE null END),0) AS "IdCiudad"
		FROM :v_Dato;
   	END IF; -- IF UPPER(:p_Mantenedor) = 'PLAZA'
   	


    -- ESPECIES EN GARANTIA
   	IF UPPER(:p_Mantenedor) = 'ESPGTA' THEN
   	
   		v_IdRegistro := 0;
		SELECT IFNULL(max("IdEspecieGarantia"),0)  INTO v_IdRegistro  FROM "BSCL.Custodia.BD::GTA.EspecieEnGarantia";
		v_IdRegistro := v_IdRegistro +1;
		
		INSERT INTO "BSCL.Custodia.BD::GTA.EspecieEnGarantia" ("IdEspecieGarantia", "Codigo", "Descripcion", "IdEstado")		
		SELECT 
			:v_IdRegistro AS "IdEspecieGarantia",
		    COALESCE(MAX(CASE WHEN "Campo" = 'Codigo' 		THEN "Valor" ELSE null END),'') AS "Codigo",		    			
		    COALESCE(MAX(CASE WHEN "Campo" = 'Descripcion' 	THEN "Valor" ELSE null END),'') AS "Descripcion",
		    COALESCE(MAX(CASE WHEN "Campo" = 'IdEstado' 	THEN "Valor" ELSE null END),'') AS "IdEstado"
		FROM :v_Dato;
   	END IF; -- IF UPPER(:p_Mantenedor) = 'ESPGTA'

   	
   	 -- PROPOSITO
    IF UPPER(:p_Mantenedor) = 'PROPOS' THEN    
        v_propositos = SELECT 
            COALESCE(MAX(CASE WHEN "Campo" = 'IdPropositoPortafolio' THEN "Valor" ELSE null END),'') AS "Codigo",                        
            COALESCE(MAX(CASE WHEN "Campo" = 'Descripcion'  THEN "Valor" ELSE null END),'') AS "Descripcion",
            COALESCE(MAX(CASE WHEN "Campo" = 'IdEstado'     THEN "Valor" ELSE null END),'') AS "IdEstado"
        FROM :v_Dato;
        
        INSERT INTO "BSCL.Portafolio.BD::POR.PropositoPortafolio" ("IdPropositoPortafolio", "Descripcion", "IdEstado")        
        SELECT 
            p."Codigo",                        
            p."Descripcion",
            p."IdEstado"
        FROM :v_propositos p
        LEFT JOIN "BSCL.Portafolio.BD::POR.PropositoPortafolio" pp ON p."Codigo" = pp."IdPropositoPortafolio"
        WHERE pp."IdPropositoPortafolio" IS NULL;
    END IF; -- IF UPPER(:p_Mantenedor) = 'PROPOS'
    
   	


    -- MANTENEDOR DE MONEDA
   	IF UPPER(:p_Mantenedor) = 'MONEDA' THEN   
            
		INSERT INTO "BSCL.Plataforma.BD::PLA.Moneda" (
			"IdMoneda","Codigo","CodigoNumerico","Nombre","Simbolo","Decimales","RangoSuperiorParidad","RangoInferiorParidad",
			"PorcentajeSuperiorParidad","PorcentajeInferiorParidad","RangoSuperiorTipoCambio","RangoInferiorTipoCambio",
			"PorcentajeSuperiorTipoCambio","PorcentajeInferiorTipoCambio","Orden","IdPais","IdEstado","IdTipoMoneda")			
		SELECT 		    
			COALESCE(MAX(CASE WHEN "Campo" = 'IdMoneda' 	THEN "Valor" ELSE null END),'') AS "IdMoneda",
			COALESCE(MAX(CASE WHEN "Campo" = 'Codigo' 		THEN "Valor" ELSE null END),'')  AS "Codigo",
			0 AS "CodigoNumerico",
			COALESCE(MAX(CASE WHEN "Campo" = 'Nombre' 		THEN "Valor" ELSE null END),'') AS "Nombre",
			COALESCE(MAX(CASE WHEN "Campo" = 'Simbolo' 		THEN "Valor" ELSE null END),'')  AS "Simbolo",
			0 AS "Decimales",
			0 AS "RangoSuperiorParidad",
			0 AS "RangoInferiorParidad",
			0 AS "PorcentajeSuperiorParidad",
			0 AS "PorcentajeInferiorParidad",
			0 AS "RangoSuperiorTipoCambio",
			0 AS "RangoInferiorTipoCambio",
			0 AS "PorcentajeSuperiorTipoCambio",
			0 AS "PorcentajeInferiorTipoCambio",
			0 AS "Orden",
			COALESCE(MAX(CASE WHEN "Campo" = 'IdPais' 		THEN "Valor" ELSE null END),0) AS "IdPais",
			COALESCE(MAX(CASE WHEN "Campo" = 'IdEstado' 	THEN "Valor" ELSE null END),'') AS "IdEstado",
			COALESCE(MAX(CASE WHEN "Campo" = 'IdTipoMoneda' THEN "Valor" ELSE null END),'') AS "IdTipoMoneda"
		    
		FROM :v_Dato;
   	END IF; -- IF UPPER(:p_Mantenedor) = 'MONEDA'
   	
   	-- MERCADO
   	IF UPPER(:p_Mantenedor) = 'MERCAD' THEN
		
		INSERT INTO "BSCL.Plataforma.BD::INS.Mercado" ("IdMercado", "Codigo", "Nombre", "Descripcion", "IdEstado")
		SELECT 
			COALESCE(MAX(CASE WHEN "Campo" = 'IdMercado' 	THEN "Valor" ELSE null END),'') AS "IdMercado",
		    COALESCE(MAX(CASE WHEN "Campo" = 'Codigo' 		THEN "Valor" ELSE null END),'') AS "Codigo",
		    COALESCE(MAX(CASE WHEN "Campo" = 'Nombre' 		THEN "Valor" ELSE null END),'') AS "Nombre",
		    COALESCE(MAX(CASE WHEN "Campo" = 'Descripcion' 	THEN "Valor" ELSE null END),'') AS "Descripcion",
		    COALESCE(MAX(CASE WHEN "Campo" = 'IdEstado' 	THEN "Valor" ELSE null END),'') AS "IdEstado"
		FROM :v_Dato;
   	END IF; -- IF UPPER(:p_Mantenedor) = 'MERCAD'
	
	-- PAIS
	IF UPPER(:p_Mantenedor) = 'PAIS' THEN
		
		v_MensajesError_Pais = SELECT 'ERROR_PAIS_CODIGO_YA_EXISTE' AS "CodigoMensaje", 'CodigoPais' AS "CodigoDato", "CodigoPais" AS "ValorDato"  
							   FROM "BSCL.Plataforma.BD::REG.Pais" AS PAIS
							   INNER JOIN :v_Dato AS DATO ON DATO."Campo" = 'CodigoPais' AND PAIS."CodigoPais" = DATO."Valor"
							   UNION ALL
							   SELECT 'ERROR_PAIS_ID_YA_EXISTE' AS "CodigoMensaje", 'IdPais' AS "CodigoDato", CAST("IdPais" AS NVARCHAR(250)) AS "ValorDato"  
							   FROM "BSCL.Plataforma.BD::REG.Pais" AS PAIS
							   INNER JOIN :v_Dato AS DATO ON DATO."Campo" = 'IdPais' AND PAIS."IdPais" = DATO."Valor"
							   UNION ALL
							   SELECT 'ERROR_CAMPO_ES_OCDE_INVALIDO' AS "CodigoMensaje", '' AS "CodigoDato", '' AS "ValorDato"
							   FROM :v_Dato WHERE "Campo" = 'EsOcde' AND "Valor" NOT IN ('S','N')
							   UNION ALL
							   SELECT 'ERROR_CAMPO_ES_PAIS_US_INVALIDO' AS "CodigoMensaje", '' AS "CodigoDato", '' AS "ValorDato"
							   FROM :v_Dato WHERE "Campo" = 'EsPaisUs' AND "Valor" NOT IN ('S','N');
							   
		CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionario" (:v_MensajesError_Pais, :v_out_ErroresDetalleMensajes);
		
		p_out_ErroresDetalle = SELECT "Codigo", "Descripcion" FROM :v_out_ErroresDetalleMensajes;
		
		SELECT COUNT(1) INTO  p_out_ErroresContador FROM :p_out_ErroresDetalle;
		
		IF p_out_ErroresContador = 0 THEN 
			INSERT INTO "BSCL.Plataforma.BD::REG.Pais" ("IdPais", "NombrePais", "CodigoPais", "UsaDivisionPais", "EsOcde", "EsPaisUs")
			SELECT
				COALESCE(MAX(CASE WHEN "Campo" = 'IdPais' 			THEN "Valor" ELSE null END),'') AS "IdPais",
			    COALESCE(MAX(CASE WHEN "Campo" = 'NombrePais' 		THEN "Valor" ELSE null END),'') AS "NombrePais",
			    COALESCE(MAX(CASE WHEN "Campo" = 'CodigoPais' 		THEN "Valor" ELSE null END),'') AS "CodigoPais",
			    'N' AS "UsaDivisionPais",
			    COALESCE(MAX(CASE WHEN "Campo" = 'EsOcde' 			THEN "Valor" ELSE null END),'') AS "EsOcde",
			    COALESCE(MAX(CASE WHEN "Campo" = 'EsPaisUs' 		THEN "Valor" ELSE null END),'') AS "EsPaisUs"
			FROM :v_Dato;
		END IF;
	END IF; -- IF UPPER(:p_Mantenedor) = 'PAIS'
	
	-- MANTENEDOR DE DERIVADOS
   	IF UPPER(:p_Mantenedor) = 'DERIVA' THEN   
            
		INSERT INTO "BSCL.Custodia.BD::DER.Configuracion" (
			 "IdEmpresa"
			,"ModalidadEjercicio" 	--Manual, Automatico
			,"TipoLiquidacion"		--Fisica, Financiera
			,"ParametroEjercicio"	--MontoUtilidadBloqueo, BloqueaEnvioOTM, BloqueaEnvioPUTsinCusCN, BloqueaEnvioCALLsinSaldoCN
			,"ParametroEjercicioValor"--S,N,monto
			)			
		SELECT 		    
			:p_IdEmpresa AS "IdEmpresa"
			,COALESCE(MAX(CASE WHEN "Campo" = 'ModalidadEjercicio' 		THEN "Valor" ELSE null END),'') AS "ModalidadEjercicio"
			,COALESCE(MAX(CASE WHEN "Campo" = 'TipoLiquidacion' 			THEN "Valor" ELSE null END),'')  AS "TipoLiquidacion"
			,COALESCE(MAX(CASE WHEN "Campo" = 'ParametroEjercicio' 		THEN "Valor" ELSE null END),'') AS "ParametroEjercicio"
			,COALESCE(MAX(CASE WHEN "Campo" = 'ParametroEjercicioValor' 	THEN "Valor" ELSE null END),'') AS "ParametroEjercicioValor"
		FROM :v_Dato;
   	END IF; -- IF UPPER(:p_Mantenedor) = 'DERIVA'
	
   	-- MANTENEDOR DE CODIGO TIPO RELACION
   	IF UPPER(:p_Mantenedor) = 'TIPREL' THEN   
            
		INSERT INTO "BSCL.Plataforma.BD::REL.CodigoTipoRelacion" (
			 "IdEmpresa"
			,"CodigoTipoRelacion" 
			,"DescripcionTipoRelacion"
			)			
		SELECT 		    
			:p_IdEmpresa AS "IdEmpresa"
			,COALESCE(MAX(CASE WHEN "Campo" = 'CodigoTipoRelacion' 		THEN "Valor" ELSE null END),'') AS "CodigoTipoRelacion"
			,COALESCE(MAX(CASE WHEN "Campo" = 'DescripcionTipoRelacion' 			THEN "Valor" ELSE null END),'')  AS "DescripcionTipoRelacion"
		FROM :v_Dato;
   	END IF; -- IF UPPER(:p_Mantenedor) = 'TIPREL'
   	
   	
    -- MANTENEDOR DE BOLSAS
   	IF UPPER(:p_Mantenedor) = 'BOLSA' THEN   
   	
   		--* Codigo de Bolsa Existe?
		SELECT 	TRIM(MAX( CASE WHEN "Campo" = 'IdBolsa'	THEN TRIM("Valor") ELSE '' END)) INTO v_IdBolsa FROM :v_Dato;		   		
		SELECT  COUNT(1) INTO v_ExisteCodigo FROM "BSCL.Plataforma.BD::INS.Bolsa" WHERE "IdBolsa" = v_IdBolsa;

		IF v_ExisteCodigo > 0 THEN
			INSERT INTO :v_MensajeDiccionario VALUES ('UI_REGISTRO_YA_EXISTE' , 'Valor', 'BP' );
			CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionario" (	:v_MensajeDiccionario,	p_out_ErroresDetalle );
			SIGNAL SALIDA_CONTROLADA;  
		ELSE 
			IF LENGTH(v_IdBolsa) > 3 THEN
				INSERT INTO :v_MensajeDiccionario VALUES ('UI_EXCEDE_LARGO_ID_BOLSA' , '', '' );			
				CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionario" (	:v_MensajeDiccionario,	p_out_ErroresDetalle );
				SIGNAL SALIDA_CONTROLADA;
			END IF;
		END IF;
	
		--* Determina si es Extranjera
   		SELECT 	TRIM("Valor") INTO v_PaisSistema FROM "BSCL.Plataforma.BD::PLA.Configuracion" WHERE	"CodigoParametro" = 'PLA.PaisSistema';
   		SELECT 	TRIM(MAX( CASE WHEN "Campo" = 'IdPais' 	THEN "Valor" ELSE '0' END)) INTO v_PaisBoveda FROM :v_Dato;

		v_EsBovedaExtranjera = CASE WHEN TRIM(v_PaisSistema) = TRIM(v_PaisBoveda) THEN 'N' ELSE 'S' END;
   	
		INSERT INTO "BSCL.Plataforma.BD::INS.Bolsa"	(
			 "IdBolsa"
			,"Codigo"
			,"Nombre"
			,"IdPais"
			,"IdEstado"
			,"EsExtranjera"
			,"DocumentoIdentidad"
			,"TieneFormaNegociacion"
			,"UsaBovedaLocal")
			
		SELECT 
		    v_IdBolsa AS "IdBolsa"
		    ,COALESCE(MAX(CASE WHEN "Campo" = 'Codigo' 	THEN "Valor" ELSE null END),'') AS "Codigo"		    			
		    ,COALESCE(MAX(CASE WHEN "Campo" = 'Nombre' 	THEN "Valor" ELSE null END),'') AS "Nombre"
		    ,v_PaisBoveda AS "IdPais"
		    ,COALESCE(MAX(CASE WHEN "Campo" = 'IdEstado' THEN "Valor" ELSE null END),'') AS "IdEstado"
		    ,v_EsBovedaExtranjera AS "EsExtranjera"
			,'' AS "DocumentoIdentidad"
			,'N' AS "TieneFormaNegociacion"
			,'N' AS "UsaBovedaLocal"
		FROM :v_Dato;			
   	
   	END IF; -- IF UPPER(:p_Mantenedor) = 'BOLSA'   	
   	
	-- Retorna Id del registro generado
	p_out_Id = CAST(:v_IdRegistro AS NVARCHAR(50));
	
END;