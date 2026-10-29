SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Plataforma.BD.Servicios::TAS.CargarArchivoTasas";

-- 
--  Proposito: Servicio que realiza la carga del archivo de tasas. Este archivo es el que viene de vuelta.
--  Autor: MMACHMAR - BCS
--  Fecha Creacion: 06/09/2016  
-- 

CREATE PROCEDURE "BSCL.Plataforma.BD.Servicios::TAS.CargarArchivoTasas" (
	-- Parametros entrada comunes en todos los servicios publicos
	IN p_IdEmpresa			BIGINT,
	IN p_IdUsuario			BIGINT,
	IN p_IdModulo			NVARCHAR(3),
	IN p_UsaReglasNegocio	NVARCHAR(1),	-- S/N se usa en caso de servicios anidados
	
	-- Parametros entrada CABECERA
	IN p_Archivo						NVARCHAR(520),
	IN p_IdClasificacionTipoInstrumento NVARCHAR(3),
	IN p_FechaCreSol					NVARCHAR(10),
	IN p_IdTipoTasa						NVARCHAR(40),
	IN p_Fuente							NVARCHAR(40),
	IN p_Rut							NVARCHAR(40),
	IN p_Fecha							NVARCHAR(10),
	IN p_Hora							NVARCHAR(8),
	IN p_TipoArchivo					NVARCHAR(3),
	
	-- Parametros entrada DETALLE
	IN p_DataTasas	TABLE(	"Nemo"			NVARCHAR(40),
							"IdMoneda"		NVARCHAR(3),
							"Tasa"			DECIMAL(26,8),
							"TasaEstimada"	DECIMAL(26,8),
							"Plazo"			BIGINT),
							
	-- Parametros salida
	OUT p_out_ResultadoProceso		TABLE("IdPrecioTasa" BIGINT, "Campos" NVARCHAR(100), "Valores" NVARCHAR(100)),
	OUT p_out_AdvertenciasContador	INT,
	OUT p_out_AdvertenciasDetalle	TABLE ("Codigo" NVARCHAR(100),"RequiereAutorizacion" NVARCHAR(40),"Descripcion" NVARCHAR(5000)),	
	OUT p_out_ErroresContador		INT,   
	OUT p_out_ErroresDetalle		TABLE ("Codigo" NVARCHAR(100),"Descripcion" NVARCHAR(5000))
)  	
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER 
DEFAULT SCHEMA "BSCL" 
AS

