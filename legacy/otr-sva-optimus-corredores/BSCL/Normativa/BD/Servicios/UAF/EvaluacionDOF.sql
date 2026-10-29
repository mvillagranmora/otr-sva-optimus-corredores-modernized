SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Normativa.BD.Servicios.UAF::EvaluacionDOF";

CREATE PROCEDURE
-- 
--  Proposito: Servicio que evalua la operacion, para determinar si reuqiere de una declaracion de los origenes de fondos. 
--  Autor: CFLORES - BCS
--  Fecha Creacion: 19-12-2016
--  
"BSCL.Normativa.BD.Servicios.UAF::EvaluacionDOF" (
	-- Parametros entrada comunes en todos los servicios publicos
	IN  p_IdEmpresa									BIGINT,			--ID Empresa destino
	IN  p_IdUsuario									BIGINT,			--ID del usuario que solicita el servicio
	IN  p_IdModulo									NVARCHAR(3),  	--ID del Modulo que lo invoca
	IN  p_UsaReglasNegocio	 						NVARCHAR(1),	-- S/N se usa en caso de servicios anidados.
	IN  p_simularOperacion							NVARCHAR(1) DEFAULT 'N', -- S/N se usa para simular operacion (algunos casos)	  
	-- Parametros entrada del servicio publico
	IN  p_IdTipoOperacion							NVARCHAR(6)	,
	IN  p_IdCliente									BIGINT,
	IN  p_MontoOperacion							DECIMAL(27,8),
	IN  p_IdMonedaOperacion							NVARCHAR(3),
	IN  p_IdMedioDePago								NVARCHAR(3),
	IN  p_FechaOperacion							NVARCHAR(10),			 --'YYYY-MM-DD'
	-- Parametros salida del servicio publico

	OUT p_out_RequiereDOF	NVARCHAR(2), --SI/NO
	OUT p_out_EvaluacionDOF	NVARCHAR(100), --Marca de Evaluacion
	-- Salidas para mensajes de advertencias y errores
	OUT p_out_AdvertenciasContador 		int,
	OUT p_out_AdvertenciasDetalle 		table ("Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(520)),	
	OUT p_out_ErroresContador 			int,
	OUT p_out_ErroresDetalle 			table ("Codigo" nvarchar(40),"Descripcion" nvarchar(520))
	
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
	DECLARE v_out_ErroresDetalle           table ("Codigo" nvarchar(40),"Descripcion" nvarchar(520));
    DECLARE v_out_AdvertenciasDetalle      table ("Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(520));	
    

	       	
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA
	BEGIN
				
			p_out_AdvertenciasContador	:= 0;
			p_out_ErroresContador		:= 0;
			v_out_AdvertenciasDetalle	= select '' as "Codigo",'' as "RequiereAutorizacion",'' as "Descripcion" from dummy where 1=2;
			v_out_ErroresDetalle		= select '' as "Codigo",'' as "Descripcion" from dummy where 1=2;
				
		--salida/error/advertencia controlado por este servicio

	--v_out_AdvertenciasDetalle 	= select * from :p_out_AdvertenciasDetalle;
	--v_out_ErroresDetalle 		= select * from :p_out_ErroresDetalle;

--p_out_AdvertenciasDetalle
--p_out_ErroresDetalle
		select count(1) into p_out_ErroresContador 
		from :v_out_ErroresDetalle;
		select count(1) into p_out_AdvertenciasContador 
		from :v_out_AdvertenciasDetalle;
		
		v_TablaCodigosMensajes = SELECT "Codigo" AS "CodigoMensaje",
								'' as "CodigoDato",
								'' as "ValorDato" 
								FROM :v_out_ErroresDetalle
								UNION ALL
								SELECT "Codigo" AS "CodigoMensaje",
								'' as "CodigoDato",
								'' as "ValorDato" 
								FROM :v_out_AdvertenciasDetalle;
		  
		CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionario" (:v_TablaCodigosMensajes,v_out_ErroresDetalleMensajes);
		--v_out_ErroresDetalleMensajes = 	SELECT '' AS "Codigo", '' AS "Descripcion" FROM DUMMY;
								
		p_out_ErroresDetalle = 	SELECT 	errores."Codigo",
									errores."Descripcion"
									--msj."Descripcion"
								FROM :v_out_ErroresDetalle errores
								INNER JOIN :v_out_ErroresDetalleMensajes msj on errores."Codigo" = msj."Codigo";
								
		p_out_AdvertenciasDetalle = SELECT 	adv."Codigo",
									adv."RequiereAutorizacion",
									adv."Descripcion"
									--msj."Descripcion"
								FROM :v_out_AdvertenciasDetalle adv
								INNER JOIN :v_out_ErroresDetalleMensajes msj on adv."Codigo" = msj."Codigo";
								
		select count(1) into p_out_ErroresContador 
		from :p_out_ErroresDetalle;
		select count(1) into p_out_AdvertenciasContador 
		from :p_out_AdvertenciasDetalle;		 									
				 					 	
	END;
	
		

	BEGIN	

		--------------------
		--Declare Variables
		--------------------
		DECLARE v_IdNormativa 						 		 	NVARCHAR(15):='UAF';
				
				
		DECLARE v_VAL_MONTO_UF_UAFDOF, v_VAL_MONTO_DOLAR_UAFDOF, v_EVAL_DOF_UAFDOF NVARCHAR(520):='';		
		DECLARE v_ValorMontoDOLAR,	v_ValorMontoUF			 	DECIMAL (26,8):=0;	
		
		DECLARE v_ParidadMontoUF,v_ParidadMontoDOLAR,v_ParidadMontoOperacion,v_montoMinimo DECIMAL (26,8):=0;	
		
		DECLARE v_IndHabitual, v_TipoPersona,v_IndPEP 	NVARCHAR(1):='';
		
		
		--variables requeridas en la creacion 
		DECLARE	v_Count,v_IdPersona,v_IdTrabajoParidad	 	 BIGINT:=0;
		DECLARE v_out_error			  			 NVARCHAR(500);

		---Tabla de registro de evaluaciones en proceso
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

			p_out_RequiereDOF		 			:= 'NO';
			p_out_EvaluacionDOF					:= '';
			----Parametro de Evaluacion
			SELECT COUNT(1) INTO v_Count
			FROM "BSCL.Normativa.BD.Vistas::NOR.ConsultarConfiguracion"
			WHERE "IdEmpresa"=p_IdEmpresa AND "CodigoParametro"='EVAL_DOF_UAFDOF' AND "IdNormativa"=v_IdNormativa;
			IF v_Count>0 THEN
				SELECT "Valor" INTO v_EVAL_DOF_UAFDOF
					FROM "BSCL.Normativa.BD.Vistas::NOR.ConsultarConfiguracion"
				WHERE "IdEmpresa"=p_IdEmpresa AND "CodigoParametro"='EVAL_DOF_UAFDOF' AND "IdNormativa"=v_IdNormativa;
				ELSE
				v_EVAL_DOF_UAFDOF:='N';
			END IF;
			
				-------------------------------------------
				--01 Parametro de Evaluacion DOF (S/N)
				-------------------------------------------			

			IF v_EVAL_DOF_UAFDOF ='N'THEN
				p_out_RequiereDOF		 			:= 'NO';
				p_out_EvaluacionDOF					:= 'parametro apagado: EVAL_DOF_UAFDOF';
				SIGNAL SALIDA_CONTROLADA;		
			ELSE
				---------------------------------------------------
				--02 Evaluacion Tipo de Operacion DOF
				--------------------------------------------------			
						
				SELECT COUNT(1) INTO v_Count
				FROM "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarTipoOperacionOrigenFondos"
				WHERE "IdEmpresa"=p_IdEmpresa AND "IdTipoOperacionOrigenFondos"=p_IdTipoOperacion;
				
				IF v_Count=0 THEN
						p_out_RequiereDOF		 	:= 'NO';
						p_out_EvaluacionDOF			:= 'TipoOperacion: no DOF';
						SIGNAL SALIDA_CONTROLADA;	
				END IF;	
				---------------------------------------------------
				--03 Evaluacion Excluir cuenta propia
				--------------------------------------------------			
				SELECT COUNT(1) INTO v_Count
				FROM "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" CLI
				INNER JOIN "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio" as SOC ON CLI."IdPersona" =SOC."IdPersona"		
				INNER JOIN "BSCL.Plataforma.BD::PLA.Empresa" EMP ON (SOC."IdSocioDeNegocio"=EMP."IdSocioDeNegocio")	
				WHERE EMP."IdEmpresa" = p_IdEmpresa AND CLI."IdCliente"=p_IdCliente
				AND CLI."IdEstado"='ACT';
			
				IF v_Count>0 THEN
						p_out_RequiereDOF		 	:= 'NO';
						p_out_EvaluacionDOF			:= 'Cliente: cta. propia';
						SIGNAL SALIDA_CONTROLADA;	
				END IF;
			
				---------------------------------------------------
				--04 Evaluacion Limite de Monto en varias Monedas
				--------------------------------------------------	
				----Parametros monto minimo
	
				SELECT COUNT(1) INTO v_Count
					FROM "BSCL.Normativa.BD.Vistas::NOR.ConsultarConfiguracion"
				WHERE "IdEmpresa"=p_IdEmpresa AND "CodigoParametro"='VAL_MONTO_UF_UAFDOF' AND "IdNormativa"=v_IdNormativa;
				
				IF v_Count>0 THEN
					SELECT "Valor" INTO v_VAL_MONTO_UF_UAFDOF
						FROM "BSCL.Normativa.BD.Vistas::NOR.ConsultarConfiguracion"
					WHERE "IdEmpresa"=p_IdEmpresa AND "CodigoParametro"='VAL_MONTO_UF_UAFDOF' AND "IdNormativa"=v_IdNormativa;
					ELSE
					v_VAL_MONTO_UF_UAFDOF:='450';
				END IF;
				v_ValorMontoUF=	CAST(TRIM(v_VAL_MONTO_UF_UAFDOF) AS DECIMAL (26,8));
				
				SELECT COUNT(1) INTO v_Count
					FROM "BSCL.Normativa.BD.Vistas::NOR.ConsultarConfiguracion"
				WHERE "IdEmpresa"=p_IdEmpresa AND "CodigoParametro"='VAL_MONTO_DOLAR_UAFDOF' AND "IdNormativa"=v_IdNormativa;
				
				IF v_Count>0 THEN
					SELECT "Valor" INTO v_VAL_MONTO_DOLAR_UAFDOF
						FROM "BSCL.Normativa.BD.Vistas::NOR.ConsultarConfiguracion"
					WHERE "IdEmpresa"=p_IdEmpresa AND "CodigoParametro"='VAL_MONTO_UF_UAFDOF' AND "IdNormativa"=v_IdNormativa;
					ELSE
					v_VAL_MONTO_DOLAR_UAFDOF:='15000';
				END IF;
				v_ValorMontoDOLAR=	CAST(TRIM(v_VAL_MONTO_DOLAR_UAFDOF) AS DECIMAL (26,8));

				---Se llevan los montos a pesos

				v_in_ObtenerParidadTipoCambio=
				SELECT	1 												AS "IdInterno"
						,TO_VARCHAR (:p_FechaOperacion, 'YYYY-MM-DD') 	AS "Fecha"
						,'UF' 										 	AS "IdMonedaOrigen"
						,'CLP' 										 	AS "IdMonedaDestino"
						, 0 										 	AS "ValorCambioLocalModificado"
						, v_ValorMontoUF	 						 	AS "MontoOrigen"
						,'ORDENES' 										AS "IdUso"
					FROM DUMMY
				UNION ALL 
				SELECT	2 												AS "IdInterno"
						,TO_VARCHAR (:p_FechaOperacion, 'YYYY-MM-DD')	AS "Fecha"
						,'USD' 										 	AS "IdMonedaOrigen"
						,'CLP' 										 	AS "IdMonedaDestino"
						, 0 										 	AS "ValorCambioLocalModificado"						
						, v_ValorMontoDOLAR	 						 	AS "MontoOrigen"
						,'ORDENES' 										AS "IdUso"
					FROM DUMMY
				UNION ALL 
				SELECT	3 AS "IdInterno"
						,TO_VARCHAR (:p_FechaOperacion, 'YYYY-MM-DD') 	AS "Fecha"
						, p_IdMonedaOperacion						 	AS "IdMonedaOrigen"
						,'CLP' 										 	AS "IdMonedaDestino"
						, 0 										 	AS "ValorCambioLocalModificado"						
						, p_MontoOperacion	 						 	AS "MontoOrigen"
						,'ORDENES' 										AS "IdUso"
					FROM DUMMY; 
					
				SELECT "BSCL.Plataforma.BD.Secuencias::MER.IdTrabajoParidad".NEXTVAL INTO v_IdTrabajoParidad FROM DUMMY;
				
				INSERT INTO "BSCL.Plataforma.BD::MER.CalcularParidadYTipoCambio_Entrada_Entrada"("IdTrabajo","IdInterno","Fecha","IdMonedaOrigen","IdMonedaDestino","ValorCambioLocalModificado","MontoOrigen","IdUso")
				SELECT :v_IdTrabajoParidad,CAR."IdInterno" AS "IdInterno",CAR."Fecha" AS "Fecha", CAR."IdMonedaOrigen" as "IdMonedaOrigen", CAR."IdMonedaDestino" as "IdMonedaDestino",
				0 as "ValorCambioLocalModificado",CAR."MontoOrigen" as "MontoOrigen",'ORDENES' as "IdUso"
				FROM :v_in_ObtenerParidadTipoCambio CAR;
				
				
				call "BSCL.Plataforma.BD.Servicios::MER.CalcularParidadYTipoCambio"(
					p_IdEmpresa => :p_IdEmpresa,
					p_IdTrabajo => :v_IdTrabajoParidad
				);
				
				v_out_Paridad=
					SELECT DISTINCT COALESCE(outt."MontoDestino",0) AS  "MontoDestino", par.*
					FROM :v_in_ObtenerParidadTipoCambio par
					LEFT JOIN "BSCL.Plataforma.BD::MER.CalcularParidadYTipoCambio_Salida_Salida" outt 
					on par."IdInterno" = outt."IdInterno" AND outt."IdTrabajo" = :v_IdTrabajoParidad;
			
				
				SELECT COUNT(1) INTO v_Count FROM :v_out_Paridad;
				
				IF v_Count > 0 THEN
					v_out_Paridad=SELECT DISTINCT * FROM  :v_out_Paridad;								
					
					v_Datos = SELECT 
							  CASE WHEN SOL."IdInterno"=1	THEN "MontoDestino" ELSE NULL END AS "ParidadMontoUF"
							, CASE WHEN SOL."IdInterno"=2	THEN "MontoDestino" ELSE NULL END AS "ParidadMontoDOLAR"
							, CASE WHEN SOL."IdInterno"=3	THEN "MontoDestino" ELSE NULL END AS "ParidadMontoOperacion"
							FROM :v_out_Paridad SOL;
					
				
					v_DatosAgrupado =SELECT  
									  MAX(SOL."ParidadMontoUF") 		AS "ParidadMontoUF"
									, MAX(SOL."ParidadMontoDOLAR")		AS "ParidadMontoDOLAR"
									, MAX(SOL."ParidadMontoOperacion")	AS "ParidadMontoOperacion"
									FROM :v_Datos SOL;	
							
							
					SELECT TOP 1 CAST(TRIM("ParidadMontoUF") AS DECIMAL (26,8)), CAST(TRIM("ParidadMontoDOLAR") AS DECIMAL (26,8)), CAST(TRIM("ParidadMontoOperacion") AS DECIMAL (26,8))
					INTO v_ParidadMontoUF,v_ParidadMontoDOLAR,v_ParidadMontoOperacion FROM :v_DatosAgrupado;
					
					
					IF p_IdMonedaOperacion='USD' OR p_IdMonedaOperacion='EUR' THEN
						v_montoMinimo=v_ParidadMontoDOLAR;
					ELSE
						v_montoMinimo=v_ParidadMontoUF;
					END if;	
					
					IF v_ParidadMontoOperacion <= v_montoMinimo THEN
						p_out_RequiereDOF		 	:= 'NO';
						p_out_EvaluacionDOF			:= 'Monto: No excedido';
						SIGNAL SALIDA_CONTROLADA;	
					ELSE --OPERACION MAYOR AL LIMITE

				---------------------------------------------------
				--05 Evaluacion Medio de Pago configurado en DOF
				--------------------------------------------------		

						SELECT COUNT(1) INTO v_Count  FROM "BSCL.Normativa.BD::UAF.MedioDePagoDOF"
						WHERE "IdEmpresa"= p_IdEmpresa AND  "IdMedioDePago"= p_IdMedioDePago AND "IdEstado"='ACT';
						
						IF v_Count = 0 THEN
							p_out_RequiereDOF		 	:= 'NO';
							p_out_EvaluacionDOF			:= 'MedioDePago: No es DOF';
							SIGNAL SALIDA_CONTROLADA;	

						ELSE
						
				---------------------------------------------------
				--06 Evaluacion Segun Medio de Pago e Indicadores
				--------------------------------------------------						
						---Indicador Habitualidad
						SELECT COALESCE(IND."ValorIndicador",'N') INTO v_IndHabitual
						FROM "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente" IND 
						WHERE (IND."IdUso"='IND' AND IND."CodigoTipoIndicador"='ESHABITUAL' AND IND."IdCliente" =p_IdCliente);
						---Tipo Persona
						SELECT PER."Tipo", PER."IdPersona" INTO v_TipoPersona,v_IdPersona
						FROM  "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio" SOC
						INNER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" PER
						ON(SOC."IdPersona"=PER."IdPersona")
						WHERE (SOC."IdCliente" =p_IdCliente);
						
						---IndicadorPEP
						SELECT COALESCE(IND."ValorIndicador",'N') INTO v_IndPEP
						FROM "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPersona" IND 
						WHERE (IND."IdUso"='IND' AND IND."CodigoTipoIndicador"='PEP' AND IND."IdPersona" =v_IdPersona);
							
						
								IF v_TipoPersona ='N' AND (v_IndHabitual='N' OR v_IndPEP ='S') THEN
									p_out_RequiereDOF		 	:= 'SI';
									p_out_EvaluacionDOF			:= 'Indicadores: Cliente es Ocasional o Persona está Expuesta Politicamente';
									SIGNAL SALIDA_CONTROLADA;
								ELSE
									--- * CALCULO DE HABITUALIDAD *
									p_out_RequiereDOF		 	:= 'NO';
									p_out_EvaluacionDOF			:= 'Indicadores: Cliente es Habitual o Persona no está Expuesta Politicamente';
									SIGNAL SALIDA_CONTROLADA;	
								END IF;
					
						END IF;
					END IF;---monto
				ELSE
					p_out_RequiereDOF		 	:= 'NO';
					p_out_EvaluacionDOF			:= 'Paridades: no encontradas';
					SIGNAL SALIDA_CONTROLADA;	
					
				END IF;
		
				
			END IF;
			

			SIGNAL SALIDA_CONTROLADA;		
		END;		
					
		--------------------
		--FIN SP 
		--------------------
	END;	
END;