SET SCHEMA "BSCL";
DROP PROCEDURE "BSCL.Normativa.BD.Servicios::CIR.CertificadoCustodia";
CREATE PROCEDURE "BSCL.Normativa.BD.Servicios::CIR.CertificadoCustodia" ( 
--  Tecnologia : CrystalReports
--  Proposito: Servicio para consultar informacion para el Certificado de custodia y Carta Circularizacion.
--  Autor: CFLORES - BCS
--  Fecha Creacion: 11/10/2016

--  Modificacion: Se incluye forma masiva desde nomina de circularizacion
--  Autor: CFLORES - BCS
--  Fecha Modificacion: 29/08/2022

	--VARIABLES DE ENTRADA OBLIGATORIAS
    IN p_IdEmpresa				BIGINT,
    IN p_IdCliente				BIGINT,
    IN p_IdPortafolio			BIGINT,
    IN p_FechaConsulta			NVARCHAR(10),			 --'YYYY-MM-DD'

    IN p_FormaSalida				NVARCHAR(14) DEFAULT 'CERTIFICADO', --'CARTA'
    IN p_UnificarPortafolios		NVARCHAR(1) DEFAULT 'N',
    IN p_IdUsuario					BIGINT DEFAULT NULL,
	IN p_CartaConfirmacionCliente   NVARCHAR(1) DEFAULT 'S'
	
	,OUT p_Salida   TABLE (
	 		"OrdenInterno"							BIGINT,	
	 		"NombreDestinatario"					NVARCHAR(400),
	 		"Rut"									NVARCHAR(40),
	 		"CodigoPortafolio"						NVARCHAR(40),
	 		"IdPortafolio"							BIGINT,	
	 		"Direccion"								NVARCHAR(200),
	 		
	 		
	 		"Comuna"								NVARCHAR(200),
	 		"Ciudad"								NVARCHAR(200),
	 		"TelefonoDestinatario"					NVARCHAR(40),
	 		"IdTipoPersona"							NVARCHAR(10),
	 		"FormaSalida"							NVARCHAR(40),
	 		"FechaConsulta"							NVARCHAR(40),
	 		"Grupo"									NVARCHAR(40),
	 		"IdClasificacionTipoInstrumento"		NVARCHAR(40),
	 		"Nemotecnico"							NVARCHAR(40),
	 		"IdMoneda"								NVARCHAR(40),
	 		"Cantidad"								DECIMAL(26,8),
	 		"ValorPresente"							DECIMAL(26,8),
	 		"FechaVencimiento"						NVARCHAR(40),
	 		"PrecioCierre"							DECIMAL(26,8),
	 		"Tasa"									DECIMAL(26,8),
	 		"IdCliente"								BIGINT,
	 		"FechaSistemaLOCAL"						NVARCHAR(40),
	 		"HoraSistemaLOCAL"						NVARCHAR(40),
	 		"CartaConfirmacionCliente"				NVARCHAR(40),
	 		"PrecioDecimales"						DECIMAL(26,8),
	 		"TasaDecimales"							DECIMAL(26,8),
	 		"EjecutivoNombre"	 					NVARCHAR(400),
	 		"EjecutivoDireccionElectronica"			NVARCHAR(100),
	 		"Sexo"									NVARCHAR(40),
	 		"EstadoCivil"							NVARCHAR(40),
	 		-------------------------------------------------------
 			"RazonCorredora"						NVARCHAR(400),
	 		"DireccionCorredora"           			NVARCHAR(300),
	 		"RutCorredora"							NVARCHAR(15),
	 		"TelefonoCorredora"						NVARCHAR(40),
	 		-------------------------------------------------------
	 		"LogoCorredora"                 BLOB,
	 		"FirmaCorredora" 				BLOB,
			"EsMasivo"								NVARCHAR(1),
			"NombreCompletoRepresentante"     NVARCHAR(80),
            "DocumentoIdentidadRepresentante" NVARCHAR(40)
	
	)

)
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
    AS
