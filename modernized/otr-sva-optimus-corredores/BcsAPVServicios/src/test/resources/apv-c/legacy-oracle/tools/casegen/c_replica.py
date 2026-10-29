"""Family C cases: ReplicaPortafolioAPV (APV-C-360 .. 379)."""
from fix import ins
from lib import L, caso, seed, tabla

F = 'ReplicaPortafolioAPV.java'
T_POR = 'BSCL.Portafolio.BD::POR.Portafolio'
T_MATRIZ = 'BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV'
T_VMATRIZ = 'BSCL.Portafolio.BD.Vistas::ConsultarMatrizAgrupador'
T_AUT = 'BSCL.Portafolio.BD::POR.Autorizado'
T_CC = 'BSCL.Portafolio.BD::POR.CuentaCustodioPortafolio'
T_PPP = 'BSCL.Portafolio.BD::POR.PreferenciaPagoPortafolio'
T_PP = 'BSCL.Plataforma.BD::PAG.PreferenciaPago'
T_IND = 'BSCL.Plataforma.BD::IND.Indicador'
T_SOL = 'BSCL.Plataforma.BD::PLA.SolicitudCambio'
SEQ = {'BSCL.Portafolio.BD.Secuencias::Autorizado': 700}

TAUT = tabla(T_AUT, '"IdAutorizado"')
TCC = tabla(T_CC, '"IdCuentaCustodioPortafolio"')
TPPP = tabla(T_PPP, '"IdPreferenciaPagoPortafolio"')
TIND = tabla(T_IND, '"IdIndicador"')


def ent(portafolio=901, tipo_entidad='AUTORIZACION', operacion='I', entidad=50, empresa=1):
    e = {'p_IdEmpresa': empresa, 'p_IdUsuario': 51, 'p_IdModulo': 'APV', 'p_UsaReglasNegocio': 'N', 'p_Portafolio': portafolio, 'p_IdEntidad': entidad}
    if tipo_entidad is not None:
        e['p_TipoEntidad'] = tipo_entidad
    if operacion is not None:
        e['p_TipoOperacion'] = operacion
    return e


def por(idp, cliente=701, matriz=1, estado='ACT', proposito='PROPOSITOAPV'):
    return ins(T_POR, IdPortafolio=idp, IdCliente=cliente, IdMatrizAgrupadorAPV=matriz, IdEstado=estado, Proposito=proposito, FechaIngreso='2024-05-06', IdPerfilLimitesExtra=idp)


def aut(ida, idp, persona=77):
    return ins(T_AUT, IdAutorizado=ida, IdPortafolio=idp, IdPersona=persona, IdTipoAutorizado=1, IdEstado='ACT', IdEstadoAutorizacion='AUT', FechaIngreso='2024-05-06', FechaCreacion='2024-05-06',
               FechaModificacion='2024-05-07', IdUsuarioCreador=9, NumeroRegistroPublico='RP-1', Asiento='A1', FichaPartida='FP1', TipoFirma='CONJUNTA')


def cc(idcc, idp, codigo='CC-1'):
    return ins(T_CC, IdCuentaCustodioPortafolio=idcc, IdPortafolio=idp, IdBoveda='B1', Codigo=codigo, FechaIngreso='2024-05-06', FechaCreacion='2024-05-06', FechaModificacion='2024-05-07',
               IdUsuarioCreador=9, IdEstado='ACT', IdEstadoAutorizacion='AUT', IdTipoCuentaCustodia=1, IdTipoCustodia=2, IdModeloCustodia=3, IdPersonaBancoCustodio=4, IdPersonaBancoLiquidador=5,
               LiquidaDividendos='S', LiquidaCupones='N', LiquidaVencimientos='S', LiquidaPrepagos='N')


# origin 901 (client 701, product 1) and the siblings 902, 903 (same client and product); 904 other client, 905 other product, 906 inactive
BASE = [ins(T_MATRIZ, IdMatrizAgrupadorAPV=1, IdProductoAgrupador=1), ins(T_MATRIZ, IdMatrizAgrupadorAPV=2, IdProductoAgrupador=2),
        ins(T_VMATRIZ, IdMatrizAgrupadorAPV=1, IdProductoAgrupador=1), ins(T_VMATRIZ, IdMatrizAgrupadorAPV=2, IdProductoAgrupador=2),
        por(901), por(902), por(903), por(904, cliente=702), por(905, matriz=2), por(906, estado='INA')]
SOLO = [ins(T_MATRIZ, IdMatrizAgrupadorAPV=1, IdProductoAgrupador=1), ins(T_VMATRIZ, IdMatrizAgrupadorAPV=1, IdProductoAgrupador=1), por(901)]

