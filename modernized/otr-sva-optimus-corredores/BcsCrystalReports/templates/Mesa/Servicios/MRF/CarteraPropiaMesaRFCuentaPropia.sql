SET SCHEMA "BSCL";
GO
DROP PROCEDURE "BSCL.Mesa.CrystalReports.Servicios::MRF.CarteraPropiaMesaRFCuentaPropia";
GO
CREATE PROCEDURE
--  Proposito: Informe cartera Cuenta Propia en Garantia mesa RF 
--  Autor: projo
--  Fecha Creacion: 04/07/2016
"BSCL.Mesa.CrystalReports.Servicios::MRF.CarteraPropiaMesaRFCuentaPropia" ( 
    --VARIABLES OBLIGATORIAS
     IN p_IdEmpresa          	          	BIGINT      -- identificador de la empresa de la operacion
    ,IN p_IdUsuario	                        BIGINT      -- usuario que ingresa la operacion
    ,IN p_IdModulo                          NVARCHAR(3) -- modulo de la operacion 
	,IN p_Fecha								DATE		--fecha de consulta
	,IN p_FechaValorizacion					DATE		--fecha valorización / proximo by default
	,IN p_TipoValorizacion					NVARCHAR(1)
	,IN p_IdCliente							INT
	,IN p_IdPortafolio						INT
	,IN p_IdSocioNegocio                    INT
	,IN p_TipoReporte						NVARCHAR(10) 
	,IN p_IdTipoValorizacion                NVARCHAR(6) DEFAULT ''
)
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
AS
BEGIN
	BEGIN
		DECLARE v_FechaSistema	NVARCHAR(10);
		DECLARE v_Out_Error		NVARCHAR(500);
   		DECLARE v_Out_Indicadores TABLE ("IdIndicador" NVARCHAR(3)
   										, "Fecha" DATE
   										, "TipoCambio" DECIMAL(26,8)
   										, "Paridad" DECIMAL(26,8)
   										, "FechaValor" DATE
   										, "Fuente" NVARCHAR(10));
   		DECLARE v_logo TABLE("LogoCorredora" BLOB);
   		DECLARE v_count INT;
   		DECLARE v_IdPortafolio  INT;
   		DECLARE v_IdCliente     INT;
		DECLARE v_todosPortafolio  NVARCHAR(1) default 'N';
   		
		BEGIN
			CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaSistema" (:p_IdEmpresa,v_FechaSistema);
			
			v_logo =	SELECT TOP 1
						"Binario" AS "LogoCorredora"
						FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarImagen"
						WHERE "NombreImagen" = 'LOGO_CORREDOR' ORDER BY "Version" DESC;
						
						
			IF p_IdPortafolio = 0 THEN
				v_todosPortafolio = 'S';
			END IF;

			IF p_IdSocioNegocio != 0 THEN 
				SELECT "IdCliente" INTO v_IdCliente
				FROM "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio"
	 			WHERE "IdSocioDeNegocio"=p_IdSocioNegocio;
	 	    ELSE
	 	    	SELECT DISTINCT Mesa."IdCliente" INTO v_IdCliente
	 	        FROM "BSCL.Mesa.BD::MES.Mesa" AS Mesa
	 	        INNER JOIN "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" AS SocNeg ON SocNeg."IdCliente" =  Mesa."IdCliente"
	 	        WHERE 
				 	Mesa."IdTipoProducto" = 'MERF'
	 	        	AND Mesa."IdEmpresa" = :p_IdEmpresa;	 	        
	 	    END IF;

				v_Cliente = SELECT "NombreCompleto" as "NombreCliente"
								,"DocumentoIdentidad"
								,"CodigoPortafolio"
								,"DescripcionPortafolio"
								,"CodigoPropositoPortafolio" 
						FROM "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" cli
							INNER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" por 
								ON (cli."IdCliente" = por."IdCliente" and cli."IdCliente" = v_IdCliente and (por."IdPortafolio" = p_IdPortafolio OR 0 = p_IdPortafolio));
						
			v_Indicadores =  Select "IdParametroFinanciero" as "IdIndicador", "Fecha",
								"Valor" as "TipoCambio" FROM "BSCL.Plataforma.BD::MER.ParametroFinanciero" Where "IdParametroFinanciero" = 'DO' AND "Fecha" = TO_VARCHAR(:p_Fecha,'YYYY-MM-DD')
							UNION ALL
							 Select "IdParametroFinanciero" as "IdIndicador", "Fecha",
								"Valor" as "TipoCambio" FROM "BSCL.Plataforma.BD::MER.ParametroFinanciero" Where "IdParametroFinanciero" = 'UF' AND "Fecha" = TO_VARCHAR(:p_Fecha,'YYYY-MM-DD')
							UNION ALL
							 Select "IdParametroFinanciero" as "IdIndicador", "Fecha",
								"Valor" as "TipoCambio" FROM "BSCL.Plataforma.BD::MER.ParametroFinanciero" Where "IdParametroFinanciero" = 'IVP' AND "Fecha" = TO_VARCHAR(:p_Fecha,'YYYY-MM-DD');
							
			v_IndicadoresProximo =  Select "IdParametroFinanciero" as "IdIndicador", "Fecha",
										"Valor" as "TipoCambio" FROM "BSCL.Plataforma.BD::MER.ParametroFinanciero" Where "IdParametroFinanciero" = 'DO' AND "Fecha" = TO_VARCHAR(:p_FechaValorizacion,'YYYY-MM-DD')
									UNION ALL
									Select "IdParametroFinanciero" as "IdIndicador", "Fecha",
										"Valor" as "TipoCambio" FROM "BSCL.Plataforma.BD::MER.ParametroFinanciero" Where "IdParametroFinanciero" = 'UF' AND "Fecha" = TO_VARCHAR(:p_FechaValorizacion,'YYYY-MM-DD')
									UNION ALL
									Select "IdParametroFinanciero" as "IdIndicador", "Fecha",
										"Valor" as "TipoCambio" FROM "BSCL.Plataforma.BD::MER.ParametroFinanciero" Where "IdParametroFinanciero" = 'IVP' AND "Fecha" = TO_VARCHAR(:p_FechaValorizacion,'YYYY-MM-DD');
							
			v_IndicadoresUltimoConocido = SELECT "IdParametroFinanciero" as "IdIndicador","Fecha","Valor" as "TipoCambio"
											FROM "BSCL.Plataforma.BD::MER.ParametroFinanciero"
											WHERE "IdParametroFinanciero" = 'DO'
											GROUP BY "IdParametroFinanciero","Fecha","Valor"
											HAVING "Fecha" = (SELECT MAX("Fecha") FROM "BSCL.Plataforma.BD::MER.ParametroFinanciero" WHERE "IdParametroFinanciero" = 'DO' AND "Fecha" <= :p_fecha)
											UNION ALL
											SELECT "IdParametroFinanciero" as "IdIndicador","Fecha","Valor" as "TipoCambio"
											FROM "BSCL.Plataforma.BD::MER.ParametroFinanciero" WHERE "IdParametroFinanciero" = 'UF' GROUP BY"IdParametroFinanciero","Fecha","Valor"
											HAVING "Fecha" = (SELECT MAX("Fecha") FROM "BSCL.Plataforma.BD::MER.ParametroFinanciero" WHERE "IdParametroFinanciero" = 'UF' AND "Fecha" <= :p_fecha)
											UNION ALL
											SELECT "IdParametroFinanciero" as "IdIndicador","Fecha","Valor" as "TipoCambio" FROM "BSCL.Plataforma.BD::MER.ParametroFinanciero" WHERE "IdParametroFinanciero" = 'IVP'
											GROUP BY "IdParametroFinanciero","Fecha","Valor"
											HAVING "Fecha" = (SELECT MAX("Fecha") FROM "BSCL.Plataforma.BD::MER.ParametroFinanciero" WHERE "IdParametroFinanciero" = 'IVP' AND "Fecha" <= :p_fecha);
			
			v_IndicadorDolar = SELECT 
  					     COALESCE(IND."IdIndicador", IND_ULTIMO."IdIndicador") AS "IdIndicador",
                         COALESCE(IND."Fecha", IND_ULTIMO."Fecha") AS "Fecha",
                         COALESCE(IND."TipoCambio", IND_ULTIMO."TipoCambio") AS "TipoCambio",
                         CASE WHEN COALESCE(IND."TipoCambio", IND_ULTIMO."TipoCambio",0) = 0 THEN 'No Hay Informacion' ELSE 'Tipo Cambio a la fecha' END  AS "Info"
 					 FROM :v_IndicadoresUltimoConocido IND_ULTIMO LEFT JOIN :v_Indicadores IND ON IND_ULTIMO."IdIndicador" = IND."IdIndicador"
 					 WHERE IND_ULTIMO."IdIndicador" = 'DO';
 					
			v_IndicadorUf = SELECT 
									COALESCE(IND."IdIndicador", IND_ULTIMO."IdIndicador") AS "IdIndicador",
									COALESCE(IND."Fecha", IND_ULTIMO."Fecha") AS "Fecha",
									COALESCE(IND."TipoCambio", IND_ULTIMO."TipoCambio") AS "TipoCambio",
									CASE WHEN COALESCE(IND."TipoCambio", IND_ULTIMO."TipoCambio",0) = 0 THEN 'No Hay Informacion' ELSE 'Tipo Cambio a la fecha' END  AS "Info"
								FROM :v_IndicadoresUltimoConocido IND_ULTIMO LEFT JOIN :v_Indicadores IND ON IND_ULTIMO."IdIndicador" = IND."IdIndicador"
								WHERE IND_ULTIMO."IdIndicador" = 'UF';
								
			v_IndicadorIvp = SELECT 
									COALESCE(IND."IdIndicador", IND_ULTIMO."IdIndicador") AS "IdIndicador",
									COALESCE(IND."Fecha", IND_ULTIMO."Fecha") AS "Fecha",
									COALESCE(IND."TipoCambio", IND_ULTIMO."TipoCambio") AS "TipoCambio",
									CASE WHEN COALESCE(IND."TipoCambio", IND_ULTIMO."TipoCambio",0) = 0 THEN 'No Hay Informacion' ELSE 'Tipo Cambio a la fecha' END  AS "Info"
								FROM :v_IndicadoresUltimoConocido IND_ULTIMO LEFT JOIN :v_Indicadores IND ON IND_ULTIMO."IdIndicador" = IND."IdIndicador"
								WHERE IND_ULTIMO."IdIndicador" = 'IVP';

			v_IndicadoresUltimoConocido = SELECT "IdParametroFinanciero" as "IdIndicador","Fecha","Valor" as "TipoCambio"
								FROM "BSCL.Plataforma.BD::MER.ParametroFinanciero"
								WHERE "IdParametroFinanciero" = 'DO'
								GROUP BY "IdParametroFinanciero","Fecha","Valor"
								HAVING "Fecha" = (SELECT MAX("Fecha") FROM "BSCL.Plataforma.BD::MER.ParametroFinanciero" WHERE "IdParametroFinanciero" = 'DO' AND "Fecha" <= :p_FechaValorizacion)
								UNION ALL
								SELECT "IdParametroFinanciero" as "IdIndicador","Fecha","Valor" as "TipoCambio"
								FROM "BSCL.Plataforma.BD::MER.ParametroFinanciero" WHERE "IdParametroFinanciero" = 'UF' GROUP BY"IdParametroFinanciero","Fecha","Valor"
								HAVING "Fecha" = (SELECT MAX("Fecha") FROM "BSCL.Plataforma.BD::MER.ParametroFinanciero" WHERE "IdParametroFinanciero" = 'UF' AND "Fecha" <= :p_FechaValorizacion)
								UNION ALL
							    SELECT "IdParametroFinanciero" as "IdIndicador","Fecha","Valor" as "TipoCambio" FROM "BSCL.Plataforma.BD::MER.ParametroFinanciero" WHERE "IdParametroFinanciero" = 'IVP'
								GROUP BY "IdParametroFinanciero","Fecha","Valor"
								HAVING "Fecha" = (SELECT MAX("Fecha") FROM "BSCL.Plataforma.BD::MER.ParametroFinanciero" WHERE "IdParametroFinanciero" = 'IVP' AND "Fecha" <= :p_FechaValorizacion);
 					
			v_IndicadorDolarProximo = SELECT 
											COALESCE(IND."IdIndicador", IND_ULTIMO."IdIndicador") AS "IdIndicador",
											COALESCE(IND."Fecha", IND_ULTIMO."Fecha") AS "Fecha",
											COALESCE(IND."TipoCambio", IND_ULTIMO."TipoCambio") AS "TipoCambio",
											CASE WHEN COALESCE(IND."TipoCambio", IND_ULTIMO."TipoCambio",0) = 0 THEN 'No Hay Informacion' ELSE 'Tipo Cambio a la fecha' END  AS "Info"
										FROM :v_IndicadoresUltimoConocido IND_ULTIMO LEFT JOIN :v_IndicadoresProximo IND ON IND_ULTIMO."IdIndicador" = IND."IdIndicador"
										WHERE IND_ULTIMO."IdIndicador" = 'DO';
								
			v_IndicadorUfProximo = SELECT 
										COALESCE(IND."IdIndicador", IND_ULTIMO."IdIndicador") AS "IdIndicador",
										COALESCE(IND."Fecha", IND_ULTIMO."Fecha") AS "Fecha",
										COALESCE(IND."TipoCambio", IND_ULTIMO."TipoCambio") AS "TipoCambio",
										CASE WHEN COALESCE(IND."TipoCambio", IND_ULTIMO."TipoCambio",0) = 0 THEN 'No Hay Informacion' ELSE 'Tipo Cambio a la fecha' END  AS "Info"
									FROM :v_IndicadoresUltimoConocido IND_ULTIMO LEFT JOIN :v_IndicadoresProximo IND ON IND_ULTIMO."IdIndicador" = IND."IdIndicador"
									WHERE IND_ULTIMO."IdIndicador" = 'UF';
								
			v_IndicadorIvpProximo = SELECT 
										COALESCE(IND."IdIndicador", IND_ULTIMO."IdIndicador") AS "IdIndicador",
										COALESCE(IND."Fecha", IND_ULTIMO."Fecha") AS "Fecha",
										COALESCE(IND."TipoCambio", IND_ULTIMO."TipoCambio") AS "TipoCambio",
										CASE WHEN COALESCE(IND."TipoCambio", IND_ULTIMO."TipoCambio",0) = 0 THEN 'No Hay Informacion' ELSE 'Tipo Cambio a la fecha' END  AS "Info"
									FROM :v_IndicadoresUltimoConocido IND_ULTIMO LEFT JOIN :v_IndicadoresProximo IND ON IND_ULTIMO."IdIndicador" = IND."IdIndicador"
	 					 WHERE IND_ULTIMO."IdIndicador" = 'IVP';

			
			/*select 'USD' as "IdIndicador",coalesce( p_Fecha, v_FechaSistema) as "Fecha" from dummy union all
								select 'UF' as "IdIndicador",coalesce( p_Fecha, v_FechaSistema) as "Fecha" from dummy union all
								select 'IVP' as "IdIndicador",coalesce( p_Fecha, v_FechaSistema) as "Fecha" from dummy;*/
			
			--CALL "BSCL.Mercado.BD.Servicios::ObtenerValorIndicador"('PARIDADES',:v_Indicadores,v_Out_Error,v_Out_Indicadores);
			
			v_Datos = SELECT "NombreCliente"
						,cli."DocumentoIdentidad"
						,"CodigoPortafolio"
						,"DescripcionPortafolio"
						,CASE 
					   	     WHEN v_todosPortafolio = 'S' THEN 'Todos'
					   	     ELSE "CodigoPropositoPortafolio"
					   	 END AS "CodigoPropositoPortafolio"
						,"LogoCorredora"
						,p_TipoValorizacion as "TipoValorizacion"
						,to_char(p_Fecha, 'DD-MM-YYYY') as "Fecha"
						,to_char(p_FechaValorizacion, 'DD-MM-YYYY') as "FechaProximo"
						,COALESCE(d."IdIndicador",'DO') as "IdIndicadorDolar"
						,to_char(d."Fecha", 'DD-MM-YYYY') as "FechaDolar"
						,to_char(dp."Fecha", 'DD-MM-YYYY') as "FechaDolarProximo"
						,CASE WHEN d."Info" = '' THEN 'Sin Información'
							ELSE d."Info" END  as "InfoDolar"
						,CASE WHEN dp."Info" = '' THEN 'Sin Información Dolar Próximo'
							ELSE dp."Info" END  as "InfoDolarProximo"
						,COALESCE(d."TipoCambio",0) as "TipoCambioDolar"
						,COALESCE(dp."TipoCambio",0) as "TipoCambioDolarProximo"
						,COALESCE(u."IdIndicador",'UF') as "IdIndicadorUf"
						,to_char(u."Fecha", 'DD-MM-YYYY') as "FechaUf"
						,CASE WHEN u."Info" = '' THEN 'Sin Información'
							ELSE u."Info" END as "InfoUF"
						,to_char(ufp."Fecha", 'DD-MM-YYYY') as "FechaUfProximo"
						,CASE WHEN ufp."Info" = '' THEN 'Sin Información UF Próximo'
							ELSE ufp."Info" END as "InfoUFProximo"
						,COALESCE(u."TipoCambio",0) as "TipoCambioUf"
						,COALESCE(ufp."TipoCambio",0) as "TipoCambioUfProximo"
						,COALESCE(i."IdIndicador",'IVP') as "IdIndicadorIvp"
						,to_char(i."Fecha", 'DD-MM-YYYY') as "FechaIvp"
						,CASE WHEN COALESCE(i."Info",'') = '' THEN 'Sin Información'
							ELSE i."Info" END as "InfoIVP"
						,COALESCE(i."TipoCambio",0) as "TipoCambioIvp"
						,to_char(ivpp."Fecha", 'DD-MM-YYYY') as "FechaIvpProximo"
						,CASE WHEN COALESCE(ivpp."Info",'') = '' THEN 'Sin Información IVP Próximo'
							ELSE ivpp."Info" END as "InfoIVPProximo"
						,COALESCE(ivpp."TipoCambio",0) as "TipoCambioIvpProximo"
						,usu."IdUsuario"
						,usu."CodigoUsuario"
						,usu."NombreCompleto" as "NombreUsuario"
						,:p_TipoReporte AS "TipoReporte"
						,:p_IdTipoValorizacion AS "IdTipoValorizacion"
					FROM :v_Cliente cli, "BSCL.Plataforma.BD.Vistas::ACC.ConsultarUsuarioCompleto" usu
						LEFT JOIN :v_IndicadorDolar d ON (d."IdIndicador" = 'DO')
						LEFT JOIN :v_IndicadorUf u ON (u."IdIndicador" = 'UF')
						LEFT JOIN :v_IndicadorIvp i ON (i."IdIndicador" = 'IVP')
						LEFT JOIN :v_IndicadorDolarProximo dp ON dp."IdIndicador" = 'DO'
						LEFT JOIN :v_IndicadorUfProximo ufp ON ufp."IdIndicador" = 'UF'
						LEFT JOIN :v_IndicadorIvpProximo ivpp ON ivpp."IdIndicador" = 'IVP'
						CROSS JOIN :v_logo
					WHERE  "IdUsuario" = p_IdUsuario and "IdEmpresa" = p_IdEmpresa;
			
				
			SELECT COUNT(*) INTO v_count FROM :v_Datos;
			IF (v_count = 0) THEN
				v_Datos = SELECT '' as "NombreCliente"
						,'' as "DocumentoIdentidad"
						,''	as "CodigoPortafolio"
						,'' as "DescripcionPortafolio"
						,'' as "CodigoPropositoPortafolio"
						,'' as "LogoCorredora"
						,'' as "TipoValorizacion"
						,to_char('0001-01-01', 'DD-MM-YYYY') as "Fecha"
						,to_char('0001-01-01', 'DD-MM-YYYY') as "FechaProximo"
						,'DO' as "IdIndicadorDolar"
						,to_char('0001-01-01', 'DD-MM-YYYY') as "FechaDolar"
						,to_char('0001-01-01', 'DD-MM-YYYY') as "FechaDolarProximo"
						,'Sin Informacion' as "InfoDolar"
						,'Sin Informacion Dolar Próximo' as "InfoDolarProximo"
						,0  as "TipoCambioDolar"
						,0  as "TipoCambioDolarProximo"
						,'UF' as "IdIndicadorUf"
						,to_char('0001-01-01', 'DD-MM-YYYY') as "FechaUf"
						,'Sin Informacion' as "InfoUF"
						,to_char('0001-01-01', 'DD-MM-YYYY') as "FechaUfProximo"
						,'Sin Informacion UF Próximo' as "InfoUFProximo"
						,0  as "TipoCambioUf"
						,0  as "TipoCambioUfProximo"
						,'IVP' as "IdIndicadorIvp"
						,to_char('0001-01-01', 'DD-MM-YYYY') as "FechaIvp"
						,'Sin Informacion' as "InfoIVP"
						,0  as "TipoCambioIvp"
						,to_char('0001-01-01', 'DD-MM-YYYY') as "FechaIvpProximo"
						,'Sin Informacion IVP Próximo' as "InfoIVPProximo"
						,0  as "TipoCambioIvpProximo"
						,0  as "IdUsuario"
						,'' as "CodigoUsuario"
						,'' as "NombreUsuario"
						,:p_TipoReporte AS "TipoReporte"
						,:p_IdTipoValorizacion AS "IdTipoValorizacion"
					FROM DUMMY;
			END IF;
			SELECT * FROM :v_Datos;
		END;
	END;
END;