BEGIN
		--------------------
		--Declare Variables
		--------------------
		DECLARE v_Count,v_idPersona,v_IdEmpleadoEjecutivo	 BIGINT:=0;
		DECLARE v_NombreDestinatario,v_EjecutivoNombre, v_EjecutivoDireccionElectronica		NVARCHAR(100):='';
		DECLARE v_IdTelefonoDestinatario, v_TelefonoDestinatario 							NVARCHAR(40):='';
		DECLARE v_IdTipoPersona								NVARCHAR(1) :='';
		DECLARE v_Direccion									NVARCHAR(80):='';
		DECLARE v_Comuna									NVARCHAR(60):='';
		DECLARE v_Ciudad									NVARCHAR(60):='';		
		DECLARE v_Rut										NVARCHAR(12):='';	
		DECLARE v_CodigoPortafolio							NVARCHAR(100):='';	
		DECLARE v_IdNormativa								NVARCHAR(15):='CIRCULARIZACION';
		DECLARE v_logo TABLE("LogoCorredora" BLOB);
		DECLARE v_firma TABLE("FirmaCorredora" BLOB);
		DECLARE v_Masivo TABLE("EsMasivo" NVARCHAR(1));
		DECLARE v_DatosRepresentanteLegal TABLE("NombreCompletoRepresentante" NVARCHAR(80), "DocumentoIdentidadRepresentante" NVARCHAR(40));
		DECLARE v_PeriodoCarta								NVARCHAR(6):='';
		DECLARE v_SIM,v_PAC,v_VTACOR,v_FORWD,v_PRSTM		NVARCHAR(3):='';

		DECLARE v_FechaSistemaUTC   	NVARCHAR(40);
		DECLARE v_FechaSistemaLOCAL   	NVARCHAR(40);
		DECLARE v_FechaHoraSistemaUTC   NVARCHAR(40);
		DECLARE v_FechaHoraSistemaLOCAL SECONDDATE;
		DECLARE v_FechaHoraUTC   		NVARCHAR(40);
		DECLARE v_FechaHoraLOCAL   		NVARCHAR(40);
		DECLARE v_AgregarCuentaCaja		NVARCHAR(1):='N';
		DECLARE v_CuentaInversionAConsultar		TABLE ("IdEjecutivo" BIGINT,"IdPortafolio" BIGINT,"IdMoneda" NVARCHAR(3));
		DECLARE v_SaldoInicialFinal			 	TABLE("IdCuentaInversion"   BIGINT,
    										  	 "SaldoEfectivoInicial"		DECIMAL(26,8),
    										  	 "SaldoEfectivoFinal"		DECIMAL(26,8),
    										  	 "SaldoMovimientoInicial"	DECIMAL(26,8),
    										  	 "SaldoMovimientoFinal"		DECIMAL(26,8));
	  	DECLARE v_SaldoPorFechaEfectiva      	TABLE("IdEjecutivo"			BIGINT,
    											 "NombreEjecutivo"			NVARCHAR(300),
    											 "CodigoSucursal"			NVARCHAR(40),
		 										 "NombreSucursal"			NVARCHAR(100),
    											 "IdCuentaInversion"      	BIGINT, 
                                              	 "CodigoCuentaInversion"	NVARCHAR(40), 
                                              	 "IdCliente"               	BIGINT,
                                              	 "NombreCliente"			NVARCHAR(300),
                                              	 "DocumentoIdentidad"		NVARCHAR(40),
                                              	 "IdPortafolio"            	BIGINT,  
                                              	 "NombrePortafolio"			NVARCHAR(100),
                                              	 "FechaSaldo" 				NVARCHAR(10),
                                              	 "IdMoneda"                	NVARCHAR(3),
                                              	 "CodigoMoneda"				NVARCHAR(40),
                                              	 "NombreMoneda"				NVARCHAR(100),
                                              	 "SaldoDisponible"          DECIMAL(26,8),
                                              	 "SaldoEnGarantia"			DECIMAL(26,8),
                                              	 "SaldoAPlazo"				DECIMAL(26,8),
                                              	 "SaldoRetiro"				DECIMAL(26,8),
                                              	 "Producto"					NVARCHAR(100));
		DECLARE v_SaldoPorFechaMovimiento		TABLE ("IdEjecutivo"		BIGINT,
    											 "NombreEjecutivo"			NVARCHAR(300),
    											 "CodigoSucursal"			NVARCHAR(40),
		 										 "NombreSucursal"			NVARCHAR(100),
												 "IdCuentaInversion"      	BIGINT, 
                                              	 "CodigoCuentaInversion"	NVARCHAR(40), 
                                              	 "IdCliente"               	BIGINT,
                                              	 "NombreCliente"			NVARCHAR(300),
                                              	 "DocumentoIdentidad"		NVARCHAR(40),
                                              	 "IdPortafolio"            	BIGINT,
                                              	 "NombrePortafolio"			NVARCHAR(100),
                                              	 "PropositoPortafolio"		NVARCHAR(100),
                                              	 "CodigoPortafolio"			NVARCHAR(100),
                                              	 "FechaSaldo" 				NVARCHAR(10),
                                              	 "IdMoneda"                	NVARCHAR(3),
                                              	 "CodigoMoneda"				NVARCHAR(40),
                                              	 "NombreMoneda"				NVARCHAR(100),
                                              	 "SaldoDisponible"          DECIMAL(26,8),
                                              	 "SaldoEnGarantia"			DECIMAL(26,8),
                                              	 "SaldoAPlazo"				DECIMAL(26,8),
                                              	 "SaldoRetiro"				DECIMAL(26,8),
                                              	 "Producto"					NVARCHAR(100));
        DECLARE v_InformacionContador  		INT;
	 	DECLARE v_InformacionDetalle   		TABLE ("Codigo" NVARCHAR(100),"Descripcion" NVARCHAR(5000));
	 	DECLARE v_AdvertenciasContador      INT;
		DECLARE v_AdvertenciasDetalle       TABLE ("Codigo" nvarchar(100),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(5000));	
		DECLARE v_ErroresContador           INT;
		DECLARE v_ErroresDetalle            TABLE ("Codigo" nvarchar(100),"Descripcion" nvarchar(5000));

		DECLARE v_fechaHoraSistema 				NVARCHAR(40):='';
		DECLARE v_IdEmpresa  					BIGINT:=0;
		DECLARE v_FechaConsulta 				NVARCHAR(10) := '2300-12-31';
		DECLARE v_decimales 					TABLE("PrecioDecimales" DECIMAL(26,8), "TasaDecimales" DECIMAL(26,8),"IdMoneda" NVARCHAR(40));
		DECLARE v_DO 							DECIMAL = 0;
		DECLARE v_countRepresentante,v_countFirma,v_countLogo	 BIGINT:=0;
		/*Se Inicializa Variable*/
		v_tablaSalida=
				SELECT
				0 AS "OrdenInterno"
				,'' AS "CodigoPortafolio"
				,0 as "IdPortafolio"
				,'' AS "Grupo"
				,'' AS "IdClasificacionTipoInstrumento"
				,'' AS "Nemotecnico"
				,'' AS "IdMoneda" 
				,0 AS "Cantidad"
				,0 AS "ValorPresente"
				,'' 	AS "FechaVencimiento"
				,0 		AS "PrecioCierre"
				,0 AS "Tasa"
				,0 AS "IdCliente"
				,0 AS "PrecioDecimales"
				,0 AS "TasaDecimales"
				FROM DUMMY WHERE 1=2;						
		v_logo =  SELECT '' AS "LogoCorredora" FROM DUMMY WHERE 1=2;
		v_firma = SELECT '' AS "FirmaCorredora" FROM DUMMY WHERE 1=2;
		v_DatosRepresentanteLegal = SELECT '' AS "NombreCompletoRepresentante", '' AS "DocumentoIdentidadRepresentante" FROM DUMMY	where 1=2;

		WITH dolarObservado AS (
		    SELECT ROW_NUMBER() OVER (PARTITION BY PF."IdParametroFinanciero" ORDER BY PF."Fecha" DESC) AS row_num,
		        PF."Valor"
		    FROM
		        "BSCL.Plataforma.BD::MER.ParametroFinanciero" PF
		    WHERE
		        PF."Fecha" <= :p_FechaConsulta
		        AND PF."IdParametroFinanciero" = 'DO')
					SELECT
					    COALESCE("Valor", 0)
					INTO v_DO
					FROM
					    dolarObservado
					WHERE
					    row_num = 1;

		v_tblParametrosFinancieros = WITH dolarObservado AS (
										    SELECT
										        ROW_NUMBER() OVER (
										            PARTITION BY PF."IdParametroFinanciero"
										            ORDER BY
										                PF."Fecha" DESC
										        ) AS row_num,
										        PF."IdParametroFinanciero",
										        PF."Fecha",
										        PF."Valor"
										    FROM
										        "BSCL.Plataforma.BD::MER.ParametroFinanciero" PF
										    WHERE
										        PF."Fecha" <= :p_FechaConsulta
										)
										SELECT
											"IdParametroFinanciero",
											"Fecha",
											"Valor"
										FROM
										    dolarObservado
										WHERE
										    row_num = 1 AND "IdParametroFinanciero" IN ('EUR', 'NZD', 'GBP', 'PEN', 'COP', 'DO');

		v_IdEmpresa=p_IdEmpresa;
		v_FechaConsulta=p_FechaConsulta;
				
		CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechas"(
		p_IdEmpresa					=> p_IdEmpresa,
		p_out_FechaSistemaUTC		=> v_FechaSistemaUTC,
		p_out_FechaSistemaLOCAL		=> v_FechaSistemaLOCAL,
		p_out_FechaHoraSistemaUTC	=> v_FechaHoraSistemaUTC,
		p_out_FechaHoraSistemaLOCAL	=> v_FechaHoraSistemaLOCAL,
		p_out_FechaHoraUTC			=> v_FechaHoraUTC,
		p_out_FechaHoraLOCAL		=> v_FechaHoraLOCAL
		);
		
		/* Datos del representante legal */
        v_DatosRepresentanteLegal = SELECT PERSONA."NombreCompleto" AS "NombreCompletoRepresentante" ,
            EMPRESA."DocumentoIdentidadRepresentante" AS "DocumentoIdentidadRepresentante"
            FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa" EMPRESA
            INNER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" PERSONA ON (PERSONA."DocumentoIdentidad" = EMPRESA."DocumentoIdentidadRepresentante")
            WHERE EMPRESA."IdEmpresa"=:p_IdEmpresa;
		
		SELECT COUNT(1) INTO v_countRepresentante FROM :v_DatosRepresentanteLegal;
			IF 	v_countRepresentante <= 0 THEN
				v_DatosRepresentanteLegal = SELECT '' AS "NombreCompletoRepresentante", '' AS "DocumentoIdentidadRepresentante" FROM DUMMY;
			END IF; 

		/*Datos del Corredor*/
		v_datosGenerales = select 
		"RazonSocial" AS "RazonCorredora", 
		"Direccion"   AS "DireccionCorredora", 
		"RUT" AS "RutCorredora", 
		"Telefono" AS "TelefonoCorredora"
		FROM "BSCL.Plataforma.BD.Vistas::CTT.ConsultarContratoDatosGenerales"
		WHERE "IdEmpresa" = p_IdEmpresa;
		
		if p_IdCliente>0 AND p_IdPortafolio>0 THEN ---Circularizacion individual y certificado custodia
			v_cliente=SELECT :p_IdCliente AS "IdCliente", :p_IdPortafolio AS "IdPortafolio"	FROM DUMMY;
			/* Logo*/
			v_logo = select TOP 1
			"Binario" AS "LogoCorredora"
			from "BSCL.Plataforma.BD.Vistas::PLA.ConsultarImagen"
			WHERE "NombreImagen" = 'LOGO_CORREDOR' ORDER BY "Version" DESC;

			SELECT COUNT(1) INTO v_countLogo FROM :v_logo;
			IF 	v_countLogo <= 0 THEN
				v_logo = select '' AS "LogoCorredora" from dummy;
			END IF; 

			/* Firma */
			v_firma =	SELECT TOP 1
						"Binario" AS "FirmaCorredora"
						FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarImagen"
						WHERE "NombreImagen" = 'FIRMA_CORREDORA' ORDER BY "Version" DESC;

			SELECT COUNT(1) INTO v_countFirma FROM :v_firma;
			IF 	v_countFirma <= 0 THEN
				v_firma = select '' AS "FirmaCorredora" from dummy;
			END IF; 
			v_Masivo = SELECT 'N' AS "EsMasivo" FROM DUMMY;
		ELSE --Masivo, omitimos el LOGO
			v_cliente=
			SELECT NOM."IdCliente",NOM."IdPortafolio" FROM "BSCL.Normativa.BD.Vistas::CIR.ConsultarNomina"  AS NOM
			LEFT JOIN "BSCL.Normativa.BD::CER.CertificadoCliente" c  ON (c."IdTipoDocumento"='CIRCCarta' AND  NOM."IdCliente"=c."IdCliente" AND NOM."IdPortafolio"=c."IdPortafolio" AND NOM."Periodo"=c."Periodo")
			WHERE  NOM."Notificar"='SI' AND NOM."Periodo"=REPLACE (LEFT(:p_FechaConsulta,7),'-','') AND c."IdCliente" IS NULL;
			--SOLO CERITIFICADOS NUEVOS
			/* Logo*/
			v_logo = select '' AS "LogoCorredora" from dummy;
			v_firma = select '' AS "FirmaCorredora" from dummy;
			v_Masivo = SELECT 'S' AS "EsMasivo" FROM DUMMY;
		END IF;
		
		/*Datos del Cliente*/
		v_datosClientes = SELECT  CLI."IdPersona",CLI."IdCliente",CLI."NombreCompleto" AS "NombreDestinatario", CLI."DocumentoIdentidad" AS "Rut",CLI."IdEmpleadoEjecutivo", CLI."IdTelefonoPrincipal", CLI."IdTipoPersona",TEL."TelefonoDestinatario" 
								 ,COALESCE(EJE."EjecutivoNombre",'') AS "EjecutivoNombre",COALESCE(EJE."EjecutivoDireccionElectronica",'') AS "EjecutivoDireccionElectronica"
								 ,COALESCE(DIR."Direccion",'') AS  "Direccion",COALESCE(DIR."Comuna",'') AS  "Comuna",COALESCE(DIR."Ciudad",'') AS  "Ciudad"
								 ,PER."Sexo", PER."EstadoCivil"
							 FROM "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" CLI
							 INNER JOIN "BSCL.SocioNegocio.BD::PER.Persona" PER ON (PER."IdPersona" = CLI."IdPersona")
							 INNER JOIN (SELECT DISTINCT "IdCliente" FROM :v_cliente) FIL ON (FIL."IdCliente"=CLI."IdCliente")
							 LEFT JOIN (
							 	SELECT EMP."NombreCompleto" AS "EjecutivoNombre",COALESCE(DIR."Direccion",'') AS "EjecutivoDireccionElectronica",EMP."IdEmpleado" 
								FROM   "BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEmpleado" EMP 
								LEFT OUTER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionElectronica" DIR
								ON (EMP."IdDireccionElectronicaPrincipal"=DIR."IdDireccionElectronica")
							 ) AS EJE ON (EJE."IdEmpleado"=CLI."IdEmpleadoEjecutivo" )
						  
							 LEFT JOIN (
								SELECT 
								RANK() over (partition by  "IdPersona" order by ("Predeterminado" || CAST ("IdTelefono" AS NVARCHAR(14)))  desc) as RANK
								,"Numero" AS "TelefonoDestinatario","IdPersona"  FROM "BSCL.SocioNegocio.BD::PER.Telefono"
								) AS TEL ON (TEL.RANK=1 AND TEL."IdPersona"=CLI."IdPersona" )
								
							 LEFT JOIN (
								SELECT 
								RANK() over (partition by  "IdPersona" order by ("Predeterminado" || CAST ("IdDireccionFisica" AS NVARCHAR(14)))  desc) as RANK
								,CONCAT(CONCAT(COALESCE("Calle",''),' '),COALESCE(CAST("Numero" AS VARCHAR),'')) AS "Direccion",COALESCE("NombreDivisionPais",'') AS "Comuna"
								,COALESCE("NombreCiudad",'') AS "Ciudad" ,"IdPersona"  
								FROM "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionFisica"
								) AS DIR ON (DIR.RANK=1 AND DIR."IdPersona"=CLI."IdPersona" );	
								
									
			
		-----------------------------------
		--PARAMETROS : Segun tipo de salida
		-----------------------------------		
		v_tablaInstrumentos=SELECT NULL AS "IdClasificacionTipoInstrumento" FROM DUMMY;
		IF p_FormaSalida='CARTA' THEN

			v_tablaInstrumentos=SELECT 	(CASE "Valor"
										WHEN 'registroRV' 		THEN 'RV'
										WHEN 'registroRF' 		THEN 'RF'
										WHEN 'registroIIF' 		THEN 'IF'
										WHEN 'registroFFMM' 	THEN 'FM'
										ELSE NULL
									END) AS 	"IdClasificacionTipoInstrumento"
									FROM "BSCL.Normativa.BD::CIR.Parametro" WHERE "Periodo"=''
									AND "Codigo"='MERCADO' AND "IdEstado"='ACT';
		END IF;
		
		
		/*FLUJO  CUSTODIA*/
		IF p_UnificarPortafolios = 'N' THEN
			v_tablaClientes=SELECT "IdCliente" AS "IdCliente", (CASE "IdPortafolio" WHEN 0 THEN NULL ELSE "IdPortafolio" END) AS "IdPortafolio"	FROM :v_cliente;
		ELSE
			v_tablaClientes=SELECT "IdCliente", "IdPortafolio" FROM  :v_cliente;
		END IF;
	
		CALL "BSCL.Normativa.BD.Servicios::CIR.ConsultarCustodia"(
				p_IdEmpresa                   => p_IdEmpresa,
				p_FechaConsulta               => TO_VARCHAR (:p_FechaConsulta, 'YYYY-MM-DD'),
				p_FormaCustodia				  => 'CUSTODIA_TOTAL',
				p_TipoFechaConsulta			  => 'ADQ',
				p_TablaCarteras				  => :v_tablaClientes,
				p_TablaInstrumentos			  => :v_tablaInstrumentos,
				p_out_TablaSalida    		  => V_out_FlujoTotalizadoFinal
		);		
		
	

		-----------------------
		--OPERACIONES A PLAZO
		-----------------------			

		  --se obtienen parametros de operaciones 	
			
			V_tablaOperacionGrupo =
						SELECT 	 MAX( CASE WHEN SOL."Valor"='registroPACTOS' THEN 'S' ELSE NULL END) AS "PAC" --PACTO
								,MAX( CASE WHEN SOL."Valor"='registroSIMULTANEA' THEN 'S' ELSE NULL END) AS "SIM" --SIMULTANEA
								,MAX( CASE WHEN SOL."Valor"='registroVENTACORTA' THEN 'S' ELSE NULL END) AS "VTACOR" --VENTAS CORTAS
								,MAX( CASE WHEN SOL."Valor"='registroFORWARD' THEN 'S' ELSE NULL END) AS "FORWD" --FORWARD
								,MAX( CASE WHEN SOL."Valor"='registroPRESTAMOS' THEN 'S' ELSE NULL END) AS "PRSTM" --PRESTAMOS
						FROM "BSCL.Normativa.BD::CIR.Parametro" AS SOL
						WHERE SOL."Periodo"=''	AND SOL."Codigo"='TIPOOPER' AND SOL."IdEstado"='ACT';
						
				SELECT TOP 1 "PAC","SIM","VTACOR","FORWD","PRSTM"
					   INTO v_PAC,v_SIM ,v_VTACOR,v_FORWD,v_PRSTM FROM :V_tablaOperacionGrupo;
					  
					  
					  
				 call "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema"(p_IdEmpresa => v_IdEmpresa, p_out_FechaHoraSistema => v_fechaHoraSistema);

		SELECT COUNT(1) INTO v_Count FROM :V_out_FlujoTotalizadoFinal;
		
	

		IF v_Count > 0 THEN
		
