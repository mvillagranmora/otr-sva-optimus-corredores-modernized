SET SCHEMA "BSCL";
GO
DROP PROCEDURE  "BSCL.Custodia.BD.Servicios::EVC.CargarEventoCapitalClienteDesdeArchivo";
GO
CREATE PROCEDURE "BSCL.Custodia.BD.Servicios::EVC.CargarEventoCapitalClienteDesdeArchivo" (

	-- Parametros entrada comunes en todos los servicios privados
	IN p_IdEmpresa				BIGINT,			--ID Empresa destino
    IN p_IdUsuario				BIGINT 			default 0,
	IN p_IdModulo				NVARCHAR(3),	--ID del Modulo que lo invoca

	-- Parametros entrada del servicio privado
	IN p_DataArchivoEncabezado	TABLE (	"INDICADORREGISTROCABECERA"		NVARCHAR(1),
										"CODIGOINTERMEDIARIO"			NVARCHAR(4),
										"NOMBREINTERMEDIARIO"           NVARCHAR(30),
										"CODIGOVALOR"					NVARCHAR(12),
										"DESCRIPCIONVALOR"				NVARCHAR(30),
										"FECHACORTE"					DECIMAL(8,0),
										"FECHAENTREGA"					DECIMAL(8,0),
										"MONEDA"						DECIMAL(2,0),
										"FACTOR"						DECIMAL(13,10),
										"INDICADORREDONDEO"				NVARCHAR(1),
										"VALORNOMINAL"					DECIMAL(14,6),
										"ESPACIOS"						NVARCHAR(26),
										"MENSAJEVALIDACION"				NVARCHAR(200)),
										
	IN p_DataArchivoDetalle		TABLE (	"INDICADORREGISTRODETALLE"		NVARCHAR(1),
										"CODIGOTITULAR"					DECIMAL(8,0),
										"NOMBRETITULAR"					NVARCHAR(50),
										"TIPODOCUMENTO"					NVARCHAR(3),
										"NUMERODOCUMENTO"				NVARCHAR(20),
										"ESPACIOS"						NVARCHAR(20),
										"STOCKSALDOTOTALFECHACORTE"		DECIMAL(14,0),
										"STOCKSALDOPRENDA"				DECIMAL(14,0),
										"STOCKSALDODISPONIBLE"			DECIMAL(14,0),
										"BENEFICIOENTREGADO"			DECIMAL(15,2),
										"MENSAJEVALIDACION"				NVARCHAR(200)),
										
	IN p_DataArchivoPie		TABLE (	"INDICADORREGISTROFINAL"		NVARCHAR(1),
									"NUMEROREGISTROSARCHIVO"		DECIMAL(10,0),
									"TOTALSALDOCONTABLE"			DECIMAL(18,0),
									"TOTALSALDOPRENDA"				DECIMAL(18,0),
									"TOTALSALDODISPONIBLE"			DECIMAL(18,0),
									"TOTALBENEFICIOSENTREGADOS"		DECIMAL(18,2),
									"ESPACIOS"						NVARCHAR(66),
									"MENSAJEVALIDACION"				NVARCHAR(200)),
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
		DECLARE	v_FechaSistemaCreacion			TIMESTAMP;
		DECLARE	v_countTransacciones			BIGINT;
		DECLARE	v_countOrdenes					BIGINT;
		DECLARE	v_countClientes					BIGINT;
		DECLARE	v_countAsignacion				BIGINT;
		DECLARE v_CuentaRegistros				BIGINT = 0;
		DECLARE v_IdEventoCapital				BIGINT;
		DECLARE v_Nemotecnico					NVARCHAR(40);
		DECLARE v_CodigoValor					NVARCHAR(40);
		DECLARE v_IdMoneda						NVARCHAR(3);
		DECLARE v_STOCKSALDOTOTALFECHACORTE		DECIMAL(18,0);
		DECLARE v_STOCKSALDOPRENDA				DECIMAL(18,0);
		DECLARE v_STOCKSALDODISPONIBLE			DECIMAL(18,0);
		DECLARE v_TOTALSALDOCONTABLE			DECIMAL(18,0);
		DECLARE v_TOTALSALDOPRENDA				DECIMAL(18,0);
		DECLARE v_TOTALSALDODISPONIBLE			DECIMAL(18,0);
		DECLARE v_FechaCierre					NVARCHAR(10);
		DECLARE v_FechaPago						NVARCHAR(10);
		DECLARE v_IdInstrumento					BIGINT;
		DECLARE v_CUENTAREGISTROSDETALLE		BIGINT = 0;
		DECLARE v_CANTIDADREGISTROSPIE			BIGINT = 0;
		--------------------
		-- Validaciones
		-- Si se encuentra algún registro, no se puede cargar el archivo.
		-- FALTA QUE ESTE PROCEDIMIENTO TENGA CONSULTA DE DICCIONARIO DE ERRORES PARA PODER SER ENVIADOS HACIA LA ui
		--------------------

		BEGIN

			-- VALIDAMOS QUE EL ARCHIVO CUADRE, RESPECTO A SUS DETALLES Y PIE DE ARCHIVO.
			SELECT 	SUM(ArchDeta."STOCKSALDOTOTALFECHACORTE") 	 
					,SUM(ArchDeta."STOCKSALDOPRENDA") 			
					,SUM(ArchDeta."STOCKSALDODISPONIBLE") 		INTO v_STOCKSALDOTOTALFECHACORTE, v_STOCKSALDOPRENDA, v_STOCKSALDODISPONIBLE
			FROM :p_DataArchivoDetalle AS ArchDeta;
			
			SELECT 	ArchPie."TOTALSALDOCONTABLE" 
					,ArchPie."TOTALSALDOPRENDA"
					,ArchPie."TOTALSALDODISPONIBLE" 			INTO v_TOTALSALDOCONTABLE, v_TOTALSALDOPRENDA, v_TOTALSALDODISPONIBLE
			FROM :p_DataArchivoPie AS ArchPie;
			
			IF (:v_STOCKSALDOTOTALFECHACORTE != :v_TOTALSALDOCONTABLE or :v_STOCKSALDOPRENDA != :v_TOTALSALDOPRENDA or :v_STOCKSALDODISPONIBLE != :v_TOTALSALDODISPONIBLE) THEN
				p_out_ErroresDetalle = select 'ARCHIVO_DE_CARGA_DESCUADRADO' as "Codigo",:v_erroroutmsg as "Descripcion" from dummy;
				SIGNAL SALIDA_CONTROLADA;				
			END IF;
			
			--VALIDAMOS NUMERO DE REGISTROS VERSUS LOS REGISTROS INFORMADOS EN PIE DE ARCHIVO.
			SELECT 	COUNT(1) INTO v_CUENTAREGISTROSDETALLE
			FROM :p_DataArchivoDetalle;
			
			SELECT 	NUMEROREGISTROSARCHIVO INTO v_CANTIDADREGISTROSPIE
			FROM :p_DataArchivoPie;
			
			IF (:v_CUENTAREGISTROSDETALLE != :v_CANTIDADREGISTROSPIE) THEN
				p_out_ErroresDetalle = select 'ARCHIVO_DE_CARGA_DESCUADRADO' as "Codigo",:v_erroroutmsg as "Descripcion" from dummy;
				SIGNAL SALIDA_CONTROLADA;				
			END IF;
			
			-- VALIDAMOS MONEDA.
			SELECT COUNT(1) INTO v_CuentaRegistros
			FROM :p_DataArchivoEncabezado AS Encabezado
				INNER JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarMoneda" AS Moneda
					ON Encabezado."MONEDA" = Moneda."MonedaSMV"
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
						ON Encabezado."MONEDA" = Moneda."MonedaSMV"
					   AND Moneda."IdEstado" = 'ACT';
			END IF;
			
		END;


		CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema" (:p_IdEmpresa,v_FechaSistemaCreacion);

		v_CuentaRegistros := 0;
		--VALIDAMOS CODIGO ISIN
		SELECT COUNT(1) INTO v_CuentaRegistros 
		FROM :p_DataArchivoEncabezado AS Archi
			INNER JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" AS ConsIns
				ON TRIM(Archi."CODIGOVALOR") = ConsIns."ISIN"
			   AND ConsIns."IdEstado" = 'ACT';
	
		IF (:v_CuentaRegistros = 1) THEN
			SELECT "Nemotecnico" INTO v_Nemotecnico 
			FROM :p_DataArchivoEncabezado AS Archi
				INNER JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" AS ConsIns
					ON TRIM(Archi."CODIGOVALOR") = ConsIns."ISIN"
				   AND ConsIns."IdEstado" = 'ACT';
		END IF;
		
		IF (:v_CuentaRegistros = 0) THEN
			p_out_ErroresContador := 1;
			p_out_ErroresDetalle = select 'UI_CODIGO_ISIN_NO_ENCONTRADO_EN_MAESTRO_INSTRUMENTOS' as "Codigo",:v_erroroutmsg as "Descripcion" from dummy;
			SIGNAL SALIDA_CONTROLADA;
		END IF;
		
		---------------------------------------------------------------
		-- Buscamos a que evento capital corresponde el archivo cargado
		---------------------------------------------------------------
		v_CuentaRegistros := 0;

		SELECT Count(1) into v_CuentaRegistros
		FROM "BSCL.Custodia.BD::CUS.EventoCapital" AS Even
			INNER JOIN :p_DataArchivoEncabezado AS Archi
				ON SUBSTRING(Even."FechaLimite",1,4) || SUBSTRING(Even."FechaLimite",6,2) || SUBSTRING(Even."FechaLimite",9,2) = Archi."FECHACORTE"
			   AND Even."Factor" = Archi."FACTOR"
		WHERE Even."Nemo" = :v_Nemotecnico
		  AND Even."IdTipoEventoCapital" = 'DV';
		  
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
			v_CuentaRegistros := 0;
			SELECT Even."IdEventoCapital", TRIM(Archi."CODIGOVALOR") into v_IdEventoCapital, v_CodigoValor
			FROM "BSCL.Custodia.BD::CUS.EventoCapital" AS Even
				INNER JOIN :p_DataArchivoEncabezado AS Archi
					ON SUBSTRING(Even."FechaLimite",1,4) || SUBSTRING(Even."FechaLimite",6,2) || SUBSTRING(Even."FechaLimite",9,2) = Archi."FECHACORTE"
				   AND Even."Factor" = Archi."FACTOR"
			WHERE Even."Nemo" = :v_Nemotecnico
			  AND Even."IdTipoEventoCapital" = 'DV'
			  AND (Even."IdEstado" = 'PRO' OR Even."IdEstado" = 'PEN');
 
			-- AGREGO ID DE CLIENTE AL DETALLE DEL ARCHIVO DE ENTRADA
			v_TablaDetalleArchivo = SELECT	Archivo."INDICADORREGISTRODETALLE"	,
											--ConCli."IdCliente"					,
											CC."IdSocioDeNegocio"				,
											Archivo."CODIGOTITULAR"				,
											Archivo."NOMBRETITULAR"				,
											Archivo."TIPODOCUMENTO"				,
											Archivo."NUMERODOCUMENTO"			,
											Archivo."ESPACIOS"					,
											Archivo."STOCKSALDOTOTALFECHACORTE"	,
											Archivo."STOCKSALDOPRENDA"			,
											Archivo."STOCKSALDODISPONIBLE"		,
											Archivo."BENEFICIOENTREGADO"		,
											Archivo."MENSAJEVALIDACION"			
									FROM :p_DataArchivoDetalle AS Archivo
										INNER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarDocumentoIdentidadPersona" CDIP
											ON CDIP."IdTipoDocumentoIdentidad" = TRIM(Archivo."TIPODOCUMENTO")
										   AND CDIP."DocumentoIdentidad" = TRIM(Archivo."NUMERODOCUMENTO")
										   AND CDIP."IdEstado" = 'ACT'
										INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" CC
											ON CDIP."IdPersona" = CC."IdPersona"
										   AND CC."IdEstado" = 'ACT';
/*										INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" as CPDI
											ON TRIM(Archivo."TIPODOCUMENTO")	= TRIM(CPDI."TipoDocumentoIdentidad")
										   AND TRIM(Archivo."NUMERODOCUMENTO")	= TRIM(CPDI."DocumentoIdentidad")
										   AND CPDI."IdEstado" = 'ACT';*/
											

			SELECT SUM(STOCKSALDODISPONIBLE) INTO v_CuentaRegistros
			FROM :v_TablaDetalleArchivo;
						
			IF (:v_CuentaRegistros > 0) THEN
				--OBTENGO LOS CLIENTES/PORTAFOLIOS CON SUS RESPECTIVAS CANTIDADES AGRUPADOS, SOLO PARA CARTERAS, NO PARA GARANTÍAS.
				v_TablaEventoClienteAgrupado = 	SELECT 	 EventoCliente."IdSocioNegocio"			AS "IdSocioNegocio"
														,SUM("CantidadDerecho")					AS "CantidadDerecho"
														,SUM("Total")							AS "Total"
												FROM "BSCL.Custodia.BD::CUS.EventoCapitalCliente" AS EventoCliente 
												WHERE EventoCliente."IdEventoCapital" = :v_IdEventoCapital
												  AND EventoCliente."TipoCartera" = 'CUS'
												  AND EventoCliente."CantidadDerecho" > 0
												  AND EventoCliente."IdBoveda" = 'CAV'
												GROUP BY EventoCliente."IdSocioNegocio";
	
				--OBTENGO LOS CLIENTES/PORTAFOLIOS CON SUS RESPECTIVAS CANTIDADES.
				v_TablaEventoCliente = 	SELECT 	 EventoCliente."IdSocioNegocio" 																											AS "IdSocioNegocio"
												,EventoCliente."IdPortafolio" 																												AS "IdPortafolio"  
												,EventoCliente."CantidadDerecho" 																											AS "CantidadDerecho"
												
												,((SUM(EventoCliente."CantidadDerecho") / EventoClienteAgrupado."CantidadDerecho") * 100 )													AS "PesoCantidadDerecho"
												,((((SUM(EventoCliente."CantidadDerecho") / EventoClienteAgrupado."CantidadDerecho") * 100 ) * ArchivoDet."STOCKSALDODISPONIBLE") / 100)	AS "ProrrateCantidadDerecho"
												
												,((SUM(EventoCliente."Total") / EventoClienteAgrupado."Total") * 100 )																		AS "PesoDividendo"
												,((((SUM(EventoCliente."Total") / EventoClienteAgrupado."Total") * 100 ) * ArchivoDet."BENEFICIOENTREGADO") / 100)							AS "ProrrateDividendo"
	
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
													EventoCliente."Total", 
													EventoClienteAgrupado."CantidadDerecho",
													EventoClienteAgrupado."Total", 
													ArchivoDet."STOCKSALDODISPONIBLE",
													ArchivoDet."BENEFICIOENTREGADO";

				--ACTUALIZO LOS CLIENTES EN SU DISPONIBLE.
				UPDATE EvCapCli SET
					 EvCapCli."CantidadDerechoArchivo"		= TablaEventoCli."ProrrateCantidadDerecho"
					,EvCapCli."MontoBeneficioArchivo"		= TablaEventoCli."ProrrateDividendo"
					,EvCapCli."DiferenciaCantidadDerecho"	= (EvCapCli."CantidadDerecho" - TablaEventoCli."ProrrateCantidadDerecho")
					,EvCapCli."DiferenciaMontoBeneficio"	= (EvCapCli."Total" - TablaEventoCli."ProrrateDividendo")
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
					 EvCapCli."CantidadDerechoArchivo"		= (PieArchivo."TOTALSALDODISPONIBLE" * -1)
					,EvCapCli."MontoBeneficioArchivo"		= (PieArchivo."TOTALBENEFICIOSENTREGADOS" * -1)
					,EvCapCli."DiferenciaCantidadDerecho"	= (EvCapCli."CantidadDerecho" - (PieArchivo."TOTALSALDODISPONIBLE" * -1))
					,EvCapCli."DiferenciaMontoBeneficio"	= (EvCapCli."Total" - (PieArchivo."TOTALBENEFICIOSENTREGADOS" * -1))
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
			
			SELECT SUM(STOCKSALDOPRENDA) INTO v_CuentaRegistros
			FROM :v_TablaDetalleArchivo;
												
			IF (:v_CuentaRegistros > 0) THEN
				--OBTENGO LOS CLIENTES/PORTAFOLIOS CON SUS RESPECTIVAS CANTIDADES AGRUPADOS, SOLO PARA CARTERAS, NO PARA GARANTÍAS.
				v_TablaEventoClienteAgrupadoGAR = 	SELECT 	 EventoCliente."IdSocioNegocio"			AS "IdSocioNegocio"
															,SUM("CantidadDerecho")					AS "CantidadDerecho"
															,SUM("Total")							AS "Total"
													FROM "BSCL.Custodia.BD::CUS.EventoCapitalCliente" AS EventoCliente 
													WHERE EventoCliente."IdEventoCapital" = :v_IdEventoCapital
													  AND EventoCliente."TipoCartera" = 'GAR'
													  AND EventoCliente."CantidadDerecho" > 0
													  AND EventoCliente."IdBoveda" = 'CAV'
													GROUP BY EventoCliente."IdSocioNegocio";
	
				--OBTENGO LOS CLIENTES/PORTAFOLIOS CON SUS RESPECTIVAS CANTIDADES.
				v_TablaEventoClienteGAR = 	SELECT 	 EventoCliente."IdSocioNegocio" 																										AS "IdSocioNegocio"
												,EventoCliente."IdPortafolio" 																												AS "IdPortafolio"  
												,EventoCliente."CantidadDerecho" 																											AS "CantidadDerecho"
												
												,((SUM(EventoCliente."CantidadDerecho") / EventoClienteAgrupado."CantidadDerecho") * 100 )													AS "PesoCantidadDerecho"
												,((((SUM(EventoCliente."CantidadDerecho") / EventoClienteAgrupado."CantidadDerecho") * 100 ) * ArchivoDet."STOCKSALDOPRENDA") / 100)		AS "ProrrateCantidadDerecho"
												
												,((SUM(EventoCliente."Total") / EventoClienteAgrupado."Total") * 100 )																		AS "PesoDividendo"
												,((((SUM(EventoCliente."Total") / EventoClienteAgrupado."Total") * 100 ) * ArchivoDet."BENEFICIOENTREGADO") / 100)							AS "ProrrateDividendo"
	
										FROM "BSCL.Custodia.BD::CUS.EventoCapitalCliente" AS EventoCliente
											INNER JOIN :v_TablaEventoClienteAgrupadoGAR AS EventoClienteAgrupado 
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
													EventoCliente."Total", 
													EventoClienteAgrupado."CantidadDerecho",
													EventoClienteAgrupado."Total", 
													ArchivoDet."STOCKSALDOPRENDA",
													ArchivoDet."BENEFICIOENTREGADO";
				  
				--ACTUALIZO LSO CLIENTES EN SU DISPONIBLE.
				UPDATE EvCapCli SET
					 EvCapCli."CantidadDerechoArchivo"		= TablaEventoCli."ProrrateCantidadDerecho"
					,EvCapCli."MontoBeneficioArchivo"		= TablaEventoCli."ProrrateDividendo"
					,EvCapCli."DiferenciaCantidadDerecho"	= (EvCapCli."CantidadDerecho" - TablaEventoCli."ProrrateCantidadDerecho")
					,EvCapCli."DiferenciaMontoBeneficio"	= (EvCapCli."Total" - TablaEventoCli."ProrrateDividendo")
				FROM "BSCL.Custodia.BD::CUS.EventoCapitalCliente" AS EvCapCli
					INNER JOIN :v_TablaEventoClienteGAR AS TablaEventoCli
						ON TablaEventoCli."IdSocioNegocio" = EvCapCli."IdSocioNegocio"
					   AND TablaEventoCli."IdPortafolio" = EvCapCli."IdPortafolio"
				WHERE EvCapCli."IdEventoCapital" = :v_IdEventoCapital
				  AND EvCapCli."TipoCartera" = 'GAR'
				  AND EvCapCli."CantidadDerecho" > 0
				  AND EvCapCli."IdBoveda" = 'CAV';
				  
				-- ACTUALIZAMOS CONTRAPARTE A PARTIR DEL ARCHIVO.
				UPDATE EvCapCli SET
					 EvCapCli."CantidadDerechoArchivo"		= (PieArchivo."TOTALSALDODISPONIBLE" * -1)
					,EvCapCli."MontoBeneficioArchivo"		= (PieArchivo."TOTALBENEFICIOSENTREGADOS" * -1)
					,EvCapCli."DiferenciaCantidadDerecho"	= (EvCapCli."CantidadDerecho" - (PieArchivo."TOTALSALDODISPONIBLE" * -1)) 
					,EvCapCli."DiferenciaMontoBeneficio"	= (EvCapCli."Total" - (PieArchivo."TOTALBENEFICIOSENTREGADOS" * -1))
				FROM "BSCL.Custodia.BD::CUS.EventoCapitalCliente" AS EvCapCli
					 ,:p_DataArchivoPie AS PieArchivo
				WHERE EvCapCli."IdEventoCapital" = :v_IdEventoCapital
				  AND EvCapCli."TipoCartera" = 'GAR'
				  AND EvCapCli."CantidadDerecho" < 0
				  AND EvCapCli."IdBoveda" = 'CAV';
			END IF;
			-------------------------------------------------------------------------------------------------------------------------------------
			-- INSERTAMOS CLIENTES QUE ESTÉN EN EL ARCHIVO, PERO QUE NO SE ENCUENTREN CONGELADOS EN LOS EVENTOS CAPITAL CLIENTE.
			-------------------------------------------------------------------------------------------------------------------------------------

			--OBTENGO LOS CLIENTES 
			v_TablaEventoClienteIdCliente = SELECT EventoCliente."IdSocioNegocio" --ConSocio."IdCliente" 
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
				v_RegistrosAInsertar =	SELECT	ArchivoDet."INDICADORREGISTRODETALLE"	,
--												ArchivoDet."IdCliente"					,
												ArchivoDet."IdSocioDeNegocio"			,
												ArchivoDet."CODIGOTITULAR"				,
												ArchivoDet."NOMBRETITULAR"				,
												ArchivoDet."TIPODOCUMENTO"				,
												ArchivoDet."NUMERODOCUMENTO"			,
												ArchivoDet."ESPACIOS"					,
												ArchivoDet."STOCKSALDOTOTALFECHACORTE"	,
												ArchivoDet."STOCKSALDOPRENDA"			,
												ArchivoDet."STOCKSALDODISPONIBLE"		,
												ArchivoDet."BENEFICIOENTREGADO"			,
												ArchivoDet."MENSAJEVALIDACION"			
										FROM :v_TablaEventoClienteIdCliente AS EventoIdCliente
											RIGHT OUTER JOIN :v_TablaDetalleArchivo AS ArchivoDet
												ON EventoIdCliente."IdSocioNegocio"  = ArchivoDet."IdSocioDeNegocio" 
										WHERE EventoIdCliente."IdSocioNegocio" IS NULL;

				--INSERTAMOS EVENTOS DE CAPITAL CLIENTE.
				SELECT SUBSTRING(Archi."FECHACORTE",1,4) || '-' || SUBSTRING(Archi."FECHACORTE",5,2) || '-' || SUBSTRING(Archi."FECHACORTE",7,2) INTO v_FechaCierre
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
					"CantidadDerechoArchivo",						"MontoBeneficioArchivo",			"DiferenciaCantidadDerecho",		"DiferenciaMontoBeneficio")
					
				SELECT 
				    "BSCL.Custodia.BD.Secuencias::EventoCapitalCliente".NEXTVAL,
																	Even."IdEventoCapital",				Even."IdTipoEventoCapital",			Even."NumeroDividendo",
				    Even."Secuencia",								ConCli."IdSocioDeNegocio",			'CLI',								ConCli."IdPortafolioDefecto",
				    Archivo."TIPODOCUMENTO",						Archivo."NUMERODOCUMENTO",			ConCli."NombreCompleto",			ConsIns."IdClasificacionTipoInstrumento",
				    ConsIns."IdTipoInstrumento",					ConsIns."IdInstrumento",			ConsIns."Nemotecnico",				0,
				    '',												:v_FechaCierre,						:v_FechaPago,						Archivo."STOCKSALDODISPONIBLE",
					'CUS',											0,									0,									0,
					Archivo."BENEFICIOENTREGADO",					:v_IdMoneda,						0,									COALESCE(Eje."CodigoEmpleado",''),
					COALESCE(Eje."NombreCompletoEjecutivo",''),		COALESCE(Eje."CodigoSucursal",''),	Current_UTCTIMESTAMP,				Current_UTCTIMESTAMP,
					p_IdUsuario,									'VIG',								'CAV',								'CAR',
					Archivo."STOCKSALDODISPONIBLE",					Archivo."BENEFICIOENTREGADO",		0,									0	
				FROM :v_RegistrosAInsertar AS Archivo
				INNER JOIN "BSCL.Custodia.BD::CUS.EventoCapital" AS Even
					ON Even."IdEventoCapital" = :v_IdEventoCapital
				LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS ConCli
					ON Archivo."IdSocioDeNegocio" = ConCli."IdSocioDeNegocio"
--					ON TRIM(Archivo."TIPODOCUMENTO")	= TRIM(ConCli."TipoDocumentoIdentidad")
--				   AND TRIM(Archivo."NUMERODOCUMENTO")	= TRIM(ConCli."DocumentoIdentidad")
--				   AND ConCli."IdEstado" = 'ACT'
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