caso(360, 'ReplicaPortafolioAPV', 'portafolio-inexistente-no-hace-nada', 'the origin portfolio is not an active APV portfolio found in the matrix view: empty output, no message and no counter change',
     L(F, 'if\\(portafolioAPV == null\\)'), ent(portafolio=999), seed(BASE, aut(50, 901)), secuencias=SEQ, tablas=[TAUT])
caso(361, 'ReplicaPortafolioAPV', 'portafolio-no-apv-o-inactivo-no-hace-nada', 'portfolio with Proposito other than PROPOSITOAPV (id 907) and inactive portfolio (906): both are not found, nothing replicated',
     L(F, 'if\\(portafolioAPV == null\\)'), ent(portafolio=907), seed(BASE, por(907, proposito='OTRO'), aut(50, 907)), secuencias=SEQ, tablas=[TAUT])
caso(362, 'ReplicaPortafolioAPV', 'sin-otros-portafolios-advertencia-y-error-de-tipo', 'no sibling portfolios and a TipoEntidad that matches no option: ADVERTENCIA "No se encontraron otros portafolios relacionados." (id 0) plus the ERROR "no coinciden"',
     L(F, 'No se encontraron otros portafolios relacionados'), ent(tipo_entidad='OTRO', operacion='I'), seed(SOLO), secuencias=SEQ, tablas=[TAUT])
caso(363, 'ReplicaPortafolioAPV', 'tipo-entidad-desconocido-con-hermanos', 'siblings exist but TipoEntidad/TipoOperacion do not match an option: only the ERROR "El TipoEntidad y el TipoOperacion no coinciden con las opciones disponibles."',
     L(F, 'no coinciden con las opciones disponibles'), ent(tipo_entidad='AUTORIZACION', operacion='U'), seed(BASE, aut(50, 901)), secuencias=SEQ, tablas=[TAUT])
caso(364, 'ReplicaPortafolioAPV', 'tipo-operacion-ausente', 'p_TipoOperacion missing: no option matches (equals on a literal): ERROR message', L(F, 'esReplicaAutorizacion\\(entrada\\)\\) \\{'),
     ent(operacion=None), seed(BASE, aut(50, 901)), secuencias=SEQ, tablas=[TAUT])
caso(365, 'ReplicaPortafolioAPV', 'autorizacion-se-replica-a-los-hermanos', 'AUTORIZACION/I: the authorized person of the origin portfolio is copied to the two sibling portfolios with ids from the Autorizado sequence block (700, 701); other client, other product and inactive portfolios are not siblings',
     L(F, 'autorizadoReplica.IdAutorizado = secuencias.get\\(index\\)'), ent(), seed(BASE, aut(50, 901)), secuencias=SEQ, tablas=[TAUT])
caso(366, 'ReplicaPortafolioAPV', 'autorizado-inexistente-cuenta-como-no-replicado', 'AUTORIZACION/I with an IdAutorizado that does not exist: nothing replicated, so the ERROR "no coinciden" is answered (no sequence block requested)',
     L(F, 'if \\(!autorizados.isEmpty\\(\\)\\)'), ent(entidad=999), seed(BASE, aut(50, 901)), secuencias=SEQ, tablas=[TAUT])
caso(367, 'ReplicaPortafolioAPV', 'autorizado-de-un-hermano-no-se-replica-en-su-portafolio', 'the authorized person belongs to sibling 902: it is skipped for 902 (same portfolio) but copied to 903; the sequence block has the size of the whole sibling list, the index advances only on copies',
     L(F, 'No replicar en el portafolio origen'), ent(entidad=51), seed(BASE, aut(51, 902)), secuencias=SEQ, tablas=[TAUT])
caso(368, 'ReplicaPortafolioAPV', 'autorizacion-sin-hermanos-cuenta-como-replicada', 'AUTORIZACION/I without siblings but with an existing authorized person: returns 1 (replicated) although nothing is copied: only the ADVERTENCIA for the missing siblings',
     L(F, 'if \\(!autorizadosReplica.isEmpty\\(\\)\\)'), ent(), seed(SOLO, aut(50, 901)), secuencias=SEQ, tablas=[TAUT])
caso(369, 'ReplicaPortafolioAPV', 'cuenta-custodio-se-replica-a-los-hermanos', 'CUENTACUSTODIO/I: the custody account (id 60 of the origin portfolio) is copied to each sibling with a new id from the CuentaCustodioPortafolio sequence',
     L(F, 'cuentaCustodioReplica.IdBoveda = cuentaCustodio.IdBoveda'), ent(tipo_entidad='CUENTACUSTODIO', entidad=60), seed(BASE, cc(60, 901)), secuencias=SEQ, tablas=[TCC])