BEGIN
  	--------------------
	--DECLARE Manejo de Errores
	--------------------	
	DECLARE SALIDA_CONTROLADA_NO_DATA 	CONDITION;
	DECLARE SALIDA_CONTROLADA 			CONDITION; 
	DECLARE v_errorcuenta 				INT DEFAULT 0;  
	DECLARE v_errorhashmsg 				table ("Codigo" NVARCHAR(100),"Valor" NVARCHAR(5000));
	DECLARE v_erroroutmsg  				NVARCHAR(520);	        	
	DECLARE v_ErrorNoData  				NVARCHAR(100);
	
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA	 
	BEGIN
		--salida/error/advertencia controlado por este servicio
		select count(1) into p_out_ErroresContador from :p_out_ErroresDetalle;
		select count(1) into p_out_AdvertenciasContador from :p_out_AdvertenciasDetalle;		 					 	
	END;	
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA_NO_DATA     
	BEGIN
		v_hash= SELECT '' as "Codigo",'' as "Valor" FROM DUMMY;	
		CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajeDiccionario" (:v_ErrorNoData,:v_hash,:v_erroroutmsg);
		p_out_ErroresDetalle =  SELECT :v_ErrorNoData  as "Codigo",:v_erroroutmsg as "Descripcion" FROM dummy UNION ALL
								SELECT * FROM :p_out_ErroresDetalle;
		SELECT COUNT(1) INTO p_out_ErroresContador FROM :p_out_ErroresDetalle;
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
	
	p_out_ResultadoProceso		= SELECT 0 as "IdPrecioTasa", '' as "Campos", '' AS "Valores" FROM dummy where 1=2;
	p_out_AdvertenciasContador	:= 0;
	p_out_ErroresContador		:= 0;
	p_out_AdvertenciasDetalle	= select '' as "Codigo",'' as "RequiereAutorizacion",'' as "Descripcion" from dummy where 1=2;
	p_out_ErroresDetalle		= select '' as "Codigo",'' as "Descripcion" from dummy where 1=2;
	
	
	BEGIN
		--------------------
		--Variables
		--------------------
		
		DECLARE v_FechaHoraSistema	SECONDDATE; -- FORMATO 'YYYY-MM-DD HH24:MI:SS.FF7'
		DECLARE v_FechaHoraUI		SECONDDATE; -- FORMATO 'YYYY-MM-DD HH24:MI:SS.FF7'
		DECLARE v_FechaDesde		SECONDDATE;
		DECLARE v_FechaHasta		SECONDDATE;
		DECLARE v_Count				BIGINT:=0;
		DECLARE v_IdFuente			BIGINT;
		DECLARE v_NombreClasTipoInst NVARCHAR(100);
		
		DECLARE v_FechaSistemaUTC   	NVARCHAR(40);
		DECLARE v_FechaSistemaLOCAL   	NVARCHAR(40);
		DECLARE v_FechaHoraSistemaUTC   NVARCHAR(40);
		DECLARE v_FechaHoraSistemaLOCAL SECONDDATE;
		DECLARE v_FechaHoraUTC   		NVARCHAR(40);
		DECLARE v_FechaHoraLOCAL   		NVARCHAR(40);
		DECLARE v_DatosGeneralesCarga   TABLE ("IdPrecioTasa" BIGINT, "IdInstrumento" BIGINT, "Nemotecnico" NVARCHAR(40), "IdClasificacionTipoInstrumento" NVARCHAR(3), "IdTipoInstrumento" NVARCHAR(3),
					"Tasa" DECIMAL(26,8), "TasaMod" DECIMAL(26,8), "TasaEstimada" DECIMAL(26,8), "TasaEstimadaMod" DECIMAL(26,8),
					"Plazo" BIGINT, "PlazoMod" BIGINT, "IdEstado" NVARCHAR(3),  "IdMoneda" NVARCHAR(3));
 
		v_DatosGeneralesCarga = SELECT 0 AS "IdPrecioTasa", 0 AS "IdInstrumento", '' AS "Nemotecnico", '' AS "IdClasificacionTipoInstrumento", '' AS "IdTipoInstrumento",
					0 AS "Tasa", 0 AS "TasaMod", 0 AS "TasaEstimada", 0 AS "TasaEstimadaMod",
					0 AS "Plazo", 0 AS "PlazoMod", '' AS "IdEstado", '' AS "IdMoneda"
			FROM DUMMY WHERE 1=2;
			
		CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema"(p_IdEmpresa,:v_FechaHoraSistema);
		
		-- Se obtiene la misma FechaHoraSistema que se muestra en la UI
    	CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechas"(
			p_IdEmpresa					=> p_IdEmpresa,
			p_out_FechaSistemaUTC		=> v_FechaSistemaUTC,
			p_out_FechaSistemaLOCAL		=> v_FechaSistemaLOCAL,
			p_out_FechaHoraSistemaUTC	=> v_FechaHoraSistemaUTC,
			p_out_FechaHoraSistemaLOCAL	=> v_FechaHoraSistemaLOCAL,
			p_out_FechaHoraUTC			=> v_FechaHoraUTC,
			p_out_FechaHoraLOCAL		=> v_FechaHoraLOCAL
		);
		--v_FechaHoraUI := p_FechaCreSol || ' ' || TO_NVARCHAR(v_FechaHoraSistema,'HH24:MI:SS');
		v_FechaHoraUI := p_FechaCreSol || ' ' || TO_NVARCHAR(v_FechaHoraSistemaLOCAL,'HH24:MI:SS');
		
		v_FechaDesde := TO_SECONDDATE(:p_FechaCreSol||' 00:00:00','YYYY-MM-DD HH24:MI:SS');
		v_FechaHasta := TO_SECONDDATE(:p_FechaCreSol||' 23:59:59','YYYY-MM-DD HH24:MI:SS');
		
		
		v_DatosGeneralesCarga =
			SELECT 0 AS "IdPrecioTasa", 0 AS "IdInstrumento", '' AS "Nemotecnico", '' AS "IdClasificacionTipoInstrumento", '' AS "IdTipoInstrumento",
					0 AS "Tasa", 0 AS "TasaMod", 0 AS "TasaEstimada", 0 AS "TasaEstimadaMod",
					0 AS "Plazo", 0 AS "PlazoMod", '' AS "IdEstado", '' AS "IdMoneda"
			FROM DUMMY WHERE 1=2;
		
		
		-- Se obtienen Datos que seran cargados
		IF (:p_IdTipoTasa = 'CURVA' OR :p_IdTipoTasa = 'PUNTO') THEN
			v_DatosGeneralesCarga =
				SELECT
					0		AS "IdPrecioTasa",
					0		AS "IdInstrumento",
					''		AS "Nemotecnico",
					:p_IdClasificacionTipoInstrumento	AS "IdClasificacionTipoInstrumento",
					''		AS "IdTipoInstrumento",
					DTA."Tasa",
					0		AS "TasaMod",
					DTA."TasaEstimada",
					0		AS "TasaEstimadaMod",
					DTA."Plazo",
					0		AS "PlazoMod",
					'CAR'	AS "IdEstado",
					MON."IdMoneda"
				FROM :p_DataTasas DTA
				INNER JOIN "BSCL.Plataforma.BD::PLA.Moneda" MON
					ON (MON."IdEstado" = 'ACT' AND
						(:p_TipoArchivo = 'XML' AND DTA."IdMoneda" = MON."CodigoNumerico") OR
						(:p_TipoArchivo = 'CSV' AND DTA."IdMoneda" = MON."IdMoneda"));
		ELSE IF (:p_IdTipoTasa = 'TASA' AND :p_TipoArchivo = 'CSV') THEN
			v_DatosGeneralesCarga =
				SELECT
					0		AS "IdPrecioTasa",
					INS."IdInstrumento",
					DTA."Nemo"	AS "Nemotecnico",
					INS."IdClasificacionTipoInstrumento",
					INS."IdTipoInstrumento",
					DTA."Tasa",
					0		AS "TasaMod",
					DTA."TasaEstimada",
					0		AS "TasaEstimadaMod",
					DTA."Plazo",
					0		AS "PlazoMod",
					'CAR'	AS "IdEstado",
					INS."IdMoneda"
				FROM :p_DataTasas DTA
				LEFT JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" INS
					ON (DTA."Nemo" = INS."Nemotecnico" AND INS."IdEstado" = 'ACT')
				WHERE INS."IdInstrumento" IS NOT NULL
				GROUP BY INS."IdInstrumento", DTA."Nemo", INS."IdClasificacionTipoInstrumento", INS."IdTipoInstrumento",
						 DTA."Tasa", DTA."TasaEstimada", DTA."Plazo", INS."IdMoneda"
