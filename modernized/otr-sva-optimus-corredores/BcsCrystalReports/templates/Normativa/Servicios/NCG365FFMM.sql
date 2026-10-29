SET SCHEMA "BSCL";
GO 
DROP PROCEDURE "BSCL.Normativa.CrystalReports.Servicios::NCG365FFMM";
GO 
CREATE PROCEDURE 
--  Tecnologia : CrystalReports
--  Proposito: Servicio para consultar transacciones de Fondos Mutuos.
--  Autor: CFLORES - BCS
--  Fecha Creacion: 03/05/2018

"BSCL.Normativa.CrystalReports.Servicios::NCG365FFMM" ( 

	--VARIABLES DE ENTRADA OBLIGATORIAS
    IN p_IdEmpresa				BIGINT,
    IN p_IdCliente				BIGINT,
    IN p_FormaSalida			NVARCHAR(14) DEFAULT 'SERIES', --'REMUNERACION'
    IN p_FechaConsulta			NVARCHAR(10)			 --'YYYY-MM-DD'
    

    --VARIABLES DE SALIDA OBLIGATORIAS
	--OUT p_out_TablaPEP	"BSCL.Normativa.BD::NOR.SALIDA_UAFCIR63PEP"

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
		DECLARE v_Direccion									NVARCHAR(80):='';
		DECLARE v_Comuna									NVARCHAR(60):='';
		DECLARE v_Ciudad									NVARCHAR(60):='';		
		DECLARE v_Rut										NVARCHAR(12):='';	
		DECLARE v_CodigoPortafolio							NVARCHAR(100):='';	
		DECLARE v_logo TABLE("LogoCorredora" BLOB);
		DECLARE v_firma TABLE("FirmaCorredora" BLOB);
		DECLARE v_logoInvestments TABLE ("LogoInvestments" BLOB);
				
		DECLARE v_FechaSistemaUTC   	NVARCHAR(40);
		DECLARE v_FechaSistemaLOCAL   	NVARCHAR(40);
		DECLARE v_FechaHoraSistemaUTC   NVARCHAR(40);
		DECLARE v_FechaHoraSistemaLOCAL SECONDDATE;
		DECLARE v_FechaHoraUTC   		NVARCHAR(40);
		DECLARE v_FechaHoraLOCAL   		NVARCHAR(40);
		
		DECLARE v_paisSistema  BIGINT:=0;
		
		/*
		p_IdEmpresa			= 1;
		p_IdCliente			= 249194;
		p_IdPortafolio=1450263;
		p_FechaConsulta=TO_VARCHAR ('2018-05-02', 'YYYY-MM-DD');
		*/
		CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechas"(
		p_IdEmpresa					=> p_IdEmpresa,
		p_out_FechaSistemaUTC		=> v_FechaSistemaUTC,
		p_out_FechaSistemaLOCAL		=> v_FechaSistemaLOCAL,
		p_out_FechaHoraSistemaUTC	=> v_FechaHoraSistemaUTC,
		p_out_FechaHoraSistemaLOCAL	=> v_FechaHoraSistemaLOCAL,
		p_out_FechaHoraUTC			=> v_FechaHoraUTC,
		p_out_FechaHoraLOCAL		=> v_FechaHoraLOCAL
		);
		v_logo = select TOP 1
		"Binario" AS "LogoCorredora"
		from "BSCL.Plataforma.BD.Vistas::PLA.ConsultarImagen"
		WHERE "NombreImagen" = 'LOGO_CORREDOR' ORDER BY "Version" DESC;

		v_firma = select TOP 1
        	"Binario" AS "FirmaCorredora"
		from "BSCL.Plataforma.BD.Vistas::PLA.ConsultarImagen"
        	WHERE "NombreImagen" = 'FIRMA_CORREDORA' ORDER BY "Version" DESC;
		
    		v_logoInvestments = select TOP 1
        	"Binario" AS "LogoInvestments"
		from "BSCL.Plataforma.BD.Vistas::PLA.ConsultarImagen"
        	WHERE "NombreImagen" = 'NCG365_LOGO_INVESTMENTS' ORDER BY "Version" DESC;
        	
		v_meses = 				SELECT  TO_BIGINT(1) "NUMERO",'Enero' AS "MES" FROM DUMMY
					  UNION ALL SELECT  TO_BIGINT(2) "NUMERO",'Febrero' AS "MES" FROM DUMMY
					  UNION ALL SELECT  TO_BIGINT(3) "NUMERO",'Marzo' AS "MES" FROM DUMMY
					  UNION ALL SELECT  TO_BIGINT(4) "NUMERO",'Abril' AS "MES" FROM DUMMY
					  UNION ALL SELECT  TO_BIGINT(5) "NUMERO",'Mayo' AS "MES" FROM DUMMY
					  UNION ALL SELECT  TO_BIGINT(6) "NUMERO",'Junio' AS "MES" FROM DUMMY
					  UNION ALL SELECT  TO_BIGINT(7) "NUMERO",'Julio' AS "MES" FROM DUMMY
					  UNION ALL SELECT  TO_BIGINT(8) "NUMERO",'Agosto' AS "MES" FROM DUMMY
					  UNION ALL SELECT  TO_BIGINT(9) "NUMERO",'Septiembre' AS "MES" FROM DUMMY
					  UNION ALL SELECT  TO_BIGINT(10) "NUMERO",'Octubre' AS "MES" FROM DUMMY
					  UNION ALL SELECT  TO_BIGINT(11) "NUMERO",'Noviembre' AS "MES" FROM DUMMY
					  UNION ALL SELECT  TO_BIGINT(12) "NUMERO",'Diciembre' AS "MES" FROM DUMMY;		
		/*Datos del Cliente*/
		SELECT TOP 1 "IdPersona","NombreCompleto", "DocumentoIdentidad","IdEmpleadoEjecutivo" INTO v_idPersona,v_NombreDestinatario,v_Rut,v_IdEmpleadoEjecutivo FROM "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente"
		WHERE "IdCliente"=p_IdCliente;

		v_datosGenerales = select 
		"RazonSocial" AS "RazonCorredora", 
		"Direccion"   AS "DireccionCorredora", 
		"BSCL.Plataforma.BD.Utilitarios::DarFormatoRut"("RUT") AS "RutCorredora", 
		"Telefono" AS "TelefonoCorredora",
		 v_EjecutivoNombre AS "EjecutivoNombre",
		 v_EjecutivoDireccionElectronica AS "EjecutivoDireccionElectronica"
		FROM "BSCL.Plataforma.BD.Vistas::CTT.ConsultarContratoDatosGenerales"
		WHERE "IdEmpresa" = p_IdEmpresa;

		v_mopvimientoFFMM =
		SELECT
			  SOLICITUD."IdSolicitudAporteRescate"
			, SOLICITUD."IdCliente"
			, SOLICITUD."IdPortafolio"
			, SOLICITUD."TipoSolicitud"
			, SOLICITUD."IdSerieFondo"
			, SOLICITUD."IdTipoMovimientoFondoMutuo"
			, SOLICITUD."FechaCertificacion"
			, SOLICITUD."FechaLiquidacion"
			, CASE WHEN SOLICITUD."EsFondoNacional" = 'S' AND SOLICITUD."IdMonedaFondo" = 'USD' THEN SOLICITUD."Monto" ELSE SOLICITUD."MontoMonedaSistema" END AS "Monto"
			, SOLICITUD."NumeroCuotas"
			, SOLICITUD."ValorCuota"
			, SOLICITUD."CostoUF"
			, SOLICITUD."IdMoneda"
			, SOLICITUD."IdEmpresa"
			, SOLICITUD."EstadoSolicitud"
			, SOLICITUD."IdInstrumento"
			, SOLICITUD."NombreSerieFondo"
			, SOLICITUD."CodigoSerie"
			, SOLICITUD."IdFondo"
			, SOLICITUD."IdEmisor"
			, SOLICITUD."Nemotecnico"
			, SOLICITUD."NombreFondo"
			, SOLICITUD."DescripcionTipoSolicitud"
			, COALESCE(TIPOMOV."MovimientoNCG365", SOLICITUD."DescripcionTipoMovimiento") AS "DescripcionTipoMovimiento"
			, SOLICITUD."FechaCertificacionRegistro"
			, SOLICITUD."IdRegimenTributario"
			, COALESCE(REG."HomologacionNCG365", SOLICITUD."IdRegimenTributario")  AS "DescripcionRegimenTributario"
			, SOLICITUD."MontoMonedaSistema"
			, "BSCL.Plataforma.BD.Utilitarios::DarFormatoRut"(SOLICITUD."DocumentoIdentidadAGF") AS "DocumentoIdentidadAGF"
			, SOLICITUD."FechaAceptacion"
		    	, SOLICITUD."EsFondoNacional"
			, SOLICITUD."IdMonedaSistema"
			, CASE WHEN SOLICITUD."EsFondoNacional" = 'S' AND SOLICITUD."IdMonedaFondo" = 'USD' THEN 'S' ELSE 'N' END AS "EsFondoNacionalDolar"
			, SOLICITUD."MontoComision"
		FROM "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarSolicitudAporteRescate" AS SOLICITUD
		LEFT JOIN "BSCL.FondoMutuo.BD::FMM.TipoMovimientoFondoMutuoHomologacionNCG365" TIPOMOV ON SOLICITUD."IdTipoMovimientoFondoMutuo" = TIPOMOV."IdTipoMovimientoFondoMutuo"
		LEFT JOIN "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarRegimen" REG ON SOLICITUD."IdRegimenTributario" = REG."IdRegimenFondoMutuo"
		WHERE SOLICITUD."IdCliente"=:p_IdCliente AND SOLICITUD."IdTipoMovimientoFondoMutuo" IN ('RESTRASP', 'APONORM', 'APOREINV', 'RESNORM', 'RESREINV', 'APOTRASP', 'APOCES', 'RESCCES')
		--AND SOLICITUD."IdPortafolio"=:p_IdPortafolio 
		AND SOLICITUD."IdEmpresa"= :p_IdEmpresa 
		AND TO_DATE(SOLICITUD."FechaCertificacion") =TO_DATE(:p_FechaConsulta)
		AND "EstadoSolicitud"='CRT';

		
	
	
		SELECT COUNT(1) INTO v_Count FROM :v_mopvimientoFFMM;

		IF v_Count > 0 THEN
		