caso(370, 'ReplicaPortafolioAPV', 'cuenta-custodio-de-portafolio-inactivo-no-se-selecciona', 'the custody account belongs to an inactive portfolio (906): the select returns nothing, nothing is copied but the option counts as replicated (no error message)',
     L(F, 'PORTAFOLIO."IdEstado" <> \\x27INA\\x27|esReplicaCuentaCustodioCrear\\(entrada\\)\\) \\{'), ent(tipo_entidad='CUENTACUSTODIO', entidad=61), seed(BASE, cc(61, 906)), secuencias=SEQ, tablas=[TCC])
caso(371, 'ReplicaPortafolioAPV', 'cuenta-custodio-solo-en-insercion', 'CUENTACUSTODIO with operation U is not an option: ERROR "no coinciden"',
     L(F, '"CUENTACUSTODIO".equals'), ent(tipo_entidad='CUENTACUSTODIO', operacion='U', entidad=60), seed(BASE, cc(60, 901)), secuencias=SEQ, tablas=[TCC])
caso(372, 'ReplicaPortafolioAPV', 'preferencia-sin-origen-igual-falla-al-preparar-el-insert', 'defect: PREFERENCIASDEPAGO/I whose preference row does not exist: the select is empty but sql.batch still prepares the insert on the mistyped table name, so even an empty replication ends in SQLException (rollback)',
     L(F, 'esReplicaPreferenciaPagoPortafolio\\(entrada\\)\\) \\{'), ent(tipo_entidad='PREFERENCIASDEPAGO', entidad=70), seed(BASE), secuencias=SEQ, tablas=[TPPP])
caso(373, 'ReplicaPortafolioAPV', 'preferencia-de-pago-inserta-en-tabla-mal-escrita-falla', 'defect: the insert targets "BSCLPortafolioBD::PORPreferenciaPagoPortafolio" (dots missing in the object name) so a preference to copy ends in SQLException and the call is rolled back; the select side is valid',
     L(F, 'SQL_INSERT_PREFERENCIA_PAGO_PORTAFOLIO_REPLICA, preferenciasPagoPortafolioReplicas'),
     ent(tipo_entidad='PREFERENCIASDEPAGO', entidad=70), seed(BASE, ins(T_PP, IdPreferenciaPago=5, IdEmpresa=1, IdTipoOperacion='REC', IdMoneda='CLP'),
                                                                 ins(T_PPP, IdPreferenciaPagoPortafolio=70, IdPortafolio=901, IdPreferenciaPago=5, IdCuentaBancaria=100, IdEstado='ACT', IdEmpresa=1, PorDefecto='S')),
     secuencias=SEQ, tablas=[TPPP])
caso(374, 'ReplicaPortafolioAPV', 'indicadores-falla-al-borrar-por-columna-inexistente', 'defect: INDICADORES/U without a matching PLA.SolicitudCambio row: no indicator is selected but the delete batch is prepared on IND.Indicador.IdPortafolio (a column that table does not have; the entity column is IdEntidad) so the call ends in SQLException (rollback)',
     L(F, 'esReplicaIndicadores\\(entrada\\)\\) \\{'), ent(tipo_entidad='INDICADORES', operacion='U', entidad=80), seed(BASE), secuencias=SEQ, tablas=[TIND])
caso(375, 'ReplicaPortafolioAPV', 'indicadores-solo-en-actualizacion', 'INDICADORES with operation I is not an option: ERROR "no coinciden"',
     L(F, '"INDICADORES".equals'), ent(tipo_entidad='INDICADORES', operacion='I', entidad=80), seed(BASE), secuencias=SEQ, tablas=[TIND])
caso(376, 'ReplicaPortafolioAPV', 'preferencias-solo-insercion-o-actualizacion', 'PREFERENCIASDEPAGO with operation D is not an option: ERROR "no coinciden"',
     L(F, '"PREFERENCIASDEPAGO".equals'), ent(tipo_entidad='PREFERENCIASDEPAGO', operacion='D', entidad=70), seed(BASE), secuencias=SEQ, tablas=[TPPP])
caso(377, 'ReplicaPortafolioAPV', 'sin-portafolio-de-entrada-no-hace-nada', 'p_Portafolio missing: the origin lookup runs with a NULL id and finds nothing: empty output',
     L(F, 'obtenerInformacionPortafolioAPV\\(con, entrada.p_Portafolio\\)'), {'p_IdEmpresa': 1, 'p_TipoEntidad': 'AUTORIZACION', 'p_TipoOperacion': 'I', 'p_IdEntidad': 50}, seed(BASE, aut(50, 901)), secuencias=SEQ, tablas=[TAUT])
