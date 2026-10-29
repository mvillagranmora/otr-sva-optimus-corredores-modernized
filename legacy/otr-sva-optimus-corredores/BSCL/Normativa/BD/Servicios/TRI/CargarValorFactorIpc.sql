SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Normativa.BD.Servicios::TRI.CargarValorFactorIpc";

--  Proposito: Cargar Valor Factor IPC.
--  Autor: Gat-Blac 
--  Fecha Creacion: 15/03/2017

CREATE PROCEDURE "BSCL.Normativa.BD.Servicios::TRI.CargarValorFactorIpc"(
	IN p_IdEmpresa 								BIGINT,
	IN p_IdUsuario 								BIGINT,
	IN p_IdModulo 								NVARCHAR(3),
    IN p_SimularOperacion 						NVARCHAR(1),
    IN p_UsaReglasNegocio 						NVARCHAR(1),
    OUT p_out_AdvertenciasContador 				INT,
	OUT p_out_AdvertenciasDetalle 				TABLE ("IdInterno"	BIGINT,"Codigo" NVARCHAR(100),"RequiereAutorizacion" NVARCHAR(40),"Descripcion" NVARCHAR(520)),
	OUT p_out_ErroresContador 					INT,
	OUT p_out_ErroresDetalle 					TABLE ("IdInterno"	BIGINT,"Codigo" NVARCHAR(100),"Descripcion" NVARCHAR(520))
)
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
AS
BEGIN
	--------------------
	--DECLARE Manejo de Errores
	--------------------
	DECLARE v_out_AdvertenciasDetalle 	TABLE ("IdInterno" BIGINT, "Codigo" nvarchar(100),"RequiereAutorizacion" NVARCHAR(1),"ValorParametro" nvarchar(100),"NombreParametro" NVARCHAR(40));
	DECLARE v_out_ErroresDetalle  		TABLE ("IdInterno" BIGINT, "Codigo" nvarchar(100),"ValorParametro" nvarchar(100),"NombreParametro" NVARCHAR(40));
	BEGIN
		v_out_AdvertenciasDetalle = SELECT 0 as "IdInterno" ,'' as "Codigo",'N' as "RequiereAutorizacion",'' as "ValorParametro",'' as "NombreParametro" from DUMMY where 1=2;
		v_out_ErroresDetalle 	  = SELECT 0 as "IdInterno" ,'' as "Codigo",'' as "ValorParametro",'' as "NombreParametro" from DUMMY where 1=2;
	END;
	
	BEGIN
		DECLARE SALIDA_CONTROLADA CONDITION;
		DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA  
		BEGIN
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
			
			p_out_ErroresDetalle = SELECT 	"errores"."IdInterno",
											"errores"."Codigo",
											"msj"."Descripcion"
									FROM :v_out_ErroresDetalle AS "errores"
									INNER JOIN :v_out_ErroresDetalleMensajes AS "msj" ON "errores"."Codigo" = "msj"."Codigo";
			
			p_out_AdvertenciasDetalle = SELECT 	"adv"."IdInterno",
												"adv"."Codigo",
												"adv"."RequiereAutorizacion",
												"msj"."Descripcion"
										FROM :v_out_AdvertenciasDetalle AS "adv"
										INNER JOIN :v_out_ErroresDetalleMensajes AS "msj" ON "adv"."Codigo" = "msj"."Codigo"; 
		END;
		
		p_out_AdvertenciasContador	:= 0;
		p_out_ErroresContador		:= 0;
		
		BEGIN
		DECLARE v_resultadoRegla 			NVARCHAR(40);
		DECLARE v_FechaSistema				NVARCHAR(10);
		
		CALL "BSCL.Plataforma.BD.Servicios::Depuracion"(:p_IdEmpresa,CURRENT_UTCTIMESTAMP,:p_IdUsuario,'BSCL.Normativa.BD.Servicios::TRI.CargarValorFactorIpc','INICIO','INICIO');
		
		CALL "BSCL.Plataforma.BD.Reglas::ServicioCumpleRequisitosBasicos"(:p_IdEmpresa,:p_IdUsuario,:p_IdModulo,:v_out_ErroresDetalle_Basicos,:v_resultadoRegla);
		
		IF :v_resultadoRegla = 'RECH' THEN
			v_out_ErroresDetalle = SELECT 0 as "IdInterno", "Codigo", '' as "ValorParametro",'' as "NombreParametro" FROM :v_out_ErroresDetalle_Basicos;
			SIGNAL SALIDA_CONTROLADA; 	
		END IF;
		
		CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaSistema" (:p_IdEmpresa,v_FechaSistema);
		
		v_valores_ipc = SELECT IPC."Agno"
							   ,IPC."AgnoMes"
							   ,:v_FechaSistema AS "Fecha"
							   ,IPC."Valor"
							   ,ADD_MONTHS(TO_DATE(IPC."AgnoMes" || '-01','YYYY-MM-DD'),1) AS "FechaIPC"
							   ,TO_CHAR(ADD_MONTHS(TO_DATE(IPC."AgnoMes" || '-01','YYYY-MM-DD'),1),'YYYY') AS "AgnoIPC"
							   ,TO_CHAR(ADD_MONTHS(TO_DATE(IPC."AgnoMes" || '-01','YYYY-MM-DD'),1),'YYYY-MM') AS "AgnoMesIPC"
						FROM "BSCL.Plataforma.BD::MER.ParametroFinanciero" IPC
						WHERE "IdParametroFinanciero" = 'IPC' AND COALESCE(IPC."Agno",'') != '';
		
		
		v_valores_ipc_faltantes = 	SELECT IPC."Agno"
										   ,IPC."AgnoMes"
										   ,IPC."Fecha"
										   ,IPC."Valor"
										   ,IPC."FechaIPC"
										   ,IPC."AgnoIPC"
										   ,IPC."AgnoMesIPC"
									FROM :v_valores_ipc IPC
									LEFT JOIN "BSCL.Normativa.BD::TRI.FactorIpc" F_IPC ON (F_IPC."Agno" = IPC."AgnoIPC" AND IPC."AgnoMesIPC" = F_IPC."AgnoMes")
									WHERE COALESCE(F_IPC."AgnoMes",'') = '';
		
		v_valores_ipc_maestro = SELECT 	IPC."Agno"
							   			,IPC."AgnoMes"
							   			,IPC."Valor"
							   FROM "BSCL.Plataforma.BD::MER.ParametroFinanciero" IPC
							   WHERE IPC."AgnoMes" = (IPC."Agno" || '-11');
							   
		v_pre_factores = SELECT "AgnoMesIPC",
								"AgnoIPC",
								CASE WHEN TO_CHAR("FechaIPC",'MM') = '01' THEN 'ENERO'
									 WHEN TO_CHAR("FechaIPC",'MM') = '02' THEN 'FEBRERO'
									 WHEN TO_CHAR("FechaIPC",'MM') = '03' THEN 'MARZO'
									 WHEN TO_CHAR("FechaIPC",'MM') = '04' THEN 'ABRIL'
									 WHEN TO_CHAR("FechaIPC",'MM') = '05' THEN 'MAYO'
									 WHEN TO_CHAR("FechaIPC",'MM') = '06' THEN 'JUNIO'
									 WHEN TO_CHAR("FechaIPC",'MM') = '07' THEN 'JULIO'
									 WHEN TO_CHAR("FechaIPC",'MM') = '08' THEN 'AGOSTO'
									 WHEN TO_CHAR("FechaIPC",'MM') = '09' THEN 'SEPTIEMBRE'
									 WHEN TO_CHAR("FechaIPC",'MM') = '10' THEN 'OCTUBRE'
									 WHEN TO_CHAR("FechaIPC",'MM') = '11' THEN 'NOVIEMBRE'
									 WHEN TO_CHAR("FechaIPC",'MM') = '12' THEN 'DICIEMBRE'
									 ELSE '' 
								END AS "DescMes",
								:v_FechaSistema AS "Fecha",
								IPC."Valor"
						  FROM :v_valores_ipc_faltantes IPC;
						  
		
		v_factores_ipc = SELECT IPC."AgnoMesIPC",
								CASE WHEN ROUND (FJO."Valor"/IPC."Valor",3)  > 1 THEN ROUND (FJO."Valor"/IPC."Valor",3) ELSE 1 END AS "Factor",
								IPC."AgnoIPC",
								IPC."DescMes",
								IPC."Fecha",
								IPC."Valor",
								TO_NUMBER(IPC."AgnoIPC") + 1 AS "AgnoTributario"
						  FROM :v_pre_factores IPC
						  INNER JOIN :v_valores_ipc_maestro FJO ON (FJO."Agno" = IPC."AgnoIPC");

						  		
		INSERT INTO "BSCL.Normativa.BD::TRI.FactorIpc"
		("Agno","AgnoMes","DescMes","Factor","Fecha","AgnoTributario","ValorIpc")
		SELECT "AgnoIPC","AgnoMesIPC","DescMes","Factor","Fecha","AgnoTributario","Valor"
		FROM :v_factores_ipc;
		
		v_log = SELECT 
						0 AS "IdCabeceraLog"
						,'MODIFICACION' AS "IdTipoLog"
						,'NOR' AS "IdModulo"
						,:p_IdUsuario AS "IdUsuario"
						,:p_IdEmpresa AS "IdEmpresa"
						,:v_FechaSistema AS "FechaSistemaLog"
						,:p_IdUsuario AS "IdUsuarioAtendido"
						,0 AS "IdPersona"
						,0 AS "IdCliente"
						,'' AS "EstadoCliente"
						,'' AS "NombreCliente"
						,'' AS "IdTipoDocumentoIdentidad"
						,'' AS "IdDocumentoIdentidad"
						,0 AS "IdPortafolio"
						,'' AS "EstadoPortafolio"
						,'CARGA_VALOR_FACTOR_IPC' AS "IdTipoRegistro"
						,0 AS "IdRegistro"
						,'ACT' AS "EstadoRegistro"
						,'' AS "IdTipoOperacion"
						,0 AS "Folio"
						,0 AS "Cantidad"
						,0 AS "PrecioTasa"
						,0 AS "Monto"
						,'' AS "IdMonedaOrigen"
						,'' AS "IdMonedaLiquidacion"
						,'' AS "TipoCambio"
						,'' AS "Nemo"
						,0 AS "IdProceso"
						,'' AS "EstadoProceso"
						,'' AS "NombreProceso"
					FROM :v_factores_ipc;
			
		v_DETALLELOG = SELECT 0 AS "IdCabeceraLog",'' as "SubRegistro", '' as "CampoRegistro", '' as "ValorAntiguo", '' as "ValorNuevo" from dummy where 1=2;
		
		CALL "BSCL.Plataforma.BD.Servicios::AgregarLogMasivo" (p_Log => :v_log,	p_LogDetalle => :v_DETALLELOG);
		
		CALL "BSCL.Plataforma.BD.Servicios::Depuracion"(:p_IdEmpresa,CURRENT_UTCTIMESTAMP,:p_IdUsuario,'BSCL.Normativa.BD.Servicios::TRI.CargarValorFactorIpc','FIN','FIN');
		
		SIGNAL SALIDA_CONTROLADA;
		
		END;
	END;	
END;
