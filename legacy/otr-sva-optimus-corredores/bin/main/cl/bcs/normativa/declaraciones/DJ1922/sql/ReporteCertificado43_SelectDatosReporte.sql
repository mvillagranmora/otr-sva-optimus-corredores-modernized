   SELECT
			  c."Folio"
			 ,c."IdEmpresa"
			 ,c."Esquema"
			 ,c."Version"
			 ,c."Anio"
			 ,c."Periodo"
			 ,c."IdCliente"
			
			 ,0 as "IdPortafolio"
			 ,COALESCE(em."NombreCompleto", '') AS "NombreCompletoEmisor"
			 ,COALESCE(em."DocumentoIdentidad", '') AS "RutEmisor"
			 ,CASE d."TipoOperacion"
			 WHEN '1' THEN 'DISTRIBUCIONES'
			 WHEN '2' THEN 'LIQUIDACIONES'
			 WHEN '3' THEN 'DISMINUCIONES'
			 WHEN '4' THEN 'RESCATES'
			 WHEN '5' THEN 'INTERESES'
			 WHEN '6' THEN 'DEVOLUCIONES'
			 END AS "TipoOperacionCertificado"
			 ,COALESCE(cli."NombreCompleto", 'Pendiente de enrolar') AS "NombreCompletoCliente"
			 ,cli."DocumentoIdentidad" AS "DocumentoIdentidadCliente"
			 ,e."RazonSocial" AS "RazonSocialEmpresa"
			 ,e."DocumentoIdentidad" AS "DocumentoIdentidadEmpresa"
			 ,e."DireccionFisicaPrincipalCalle" || ' ' || e."DireccionFisicaPrincipalNumero" || ',' || e."DireccionFisicaPrincipalSector" || ' ' || e."DireccionFisicaPrincipalNombreCiudad" AS "DireccionEmpresa"
			 ,e."DireccionFisicaPrincipalNombreCiudad" || ' ' || TO_CHAR(TO_DATE(:Fechacertificado,'YYYY-MM-DD'),'DD/MM/YYYY') AS "CiudadFecha"
			 ,COALESCE(GIRO."Nombre",'') AS "GiroActividad"
			 ,'1993-06-04' AS "FechaNumeroCuotas"
			 ,c."Anio" || '-12-31' AS "FechaMontoActualizado"
			 ---------------------------------------------------------------------------
			 ,d."IdDetalleCertificadoClienteDJ1922N43"
			 ,d."IdCertificadoCliente"
			 ,TO_CHAR(d."FechaCertificado")
			 ,d."IdOperacion"
			 ,d."RutAdministradora"
			 ,d."IdEmisor"
			 ,d."RutFondo"
			 ,d."RunFondo"
			 ,d."Fondo"
			 ,d."TipoFondo"
			 ,d."RutCliente"
			 ,d."NombreParticipe"
			 ,d."TaxId"
			 ,d."CodigoPais"
			 ,d."TipoOperacion"
			 ,TO_DATE(d."FechaInversion", 'YYYY-MM-DD') AS "FechaInversion"
			 ,TO_VARCHAR(d."FechaInversion", 'YYYY-MM-DD') AS "FechaInversionText"
			 ,d."MonedaReajuste"
			 ,d."MontoNominalAportes"
			 ,d."MontoNominalAportesActualizado"
			 ,d."NumeroSerieCuotas"
			 ,d."CantidadCuotasParticipe"
			 ,d."ParticipeCumpleRequisitos"
			 ,d."CuotasCumplenRequisitos"
			 ,d."ValorCuotaPesos"
			 ,d."NumCuotasSerieParticipe"
			 ,d."PorcentajeParticipacion31Dic"
			 ,d."ValNominalCuotasRescatadas"
			 ,d."CantidadCuotasRescatadas"
			 ,d."DiferenciaPorRescateOEnajenacion"
			 ,d."DiferenciaPorRescateOEnajenacionActualizado"
			 ,d."DifPorRescateOEnajenacionCFIAcogido107"
			 ,d."DifPorRescateOEnajenacionCFIAcogido107Actualizado"
		     ,CASE WHEN :Version <= 2025 THEN
					CASE "TipoFondo" WHEN 'F' THEN  
						CASE WHEN d."DifPorRescateOEnajenacionCFIAcogido107Actualizado" < 0 THEN 0 ELSE d."DifPorRescateOEnajenacionCFIAcogido107Actualizado" end
					ELSE 0 END
		     ELSE --version >2025
		            CASE WHEN d."DiferenciaPorRescateOEnajenacionActualizado" < 0 THEN 0 ELSE d."DiferenciaPorRescateOEnajenacionActualizado" end
		     END AS "MAYORVALOR_FM"
			
		     ,CASE WHEN :Version <= 2025 THEN
		            CASE "TipoFondo" WHEN 'F' THEN  
		                CASE WHEN d."DifPorRescateOEnajenacionCFIAcogido107Actualizado" > 0 THEN 0 ELSE d."DifPorRescateOEnajenacionCFIAcogido107Actualizado" * -1 END
		            ELSE 0 END
		     ELSE --version >2025
		            CASE WHEN d."DiferenciaPorRescateOEnajenacionActualizado" > 0 THEN 0 ELSE d."DiferenciaPorRescateOEnajenacionActualizado" * -1 END
		     END AS "MENORVALOR_FM"
		
		     ,CASE WHEN :Version <= 2025 THEN
		            CASE "TipoFondo" WHEN 'I' THEN
		                CASE WHEN d."DiferenciaPorRescateOEnajenacionActualizado" < 0  THEN 0 ELSE d."DiferenciaPorRescateOEnajenacionActualizado"  END
		            ELSE 0 END
		     ELSE --version >2025
		            CASE WHEN d."DifPorRescateOEnajenacionCFIAcogido107Actualizado" < 0 THEN 0 ELSE d."DifPorRescateOEnajenacionCFIAcogido107Actualizado"  END
		     END AS "MAYORVALOR_I"
			
		     ,CASE WHEN :Version <= 2025 THEN
		            CASE "TipoFondo" WHEN 'I' THEN
		                CASE WHEN d."DiferenciaPorRescateOEnajenacionActualizado" > 0 THEN 0 ELSE d."DiferenciaPorRescateOEnajenacionActualizado" * -1 END
		            ELSE 0 END
		     ELSE --version >2025
		            CASE WHEN d."DifPorRescateOEnajenacionCFIAcogido107Actualizado" > 0 THEN 0 ELSE d."DifPorRescateOEnajenacionCFIAcogido107Actualizado" * -1 END
		     END AS "MENORVALOR_I"
			 ,d."ValNominalInversionaFechaLiquidacion"
			 ,d."CantCuotasCedidasPorLiquidacion"
			 ,d."DiferenciaValorCuotasFechaLiquidacion"
			 ,d."DiferenciaValorCuotasFechaLiquidacionActualizado"
			 ,d."TributaSa"
			 ,d."DivRemDistAfectGCConIDPC"
			 ,d."DivRemDistAfectGCConIDPCal31122016"
			 ,d."DivRemDistAfectGCConIDPCVOL"
			 ,d."DivRemDistAfectGCSINDC"
			 ,d."DisRemRentasTribCProvRap_C96"
			 ,d."DisRemRentasTribCORP_C97"
			 ,d."DisRemRentasTribCExDD_C98"
			 ,d."DisRemRentasTribCUtAFFut_C99"
			 ,d."DivRemDistNoRentaFUT"
			 ,d."DisRemRentasExIGCAfecImptoAdi_C100"
			 ,d."CredImpGloCompNSRSD"
			 ,d."CredImpGloCompNSRCD"
			 ,d."CredIGCAcumD01012017RANSRD01012020SDD_C101"
			 ,d."CredIGCAcumD01012017RANSRD01012020CDD_C102"
			 ,d."CredImpGloCompSRSD"
			 ,d."CredImpGloCompSRCD"
			 ,d."CredIGCAcumD01012017RXSRSDD_C103"
			 ,d."CredIGCAcumD01012017RXSRCDD_C104"
			 ,d."CredImpGloCompCTDCIF"
			 ,d."CredImpGloCompCDD31122016"
			 ,d."CredImpGloCompSDD31122016"
			 ,d."CredIGCAcumH31122016RXSDD_C105"
			 ,d."CredIGCAcumH31122016RXCDD_C106"
			 ,d."CredImpGloCompCTDCIF31122016"
			 ,d."CredImpGloCompCITAExArt21"
			 ,d."DetBenNetoPerRutFdo_C107"
			 ,d."DetBenNetoPerPOS_C113"
			 ,d."DetBenNetoPerNEG_C114"
			 ,d."DividendosExentosIGCoAdicional"
			 ,d."DividendosExentosIGCoAdicionalActualizado"
			 ,d."DividendosNoConstitutivosDeRenta"
			 ,d."DividendosNoConstitutivosDeRentaActualizado"
			 ,d."DivRemDistNoRentaFE"
			 ,d."DividendosAfectosImptoUnicoTasa10"
			 ,d."DividendosAfectosImptoUnicoTasa10Actualizado"
			 ,d."DividendosAfectosImptoUnicoTasa4"
			 ,d."DividendosAfectosImptoUnicoTasa4Actualizado"
			 ,d."DistribucionXDiminucionNoImputadaCapital"
			 ,d."DevolucionesDeCapital"
			 ,d."DevolucionesDeCapitalActualizado"
			 ,d."MontoInversionenCuotasCFIAnterior93"
			 ,d."NumeroCertificado"
			 ,COALESCE( COALESCE(IPCACTR."Valor", 1) / NULLIF(IPCINVR."Valor", 0), 0) AS "FactorActualizacion"
			 ,TO_CHAR(d."FechaOrigenInversion")
			 ,SUMA."SumaNumeroCuotas"
			 ,SUMA."SumaMontoActualizado"
			 ,COALESCE(CP."Certificacion",'N') AS "Certificado"
 			 ,COALESCE(CP."EstadoCertificado",'N') AS "EstadoCertificado"
  		     ,coalesce(rzs."RazonSocial",coalesce(fm."NombreSerieFondo",''))  AS "NombreFondo"
			 ,d."RunNemotecnico"
			FROM  --AQUI la tabla que filtra los ruts
			(
				SELECT CER."IdCertificadoCliente", CER."RutCliente", CER."IdEmpresa", CER."Folio", CER."Esquema", CER."Version", CER."Anio", CER."Periodo", CER."IdCliente"
				 FROM  "BSCL.Normativa.BD::TRI.CertificadoCliente" AS CER
				 WHERE CER."IdEmpresa"=:IdEmpresa AND CER."Version"=:Version AND CER."IdTipoCertificado"='CERT43'
				 AND CER."IdCertificadoCliente" in (:IdCertificadoCliente)
			) AS c
			INNER JOIN "BSCL.Normativa.BD::TRI.DetalleCertificadoClienteDJ1922N43" 	d 	ON (c."IdCertificadoCliente" = d."IdCertificadoCliente")
			INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" 			cli ON (TRIM(cli."DocumentoIdentidad") = TRIM(c."RutCliente"))
			INNER JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa" 			e 	ON (e."IdEmpresa" = c."IdEmpresa")
			INNER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" PER ON (PER."IdPersona" = e."IdPersona")
			INNER JOIN 
			(
				SELECT SUM(CASE WHEN "FechaOrigenInversion" < '1993-06-04' THEN "CantidadCuotasParticipe"  ELSE 0 END) AS "SumaNumeroCuotas"
					  ,SUM("MontoNominalAportesActualizado") AS "SumaMontoActualizado"
				      ,"IdCertificadoCliente"
				FROM "BSCL.Normativa.BD::TRI.DetalleCertificadoClienteDJ1922N43"
				GROUP BY "IdCertificadoCliente"
			) AS SUMA ON c."IdCertificadoCliente"=SUMA."IdCertificadoCliente"
			LEFT OUTER JOIN (SELECT "RazonSocial","Nemotecnico"  ,RANK() OVER(PARTITION BY "Nemotecnico" ORDER BY "IdInstrumento" ) AS "Rank"
								FROM  "BSCL.Plataforma.BD::INS.Instrumento") AS rzs  on ( rzs."Rank"=1 AND rzs."Nemotecnico" = d."Fondo")
			LEFT OUTER JOIN	"BSCL.FondoMutuo.BD::FMM.SerieDeFondo" as fm on (fm."IdSerieFondo" = d."Fondo")				
								
			LEFT OUTER JOIN "BSCL.Plataforma.BD.Vistas::MER.ConsultarEmisor" EM ON (LEFT(RIGHT(EM."IdEmisor")) = LEFT(RIGHT(d."IdEmisor")))
			LEFT OUTER JOIN  "BSCL.Plataforma.BD.Vistas::REG.ConsultarGiro" GIRO ON (GIRO."IdGiro" = PER."IdGiroPrincipal")
			LEFT  JOIN "BSCL.Plataforma.BD::MER.ParametroFinanciero" IPCINVR  ON (substring(ADD_MONTHS (TO_DATE (COALESCE(d."FechaInversion",'1900-01-01'), 'YYYY-MM-DD'), -1),0,7) = IPCINVR."AgnoMes")
			AND IPCINVR."IdParametroFinanciero" = 'IPC'
			LEFT  JOIN "BSCL.Plataforma.BD::MER.ParametroFinanciero" IPCACTR  ON (SUBSTRING(COALESCE(d."FechaInversion",'1900-01-01'),0,4) || '-11' = IPCACTR."AgnoMes")
			AND IPCACTR."IdParametroFinanciero" = 'IPC'
			LEFT  JOIN "BSCL.Normativa.BD::TRI.ControlProceso" CP ON (CP."Esquema" = 'DJ1922' AND CP."Version" = :Version)
			WHERE c."Version" = :Version
			ORDER BY c."Folio"