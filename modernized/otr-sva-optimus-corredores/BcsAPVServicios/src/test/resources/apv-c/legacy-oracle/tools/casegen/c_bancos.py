"""Family C cases: CrearBancoPrevired (380-389), ModificarBancoPrevired (390-399), EliminarBancoPrevired (400-409), ValidarBancoPrevired / ValidarMandatosPAC (410-411)."""
from fix import T_CTABCO
from lib import L, caso, ins, seed, tabla

T_CBP = 'BSCL.APV.BD::REC.CodigoBancoPrevired'
TCBP = tabla(T_CBP, '"CodigoBancoPrevired"')


def b(codigo, cuenta, interno=1):
    d = {}
    if codigo is not None:
        d['CodigoBancoPrevired'] = codigo
    if cuenta is not None:
        d['IdCuentaBanco'] = cuenta
    if interno is not None:
        d['IdInterno'] = interno
    return d


def e(*bancos):
    return {'p_IdEmpresa': 1, 'p_IdUsuario': 51, 'p_IdModulo': 'APV', 'p_SimularOperacion': 'N', 'p_UsaReglasNegocio': 'S', 'p_BancosPrevired': list(bancos)}


CUENTAS = [ins(T_CTABCO, IdCuentaBanco=122, IdBanco=12, NumeroCuentaBancaria='13602227'), ins(T_CTABCO, IdCuentaBanco=123, IdBanco=16, NumeroCuentaBancaria='5555')]
EXISTE = ins(T_CBP, CodigoBancoPrevired='BCI', IdCuentaBanco=122)
EXISTE2 = ins(T_CBP, CodigoBancoPrevired='SAN', IdCuentaBanco=123)

# ------------------------------------------------------------------------------------------------ CrearBancoPrevired
F = 'CrearBancoPrevired.java'
caso(380, 'CrearBancoPrevired', 'inserta-dos-bancos-validos', 'two new bank codes with existing accounts: both inserted, no message', L(F, 'insertarBancosPrevired\\(con, bancosPreviredAInsertar\\)'),
     e(b('CHI', 122, 1), b('ITA', 123, 2)), seed(CUENTAS), tablas=[TCBP])
caso(381, 'CrearBancoPrevired', 'codigo-existente-error', 'a code that already exists: error UI_ERROR_CODIGO_BANCO_PREVIRED_YA_EXISTE, not inserted', L('ValidarBancoPrevired.java', 'MENSAJE_BANCO_PREVIRED_YA_EXISTE'),
     e(b('BCI', 122, 1)), seed(CUENTAS, EXISTE), tablas=[TCBP])
caso(382, 'CrearBancoPrevired', 'cuenta-inexistente-error', 'an account id that does not exist: UI_CUENTA_BANCO_NO_EXISTE', L('ValidarBancoPrevired.java', 'noExisteCuentaBanco\\(cuentaBancosPorIdCuentaBancoExistentes'),
     e(b('CHI', 999, 1)), seed(CUENTAS), tablas=[TCBP])
caso(383, 'CrearBancoPrevired', 'existente-y-cuenta-inexistente-dos-errores', 'one row with both problems: two messages in order (already exists, account missing)',
     L('ValidarBancoPrevired.java', 'validarCreacionBancoPrevired'), e(b('BCI', 999, 1)), seed(CUENTAS, EXISTE), tablas=[TCBP])
caso(384, 'CrearBancoPrevired', 'lista-vacia-error', 'empty list: UI_ERROR_NO_SE_INGRESARON_BANCOS_PREVIRED (id 0), nothing inserted', L('ValidarBancoPrevired.java', 'esEntradaVacia\\(bancosPrevired\\)'),
     e(), seed(CUENTAS), tablas=[TCBP])
