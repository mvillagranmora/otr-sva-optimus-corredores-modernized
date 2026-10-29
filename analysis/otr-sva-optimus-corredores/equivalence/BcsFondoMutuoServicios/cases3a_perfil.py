"""FM-REQ-20 cases: ObtenerPerfilFondoMutuo (profile of the user)."""
from cases3a_lib import *

CONF = '"BSCL.FondoMutuo.BD::FMM.ConfiguracionFondoMutuo"'
ROLES = '"BSCL.Plataforma.BD.Vistas::ACC.ConsultarRolesPerfilesUsuario"'


def perfil(cid, ref, usuario, seed=None):
    caso(cid, "ObtenerPerfilFondoMutuo", ref, {"p_IdUsuario": usuario}, seed)


def generar():
    perfil("FM-REQ-20a_perfil-comercial", "FM-REQ-20: el usuario del universo base tiene el perfil comercial", 7)
    perfil("FM-REQ-20b_perfil-backoffice", "FM-REQ-20: perfil de back office",
           8, [insert(CONF, {"Key": "PERFIL_BACKOFFICE", "Value": "6"}), insert(ROLES, {"IdPerfil": 6, "IdUsuario": 8})])
    perfil("FM-REQ-20c_sin-perfil", "FM-REQ-20: usuario sin rol de fondos mutuos => PerfilFondoMutuo nulo y contador 0", 99)
    perfil("FM-REQ-20d_otro-perfil-no-cuenta", "FM-REQ-20: un rol que no es perfil de fondos mutuos no cuenta (la clave no es PERFIL_COMERCIAL/BACKOFFICE)",
           9, [insert(CONF, {"Key": "OTRA_CLAVE", "Value": "7"}), insert(ROLES, {"IdPerfil": 7, "IdUsuario": 9})])
    perfil("FM-REQ-20e_usuario-con-ambos-perfiles", "FM-REQ-20 D-REQ-8 (QD-09): TOP 1 sin ORDER BY con los dos perfiles: gana la primera fila que entrega la base",
           10, [insert(CONF, {"Key": "PERFIL_BACKOFFICE", "Value": "6"}), insert(ROLES, {"IdPerfil": 6, "IdUsuario": 10}), insert(ROLES, {"IdPerfil": 5, "IdUsuario": 10})])
    perfil("FM-REQ-20f_usuario-nulo", "FM-REQ-20: p_IdUsuario nulo: la consulta no encuentra filas", None)