--
				UNION 
				SELECT
					0		AS "IdPrecioTasa",
					IFNULL(INS."IdInstrumento",0) AS "IdInstrumento",
					DTA."Nemo"											AS "Nemotecnico",
					IFNULL(INS."IdClasificacionTipoInstrumento",'IF') 	AS "IdClasificacionTipoInstrumento",
					'PAG' 												AS "IdTipoInstrumento",
					DTA."Tasa",
					0													AS "TasaMod",
					DTA."TasaEstimada",
					0													AS "TasaEstimadaMod",
					DTA."Plazo",
					0		AS "PlazoMod",
					'CAR'	AS "IdEstado",
					IFNULL(INS."IdMoneda",CASE 
											WHEN SUBSTRING(DTA."Nemo",2,1) = 'N' THEN 'CLP' 
											WHEN SUBSTRING(DTA."Nemo",2,1) = '*' THEN 'USD'
											WHEN SUBSTRING(DTA."Nemo",2,1) = 'D' THEN 'USD'
											WHEN SUBSTRING(DTA."Nemo",2,1) = 'E' THEN 'EUR'
											WHEN SUBSTRING(DTA."Nemo",2,1) = 'R' THEN 'EUR'
											WHEN SUBSTRING(DTA."Nemo",2,1) = 'U' THEN 'UF'
										  END														)			 AS "IdMoneda"			
				FROM :p_DataTasas DTA
				LEFT JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" INS
					ON (DTA."Nemo" = INS."Nemotecnico" AND INS."IdEstado" = 'ACT')
				WHERE INS."IdInstrumento" IS NULL
				GROUP BY INS."IdInstrumento", DTA."Nemo", INS."IdClasificacionTipoInstrumento", INS."IdTipoInstrumento",
						 DTA."Tasa", DTA."TasaEstimada", DTA."Plazo", INS."IdMoneda";

		ELSE
			--select 'parametrosGenerales', v_FechaDesde, v_FechaHasta, p_Archivo, p_IdEmpresa, p_Fuente from dummy;
			v_DatosGeneralesCarga =
				SELECT
					TAS."IdPrecioTasa",
					TAS."IdInstrumento",
					TAS."Nemotecnico",
					TAS."IdClasificacionTipoInstrumento",
					TAS."IdTipoInstrumento",
					TAS."Tasa",
					DTA."Tasa" AS "TasaMod",
					TAS."TasaEstimada",
					DTA."TasaEstimada" AS "TasaEstimadaMod",
					TAS."Plazo",
					DTA."Plazo" AS "PlazoMod",
					TAS."IdEstado",
					TAS."IdMoneda"
				FROM :p_DataTasas DTA
				INNER JOIN "BSCL.Plataforma.BD.Vistas::TAS.ConsultarPrecioTasa" TAS
					ON ( DTA."Nemo"			= TAS."Nemotecnico" AND 
						(TAS."FechaSolicitud" BETWEEN v_FechaDesde AND v_FechaHasta) AND
						 SUBSTR_BEFORE (TAS."NombreArchivo",'.') = SUBSTR_BEFORE (:p_Archivo,'.') AND
						 TAS."IdEmpresa"	= :p_IdEmpresa)
				INNER JOIN "BSCL.Plataforma.BD.Vistas::TAS.ConsultarFuente" FU
					ON (TAS."IdFuente" = FU."IdFuente" AND 
						--FU."Codigo" = :p_Fuente AND	-- se reemplaza por linea sigte
						FU."Descripcion" = :p_Fuente AND 
						FU."IdEmpresa" = :p_IdEmpresa)
				WHERE TAS."Borrado" = 'N';
		END IF;
		END IF;
		
		
		--------------------
		--VALIDACIONES
		--------------------	   
		IF p_UsaReglasNegocio = 'S' THEN
			DECLARE v_resultadoRegla NVARCHAR(40);
			
			-- Validaciones datos de entrada			
			CALL "BSCL.Plataforma.BD.Reglas::ServicioCumpleRequisitosBasicos"(:p_IdEmpresa,:p_IdUsuario,:p_IdModulo,v_out_ErroresDetalle,:v_resultadoRegla);
			IF :v_resultadoRegla = 'RECH' THEN
				p_out_ErroresDetalle = select "Codigo", "Descripcion" from :v_out_ErroresDetalle;
				SIGNAL SALIDA_CONTROLADA; 	
			END IF;
		END IF;
		
		
		-- Validacion para parametro ClasificacionTipoInstrumento incorrecto
		SELECT COUNT(1) INTO v_Count
		FROM "BSCL.Plataforma.BD.Vistas::INS.ConsultarClasificacionTipoInstrumento"
		WHERE "IdClasificacionTipoInstrumento" = p_IdClasificacionTipoInstrumento OR p_IdClasificacionTipoInstrumento = '';
		IF (v_Count = 0) THEN
			p_out_ErroresDetalle = SELECT 'PARAMETRO ENTRADA CLASIFICACION TIPO INSTRUMENTO INCORRECTO' AS "Codigo",:v_erroroutmsg AS "Descripcion" FROM dummy;
			SIGNAL SALIDA_CONTROLADA;
		END IF;
		
		
		-- Validacion para parametro TipoTasa incorrecto
		SELECT COUNT(1) INTO v_Count
		FROM "BSCL.Plataforma.BD.Vistas::TAS.ConsultarTipoTasa"
		WHERE "IdTipoTasa" = p_IdTipoTasa OR p_IdTipoTasa = '';
		IF (v_Count = 0) THEN
			p_out_ErroresDetalle = SELECT 'PARAMETRO ENTRADA TIPO TASA INCORRECTO' AS "Codigo",:v_erroroutmsg AS "Descripcion" FROM dummy;
			SIGNAL SALIDA_CONTROLADA;
		END IF;
		
		
		-- Validacion para parametro Fuente incorrecto
		SELECT COUNT(1) INTO v_Count
		FROM "BSCL.Plataforma.BD.Vistas::TAS.ConsultarFuente"
		WHERE
			--"Codigo" = :p_Fuente	-- se reemplaza por linea sigte
			"Descripcion" = :p_Fuente
		;
		IF (v_Count = 0) THEN
			p_out_ErroresDetalle = SELECT 'PARAMETRO ENTRADA FUENTE INCORRECTO' AS "Codigo",:v_erroroutmsg AS "Descripcion" FROM dummy;
			SIGNAL SALIDA_CONTROLADA;
		ELSE
			SELECT "IdFuente" INTO v_IdFuente
			FROM "BSCL.Plataforma.BD.Vistas::TAS.ConsultarFuente"
			WHERE
				--"Codigo" = :p_Fuente	-- se reemplaza por linea sigte
				"Descripcion" = :p_Fuente
			;
		END IF;
		
		
		-- Validaciones para TipoTasa CURVA y PUNTO
		IF (:p_IdTipoTasa = 'CURVA' OR :p_IdTipoTasa = 'PUNTO' OR (:p_IdTipoTasa = 'TASA' AND :p_TipoArchivo = 'CSV')) THEN --comentario
			
			-- validacion datos a cargar incorrectos
			SELECT COUNT(1) INTO v_Count FROM :v_DatosGeneralesCarga;
			IF (v_Count = 0) THEN
				p_out_ErroresDetalle = SELECT 'DATOS PARA CARGAR INCORRECTOS' AS "Codigo",:v_erroroutmsg AS "Descripcion" FROM dummy;
				SIGNAL SALIDA_CONTROLADA;
			END IF;
			
			-- validacion existen Tasas
			SELECT COUNT(1) INTO v_Count
			FROM "BSCL.Plataforma.BD.Vistas::TAS.ConsultarPrecioTasa"
			WHERE "IdClasificacionTipoInstrumento" = p_IdClasificacionTipoInstrumento
			  AND "IdTipoTasa"		= p_IdTipoTasa
			  AND "IdFuente"		= v_IdFuente
			  AND ("FechaSolicitud"	BETWEEN v_FechaDesde AND v_FechaHasta)
			  AND "NombreArchivo"	= p_Archivo
			  AND "IdEmpresa"		= p_IdEmpresa
			  AND "Borrado"			= 'N';
			
			IF (v_Count > 0) THEN
				p_out_ErroresDetalle = SELECT 'YA EXISTEN TASAS DEL ARCHIVO' AS "Codigo",:v_erroroutmsg AS "Descripcion" FROM dummy;
				SIGNAL SALIDA_CONTROLADA;
			END IF;
			
			-- validacion carga de Curva FWD y Punto FWD aplica solo con clasificacion tipo instrumento DV
			SELECT "Nombre" INTO v_NombreClasTipoInst
			FROM "BSCL.Plataforma.BD::INS.ClasificacionTipoInstrumento"
			WHERE "IdClasificacionTipoInstrumento" = p_IdClasificacionTipoInstrumento;
			IF (p_IdClasificacionTipoInstrumento <> 'DV' AND (:p_IdTipoTasa = 'CURVA' OR :p_IdTipoTasa = 'PUNTO')) THEN
				p_out_ErroresDetalle = SELECT 'CARGA DE '||:p_IdTipoTasa||' NO APLICA PARA EL MERCADO '||v_NombreClasTipoInst AS "Codigo",
											  :v_erroroutmsg AS "Descripcion" FROM dummy;
				SIGNAL SALIDA_CONTROLADA;
			END IF;
		
		-- Validaciones para Tasas previamente Solicitadas (registros ya creados en tabla PrecioTasa)
		ELSE
			
			-- validacion existen Tasas
			SELECT COUNT(1) INTO v_Count FROM :v_DatosGeneralesCarga;
			IF (v_Count = 0) THEN
				p_out_ErroresDetalle = SELECT 'NOMBRE DEL ARCHIVO NO ES EL MISMO QUE REALIZO LA SOLICITUD DE TASAS' AS "Codigo",:v_erroroutmsg AS "Descripcion" FROM dummy;
				SIGNAL SALIDA_CONTROLADA;
			END IF;
			
			
			-- validacion Tasas que ya esten Cargadas, Oficializadas o Publicadas
			SELECT COUNT(1) INTO v_Count FROM :v_DatosGeneralesCarga WHERE "IdEstado" IN ('OFI','PUB');
			IF (v_Count > 0) THEN
				p_out_ErroresDetalle = SELECT 'TASAS YA FUERON OFICIALIZADAS O PUBLICADAS' AS "Codigo",:v_erroroutmsg AS "Descripcion" FROM dummy;
				SIGNAL SALIDA_CONTROLADA;
			END IF;
			
			
			-- validacion Tasas que no han sido Solicitadas
			SELECT COUNT(1) INTO v_Count FROM :v_DatosGeneralesCarga WHERE "IdEstado" = 'CRA';
			IF (v_Count > 0) THEN
				p_out_ErroresDetalle = SELECT 'TASAS NO HAN SIDO SOLICITADAS' AS "Codigo",:v_erroroutmsg AS "Descripcion" FROM dummy;
				SIGNAL SALIDA_CONTROLADA;
			END IF;
			
		END IF;
		
		
		--------------------
		--LOGICA
		--------------------
		
		-- Nuevas Tasas son cargadas a traves de archivo con TipoTasa CURVA y PUNTO
		IF (:p_IdTipoTasa = 'CURVA' OR :p_IdTipoTasa = 'PUNTO' OR (:p_IdTipoTasa = 'TASA' AND :p_TipoArchivo = 'CSV')) THEN
			v_NuevasTasas =
				SELECT
					"BSCL.Plataforma.BD.Secuencias::TAS.IdPrecioTasa".NEXTVAL AS "IdPrecioTasa"
					,:v_IdFuente						AS "IdFuente"
					,"IdInstrumento"
					,"Nemotecnico"
					,:p_IdTipoTasa						AS "IdTipoTasa"
					,"Tasa"
					,"TasaEstimada"
					,"Plazo"
					,"IdEstado"
					,:p_IdEmpresa						AS "IdEmpresa"
					,:p_IdUsuario						AS "IdUsuarioCreador"
					,v_FechaHoraUI						AS "FechaSolicitud"
					--,:v_FechaHoraSistema				AS "FechaSolicitud"
					--,:v_FechaHoraSistemaLOCAL			AS "FechaIngreso"
					,:v_FechaHoraSistema				AS "FechaIngreso"
					,CURRENT_UTCTIMESTAMP				AS "FechaCreacion"
					,CURRENT_UTCTIMESTAMP				AS "FechaModificacion"
					,:p_Archivo							AS "NombreArchivo"
					,CASE WHEN :p_IdTipoTasa = 'CURVA' THEN 'TASA' ELSE :p_IdTipoTasa END AS "TipoRegistro"
					,"IdMoneda"
					,"IdClasificacionTipoInstrumento"
					,"IdTipoInstrumento"
				FROM :v_DatosGeneralesCarga
				WHERE "IdPrecioTasa" = 0;
			
			--select 'v_FechaHoraSistemaLOCAL', :v_FechaHoraSistemaLOCAL from dummy;
			v_TasasSeranCargadas =
				SELECT
					*,
					'Tasa / TasaEstimada / Plazo' AS "Campos",
					"TasaMod"||' / '||"TasaEstimadaMod"||' / '||"PlazoMod" AS "ValoresAntiguos",
					"Tasa"||' / '||"TasaEstimada"||' / '||"Plazo" AS "ValoresNuevos"
				FROM :v_DatosGeneralesCarga
				WHERE "IdEstado" IN ('SOL','CRS','MOD','CAR');
			
			INSERT INTO "BSCL.Plataforma.BD::TAS.PrecioTasa"(
				 "IdPrecioTasa"
				,"IdFuente"
				,"IdInstrumento"
				,"Nemotecnico"
				,"IdTipoTasa"
				,"Tasa"
				,"TasaEstimada"
				,"Plazo"
				,"IdEstado"
				,"IdEmpresa"
				,"IdUsuarioCreador"
				,"FechaSolicitud"
				,"FechaIngreso"
				,"FechaCreacion"
				,"FechaModificacion"
				,"NombreArchivo"
				,"TipoRegistro"
				,"IdMoneda"
				,"IdClasificacionTipoInstrumento"
				,"IdTipoInstrumento")
			SELECT * FROM :v_NuevasTasas;
			
			-- RESULTADO PROCESO
			p_out_ResultadoProceso =
				SELECT
					"IdPrecioTasa",
					'Tasa / TasaEstimada / Plazo / IdMoneda' AS "Campos",
					"Tasa"||' / '||"TasaEstimada"||' / '||"Plazo"||' / '||"IdMoneda" AS "Valores"
				FROM :v_NuevasTasas;
				
				-- LOG DE AUDITORIA
			p_DetalleLog = 
				SELECT
					"Nemotecnico" AS "SubRegistro",
					"Campos" AS "CampoRegistro",
					"ValoresAntiguos" AS "ValorAntiguo",
					"ValoresNuevos" AS "ValorNuevo"
				FROM :v_TasasSeranCargadas;
			
			CALL "BSCL.Plataforma.BD.Servicios::AgregarLog"(
				 p_IdTipoLog 		 => 'INGRESO'
				,p_IdModulo 		 => :p_IdModulo
				,p_IdUsuario 		 => :p_IdUsuario
				,p_IdEmpresa 		 => :p_IdEmpresa
				,p_FechaSistemaLog 	 => TO_NVARCHAR(:v_FechaHoraSistema,'YYYY-MM-DD')
				,p_IdUsuarioAtendido => :p_IdUsuario
				,p_IdTipoRegistro 	 => 'CARGATASAS'
				,p_NombreProceso 	 => 'CARGA ARCHIVO TASAS'
				,p_DetalleLog 		 => :p_DetalleLog
			);
			
			
		-- Tasas modificadas
		ELSE
			
			v_TasasSeranCargadas =
				SELECT
					*,
					'Tasa / TasaEstimada / Plazo' AS "Campos",
					"Tasa"||' / '||"TasaEstimada"||' / '||"Plazo" AS "ValoresAntiguos",
					"TasaMod"||' / '||"TasaEstimadaMod"||' / '||"PlazoMod" AS "ValoresNuevos"
				FROM :v_DatosGeneralesCarga
				WHERE "IdEstado" IN ('SOL','CRS','MOD','CAR');
			--select * from :v_TasasSeranCargadas;
			
			-- Update de Campos Tasa, TasaEstimada y Plazo
			UPDATE TAS
			SET
				TAS."Tasa"				= TCAR."TasaMod",
				TAS."TasaEstimada"		= TCAR."TasaEstimadaMod",
				TAS."Plazo"				= TCAR."PlazoMod",
				TAS."IdEstado"			= 'CAR',
				TAS."IdUsuarioModificacion" = :p_IdUsuario,
				TAS."FechaModificacion"	= CURRENT_UTCTIMESTAMP
			FROM "BSCL.Plataforma.BD::TAS.PrecioTasa" TAS
			INNER JOIN :v_TasasSeranCargadas TCAR ON (TAS."IdPrecioTasa" = TCAR."IdPrecioTasa");
			
			
			-- RESULTADO PROCESO
			p_out_ResultadoProceso = SELECT "IdPrecioTasa", "Campos", "ValoresNuevos" AS "Valores" FROM :v_TasasSeranCargadas;
			
			-- LOG DE AUDITORIA
			p_DetalleLog = 
				SELECT
					"Nemotecnico" AS "SubRegistro",
					"Campos" AS "CampoRegistro",
					"ValoresAntiguos" AS "ValorAntiguo",
					"ValoresNuevos" AS "ValorNuevo"
				FROM :v_TasasSeranCargadas;
			
			CALL "BSCL.Plataforma.BD.Servicios::AgregarLog"(
				 p_IdTipoLog 		 => 'MODIFICACION'
				,p_IdModulo 		 => :p_IdModulo
				,p_IdUsuario 		 => :p_IdUsuario
				,p_IdEmpresa 		 => :p_IdEmpresa
				,p_FechaSistemaLog 	 => TO_NVARCHAR(:v_FechaHoraSistema,'YYYY-MM-DD')
				,p_IdUsuarioAtendido => :p_IdUsuario
				,p_IdTipoRegistro 	 => 'CARGATASAS'
				,p_NombreProceso 	 => 'CARGA ARCHIVO TASAS'
				,p_DetalleLog 		 => :p_DetalleLog
			);
	
		END IF;
		
  	END;
END;