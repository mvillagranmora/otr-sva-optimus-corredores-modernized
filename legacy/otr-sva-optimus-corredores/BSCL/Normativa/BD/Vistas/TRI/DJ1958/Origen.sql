SET SCHEMA "BSCL";

DROP VIEW "BSCL.Normativa.BD.Vistas::TRI.DJ1958.Origen";
		  
CREATE VIEW "BSCL.Normativa.BD.Vistas::TRI.DJ1958.Origen"(
		  "ID"
		, "IdEmpresa"
		, "IdDocumento"
		, "VboUsuario"
		, "Observaciones"
		, "FuenteDatos"
		, "TipoRegistro"
		, "PaisResidenciaDeclarante"
		, "TipoInstitucion"
		, "PoseeCuentasFinanciera"
		, "PeriodoReporte"
		, "PaisResidencia"
		, "TipoPersona"
		, "TipoJuridico"
		, "Nombres"
		, "Apellidos"
		, "RazonSocial"
		, "PaisTaxID"
		, "TaxID"
		, "PaisDomicilio"
		, "TipoDomicilio"
		, "Domicilio"
		, "FechaNacimiento"
		, "NumeroCuenta"
		, "CuentaNoDocumentada"
		, "CuentaCerrada"
		, "DivisaSaldo"
		, "SaldoActualizado"
		, "PaisResidenciaControlador"
		, "NombreControlador"
		, "ApellidosControlador"
		, "PaisTaxIDControlador"
		, "TaxIDControlador"
		, "PaisDomicilioControlador"
		, "TipoDomicilioControlador"
		, "DomicilioControlador"
		, "FechaNacimientoControlador"
		, "TipoPagoCuentaFinanciera"
		, "DivisaPagoCuentaFinanciera"
		, "MontoPagoActualizado"
		, "IdCliente"
		--, "Portafolio"
		, "Nemo"
		, "IdTipoInstrumento"
		, "IdClasificacionTipoInstrumento"
		--, "IdTipoProducto"
		, "RutFondo"
		

	
) AS

 	 
			  SELECT	
								
								  0 										AS "ID"
								,"ORG"."IdEmpresa" 							AS "IdEmpresa"  
								, 0 										AS "IdDocumento"
								,'N' 										AS "VboUsuario"
								,'' 										AS "Observaciones"
								,'' 										AS "FuenteDatos"
								,"ORG"."TipoRegistro" 						AS  "TipoRegistro"
				
			
								, 'CL'										AS "PaisResidenciaDeclarante"
								, '1'										AS "TipoInstitucion"
								--, (CASE WHEN LEFT("ORG"."FechaConsulta",4)='2024' THEN 2 ELSE 1 END) AS  "PoseeCuentasFinanciera"
								, 1 AS  "PoseeCuentasFinanciera"
								, "ORG"."FechaConsulta" 					AS "PeriodoReporte"
								, COALESCE( "PAI"."CodigoPais",'')			AS "PaisResidencia"
								, (CASE WHEN "ORG"."TipoPersona"='N' THEN '1'
										ELSE '2' END)  						AS "TipoPersona" --*
								, (CASE WHEN "ORG"."TipoPersona"='N' THEN ''
										ELSE 'CRS102' END) 					AS "TipoJuridico"
								, (CASE WHEN "ORG"."TipoPersona"='N' THEN "PER"."Nombres"
										ELSE '' END)			  			AS "Nombres"
								, (CASE WHEN "ORG"."TipoPersona"='N' THEN "PER"."ApellidoPaterno" || ' ' || "PER"."ApellidoMaterno"
										ELSE '' END) 						AS "Apellidos"
								, (CASE WHEN "ORG"."TipoPersona"='N' THEN ''
										ELSE "PER"."RazonSocial" END)		AS "RazonSocial"
								, COALESCE( "PAI"."CodigoPais",'') 				AS "PaisTaxID"
								, COALESCE( "DOC"."DocumentoIdentidad",'') 		AS "TaxID"
								, COALESCE( "PAID"."CodigoPais",'') 			AS "PaisDomicilio"
								, 'CRS301'										AS "TipoDomicilio"
								, COALESCE( "DIR"."Calle",'') || ' ' || COALESCE("DIR"."Numero",'')  			AS "Domicilio"
								, (CASE WHEN "ORG"."TipoPersona"='N' THEN "ORG"."FechaNacimiento"
										ELSE '0001-01-01' END)  						AS "FechaNacimiento"
								, (CASE  WHEN TRIM(COALESCE("PER"."TipoDocumentoIdentidad",'')) <> 'R' THEN  REPLACE(REPLACE_REGEXPR('[@%:-]|[a-zA-Z]' IN COALESCE("PER"."DocumentoIdentidad",'') WITH '' OCCURRENCE ALL),' ','')
										 ELSE TO_BIGINT ((ltrim(rtrim(substring(COALESCE(replace("PER"."DocumentoIdentidad",'.',''),'0'),1,LENGTH(COALESCE(replace("PER"."DocumentoIdentidad",'.',''),'0'))-2))))) 
									 END 
								  )												AS "NumeroCuenta"
								, '2' 											AS "CuentaNoDocumentada"
								, (CASE WHEN "ORG"."IdEstadoCliente"='ACT' THEN '2'
										ELSE '1'  END)  						AS "CuentaCerrada"
								, COALESCE("HMON1"."ValorDestino","ORG"."IdMonedaInstrumento") 		AS "DivisaSaldo"
								, "ORG"."ValorMercado" 							AS "SaldoActualizado"
								---------------------------------------------------------------------
								, '' AS "PaisResidenciaControlador"
								, '' AS "NombreControlador"
								, '' AS "ApellidosControlador"
								, '' AS "PaisTaxIDControlador"
								, '' AS "TaxIDControlador"
								, '' AS "PaisDomicilioControlador"
								, '' AS "TipoDomicilioControlador"
								, '' AS "DomicilioControlador"
								, '0001-01-01' AS "FechaNacimientoControlador"
								, (CASE WHEN "ORG"."TipoRegistro"='CUS' THEN ''
										WHEN "ORG"."TipoRegistro"='DIV' THEN 'CRS501'
										WHEN "ORG"."TipoRegistro"='CCU' THEN 'CRS502'
										WHEN "ORG"."TipoRegistro"='DAP' THEN 'CRS502'
										WHEN "ORG"."TipoRegistro"='MRF' THEN 'CRS503'--Interes
										WHEN "ORG"."TipoRegistro"='FMM' THEN 'CRS503'
										WHEN "ORG"."TipoRegistro"='FAC' THEN 'CRS504'
										ELSE ''  END)   AS "TipoPagoCuentaFinanciera"
								---------------------------------------------------------------------
								, COALESCE("HMON2"."ValorDestino","ORG"."DivisaPagoCuentaFinanciera") 	AS "DivisaPagoCuentaFinanciera"
								, "ORG"."MontoPagoActualizado" 				AS "MontoPagoActualizado"
								, "ORG"."IdCliente"							AS "IdCliente"
								--, "ORG"."Portafolio"						AS "Portafolio"
								, "ORG"."Nemo"								AS "Nemo"
								, "ORG"."IdTipoInstrumento" 				AS "IdTipoInstrumento"
								, "ORG"."IdClasificacionTipoInstrumento"	AS "IdClasificacionTipoInstrumento"
								--, "ORG"."IdTipoProducto"					AS "IdTipoProducto"
								, "ORG"."RutFondo"							AS "RutFondo"	
	--SELECT "PRE".*,"ORG".* 
				FROM "BSCL.Normativa.BD.Vistas::TRI.DJ1958.PreOrigen" AS  "ORG" 
				
				INNER JOIN  BSCL."BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona"  AS "PER" ON ("PER"."IdPersona"="ORG"."IdPersona")
				
				LEFT JOIN ( --TAXID
							SELECT "DOCS"."IdPersona","DOCS"."DocumentoIdentidad"
							 ,RANK() OVER (PARTITION BY "DOCS"."IdPersona" ORDER BY "DOCS"."FechaEmision" DESC) as "RankNum"
				   			FROM BSCL."BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS "DOCS" 
				   			WHERE "DOCS"."IdEstado" ='ACT'  AND "DOCS"."IdTipoDocumentoIdentidad"='TAX'
						  ) AS "DOC" ON ("ORG"."IdPersona" = "DOC"."IdPersona" AND  "DOC"."RankNum"=1 )
				LEFT JOIN ( ---RESIDENCIA
							SELECT "PRES"."IdPersona","PRES"."FechaInicioTributacionPais","PRES"."IdPais"
							 ,RANK() OVER (PARTITION BY "PRES"."IdPersona" ORDER BY "PRES"."FechaInicioTributacionPais" DESC) as "RankNum"
				   			FROM BSCL."BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaPaisResidenciaFiscal" AS "PRES"
							LEFT JOIN "BSCL.Normativa.BD::DOC.Parametro" AS "PAR"	ON ("Esquema"='DJ1958' AND "Version" = '0' AND "Codigo" = 'FechaTributacionPais') 
				   			WHERE "PRES"."IdEstado" ='ACT' AND ("PRES"."FechaInicioTributacionPais"<= "PAR"."Valor") 
				   
						  ) AS "PRE" ON ("ORG"."IdPersona" = "PRE"."IdPersona" AND  "PRE"."RankNum"=1 )
				LEFT JOIN "BSCL.Plataforma.BD.Vistas::REG.ConsultarPais" 		AS "PAI" 	ON ("PAI"."IdPais" = "PRE"."IdPais")
				LEFT JOIN "BSCL.Plataforma.BD.Vistas::REG.ConsultarPais" 		AS "PAID" 	ON ("PAID"."IdPais" = "ORG"."PaisPersona")
				LEFT JOIN "BSCL"."BSCL.SocioNegocio.BD::PER.DireccionFisica" 	AS "DIR"	ON ("DIR"."IdPersona"= "ORG"."IdPersona" AND "DIR"."Borrado"='N' AND "DIR"."IdEstado"='ACT' AND "DIR"."Predeterminado"='S')
				LEFT JOIN "BSCL.Normativa.BD::DOC.Homologacion" 				AS "HMON1"  ON ("HMON1"."Esquema"='DJ1958' AND "HMON1"."Tipo"='DJ1958MONEDA' AND "HMON1"."Version"=YEAR("ORG"."FechaConsulta") AND "HMON1"."ValorOrigen"="ORG"."IdMonedaInstrumento" )
				LEFT JOIN "BSCL.Normativa.BD::DOC.Homologacion" 				AS "HMON2"  ON ("HMON2"."Esquema"='DJ1958' AND "HMON2"."Tipo"='DJ1958MONEDA' AND "HMON2"."Version"=YEAR("ORG"."FechaConsulta") AND "HMON2"."ValorOrigen"="ORG"."DivisaPagoCuentaFinanciera" )	
				

WITH read ONLY;

GRANT SELECT ON "BSCL.Normativa.BD.Vistas::TRI.DJ1958.Origen" TO bcs_desarrollador WITH GRANT OPTION;