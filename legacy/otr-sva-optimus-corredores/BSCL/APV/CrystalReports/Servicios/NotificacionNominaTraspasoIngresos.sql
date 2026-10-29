SET SCHEMA BSCL;

DROP PROCEDURE "BSCL.APV.CrystalReports.Servicios::NotificacionNominaTraspasoIngresos";
--
--  Proposito: Servicio para generar el contratosSAAPV
--  Autor: EORTIZ - PRONETIT
--  Fecha Creacion: 2018

CREATE PROCEDURE "BSCL.APV.CrystalReports.Servicios::NotificacionNominaTraspasoIngresos"(
    -- ID empresa del login.
    IN p_IdEmpresa                      BIGINT       ,
    -- ID usuario del login.
    IN p_IdUsuario                      BIGINT       ,
    -- ID modulo invocador.
    IN p_IdModulo                       NVARCHAR(3)  ,
    
    IN p_Version                        NVARCHAR(10) ,
    
    IN p_Todos                          NVARCHAR(1) DEFAULT 'S',
    
    IN p_IdCliente                      BIGINT      ,
    
    IN p_IdPortafolio                   BIGINT      DEFAULT 0,
    
    IN p_IdContrato						BIGINT   
    
)
    LANGUAGE sqlscript
    SQL security invoker
    DEFAULT SCHEMA "BSCL"
AS
BEGIN
    --DECLARE Manejo de Errores
    --------------------
    DECLARE v_out_AdvertenciasDetalle   TABLE ("IdInterno" BIGINT, "Codigo" nvarchar(100),"RequiereAutorizacion" NVARCHAR(1),"ValorParametro" nvarchar(100),"NombreParametro" NVARCHAR(40));
    DECLARE v_out_ErroresDetalle        TABLE ("IdInterno" BIGINT, "Codigo" nvarchar(100),"ValorParametro" nvarchar(100),"NombreParametro" NVARCHAR(40));
    BEGIN
        v_out_AdvertenciasDetalle = SELECT 0 as "IdInterno" ,'' as "Codigo",'N' as "RequiereAutorizacion",'' as "ValorParametro",'' as "NombreParametro" from DUMMY where 1=2;
        v_out_ErroresDetalle = SELECT 0 as "IdInterno" ,'' as "Codigo",'' as "ValorParametro",'' as "NombreParametro" from DUMMY where 1=2;
    END;
    BEGIN
        BEGIN
            DECLARE v_resultadoRegla    NVARCHAR(40);
            DECLARE v_FechaSistema      NVARCHAR(10);
            
            --CALL "BSCL.Plataforma.BD.Servicios::Depuracion"(:p_IdEmpresa,CURRENT_UTCTIMESTAMP,:p_IdUsuario,'DJ1899.GenerarCertificado24','INICIO','INICIO');
                
            --CALL "BSCL.Plataforma.BD.Reglas::ServicioCumpleRequisitosBasicos"(:p_IdEmpresa,:p_IdUsuario,:p_IdModulo,:v_out_ErroresDetalle_Basicos,:v_resultadoRegla);
            
           -- IF :v_resultadoRegla = 'RECH' THEN
              --  v_out_ErroresDetalle = SELECT 0 as "IdInterno", "Codigo", '' as "ValorParametro",'' as "NombreParametro" FROM :v_out_ErroresDetalle_Basicos;
            --END IF;
            
            --CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaSistema" (:p_IdEmpresa,v_FechaSistema);
   
            --CALL "BSCL.Plataforma.BD.Servicios::Depuracion"(:p_IdEmpresa,CURRENT_UTCTIMESTAMP,:p_IdUsuario,'DJ1899.GenerarCertificado24','FIN','FIN');
            
            v_Traspasos =
				SELECT
                	--Trabajador Traspaso
                    '7016566-K' AS "RUT",
                    'JAIME GONZALO BLANCO BARRIO' AS "AporteEmpleador",
                    --Deposito Convenido
                    0 		 AS "SaldoAnteriorDP",
                    0 		 AS "SaldoPosteriorDP",
                    0 		 AS "SaldoPosteriorCircularDP",
                    --Deposito APV
                    0 		 AS "SaldoAnteriorAPV",
                    18804745 AS "RegimenTributarioB",
                    0 		 AS "RegimenTributarioA",
                    0		 AS "BonificacionFiscal",
                    --
                    0 		 AS "APV",
                    18804745 AS "TOTAL",
                    5009554	 AS "FOLIOSAAPV"
               FROM dummy UNION ALL 
               SELECT
                	--Trabajador Traspaso
                    '7025957-5' AS "RUT",
                    'CONSTANZO JOSE SCARELLA GUTIERREZ' AS "AporteEmpleador",
                    --Deposito Convenido
                    0 		 AS "SaldoAnteriorDP",
                    0 		 AS "SaldoPosteriorDP",
                    0 		 AS "SaldoPosteriorCircularDP",
                    --Deposito APV
                    0 		 AS "SaldoAnteriorAPV",
                    16977095 AS "RegimenTributarioB",
                    0 		 AS "RegimenTributarioA",
                    0		 AS "BonificacionFiscal",
                    --
                    0 		 AS "APV",
                    16977095 AS "TOTAL",
                    5009491	 AS "FOLIOSAAPV"
               FROM dummy UNION ALL 
               SELECT
                	--Trabajador Traspaso
                    '9296820-0' AS "RUT",
                    'LUIS ALARCANIO ARAYA ALIAGA' AS "AporteEmpleador",
                    --Deposito Convenido
                    0 		 AS "SaldoAnteriorDP",
                    0 		 AS "SaldoPosteriorDP",
                    0 		 AS "SaldoPosteriorCircularDP",
                    --Deposito APV
                    0 		 AS "SaldoAnteriorAPV",
                    0		 AS "RegimenTributarioB",
                    5208974	 AS "RegimenTributarioA",
                    611600	 AS "BonificacionFiscal",
                    --
                    0 		 AS "APV",
                    5820574  AS "TOTAL",
                    942901	 AS "FOLIOSAAPV"
               FROM dummy UNION ALL 
               SELECT
                	--Trabajador Traspaso
                    '17736241-7' AS "RUT",
                    'NIKOL ALEJANDRA CAMPILLAY BARRERA' AS "AporteEmpleador",
                    --Deposito Convenido
                    0 		 AS "SaldoAnteriorDP",
                    0 		 AS "SaldoPosteriorDP",
                    0 		 AS "SaldoPosteriorCircularDP",
                    --Deposito APV
                    0 		 AS "SaldoAnteriorAPV",
                    0		 AS "RegimenTributarioB",
                    792632	 AS "RegimenTributarioA",
                    9643	 AS "BonificacionFiscal",
                    --
                    0 		 AS "APV",
                    802275	 AS "TOTAL",
                    5009653	 AS "FOLIOSAAPV"
               FROM dummy;
               
               
            SELECT
                1 AS "IdEmpresa",         
                2019 AS "Anio",--año
                --Institución Autorizada
                'IM TRUST - CREDICORP CAPITAL ///' AS "NombreEntidadRecaudadora", --razon social de la administradora
                '99549940-1' AS "DocumentoIdentidadEntidadRecaudadora", --rut de la administradora
                'SANTIAGO 31/12/2018' AS "CiudadFecha", --ciudad y fecha
                *
            FROM :v_Traspasos AS T; 
        END;
    END;                                
END;
