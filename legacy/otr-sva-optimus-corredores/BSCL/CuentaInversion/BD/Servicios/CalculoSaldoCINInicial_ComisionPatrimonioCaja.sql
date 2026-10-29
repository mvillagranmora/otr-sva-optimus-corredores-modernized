SET SCHEMA "BSCL";
GO
DROP PROCEDURE "BSCL.CuentaInversion.BD.Servicios::CalculoSaldoCINInicial_ComisionPatrimonioCaja";
GO

CREATE PROCEDURE   
-- 
--  Proposito: Realizar carga inicial para calculo de monto patrimonio para comisiones trimestrales
--  Autor: BCS
--  Fecha Creacion: 29/07/2024
--

"BSCL.CuentaInversion.BD.Servicios::CalculoSaldoCINInicial_ComisionPatrimonioCaja" (
    IN p_IdEmpresa BIGINT,
    IN p_IdUsuario BIGINT,
    IN p_IdModulo NVARCHAR(3),
    IN p_SimularOperacion NVARCHAR(1),
    IN p_UsaReglasNegocio NVARCHAR(1),
    IN p_FechaCalculo NVARCHAR(10),
    OUT p_out_Resultado TABLE(
        "IdPortafolio" BIGINT,
        "MontoPatrimonioAfectoComision" DECIMAL(26,8)
    )
)
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER 
DEFAULT SCHEMA "BSCL"
AS
BEGIN
    DECLARE v_out_Resultado TABLE ("IdPortafolio" BIGINT, "MontoPatrimonioAfectoComision" DECIMAL(26,8));
    DECLARE v_IdTrabajoParidad BIGINT := 0;
    DECLARE v_MonedaSistema NVARCHAR(3);
    DECLARE v_IdPaisSistema BIGINT;
    DECLARE v_portafoliosPatrimonio TABLE ("IdPortafolio" BIGINT);
    DECLARE v_saldosMonedaSistemaAgrupados TABLE ("IdPortafolio" BIGINT, "IdMoneda" NVARCHAR(10), "IdCuentaInversion" BIGINT, "CantidadRegistros" BIGINT, "Monto" DECIMAL(26,8), "FechaPatrimonio" SECONDDATE);

    -- DECLARACION DE LA VARIABLE DE TABLA TEMPORAL FUERA DEL CUERPO PRINCIPAL
    DECLARE v_inMonedasTablaPaso TABLE ("Fecha" SECONDDATE, "IdMoneda" NVARCHAR(10));

    -- INICIALIZACION DE RESULTADOS TEMPORALES
    v_out_Resultado = SELECT 0 AS "IdPortafolio", 0 AS "MontoPatrimonioAfectoComision" FROM DUMMY WHERE 1=2;

    -- OBTENER LA MONEDA DEL SISTEMA
    SELECT TOP 1 COALESCE("Valor", '') INTO v_MonedaSistema
    FROM "BSCL.Plataforma.BD::PLA.Configuracion"
    WHERE "IdEmpresa" = :p_IdEmpresa AND "CodigoParametro" = 'PLA.MonedaSistema';


    -- OBTENER EL PAIS DEL SISTEMA
    SELECT "IdPais" INTO v_IdPaisSistema
    FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa"
    WHERE "IdEmpresa" = :p_IdEmpresa;

    -- OBTENER LOS PORTAFOLIOS DE PATRIMONIO
    v_portafoliosPatrimonio = SELECT "IdPortafolio"
                              FROM "BSCL.CuentaInversion.BD::CTA.PatrimonioCaja"
                              WHERE "FechaPatrimonio" = :p_FechaCalculo AND "TipoFechaConsulta" = 'ADQ';

    -- CALCULAR LOS SALDOS EN LA MONEDA DEL SISTEMA AGRUPADOS
    v_saldosMonedaSistemaAgrupados = 
        SELECT   
            PP."IdPortafolio",
            CIN."IdMoneda",
            CIN."IdCuentaInversion",
            COUNT(1) AS "CantidadRegistros",
            SUM(CASE 
                    WHEN c."IdEstado" = 'CRT' THEN 
                        CASE 
                            WHEN c."IdTipoMovimiento" = 'MOVCAR' THEN -1 * c."Monto" 
                            ELSE c."Monto" 
                        END 
                    ELSE 0 
                END) AS "Monto",
            TO_NVARCHAR(:p_FechaCalculo, 'YYYY-MM-DD') AS "FechaPatrimonio"
        FROM :v_portafoliosPatrimonio AS PP 
        LEFT JOIN "BSCL.CuentaInversion.BD::CTA.CuentaInversion" AS CIN ON CIN."IdPortafolio" = PP."IdPortafolio" 
        LEFT JOIN "BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento" AS c ON CIN."IdCuentaInversion" = c."IdCuentaInversion" 
            AND c."IdEstado" IN ('CRT') AND c."FechaEfectivaMovimiento" <= :p_FechaCalculo 
        LEFT OUTER JOIN "BSCL.CuentaInversion.BD::CTA.CobroComisionExclusionConcepto" AS CCEC ON CCEC."IdConcepto" = c."IdConcepto"
        WHERE "IdMoneda" = :v_MonedaSistema AND COALESCE(CCEC."IdExclusionConcepto", 0) = 0
        GROUP BY PP."IdPortafolio", CIN."IdCuentaInversion", CIN."IdMoneda"
        ORDER BY PP."IdPortafolio";

    -- INSERTAR LOS RESULTADOS AGRUPADOS EN LA TABLA DE PROCESO
    INSERT INTO "BSCL.CuentaInversion.BD::GBL.CalculoSaldoCINInicial_Proceso_Patrimonio"("IdPortafolio", "FechaPatrimonio", "Monto")
    SELECT SMSA."IdPortafolio", :p_FechaCalculo, SMSA."Monto"
    FROM :v_saldosMonedaSistemaAgrupados AS SMSA;

    -- ACTUALIZAR LA TABLA PATRIMONIOCAJA
    UPDATE "BSCL.CuentaInversion.BD::CTA.PatrimonioCaja" AS pc
    SET "MontoPatrimonioAfectoComision" = (
        SELECT ROUND(SUM(cp."Monto"), 0, ROUND_HALF_UP)
        FROM "BSCL.CuentaInversion.BD::GBL.CalculoSaldoCINInicial_Proceso_Patrimonio" AS cp
        WHERE cp."IdPortafolio" = pc."IdPortafolio" AND cp."FechaPatrimonio" = pc."FechaPatrimonio"
    )
    WHERE EXISTS (
        SELECT 1
        FROM "BSCL.CuentaInversion.BD::GBL.CalculoSaldoCINInicial_Proceso_Patrimonio" AS cp
        WHERE cp."IdPortafolio" = pc."IdPortafolio" AND cp."FechaPatrimonio" = pc."FechaPatrimonio"
    ) AND "TipoFechaConsulta" = 'ADQ';

    -- ASIGNAR LOS RESULTADOS A LA VARIABLE DE SALIDA
    p_out_Resultado = 
        SELECT pc."IdPortafolio", pc."MontoPatrimonioAfectoComision"
        FROM "BSCL.CuentaInversion.BD::CTA.PatrimonioCaja" AS pc
        WHERE EXISTS (
            SELECT 1
            FROM "BSCL.CuentaInversion.BD::GBL.CalculoSaldoCINInicial_Proceso_Patrimonio" AS cp
            WHERE cp."IdPortafolio" = pc."IdPortafolio" AND cp."FechaPatrimonio" = pc."FechaPatrimonio" AND pc."TipoFechaConsulta" = 'ADQ'
        )
        ORDER BY "IdPortafolio";

END;