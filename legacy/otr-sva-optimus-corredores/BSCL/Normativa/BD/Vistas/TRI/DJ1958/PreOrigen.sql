SET SCHEMA "BSCL";

DROP VIEW "BSCL.Normativa.BD.Vistas::TRI.DJ1958.PreOrigen";

CREATE VIEW "BSCL.Normativa.BD.Vistas::TRI.DJ1958.PreOrigen" (

		  "IdEmpresa"
		, "PaisPersona"
		, "TipoPersona"
		, "FechaNacimiento"
		--------------------
		, "TipoRegistro"
		, "FechaConsulta"
		, "IdPersona"
		, "IdCliente"
		, "IdEstadoCliente"
		, "IdInstrumento"
		, "Nemo"
		, "IdTipoInstrumento"
		, "IdClasificacionTipoInstrumento"
		, "IdMonedaInstrumento"
		, "ValorMercado" 	--moneda instrumento
		, "IdMonedaSistema"
		, "MontoMercado"	--moneda sistema
		, "DivisaPagoCuentaFinanciera"
		, "MontoPagoActualizado"
		, "RutFondo"
	
) AS

	SELECT 
	 	 "IdEmpresa"
		, "PaisPersona"
		, "TipoPersona"
		, "FechaNacimiento"
		, "TipoRegistro"
		, TO_SECONDDATE((YEAR("FechaConsulta") || '-12'|| '-31 00:00:00'),'YYYY-MM-DD HH24:MI:SS') AS "FechaConsulta"
		, "IdPersona"
		, "IdCliente"
		, "IdEstadoCliente"
		, "IdInstrumento"
		, "Nemo"
		, "IdTipoInstrumento"
		, "IdClasificacionTipoInstrumento"
		, "IdMonedaInstrumento"
		, SUM("ValorMercado") AS "ValorMercado"
		, "IdMonedaSistema"
		, SUM("MontoMercado") AS "MontoMercado"
		, "DivisaPagoCuentaFinanciera" AS "DivisaPagoCuentaFinanciera"
		, SUM("MontoPagoActualizado")  AS "MontoPagoActualizado"
		, "RutFondo"
	
	FROM 
	
	(
		---CUSTODIA
			  SELECT	
								  "CAR"."IdEmpresa"							AS "IdEmpresa"
								, "PER"."IdPaisResidencia"					AS "PaisPersona"
								, "PER"."Tipo"								AS "TipoPersona"
								, "PER"."FechaNacimiento" 					AS "FechaNacimiento"
								, 'CUS'										AS "TipoRegistro"
								, "CAR"."FechaRespaldo"						AS "FechaConsulta"
								, "PER"."IdPersona"							AS "IdPersona"
								, "CLI"."IdCliente" 						AS "IdCliente"
								, (CASE "CLI"."IdEstado" WHEN 'ACT' THEN CASE "CLI"."EsEnrolado" WHEN 'P' THEN 'PEN' WHEN 'N' THEN 'PEN' ELSE 'ACT' END ELSE 'BLO' END) AS "IdEstadoCliente"
								, 0  AS "IdInstrumento"
								, '' AS "Nemo"
								, '' AS "IdTipoInstrumento"
								, '' AS "IdClasificacionTipoInstrumento"
								, "CAR"."IdMonedaInstrumento"						AS "IdMonedaInstrumento"
								, "CAR"."INGMontoMercadoActualMonedaInstrumento" 	AS "ValorMercado"
								, "CAR"."IdMonedaSistema"							AS "IdMonedaSistema"
								, "CAR"."INGMontoATasaMercadoActual" 				AS "MontoMercado"
								, "CAR"."IdMonedaInstrumento" AS "DivisaPagoCuentaFinanciera"
								, 0  AS "MontoPagoActualizado"
								,CASE WHEN "CAR"."IdTipoInstrumento" IN ('FFM','CFI','ETN') THEN COALESCE(Rutfondos."ValorDestino",'') 
								ELSE '' END AS "RutFondo"
				FROM "BSCL.SocioNegocio.BD::CLI.Cliente" AS "CLI"     
				INNER JOIN "BSCL.SocioNegocio.BD::PER.Persona"   		AS "PER" ON ( "PER"."IdPersona" = "CLI"."IdPersona" AND "PER"."ResidenciaTributariaExtranjera"='S')
				INNER JOIN "BSCL.SocioNegocio.BD::PER.Fatca"			AS "FAT" ON ( "PER"."IdPersona" = "FAT"."IdPersona" AND "FAT"."DeclaracionCRS"='S')
				INNER JOIN "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" 	AS "SOC" ON ( "CLI"."IdCliente" = "SOC"."IdCliente")
				INNER JOIN
					(	
							SELECT  
							RANK() OVER
							(PARTITION BY "FolioCartera", "IdCliente"
							ORDER BY "FolioCartera","FechaRespaldo" DESC)
							as RankNum
							,*
							FROM 
							"BSCL.Custodia.BD::HIS.Cartera"
							
							WHERE "IdEstado" ='VIG' AND	 "IdTipoInstrumento" NOT IN ('PRT','EMP','SIM','RRV','RRF','PAC','FWD') 
							AND  MONTH("FechaRespaldo") = 12
				
				)AS "CAR" ON ("CAR"."IdEmpresa" = "CLI"."IdEmpresa" AND "CLI"."IdCliente" = "CAR"."IdCliente" AND  RankNum=1)
				LEFT  JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" ins ON trim(ins."IdInstrumento") = trim("CAR"."IdInstrumento")
				LEFT  JOIN "BSCL.Plataforma.BD::INS.Instrumento" inst ON TRIM(inst."IdInstrumento") = TRIM(ins."IdInstrumento")
				LEFT  JOIN "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarFondo" fondo ON trim(fondo."IdSerieFondo") = trim("CAR"."Nemotecnico")
				LEFT  JOIN "BSCL.Plataforma.BD.Vistas::MER.ConsultarEmisor" emi ON (ins."IdEmisor"=emi."IdEmisor")
				LEFT  JOIN "BSCL.Normativa.BD::DOC.Homologacion"  Rutfondos on (Rutfondos."Esquema"='0' AND Rutfondos."Tipo"='RutFondo'  AND (inst."DocumentoIdentidadFondo"=Rutfondos."ValorOrigen" OR fondo."CodigoSVS" = RutFondos."ValorOrigen" ))
				
  			 	WHERE "CAR"."IdEstado" ='VIG' AND	  "CAR"."IdTipoInstrumento" NOT IN ('PRT','EMP','SIM','RRV','RRF','PAC','FWD') 
  			 	
  			---DIVIDENDO
  			UNION ALL 	
  			 	
  			 	SELECT	
								  "CAR"."IdEmpresa"							AS "IdEmpresa"
								, "PER"."IdPaisResidencia"					AS "PaisPersona"
								, "PER"."Tipo"								AS "TipoPersona"
								, "PER"."FechaNacimiento" 					AS "FechaNacimiento"
								, 'DIV'										AS "TipoRegistro"
								, "CAR"."FechaPago"							AS "FechaConsulta"
								, "PER"."IdPersona"							AS "IdPersona"
								, "CLI"."IdCliente" 						AS "IdCliente"
								, (CASE "CLI"."IdEstado" WHEN 'ACT' THEN CASE "CLI"."EsEnrolado" WHEN 'P' THEN 'PEN' WHEN 'N' THEN 'PEN' ELSE 'ACT' END ELSE 'BLO' END) AS "IdEstadoCliente"
								, "CAR"."IdInstrumento"						AS "IdInstrumento"
								, "CAR"."Nemo"								AS "Nemo"
								, "CAR"."IdTipoInstrumento"					AS "IdTipoInstrumento"
								, "CAR"."IdClasificacionTipoInstrumento"	AS "IdClasificacionTipoInstrumento"
								, "CAR"."IdMoneda"							AS "IdMonedaInstrumento"
								, 0 	AS "ValorMercado"
								, ''	AS "IdMonedaSistema"
								, 0		AS "MontoMercado"
								, "CAR"."IdMoneda"							AS "DivisaPagoCuentaFinanciera"
								, "CAR"."Total" 							AS "MontoPagoActualizado"
								,CASE WHEN "CAR"."IdTipoInstrumento" IN ('FFM','CFI','ETN') THEN COALESCE(Rutfondos."ValorDestino",'') 
								ELSE '' END AS "RutFondo"
				FROM "BSCL.SocioNegocio.BD::CLI.Cliente" AS "CLI"     
				INNER JOIN "BSCL.SocioNegocio.BD::PER.Persona"   		AS "PER" ON ( "PER"."IdPersona" = "CLI"."IdPersona" AND "PER"."ResidenciaTributariaExtranjera"='S')
				INNER JOIN "BSCL.SocioNegocio.BD::PER.Fatca"			AS "FAT" ON ( "PER"."IdPersona" = "FAT"."IdPersona" AND "FAT"."DeclaracionCRS"='S')
				INNER JOIN "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" 	AS "SOC" ON ( "CLI"."IdCliente" = "SOC"."IdCliente")
				INNER JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarEventoCapitalCliente"	AS "CAR" ON ("CAR"."IdEmpresa" = "CLI"."IdEmpresa" AND "CLI"."IdCliente" = "CAR"."IdCliente")
				LEFT  JOIN "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarFondo" fondo ON trim(fondo."IdSerieFondo") = trim("CAR"."Nemo")
				LEFT  JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" ins ON trim(ins."IdInstrumento") = trim("CAR"."IdInstrumento")
				LEFT  JOIN "BSCL.Plataforma.BD::INS.Instrumento" inst ON TRIM(inst."IdInstrumento") = TRIM(ins."IdInstrumento")
				LEFT  JOIN "BSCL.Plataforma.BD.Vistas::MER.ConsultarEmisor" emi ON (ins."IdEmisor"=emi."IdEmisor")
				LEFT  JOIN "BSCL.Normativa.BD::DOC.Homologacion"  Rutfondos on (Rutfondos."Esquema"='0' AND Rutfondos."Tipo"='RutFondo'  AND (inst."DocumentoIdentidadFondo"=Rutfondos."ValorOrigen" OR fondo."CodigoSVS" = RutFondos."ValorOrigen" ))
  			 	WHERE "CAR"."IdEstado" ='CER' AND  "CAR"."IdTipoEventoCapital"  in('DV','RC') AND "CAR"."TipoSocioNegocio" = 'CLI' AND "CAR"."Total" > 0
  			 	
			---CORTE CUPON
			UNION ALL 	
  			 	
  			 	SELECT	
								  "CAR"."IdEmpresa"							AS "IdEmpresa"
								, "PER"."IdPaisResidencia"					AS "PaisPersona"
								, "PER"."Tipo"								AS "TipoPersona"
								, "PER"."FechaNacimiento" 					AS "FechaNacimiento"
								, 'CCU'										AS "TipoRegistro"
								, "CAR"."FechaPago"							AS "FechaConsulta"
								, "PER"."IdPersona"							AS "IdPersona"
								, "CLI"."IdCliente" 						AS "IdCliente"
								, (CASE "CLI"."IdEstado" WHEN 'ACT' THEN CASE "CLI"."EsEnrolado" WHEN 'P' THEN 'PEN' WHEN 'N' THEN 'PEN' ELSE 'ACT' END ELSE 'BLO' END) AS "IdEstadoCliente"
								, "CAR"."IdInstrumento"						AS "IdInstrumento"
								, "CAR"."Nemo"								AS "Nemo"
								, "CAR"."IdTipoInstrumento"					AS "IdTipoInstrumento"
								, "CAR"."IdClasificacionTipoInstrumento"	AS "IdClasificacionTipoInstrumento"
								, "CAR"."IdMoneda"							AS "IdMonedaInstrumento"
								, 0 	AS "ValorMercado"
								, ''	AS "IdMonedaSistema"
								, 0		AS "MontoMercado"
								, "CAR"."IdMoneda"							AS "DivisaPagoCuentaFinanciera"
								, "CAR"."MontoInteres" 						AS "MontoPagoActualizado"
								,CASE WHEN "CAR"."IdTipoInstrumento" IN ('FFM','CFI','ETN') THEN COALESCE(Rutfondos."ValorDestino",'') 
								ELSE '' END AS "RutFondo"
				FROM "BSCL.SocioNegocio.BD::CLI.Cliente" AS "CLI"     
				INNER JOIN "BSCL.SocioNegocio.BD::PER.Persona"   		AS "PER" ON ( "PER"."IdPersona" = "CLI"."IdPersona" AND "PER"."ResidenciaTributariaExtranjera"='S')
				INNER JOIN "BSCL.SocioNegocio.BD::PER.Fatca"			AS "FAT" ON ( "PER"."IdPersona" = "FAT"."IdPersona" AND "FAT"."DeclaracionCRS"='S')
				INNER JOIN "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" 	AS "SOC" ON ( "CLI"."IdCliente" = "SOC"."IdCliente")
				INNER JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarEventoCapitalCliente"	AS "CAR" ON ("CAR"."IdEmpresa" = "CLI"."IdEmpresa" AND "CLI"."IdCliente" = "CAR"."IdCliente")
				LEFT  JOIN "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarFondo" fondo ON trim(fondo."IdSerieFondo") = trim("CAR"."Nemo")
				LEFT  JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" ins ON trim(ins."IdInstrumento") = trim("CAR"."IdInstrumento")
				LEFT  JOIN "BSCL.Plataforma.BD::INS.Instrumento" inst ON TRIM(inst."IdInstrumento") = TRIM(ins."IdInstrumento")
				LEFT  JOIN "BSCL.Plataforma.BD.Vistas::MER.ConsultarEmisor" emi ON (ins."IdEmisor"=emi."IdEmisor")
				LEFT  JOIN "BSCL.Normativa.BD::DOC.Homologacion"  Rutfondos on (Rutfondos."Esquema"='0' AND Rutfondos."Tipo"='RutFondo'  AND (inst."DocumentoIdentidadFondo"=Rutfondos."ValorOrigen" OR fondo."CodigoSVS" = RutFondos."ValorOrigen" ))
  			 	WHERE "CAR"."IdEstado" ='CER' AND  "CAR"."IdTipoEventoCapital" = 'CC' AND "CAR"."TipoSocioNegocio" = 'CLI' 
  			 	
  			 	
  			---MESA RF
			UNION ALL 	

  			 		SELECT	
								  "CAR"."IdEmpresa"							AS "IdEmpresa"
								, "PER"."IdPaisResidencia"					AS "PaisPersona"
								, "PER"."Tipo"								AS "TipoPersona"
								, "PER"."FechaNacimiento" 					AS "FechaNacimiento"
								, 'MRF'										AS "TipoRegistro"
								, "CAR"."FechaOperacion"					AS "FechaConsulta"
								, "PER"."IdPersona"							AS "IdPersona"
								, "CLI"."IdCliente" 						AS "IdCliente"
								, (CASE "CLI"."IdEstado" WHEN 'ACT' THEN CASE "CLI"."EsEnrolado" WHEN 'P' THEN 'PEN' WHEN 'N' THEN 'PEN' ELSE 'ACT' END ELSE 'BLO' END) AS "IdEstadoCliente"
								, "CAR"."IdInstrumento"						AS "IdInstrumento"
								, "CAR"."Nemotecnico"						AS "Nemo"
								, "CAR"."IdTipoInstrumento"					AS "IdTipoInstrumento"
								, "CAR"."IdClasificacionTipoInstrumento"	AS "IdClasificacionTipoInstrumento"
								, "CT"."IdMoneda"							AS "IdMonedaInstrumento"
								, 0 	AS "ValorMercado"
								, ''	AS "IdMonedaSistema"
								, 0		AS "MontoMercado"
								, "CT"."IdMoneda"		AS "DivisaPagoCuentaFinanciera"
								, "CTA"."Monto"			AS "MontoPagoActualizado"
								,CASE WHEN "CAR"."IdTipoInstrumento" IN ('FFM','CFI','ETN') THEN COALESCE(Rutfondos."ValorDestino",'') 
								ELSE '' END AS "RutFondo"
	
				FROM "BSCL.SocioNegocio.BD::CLI.Cliente" AS "CLI"     
				INNER JOIN "BSCL.SocioNegocio.BD::PER.Persona"   		AS "PER" ON ( "PER"."IdPersona" = "CLI"."IdPersona" AND "PER"."ResidenciaTributariaExtranjera"='S')
				INNER JOIN "BSCL.SocioNegocio.BD::PER.Fatca"			AS "FAT" ON ( "PER"."IdPersona" = "FAT"."IdPersona" AND "FAT"."DeclaracionCRS"='S')
				INNER JOIN "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" 	AS "SOC" ON ( "CLI"."IdCliente" = "SOC"."IdCliente")
				INNER JOIN "BSCL.Custodia.BD::CUS.Cartera"				AS "CAR" ON ( "CAR"."IdEmpresa" = "CLI"."IdEmpresa" AND "CLI"."IdCliente" = "CAR"."IdCliente")
				INNER JOIN "BSCL.Mesa.BD::MES.Operacion"				AS "OPE" ON ( "OPE"."IdOperacion" = "CAR"."IdOperacion")
				INNER JOIN "BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento"  AS "CTA" ON ("CTA"."IdComprobante"="CAR"."IdCartera" AND "CTA"."IdTipoComprobante"='CUS' AND "CTA"."IdTipoMovimiento"='MOVABN' AND "CTA"."IdEstado"='CRT')
				INNER JOIN "BSCL.CuentaInversion.BD::CTA.CuentaInversion" 		AS "CT"  ON ("CTA"."IdCuentaInversion"="CT"."IdCuentaInversion")
  			 	LEFT  JOIN "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarFondo" fondo ON trim(fondo."IdSerieFondo") = trim("CAR"."Nemotecnico")
				LEFT  JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" ins ON trim(ins."IdInstrumento") = trim("CAR"."IdInstrumento")
				LEFT  JOIN "BSCL.Plataforma.BD::INS.Instrumento" inst ON TRIM(inst."IdInstrumento") = TRIM(ins."IdInstrumento")
				LEFT  JOIN "BSCL.Plataforma.BD.Vistas::MER.ConsultarEmisor" emi ON (ins."IdEmisor"=emi."IdEmisor")
				LEFT  JOIN "BSCL.Normativa.BD::DOC.Homologacion"  Rutfondos on (Rutfondos."Esquema"='0' AND Rutfondos."Tipo"='RutFondo'  AND (inst."DocumentoIdentidadFondo"=Rutfondos."ValorOrigen" OR fondo."CodigoSVS" = RutFondos."ValorOrigen" ))
				WHERE  "CAR"."IdClasificacionTipoInstrumento" = 'RF' AND "CAR"."EGRIdConcepto" = 'VCDTIT' AND "CAR"."IdEstado"  = 'VCD'
  			 	AND  "CAR"."IdTipoOperacion" NOT IN ('COMPAC','VENPAC') 
  			 	
  			 	
  			 	
  			---VENCIMIENTOS  IF (DAP)
			UNION ALL 	
  			
			SELECT * FROM 
  			(
				SELECT	
								  "CAR"."IdEmpresa"							AS "IdEmpresa"
								, "PER"."IdPaisResidencia"					AS "PaisPersona"
								, "PER"."Tipo"								AS "TipoPersona"
								, "PER"."FechaNacimiento" 					AS "FechaNacimiento"
								, 'DAP'										AS "TipoRegistro"
								, "CAR"."FechaVencimientoOperacion"			AS "FechaConsulta"
								, "PER"."IdPersona"							AS "IdPersona"
								, "CLI"."IdCliente" 						AS "IdCliente"
								, (CASE "CLI"."IdEstado" WHEN 'ACT' THEN CASE "CLI"."EsEnrolado" WHEN 'P' THEN 'PEN' WHEN 'N' THEN 'PEN' ELSE 'ACT' END ELSE 'BLO' END) AS "IdEstadoCliente"
								, "CAR"."IdInstrumento"						AS "IdInstrumento"
								, "CAR"."Nemotecnico"						AS "Nemo"
								, "CAR"."IdTipoInstrumento"					AS "IdTipoInstrumento"
								, "CAR"."IdClasificacionTipoInstrumento"	AS "IdClasificacionTipoInstrumento"
								, "CAR"."IdMonedaSistema"					AS "IdMonedaInstrumento"
								, 0 	AS "ValorMercado"
								, ''	AS "IdMonedaSistema"
								, 0		AS "MontoMercado"
								, "CAR"."IdMonedaSistema"	AS "DivisaPagoCuentaFinanciera"
								,  (CAR."EGRMonto"- ((CAR."EGRValorRescate"/ CAR."ValorRescate") * CAR."MontoCompra") ) - 
								(
								  ( (CAR."EGRMonto"- ((CAR."EGRValorRescate"/ CAR."ValorRescate") * CAR."MontoCompra") ) / 
								     CASE WHEN DAYS_BETWEEN (CAR."FechaOperacion",CAR."FechaVencimientoOperacion")  > 0 THEN DAYS_BETWEEN (CAR."FechaOperacion",CAR."FechaVencimientoOperacion") ELSE 1 END )  *
								  	 DAYS_BETWEEN (CAR."FechaOperacion",(TO_DATE((YEAR("FechaOperacion") || '-12'|| '-31'),'YYYY-MM-DD')))
								)
								AS "MontoPagoActualizado"
								,CASE WHEN "CAR"."IdTipoInstrumento" IN ('FFM','CFI','ETN') THEN COALESCE(Rutfondos."ValorDestino",'') 
								ELSE '' END AS "RutFondo"
	
				FROM "BSCL.SocioNegocio.BD::CLI.Cliente" AS "CLI"     
				INNER JOIN "BSCL.SocioNegocio.BD::PER.Persona"   		AS "PER" ON ( "PER"."IdPersona" = "CLI"."IdPersona" AND "PER"."ResidenciaTributariaExtranjera"='S')
				INNER JOIN "BSCL.SocioNegocio.BD::PER.Fatca"			AS "FAT" ON ( "PER"."IdPersona" = "FAT"."IdPersona" AND "FAT"."DeclaracionCRS"='S')
				INNER JOIN "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" 	AS "SOC" ON ( "CLI"."IdCliente" = "SOC"."IdCliente")
				INNER JOIN "BSCL.Custodia.BD::CUS.Cartera"				AS "CAR" ON ( "CAR"."IdEmpresa" = "CLI"."IdEmpresa" AND "CLI"."IdCliente" = "CAR"."IdCliente")
				LEFT  JOIN "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarFondo" fondo ON trim(fondo."IdSerieFondo") = trim("CAR"."Nemotecnico")
				LEFT  JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" ins ON trim(ins."IdInstrumento") = trim("CAR"."IdInstrumento")
				LEFT  JOIN "BSCL.Plataforma.BD::INS.Instrumento" inst ON TRIM(inst."IdInstrumento") = TRIM(ins."IdInstrumento")
				LEFT  JOIN "BSCL.Plataforma.BD.Vistas::MER.ConsultarEmisor" emi ON (ins."IdEmisor"=emi."IdEmisor")
				LEFT  JOIN "BSCL.Normativa.BD::DOC.Homologacion"  Rutfondos on (Rutfondos."Esquema"='0' AND Rutfondos."Tipo"='RutFondo'  AND (inst."DocumentoIdentidadFondo"=Rutfondos."ValorOrigen" OR fondo."CodigoSVS" = RutFondos."ValorOrigen" ))
				
				WHERE  "CAR"."IdClasificacionTipoInstrumento" = 'IF' AND "CAR"."EGRIdConcepto" = 'VCDTIT' AND "CAR"."IdEstado"  = 'VCD'
  			 	AND  "CAR"."IdTipoOperacion" NOT IN ('COMPAC','VENPAC') 
  			 	AND CAR."ValorRescate">0
  			 ) WHERE  "MontoPagoActualizado">0	
  			 	
  			 	
  			 	
  			---FFMM
			UNION ALL 	
  			 	
  			 	SELECT	
								  "CAR"."IdEmpresa"							AS "IdEmpresa"
								, "PER"."IdPaisResidencia"					AS "PaisPersona"
								, "PER"."Tipo"								AS "TipoPersona"
								, "PER"."FechaNacimiento" 					AS "FechaNacimiento"
								, 'FMM'										AS "TipoRegistro"
								, "CAR"."FechaValorCuota"					AS "FechaConsulta"
								, "PER"."IdPersona"							AS "IdPersona"
								, "CLI"."IdCliente" 						AS "IdCliente"
								, (CASE "CLI"."IdEstado" WHEN 'ACT' THEN CASE "CLI"."EsEnrolado" WHEN 'P' THEN 'PEN' WHEN 'N' THEN 'PEN' ELSE 'ACT' END ELSE 'BLO' END) AS "IdEstadoCliente"
								, COALESCE("SER_FDO"."IdInstrumento",0)  	AS "IdInstrumento"
								, COALESCE("SER_FDO"."IdSerieFondo",'')		AS "Nemo"
								, COALESCE("INS"."IdTipoInstrumento",'')	AS "IdTipoInstrumento"
								, COALESCE("TINS"."IdClasificacionTipoInstrumento",'')	AS "IdClasificacionTipoInstrumento"
								, "CAR"."IdMoneda"							AS "IdMonedaInstrumento"
								, 0 	AS "ValorMercado"
								, ''	AS "IdMonedaSistema"
								, 0		AS "MontoMercado"
								, "CAR"."IdMoneda" 		AS "DivisaPagoCuentaFinanciera"
								, "CAR"."Monto" 		AS "MontoPagoActualizado"
								,CASE WHEN "INS"."IdTipoInstrumento" IN ('FFM','CFI','ETN') THEN COALESCE(Rutfondos."ValorDestino",'') 
								ELSE '' END AS "RutFondo"
	
				FROM "BSCL.SocioNegocio.BD::CLI.Cliente" AS "CLI"     
				INNER JOIN "BSCL.SocioNegocio.BD::PER.Persona"   			AS "PER" ON ( "PER"."IdPersona" = "CLI"."IdPersona" AND "PER"."ResidenciaTributariaExtranjera"='S')
				INNER JOIN "BSCL.SocioNegocio.BD::PER.Fatca"				AS "FAT" ON ( "PER"."IdPersona" = "FAT"."IdPersona" AND "FAT"."DeclaracionCRS"='S')
				INNER JOIN "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" 		AS "SOC" ON ( "CLI"."IdCliente" = "SOC"."IdCliente")
				INNER JOIN "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate"	AS "CAR" ON ( "CAR"."IdEmpresa" = "CLI"."IdEmpresa" AND "CLI"."IdCliente" = "CAR"."IdCliente")
				LEFT OUTER JOIN "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" 		AS "SER_FDO" ON ("SER_FDO"."IdSerieFondo" = "CAR"."IdSerieFondo")
				LEFT OUTER JOIN "BSCL.Plataforma.BD::INS.Instrumento" 		AS "INS" 	ON	("INS"."IdInstrumento" = "SER_FDO"."IdInstrumento")
				LEFT OUTER JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" 		AS "TINS" 	ON	("INS"."IdTipoInstrumento" = "TINS"."IdTipoInstrumento")
				LEFT  JOIN "BSCL.Plataforma.BD.Vistas::MER.ConsultarEmisor" emi ON (ins."IdEmisor"=emi."IdEmisor")
				LEFT  JOIN "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarFondo" fondo ON trim(fondo."IdSerieFondo") = trim("CAR"."IdSerieFondo")
				LEFT  JOIN "BSCL.Normativa.BD::DOC.Homologacion"  Rutfondos on (Rutfondos."Esquema"='0' AND Rutfondos."Tipo"='RutFondo'  AND (ins."DocumentoIdentidadFondo"=Rutfondos."ValorOrigen" OR fondo."CodigoSVS" = RutFondos."ValorOrigen" ))
				
				WHERE "CAR"."IdTipoMovimientoFondoMutuo" IN ('RESNORM','RESREINV','RESCDF') 	AND "CAR"."Estado" = 'CRT'
  		---FACTURACION
				
			UNION ALL
			
			SELECT	
					  "COM"."IdEmpresa"							AS "IdEmpresa"
					, "PER"."IdPaisResidencia"					AS "PaisPersona"
					, "PER"."Tipo"								AS "TipoPersona"
					, "PER"."FechaNacimiento" 					AS "FechaNacimiento"
					, 'FAC'										AS "TipoRegistro"
					, "COM"."FechaComprobante"					AS "FechaConsulta"
					, "PER"."IdPersona"							AS "IdPersona"
					, "CLI"."IdCliente" 						AS "IdCliente"
					, (CASE "CLI"."IdEstado" WHEN 'ACT' THEN CASE "CLI"."EsEnrolado" WHEN 'P' THEN 'PEN' WHEN 'N' THEN 'PEN' ELSE 'ACT' END ELSE 'BLO' END) AS "IdEstadoCliente"
					, COALESCE("CAR"."IdInstrumento",0)  	AS "IdInstrumento"
					, COALESCE("INS"."Nemotecnico",'')			AS "Nemo"
					, COALESCE("INS"."IdTipoInstrumento",'')	AS "IdTipoInstrumento"
					, COALESCE("INS"."IdClasificacionTipoInstrumento",'')	AS "IdClasificacionTipoInstrumento"
					, "CAR"."IdMonedaInstrumento"									AS "IdMonedaInstrumento"
					, 0 	AS "ValorMercado"
					, ''	AS "IdMonedaSistema"
					, 0		AS "MontoMercado"
					,"CAR"."IdMonedaInstrumento"	AS "DivisaPagoCuentaFinanciera"
					,( (CASE WHEN ("CAR"."EGRCantidad" > "CAR"."CantidadNCE") THEN ("CAR"."EGRCantidad" - "CAR"."CantidadNCE") ELSE "CAR"."EGRCantidad" END)  * "DETALLE"."Precio") - ( (CASE WHEN ("CAR"."EGRCantidad" > "CAR"."CantidadNCE") THEN ("CAR"."EGRCantidad" - "CAR"."CantidadNCE") ELSE "CAR"."EGRCantidad" END)  * "CAR"."PrecioCompra") AS "MontoPagoActualizado"
					,CASE WHEN "INS"."IdTipoInstrumento" IN ('FFM','CFI','ETN') THEN COALESCE(Rutfondos."ValorDestino",'') 
					ELSE '' END AS "RutFondo"			
								
				FROM "BSCL.SocioNegocio.BD::CLI.Cliente" AS "CLI"     
				INNER JOIN "BSCL.SocioNegocio.BD::PER.Persona"   				AS "PER" ON ( "PER"."IdPersona" = "CLI"."IdPersona" AND "PER"."ResidenciaTributariaExtranjera"='S')
				INNER JOIN "BSCL.SocioNegocio.BD::PER.Fatca"					AS "FAT" ON ( "PER"."IdPersona" = "FAT"."IdPersona" AND "FAT"."DeclaracionCRS"='S')
				INNER JOIN "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" 			AS "SOC" ON ( "CLI"."IdCliente" = "SOC"."IdCliente")
				INNER JOIN "BSCL.Facturacion.BD.Vistas::ConsultarComprobante" 	AS "COM" ON ( "COM"."IdEmpresa" = "CLI"."IdEmpresa" AND "CLI"."IdCliente" = "COM"."IdCliente")
				INNER JOIN "BSCL.Facturacion.BD.Vistas::ConsultarComprobanteDetalle" AS "DETALLE" ON ( "DETALLE"."IdComprobante" = "COM"."IdComprobante")
		 		
		 		INNER  JOIN 
		 		
		 		(
				 		
		 			SELECT "VTA"."EGRCantidad","VTA"."EGRFolioComprobante","VTA"."IdInstrumento","VTA"."IdMonedaInstrumento","VTA"."PrecioCompra" ,"VTA"."IdCliente", COALESCE("NCE"."Cantidad",0) AS "CantidadNCE"
		 			FROM 
		 				(
			 				SELECT sum("EGRCantidad") AS "EGRCantidad","EGRFolioComprobante","IdInstrumento","IdMonedaInstrumento","PrecioCompra" ,"IdCliente"
					 		FROM  "BSCL.Custodia.BD::CUS.Cartera" 
						 	WHERE "EGRFolioComprobante">0 AND "EGRCantidad">0
							group BY "EGRFolioComprobante","IdInstrumento" ,"PrecioCompra","IdCliente","IdInstrumento","IdMonedaInstrumento"
					 	)AS "VTA"
					 
					 LEFT JOIN	
					 	(

						 	SELECT "COM"."IdComprobanteReferencia","COM"."IdComprobante", "DETALLE"."Cantidad","DETALLE"."IdInstrumento","COM"."IdCliente"
						 	FROM "BSCL.Facturacion.BD.Vistas::ConsultarComprobante" AS "COM" 
						 	INNER JOIN "BSCL.Facturacion.BD.Vistas::ConsultarComprobanteDetalle"  AS "DETALLE" ON ( "DETALLE"."IdComprobante" = "COM"."IdComprobante")
						 	WHERE  "COM"."IdTipoComprobante" IN ('NCR','NCE','NEO') AND "COM"."IdComprobanteReferencia">0 
					 	
					 	) AS "NCE" ON  ("VTA"."EGRFolioComprobante"="NCE"."IdComprobanteReferencia" AND "VTA"."IdInstrumento" = "NCE"."IdInstrumento")
		 		
		 		) AS "CAR" ON ( "CAR"."EGRFolioComprobante" = "COM"."IdComprobante" AND "CAR"."IdCliente" = "COM"."IdCliente"  AND "DETALLE"."IdInstrumento" ="CAR"."IdInstrumento" AND "CAR"."EGRCantidad">0 AND "DETALLE"."Cantidad" >"CAR"."CantidadNCE")
		 		LEFT  JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" 	 AS "INS" ON ("INS"."IdInstrumento"	= "CAR" ."IdInstrumento")
		 		LEFT  JOIN "BSCL.Plataforma.BD::INS.Instrumento" inst ON TRIM(inst."IdInstrumento") = TRIM("INS"."IdInstrumento")
		 		LEFT  JOIN "BSCL.Plataforma.BD.Vistas::MER.ConsultarEmisor" emi ON (ins."IdEmisor"=emi."IdEmisor")
				LEFT  JOIN "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarFondo" fondo ON trim(fondo."IdSerieFondo") = trim("INS"."Nemotecnico")
				LEFT  JOIN "BSCL.Normativa.BD::DOC.Homologacion"  Rutfondos on (Rutfondos."Esquema"='0' AND Rutfondos."Tipo"='RutFondo'  AND (inst."DocumentoIdentidadFondo"=Rutfondos."ValorOrigen" OR fondo."CodigoSVS" = RutFondos."ValorOrigen" ))
				
  			 	WHERE "COM"."IdEstadoSII"  IN ('ACP','REC')	 AND "COM"."AccionTipoOperacion" = 'VENTA' AND "COM"."IdTipoComprobante" NOT IN ('NCR','NCE','NEO')-- AND "COM"."IdComprobanteReferencia"=0
  			 		  AND ( (CASE WHEN ("CAR"."EGRCantidad" > "CAR"."CantidadNCE") THEN ("CAR"."EGRCantidad" - "CAR"."CantidadNCE") ELSE "CAR"."EGRCantidad" END)  * "DETALLE"."Precio") - ( (CASE WHEN ("CAR"."EGRCantidad" > "CAR"."CantidadNCE") THEN ("CAR"."EGRCantidad" - "CAR"."CantidadNCE") ELSE "CAR"."EGRCantidad" END)  * "CAR"."PrecioCompra") >0
  			 	
	) AS "DAT"
	GROUP BY  "IdEmpresa"
			, "PaisPersona"
			, "TipoPersona"
			, "FechaNacimiento"
			, "TipoRegistro"
			,YEAR("FechaConsulta")
			, "IdPersona"
			, "IdCliente"
			, "IdEstadoCliente"
			, "IdInstrumento"
			, "Nemo"
			, "IdTipoInstrumento"
			, "IdClasificacionTipoInstrumento"
			, "IdMonedaInstrumento"
			--, "ValorMercado" 
			, "IdMonedaSistema"
			--, "MontoMercado"	
			, "DivisaPagoCuentaFinanciera"
			,"RutFondo"
			
				
WITH read ONLY;	
GRANT SELECT ON "BSCL.Normativa.BD.Vistas::TRI.DJ1958.PreOrigen" TO bcs_desarrollador WITH GRANT OPTION;