
SET SCHEMA "BSCL";
GO 
DROP PROCEDURE  "BSCL.Portafolio.BD.Reglas::ServicioCumpleRequisitosGestionPreferenciaPago";
GO
CREATE PROCEDURE
--  
--  Proposito: Valida que las preferencias de pago del Socio cumplan con las reglas de negocio
--  Autor: JVILLAGRAN - BCS
--  Fecha Creacion: 17/11/2017
--     
"BSCL.Portafolio.BD.Reglas::ServicioCumpleRequisitosGestionPreferenciaPago"(
	IN  p_IdEmpresa					  BIGINT,
	IN  p_IdPortafolio 				  BIGINT,
	IN  p_IdPreferenciaPagoPortafolio BIGINT 	DEFAULT 0,
	IN  p_IdPreferenciaPago			  NVARCHAR(40), 
	IN  p_IdCuentaBancaria			  BIGINT,
		
	
	-- Salidas para mensajes de errores	
	OUT p_out_ErroresDetalle         TABLE ("Codigo" nvarchar(100), "Descripcion" nvarchar(520)),
	OUT p_out_AdvertenciasDetalle	 TABLE ("Codigo" nvarchar(100), "RequiereAutorizacion" nvarchar(40), "Descripcion" nvarchar(520)),	
    OUT p_out_ResultadoRegla         NVARCHAR(40)   -- OK   = Cumple todos los requisitos		
											        -- ADVE = Alguna de las reglas posee advertencias
                                                    -- RECH = Alguna de las reglas detienen la operacion

) 	
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER
	DEFAULT SCHEMA BSCL
