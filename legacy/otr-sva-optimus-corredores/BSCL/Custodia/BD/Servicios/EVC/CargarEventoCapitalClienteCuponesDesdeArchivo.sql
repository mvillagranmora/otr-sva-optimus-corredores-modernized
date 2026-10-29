SET SCHEMA "BSCL";
GO
DROP PROCEDURE  "BSCL.Custodia.BD.Servicios::EVC.CargarEventoCapitalClienteCuponesDesdeArchivo";
GO
CREATE PROCEDURE "BSCL.Custodia.BD.Servicios::EVC.CargarEventoCapitalClienteCuponesDesdeArchivo" (

	-- Parametros entrada comunes en todos los servicios privados
	IN p_IdEmpresa				BIGINT			default 0,
    IN p_IdUsuario				BIGINT 			default 0,
	IN p_IdModulo				NVARCHAR(3),	--ID del Modulo que lo invoca

	-- Parametros entrada del servicio privado
	IN p_DataArchivoDetalle		TABLE (	"CODIGOINTERMEDIARIO"			DECIMAL(4,0),
										"NOMBREINTERMEDIARIO"			NVARCHAR(50),
										"CODIGOTITULAR"					DECIMAL(8,0),
										"NOMBRETITULAR"					NVARCHAR(50),
										"TIPODOCUMENTO"					NVARCHAR(3),
										"NUMERODOCUMENTO"				NVARCHAR(20),
										"CODIGOVALOR"					NVARCHAR(12),
										"DESCRIPCIONVALOR"				NVARCHAR(40),
										"FECHACORTE"					NVARCHAR(10),
										"FECHAENTREGA"					NVARCHAR(10),
										"FECHAACUERDO"					NVARCHAR(10),
										"STOCKTOTAL"					DECIMAL(14,0),
										"CEROS"							NVARCHAR(14),
										"VALORNOMINAL"					DECIMAL(9,0),
										"CANTIDADENTREGA"				DECIMAL(15,0),
										"MONEDA"						NVARCHAR(3),
										"IMPORTE1"						NVARCHAR(60),
										"IMPORTE2"						NVARCHAR(60),
										"INTERESBRUTO"					DECIMAL(13,0),
										"CEROS2"						NVARCHAR(13),
										"INTERESNETO"					DECIMAL(13,0),
										"AMORTIZACIONBRUTO"				DECIMAL(13,0),
										"CEROS3"						NVARCHAR(13),
										"TOTALAMORTIZACION"				DECIMAL(13,0),
										"CORRELATIVO"					DECIMAL(5,0),
										"CEROS4"						NVARCHAR(15)),
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
	SEQUENTIAL EXECUTION
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
		DECLARE	v_FechaSistemaCreacion					TIMESTAMP;
		DECLARE	v_countTransacciones					BIGINT;
		DECLARE	v_countOrdenes							BIGINT;
		DECLARE	v_countClientes							BIGINT;
		DECLARE	v_countAsignacion						BIGINT;
		DECLARE v_CuentaRegistros						BIGINT = 0;
		DECLARE v_IdEventoCapital						BIGINT;
		DECLARE v_Nemotecnico							NVARCHAR(40);
		DECLARE v_IdInstrumento							BIGINT;
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

			-- VALIDAMOS MONEDA.
			SELECT COUNT(1) INTO v_CuentaRegistros
			FROM :p_DataArchivoDetalle AS Detalle
				INNER JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarMoneda" AS Moneda
					ON Detalle."MONEDA" = Moneda."Simbolo"
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
				FROM :p_DataArchivoDetalle AS Detalle
					INNER JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarMoneda" AS Moneda
						ON Detalle."MONEDA" = Moneda."Simbolo"
					   AND Moneda."IdEstado" = 'ACT';
			END IF;

			v_CuentaRegistros := 0;
			
			SELECT COUNT(1) INTO v_CuentaRegistros 
			FROM :p_DataArchivoDetalle AS Archi
				INNER JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" AS ConsIns
					ON TRIM(Archi."CODIGOVALOR") = ConsIns."ISIN"
				   AND ConsIns."IdEstado" = 'ACT';
		
			IF (:v_CuentaRegistros = 0) THEN
				p_out_ErroresContador := 1;
				p_out_ErroresDetalle = select 'UI_CODIGO_ISIN_NO_ENCONTRADO_EN_MAESTRO_INSTRUMENTOS' as "Codigo",:v_erroroutmsg as "Descripcion" from dummy;
				SIGNAL SALIDA_CONTROLADA;
			END IF;
		
			IF (:v_CuentaRegistros = 1) THEN
				SELECT COALESCE(ConsIns."Nemotecnico",''), ConsIns."IdInstrumento"  INTO v_Nemotecnico, v_IdInstrumento 
				FROM :p_DataArchivoDetalle AS Archi
					INNER JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" AS ConsIns
						ON TRIM(Archi."CODIGOVALOR") = ConsIns."ISIN"
					   AND ConsIns."IdEstado" = 'ACT';
			END IF;
	

			v_CuentaRegistros := 0;
			
			
		END;


		CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema" (:p_IdEmpresa,v_FechaSistemaCreacion);

		
		---------------------------------------------------------------
		-- Buscamos a que evento capital corresponde el archivo cargado
		---------------------------------------------------------------
		
		v_CuentaRegistros := 0;
		
--		SELECT Archi."FECHAREGISTRO", Archi."FACTOR", trim(Archi."CODIGOVALOR"), trim(:v_Nemotecnico) FROM :p_DataArchivoEncabezado AS Archi;
--		SELECT v_IdInstrumento AS v_IdInstrumento, SUBSTRING(Archi."FECHAENTREGA",7,4) || SUBSTRING(Archi."FECHAENTREGA",4,2) || SUBSTRING(Archi."FECHAENTREGA",1,2) AS fecha FROM :p_DataArchivoDetalle AS Archi;

		SELECT Count(1) into v_CuentaRegistros
		FROM "BSCL.Custodia.BD::CUS.EventoCapital" AS Even
			INNER JOIN :p_DataArchivoDetalle AS Archi
				ON SUBSTRING(Even."FechaPago",1,4) || SUBSTRING(Even."FechaPago",6,2) || SUBSTRING(Even."FechaPago",9,2) = SUBSTRING(Archi."FECHAENTREGA",7,4) || SUBSTRING(Archi."FECHAENTREGA",4,2) || SUBSTRING(Archi."FECHAENTREGA",1,2)
		WHERE Even."IdInstrumento" = :v_IdInstrumento
		  AND Even."IdTipoEventoCapital" = 'CC'
		  AND (Even."IdEstado" = 'VIG');
		  
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

			SELECT SUBSTRING(Archi."FECHACORTE",7,4) || '-' || SUBSTRING(Archi."FECHACORTE",4,2) || '-' || SUBSTRING(Archi."FECHACORTE",1,2) INTO v_FechaCierre
			FROM :p_DataArchivoDetalle AS Archi;
			
			SELECT SUBSTRING(Archi."FECHAENTREGA",7,4) || '-' || SUBSTRING(Archi."FECHAENTREGA",4,2) || '-' || SUBSTRING(Archi."FECHAENTREGA",1,2) INTO v_FechaPago 
			FROM :p_DataArchivoDetalle AS Archi;
	
			SELECT Even."IdEventoCapital" , TRIM(Archi."CODIGOVALOR") into v_IdEventoCapital, v_CodigoValor
			FROM "BSCL.Custodia.BD::CUS.EventoCapital" AS Even
				INNER JOIN :p_DataArchivoDetalle AS Archi
					ON SUBSTRING(Even."FechaPago",1,4) || SUBSTRING(Even."FechaPago",6,2) || SUBSTRING(Even."FechaPago",9,2) = SUBSTRING(Archi."FECHAENTREGA",7,4) || SUBSTRING(Archi."FECHAENTREGA",4,2) || SUBSTRING(Archi."FECHAENTREGA",1,2)
			WHERE Even."IdInstrumento" = :v_IdInstrumento
			  AND Even."IdTipoEventoCapital" = 'CC'
			  AND (Even."IdEstado" = 'VIG');

			-- AGREGO ID DE CLIENTE AL DETALLE DEL ARCHIVO DE ENTRADA
			v_TablaDetalleArchivo	= 	SELECT	 Archivo."CODIGOINTERMEDIARIO"
												,Archivo."NOMBREINTERMEDIARIO"
												,Archivo."CODIGOTITULAR"		
												,Archivo."NOMBRETITULAR"		
												,Archivo."TIPODOCUMENTO"		
												,Archivo."NUMERODOCUMENTO"	
--												,ConCli."IdCliente"
												,CC."IdSocioDeNegocio"
												,Archivo."CODIGOVALOR"		
												,Archivo."DESCRIPCIONVALOR"	
												,Archivo."FECHACORTE"		
												,Archivo."FECHAENTREGA"		
												,Archivo."FECHAACUERDO"		
												,Archivo."STOCKTOTAL"		
												,Archivo."CEROS"				
												,Archivo."VALORNOMINAL"		
												,Archivo."CANTIDADENTREGA"	
												,Archivo."MONEDA"			
												,Archivo."IMPORTE1"			
												,Archivo."IMPORTE2"			
												,Archivo."INTERESBRUTO"		
												,Archivo."CEROS2"			
												,Archivo."INTERESNETO"		
												,Archivo."AMORTIZACIONBRUTO"	
												,Archivo."CEROS3"			
												,Archivo."TOTALAMORTIZACION"	
												,Archivo."CORRELATIVO"		
												,Archivo."CEROS4"			
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
			
			--OBTENGO LOS CLIENTES/PORTAFOLIOS CON SUS RESPECTIVAS CANTIDADES AGRUPADOS, SOLO PARA CARTERAS, NO PARA GARANTÍAS.
			v_TablaEventoClienteAgrupado = 	SELECT 	 EventoCliente."IdSocioNegocio"			AS "IdSocioNegocio"
													,SUM("MontoCapital")					AS "MontoCapital"
													,SUM("MontoInteres")					AS "MontoInteres"
													,SUM("Total")							AS "Total"
											FROM "BSCL.Custodia.BD::CUS.EventoCapitalCliente" AS EventoCliente 
											WHERE EventoCliente."IdEventoCapital" = :v_IdEventoCapital
											  AND EventoCliente."TipoCartera" = 'CUS'
											  AND EventoCliente."CantidadDerecho" > 0
											  AND EventoCliente."IdBoveda" = 'CAV'
											GROUP BY EventoCliente."IdSocioNegocio";
									
--			SELECT 'v_TablaEventoClienteAgrupado', * FROM :v_TablaEventoClienteAgrupado;

			--OBTENGO LOS CLIENTES/PORTAFOLIOS CON SUS RESPECTIVAS CANTIDADES.
			v_TablaEventoCliente = 	SELECT 	 EventoCliente."IdSocioNegocio" 																								AS "IdSocioNegocio"
											,EventoCliente."IdPortafolio" 																									AS "IdPortafolio"  
											,EventoCliente."CantidadDerecho" 																								AS "CantidadDerecho"
											,((SUM(EventoCliente."MontoCapital") / EventoClienteAgrupado."MontoCapital") * 100 )											AS "Peso"
										 	,((((SUM(EventoCliente."MontoInteres") / EventoClienteAgrupado."MontoInteres") * 100 ) * ArchivoDet."INTERESNETO") / 100) 		AS "ProrrateoMontoIneteres"
										 	,((((SUM(EventoCliente."MontoCapital") / EventoClienteAgrupado."MontoCapital") * 100 ) * ArchivoDet."TOTALAMORTIZACION") / 100)	AS "ProrrateoMontoCapital"
									FROM "BSCL.Custodia.BD::CUS.EventoCapitalCliente" AS EventoCliente
										INNER JOIN :v_TablaEventoClienteAgrupado AS EventoClienteAgrupado 
											ON  EventoClienteAgrupado."IdSocioNegocio" = EventoCliente."IdSocioNegocio"
--										INNER JOIN "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio" AS ConSocio
--											ON ConSocio."IdSocioDeNegocio" = EventoCliente."IdSocioNegocio"
										INNER JOIN :v_TablaDetalleArchivo AS ArchivoDet
											ON ArchivoDet."IdSocioDeNegocio" = EventoCliente."IdSocioNegocio" 
									WHERE EventoCliente."IdEventoCapital" = :v_IdEventoCapital
									  AND EventoCliente."TipoCartera" = 'CUS'
									  AND EventoCliente."CantidadDerecho" > 0
									  AND EventoCliente."IdBoveda" = 'CAV'
									GROUP BY	EventoCliente."IdSocioNegocio", 
												EventoCliente."IdPortafolio", 
												EventoCliente."CantidadDerecho", 
												EventoClienteAgrupado."MontoCapital", 
												EventoClienteAgrupado."MontoInteres",
												ArchivoDet."INTERESNETO", 
												ArchivoDet."TOTALAMORTIZACION"; 

--			SELECT 'v_TablaEventoCliente', * FROM :v_TablaEventoCliente;
			  
			--ACTUALIZO LSO CLIENTES EN SU DISPONIBLE.
			UPDATE EvCapCli SET
				 EvCapCli."MontoInteresArchivo"		= TablaEventoCli."ProrrateoMontoIneteres"
				,EvCapCli."MontoCapitalArchivo"		= TablaEventoCli."ProrrateoMontoCapital"
				,EvCapCli."DiferenciaMontoInteres"	= (EvCapCli."MontoInteres" - TablaEventoCli."ProrrateoMontoIneteres")
				,EvCapCli."DiferenciaMontoCapital"	= (EvCapCli."MontoCapital" - TablaEventoCli."ProrrateoMontoCapital")
			FROM "BSCL.Custodia.BD::CUS.EventoCapitalCliente" AS EvCapCli
				INNER JOIN :v_TablaEventoCliente AS TablaEventoCli
					ON TablaEventoCli."IdSocioNegocio" = EvCapCli."IdSocioNegocio"
				   AND TablaEventoCli."IdPortafolio" = EvCapCli."IdPortafolio"
			WHERE EvCapCli."IdEventoCapital" = :v_IdEventoCapital
			  AND EvCapCli."TipoCartera" = 'CUS'
			  AND EvCapCli."CantidadDerecho" > 0
			  AND EvCapCli."IdBoveda" = 'CAV';
			  
			-------------------------------------------------------------------------------------------------------------------------------------

			--OBTENGO LOS CLIENTES/PORTAFOLIOS CON SUS RESPECTIVAS CANTIDADES AGRUPADOS, SOLO PARA GARANTÍAS.
			v_TablaEventoClienteAgrupadoGar	 = 	SELECT 	 EventoCliente."IdSocioNegocio"			AS "IdSocioNegocio"
														,SUM("MontoCapital")					AS "MontoCapital"
														,SUM("MontoInteres")					AS "MontoInteres"
														,SUM("Total")							AS "Total"
												FROM "BSCL.Custodia.BD::CUS.EventoCapitalCliente" AS EventoCliente 
												WHERE EventoCliente."IdEventoCapital" = :v_IdEventoCapital
												  AND EventoCliente."TipoCartera" = 'GAR'
												  AND EventoCliente."CantidadDerecho" > 0
												  AND EventoCliente."IdBoveda" = 'CAV'
												GROUP BY EventoCliente."IdSocioNegocio";
									
--			SELECT 'v_TablaEventoClienteAgrupado', * FROM :v_TablaEventoClienteAgrupado;

			--OBTENGO LOS CLIENTES/PORTAFOLIOS CON SUS RESPECTIVAS CANTIDADES.
			v_TablaEventoClienteGar = 	SELECT 	 EventoCliente."IdSocioNegocio" 																																	AS "IdSocioNegocio"
												,EventoCliente."IdPortafolio" 																																		AS "IdPortafolio"  
												,EventoCliente."CantidadDerecho" 																																	AS "CantidadDerecho"
												,((SUM(EventoCliente."MontoCapital") / EventoClienteAgrupado."MontoCapital") * 100 )																				AS "Peso"
											 	,((((SUM(EventoCliente."MontoInteres") / EventoClienteAgrupado."MontoInteres") * 100 ) * (ArchivoDet."INTERESBRUTO" - ArchivoDet."INTERESNETO")) / 100) 			AS "ProrrateoMontoIneteres"
											 	,((((SUM(EventoCliente."MontoCapital") / EventoClienteAgrupado."MontoCapital") * 100 ) * (ArchivoDet."AMORTIZACIONBRUTO" - ArchivoDet."TOTALAMORTIZACION")) / 100)	AS "ProrrateoMontoCapital"
										FROM "BSCL.Custodia.BD::CUS.EventoCapitalCliente" AS EventoCliente
											INNER JOIN :v_TablaEventoClienteAgrupadoGar AS EventoClienteAgrupado 
												ON  EventoClienteAgrupado."IdSocioNegocio" = EventoCliente."IdSocioNegocio"
--											INNER JOIN "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio" AS ConSocio
--												ON ConSocio."IdSocioDeNegocio" = EventoCliente."IdSocioNegocio"
											INNER JOIN :v_TablaDetalleArchivo AS ArchivoDet
												ON ArchivoDet."IdSocioDeNegocio" = EventoCliente."IdSocioNegocio" 
										WHERE EventoCliente."IdEventoCapital" = :v_IdEventoCapital
										  AND EventoCliente."TipoCartera" = 'GAR'
										  AND EventoCliente."CantidadDerecho" > 0
										  AND EventoCliente."IdBoveda" = 'CAV'
										GROUP BY	EventoCliente."IdSocioNegocio", 
													EventoCliente."IdPortafolio", 
													EventoCliente."CantidadDerecho", 
													EventoClienteAgrupado."MontoCapital", 
													EventoClienteAgrupado."MontoInteres", 
													ArchivoDet."INTERESBRUTO", 
													ArchivoDet."INTERESNETO",
													ArchivoDet."AMORTIZACIONBRUTO",
													ArchivoDet."TOTALAMORTIZACION"; 

--			SELECT 'v_TablaEventoCliente', * FROM :v_TablaEventoCliente;

			--ACTUALIZO LSO CLIENTES GARANTIAS
			UPDATE EvCapCli SET
				 EvCapCli."MontoInteresArchivo"		= TablaEventoCli."ProrrateoMontoIneteres"
				,EvCapCli."MontoCapitalArchivo"		= TablaEventoCli."ProrrateoMontoCapital"
				,EvCapCli."DiferenciaMontoInteres"	= (EvCapCli."MontoInteres" - TablaEventoCli."ProrrateoMontoIneteres")
				,EvCapCli."DiferenciaMontoCapital"	= (EvCapCli."MontoCapital" - TablaEventoCli."ProrrateoMontoCapital")
			FROM "BSCL.Custodia.BD::CUS.EventoCapitalCliente" AS EvCapCli
				INNER JOIN :v_TablaEventoClienteGar AS TablaEventoCli
					ON TablaEventoCli."IdSocioNegocio" = EvCapCli."IdSocioNegocio"
				   AND TablaEventoCli."IdPortafolio" = EvCapCli."IdPortafolio"
			WHERE EvCapCli."IdEventoCapital" = :v_IdEventoCapital
			  AND EvCapCli."TipoCartera" = 'GAR'
			  AND EvCapCli."CantidadDerecho" > 0
			  AND EvCapCli."IdBoveda" = 'CAV';
		END IF;
	END;
END;
GO