--------------------
--MOVIMIENTOS FFMM
--------------------
					IF :p_FormaSalida='SERIES' THEN
						v_tablaSalida=
							SELECT
							--GENERALES
							 v_NombreDestinatario AS "NombreDestinatario"
							,"BSCL.Plataforma.BD.Utilitarios::DarFormatoRut"(v_Rut) AS "Rut"
							, CP."CodigoPortafolio" as "CodigoPortafolio"
							, SOLICITUD."IdSolicitudAporteRescate"
							, SOLICITUD."IdCliente"
							, SOLICITUD."IdPortafolio"
							, SOLICITUD."TipoSolicitud"
							, SOLICITUD."IdSerieFondo"
							, SOLICITUD."IdTipoMovimientoFondoMutuo"
							, SOLICITUD."FechaLiquidacion"
							, SOLICITUD."Monto"
							, SOLICITUD."NumeroCuotas"
							, SOLICITUD."ValorCuota"
							, SOLICITUD."CostoUF"
							, SOLICITUD."IdMoneda"
							, SOLICITUD."IdEmpresa"
							, SOLICITUD."EstadoSolicitud"
							, SOLICITUD."IdInstrumento"
							, SOLICITUD."NombreSerieFondo"
							, SOLICITUD."CodigoSerie"
							, SOLICITUD."IdFondo"
							, SOLICITUD."IdEmisor"
							, SOLICITUD."Nemotecnico"
							, SOLICITUD."NombreFondo"
							, SOLICITUD."DescripcionTipoSolicitud"
							, SOLICITUD."DescripcionTipoMovimiento"
							, SOLICITUD."FechaCertificacion"
							,CASE SOLICITUD."FechaCertificacion" WHEN '0001-01-01 00:00:00' THEN ''
							 ELSE TO_VARCHAR(COALESCE(SOLICITUD."FechaCertificacion",''),'DD/MM/YYYY') END 									AS "FechaCertificacionRegistroStr"
							,LPAD(TO_VARCHAR(DAYOFMONTH(TO_DATE(SOLICITUD."FechaCertificacion"))),2,'00')									AS "DiaCertificacionRegistro"
							,(SELECT "MES" FROM :v_meses WHERE "NUMERO" = TO_BIGINT(month(TO_DATE(SOLICITUD."FechaCertificacion"))))	 	AS "MesCertificacionRegistro"
							,TO_VARCHAR(YEAR(TO_DATE(SOLICITUD."FechaCertificacion"))) 														AS "AnnoCertificacionRegistro"
							,TO_VARCHAR(v_FechaHoraSistemaLOCAL,'DD-MM-YYYY')					AS "FechaSistemaLOCAL"
						    	,TO_VARCHAR(v_FechaHoraSistemaLOCAL,'HH24:MI')		AS "HoraSistemaLOCAL"
							,COALESCE(EMI."DocumentoIdentidad",'') AS "RutEmisor"
							,SOLICITUD."FechaCertificacionRegistro"
							,CASE WHEN SOLICITUD."EsFondoNacional"= 'S' THEN '13:00' ELSE '16:00' END AS "HoraCertificacionRegistro"
							,COALESCE(MON."Nombre",'') AS "NombreMoneda"
							,COALESCE(MON."Simbolo",'') AS "SimboloMoneda"
							, SOLICITUD."IdRegimenTributario"                              
                            				, SOLICITUD."DescripcionRegimenTributario"   
							, SOLICITUD."MontoMonedaSistema"
			                , CASE WHEN SOLICITUD."EsFondoNacional"= 'S'  THEN  SOLICITUD."DocumentoIdentidadAGF" ELSE '-' END "DocumentoIdentidadAGF"
			                , TO_VARCHAR(SOLICITUD."FechaAceptacion",'DD/MM/YY') AS "FechaAceptacion"              
							FROM :v_mopvimientoFFMM SOLICITUD
							INNER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" CP
							ON (CP."IdPortafolio"=SOLICITUD."IdPortafolio")
							INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" CLI
							ON (CLI."IdCliente"=SOLICITUD."IdCliente")
							---emisor
							LEFT JOIN "BSCL.Plataforma.BD.Vistas::MER.ConsultarEmisor" EMI
							ON (EMI."IdEmisor"=SOLICITUD."IdEmisor")
							--moneda
							LEFT JOIN"BSCL.Plataforma.BD.Vistas::PLA.ConsultarMoneda" MON
							ON (MON."IdMoneda"= CASE WHEN SOLICITUD."EsFondoNacionalDolar" = 'S'THEN 'USD' ELSE SOLICITUD."IdMonedaSistema" END)
							WHERE EMI."IndicadorAGF"='S'
							
							UNION ALL
							
							SELECT
								--GENERALES
								 v_NombreDestinatario AS "NombreDestinatario"
								,"BSCL.Plataforma.BD.Utilitarios::DarFormatoRut"(v_Rut) AS "Rut"
								, CP."CodigoPortafolio" as "CodigoPortafolio"
								, SOLICITUD."IdSolicitudAporteRescate"
								, SOLICITUD."IdCliente"
								, SOLICITUD."IdPortafolio"
								, SOLICITUD."TipoSolicitud"
								, SOLICITUD."IdSerieFondo"
								, SOLICITUD."IdTipoMovimientoFondoMutuo"
								, SOLICITUD."FechaLiquidacion"
								, SOLICITUD."MontoComision" AS "Monto"
								,  0 AS "NumeroCuotas"
								, SOLICITUD."ValorCuota"
								, SOLICITUD."CostoUF"
								, SOLICITUD."IdMoneda"
								, SOLICITUD."IdEmpresa"
								, SOLICITUD."EstadoSolicitud"
								, SOLICITUD."IdInstrumento"
								, SOLICITUD."NombreSerieFondo"
								, SOLICITUD."CodigoSerie"
								, SOLICITUD."IdFondo"
								, SOLICITUD."IdEmisor"
								, SOLICITUD."Nemotecnico"
								, SOLICITUD."NombreFondo"
								, SOLICITUD."DescripcionTipoSolicitud"
								, 'COMISION' AS "DescripcionTipoMovimiento"
								, SOLICITUD."FechaCertificacion"
								,CASE SOLICITUD."FechaCertificacion" WHEN '0001-01-01 00:00:00' THEN ''
								 ELSE TO_VARCHAR(COALESCE(SOLICITUD."FechaCertificacion",''),'DD/MM/YYYY') END 									AS "FechaCertificacionRegistroStr"
								,LPAD(TO_VARCHAR(DAYOFMONTH(TO_DATE(SOLICITUD."FechaCertificacion"))),2,'00')									AS "DiaCertificacionRegistro"
								,(SELECT "MES" FROM :v_meses WHERE "NUMERO" = TO_BIGINT(month(TO_DATE(SOLICITUD."FechaCertificacion"))))	 	AS "MesCertificacionRegistro"
								,TO_VARCHAR(YEAR(TO_DATE(SOLICITUD."FechaCertificacion"))) 														AS "AnnoCertificacionRegistro"
								,TO_VARCHAR(v_FechaHoraSistemaLOCAL,'DD-MM-YYYY')					AS "FechaSistemaLOCAL"
							    ,TO_VARCHAR(v_FechaHoraSistemaLOCAL,'HH24:MI')		AS "HoraSistemaLOCAL"
								,COALESCE(EMI."DocumentoIdentidad",'') AS "RutEmisor"
								,SOLICITUD."FechaCertificacionRegistro"
								,CASE WHEN SOLICITUD."EsFondoNacional"= 'S' THEN '13:00' ELSE '16:00' END AS "HoraCertificacionRegistro"
								,COALESCE(MON."Nombre",'') AS "NombreMoneda"
								,COALESCE(MON."Simbolo",'') AS "SimboloMoneda"
								, SOLICITUD."IdRegimenTributario"                              
	                            , SOLICITUD."DescripcionRegimenTributario"   
								, SOLICITUD."MontoMonedaSistema"
				                , CASE WHEN SOLICITUD."EsFondoNacional"= 'S'  THEN  SOLICITUD."DocumentoIdentidadAGF" ELSE '-' END "DocumentoIdentidadAGF"
				                , TO_VARCHAR(SOLICITUD."FechaAceptacion",'DD/MM/YY') AS "FechaAceptacion"              
							FROM :v_mopvimientoFFMM SOLICITUD
							INNER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" CP
							ON (CP."IdPortafolio"=SOLICITUD."IdPortafolio")
							INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" CLI
							ON (CLI."IdCliente"=SOLICITUD."IdCliente")
							---emisor
							LEFT JOIN "BSCL.Plataforma.BD.Vistas::MER.ConsultarEmisor" EMI
							ON (EMI."IdEmisor"=SOLICITUD."IdEmisor")
							--moneda
							LEFT JOIN"BSCL.Plataforma.BD.Vistas::PLA.ConsultarMoneda" MON
							ON (MON."IdMoneda"= CASE WHEN SOLICITUD."EsFondoNacionalDolar" = 'S'THEN 'USD' ELSE SOLICITUD."IdMonedaSistema" END)
							WHERE EMI."IndicadorAGF"='S' AND SOLICITUD."MontoComision" > 0
							ORDER BY "IdSolicitudAporteRescate","DescripcionTipoMovimiento";
						ELSE --REMUNERACION
							IF :p_FormaSalida='REMUNERACION' THEN
								v_tablaComisionMax = 
									SELECT 
										MAX(COM."IdComision") AS "IdComision"
										, SOLICITUD."IdSerieFondo"
										, COM."Estado"
										, COM."TipoComision"
									FROM :v_mopvimientoFFMM SOLICITUD
									LEFT JOIN "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" SERIES ON (SOLICITUD."IdSerieFondo"=SERIES."IdSerieFondo")
									INNER JOIN "BSCL.FondoMutuo.BD::FMM.Comision" AS COM ON SERIES."IdSerieFondo" = COM."IdSerieFondo" AND COM."Estado" = 'ACT' AND COM."TipoComision"= 'SALIDA'
									GROUP BY SOLICITUD."IdSerieFondo",COM."Estado", COM."TipoComision";
								v_tablaSalida2=
									SELECT
									--GENERALES
									  SOLICITUD."IdSerieFondo"
									, SOLICITUD."NombreFondo"
									, SOLICITUD."CodigoSerie"
									, CASE WHEN (TC."DiasDesde" = 0 AND TC."DiasHasta" = 0) THEN '-' ELSE COALESCE(TC."DiasDesde" || '-' || TC."DiasHasta", '-') END AS "DiasComision"
									, CASE WHEN COALESCE(TC."PorcentajeComision",0) = 0 THEN '-' ELSE REPLACE(CAST(ROUND(CAST(TC."PorcentajeComision" AS NVARCHAR),3) AS	NVARCHAR),'.',',') END AS "PorcentajeComision"
									, CASE WHEN "PorcentajeRemuneracion"  = 0 THEN '-' ELSE REPLACE(CAST(ROUND(CAST("PorcentajeRemuneracion" AS NVARCHAR),3) AS	NVARCHAR),'.',',') END AS "PorcentajeRemuneracion"
								  	, CASE WHEN "PorcentajeGastoOpera"  = 0 THEN '-' ELSE REPLACE(CAST(ROUND(CAST("PorcentajeGastoOpera" AS NVARCHAR),3) AS	NVARCHAR),'.',',') END AS "PorcentajeGastoOpera"						  	
								  	, CASE WHEN "PorcentajeCostoAdm"  = 0 THEN '-' ELSE REPLACE(CAST(ROUND(CAST("PorcentajeCostoAdm" AS NVARCHAR),3) AS	NVARCHAR),'.',',') END AS "PorcentajeCostoAdm"
									FROM :v_mopvimientoFFMM SOLICITUD
									INNER JOIN "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" SERIES ON (SOLICITUD."IdSerieFondo"=SERIES."IdSerieFondo")
									LEFT JOIN :v_tablaComisionMax AS COM ON SERIES."IdSerieFondo" = COM."IdSerieFondo"
									LEFT JOIN "BSCL.FondoMutuo.BD::FMM.TramoComision" AS TC ON (TC."IdComision" = COM."IdComision" AND TC."Estado" = 'ACT')
									GROUP BY  SOLICITUD."IdSerieFondo"
									, SOLICITUD."NombreFondo"
									, SOLICITUD."CodigoSerie"
									, SERIES."PorcentajeComision"
								  	, SERIES."PorcentajeRemuneracion"
								  	, SERIES."PorcentajeGastoOpera" 
								  	, SERIES."PorcentajeCostoAdm"
								  	, TC."DiasDesde"
								  	, TC."DiasHasta"
								  	, TC."PorcentajeComision"
									ORDER BY TC."DiasDesde";
		
								--END IF;	
							ELSE 
								v_tablaSalida3=
									SELECT
										  SOLICITUD."IdSerieFondo"
										, SOLICITUD."NombreFondo"
										, SOLICITUD."CodigoSerie"
										, SOLICITUD."IdEmisor"
										, CASE WHEN SOLICITUD."EsFondoNacional"= 'S'  THEN  SOLICITUD."DocumentoIdentidadAGF" ELSE '-' END "DocumentoIdentidadAGF"
										, SOLICITUD."FechaAceptacion"
										, CASE WHEN SOLICITUD."EsFondoNacional"= 'S' THEN '13:00' ELSE '16:00' END AS "HoraCertificacionRegistro"
										, SOLICITUD."Monto"
										, COALESCE(MON."Nombre",'') AS "NombreMoneda"
										, SOLICITUD."NumeroCuotas"
										, SOLICITUD."DescripcionTipoMovimiento"
										, SOLICITUD."DescripcionRegimenTributario"
										, SOLICITUD."IdSolicitudAporteRescate"
									FROM :v_mopvimientoFFMM SOLICITUD
									LEFT JOIN"BSCL.Plataforma.BD.Vistas::PLA.ConsultarMoneda" MON
									ON (MON."IdMoneda"= CASE WHEN SOLICITUD."EsFondoNacionalDolar" = 'S'THEN 'USD' ELSE SOLICITUD."IdMonedaSistema" END);
							END IF;
						END IF;	
			
			
		ELSE
		
					IF :p_FormaSalida='SERIES' THEN
								v_tablaSalida=
									SELECT
									 v_NombreDestinatario AS "NombreDestinatario"
									,v_Rut AS "Rut"
									, '' as "CodigoPortafolio"
									, 0  AS "IdSolicitudAporteRescate"
									, 0  AS "IdCliente"
									, 0  AS "IdPortafolio"
									, '' as "TipoSolicitud"
									, '' as "IdSerieFondo"
									, '' as "IdTipoMovimientoFondoMutuo"
									, '' as "FechaLiquidacion"
									, 0  AS "Monto"
									, 0  AS "NumeroCuotas"
									, 0  AS "ValorCuota"
									, 0  AS "CostoUF"
									, '' as "IdMoneda"
									, 0  AS "IdEmpresa"
									, '' as "EstadoSolicitud"
									, 0  AS "IdInstrumento"
									, '' as "NombreSerieFondo"
									, '' as "CodigoSerie"
									, '' as "IdFondo"
									, '' as "IdEmisor"
									, '' as "Nemotecnico"
									, '' as "NombreFondo"
									, '' as "DescripcionTipoSolicitud"
									, '' as "DescripcionTipoMovimiento"
									, '' as "FechaCertificacion"
									, '' AS "FechaCertificacionRegistroStr"
									, ''  AS "DiaCertificacionRegistro"
									, ''  AS "MesCertificacionRegistro"
									, ''  AS "AnnoCertificacionRegistro"
									,TO_VARCHAR(v_FechaHoraSistemaLOCAL,'DD-MM-YYYY')	AS "FechaSistemaLOCAL"
									,TO_VARCHAR(v_FechaHoraSistemaLOCAL,'HH24:MI')		AS "HoraSistemaLOCAL"
									, ''  AS "RutEmisor"
									, ''  AS "FechaCertificacionRegistro"
									, ''  AS "HoraCertificacionRegistro"
									, ''  AS "NombreMoneda"
									, ''  AS "SimboloMoneda"
									, ''  AS "IdRegimenTributario"
									, ''  AS "DescripcionRegimenTributario"
									, 0   AS "MontoMonedaSistema"
			                        , ''  AS "DocumentoIdentidadAGF"
			                        , ''  AS "FechaAceptacion"
									FROM DUMMY;	
						ELSE --REMUNERACION
							IF :p_FormaSalida='REMUNERACION' THEN
								v_tablaSalida2=
									SELECT
									 ''  AS "IdSerieFondo"
									,''  AS "NombreFondo"
									,''  AS "CodigoSerie"
									, 0  AS "DiasComision"
								  	, 0  AS "PorcentajeComision"
								  	, 0  AS "PorcentajeRemuneracion"
								  	, 0  AS "PorcentajeGastoOpera" 
								  	, 0  AS "PorcentajeCostoAdm"
								  	FROM DUMMY;
							ELSE
								v_tablaSalida3=
									SELECT
										  '' AS "IdSerieFondo"
										, '' AS "NombreFondo"
										, '' AS "CodigoSerie"
										, '' AS "IdEmisor"
										, '' AS "DocumentoIdentidadAGF"
										, '' AS "FechaAceptacion"
										, '' AS "HoraCertificacionRegistro"
										, 0  AS "Monto"
										, '' AS "NombreMoneda"
										, 0  AS "NumeroCuotas"
										, '' AS "DescripcionTipoMovimiento"
										, '' AS "DescripcionRegimenTributario"
										, 0 AS "IdSolicitudAporteRescate"
									FROM :v_mopvimientoFFMM SOLICITUD;
								END IF;
						END IF;										
		
		END IF; --IF v_Count > 0 THEN
IF v_Count > 0 THEN
	IF :p_FormaSalida='SERIES' THEN	
		SELECT * FROM :v_tablaSalida
		CROSS JOIN :v_datosGenerales
		CROSS JOIN :v_firma
		CROSS JOIN :v_logo 
		CROSS JOIN :v_logoInvestments ORDER BY "IdSolicitudAporteRescate";	
	ELSE
		IF :p_FormaSalida='REMUNERACION' THEN
			SELECT * FROM :v_tablaSalida2
			CROSS JOIN :v_datosGenerales
			CROSS JOIN :v_firma
			CROSS JOIN :v_logo  
			CROSS JOIN :v_logoInvestments ORDER BY "IdSerieFondo";	
		ELSE
			SELECT * FROM :v_tablaSalida3
			CROSS JOIN :v_datosGenerales
			CROSS JOIN :v_firma
			CROSS JOIN :v_logo 
			CROSS JOIN :v_logoInvestments ORDER BY "IdSolicitudAporteRescate";
		END IF;
	END IF;
END IF;		
END;