AS
BEGIN
	DECLARE SALIDA_CONTROLADA CONDITION;
	DECLARE SALIDA_CONTROLADA_NO_DATA CONDITION;
	DECLARE v_erroroutmsg  			NVARCHAR(520);
	DECLARE v_ErrorNoData  			NVARCHAR(100); 
	DECLARE v_resultadoRegla       	NVARCHAR(40);
	DECLARE v_ErroresContador 		INT; 
	

	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA     
	BEGIN
		p_out_resultadoRegla	:= 'RECH';	
		v_hASh = SELECT 'FolioCartera' AS "Codigo", 0 AS "Valor" FROM DUMMY;		
		CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajeDiccionario" (:v_ErrorNoData,:v_hASh,:v_erroroutmsg);

		p_out_ErroresDetalle =  SELECT :v_ErrorNoData  AS "Codigo",:v_erroroutmsg AS "Descripcion" FROM dummy UNION ALL
								SELECT * FROM :p_out_ErroresDetalle;
	END; 
	
	p_out_resultadoRegla	  := 'OK';
	p_out_ErroresDetalle	   = SELECT '' AS "Codigo",'' AS "Descripcion"  FROM dummy WHERE 1=2;
	p_out_AdvertenciasDetalle  = SELECT '' AS "Codigo", '' AS "RequiereAutorizacion", '' AS "Descripcion" FROM dummy WHERE 1=2;
	
	
	
	
	BEGIN	
		DECLARE v_error_General			BIGINT;
	   	DECLARE v_error_Negocio_ADVE	BIGINT;
	   	DECLARE v_error_Duplicado		BIGINT;	   
		DECLARE v_TablaCodigosMensajes	TABLE ("CodigoMensaje" NVARCHAR(100),"CodigoDato" NVARCHAR(100),"ValorDato" NVARCHAR(520));
		DECLARE v_TablaRequisitos     	TABLE ("Prioridad" INT,"Cumple" BIGINT, "CodigoError" NVARCHAR(100),"CodigoDato" NVARCHAR(100), "ValorDato" NVARCHAR(520),"IdPreferenciaPago" NVARCHAR(40) );		
		DECLARE v_LocalizacionSistema 	NVARCHAR(20) := '';
		DECLARE v_CountTipoProductoMesa INT;
		
			
		DECLARE EXIT HANDLER FOR SQL_ERROR_CODE 1299
		BEGIN
			v_ErrorNoData := 'DATA_NO_EXISTE';
			SIGNAL SALIDA_CONTROLADA_NO_DATA;
		END;

		v_TablaCodigosMensajes	= SELECT '' AS "CodigoMensaje",'' AS "CodigoDato",'' AS "ValorDato" FROM dummy WHERE 1=2;	
		v_TablaRequisitos    	= SELECT  0 AS "Prioridad", 0 AS "Cumple",'' AS "CodigoError",'' AS "CodigoDato", '' AS "ValorDato", 0 AS "IdPreferenciaPago"  FROM dummy WHERE (1=2);		


		
		-- Localizacion sistema (PE/CL)
		SELECT "Valor" 
		INTO v_LocalizacionSistema 
		FROM BSCL."BSCL.Plataforma.BD::PLA.Configuracion" 
		WHERE "CodigoParametro" = 'PLA.LocalizacionSistema' 
		AND "IdEmpresa" = :p_IdEmpresa;
		
		
		------------------------------------------------------
		-- VALIDACIONES BASICAS
		------------------------------------------------------
		
		IF (:p_IdPreferenciaPagoPortafolio = 0) THEN
			-- Valida si ya existe la preferencia de pago durante el insert
			v_TablaRequisitos = 
			SELECT  1 AS "Prioridad",
					CASE
						 WHEN (COUNT("IdPreferenciaPagoPortafolio") > 0) THEN 0
						 ELSE  1
					END AS "Cumple",
					CASE
						 WHEN (COUNT("IdPreferenciaPagoPortafolio") > 0) THEN 'PREFERENCIA_PAGO_EXISTE'
						 ELSE  ''
					END AS "CodigoError",
					'IdPreferenciaPagoPortafolio' AS "CodigoDato",
			       	CAST(p_IdPreferenciaPagoPortafolio as NVARCHAR(100)) AS "ValorDato",		       	
			       	:p_IdPreferenciaPago AS "IdPreferenciaPago"		       	
			FROM "BSCL.Portafolio.BD::POR.PreferenciaPagoPortafolio"
		   WHERE "IdPortafolio" 	 			= :p_IdPortafolio
		     AND "IdPreferenciaPago"			= :p_IdPreferenciaPago
		     AND "IdCuentaBancaria"  			= :p_IdCuentaBancaria
		     AND "IdEstado"                     = 'ACT'
			UNION ALL SELECT "Prioridad","Cumple","CodigoError","CodigoDato","ValorDato","IdPreferenciaPago" FROM :v_TablaRequisitos;			

		ELSE 
			IF (:p_IdPreferenciaPagoPortafolio > 0) THEN
				SELECT COUNT(*) INTO v_error_Duplicado
				FROM "BSCL.Portafolio.BD::POR.PreferenciaPagoPortafolio"
			   WHERE "IdPortafolio" 				= :p_IdPortafolio
			     AND "IdPreferenciaPago" 			= :p_IdPreferenciaPago
			     AND "IdCuentaBancaria"  			= :p_IdCuentaBancaria
			     AND "IdPreferenciaPagoPortafolio" != :p_IdPreferenciaPagoPortafolio;
			     
			     IF (v_error_Duplicado > 0) THEN
			     	-- elimina registros duplicados en el portafolio manteniendo el modificado
			     	-- Bug corregido en proceso de forma de pago ha generado registros duplicados en los ambientes
			     	-- No modifique si no conoce el proceso.
					DELETE FROM "BSCL.Portafolio.BD::POR.PreferenciaPagoPortafolio"
				     WHERE "IdPortafolio" 	 			  = :p_IdPortafolio
				       AND "IdPreferenciaPago" 			  = :p_IdPreferenciaPago
				       AND "IdCuentaBancaria"  			  = :p_IdCuentaBancaria	
				       AND "IdPreferenciaPagoPortafolio" != :p_IdPreferenciaPagoPortafolio;  
			     END IF;
		     END IF;			    
		END IF;
		

		-- Valida si existe o esta activa la preferencia de negocio en la mesa cuando corresponda
		SELECT COUNT(*) INTO v_CountTipoProductoMesa FROM "BSCL.Plataforma.BD.Vistas::PAG.ConsultarPreferenciaPago"
		WHERE "IdPreferenciaPago" = :p_IdPreferenciaPago
			AND "IdTipoProducto" IN ('MERF','MEFW','MEME');
		
		IF (v_CountTipoProductoMesa > 0) THEN
			v_TablaRequisitos =
			SELECT 1 AS "Prioridad",
			CASE WHEN (COUNT(pagMes."IdPreferenciaPago") = 0) THEN 0 ELSE 1 END AS "Cumple",
			CASE WHEN (COUNT(pagMes."IdPreferenciaPago") = 0) THEN 'UI_FORMA_PAGO_NO_EXISTE_EN_MESA' ELSE '' END AS "CodigoError",
			'IdPreferenciaPago' AS "CodigoDato",
			CAST(:p_IdPreferenciaPagoPortafolio as NVARCHAR(100)) AS "ValorDato",	       	
			:p_IdPreferenciaPago AS "IdPreferenciaPago"
			FROM "BSCL.Mesa.BD::MES.FormaDePagoMesa" pagMes
			INNER JOIN "BSCL.Plataforma.BD.Vistas::PAG.ConsultarPreferenciaPago" prefPag 
				ON (pagMes."IdPreferenciaPago" = prefPag."IdPreferenciaPago" and pagMes."IdEmpresa" = prefPag."IdEmpresa" and prefPag."IdEstado" = 'ACT')
			WHERE pagMes."IdPreferenciaPago" = :p_IdPreferenciaPago
				AND pagMes."IdEstado" = 'ACT'
				AND prefPag."IdTipoProducto" IN ('MERF','MEFW','MEME')
			UNION ALL SELECT "Prioridad","Cumple","CodigoError","CodigoDato","ValorDato","IdPreferenciaPago" FROM :v_TablaRequisitos;
		END IF;
		
		-------------------------------------------------
		-- VALIDACIONES DE NEGOCIO
		-------------------------------------------------
		
		v_PreferenciaPago = 
		SELECT PF."IdPreferenciaPago", PF."IdMedioDePago" ,MDP."TipoMedioDePago"
		FROM "BSCL.Plataforma.BD::PAG.PreferenciaPago" AS PF
		INNER JOIN "BSCL.Plataforma.BD::PAG.MedioDePago" AS MDP ON (MDP."IdMedioDePago" = PF."IdMedioDePago")
		WHERE PF."IdPreferenciaPago" = :p_IdPreferenciaPago;
				
		IF (v_LocalizacionSistema = 'PE') THEN -- Solo para PERU
				-- Valida que para preferencias  con "Transferencia Interbancaria" y cuenta nacionales, exista el Codigo de cuenta interbancario(CCI)
				v_TablaRequisitos = 
				SELECT  1 AS "Prioridad",
						CASE
							 WHEN (COALESCE(PCB."CodigoCuentaInterbancario",'') = '') THEN 0
							 ELSE  1
						END AS "Cumple",
						CASE
							 WHEN (COALESCE(PCB."CodigoCuentaInterbancario",'') = '') THEN 'CUENTA_BANCARIA_SIN_CODIGO_INTERBANCARIO_CCI'
							 ELSE  ''
						END AS "CodigoError",
						'NombreCuentaBancaria' AS "CodigoDato",				
				       	CAST(PCB."NombreCuentaBancariaFull" as NVARCHAR(100)) AS "ValorDato",		       	
				       	PP."IdPreferenciaPago"		       	
				FROM "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaCuentasBancarias" AS PCB
				INNER JOIN :v_PreferenciaPago AS PP ON (1=1)				
				WHERE PCB."IdCuentaBancaria" = :p_IdCuentaBancaria
				AND   PCB."IdTipoBanco" = 'NAC' -- Cuenta bancaria de banco nacional 
				AND PP."IdMedioDePago" = 'INT'  -- Si es Transferencia interbancaria
				UNION ALL SELECT "Prioridad","Cumple","CodigoError","CodigoDato","ValorDato","IdPreferenciaPago" FROM :v_TablaRequisitos;
				
				
				-- Valida que para preferencias con "Transferencia Interbancaria" y cuenta internacionales, existan los Codigos ABA/IBAN
				v_TablaRequisitos = 
				SELECT  1 AS "Prioridad",
						CASE
							 WHEN COALESCE(PCB."CodigoAba",'') = '' AND COALESCE(PCB."CodigoIban",'') = '' AND COALESCE(PCB."OtroCodigoBancoExtranjero",'') = '' AND COALESCE(PCB."OtroCodigoCuentaExtranjero",'')  = '' THEN 0
							 WHEN COALESCE(PCB."CodigoAba",'') = '' THEN 0
							 ELSE  1
						END AS "Cumple",
						CASE 
							 WHEN COALESCE(PCB."CodigoAba",'') = '' AND COALESCE(PCB."OtroCodigoBancoExtranjero",'') = '' AND COALESCE(PCB."OtroCodigoCuentaExtranjero",'')  = '' THEN 'CUENTA_BANCARIA_SIN_CODIGOS_ABA/IBAN'
							 WHEN COALESCE(PCB."CodigoAba",'') = '' THEN 'CUENTA_BANCARIA_SIN_CODIGOS_ABA/IBAN'
							 ELSE  ''
						END AS "CodigoError",
						'NombreCuentaBancaria' AS "CodigoDato",				
				       	CAST(PCB."NombreCuentaBancariaFull" as NVARCHAR(100)) AS "ValorDato",		       	
				       	PP."IdPreferenciaPago"		       	
				FROM "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaCuentasBancarias" AS PCB
				INNER JOIN :v_PreferenciaPago AS PP ON (1=1)				
				WHERE PCB."IdCuentaBancaria" = :p_IdCuentaBancaria
				AND   PCB."IdTipoBanco" = 'EXT' -- Cuenta bancaria de banco internacional 
				AND PP."IdMedioDePago" = 'INT'  -- Si es Transferencia interbancaria
				UNION ALL SELECT "Prioridad","Cumple","CodigoError","CodigoDato","ValorDato","IdPreferenciaPago" FROM :v_TablaRequisitos;				
		END IF;


		IF (v_LocalizacionSistema = 'CL') THEN -- Solo para CHILE
				-- Valida que para preferencias con "Transferencia" y cuenta internacionales, existan los Codigos ABA/IBAN
				v_TablaRequisitos = 
				SELECT  1 AS "Prioridad",
						CASE
							 WHEN COALESCE(PCB."CodigoAba",'') = '' AND COALESCE(PCB."OtroCodigoBancoExtranjero",'') = '' AND COALESCE(PCB."OtroCodigoCuentaExtranjero",'')  = '' THEN 0
							 WHEN COALESCE(PCB."CodigoAba",'') = '' THEN 0
							 ELSE  1
						END AS "Cumple",
						CASE 
							 WHEN COALESCE(PCB."CodigoAba",'') = '' AND COALESCE(PCB."OtroCodigoBancoExtranjero",'') = '' AND COALESCE(PCB."OtroCodigoCuentaExtranjero",'')  = '' THEN 'CUENTA_BANCARIA_SIN_CODIGOS_ABA/IBAN'
							 WHEN COALESCE(PCB."CodigoAba",'') = '' THEN 'CUENTA_BANCARIA_SIN_CODIGOS_ABA/IBAN'
							 ELSE  ''
						END AS "CodigoError",
						'NombreCuentaBancaria' AS "CodigoDato",				
				       	CAST(PCB."NombreCuentaBancariaFull" as NVARCHAR(100)) AS "ValorDato",		       	
				       	PP."IdPreferenciaPago"		       	
				FROM "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaCuentasBancarias" AS PCB
				INNER JOIN :v_PreferenciaPago AS PP ON (1=1)				
				WHERE PCB."IdCuentaBancaria" = :p_IdCuentaBancaria
				AND   PCB."IdTipoBanco" = 'EXT' -- Cuenta bancaria de banco internacional 
				AND PP."TipoMedioDePago" = 'TRA'  -- Si es tipo Transferencia
				UNION ALL SELECT "Prioridad","Cumple","CodigoError","CodigoDato","ValorDato","IdPreferenciaPago" FROM :v_TablaRequisitos;		
		END IF;



			
		
		
		IF (:p_IdPreferenciaPagoPortafolio <> 0) THEN
				v_TablaRequisitos = 
				SELECT  1 AS "Prioridad",
						CASE
							 WHEN (COUNT(PPP."IdPreferenciaPagoPortafolio") = 0) THEN 0
							 ELSE  1
						END AS "Cumple",
						CASE
							 WHEN (COUNT(PPP."IdPreferenciaPagoPortafolio") = 0) THEN 'ENTIDAD_NO_EXISTE'
							 ELSE  ''
						END AS "CodigoError",
						'IdPreferenciaPagoPortafolio' AS "CodigoDato",
				       	CAST(p_IdPreferenciaPagoPortafolio as NVARCHAR(100)) AS "ValorDato",		       	
				       	'' AS "IdPreferenciaPago"		       	
				FROM "BSCL.Portafolio.BD::POR.PreferenciaPagoPortafolio"  AS PPP
				WHERE "IdPreferenciaPagoPortafolio" = :p_IdPreferenciaPagoPortafolio
				UNION ALL SELECT "Prioridad","Cumple","CodigoError","CodigoDato","ValorDato","IdPreferenciaPago" FROM :v_TablaRequisitos;		
		END IF;
 


		
		-------------------------------------------------
		-- ANALISIS DE DATOS
		-------------------------------------------------
		

		SELECT   COUNT(CASE WHEN "Prioridad" = 1 THEN 1 END)
	   			,COUNT(CASE WHEN "Prioridad" = 2 THEN 1 END)
	   	INTO v_error_General, v_error_Negocio_ADVE
	   	FROM :v_TablaRequisitos WHERE "Cumple" = 0;
	   	
	   	
	   	p_out_resultadoRegla := CASE WHEN :v_error_General > 0      THEN 'RECH'
	   								 WHEN :v_error_Negocio_ADVE > 0 THEN 'ADVE'
	   								 ELSE 'OK'
	   							END;
	    
	    
	    v_TablaCodigosMensajes =
		SELECT 	"CodigoError" AS "CodigoMensaje"
			   ,"CodigoDato"
			   ,"ValorDato" 
		FROM :v_TablaRequisitos
		WHERE "Cumple" = 0
		ORDER By "Prioridad" DESC;
		CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionario" (:v_TablaCodigosMensajes,p_out_ErroresDetalle);

		 
		p_out_AdvertenciasDetalle =
		SELECT ED."Codigo",	
			   '' AS "RequiereAutorizacion",
			   CASE WHEN "Prioridad" > 1 THEN (ED."Descripcion")
					ELSE ED."Descripcion"
			   END AS "Descripcion"
		 FROM :p_out_ErroresDetalle AS ED
		 INNER JOIN :v_TablaRequisitos AS TR ON (TR."CodigoError" = ED."Codigo")
		 WHERE TR."Cumple" = 0
		 AND   TR."Prioridad" > 1;  -- Errores de Negocio (ADVE)
		 
		
		p_out_ErroresDetalle =
		SELECT ED."Codigo",		
			   CASE WHEN "Prioridad" > 1 THEN ED."Descripcion"
					ELSE ED."Descripcion"
			   END AS "Descripcion"
		 FROM :p_out_ErroresDetalle AS ED
		 INNER JOIN :v_TablaRequisitos AS TR ON (TR."CodigoError" = ED."Codigo")
		 WHERE TR."Cumple" = 0
		 AND   TR."Prioridad" = 1;  -- Errores generales (RECH)

		 
    END;
	
END;