--------------------
--CUSTODIA
--------------------
		v_tablaSalida=
			SELECT
			--GENERALES
			10 AS "OrdenInterno"
			,CP."CodigoPortafolio" as "CodigoPortafolio"
			,CP."IdPortafolio" as "IdPortafolio"
			,(COALESCE(CLA."Nombre",'') || ' - ' || COALESCE(F."IdMoneda",'')) AS "Grupo"
			--ESPECIFICOS
			,COALESCE(F."IdClasificacionTipoInstrumento",'') AS "IdClasificacionTipoInstrumento"
			,COALESCE(F."Nemotecnico",'') AS "Nemotecnico"
			,COALESCE(F."IdMoneda",'') AS "IdMoneda"
			,SUM(F."CantidadDisponible") AS "Cantidad"
			,CASE WHEN F."IdClasificacionTipoInstrumento" IN ('IF', 'RF') 
				  THEN SUM(F."MontoMercadoCustodia")
				  WHEN F."PrecioMercadoCustodia" = 0 AND INS."IdEstado" = 'INA'
				  THEN SUM(F."CantidadDisponible")*INS."Precio" 
				  ELSE SUM(F."CantidadDisponible")*F."PrecioMercadoCustodia" 
			 END AS "ValorPresente"
			,CASE F."FechaVencimiento" WHEN '0001-01-01' THEN ''
			 ELSE TO_VARCHAR(COALESCE(F."FechaVencimiento",''),'DD/MM/YYYY') END AS "FechaVencimiento"
			,CASE 
				WHEN F."IdMoneda" IN ('EUR', 'NZD', 'GBP', 'PEN', 'COP') THEN
					F."PrecioMercadoCustodia" / (v_DO * COALESCE(PF."Valor", 1))
				WHEN F."IdMoneda" IN ('USD', 'DO') THEN
					F."PrecioMercadoCustodia" / v_DO
				WHEN F."PrecioMercadoCustodia" = 0 AND INS."IdEstado" = 'INA' THEN
					INS."Precio"
				ELSE 
					F."PrecioMercadoCustodia"
			END AS "PrecioCierre"
			,MAX(F."TasaMercadoActual") AS "Tasa"	
			,F."IdCliente"
			,COALESCE(MF."PrecioDecimales", 0) AS "PrecioDecimales"
			,COALESCE(MF."TasaDecimales", 0) AS "TasaDecimales"
			FROM :V_out_FlujoTotalizadoFinal F
			INNER JOIN "BSCL.Plataforma.BD::INS.Instrumento" INS
			ON (INS."IdInstrumento" = F."IdInstrumento")
			INNER JOIN "BSCL.Plataforma.BD::INS.ClasificacionTipoInstrumento" CLA
			ON (CLA."IdClasificacionTipoInstrumento"=F."IdClasificacionTipoInstrumento")
			--INNER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" CP
			INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" CP
			ON (CP."IdPortafolio"=F."IdPortafolio")
			LEFT JOIN "BSCL.Plataforma.BD::PLA.MonedaFormato" MF
			ON MF."IdTipoInstrumento" = F."IdTipoInstrumento"
			AND MF."IdUsoMonedaFormato" = 'GEN'
			AND MF."IdMoneda" = F."IdMoneda"
			AND MF."IdTipoOperacion" = ''
			LEFT JOIN :v_tblParametrosFinancieros PF ON PF."IdParametroFinanciero" = F."IdMoneda"
			WHERE CLA."IdEstado"='ACT' AND F."TipoCartera"='CUSTODIA'
			AND (
			    (v_PAC = 'S' AND F."IdTipoOperacion" NOT IN ('COMPAC', 'VENPAC'))
			    OR (v_PAC <> 'S')
			)
			GROUP BY PF."Valor", F."IdCliente",F."IdClasificacionTipoInstrumento",CLA."Nombre",F."Nemotecnico",F."IdMoneda",F."PrecioMercadoCustodia",F."FechaVencimiento",CP."CodigoPortafolio",CP."IdPortafolio",MF."PrecioDecimales",MF."TasaDecimales",INS."Precio",INS."IdEstado";

