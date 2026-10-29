"""Cases of CambiarEstadoRevocacionSAAPV (ids 316-329)."""
from lib import *  # noqa: F401,F403

R = 'CambiarEstadoRevocacionSAAPV'
RF = 'CambiarEstadoRevocacionSAAPV.java'
BASE = {'p_IdEmpresa': 1, 'p_IdUsuario': 21, 'p_IdModulo': 'APV'}
SEQR = {'BSCL.APV.BD.Secuencias::IdSAAPVRevocacion': 700}


def e(contratos, **kw):
    d = dict(BASE)
    d['p_ContratosSAAPV'] = contratos
    d.update(kw)
    return d


def mov(folio, idp=501, idc=701, fecha='2025-04-01', glosa=3, desc='MOTIVO'):
    d = {'IdPersona': idp, 'IdCliente': idc, 'IdFolioContratoSAAPV': folio, 'FechaRevocacion': fecha, 'IdGlosa': glosa, 'DescripcionGlosa': desc}
    return {k: v for k, v in d.items() if v is not None}


PER = [persona(501, '11111111-1', idc=701), persona(502, '22222222-2', idc=702)]
TR = [tabla(T_CONTRATO, '"IdContratoSAAPV"', COLS_CONTRATO_ESTADO)]
LR = L(RF, r'for \(InContrato mov : entrada.p_ContratosSAAPV \)', r'i\+\+;')

caso(316, R, 'revoca-un-contrato-vigente', 'a VIG contract of the person: entity OK with the sequence value as IdFolioSAAPV, CambiarEstadoContratosSAAPV (REV) revokes it', LR,
     e([mov('F-R1')]), seed(PER, contrato(9001, 'F-R1', IdEstado='VIG')), secuencias=SEQR, tablas=TR)
caso(317, R, 'cliente-nulo-es-cliente-no-existe', 'IdCliente not informed: CLIENTE_NO_EXISTE and the error counter ContadorErrores 1', L(RF, r'CLIENTE_NO_EXISTE'),
     e([mov('F-R1', idc=None)]), seed(PER, contrato(9001, 'F-R1', IdEstado='VIG')), secuencias=SEQR, tablas=TR)
caso(318, R, 'persona-nula-es-cliente-no-existe', 'IdPersona not informed: the same CLIENTE_NO_EXISTE', L(RF, r'mov.IdCliente == null \|\| mov.IdPersona == null'),
     e([mov('F-R1', idp=None)]), seed(PER, contrato(9001, 'F-R1', IdEstado='VIG')), secuencias=SEQR, tablas=TR)
caso(319, R, 'contrato-no-vigente-es-contrato-no-valido', 'the folio is not a VIG contract: CONTRATO_NO_VALIDO', L(RF, r'CONTRATO_NO_VALIDO'),
     e([mov('F-R1')]), seed(PER, contrato(9001, 'F-R1', IdEstado='PDI')), secuencias=SEQR, tablas=TR)
caso(320, R, 'folio-inexistente-es-contrato-no-valido', 'a folio that does not exist: CONTRATO_NO_VALIDO', L(RF, r'hcontratosExistentesValidaciones.size\(\) == 0'),
     e([mov('NO-EXISTE')]), seed(PER, contrato(9001, 'F-R1', IdEstado='VIG')), secuencias=SEQR, tablas=TR)
caso(321, R, 'dos-contratos-reutilizan-el-mismo-objeto-de-entrada', 'two valid contracts: the same InContrato object is mutated and added twice, so the second CambiarEstado call receives the second folio twice and the first contract is revoked only by the first call',
     L(RF, r'contratosSAAPV_Entrada.p_ContratosSAAPV.add\(inContrato\)'),
     e([mov('F-R1'), mov('F-R2', idp=502, idc=702)]), seed(PER, contrato(9001, 'F-R1', IdEstado='VIG'), contrato(9002, 'F-R2', IdEstado='VIG', IdPersona=502, IdCliente=702)), secuencias=SEQR, tablas=TR)
caso(322, R, 'secuencia-solo-avanza-en-los-validos', 'an invalid contract between two valid ones does not consume a sequence value (i only increments on success) while the sequence call asks for the full list size', L(RF, r'Utils.getSecuencia\(con, "BSCL.APV.BD.Secuencias::IdSAAPVRevocacion"'),
     e([mov('F-R1'), mov('NO-EXISTE'), mov('F-R2', idp=502, idc=702)]),
     seed(PER, contrato(9001, 'F-R1', IdEstado='VIG'), contrato(9002, 'F-R2', IdEstado='VIG', IdPersona=502, IdCliente=702)), secuencias=SEQR, tablas=TR)
caso(323, R, 'con-mandato-pac-llama-a-la-revocacion-del-mandato', 'a revoked contract with a PAC mandate: the nested CambiarEstadoContratosSAAPV calls RevocacionMandatoPAC (module TES) with the mandate', L(RF, r'cambiarEstadoContratosSAAPV.logica\(con, contratosSAAPV_Entrada\)'),
     e([mov('F-R1')]), seed(PER, contrato(9001, 'F-R1', IdEstado='VIG'), ins(T_MANDATO, IdMandatoPAC=300, Estado='ACT', FolioSAAPV='F-R1')),
     colaboradores={'RevocacionMandatoPAC': {'P_OUT_ERRORESCONTADOR': 0, 'P_OUT_MENSAJES': []}}, secuencias=SEQR, tablas=TR)
caso(324, R, 'lista-vacia', 'no contracts: the sequence is asked for 0 values; the answer has no contracts and ContadorErrores 0', L(RF, r'new Long\(entrada.p_ContratosSAAPV.size\(\)\)'),
     e([]), seed(PER), secuencias=SEQR, tablas=TR)
caso(325, R, 'lista-nula-lanza-NPE', 'null list: entrada.p_ContratosSAAPV.size() throws NullPointerException', L(RF, r'new Long\(entrada.p_ContratosSAAPV.size\(\)\)'),
     e(None), seed(PER), secuencias=SEQR, tablas=TR)
caso(326, R, 'requisitos-basicos-con-errores-se-ignoran', 'RevisarRequisitosBasicos answers an error: the result is not used, the revocation goes on (and the call is recorded)', L(RF, r'RevisarRequisitosBasicos basicos = new'),
     e([mov('F-R1')]), seed(PER, contrato(9001, 'F-R1', IdEstado='VIG')),
     colaboradores={'requisitosBasicos': [{'id': 1, 'tipo': 'ERROR', 'codigo': 'USUARIO_NO_EXISTE', 'descripcion': 'Usuario no existe'}]}, secuencias=SEQR, tablas=TR)
