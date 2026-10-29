SET SCHEMA "BSCL";
--Creacion 19-12-2023
--Mrojas-CELBUHOPRO-1096

DROP VIEW "BSCL.Custodia.BD.Vistas::CUS.ConsultarCircularCMF";

CREATE VIEW "BSCL.Custodia.BD.Vistas::CUS.ConsultarCircularCMF" AS

SELECT 
"Fecha",
"IdClasificacionTipoInstrumentoModificado",
REPLACE( REPLACE("Nemotecnico",'ñ','#'),'Ñ','#') AS "Nemotecnico",
TO_DECIMAL(SUM("CantidadUnidades"),19,4) AS "CantidadUnidades",
"BSCL.Normativa.BD.Utilitarios::NOR.FormateaTextoCircularNCG499"("CodigoPais") AS "CodigoPais",
"BSCL.Normativa.BD.Utilitarios::NOR.FormateaTextoCircularNCG499"("ISIN") AS "ISIN",
"SituacionInstrumento" AS "SituacionInstrumento",
"SituacionInstrumentoAux" AS "SituacionInstrumentoAux",
"BSCL.Normativa.BD.Utilitarios::NOR.FormateaTextoCircularNCG499"("IdTipoInstrumento") AS "IdTipoInstrumento",
"IdCliente",
"FechaVencimiento",
MAX("ClasificacionRiesgoInstrumento") AS "ClasificacionRiesgoInstrumento",
MAX("TendenciaClasificacionriesgo") AS "TendenciaClasificacionriesgo",
"BSCL.Normativa.BD.Utilitarios::NOR.FormateaTextoCircularNCG499"("IdMonedaPrincipal") AS "IdMonedaPrincipal",
"Tir",
"BSCL.Normativa.BD.Utilitarios::NOR.FormateaTextoCircularNCG499"("CodigoValorizacion") AS "CodigoValorizacion",
SUM("ValorizacionCierre") AS "ValorizacionCierre",
"BaseTasa",
"BSCL.Normativa.BD.Utilitarios::NOR.FormateaTextoCircularNCG499"("CodigoPaisEmisionInstrumento") AS "CodigoPaisEmisionInstrumento",
"BSCL.Normativa.BD.Utilitarios::NOR.FormateaTextoCircularNCG499"(MAX("FuentePrecios")) AS "FuentePrecios",
 MAX("Duration") AS "Duration",
