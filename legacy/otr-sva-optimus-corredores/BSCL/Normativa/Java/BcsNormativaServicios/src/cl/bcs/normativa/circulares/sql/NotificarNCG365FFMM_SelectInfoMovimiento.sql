/**************************************************************
Proposito	: Script consulta de Info de movimientos
autor		: CFlores
fecha		: 14/05/2018
***********************************************************/

	SELECT
			  SOLICITUD."IdSolicitudAporteRescate"
			, SOLICITUD."IdCliente"
			, SOLICITUD."IdPersona"
			, SOLICITUD."IdPortafolio"
			, SOLICITUD."TipoSolicitud"
			, SOLICITUD."IdSerieFondo"
			, SOLICITUD."IdTipoMovimientoFondoMutuo"
			, SOLICITUD."FechaCertificacion"
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
			, SOLICITUD."FechaCertificacionRegistro"
			, SOLICITUD."IdRegimenTributario"
			, SOLICITUD."DescripcionRegimenTributario"
			, CLI."NombreCompleto" AS "NombreDestinatario"
			, CLI."DocumentoIdentidad"
			, COALESCE(EMI."DocumentoIdentidad",'') AS "RutEmisor"
			,COALESCE(MON."Nombre",'') AS "NombreMoneda"
			,COALESCE(MON."Simbolo",'') AS "SimboloMoneda"
			,CASE SOLICITUD."FechaCertificacionRegistro" WHEN '0001-01-01 00:00:00' THEN ''
			 ELSE TO_VARCHAR(COALESCE(SOLICITUD."FechaCertificacionRegistro",''),'DD/MM/YY') END	AS "FechaCertificacionRegistroStr"
			,LPAD(HOUR(SOLICITUD."FechaCertificacionRegistro"),2,'00') || ':'|| LPAD(MINUTE(SOLICITUD."FechaCertificacionRegistro"),2,'00') AS "HoraCertificacionRegistro"
			,LPAD(TO_VARCHAR(DAYOFMONTH(TO_DATE(SOLICITUD."FechaCertificacionRegistro"))),2,'00')									AS "DiaCertificacionRegistro"
			,(SELECT "MES" FROM (
									SELECT  TO_BIGINT(1) "NUMERO",'Enero' AS "MES" FROM DUMMY
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
									  UNION ALL SELECT  TO_BIGINT(12) "NUMERO",'Diciembre' AS "MES" FROM DUMMY
			
									)WHERE "NUMERO" = TO_BIGINT(month(TO_DATE(SOLICITUD."FechaCertificacionRegistro"))))	 	AS "MesCertificacionRegistro"
			,TO_VARCHAR(YEAR(TO_DATE(SOLICITUD."FechaCertificacionRegistro"))) 														AS "AñoCertificacionRegistro"
		FROM "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarSolicitudAporteRescate" AS SOLICITUD
		INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS CLI ON (CLI."IdCliente"=SOLICITUD."IdCliente")
		---emisor
			LEFT JOIN "BSCL.Plataforma.BD.Vistas::MER.ConsultarEmisor" EMI
			ON (EMI."IdEmisor"=SOLICITUD."IdEmisor")
			--moneda
			LEFT JOIN"BSCL.Plataforma.BD.Vistas::PLA.ConsultarMoneda" MON
			ON (MON."IdMoneda"=SOLICITUD."IdMoneda")
		WHERE  SOLICITUD."IdEmpresa"= :IdEmpresa
			
			AND TO_DATE(SOLICITUD."FechaCertificacion") =TO_DATE(:FechaCertificacion)
			AND "EstadoSolicitud"='CRT' AND  EMI."IndicadorAGF"='S'
			ORDER BY SOLICITUD."IdSolicitudAporteRescate", SOLICITUD."IdCliente";
			
			
			
			