-------------------------------------------------
-- CUSTODIA NEGATIVA (Por ahora solo mercado RF)
-------------------------------------------------
		v_tablaSalida= SELECT * FROM :v_tablaSalida UNION ALL
			SELECT
			--GENERALES
			10 AS "OrdenInterno"
			,CP."CodigoPortafolio" as "CodigoPortafolio"
			,CP."IdPortafolio" as "IdPortafolio"
			,(COALESCE(CLA."Nombre",'') || ' - ' || COALESCE(F."IdMoneda",'')) AS "Grupo"
			--ESPECIFICOS
			,COALESCE(F."IdClasificacionTipoInstrumento",'') AS "IdClasificacionTipoInstrumento"
			,COALESCE(F."Nemotecnico",'') AS "Nemotecnico"
			,COALESCE(F."IdMoneda",'') AS "IdMoneda"
			,SUM(F."CantidadDisponible") * -1 AS "Cantidad"
			,SUM(F."MontoMercadoCustodia") *-1 AS "ValorPresente"
			,CASE F."FechaVencimiento" WHEN '0001-01-01' THEN ''
			 ELSE TO_VARCHAR(COALESCE(F."FechaVencimiento",''),'DD/MM/YYYY') END AS "FechaVencimiento"
			,CASE 
				WHEN F."IdMoneda" IN ('EUR', 'NZD', 'GBP', 'PEN', 'COP') THEN
					F."PrecioMercadoCustodia" / (v_DO * COALESCE(PF."Valor", 1))
				WHEN F."IdMoneda" IN ('USD', 'DO') THEN
					F."PrecioMercadoCustodia" / v_DO
				ELSE 
					F."PrecioMercadoCustodia"
			END AS "PrecioCierre"
			,MAX(F."TasaMercadoActual") AS "Tasa"	
			,F."IdCliente"
			,MF."PrecioDecimales"
			,MF."TasaDecimales"
			FROM :V_out_FlujoTotalizadoFinal F
			INNER JOIN "BSCL.Plataforma.BD::INS.ClasificacionTipoInstrumento" CLA
			ON (CLA."IdClasificacionTipoInstrumento"=F."IdClasificacionTipoInstrumento")
			INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" CP
			ON (CP."IdPortafolio"=F."IdPortafolio")
			INNER JOIN "BSCL.Plataforma.BD::PLA.MonedaFormato" MF
			ON MF."IdTipoInstrumento" = F."IdTipoInstrumento"
			AND MF."IdUsoMonedaFormato" = 'GEN'
			AND MF."IdMoneda" = F."IdMoneda"
			AND MF."IdTipoOperacion" = ''
			LEFT JOIN :v_tblParametrosFinancieros PF ON PF."IdParametroFinanciero" = F."IdMoneda"
			WHERE CLA."IdEstado"='ACT' AND F."TipoCartera"='CUSTODIA_NEGATIVA'
			GROUP BY PF."Valor", F."IdCliente",F."IdClasificacionTipoInstrumento",CLA."Nombre",F."Nemotecnico",F."IdMoneda",F."PrecioMercadoCustodia",F."FechaVencimiento",CP."CodigoPortafolio",CP."IdPortafolio",MF."PrecioDecimales",MF."TasaDecimales";