caso(385, 'CrearBancoPrevired', 'id-interno-repetido-error', 'IdInterno repeated: UI_ERROR_ID_INTERNO_REPETIDO_ENTRADA with the id, every row becomes incorrect, nothing inserted',
     L('ValidarBancoPrevired.java', 'MENSAJE_ID_INTERNO_REPETIDO_ENTRADA'), e(b('CHI', 122, 7), b('ITA', 123, 7)), seed(CUENTAS), tablas=[TCBP])
caso(386, 'CrearBancoPrevired', 'codigo-repetido-error-sin-valores', 'same bank code twice: UI_ERROR_CODIGO_BANCO_PREVIRED_REPETIDO_ENTRADA is built with an EMPTY value map (the code is not passed)',
     L('ValidarBancoPrevired.java', 'MENSAJE_CODIGO_BANCO_PREVIRED_REPETIDO_ENTRADA'), e(b('CHI', 122, 1), b('CHI', 123, 2)), seed(CUENTAS), tablas=[TCBP])
caso(387, 'CrearBancoPrevired', 'id-interno-nulo-lanza-NullPointerException', 'IdInterno missing: groupingBy cannot map a null key, NullPointerException', L('ValidarBancoPrevired.java', 'groupingBy\\(bancoPrevired->bancoPrevired.IdInterno\\)'),
     e(b('CHI', 122, None)), seed(CUENTAS), tablas=[TCBP])
caso(388, 'CrearBancoPrevired', 'mezcla-inserta-solo-los-correctos', 'one valid and one existing code: only the valid one is inserted, one error', L(F, 'bancosPreviredAInsertar = resultadoValidacion.BancosPreviredCorrectos'),
     e(b('CHI', 122, 1), b('BCI', 123, 2)), seed(CUENTAS, EXISTE), tablas=[TCBP])
caso(389, 'CrearBancoPrevired', 'codigo-nulo-lanza-NullPointerException', 'CodigoBancoPrevired missing: NullPointerException', L('ValidarBancoPrevired.java', 'groupingBy\\(bancoPrevired->bancoPrevired.CodigoBancoPrevired\\)'),
     e(b(None, 122, 1)), seed(CUENTAS), tablas=[TCBP])

# ------------------------------------------------------------------------------------------------ ModificarBancoPrevired
F = 'ModificarBancoPrevired.java'
caso(390, 'ModificarBancoPrevired', 'actualiza-la-cuenta', 'existing code and existing account: the account of the code is updated', L(F, 'modificarBancosPrevired\\(con, bancosPreviredAActualizar\\)'),
     e(b('BCI', 123, 1)), seed(CUENTAS, EXISTE), tablas=[TCBP])
caso(391, 'ModificarBancoPrevired', 'codigo-inexistente-error', 'unknown code: UI_ERROR_NO_EXISTE_BANCO_PREVIRED', L('ValidarBancoPrevired.java', 'MENSAJE_NO_EXISTE_BANCO_PREVIRED'),
     e(b('XXX', 123, 1)), seed(CUENTAS, EXISTE), tablas=[TCBP])
caso(392, 'ModificarBancoPrevired', 'cuenta-inexistente-error', 'existing code but unknown account: UI_CUENTA_BANCO_NO_EXISTE, row unchanged', L('ValidarBancoPrevired.java', 'validarModificacionBancoPrevired'),
     e(b('BCI', 999, 1)), seed(CUENTAS, EXISTE), tablas=[TCBP])
caso(393, 'ModificarBancoPrevired', 'ambos-errores', 'unknown code and unknown account: two messages in order', L('ValidarBancoPrevired.java', 'noExisteBancoPrevired\\(bancosPreviredPorCodigoBancoPreviredExistentes, bancoPrevired\\)'),
     e(b('XXX', 999, 1)), seed(CUENTAS, EXISTE), tablas=[TCBP])
