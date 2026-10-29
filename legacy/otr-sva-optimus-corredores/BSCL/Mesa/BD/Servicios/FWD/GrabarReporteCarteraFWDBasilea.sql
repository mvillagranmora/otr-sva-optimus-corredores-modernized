SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Mesa.BD.Servicios::FWD.GrabarReporteCarteraFWDBasilea";

--
--  Proposito: servicio para grabar reporte cartera Basilea al cierre de mesa FWD
--  Autor: APENA
--  Fecha Creacion: /02/2024
--

CREATE PROCEDURE "BSCL.Mesa.BD.Servicios::FWD.GrabarReporteCarteraFWDBasilea"(
	-- Parametros entrada
	IN  p_IdEmpresa             BIGINT,                                                                            
    IN  p_IdUsuario             BIGINT,  
	IN  p_FechaOperacion		DATE
)
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER  
DEFAULT SCHEMA "BSCL"
--READS SQL DATA
AS
BEGIN
	
	DECLARE SALIDA_CONTROLADA_NO_DATA CONDITION; 
	--------------------
	--Logica
	--------------------
		
	v_Cartera =	
		WITH "Cartera" AS (
						WITH "FactorCorrespondiente" AS (
						    SELECT
						        CanastaMoneda."IdCanasta",
						        CanastaMoneda."IdMoneda",
						        MIN(FactorCanasta_Menor."Hasta") AS "MIN",
						        MAX(FactorCanasta_Mayor."Hasta") AS "MAX"
						    FROM "BSCL.Mesa.BD.Vistas::FWD.ConsultarCanastaMoneda" CanastaMoneda
						    LEFT JOIN "BSCL.Mesa.BD.Vistas::FWD.ConsultarFactoresCanasta" FactorCanasta_Menor
						        ON (FactorCanasta_Menor."IdEmpresa" = CanastaMoneda."IdEmpresa" 
						            AND FactorCanasta_Menor."IdCanasta" = CanastaMoneda."IdCanasta") 
						    LEFT JOIN "BSCL.Mesa.BD.Vistas::FWD.ConsultarFactoresCanasta" FactorCanasta_Mayor
						        ON (FactorCanasta_Mayor."IdEmpresa" = CanastaMoneda."IdEmpresa" 
						            AND FactorCanasta_Mayor."IdCanasta" = CanastaMoneda."IdCanasta")
						    WHERE CanastaMoneda."IdEmpresa" = :p_IdEmpresa 
						    GROUP BY CanastaMoneda."IdCanasta", CanastaMoneda."IdMoneda"
						),"CanastaFiltrada" AS (
						    SELECT
						        Canasta."Factor",
						        Canasta."IdCanasta",
						        Canasta."Hasta",
						        Factor."IdMoneda",
						        RANK() OVER (PARTITION BY Canasta."IdCanasta", Factor."IdMoneda" ORDER BY Canasta."IdFactor" DESC) AS "Rank"
						    FROM "BSCL.Mesa.BD.Vistas::FWD.ConsultarFactoresCanasta" AS Canasta
						    INNER JOIN "FactorCorrespondiente" AS Factor
						    		ON (COALESCE(Factor."MIN", Factor."MAX") = Canasta."Hasta" 
						            AND Canasta."IdEmpresa" = :p_IdEmpresa 
						            AND Canasta."IdCanasta" = Factor."IdCanasta")
						)
						SELECT
						MF."IdCuentaCustodioPortafolio" 				AS "CuentaCliente"
						,MF."NombreInstrumento" 						AS "DescripcionProductoFWD"			
						,CASE WHEN MF."NombreInstrumento" = 'SEGURO DE CAMBIO REAJUSTABLE'THEN	2 ELSE 1 END 	AS "IdInstrumento"
						,MF."IdOperacion" 								AS "FolioOperacion"
						,CAR."FolioCartera"								AS "FolioCartera"
						,INITCAP(LOWER(MF."NombreInstrumento"))							AS "NombreInstrumento"
						,REPLACE( CASE WHEN COALESCE(MF."RutCliente",'') <> '' AND MF."TipoDocumentoIdentidad" = 'R' THEN
							"BSCL.Plataforma.BD.Utilitarios::DarFormatoNumero"(SUBSTR_BEFORE(MF."RutCliente",'-'),'i',0)||
							'-'||SUBSTR_AFTER(MF."RutCliente",'-')
						  ELSE COALESCE(MF."RutCliente",'-') END, '.', '')		AS "RutCliente"
						,REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(MF."NombreCliente",'Á','A'),'É','E'),'Í','I'),'Ó','O'),'Ú','U') AS "NombreCliente"
						,TO_VARCHAR(MF."FechaOperacion",'DD-MM-YYYY')       AS "FechaInicio"
						,TO_VARCHAR(MF."FechaVencimiento",'DD-MM-YYYY')      AS "FechaVencimiento"
						,MF."Plazo"										AS "PlazoOperacion"			
						,(CASE WHEN MF."IdMonedaPrincipal" = MF."IdMonedaSecundaria" THEN MF."IdMonedaPrincipal"
							  ELSE MF."IdMonedaPrincipal" || '/' || MF."IdMonedaSecundaria" END) AS "IdMoneda"
						,MF."MontoMonedaPrincipal"						AS "MontoNocional"
						,(CASE WHEN MF."IdMonedaPrincipal"='CLP' THEN MF."MontoMonedaPrincipal" 
							   WHEN MF."IdMonedaSecundaria"='CLP' THEN MF."MontoMonedaSecundaria" 
 							   WHEN (MF."IdMonedaPrincipal"!='CLP' AND MF."IdMonedaSecundaria"!='CLP') AND MF."Nemotecnico" = 'ARBITR' THEN COALESCE((MF."MontoMonedaPrincipal" * MF."ParidadCierre") * PTCUFOPE."TipoCambioMercado", 0)
 							   WHEN (MF."IdMonedaPrincipal"!='CLP' AND MF."IdMonedaSecundaria"!='CLP') AND MF."Nemotecnico" <> 'ARBITR' THEN COALESCE((MF."MontoMonedaPrincipal" * MF."TipoCambioCierre") * PTCUFOPE."TipoCambioMercado", 0) END) AS "MontoNacionalCLP"
						,ROUND (CASE WHEN MF."Nemotecnico" = 'ARBITR' THEN MF."ParidadCierre" ELSE MF."TipoCambioCierre" END,4) AS "TipoCambioFWD"
						,MF."PlazoResidual"								AS "PlazoRem"
						,MF."MontoValorRazonableActivo"					AS "MontoValorRazonableActivo"
						,MF."MontoValorRazonablePasivo"			 		AS "MontoValorRazonablePasivo"
						,MF."MontoValorRazonableActivo" - "MontoValorRazonablePasivo" AS "ValorRazonable"
						,MF."TasaValorRazonableActivo"					AS "TasaValorRazonableActivo"
						,MF."TasaValorRazonablePasivo" 			 		AS "TasaValorRazonablePasivo"
						,CASE WHEN MF."IdTipoOperacion" = 'COMFWD' THEN 'COMPRA' ELSE 'VENTA' END AS "TipoOperacion"		
						,COALESCE(CAN."Factor", 0.0) 						AS "Factor"
						,:p_FechaOperacion								AS "FechaConsulta"
						,TO_VARCHAR(:p_FechaOperacion,'DD-MM-YYYY')			AS "FechaConsultaFormato"
						,COALESCE(PTCDO."TipoCambioMercado",0)			AS "DO"	 	
						,COALESCE(PTCUF."TipoCambioMercado",0)			AS "UF"
						,CASE WHEN CONF."Valor" = 'S' THEN COALESCE(PTCUSD."PrecioSpotCompra",0)	ELSE COALESCE(PTCDO."PrecioSpotCompra",0) 	END AS "PrecioSpotCompra"
						,CASE WHEN CONF."Valor" = 'S' THEN COALESCE(PTCUSD."PrecioSpotVenta",0) 	ELSE COALESCE(PTCDO."PrecioSpotVenta",0) 	END AS "PrecioSpotVenta"
						,CASE WHEN CONF."Valor" = 'S' THEN COALESCE(PTCUSD."PrecioSpotPromedio",0) 	ELSE COALESCE(PTCDO."PrecioSpotPromedio",0) END AS "PrecioSpotPromedio"
					    ,COALESCE(PTCUSD."TipoCambioMercado",0)			AS "USD"
						,USU."CodigoUsuario"							AS "Usuario"
						,CASE WHEN CONF."Valor" = 'S' THEN 'USD' ELSE 'DO' END AS "TipoCambioPar"	 
						,CASE WHEN CONF."Valor" = 'S' THEN COALESCE(PTCUSD."TipoCambioMercado",0) ELSE COALESCE(PTCDO."TipoCambioMercado",0) END AS "TipoCambioVal"	  
					FROM "BSCL.Mesa.BD.Vistas::FWD.ConsultarMovimientosFWD" MF
					LEFT JOIN "BSCL.Custodia.BD::CUS.Cartera" AS CAR ON MF."IdCartera"=CAR."IdCartera"
					LEFT JOIN "BSCL.Plataforma.BD::ACC.Usuario" AS USU
						ON (USU."IdUsuario" = :p_IdUsuario)
					LEFT JOIN "BSCL.Mesa.BD::SPT.ParidadYTipoCambio" PTCDO
						ON (PTCDO."Origen"	= 'OBSERVADO' AND 
							PTCDO."Fecha"	= :p_FechaOperacion AND 
							PTCDO."IdMoneda"= 'DO')
					LEFT JOIN "BSCL.Mesa.BD::SPT.ParidadYTipoCambio" PTCUF
						ON (PTCUF."Origen"	= 'OBSERVADO' AND 
							PTCUF."Fecha"	= :p_FechaOperacion AND 
							PTCUF."IdMoneda"= 'UF')
					LEFT JOIN "BSCL.Mesa.BD::SPT.ParidadYTipoCambio" PTCUSD
						ON (PTCUSD."Origen"	= 'OBSERVADO' AND 
							PTCUSD."Fecha"	= :p_FechaOperacion AND 
							PTCUSD."IdMoneda"= 'USD')
					LEFT JOIN "BSCL.Mesa.BD::SPT.ParidadYTipoCambio" PTCUFOPE
						 ON (PTCUFOPE."Origen" = 'OBSERVADO' AND
							 PTCUFOPE."Fecha" = MF."FechaOperacion" AND
							 PTCUFOPE."IdMoneda"= 'UF')
					LEFT JOIN "BSCL.Plataforma.BD::PLA.MonedaFormato" MONPRI
						ON (MONPRI."IdUsoMonedaFormato" = 'FWD' AND MONPRI."IdMoneda" = MF."IdMonedaPrincipal" AND  MONPRI."IdTipoInstrumento" = MF."IdTipoInstrumento")
					LEFT JOIN "BSCL.Plataforma.BD::PLA.MonedaFormato" MONSEC
						ON (MONSEC."IdUsoMonedaFormato" = 'FWD' AND MONSEC."IdMoneda" = MF."IdMonedaSecundaria" AND  MONSEC."IdTipoInstrumento" = MF."IdTipoInstrumento")
					LEFT JOIN "BSCL.Plataforma.BD::PLA.MonedaFormato" MONDO
						ON (MONDO."IdUsoMonedaFormato" = 'FWD' AND MONDO."IdMoneda" = 'DO' AND  MONDO."IdTipoInstrumento" = MF."IdTipoInstrumento")
					LEFT JOIN "BSCL.Plataforma.BD::PLA.MonedaFormato" MONUF
						ON (MONUF."IdUsoMonedaFormato" = 'FWD' AND MONUF."IdMoneda" = 'UF' AND  MONUF."IdTipoInstrumento" = MF."IdTipoInstrumento")
					LEFT JOIN "BSCL.Plataforma.BD::PLA.MonedaFormato" MONUSD
						ON (MONUSD."IdUsoMonedaFormato" = 'FWD' AND MONUSD."IdMoneda" = 'USD' AND  MONUSD."IdTipoInstrumento" = MF."IdTipoInstrumento")
					LEFT JOIN "BSCL.Mesa.BD::MES.ConfiguracionMesa" CONF
						ON (CONF."CodigoParametro" = 'TIPO_CAMBIO_VALORIZA_BASILEA')
					LEFT JOIN "CanastaFiltrada" CAN
    					ON (CAN."Rank" = 1 AND CAN."Hasta" >= MF."PlazoResidual" AND CAN."IdMoneda" = MF."IdMonedaPrincipal")
					WHERE MF."Estado"='VIG'
					  AND MF."FechaOperacion" <= :p_FechaOperacion
					  AND MF."FechaVencimiento" > :p_FechaOperacion 
				)
			SELECT
				 CAR.*
				,CASE WHEN CAR."TipoCambioPar" = 'DO' 
					THEN COALESCE(CAR."Factor" * CAR."MontoNocional" * CAR."DO" / 100,0)
					ELSE 				
						CASE WHEN CAR."TipoOperacion" = 'COMPRA'
							THEN 
								CASE WHEN CAR."PrecioSpotCompra" > 0
									THEN 
										COALESCE(CAR."Factor" * CAR."MontoNocional" * CAR."PrecioSpotCompra" / 100,0)
					  				ELSE 
					  					COALESCE(CAR."Factor" * CAR."MontoNocional" * CAR."PrecioSpotPromedio" / 100,0) 
					  				END
							ELSE  
								CASE WHEN CAR."PrecioSpotVenta" > 0
									THEN 
										COALESCE(CAR."Factor" * CAR."MontoNocional" * CAR."PrecioSpotVenta" / 100,0)
					  				ELSE 
					  					COALESCE(CAR."Factor" * CAR."MontoNocional" * CAR."PrecioSpotPromedio" / 100,0) 
					  				END
					  		END
					END AS "MontoMatriz" 
				,CASE WHEN CAR."TipoOperacion" = 'COMPRA'
						THEN 
							CASE WHEN CAR."ValorRazonable" >= 0 
								THEN 
									CASE WHEN CAR."TipoCambioPar" = 'DO' 
									THEN 
										COALESCE(CAR."Factor" * CAR."MontoNocional" * CAR."DO" / 100,0) + CAR."ValorRazonable"
									ELSE  
										CASE WHEN CAR."PrecioSpotCompra" > 0
											THEN 
												COALESCE(CAR."Factor" * CAR."MontoNocional" * CAR."PrecioSpotCompra" / 100,0) + CAR."ValorRazonable"
											ELSE 
												COALESCE(CAR."Factor" * CAR."MontoNocional" * CAR."PrecioSpotPromedio" / 100,0) + CAR."ValorRazonable"
											END
									END
								ELSE 
									CASE WHEN CAR."TipoCambioPar" = 'DO' 
									THEN 
										COALESCE(CAR."Factor" * CAR."MontoNocional" * CAR."DO" / 100,0)
									ELSE 
										CASE WHEN CAR."PrecioSpotCompra" > 0
											THEN 
												COALESCE(CAR."Factor" * CAR."MontoNocional" * CAR."PrecioSpotCompra" / 100,0) 
											ELSE 
												COALESCE(CAR."Factor" * CAR."MontoNocional" * CAR."PrecioSpotPromedio" / 100,0)
											END
									END
								END
						ELSE 
					CASE WHEN CAR."ValorRazonable" >= 0 
						THEN 
							CASE WHEN CAR."TipoCambioPar" = 'DO' 
								THEN 
									COALESCE(CAR."Factor" * CAR."MontoNocional" * CAR."DO" / 100,0) + CAR."ValorRazonable"
								ELSE
									CASE WHEN CAR."PrecioSpotVenta" > 0
										THEN 
											COALESCE(CAR."Factor" * CAR."MontoNocional" * CAR."PrecioSpotVenta" / 100,0) + CAR."ValorRazonable"
										ELSE 
											COALESCE(CAR."Factor" * CAR."MontoNocional" * CAR."PrecioSpotPromedio" / 100,0) + CAR."ValorRazonable"
										END
								END
						ELSE 
							CASE WHEN CAR."TipoCambioPar" = 'DO' 
								THEN 
									COALESCE(CAR."Factor" * CAR."MontoNocional" * CAR."DO" / 100,0)
								ELSE
									CASE WHEN CAR."PrecioSpotVenta" > 0
										THEN 
											COALESCE(CAR."Factor" * CAR."MontoNocional" * CAR."PrecioSpotVenta" / 100,0) 
										ELSE 
											COALESCE(CAR."Factor" * CAR."MontoNocional" * CAR."PrecioSpotPromedio" / 100,0) 
										END 
								END
						END
				END AS "EquivalenteCredito"
			FROM "Cartera" CAR;	    
			
			DELETE FROM "BSCL.Mesa.BD::FWD.Replica.TBBASIPA";
			  
			INSERT INTO "BSCL.Mesa.BD::FWD.Replica.TBBASIPA"(
				"FEC_CARTERA",
				"DOLAR_OBS",
				"UF_DIA",
				"COD_SUB_PRODFWD",
				"DESC_SUB_PRODFWD",
				"TIPO_OPER",
				"MON_FWD",
				"RUT_CLI",
				"SEC_RUT_CLI",
				"NOM_RAZ_CLI",
				"FOL_CARTERA",
				"FOL_OPERACION",
				"FEC_INIC",
				"FEC_VCTO",
				"PLAZO_OPER",
				"MTO_NOCIONAL",
				"MTO_NOC_CLP",
				"TIP_CAMBIO_FWD",
				"PLAZO_REM",
				"FACTOR",
				"MTO_MATRIZ",
				"VAL_RAZ_ACT",
				"TASA_ACT",
				"VAL_RAZ_PAS",
				"TASA_PAS",
				"VALOR_RAZ",
				"EQUIV_CRED",
				"OPERADOR"
			)
			SELECT
				"FechaConsultaFormato",
				ROUND("DO",2),
				ROUND("UF",2),
				"IdInstrumento",
				SUBSTRING("NombreInstrumento",0,50),
				"TipoOperacion",
				"IdMoneda",
				SUBSTRING("RutCliente",0,11),
				"CuentaCliente",
				LEFT("NombreCliente",50),
				CAST("FolioCartera" AS VARCHAR(20)),
				"FolioOperacion",
				"FechaInicio",
				"FechaVencimiento",
				"PlazoOperacion",
				"MontoNocional",
				"MontoNacionalCLP",
				"TipoCambioFWD",
				"PlazoRem",
				"Factor",
				"MontoMatriz",
				"MontoValorRazonableActivo",
				"TasaValorRazonableActivo",
				"MontoValorRazonablePasivo",
				"TasaValorRazonablePasivo",
				"ValorRazonable",
				"EquivalenteCredito",
				"Usuario"
			FROM :v_Cartera;
	
END;