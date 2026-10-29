SET SCHEMA "BSCL";
GO
DROP PROCEDURE "BSCL.Plataforma.BD.Servicios::LIM.EvaluacionLimite";
GO
CREATE PROCEDURE
-- 
--  Proposito: Servicio que obtiene informacion de las evaluaciones a Crear, 
--  Autor: CFLORES - BCS
--  Fecha Creacion: 04-05-2016
--  
"BSCL.Plataforma.BD.Servicios::LIM.EvaluacionLimite" (
	-- Parametros entrada comunes en todos los servicios publicos
	IN  p_IdEmpresa									BIGINT,			--ID Empresa destino
	IN  p_IdUsuario									BIGINT,			--ID del usuario que solicita el servicio
	IN  p_IdModulo									NVARCHAR(3),  	--ID del Modulo que lo invoca
	IN  p_UsaReglasNegocio	 						NVARCHAR(1),	-- S/N se usa en caso de servicios anidados.
	IN  p_simularOperacion							NVARCHAR(1) DEFAULT 'N', -- S/N se usa para simular operacion (algunos casos)	  
	-- Parametros entrada del servicio publico
	IN p_tablaOperacion 		TABLE("IdPortafolio"					BIGINT,
									  "IdCliente"						BIGINT,
									  "MontoUtilizado"          		DECIMAL(26,8),
									  "IdMoneda"                		NVARCHAR(3),
									  "TipoCambio"						DECIMAL(26,8),
									  "IdOperacion" 					BIGINT, 
									  "IdTipoOperacion"        			NVARCHAR(6),
									  "IdTipoInstrumento"       		NVARCHAR(3),
									  "IdClasificacionTipoInstrumento" 	NVARCHAR(3),
									  "IdTipoReajuste"          		NVARCHAR(3),
									  "IdInstrumento"          			BIGINT,
									  "IdEmisor"      		    		NVARCHAR(40),
									  "Tasa"                    		DECIMAL (26,8),
									  "DiasPlazo"               		INTEGER,
									  "FechaOperacion"					NVARCHAR(10),--'YYYY-MM-DD'
									  "CodigoOperador"					NVARCHAR(40),
									  "IdAgente"						BIGINT,
									  "IdCanalIngreso" 					NVARCHAR(3)
									  ),
	
	IN p_IdTipoEntidad		 	NVARCHAR(40),---ORDEN, SPOT, FORWARD
	IN p_GeneraAutorizacion	 	NVARCHAR(2)		DEFAULT 'N', --- En 'N' el evento lo genera el Negocio.
	
	-- Parametros salida del servicio publico
	OUT p_out_RegistroEvaluacionLimite	table (		"IdOperacion" 				 		BIGINT,
													"IdTipoOperacion" 			 		NVARCHAR(6),
													"IdTipoInstrumento"       	 		NVARCHAR(3),
													"IdClasificacionTipoInstrumento" 	NVARCHAR(3),
													"MontoUtilizado"             		DECIMAL(26,8),													
													"IdMoneda" 					 		NVARCHAR(3),
													"IdLimite"                  		BIGINT,
													"IdTipoLimite"                		NVARCHAR(40),
													"MontoExcedido"              		DECIMAL(26,8),
													"ConceptoExceso" 			 		NVARCHAR(520),
													"IdEstado"                   		NVARCHAR(3),
													"IdEstadoAutorizacion" 		 		NVARCHAR(3),
													"IdTipoEvento"       		 		NVARCHAR(6),
													"Prioridad"					 		BIGINT,
													"ConceptoEvaluacion" 		 		NVARCHAR(40),
													"MontoLimite"             		    DECIMAL(26,8),
													"MontoCalculado"           		    DECIMAL(26,8),
													"IdMonedaCalculo"   		   		NVARCHAR(3),
													"IdTipoEntidad"         	 		NVARCHAR(40),
													"IdCliente" 						BIGINT, 
													"IdPortafolio" 						BIGINT,
									  				"IdCanalIngreso" 					NVARCHAR(3)), ---Detalle resultado de limites
	OUT p_out_EstadoOperacion	table (	"IdOperacion" 				 		BIGINT,
										"IdEstado"                   		NVARCHAR(3)),	---Resume el estado segun limite con mayor prioridad		
    OUT p_out_idAuthtable 				table ("IdEvento" BIGINT,"NumeroOperacionConcepto" 	NVARCHAR(40)),
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
	       	
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA
	BEGIN

		select count(1) into p_out_ErroresContador 
		from :p_out_ErroresDetalle;
		select count(1) into p_out_AdvertenciasContador 
		from :p_out_AdvertenciasDetalle;
		
		v_TablaCodigosMensajes = SELECT "Codigo" AS "CodigoMensaje",
								'' as "CodigoDato",
								'' as "ValorDato" 
								FROM :p_out_ErroresDetalle
								UNION ALL
								SELECT "Codigo" AS "CodigoMensaje",
								'' as "CodigoDato",
								'' as "ValorDato" 
								FROM :p_out_AdvertenciasDetalle;
		  
		CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionario" (:v_TablaCodigosMensajes,p_out_ErroresDetalleMensajes);
		--p_out_ErroresDetalleMensajes = 	SELECT '' AS "Codigo", '' AS "Descripcion" FROM DUMMY;
								
		p_out_ErroresDetalle = 	SELECT 	errores."Codigo",
									errores."Descripcion"
									--msj."Descripcion"
								FROM :p_out_ErroresDetalle errores
								INNER JOIN :p_out_ErroresDetalleMensajes msj on errores."Codigo" = msj."Codigo";
								
		p_out_AdvertenciasDetalle = SELECT 	adv."Codigo",
									adv."RequiereAutorizacion",
									adv."Descripcion"
									--msj."Descripcion"
								FROM :p_out_AdvertenciasDetalle adv
								INNER JOIN :p_out_ErroresDetalleMensajes msj on adv."Codigo" = msj."Codigo";
								
		select count(1) into p_out_ErroresContador 
		from :p_out_ErroresDetalle;
		select count(1) into p_out_AdvertenciasContador 
		from :p_out_AdvertenciasDetalle;		 									
				 					 	
	END;
	p_out_AdvertenciasContador := 0;
    p_out_ErroresContador       := 0;
    p_out_AdvertenciasDetalle   = select '' as "Codigo",'' as "RequiereAutorizacion",'' as "Descripcion" from dummy where 1=2;
    p_out_ErroresDetalle        = select '' as "Codigo",'' as "Descripcion" from dummy where 1=2;
		

	BEGIN	

-------- lista de Limites
--PORTAFOLIO
--(A)	LIMITE POR SOBREGIRO
--(B)	LIMITE POR PRODUCTO (PLANTILLA TIPO INSTRUMENTO / OPERACION )

		--CUENTA PROPIA
--RV, RF
--(A)	LIMITE CUENTA PROPIA	:  EXCLUIR EMISOR		
--(B)	LIMITE CUENTA PROPIA	:  CLASIFICACION DE RIESGO
--(C)	LIMITE CUENTA PROPIA	:  MARGEN DE TASAS
--(D)	LIMITE CUENTA PROPIA	:  PLAZO

--(E)	LIMITE CUENTA PROPIA	:  SPOT : POSICION INTERBANCARIA (INTRADAY/OVERNIGTH)
--(F)	LIMITE CUENTA PROPIA	:  SPOT : MESA DISTRIBUCION
--(G)	LIMITE CUENTA PROPIA	:  LIMITE FORWARD 
--(H)	LIMITE CUENTA PROPIA	:  CARTERA
--(I)	LIMITE CUENTA PROPIA	:  AGENTE
--(J)	LIMITE CUENTA PROPIA	:  GRUPO EMISOR
--(K)	LIMITE CUENTA PROPIA	:  EMISOR

		---Definiciones

---Tabla : Limites campo IdMoneda es igual a la Moneda de la Operacion para hacer coincidir en las busquedas
---Tabla : Limites campo IdMonedaCalculo es igual a la Moneda del limite, el monto de la operacion debe ser llevado a esta moneda 
--			para hacer comparacion con el limite.



		--------------------
		--Declare Variables
		--------------------
		DECLARE v_tipoEntidad									NVARCHAR(40);--para la autorizacion
		DECLARE v_IdConcepto	       							NVARCHAR(6);
		DECLARE v_Evento      									BIGINT:=1;
		DECLARE v_ConceptoExceso				  				NVARCHAR(520):='';
		DECLARE v_MontoExcedido,v_MontoUtilizado,v_MontoTotal	DECIMAL(26,8) :=0;
		DECLARE v_IdComportamiento							 	NVARCHAR(3):='';
		DECLARE v_estadoAutOK,v_estadoAutNoOK, v_estadoOK,  v_estadoPEN NVARCHAR(3):='';
		DECLARE v_out_IdSecuencias 								table ("IdSecuencia" BIGINT);
		
	    DECLARE v_out_AdvertenciasContador, v_out_ErroresContador INT;

		DECLARE v_fechaHoraSistema NVARCHAR(40);
		
		
		--variables requeridas en la creacion 
		DECLARE	v_IdEvaluacionLimite,v_IdTrabajoParidad 			 BIGINT:=0;
		DECLARE	v_IdClienteCtaPropia 			 BIGINT:=0;
		DECLARE v_DocumentoIdentidadCtaPropia 	 NVARCHAR(40);
		DECLARE	v_NombreEvento 					 NVARCHAR(100):='';
		DECLARE	v_Count,v_CountParidad		 	 BIGINT:=0;
		DECLARE	v_Count_Limites, v_Count_LimitesCtaPropia BIGINT:=0;
		DECLARE	v_Count_Eval_Con_AUT 			 BIGINT:=0;
		DECLARE	V_Eval_TipoLimite				 NVARCHAR(40):='';
		DECLARE v_DescripcionModulo				 NVARCHAR(100):='';
		DECLARE v_out_error			  			 NVARCHAR(500);
		DECLARE v_ConceptoEvaluacion			 NVARCHAR(40):='';

		
		---Tabla de registro de evaluaciones en proceso
		DECLARE v_Tab_EvaluacionPRO table ("IdInterno"                    		BIGINT,			--EVAL
										   "IdLimite"                    		BIGINT,			--EVAL
										   "IdTipoLimite"                		NVARCHAR(40),		--EVAL
										   "IdTipoOperacion" 			 		NVARCHAR(6),		--OPER
										   "IdOperacion" 				 		BIGINT,			--OPER
										   "IdCliente"							BIGINT,			--OPER
										   "IdPortafolio"						BIGINT,			--OPER
										   "IdTipoInstrumento"      	 		NVARCHAR(3),		--OPER
										   "IdClasificacionTipoInstrumento" 	NVARCHAR(3),		--OPER
										   "IdTipoReajuste"         	 		NVARCHAR(3),		--OPER
										   "IdInstrumento"          	 		BIGINT,			--OPER
										   "IdEmisor"     				 		NVARCHAR(40),		--OPER
										   "Tasa"                    	 		DECIMAL (26,8),	--OPER
								  		   "DiasPlazo"               	 		INTEGER,			--OPER
										   "FechaOperacion"				 		NVARCHAR(10),		--OPER
										   "CodigoOperador"				 		NVARCHAR(40),		--OPER									   
										   "IdMoneda" 					 		NVARCHAR(3),		--OPER
										   "TipoCambio"				     		DECIMAL(26,8),		--OPER
										   "ConceptoExceso" 			 		NVARCHAR(520),		--EVAL
										   "MontoExcedido"               		DECIMAL(26,8),		--EVAL
										   "MontoUtilizado"              		DECIMAL(26,8),		--OPER
										   "IdEstado"                    		NVARCHAR(3),		--EVAL
										   "IdEstadoAutorizacion" 		 		NVARCHAR(3),		--EVAL
										   "IdAgente"					 		BIGINT,			--OPER
										   "IdCanalIngreso"						NVARCHAR(3),	--OPER
										   "AccionCustodia"					 	NVARCHAR(40),	--EVAL
										   "LIM_IdComportamiento" 			NVARCHAR(3),  --TPO LIMITE
										   "LIM_IdTipoEvento"      			NVARCHAR(6),
										   "LIM_Prioridad"					BIGINT,
										   "LIM_IdInstrumento"          	BIGINT,       --LIMITES
										   "LIM_UnidadPlazo"                NVARCHAR(1),
										   "LIM_Plazo"                      INTEGER,
										   "LIM_IdTipoReajuste"             NVARCHAR(3),
										   "LIM_CodigoOperador"				NVARCHAR(40),
										   "LIM_MontoTotal"                 DECIMAL (26,8),
										   "LIM_MontoIntraday"              DECIMAL (26,8),
										   "LIM_MontoOvernight"             DECIMAL (26,8),
										   "LIM_IdMoneda"          		    NVARCHAR(3),
										   "LIM_IdMonedaCalculo"   		    NVARCHAR(3),
										   "LIM_ClasificacionRiesgoMinima"	NVARCHAR(20),
										   "LIM_EsPersonalizada"			NVARCHAR(1),
										   "LIM_FechaVigencia"				DATE,
										   "LIM_EsTotalizador"		        NVARCHAR(1),
										   "LIM_IdMesa"                     BIGINT,
										   "LIM_IdCanalIngreso" 			NVARCHAR(3),
										   "LIM_IdTipoOperacion"           	NVARCHAR(6),
										   "LIM_IdTipoInstrumento"			NVARCHAR(3),
										   "LIM_IdClasificacionTipoInstrumento" NVARCHAR(3),
										   "LIM_IdClasificacionRiesgo"		BIGINT,
										   "LIM_IdAgente"					BIGINT,
										   "LIM_IdGrupoEmisor"				NVARCHAR(40),
										   "LIM_IdEmisor"   		 		NVARCHAR(40)		
										   --"DocumentoIdentidad"				NVARCHAR(40)
										   );
										   
		DECLARE v_Tab_Operacion table ("IdOperacion" 				 	BIGINT);
		DECLARE v_IdEntidadClasificadoraRiesgo       					NVARCHAR(10);
		
		DECLARE V_LIMCTPEXCLUIREMISOR,V_LIMCTPCLASIFRIESGO,V_LIMCTPMARGENTASAS,V_LIMCTPPLAZO,V_LIMCTPPOSICIONINTERBANCARIO,V_LIMCTPMESADISTRIBUCION
				,V_LIMCTPFORWARD,V_LIMCTPCARTERA,V_LIMCTPAGENTE,V_LIMCTPGRUPOEMISOR,V_LIMCTPEMISOR	NVARCHAR(1):='N';
		
	    p_out_AdvertenciasDetalle   = select '' as "Codigo",'' as "RequiereAutorizacion",'' as "Descripcion" from dummy where 1=2;
        p_out_ErroresDetalle        = select '' as "Codigo",'' as "Descripcion" from dummy where 1=2;

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
		
			v_estadoAutOK		 			:= 'ING';
			v_estadoAutNoOK		 			:= 'PEN';
			v_estadoOK			 			:= 'ACP';
			--v_estadoNoOK			 		:= 'ADV';--Se puede obtener desde el tipo de limite (ADV,RCZ)
			v_estadoPEN			 			:= 'PEN';
			v_tipoEntidad					:= 'EVALUACIONLIMITE';
			v_IdConcepto					:= 'EVALIM';
			
			v_Tab_SalidaEvaluaciones	= select * from :p_out_RegistroEvaluacionLimite where 1=2;
			v_Tab_EvaluacionOK  		= select * from :p_out_RegistroEvaluacionLimite where 1=2;
			
			p_out_AdvertenciasContador	:= 0;
			p_out_ErroresContador		:= 0;
			p_out_AdvertenciasDetalle	= select '' as "Codigo",'' as "RequiereAutorizacion",'' as "Descripcion" from dummy where 1=2;
			p_out_ErroresDetalle		= select '' as "Codigo",'' as "Descripcion" from dummy where 1=2;
				
			--call "BSCL.Plataforma.BD.Servicios::Depuracion"(1,CURRENT_UTCTIMESTAMP,1,'BSCL.Plataforma.BD.Servicios::LIM.EvaluacionLimite',p_IdTipoEntidad,'INICIO LLAMADA');
			
			call "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema"(
				p_IdEmpresa => p_IdEmpresa,
				p_out_FechaHoraSistema => v_fechaHoraSistema
			);
			
			SELECT count(1) into v_Count FROM :p_tablaOperacion; 
			IF :v_Count = 0 THEN
				SIGNAL SALIDA_CONTROLADA;
			END IF;
			
			SELECT COALESCE("Valor",'STDANDPOOR') INTO v_IdEntidadClasificadoraRiesgo
			FROM "BSCL.Plataforma.BD::LIM.Configuracion"
			WHERE "IdEmpresa"=p_IdEmpresa AND "Codigo"='ENTIDADCLASIFICADORARIESGO';
			
			v_IdEntidadClasificadoraRiesgo:= COALESCE(v_IdEntidadClasificadoraRiesgo,'STDANDPOOR');
			
			-- Se verifica si el Modulo de las Operaciones está excluido de Evaluación
			SELECT COUNT(1) INTO v_Count 
			FROM "BSCL.Plataforma.BD::LIM.ExcluirLimite"
			WHERE "IdModulo"=p_IdModulo AND "IdEstado" = 'ACT'
			 AND "IdTipoInstrumento"='' AND "IdEmpresa"=p_IdEmpresa;
			
			IF :v_Count > 0 THEN
				/*Se agregan operaciones que no cumplieron con los criterios para ser evaluados en la salida como OK */
				v_ConceptoEvaluacion:='';
				v_Tab_EvaluacionOK=
					SELECT	OPE."IdOperacion",
							OPE."IdTipoOperacion",
							OPE."IdTipoInstrumento",
							OPE."IdClasificacionTipoInstrumento",
							OPE."MontoUtilizado",
							'' AS "IdMoneda",
							0 AS "IdLimite",
							'' AS "IdTipoLimite",
							0 AS "MontoExcedido",
							'' AS "ConceptoExceso",
							v_estadoOK AS "IdEstado",
							v_estadoAutOK AS "IdEstadoAutorizacion",
							'' AS "IdTipoEvento",
							0 AS "Prioridad",
							--'' AS "DocumentoIdentidad",
							v_ConceptoEvaluacion AS "ConceptoEvaluacion",
							0 AS "MontoLimite",
							0 AS "MontoCalculado",
							'' AS"IdMonedaCalculo",
							p_IdTipoEntidad		AS "IdTipoEntidad",
							OPE."IdCliente",
							OPE."IdPortafolio",
							OPE."IdCanalIngreso"
					FROM :p_tablaOperacion OPE;
				
				v_Tab_SalidaEvaluaciones=SELECT * FROM :v_Tab_EvaluacionOK UNION ALL SELECT * FROM :v_Tab_SalidaEvaluaciones;
				p_out_RegistroEvaluacionLimite = SELECT * from :v_Tab_SalidaEvaluaciones;
				
				p_out_EstadoOperacion=	SELECT  IFNULL(PEN."IdOperacion",ACP."IdOperacion") as "IdOperacion",
												IFNULL(PEN."IdEstado",ACP."IdEstado") as "IdEstado"
												FROM       	
													(SELECT DISTINCT "IdOperacion","IdEstado" FROM :p_out_RegistroEvaluacionLimite
														WHERE "IdEstado"='ACP'
													) AS ACP
												FULL JOIN		
													( SELECT  DISTINCT EVA."IdOperacion",EVA."IdEstado" from :p_out_RegistroEvaluacionLimite EVA 
														LEFT OUTER JOIN "BSCL.Plataforma.BD::LIM.EstadoLim" EST
																			ON (EVA."IdEstado"= EST."IdEstado")
															INNER JOIN (SELECT EVL."IdOperacion", MAX(COALESCE(EST."Prioridad",0)) AS "Prioridad"
																		 FROM :p_out_RegistroEvaluacionLimite EVL
																		 LEFT OUTER JOIN "BSCL.Plataforma.BD::LIM.EstadoLim" EST
																			ON (EVL."IdEstado"= EST."IdEstado")
																		 WHERE EVL."IdEstado"<>'ACP'
																		 GROUP BY EVL."IdOperacion" ) AS GRU
															ON (EVA."IdOperacion"=GRU."IdOperacion" AND COALESCE(EST."Prioridad",0)=GRU."Prioridad")
															WHERE EVA."IdEstado"<>'ACP'
													) AS PEN	
												ON (ACP."IdOperacion"= PEN."IdOperacion");	
					
				SIGNAL SALIDA_CONTROLADA;
			END IF;
		
		
			/*INICIO: APLICAR EVALUACION DE LIMITES*/
					
			---Valores de la cuenta propia	
			SELECT CLI."IdCliente", CLI."DocumentoIdentidad" INTO v_IdClienteCtaPropia,v_DocumentoIdentidadCtaPropia
			FROM "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" CLI
			--INNER JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa" EMP ON (CLI."IdPersona"=EMP."IdPersona")
			INNER JOIN "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio" as SOC ON CLI."IdPersona" =SOC."IdPersona"		
			INNER JOIN "BSCL.Plataforma.BD::PLA.Empresa" EMP ON (SOC."IdSocioDeNegocio"=EMP."IdSocioDeNegocio")	
			WHERE EMP."IdEmpresa" = p_IdEmpresa
			AND CLI."IdEstado"='ACT';
		
/********************************************  EVALUACION DE LIMITES   ***************************************************************************/		

			---EXISTENCIA DE LIMITES A NIVEL DE PORTAFOLIO (No aplica para la cta Propia)
				
			--1) Se buscan registros que tienen Limites (Estos deben pasar por toda la evaluacion de Limites configurados 
	
			--Obtener limites aplicables a las operaciones
			v_Tab_EvaluacionPRO =
				SELECT	ROW_NUMBER() OVER() AS "IdInterno",
						LIM."IdLimite",
						TPO."IdTipoLimite", 
						OPE."IdTipoOperacion",
						OPE."IdOperacion",
						OPE."IdCliente",
						OPE."IdPortafolio",
						OPE."IdTipoInstrumento",
						OPE."IdClasificacionTipoInstrumento",
						OPE."IdTipoReajuste",
						OPE."IdInstrumento",
						OPE."IdEmisor",
						OPE."Tasa",
						OPE."DiasPlazo",
						OPE."FechaOperacion",
						OPE."CodigoOperador",
						OPE."IdMoneda",
						OPE."TipoCambio",
						'' AS "ConceptoExceso",
						0 AS "MontoExcedido",
						OPE."MontoUtilizado",
						v_estadoOK AS "IdEstado",
						v_estadoAutOK AS "IdEstadoAutorizacion",
						OPE."IdAgente",
						OPE."IdCanalIngreso",
						''	AS "AccionCustodia",
						TPO."IdComportamiento"  AS "LIM_IdComportamiento" ,
						TPO."IdTipoEvento"  	AS "LIM_IdTipoEvento",
						TPO."Prioridad"  		AS "LIM_Prioridad",
	 				    LIM."IdInstrumento" 	AS "LIM_IdInstrumento",
						LIM."UnidadPlazo"		AS "LIM_UnidadPlazo",
						LIM."Plazo"				AS "LIM_Plazo",
						LIM."IdTipoReajuste"	AS "LIM_IdTipoReajuste",
						LIM."CodigoOperador"	AS "LIM_CodigoOperador",
						LIM."MontoTotal"		AS "LIM_MontoTotal",
						LIM."MontoIntraday"		AS "LIM_MontoIntraday",
						LIM."MontoOvernight"	AS "LIM_MontoOvernight",
						LIM."IdMoneda"			AS "LIM_IdMoneda",
						LIM."IdMonedaCalculo" AS "LIM_IdMonedaCalculo",					
						LIM."ClasificacionRiesgoMinima"	AS "LIM_ClasificacionRiesgoMinima",
						LIM."EsPersonalizada"	AS "LIM_EsPersonalizada",
						LIM."FechaVigencia"		AS "LIM_FechaVigencia",
						LIM."EsTotalizador" 	AS "LIM_EsTotalizador",
						LIM."IdMesa"			AS "LIM_IdMesa",
						LIM."IdCanalIngreso"	AS "LIM_IdCanalIngreso",
						LIM."IdTipoOperacion"	AS "LIM_IdTipoOperacion",
						LIM."IdTipoInstrumento"	AS "LIM_IdTipoInstrumento",
						LIM."IdClasificacionTipoInstrumento" AS "LIM_IdClasificacionTipoInstrumento",
						0						AS "LIM_IdClasificacionRiesgo",
						LIM."IdAgente"			AS "LIM_IdAgente",
						LIM."IdGrupoEmisor"		AS "LIM_IdGrupoEmisor",
						''						AS "LIM_IdEmisor"
						--CLI."DocumentoIdentidad" AS "DocumentoIdentidad"
				FROM :p_tablaOperacion OPE 
				INNER JOIN "BSCL.Plataforma.BD.Vistas::LIM.ConsultarLimitePortafolio" LIM
					ON (OPE."IdPortafolio"=LIM."IdPortafolio" 
					AND OPE."IdMoneda"=LIM."IdMoneda" --AND OPE."IdTipoOperacion"=LIM."IdTipoOperacion" AND OPE."IdTipoInstrumento"=LIM."IdTipoInstrumento"
					)
				INNER JOIN "BSCL.Plataforma.BD::LIM.TipoLimite" TPO
					ON (TPO."IdTipoLimite"=LIM."IdTipoLimite")
				LEFT JOIN "BSCL.Plataforma.BD::LIM.ExcluirLimite" EXLIM
					ON (EXLIM."IdModulo"=p_IdModulo 
					AND EXLIM."IdEmpresa"=p_IdEmpresa 
					AND EXLIM."IdTipoInstrumento"=OPE."IdTipoInstrumento"
					AND EXLIM."IdEstado" = 'ACT')
				WHERE TPO."IdEstado"='ACT'
				  --AND OPE."IdCliente" <> v_IdClienteCtaPropia ---No sea Cta Propia
				  AND EXLIM."IdTipoInstrumento" IS NULL;
			SELECT COUNT(1) INTO v_Count_Limites FROM :v_Tab_EvaluacionPRO;
				
			IF v_Count_Limites>0 THEN
	
----------------(A)	LIMITE POR SOBREGIRO

				V_Eval_TipoLimite:='LIMPORTSOBREGIRO';
				v_ConceptoEvaluacion:='MontoUtilizado';
				v_Tab_SalidaEvaluaciones=
					SELECT	0 AS "IdOperacion",
							'' AS "IdTipoOperacion",
							'' AS "IdTipoInstrumento",
							'' AS "IdClasificacionTipoInstrumento",
							PRO."MontoUtilizado",
							PRO."IdMoneda",
							PRO."IdLimite",
							PRO."IdTipoLimite" AS "IdTipoLimite",
							(CASE WHEN PRO."MontoUtilizado" > PRO."LIM_MontoTotal" THEN (PRO."MontoUtilizado" - PRO."LIM_MontoTotal") 
								  ELSE 0 END) AS "MontoExcedido", ---VALIDACION
							PRO."ConceptoExceso",
							(CASE WHEN PRO."MontoUtilizado" > PRO."LIM_MontoTotal" THEN (CASE WHEN PRO."LIM_IdComportamiento" ='AUT' THEN v_estadoPEN ELSE PRO."LIM_IdComportamiento" END)
								  ELSE PRO."IdEstado" END) AS "IdEstado",
							---Siempre va OK con autorizacion, (se determina segun combinatoria en la vista)
							(CASE WHEN PRO."MontoUtilizado" > PRO."LIM_MontoTotal" THEN (CASE WHEN PRO."LIM_IdComportamiento" ='AUT' THEN v_estadoAutNoOK ELSE PRO."IdEstadoAutorizacion" END)
								  ELSE PRO."IdEstadoAutorizacion" END) AS "IdEstadoAutorizacion",
							PRO."LIM_IdTipoEvento" AS "IdTipoEvento",
							PRO."LIM_Prioridad" AS "Prioridad",
							--PRO."DocumentoIdentidad",
							v_ConceptoEvaluacion AS "ConceptoEvaluacion",
							PRO."LIM_MontoTotal" AS "MontoLimite",
							0 as "MontoCalculado",
							PRO."LIM_IdMoneda" AS "IdMonedaCalculo",
							p_IdTipoEntidad		AS "IdTipoEntidad",
							PRO."IdCliente",
							PRO."IdPortafolio",
							PRO."IdCanalIngreso"
					FROM :v_Tab_EvaluacionPRO PRO 
					WHERE PRO."IdTipoLimite"=V_Eval_TipoLimite; 
					--UNION ALL SELECT * FROM :v_Tab_SalidaEvaluaciones;
					
				
----------------(B)	LIMITE POR PRODUCTO (PLANTILLA TIPO INSTRUMENTO / OPERACION )
				
				V_Eval_TipoLimite:='LIMPORTPRODUCTO';
				v_ConceptoEvaluacion:='MontoLimite#MontoCalculado';
				
				---Se evalua el % del monto de la operacion por sobre el patrimonio
				---Si el Limite tiene % se evalua solo % aunque tenga monto ingresado
				---Se evalua Monto cunado el limite no tiene % ingresado

			v_Tab_EvaluacionPROPLANTILLA = SELECT *
			FROM :v_Tab_EvaluacionPRO PRO
			WHERE PRO."IdTipoLimite"=V_Eval_TipoLimite
				 AND (PRO."IdTipoOperacion"				= PRO."LIM_IdTipoOperacion"		OR PRO."LIM_IdTipoOperacion"	='')
				 AND (PRO."IdTipoInstrumento"				= PRO."LIM_IdTipoInstrumento"	OR PRO."LIM_IdTipoInstrumento"	='')
  				 AND PRO."IdClasificacionTipoInstrumento"	= PRO."LIM_IdClasificacionTipoInstrumento"
  				 AND (PRO."IdCanalIngreso"= PRO."LIM_IdCanalIngreso" OR PRO."LIM_IdCanalIngreso"='');-- OR PRO."LIM_IdCanalIngreso"=0);
  				 

			--Verificar si existen operaciones para el Limite (TipoOperacion + TipoInstrumento + CodigoOperador + IdMoneda )
			SELECT COUNT(1) INTO v_Count
			FROM :v_Tab_EvaluacionPROPLANTILLA;
  				 
				IF v_Count > 0 THEN

						v_tablaOperaciones=SELECT "IdPortafolio",
												  "IdCliente",
												  "MontoUtilizado",
												  "IdMoneda",
												  "TipoCambio",
												  "IdOperacion",
												  "IdTipoOperacion",
												  "IdTipoInstrumento",
												  "IdClasificacionTipoInstrumento",
												  "IdTipoReajuste",
												  "IdInstrumento",
												  "IdEmisor",
												  "Tasa",
												  "DiasPlazo",
												  "FechaOperacion",
												  "CodigoOperador",
												  "IdAgente",
												  "IdCanalIngreso"
									   FROM :p_tablaOperacion;
		
		
						CALL "BSCL.Plataforma.BD.Servicios::LIM.ConsultarMontoUtilizado"(
							p_IdEmpresa                   => p_IdEmpresa,
							p_IdUsuario					  => p_IdUsuario,
							p_IdModulo					  => p_IdModulo,
							p_UsaReglasNegocio			  => p_UsaReglasNegocio,
							p_simularOperacion			  => p_simularOperacion,
							p_tablaOperaciones 			  => :v_tablaOperaciones,
							p_IdTipoEntidad				  => p_IdTipoEntidad,
							p_Fecha						  => TO_VARCHAR (:v_fechaHoraSistema, 'YYYY-MM-DD'),
							p_out_tablaMontoUtilizado     => v_tablaUtilizados);
			
						--Pasar Monto Disponible de las carteras a Moneda de los Limites
						v_Monedas_limites=
							SELECT DISTINCT PRO."IdMoneda" as "MonedaDestino"
							FROM :v_Tab_EvaluacionPROPLANTILLA PRO;
	
						V_TotalDisponiblesClientes=
							SELECT ROW_NUMBER() OVER() AS "IdInterno",FLU.*, MON."MonedaDestino"
							FROM :v_tablaUtilizados FLU,:v_Monedas_limites MON;
						
						
						v_in_ObtenerParidadTipoCambio=
						SELECT	CAR."IdInterno" AS "IdInterno",TO_VARCHAR (:v_fechaHoraSistema, 'YYYY-MM-DD') as "Fecha", CAR."IdMoneda" as "IdMonedaOrigen",CAR."MonedaDestino" AS "IdMonedaDestino",0 AS "ValorCambioLocalModificado"
						, CAR."MontoUtilizado" as "MontoOrigen",'ORDENES' AS "IdUso"
						FROM :V_TotalDisponiblesClientes CAR;	
							
						SELECT "BSCL.Plataforma.BD.Secuencias::MER.IdTrabajoParidad".NEXTVAL INTO v_IdTrabajoParidad FROM DUMMY;
						
						INSERT INTO "BSCL.Plataforma.BD::MER.CalcularParidadYTipoCambio_Entrada_Entrada"("IdTrabajo","IdInterno","Fecha","IdMonedaOrigen","IdMonedaDestino","ValorCambioLocalModificado","MontoOrigen","IdUso")
						SELECT :v_IdTrabajoParidad,CAR."IdInterno" AS "IdInterno",CAR."Fecha" AS "Fecha", CAR."IdMonedaOrigen" as "IdMonedaOrigen", CAR."IdMonedaDestino" as "IdMonedaDestino",
						0 as "ValorCambioLocalModificado",CAR."MontoOrigen" as "MontoOrigen",'ORDENES' as "IdUso"
						FROM :v_in_ObtenerParidadTipoCambio CAR;
						
						call "BSCL.Plataforma.BD.Servicios::MER.CalcularParidadYTipoCambio"(
							p_IdEmpresa => :p_IdEmpresa,
							p_IdTrabajo => :v_IdTrabajoParidad
						);	
							
						v_out_ParidadDisponibles=
							SELECT DISTINCT COALESCE(outt."MontoDestino",0) AS  "MontoDestino", par.*
							FROM :v_in_ObtenerParidadTipoCambio par
							LEFT JOIN "BSCL.Plataforma.BD::MER.CalcularParidadYTipoCambio_Salida_Salida" outt 
							on par."IdInterno" = outt."IdInterno" AND outt."IdTrabajo" = :v_IdTrabajoParidad;

						
						v_out_ParidadDisponibles=SELECT DISTINCT * FROM  :v_out_ParidadDisponibles;					
						---Igualar montos de Paridades, Sumar monto segun Cliente, Clasificacion, tipo Instrumento, instrumento
						v_MontoDisponibleParidadLM=
							SELECT SUM(PAR."MontoDestino") AS "MontoUtilizado", CAR."IdCliente",CAR."IdPortafolio",CAR."IdClasificacionTipoInstrumento", CAR."IdTipoInstrumento",PAR."IdMonedaDestino"
							FROM :V_TotalDisponiblesClientes CAR 
							INNER JOIN :v_out_ParidadDisponibles PAR
							--ON (CAR."IdMoneda"= PAR."IdMonedaOrigen" AND  CAR."MontoUtilizado"= PAR."MontoOrigen" AND CAR."MonedaDestino"= PAR."IdMonedaDestino")
							ON (CAR."IdInterno"= PAR."IdInterno")
							GROUP BY CAR."IdCliente",CAR."IdPortafolio",CAR."IdClasificacionTipoInstrumento", CAR."IdTipoInstrumento",PAR."IdMonedaDestino";
							
					---CFLORES: Modificacion temporal evaluara el monto directo de la operacion si no hay nafa en custodia
					--COALESCE(PAR."MontoUtilizado",0) por 	COALESCE(PAR."MontoUtilizado",OPE."MontoUtilizado")
					v_Tab_SalidaEvaluaciones=
						SELECT	PRO."IdOperacion",
								PRO."IdTipoOperacion",
								PRO."IdTipoInstrumento",
								PRO."IdClasificacionTipoInstrumento",
								PRO."MontoUtilizado",
								PRO."IdMoneda",
								PRO."IdLimite",
								PRO."IdTipoLimite" AS "IdTipoLimite",
								(CASE WHEN COALESCE(PAR."MontoUtilizado",PRO."MontoUtilizado") > PRO."LIM_MontoTotal" THEN (COALESCE(PAR."MontoUtilizado",PRO."MontoUtilizado") - PRO."LIM_MontoTotal")
									  ELSE 0 END) AS "MontoExcedido", ---VALIDACION
								--COALESCE(PAR."MontoDisponible",0) AS "MontoExcedido",
								PRO."ConceptoExceso",
								(CASE WHEN COALESCE(PAR."MontoUtilizado",PRO."MontoUtilizado")> PRO."LIM_MontoTotal" THEN (CASE WHEN PRO."LIM_IdComportamiento" ='AUT' THEN v_estadoPEN ELSE PRO."LIM_IdComportamiento" END)
									  ELSE PRO."IdEstado" END)  AS "IdEstado",
								---Siempre va OK con autorizacion, (se determina segun combinatoria en la vista)
								(CASE WHEN COALESCE(PAR."MontoUtilizado",PRO."MontoUtilizado") > PRO."LIM_MontoTotal" THEN (CASE WHEN PRO."LIM_IdComportamiento" ='AUT' THEN v_estadoAutNoOK ELSE PRO."IdEstadoAutorizacion" END)
									 ELSE PRO."IdEstadoAutorizacion" END) AS "IdEstadoAutorizacion",
								PRO."LIM_IdTipoEvento" AS "IdTipoEvento",
								PRO."LIM_Prioridad" AS "Prioridad",
								v_ConceptoEvaluacion AS "ConceptoEvaluacion",
								 PRO."LIM_MontoTotal" AS "MontoLimite",
								COALESCE(PAR."MontoUtilizado",0) as "MontoCalculado",
								PRO."LIM_IdMoneda" AS"IdMonedaCalculo",
								p_IdTipoEntidad		AS "IdTipoEntidad",
								PRO."IdCliente",
								PRO."IdPortafolio",
								PRO."IdCanalIngreso"
						FROM :v_Tab_EvaluacionPROPLANTILLA PRO 
						LEFT JOIN :v_MontoDisponibleParidadLM PAR
						ON (PRO."LIM_IdMoneda"= PAR."IdMonedaDestino" AND PRO."IdClasificacionTipoInstrumento"= PAR."IdClasificacionTipoInstrumento"
						AND PRO."IdTipoInstrumento"= PAR."IdTipoInstrumento" AND PRO."IdCliente"= PAR."IdCliente" AND PRO."IdPortafolio"= PAR."IdPortafolio")
						UNION ALL SELECT * FROM :v_Tab_SalidaEvaluaciones;
				END IF;		
			
			END IF;	--FIN: EVALUACION LIMITES A NIVEL DE PORTAFOLIO
	   
	   
				
