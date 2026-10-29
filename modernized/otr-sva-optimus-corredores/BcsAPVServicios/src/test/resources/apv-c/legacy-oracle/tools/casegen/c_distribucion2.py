"""Family C cases: ProcesarDistribucion, rezago and distribution variants (APV-C-010 .. 049)."""
from c_distribucion import FFM_OK, MASK, SEQ, TABLAS, ent
from fix import T_INS, T_SOLAPO, T_CFGAPV, T_V_FONDO, T_ASIGN, ins, msg, rcli, rec
from lib import L, caso, seed
from pd import (asig, cliente_sn, contrato_y_distribucion, crd, cuota, escenario_ok, gest, sc_calcular_cuotas, sc_cargo_abono, sc_solicitud, sc_traspaso_ok, sol_resultado)

F = 'ProcesarDistribucion.java'
T_DISTR = 'BSCL.APV.BD::SCS.DistribucionInversion'

# ------------------------------------------------------------------------------------------------ validation fails: rezago
caso(10, 'ProcesarDistribucion', 'validacion-nok-pasa-a-rezago-con-traspaso', 'client blocked (validation NOK), amount > 0 and state CON: warning CAMBIO_DE_ESTADO_A_REZAGO, TraspasoCuentasBancarias REZ runs for the total, transition row and client detail REZ; nothing is distributed',
     L(F, 'CAMBIO_DE_ESTADO_A_REZAGO'), ent(), seed(escenario_ok(), cliente_sn(estado='BLO')), secuencias=SEQ, colaboradores=sc_traspaso_ok(2), tablas=TABLAS, enmascarar=MASK)
caso(11, 'ProcesarDistribucion', 'validacion-nok-ya-en-rezago-no-vuelve-a-traspasar', 'same failure but the detail is already in REZ: no traspaso (the REZ guard), only the transition, the warning and the state',
     L(F, r'!fila.IdEstadoRecaudacionCliente.equals\("REZ"\)'), ent(), seed(escenario_ok(estado='REZ'), cliente_sn(estado='BLO')), secuencias=SEQ, tablas=TABLAS, enmascarar=MASK)
caso(12, 'ProcesarDistribucion', 'validacion-nok-sin-contrato-asignado', 'no assignment row for the client detail: "No tiene contratos vigentes." (NOK), the rezago path runs', L(F, 'DETALLE_VALIDACION_SAAPV'),
     ent(), seed(escenario_ok(con_asig=False)), secuencias=SEQ, colaboradores=sc_traspaso_ok(2), tablas=TABLAS, enmascarar=MASK)
caso(13, 'ProcesarDistribucion', 'rezago-con-error-de-tesoreria-suma-errores', 'the REZ traspaso answers a Tesoreria error: the counter adds the traspaso errors and its messages are copied with id 0',
     L(F, r'salida.P_OUT_ERRORESCONTADOR \+= OutTrasCtaBca_Entrada.P_OUT_ERRORESCONTADOR;'), ent(), seed(escenario_ok(), cliente_sn(estado='BLO')), secuencias=SEQ,
     colaboradores=dict(sc_traspaso_ok(2), CrearIngresoEgreso={'salida': {'P_OUT_MENSAJES': [msg(1, 'ERROR', 'SIN_SALDO', 'sin saldo')], 'P_OUT_INGRESOEGRESOGENERADO': [], 'P_OUT_RESULTADOPROCESO': []}}),
     tablas=TABLAS, enmascarar=MASK)
caso(14, 'ProcesarDistribucion', 'una-valida-y-una-rezagada', 'two client details: one valid (distributed) and one with a blocked client (rezago): the second is removed from the distribution',
     L(F, r'_Recaudacion.remove\(i\)'), ent(ids=(10, 20)),
     seed(escenario_ok(), rec(20, 'CON', monto=50000, banco_=17, cuenta='111222'), rcli(21, 20, None, 'CON', apv=50000, idcliente=702), cliente_sn(702, 502, 'BLO'),
          crd(21, 20, idcli=702, idpersona=502, monto=50000), asig(21, 9002), gest(9002, 902, monto=50000, idpersona=502), contrato_y_distribucion(9002)[:1]),
     secuencias=SEQ, colaboradores=dict(FFM_OK, **sc_traspaso_ok(2)), tablas=TABLAS, enmascarar=MASK)

