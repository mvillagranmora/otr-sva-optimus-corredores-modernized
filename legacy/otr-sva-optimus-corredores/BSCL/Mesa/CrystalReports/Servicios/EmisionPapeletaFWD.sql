SET SCHEMA "BSCL";
GO
DROP PROCEDURE "BSCL.Mesa.CrystalReports.Servicios::EmisionPapeletaFWD";

GO
--
--  Proposito: servicio para consultar Emision de Papeleta FWD
--  Autor: EORTIZ - PRONET-IT
--  Fecha Creacion: /10/2016
--

CREATE PROCEDURE "BSCL.Mesa.CrystalReports.Servicios::EmisionPapeletaFWD"(
    -- Parametros entrada
    IN p_IdEmpresa              BIGINT,
    IN p_FolioOperacion         BIGINT
)
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER  
DEFAULT SCHEMA "BSCL"
--READS SQL DATA
AS
BEGIN
    --------------------
    --Variables
    --------------------
    DECLARE varFECHA                DATE;
    DECLARE varUF                   DECIMAL(26,4);
    DECLARE varDOLAR                DECIMAL(26,4);
    DECLARE varExisteOp             INT;
    DECLARE varIdOperacion          BIGINT;
    
    DECLARE v_FechaSistemaUTC       NVARCHAR(40);
    DECLARE v_FechaSistemaLOCAL     NVARCHAR(40);
    DECLARE v_FechaHoraSistemaUTC   NVARCHAR(40);
    DECLARE v_FechaHoraSistemaLOCAL SECONDDATE;
    DECLARE v_FechaHoraUTC          NVARCHAR(40);
    DECLARE v_FechaHoraLOCAL        NVARCHAR(40);
    DECLARE v_TblUF                 TABLE ("varUF" DECIMAL(26,4));
    DECLARE v_TblUS                 TABLE ("varDOLAR" DECIMAL(26,4));
    DECLARE v_SegundosZonaHoraria   BIGINT;

    v_logo =
        SELECT TOP 1 "Binario" AS "LogoCorredora"
        FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarImagen"
        WHERE "NombreImagen" = 'LOGO_CORREDOR' ORDER BY "Version" DESC;
    
    -- Se obtiene la misma FechaHoraSistema que se muestra en la UI
    CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechas"(
        p_IdEmpresa                 => p_IdEmpresa,
        p_out_FechaSistemaUTC       => v_FechaSistemaUTC,
        p_out_FechaSistemaLOCAL     => v_FechaSistemaLOCAL,
        p_out_FechaHoraSistemaUTC   => v_FechaHoraSistemaUTC,
        p_out_FechaHoraSistemaLOCAL => v_FechaHoraSistemaLOCAL,
        p_out_FechaHoraUTC          => v_FechaHoraUTC,
        p_out_FechaHoraLOCAL        => v_FechaHoraLOCAL
    );
    
    CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerConfiguracion"(:p_IdEmpresa, 'PLA.SegundosZonaHoraria', :v_SegundosZonaHoraria);
   
    --Validar SI Existe Operacion
    SELECT count(1) INTO varExisteOp
    FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera" CC
    WHERE CC."IdTipoProducto"   = 'MEFW'
      --AND CC."FolioCartera"   = :p_FolioOperacion
      AND CC."IdCartera"        = :p_FolioOperacion;
    
    --Se obtiene FechaOperacion, IdOperacion
    SELECT coalesce(CC."FechaOperacion",'1900-01-01'),  coalesce(CC."IdOperacion",'0') INTO varFECHA, varIdOperacion
    FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera" CC
    WHERE CC."IdTipoProducto"   = 'MEFW' 
      --AND CC."FolioCartera"   = :p_FolioOperacion
      AND CC."IdCartera"        = :p_FolioOperacion;
        
        
    IF (varExisteOp != 0) THEN
        --DOLAR OBSERVADO
        v_TblUS = 
            SELECT  PTC."TipoCambioMercado" AS "varDOLAR"
            FROM    "BSCL.Mesa.BD::SPT.ParidadYTipoCambio" PTC
            WHERE   PTC."Origen" = 'OBSERVADO'
                    AND PTC."Fecha" = :varFECHA
                    AND PTC."IdMoneda"= 'DO';

        SELECT COUNT(1) INTO varDOLAR FROM :v_TblUS;
        IF varDOLAR > 0 THEN
            SELECT "varDOLAR" INTO varDOLAR FROM :v_TblUS;
        END IF;
        
        --UF        
        v_TblUF =
            SELECT  PTC."TipoCambioMercado" AS "varUF"
            FROM    "BSCL.Mesa.BD::SPT.ParidadYTipoCambio" PTC
            WHERE   PTC."Origen" = 'OBSERVADO'
                    AND PTC."Fecha" = :varFECHA
                    AND PTC."IdMoneda"= 'UF';

        SELECT COUNT(1) INTO varUF FROM :v_TblUF;
        IF varUF > 0 THEN
            SELECT "varUF" INTO varUF FROM :v_TblUF;
        END IF;                 

        
        v_DatosCartera =
            SELECT
                 CC."IdCartera"
                ,CC."IdOperacion"
                ,CC."FolioCartera"              AS "NroOperacion"
                ,CLI."NombreCompleto"           AS "NombreCliente"
                ,DIR."Calle"                    AS "Direccion"
                --,TEL."Numero"                     AS "Fono"
                ,''                             AS "Fono"
                --,CLI."DocumentoIdentidad"         AS "RutCliente"
                ,CASE WHEN COALESCE(CLI."DocumentoIdentidad",'') <> '' AND CLI."TipoDocumentoIdentidad" = 'R' THEN
                        "BSCL.Plataforma.BD.Utilitarios::DarFormatoNumero"(SUBSTR_BEFORE(CLI."DocumentoIdentidad",'-'),'i',0)||
                        '-'||SUBSTR_AFTER(CLI."DocumentoIdentidad",'-')
                      ELSE COALESCE(CLI."DocumentoIdentidad",'-') END   AS "RutCliente"
                ,POR."CodigoPortafolio" ||' '||POR."Proposito"      AS "CodigoPortafolio"
                ,TO_VARCHAR(v_FechaHoraSistemaLOCAL,'DD-MM-YYYY')   AS "FechaImpresion"
                ,TO_VARCHAR(v_FechaHoraSistemaLOCAL,'HH24:MI')      AS "HoraImpresion"
                ,CASE WHEN CC."IdTipoOperacion" = 'COMFWD' THEN 'COMPRA' WHEN CC."IdTipoOperacion" = 'VENFWD' THEN 'VENTA' END AS "TipoOperacion"
                ,CC."IdTipoProducto"            AS "Producto"
                ,CC."IdInstrumento"
                ,CC."TipoInstrumento"
                ,CC."Nemotecnico"
                ,CI."RazonSocial"               AS "NombreInstrumento"
                ,CC."FechaOperacion"            AS "FechaInicio"
                ,TO_VARCHAR(CC."FechaOperacion",'DD-MM-YYYY')   AS "FechaOperacionFormato"
                ,TO_CHAR(ADD_SECONDS(OPE."FechaOperacion",:v_SegundosZonaHoraria), 'HH24:MI:SS') AS "HoraOperacion"
                ,OPE."FechaOperacion"                           AS "FechaOperacionOPE"
                ,CC."FechaVencimientoOperacion" AS "FechaVcto"
                ,TO_VARCHAR(CC."FechaVencimientoOperacion",'DD-MM-YYYY') AS "FechaVctoFormato"
                ,CC."PlazoEnDiasOperacion"      AS "Plazo"
                ,CC."IdMonedaPrincipal"         AS "MonedaOperacion"
                ,CC."IdMonedaSecundaria"        AS "MonedaConversion"
                ,CASE
		        WHEN CC."EGRIdTipoOperacion" = 'ANTFWD' THEN CC."EGRMontoMonedaPrincipal" 
		        ELSE CC."INGMontoMonedaPrincipal" END                          AS "MontoOperacion"
                ,CASE WHEN CC."Nemotecnico" = 'ARBITR' THEN CC."ValorParidadCierre" ELSE CC."ValorTipoCambioCierre" END AS "PrecioFDW"  --depende del instrumento
		        ,CASE
		        WHEN CC."EGRIdTipoOperacion" = 'ANTFWD' THEN CC."EGRMontoMonedaSecundaria" 
		        ELSE CC."INGMontoMonedaSecundaria" END                         AS "MontoFinal"
                ,PA."NombreParametro"           AS "Modalidad"
                ,CC."Glosa"                     AS "Observaciones"
                ,CC."NombreEstado"              AS "Estado"
                ,CC."FechaLiquidacion"
                ,TO_VARCHAR(CC."FechaLiquidacion",'DD-MM-YYYY') AS "FechaLiquidacionFormato"
                ,CASE CC."IdTipoCanalIngreso" WHEN 'MES' THEN 'MESA' ELSE CC."IdTipoCanalIngreso" END AS "Origen"
                ,:varDOLAR                      AS "Obs_Inicio" -- DolarObservado
                ,:varUF                         AS "UF_Inicio"  -- ValorUF dia operacion
                ,US."CodigoUsuario"             AS "UsuarioCreacion"
                ,CC."EGRValorTipoCambioSpot"    AS "TasaCambioSpot"
                ,OPE."MontoOperacionInicial"
                ,CC."INGMontoMonedaSecundaria"  AS "MontoEquivalente"
                ,PA1."NombreParametro"          AS "Fixing"
                ,'' AS "Sistema" --Mercado
                ,'' AS "RutAdministrador" 
                ,'' AS "NombreAdministrador"
                ,:p_IdEmpresa                   AS "Empresa"
                ,CC."ValorParidadCierre"
                ,CC."ValorTipoCambioCierre"
                ,COALESCE(MONPRI."CantidadDecimales",4)    AS "CantidadDecimalesPrincipal"
                ,COALESCE(MONSEC."CantidadDecimales",4)    AS "CantidadDecimalesSecundaria"
                ,COALESCE(MONPRI."MontoDecimales",4)    AS "MontoDecimalesPrincipal"
                ,COALESCE(MONSEC."MontoDecimales",4)    AS "MontoDecimalesSecundaria"
                ,COALESCE(MONPRI."TipoCambioDecimales",4)    AS "TipoCambioDecimalesPrincipal"
                ,COALESCE(MONSEC."TipoCambioDecimales",4)    AS "TipoCambioDecimalesSecundaria"
                ,COALESCE(MONDO."MontoDecimales",4)     AS "MontoDecimalesDO"
                ,COALESCE(MONUF."MontoDecimales",4)     AS "MontoDecimalesUF"
                ,COALESCE(OPE."IdMonedaCompensacion",'') AS "MonedaCompensacion"
            FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera" CC
            INNER JOIN "BSCL.Mesa.BD::MES.Operacion" AS OPE
                ON (OPE."IdOperacion" = CC."IdOperacion" AND OPE."IdTipoProducto" = CC."IdTipoProducto")
            LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" CLI
                ON (CLI."IdCliente" = CC."IdCliente")
            LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionFisica" DIR
                ON (DIR."IdPersona" = CLI."IdPersona")
            --LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaTelefono" TEL
                --ON (TEL."IdPersona" = CLI."IdPersona")
            LEFT JOIN "BSCL.Plataforma.BD::ACC.Usuario" US
                ON (CC."IdUsuario" = US."IdUsuario")
            LEFT JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" CI
                ON (CC."IdInstrumento" = CI."IdInstrumento")
            LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS POR
                ON (POR."IdCliente" = CC."IdCliente" AND POR."IdPortafolio" = CC."IdPortafolio" AND POR."IdEstado" = 'ACT')
            LEFT JOIN "BSCL.Mesa.BD::MES.Parametro" PA
                ON (PA."IdParametro" = CC."IdModalidadCumplimiento")
            LEFT JOIN "BSCL.Mesa.BD::MES.Parametro" PA1
                ON (PA1."IdParametro" = CC."IdFixing")
            LEFT JOIN "BSCL.Plataforma.BD::PLA.MonedaFormato" MONPRI
                ON MONPRI."IdUsoMonedaFormato" = 'FWD' AND MONPRI."IdMoneda" = CC."IdMonedaPrincipal" AND MONPRI."IdTipoOperacion" = CI."Nemotecnico"
            LEFT JOIN "BSCL.Plataforma.BD::PLA.MonedaFormato" MONSEC
                ON MONSEC."IdUsoMonedaFormato" = 'FWD' AND MONSEC."IdMoneda" = CC."IdMonedaSecundaria" AND MONSEC."IdTipoOperacion" = CI."Nemotecnico"
            LEFT JOIN "BSCL.Plataforma.BD::PLA.MonedaFormato" MONDO
                ON (MONDO."IdUsoMonedaFormato" = 'FWD' AND MONDO."IdMoneda" = 'DO')
            LEFT JOIN "BSCL.Plataforma.BD::PLA.MonedaFormato" MONUF
                ON (MONUF."IdUsoMonedaFormato" = 'FWD' AND MONUF."IdMoneda" = 'UF')
            WHERE CC."IdTipoProducto"   = 'MEFW'
              --AND CC."FolioCartera"   = :p_FolioOperacion     -- Se reemplaza por la condicion siguiente
              AND CC."IdCartera"        = :p_FolioOperacion
              AND CC."IdEmpresa"        = :p_IdEmpresa;
        
        
        v_FormaDePago1 =
            SELECT
                TOP 1 "NombreMedioDePago",
                "FolioOperacion",
                "FechaCondicionLiquidacion",
                "IdTipoProducto"
            FROM "BSCL.Mesa.BD.Vistas::ConsultarFormaDePago"
            WHERE "IdTipoMovimiento"= 'MOVEGR'
              AND "FolioOperacion"  = :varIdOperacion
            ORDER BY "FolioOperacion";
        
        v_FormaDePago2 =
            SELECT
                TOP 1 "NombreMedioDePago",
                "FolioOperacion",
                "FechaCondicionLiquidacion",
                "IdTipoProducto"
            FROM "BSCL.Mesa.BD.Vistas::ConsultarFormaDePago"
            WHERE "IdTipoMovimiento"= 'MOVING'
              AND "FolioOperacion"  = :varIdOperacion
            ORDER BY "FolioOperacion";
        
        v_MaxFechaLiquidacion =
            SELECT
                 CC."IdCartera"
                ,CASE WHEN FPE."FechaLiquidacionComprobantePagoOperacion" > FPR."FechaLiquidacionComprobantePagoOperacion" THEN FPE."FechaLiquidacionComprobantePagoOperacion"
                      WHEN FPE."FechaLiquidacionComprobantePagoOperacion" < FPR."FechaLiquidacionComprobantePagoOperacion" THEN FPR."FechaLiquidacionComprobantePagoOperacion"
                      ELSE COALESCE(FPE."FechaLiquidacionComprobantePagoOperacion",FPR."FechaLiquidacionComprobantePagoOperacion") END AS "FechaLiquidacion"
            FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCarteraLite" CC
            LEFT JOIN (
                    SELECT DISTINCT "IdOperacion","FechaLiquidacionComprobantePagoOperacion","IdEmpresa"
                    --FROM "BSCL.Mesa.BD.Vistas::ConsultarComprobantePagoOperacion"
                    FROM "BSCL.Mesa.BD::MES.ComprobantePagoOperacion"
                    WHERE "IdTipoProducto" = 'MEFW' AND "RecibePaga" = 'P'
                ) AS FPE
                ON (FPE."IdOperacion" = CC."IdCartera" AND FPE."IdEmpresa" = CC."IdEmpresa")
            LEFT JOIN (
                    SELECT DISTINCT "IdOperacion","FechaLiquidacionComprobantePagoOperacion","IdEmpresa"
                    --FROM "BSCL.Mesa.BD.Vistas::ConsultarComprobantePagoOperacion"
                    FROM "BSCL.Mesa.BD::MES.ComprobantePagoOperacion"
                    WHERE "IdTipoProducto" = 'MEFW' AND "RecibePaga" = 'R'
                ) AS FPR
               ON (FPR."IdOperacion" = CC."IdCartera" AND FPR."IdEmpresa" = CC."IdEmpresa")
            WHERE CC."IdTipoProducto"   = 'MEFW'
              --AND CC."FolioCartera"   = :p_FolioOperacion     -- Se reemplaza por la condicion siguiente
              AND CC."IdCartera"        = :p_FolioOperacion
              AND CC."IdEmpresa"        = :p_IdEmpresa;
                
                
        v_Papeleta =
            SELECT
                 CC."IdOperacion"
                ,CC."NroOperacion"
                ,CC."NombreCliente"
                ,CC."Direccion"
                ,CC."Fono"
                ,CC."RutCliente"
                ,CC."CodigoPortafolio"
                ,CC."FechaImpresion"
                ,CC."HoraImpresion"
                ,CC."TipoOperacion"
                ,CC."Producto"
                ,CC."IdInstrumento"
                ,CC."TipoInstrumento"
                ,CC."Nemotecnico"
                ,CC."NombreInstrumento"
                ,CC."FechaInicio"
                ,CC."FechaOperacionFormato"
                ,CC."HoraOperacion"
                ,CC."FechaOperacionOPE"
                ,CC."FechaVcto"
                ,CC."FechaVctoFormato"
                ,CC."Plazo"
                ,CC."MonedaOperacion"
                ,CC."MonedaConversion"
                ,CC."MontoOperacion"
                ,CC."PrecioFDW"
                ,CC."MontoFinal"
                ,CC."Modalidad"
                ,CC."Observaciones"
                ,CC."Estado"
                ,FP1."NombreMedioDePago"        AS "NombreFormaPago"
                ,FP1."FechaCondicionLiquidacion" AS "FechaValuta"
                --,CC."FechaLiquidacion"
                ,FLIQ."FechaLiquidacion"
                --,CC."FechaLiquidacionFormato"
                ,TO_VARCHAR(FLIQ."FechaLiquidacion",'DD-MM-YYYY')   AS "FechaLiquidacionFormato"
                ,FP2."NombreMedioDePago"        AS "NombreFormaPago2"
                ,CC."Origen"    
                ,CC."Obs_Inicio"
                ,CC."UF_Inicio"
                ,CC."UsuarioCreacion"           
                ,CC."TasaCambioSpot"
                ,CC."MontoOperacionInicial"
                ,CC."MontoEquivalente"
                ,CC."Fixing"
                ,CC."Sistema"
                ,CC."RutAdministrador" 
                ,CC."NombreAdministrador"
                ,CC."Empresa"
                ,CC."ValorParidadCierre"
                ,CC."ValorTipoCambioCierre"
                ,CC."CantidadDecimalesPrincipal"
                ,CC."CantidadDecimalesSecundaria"
                ,CC."MontoDecimalesPrincipal"
                ,CC."MontoDecimalesSecundaria"
                ,CC."TipoCambioDecimalesPrincipal"
                ,CC."TipoCambioDecimalesSecundaria"
                ,CC."MontoDecimalesDO"
                ,CC."MontoDecimalesUF"
                ,LOGO."LogoCorredora"
                ,CC."MonedaCompensacion"
            FROM :v_DatosCartera CC
            INNER JOIN :v_MaxFechaLiquidacion FLIQ
                ON (CC."IdCartera" = FLIQ."IdCartera")
            LEFT JOIN :v_FormaDePago1 FP1
                ON (FP1."FolioOperacion" = CC."IdOperacion" AND FP1."IdTipoProducto" = CC."Producto")
            LEFT JOIN :v_FormaDePago2 FP2
                ON (FP2."FolioOperacion" = CC."IdOperacion" AND FP2."IdTipoProducto" = CC."Producto")
            LEFT JOIN :v_logo LOGO
                ON (LOGO."LogoCorredora" IS NOT NULL)
            ;
    ELSE
        v_Papeleta =
            SELECT
                 0          AS "IdOperacion"        
                ,0          AS "NroOperacion"
                ,''         AS "NombreCliente"
                ,''         AS "Direccion"
                ,''         AS "Fono"
                ,''         AS "RutCliente"
                ,''         AS "CodigoPortafolio"
                ,''         AS "FechaImpresion"
                ,''         AS "HoraImpresion"
                ,''         AS "TipoOperacion"--formula
                ,''         AS "Producto"
                ,0          AS "IdInstrumento"--"SubProducto"
                ,''         AS "TipoInstrumento"
                ,''         AS "Nemotecnico"
                ,''         AS "NombreInstrumento"
                ,''         AS "FechaInicio"
                ,''         AS "FechaOperacionFormato"
                ,''         AS "HoraOperacion"
                ,''         AS "FechaOperacionOPE"
                ,''         AS "FechaVcto"
                ,''         AS "FechaVctoFormato"
                ,0          AS "Plazo"
                ,''         AS "MonedaOperacion"
                ,''         AS "MonedaConversion"
                ,0          AS "MontoOperacion"
                ,0          AS "PrecioFDW"  --depende del instrumento
                ,0          AS "MontoFinal"
                ,0          AS "Modalidad"
                ,CASE WHEN varExisteOp = 0 THEN 'NO EXISTE OPERACI�N' 
                 WHEN varDOLAR = 0 THEN 'NO EXISTE DOLAR PARA LA FECHA DE OPERACION'
                 WHEN varUF = 0 THEN 'NO EXISTE UF PARA LA FECHA DE OPERACION'
                 ELSE 'ERROR AL OBTENER DATOS DE REPORTE PAPELETE FORWARD' END  AS "Observaciones"
                ,''         AS "Estado"
                ,''         AS "NombreFormaPago"
                ,''         AS "FechaValuta"
                ,''         AS "FechaLiquidacion"
                ,''         AS "FechaLiquidacionFormato"
                ,''         AS "NombreFormaPago2"                                   
                ,''         AS "Origen" 
                ,0          AS "Obs_Inicio" -- DolarObservado
                ,0          AS "UF_Inicio"  -- ValorUF dia operacion
                ,''         AS "UsuarioCreacion"            
                ,0          AS "TasaCambioSpot"
                ,0          AS "MontoOperacionInicial"
                ,0			AS "MontoEquivalente"
                ,0          AS "Fixing"
                ,''         AS "Sistema" --Mercado
                ,''         AS "RutAdministrador" 
                ,''         AS "NombreAdministrador"
                ,0          AS "Empresa"
                ,0          AS "ValorParidadCierre"
                ,0          AS "ValorTipoCambioCierre"
                ,4          AS "CantidadDecimalesPrincipal"
                ,4          AS "CantidadDecimalesSecundaria"
                ,4			AS "MontoDecimalesPrincipal"
                ,4			AS "MontoDecimalesSecundaria"
                ,4			AS "TipoCambioDecimalesPrincipal"
                ,4			AS "TipoCambioDecimalesSecundaria"
                ,4          AS "MontoDecimalesDO"
                ,4          AS "MontoDecimalesUF"
                ,LOGO."LogoCorredora"
                ,''			AS "MonedaCompensacion"
            FROM DUMMY
            LEFT JOIN :v_logo LOGO
                ON (LOGO."LogoCorredora" IS NOT NULL);
    END IF;
        
    --Salida del SP
    SELECT * FROM :v_Papeleta;
    
END;
GO