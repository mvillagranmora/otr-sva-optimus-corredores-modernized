SET SCHEMA "BSCL";
GO
DROP PROCEDURE  "BSCL.Custodia.BD.Servicios::EVC.CargarEventoCapitalClienteLiberadasDesdeArchivo";
GO
CREATE PROCEDURE "BSCL.Custodia.BD.Servicios::EVC.CargarEventoCapitalClienteLiberadasDesdeArchivo" (

	-- Parametros entrada comunes en todos los servicios privados
	IN p_IdEmpresa				BIGINT,			--ID Empresa destino
    IN p_IdUsuario				BIGINT 			default 0,
	IN p_IdModulo				NVARCHAR(3),	--ID del Modulo que lo invoca

	-- Parametros entrada del servicio privado
	IN p_DataArchivoEncabezado	TABLE (	"INDICADORREGISTROCABECERA"		NVARCHAR(1),
										"TIPOPROCESO"					NVARCHAR(4),
										"DESCRIPCIONPROCESO"           	NVARCHAR(40),
										"CODIGOINTERMEDIARIO"			NVARCHAR(4),
										"NOMBREINTERMEDIARIO"			NVARCHAR(30),
										"CODIGOVALOR"					NVARCHAR(12),
										"DESCRIPCIONVALOR"				NVARCHAR(30),
										"FECHAREGISTRO"					DECIMAL(8,0),
										"FECHAENTREGA"					DECIMAL(8,0),
										"MONEDA"						NVARCHAR(3),
										"FACTOR"						DECIMAL(15,12),
										"INDICADORREDONDEO"				NVARCHAR(1),
										"VALORNOMINAL"					DECIMAL(14,6),
										"MENSAJEVALIDACION"				NVARCHAR(200)),
										
	IN p_DataArchivoDetalle		TABLE (	"INDICADORREGISTRODETALLE"		NVARCHAR(1),
										"CODIGOTITULAR"					DECIMAL(8,0),
										"NOMBRETITULAR"					NVARCHAR(50),
										"TIPODOCUMENTO"					NVARCHAR(3),
										"NUMERODOCUMENTO"				NVARCHAR(20),
										"SALDOCONTABLESTOCK"			DECIMAL(14,0),
										"ACCIONESENTREGADAS"			DECIMAL(14,0),
										"ACCIONESNETREGADASDISPONIBLES"	DECIMAL(14,0),
										"SALDOCONTABLEFINAL"			DECIMAL(14,0),
										"ESPACIOS"						NVARCHAR(32),
										"MENSAJEVALIDACION"				NVARCHAR(200)),
										
	IN p_DataArchivoPie		TABLE (	"INDICADORREGISTROFINAL"				NVARCHAR(1),
									"NUMEROREGISTROSARCHIVO"				DECIMAL(10,0),
									"TOTALSALDOCONTABLE"					DECIMAL(18,0),
									"TOTALACCIONESENTREGADAS"				DECIMAL(18,0),
									"TOTALACCIONESENTREGADASDISPONIBLE"		DECIMAL(18,0),
									"TOTALSALDOCONTABLEFINAL"				DECIMAL(18,0),
									"ESPACIOS"								NVARCHAR(87),
									"MENSAJEVALIDACION"						NVARCHAR(200)),
	-- Salidas para mensajes de advertencias y errores

	OUT p_out_AdvertenciasContador  	 	int,
	OUT p_out_AdvertenciasDetalle  	 	 	table ("Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(520)),
	OUT p_out_ErroresContador  	 	 	 	int,
	OUT p_out_ErroresDetalle  	 	 	 	table ("Codigo" nvarchar(40),"Descripcion" nvarchar(5000))
)

LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER
DEFAULT SCHEMA "BSCL"
AS
BEGIN

	--------------------
	--DECLARE Manejo de Errores
	--------------------
	DECLARE SALIDA_CONTROLADA CONDITION;
	DECLARE v_errorcuenta INT DEFAULT 0;
	DECLARE v_errorhashmsg table ("Codigo" nvarchar(40),"Valor" nvarchar(520));
	DECLARE v_erroroutmsg  nvarchar(520);
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA
	BEGIN
		--salida/error/advertencia controlado por este servicio
		select count(1) into p_out_ErroresContador
		from :p_out_ErroresDetalle;
		select count(1) into p_out_AdvertenciasContador
		from :p_out_AdvertenciasDetalle;
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

	p_out_AdvertenciasContador	:= 0;
	p_out_ErroresContador		:= 0;
	p_out_AdvertenciasDetalle	= select '' as "Codigo",'' as "RequiereAutorizacion",'' as "Descripcion" from dummy where 1=2;
	p_out_ErroresDetalle		= select '' as "Codigo",'' as "Descripcion" from dummy where 1=2;

	BEGIN
		--------------------
		--Variables
		--------------------
		
		DECLARE v_CuentaRegistros						BIGINT = 0;
		DECLARE v_IdEventoCapital						BIGINT;
		DECLARE v_Nemotecnico							NVARCHAR(40);
		DECLARE v_CodigoValor							NVARCHAR(40);
		DECLARE v_IdMoneda								NVARCHAR(3);
		DECLARE v_SALDOCONTABLESTOCK					DECIMAL(18,0);
		DECLARE v_ACCIONESENTREGADAS					DECIMAL(18,0);
		DECLARE v_ACCIONESNETREGADASDISPONIBLES			DECIMAL(18,0);
		DECLARE v_SALDOCONTABLEFINAL					DECIMAL(18,0);
		DECLARE v_TOTALSALDOCONTABLE					DECIMAL(18,0);
		DECLARE v_TOTALACCIONESENTREGADAS				DECIMAL(18,0);
		DECLARE v_TOTALACCIONESENTREGADASDISPONIBLE		DECIMAL(18,0);
		DECLARE v_TOTALSALDOCONTABLEFINAL				DECIMAL(18,0);
		DECLARE v_FechaCierre							NVARCHAR(10);
		DECLARE v_FechaPago								NVARCHAR(10);
		--------------------
		-- Validaciones
		-- Si se encuentra algún registro, no se puede cargar el archivo.
		-- FALTA QUE ESTE PROCEDIMIENTO TENGA CONSULTA DE DICCIONARIO DE ERRORES PARA PODER SER ENVIADOS HACIA LA ui
		--------------------
		BEGIN

			-- VALIDAMOS QUE EL ARCHIVO CUADRE, RESPECTO A SUS DETALLES Y PIE DE ARCHIVO.
			SELECT 	SUM(ArchDeta."SALDOCONTABLESTOCK") 	 
					,SUM(ArchDeta."ACCIONESENTREGADAS") 			
					,SUM(ArchDeta."ACCIONESNETREGADASDISPONIBLES")
					,SUM(ArchDeta."SALDOCONTABLEFINAL")					INTO v_SALDOCONTABLESTOCK, v_ACCIONESENTREGADAS, v_ACCIONESNETREGADASDISPONIBLES, v_SALDOCONTABLEFINAL
			FROM :p_DataArchivoDetalle AS ArchDeta;
			
			SELECT 	ArchPie."TOTALSALDOCONTABLE" 
					,ArchPie."TOTALACCIONESENTREGADAS"
					,ArchPie."TOTALACCIONESENTREGADASDISPONIBLE"
					,ArchPie."TOTALSALDOCONTABLEFINAL"					INTO v_TOTALSALDOCONTABLE, v_TOTALACCIONESENTREGADAS, v_TOTALACCIONESENTREGADASDISPONIBLE, v_TOTALSALDOCONTABLEFINAL 
			FROM :p_DataArchivoPie AS ArchPie;

			IF (:v_SALDOCONTABLESTOCK != :v_TOTALSALDOCONTABLE or :v_ACCIONESENTREGADAS != :v_TOTALACCIONESENTREGADAS or :v_ACCIONESNETREGADASDISPONIBLES != :v_TOTALACCIONESENTREGADASDISPONIBLE OR :v_SALDOCONTABLEFINAL != :v_TOTALSALDOCONTABLEFINAL) THEN
				p_out_ErroresDetalle = select 'ARCHIVO_DE_CARGA_DESCUADRADO' as "Codigo",:v_erroroutmsg as "Descripcion" from dummy;
				SIGNAL SALIDA_CONTROLADA;				
			END IF;
			
			-- VALIDAMOS MONEDA.
			SELECT COUNT(1) INTO v_CuentaRegistros
			FROM :p_DataArchivoEncabezado AS Encabezado
				INNER JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarMoneda" AS Moneda
					ON Encabezado."MONEDA" = Moneda."Simbolo"
				   AND Moneda."IdEstado" = 'ACT';
			
			IF(v_CuentaRegistros > 1) THEN
				p_out_ErroresContador := 1;
				p_out_ErroresDetalle = select 'UI_EXISTE_MAS_DE_UNA_MONEDA_CON_CODIGO_MONEDA_DEL_ARCHIVO' as "Codigo",:v_erroroutmsg as "Descripcion" from dummy;
				SIGNAL SALIDA_CONTROLADA;
			END IF;
	
			IF(v_CuentaRegistros = 0) THEN
				p_out_ErroresContador := 1;
				p_out_ErroresDetalle = select 'UI_MONEDA_DEL_ARCHIVO_NO_REGISTRADA_EN_SISTEMA' as "Codigo",:v_erroroutmsg as "Descripcion" from dummy;
				SIGNAL SALIDA_CONTROLADA;
			END IF;
	
			IF(v_CuentaRegistros = 1) THEN
				SELECT Moneda."IdMoneda" INTO v_IdMoneda
				FROM :p_DataArchivoEncabezado AS Encabezado
					INNER JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarMoneda" AS Moneda
						ON Encabezado."MONEDA" = Moneda."Simbolo"
					   AND Moneda."IdEstado" = 'ACT';
			END IF;

			v_CuentaRegistros := 0;
			
			-- VALIDAMOS INSTRUMENTO
			SELECT COUNT(1) INTO v_CuentaRegistros 
			FROM :p_DataArchivoEncabezado AS Archi
				INNER JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" AS ConsIns
					ON TRIM(Archi."CODIGOVALOR") = ConsIns."ISIN"
				   AND ConsIns."IdEstado" = 'ACT';
		
			IF (:v_CuentaRegistros = 0) THEN
				p_out_ErroresContador := 1;
				p_out_ErroresDetalle = select 'UI_CODIGO_ISIN_NO_ENCONTRADO_EN_MAESTRO_INSTRUMENTOS' as "Codigo",:v_erroroutmsg as "Descripcion" from dummy;
				SIGNAL SALIDA_CONTROLADA;
			END IF;
		
			IF (:v_CuentaRegistros = 1) THEN
				SELECT COALESCE(ConsIns."Nemotecnico",'') INTO v_Nemotecnico 
				FROM :p_DataArchivoEncabezado AS Archi
					INNER JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" AS ConsIns
						ON TRIM(Archi."CODIGOVALOR") = ConsIns."ISIN"
					   AND ConsIns."IdEstado" = 'ACT';
			END IF;
	
			
			v_CuentaRegistros := 0;
			
			-- VALIDAMOS ESTADO DEL EVENTO CAPITAL
			SELECT Count(1) into v_CuentaRegistros
			FROM "BSCL.Custodia.BD::CUS.EventoCapital" AS Even
				INNER JOIN :p_DataArchivoEncabezado AS Archi
					ON SUBSTRING(Even."FechaLimite",1,4) || SUBSTRING(Even."FechaLimite",6,2) || SUBSTRING(Even."FechaLimite",9,2) = Archi."FECHAREGISTRO"
				   AND Even."Factor" = Archi."FACTOR"
			WHERE Even."Nemo" = trim(:v_Nemotecnico)
			  AND Even."IdTipoEventoCapital" = 'DA'
			  AND Even."IdEstado" = 'VIG';
	
			IF(v_CuentaRegistros > 0) THEN
				p_out_ErroresContador := 1;
				p_out_ErroresDetalle = select 'EVENTO_CAPITAL_NO_PROCESADO_NO_SE_PUEDE_CARGAR_ARCHIVO' as "Codigo",:v_erroroutmsg as "Descripcion" from dummy;
				SIGNAL SALIDA_CONTROLADA;
			END IF;
	
			
		END;

		---------------------------------------------------------------
		-- Buscamos a que evento capital corresponde el archivo cargado
		---------------------------------------------------------------
		
		v_CuentaRegistros := 0;

		SELECT Count(1) into v_CuentaRegistros
		FROM "BSCL.Custodia.BD::CUS.EventoCapital" AS Even
			INNER JOIN :p_DataArchivoEncabezado AS Archi
				ON SUBSTRING(Even."FechaLimite",1,4) || SUBSTRING(Even."FechaLimite",6,2) || SUBSTRING(Even."FechaLimite",9,2) = Archi."FECHAREGISTRO"
			   AND Even."Factor" = Archi."FACTOR"
		WHERE Even."Nemo" = trim(:v_Nemotecnico)
		  AND Even."IdTipoEventoCapital" = 'EL'
		  AND (Even."IdEstado" = 'PRO' OR Even."IdEstado" = 'PEN');
		  
		IF(v_CuentaRegistros > 1) THEN
			p_out_ErroresContador := 1;
			p_out_ErroresDetalle = select 'ARCHIVO_DE_CARGA_COINCIDE_CON_MAS_DE_UN_EVENTO' as "Codigo",:v_erroroutmsg as "Descripcion" from dummy;
			SIGNAL SALIDA_CONTROLADA;
		END IF;

		IF(v_CuentaRegistros = 0) THEN
			p_out_ErroresContador := 1;
			p_out_ErroresDetalle = select 'UI_EVENTO_CAPITAL_NO_ENCONTRADO' as "Codigo",:v_erroroutmsg as "Descripcion" from dummy;
			SIGNAL SALIDA_CONTROLADA;
		END IF;

		IF(v_CuentaRegistros = 1) THEN
			--ACTUALIZAMOS EVENTOS DE CAPITAL CLIENTE.

			SELECT SUBSTRING(Archi."FECHAREGISTRO",1,4) || '-' || SUBSTRING(Archi."FECHAREGISTRO",5,2) || '-' || SUBSTRING(Archi."FECHAREGISTRO",7,2) INTO v_FechaCierre
			FROM :p_DataArchivoEncabezado AS Archi;
			
			SELECT SUBSTRING(Archi."FECHAENTREGA",1,4) || '-' || SUBSTRING(Archi."FECHAENTREGA",5,2) || '-' || SUBSTRING(Archi."FECHAENTREGA",7,2) INTO v_FechaPago 
			FROM :p_DataArchivoEncabezado AS Archi;
	
			SELECT Even."IdEventoCapital" , TRIM(Archi."CODIGOVALOR") into v_IdEventoCapital, v_CodigoValor
			FROM "BSCL.Custodia.BD::CUS.EventoCapital" AS Even
				INNER JOIN :p_DataArchivoEncabezado AS Archi
					ON SUBSTRING(Even."FechaLimite",1,4) || SUBSTRING(Even."FechaLimite",6,2) || SUBSTRING(Even."FechaLimite",9,2) = Archi."FECHAREGISTRO"
				   AND Even."Factor" = Archi."FACTOR"
			WHERE Even."Nemo" = trim(:v_Nemotecnico)
			  AND Even."IdTipoEventoCapital" = 'EL'
			  AND (Even."IdEstado" = 'PRO' OR Even."IdEstado" = 'PEN');

			v_CuentaRegistros := 0;
			
			-- AGREGO ID DE CLIENTE AL DETALLE DEL ARCHIVO DE ENTRADA
			v_TablaDetalleArchivo	= 	SELECT 	 Archivo."INDICADORREGISTRODETALLE"		
												,Archivo."CODIGOTITULAR"					
												,Archivo."NOMBRETITULAR"
--												,ConCli."IdCliente"
												,CC."IdSocioDeNegocio"
												,Archivo."TIPODOCUMENTO"					
												,Archivo."NUMERODOCUMENTO"				
												,Archivo."SALDOCONTABLESTOCK"			
												,Archivo."ACCIONESENTREGADAS"			
												,Archivo."ACCIONESNETREGADASDISPONIBLES"	
												,Archivo."SALDOCONTABLEFINAL"			
												,Archivo."ESPACIOS"						
												,Archivo."MENSAJEVALIDACION"				
										FROM :p_DataArchivoDetalle AS Archivo
											INNER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarDocumentoIdentidadPersona" CDIP
												ON CDIP."IdTipoDocumentoIdentidad" = TRIM(Archivo."TIPODOCUMENTO")
											   AND CDIP."DocumentoIdentidad" = TRIM(Archivo."NUMERODOCUMENTO")
											   AND CDIP."IdEstado" = 'ACT'
											INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" CC
												ON CDIP."IdPersona" = CC."IdPersona"
											   AND CC."IdEstado" = 'ACT';
/*											INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" as CPDI
												ON TRIM(Archivo."TIPODOCUMENTO")	= TRIM(CPDI."TipoDocumentoIdentidad")
											   AND TRIM(Archivo."NUMERODOCUMENTO")	= TRIM(CPDI."DocumentoIdentidad")
											   AND CPDI."IdEstado" = 'ACT';*/
											   
--			SELECT 'v_TablaDetalleArchivo', * FROM :v_TablaDetalleArchivo;
			SELECT SUM("ACCIONESNETREGADASDISPONIBLES") INTO v_CuentaRegistros
			FROM :v_TablaDetalleArchivo;

			IF (:v_CuentaRegistros > 0) THEN
				--OBTENGO LOS CLIENTES/PORTAFOLIOS CON SUS RESPECTIVAS CANTIDADES AGRUPADOS, SOLO PARA CARTERAS, NO PARA GARANTÍAS.
				v_TablaEventoClienteAgrupado = 	SELECT 	 EventoCliente."IdSocioNegocio"			AS"IdSocioNegocio" 
														,SUM(EventoCliente."CantidadDerecho") 	AS "CantidadDerecho"
												FROM "BSCL.Custodia.BD::CUS.EventoCapitalCliente" AS EventoCliente 
												WHERE EventoCliente."IdEventoCapital" = :v_IdEventoCapital
												  AND EventoCliente."TipoCartera" = 'CUS'
												  AND EventoCliente."CantidadDerecho" > 0
												  AND EventoCliente."IdBoveda" = 'CAV'
												GROUP BY EventoCliente."IdSocioNegocio";
										
--			SELECT 'v_TablaEventoClienteAgrupado', * FROM :v_TablaEventoClienteAgrupado;
	
				--OBTENGO LOS CLIENTES/PORTAFOLIOS CON SUS RESPECTIVAS CANTIDADES.
				v_TablaEventoCliente = 	SELECT 	 EventoCliente."IdSocioNegocio" 																													AS "IdSocioNegocio"
												,EventoCliente."IdPortafolio" 																														AS "IdPortafolio"  
												,EventoCliente."CantidadDerecho" 																													AS "CantidadDerecho"
												,((SUM(EventoCliente."CantidadDerecho") / EventoClienteAgrupado."CantidadDerecho") * 100 )															AS "Peso"
											 	,((((SUM(EventoCliente."CantidadDerecho") / EventoClienteAgrupado."CantidadDerecho") * 100 ) * ArchivoDet."SALDOCONTABLESTOCK") / 100) 				AS "ProrrateoCantidadDerecho"
											 	,((((SUM(EventoCliente."CantidadDerecho") / EventoClienteAgrupado."CantidadDerecho") * 100 ) * ArchivoDet."ACCIONESNETREGADASDISPONIBLES") / 100) 	AS "ProrrateoDisponible"
										FROM "BSCL.Custodia.BD::CUS.EventoCapitalCliente" AS EventoCliente
											INNER JOIN :v_TablaEventoClienteAgrupado AS EventoClienteAgrupado 
												ON  EventoClienteAgrupado."IdSocioNegocio" = EventoCliente."IdSocioNegocio"
--											INNER JOIN "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio" AS ConSocio
--												ON ConSocio."IdSocioDeNegocio" = EventoCliente."IdSocioNegocio"
											INNER JOIN :v_TablaDetalleArchivo AS ArchivoDet
												ON ArchivoDet."IdSocioDeNegocio" = EventoCliente."IdSocioNegocio" 
										WHERE EventoCliente."IdEventoCapital" = :v_IdEventoCapital
										  AND EventoCliente."TipoCartera" = 'CUS'
										  AND EventoCliente."CantidadDerecho" > 0
										  AND EventoCliente."IdBoveda" = 'CAV'
										GROUP BY	EventoCliente."IdSocioNegocio", 
													EventoCliente."IdPortafolio", 
													EventoCliente."CantidadDerecho", 
													EventoClienteAgrupado."CantidadDerecho", 
													ArchivoDet."SALDOCONTABLESTOCK", 
													ArchivoDet."ACCIONESNETREGADASDISPONIBLES"; 
	
--			SELECT 'v_TablaEventoCliente', * FROM :v_TablaEventoCliente;
	
				--ACTUALIZO LSO CLIENTES EN SU DISPONIBLE.
				UPDATE EvCapCli SET
					 EvCapCli."CantidadDerechoArchivo"			= TablaEventoCli."ProrrateoCantidadDerecho"
					,EvCapCli."AccionesDisponiblesArchivo"		= TablaEventoCli."ProrrateoDisponible"
					,EvCapCli."DiferenciaCantidadDerecho" 		= (EvCapCli."CantidadDerecho" - TablaEventoCli."ProrrateoCantidadDerecho")
					,EvCapCli."DiferenciaAccionesCantidadNueva" = (EvCapCli."CantidadNueva" - TablaEventoCli."ProrrateoDisponible")
				FROM "BSCL.Custodia.BD::CUS.EventoCapitalCliente" AS EvCapCli
					INNER JOIN :v_TablaEventoCliente AS TablaEventoCli
						ON TablaEventoCli."IdSocioNegocio" = EvCapCli."IdSocioNegocio"
					   AND TablaEventoCli."IdPortafolio" = EvCapCli."IdPortafolio"
				WHERE EvCapCli."IdEventoCapital" = :v_IdEventoCapital
				  AND EvCapCli."TipoCartera" = 'CUS'
				  AND EvCapCli."CantidadDerecho" > 0
				  AND EvCapCli."IdBoveda" = 'CAV';
				  
				-- ACTUALIZAMOS CONTRAPARTE A PARTIR DEL ARCHIVO.
				UPDATE EvCapCli SET
					 EvCapCli."CantidadDerechoArchivo"			= PieArchivo."TOTALSALDOCONTABLE"
					,EvCapCli."AccionesDisponiblesArchivo"		= PieArchivo."TOTALACCIONESENTREGADAS"
					,EvCapCli."DiferenciaCantidadDerecho" 		= (EvCapCli."CantidadDerecho" - PieArchivo."TOTALSALDOCONTABLE")
					,EvCapCli."DiferenciaAccionesCantidadNueva" = (EvCapCli."CantidadNueva" - PieArchivo."TOTALACCIONESENTREGADAS")
				FROM "BSCL.Custodia.BD::CUS.EventoCapitalCliente" AS EvCapCli
					 ,:p_DataArchivoPie AS PieArchivo
				WHERE EvCapCli."IdEventoCapital" = :v_IdEventoCapital
				  AND EvCapCli."TipoCartera" = 'CUS'
				  AND EvCapCli."CantidadDerecho" < 0
				  AND EvCapCli."IdBoveda" = 'CAV';
				  
			END IF;
			-------------------------------------------------------------------------------------------------------------------------------------
			-- GARANTIAS
			-------------------------------------------------------------------------------------------------------------------------------------

			v_CuentaRegistros := 0;

			SELECT SUM("ACCIONESENTREGADAS"-"ACCIONESNETREGADASDISPONIBLES") INTO v_CuentaRegistros
			FROM :v_TablaDetalleArchivo;			

			IF (v_CuentaRegistros > 0) THEN

				--OBTENGO LOS CLIENTES/PORTAFOLIOS CON SUS RESPECTIVAS CANTIDADES AGRUPADOS, SOLO PARA GARANTÍAS.
				v_TablaEventoClienteAgrupadoGar = 	SELECT 	 EventoCliente."IdSocioNegocio"			AS"IdSocioNegocio" 
															,SUM(EventoCliente."CantidadDerecho") 	AS "CantidadDerecho"
													FROM "BSCL.Custodia.BD::CUS.EventoCapitalCliente" AS EventoCliente 
													WHERE EventoCliente."IdEventoCapital" = :v_IdEventoCapital
													  AND EventoCliente."TipoCartera" = 'GAR'
													  AND EventoCliente."CantidadDerecho" > 0
													  AND EventoCliente."IdBoveda" = 'CAV'
													GROUP BY EventoCliente."IdSocioNegocio";
										
--			SELECT 'v_TablaEventoClienteAgrupado', * FROM :v_TablaEventoClienteAgrupado;
	
				--OBTENGO LOS CLIENTES/PORTAFOLIOS CON SUS RESPECTIVAS CANTIDADES.
				v_TablaEventoClienteGar = 	SELECT 	 EventoCliente."IdSocioNegocio" 																													AS "IdSocioNegocio"
													,EventoCliente."IdPortafolio" 																														AS "IdPortafolio"  
													,EventoCliente."CantidadDerecho" 																													AS "CantidadDerecho"
													,((SUM(EventoCliente."CantidadDerecho") / EventoClienteAgrupado."CantidadDerecho") * 100 )															AS "Peso"
												 	,((((SUM(EventoCliente."CantidadDerecho") / EventoClienteAgrupado."CantidadDerecho") * 100 ) * ArchivoDet."SALDOCONTABLESTOCK") / 100) 				AS "ProrrateoCantidadDerecho"
												 	,((((SUM(EventoCliente."CantidadDerecho") / EventoClienteAgrupado."CantidadDerecho") * 100 ) * (ArchivoDet."ACCIONESENTREGADAS" - ArchivoDet."ACCIONESNETREGADASDISPONIBLES")) / 100) 	AS "ProrrateoDisponible"
											FROM "BSCL.Custodia.BD::CUS.EventoCapitalCliente" AS EventoCliente
												INNER JOIN :v_TablaEventoClienteAgrupadoGar AS EventoClienteAgrupado 
													ON  EventoClienteAgrupado."IdSocioNegocio" = EventoCliente."IdSocioNegocio"
--												INNER JOIN "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio" AS ConSocio
--													ON ConSocio."IdSocioDeNegocio" = EventoCliente."IdSocioNegocio"
												INNER JOIN :v_TablaDetalleArchivo AS ArchivoDet
													ON ArchivoDet."IdSocioDeNegocio" = EventoCliente."IdSocioNegocio" 
											WHERE EventoCliente."IdEventoCapital" = :v_IdEventoCapital
											  AND EventoCliente."TipoCartera" = 'GAR'
											  AND EventoCliente."CantidadDerecho" > 0
											  AND EventoCliente."IdBoveda" = 'CAV'
											GROUP BY	EventoCliente."IdSocioNegocio", 
														EventoCliente."IdPortafolio", 
														EventoCliente."CantidadDerecho", 
														EventoClienteAgrupado."CantidadDerecho", 
														ArchivoDet."SALDOCONTABLESTOCK", 
														ArchivoDet."ACCIONESENTREGADAS",
														ArchivoDet."ACCIONESNETREGADASDISPONIBLES"; 
	
--			SELECT 'v_TablaEventoCliente', * FROM :v_TablaEventoCliente;
	
				--ACTUALIZO LSO CLIENTES GARANTIAS
				UPDATE EvCapCli SET
					 EvCapCli."CantidadDerechoArchivo"			= TablaEventoCli."ProrrateoCantidadDerecho"
					,EvCapCli."AccionesDisponiblesArchivo"		= TablaEventoCli."ProrrateoDisponible"
					,EvCapCli."DiferenciaCantidadDerecho" 		= (EvCapCli."CantidadDerecho" - TablaEventoCli."ProrrateoCantidadDerecho")
					,EvCapCli."DiferenciaAccionesCantidadNueva" = (EvCapCli."CantidadNueva" - TablaEventoCli."ProrrateoDisponible")
				FROM "BSCL.Custodia.BD::CUS.EventoCapitalCliente" AS EvCapCli
					INNER JOIN :v_TablaEventoClienteGar AS TablaEventoCli
						ON TablaEventoCli."IdSocioNegocio" = EvCapCli."IdSocioNegocio"
					   AND TablaEventoCli."IdPortafolio" = EvCapCli."IdPortafolio"
				WHERE EvCapCli."IdEventoCapital" = :v_IdEventoCapital
				  AND EvCapCli."TipoCartera" = 'GAR'
				  AND EvCapCli."CantidadDerecho" > 0
				  AND EvCapCli."IdBoveda" = 'CAV';
				  
				-- ACTUALIZAMOS CONTRAPARTE A PARTIR DEL ARCHIVO.
				UPDATE EvCapCli SET
					 EvCapCli."CantidadDerechoArchivo"			= PieArchivo."TOTALSALDOCONTABLE"
					,EvCapCli."AccionesDisponiblesArchivo"		= PieArchivo."TOTALACCIONESENTREGADAS"
					,EvCapCli."DiferenciaCantidadDerecho" 		= (EvCapCli."CantidadDerecho" - PieArchivo."TOTALSALDOCONTABLE")
					,EvCapCli."DiferenciaAccionesCantidadNueva" = (EvCapCli."CantidadNueva" - PieArchivo."TOTALACCIONESENTREGADAS")
				FROM "BSCL.Custodia.BD::CUS.EventoCapitalCliente" AS EvCapCli
					 ,:p_DataArchivoPie AS PieArchivo
				WHERE EvCapCli."IdEventoCapital" = :v_IdEventoCapital
				  AND EvCapCli."TipoCartera" = 'CUS'
				  AND EvCapCli."CantidadDerecho" < 0
				  AND EvCapCli."IdBoveda" = 'CAV';
			END IF;
			
			-------------------------------------------------------------------------------------------------------------------------------------
			-- INSERTAMOS CLIENTES QUE ESTÉN EN EL ARCHIVO, PERO QUE NO SE ENCUENTREN CONGELADOS EN LOS EVENTOS CAPITAL CLIENTE.
			-------------------------------------------------------------------------------------------------------------------------------------

			--OBTENGO LOS CLIENTES 
			v_TablaEventoClienteIdCliente = SELECT EventoCliente."IdSocioNegocio" 
											FROM "BSCL.Custodia.BD::CUS.EventoCapitalCliente" AS EventoCliente
--												INNER JOIN "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio" AS ConSocio
--													ON ConSocio."IdSocioDeNegocio" = EventoCliente."IdSocioNegocio"
											WHERE EventoCliente."IdEventoCapital" = :v_IdEventoCapital
											  AND EventoCliente."CantidadDerecho" > 0
											  AND EventoCliente."IdBoveda" = 'CAV';

			v_CuentaRegistros := 0;

			-- BUSCAMOS REGISTROS DEL ARCHIVO QUE NO SE ENCUENTREN CONGELADOS EN LOS EVENTOS DE CLIENTES PARA PODER INSERTARLOS
			SELECT COUNT(1) INTO v_CuentaRegistros
			FROM :v_TablaEventoClienteIdCliente AS EventoIdCliente
				RIGHT OUTER JOIN :v_TablaDetalleArchivo AS ArchivoDet
					ON EventoIdCliente."IdSocioNegocio"  = ArchivoDet."IdSocioDeNegocio" 
			WHERE EventoIdCliente."IdSocioNegocio" IS NULL;
			
			-- SI HAY REGISTROS, SE DEBEN INSERTAR.
			IF (:v_CuentaRegistros > 0 ) THEN
				v_RegistrosAInsertar =	SELECT	ArchivoDet."INDICADORREGISTRODETALLE"     ,
												ArchivoDet."CODIGOTITULAR"                ,
												ArchivoDet."NOMBRETITULAR"                ,
--												ArchivoDet."IdCliente"                    ,
												ArchivoDet."IdSocioDeNegocio"			,
												ArchivoDet."TIPODOCUMENTO"                ,
												ArchivoDet."NUMERODOCUMENTO"              ,
												ArchivoDet."SALDOCONTABLESTOCK"           ,
												ArchivoDet."ACCIONESENTREGADAS"           ,
												ArchivoDet."ACCIONESNETREGADASDISPONIBLES",
												ArchivoDet."SALDOCONTABLEFINAL"           ,
												ArchivoDet."ESPACIOS"                     ,
												ArchivoDet."MENSAJEVALIDACION"			
										FROM :v_TablaEventoClienteIdCliente AS EventoIdCliente
											RIGHT OUTER JOIN :v_TablaDetalleArchivo AS ArchivoDet
												ON EventoIdCliente."IdSocioNegocio"  = ArchivoDet."IdSocioDeNegocio"
										WHERE EventoIdCliente."IdSocioNegocio" IS NULL;

				SELECT SUBSTRING(Archi."FECHAREGISTRO",1,4) || '-' || SUBSTRING(Archi."FECHAREGISTRO",5,2) || '-' || SUBSTRING(Archi."FECHAREGISTRO",7,2) INTO v_FechaCierre
				FROM :p_DataArchivoEncabezado AS Archi;
				
				SELECT SUBSTRING(Archi."FECHAENTREGA",1,4) || '-' || SUBSTRING(Archi."FECHAENTREGA",5,2) || '-' || SUBSTRING(Archi."FECHAENTREGA",7,2) INTO v_FechaPago 
				FROM :p_DataArchivoEncabezado AS Archi;
		
				INSERT INTO "BSCL.Custodia.BD::CUS.EventoCapitalCliente"
			   	(	"IdEventoCapitalCliente",				
																	"IdEventoCapital",					"IdTipoEventoCapital",				"NumeroDividendo",
					"Secuencia",									"IdSocioNegocio",					"TipoSocioNegocio",					"IdPortafolio",				
					"IdTipoDocumentoIdentidad",						"DocumentoIdentidad",				"NombreCliente",					"IdClasificacionTipoInstrumento",	
					"IdTipoInstrumento",							"IdInstrumento",					"Nemo",								"IdInstrumentoNuevo",
					"NemoNuevo",									"FechaCierre",						"FechaPago",						"CantidadDerecho",		
					"TipoCartera",									"CantidadNueva",					"MontoCapital",						"MontoInteres",				
					"Total",										"IdMoneda",							"SecuenciaCupon",					"CodigoUsuarioAgente",				
					"NombreAgente",									"CodigoSucursal",					"FechaHoraIngresoRegistro",			"FechaHoraUltimaActualizacion",		
					"IdUsuario",									"IdEstado",							"IdBoveda",							"Origen",
					"CantidadDerechoArchivo",						"AccionesDisponiblesArchivo",		"DiferenciaCantidadDerecho",		"DiferenciaAccionesCantidadNueva")
					
				SELECT 
				    "BSCL.Custodia.BD.Secuencias::EventoCapitalCliente".NEXTVAL,
																	Even."IdEventoCapital",				Even."IdTipoEventoCapital",			Even."NumeroDividendo",
				    Even."Secuencia",								ConCli."IdSocioDeNegocio",			'CLI',								ConCli."IdPortafolioDefecto",
				    Archivo."TIPODOCUMENTO",						Archivo."NUMERODOCUMENTO",			ConCli."NombreCompleto",			ConsIns."IdClasificacionTipoInstrumento",
				    ConsIns."IdTipoInstrumento",					ConsIns."IdInstrumento",			ConsIns."Nemotecnico",				0,
				    '',												:v_FechaCierre,						:v_FechaPago,						Archivo."SALDOCONTABLESTOCK",
					'CUS',											0,									0,									0,
					Archivo."ACCIONESNETREGADASDISPONIBLES",		:v_IdMoneda,						0,									COALESCE(Eje."CodigoEmpleado",''),
					COALESCE(Eje."NombreCompletoEjecutivo",''),		COALESCE(Eje."CodigoSucursal",''),	Current_UTCTIMESTAMP,				Current_UTCTIMESTAMP,
					p_IdUsuario,									'VIG',								'CAV',								'CAR',
					Archivo."SALDOCONTABLESTOCK",					Archivo."ACCIONESNETREGADASDISPONIBLES",		0,									0	
				FROM :v_RegistrosAInsertar AS Archivo
				INNER JOIN "BSCL.Custodia.BD::CUS.EventoCapital" AS Even
					ON Even."IdEventoCapital" = :v_IdEventoCapital
				LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS ConCli
					ON Archivo."IdSocioDeNegocio" = ConCli."IdSocioDeNegocio"
--					ON TRIM(Archivo."TIPODOCUMENTO")	= TRIM(ConCli."TipoDocumentoIdentidad")
--				   AND TRIM(Archivo."NUMERODOCUMENTO")	= TRIM(ConCli."DocumentoIdentidad")
				INNER JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" AS ConsIns
					ON :v_CodigoValor = ConsIns."ISIN"
				   AND ConsIns."IdEstado" = 'ACT'
				LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::EJE.ConsultarEjecutivoPortafolio"	AS Eje
					ON (ConCli."IdPortafolioDefecto" = Eje."IdPortafolio" 
				   AND Eje."IdEmpleadoEjecutivoPortafolio" = Eje."IdEmpleadoSupervisor" );
			END IF;
		END IF;
	END;
END;
GO