# ------------------------------------------------------------------------------------------------ distribution variants
caso(21, 'ProcesarDistribucion', 'solo-aporte-saapv-no-mueve-cuenta-inversion', 'p_SoloAporteSAAPV S: no abono/cargo of the cuenta inversion, the fund request is still built', L(F, r'if\( "N".equals\(in.p_SoloAporteSAAPV\) \)'),
     ent(p_SoloAporteSAAPV='S'), seed(escenario_ok()), secuencias=SEQ, colaboradores=FFM_OK, tablas=TABLAS, enmascarar=MASK)
D3 = contrato_y_distribucion(porcentaje=33.33, instrumento=421060, nemo='FFMM-A')
D3 += [ins(T_DISTR, IdDistribucionInversion=2, IdContratoSAAPV=9001, IdInstrumento=421061, Nemotecnico='FFMM-B', MontoPesos=0, Porcentaje=33.33, IdEstado='VIG'),
       ins(T_INS, IdInstrumento=421061, IdTipoInstrumento='FFM', IdMoneda='CLP', Nemotecnico='FFMM-B'), ins(T_V_FONDO, IdInstrumento=421061, IdSerieFondo='FFMM-B', IdMoneda='CLP', RestringeAporte='N'),
       ins(T_DISTR, IdDistribucionInversion=3, IdContratoSAAPV=9001, IdInstrumento=421062, Nemotecnico='FFMM-C', MontoPesos=0, Porcentaje=33.33, IdEstado='VIG'),
       ins(T_INS, IdInstrumento=421062, IdTipoInstrumento='FFM', IdMoneda='CLP', Nemotecnico='FFMM-C'), ins(T_V_FONDO, IdInstrumento=421062, IdSerieFondo='FFMM-C', IdMoneda='CLP', RestringeAporte='N')]
caso(22, 'ProcesarDistribucion', 'tres-fondos-el-resto-se-suma-al-ultimo', 'three funds of 33.33% on 100000: each amount is truncated (33330) and the 10 pesos left over are added to the LAST fund', L(F, 'si nos falto repartir unos pesos'),
     ent(), seed(escenario_ok(dist=D3)), secuencias=SEQ,
     colaboradores=dict(FFM_OK, **sc_calcular_cuotas(cuota(1, 33330, 30), cuota(2, 33330, 31), cuota(3, 33340, 32)),
                        **sc_solicitud(resultados=[sol_resultado(1, 3001, 5000), sol_resultado(2, 3002, 5000), sol_resultado(3, 3003, 5000)])), tablas=TABLAS, enmascarar=MASK)
caso(23, 'ProcesarDistribucion', 'sin-distribucion-usa-el-fondo-por-defecto', 'the contract has no distribution: the default fund of PLA.ConfiguracionAPV (APV.FondoDistribucionDefecto) is used at 100%', L(F, 'SQL_SELECT_DistribucionDefecto, ParamsConsultaDistribucionDefecto'),
     ent(), seed(escenario_ok(dist=contrato_y_distribucion()[:2]), ins(T_CFGAPV, CodigoParametro='APV.FondoDistribucionDefecto', Valor='FFMM-D'),
                 ins(T_V_FONDO, IdInstrumento=421090, IdSerieFondo='FFMM-D', IdMoneda='CLP', RestringeAporte='N')), secuencias=SEQ, colaboradores=FFM_OK, tablas=TABLAS, enmascarar=MASK)
caso(24, 'ProcesarDistribucion', 'solo-cuenta-inversion-sin-distribucion', 'p_SoloAporteCuentaInversion S and no distribution: no default fund, the abono uses concept ACIAPV and no FFMM request is made', L(F, '"ACIAPV"'),
     ent(p_SoloAporteCuentaInversion='S'), seed(escenario_ok(dist=contrato_y_distribucion()[:2])), secuencias=SEQ, colaboradores=dict(sc_cargo_abono(), **sc_traspaso_ok(2)), tablas=TABLAS, enmascarar=MASK)
caso(25, 'ProcesarDistribucion', 'fondo-restringido-se-excluye-y-usa-defecto', 'the only distribution fund restricts contributions (RestringeAporte S) and p_DistribucionOrigen is N: the restricted line is excluded, the default fund is used',
     L(F, 'ParamsConsultaDistribucionContrato.put\\("DistribucionOrigen"'),
     ent(), seed(escenario_ok(dist=contrato_y_distribucion(restringe='S')), ins(T_CFGAPV, CodigoParametro='APV.FondoDistribucionDefecto', Valor='FFMM-D'),
                 ins(T_V_FONDO, IdInstrumento=421090, IdSerieFondo='FFMM-D', IdMoneda='CLP', RestringeAporte='N')), secuencias=SEQ, colaboradores=FFM_OK, tablas=TABLAS, enmascarar=MASK)
