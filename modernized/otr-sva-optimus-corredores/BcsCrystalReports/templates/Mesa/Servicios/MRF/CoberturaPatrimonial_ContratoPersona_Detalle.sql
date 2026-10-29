SET SCHEMA "BSCL";
GO
DROP PROCEDURE "BSCL.Mesa.CrystalReports.Servicios::MRF.CoberturaPatrimonial_ContratoPersona_Detalle";
GO
CREATE PROCEDURE "BSCL.Mesa.CrystalReports.Servicios::MRF.CoberturaPatrimonial_ContratoPersona_Detalle"
(
    -- VARIABLES OBLIGATORIAS --
    IN p_KeyInforme                         NVARCHAR(100),   
    IN p_IdCliente                          BIGINT,    
    IN p_opcion                             BIGINT,         
    IN p_TipoValor                          BIGINT,         
    IN p_NombreMesa                         NVARCHAR(30), 
    IN p_plazoDias							BIGINT,
    IN p_IdEmpresa							BIGINT,
    IN p_FechaValorizacion                  DATE,

    OUT p_out_CoberturaPatrimonioDetalle   TABLE  
    (   
        "FolioComprobante"                  BIGINT,
        "Cantidad"                          DECIMAL(26,8),   
        "TIR"                               DECIMAL(26,10), 
        "IdValorizacion"                    BIGINT,
        "IdTipoOperacion"                   NVARCHAR(6),
        "IdInstrumento"                     BIGINT,
        "IdTipoInstrumento"                 NVARCHAR(3), 
        "MontoActual"                       DECIMAL(26,8),
        "FolioCartera"                      BIGINT,
        "FechaVcto"                         NVARCHAR(40),   
        "Emisor"                            NVARCHAR(40),
        "Nemotecnico"                       NVARCHAR(40),
        "Corredor"                          NVARCHAR(100),
        "IdMonedaInstrumento"               NVARCHAR(3),
        "FechaVencimientoInstrumento"       NVARCHAR(40),
        "PlazoEnDiasOperacion"              BIGINT,
        "IdClasificacionTipoInstrumento"    NVARCHAR(3),
        "TieneCorteDeCupon"                 NVARCHAR(1), 
         "FechaCorteCupon"                  NVARCHAR(40), 
        "OpcionSeleccionada"                VARCHAR(23), 
        "TipoValorizacion"                  VARCHAR(9),  
        "OpcPlazoDias"                      BIGINT,   
        "IdEmpresa"                         BIGINT,
        "TipoCartera"                       NVARCHAR(10),
        "NombreMesa"                        NVARCHAR(30), 
        "ValorMercado"						DECIMAL(26,8),
        "CatRie"                            VARCHAR(8),
        "CantidadDecimales"                 BIGINT,
        "Cartera"      						NVARCHAR(40),
        "TipoInstrumento"					NVARCHAR(520),
        "CLA_RIE"							NVARCHAR(40),
        "NombreIndicador"					NVARCHAR(40),
        "Plazo"								BIGINT,
        "PlazoPacto"						BIGINT,
        "TramoCobertura"					DECIMAL(18,4),
        "Clasificacion"						NVARCHAR(40),
        "Tipo_Amo"							BIGINT,
        "Fecha"								NVARCHAR(40),
        "TipoEmisor"						NVARCHAR(10),        
        "Duration"                          DECIMAL(26,8),
        "PorcentajeCobertura"               DECIMAL(18,4),  
        "MontoCobertura"                    BIGINT,
        "FolioOperacion"					BIGINT
    ),

    OUT p_out_ErroresContador INT
)
    LANGUAGE SQLSCRIPT  
    SQL SECURITY INVOKER  
    DEFAULT SCHEMA "BSCL"  
    AS  
    BEGIN  
	   
	    
	-- VARIABLES --
    DECLARE varFechaSistema 				DATE;
	DECLARE varIdCliente                    BIGINT;
	DECLARE porCobertura                    NVARCHAR(4);
	   
	 SELECT  "Valor"
	 INTO varFechaSistema
	 FROM  "BSCL.Plataforma.BD::PLA.Configuracion"
	 WHERE  "IdEmpresa" = :p_IdEmpresa AND "CodigoParametro" = 'PLA.FechaSistema';

	SELECT "ValorParametro" INTO porCobertura
	FROM "BSCL.Mesa.BD::MES.Parametro"
	WHERE "NombreTipoParametro" = '%_COBERTURA_PATRIMONIAL';

	DELETE FROM "BSCL.Mesa.BD::MRF.CoberturaPatrimonial"
	WHERE "Fecha"                = TO_NVARCHAR(:p_FechaValorizacion, 'YYYY-MM-DD')
	  AND "IdOpcionSeleccionada" = :p_opcion
	  AND "IdTipoValorizacion"   = :p_TipoValor;
	  
    IF :p_IdCliente = 0 THEN
        SELECT DISTINCT Mesa."IdCliente"
        INTO varIdCliente
        FROM "BSCL.Mesa.BD::MES.Mesa" AS Mesa 
        INNER JOIN "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" AS SocNeg
        ON SocNeg."IdCliente" =  Mesa."IdCliente"
        WHERE Mesa."IdTipoProducto" = 'MERF'
        AND Mesa."IdEmpresa" = :p_IdEmpresa;
    ELSE 
        varIdCliente = :p_IdCliente;
    END IF;

	-- Tabla Temporal --
     CREATE LOCAL TEMPORARY TABLE #Tmp_Cobertura 
     (
        "FolioComprobante"                  BIGINT,
        "Cantidad"                          DECIMAL(26,8),   
        "TIR"                               DECIMAL(26,10), 
        "IdValorizacion"                    BIGINT,
        "IdTipoOperacion"                   NVARCHAR(6),
        "IdInstrumento"                     BIGINT,
        "IdTipoInstrumento"                 NVARCHAR(3), 
        "MontoActual"                       DECIMAL(26,8),
        "FolioCartera"                      BIGINT,
        "FechaVcto"                         NVARCHAR(40),  
        "Emisor"                            NVARCHAR(40),
        "Nemotecnico"                       NVARCHAR(40),
        "Corredor"                          NVARCHAR(100),
        "IdMonedaInstrumento"               NVARCHAR(3),
        "FechaVencimientoInstrumento"       NVARCHAR(40),  
        "PlazoEnDiasOperacion"              BIGINT,
        "IdClasificacionTipoInstrumento"    NVARCHAR(3),
        "TieneCorteDeCupon"                 NVARCHAR(1), 
        "FechaCorteCupon"                   NVARCHAR(40),  
        "OpcionSeleccionada"                VARCHAR(23), 
        "TipoValorizacion"                  VARCHAR(9),  
        "OpcPlazoDias"                      BIGINT,   
        "IdEmpresa"                         BIGINT,
        "TipoCartera"                       NVARCHAR(10),
        "NombreMesa"                        NVARCHAR(30),  
        "ValorMercado"						DECIMAL(26,8),
        "CatRie"                            VARCHAR(8),
        "CantidadDecimales"                 BIGINT,
        "Cartera"      						NVARCHAR(40),
        "TipoInstrumento"					NVARCHAR(520),
        "CLA_RIE"							NVARCHAR(40),
        "NombreIndicador"					NVARCHAR(40),
        "Plazo"								BIGINT,
        "PlazoPacto"						BIGINT,
        "TramoCobertura"					DECIMAL(18,4),
        "Clasificacion"						NVARCHAR(40),
        "Tipo_Amo"							BIGINT,
        "Fecha"								NVARCHAR(40),
        "TipoEmisor"						NVARCHAR(10),
        "Duration"                          DECIMAL(26,8),
        "FolioOperacion"					BIGINT	
     );
    
    INSERT INTO #Tmp_Cobertura 
    (
	    "FolioComprobante", "Cantidad", "TIR", "IdValorizacion", "IdTipoOperacion", "IdInstrumento", "IdTipoInstrumento", "MontoActual",
	    "FolioCartera", "FechaVcto", "Emisor", "Nemotecnico", "Corredor", "IdMonedaInstrumento", "FechaVencimientoInstrumento",
	    "PlazoEnDiasOperacion", "IdClasificacionTipoInstrumento", "TieneCorteDeCupon", "FechaCorteCupon", "OpcionSeleccionada", 
	    "TipoValorizacion", "OpcPlazoDias", "IdEmpresa", "TipoCartera", "NombreMesa", "ValorMercado", "CatRie",
	    "CantidadDecimales", "Cartera", "TipoInstrumento", "CLA_RIE", "NombreIndicador", "Plazo", "PlazoPacto", "TramoCobertura", "Clasificacion", "Tipo_Amo",
	    "Fecha", "TipoEmisor", "Duration","FolioOperacion"
    )
	SELECT 
		CVI."FolioComprobante",  
		CASE 
			WHEN CVI."IdClasificacionTipoInstrumento" = 'IF' THEN 
				CVI."ValorRescate"
			ELSE CVi."Cantidad"
		END AS "Cantidad",  
		CVI."TasaActual" AS "TIR",  
		CVI."IdValorizacion",  
		CVI."IdTipoOperacion",  
		CVI."IdInstrumento",  
		CVI."IdTipoInstrumento",  
		CVI."MontoActual",  
		CVI."FolioCartera",  
		TO_NVARCHAR(CVI."FechaVencimientoOperacion", 'DD-MM-YYYY') as "FechaVcto",
		CVI."Emisor",  
		CASE CVI."IdClasificacionTipoInstrumento" WHEN 'IF' THEN CVI."NemoSVS" ELSE CVI."Nemotecnico" END as "Nemotecnico",  
		EMP."Nombre" AS "Corredor",  
		CVI."IdMonedaInstrumento",  
		TO_NVARCHAR(CVI."FechaVencimientoInstrumento", 'DD-MM-YYYY') as "FechaVencimientoInstrumento", 
		CVI."PlazoEnDiasOperacion",  
		CVI."IdClasificacionTipoInstrumento",  
		INS."TieneCupon",  
		TO_NVARCHAR(CVI."FechaCorteCupon", 'DD-MM-YYYY') as "FechaCorteCupon", 
		CASE   
			WHEN :p_opcion = 1 THEN 'POR CARTERAS'  
			ELSE 'POR PACTOS CALZADOS'  
		END AS "OpcionSeleccionada",  
		CASE   
			WHEN :p_TipoValor = 1 THEN 'ART.514'  
			ELSE 'A MERCADO'  
		END AS "TipoValorizacion",  
		:p_plazoDias AS "OpcPlazoDias",  
		CVI."IdEmpresa",
		CVI."TipoCarteraCorredor" AS "TipoCartera",
		:p_NombreMesa AS "NombreMesa",
		CVI."MontoActualMonedaInstrumento" AS "ValorMercado",
		CASE
			WHEN CVI."Diferenciador" IN ('CG', 'CD', 'PC') THEN
				CASE 
		            WHEN DAYS_BETWEEN(TO_SECONDDATE(:varFechaSistema, 'YYYY-MM-DD'), TO_SECONDDATE(CVI."FechaVencimientoInstrumento", 'YYYY-MM-DD')) < 0 THEN ''
		            WHEN DAYS_BETWEEN(TO_SECONDDATE(:varFechaSistema, 'YYYY-MM-DD'), TO_SECONDDATE(CVI."FechaVencimientoInstrumento", 'YYYY-MM-DD')) <= 90 THEN 'A'
		            WHEN DAYS_BETWEEN(TO_SECONDDATE(:varFechaSistema, 'YYYY-MM-DD'), TO_SECONDDATE(CVI."FechaVencimientoInstrumento", 'YYYY-MM-DD')) > 90 AND 
		            DAYS_BETWEEN(TO_SECONDDATE(:varFechaSistema, 'YYYY-MM-DD'), TO_SECONDDATE(CVI."FechaVencimientoInstrumento", 'YYYY-MM-DD')) <= 360 THEN 'B'
		            WHEN DAYS_BETWEEN(TO_SECONDDATE(:varFechaSistema, 'YYYY-MM-DD'), TO_SECONDDATE(CVI."FechaVencimientoInstrumento", 'YYYY-MM-DD')) > 360 AND 
		            DAYS_BETWEEN(TO_SECONDDATE(:varFechaSistema, 'YYYY-MM-DD'), TO_SECONDDATE(CVI."FechaVencimientoInstrumento", 'YYYY-MM-DD')) <= 1800 THEN 'C'
		            WHEN DAYS_BETWEEN(TO_SECONDDATE(:varFechaSistema, 'YYYY-MM-DD'), TO_SECONDDATE(CVI."FechaVencimientoInstrumento", 'YYYY-MM-DD')) > 1800 AND 
		            DAYS_BETWEEN(TO_SECONDDATE(:varFechaSistema, 'YYYY-MM-DD'), TO_SECONDDATE(CVI."FechaVencimientoInstrumento", 'YYYY-MM-DD')) <= 4320 AND 
		            INS."NumeroCupones" > 1 THEN 'D'
		            WHEN DAYS_BETWEEN(TO_SECONDDATE(:varFechaSistema, 'YYYY-MM-DD'), TO_SECONDDATE(CVI."FechaVencimientoInstrumento", 'YYYY-MM-DD')) > 1800 AND 
		            DAYS_BETWEEN(TO_SECONDDATE(:varFechaSistema, 'YYYY-MM-DD'), TO_SECONDDATE(CVI."FechaVencimientoInstrumento", 'YYYY-MM-DD')) <= 4320 AND 
		            INS."NumeroCupones" <= 1 THEN 'D'
		            WHEN DAYS_BETWEEN(TO_SECONDDATE(:varFechaSistema, 'YYYY-MM-DD'), TO_SECONDDATE(CVI."FechaVencimientoInstrumento", 'YYYY-MM-DD')) > 4320 AND 
		            INS."NumeroCupones" > 1 THEN 'E'
				    ELSE 'E'
				END
			ELSE 
				CASE 
		            WHEN DAYS_BETWEEN(TO_SECONDDATE(:varFechaSistema, 'YYYY-MM-DD'), TO_SECONDDATE(CVI."FechaVencimientoOperacion", 'YYYY-MM-DD')) < 0 THEN ''
		            WHEN DAYS_BETWEEN(TO_SECONDDATE(:varFechaSistema, 'YYYY-MM-DD'), TO_SECONDDATE(CVI."FechaVencimientoOperacion", 'YYYY-MM-DD')) <= 90 THEN 'A'
		            WHEN DAYS_BETWEEN(TO_SECONDDATE(:varFechaSistema, 'YYYY-MM-DD'), TO_SECONDDATE(CVI."FechaVencimientoOperacion", 'YYYY-MM-DD')) > 90 AND 
		            DAYS_BETWEEN(TO_SECONDDATE(:varFechaSistema, 'YYYY-MM-DD'), TO_SECONDDATE(CVI."FechaVencimientoOperacion", 'YYYY-MM-DD')) <= 360 THEN 'B'
		            WHEN DAYS_BETWEEN(TO_SECONDDATE(:varFechaSistema, 'YYYY-MM-DD'), TO_SECONDDATE(CVI."FechaVencimientoOperacion", 'YYYY-MM-DD')) > 360 AND 
		            DAYS_BETWEEN(TO_SECONDDATE(:varFechaSistema, 'YYYY-MM-DD'), TO_SECONDDATE(CVI."FechaVencimientoOperacion", 'YYYY-MM-DD')) <= 1800 THEN 'C'
		            WHEN DAYS_BETWEEN(TO_SECONDDATE(:varFechaSistema, 'YYYY-MM-DD'), TO_SECONDDATE(CVI."FechaVencimientoOperacion", 'YYYY-MM-DD')) > 1800 AND 
		            DAYS_BETWEEN(TO_SECONDDATE(:varFechaSistema, 'YYYY-MM-DD'), TO_SECONDDATE(CVI."FechaVencimientoOperacion", 'YYYY-MM-DD')) <= 4320 AND 
		            INS."NumeroCupones" > 1 THEN 'D'
		            WHEN DAYS_BETWEEN(TO_SECONDDATE(:varFechaSistema, 'YYYY-MM-DD'), TO_SECONDDATE(CVI."FechaVencimientoOperacion", 'YYYY-MM-DD')) > 1800 AND 
		            DAYS_BETWEEN(TO_SECONDDATE(:varFechaSistema, 'YYYY-MM-DD'), TO_SECONDDATE(CVI."FechaVencimientoOperacion", 'YYYY-MM-DD')) <= 4320 AND 
		            INS."NumeroCupones" <= 1 THEN 'D'
		            WHEN DAYS_BETWEEN(TO_SECONDDATE(:varFechaSistema, 'YYYY-MM-DD'), TO_SECONDDATE(CVI."FechaVencimientoOperacion", 'YYYY-MM-DD')) > 4320 AND 
		            INS."NumeroCupones" > 1 THEN 'E'
				    ELSE 'E'
				END
		END AS "CatRie",
		COALESCE(MON."CantidadDecimales", 0) AS "CantidadDecimales",
		CASE 
			WHEN CVI."Diferenciador" IN ('PC', 'PD') THEN 'Cartera Comprometida'
			WHEN CVI."IdTipoOperacion" IN ( 'COMTER' ) THEN 
				CASE 
					WHEN CVI."Diferenciador" = 'CG' THEN 'Cartera Garantia'
					ELSE 'Cartera Libre'
				END
			WHEN CVI."Diferenciador" = 'CG' THEN 'Cartera Garantia'
			WHEN CVI."Diferenciador" = 'PP' THEN 'Cartera Pactada'
			ELSE  'Cartera Libre'
		END AS "Cartera",
		TINS."Descripcion" AS "TipoInstrumento",
		COALESCE(IRIE."ValorClasificacionRiesgo",'') AS "CLA_RIE",
		CIP."Codigo" AS "NombreIndicador",
		DAYS_BETWEEN ( TO_SECONDDATE( :varFechaSistema, 'YYYY-MM-DD'), TO_SECONDDATE( CVI."FechaVencimientoInstrumento", 'YYYY-MM-DD') ) AS "Plazo",
		DAYS_BETWEEN ( TO_SECONDDATE( :varFechaSistema, 'YYYY-MM-DD'), TO_SECONDDATE(CVI."FechaVencimientoOperacion", 'YYYY-MM-DD') ) AS "PlazoPacto",
		CASE 
            WHEN DAYS_BETWEEN(TO_SECONDDATE(:varFechaSistema, 'YYYY-MM-DD'), TO_SECONDDATE(CVI."FechaVencimientoInstrumento", 'YYYY-MM-DD')) < 0 THEN 0
            WHEN DAYS_BETWEEN(TO_SECONDDATE(:varFechaSistema, 'YYYY-MM-DD'), TO_SECONDDATE(CVI."FechaVencimientoInstrumento", 'YYYY-MM-DD')) <= 90 THEN 2
            WHEN DAYS_BETWEEN(TO_SECONDDATE(:varFechaSistema, 'YYYY-MM-DD'), TO_SECONDDATE(CVI."FechaVencimientoInstrumento", 'YYYY-MM-DD')) > 90 AND 
            DAYS_BETWEEN(TO_SECONDDATE(:varFechaSistema, 'YYYY-MM-DD'), TO_SECONDDATE(CVI."FechaVencimientoInstrumento", 'YYYY-MM-DD')) <= 360 THEN 5
            WHEN DAYS_BETWEEN(TO_SECONDDATE(:varFechaSistema, 'YYYY-MM-DD'), TO_SECONDDATE(CVI."FechaVencimientoInstrumento", 'YYYY-MM-DD')) > 360 AND 
            DAYS_BETWEEN(TO_SECONDDATE(:varFechaSistema, 'YYYY-MM-DD'), TO_SECONDDATE(CVI."FechaVencimientoInstrumento", 'YYYY-MM-DD')) <= 1800 THEN 8
            WHEN DAYS_BETWEEN(TO_SECONDDATE(:varFechaSistema, 'YYYY-MM-DD'), TO_SECONDDATE(CVI."FechaVencimientoInstrumento", 'YYYY-MM-DD')) > 1800 AND 
            DAYS_BETWEEN(TO_SECONDDATE(:varFechaSistema, 'YYYY-MM-DD'), TO_SECONDDATE(CVI."FechaVencimientoInstrumento", 'YYYY-MM-DD')) <= 4320 AND 
            INS."NumeroCupones" > 1 THEN 9
            WHEN DAYS_BETWEEN(TO_SECONDDATE(:varFechaSistema, 'YYYY-MM-DD'), TO_SECONDDATE(CVI."FechaVencimientoInstrumento", 'YYYY-MM-DD')) > 1800 AND 
            DAYS_BETWEEN(TO_SECONDDATE(:varFechaSistema, 'YYYY-MM-DD'), TO_SECONDDATE(CVI."FechaVencimientoInstrumento", 'YYYY-MM-DD')) <= 4320 AND 
            INS."NumeroCupones" <= 1 THEN 14
            WHEN DAYS_BETWEEN(TO_SECONDDATE(:varFechaSistema, 'YYYY-MM-DD'), TO_SECONDDATE(CVI."FechaVencimientoInstrumento", 'YYYY-MM-DD')) > 4320 AND 
            INS."NumeroCupones" > 1 THEN 14
		    ELSE 25
	    END * 
        CASE 
            WHEN CVI."Diferenciador" IN ('CD', 'CG') AND CVI."IdTipoOperacion" NOT IN ('VENPAC', 'VENTER') AND TEMI."CodigoTipoEmisor" = 'ES' THEN 1
            WHEN CVI."Diferenciador" NOT IN ('CD', 'CG') AND CVI."IdTipoOperacion" NOT IN ('VENPAC', 'VENTER') AND TEMI."CodigoTipoEmisor" <> 'ES' THEN 1.3
            WHEN CVI."TipoCarteraCorredor" = 'CC' THEN 0
            ELSE 
                CASE 
                    WHEN TEMI."CodigoTipoEmisor" = 'ES' THEN 1
                    ELSE 1.3
                END
        END AS "TramoCobertura",
		SEG."NombreSegmentoCliente" AS "Clasificacion",
		CASE 
			WHEN INS."TieneCupon" = 'S' THEN 1 
			ELSE 2 
		END AS "Tipo_Amo",
		TO_NVARCHAR(:p_FechaValorizacion, 'DD-MM-YYYY') as "Fecha",
		COALESCE(TEMI."CodigoTipoEmisor",'-') AS "CodigoTipoEmisor",
		CVI."Duration",
		CVI."FolioOperacion"
	FROM "BSCL.Custodia.BD::VAL.CarteraValorizadaIFRF" CVI  
	INNER JOIN "BSCL.Plataforma.BD::MER.Emisor" Ex ON CVI."Emisor" = Ex."IdEmisor"
	INNER JOIN "BSCL.Plataforma.BD::MER.TipoEmisor" CIP ON CIP."IdTipoEmisor" = Ex."IdTipoEmisor"
	INNER JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI  ON CLI."IdCliente" = :varIdCliente
	LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" INS ON INS."IdInstrumento" = CVI."IdInstrumento"
	LEFT JOIN "BSCL.Plataforma.BD::PLA.Empresa" AS EMP ON EMP."IdEmpresa" = CVI."IdEmpresa"  
	LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" AS TINS ON (TINS."IdTipoInstrumento" = CVI."IdTipoInstrumento")
	LEFT JOIN "BSCL.Plataforma.BD::MER.TipoInstrumentoTipoEmisor" TEMI ON TEMI."IdTipoInstrumento" = CVI."IdTipoInstrumento"
			AND CVI."IdTipoInstrumento" = TEMI."IdTipoInstrumento"
			AND ( 
				TEMI."Nemotecnico" != '' AND (TRIM(TEMI."Nemotecnico") = CVI."Nemotecnico")
										 AND ((TEMI."IdEmisor" != '' AND TEMI."IdEmisor" = CVI."Emisor") OR (TEMI."IdEmisor" = ''))					
				OR
				TEMI."Nemotecnico" = '' AND ((TEMI."IdEmisor" = CVI."Emisor") OR (TEMI."IdEmisor" = ''))
			)
	LEFT JOIN "BSCL.Plataforma.BD::PLA.MonedaFormato" AS MON ON MON."IdUsoMonedaFormato" = 'GEN'
			AND MON."IdMoneda" = CVI."IdMonedaInstrumento"
			AND (
					CVI."IdTipoInstrumento" != '' AND MON."IdTipoInstrumento" = CVI."IdTipoInstrumento"
					OR 
					CVI."IdTipoInstrumento" = '' AND MON."IdTipoInstrumento" =''
				)
	LEFT JOIN "BSCL.Plataforma.BD::INS.InstrumentoValorClasificacionRiesgo" AS IRIE ON IRIE."IdInstrumento" = CVI."IdInstrumento"
	LEFT JOIN  "BSCL.SocioNegocio.BD::CLI.TipoSegmento" AS  SEG ON SEG."IdSegmentoCliente" = CLI."IdSegmentoCliente"
	WHERE CVI."IdInforme" = :p_KeyInforme;
           
	INSERT INTO "BSCL.Mesa.BD::MRF.CoberturaPatrimonial"
	(
	    "IdCoberturaPatrimonial",
	    "FolioComprobante",
	    "IdTipoOperacion",
	    "IdInstrumento",
	    "IdTipoInstrumento",
	    "TipoInstrumento",
	    "ValorMercado",
	    "TIR",
	    "Cartera",
	    "CC",
	    "CC1",
	    "FechaVcto",
	    "Cantidad",
	    "Emisor",
	    "Nemotecnico",
	    "Corredor",
	    "Variable_%Cobertura",
	    "IdMonedaInstrumento",
	    "CLA_RIE",
	    "FechaVencimientoInstrumento",
	    "IdIndicador",
	    "NombreIndicador",
	    "Plazo",
	    "PlazoPacto",
	    "TramoCobertura",
	    "PorcentajeCobertura",
	    "MontoCobertura",
	    "Clasificacion",
	    "CatRie",
	    "CatRiePactos",
	    "Tipo Amo",
	    "TieneCupon",
	    "Fecha",
	    "IdOpcionSeleccionada",
	    "OpcionSeleccionada",
	    "IdTipoValorizacion",
	    "TipoValorizacion",
	    "IdOpcPlazoDias",
	    "OpcPlazoDias",
	    "Observaciones",
	    "IdEmpresa",
	    "TipoEmisor",
	    "TipoCartera"
	)
	SELECT
	    "BSCL.Mesa.BD.Secuencias::MRF.IdCoberturaPatrimonial".NEXTVAL,
	    COALESCE(TMP."FolioComprobante",            0),
	    COALESCE(TMP."IdTipoOperacion",             ''),
	    COALESCE(TMP."IdInstrumento",               0),
	    COALESCE(TMP."IdTipoInstrumento",           ''),
	    LEFT(COALESCE(TMP."TipoInstrumento",        ''), 40),
	    COALESCE(TMP."ValorMercado",                0),
	    COALESCE(TMP."TIR",                         0),
	    COALESCE(TMP."Cartera",                     ''),
	    '',
	    '',
	    COALESCE(TMP."FechaVcto",                   '1900-01-01'),
	    COALESCE(TMP."Cantidad",                    0),
	    COALESCE(TMP."Emisor",                      ''),
	    COALESCE(TMP."Nemotecnico",                 ''),
	    COALESCE(TMP."Corredor",                    ''),
	    COALESCE(:porCobertura,                     ''),
	    COALESCE(TMP."IdMonedaInstrumento",         ''),
	    COALESCE(TMP."CLA_RIE",                     ''),
	    COALESCE(TMP."FechaVencimientoInstrumento", '1900-01-01'),
	    COALESCE(TMP."TipoEmisor",                  ''),
	    COALESCE(TMP."NombreIndicador",             ''),
	    COALESCE(TMP."Plazo",                       0),
	    COALESCE(TMP."PlazoPacto",                  0),
	    COALESCE(TMP."TramoCobertura",              0),
	    CASE
	        WHEN TMP."NombreIndicador" = 'BANCO'
	        THEN (TMP."TramoCobertura" * TMP."TramoCobertura")
	        ELSE TMP."TramoCobertura"
	    END,
	    CASE
	        WHEN TMP."NombreIndicador" = 'BANCO'
	        THEN CEIL(COALESCE((TMP."ValorMercado" * (TMP."TramoCobertura" * TMP."TramoCobertura") / 100), 0))
	        ELSE CEIL(COALESCE((TMP."ValorMercado" *  TMP."TramoCobertura" / 100), 0))
	    END,
	    COALESCE(TMP."Clasificacion",   ''),
	    CASE
	        WHEN TMP."CatRie" = 'D' AND TMP."Tipo_Amo" = 1 THEN 'D TIPO 1'
	        WHEN TMP."CatRie" = 'D' AND TMP."Tipo_Amo" = 2 THEN 'D TIPO 2'
	        WHEN TMP."CatRie" = 'E' AND TMP."Tipo_Amo" = 1 THEN 'E TIPO 1'
	        WHEN TMP."CatRie" = 'E' AND TMP."Tipo_Amo" = 2 THEN 'E TIPO 2'
	        ELSE TMP."CatRie"
	    END,
	    '',
	    TMP."Tipo_Amo",
	    CASE WHEN TMP."Tipo_Amo" = 1 THEN 'S' ELSE 'N' END,
	    TO_NVARCHAR(:p_FechaValorizacion, 'YYYY-MM-DD'),
	    :p_opcion,
	    CASE WHEN :p_opcion = 1 THEN 'POR CARTERAS' ELSE 'POR PACTOS CALZADOS' END,
	    :p_TipoValor,
	    CASE WHEN :p_TipoValor = 1 THEN 'ART.514' ELSE 'A MERCADO' END,
	    :p_plazoDias,
	    CASE
	        WHEN :p_plazoDias = 1 THEN 'Menor a 90'
	        WHEN :p_plazoDias = 2 THEN 'Mayor a 90'
	        ELSE 'TODOS'
	    END,
	    '',
	    :p_IdEmpresa,
	    CASE WHEN TMP."TipoEmisor" = '-' THEN '' ELSE COALESCE(TMP."TipoEmisor", '') END,
	    COALESCE(TMP."TipoCartera", '')
	FROM #Tmp_Cobertura TMP;
	
    -- Obtener datos y asignarlos a la tabla de salida
    p_out_CoberturaPatrimonioDetalle =
                                    SELECT  
                                        TEMP."FolioComprobante", 
                                        TEMP."Cantidad", 
                                        TEMP."TIR", 
                                        TEMP."IdValorizacion", 
                                        TEMP."IdTipoOperacion", 
                                        TEMP."IdInstrumento", 
                                        TEMP."IdTipoInstrumento", 
                                        TEMP."MontoActual",
	    								TEMP."FolioCartera", 
	    								TEMP."FechaVcto", 
	    								TEMP."Emisor", 
	    								TEMP."Nemotecnico", 
	    								TEMP."Corredor", 
	    								TEMP."IdMonedaInstrumento", 
	    								TEMP."FechaVencimientoInstrumento",
	    								TEMP."PlazoEnDiasOperacion", 
	    								TEMP."IdClasificacionTipoInstrumento", 
	    								TEMP."TieneCorteDeCupon", 
	    								TEMP."FechaCorteCupon", 
	    								TEMP."OpcionSeleccionada", 
	    								TEMP."TipoValorizacion", 
	    								TEMP."OpcPlazoDias", 
	    								TEMP."IdEmpresa", 
	    								TEMP."TipoCartera", 
	    								TEMP."NombreMesa", 
	    								TEMP."ValorMercado", 
	    								TEMP."CatRie",
	    								TEMP."CantidadDecimales", 
	    								TEMP."Cartera", 
	    								TEMP."TipoInstrumento", 
	    								TEMP."CLA_RIE", 
	    								TEMP."NombreIndicador", 
	    								TEMP."Plazo", 
	    								TEMP."PlazoPacto", 
	    								TEMP."TramoCobertura", 
	    								TEMP."Clasificacion", 
	    								TEMP."Tipo_Amo",
	   								 	TEMP."Fecha", 
	   								 	TEMP."TipoEmisor",
                                        TEMP."Duration",
                                        CASE
								            WHEN TEMP."NombreIndicador" = 'BANCO' THEN (TEMP."TramoCobertura" * TEMP."TramoCobertura")
								            ELSE TEMP."TramoCobertura"
								        END AS "PorcentajeCobertura",
								        CASE 
									        WHEN TEMP."NombreIndicador" = 'BANCO' THEN 
									        	CEIL(COALESCE((TEMP."ValorMercado" * (TEMP."TramoCobertura" * TEMP."TramoCobertura")/100),0))
									        ELSE CEIL(COALESCE((TEMP."ValorMercado" *  TEMP."TramoCobertura"/100),0))
                                        END AS "MontoCobertura",
                                        TEMP."FolioOperacion"
                                        FROM #Tmp_Cobertura TEMP;

    p_out_ErroresContador = 0;  
    DROP TABLE #Tmp_Cobertura;

END;