--------------------
--GARANTIA
--------------------
		v_tablaSalida=  SELECT * FROM :v_tablaSalida UNION ALL
			SELECT
			--GENERALES
			11 AS "OrdenInterno"
			,CP."CodigoPortafolio" as "CodigoPortafolio"
			,CP."IdPortafolio" as "IdPortafolio"
			,'GARANTIA' AS "Grupo"
			--ESPECIFICOS
			,COALESCE(F."IdClasificacionTipoInstrumento",'') AS "IdClasificacionTipoInstrumento"
			,COALESCE(F."Nemotecnico",'') AS "Nemotecnico"
			,COALESCE(F."IdMoneda",'') AS "IdMoneda"
			,SUM(F."CantidadDisponible") AS "Cantidad"
			,CASE WHEN F."IdClasificacionTipoInstrumento" IN ('IF', 'RF') 
				  THEN SUM(F."MontoMercadoCustodia")
				  ELSE SUM(F."CantidadDisponible")*F."PrecioMercadoCustodia" 
			 END AS "ValorPresente"
			,CASE F."FechaVencimiento" WHEN '0001-01-01' THEN ''
			 ELSE TO_VARCHAR(COALESCE(F."FechaVencimiento",''),'DD/MM/YYYY') END AS "FechaVencimiento"
			,F."PrecioMercadoCustodia" 		AS "PrecioCierre"
			,MAX(F."TasaMercadoActual") AS "Tasa"	
			,F."IdCliente"
			,MF."PrecioDecimales"
			,MF."TasaDecimales"
			FROM :V_out_FlujoTotalizadoFinal F
			INNER JOIN "BSCL.Plataforma.BD::INS.ClasificacionTipoInstrumento" CLA
			ON (CLA."IdClasificacionTipoInstrumento"=F."IdClasificacionTipoInstrumento")
			--INNER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" CP
			INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" CP
			ON (CP."IdPortafolio"=F."IdPortafolio")
			INNER JOIN "BSCL.Plataforma.BD::PLA.MonedaFormato" MF
			ON MF."IdTipoInstrumento" = F."IdTipoInstrumento"
			AND MF."IdUsoMonedaFormato" = 'GEN'
			AND MF."IdMoneda" = F."IdMoneda"
			AND MF."IdTipoOperacion" = ''
			WHERE CLA."IdEstado"='ACT' AND F."TipoCartera"='GARANTIAS'
			GROUP BY F."IdCliente",F."IdClasificacionTipoInstrumento",CLA."Nombre",F."Nemotecnico",F."IdMoneda",F."PrecioMercadoCustodia",F."FechaVencimiento",CP."CodigoPortafolio",CP."IdPortafolio",MF."PrecioDecimales",MF."TasaDecimales";


