SELECT
    to_bigint(1) AS "NumeroPolizaRunFondoCodigoPlan",
    'UNICA' AS "Serie",
    to_bigint(2) AS "NumeroContratosApvc",
    --NumeroCuentasIndividualesApvcVigentes
    to_bigint(3) AS "NumeroCuentasIndividualesApvcVigentes_LetraA_Masculino",
    to_bigint(4) AS "NumeroCuentasIndividualesApvcVigentes_LetraA_Femenino",
    to_bigint(5) AS "NumeroCuentasIndividualesApvcVigentes_Total_Masculino",
    to_bigint(6) AS "NumeroCuentasIndividualesApvcVigentes_Total_Femenino",
    --NumeroAseguradosParticipesAportantesClientesPlan
    to_bigint(7) AS "NumeroAseguradosParticipesAportantesClientesPlan_LetraA_Masculino",
    to_bigint(8) AS "NumeroAseguradosParticipesAportantesClientesPlan_LetraA_Femenino",
    to_bigint(9) AS "NumeroAseguradosParticipesAportantesClientesPlan_Total_Masculino",
    to_bigint(10) AS "NumeroAseguradosParticipesAportantesClientesPlan_Total_Femenino",
    --SaldoTotalAcumulado
    to_decimal(11,26,8) AS "SaldoTotalAcumulado_Empleador_Masculino",
    to_decimal(12,26,8) AS "SaldoTotalAcumulado_Empleador_Femenino",
    to_decimal(13,26,8) AS "SaldoTotalAcumulado_Trabajador_LetraA_Masculino",
    to_decimal(14,26,8) AS "SaldoTotalAcumulado_Trabajador_LetraA_Femenino",
    to_decimal(15,26,8) AS "SaldoTotalAcumulado_Trabajador_LetraB_Masculino",
    to_decimal(16,26,8) AS "SaldoTotalAcumulado_Trabajador_LetraB_Femenino",
    --Bonificacion
    to_decimal(17,26,8) AS "Bonificacion_LetraA_Masculino",
    to_decimal(18,26,8) AS "Bonificacion_LetraA_Femenino",
    --NumeroAportes
    to_bigint(19) AS "NumeroAportes_Empleador_Masculino",
    to_bigint(20) AS "NumeroAportes_Empleador_Femenino",
    to_bigint(21) AS "NumeroAportes_Trabajador_LetraA_Masculino",
    to_bigint(22) AS "NumeroAportes_Trabajador_LetraA_Femenino",
    to_bigint(23) AS "NumeroAportes_Trabajador_LetraB_Masculino",
    to_bigint(24) AS "NumeroAportes_Trabajador_LetraB_Femenino",
    --MontoTotalAportes
    to_decimal(25,26,8) AS "MontoTotalAportes_Empleador_Masculino",
    to_decimal(26,26,8) AS "MontoTotalAportes_Empleador_Femenino",
    to_decimal(27,26,8) AS "MontoTotalAportes_Trabajador_LetraA_Masculino",
    to_decimal(28,26,8) AS "MontoTotalAportes_Trabajador_LetraA_Femenino",
    to_decimal(29,26,8) AS "MontoTotalAportes_Trabajador_LetraB_Masculino",
    to_decimal(30,26,8) AS "MontoTotalAportes_Trabajador_LetraB_Femenino",
    --NumeroTraspasosRecibidos
    to_bigint(31) AS "NumeroTraspasosRecibidos_Empleador_Masculino",
    to_bigint(32) AS "NumeroTraspasosRecibidos_Empleador_Femenino",
    to_bigint(33) AS "NumeroTraspasosRecibidos_Trabajador_Masculino",
    to_bigint(34) AS "NumeroTraspasosRecibidos_Trabajador_Femenino",
    --MontoTotalTraspasosRecibidos
    to_decimal(35,26,8) AS "MontoTotalTraspasosRecibidos_Empleador_Masculino",
    to_decimal(36,26,8) AS "MontoTotalTraspasosRecibidos_Empleador_Femenino",
    to_decimal(37,26,8) AS "MontoTotalTraspasosRecibidos_Trabajador_Masculino",
    to_decimal(38,26,8) AS "MontoTotalTraspasosRecibidos_Trabajador_Femenino",
    --NumeroRetiros
    to_bigint(39) AS "NumeroRetiros_Empleador_Masculino",
    to_bigint(40) AS "NumeroRetiros_Empleador_Femenino",
    to_bigint(41) AS "NumeroRetiros_Trabajador_LetraA_Masculino",
    to_bigint(42) AS "NumeroRetiros_Trabajador_LetraA_Femenino",
    to_bigint(43) AS "NumeroRetiros_Trabajador_LetraB_Masculino",
    to_bigint(44) AS "NumeroRetiros_Trabajador_LetraB_Femenino",
    --MontoTotalRetiros
    to_decimal(45,26,8) AS "MontoTotalRetiros_Empleador_Masculino",
    to_decimal(46,26,8) AS "MontoTotalRetiros_Empleador_Femenino",
    to_decimal(47,26,8) AS "MontoTotalRetiros_Trabajador_LetraA_Masculino",
    to_decimal(48,26,8) AS "MontoTotalRetiros_Trabajador_LetraA_Femenino",
    to_decimal(49,26,8) AS "MontoTotalRetiros_Trabajador_LetraB_Masculino",
    to_decimal(50,26,8) AS "MontoTotalRetiros_Trabajador_LetraB_Femenino",
    --NumeroTraspasosRealizados
    to_bigint(51) AS "NumeroTraspasosRealizados_Empleador_Masculino",
    to_bigint(52) AS "NumeroTraspasosRealizados_Empleador_Femenino",
    to_bigint(53) AS "NumeroTraspasosRealizados_Trabajador_Masculino",
    to_bigint(54) AS "NumeroTraspasosRealizados_Trabajador_Femenino",
    --MontoTotalTraspasosRealizados
    to_decimal(55,26,8) AS "MontoTotalTraspasosRealizados_Empleador_Masculino",
    to_decimal(56,26,8) AS "MontoTotalTraspasosRealizados_Empleador_Femenino",
    to_decimal(57,26,8) AS "MontoTotalTraspasosRealizados_Trabajador_Masculino",
    to_decimal(58,26,8) AS "MontoTotalTraspasosRealizados_Trabajador_Femenino"
FROM DUMMY
WHERE 1=2;--Se deja la estructura dummy, ya que actualmente NO se maneja apv colectivo, pero le java si esta capacitado para leer esta informacion