caso(26, 'ProcesarDistribucion', 'distribucion-origen-s-incluye-fondo-restringido', 'p_DistribucionOrigen S keeps the restricted fund line', L(F, 'ParamsConsultaDistribucionContrato.put\\("DistribucionOrigen"'),
     ent(p_DistribucionOrigen='S'), seed(escenario_ok(dist=contrato_y_distribucion(restringe='S'))), secuencias=SEQ, colaboradores=FFM_OK, tablas=TABLAS, enmascarar=MASK)
caso(27, 'ProcesarDistribucion', 'fondo-en-dolares-arma-formas-de-pago-spot', 'a USD fund: spot payment forms (MOVEGR USD / MOVING CLP) are added, ForzarSpot S, and the spot amount is repaired from CalcularMontoNumeroCuota',
     L(F, 'VENSPT_CIN_USD_0'), ent(), seed(escenario_ok(dist=contrato_y_distribucion(moneda='USD'))), secuencias=SEQ,
     colaboradores=dict(FFM_OK, **sc_calcular_cuotas(dict(cuota(1, 105.55, 98.5), IdMonedaSistema='CLP'))), tablas=TABLAS, enmascarar=MASK)
caso(28, 'ProcesarDistribucion', 'error-al-crear-solicitud-ffmm-se-traga-y-anota', 'CrearSolicitudAporteRescateNormal throws: the exception is swallowed, every recaudacion gets the observation "No se ingresa aporte FFMM..." (written to the transition row) and the process still completes',
     L(F, 'No se ingresa aporte FFMM'), ent(), seed(escenario_ok()), secuencias=SEQ, colaboradores=dict(FFM_OK, CrearSolicitudAporteRescateNormal={'lanza': 'FFMM down'}), tablas=TABLAS, enmascarar=MASK)
caso(29, 'ProcesarDistribucion', 'solicitud-ffmm-con-error-bloquea-el-cierre', 'CrearSolicitud answers an ERROR: its error message is returned, no FFMM result is reported and the process ends with PROCESO_DISTRIBUCION_CON_ERRORES (no state updates)',
     L(F, 'PROCESO_DISTRIBUCION_CON_ERRORES'), ent(), seed(escenario_ok()), secuencias=SEQ,
     colaboradores=dict(FFM_OK, **sc_solicitud(mensajes=[msg(1, 'ERROR', 'FONDO_CERRADO', 'fondo cerrado')], errores=1)), tablas=TABLAS, enmascarar=MASK)
caso(30, 'ProcesarDistribucion', 'cuenta-inversion-con-error', 'CrearCargoAbono answers an ERROR: it is added and counted, the process ends with PROCESO_DISTRIBUCION_CON_ERRORES', L(F, 'mensajesErroresCuentaInversion'),
     ent(), seed(escenario_ok()), secuencias=SEQ, colaboradores=dict(FFM_OK, **sc_cargo_abono([msg(1, 'ERROR', 'CTA_BLOQUEADA', 'cuenta bloqueada')])), tablas=TABLAS, enmascarar=MASK)
caso(31, 'ProcesarDistribucion', 'traspaso-dis-con-error', 'the DIS traspaso answers a Tesoreria error: errors counted, PROCESO_DISTRIBUCION_CON_ERRORES, states untouched', L(F, 'Inicio Traspaso Cuenta Bancarias'),
     ent(), seed(escenario_ok()), secuencias=SEQ,
     colaboradores=dict(FFM_OK, CrearIngresoEgreso={'salida': {'P_OUT_MENSAJES': [msg(1, 'ERROR', 'SIN_SALDO', 'sin saldo')], 'P_OUT_INGRESOEGRESOGENERADO': [], 'P_OUT_RESULTADOPROCESO': []}}),
     tablas=TABLAS, enmascarar=MASK)
caso(32, 'ProcesarDistribucion', 'pago-unico-cierra-el-contrato', 'Pago UNI: the contract becomes COM and the FFMM request is not auto-entered (IngresoAutomatico N)', L(F, r'fila.Pago.equals\("UNI"\)'),
     ent(), seed(escenario_ok(pago='UNI')), secuencias=SEQ, colaboradores=FFM_OK, tablas=TABLAS, enmascarar=MASK)