caso(394, 'ModificarBancoPrevired', 'lista-vacia-error', 'empty list: UI_ERROR_NO_SE_INGRESARON_BANCOS_PREVIRED', L('ValidarBancoPrevired.java', 'esEntradaVacia'), e(), seed(CUENTAS, EXISTE), tablas=[TCBP])
caso(395, 'ModificarBancoPrevired', 'repetidos-error', 'same IdInterno twice: repeated error and nothing updated', L('ValidarBancoPrevired.java', 'esAgrupacionConRegistrosRepetidos\\(bancosPreviredAgrupacion\\)'),
     e(b('BCI', 123, 5), b('SAN', 122, 5)), seed(CUENTAS, EXISTE, EXISTE2), tablas=[TCBP])
caso(396, 'ModificarBancoPrevired', 'dos-actualizaciones', 'two valid rows updated in one call', L(F, 'sql.batch\\(con, SQL_UPDATE_BANCOS_PREVIRED'),
     e(b('BCI', 123, 1), b('SAN', 122, 2)), seed(CUENTAS, EXISTE, EXISTE2), tablas=[TCBP])

# ------------------------------------------------------------------------------------------------ EliminarBancoPrevired
F = 'EliminarBancoPrevired.java'
caso(400, 'EliminarBancoPrevired', 'elimina-existentes', 'two existing codes are deleted, no message', L(F, 'eliminarBancosPrevired\\(con, bancosPreviredAEliminar\\)'),
     e(b('BCI', None, 1), b('SAN', None, 2)), seed(CUENTAS, EXISTE, EXISTE2), tablas=[TCBP])
caso(401, 'EliminarBancoPrevired', 'codigo-inexistente-error', 'unknown code: UI_ERROR_NO_EXISTE_BANCO_PREVIRED', L('ValidarBancoPrevired.java', 'validarEliminacionBancoPrevired'),
     e(b('XXX', None, 1)), seed(CUENTAS, EXISTE), tablas=[TCBP])
caso(402, 'EliminarBancoPrevired', 'lista-vacia-error', 'empty list: error message', L('ValidarBancoPrevired.java', 'esEntradaVacia'), e(), seed(CUENTAS, EXISTE), tablas=[TCBP])
caso(403, 'EliminarBancoPrevired', 'codigo-repetido-error', 'same code twice: repeated error, nothing deleted', L('ValidarBancoPrevired.java', 'MENSAJE_CODIGO_BANCO_PREVIRED_REPETIDO_ENTRADA'),
     e(b('BCI', None, 1), b('BCI', None, 2)), seed(CUENTAS, EXISTE), tablas=[TCBP])
caso(404, 'EliminarBancoPrevired', 'mezcla-elimina-solo-el-existente', 'one existing and one unknown code: the existing one is deleted, an error for the other', L('ValidarBancoPrevired.java', 'BancosPreviredInCorrectos.add\\(bancoPrevired\\);\\s*$'),
     e(b('BCI', None, 1), b('XXX', None, 2)), seed(CUENTAS, EXISTE, EXISTE2), tablas=[TCBP])
caso(405, 'EliminarBancoPrevired', 'cuenta-no-se-valida-al-eliminar', 'deleting does not validate the account id (999 does not exist): the code is deleted', L('ValidarBancoPrevired.java', 'validarEliminacionBancoPrevired'),
     e(b('BCI', 999, 1)), seed(CUENTAS, EXISTE), tablas=[TCBP])

# ------------------------------------------------------------------------------------------------ not invocable: call() returns null
caso(410, 'ValidarBancoPrevired', 'call-devuelve-null', 'ValidarBancoPrevired.call returns null: the pipeline answers null (not an operation, no route)', L('ValidarBancoPrevired.java', 'TODO Auto-generated method stub'),
     {'p_IdEmpresa': 1, 'p_BancosPrevired': [b('CHI', 122, 1)]}, seed(CUENTAS), tablas=[TCBP])
caso(411, 'ValidarMandatosPAC', 'call-devuelve-null', 'ValidarMandatosPAC.call returns null: the pipeline answers null', L('ValidarMandatosPAC.java', 'return null'),
     {'p_IdEmpresa': 1, 'p_IdUsuario': 51}, seed())