-----------------------
--OPERACIONES A PLAZO
-----------------------			
				
		  --Se obtienen operaciones segun Productos configurados para la Normativa
	
		  v_tablaProductos=
		 	SELECT   NPRO."IdTipoProducto"
		 			,NPRO."IdClasificacionTipoInstrumento"
				 	,NPRO."IdTipoInstrumento"
				 	,NPRO."IdTipoOperacion" 
				 	,NPRO."Uso" 
				 	
		 	FROM  "BSCL.Normativa.BD::NOR.Producto" NPRO 	--PRODUCTO NORMATIVA
			WHERE NPRO."IdNormativa"=:v_IdNormativa;
		
			SELECT COUNT(1) INTO v_Count FROM :v_tablaProductos;
			IF v_Count=0 THEN
				v_tablaProductos =SELECT
				'' AS "IdTipoProducto",
				'' AS "IdClasificacionTipoInstrumento",
				'' AS "IdTipoInstrumento", 
				'' AS "IdTipoOperacion",
				'' AS "Uso" 
				FROM DUMMY WHERE 1=2;	
			END IF;

					
			IF v_SIM='S' THEN --SIMULTANEA
			
			SELECT :p_IdPortafolio AS "portafolio" FROM DUMMY;
			
					CALL "BSCL.Normativa.BD.Servicios::CIR.SimultaneaVigentes"(
						p_IdEmpresa => :v_IdEmpresa,
						p_FechaConsulta => :v_FechaConsulta,
						p_cliente => :v_cliente,
						p_fechaHoraSistema => :v_fechaHoraSistema,
						p_out_FlujoTotalizadoFinal  => :V_out_FlujoTotalizadoFinal,
						p_Salida => v_tablaSalidaSumultanea
					
					);
					
			v_tablaSalida =	SELECT * FROM :v_tablaSalidaSumultanea UNION ALL SELECT * FROM :v_tablaSalida;
					
		
			END IF; 
			
			IF v_PAC='S' THEN --PACTO
			
				CALL "BSCL.Normativa.BD.Servicios::CIR.PactoVigentes"(
						p_IdEmpresa => :v_IdEmpresa,
						p_FechaConsulta => :v_FechaConsulta,
						p_cliente => :v_cliente,
						p_fechaHoraSistema => :v_fechaHoraSistema,	
						p_MostrarSubyacentePacto => 'S',					
						p_Salida => v_tablaSalidaPacto
					
					);
					
			v_tablaSalida =	SELECT * FROM :v_tablaSalidaPacto UNION ALL SELECT * FROM :v_tablaSalida;
			
			END IF; 

			
			
			IF v_PRSTM='S' THEN --PRESTAMOS
			
				CALL "BSCL.Normativa.BD.Servicios::CIR.EmprestitoPrestamoVigentes"(
						p_IdEmpresa => :v_IdEmpresa,
						p_FechaConsulta => :v_FechaConsulta,
						p_OrdenInterno => 60,
						p_Grupo => 'EMPRESTITO VENTA CORTA',
						p_IdTipoPrestamo => 'PRTVTC',
						p_IdTipoOperacion => 'EMPRES',
						p_EsVentaCorta => 'S',
						p_cliente => :v_cliente,
						p_fechaHoraSistema => :v_fechaHoraSistema,
						p_Salida => v_tablaSalidaEmprestito
					
					);
					
				v_tablaSalida =	SELECT * FROM :v_tablaSalidaEmprestito UNION ALL SELECT * FROM :v_tablaSalida;


				CALL "BSCL.Normativa.BD.Servicios::CIR.FinesGeneralesVigentes"(
							p_IdEmpresa => :v_IdEmpresa,
							p_FechaConsulta => :v_FechaConsulta,
							p_OrdenInterno => 80,
							p_Grupo => 'PRESTAMO FINES GENERALES',
							p_IdTipoOperacion => 'PRESTA',
							p_IdTipoPrestamo => 'PRTFIN',
							p_cliente => :v_cliente,
							p_fechaHoraSistema => :v_fechaHoraSistema,
							p_Salida => v_tablaSalidaPrestamoFin
						
						);
						
				v_tablaSalida =	SELECT * FROM :v_tablaSalidaPrestamoFin UNION ALL SELECT * FROM :v_tablaSalida;

			
				CALL "BSCL.Normativa.BD.Servicios::CIR.FinesGeneralesVigentes"(
							p_IdEmpresa => :v_IdEmpresa,
							p_FechaConsulta => :v_FechaConsulta,
							p_OrdenInterno => 90,
							p_Grupo => 'EMPRESTITO FINES GENERALES',
							p_IdTipoOperacion => 'EMPRES',
							p_IdTipoPrestamo => 'PRTFIN',
							p_cliente => :v_cliente,
							p_fechaHoraSistema => :v_fechaHoraSistema,
							p_Salida => v_tablaSalidaEmprestitoFin
						
						);
						
				v_tablaSalida =	SELECT * FROM :v_tablaSalidaEmprestitoFin UNION ALL SELECT * FROM :v_tablaSalida;
			
			
				CALL "BSCL.Normativa.BD.Servicios::CIR.FinesGeneralesVigentes"(
							p_IdEmpresa => :v_IdEmpresa,
							p_FechaConsulta => :v_FechaConsulta,
							p_OrdenInterno => 110,
							p_Grupo => 'PRESTAMO ADMINISTRATIVO',
							p_IdTipoOperacion => 'PRESTA',
							p_IdTipoPrestamo => 'PRTADM',
							p_cliente => :v_cliente,
							p_fechaHoraSistema => :v_fechaHoraSistema,
							p_Salida => v_tablaSalidaPrestamoAdministrativo
						
						);
						
				v_tablaSalida =	SELECT * FROM :v_tablaSalidaPrestamoAdministrativo UNION ALL SELECT * FROM :v_tablaSalida;
			
			
				CALL "BSCL.Normativa.BD.Servicios::CIR.FinesGeneralesVigentes"(
							p_IdEmpresa => :v_IdEmpresa,
							p_FechaConsulta => :v_FechaConsulta,
							p_OrdenInterno => 120,
							p_Grupo => 'EMPRESTITO ADMINISTRATIVO',
							p_IdTipoOperacion => 'EMPRES',
							p_IdTipoPrestamo => 'PRTADM',
							p_cliente => :v_cliente,
							p_fechaHoraSistema => :v_fechaHoraSistema,
							p_Salida => v_tablaSalidaPrestamoAdministrativo
						
						);
						
				v_tablaSalida =	SELECT * FROM :v_tablaSalidaPrestamoAdministrativo UNION ALL SELECT * FROM :v_tablaSalida;
			
			END IF;

		END IF;

		IF v_VTACOR='S' THEN --VENTAS CORTAS
		
				CALL "BSCL.Normativa.BD.Servicios::CIR.EmprestitoPrestamoVigentes"(
							p_IdEmpresa => :v_IdEmpresa,
							p_FechaConsulta => :v_FechaConsulta,
							p_OrdenInterno => 70,
							p_Grupo => 'VENTA CORTA',
							p_IdTipoOperacion => 'PRESTA',
							p_EsVentaCorta => 'S',
							p_IdTipoPrestamo => 'PRTVTC',
							p_cliente => :v_cliente,
							p_fechaHoraSistema => :v_fechaHoraSistema,
							p_Salida => v_tablaSalidaPrestamo
						
						);
						
				v_tablaSalida =	SELECT * FROM :v_tablaSalidaPrestamo UNION ALL SELECT * FROM :v_tablaSalida;
		
		END IF;

		IF v_FORWD='S' THEN --FORWARD 

 			CALL "BSCL.Normativa.BD.Servicios::CIR.ForwardsVigentes"(
 					p_IdEmpresa => :v_IdEmpresa,
 					p_FechaConsulta => :v_FechaConsulta,
 					p_cliente => :v_cliente,
					p_fechaHoraSistema => :v_fechaHoraSistema,
					p_Salida => v_tablaSalidaForward
				);

			v_tablaSalida = SELECT * FROM :v_tablaSalidaForward UNION ALL SELECT * FROM :v_tablaSalida;
		END IF;
		
		SELECT COUNT(1) INTO v_Count FROM :v_tablaSalida;

		IF v_Count = 0 THEN
			v_tablaSalida=
				SELECT
				0 AS "OrdenInterno"
				,CP."CodigoPortafolio" AS "CodigoPortafolio"
				,CP."IdPortafolio" as "IdPortafolio"
				,'' AS "Grupo"
				,'' AS "IdClasificacionTipoInstrumento"
				,'' AS "Nemotecnico"
				,'' AS "IdMoneda" 
				,0 AS "Cantidad"
				,0 AS "ValorPresente"
				,'' 	AS "FechaVencimiento"
				,0 		AS "PrecioCierre"
				,0 AS "Tasa"
				,TC."IdCliente" AS "IdCliente"
				,0 AS "PrecioDecimales"
				,0 AS "TasaDecimales"
				FROM :v_tablaClientes TC
				INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" CP			
				ON (CP."IdPortafolio"=TC."IdPortafolio");--DUMMY;-- WHERE 1=2;
		END IF;
		
		--verificamos si se debe agregar cuentacaja al reporte
		SELECT "Valor" INTO v_AgregarCuentaCaja
		FROM "BSCL.Normativa.BD::NOR.Configuracion"
		WHERE "CodigoParametro" = 'AGREGAR_CUENTA_CAJA_CIRCULARIZACION';
		
		IF v_AgregarCuentaCaja = 'S' THEN
		
			v_CuentaInversionAConsultar = SELECT NULL AS "IdEjecutivo",
			"IdPortafolio" as "IdPortafolio", NULL AS "IdMoneda" 
			FROM :v_tablaClientes;
			
			CALL "BSCL.CuentaInversion.BD.Servicios::ConsultarSaldoCuentaInversionConsolidada"
				(p_IdEmpresa => p_IdEmpresa,
				p_IdUsuario => p_IdUsuario, 
				p_IdModulo => 'CTA',
				p_UsaReglasNegocio => 'S',
				p_CuentaInversionAConsultar => :v_CuentaInversionAConsultar,
				p_IdCliente => null,
				p_IdPortafolio => null,
				p_IdProductoAgrupador => null,
				p_FechaDesde => p_FechaConsulta,
				p_FechaHasta => p_FechaConsulta,
				p_IncluyeOtrosSaldos => 'S',
				p_IncluyeProductoApv => 'S',
				p_out_SaldoInicialFinal => v_SaldoInicialFinal,
				p_out_SaldoPorFechaEfectiva => v_SaldoPorFechaEfectiva,
				p_out_SaldoPorFechaMovimiento => v_SaldoPorFechaMovimiento,
				p_out_InformacionContador => v_InformacionContador,
				p_out_InformacionDetalle => v_InformacionDetalle,
				p_out_AdvertenciasContador => v_AdvertenciasContador,
				p_out_AdvertenciasDetalle => v_AdvertenciasDetalle,
				p_out_ErroresContador => v_ErroresContador,
				p_out_ErroresDetalle => v_ErroresDetalle);
			
			v_tablaSalida=  SELECT
				140 AS "OrdenInterno"
				,CP."CodigoPortafolio" as "CodigoPortafolio"
				,CP."IdPortafolio" as "IdPortafolio"
				,'CUENTA CAJA' AS "Grupo"
				--ESPECIFICOS
				,COALESCE(F."NombreMoneda",'') AS "IdClasificacionTipoInstrumento"
				,COALESCE(F."NombreMoneda",'') AS "Nemotecnico"
				,COALESCE(F."IdMoneda",'') AS "IdMoneda"
				,0 AS "Cantidad"
				,SUM(F."SaldoDisponible") AS "ValorPresente"
				,'' AS "FechaVencimiento"
				,0 AS "PrecioCierre"
				,0 AS "Tasa"	
				,F."IdCliente" AS "IdCliente"
				,0 AS "PrecioDecimales"
				,0 AS "TasaDecimales"
				FROM :v_SaldoPorFechaMovimiento F
				---INNER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" CP
				INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" CP
				ON (CP."IdPortafolio"=F."IdPortafolio")
				INNER JOIN :v_tablaClientes TC
				ON (CP."IdPortafolio"=TC."IdPortafolio")
				GROUP BY CP."CodigoPortafolio",F."IdCliente",CP."IdPortafolio",F."NombreMoneda",F."IdMoneda"
				UNION SELECT * FROM :v_tablaSalida;
		END IF;



	--Tabla salida final
	v_tablaSalida_final=
	SELECT 
				sal."OrdenInterno"
				,cli."NombreDestinatario"
				,cli."Rut"
				,sal."CodigoPortafolio"
				,sal."IdPortafolio"
				,cli."Direccion"
				,cli."Comuna"
				,cli."Ciudad"
				,cli."TelefonoDestinatario"
				,cli."IdTipoPersona"
				,p_FormaSalida AS "FormaSalida"
				,TO_VARCHAR(COALESCE(:p_FechaConsulta,''),'DD-MM-YYYY')  AS "FechaConsulta"
				,sal."Grupo"
				--ESPECIFICOS
				,sal."IdClasificacionTipoInstrumento"
				,sal."Nemotecnico"
				,sal."IdMoneda"
				,sal."Cantidad"
				,sal."ValorPresente"
				,sal."FechaVencimiento"
				,sal."PrecioCierre"
				,sal."Tasa"	
				,sal."IdCliente"
				,TO_VARCHAR(v_FechaHoraSistemaLOCAL,'DD-MM-YYYY')	AS "FechaSistemaLOCAL"
				,TO_VARCHAR(v_FechaHoraSistemaLOCAL,'HH24:MI')		AS "HoraSistemaLOCAL"
				,:p_CartaConfirmacionCliente AS "CartaConfirmacionCliente"
				,sal."PrecioDecimales"
				,sal."TasaDecimales"
				,cli."EjecutivoNombre"
		 		,cli."EjecutivoDireccionElectronica"
		 		,cli."Sexo"
		 		,cli."EstadoCivil"
	FROM :v_tablaSalida sal 
	INNER JOIN :v_datosClientes cli ON (cli."IdCliente"=sal."IdCliente");
	
	p_Salida=
	SELECT *
	/*
		"OrdenInterno",
		"NombreDestinatario",
	"Rut",
	"CodigoPortafolio",
	"IdPortafolio",
	"Direccion"
	--"IdCliente"
	*/
	FROM :v_tablaSalida_final
	CROSS JOIN :v_datosGenerales
	CROSS JOIN :v_logo 
	CROSS JOIN :v_firma
	CROSS JOIN :v_Masivo
	CROSS JOIN :V_DatosRepresentanteLegal
	ORDER BY "IdCliente","IdPortafolio","OrdenInterno","Grupo";
	
SELECT * FROM :p_Salida;

END;