caso(33, 'ProcesarDistribucion', 'canal-web-no-ingresa-automatico-y-sin-origen-rec', 'p_Canal WEB: IngresoAutomatico N, p_IdTipoCanalIngreso WEB, no origin REC and no IgnorarMontoMinimo', L(F, r'esAporteSAAPVOrigenWebService\(in.p_Canal\)'),
     ent(p_Canal='WEB'), seed(escenario_ok()), secuencias=SEQ, colaboradores=FFM_OK, tablas=TABLAS, enmascarar=MASK)
caso(34, 'ProcesarDistribucion', 'canal-opt-origen-web-no-ingresa-automatico', 'p_Canal OPT with p_Origen WEB: IngresoAutomatico N', L(F, r'"OPT".equalsIgnoreCase\(in.p_Canal\) && "WEB".equalsIgnoreCase\(in.p_Origen\)'),
     ent(p_Canal='OPT', p_Origen='WEB'), seed(escenario_ok()), secuencias=SEQ, colaboradores=FFM_OK, tablas=TABLAS, enmascarar=MASK)
caso(35, 'ProcesarDistribucion', 'simulacion-s-se-propaga-a-las-solicitudes', 'p_Simula S: p_SimularOperacion S reaches the FFMM request and TraspasoCuentasBancarias', L(F, r'in.p_Simula.trim\(\).equalsIgnoreCase\("S"\)'),
     ent(p_Simula='S'), seed(escenario_ok()), secuencias=SEQ, colaboradores=FFM_OK, tablas=TABLAS, enmascarar=MASK)
caso(36, 'ProcesarDistribucion', 'solicitudes-ffmm-asociadas-se-excluyen', 'DIS and the client detail already has an FFMM request (not annulled): warning RECAUDACION_CON_SOLICITUD_FFMM_ASOCIADA, the row is removed and the process ends with NO_SE_ENCONTRARON_REGISTROS_PARA_DISTRIBUIR',
     L(F, 'RECAUDACION_CON_SOLICITUD_FFMM_ASOCIADA'), ent(), seed(escenario_ok(), ins(T_SOLAPO, IdSolicitudAporteRescate=3999, IdOrigenRecaCliente=11, Estado='ING')), secuencias=SEQ, colaboradores=FFM_OK,
     tablas=TABLAS, enmascarar=MASK)
caso(37, 'ProcesarDistribucion', 'solicitud-anulada-no-bloquea', 'the associated FFMM request is annulled (ANU): it does not block the distribution', L(F, 'listaSolicitudesAsociadas = buscarSolicitudesFFMMAsociadas'),
     ent(), seed(escenario_ok(), ins(T_SOLAPO, IdSolicitudAporteRescate=3999, IdOrigenRecaCliente=11, Estado='ANU')), secuencias=SEQ, colaboradores=FFM_OK, tablas=TABLAS, enmascarar=MASK)
caso(38, 'ProcesarDistribucion', 'opcion-no-dis-no-consulta-solicitudes-asociadas', 'option PAC (not DIS): no FOR UPDATE lock and no check of associated FFMM requests (even if one exists)', L(F, r'"DIS".equalsIgnoreCase\(in.p_Opcion\)'),
     ent(opcion='PAC'), seed(escenario_ok(), ins(T_SOLAPO, IdSolicitudAporteRescate=3999, IdOrigenRecaCliente=11, Estado='ING')), secuencias=SEQ, colaboradores=FFM_OK, tablas=TABLAS, enmascarar=MASK)
caso(39, 'ProcesarDistribucion', 'sin-portafolio-de-la-corredora-lanza-IndexOutOfBounds', 'PLA.Empresa without the default portfolio: _IdPortafolioDefecto.get(0) throws IndexOutOfBoundsException (after the validation work, states rolled back)',
     L(F, r'IdPortafolioCorredora = _IdPortafolioDefecto.get\(0\)'), ent(), seed(escenario_ok(empresa_=False)), secuencias=SEQ, colaboradores=FFM_OK, tablas=TABLAS, enmascarar=MASK)
caso(40, 'ProcesarDistribucion', 'rectra-valida-por-monto-y-marca-traspaso', 'option RECTRA: validation by amount only, the FFMM request is flagged EsTraspaso S and the DIS traspaso uses the traspaso concepts (ITRAPV/ETRAPV)', L(F, '"RECTRA"'),
     ent(opcion='RECTRA'), seed(escenario_ok()), secuencias=SEQ, colaboradores=FFM_OK, tablas=TABLAS, enmascarar=MASK)
