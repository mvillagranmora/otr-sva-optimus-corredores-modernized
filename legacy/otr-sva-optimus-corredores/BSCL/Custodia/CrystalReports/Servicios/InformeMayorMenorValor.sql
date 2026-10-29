SET SCHEMA "BSCL";
GO
DROP PROCEDURE "BSCL.Custodia.CrystalReports.Servicios::InformeMayorMenorValor";
GO
CREATE PROCEDURE "BSCL.Custodia.CrystalReports.Servicios::InformeMayorMenorValor" ( 
    --VARIABLES OBLIGATORIAS
   	IN p_IdInforme              			NVARCHAR(80)
   	,IN p_IdEmpresa							BIGINT	
)
LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
AS
BEGIN
    DECLARE v_count           INT;
   	DECLARE v_FechaSistema    NVARCHAR(10);
    BEGIN
    
    CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaSistema" (:p_IdEmpresa,v_FechaSistema);
	    
    v_InformeMayorMenorValor =  SELECT 
                                    CAR."FolioCartera",
                                    POR."CodigoPortafolio",
                                    POR."Proposito",
                                    CAR."FolioOperacion",
                                    CAR."IdOperacion",
                                    CASE WHEN IFNULL(PER."Nombres",'') = '' 
                                        THEN PER."RazonSocial" 
                                    ELSE 
                                        CONCAT(CONCAT(CONCAT(PER."Nombres",' '),CONCAT(PER."ApellidoPaterno",' ') )  ,PER."ApellidoMaterno") 
                                    END AS "NombreCliente",                                    
                                    CAR."Diferenciador",
                                    CASE 
                                      WHEN CAR."IdClasificacionTipoInstrumento" = 'IF' THEN GREATEST(DAYS_BETWEEN (TO_DATE(:v_FechaSistema, 'YYYY-MM-DD'), TO_DATE("FechaVencimientoInstrumento", 'YYYY-MM-DD')),0)
                                      WHEN CAR."IdClasificacionTipoInstrumento" = 'RF' AND CAR."IdTipoInstrumento" = 'LTH' THEN GREATEST(ROUND(DAYS_BETWEEN (TO_DATE(:v_FechaSistema, 'YYYY-MM-DD'), TO_DATE("FechaVencimientoInstrumento", 'YYYY-MM-DD')) / 360),0)
                                      WHEN CAR."IdClasificacionTipoInstrumento" = 'RF' AND CAR."IdTipoInstrumento" != 'LTH' THEN GREATEST(ROUND(DAYS_BETWEEN (TO_DATE(:v_FechaSistema, 'YYYY-MM-DD'), TO_DATE("FechaVencimientoInstrumento", 'YYYY-MM-DD')) / 365),0)
                                    ELSE 0
                                    END AS "Plazo",
                                    CASE 
                                      WHEN CAR."IdClasificacionTipoInstrumento" = 'RV' THEN 
                                          GREATEST(DAYS_BETWEEN(TO_DATE(CAR."FechaValorizacionProximo", 'YYYY-MM-DD'), "FechaVencimientoOperacion"), 1)
                                        WHEN CAR."IdClasificacionTipoInstrumento" IN ('RF', 'IF') THEN 
                                          DAYS_BETWEEN(TO_DATE(CAR."FechaValorizacionProximo", 'YYYY-MM-DD'), "FechaVencimientoInstrumento")
                                        ELSE 0
                                    END AS "Dias",
                                    CAR."IdTipoInstrumento",
                                    CAR."IdClasificacionTipoInstrumento",
                                    CAR."NemoSVS",
                                    CAR."Nemotecnico",
                                    CAR."IdMonedaInstrumento",
                                    CAR."IdTipoReajuste",
                                    CAR."Emisor",
                                    CAR."CodigoIFRS",
                                    CAR."IdBoveda",
                                    COALESCE(TT."CodigoTipoEmisor",'-') AS "CodigoTipoEmisor",
                                    CAR."Cantidad",
                                    CAR."TasaCompra",
                                    CAR."TasaActual",
                                    CAR."PrecioCompra",
                                    CAR."PrecioActual",
                                    CAR."MontoCompra",
                                    CAR."MontoCompraMonedaInstrumento",
                                    CAR."MontoActual",
                                    CAR."MontoActualMonedaInstrumento",
                                    CAR."MontoProximo",
                                    CAR."MontoProximoMonedaInstrumento",
                                    TO_VARCHAR(CAR."FechaVencimientoOperacion", 'DD-MM-YYYY') AS "FechaVencimientoOperacion",
                                    CAR."ValorRescate",
                                    CAR."Duration",
                                    CAR."Convexidad",
                                    TO_VARCHAR(CAR."FechaOperacion", 'DD-MM-YYYY') AS "FechaOperacion",
                                    TO_VARCHAR(CAR."FechaEmisionInstrumento", 'DD-MM-YYYY') AS "FechaEmisionInstrumento",
                                    TO_VARCHAR(CAR."FechaLiquidacion", 'DD-MM-YYYY') AS "FechaLiquidacion",
                                    TO_VARCHAR(CAR."FechaVencimientoInstrumento", 'DD-MM-YYYY') AS "FechaVencimientoInstrumento",
                                    TO_VARCHAR(CAR."FechaValorizacionActual", 'DD-MM-YYYY') AS "FechaValorizacionActual",
                                    COALESCE(FLAG."FlagDeterioro",'N') AS "FlagDeterioro", 
									COALESCE(FLAG."PorcentajeDeterioro",0 ) AS "PorcentajeDeterioro",
									CASE FLAG."FlagDeterioro" WHEN 'S' THEN 
										CASE WHEN CAR."IdClasificacionTipoInstrumento" = 'IF' 
											THEN (COALESCE(FLAG."PorcentajeDeterioro" ,0)/100) * COALESCE(CAR."ValorRescate",0)
											ELSE (COALESCE(FLAG."PorcentajeDeterioro" ,0)/100) * COALESCE(CAR."Cantidad",0)
										END
									ELSE 0 
									END AS "Deterioro",
									COALESCE(FLAG."Fase",0) AS "Fases",
									CAR."TasaProximo",
									CAR."PrecioProximo",
								 "prmTipoInforme"  AS "TipoInforme",
                 	DI."DocumentoIdentidad" ,
									PER."RazonSocial"
                                FROM "BSCL.Custodia.BD::VAL.CarteraValorizadaIFRF" AS CAR
                                INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" POR ON POR."IdPortafolio" = CAR."IdPortafolio"
                                INNER JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" CLI ON CLI."IdCliente" = CAR."IdCliente" 
                                INNER JOIN "BSCL.SocioNegocio.BD::PER.Persona" PER ON PER."IdPersona" = CLI."IdPersona" 
                                INNER JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" DI ON PER."IdDocumentoIdentidad" = DI."IdDocumentoIdentidad" 
                                LEFT JOIN "BSCL.Plataforma.BD::MER.DeterioroEmisor" AS FLAG ON FLAG."Emisor" = CAR."Emisor" AND FLAG."IdEstado" = 'ACT'
                                LEFT JOIN "BSCL.Plataforma.BD::MER.TipoInstrumentoTipoEmisor" AS TT ON CAR."IdClasificacionTipoInstrumento" = TT."IdClasificacionTipoInstrumento"
									AND CAR."IdTipoInstrumento" = TT."IdTipoInstrumento"                              
									AND ( 
										TT."Nemotecnico" != '' AND (TRIM(TT."Nemotecnico") = CAR."Nemotecnico")
															   AND ((TT."IdEmisor" != '' AND TT."IdEmisor" = CAR."Emisor") OR (TT."IdEmisor" = ''))					
										OR
										TT."Nemotecnico" = '' AND ((TT."IdEmisor" = CAR."Emisor") OR (TT."IdEmisor" = ''))
                                )
                                WHERE "IdInforme" = :p_IdInforme;
    SELECT COUNT(*) INTO v_count FROM :v_InformeMayorMenorValor;

    IF (v_count > 0) THEN 
      SELECT
        "FolioCartera",
        "CodigoPortafolio",
        "Proposito",
        "FolioOperacion",
        "IdOperacion",
        "NombreCliente",
        "Diferenciador",
        "Dias",
        CASE 
        	WHEN "Dias" > 7 THEN 'S'
        	ELSE 'N'
        END AS "MayorIgualSieteDias",   
        "IdTipoInstrumento",
        "IdClasificacionTipoInstrumento",
        "NemoSVS",
        "Nemotecnico",
        "IdMonedaInstrumento",
        "IdTipoReajuste",
        "Emisor",
        "CodigoTipoEmisor",
        "CodigoIFRS",
        "IdBoveda",
        COALESCE("CodigoTipoEmisor", '-') AS "Familia",
        "Cantidad",
        "TasaCompra",
        "MontoCompra",
        "MontoCompraMonedaInstrumento",
        "PrecioCompra",
        "PrecioActual",
        "MontoActual",
        "MontoActualMonedaInstrumento",
        "MontoProximo",
        "MontoProximoMonedaInstrumento",
        "TasaActual",
        "FechaVencimientoOperacion",
        "ValorRescate",
        "Duration",        
        "Convexidad",
        CASE
        	WHEN COALESCE("MontoProximo",0) - COALESCE("MontoActual",0) < 0 
        		THEN ABS(COALESCE("MontoProximo",0) - COALESCE("MontoActual",0))
        	ELSE 0
        END AS "MenorValor",
        CASE
          WHEN COALESCE("MontoProximo",0) - COALESCE("MontoActual",0) > 0 
        		THEN ABS(COALESCE("MontoProximo",0) - COALESCE("MontoActual",0))
        	ELSE 0
        END AS "MayorValor",
        "FechaOperacion",
        "FechaEmisionInstrumento",
        "FechaLiquidacion",
        "FechaVencimientoInstrumento",
        "FechaValorizacionActual",
        "FlagDeterioro",
        "PorcentajeDeterioro",
        "Deterioro",
        "Fases",
        "TasaProximo",
		    "PrecioProximo",
        "TipoInforme",
        "DocumentoIdentidad",
        "RazonSocial"
      FROM :v_InformeMayorMenorValor
      ORDER BY "Diferenciador";
    ELSE
      SELECT   
        0   AS "FolioCartera",
        0	AS "CodigoPortafolio",
        ''  AS "Proposito",
        0 AS "FolioOperacion",
        0	AS "IdOperacion",
        ''  AS "NombreCliente",
        ''  AS "Diferenciador",
        0   AS "Dias",
        ''	AS "MayorIgualSieteDias",
        ''  AS "IdTipoInstrumento",
        ''	AS "IdClasificacionTipoInstrumento",
        ''  AS "NemoSVS",
        ''  AS "Nemotecnico",
        ''	AS "IdMonedaInstrumento",
        ''  AS "IdTipoReajuste",
        ''  AS "Emisor",
        ''	AS "CodigoTipoEmisor",
        ''  AS "CodigoIFRS",
        ''	AS "IdBoveda",
        ''	AS "Familia",
        0   AS "Cantidad",
        0   AS "TasaCompra",
        0   AS "MontoCompra",
        0	AS "MontoCompraMonedaInstrumento",
        0	AS "PrecioCompra",
        0	AS "PrecioActual",
        0	AS "MontoActual",
        0   AS "MontoActualMonedaInstrumento",
        0   AS "MontoProximo",
        0   AS "MontoProximoMonedaInstrumento",
        0   AS "TasaActual",
        '0001-01-01'  AS "FechaVencimientoOperacion",
        0   AS "Duration",
        0 AS "ValorRescate",
        0	AS "Convexidad",
        0	AS "MenorValor",
        0	AS "MayorValor",
        '0001-01-01'  AS "FechaOperacion",
        '0001-01-01'  AS "FechaEmisionInstrumento",
        '0001-01-01'  AS "FechaLiquidacion",
        '0001-01-01'  AS "FechaVencimientoInstrumento",
        '0001-01-01'  AS "FechaValorizacionActual",
        0	AS "FlagDeterioro",
        0	AS "PorcentajeDeterioro",
        0	AS "Deterioro",
        0	AS "Fases",
        0   AS "TasaProximo",
        0   AS "PrecioProximo",
        '' AS "TipoInforme",
        '' AS "DocumentoIdentidad",
        '' AS "RazonSocial"
	    FROM DUMMY;
    
    END IF;
	
	END;
END