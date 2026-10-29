SET SCHEMA BSCL;

DROP PROCEDURE "BSCL.Normativa.BD.Servicios::TRI.DJ1894.CargarFondos";

CREATE PROCEDURE "BSCL.Normativa.BD.Servicios::TRI.DJ1894.CargarFondos"
(
	-- ID empresa del login.
    IN p_IdEmpresa 	BIGINT		 ,
    -- ID usuario del login.
    IN p_IdUsuario 	BIGINT		 ,
    -- ID modulo invocador.
    IN p_IdModulo	NVARCHAR(3)	 ,
    
    IN p_Version	NVARCHAR(10),
    	
	OUT p_out_AdvertenciasContador 		INT,
	OUT p_out_AdvertenciasDetalle 		TABLE ("IdInterno"	BIGINT,"Codigo" NVARCHAR(100),"RequiereAutorizacion" NVARCHAR(40),"Descripcion" NVARCHAR(520)),
	OUT p_out_ErroresContador 			INT,
	OUT p_out_ErroresDetalle 			TABLE ("IdInterno"	BIGINT,"Codigo" NVARCHAR(100),"Descripcion" NVARCHAR(520))
)
	LANGUAGE sqlscript
    SQL security invoker
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
		v_out_ErroresDetalle = SELECT 0 as "IdInterno" ,'' as "Codigo",'' as "ValorParametro",'' as "NombreParametro" from DUMMY where 1=2;
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
			DECLARE v_resultadoRegla 	NVARCHAR(40);
			DECLARE v_FechaSistema		NVARCHAR(10);
			
			CALL "BSCL.Plataforma.BD.Servicios::Depuracion"(1,CURRENT_UTCTIMESTAMP,1,'DJ1894.CargarFondos','INICIO','INICIO');
			
			CALL "BSCL.Plataforma.BD.Reglas::ServicioCumpleRequisitosBasicos"(:p_IdEmpresa,:p_IdUsuario,:p_IdModulo,:v_out_ErroresDetalle_Basicos,:v_resultadoRegla);
			
			IF :v_resultadoRegla = 'RECH' THEN
				v_out_ErroresDetalle = SELECT 0 as "IdInterno", "Codigo", '' as "ValorParametro",'' as "NombreParametro" FROM :v_out_ErroresDetalle_Basicos;
				SIGNAL SALIDA_CONTROLADA; 	
			END IF;
			
			CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaSistema" (:p_IdEmpresa,v_FechaSistema);
			
			v_fondos_insertar = SELECT FONDO."IdFondo",
									   'DJ1894' AS "Esquema",
									   :p_Version AS "Version"
								FROM "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarFondoCombobox" FONDO
								LEFT JOIN "BSCL.Normativa.BD::TRI.Fondos" TRI_FONDOS ON (FONDO."IdFondo" = TRI_FONDOS."IdFondo" AND TRI_FONDOS."Esquema" = 'DJ1894' AND TRI_FONDOS."Version" = :p_Version)
								WHERE FONDO."FondoNormativo" = 'S' AND COALESCE(TRI_FONDOS."IdFondo",'') = '';
			
			v_fondos_eliminar = SELECT FONDO."IdFondo",
									   TRI_FONDOS."Esquema",
									   TRI_FONDOS."Version"
								FROM "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarFondoCombobox" FONDO
								INNER JOIN "BSCL.Normativa.BD::TRI.Fondos" TRI_FONDOS ON (FONDO."IdFondo" = TRI_FONDOS."IdFondo" AND TRI_FONDOS."Esquema" = 'DJ1894' AND TRI_FONDOS."Version" = :p_Version)
								WHERE FONDO."FondoNormativo" = 'N';
			
			INSERT INTO "BSCL.Normativa.BD::TRI.Fondos"("IdFondo","Esquema","Version","PorcentajeAccionario","Observacion","IdEstado")
			SELECT "IdFondo","Esquema","Version",0 AS "PorcentajeAccionario",'' AS "Observacion",'ACT' AS "IdEstado"
			FROM :v_fondos_insertar;
			
			DELETE FROM "BSCL.Normativa.BD::TRI.Fondos" TRI_FONDOS
			WHERE EXISTS(SELECT 1 FROM :v_fondos_eliminar FONDOS WHERE TRI_FONDOS."IdFondo" = FONDOS."IdFondo" AND TRI_FONDOS."Esquema" = FONDOS."Esquema" AND TRI_FONDOS."Version" = FONDOS."Version");
			
			v_log = SELECT	0 AS "IdCabeceraLog"
					,'INGRESO' AS "IdTipoLog"
					,:p_IdModulo  AS "IdModulo"
					,:p_IdUsuario AS "IdUsuario"
					,:p_IdEmpresa AS "IdEmpresa"
					,:v_FechaSistema AS "FechaSistemaLog"
					,:p_IdUsuario AS "IdUsuarioAtendido"
					,0 AS "IdCliente"
					,'' AS "EstadoCliente"
					,'' AS "NombreCliente"
					,'' AS "IdTipoDocumentoIdentidad"
					,'' AS "IdDocumentoIdentidad"
					,0 AS "IdPortafolio"
					,'' AS "EstadoPortafolio"
					,'INSERTAR_FONDO_DJ1894' AS "IdTipoRegistro"
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
					,"IdFondo" AS "Nemo"
					,0 AS "IdProceso"
					,'' AS "EstadoProceso"
					,'' AS "NombreProceso"
				FROM :v_fondos_insertar
				UNION ALL 
				SELECT	0 AS "IdCabeceraLog"
					,'MODIFICACION' AS "IdTipoLog"
					,:p_IdModulo  AS "IdModulo"
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
					,'ELIMINAR_FONDO_DJ1894' AS "IdTipoRegistro"
					,0 AS "IdRegistro"
					,'' AS "EstadoRegistro"
					,'' AS "IdTipoOperacion"
					,0 AS "Folio"
					,0 AS "Cantidad"
					,0 AS "PrecioTasa"
					,0 AS "Monto"
					,'' AS "IdMonedaOrigen"
					,'' AS "IdMonedaLiquidacion"
					,'' AS "TipoCambio"
					,"IdFondo" AS "Nemo"
					,0 AS "IdProceso"
					,'' AS "EstadoProceso"
					,'' AS "NombreProceso"
				FROM :v_fondos_eliminar;
			
			v_DETALLELOG = SELECT 0 AS "IdCabeceraLog",'' as "SubRegistro", '' as "CampoRegistro", '' as "ValorAntiguo", '' as "ValorNuevo" from dummy where 1=2;
			
			CALL "BSCL.Plataforma.BD.Servicios::AgregarLogMasivo" (p_Log => :v_log,	p_LogDetalle => :v_DETALLELOG);
			
			CALL "BSCL.Plataforma.BD.Servicios::Depuracion"(1,CURRENT_UTCTIMESTAMP,1,'DJ1894.CargarFondos','FIN','FIN');
		
			SIGNAL SALIDA_CONTROLADA;
		
		END;
	END;
END;
