"""Family C cases: ProcesarDistribucion, share-order (renta variable) branch (APV-C-041 .. 049)."""
from c_distribucion import FFM_OK, MASK, SEQ, TABLAS, ent
from fix import T_V_FONDO, ins, msg
from lib import L, caso, seed
from pd import contrato_y_distribucion, escenario_ok, sc_calcular_cuotas, sc_cargo_abono, sc_ordenes, sc_precio, sc_solicitud, sc_traspaso_ok, sol_resultado, cuota

F = 'ProcesarDistribucion.java'
PRECIO = {'IdInstrumento': 5001, 'PrecioCierre': 2500, 'IdBolsa': 'BCS'}
CALC = {'IdInterno': 1, 'CantidadOperacion': 42.5}


def acciones(**kw):
    return contrato_y_distribucion(tipo='ACN', nemo='CHILE', instrumento=5001, **kw)


RV_OK = dict(sc_traspaso_ok(2), **sc_cargo_abono(), **sc_precio(PRECIO), **sc_ordenes(calculadas=[CALC], mensajes=[msg(1, 'INFORMACION', 'ORDEN_CREADA', 'orden creada')]))

caso(41, 'ProcesarDistribucion', 'distribuye-una-accion-con-orden', 'one valid deposit distributed 100% to a share: price query, quantity calculation, one purchase order (Glosa = the client detail id) and the order message mapped to the detail',
     L(F, r'Orden.IdInterno = contadorOrden'), ent(p_DistribucionOrigen='S'), seed(escenario_ok(dist=acciones())), secuencias=SEQ, colaboradores=RV_OK, tablas=TABLAS, enmascarar=MASK)
caso(42, 'ProcesarDistribucion', 'error-de-la-orden-queda-en-la-observacion', 'the order service answers an error message for the order: it is copied to the output with the client detail id and written into the transition Observacion as "RV: <nemo>: <text>"',
     L(F, r'_Recaudacion.get\(i\).Observacion = "RV: "'), ent(p_DistribucionOrigen='S'),
     seed(escenario_ok(dist=acciones())), secuencias=SEQ,
     colaboradores=dict(RV_OK, **sc_ordenes(calculadas=[CALC], mensajes=[msg(1, 'ERROR', 'ORDEN_RECHAZADA', 'orden rechazada')], errores=1)), tablas=TABLAS, enmascarar=MASK)
caso(43, 'ProcesarDistribucion', 'accion-y-fondo-reparten-el-monto', 'the contract distributes 50% to a share and 50% to a fund: one order and one FFMM request in the same run',
     L(F, r'if \(filaDistribucionContrato.IdTipoInstrumento.equals\("FFM"\)\)'), ent(p_DistribucionOrigen='S'),
     seed(escenario_ok(dist=contrato_y_distribucion(porcentaje=50, tipo='FFM', nemo='FFMM-A', instrumento=421060)
                       + [ins('BSCL.APV.BD::SCS.DistribucionInversion', IdDistribucionInversion=2, IdContratoSAAPV=9001, IdInstrumento=5001, Nemotecnico='CHILE', MontoPesos=0, Porcentaje=50, IdEstado='VIG'),
                          ins('BSCL.Plataforma.BD::INS.Instrumento', IdInstrumento=5001, IdTipoInstrumento='ACN', IdMoneda='CLP', Nemotecnico='CHILE')])),
     secuencias=SEQ, colaboradores=dict(RV_OK, **sc_calcular_cuotas(cuota(1, 50000, 49.2)), **sc_solicitud(resultados=[sol_resultado(1, 3001, 5000)])), tablas=TABLAS, enmascarar=MASK)
caso(44, 'ProcesarDistribucion', 'orden-sin-cantidad-calculada', 'the quantity calculation answers no order: the purchase order goes with Cantidad 0 (the defaulted amount) and the flow goes on',
     L(F, r'ObjOrden.get\(j\).Cantidad = OutCalcularmonto'), ent(p_DistribucionOrigen='S'), seed(escenario_ok(dist=acciones())), secuencias=SEQ,
     colaboradores=dict(RV_OK, **sc_ordenes(calculadas=[], mensajes=[msg(1, 'INFORMACION', 'ORDEN_CREADA', 'orden creada')])), tablas=TABLAS, enmascarar=MASK)
caso(45, 'ProcesarDistribucion', 'accion-sin-origen-s-queda-fuera-de-la-distribucion', 'defect: the distribution query keeps a non-fund instrument only when p_DistribucionOrigen is S (FON.RestringeAporte is NULL for a share): with the default N the share is dropped, the default distribution is not read either (p_SoloAporteCuentaInversion absent), so nothing is distributed and the detail stays CON',
     L(F, r'ParamsConsultaDistribucionContrato.put\("DistribucionOrigen"'), ent(), seed(escenario_ok(dist=acciones())), secuencias=SEQ, colaboradores=RV_OK, tablas=TABLAS, enmascarar=MASK)