"BSCL.Normativa.BD.Utilitarios::NOR.FormateaTextoCircularNCG499"("Relacionado") AS "Relacionado",
"RutCliente",
"Digito",
"BSCL.Normativa.BD.Utilitarios::NOR.FormateaTextoCircularNCG499"("NombrePersona") AS "NombrePersona",
"IdEmisor",
"IdOrigen",
"IdEstado",
"IdInstrumento"
FROM
(SELECT 
    CIR."Fecha", 
    CIR."ClasificacionInstrumentoCustodia" AS "IdClasificacionTipoInstrumentoModificado",
    CIR."Nemotecnico",
    CIR."ISIN",
    CIR."IdCliente",
    CASE 
        WHEN CIR."CodigoPais" IS NULL OR CIR."CodigoPais" = ' ' THEN
            CASE 
                WHEN HOMO_RESIDENCIA."ValorOrigen" IS NOT NULL THEN HOMO_RESIDENCIA."ValorDestino"
                ELSE NULL
            END
        ELSE CIR."CodigoPais"
    END AS "CodigoPais",
    CASE 
    	WHEN CIR."CantidadCustodia" < 0 THEN 'OR'
    	ELSE 'NR'
    END AS "SituacionInstrumento",
    'NR' AS "SituacionInstrumentoAux",
    COALESCE(HOMO_TIPO_INSTRUMENTO."ValorDestino", CIR."IdTipoInstrumento") AS "IdTipoInstrumento",
    CASE
	    WHEN COALESCE(HOMO_TIPO_INSTRUMENTO_DES."ValorDestino", HOMO_TIPO_INSTRUMENTO."ValorDestino") IN ('BBPNEE','BBP') THEN '1999-01-01'
	    ELSE CIR."FechaVencimiento"
    END AS "FechaVencimiento",
    CAST(CIR."CantidadCustodia" AS DECIMAL(26,4)) AS "CantidadUnidades",
    CASE
        WHEN TRIM(CIR."ClasificacionRiesgoInstrumento") IN ('N', 'BC') THEN ''
        ELSE COALESCE(HOMO_TIPO_CLASIFICACIONRIESGO."ValorDestino", CIR."ClasificacionRiesgoInstrumento")
    END AS "ClasificacionRiesgoInstrumento",
    CIR."TendenciaDeRiesgo" AS "TendenciaClasificacionriesgo",
    CASE
        WHEN CIR."IdMonedaInstrReajuste" = HOMO_MONEDA."ValorOrigen" THEN HOMO_MONEDA."ValorDestino"
        ELSE CIR."IdMonedaPrincipal"
    END AS "IdMonedaPrincipal", 
    CASE
        WHEN CIR."IdClasificacionTipoInstrumento" = 'RV' THEN CIR."PrecioMercadoCustodia"
        WHEN CIR."IdClasificacionTipoInstrumento" = 'RF' THEN CIR."TasaMercadoCustodia"
        WHEN CIR."IdClasificacionTipoInstrumento" = 'IF' THEN CIR."TasaMercadoCustodia"
        WHEN CIR."IdClasificacionTipoInstrumento" = 'FM' THEN CIR."PrecioMercadoCustodia"
        ELSE 0
    END AS "Tir",
    CASE
        WHEN CIR."CodigoValorizacion" != '' THEN CIR."CodigoValorizacion"
        WHEN CIR."CodigoValorizacion" = '' AND CIR."IdClasificacionTipoInstrumento" IN ('RV','FM') THEN 'PR'
        WHEN CIR."CodigoValorizacion" = '' AND CIR."IdClasificacionTipoInstrumento" IN ('RF','IF') THEN 'TR'
        ELSE '' 
    END AS "CodigoValorizacion",
    CASE 
        WHEN CIR."BaseTasa" IS NOT NULL THEN CIR."BaseTasa"
        ELSE
            CASE 
                WHEN CIR."Nemotecnico" LIKE '%PDBC%' AND CIR."IdMonedaInstrReajuste" = 'CLP' THEN 30
                WHEN CIR."IdClasificacionTipoInstrumento" = 'IF' AND CIR."IdMonedaInstrReajuste" = 'CLP' AND INS."Reajustable" = 'N' THEN 30
                WHEN CIR."IdClasificacionTipoInstrumento" = 'IF' THEN 360
                WHEN CIR."IdClasificacionTipoInstrumento" = 'RF' AND INS."IndicadorBolson" = 'S' THEN 365
                WHEN CIR."IdClasificacionTipoInstrumento" = 'RF' THEN 360
                ELSE 0
            END
    END AS "BaseTasa",
    CIR."MontoMercadoCustodia" AS "ValorizacionCierre",
    CASE 
        WHEN CIR."CodigoPaisEmisionInstrumento" IS NULL THEN (CASE WHEN CIR."ISIN" IS NOT NULL AND CIR."ISIN" != '' THEN SUBSTRING(CIR."ISIN", 0, 2)  ELSE 'CL' END)
        ELSE CIR."CodigoPaisEmisionInstrumento"
    END AS "CodigoPaisEmisionInstrumento",
    CIR."FuentePrecios",
    CIR."Duration",
    CASE 
        WHEN IndPer."ValorIndicador" = 'S' THEN 'RE'
        ELSE 'NR'
    END AS "Relacionado",
    CASE 
    	WHEN CIR."IdClasificacionTipoInstrumento" = 'FM' THEN LPAD(LEFT(COALESCE (FD_FM."CodigoSVS",''), INSTR(COALESCE(FD_FM."CodigoSVS",''), '-') - 1), 9, '0')
    	WHEN CIR."IdTipoInstrumento" IN ('CFI', 'CFM') THEN LPAD(LEFT(COALESCE (INSTRU."DocumentoIdentidadFondo", ''), INSTR(COALESCE(INSTRU."DocumentoIdentidadFondo", ''), '-') - 1), 9, '0')
    	ELSE LPAD(LEFT(COALESCE (DOC."DocumentoIdentidad",''), INSTR(COALESCE(DOC."DocumentoIdentidad",''), '-') - 1), 9, '0')
    END AS "RutCliente",
    CASE 
    	WHEN CIR."IdClasificacionTipoInstrumento" = 'FM' THEN RIGHT(COALESCE(FD_FM."CodigoSVS",''), 1)
    	WHEN CIR."IdTipoInstrumento" IN ('CFI', 'CFM') THEN RIGHT(COALESCE(INSTRU."DocumentoIdentidadFondo",''), 1)
    	ELSE RIGHT(COALESCE(DOC."DocumentoIdentidad",''), 1)
    END AS "Digito",
    RPAD(LTRIM(COALESCE(PER_EMI."RazonSocial",'')), 100, ' ') AS "NombrePersona",
    CIR."IdEmisor",
    CIR."IdOrigen",
    CIR."IdEstado",
    CIR."IdInstrumento"
FROM 
    "BSCL.Custodia.BD::CUS.CircularCarteraCMF" CIR
LEFT JOIN 
    "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON CIR."IdCliente" = CLI."IdCliente"
LEFT JOIN 
    "BSCL.SocioNegocio.BD::PER.Persona" AS PER ON CLI."IdPersona" = PER."IdPersona"
LEFT JOIN 
    "BSCL.Plataforma.BD::REG.Pais" AS PaisRes ON PER."IdPaisResidencia" = PaisRes."IdPais"
LEFT JOIN 
    "BSCL.Plataforma.BD::INS.Instrumento" AS INS ON CIR."Nemotecnico" = INS."Nemotecnico"
LEFT JOIN 
    "BSCL.Plataforma.BD::REG.Pais" AS PaisEmi ON INS."IdPaisEmisor" = PaisEmi."IdPais"
LEFT JOIN 
    "BSCL.Plataforma.BD::MER.Emisor" AS EMI ON CIR."IdEmisor" = EMI."IdEmisor"
LEFT JOIN 
    "BSCL.SocioNegocio.BD::PER.Persona" AS PER_EMI ON EMI."IdPersona" = PER_EMI."IdPersona"
LEFT JOIN 
	"BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPersona" AS IndPer ON EMI."IdPersona" = IndPer."IdPersona" AND IndPer."CodigoTipoIndicador" ='RELCORRPER'
LEFT JOIN 
    "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS DOC ON EMI."IdPersona" = DOC."IdPersona" AND DOC."Predeterminado" = 'S'
LEFT JOIN 
    "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" AS SerieFon ON CIR."Nemotecnico" = SerieFon."IdSerieFondo"   
LEFT JOIN 
    "BSCL.Normativa.BD::DOC.Homologacion" HOMO_EMISION
    ON PaisEmi."CodigoPais" = HOMO_EMISION."ValorOrigen"
    AND HOMO_EMISION."Tipo" = 'PAISCONTRACMF'
LEFT JOIN 
    "BSCL.Plataforma.BD::INS.Corredora" AS COR ON PER."IdPersona" = COR."IdPersona"
LEFT JOIN 
    "BSCL.Normativa.BD::DOC.Homologacion" HOMO_RESIDENCIA
    ON PaisRes."CodigoPais" = HOMO_RESIDENCIA."ValorOrigen"
    AND HOMO_RESIDENCIA."Tipo" = 'PAISCONTRACMF'
LEFT JOIN 
    "BSCL.Normativa.BD::DOC.Homologacion" HOMO_TIPO_INSTRUMENTO
    ON CIR."IdTipoInstrumento" = HOMO_TIPO_INSTRUMENTO."ValorOrigen"
    AND HOMO_TIPO_INSTRUMENTO."Tipo" = 'INSTRUMENTOCMF'
LEFT JOIN 
    "BSCL.Normativa.BD::DOC.Homologacion" HOMO_TIPO_INSTRUMENTO_DES
    ON CIR."IdTipoInstrumentoCMF" = HOMO_TIPO_INSTRUMENTO_DES."ValorDestino"
    AND HOMO_TIPO_INSTRUMENTO_DES."Tipo" = 'INSTRUMENTOCMF'
LEFT JOIN "BSCL.Normativa.BD::DOC.Homologacion" AS HOMO_MONEDA 
	ON HOMO_MONEDA."ValorOrigen" = CIR."IdMonedaInstrReajuste"
    AND HOMO_MONEDA."Esquema" ='CMF'
    AND HOMO_MONEDA."Tipo" = 'MONEDAPRINCIPAL'
LEFT JOIN "BSCL.Normativa.BD::DOC.Homologacion" AS HOMO_TIPO_CLASIFICACIONRIESGO 
	ON HOMO_TIPO_CLASIFICACIONRIESGO."ValorOrigen" = TRIM(CIR."ClasificacionRiesgoInstrumento")
    AND HOMO_TIPO_CLASIFICACIONRIESGO."Esquema" ='CMF'
    AND HOMO_TIPO_CLASIFICACIONRIESGO."Tipo" = 'CLASIFICACIONRIESGO'
LEFT JOIN "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" AS SER ON SER."IdSerieFondo" = CIR."Nemotecnico"
LEFT JOIN "BSCL.FondoMutuo.BD::FMM.Fondo" AS FD ON FD."IdFondo" = SER."IdFondo"
LEFT JOIN "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" AS SER_FM ON SER_FM."NemoCMF" = CIR."Nemotecnico"
LEFT JOIN "BSCL.FondoMutuo.BD::FMM.Fondo" AS FD_FM ON FD_FM."IdFondo" = SER_FM."IdFondo"
LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS INSTRU ON INSTRU."Nemotecnico" = CIR."Nemotecnico"
WHERE CIR."IdEstado" IN ('ACT', 'DES', 'NEG')
    UNION    
    SELECT 
    CIR."Fecha", 
    CIR."ClasificacionInstrumentoGarantia" AS "IdClasificacionTipoInstrumentoModificado",
 	CIR."Nemotecnico",
    CIR."ISIN",
    CIR."IdCliente",
    CASE 
        WHEN CIR."CodigoPais" IS NULL OR CIR."CodigoPais" = '' THEN
            CASE 
                WHEN HOMO_RESIDENCIA."ValorOrigen" IS NOT NULL THEN HOMO_RESIDENCIA."ValorDestino"
                ELSE NULL
            END
        ELSE CIR."CodigoPais"
    END AS "CodigoPais",
    CASE 
    	WHEN CIR."CantidadGarantia" < 0 THEN 'OR'
    	ELSE 'GT'
    END AS "SituacionInstrumento",
    'GT' AS "SituacionInstrumentoAux",
    COALESCE(HOMO_TIPO_INSTRUMENTO."ValorDestino", CIR."IdTipoInstrumento") AS "IdTipoInstrumento",   
    CASE
	    WHEN COALESCE(HOMO_TIPO_INSTRUMENTO_DES."ValorDestino", HOMO_TIPO_INSTRUMENTO."ValorDestino") IN ('BBPNEE','BBP') THEN '1999-01-01'
	    ELSE CIR."FechaVencimiento"
    END AS "FechaVencimiento",
	CAST(CIR."CantidadGarantia" AS DECIMAL(26,4)) AS "CantidadUnidades",
    CASE
        WHEN TRIM(CIR."ClasificacionRiesgoInstrumento") IN ('N', 'BC') THEN ''
        ELSE COALESCE(HOMO_TIPO_CLASIFICACIONRIESGO."ValorDestino", CIR."ClasificacionRiesgoInstrumento")
    END AS "ClasificacionRiesgoInstrumento",
    CIR."TendenciaDeRiesgo" AS "TendenciaClasificacionriesgo",
    CASE
        WHEN CIR."IdMonedaInstrReajuste" = HOMO_MONEDA."ValorOrigen" THEN HOMO_MONEDA."ValorDestino"
        ELSE CIR."IdMonedaPrincipal"
    END AS "IdMonedaPrincipal", 
    CASE
        WHEN CIR."IdClasificacionTipoInstrumento" = 'RV' THEN CIR."PrecioMercadoCustodia"
        WHEN CIR."IdClasificacionTipoInstrumento" = 'RF' THEN CIR."TasaMercadoCustodia"
        WHEN CIR."IdClasificacionTipoInstrumento" = 'IF' THEN CIR."TasaMercadoCustodia"
        WHEN CIR."IdClasificacionTipoInstrumento" = 'FM' THEN CIR."PrecioMercadoCustodia"
        ELSE 0
    END AS "Tir",
    CASE
        WHEN CIR."CodigoValorizacion" != '' THEN CIR."CodigoValorizacion"
        WHEN CIR."CodigoValorizacion" = '' AND CIR."IdClasificacionTipoInstrumento" IN ('RV','FM') THEN 'PR'
        WHEN CIR."CodigoValorizacion" = '' AND CIR."IdClasificacionTipoInstrumento" IN ('RF','IF') THEN 'TR'
        ELSE '' 
    END AS "CodigoValorizacion",
    CASE 
        WHEN CIR."BaseTasa" IS NOT NULL THEN CIR."BaseTasa"
        ELSE
            CASE 
                WHEN CIR."Nemotecnico" LIKE '%PDBC%' AND CIR."IdMonedaInstrReajuste" = 'CLP' THEN 30
                WHEN CIR."IdClasificacionTipoInstrumento" = 'IF' AND CIR."IdMonedaInstrReajuste" = 'CLP' AND INS."Reajustable" = 'N' THEN 30
                WHEN CIR."IdClasificacionTipoInstrumento" = 'IF' THEN 360
                WHEN CIR."IdClasificacionTipoInstrumento" = 'RF' AND INS."IndicadorBolson" = 'S' THEN 365
                WHEN CIR."IdClasificacionTipoInstrumento" = 'RF' THEN 360
                ELSE 0
            END
    END AS "BaseTasa",
    CIR."ValorizacionGarantia" AS "ValorizacionCierre",
    CASE 
        WHEN CIR."CodigoPaisEmisionInstrumento" IS NULL THEN (CASE WHEN CIR."ISIN" IS NOT NULL AND CIR."ISIN" != '' THEN SUBSTRING(CIR."ISIN", 0, 2)  ELSE 'CL' END)
        ELSE CIR."CodigoPaisEmisionInstrumento"
    END AS "CodigoPaisEmisionInstrumento",
    CIR."FuentePreciosGarantia",
    CIR."Duration",
    CASE 
        WHEN IndPer."ValorIndicador" = 'S' THEN 'RE'
        ELSE 'NR'
    END AS "Relacionado",
    CASE 
    	WHEN CIR."IdClasificacionTipoInstrumento" = 'FM' THEN LPAD(LEFT(COALESCE (FD_FM."CodigoSVS",''), INSTR(COALESCE(FD_FM."CodigoSVS",''), '-') - 1), 9, '0')
    	WHEN CIR."IdTipoInstrumento" IN ('CFI', 'CFM') THEN LPAD(LEFT(COALESCE (INSTRU."DocumentoIdentidadFondo", ''), INSTR(COALESCE(INSTRU."DocumentoIdentidadFondo", ''), '-') - 1), 9, '0')
    	ELSE LPAD(LEFT(COALESCE (DOC."DocumentoIdentidad",''), INSTR(COALESCE(DOC."DocumentoIdentidad",''), '-') - 1), 9, '0')
    END AS "RutCliente",
    CASE 
    	WHEN CIR."IdClasificacionTipoInstrumento" = 'FM' THEN RIGHT(COALESCE(FD_FM."CodigoSVS",''), 1)
    	WHEN CIR."IdTipoInstrumento" IN ('CFI', 'CFM') THEN RIGHT(COALESCE(INSTRU."DocumentoIdentidadFondo",''), 1)
    	ELSE RIGHT(COALESCE(DOC."DocumentoIdentidad",''), 1)
    END AS "Digito",
    RPAD(LTRIM(COALESCE(PER_EMI."RazonSocial",'')), 100, ' ') AS "NombrePersona",
    CIR."IdEmisor",
    CIR."IdOrigen",
    CIR."IdEstado",
    CIR."IdInstrumento"
FROM 
    "BSCL.Custodia.BD::CUS.CircularCarteraCMF" CIR
LEFT JOIN 
    "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON CIR."IdCliente" = CLI."IdCliente"
LEFT JOIN 
    "BSCL.SocioNegocio.BD::PER.Persona" AS PER ON CLI."IdPersona" = PER."IdPersona"
LEFT JOIN 
    "BSCL.Plataforma.BD::REG.Pais" AS PaisRes ON PER."IdPaisResidencia" = PaisRes."IdPais"
LEFT JOIN 
    "BSCL.Plataforma.BD::INS.Instrumento" AS INS ON CIR."Nemotecnico" = INS."Nemotecnico"
LEFT JOIN 
    "BSCL.Plataforma.BD::REG.Pais" AS PaisEmi ON INS."IdPaisEmisor" = PaisEmi."IdPais"
LEFT JOIN 
    "BSCL.Plataforma.BD::MER.Emisor" AS EMI ON CIR."IdEmisor" = EMI."IdEmisor"
LEFT JOIN 
    "BSCL.SocioNegocio.BD::PER.Persona" AS PER_EMI ON EMI."IdPersona" = PER_EMI."IdPersona"
LEFT JOIN 
    "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS DOC ON EMI."IdPersona" = DOC."IdPersona" AND DOC."Predeterminado" = 'S'
LEFT JOIN 
	"BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPersona" AS IndPer ON EMI."IdPersona" = IndPer."IdPersona" AND IndPer."CodigoTipoIndicador" ='RELCORRPER'
LEFT JOIN 
    "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" AS SerieFon ON CIR."Nemotecnico" = SerieFon."IdSerieFondo" 
LEFT JOIN 
    "BSCL.Normativa.BD::DOC.Homologacion" HOMO_EMISION
    ON PaisEmi."CodigoPais" = HOMO_EMISION."ValorOrigen"
    AND HOMO_EMISION."Tipo" = 'PAISCONTRACMF'
LEFT JOIN 
    "BSCL.Plataforma.BD::INS.Corredora" AS COR ON PER."IdPersona" = COR."IdPersona"
LEFT JOIN 
    "BSCL.Normativa.BD::DOC.Homologacion" HOMO_RESIDENCIA
    ON PaisRes."CodigoPais" = HOMO_RESIDENCIA."ValorOrigen"
    AND HOMO_RESIDENCIA."Tipo" = 'PAISCONTRACMF'
LEFT JOIN 
    "BSCL.Normativa.BD::DOC.Homologacion" HOMO_TIPO_INSTRUMENTO
    ON CIR."IdTipoInstrumento" = HOMO_TIPO_INSTRUMENTO."ValorOrigen"
    AND HOMO_TIPO_INSTRUMENTO."Tipo" = 'INSTRUMENTOCMF'
LEFT JOIN 
    "BSCL.Normativa.BD::DOC.Homologacion" HOMO_TIPO_INSTRUMENTO_DES
    ON CIR."IdTipoInstrumentoCMF" = HOMO_TIPO_INSTRUMENTO_DES."ValorDestino"
    AND HOMO_TIPO_INSTRUMENTO_DES."Tipo" = 'INSTRUMENTOCMF'
LEFT JOIN "BSCL.Normativa.BD::DOC.Homologacion" AS HOMO_MONEDA 
	ON HOMO_MONEDA."ValorOrigen" =  CIR."IdMonedaInstrReajuste"
    AND HOMO_MONEDA."Esquema" ='CMF'
    AND HOMO_MONEDA."Tipo" = 'MONEDAPRINCIPAL'
LEFT JOIN "BSCL.Normativa.BD::DOC.Homologacion" AS HOMO_TIPO_CLASIFICACIONRIESGO 
	ON HOMO_TIPO_CLASIFICACIONRIESGO."ValorOrigen" = TRIM(CIR."ClasificacionRiesgoInstrumento")
    AND HOMO_TIPO_CLASIFICACIONRIESGO."Esquema" ='CMF'
    AND HOMO_TIPO_CLASIFICACIONRIESGO."Tipo" = 'CLASIFICACIONRIESGO'
LEFT JOIN "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" AS SER ON SER."IdSerieFondo" = CIR."Nemotecnico"
LEFT JOIN "BSCL.FondoMutuo.BD::FMM.Fondo" AS FD ON FD."IdFondo" = SER."IdFondo"
LEFT JOIN "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" AS SER_FM ON SER_FM."NemoCMF" = CIR."Nemotecnico"
LEFT JOIN "BSCL.FondoMutuo.BD::FMM.Fondo" AS FD_FM ON FD_FM."IdFondo" = SER_FM."IdFondo"
LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS INSTRU ON INSTRU."Nemotecnico" = CIR."Nemotecnico"
WHERE CIR."IdEstado" IN ('ACT', 'DES', 'NEG')

   UNION
   
   SELECT 
    CIR."Fecha", 
    CIR."ClasificacionInstrumentoPrestamo" AS "IdClasificacionTipoInstrumentoModificado",
    CIR."Nemotecnico",
    CIR."ISIN",
    CIR."IdCliente",
    CASE 
        WHEN CIR."CodigoPais" IS NULL OR CIR."CodigoPais" = '' THEN
            CASE 
                WHEN HOMO_RESIDENCIA."ValorOrigen" IS NOT NULL THEN HOMO_RESIDENCIA."ValorDestino"
                ELSE NULL
            END
        ELSE CIR."CodigoPais"
    END AS "CodigoPais",
    CASE 
    	WHEN CIR."CantidadPrestamo" < 0 THEN 'OR'
    	ELSE 'PR'
    END AS "SituacionInstrumento",
    'PR' AS "SituacionInstrumentoAux",
    COALESCE(HOMO_TIPO_INSTRUMENTO."ValorDestino", CIR."IdTipoInstrumento") AS "IdTipoInstrumento",
    CASE
	    WHEN COALESCE(HOMO_TIPO_INSTRUMENTO_DES."ValorDestino", HOMO_TIPO_INSTRUMENTO."ValorDestino") IN ('BBPNEE','BBP') THEN '1999-01-01'
	    ELSE CIR."FechaVencimiento"
    END AS "FechaVencimiento",
	CAST(CIR."CantidadPrestamo" AS DECIMAL(26,4)) AS "CantidadUnidades",
    CASE
        WHEN TRIM(CIR."ClasificacionRiesgoInstrumento") IN ('N', 'BC') THEN ''
        ELSE COALESCE(HOMO_TIPO_CLASIFICACIONRIESGO."ValorDestino", CIR."ClasificacionRiesgoInstrumento")
    END AS "ClasificacionRiesgoInstrumento",
    CIR."TendenciaDeRiesgo" AS "TendenciaClasificacionriesgo",
    CASE
        WHEN CIR."IdMonedaInstrReajuste" = HOMO_MONEDA."ValorOrigen" THEN HOMO_MONEDA."ValorDestino"
        ELSE CIR."IdMonedaPrincipal"
    END AS "IdMonedaPrincipal", 
    CASE
        WHEN CIR."IdClasificacionTipoInstrumento" = 'RV' THEN CIR."PrecioMercadoCustodia"
        WHEN CIR."IdClasificacionTipoInstrumento" = 'RF' THEN CIR."TasaMercadoCustodia"
        WHEN CIR."IdClasificacionTipoInstrumento" = 'IF' THEN CIR."TasaMercadoCustodia"
        WHEN CIR."IdClasificacionTipoInstrumento" = 'FM' THEN CIR."PrecioMercadoCustodia"
        ELSE 0
    END AS "Tir",
    CASE
        WHEN CIR."CodigoValorizacion" != '' THEN CIR."CodigoValorizacion"
        WHEN CIR."CodigoValorizacion" = '' AND CIR."IdClasificacionTipoInstrumento" IN ('RV','FM') THEN 'PR'
        WHEN CIR."CodigoValorizacion" = '' AND CIR."IdClasificacionTipoInstrumento" IN ('RF','IF') THEN 'TR'
        ELSE '' 
    END AS "CodigoValorizacion",
    CASE 
        WHEN CIR."BaseTasa" IS NOT NULL THEN CIR."BaseTasa"
        ELSE
            CASE 
                WHEN CIR."Nemotecnico" LIKE '%PDBC%' AND CIR."IdMonedaInstrReajuste" = 'CLP' THEN 30
                WHEN CIR."IdClasificacionTipoInstrumento" = 'IF' AND CIR."IdMonedaInstrReajuste" = 'CLP' AND INS."Reajustable" = 'N' THEN 30
                WHEN CIR."IdClasificacionTipoInstrumento" = 'IF' THEN 360
                WHEN CIR."IdClasificacionTipoInstrumento" = 'RF' AND INS."IndicadorBolson" = 'S' THEN 365
                WHEN CIR."IdClasificacionTipoInstrumento" = 'RF' THEN 360
                ELSE 0
            END
    END AS "BaseTasa",
    CIR."ValorizacionPrestamo" AS "ValorizacionCierre",
    CASE 
        WHEN CIR."CodigoPaisEmisionInstrumento" IS NULL THEN (CASE WHEN CIR."ISIN" IS NOT NULL AND CIR."ISIN" != '' THEN SUBSTRING(CIR."ISIN", 0, 2)  ELSE 'CL' END)
        ELSE CIR."CodigoPaisEmisionInstrumento"
    END AS "CodigoPaisEmisionInstrumento",
    CIR."FuentePreciosPrestamo",
    CIR."Duration",
    CASE 
        WHEN IndPer."ValorIndicador" = 'S' THEN 'RE'
        ELSE 'NR'
    END AS "Relacionado",
    CASE 
    	WHEN CIR."IdClasificacionTipoInstrumento" = 'FM' THEN LPAD(LEFT(COALESCE (FD_FM."CodigoSVS",''), INSTR(COALESCE(FD_FM."CodigoSVS",''), '-') - 1), 9, '0')
    	WHEN CIR."IdTipoInstrumento" IN ('CFI', 'CFM') THEN LPAD(LEFT(COALESCE (INSTRU."DocumentoIdentidadFondo", ''), INSTR(COALESCE(INSTRU."DocumentoIdentidadFondo", ''), '-') - 1), 9, '0')
    	ELSE LPAD(LEFT(COALESCE (DOC."DocumentoIdentidad",''), INSTR(COALESCE(DOC."DocumentoIdentidad",''), '-') - 1), 9, '0')
    END AS "RutCliente",
    CASE 
    	WHEN CIR."IdClasificacionTipoInstrumento" = 'FM' THEN RIGHT(COALESCE(FD_FM."CodigoSVS",''), 1)
    	WHEN CIR."IdTipoInstrumento" IN ('CFI', 'CFM') THEN RIGHT(COALESCE(INSTRU."DocumentoIdentidadFondo",''), 1)
    	ELSE RIGHT(COALESCE(DOC."DocumentoIdentidad",''), 1)
    END AS "Digito",
    RPAD(LTRIM(COALESCE(PER_EMI."RazonSocial",'')), 100, ' ') AS "NombrePersona",
    CIR."IdEmisor",
    CIR."IdOrigen",
    CIR."IdEstado",
    CIR."IdInstrumento"
FROM 
    "BSCL.Custodia.BD::CUS.CircularCarteraCMF" CIR
LEFT JOIN 
    "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON CIR."IdCliente" = CLI."IdCliente"
LEFT JOIN 
    "BSCL.SocioNegocio.BD::PER.Persona" AS PER ON CLI."IdPersona" = PER."IdPersona"
LEFT JOIN 
    "BSCL.Plataforma.BD::REG.Pais" AS PaisRes ON PER."IdPaisResidencia" = PaisRes."IdPais"
LEFT JOIN 
    "BSCL.Plataforma.BD::MER.Emisor" AS EMI ON CIR."IdEmisor" = EMI."IdEmisor"
LEFT JOIN 
    "BSCL.SocioNegocio.BD::PER.Persona" AS PER_EMI ON EMI."IdPersona" = PER_EMI."IdPersona"
LEFT JOIN 
    "BSCL.Plataforma.BD::INS.Instrumento" AS INS ON CIR."Nemotecnico" = INS."Nemotecnico"
LEFT JOIN 
    "BSCL.Plataforma.BD::REG.Pais" AS PaisEmi ON INS."IdPaisEmisor" = PaisEmi."IdPais"
LEFT JOIN 
    "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS DOC ON EMI."IdPersona" = DOC."IdPersona" AND DOC."Predeterminado" = 'S'
LEFT JOIN 
	"BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPersona" AS IndPer ON EMI."IdPersona" = IndPer."IdPersona" AND IndPer."CodigoTipoIndicador" ='RELCORRPER'
LEFT JOIN 
    "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" AS SerieFon ON CIR."Nemotecnico" = SerieFon."IdSerieFondo" 
LEFT JOIN 
    "BSCL.Normativa.BD::DOC.Homologacion" HOMO_EMISION
    ON PaisEmi."CodigoPais" = HOMO_EMISION."ValorOrigen"
    AND HOMO_EMISION."Tipo" = 'PAISCONTRACMF'
LEFT JOIN 
    "BSCL.Plataforma.BD::INS.Corredora" AS COR ON PER."IdPersona" = COR."IdPersona"
LEFT JOIN 
    "BSCL.Normativa.BD::DOC.Homologacion" HOMO_RESIDENCIA
    ON PaisRes."CodigoPais" = HOMO_RESIDENCIA."ValorOrigen"
    AND HOMO_RESIDENCIA."Tipo" = 'PAISCONTRACMF'
LEFT JOIN 
    "BSCL.Normativa.BD::DOC.Homologacion" HOMO_TIPO_INSTRUMENTO
    ON CIR."IdTipoInstrumento" = HOMO_TIPO_INSTRUMENTO."ValorOrigen"
    AND HOMO_TIPO_INSTRUMENTO."Tipo" = 'INSTRUMENTOCMF'
LEFT JOIN 
    "BSCL.Normativa.BD::DOC.Homologacion" HOMO_TIPO_INSTRUMENTO_DES
    ON CIR."IdTipoInstrumentoCMF" = HOMO_TIPO_INSTRUMENTO_DES."ValorDestino"
    AND HOMO_TIPO_INSTRUMENTO_DES."Tipo" = 'INSTRUMENTOCMF'
LEFT JOIN "BSCL.Normativa.BD::DOC.Homologacion" AS HOMO_MONEDA 
	ON HOMO_MONEDA."ValorOrigen" =  CIR."IdMonedaInstrReajuste"
    AND HOMO_MONEDA."Esquema" ='CMF'
    AND HOMO_MONEDA."Tipo" = 'MONEDAPRINCIPAL'
LEFT JOIN "BSCL.Normativa.BD::DOC.Homologacion" AS HOMO_TIPO_CLASIFICACIONRIESGO 
	ON HOMO_TIPO_CLASIFICACIONRIESGO."ValorOrigen" = TRIM(CIR."ClasificacionRiesgoInstrumento")
    AND HOMO_TIPO_CLASIFICACIONRIESGO."Esquema" ='CMF'
    AND HOMO_TIPO_CLASIFICACIONRIESGO."Tipo" = 'CLASIFICACIONRIESGO'
LEFT JOIN "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" AS SER ON SER."IdSerieFondo" = CIR."Nemotecnico"
LEFT JOIN "BSCL.FondoMutuo.BD::FMM.Fondo" AS FD ON FD."IdFondo" = SER."IdFondo"
LEFT JOIN "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" AS SER_FM ON SER_FM."NemoCMF" = CIR."Nemotecnico"
LEFT JOIN "BSCL.FondoMutuo.BD::FMM.Fondo" AS FD_FM ON FD_FM."IdFondo" = SER_FM."IdFondo"
LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS INSTRU ON INSTRU."Nemotecnico" = CIR."Nemotecnico"
WHERE CIR."IdEstado" IN ('ACT', 'DES','NEG')
UNION
   SELECT 
    CIR."Fecha", 
    CIR."ClasificacionInstrumentoPlazo" AS "IdClasificacionTipoInstrumentoModificado",
    CIR."Nemotecnico",
    CIR."ISIN",
    CIR."IdCliente",
    CASE 
        WHEN CIR."CodigoPais" IS NULL OR CIR."CodigoPais" = '' THEN
            CASE 
                WHEN HOMO_RESIDENCIA."ValorOrigen" IS NOT NULL THEN HOMO_RESIDENCIA."ValorDestino"
                ELSE NULL
            END
        ELSE CIR."CodigoPais"
    END AS "CodigoPais",
    CASE 
    	WHEN CIR."CantidadPlazoCompra" < 0 THEN 'OR'
    	ELSE 'OP'
    END AS "SituacionInstrumento",
    'OP' AS "SituacionInstrumentoAux",
    CIR."IdTipoInstrumentoPlazoCompra" AS "IdTipoInstrumento", 
    CASE
	    WHEN COALESCE(HOMO_TIPO_INSTRUMENTO_DES."ValorDestino", HOMO_TIPO_INSTRUMENTO."ValorDestino") IN ('BBPNEE','BBP') THEN '1999-01-01'
	    ELSE CIR."FechaVencimiento"
    END AS "FechaVencimiento",
	CAST(CIR."CantidadPlazoCompra" AS DECIMAL(26,4)) AS "CantidadUnidades",
    CASE
        WHEN TRIM(CIR."ClasificacionRiesgoInstrumento") IN ('N', 'BC') THEN ''
        ELSE COALESCE(HOMO_TIPO_CLASIFICACIONRIESGO."ValorDestino", CIR."ClasificacionRiesgoInstrumento")
    END AS "ClasificacionRiesgoInstrumento",
    CIR."TendenciaDeRiesgo" AS "TendenciaClasificacionriesgo",
    CASE
        WHEN CIR."IdMonedaInstrReajuste" = HOMO_MONEDA."ValorOrigen" THEN HOMO_MONEDA."ValorDestino"
        ELSE CIR."IdMonedaPrincipal"
    END AS "IdMonedaPrincipal", 
    CASE
        WHEN CIR."IdClasificacionTipoInstrumento" = 'RV' THEN CIR."PrecioMercadoCustodia"
        WHEN CIR."IdClasificacionTipoInstrumento" = 'RF' THEN CIR."TasaMercadoCustodia"
        WHEN CIR."IdClasificacionTipoInstrumento" = 'IF' THEN CIR."TasaMercadoCustodia"
        WHEN CIR."IdClasificacionTipoInstrumento" = 'FM' THEN CIR."PrecioMercadoCustodia"
        ELSE 0
    END AS "Tir",
    CASE
        WHEN CIR."CodigoValorizacion" != '' THEN CIR."CodigoValorizacion"
        WHEN CIR."CodigoValorizacion" = '' AND CIR."IdClasificacionTipoInstrumento" IN ('RV','FM') THEN 'PR'
        WHEN CIR."CodigoValorizacion" = '' AND CIR."IdClasificacionTipoInstrumento" IN ('RF','IF') THEN 'TR'
        ELSE '' 
    END AS "CodigoValorizacion",
    CASE 
        WHEN CIR."BaseTasa" IS NOT NULL THEN CIR."BaseTasa"
        ELSE
            CASE 
                WHEN CIR."Nemotecnico" LIKE '%PDBC%' AND CIR."IdMonedaInstrReajuste" = 'CLP' THEN 30
                WHEN CIR."IdClasificacionTipoInstrumento" = 'IF' AND CIR."IdMonedaInstrReajuste" = 'CLP' AND INS."Reajustable" = 'N' THEN 30
                WHEN CIR."IdClasificacionTipoInstrumento" = 'IF' THEN 360
                WHEN CIR."IdClasificacionTipoInstrumento" = 'RF' AND INS."IndicadorBolson" = 'S' THEN 365
                WHEN CIR."IdClasificacionTipoInstrumento" = 'RF' THEN 360
                ELSE 0
            END
    END AS "BaseTasa",
    CIR."ValorizacionPlazoCompra" AS "ValorizacionCierre",
    CASE 
        WHEN CIR."CodigoPaisEmisionInstrumento" IS NULL THEN (CASE WHEN CIR."ISIN" IS NOT NULL AND CIR."ISIN" != '' THEN SUBSTRING(CIR."ISIN", 0, 2)  ELSE 'CL' END)
        ELSE CIR."CodigoPaisEmisionInstrumento"
    END AS "CodigoPaisEmisionInstrumento",
    CIR."FuentePreciosPlazo",
    CIR."Duration",
    CASE 
        WHEN IndPer."ValorIndicador" = 'S' THEN 'RE'
        ELSE 'NR'
    END AS "Relacionado",
    CASE 
    	WHEN CIR."IdClasificacionTipoInstrumento" = 'FM' THEN LPAD(LEFT(COALESCE (FD_FM."CodigoSVS",''), INSTR(COALESCE(FD_FM."CodigoSVS",''), '-') - 1), 9, '0')
    	WHEN CIR."IdTipoInstrumento" IN ('CFI', 'CFM') THEN LPAD(LEFT(COALESCE (INSTRU."DocumentoIdentidadFondo", ''), INSTR(COALESCE(INSTRU."DocumentoIdentidadFondo", ''), '-') - 1), 9, '0')
    	ELSE LPAD(LEFT(COALESCE (DOC."DocumentoIdentidad",''), INSTR(COALESCE(DOC."DocumentoIdentidad",''), '-') - 1), 9, '0')
    END AS "RutCliente",
    CASE 
    	WHEN CIR."IdClasificacionTipoInstrumento" = 'FM' THEN RIGHT(COALESCE(FD_FM."CodigoSVS",''), 1)
    	WHEN CIR."IdTipoInstrumento" IN ('CFI', 'CFM') THEN RIGHT(COALESCE(INSTRU."DocumentoIdentidadFondo",''), 1)
    	ELSE RIGHT(COALESCE(DOC."DocumentoIdentidad",''), 1)
    END AS "Digito",
    RPAD(LTRIM(COALESCE(PER_EMI."RazonSocial",'')), 100, ' ') AS "NombrePersona",
    CIR."IdEmisor",
    CIR."IdOrigen",
    CIR."IdEstado",
    CIR."IdInstrumento"
FROM 
    "BSCL.Custodia.BD::CUS.CircularCarteraCMF" CIR
LEFT JOIN 
    "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON CIR."IdCliente" = CLI."IdCliente"
LEFT JOIN 
    "BSCL.SocioNegocio.BD::PER.Persona" AS PER ON CLI."IdPersona" = PER."IdPersona"
LEFT JOIN 
    "BSCL.Plataforma.BD::REG.Pais" AS PaisRes ON PER."IdPaisResidencia" = PaisRes."IdPais"
LEFT JOIN 
    "BSCL.Plataforma.BD::MER.Emisor" AS EMI ON CIR."IdEmisor" = EMI."IdEmisor"
LEFT JOIN 
    "BSCL.SocioNegocio.BD::PER.Persona" AS PER_EMI ON EMI."IdPersona" = PER_EMI."IdPersona"
LEFT JOIN 
    "BSCL.Plataforma.BD::INS.Instrumento" AS INS ON CIR."Nemotecnico" = INS."Nemotecnico"
LEFT JOIN 
    "BSCL.Plataforma.BD::REG.Pais" AS PaisEmi ON INS."IdPaisEmisor" = PaisEmi."IdPais"
LEFT JOIN 
    "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS DOC ON EMI."IdPersona" = DOC."IdPersona" AND DOC."Predeterminado" = 'S'
LEFT JOIN 
	"BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPersona" AS IndPer ON EMI."IdPersona" = IndPer."IdPersona" AND IndPer."CodigoTipoIndicador" ='RELCORRPER'
LEFT JOIN 
    "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" AS SerieFon ON CIR."Nemotecnico" = SerieFon."IdSerieFondo" 
LEFT JOIN 
    "BSCL.Normativa.BD::DOC.Homologacion" HOMO_EMISION
    ON PaisEmi."CodigoPais" = HOMO_EMISION."ValorOrigen"
    AND HOMO_EMISION."Tipo" = 'PAISCONTRACMF'
LEFT JOIN 
    "BSCL.Plataforma.BD::INS.Corredora" AS COR ON PER."IdPersona" = COR."IdPersona"
LEFT JOIN 
    "BSCL.Normativa.BD::DOC.Homologacion" HOMO_RESIDENCIA
    ON PaisRes."CodigoPais" = HOMO_RESIDENCIA."ValorOrigen"
    AND HOMO_RESIDENCIA."Tipo" = 'PAISCONTRACMF'
LEFT JOIN 
    "BSCL.Normativa.BD::DOC.Homologacion" HOMO_TIPO_INSTRUMENTO
    ON CIR."IdTipoInstrumento" = HOMO_TIPO_INSTRUMENTO."ValorOrigen"
    AND HOMO_TIPO_INSTRUMENTO."Tipo" = 'INSTRUMENTOCMF'
LEFT JOIN 
    "BSCL.Normativa.BD::DOC.Homologacion" HOMO_TIPO_INSTRUMENTO_DES
    ON CIR."IdTipoInstrumentoCMF" = HOMO_TIPO_INSTRUMENTO_DES."ValorDestino"
    AND HOMO_TIPO_INSTRUMENTO_DES."Tipo" = 'INSTRUMENTOCMF'
LEFT JOIN "BSCL.Normativa.BD::DOC.Homologacion" AS HOMO_MONEDA 
	ON HOMO_MONEDA."ValorOrigen" =  CIR."IdMonedaInstrReajuste"
    AND HOMO_MONEDA."Esquema" ='CMF'
    AND HOMO_MONEDA."Tipo" = 'MONEDAPRINCIPAL'
LEFT JOIN "BSCL.Normativa.BD::DOC.Homologacion" AS HOMO_TIPO_CLASIFICACIONRIESGO 
	ON HOMO_TIPO_CLASIFICACIONRIESGO."ValorOrigen" = TRIM(CIR."ClasificacionRiesgoInstrumento")
    AND HOMO_TIPO_CLASIFICACIONRIESGO."Esquema" ='CMF'
    AND HOMO_TIPO_CLASIFICACIONRIESGO."Tipo" = 'CLASIFICACIONRIESGO'
LEFT JOIN "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" AS SER ON SER."IdSerieFondo" = CIR."Nemotecnico"
LEFT JOIN "BSCL.FondoMutuo.BD::FMM.Fondo" AS FD ON FD."IdFondo" = SER."IdFondo"
LEFT JOIN "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" AS SER_FM ON SER_FM."NemoCMF" = CIR."Nemotecnico"
LEFT JOIN "BSCL.FondoMutuo.BD::FMM.Fondo" AS FD_FM ON FD_FM."IdFondo" = SER_FM."IdFondo"
LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS INSTRU ON INSTRU."Nemotecnico" = CIR."Nemotecnico"
WHERE CIR."IdEstado" IN ('ACT', 'DES','NEG')
UNION
   SELECT 
    CIR."Fecha", 
    CIR."ClasificacionInstrumentoPlazo" AS "IdClasificacionTipoInstrumentoModificado",
    CIR."Nemotecnico",
    CIR."ISIN",
    CIR."IdCliente",
    CASE 
        WHEN CIR."CodigoPais" IS NULL OR CIR."CodigoPais" = '' THEN
            CASE 
                WHEN HOMO_RESIDENCIA."ValorOrigen" IS NOT NULL THEN HOMO_RESIDENCIA."ValorDestino"
                ELSE NULL
            END
        ELSE CIR."CodigoPais"
    END AS "CodigoPais",
    CASE 
    	WHEN CIR."CantidadPlazoVenta" < 0 THEN 'OR'
    	ELSE 'OP'
    END AS "SituacionInstrumento",
    'OP' AS "SituacionInstrumentoAux",
    CIR."IdTipoInstrumentoPlazoVenta" AS "IdTipoInstrumento",
    CASE
	    WHEN COALESCE(HOMO_TIPO_INSTRUMENTO_DES."ValorDestino", HOMO_TIPO_INSTRUMENTO."ValorDestino") IN ('BBPNEE','BBP') THEN '1999-01-01'
	    ELSE CIR."FechaVencimiento"
    END AS "FechaVencimiento",
	CAST(CIR."CantidadPlazoVenta" AS DECIMAL(26,4)) AS "CantidadUnidades",
    CASE
        WHEN TRIM(CIR."ClasificacionRiesgoInstrumento") IN ('N', 'BC') THEN ''
        ELSE COALESCE(HOMO_TIPO_CLASIFICACIONRIESGO."ValorDestino", CIR."ClasificacionRiesgoInstrumento")
    END AS "ClasificacionRiesgoInstrumento",
    CIR."TendenciaDeRiesgo" AS "TendenciaClasificacionriesgo",
    CASE
        WHEN CIR."IdMonedaInstrReajuste" = HOMO_MONEDA."ValorOrigen" THEN HOMO_MONEDA."ValorDestino"
        ELSE CIR."IdMonedaPrincipal"
    END AS "IdMonedaPrincipal", 
    CASE
        WHEN CIR."IdClasificacionTipoInstrumento" = 'RV' THEN CIR."PrecioMercadoCustodia"
        WHEN CIR."IdClasificacionTipoInstrumento" = 'RF' THEN CIR."TasaMercadoCustodia"
        WHEN CIR."IdClasificacionTipoInstrumento" = 'IF' THEN CIR."TasaMercadoCustodia"
        WHEN CIR."IdClasificacionTipoInstrumento" = 'FM' THEN CIR."PrecioMercadoCustodia"
        ELSE 0
    END AS "Tir",
    CASE
        WHEN CIR."CodigoValorizacion" != '' THEN CIR."CodigoValorizacion"
        WHEN CIR."CodigoValorizacion" = '' AND CIR."IdClasificacionTipoInstrumento" IN ('RV','FM') THEN 'PR'
        WHEN CIR."CodigoValorizacion" = '' AND CIR."IdClasificacionTipoInstrumento" IN ('RF','IF') THEN 'TR'
        ELSE '' 
    END AS "CodigoValorizacion",
    CASE 
        WHEN CIR."BaseTasa" IS NOT NULL THEN CIR."BaseTasa"
        ELSE
            CASE 
                WHEN CIR."Nemotecnico" LIKE '%PDBC%' AND CIR."IdMonedaInstrReajuste" = 'CLP' THEN 30
                WHEN CIR."IdClasificacionTipoInstrumento" = 'IF' AND CIR."IdMonedaInstrReajuste" = 'CLP' AND INS."Reajustable" = 'N' THEN 30
                WHEN CIR."IdClasificacionTipoInstrumento" = 'IF' THEN 360
                WHEN CIR."IdClasificacionTipoInstrumento" = 'RF' AND INS."IndicadorBolson" = 'S' THEN 365
                WHEN CIR."IdClasificacionTipoInstrumento" = 'RF' THEN 360
                ELSE 0
            END
    END AS "BaseTasa",
    CIR."ValorizacionPlazoVenta" AS "ValorizacionCierre",
    CASE 
        WHEN CIR."CodigoPaisEmisionInstrumento" IS NULL THEN (CASE WHEN CIR."ISIN" IS NOT NULL AND CIR."ISIN" != '' THEN SUBSTRING(CIR."ISIN", 0, 2)  ELSE 'CL' END)
        ELSE CIR."CodigoPaisEmisionInstrumento"
    END AS "CodigoPaisEmisionInstrumento",
    CIR."FuentePreciosPlazo",
    CIR."Duration",
    CASE 
        WHEN IndPer."ValorIndicador" = 'S' THEN 'RE'
        ELSE 'NR'
    END AS "Relacionado",
    CASE 
    	WHEN CIR."IdClasificacionTipoInstrumento" = 'FM' THEN LPAD(LEFT(COALESCE (FD_FM."CodigoSVS",''), INSTR(COALESCE(FD_FM."CodigoSVS",''), '-') - 1), 9, '0')
    	WHEN CIR."IdTipoInstrumento" IN ('CFI', 'CFM') THEN LPAD(LEFT(COALESCE (INSTRU."DocumentoIdentidadFondo", ''), INSTR(COALESCE(INSTRU."DocumentoIdentidadFondo", ''), '-') - 1), 9, '0')
    	ELSE LPAD(LEFT(COALESCE (DOC."DocumentoIdentidad",''), INSTR(COALESCE(DOC."DocumentoIdentidad",''), '-') - 1), 9, '0')
    END AS "RutCliente",
    CASE 
    	WHEN CIR."IdClasificacionTipoInstrumento" = 'FM' THEN RIGHT(COALESCE(FD_FM."CodigoSVS",''), 1)
    	WHEN CIR."IdTipoInstrumento" IN ('CFI', 'CFM') THEN RIGHT(COALESCE(INSTRU."DocumentoIdentidadFondo",''), 1)
    	ELSE RIGHT(COALESCE(DOC."DocumentoIdentidad",''), 1)
    END AS "Digito",
    RPAD(LTRIM(COALESCE(PER_EMI."RazonSocial",'')), 100, ' ') AS "NombrePersona",
    CIR."IdEmisor",
    CIR."IdOrigen",
    CIR."IdEstado",
    CIR."IdInstrumento"
FROM 
    "BSCL.Custodia.BD::CUS.CircularCarteraCMF" CIR
LEFT JOIN 
    "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON CIR."IdCliente" = CLI."IdCliente"
LEFT JOIN 
    "BSCL.SocioNegocio.BD::PER.Persona" AS PER ON CLI."IdPersona" = PER."IdPersona"
LEFT JOIN 
    "BSCL.Plataforma.BD::REG.Pais" AS PaisRes ON PER."IdPaisResidencia" = PaisRes."IdPais"
LEFT JOIN 
    "BSCL.Plataforma.BD::MER.Emisor" AS EMI ON CIR."IdEmisor" = EMI."IdEmisor"
LEFT JOIN 
    "BSCL.SocioNegocio.BD::PER.Persona" AS PER_EMI ON EMI."IdPersona" = PER_EMI."IdPersona"
LEFT JOIN 
    "BSCL.Plataforma.BD::INS.Instrumento" AS INS ON CIR."Nemotecnico" = INS."Nemotecnico"
LEFT JOIN 
    "BSCL.Plataforma.BD::REG.Pais" AS PaisEmi ON INS."IdPaisEmisor" = PaisEmi."IdPais"
LEFT JOIN 
    "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS DOC ON EMI."IdPersona" = DOC."IdPersona" AND DOC."Predeterminado" = 'S'
LEFT JOIN 
	"BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPersona" AS IndPer ON EMI."IdPersona" = IndPer."IdPersona" AND IndPer."CodigoTipoIndicador" ='RELCORRPER'
LEFT JOIN 
    "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" AS SerieFon ON CIR."Nemotecnico" = SerieFon."IdSerieFondo" 
LEFT JOIN 
    "BSCL.Normativa.BD::DOC.Homologacion" HOMO_EMISION
    ON PaisEmi."CodigoPais" = HOMO_EMISION."ValorOrigen"
    AND HOMO_EMISION."Tipo" = 'PAISCONTRACMF'
LEFT JOIN 
    "BSCL.Plataforma.BD::INS.Corredora" AS COR ON PER."IdPersona" = COR."IdPersona"
LEFT JOIN 
    "BSCL.Normativa.BD::DOC.Homologacion" HOMO_RESIDENCIA
    ON PaisRes."CodigoPais" = HOMO_RESIDENCIA."ValorOrigen"
    AND HOMO_RESIDENCIA."Tipo" = 'PAISCONTRACMF'
LEFT JOIN 
    "BSCL.Normativa.BD::DOC.Homologacion" HOMO_TIPO_INSTRUMENTO
    ON CIR."IdTipoInstrumento" = HOMO_TIPO_INSTRUMENTO."ValorOrigen"
    AND HOMO_TIPO_INSTRUMENTO."Tipo" = 'INSTRUMENTOCMF'
LEFT JOIN 
    "BSCL.Normativa.BD::DOC.Homologacion" HOMO_TIPO_INSTRUMENTO_DES
    ON CIR."IdTipoInstrumentoCMF" = HOMO_TIPO_INSTRUMENTO_DES."ValorDestino"
    AND HOMO_TIPO_INSTRUMENTO_DES."Tipo" = 'INSTRUMENTOCMF'
LEFT JOIN "BSCL.Normativa.BD::DOC.Homologacion" AS HOMO_MONEDA 
	ON HOMO_MONEDA."ValorOrigen" =  CIR."IdMonedaInstrReajuste"
    AND HOMO_MONEDA."Esquema" ='CMF'
    AND HOMO_MONEDA."Tipo" = 'MONEDAPRINCIPAL'
LEFT JOIN "BSCL.Normativa.BD::DOC.Homologacion" AS HOMO_TIPO_CLASIFICACIONRIESGO 
	ON HOMO_TIPO_CLASIFICACIONRIESGO."ValorOrigen" = TRIM(CIR."ClasificacionRiesgoInstrumento")
    AND HOMO_TIPO_CLASIFICACIONRIESGO."Esquema" ='CMF'
    AND HOMO_TIPO_CLASIFICACIONRIESGO."Tipo" = 'CLASIFICACIONRIESGO'
LEFT JOIN "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" AS SER ON SER."IdSerieFondo" = CIR."Nemotecnico"
LEFT JOIN "BSCL.FondoMutuo.BD::FMM.Fondo" AS FD ON FD."IdFondo" = SER."IdFondo"
LEFT JOIN "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" AS SER_FM ON SER_FM."NemoCMF" = CIR."Nemotecnico"
LEFT JOIN "BSCL.FondoMutuo.BD::FMM.Fondo" AS FD_FM ON FD_FM."IdFondo" = SER_FM."IdFondo"
LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS INSTRU ON INSTRU."Nemotecnico" = CIR."Nemotecnico"
WHERE CIR."IdEstado" IN ('ACT', 'DES','NEG')
   )
WHERE
    "IdTipoInstrumento" NOT IN ('PRT')
    AND "Nemotecnico" NOT IN (SELECT * FROM "BSCL.Normativa.BD.Utilitarios::NOR.SplitTextBySystemParams"('EXCLUIR_INSTRUMENTOS', ';', 'PARAMETRO'))
GROUP BY
"Fecha",
"IdClasificacionTipoInstrumentoModificado",
"Nemotecnico",
"CodigoPais",
"ISIN",
"IdCliente",
"SituacionInstrumento",
"SituacionInstrumentoAux",
"IdTipoInstrumento",
"FechaVencimiento",
"IdMonedaPrincipal",
"Tir",
"CodigoValorizacion",
"BaseTasa",
"CodigoPaisEmisionInstrumento",
"Relacionado",
"RutCliente",
"Digito",
"NombrePersona",
"IdEmisor",
"IdOrigen",
"IdEstado",
"IdInstrumento"
HAVING
    SUM("CantidadUnidades") > 0
ORDER BY "Nemotecnico"
