		
SET SCHEMA "BSCL";

DROP PROCEDURE  "BSCL.Mesa.BD.Servicios::CrearMonedaPorProducto";

CREATE PROCEDURE 
-- 
--  Proposito: 
--  Fecha Creacion: 01/07/2016
--  
"BSCL.Mesa.BD.Servicios::CrearMonedaPorProducto" ( 
	IN p_IdEmpresa         BIGINT DEFAULT 1,
    IN p_IdUsuario         BIGINT DEFAULT 1,
    IN p_IdModulo          NVARCHAR(3) DEFAULT '',
	IN p_MonedaPorProducto TABLE(
								"IdInstrumento" BIGINT,
								"IdEmpresa" BIGINT,
								"Nemotecnico"	NVARCHAR(6), 
								"IdTipoProducto" NVARCHAR(4),
								"IdTipoInstrumento" NVARCHAR(3), 
								"IdMonedaPrincipal"	NVARCHAR(3),
    							"IdMonedaSecundaria"	NVARCHAR(3),
    							"IdEstado" NVARCHAR(3),
								"FeriadoMonedaPrincipal" NVARCHAR(1)
    							),
									
	--Salida
   	 -- Salidas para mensajes de advertencias y errores
	OUT p_out_AdvertenciasContador			INT,
	OUT p_out_AdvertenciasDetalle			TABLE ("IdInterno"	BIGINT,"Codigo" NVARCHAR(100),"RequiereAutorizacion" NVARCHAR(40),"Descripcion" NVARCHAR(520)),	
	OUT p_out_ErroresContador 				INT,
	OUT p_out_ErroresDetalle 				TABLE ("IdInterno"	BIGINT,"Codigo" NVARCHAR(100),"Descripcion" NVARCHAR(520))
   ) 
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
AS
BEGIN 
	DECLARE cantidad INT;
	--------------------
	--DECLARE Manejo de Errores
	--------------------
	DECLARE SALIDA_CONTROLADA CONDITION; 
	DECLARE v_errorhashmsg table ("Codigo" nvarchar(100),"Valor" nvarchar(520));
	DECLARE v_erroroutmsg  nvarchar(520);	        	
	DECLARE v_out_AdvertenciasDetalle  TABLE ("IdInterno" BIGINT, "Codigo" nvarchar(100),"RequiereAutorizacion" NVARCHAR(1),"ValorParametro" nvarchar(100),"NombreParametro" NVARCHAR(40))
											DEFAULT SELECT  0  AS "IdInterno"
													,'' AS "Codigo"
													,'' AS "RequiereAutorizacion"
													,'' AS "ValorParametro"
													,'' AS "NombreParametro"
													FROM DUMMY
													WHERE 1=2;
	DECLARE v_out_ErroresDetalle  		TABLE ("IdInterno" BIGINT, "Codigo" nvarchar(100),"ValorParametro" nvarchar(100),"NombreParametro" NVARCHAR(40))
											DEFAULT SELECT 0  AS "IdInterno"
														   ,'' AS "Codigo"
														   ,'' AS "ValorParametro"
														   ,'' AS "NombreParametro"
													FROM DUMMY
													WHERE 1=2;
	
	DECLARE v_count_operaciones				BIGINT;
	
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
									errores."Codigo",
									msj."Descripcion"
								FROM :v_out_ErroresDetalle errores
								INNER JOIN :v_out_ErroresDetalleMensajes msj on errores."Codigo" = msj."Codigo";
								
		p_out_AdvertenciasDetalle = SELECT adv."IdInterno",
									adv."Codigo",
									adv."RequiereAutorizacion",
									msj."Descripcion"
								FROM :v_out_AdvertenciasDetalle adv
								INNER JOIN :v_out_ErroresDetalleMensajes msj on adv."Codigo" = msj."Codigo";
				 						 	
	END;
	p_out_AdvertenciasContador	:= 0;
	p_out_ErroresContador		:= 0;
	v_out_AdvertenciasDetalle = SELECT 0 as "IdInterno" ,'' as "Codigo",'N' as "RequiereAutorizacion",'' as "ValorParametro",'' as "NombreParametro" from dummy where 1=2;
	v_out_ErroresDetalle = SELECT 0 as "IdInterno" ,'' as "Codigo",'' as "ValorParametro",'' as "NombreParametro" from dummy where 1=2;
	
    BEGIN
    
    
		--* Verifica que el registro no exista
		--* ----------------------------------
		v_TblErrores = 
			SELECT	TBL."IdInstrumento",
					TBL."Nemotecnico",
					TBL."IdTipoInstrumento",
					TBL."IdMonedaPrincipal",
					TBL."IdMonedaSecundaria"
			FROM	"BSCL.Mesa.BD::MES.InstrumentoMoneda" AS TBL,
					:p_MonedaPorProducto AS TMP
			WHERE	TBL."IdEmpresa" = TMP."IdEmpresa"
					AND TBL."Nemotecnico" = TMP."Nemotecnico"
					AND TBL."IdTipoProducto" = TMP."IdTipoProducto"
					AND TBL."IdTipoInstrumento" = TMP."IdTipoInstrumento"
					AND TBL."IdMonedaPrincipal"	= TMP."IdMonedaPrincipal"	
					AND TBL."IdMonedaSecundaria" = TMP."IdMonedaSecundaria";


		SELECT 	COUNT(1) AS "NroRegistros" 
		INTO	v_count_operaciones
		FROM 	:v_TblErrores;

		IF v_count_operaciones > 0 THEN
			v_out_ErroresDetalle = 	
				SELECT	"IdInstrumento" AS "IdInterno",
						'PRODUCTO_MONEDA_YA_EXISTE' AS "Codigo",
						'' AS "ValorParametro",
						'' AS "NombreParametro" 
				FROM	:v_TblErrores;
			SIGNAL SALIDA_CONTROLADA;
		END IF;
    
		v_tblMonedaPorProducto_ = select * from "BSCL.Mesa.BD::MES.InstrumentoMoneda" where 1=2;
		v_tblMonedaPorProducto_ =
			SELECT 
			"IdInstrumento"
			,"IdEmpresa"
			,"Nemotecnico"
			,"IdTipoProducto"
			,"IdTipoInstrumento"
			,"IdMonedaPrincipal"
			,"IdMonedaSecundaria"
			,"IdEstado"
			,"FeriadoMonedaPrincipal"
			,0 AS "IdInstrumentoMoneda"
			FROM :p_MonedaPorProducto;
	
		CALL "BSCL.Mesa.BD.Reglas::ServicioCumpleRequisitosCrearMonedaPorProducto"(:v_tblMonedaPorProducto_,:v_out_SalidaDetalle,:v_out_Salida);
	
		v_out_AdvertenciasDetalle = SELECT "IdInstrumento" as "IdInterno" ,"Codigo",'N' as "RequiereAutorizacion","ValorParametro","NombreParametro"
										FROM :v_out_SalidaDetalle
										WHERE "Resultado" = 'ADV'
										UNION ALL
										SELECT "IdInstrumento" ,"Codigo",'S' as "RequiereAutorizacion","ValorParametro","NombreParametro"
										FROM :v_out_SalidaDetalle
										WHERE "Resultado" = 'AUT';
		v_out_ErroresDetalle = 		SELECT "IdInstrumento" as "IdInterno" ,"Codigo","ValorParametro","NombreParametro"
										FROM :v_out_SalidaDetalle
										WHERE "Resultado" = 'ERR';
								
		v_operaciones = SELECT mpp.* 
					FROM :v_tblMonedaPorProducto_ mpp
					INNER JOIN :v_out_Salida SD on mpp."IdInstrumento" = SD."IdInstrumento" and SD."Resultado" IN ('ADV','AUT','OK');

		SELECT COUNT(1) as "count" into v_count_operaciones
		FROM :v_operaciones;
		
		if (v_count_operaciones = 0) THEN
			SIGNAL SALIDA_CONTROLADA;
		END IF;
	    --************************************************************************
		--*INSERT TABLA "BSCL.Mesa.BD::MES.TipoInstrumentoMoneda"  *
		--************************************************************************
		v_tblMonedaPorProducto_ = 
			SELECT 
				"IdInstrumento",
				"IdEmpresa",
				"Nemotecnico", 
				"IdTipoProducto",
				"IdTipoInstrumento", 
				"IdMonedaPrincipal",
				"IdMonedaSecundaria",
				"IdEstado",
				"FeriadoMonedaPrincipal",
				"BSCL.Mesa.BD.Secuencias::InstrumentoMoneda".NEXTVAL AS "IdInstrumentoMoneda"
			FROM :v_tblMonedaPorProducto_;
			
			INSERT INTO "BSCL.Mesa.BD::MES.InstrumentoMoneda"
				("IdInstrumento","IdEmpresa","Nemotecnico","IdTipoProducto","IdTipoInstrumento","IdMonedaPrincipal","IdMonedaSecundaria","IdEstado", "FeriadoMonedaPrincipal","IdInstrumentoMoneda")
			SELECT 
			"IdInstrumento",
			"IdEmpresa",
			"Nemotecnico", 
			"IdTipoProducto",
			"IdTipoInstrumento", 
			"IdMonedaPrincipal",
			"IdMonedaSecundaria",
			"IdEstado",
			"FeriadoMonedaPrincipal",
			"IdInstrumentoMoneda"
			FROM :v_tblMonedaPorProducto_;

		
		--*******************************
		
		--LOG DE AUDITORIA (Inicio)
		BEGIN
			DECLARE p_IdLog BIGINT;
			DECLARE v_fechaHoraSistema NVARCHAR(40);

			CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema"(
				p_IdEmpresa => p_IdEmpresa,
				p_out_FechaHoraSistema => v_fechaHoraSistema
			);
			
			v_datos_log = SELECT 
								"BSCL.Plataforma.BD.Secuencias::Log".NEXTVAL AS "IdCabeceraLog",
								"IdInstrumento",
								"IdEmpresa",
								"Nemotecnico", 
								"IdTipoProducto",
								"IdTipoInstrumento", 
								"IdMonedaPrincipal",
								"IdMonedaSecundaria",
								"IdEstado",
								"FeriadoMonedaPrincipal",
								"IdInstrumentoMoneda"
						  FROM :v_tblMonedaPorProducto_;
							
			v_log = SELECT 	"IdCabeceraLog"
							,'INGRESO' AS "IdTipoLog"
							,:p_IdModulo AS "IdModulo"
							,:p_IdUsuario AS "IdUsuario"
							,:p_IdEmpresa AS "IdEmpresa"
							,TO_VARCHAR (COALESCE(:v_fechaHoraSistema, '0001-01-01'), 'YYYY-MM-DD') as "FechaSistemaLog"
							,:p_IdUsuario AS "IdUsuarioAtendido"
							,0  AS "IdPersona"
							,0  AS "IdCliente"
							,'' AS "EstadoCliente"
							,'' AS "NombreCliente"
							,'' AS "IdTipoDocumentoIdentidad"
							,'' AS "IdDocumentoIdentidad"
							,0  AS "IdPortafolio"
							,'' AS "EstadoPortafolio"
							,'CREAR_MONEDA_PRODUCTO' AS "IdTipoRegistro"
							,"IdInstrumentoMoneda" AS "IdRegistro"
							,'' AS "EstadoRegistro"
							,'' AS "IdTipoOperacion"
							,0 AS "Folio"
							,0 AS "Cantidad"
							,0 AS "PrecioTasa"
							,0 AS "Monto"
							,'' AS "IdMonedaOrigen"
							,'' AS "IdMonedaLiquidacion"
							,'' AS "TipoCambio"
							,"Nemotecnico" AS "Nemo"
							,0  AS "IdProceso" 
							,'' AS "EstadoProceso"
							,'CrearMonedaPorProducto' AS "NombreProceso"
					FROM :v_datos_log;

			v_LogDetalle = 
						SELECT
							"IdCabeceraLog" AS "IdCabeceraLog"
							,'CREAR_MONEDA_PRODUCTO' AS "SubRegistro"
							,'Tipo Producto' AS "CampoRegistro"
							,'' AS "ValorAntiguo"
							,"IdTipoProducto" AS "ValorNuevo"
						FROM :v_datos_log
						WHERE COALESCE("IdTipoProducto", '') <> ''
						
						UNION ALL
						
						SELECT
							"IdCabeceraLog" AS "IdCabeceraLog"
							,'CREAR_MONEDA_PRODUCTO' AS "SubRegistro"
							,'Tipo Instrumento' AS "CampoRegistro"
							,'' AS "ValorAntiguo"
							,"IdTipoInstrumento" AS "ValorNuevo"
						FROM :v_datos_log
						WHERE COALESCE("IdTipoInstrumento", '') <> ''
						
						UNION ALL
						
						SELECT
							"IdCabeceraLog" AS "IdCabeceraLog"
							,'CREAR_MONEDA_PRODUCTO' AS "SubRegistro"
							,'Moneda Principal' AS "CampoRegistro"
							,'' AS "ValorAntiguo"
							,"IdMonedaPrincipal" AS "ValorNuevo"
						FROM :v_datos_log
						WHERE COALESCE("IdMonedaPrincipal", '') <> ''
						
						UNION ALL
						
						SELECT
							"IdCabeceraLog" AS "IdCabeceraLog"
							,'CREAR_MONEDA_PRODUCTO' AS "SubRegistro"
							,'Moneda Secundaria' AS "CampoRegistro"
							,'' AS "ValorAntiguo"
							,"IdMonedaSecundaria" AS "ValorNuevo"
						FROM :v_datos_log
						WHERE COALESCE("IdMonedaSecundaria", '') <> ''
						
						UNION ALL
						
						SELECT
							"IdCabeceraLog" AS "IdCabeceraLog"
							,'CREAR_MONEDA_PRODUCTO' AS "SubRegistro"
							,'Feriado Moneda Principal' AS "CampoRegistro"
							,'' AS "ValorAntiguo"
							,"FeriadoMonedaPrincipal" AS "ValorNuevo"
						FROM :v_datos_log
						WHERE COALESCE("FeriadoMonedaPrincipal", '') <> '';
						
			CALL "BSCL.Plataforma.BD.Servicios::AgregarLogMasivo" (p_Log => :v_log,	p_LogDetalle => :v_LogDetalle);
		END;
		--LOG DE AUDITORIA (Fin)
	END;
END;