/********************************************  EVALUACION DE LIMITES CUENTA PROPIA ***************************************************************************/
				
			-- Se identifica si la cuenta propia está considerada en las operaciones a evaluar
			SELECT COUNT(1) into v_Count_LimitesCtaPropia FROM :p_tablaOperacion WHERE "IdCliente" = v_IdClienteCtaPropia;
			
			IF :v_Count_LimitesCtaPropia > 0 AND p_simularOperacion='N' THEN --SE EVALUA LA CUENTA PROPIA

				-- Se buscan limites configurados para la Cta Propia si no existen se envian operaciones de inmediato a la Salida
				v_ConceptoEvaluacion:='';
				
				SELECT COUNT(1) INTO v_Count 
				FROM "BSCL.Plataforma.BD.Vistas::LIM.ConsultarLimiteCtaPropia" WHERE "IdEmpresa"=p_IdEmpresa;
				
				IF v_Count=0 THEN --No existen Limites Cta Propia Configurados
					v_Tab_EvaluacionOK=
						SELECT	OPE."IdOperacion",
								OPE."IdTipoOperacion",
								OPE."IdTipoInstrumento",
								OPE."IdClasificacionTipoInstrumento",
								OPE."MontoUtilizado",								
								'' AS "IdMoneda",
								0 AS "IdLimite",
								'' AS "IdTipoLimite",
								0 AS "MontoExcedido",
								'' AS "ConceptoExceso",
								v_estadoOK AS "IdEstado",
								v_estadoAutOK AS "IdEstadoAutorizacion",
								'' AS "IdTipoEvento",
								0 AS "Prioridad",
								v_ConceptoEvaluacion AS "ConceptoEvaluacion",
								0 AS "MontoLimite",
								0 AS "MontoCalculado",
								'' AS"IdMonedaCalculo",
								p_IdTipoEntidad		AS "IdTipoEntidad",
								OPE."IdCliente",
								OPE."IdPortafolio",
								OPE."IdCanalIngreso"
						FROM :p_tablaOperacion OPE
						WHERE OPE."IdCliente" = v_IdClienteCtaPropia; ---Solo Cta Propia
					
					v_Tab_SalidaEvaluaciones = SELECT * FROM :v_Tab_EvaluacionOK UNION ALL SELECT * FROM :v_Tab_SalidaEvaluaciones;
				
				ELSE --existen Limites Cta Propia Configurados
				
					--1) Se buscan operaciones que tienen Limites (Estos deben pasar por toda la evaluacion de Limites configurados
					
					--Obtener limites aplicables a las operaciones
					v_Tab_EvaluacionPRO=
						SELECT 	ROW_NUMBER() OVER() AS "IdInterno",
								LIM."IdLimite",
								LIM."IdTipoLimite", 
								OPE."IdTipoOperacion",
								OPE."IdOperacion",
								OPE."IdCliente",
								OPE."IdPortafolio",
								OPE."IdTipoInstrumento",
								OPE."IdClasificacionTipoInstrumento",
								OPE."IdTipoReajuste",
								OPE."IdInstrumento",
								OPE."IdEmisor",
								OPE."Tasa",
								OPE."DiasPlazo",
								OPE."FechaOperacion",
								OPE."CodigoOperador",
								OPE."IdMoneda",
								OPE."TipoCambio",
								'' AS "ConceptoExceso",
								0 AS "MontoExcedido",
								OPE."MontoUtilizado",
								v_estadoOK AS "IdEstado",
								v_estadoAutOK AS "IdEstadoAutorizacion",
								OPE."IdAgente",
								OPE."IdCanalIngreso",
								TIP."AccionCustodia" 	AS "AccionCustodia",
								LIM."IdComportamiento"  AS "LIM_IdComportamiento" ,
								LIM."IdTipoEvento"  	AS "LIM_IdTipoEvento",
								LIM."Prioridad"  		AS "LIM_Prioridad",
			 				    LIM."IdInstrumento" 	AS "LIM_IdInstrumento",
								LIM."UnidadPlazo"		AS "LIM_UnidadPlazo",
								LIM."Plazo"				AS "LIM_Plazo",
								LIM."IdTipoReajuste"	AS "LIM_IdTipoReajuste",
								LIM."CodigoOperador"	AS "LIM_CodigoOperador",
								LIM."MontoTotal"		AS "LIM_MontoTotal",
								LIM."MontoIntraday"		AS "LIM_MontoIntraday",
								LIM."MontoOvernight"	AS "LIM_MontoOvernight",
								LIM."IdMoneda"			AS "LIM_IdMoneda",
								LIM."IdMonedaCalculo" AS "LIM_IdMonedaCalculo",															
								LIM."ClasificacionRiesgoMinima"	AS "LIM_ClasificacionRiesgoMinima",
								LIM."EsPersonalizada"	AS "LIM_EsPersonalizada",
								LIM."FechaVigencia"		AS "LIM_FechaVigencia",
								LIM."EsTotalizador" 	AS "LIM_EsTotalizador",
								LIM."IdMesa"			AS "LIM_IdMesa",
								LIM."IdCanalIngreso"	AS "LIM_IdCanalIngreso",
								LIM."IdTipoOperacion"	AS "LIM_IdTipoOperacion",
								LIM."IdTipoInstrumento"	AS "LIM_IdTipoInstrumento",
								LIM."IdClasificacionTipoInstrumento" AS "LIM_IdClasificacionTipoInstrumento",
								LIM."IdClasificacionRiesgo"	AS "LIM_IdClasificacionRiesgo",
								LIM."IdAgente"			AS "LIM_IdAgente",
								LIM."IdGrupoEmisor"		AS "LIM_IdGrupoEmisor",
								LIM."IdEmisor"			AS "LIM_IdEmisor"
								--v_DocumentoIdentidadCtaPropia AS "DocumentoIdentidad"
						FROM :p_tablaOperacion OPE 
						INNER JOIN "BSCL.Plataforma.BD.Vistas::LIM.ConsultarLimiteCtaPropia" LIM
							ON (OPE."IdCliente"=LIM."IdCliente" 
							AND (OPE."IdTipoOperacion"				=LIM."IdTipoOperacion" 		OR LIM."IdTipoOperacion"	='')
							AND (OPE."IdTipoInstrumento"			=LIM."IdTipoInstrumento"	OR LIM."IdTipoInstrumento"	='')
							AND OPE."IdClasificacionTipoInstrumento"=LIM."IdClasificacionTipoInstrumento")
		  				    AND (OPE."IdCanalIngreso"= LIM."IdCanalIngreso" OR LIM."IdCanalIngreso"='' OR LIM."IdCanalIngreso"=0)
		  				    AND (OPE."IdInstrumento"= LIM."IdInstrumento" OR LIM."IdInstrumento"=0)
						INNER JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" TIP
							ON (TIP."IdTipoOperacion"=OPE."IdTipoOperacion")	
						--INNER JOIN :v_Tab_EvaluacionPRO_grupo GRU 
						--	ON (GRU."IdOperacion"=OPE."IdOperacion")
						LEFT JOIN "BSCL.Plataforma.BD::LIM.ExcluirLimite" EXLIM
							ON (EXLIM."IdModulo"=p_IdModulo 
							AND EXLIM."IdEmpresa"=p_IdEmpresa 
							AND EXLIM."IdTipoInstrumento"=OPE."IdTipoInstrumento"
							AND EXLIM."IdEstado"='ACT')
						WHERE LIM."IdEstado"='ACT' --TPO."IdEstado"='ACT' AND
						  AND OPE."IdCliente" = v_IdClienteCtaPropia ---Solo Cta Propia
						  AND EXLIM."IdTipoInstrumento" IS NULL;
					
					--Conteo de limites aplicables
					v_Count_LimitesCtaPropia:=0;
					
					SELECT COUNT(1) INTO v_Count_LimitesCtaPropia FROM :v_Tab_EvaluacionPRO;
					
					IF v_Count_LimitesCtaPropia>0   THEN
					
					--Busca si hay operaciones que requieren Limites y llena la variable correspondiente 

					v_DatosLimitesCtaPropia = 
					SELECT	  COALESCE(GRU."LIMCTPEXCLUIREMISOR", 'N')			AS "LIMCTPEXCLUIREMISOR"
							, COALESCE(GRU."LIMCTPCLASIFRIESGO", 'N')			AS "LIMCTPCLASIFRIESGO"
							, COALESCE(GRU."LIMCTPMARGENTASAS", 'N')			AS "LIMCTPMARGENTASAS"						
							, COALESCE(GRU."LIMCTPPLAZO", 'N')					AS "LIMCTPPLAZO"												
							, COALESCE(GRU."LIMCTPPOSICIONINTERBANCARIO", 'N')	AS "LIMCTPPOSICIONINTERBANCARIO"
							, COALESCE(GRU."LIMCTPMESADISTRIBUCION", 'N')		AS "LIMCTPMESADISTRIBUCION"
							, COALESCE(GRU."LIMCTPFORWARD", 'N')				AS "LIMCTPFORWARD"
							, COALESCE(GRU."LIMCTPCARTERA",'N')					AS "LIMCTPCARTERA"
							, COALESCE(GRU."LIMCTPAGENTE", 'N')					AS "LIMCTPAGENTE"
							, COALESCE(GRU."LIMCTPGRUPOEMISOR", 'N')			AS "LIMCTPGRUPOEMISOR"
							, COALESCE(GRU."LIMCTPEMISOR", 'N')					AS "LIMCTPEMISOR"						
					FROM
							(
								SELECT  
	 							 MAX( CASE WHEN SOL."IdTipoLimite"='LIMCTPEXCLUIREMISOR'				THEN 'S' ELSE NULL END) AS "LIMCTPEXCLUIREMISOR"
								,MAX( CASE WHEN SOL."IdTipoLimite"='LIMCTPCLASIFRIESGO'					THEN 'S' ELSE NULL END) AS "LIMCTPCLASIFRIESGO"
								,MAX( CASE WHEN SOL."IdTipoLimite"='LIMCTPMARGENTASAS'					THEN 'S' ELSE NULL END) AS "LIMCTPMARGENTASAS"
								,MAX( CASE WHEN SOL."IdTipoLimite"='LIMCTPPLAZO'						THEN 'S' ELSE NULL END) AS "LIMCTPPLAZO"							
								,MAX( CASE WHEN SOL."IdTipoLimite"='LIMCTPPOSICIONINTERBANCARIO'		THEN 'S' ELSE NULL END) AS "LIMCTPPOSICIONINTERBANCARIO"
								,MAX( CASE WHEN SOL."IdTipoLimite"='LIMCTPMESADISTRIBUCION'				THEN 'S' ELSE NULL END) AS "LIMCTPMESADISTRIBUCION"
								,MAX( CASE WHEN SOL."IdTipoLimite"='LIMCTPFORWARD'						THEN 'S' ELSE NULL END) AS "LIMCTPFORWARD"
								,MAX( CASE WHEN SOL."IdTipoLimite"='LIMCTPCARTERA'						THEN 'S' ELSE NULL END) AS "LIMCTPCARTERA"
								,MAX( CASE WHEN SOL."IdTipoLimite"='LIMCTPAGENTE'						THEN 'S' ELSE NULL END) AS "LIMCTPAGENTE"
								,MAX( CASE WHEN SOL."IdTipoLimite"='LIMCTPGRUPOEMISOR'					THEN 'S' ELSE NULL END) AS "LIMCTPGRUPOEMISOR"
								,MAX( CASE WHEN SOL."IdTipoLimite"='LIMCTPEMISOR'						THEN 'S' ELSE NULL END) AS "LIMCTPEMISOR"							
								FROM (	SELECT "IdTipoLimite" FROM :v_Tab_EvaluacionPRO 
										  WHERE "IdTipoLimite" IN ('LIMCTPEXCLUIREMISOR'
										  						  ,'LIMCTPCLASIFRIESGO','LIMCTPMARGENTASAS','LIMCTPPLAZO'
										  						  ,'LIMCTPPOSICIONINTERBANCARIO'
										  						  ,'LIMCTPMESADISTRIBUCION','LIMCTPFORWARD','LIMCTPCARTERA'
										  						  ,'LIMCTPAGENTE','LIMCTPGRUPOEMISOR','LIMCTPEMISOR')					  
										 GROUP BY "IdTipoLimite" 
									) AS SOL
							)AS GRU;
							
						SELECT TOP 1 "LIMCTPEXCLUIREMISOR","LIMCTPCLASIFRIESGO","LIMCTPMARGENTASAS","LIMCTPPLAZO","LIMCTPPOSICIONINTERBANCARIO","LIMCTPMESADISTRIBUCION"
							,"LIMCTPFORWARD","LIMCTPCARTERA","LIMCTPAGENTE","LIMCTPGRUPOEMISOR","LIMCTPEMISOR"
						 INTO V_LIMCTPEXCLUIREMISOR,V_LIMCTPCLASIFRIESGO,V_LIMCTPMARGENTASAS,V_LIMCTPPLAZO,V_LIMCTPPOSICIONINTERBANCARIO,V_LIMCTPMESADISTRIBUCION
							,V_LIMCTPFORWARD,V_LIMCTPCARTERA,V_LIMCTPAGENTE,V_LIMCTPGRUPOEMISOR,V_LIMCTPEMISOR	
						FROM :v_DatosLimitesCtaPropia;

					--- Se busca si hay Limites que requieren obtener la Paridad Monetaria, se pasa el monto a la Moneda de cada Limite

							--SELECT COUNT(1) INTO v_Count
							--FROM :v_Tab_EvaluacionPRO PRO
							--WHERE PRO."IdTipoLimite" IN ('LIMCTPCLASIFRIESGO','LIMCTPPOSICIONINTERBANCARIO','LIMCTPMESADISTRIBUCION','LIMCTPFORWARD','LIMCTPCARTERA','LIMCTPAGENTE','LIMCTPGRUPOEMISOR');					  
							
							SELECT LOCATE (V_LIMCTPCLASIFRIESGO || V_LIMCTPPOSICIONINTERBANCARIO || V_LIMCTPMESADISTRIBUCION || V_LIMCTPFORWARD || V_LIMCTPCARTERA || V_LIMCTPAGENTE || V_LIMCTPGRUPOEMISOR || V_LIMCTPEMISOR, 'S') "locate" INTO  v_Count FROM DUMMY;
							-- v_Count:=0;
							IF v_Count > 0 THEN
								---Pasar Monto de la operacion a Monto del Limite
				
								v_in_ObtenerParidadTipoCambio=
								SELECT	PRO."IdInterno" AS "IdInterno",TO_VARCHAR (PRO."FechaOperacion", 'YYYY-MM-DD') as "Fecha", PRO."IdMoneda" as "IdMonedaOrigen",PRO."LIM_IdMonedaCalculo" AS "IdMonedaDestino",PRO."TipoCambio" AS "ValorCambioLocalModificado"
								, PRO."MontoUtilizado" as "MontoOrigen",'ORDENES' AS "IdUso"
								FROM :v_Tab_EvaluacionPRO PRO
									WHERE PRO."IdTipoLimite"IN ('LIMCTPCLASIFRIESGO','LIMCTPPOSICIONINTERBANCARIO','LIMCTPMESADISTRIBUCION','LIMCTPFORWARD','LIMCTPCARTERA','LIMCTPAGENTE','LIMCTPGRUPOEMISOR','LIMCTPEMISOR');
								
								
								SELECT "BSCL.Plataforma.BD.Secuencias::MER.IdTrabajoParidad".NEXTVAL INTO v_IdTrabajoParidad FROM DUMMY;
								
								INSERT INTO "BSCL.Plataforma.BD::MER.CalcularParidadYTipoCambio_Entrada_Entrada"("IdTrabajo","IdInterno","Fecha","IdMonedaOrigen","IdMonedaDestino","ValorCambioLocalModificado","MontoOrigen","IdUso")
								SELECT :v_IdTrabajoParidad,CAR."IdInterno" AS "IdInterno",CAR."Fecha" AS "Fecha", CAR."IdMonedaOrigen" as "IdMonedaOrigen", CAR."IdMonedaDestino" as "IdMonedaDestino",
								0 as "ValorCambioLocalModificado",CAR."MontoOrigen" as "MontoOrigen",'ORDENES' as "IdUso"
								FROM :v_in_ObtenerParidadTipoCambio CAR;
										
								call "BSCL.Plataforma.BD.Servicios::MER.CalcularParidadYTipoCambio"(
									p_IdEmpresa => :p_IdEmpresa,
									p_IdTrabajo => :v_IdTrabajoParidad
								);	
									
								v_out_Paridades=
									SELECT DISTINCT COALESCE(outt."MontoDestino",0) AS  "MontoDestino", par.*
									FROM :v_in_ObtenerParidadTipoCambio par
									LEFT JOIN "BSCL.Plataforma.BD::MER.CalcularParidadYTipoCambio_Salida_Salida" outt 
									on par."IdInterno" = outt."IdInterno" AND outt."IdTrabajo" = :v_IdTrabajoParidad;								
				
								v_out_Paridades=SELECT DISTINCT * FROM  :v_out_Paridades;
										
								select count(1) into v_Count from :v_out_Paridades;
								IF (:v_Count = 0) THEN 
									p_out_ErroresDetalle=
										select 'ERROR_OBTENER_PARIDADES' as "Codigo",:v_out_error as "Descripcion" from dummy
										UNION ALL SELECT * FROM :p_out_ErroresDetalle;
								END IF;
							END IF;



------------------------(A)	LIMITE CUENTA PROPIA	:  EXCLUIR EMISOR
					IF V_LIMCTPEXCLUIREMISOR='S' THEN
						V_Eval_TipoLimite:='LIMCTPEXCLUIREMISOR';
						v_ConceptoEvaluacion:='IdEmisor';
						v_Tab_SalidaEvaluaciones=
							SELECT	PRO."IdOperacion",
									PRO."IdTipoOperacion",
									PRO."IdTipoInstrumento",
									PRO."IdClasificacionTipoInstrumento",
									PRO."MontoUtilizado",									
									PRO."IdMoneda",
									PRO."IdLimite",
									PRO."IdTipoLimite" AS "IdTipoLimite",									
									0 AS "MontoExcedido", 
									PRO."ConceptoExceso",									
									(CASE WHEN (EMI."IdEmisor"=PRO."IdEmisor") THEN  (CASE WHEN PRO."LIM_IdComportamiento" ='AUT' THEN v_estadoPEN ELSE PRO."LIM_IdComportamiento" END)
										  ELSE PRO."IdEstado" END)  AS "IdEstado",
									---Siempre va OK con autorizacion, (se determina segun combinatoria en la vista)
									(CASE WHEN (EMI."IdEmisor"=PRO."IdEmisor") THEN  (CASE WHEN PRO."LIM_IdComportamiento" ='AUT' THEN v_estadoAutNoOK ELSE PRO."IdEstadoAutorizacion" END)
										  ELSE PRO."IdEstadoAutorizacion" END) AS "IdEstadoAutorizacion",
									PRO."LIM_IdTipoEvento" AS "IdTipoEvento",
									PRO."LIM_Prioridad" AS "Prioridad",
									--PRO."DocumentoIdentidad",
									v_ConceptoEvaluacion AS "ConceptoEvaluacion",
									0 AS "MontoLimite",
									0 AS "MontoCalculado",
									'' AS"IdMonedaCalculo",
									p_IdTipoEntidad		AS "IdTipoEntidad",
									PRO."IdCliente",
									PRO."IdPortafolio",
									PRO."IdCanalIngreso"
							FROM :v_Tab_EvaluacionPRO PRO 
							LEFT JOIN "BSCL.Plataforma.BD.Vistas::LIM.ConsultarLimiteEmisor" EMI
								ON (EMI."IdLimite"=PRO."IdLimite" AND EMI."IdEmisor"=PRO."IdEmisor" AND EMI."IdEstado"='ACT')
							WHERE PRO."IdTipoLimite"=V_Eval_TipoLimite
							 -- AND (PRO."IdTipoOperacion"				=PRO."LIM_IdTipoOperacion"		OR PRO."LIM_IdTipoOperacion"	='')
							 -- AND (PRO."IdTipoInstrumento"				=PRO."LIM_IdTipoInstrumento"	OR PRO."LIM_IdTipoInstrumento"	='')
 						     -- AND PRO."IdClasificacionTipoInstrumento"	=PRO."IdClasificacionTipoInstrumento"			  
							UNION ALL SELECT * FROM :v_Tab_SalidaEvaluaciones;
						END IF;	
					
------------------------(B)	LIMITE CUENTA PROPIA	:  CLASIFICACION DE RIESGO

						---CLASIFICACION RIESGO : INSTRUMENTO
						
						IF V_LIMCTPCLASIFRIESGO='S' THEN
							V_Eval_TipoLimite:='LIMCTPCLASIFRIESGO'; 
							v_ConceptoEvaluacion:='NivelClasificacionRiesgo/MontoUtilizado';
							v_Tab_Liminstrumento=
							SELECT	PRO."IdOperacion",
									PRO."IdTipoOperacion",
									PRO."IdTipoInstrumento",
									PRO."IdClasificacionTipoInstrumento",
									--PRO."MontoUtilizado",									
									PAR."MontoDestino" AS "MontoUtilizado",
									PRO."IdMoneda",
									PRO."IdLimite",
									PRO."IdTipoLimite" AS "IdTipoLimite",									
									(CASE WHEN (COALESCE(CLL."Nivel",0) > NVL."NivelDeEvaluacion") THEN 0
										  ELSE
											 	(CASE WHEN PRO."LIM_MontoTotal" > 0 THEN ---Validar si se configuro un monto, Si el monto es mayor al limite, NO SE PERMITE
														 (CASE WHEN PAR."MontoDestino" > PRO."LIM_MontoTotal" THEN (PAR."MontoDestino" - PRO."LIM_MontoTotal")
													 		 ELSE 0  END)	
											   	ELSE 0 END)
									END)  AS "MontoExcedido",
									PRO."ConceptoExceso",									 
									------------------- ( NVL LIMITE    > NVL OPERACION , NO SE PERMITE, SIEL MONTO OPERACION ES MAYOR AL LIMITE NO SE PERMITE)
									(CASE WHEN (COALESCE(CLL."Nivel",0) > NVL."NivelDeEvaluacion") THEN (CASE WHEN PRO."LIM_IdComportamiento" ='AUT' THEN v_estadoPEN ELSE PRO."LIM_IdComportamiento" END)
										  ELSE
											 	(CASE WHEN PRO."LIM_MontoTotal" > 0 THEN ---Validar si se configuro un monto, Si el monto es mayor al limite, NO SE PERMITE
														 (CASE WHEN PAR."MontoDestino" > PRO."LIM_MontoTotal" THEN (CASE WHEN PRO."LIM_IdComportamiento" ='AUT' THEN v_estadoPEN ELSE PRO."LIM_IdComportamiento" END)
													 		 ELSE PRO."IdEstado"  END)	
											   	ELSE PRO."IdEstado"	 END)
									END)  AS "IdEstado", ---ACEPTADO
									---Siempre va OK con autorizacion, (se determina segun combinatoria en la vista)
									(CASE WHEN (COALESCE(CLL."Nivel",0) > NVL."NivelDeEvaluacion") THEN (CASE WHEN PRO."LIM_IdComportamiento" ='AUT' THEN v_estadoAutNoOK ELSE PRO."IdEstadoAutorizacion" END)
										  ELSE 
										 		 (CASE WHEN PRO."LIM_MontoTotal" > 0 THEN ---Validar si se configuro un monto, Si el monto es mayor al limite, NO SE PERMITE
														 (CASE WHEN PAR."MontoDestino" > PRO."LIM_MontoTotal" THEN (CASE WHEN PRO."LIM_IdComportamiento" ='AUT' THEN v_estadoAutNoOK ELSE PRO."IdEstadoAutorizacion" END)
																 ELSE PRO."IdEstadoAutorizacion"  END)	
												 ELSE PRO."IdEstadoAutorizacion" END)
									END) AS "IdEstadoAutorizacion",	
									PRO."LIM_IdTipoEvento" AS "IdTipoEvento",
									PRO."LIM_Prioridad" AS "Prioridad",
									--PRO."DocumentoIdentidad",
									v_ConceptoEvaluacion AS "ConceptoEvaluacion",
									(CASE WHEN PRO."LIM_MontoTotal" > 0 THEN PRO."LIM_MontoTotal" ELSE 0 END) AS "MontoLimite",
									0 AS "MontoCalculado",
									PRO."LIM_IdMonedaCalculo" AS"IdMonedaCalculo",
									p_IdTipoEntidad		AS "IdTipoEntidad",
									PRO."IdCliente",
									PRO."IdPortafolio",
									PRO."IdCanalIngreso"
							FROM :v_Tab_EvaluacionPRO PRO
							INNER JOIN :v_out_Paridades PAR 
							ON (PRO."FechaOperacion" = PAR."Fecha" AND PRO."IdMoneda"= PAR."IdMonedaOrigen" AND  PRO."MontoUtilizado"= PAR."MontoOrigen" AND PRO."LIM_IdMonedaCalculo"= PAR."IdMonedaDestino")
							LEFT JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarClasificacionRiesgo" CLL
								ON (PRO."LIM_IdClasificacionRiesgo" = CLL."IdClasificacionRiesgo" AND CLL."IdEstadoTipoClasificacionRiesgo"='ACT')
							LEFT JOIN "BSCL.Plataforma.BD.Vistas::LIM.ConsultarNivelClasificacionRiesgoInstrumento" NVL
								ON (NVL."IdInstrumento"=PRO."IdInstrumento" AND NVL."IdTipoInstrumento"=PRO."IdTipoInstrumento" AND NVL."IdEntidadClasificadoraRiesgoDestino" = v_IdEntidadClasificadoraRiesgo)
							WHERE PRO."IdTipoLimite"=V_Eval_TipoLimite AND NVL."NivelDeEvaluacion" IS NOT NULL;
							--  AND (PRO."IdTipoOperacion"				=PRO."LIM_IdTipoOperacion"		OR PRO."LIM_IdTipoOperacion"	='')
							--  AND (PRO."IdTipoInstrumento"				=PRO."LIM_IdTipoInstrumento"	OR PRO."LIM_IdTipoInstrumento"	='')
  							--  AND PRO."IdClasificacionTipoInstrumento"	=PRO."LIM_IdClasificacionTipoInstrumento";
							 
						--Esta linea se comenta si se habilta la Clasificacion por Emisor 
							v_Tab_SalidaEvaluaciones=SELECT * FROM :v_Tab_Liminstrumento UNION ALL SELECT * FROM :v_Tab_SalidaEvaluaciones;
						END IF;  
						/*********** CLASIFICACION RIESGO : EMISOR	*********   */
						/* Para habilitar la Clasificacion de Riesgo por Emisor, se debe ingresar la Clasificacion y llenar la Equivalencia */	  
						/*
							 --Agregar rechazados por instrumento a la salida
						 v_Tab_SalidaEvaluaciones=SELECT * FROM :v_Tab_Liminstrumento WHERE "IdEstadoAutorizacion"<> v_estadoAutOK OR "IdEstado"=PRO."LIM_IdComportamiento"
							  UNION ALL SELECT * FROM :v_Tab_SalidaEvaluaciones;	
						
					 -- Los OK por Instrumento pasan por evaluacion del Emisor 
						SELECT COUNT(1) INTO v_Count FROM :v_Tab_Liminstrumento WHERE "IdEstadoAutorizacion"= v_estadoAutOK AND "IdEstado"=v_estadoOK;
						IF v_Count>0 THEN
						   v_Tab_Operacion= SELECT "IdOperacion" FROM :v_Tab_Liminstrumento WHERE "IdEstadoAutorizacion"= v_estadoAutOK AND "IdEstado"=v_estadoOK;
	
							---CLASIFICACION RIESGO : EMISOR
							v_ConceptoEvaluacion:='NivelClasificacionRiesgo/Emisor';
							v_Tab_SalidaEvaluaciones=SELECT 
							   PRO."IdOperacion",
							   PRO."IdTipoOperacion",
							   PRO."IdTipoInstrumento",
							   PRO."IdClasificacionTipoInstrumento",
							   PRO."MontoUtilizado",
							   PRO."IdMoneda",
							   PRO."IdLimite",
							   PRO."IdTipoLimite" AS "IdTipoLimite",
							   0 AS "MontoExcedido", 
							   PRO."ConceptoExceso",
	  					      (CASE WHEN (COALESCE(CLL."Nivel",0) > NVL."NivelDeEvaluacion") THEN  (CASE WHEN PRO."LIM_IdComportamiento" ='AUT' THEN v_estadoPEN ELSE PRO."LIM_IdComportamiento" END)
						   		  ELSE PRO."IdEstado" END)  AS "IdEstado",
							    ---Siempre va OK con autorizacion, (se determina segun combinatoria en la vista)
							    --------------- ( NVL LIMITE    >     NVL OPERACION  , NO SE PERMITE )
							   (CASE WHEN (COALESCE(CLL."Nivel",0) > NVL."NivelDeEvaluacion") THEN  (CASE WHEN PRO."LIM_IdComportamiento" ='AUT' THEN v_estadoAutNoOK ELSE PRO."IdEstadoAutorizacion" END)
							   		 ELSE PRO."IdEstadoAutorizacion" END) AS "IdEstadoAutorizacion",
							   PRO."LIM_IdTipoEvento" AS "IdTipoEvento",
							   PRO."LIM_Prioridad" AS "Prioridad",
							   --PRO."DocumentoIdentidad",
							   v_ConceptoEvaluacion AS "ConceptoEvaluacion",
							   p_IdTipoEntidad		AS "IdTipoEntidad"	   
							 FROM :v_Tab_EvaluacionPRO PRO  INNER JOIN :v_Tab_Operacion INS ON (INS."IdOperacion"=PRO."IdOperacion")
							 LEFT JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarClasificacionRiesgo" CLL
							 ON (PRO."LIM_IdClasificacionRiesgo" = CLL."IdClasificacionRiesgo" AND CLL."IdEstadoTipoClasificacionRiesgo"='ACT')
							 LEFT JOIN "BSCL.Plataforma.BD.Vistas::LIM.ConsultarNivelClasificacionRiesgoEmisor"  NVL
							 ON (NVL."IdEmisor"=PRO."IdEmisor" AND NVL."IdEntidadClasificadoraRiesgoDestino" = v_IdEntidadClasificadoraRiesgo)
							 WHERE PRO."IdTipoLimite"=V_Eval_TipoLimite AND NVL."NivelDeEvaluacion" IS NOT NULL
								AND (PRO."IdTipoOperacion"					= PRO."LIM_IdTipoOperacion"		OR PRO."LIM_IdTipoOperacion"	='')
								AND (PRO."IdTipoInstrumento"				= PRO."LIM_IdTipoInstrumento"	OR PRO."LIM_IdTipoInstrumento"	='')
								AND PRO."IdClasificacionTipoInstrumento"	= PRO."LIM_IdClasificacionTipoInstrumento"
							 UNION ALL SELECT * FROM :v_Tab_SalidaEvaluaciones;
						END IF; 	 
						*/
						
						
------------------------(C)	LIMITE CUENTA PROPIA	:  MARGEN DE TASAS 
	
						IF V_LIMCTPMARGENTASAS='S' THEN
							V_Eval_TipoLimite:='LIMCTPMARGENTASAS'; 
							v_ConceptoEvaluacion:='Tasa';						
							v_Tab_SalidaEvaluaciones=
								SELECT	PRO."IdOperacion",
										PRO."IdTipoOperacion",
										PRO."IdTipoInstrumento",
										PRO."IdClasificacionTipoInstrumento",
										PRO."MontoUtilizado",										
										PRO."IdMoneda",
										PRO."IdLimite",
										PRO."IdTipoLimite" AS "IdTipoLimite",										
										0 AS "MontoExcedido", 
										PRO."ConceptoExceso",										
										----- ( TASA OPERACION ESTA ENTRE TASA MENOS Y MAS , SE PERMITE )
										(CASE WHEN (PRO."Tasa" BETWEEN MNP."TasaFinalMenos" AND MNP."TasaFinalMas") THEN PRO."IdEstado"
											  ELSE (CASE WHEN PRO."LIM_IdComportamiento" ='AUT' THEN v_estadoPEN ELSE PRO."LIM_IdComportamiento" END)
										END)  AS "IdEstado", ---ACEPTADO
										---Siempre va OK con autorizacion, (se determina segun combinatoria en la vista)
										(CASE WHEN (PRO."Tasa" BETWEEN MNP."TasaFinalMenos" AND MNP."TasaFinalMas")  THEN PRO."IdEstadoAutorizacion" 
											  ELSE (CASE WHEN PRO."LIM_IdComportamiento" ='AUT' THEN v_estadoAutNoOK ELSE PRO."IdEstadoAutorizacion" END)
										END) AS "IdEstadoAutorizacion",
										PRO."LIM_IdTipoEvento" AS "IdTipoEvento",
										PRO."LIM_Prioridad" AS "Prioridad",
										--PRO."DocumentoIdentidad",
										v_ConceptoEvaluacion AS "ConceptoEvaluacion",
										0 AS "MontoLimite",
										0 AS "MontoCalculado",
										'' AS"IdMonedaCalculo",
										p_IdTipoEntidad		AS "IdTipoEntidad",
										PRO."IdCliente",
										PRO."IdPortafolio",
										PRO."IdCanalIngreso"
								FROM :v_Tab_EvaluacionPRO PRO
								INNER JOIN "BSCL.Plataforma.BD.Vistas::LIM.ConsultarLimiteRangosMontoPlazo" MNP
								--INNER JOIN "BSCL.Plataforma.BD::LIM.RangosMontoPlazo" MNP								
									ON (PRO."IdLimite"=MNP."IdLimite"
									AND PRO."MontoUtilizado" BETWEEN MNP."MontoDesde" AND MNP."MontoHasta"
									AND PRO."DiasPlazo" BETWEEN MNP."PlazoDesde" AND MNP."PlazoHasta")
								WHERE PRO."IdTipoLimite"=V_Eval_TipoLimite
								 -- AND (PRO."IdTipoOperacion"				=PRO."LIM_IdTipoOperacion" 		OR PRO."LIM_IdTipoOperacion"	='')
								 -- AND (PRO."IdTipoInstrumento"				=PRO."LIM_IdTipoInstrumento"	OR PRO."LIM_IdTipoInstrumento"	='')
								 -- AND PRO."IdClasificacionTipoInstrumento"	=PRO."LIM_IdClasificacionTipoInstrumento"
								  AND PRO."IdMoneda"=PRO."LIM_IdMoneda"
								UNION ALL SELECT * FROM :v_Tab_SalidaEvaluaciones;
							
							---Incluir error cuando la operacion está en moneda distinta a la fijada en el limite
							v_ConceptoEvaluacion:='IdMoneda';
							v_Tab_SalidaEvaluaciones=
								SELECT	PRO."IdOperacion",
										PRO."IdTipoOperacion",
										PRO."IdTipoInstrumento",
										PRO."IdClasificacionTipoInstrumento",
										PRO."MontoUtilizado",
										PRO."IdMoneda",
										PRO."IdLimite",
										PRO."IdTipoLimite" AS "IdTipoLimite",										
										0 AS "MontoExcedido", 
										PRO."ConceptoExceso",
										--------------- ( MONEDA NO COINCIDE CON LA MONEDA DEL LIMITE)
										(CASE WHEN PRO."LIM_IdComportamiento" ='AUT' THEN v_estadoPEN ELSE PRO."LIM_IdComportamiento" END) AS "IdEstado", 
										(CASE WHEN PRO."LIM_IdComportamiento" ='AUT' THEN v_estadoAutNoOK ELSE PRO."IdEstadoAutorizacion" END) AS "IdEstadoAutorizacion",
										PRO."LIM_IdTipoEvento" AS "IdTipoEvento",
										PRO."LIM_Prioridad" AS "Prioridad",
										--PRO."DocumentoIdentidad",
										v_ConceptoEvaluacion AS "ConceptoEvaluacion",
										0 AS "MontoLimite",
										0 AS "MontoCalculado",
										'' AS"IdMonedaCalculo",
										p_IdTipoEntidad		AS "IdTipoEntidad",
										PRO."IdCliente",
										PRO."IdPortafolio",
										PRO."IdCanalIngreso"
								FROM :v_Tab_EvaluacionPRO PRO
								WHERE PRO."IdTipoLimite"=V_Eval_TipoLimite
								--  AND (PRO."IdTipoOperacion"				= PRO."LIM_IdTipoOperacion"			OR PRO."LIM_IdTipoOperacion"	='')
								--  AND (PRO."IdTipoInstrumento"				= PRO."LIM_IdTipoInstrumento"		OR PRO."LIM_IdTipoInstrumento"	='')
								--  AND PRO."IdClasificacionTipoInstrumento"	= PRO."LIM_IdClasificacionTipoInstrumento"
								  AND PRO."IdMoneda" <> PRO."LIM_IdMoneda"
								UNION ALL SELECT * FROM :v_Tab_SalidaEvaluaciones;
						END IF;
						
						
------------------------(D)	LIMITE CUENTA PROPIA	:  PLAZO 						 

						IF V_LIMCTPPLAZO='S' THEN
							V_Eval_TipoLimite:='LIMCTPPLAZO'; 
							v_ConceptoEvaluacion:='DiasPlazo';						
							v_Tab_SalidaEvaluaciones=
								SELECT	PRO."IdOperacion",
										PRO."IdTipoOperacion",
										PRO."IdTipoInstrumento",
										PRO."IdClasificacionTipoInstrumento",
										PRO."MontoUtilizado",										
										PRO."IdMoneda",
										PRO."IdLimite",
										PRO."IdTipoLimite" AS "IdTipoLimite",
										0 AS "MontoExcedido",
										PRO."ConceptoExceso", 
										--------- ( PLAZO OPERACION > PLAZO LIMITE , NO SE PERMITE )
										(CASE WHEN (PRO."DiasPlazo" > PRO."LIM_Plazo") THEN (CASE WHEN PRO."LIM_IdComportamiento" ='AUT' THEN v_estadoPEN ELSE PRO."LIM_IdComportamiento" END)
											  ELSE PRO."IdEstado" END)  AS "IdEstado", ---ACEPTADO
										---Siempre va OK con autorizacion, (se determina segun combinatoria en la vista)
										(CASE WHEN (PRO."DiasPlazo" > PRO."LIM_Plazo") THEN (CASE WHEN PRO."LIM_IdComportamiento" ='AUT' THEN v_estadoAutNoOK ELSE PRO."IdEstadoAutorizacion" END)
											  ELSE PRO."IdEstadoAutorizacion" END) AS "IdEstadoAutorizacion",
										PRO."LIM_IdTipoEvento" AS "IdTipoEvento",
										PRO."LIM_Prioridad" AS "Prioridad",
										--PRO."DocumentoIdentidad",
										v_ConceptoEvaluacion AS "ConceptoEvaluacion",
										0 AS "MontoLimite",
										0 AS "MontoCalculado",
										'' AS"IdMonedaCalculo",
										p_IdTipoEntidad		AS "IdTipoEntidad",
										PRO."IdCliente",
										PRO."IdPortafolio",
										PRO."IdCanalIngreso"
								FROM :v_Tab_EvaluacionPRO PRO
								WHERE PRO."IdTipoLimite"=V_Eval_TipoLimite
								--  AND (PRO."IdTipoOperacion"					=PRO."LIM_IdTipoOperacion"		OR PRO."LIM_IdTipoOperacion"	='')
								--  AND (PRO."IdTipoInstrumento"					=PRO."LIM_IdTipoInstrumento" 	OR PRO."LIM_IdTipoInstrumento"	='')
								--  AND PRO."IdClasificacionTipoInstrumento"		=PRO."LIM_IdClasificacionTipoInstrumento"								  
								  AND PRO."IdMoneda"=PRO."LIM_IdMoneda"
								UNION ALL SELECT * FROM :v_Tab_SalidaEvaluaciones;
						END IF;
						

					
					
--------------------(E)	LIMITE CUENTA PROPIA	:  SPOT : POSICION INTERBANCARIA (INTRADAY/OVERNIGTH)
					
					V_Eval_TipoLimite:='LIMCTPPOSICIONINTERBANCARIO';
					
					--Verificar si existen operaciones para el Limite (TipoOperacion + TipoInstrumento + CodigoOperador + IdMoneda )
					SELECT COUNT(1) INTO v_Count
					FROM :v_Tab_EvaluacionPRO PRO
					WHERE PRO."IdTipoLimite"=V_Eval_TipoLimite
					  AND (PRO."IdTipoOperacion"				= PRO."LIM_IdTipoOperacion"			OR PRO."LIM_IdTipoOperacion"	='')
					  AND (PRO."IdTipoInstrumento"				= PRO."LIM_IdTipoInstrumento" 		OR PRO."LIM_IdTipoInstrumento"	='')
					  AND PRO."IdClasificacionTipoInstrumento"	= PRO."LIM_IdClasificacionTipoInstrumento"
					  AND PRO."CodigoOperador"=PRO."LIM_CodigoOperador"
					  AND PRO."IdMoneda"=PRO."LIM_IdMoneda";--Moneda operacion(limite) vs  Moneda operacion
					
					IF v_Count > 0 THEN
						--p_out_ErroresDetalle =  select 'x' as "Codigo",'x' as "Descripcion" from dummy
						--	UNION ALL  SELECT * FROM :p_out_ErroresDetalle;		
						
						---MONTOINTRADAY
						/*EN DESARROLLO: OBTENER AQUI MONTO DE LA POSICION*/
						--Pasar Monto de la operacion a Monto del Limite

						v_in_ObtenerParidadTipoCambio=
								SELECT	PRO."IdInterno" AS "IdInterno",TO_VARCHAR (PRO."FechaOperacion", 'YYYY-MM-DD') as "Fecha", PRO."IdMoneda" as "IdMonedaOrigen",PRO."LIM_IdMonedaCalculo" AS "IdMonedaDestino",0 AS "ValorCambioLocalModificado"
								, PRO."MontoUtilizado" as "MontoOrigen",'ORDENES' AS "IdUso"
							FROM :v_Tab_EvaluacionPRO PRO
							WHERE PRO."IdTipoLimite"=V_Eval_TipoLimite
							  AND PRO."CodigoOperador"=PRO."LIM_CodigoOperador"
							  AND PRO."IdMoneda"=PRO."LIM_IdMoneda";--Moneda operacion(limite) vs  Moneda operacion
									
						SELECT "BSCL.Plataforma.BD.Secuencias::MER.IdTrabajoParidad".NEXTVAL INTO v_IdTrabajoParidad FROM DUMMY;
						
						INSERT INTO "BSCL.Plataforma.BD::MER.CalcularParidadYTipoCambio_Entrada_Entrada"("IdTrabajo","IdInterno","Fecha","IdMonedaOrigen","IdMonedaDestino","ValorCambioLocalModificado","MontoOrigen","IdUso")
						SELECT :v_IdTrabajoParidad,CAR."IdInterno" AS "IdInterno",CAR."Fecha" AS "Fecha", CAR."IdMonedaOrigen" as "IdMonedaOrigen", CAR."IdMonedaDestino" as "IdMonedaDestino",
						0 as "ValorCambioLocalModificado",CAR."MontoOrigen" as "MontoOrigen",'ORDENES' as "IdUso"
						FROM :v_in_ObtenerParidadTipoCambio CAR;
						
						call "BSCL.Plataforma.BD.Servicios::MER.CalcularParidadYTipoCambio"(
							p_IdEmpresa => :p_IdEmpresa,
							p_IdTrabajo => :v_IdTrabajoParidad
						);	

						v_out_Paridades2=
							SELECT DISTINCT COALESCE(outt."MontoDestino",0) AS  "MontoDestino", par.*
							FROM :v_in_ObtenerParidadTipoCambio par
							LEFT JOIN "BSCL.Plataforma.BD::MER.CalcularParidadYTipoCambio_Salida_Salida" outt 
							on par."IdInterno" = outt."IdInterno" AND outt."IdTrabajo" = :v_IdTrabajoParidad;
			
						SELECT COUNT(1) into v_Count from :v_out_Paridades2;
						IF (:v_Count = 0) THEN 
							p_out_ErroresDetalle=
								SELECT 'ERROR_OBTENER_PARIDADES' as "Codigo",:v_out_error as "Descripcion" from dummy
								UNION ALL SELECT * FROM :p_out_ErroresDetalle;
						ELSE
							v_out_Paridades2=SELECT DISTINCT * FROM  :v_out_Paridades2;
							v_ConceptoEvaluacion:='MontoIntraday';
							v_Tab_SalidaEvaluaciones=
								SELECT	PRO."IdOperacion",
										PRO."IdTipoOperacion",
										PRO."IdTipoInstrumento",
										PRO."IdClasificacionTipoInstrumento",
										PRO."MontoUtilizado",										
										PRO."IdMoneda",
										PRO."IdLimite",
										PRO."IdTipoLimite" AS "IdTipoLimite",
										0 AS "MontoExcedido",
										PRO."ConceptoExceso", 
										------------ ( MONTO OPERACION > MONTO INTRADAY , NO SE PERMITE )
										(CASE WHEN (PAR."MontoDestino" > PRO."LIM_MontoIntraday") THEN (CASE WHEN PRO."LIM_IdComportamiento" ='AUT' THEN v_estadoPEN ELSE PRO."LIM_IdComportamiento" END)
											  ELSE PRO."IdEstado" END) AS "IdEstado", ---ACEPTADO
										---Siempre va OK con autorizacion, (se determina segun combinatoria en la vista)
										(CASE WHEN (PAR."MontoDestino" > PRO."LIM_MontoIntraday") THEN (CASE WHEN PRO."LIM_IdComportamiento" ='AUT' THEN v_estadoAutNoOK ELSE PRO."IdEstadoAutorizacion" END)
											  ELSE PRO."IdEstadoAutorizacion" END) AS "IdEstadoAutorizacion",
										PRO."LIM_IdTipoEvento" AS "IdTipoEvento",
										PRO."LIM_Prioridad" AS "Prioridad",
										--PRO."DocumentoIdentidad",
										v_ConceptoEvaluacion AS "ConceptoEvaluacion",
										PRO."LIM_MontoIntraday" AS "MontoLimite",
										0 AS "MontoCalculado",
										PRO."LIM_IdMoneda" AS"IdMonedaCalculo",
										p_IdTipoEntidad		AS "IdTipoEntidad",
										PRO."IdCliente",
										PRO."IdPortafolio",
										PRO."IdCanalIngreso"
								FROM :v_Tab_EvaluacionPRO PRO
								INNER JOIN :v_out_Paridades2 PAR
									ON (PRO."FechaOperacion" = PAR."Fecha" AND PRO."IdMoneda"= PAR."IdMonedaOrigen" AND  PRO."MontoUtilizado"= PAR."MontoOrigen" AND PRO."LIM_IdMoneda"= PAR."IdMonedaDestino")
								WHERE PRO."IdTipoLimite"=V_Eval_TipoLimite
								--  AND (PRO."IdTipoOperacion"				= PRO."LIM_IdTipoOperacion" 	OR PRO."LIM_IdTipoOperacion"	='')
								--  AND (PRO."IdTipoInstrumento"				= PRO."LIM_IdTipoInstrumento" 	OR PRO."LIM_IdTipoInstrumento"	='')
								--  AND PRO."IdClasificacionTipoInstrumento"	= PRO."LIM_IdClasificacionTipoInstrumento"
								  AND PRO."CodigoOperador"=PRO."LIM_CodigoOperador"
								  AND PRO."IdMoneda"=PRO."LIM_IdMoneda" --Moneda operacion(limite) vs  Moneda operacion
								UNION ALL SELECT * FROM :v_Tab_SalidaEvaluaciones;
						END IF;
						
						---MONTOOVERNIGHT 
						---Pasar Monto de la operacion a Monto del Limite
						/*EN DESARROLLO: OBTENER AQUI MONTO DE LA POSICION*/

						v_in_ObtenerParidadTipoCambio=
								SELECT	PRO."IdInterno" AS "IdInterno",TO_VARCHAR (PRO."FechaOperacion", 'YYYY-MM-DD') as "Fecha", PRO."IdMoneda" as "IdMonedaOrigen",PRO."LIM_IdMonedaCalculo" AS "IdMonedaDestino",0 AS "ValorCambioLocalModificado"
								,  PRO."MontoUtilizado" as "MontoOrigen",'ORDENES' AS "IdUso"
							FROM :v_Tab_EvaluacionPRO PRO
							WHERE PRO."IdTipoLimite"=V_Eval_TipoLimite
							  AND PRO."CodigoOperador"=PRO."LIM_CodigoOperador"
							  AND PRO."IdMoneda" = PRO."LIM_IdMoneda";--Moneda operacion(limite) vs  Moneda operacion

						SELECT "BSCL.Plataforma.BD.Secuencias::MER.IdTrabajoParidad".NEXTVAL INTO v_IdTrabajoParidad FROM DUMMY;
						
						INSERT INTO "BSCL.Plataforma.BD::MER.CalcularParidadYTipoCambio_Entrada_Entrada"("IdTrabajo","IdInterno","Fecha","IdMonedaOrigen","IdMonedaDestino","ValorCambioLocalModificado","MontoOrigen","IdUso")
						SELECT :v_IdTrabajoParidad,CAR."IdInterno" AS "IdInterno",CAR."Fecha" AS "Fecha", CAR."IdMonedaOrigen" as "IdMonedaOrigen", CAR."IdMonedaDestino" as "IdMonedaDestino",
						0 as "ValorCambioLocalModificado",CAR."MontoOrigen" as "MontoOrigen",'ORDENES' as "IdUso"
						FROM :v_in_ObtenerParidadTipoCambio CAR;
						
						call "BSCL.Plataforma.BD.Servicios::MER.CalcularParidadYTipoCambio"(
							p_IdEmpresa => :p_IdEmpresa,
							p_IdTrabajo => :v_IdTrabajoParidad
						);	
							

						v_out_Paridades2=
							SELECT DISTINCT COALESCE(outt."MontoDestino",0) AS  "MontoDestino", par.*
							FROM :v_in_ObtenerParidadTipoCambio par
							LEFT JOIN "BSCL.Plataforma.BD::MER.CalcularParidadYTipoCambio_Salida_Salida" outt 
							on par."IdInterno" = outt."IdInterno" AND outt."IdTrabajo" = :v_IdTrabajoParidad;

			
						SELECT COUNT(1) into v_Count from :v_out_Paridades2;
						IF (:v_Count = 0) THEN
							p_out_ErroresDetalle=
								SELECT 'ERROR_OBTENER_PARIDADES' as "Codigo",:v_out_error as "Descripcion" from dummy
								UNION ALL SELECT * FROM :p_out_ErroresDetalle;
						ELSE
							v_out_Paridades2=SELECT DISTINCT * FROM  :v_out_Paridades2;
							v_ConceptoEvaluacion:='MontoOvernight';
							v_Tab_SalidaEvaluaciones=
								SELECT	PRO."IdOperacion",
										PRO."IdTipoOperacion",
										PRO."IdTipoInstrumento",
										PRO."IdClasificacionTipoInstrumento",
										PRO."MontoUtilizado",
										PRO."IdMoneda",
										PRO."IdLimite",
										PRO."IdTipoLimite" AS "IdTipoLimite",
										0 AS "MontoExcedido", 
										PRO."ConceptoExceso",
										------------ ( MONTO OPERACION > MONTO OVERNIGHT , NO SE PERMITE )
										(CASE WHEN (PAR."MontoDestino" > PRO."LIM_MontoOvernight") THEN (CASE WHEN PRO."LIM_IdComportamiento" ='AUT' THEN v_estadoPEN ELSE PRO."LIM_IdComportamiento" END)
											  ELSE PRO."IdEstado" END) AS "IdEstado", ---ACEPTADO
										---Siempre va OK con autorizacion, (se determina segun combinatoria en la vista)
										(CASE WHEN (PAR."MontoDestino" > PRO."LIM_MontoOvernight") THEN (CASE WHEN PRO."LIM_IdComportamiento" ='AUT' THEN v_estadoAutNoOK ELSE PRO."IdEstadoAutorizacion" END)
											  ELSE PRO."IdEstadoAutorizacion" END) AS "IdEstadoAutorizacion",
										PRO."LIM_IdTipoEvento" AS "IdTipoEvento",
										PRO."LIM_Prioridad" AS "Prioridad",
										--PRO."DocumentoIdentidad",
										v_ConceptoEvaluacion AS "ConceptoEvaluacion",
										PRO."LIM_MontoOvernight" AS "MontoLimite",
										0 AS "MontoCalculado",
										PRO."LIM_IdMoneda" AS"IdMonedaCalculo",
										p_IdTipoEntidad		AS "IdTipoEntidad",
										PRO."IdCliente",
										PRO."IdPortafolio",
										PRO."IdCanalIngreso"
								FROM :v_Tab_EvaluacionPRO PRO
								INNER JOIN :v_out_Paridades2 PAR
									ON (PRO."FechaOperacion" = PAR."Fecha" AND PRO."IdMoneda"= PAR."IdMonedaOrigen" AND  PRO."MontoUtilizado"= PAR."MontoOrigen" AND PRO."LIM_IdMonedaCalculo"= PAR."IdMonedaDestino")
								WHERE PRO."IdTipoLimite"=V_Eval_TipoLimite
				  				--  AND (PRO."IdTipoOperacion"				= PRO."LIM_IdTipoOperacion" 	OR PRO."LIM_IdTipoOperacion"	='')
								--  AND (PRO."IdTipoInstrumento"				= PRO."LIM_IdTipoInstrumento"	OR PRO."LIM_IdTipoInstrumento"	='')
								--  AND PRO."IdClasificacionTipoInstrumento"	= PRO."LIM_IdClasificacionTipoInstrumento"
								  AND PRO."CodigoOperador"=PRO."LIM_CodigoOperador"
								  AND PRO."IdMoneda"=PRO."LIM_IdMoneda" --Moneda operacion(limite) vs  Moneda operacion
								UNION ALL SELECT * FROM :v_Tab_SalidaEvaluaciones;
						END IF;
						
					END IF;
					
								 
--------------------(F)	LIMITE CUENTA PROPIA	:  SPOT : MESA DISTRIBUCION

					IF V_LIMCTPMESADISTRIBUCION='S' THEN
						V_Eval_TipoLimite:='LIMCTPMESADISTRIBUCION';
						v_ConceptoEvaluacion:='MontoTotal';
						v_Tab_SalidaEvaluaciones=
							SELECT	PRO."IdOperacion",
									PRO."IdTipoOperacion",
									PRO."IdTipoInstrumento",
									PRO."IdClasificacionTipoInstrumento",
									PRO."MontoUtilizado",
									PRO."IdMoneda",
									PRO."IdLimite",
									PRO."IdTipoLimite" AS "IdTipoLimite",
									0 AS "MontoExcedido", 
									PRO."ConceptoExceso",
									------------ ( MONTO OPERACION > MONTO LIMITE , NO SE PERMITE )
									(CASE WHEN (PAR."MontoDestino" > PRO."LIM_MontoTotal") THEN (CASE WHEN PRO."LIM_IdComportamiento" ='AUT' THEN v_estadoPEN ELSE PRO."LIM_IdComportamiento" END)
										  ELSE PRO."IdEstado" END) AS "IdEstado", ---ACEPTADO
									---Siempre va OK con autorizacion, (se determina segun combinatoria en la vista)
									(CASE WHEN (PAR."MontoDestino" > PRO."LIM_MontoTotal") THEN (CASE WHEN PRO."LIM_IdComportamiento" ='AUT' THEN v_estadoAutNoOK ELSE PRO."IdEstadoAutorizacion" END)
										  ELSE PRO."IdEstadoAutorizacion" END) AS "IdEstadoAutorizacion",
									PRO."LIM_IdTipoEvento" AS "IdTipoEvento",
									PRO."LIM_Prioridad" AS "Prioridad",
									--PRO."DocumentoIdentidad",
									v_ConceptoEvaluacion AS "ConceptoEvaluacion",
									PRO."LIM_MontoTotal" AS "MontoLimite",
									0 AS "MontoCalculado",
									PRO."LIM_IdMonedaCalculo" AS"IdMonedaCalculo",
									p_IdTipoEntidad		AS "IdTipoEntidad",
									PRO."IdCliente",
									PRO."IdPortafolio",
									PRO."IdCanalIngreso"
							FROM :v_Tab_EvaluacionPRO PRO
							INNER JOIN :v_out_Paridades PAR 
								ON (PRO."FechaOperacion" = PAR."Fecha" AND PRO."IdMoneda"= PAR."IdMonedaOrigen" AND  PRO."MontoUtilizado"= PAR."MontoOrigen" AND PRO."LIM_IdMonedaCalculo"= PAR."IdMonedaDestino")
							WHERE PRO."IdTipoLimite"=V_Eval_TipoLimite
							--  AND (PRO."IdTipoOperacion"				= PRO."LIM_IdTipoOperacion" 	OR PRO."LIM_IdTipoOperacion"	='')
						  	--  AND (PRO."IdTipoInstrumento"				= PRO."LIM_IdTipoInstrumento"	OR PRO."LIM_IdTipoInstrumento"	='')
						  	--  AND PRO."IdClasificacionTipoInstrumento"	= PRO."LIM_IdClasificacionTipoInstrumento"	
							  AND PRO."CodigoOperador"=PRO."LIM_CodigoOperador"
							  AND PRO."IdMoneda"=PRO."LIM_IdMoneda" --Moneda operacion(limite) vs  Moneda operacion
							UNION ALL SELECT * FROM :v_Tab_SalidaEvaluaciones;
					END IF;					
					
--------------------(G)	LIMITE CUENTA PROPIA	:  LIMITE FORWARD
					IF V_LIMCTPFORWARD='S' THEN
						V_Eval_TipoLimite:='LIMCTPFORWARD';
						v_ConceptoEvaluacion:='MontoTotal';
						v_Tab_SalidaEvaluaciones=
							SELECT	PRO."IdOperacion",
									PRO."IdTipoOperacion",
									PRO."IdTipoInstrumento",
									PRO."IdClasificacionTipoInstrumento",
									PRO."MontoUtilizado",
									PRO."IdMoneda",
									PRO."IdLimite",
									PRO."IdTipoLimite" AS "IdTipoLimite",
									0 AS "MontoExcedido", 
									PRO."ConceptoExceso",
									------------ ( MONTO OPERACION > MONTO LIMITE , NO SE PERMITE )
									(CASE WHEN (COALESCE(PAR."MontoDestino",PRO."MontoUtilizado") > PRO."LIM_MontoTotal") THEN (CASE WHEN PRO."LIM_IdComportamiento" ='AUT' THEN v_estadoPEN ELSE PRO."LIM_IdComportamiento" END)
										  ELSE PRO."IdEstado" END) AS "IdEstado", ---ACEPTADO
									---Siempre va OK con autorizacion, (se determina segun combinatoria en la vista)
									(CASE WHEN (COALESCE(PAR."MontoDestino",PRO."MontoUtilizado") > PRO."LIM_MontoTotal") THEN (CASE WHEN PRO."LIM_IdComportamiento" ='AUT' THEN v_estadoAutNoOK ELSE PRO."IdEstadoAutorizacion" END)
										  ELSE PRO."IdEstadoAutorizacion" END) AS "IdEstadoAutorizacion",
									PRO."LIM_IdTipoEvento" AS "IdTipoEvento",
									PRO."LIM_Prioridad" AS "Prioridad",
									--PRO."DocumentoIdentidad",
									v_ConceptoEvaluacion AS "ConceptoEvaluacion",
									PRO."LIM_MontoTotal" AS "MontoLimite",
									0 AS "MontoCalculado",
									PRO."LIM_IdMonedaCalculo" AS"IdMonedaCalculo",
									p_IdTipoEntidad		AS "IdTipoEntidad",
									PRO."IdCliente",
									PRO."IdPortafolio",
									PRO."IdCanalIngreso"
							FROM :v_Tab_EvaluacionPRO PRO
							INNER JOIN :v_out_Paridades PAR 
								ON (PRO."FechaOperacion" = PAR."Fecha" AND PRO."IdMoneda"= PAR."IdMonedaOrigen" AND  PRO."MontoUtilizado"= PAR."MontoOrigen" AND PRO."LIM_IdMonedaCalculo"= PAR."IdMonedaDestino")
							WHERE PRO."IdTipoLimite"=V_Eval_TipoLimite
					  		--  AND (PRO."IdTipoOperacion"				= PRO."LIM_IdTipoOperacion" 	OR PRO."LIM_IdTipoOperacion"	='')
							--  AND (PRO."IdTipoInstrumento"				= PRO."LIM_IdTipoInstrumento"	OR PRO."LIM_IdTipoInstrumento"	='')
							--  AND PRO."IdClasificacionTipoInstrumento"	= PRO."LIM_IdClasificacionTipoInstrumento"	
							  AND PRO."CodigoOperador"=PRO."LIM_CodigoOperador"
							  AND PRO."IdMoneda" = PRO."LIM_IdMoneda" --Moneda operacion(limite) vs  Moneda operacion
							UNION ALL SELECT * FROM :v_Tab_SalidaEvaluaciones;
					END IF;							
					
--------------------(H)	LIMITE CUENTA PROPIA	:  CARTERA

					V_Eval_TipoLimite:='LIMCTPCARTERA';
					
					--Verificar si existen operaciones para el Limite (TipoOperacion + TipoInstrumento + CodigoOperador + IdMoneda )
					SELECT COUNT(1) INTO v_Count
					FROM :v_Tab_EvaluacionPRO PRO
					WHERE PRO."IdTipoLimite"=V_Eval_TipoLimite
					  AND (PRO."IdTipoOperacion"				= PRO."LIM_IdTipoOperacion" 	OR PRO."LIM_IdTipoOperacion"	='')
					  AND (PRO."IdTipoInstrumento"				= PRO."LIM_IdTipoInstrumento"	OR PRO."LIM_IdTipoInstrumento"	='')
					  AND PRO."IdClasificacionTipoInstrumento"	= PRO."LIM_IdClasificacionTipoInstrumento"	
					  AND PRO."IdMoneda" = PRO."LIM_IdMoneda";--Moneda operacion(limite) vs  Moneda operacion

					IF v_Count > 0 THEN
						/*EN DESARROLLO: OBTENER AQUI MONTO DISPONIBLE TOTAL EN CUSTODIA */
						/*FLUJO  CUSTODIA*/
						v_tablaInstrumentos=SELECT
												NULL AS  "IdClasificacionTipoInstrumento",
												NULL AS "IdTipoInstrumento",
												NULL AS "IdInstrumento",
												NULL AS  "IdEmisor",
												NULL AS  "FechaVencimiento",
												NULL AS  "IdTipoReajuste",
												'N' "EsFungible",
												NULL AS  "IdBoveda" FROM DUMMY;
						
						v_tablaCarteras=SELECT
												NULL AS "FolioCartera",
												NULL AS "FolioGarantia",
												NULL AS "FolioSubyacente",
												v_IdClienteCtaPropia AS "IdCliente",
												NULL AS "IdPortafolio" FROM DUMMY;
						
						
						 V_out_FlujoTotalizadoFinal=SELECT CUS.*,CUS."Monto" AS "MontoDisponible" FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCarteraDisponible" CUS
								INNER JOIN :v_tablaCarteras CAR
								ON (CAR."IdCliente"=CUS."IdCliente")
							--	INNER JOIN :v_tablaInstrumentos INS
							--	ON (INS."IdClasificacionTipoInstrumento"=CUS."IdClasificacionTipoInstrumento"
							--		AND  INS."IdTipoInstrumento"=CUS."IdTipoInstrumento")
								WHERE TO_VARCHAR (CUS."FechaLiquidacion", 'YYYY-MM-DD')=TO_VARCHAR (:v_fechaHoraSistema, 'YYYY-MM-DD');
								
						/*PARIDADES MONTOS CUSTODIA*/
						---Pasar Monto Disponible de las carteras a Moneda de los Limites
						v_Monedas_limites=
							SELECT DISTINCT PRO."LIM_IdMonedaCalculo" as "MonedaDestino"
							FROM :v_Tab_EvaluacionPRO PRO
							WHERE PRO."IdTipoLimite"=V_Eval_TipoLimite
						  	  AND (PRO."IdTipoOperacion"				= PRO."LIM_IdTipoOperacion" 	OR PRO."LIM_IdTipoOperacion"	='')
							  AND (PRO."IdTipoInstrumento"				= PRO."LIM_IdTipoInstrumento"	OR PRO."LIM_IdTipoInstrumento"	='')
							  AND PRO."IdClasificacionTipoInstrumento"	= PRO."LIM_IdClasificacionTipoInstrumento"	
							  AND PRO."IdMoneda" = PRO."LIM_IdMoneda";--Moneda operacion(limite) vs  Moneda operacion
						
						V_TotalCartera=
							SELECT ROW_NUMBER() OVER() AS "IdInterno",FLU.*, MON."MonedaDestino"
							FROM :V_out_FlujoTotalizadoFinal FLU,:v_Monedas_limites MON;
						

						v_in_ObtenerParidadTipoCambio=
							SELECT	CAR."IdInterno" AS "IdInterno",TO_VARCHAR (:v_fechaHoraSistema, 'YYYY-MM-DD') as "Fecha", CAR."IdMoneda" as "IdMonedaOrigen",CAR."MonedaDestino" AS "IdMonedaDestino",0 AS "ValorCambioLocalModificado"
							, CAR."MontoDisponible" as "MontoOrigen",'ORDENES' AS "IdUso"
							FROM :V_TotalCartera CAR;
						
						
						
						SELECT "BSCL.Plataforma.BD.Secuencias::MER.IdTrabajoParidad".NEXTVAL INTO v_IdTrabajoParidad FROM DUMMY;
						
						INSERT INTO "BSCL.Plataforma.BD::MER.CalcularParidadYTipoCambio_Entrada_Entrada"("IdTrabajo","IdInterno","Fecha","IdMonedaOrigen","IdMonedaDestino","ValorCambioLocalModificado","MontoOrigen","IdUso")
						SELECT :v_IdTrabajoParidad,CAR."IdInterno" AS "IdInterno",CAR."Fecha" AS "Fecha", CAR."IdMonedaOrigen" as "IdMonedaOrigen", CAR."IdMonedaDestino" as "IdMonedaDestino",
						0 as "ValorCambioLocalModificado",CAR."MontoOrigen" as "MontoOrigen",'ORDENES' as "IdUso"
						FROM :v_in_ObtenerParidadTipoCambio CAR;
						
						call "BSCL.Plataforma.BD.Servicios::MER.CalcularParidadYTipoCambio"(
							p_IdEmpresa => :p_IdEmpresa,
							p_IdTrabajo => :v_IdTrabajoParidad
						);	
						
						
						v_out_Paridad2=
						SELECT DISTINCT COALESCE(outt."MontoDestino",0) AS  "MontoDestino", par.*
						FROM :v_in_ObtenerParidadTipoCambio par
						LEFT JOIN "BSCL.Plataforma.BD::MER.CalcularParidadYTipoCambio_Salida_Salida" outt 
						on par."IdInterno" = outt."IdInterno" AND outt."IdTrabajo" = :v_IdTrabajoParidad;
					
						
						v_out_Paridad2=SELECT DISTINCT * FROM  :v_out_Paridad2;
						
						---Igualar montos de Paridades, Sumar monto segun Clasificacion y tipo Instrumento
						v_MontoDisponibleParidad=
							SELECT SUM(PAR."MontoDestino") AS "MontoDisponible", PAR."IdMonedaDestino",CAR."IdTipoInstrumento"
							FROM :V_TotalCartera CAR 
							INNER JOIN :v_out_Paridad2 PAR
								ON (CAR."IdMoneda"= PAR."IdMonedaOrigen" AND  CAR."MontoDisponible"= PAR."MontoOrigen" AND CAR."MonedaDestino"= PAR."IdMonedaDestino")
							GROUP BY PAR."IdMonedaDestino",CAR."IdTipoInstrumento";
							
						
						/*FIN : PARIDADES MONTOS CUSTODIA*/	
							
						---Pasar Monto de la operacion a Monto del Limite

							v_ConceptoEvaluacion:='MontoDisponible';
							v_Tab_SalidaEvaluaciones=
								SELECT	PRO."IdOperacion",
										PRO."IdTipoOperacion",
										PRO."IdTipoInstrumento",
										PRO."IdClasificacionTipoInstrumento",
										PRO."MontoUtilizado",										
										PRO."IdMoneda",
										PRO."IdLimite",
										PRO."IdTipoLimite" AS "IdTipoLimite",
										(CASE WHEN ((COALESCE(PAR."MontoDisponible",0)	 +  (CASE WHEN PRO."AccionCustodia"='RETIRO' THEN 0 ELSE OPR."MontoDestino" END) )  > PRO."LIM_MontoTotal") THEN ((COALESCE(PAR."MontoDisponible",0) + (CASE WHEN PRO."AccionCustodia"='RETIRO' THEN 0 ELSE OPR."MontoDestino" END)) - PRO."LIM_MontoTotal") ELSE 0 END )  AS "MontoExcedido", 
										PRO."ConceptoExceso",
										--------------- ( MONTO DISPONIBLE > MONTO LIMITE , NO SE PERMITE ) * REVISAR CUANDO ES VENTA
										(CASE WHEN ((COALESCE(PAR."MontoDisponible",0) + (CASE WHEN PRO."AccionCustodia"='RETIRO' THEN 0 ELSE OPR."MontoDestino" END)) > PRO."LIM_MontoTotal") THEN (CASE WHEN PRO."LIM_IdComportamiento" ='AUT' THEN v_estadoPEN ELSE PRO."LIM_IdComportamiento" END)
										  ELSE PRO."IdEstado" END) AS "IdEstado", ---ACEPTADO
										---Siempre va OK con autorizacion, (se determina segun combinatoria en la vista)
										(CASE WHEN ((COALESCE(PAR."MontoDisponible",0) + (CASE WHEN PRO."AccionCustodia"='RETIRO' THEN 0 ELSE OPR."MontoDestino" END)) > PRO."LIM_MontoTotal") THEN (CASE WHEN PRO."LIM_IdComportamiento" ='AUT' THEN v_estadoAutNoOK ELSE PRO."IdEstadoAutorizacion" END)
											 ELSE PRO."IdEstadoAutorizacion" END) AS "IdEstadoAutorizacion",
										PRO."LIM_IdTipoEvento" AS "IdTipoEvento",
										PRO."LIM_Prioridad" AS "Prioridad",
										--PRO."DocumentoIdentidad",
										v_ConceptoEvaluacion AS "ConceptoEvaluacion",
										PRO."LIM_MontoTotal" AS "MontoLimite",
										(COALESCE(PAR."MontoDisponible",0)	 +  (CASE WHEN PRO."AccionCustodia"='RETIRO' THEN 0 ELSE OPR."MontoDestino" END) ) AS "MontoCalculado",
										PRO."LIM_IdMonedaCalculo" AS"IdMonedaCalculo",
										p_IdTipoEntidad		AS "IdTipoEntidad",
										PRO."IdCliente",
										PRO."IdPortafolio",
										PRO."IdCanalIngreso"
								FROM :v_Tab_EvaluacionPRO PRO
								INNER JOIN :v_out_Paridades OPR
									ON (PRO."FechaOperacion" = OPR."Fecha" AND PRO."IdMoneda"= OPR."IdMonedaOrigen" AND  PRO."MontoUtilizado"= OPR."MontoOrigen" AND PRO."LIM_IdMonedaCalculo"= OPR."IdMonedaDestino")
								LEFT JOIN :v_MontoDisponibleParidad PAR
									ON (PRO."LIM_IdMoneda"= PAR."IdMonedaDestino" AND PRO."IdTipoInstrumento"= PAR."IdTipoInstrumento")
								WHERE PRO."IdTipoLimite"=V_Eval_TipoLimite
						  		--  AND (PRO."IdTipoOperacion"				= PRO."LIM_IdTipoOperacion" 	OR PRO."LIM_IdTipoOperacion"	='')
								--  AND (PRO."IdTipoInstrumento"				= PRO."LIM_IdTipoInstrumento"	OR PRO."LIM_IdTipoInstrumento"	='')
								--  AND PRO."IdClasificacionTipoInstrumento"	= PRO."LIM_IdClasificacionTipoInstrumento"	
								  AND PRO."IdMoneda" = PRO."LIM_IdMoneda" --Moneda operacion(limite) vs  Moneda operacion
								UNION ALL SELECT * FROM :v_Tab_SalidaEvaluaciones;
						
					END IF;
					
					
--------------------(I)	LIMITE CUENTA PROPIA	:  AGENTE
					IF V_LIMCTPAGENTE='S' THEN
							V_Eval_TipoLimite:='LIMCTPAGENTE';
							v_ConceptoEvaluacion:='MontoTotal,IdAgente';
							v_Tab_SalidaEvaluaciones=
								SELECT	PRO."IdOperacion",
										PRO."IdTipoOperacion",
										PRO."IdTipoInstrumento",
										PRO."IdClasificacionTipoInstrumento",
										PRO."MontoUtilizado",
										PRO."IdMoneda",
										PRO."IdLimite",
										PRO."IdTipoLimite" AS "IdTipoLimite",
										(CASE WHEN PAR."MontoDestino" > PRO."LIM_MontoTotal" THEN (PAR."MontoDestino" - PRO."LIM_MontoTotal") 
											  ELSE 0 END)  AS "MontoExcedido",
										PRO."ConceptoExceso",											  
										------------ ( MONTO OPERACION > MONTO LIMITE , NO SE PERMITE )
										(CASE WHEN (PAR."MontoDestino" > PRO."LIM_MontoTotal") THEN (CASE WHEN PRO."LIM_IdComportamiento" ='AUT' THEN v_estadoPEN ELSE PRO."LIM_IdComportamiento" END)
											  ELSE PRO."IdEstado" END) AS "IdEstado", ---ACEPTADO
										---Siempre va OK con autorizacion, (se determina segun combinatoria en la vista)
										(CASE WHEN (PAR."MontoDestino" > PRO."LIM_MontoTotal") THEN (CASE WHEN PRO."LIM_IdComportamiento" ='AUT' THEN v_estadoAutNoOK ELSE PRO."IdEstadoAutorizacion" END)
											  ELSE PRO."IdEstadoAutorizacion" END) AS "IdEstadoAutorizacion",
										PRO."LIM_IdTipoEvento" AS "IdTipoEvento",
										PRO."LIM_Prioridad" AS "Prioridad",
										--PRO."DocumentoIdentidad",
										v_ConceptoEvaluacion AS "ConceptoEvaluacion",
										PRO."LIM_MontoTotal" AS "MontoLimite",
										0 AS "MontoCalculado",
										PRO."LIM_IdMonedaCalculo" AS"IdMonedaCalculo",
										p_IdTipoEntidad		AS "IdTipoEntidad",
										PRO."IdCliente",
										PRO."IdPortafolio",
										PRO."IdCanalIngreso"
								FROM :v_Tab_EvaluacionPRO PRO
								INNER JOIN :v_out_Paridades PAR 
									ON (PRO."FechaOperacion" = PAR."Fecha" AND PRO."IdMoneda"= PAR."IdMonedaOrigen" AND  PRO."MontoUtilizado"= PAR."MontoOrigen" AND PRO."LIM_IdMonedaCalculo"= PAR."IdMonedaDestino")
								WHERE PRO."IdTipoLimite"=V_Eval_TipoLimite
						  		--  AND (PRO."IdTipoOperacion"				= PRO."LIM_IdTipoOperacion" 	OR PRO."LIM_IdTipoOperacion"	='')
								--  AND (PRO."IdTipoInstrumento"				= PRO."LIM_IdTipoInstrumento"	OR PRO."LIM_IdTipoInstrumento"	='')
								--  AND PRO."IdClasificacionTipoInstrumento"	= PRO."LIM_IdClasificacionTipoInstrumento"	
								  AND PRO."IdAgente"=PRO."LIM_IdAgente"
								  AND PRO."IdMoneda"=PRO."LIM_IdMoneda" --Moneda operacion(limite) vs  Moneda operacion
								UNION ALL SELECT * FROM :v_Tab_SalidaEvaluaciones;
					END IF;
									
------------------------(J)	LIMITE CUENTA PROPIA	:  GRUPO EMISOR
					IF V_LIMCTPGRUPOEMISOR='S' THEN
						V_Eval_TipoLimite:='LIMCTPGRUPOEMISOR';
						v_ConceptoEvaluacion:='IdEmisor,MontoUtilizado';
						v_Tab_SalidaEvaluaciones=
							SELECT	PRO."IdOperacion",
									PRO."IdTipoOperacion",
									PRO."IdTipoInstrumento",
									PRO."IdClasificacionTipoInstrumento",
									PRO."MontoUtilizado",
									PRO."IdMoneda",
									PRO."IdLimite",
									PRO."IdTipoLimite" AS "IdTipoLimite",
									(CASE WHEN (EMI."IdEmisor"=PRO."IdEmisor") THEN  0
										  ELSE 
												  (CASE WHEN PAR."MontoDestino"> PRO."LIM_MontoTotal" THEN
												  			(PAR."MontoDestino" - PRO."LIM_MontoTotal")
												  ELSE 0  END )
									END)  AS "MontoExcedido",
									PRO."ConceptoExceso",
									------------ ( SI EL EMISOR ES IGUAL SE EVALUA EL MONTO CONNTRA EL LIMITE, SI ES MAYOR NO SE PERMITE )
									(CASE WHEN (EMI."IdEmisor"=PRO."IdEmisor") THEN  (CASE WHEN PRO."LIM_IdComportamiento" ='AUT' THEN v_estadoPEN ELSE PRO."LIM_IdComportamiento" END)
										  ELSE 
												  (CASE WHEN PAR."MontoDestino"> PRO."LIM_MontoTotal" THEN
												  			(CASE WHEN PRO."LIM_IdComportamiento" ='AUT' THEN v_estadoPEN
												  			 ELSE PRO."LIM_IdComportamiento" END)
												  ELSE PRO."IdEstado"  END )
									END)  AS "IdEstado",										  
									---Siempre va OK con autorizacion, (se determina segun combinatoria en la vista)
									(CASE WHEN (EMI."IdEmisor"=PRO."IdEmisor") THEN  (CASE WHEN PRO."LIM_IdComportamiento" ='AUT' THEN v_estadoAutNoOK ELSE PRO."IdEstadoAutorizacion" END)
										  ELSE 
										  		(CASE WHEN PAR."MontoDestino"> PRO."LIM_MontoTotal" THEN
										  				  (CASE WHEN PRO."LIM_IdComportamiento" ='AUT' THEN v_estadoAutNoOK
										  				   ELSE PRO."IdEstadoAutorizacion" END)
												ELSE PRO."IdEstadoAutorizacion" END)
									END) AS "IdEstadoAutorizacion",
									PRO."LIM_IdTipoEvento" AS "IdTipoEvento",
									PRO."LIM_Prioridad" AS "Prioridad",
									v_ConceptoEvaluacion AS "ConceptoEvaluacion",
									PRO."LIM_MontoTotal"  AS "MontoLimite",
									0 AS "MontoCalculado",
									PRO."LIM_IdMonedaCalculo" AS"IdMonedaCalculo",
									p_IdTipoEntidad		AS "IdTipoEntidad",
									PRO."IdCliente",
									PRO."IdPortafolio",
									PRO."IdCanalIngreso"
							FROM :v_Tab_EvaluacionPRO PRO 
							INNER JOIN :v_out_Paridades PAR 
							ON (PRO."FechaOperacion" = PAR."Fecha" AND PRO."IdMoneda"= PAR."IdMonedaOrigen" AND  PRO."MontoUtilizado"= PAR."MontoOrigen" AND PRO."LIM_IdMonedaCalculo"= PAR."IdMonedaDestino")
								LEFT JOIN "BSCL.Plataforma.BD.Vistas::LIM.ConsultarGrupoEmisorDetalle" EMI
								ON (EMI."IdGrupoEmisor"=PRO."LIM_IdGrupoEmisor" AND EMI."IdEmisor"=PRO."IdEmisor" AND EMI."IdEstado"='ACT')
							WHERE PRO."IdTipoLimite"=V_Eval_TipoLimite
							--  AND (PRO."IdTipoOperacion"				=PRO."LIM_IdTipoOperacion"		OR PRO."LIM_IdTipoOperacion"	='')
							--  AND (PRO."IdTipoInstrumento"				=PRO."LIM_IdTipoInstrumento"	OR PRO."LIM_IdTipoInstrumento"	='')
 						    --  AND PRO."IdClasificacionTipoInstrumento"	=PRO."IdClasificacionTipoInstrumento"			  
							UNION ALL SELECT * FROM :v_Tab_SalidaEvaluaciones;
					END IF;							
							
--------------------(K)	LIMITE CUENTA PROPIA	:  EMISOR
					IF V_LIMCTPEMISOR='S' THEN
							V_Eval_TipoLimite:='LIMCTPEMISOR';
							v_ConceptoEvaluacion:='MontoTotal,IdEmisor';
							v_Tab_SalidaEvaluaciones=
								SELECT	PRO."IdOperacion",
										PRO."IdTipoOperacion",
										PRO."IdTipoInstrumento",
										PRO."IdClasificacionTipoInstrumento",
										PRO."MontoUtilizado",
										PRO."IdMoneda",
										PRO."IdLimite",
										PRO."IdTipoLimite" AS "IdTipoLimite",
										(CASE WHEN PAR."MontoDestino" > PRO."LIM_MontoTotal" THEN (PAR."MontoDestino" - PRO."LIM_MontoTotal") 
											  ELSE 0 END)  AS "MontoExcedido",
										PRO."ConceptoExceso",	  
										------------ ( MONTO OPERACION > MONTO LIMITE , NO SE PERMITE )
										(CASE WHEN (PAR."MontoDestino" > PRO."LIM_MontoTotal") THEN (CASE WHEN PRO."LIM_IdComportamiento" ='AUT' THEN v_estadoPEN ELSE PRO."LIM_IdComportamiento" END)
											  ELSE PRO."IdEstado" END) AS "IdEstado", ---ACEPTADO
										---Siempre va OK con autorizacion, (se determina segun combinatoria en la vista)
										(CASE WHEN (PAR."MontoDestino" > PRO."LIM_MontoTotal") THEN (CASE WHEN PRO."LIM_IdComportamiento" ='AUT' THEN v_estadoAutNoOK ELSE PRO."IdEstadoAutorizacion" END)
											  ELSE PRO."IdEstadoAutorizacion" END) AS "IdEstadoAutorizacion",
										PRO."LIM_IdTipoEvento" AS "IdTipoEvento",
										PRO."LIM_Prioridad" AS "Prioridad",
										--PRO."DocumentoIdentidad",
										v_ConceptoEvaluacion AS "ConceptoEvaluacion",
										PRO."LIM_MontoTotal" AS "MontoLimite",
										0 AS "MontoCalculado",
										PRO."LIM_IdMonedaCalculo" AS"IdMonedaCalculo",
										p_IdTipoEntidad		AS "IdTipoEntidad",
										PRO."IdCliente",
										PRO."IdPortafolio",
										PRO."IdCanalIngreso"
								FROM :v_Tab_EvaluacionPRO PRO
								INNER JOIN :v_out_Paridades PAR 
									ON (PRO."FechaOperacion" = PAR."Fecha" AND PRO."IdMoneda"= PAR."IdMonedaOrigen" AND  PRO."MontoUtilizado"= PAR."MontoOrigen" AND PRO."LIM_IdMonedaCalculo"= PAR."IdMonedaDestino")
								WHERE PRO."IdTipoLimite"=V_Eval_TipoLimite
						  		--  AND (PRO."IdTipoOperacion"				= PRO."LIM_IdTipoOperacion" 	OR PRO."LIM_IdTipoOperacion"	='')
								--  AND (PRO."IdTipoInstrumento"				= PRO."LIM_IdTipoInstrumento"	OR PRO."LIM_IdTipoInstrumento"	='')
								--  AND PRO."IdClasificacionTipoInstrumento"	= PRO."LIM_IdClasificacionTipoInstrumento"	
								  AND PRO."IdEmisor"=PRO."LIM_IdEmisor"
								  AND PRO."IdMoneda"=PRO."LIM_IdMoneda" --Moneda operacion(limite) vs  Moneda operacion
								UNION ALL SELECT * FROM :v_Tab_SalidaEvaluaciones;
					END IF;										
				END IF;							
				
			END IF;	--fin existen Limites Cta Propia Configurados
			
		END IF;	--FIN: SE EVALUA LA CUENTA PROPIA

			---CREACION DEL REGISTRO DE EVALUACION (TABLA FISICA)
			SELECT COUNT(1) INTO v_Count FROM :v_Tab_SalidaEvaluaciones WHERE "IdLimite" > 0;
			IF v_Count > 0 AND p_simularOperacion='N' THEN
			
				v_Tab_EvaluacionPRO_grupo=
				SELECT	"BSCL.Plataforma.BD.Secuencias::LIM.IdEvaluacionLimite".NEXTVAL AS "IdEvaluacionLimite",
								EVA."IdOperacion",
								EVA."IdTipoOperacion"
						FROM :v_Tab_SalidaEvaluaciones EVA
						WHERE EVA."IdLimite" > 0
						GROUP BY EVA."IdOperacion",EVA."IdTipoOperacion";
			
				v_Tab_GrabarEvaluacion=
						SELECT 	GRU."IdEvaluacionLimite",
								("BSCL.Plataforma.BD.Secuencias::LIM.IdSecuenciaEvaluacion".NEXTVAL) AS "IdSecuenciaEvaluacion",
								EVA."IdLimite",
								EVA."IdTipoOperacion",
								EVA."IdOperacion",
								EVA."IdMoneda",
								EVA."ConceptoExceso",
								EVA."MontoExcedido",
								EVA."MontoUtilizado",
								EVA."IdEstado",
								EVA."IdEstadoAutorizacion",
								EVA."IdTipoEvento",
								EVA."Prioridad",
								EVA."ConceptoEvaluacion",
								EVA."IdTipoEntidad",
								EVA."IdTipoInstrumento",
							    EVA."IdClasificacionTipoInstrumento",
							    EVA."IdTipoLimite",
							    EVA."MontoLimite",
							    EVA."MontoCalculado",
							    EVA."IdMonedaCalculo",
								EVA."IdCliente",
								EVA."IdPortafolio",
								EVA."IdCanalIngreso"
				 		FROM :v_Tab_SalidaEvaluaciones EVA 
				 		INNER JOIN :v_Tab_EvaluacionPRO_grupo GRU
				 		ON (GRU."IdOperacion"=EVA."IdOperacion" AND GRU."IdTipoOperacion"=EVA."IdTipoOperacion")
				 		WHERE EVA."IdLimite" > 0
				 		ORDER BY EVA."IdOperacion",	EVA."IdTipoOperacion";
						
				
				SELECT 	EVA."IdEvaluacionLimite",
						("BSCL.Plataforma.BD.Secuencias::LIM.IdSecuenciaEvaluacion".NEXTVAL) AS "IdSecuenciaEvaluacion",
						EVA."IdLimite",
						EVA."IdTipoOperacion",
						EVA."IdOperacion",
						0 AS "IdUsuarioAutoriza",
						EVA."IdMoneda",
						EVA."ConceptoExceso",
						EVA."MontoExcedido",
						EVA."MontoUtilizado",
						v_fechaHoraSistema AS "FechaIngreso",
						CURRENT_UTCTIMESTAMP AS "FechaCreacion",
						EVA."IdEstado",
						'0001-01-01 00:00:00' AS "FechaAutorizacion",
						EVA."IdEstadoAutorizacion",
						p_IdUsuario AS "IdUsuarioCreador",
						EVA."IdTipoEvento",
						EVA."Prioridad",
						EVA."ConceptoEvaluacion",
						EVA."IdTipoEntidad",
						EVA."IdTipoInstrumento",
					    EVA."IdClasificacionTipoInstrumento",
					    EVA."IdTipoLimite",
					    EVA."MontoLimite",
					    EVA."MontoCalculado",
					    EVA."IdMonedaCalculo",
						EVA."IdCliente",
						EVA."IdPortafolio",
						EVA."IdCanalIngreso"					    
		 		FROM :v_Tab_GrabarEvaluacion EVA ORDER BY EVA."IdEvaluacionLimite"	
				INTO "BSCL.Plataforma.BD::LIM.EvaluacionLimites"(
					   "IdEvaluacionLimite",
					   "IdSecuenciaEvaluacion",
					   "IdLimite",
					   "IdTipoOperacion",
					   "IdOperacion",
					   "IdUsuarioAutoriza",
					   "IdMoneda",
					   "ConceptoExceso",
					   "MontoExcedido",
					   "MontoUtilizado",
					   "FechaIngreso",
					   "FechaCreacion",
					   "IdEstado",
					   "FechaAutorizacion",
					   "IdEstadoAutorizacion",
					   "IdUsuarioCreador",
					   "IdTipoEvento",
					   "Prioridad",
					   "ConceptoEvaluacion",
					   "IdTipoEntidad",
					   "IdTipoInstrumento",
					   "IdClasificacionTipoInstrumento",
					   "IdTipoLimite",
					   "MontoLimite",
					   "MontoCalculado",
					   "IdMonedaCalculo",
					   "IdCliente",
					   "IdPortafolio",
					   "IdCanalIngreso"					   
					   );
			END IF;
			
			/*Se agregan operaciones que no cumplieron con los criterios para ser evaluados en la salida como OK */
			v_ConceptoEvaluacion:='';
			v_Tab_EvaluacionOK=
				SELECT	OPE."IdOperacion",
						OPE."IdTipoOperacion",
						OPE."IdTipoInstrumento",
						OPE."IdClasificacionTipoInstrumento",
						OPE."MontoUtilizado",
						'' AS "IdMoneda",
						0 AS "IdLimite",
						'' AS "IdTipoLimite",
						0 AS "MontoExcedido",
						'' AS "ConceptoExceso",						
						v_estadoOK AS "IdEstado",
						v_estadoAutOK AS "IdEstadoAutorizacion",
						'' AS "IdTipoEvento",
						0 AS "Prioridad",
						--'' AS "DocumentoIdentidad",
						v_ConceptoEvaluacion AS "ConceptoEvaluacion",
						0 AS "MontoLimite",
						0 AS "MontoCalculado",
						'' AS"IdMonedaCalculo",
						p_IdTipoEntidad		AS "IdTipoEntidad",
						OPE."IdCliente",
						OPE."IdPortafolio",
						OPE."IdCanalIngreso"		
				FROM :p_tablaOperacion OPE
				LEFT JOIN :v_Tab_SalidaEvaluaciones EVA
					ON (OPE."IdOperacion"=EVA."IdOperacion")
				WHERE  EVA."IdOperacion" IS NULL;
			
			v_Tab_SalidaEvaluaciones=SELECT * FROM :v_Tab_EvaluacionOK UNION ALL SELECT * FROM :v_Tab_SalidaEvaluaciones;
					
			/*FIN:APLICAR EVALUACION DE LIMITES*/
			
			---LLENAR TABLA DE RESULTADO PARA LA SALIDA
			--Enviar todos los que no requieren de una Autorizacion (Aceptado o rechazado)
			p_out_RegistroEvaluacionLimite = SELECT * from :v_Tab_SalidaEvaluaciones WHERE "IdEstadoAutorizacion" =v_estadoAutOK
												UNION ALL SELECT * FROM :p_out_RegistroEvaluacionLimite;	
													
			--Enviar todos los que requieren de una Autorizacion  (solo el con mayor prioridad se agrega a la salida)
			p_out_RegistroEvaluacionLimite = 
							SELECT EVA.* from :v_Tab_SalidaEvaluaciones EVA 
								INNER JOIN (SELECT "IdOperacion", MIN("Prioridad") AS "Prioridad" FROM :v_Tab_SalidaEvaluaciones	WHERE "IdEstadoAutorizacion" =v_estadoAutNoOK GROUP BY "IdOperacion" ) AS GRU
								ON (EVA."IdOperacion"=GRU."IdOperacion" AND EVA."Prioridad"=GRU."Prioridad")
							WHERE EVA."IdEstadoAutorizacion"=v_estadoAutNoOK
							UNION ALL SELECT * FROM :p_out_RegistroEvaluacionLimite;	
			--Enviar resumen segun prioridad				
			p_out_EstadoOperacion=	SELECT  IFNULL(PEN."IdOperacion",ACP."IdOperacion") as "IdOperacion",
											IFNULL(PEN."IdEstado",ACP."IdEstado") as "IdEstado"
												FROM       	
													(SELECT DISTINCT "IdOperacion","IdEstado" FROM :p_out_RegistroEvaluacionLimite
														WHERE "IdEstado"='ACP'
													) AS ACP
												FULL JOIN		
													(
													SELECT  DISTINCT EVA."IdOperacion",EVA."IdEstado" from :p_out_RegistroEvaluacionLimite EVA 
														LEFT OUTER JOIN "BSCL.Plataforma.BD::LIM.EstadoLim" EST
																			ON (EVA."IdEstado"= EST."IdEstado")
															INNER JOIN (SELECT EVL."IdOperacion", MAX(COALESCE(EST."Prioridad",0)) AS "Prioridad"
																		 FROM :p_out_RegistroEvaluacionLimite EVL
																		 LEFT OUTER JOIN "BSCL.Plataforma.BD::LIM.EstadoLim" EST
																			ON (EVL."IdEstado"= EST."IdEstado")
																		 WHERE EVL."IdEstado"<>'ACP'
																		 GROUP BY EVL."IdOperacion" ) AS GRU
															ON (EVA."IdOperacion"=GRU."IdOperacion" AND COALESCE(EST."Prioridad",0)=GRU."Prioridad")
															WHERE EVA."IdEstado"<>'ACP'

													) AS PEN	
												ON (ACP."IdOperacion"= PEN."IdOperacion");								
							
			--call "BSCL.Plataforma.BD.Servicios::Depuracion"(1,CURRENT_UTCTIMESTAMP,1,'BSCL.Plataforma.BD.Servicios::LIM.EvaluacionLimite',p_IdTipoEntidad,'TERMINO LLAMADA');							
						
			---GENERAR EVENTO DE AUTORIZACION SOLO CUANDO EL NEGOCIO NO LO ENVIA

				IF p_GeneraAutorizacion='S'	AND p_simularOperacion='N' THEN
					SELECT COUNT(1) INTO v_Count_Eval_Con_AUT FROM :v_Tab_SalidaEvaluaciones WHERE "IdEstadoAutorizacion" =v_estadoAutNoOK;

					IF v_Count_Eval_Con_AUT>0 AND p_GeneraAutorizacion='S' THEN
						------------------------------------------
						--Genera Evento de Autorizacion
						------------------------------------------
						DECLARE	v_Glosa							NVARCHAR(260);
						DECLARE v_out_idAuth 					bigint;
						DECLARE v_out_idAuthtable 				table ("IdEvento" BIGINT,"NumeroOperacionConcepto" 	NVARCHAR(40));
						DECLARE v_out_AuthAdvertenciasContador 	int;
						DECLARE v_out_AuthErroresContador 		int;
				
						v_out_AuthAdvertenciasDetalle	= select '' as "Codigo",'' as "RequiereAutorizacion",'' as "Descripcion" from dummy where 1=2;
						v_out_AuthErroresDetalle		= select '' as "Codigo",'' as "Descripcion" from dummy where 1=2;
						
						SELECT TRIM("Nombre") INTO v_NombreEvento FROM  "BSCL.Plataforma.BD.Vistas::AUT.ConsultarTipoEvento" WHERE "IdTipoEvento"=v_IdConcepto AND "IdEmpresa" = :p_IdEmpresa;
						SELECT TRIM("Nombre") INTO v_DescripcionModulo FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarModulo" WHERE "IdModulo"=p_IdModulo;
						SELECT v_NombreEvento || ' : '|| v_DescripcionModulo || ' / ' || 'MASIVO'  into  v_Glosa 
						FROM DUMMY;
					
						v_tablaEventos=SELECT 
										(CASE WHEN EVA."IdTipoEvento"='' THEN v_IdConcepto ELSE EVA."IdTipoEvento" END) AS "IdTipoEvento",              		 
									    p_IdModulo 	 AS "IdModuloSolicita",
									    p_IdUsuario	 AS "IdUsuarioSolicita",
									    TO_VARCHAR (v_fechaHoraSistema, 'YYYY-MM-DD') AS "Fecha",  
									    EVA."IdTipoOperacion" 		AS "IdTipoOperacion",
									    (CASE WHEN EVA."IdTipoEvento"='' THEN v_IdConcepto ELSE EVA."IdTipoEvento" END)	AS	"IdConcepto",
									    CAST(EVA."IdEvaluacionLimite" AS NVARCHAR(40)) 	AS  "NumeroOperacionConcepto",
									    TO_VARCHAR (v_fechaHoraSistema, 'YYYY-MM-DD') 			AS "FechaOperacionConcepto",
									    0  AS "IdCliente",
									    0  AS "IdFuncionario",
									    0  AS "IdProveedor",
									    '' AS "IdTipoProducto",
										'' AS "IdClasificacionTipoInstrumento",
										'' AS "IdTipoInstrumento",
										0  AS "IdInstrumento",
										'' AS "Nemo",
									    0  AS "Cantidad",
									    0  AS "Monto",
									    0  AS "Precio",
									    0  AS "Tasa",
									    '' AS "IdMoneda",
									   --''AS "Glosa"
									   (COALESCE(TIP."Nombre",'') || ' -> ' || CAST (EVA."IdOperacion" AS NVARCHAR(15)) || ' / Evaluación : ' || EVA."ConceptoEvaluacion")  AS "Glosa",
									   0 as "IdUnion" 
						FROM :v_Tab_GrabarEvaluacion EVA
							--LEFT JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarTipoOperacion" TIP
							--ON ( EVA."IdTipoOperacion" = TIP."IdTipoOperacion"  AND EVA."IdTipoInstrumento"=TIP."IdTipoInstrumento") 
							LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" TIP
							ON ( EVA."IdTipoOperacion" = TIP."IdTipoOperacion") 
						WHERE EVA."IdEstadoAutorizacion" =v_estadoAutNoOK;
					
					----------------------------------------------------------------------------------------------------------------							
			        v_tablaEventosDetalle = select  "IdEventoDetalle" as "IdUnion","NombreCampo","TipoDato","Valor","Clasificacion"
			   									from "BSCL.Plataforma.BD::AUT.EventoDetalle" 
			   									where "IdEventoDetalle" = 0;
			   		-----------------------------------------------------------------------------------------------------------------
						CALL"BSCL.Plataforma.BD.Servicios::AUT.CrearEventoAutorizacionMasivo"(
							p_IdEmpresa => p_IdEmpresa,
							p_IdUsuario => p_IdUsuario,
							p_IdModulo => p_IdModulo,
							p_UsaReglasNegocio=> :p_UsaReglasNegocio,
						    p_simularOperacion=> 'N',
							p_tablaEventos=> :v_tablaEventos,	
							p_tablaDetalle => :v_tablaEventosDetalle,
							--salida
							p_out_IdEvento => v_out_idAuthtable,
							p_out_AdvertenciasContador => v_out_AuthAdvertenciasContador,
							p_out_AdvertenciasDetalle =>  v_out_AuthAdvertenciasDetalle,
							p_out_ErroresContador =>  v_out_ErroresContador,
							p_out_ErroresDetalle =>  p_out_ErroresDetalle);
							
							--p_out_ErroresContador =>  v_out_AuthErroresContador,
							--p_out_ErroresDetalle =>  v_out_AuthErroresDetalle);
							

			            p_out_idAuthtable=select * from :v_out_idAuthtable;
			            
			            IF v_out_ErroresContador > 0 then
			            	SIGNAL SALIDA_CONTROLADA;
			            ELSE
							--Mensaje de Salida
		
							--v_hash= select '' as "Codigo",'' as "Valor" FROM DUMMY;
							--CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajeDiccionario" ('REQUIERE_AUTORIZACION',:v_hash,:v_erroroutmsg);
							p_out_AdvertenciasDetalle =  select 'LIMITE_REQUIERE_AUTORIZACION' as "Codigo",'S' as "RequiereAutorizacion",'LIMITE_REQUIERE_AUTORIZACION' as "Descripcion" from dummy;
														 --UNION ALL  select * from :p_out_AdvertenciasDetalle;
							--SIGNAL SALIDA_CONTROLADA;
						END if;

					END IF;

				END IF;---fin  p_GeneraAutorizacion='S'

			
			
			SIGNAL SALIDA_CONTROLADA;		
		END;		
					
		--------------------
		--FIN SP 
		--------------------
	END;	
END;