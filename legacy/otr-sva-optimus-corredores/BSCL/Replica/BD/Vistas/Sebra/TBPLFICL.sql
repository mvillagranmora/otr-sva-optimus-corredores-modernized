SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::Sebra.TBPLFICL"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::Sebra.TBPLFICL" AS

				SELECT
					POR."IdPortafolio",
					'C' AS "TIPO_REG" , --char(1) NOT NULL,
					coalesce ("BSCL.Replica.BD.Servicios::TO_LEGACY_RUT"(CAST( IDENT."DocumentoIdentidad" AS VARCHAR(11))), '')  AS "RUT_CLI" , --char(11) NOT NULL,
					coalesce (POR."CodigoPortafolio",'-1') AS "SEC_RUT_CLI" , --decimal(2, 0) NOT NULL,
					CAST(CASE WHEN PER."Tipo" = 'N' then PER."ApellidoPaterno"||' '||PER."ApellidoMaterno"||' '||PER."Nombres" ELSE  PER."RazonSocial" END AS VARCHAR(50)) AS "NOMBRE_CLI" , --char(50) NOT NULL,
					'' AS "ALIAS_CLI" , --char(10) NOT NULL,
					'SU01' AS "COD_SUC" , --char(4) NOT NULL,
					CAST(coalesce(COD_EMP_TRUN4."CodigoEmpleado",'') AS VARCHAR(40)) AS "COD_AGENTE" , --char(4) NOT NULL,
					CASE WHEN PER."Tipo" = 'N' THEN 'PN' ELSE 'PJ' END  AS "TIPO_CLI" , --char(2) NOT NULL,
					CAST(CASE WHEN PER."IdEstado" = 'BLO' then 'B' ELSE CASE WHEN POR."IdEstado" = 'ACT' then 'V' ELSE 'B' END END AS VARCHAR(2))  AS "ESTADO_CLI" , --char(1) NOT NULL,
					'N' AS "IND_ACTUAC_PRO_CORR" , --char(1) NOT NULL, --REG_ID = 'APCOR'
					LEFT(COALESCE(IND_ADM_CARTERA."ValorIndicador", 'N'),1) AS "IND_ADM_CARTERA" , --char(1) NOT NULL,
					'N' AS "IND_COBRO_SEP" , --char(1) NOT NULL, * --REG_ID = 'COGAS'
					'N' AS "IND_EJE_SIN_SEC" , --char(1) NOT NULL,  --REG_ID = 'EJSSE'
					'N' AS "IND_EMI_CHEQUE" , --char(1) NOT NULL, * --REG_ID = 'EMCHE'
					LEFT(COALESCE(IND_ENV_FACTURA."ValorIndicador", 'E'),1) AS "IND_ENV_FACTURA" , --char(1) NOT NULL, E-R-D 
					'N' AS "IND_FACTURA_PRE" , --char(1) NOT NULL, S
					'N' AS "IND_FACUL_CORR" , --char(1) NOT NULL, S --REG_ID = 'PACOR'
					'N' AS "IND_ORDEN_SOL" , --char(1) NOT NULL, E-J-0-S S --REG_ID = 'PACOB'
					'S' AS "IND_SEP_FACTURA_CV" , --char(1) NOT NULL,
					'N' AS "IND_SEP_FACTURA_INST" , --char(1) NOT NULL,  N
					COALESCE(IND_TIT_CUST."ValorIndicador", 'N') AS "IND_TIT_CUST" , --char(1) NOT NULL,
					LEFT(PER."Cargo", 30) AS "CARGO" , --char(20) NOT NULL,
					'' AS "CASILLA_POSTAL" , --char(15) NOT NULL,
					COALESCE(tb5."CIUDAD_COMER",COALESCE( tb5."CIUDAD_PART",'')) AS "CIUDAD_COMER" , --char(4) NOT NULL,
					COALESCE(COALESCE(CIU."COD_TRAD", CAST(CIU."IdCiudad" AS VARCHAR(4))), COALESCE( tb5."CIUDAD_COMER",'')) AS "CIUDAD_EMPLEA" , --char(4) NOT NULL,
					COALESCE(tb5."CIUDAD_PART",COALESCE( tb5."CIUDAD_COMER",'')) AS "CIUDAD_PART" , --char(4) NOT NULL,
					'' AS "CIUDAD_POSTAL" , --char(4) NOT NULL,
					
					COALESCE (CAST(tb2."COD_BANCO1" AS VARCHAR(4)), '') AS "COD_BCO_CLI_01" , --char(4) NOT NULL,
					COALESCE (CAST(tb2."COD_BANCO2" AS VARCHAR(4)), '') AS "COD_BCO_CLI_02" , --char(4) NOT NULL,
					COALESCE (CAST(tb2."COD_BANCO3" AS VARCHAR(4)), '') AS "COD_BCO_CLI_03" , --char(4) NOT NULL,
					COALESCE (CAST(tb2."COD_BANCO4" AS VARCHAR(4)), '') AS "COD_BCO_CLI_04" , --char(4) NOT NULL,
					'' AS "COD_BCO_CLI_05" , --char(4) NOT NULL,
					
					'' AS "COD_CARGO_6869" , --char(2) NOT NULL,
					'' AS "COD_CARGO_SVS" , --char(2) NOT NULL,  
					COALESCE(tb5."COMUNA_COMER", COALESCE(tb5."COMUNA_PART", '')) AS "COMUNA_COMER" , --char(4) NOT NULL,
					COALESCE(COALESCE(DIR_SITRADEMP."COD_TRAD", CAST(DIREMP."IdCiudad" AS VARCHAR(4))),COALESCE(tb5."COMUNA_COMER", '')) AS "COMUNA_EMPLEA" , --char(4) NOT NULL,  
					COALESCE(tb5."COMUNA_PART", COALESCE(tb5."COMUNA_COMER", '')) AS "COMUNA_PART" , --char(4) NOT NULL,
					
					'' AS "CORREO_POSTAL" , --char(15) NOT NULL,
					
					LEFT(coalesce (tb2."CTA_BCO1", ''),20) AS "CTA_BCO_CLI_01" , --char(20) NOT NULL,
					LEFT(coalesce (tb2."CTA_BCO2", ''),20) AS "CTA_BCO_CLI_02" , --char(20) NOT NULL,
					LEFT(coalesce (tb2."CTA_BCO3", ''),20) AS "CTA_BCO_CLI_03" , --char(20) NOT NULL,
					LEFT(coalesce (tb2."CTA_BCO4", ''),20) AS "CTA_BCO_CLI_04" , --char(20) NOT NULL,
					'' AS "CTA_BCO_CLI_05" , --char(20) NOT NULL,
					
					LEFT(COALESCE(tb5."CODP_COME",COALESCE(tb5."CODP_PART",'')),40) AS "DIRECCION_COMER" , --char(40) NOT NULL,
					LEFT(COALESCE(concat(DIREMP."Calle", concat(' ',  DIREMP."Numero")),COALESCE(tb5."CODP_COME",'')),40) AS "DIRECCION_EMPLEA" , --char(40) NOT NULL,  
					LEFT(COALESCE(tb5."CODP_PART",COALESCE(tb5."CODP_COME",'')),40) AS "DIRECCION_PART" , --char(40) NOT NULL,
					
					'' AS "DIRIGIRSE_A" , --char(20) NOT NULL,
					
					CAST(PER."Empleador" AS VARCHAR(100)) AS "EMPLEADOR" , --char(50) NOT NULL,

					CAST(CASE WHEN PER."EstadoCivil" = 'CA' then 'C'--'ECIVICASAD'
							 WHEN PER."EstadoCivil" = 'DI' then 'P'--'ECIVIDIVOR'
							 WHEN PER."EstadoCivil" = 'SE' then 'P'--'ECIVISEPAR'
							 WHEN PER."EstadoCivil" = 'SO' then 'S'--'ECIVISOLTE'
							 WHEN PER."EstadoCivil" = 'VI' then 'V'--'ECIVIVIUDO'
							 ELSE 
								CASE WHEN PER."Tipo" = 'J' THEN 'S' ELSE'' END 
							 END AS VARCHAR(10)) AS "ESTADO_CIVIL" , --char(1) NOT NULL,
					
					'' AS "FAX_COMER_01" , --char(20) NOT NULL,
					'' AS "FAX_COMER_02" , --char(20) NOT NULL,
					'' AS "FAX_COMER_03" , --char(20) NOT NULL,
					'' AS "FAX_PART_01" , --char(20) NOT NULL,
					'' AS "FAX_PART_02" , --char(20) NOT NULL,
					'' AS "FAX_PART_03" , --char(20) NOT NULL,
					
					TO_VARCHAR (CASE WHEN PER."FechaIngreso" < '1900-01-01' THEN '1900-01-01' ELSE PER."FechaIngreso" end ,'YYYY-MM-DD') AS "FEC_ING" , 
					
					COALESCE(LEFT(CASE WHEN PER."FechaNacimiento" < '1900-01-01' THEN '1900-01-01' ELSE PER."FechaNacimiento" END,10),'1900-01-01') AS "FEC_NACIM" , --char(10) NOT NULL, 
					
					'1900-01-01' AS "FEC_ULT_CARTOLA" , --char(10) NOT NULL,
					'1900-01-01' AS "FEC_ULT_MVTO" , --char(10) NOT NULL,
					 
					'' AS "FONO_COMER_01" , --char(20) NOT NULL,
					'' AS "FONO_COMER_02" , --char(20) NOT NULL,
					'' AS "FONO_COMER_03" , --char(20) NOT NULL,
					
					LEFT(coalesce (TELEMP."Numero", ''),20) AS "FONO_EMPLEA" , --char(20) NOT NULL, 
					
					LEFT(coalesce (tb4."FONO_PART_01", ''),20) AS "FONO_PART_01" , --char(20) NOT NULL, 
					LEFT(coalesce (tb4."FONO_PART_02", ''),20) AS "FONO_PART_02" , --char(20) NOT NULL,
					LEFT(coalesce (tb4."FONO_PART_03", ''),20) AS "FONO_PART_03" , --char(20) NOT NULL,
					
					LEFT(coalesce (CAST(UPPER(GIR."Nombre") AS VARCHAR),''),30) AS "GIRO_COMER" , --char(30) NOT NULL,--   JK nombre del giro
					
					'' AS "IND_COD_BCO" , --char(1) NOT NULL, N-1-2-3-4
					COALESCE(CASE 
						WHEN DIRPOR."CodigoProposito" = 'PART' THEN 'P'
						WHEN DIRPOR."CodigoProposito" = 'COMER' THEN 'C'
					ELSE '' END ,'') AS "IND_CORRESP",  --char(1) NOT NULL, C-P REG_ID = 'ENCOR'
					'N' AS "IND_DEP_FACTURA" , --char(1) NOT NULL,
					COALESCE(IND_DER_PRIV."ValorIndicador", 'N') AS "IND_DER_PRIV" , --char(1) NOT NULL,
					'' AS "IND_ENV_FAX" , --char(1) NOT NULL, REG_ID = 'PRELI'
					'N' AS "IND_ENV_INF" , --char(1) NOT NULL,
					COALESCE(IND_ENV_MVTO."ValorIndicador", 'N') AS "IND_ENV_MVTO" , --char(1) NOT NULL,
					'N' AS "IND_ENV_TRIP" , --char(1) NOT NULL, REG_ID = 'ENTRI'
					'' AS "IND_FICHA_CLI" , --char(1) NOT NULL,  -- 
					'' AS "IND_GASTO_FAC" , --char(1) NOT NULL, S
					COALESCE(IND_HAB_US."ValorIndicador", 'N') AS "IND_HAB_US" , --char(1) NOT NULL, N
					'N' AS "IND_INF_PRIV" , --char(1) NOT NULL,
					'' AS "IND_LIBRE_01" , --char(1) NOT NULL,
					COALESCE(IND_LIBRE_02."ValorIndicador", '') AS "IND_LIBRE_02" , --char(1) NOT NULL,
					COALESCE(IND_LIBRE_03."ValorIndicador", '') AS "IND_LIBRE_03" , --char(1) NOT NULL,
					COALESCE(IND_LIBRE_04."ValorIndicador", '') AS "IND_LIBRE_04" , --char(1) NOT NULL,
					COALESCE(IND_LIBRE_05."ValorIndicador", '') AS "IND_LIBRE_05" , --char(1) NOT NULL,
					'' AS "IND_LIBRE_06" , --char(1) NOT NULL, REG_ID = 'COAGT'
					'' AS "IND_LIBRE_07" , --char(1) NOT NULL,
					COALESCE(IND_LIBRE_08."ValorIndicador", '') AS "IND_LIBRE_08" , --char(1) NOT NULL,
					COALESCE(IND_LIBRE_09."ValorIndicador", '') AS "IND_LIBRE_09" , --char(1) NOT NULL,
					COALESCE(IND_LIBRE_10."ValorIndicador", '') AS "IND_LIBRE_10" , --char(1) NOT NULL,
					'N' AS "IND_MULTICORR" , --char(1) NOT NULL, REG_ID = 'MULCO'
					'N' AS "IND_NAC" , --char(1) NOT NULL, 
					'N' AS "IND_NOM_ADM" , --char(1) NOT NULL, REG_ID = 'NOADM'
					'' AS "IND_PAGO_COBRO" , --char(1) NOT NULL, C-D-V REG_ID = 'PACOB'
					coalesce (CASE 
						 WHEN i_REL."CodigoValorIndicador" = 'ADMINISTRACION' THEN 'A'
						 WHEN i_REL."CodigoValorIndicador" = 'LABORAL' THEN 'L'
						 WHEN i_REL."CodigoValorIndicador" = 'PROPIEDAD' THEN 'P' 
						 WHEN i_REL."CodigoValorIndicador" = 'NOVINCULADO' THEN 'N'
						 ELSE 'N' END, '') AS "IND_REL_CORR" , --char(1) NOT NULL,   
					COALESCE( CAST(DIR_ELEC."Direccion" AS VARCHAR(50)),'') AS "INTERNET" , --char(50) NOT NULL,
					0.0 AS "MONTO_COMIS_MIN" , --decimal(15, 4) NOT NULL,
					0.0 AS "MONTO_MAX_SGIRO" , --decimal(15, 4) NOT NULL,
					0.0 AS "MONTO_TOL_MAX_SGIRO" , --decimal(15, 4) NOT NULL,
					COALESCE(left(CONJU."ApellidoPaterno"||' '||CONJU."ApellidoMaterno"||' '||CONJU."Nombres",50),'') AS "NOMBRE_CONYUGE" , --char(50) NOT NULL, -- 
					
					LEFT(COALESCE(FANT."DescripcionNombreFantasia",''),30) AS "NOMBRE_FANTAS" , --char(30) NOT NULL,

					PER."IdPersona" AS "NUM_FICHA" , --decimal(8, 0) NOT NULL, 
					0.0 AS "NUM_ULT_CARTOLA" , --decimal(8, 0) NOT NULL,
					
					COALESCE(tb5."PAIS_COMER", COALESCE(tb5."PAIS_PART", '')) AS "PAIS_COMER" , --char(4) NOT NULL,--
					COALESCE(tb5."PAIS_PART", COALESCE(tb5."PAIS_COMER", '')) AS "PAIS_PART" , --char(4) NOT NULL,-- 
					
					
					'' AS "PAIS_POSTAL" , --char(4) NOT NULL,
					
					COALESCE (CAST(PF_SITRAD."COD_TRAD" AS VARCHAR(4)), '') AS "PROFESION" , --char(4) NOT NULL,
					
					CAST(COALESCE(tb5."REG_COMER", COALESCE (tb5."REG_PART", '0')) AS VARCHAR(4)) AS "REG_COMER" , --char(2) NOT NULL, 
					CAST(COALESCE(DIVPAEMP."IdClasificacion1", COALESCE (tb5."REG_COMER", '0')) AS VARCHAR(4)) AS "REG_EMPLEA" , --char(2) NOT NULL,
					CAST(COALESCE(tb5."REG_PART", COALESCE (tb5."REG_COMER", '0')) AS VARCHAR(4)) AS "REG_PART" , --char(2) NOT NULL,
					
					COALESCE (CASE PER."RegimenConyugal" 
						WHEN 'PG' THEN 'G'
						WHEN 'SB' THEN 'B'
						WHEN 'SC' THEN 'C'
						WHEN 'SR' THEN 'S'
					ELSE '' END, '') AS "REGIMEN_CONYUGAL" , --char(1) NOT NULL,  
					
					coalesce ("BSCL.Replica.BD.Servicios::TO_LEGACY_RUT"(CAST(IDENTCONJU."DocumentoIdentidad" AS VARCHAR(11))), '') AS "RUT_CONYUGE" , --char(11) NOT NULL, 
					PER."Sexo" AS "SEXO" , --char(1) NOT NULL,--
					
					COALESCE(tb2."SUC_BCO1", '') AS "SUC_BCO_CLI_01" , --char(20) NOT NULL,
					COALESCE(tb2."SUC_BCO2", '') AS "SUC_BCO_CLI_02" , --char(20) NOT NULL,
					COALESCE(tb2."SUC_BCO3", '') AS "SUC_BCO_CLI_03" , --char(20) NOT NULL,
					COALESCE(tb2."SUC_BCO4", '') AS "SUC_BCO_CLI_04" , --char(20) NOT NULL,
					'' AS "SUC_BCO_CLI_05" , --char(20) NOT NULL,
					
					coalesce (CASE  tb2."TIPO_CTA_BCO_01" WHEN  'CUCO'  THEN 'CC' WHEN 'CUVI' THEN 'AV' WHEN 'CUAH' THEN 'AH' ELSE '' END ,'') AS "TIPO_CTA_BCO_01" , --char(2) NOT NULL, JK falta homologar
					coalesce (CASE  tb2."TIPO_CTA_BCO_02" WHEN  'CUCO'  THEN 'CC' WHEN 'CUVI' THEN 'AV' WHEN 'CUAH' THEN 'AH' ELSE '' END ,'') AS "TIPO_CTA_BCO_02" , --char(2) NOT NULL,JK falta homologar
					coalesce (CASE  tb2."TIPO_CTA_BCO_03" WHEN  'CUCO'  THEN 'CC' WHEN 'CUVI' THEN 'AV' WHEN 'CUAH' THEN 'AH' ELSE '' END ,'') AS "TIPO_CTA_BCO_03" , --char(2) NOT NULL,JK falta homologar
					coalesce (CASE  tb2."TIPO_CTA_BCO_04" WHEN  'CUCO'  THEN 'CC' WHEN 'CUVI' THEN 'AV' WHEN 'CUAH' THEN 'AH' ELSE '' END ,'') AS "TIPO_CTA_BCO_04" , --char(2) NOT NULL,JK falta homologar
					'' AS "TIPO_CTA_BCO_05" , --char(2) NOT NULL,
					
					'' AS "TIPO_MON_COMIS_MIN" , --char(2) NOT NULL,
					'' AS "TIPO_MON_SGIRO" , --char(2) NOT NULL,
					CASE i_PERIOD."NombreValorIndicador"
						WHEN 'ANUAL' THEN 'A '
						WHEN 'MENSUAL' THEN 'M '
						WHEN 'TRIMESTRAL' THEN 'T '
						WHEN 'SEMESTRAL' THEN 'S '
						ELSE '  ' 
					END AS "TIPO_PER_CARTOLA" , --char(2) NOT NULL,
					CASE PER."Tipo" 
						WHEN 'N' THEN 'NA'
						WHEN 'J' THEN 'JU'
						ELSE '' 
					END AS "TIPO_SVS" , --char(2) NOT NULL,
					'N' AS "INDICADORES" , --char(50) NOT NULL,
					'' AS "COD_REF" , --char(4) NOT NULL,
					COALESCE(CAST(LEFT(DCV."IdTipoCuentaCustodia", 1) AS VARCHAR(1)), '') AS "TIPO_CUENTA_DCV" , --char(1) NOT NULL,
					--COALESCE(CAST(CASE DCV."Codigo" WHEN '' THEN '0' ELSE DCV."Codigo" END AS DECIMAL(8,0)), 0.0) AS "CUENTA_DCV" , --decimal(8, 0) NOT NULL,
					
					COALESCE(CAST(
					
					CASE 
						WHEN COALESCE(DCV."Codigo",'0') = '0' THEN '0'
						WHEN TRIM(DCV."Codigo") = '' THEN '0'
						ELSE
							CASE
								WHEN "BSCL.Replica.BD.Servicios::func_IsNumeric"(DCV."Codigo") = 0 THEN '0'
								ELSE DCV."Codigo"
							END
					END
					AS DECIMAL(8,0)), 0.0) AS "CUENTA_DCV" , --decimal(8, 0) NOT NULL,
					
					
					coalesce ("BSCL.Replica.BD.Servicios::TO_LEGACY_RUT"(CAST(IDENTEMP."DocumentoIdentidad" AS VARCHAR(11))), '') AS "RUT_EMPLEA" , --char(11) NOT NULL,
					
					'' AS "WEB_EMPLEA" , --char(50) NOT NULL,
					'' AS "COD_AGENTE_R" , --char(4) NOT NULL,
					'' AS "COD_SUC_R" , --char(4) NOT NULL,
					'N' AS "IND_PRIVADO" , --char(1) NOT NULL,
					'' AS "INDICADOR" , --char(50) NOT NULL,
					0.00 AS "FOLIOCONDGENERALESRF" , --numeric(10, 0) NOT NULL,
					'1900-01-01 00:00:00' AS "FECHACONDGENERALESRF"  --datetime NOT NULL,

				FROM "BSCL.SocioNegocio.BD::PER.Persona" as PER
				INNER JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as IDENT ON PER."IdDocumentoIdentidad" = IDENT."IdDocumentoIdentidad"
				INNER JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" as CLI ON CLI."IdPersona" = PER."IdPersona"
				INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio"  as POR ON CLI."IdCliente" = POR."IdCliente"
				LEFT JOIN "BSCL.SocioNegocio.BD::PER.DireccionFisica" AS DIRPOR ON DIRPOR."IdDireccionFisica" = POR."IdDireccionEnvioCorrespondencia"
				LEFT JOIN (
					SELECT "IdPortafolio","IdTipoCuentaCustodia","Codigo" ,
					row_number() over (partition by "IdPortafolio" order by "IdTipoCuentaCustodia","Codigo") as r
					FROM "BSCL.Portafolio.BD::POR.CuentaCustodioPortafolio" 
					WHERE "IdEstado" = 'ACT' AND "IdBoveda" = 'DCV' ) as DCV
					 ON (POR."IdPortafolio" = DCV."IdPortafolio") and r = 1
				--INDICADORES select ind_cam_homolog, reg_id, * from INDICADOR where ind_tab_homolog = 'TBPLFICL' order by ind_cam_homolog
				LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente" i_REL ON i_REL."CodigoTipoIndicador" = '1064PROP' AND i_REL."IdCliente" = CLI."IdCliente"
				LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPortafolio" i_PERIOD ON i_PERIOD."CodigoTipoIndicador" = 'CAR_PERIOD' AND i_PERIOD."IdPortafolio" = POR."IdPortafolio"
				LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPortafolio" IND_TIT_CUST ON IND_TIT_CUST."CodigoTipoIndicador" = 'ORDOCUSTRV' AND IND_TIT_CUST."IdPortafolio" = POR."IdPortafolio"
				LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPortafolio" IND_ENV_FACTURA ON IND_ENV_FACTURA."CodigoTipoIndicador" = 'FAC_ENVFEM' AND IND_ENV_FACTURA."IdPortafolio" = POR."IdPortafolio"
				LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPortafolio" IND_ADM_CARTERA ON IND_ADM_CARTERA."CodigoTipoIndicador" = 'ADMCART' AND IND_ADM_CARTERA."IdPortafolio" = POR."IdPortafolio"
				LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente" IND_DER_PRIV ON IND_DER_PRIV."CodigoTipoIndicador" = 'DERPRIV' AND IND_DER_PRIV."IdCliente" = CLI."IdCliente"
				LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente" IND_ENV_MVTO ON IND_ENV_MVTO."CodigoTipoIndicador" = 'CC' AND IND_ENV_MVTO."IdCliente" = CLI."IdCliente"
				LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente" IND_HAB_US ON IND_HAB_US."CodigoTipoIndicador" = 'ESHABITUAL' AND IND_HAB_US."IdCliente" = CLI."IdCliente"
				LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPortafolio" IND_LIBRE_02 ON IND_LIBRE_02."CodigoTipoIndicador" = 'TES_NIE' AND IND_LIBRE_02."IdPortafolio" = POR."IdPortafolio"
				LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPortafolio" IND_LIBRE_03 ON IND_LIBRE_03."CodigoTipoIndicador" = 'ORDVNNOMRV' AND IND_LIBRE_03."IdPortafolio" = POR."IdPortafolio"
				LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPortafolio" IND_LIBRE_04 ON IND_LIBRE_04."CodigoTipoIndicador" = 'GEN_SOBCTA' AND IND_LIBRE_04."IdPortafolio" = POR."IdPortafolio"
				LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente" IND_LIBRE_05 ON IND_LIBRE_05."CodigoTipoIndicador" = 'CM' AND IND_LIBRE_05."IdCliente" = CLI."IdCliente"
				LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente" IND_LIBRE_08 ON IND_LIBRE_08."CodigoTipoIndicador" = 'ANTLEGAL' AND IND_LIBRE_08."IdCliente" = CLI."IdCliente"
				LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPortafolio" IND_LIBRE_09 ON IND_LIBRE_09."CodigoTipoIndicador" = 'GEN_OCCINV' AND IND_LIBRE_09."IdPortafolio" = POR."IdPortafolio"
				LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente" IND_LIBRE_10 ON IND_LIBRE_10."CodigoTipoIndicador" = 'FOTOCOPIA' AND IND_LIBRE_10."IdCliente" = CLI."IdCliente"
				--INDICADORES 
				LEFT JOIN "BSCL.SocioNegocio.BD::EJE.Ejecutivo" as EJE ON EJE."IdEmpleadoEjecutivo" = POR."IdEmpleadoEjecutivo"
				LEFT JOIN "BSCL.SocioNegocio.BD::EMP.Empleado" as EMP ON EJE."IdEmpleadoEjecutivo" = EMP."IdEmpleado"
				LEFT JOIN (
					SELECT "IdEmpleado", CASE WHEN RANK > 1 THEN CONCAT(RPAD("CodigoEmpleado",3),RANK) ELSE "CodigoEmpleado" END AS "CodigoEmpleado" FROM (
						SELECT RANK() OVER (PARTITION BY RPAD("CodigoEmpleado",4) ORDER BY "CodigoEmpleado" ASC)  as RANK, "IdEmpleado", RPAD("CodigoEmpleado",4) AS "CodigoEmpleado" FROM "BSCL.SocioNegocio.BD::EMP.Empleado"
					) WHERE RANK < 10
				) AS COD_EMP_TRUN4 ON COD_EMP_TRUN4."IdEmpleado" = EMP."IdEmpleado"
				LEFT JOIN BSCL."BSCL.Plataforma.BD::REG.Giro" AS GIR ON GIR."IdGiro" = PER."IdGiroPrincipal"
				LEFT JOIN "BSCL.SocioNegocio.BD::PER.DireccionElectronica" AS DIR_ELEC ON DIR_ELEC."IdDireccionElectronica" = PER."IdDireccionElectronicaPrincipal"
				LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" as PEREMP ON PER."IdPersonaEmpleador" = PEREMP."IdPersona"
				LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as IDENTEMP ON PEREMP."IdDocumentoIdentidad" = IDENTEMP."IdDocumentoIdentidad"
				LEFT JOIN "BSCL.SocioNegocio.BD::PER.DireccionFisica" AS DIREMP ON DIREMP."IdDireccionFisica" = PEREMP."IdDireccionFisicaPrincipal"
				LEFT JOIN "BSCL.Plataforma.BD::REG.Ciudad" AS CIUEMP ON CIUEMP."IdCiudad" = DIREMP."IdCiudad"
				LEFT JOIN BSCL."BSCL.Sebra.BD::MIG.TBSITRAD" DIR_SITRADEMP ON DIR_SITRADEMP."TIPO_TRAD" = 'CIUD' AND UPPER(TRIM(DIR_SITRADEMP."GLOSA")) = UPPER(TRIM(CIUEMP."NombreCiudad")) 

				LEFT JOIN "BSCL.Plataforma.BD::REG.DivisionPais" DIVPAEMP ON DIVPAEMP."IdDivisionPais" = DIREMP."IdDivisionPais"
				LEFT JOIN BSCL."BSCL.Sebra.BD::MIG.TBSITRAD" COM ON COM."TIPO_TRAD" = 'COMU' AND UPPER(TRIM(COM."GLOSA")) = UPPER(TRIM(DIVPAEMP."NombreDivisionPais")) 
				LEFT JOIN (
					SELECT P."IdPais", SITRAD."COD_TRAD" FROM "BSCL.Plataforma.BD::REG.Pais" P
					LEFT JOIN BSCL."BSCL.Sebra.BD::MIG.TBSITRAD" SITRAD ON SITRAD."TIPO_TRAD" = 'PAIS' AND UPPER(TRIM(SITRAD."GLOSA")) = UPPER(TRIM(P."NombrePais")) 
					WHERE SITRAD."COD_TRAD" IS NOT NULL
				)PAIS ON PAIS."IdPais" = DIVPAEMP."IdPais"
				LEFT JOIN (
					SELECT C."IdCiudad", SITRAD."COD_TRAD" FROM "BSCL.Plataforma.BD::REG.Ciudad" C
					LEFT JOIN BSCL."BSCL.Sebra.BD::MIG.TBSITRAD" SITRAD ON SITRAD."TIPO_TRAD" = 'CIUD' AND UPPER(TRIM(SITRAD."GLOSA")) = UPPER(TRIM(C."NombreCiudad")) 
					WHERE SITRAD."COD_TRAD" IS NOT NULL
				) CIU ON CIU."IdCiudad" = DIVPAEMP."IdCiudad"

				LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" CONJU ON CONJU."IdPersona" = PER."IdPersonaConyuge"
				LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as IDENTCONJU ON CONJU."IdDocumentoIdentidad" = IDENTCONJU."IdDocumentoIdentidad"
				LEFT JOIN "BSCL.Plataforma.BD::REG.Profesion" AS PROF ON PROF."IdProfesion" = PER."IdProfesion"
				LEFT JOIN BSCL."BSCL.Sebra.BD::MIG.TBSITRAD" PF_SITRAD ON PF_SITRAD."TIPO_TRAD" = 'PROF' AND UPPER(TRIM(PF_SITRAD."GLOSA")) = UPPER(TRIM(PROF."Nombre")) 
				LEFT JOIN "BSCL.SocioNegocio.BD::PER.Telefono" AS TELEMP ON TELEMP."IdTelefono" = PEREMP."IdTelefonoPrincipal"
				LEFT JOIN "BSCL.SocioNegocio.BD::PER.NombreFantasia" FANT ON FANT."IdNombreFantasia" = PER."IdNombreFantasiaPrincipal" AND PER."IdPersona" = FANT."IdPersona"
				LEFT JOIN (
					select 
							 "IdPersona", 
							max(CASE WHEN IdRow = 1 THEN  COD_BCO_CLI  ELSE ''  END)  AS "COD_BANCO1",
							max(CASE WHEN IdRow = 1 THEN  CTA_BCO_CLI  ELSE '' END)  AS "CTA_BCO1",
							max(CASE WHEN IdRow = 1 THEN  TIPO_CTA_BCO  ELSE '' END)  AS "TIPO_CTA_BCO_01",
							max(CASE WHEN IdRow = 1 THEN  SUC_BCO_CLI  ELSE '' END)  AS "SUC_BCO1",
							max(CASE WHEN IdRow = 2 THEN  COD_BCO_CLI  ELSE ''  END)  AS "COD_BANCO2",
							max(CASE WHEN IdRow = 2 THEN  CTA_BCO_CLI  ELSE '' END)  AS "CTA_BCO2",
							max(CASE WHEN IdRow = 2 THEN  TIPO_CTA_BCO  ELSE '' END)  AS "TIPO_CTA_BCO_02",
							max(CASE WHEN IdRow = 2 THEN  SUC_BCO_CLI  ELSE '' END)  AS "SUC_BCO2",
							max(CASE WHEN IdRow = 3 THEN  COD_BCO_CLI  ELSE ''  END)  AS "COD_BANCO3",
							max(CASE WHEN IdRow = 3 THEN  CTA_BCO_CLI  ELSE '' END)  AS "CTA_BCO3",
							max(CASE WHEN IdRow = 3 THEN  TIPO_CTA_BCO  ELSE '' END)  AS "TIPO_CTA_BCO_03",
							max(CASE WHEN IdRow = 3 THEN  SUC_BCO_CLI  ELSE '' END)  AS "SUC_BCO3",
							max(CASE WHEN IdRow = 4 THEN  COD_BCO_CLI  ELSE ''  END)  AS "COD_BANCO4",
							max(CASE WHEN IdRow = 4 THEN  CTA_BCO_CLI  ELSE '' END)  AS "CTA_BCO4",
							max(CASE WHEN IdRow = 4 THEN  TIPO_CTA_BCO  ELSE '' END)  AS "TIPO_CTA_BCO_04",
							max(CASE WHEN IdRow = 4 THEN  SUC_BCO_CLI  ELSE '' END)  AS "SUC_BCO4"
					 FROM (
						SELECT   CTAB."IdPersona",
							RANK() OVER (PARTITION BY CTAB."IdPersona" ORDER BY CTAB."IdCuentaBancaria" ASC)  as IdRow, 
							 BAN."CodigoBanco" as COD_BCO_CLI , CTAB."NumeroCuentaBancaria" as CTA_BCO_CLI, CTAB."IdTipoCuentaBancaria" as TIPO_CTA_BCO	, COALESCE(SUC."CodigoSucursal",'') AS SUC_BCO_CLI	
						FROM "BSCL.SocioNegocio.BD::PER.CuentaBancaria"  as CTAB
						INNER JOIN BSCL."BSCL.Plataforma.BD::BAN.Banco" AS BAN ON CTAB."IdBanco" = BAN."IdBanco"
						LEFT JOIN BSCL."BSCL.Tesoreria.BD::TES.SucursalesBancarias" SUC_BAN ON SUC_BAN."IdBanco" = BAN."IdBanco"
						LEFT JOIN "BSCL.Plataforma.BD::PLA.Sucursal" SUC ON SUC_BAN."IdSucursal" = SUC."IdSucursal"
						ORDER BY 1
					) as tb1
						where IdRow < 5
						group by  "IdPersona"
					) as tb2 on (PER."IdPersona" = tb2."IdPersona")

				LEFT JOIN 
					(
					select 
							 "IdPersona", 
							max(CASE WHEN IdRow = 1 THEN  FONO_PART  ELSE ''  END)   AS "FONO_PART_01",
							max(CASE WHEN IdRow = 2 THEN  FONO_PART  ELSE '' END)   AS "FONO_PART_02",
							max(CASE WHEN IdRow = 3 THEN  FONO_PART  ELSE ''  END)   AS "FONO_PART_03"
					from (
						SELECT   "IdPersona",
							RANK() OVER (PARTITION BY "IdPersona" ORDER BY "IdTelefono" ASC)  as IdRow, 
							 "Numero" as FONO_PART 			
						FROM "BSCL.SocioNegocio.BD::PER.Telefono"  as TEL
							
								
					) as tb3
						where IdRow < 4
						group by  "IdPersona"
					) as tb4 on (PER."IdPersona" = tb4."IdPersona")

				LEFT JOIN (
					select 
							"IdPersona", 
							max(CASE WHEN "CodigoProposito" IN ('COME','LABO') THEN concat(DF."Calle", concat(' ', DF."Numero"))  ELSE NULL  END)   AS "CODP_COME",
							max(CASE WHEN "CodigoProposito" IN ('COME','LABO') THEN COALESCE(COM_SITRAD."COD_TRAD", RIGHT(CONCAT('0000',CAST(DP."IdCiudad" AS VARCHAR(4))),4))  ELSE NULL  END)   AS "COMUNA_COMER",--  
							max(CASE WHEN "CodigoProposito" IN ('COME','LABO') THEN COALESCE(C_SITRAD."COD_TRAD", RIGHT(CONCAT('0000',CAST(DP."IdCiudad" AS VARCHAR(4))),4))  ELSE NULL  END)   AS "CIUDAD_COMER",-- 
							max(CASE WHEN "CodigoProposito" IN ('COME','LABO') THEN DP."IdClasificacion1" ELSE NULL END) AS "REG_COMER",
							max(CASE WHEN "CodigoProposito" IN ('COME','LABO') THEN COALESCE(P_SITRAD."COD_TRAD", RIGHT(CONCAT('0000',CAST(DP."IdPais" AS VARCHAR(4))),4))  ELSE NULL END) AS "PAIS_COMER",
								
							max(CASE WHEN "CodigoProposito" = 'PART' THEN concat(DF."Calle", concat(' ', DF."Numero"))  ELSE NULL  END)   AS "CODP_PART",
							max(CASE WHEN "CodigoProposito" = 'PART' THEN COALESCE(COM_SITRAD."COD_TRAD", RIGHT(CONCAT('0000',CAST(DP."IdCiudad" AS VARCHAR(4))),4))  ELSE NULL  END)   AS "COMUNA_PART", --
							max(CASE WHEN "CodigoProposito" = 'PART' THEN COALESCE(C_SITRAD."COD_TRAD", RIGHT(CONCAT('0000',CAST(DP."IdCiudad" AS VARCHAR(4))),4))  ELSE NULL  END)   AS "CIUDAD_PART",
							max(CASE WHEN "CodigoProposito" = 'PART' THEN DP."IdClasificacion1"  ELSE NULL  END) AS "REG_PART",
							max(CASE WHEN "CodigoProposito" = 'PART' THEN COALESCE(P_SITRAD."COD_TRAD", RIGHT(CONCAT('0000',CAST(DP."IdPais" AS VARCHAR(4))),4))  ELSE NULL END) AS "PAIS_PART"
						
						FROM "BSCL.SocioNegocio.BD::PER.DireccionFisica"  as DF
						LEFT JOIN "BSCL.Plataforma.BD::REG.DivisionPais" AS DP ON DP."IdDivisionPais" = DF."IdDivisionPais"
						LEFT JOIN "BSCL.Plataforma.BD::REG.Ciudad" C ON DP."IdCiudad" = C."IdCiudad"
						LEFT JOIN BSCL."BSCL.Sebra.BD::MIG.TBSITRAD" C_SITRAD ON C_SITRAD."TIPO_TRAD" = 'CIUD' AND UPPER(TRIM(C_SITRAD."GLOSA")) = UPPER(TRIM(C."NombreCiudad"))
						LEFT JOIN BSCL."BSCL.Sebra.BD::MIG.TBSITRAD" COM_SITRAD ON COM_SITRAD."TIPO_TRAD" = 'COMU' AND UPPER(TRIM(COM_SITRAD."GLOSA")) = UPPER(TRIM(C."NombreCiudad"))
						LEFT JOIN "BSCL.Plataforma.BD::REG.Pais" P ON DP."IdPais" = P."IdPais"
						LEFT JOIN BSCL."BSCL.Sebra.BD::MIG.TBSITRAD" P_SITRAD ON P_SITRAD."TIPO_TRAD" = 'PAIS' AND UPPER(TRIM(P_SITRAD."GLOSA")) = UPPER(TRIM(P."NombrePais"))
						group by  "IdPersona"
					) as tb5 on (PER."IdPersona" = tb5."IdPersona")
						
				WHERE  "BSCL.Replica.BD.Servicios::func_IsNumeric"(POR."CodigoPortafolio") != 0					


WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::Sebra.TBPLFICL" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
