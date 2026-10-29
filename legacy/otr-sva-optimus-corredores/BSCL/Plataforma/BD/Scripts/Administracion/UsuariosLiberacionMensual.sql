--Script para la habilitación de los usuarios del laboratorio asociado a la nivelación mensual
--L4b0RatOr1o+
--ee0cb0b499f44e809a369e695f4019e1a9bb01fccacdc299afc3bf74fa726105

--=========SURA=========

--CORREDORES
UPSERT BSCL."BSCL.Plataforma.BD::ACC.Usuario"
("IdUsuario", "IdEmpresa", "CodigoUsuario", "Clave", "FechaIngreso", "FechaVigencia", "FechaUltimaConexion", "DiasVencimientoClave", "Conectado", "Hora", "Terminal", "IdEstado", "IntentosConexion", "FechaBloqueoTemporal", "TipoEntidad", "IdEntidad", "EsAutorizado", "DebeCambiarClave", "UltimoCambioClave", "CodigoMenu", "IdOrigen", "IdCarga", "ClaveProvisoria", "IntentosConexionClaveProvisoria", "FechaClaveProvisoria", "UsuarioClientes", "UsuarioNegocios", "UsuarioServicios", "UsuarioCorredores", "EsUsuarioBolsa")
VALUES(99999, 1, 'PRUEBASLIBERACION', '13CA9D17DC3CA121A633E9850CBD396641D968FA9E883D756AA6FA222C77A7B2', '0001-01-01', '2999-12-31', '2022-10-22', 999999.00000000, 'N', '23:42:03', '', 'ACT', 0, '0001-01-01 00:00:00.000', 'EMP', 839, 'S', 'N', '2022-09-22 20:29:27.000', '', 9999, 99999, '', 0, '0001-01-01 00:00:00.000', 'N', 'S', 'S', 'S', 'N')
WITH PRIMARY KEY;

UPSERT BSCL."BSCL.Plataforma.BD::ACC.Usuario"
("IdUsuario", "IdEmpresa", "CodigoUsuario", "Clave", "FechaIngreso", "FechaVigencia", "FechaUltimaConexion", "DiasVencimientoClave", "Conectado", "Hora", "Terminal", "IdEstado", "IntentosConexion", "FechaBloqueoTemporal", "TipoEntidad", "IdEntidad", "EsAutorizado", "DebeCambiarClave", "UltimoCambioClave", "CodigoMenu", "IdOrigen", "IdCarga", "ClaveProvisoria", "IntentosConexionClaveProvisoria", "FechaClaveProvisoria", "UsuarioClientes", "UsuarioNegocios", "UsuarioServicios", "UsuarioCorredores", "EsUsuarioBolsa")
VALUES(99999, 1, 'PRUEBASLIBERACION', '13CA9D17DC3CA121A633E9850CBD396641D968FA9E883D756AA6FA222C77A7B2', '0001-01-01', '2999-12-31', '2022-10-22', 999999.00000000, 'N', '23:42:03', '', 'BLO', 0, '0001-01-01 00:00:00.000', 'EMP', 839, 'S', 'N', '2022-09-22 20:29:27.000', '', 9999, 99999, '', 0, '0001-01-01 00:00:00.000', 'N', 'S', 'S', 'S', 'N')
WITH PRIMARY KEY;


--CLIENTES
UPSERT BSCL."BSCL.Plataforma.BD::ACC.Usuario"
("IdUsuario", "IdEmpresa", "CodigoUsuario", "Clave", "FechaIngreso", "FechaVigencia", "FechaUltimaConexion", "DiasVencimientoClave", "Conectado", "Hora", "Terminal", "IdEstado", "IntentosConexion", "FechaBloqueoTemporal", "TipoEntidad", "IdEntidad", "EsAutorizado", "DebeCambiarClave", "UltimoCambioClave", "CodigoMenu", "IdOrigen", "IdCarga", "ClaveProvisoria", "IntentosConexionClaveProvisoria", "FechaClaveProvisoria", "UsuarioClientes", "UsuarioNegocios", "UsuarioServicios", "UsuarioCorredores", "EsUsuarioBolsa")
VALUES(999999, 1, 'PRUEBASLIBERACIONCLIENTE', '13CA9D17DC3CA121A633E9850CBD396641D968FA9E883D756AA6FA222C77A7B2', '2020-06-30', '0001-01-01', '2022-11-10', 60.00000000, 'S', '20:23:51', '', 'ACT', 0, '0001-01-01 00:00:00.000', 'CLI', 46714, 'S', 'N', '2022-09-21 14:29:23.000', '', 666, 666, '', 0, '0001-01-01 00:00:00.000', 'S', 'N', 'N', 'N', 'N')
WITH PRIMARY KEY;

UPSERT BSCL."BSCL.Plataforma.BD::ACC.Usuario"
("IdUsuario", "IdEmpresa", "CodigoUsuario", "Clave", "FechaIngreso", "FechaVigencia", "FechaUltimaConexion", "DiasVencimientoClave", "Conectado", "Hora", "Terminal", "IdEstado", "IntentosConexion", "FechaBloqueoTemporal", "TipoEntidad", "IdEntidad", "EsAutorizado", "DebeCambiarClave", "UltimoCambioClave", "CodigoMenu", "IdOrigen", "IdCarga", "ClaveProvisoria", "IntentosConexionClaveProvisoria", "FechaClaveProvisoria", "UsuarioClientes", "UsuarioNegocios", "UsuarioServicios", "UsuarioCorredores", "EsUsuarioBolsa")
VALUES(999999, 1, 'PRUEBASLIBERACIONCLIENTE', '13CA9D17DC3CA121A633E9850CBD396641D968FA9E883D756AA6FA222C77A7B2', '2020-06-30', '0001-01-01', '2022-11-10', 60.00000000, 'S', '20:23:51', '', 'BLO', 0, '0001-01-01 00:00:00.000', 'CLI', 46714, 'S', 'N', '2022-09-21 14:29:23.000', '', 666, 666, '', 0, '0001-01-01 00:00:00.000', 'S', 'N', 'N', 'N', 'N')
WITH PRIMARY KEY;


--=========MBI=========

--CORREDORES
UPSERT BSCL."BSCL.Plataforma.BD::ACC.Usuario"
("IdUsuario", "IdEmpresa", "CodigoUsuario", "Clave", "FechaIngreso", "FechaVigencia", "FechaUltimaConexion", "DiasVencimientoClave", "Conectado", "Hora", "Terminal", "IdEstado", "IntentosConexion", "FechaBloqueoTemporal", "TipoEntidad", "IdEntidad", "EsAutorizado", "DebeCambiarClave", "UltimoCambioClave", "CodigoMenu", "IdOrigen", "IdCarga", "ClaveProvisoria", "IntentosConexionClaveProvisoria", "FechaClaveProvisoria", "UsuarioClientes", "UsuarioNegocios", "UsuarioServicios", "UsuarioCorredores", "EsUsuarioBolsa")
VALUES(99999, 1, 'PRUEBASLIBERACION', '13CA9D17DC3CA121A633E9850CBD396641D968FA9E883D756AA6FA222C77A7B2', '0001-01-01', '0001-01-01', '2022-11-14', 999999.00000000, 'S', '14:28:07', ' ', 'ACT', 0, '0001-01-01 00:00:00.000', 'EMP', 108, 'S', 'N', '2022-01-17 19:22:14.000', '', 9999, 99999, '', 0, '0001-01-01 00:00:00.000', 'N', 'S', 'S', 'S', 'S')
WITH PRIMARY KEY;

UPSERT BSCL."BSCL.Plataforma.BD::ACC.Usuario"
("IdUsuario", "IdEmpresa", "CodigoUsuario", "Clave", "FechaIngreso", "FechaVigencia", "FechaUltimaConexion", "DiasVencimientoClave", "Conectado", "Hora", "Terminal", "IdEstado", "IntentosConexion", "FechaBloqueoTemporal", "TipoEntidad", "IdEntidad", "EsAutorizado", "DebeCambiarClave", "UltimoCambioClave", "CodigoMenu", "IdOrigen", "IdCarga", "ClaveProvisoria", "IntentosConexionClaveProvisoria", "FechaClaveProvisoria", "UsuarioClientes", "UsuarioNegocios", "UsuarioServicios", "UsuarioCorredores", "EsUsuarioBolsa")
VALUES(99999, 1, 'PRUEBASLIBERACION', '13CA9D17DC3CA121A633E9850CBD396641D968FA9E883D756AA6FA222C77A7B2', '0001-01-01', '0001-01-01', '2022-11-14', 999999.00000000, 'S', '14:28:07', ' ', 'BLO', 0, '0001-01-01 00:00:00.000', 'EMP', 108, 'S', 'N', '2022-01-17 19:22:14.000', '', 9999, 99999, '', 0, '0001-01-01 00:00:00.000', 'N', 'S', 'S', 'S', 'S')
WITH PRIMARY KEY;


--CLIENTES
UPSERT BSCL."BSCL.Plataforma.BD::ACC.Usuario"
("IdUsuario", "IdEmpresa", "CodigoUsuario", "Clave", "FechaIngreso", "FechaVigencia", "FechaUltimaConexion", "DiasVencimientoClave", "Conectado", "Hora", "Terminal", "IdEstado", "IntentosConexion", "FechaBloqueoTemporal", "TipoEntidad", "IdEntidad", "EsAutorizado", "DebeCambiarClave", "UltimoCambioClave", "CodigoMenu", "IdOrigen", "IdCarga", "ClaveProvisoria", "IntentosConexionClaveProvisoria", "FechaClaveProvisoria", "UsuarioClientes", "UsuarioNegocios", "UsuarioServicios", "UsuarioCorredores", "EsUsuarioBolsa")
VALUES(999999, 1, 'PRUEBASLIBERACIONCLIENTE', '13CA9D17DC3CA121A633E9850CBD396641D968FA9E883D756AA6FA222C77A7B2', '2022-11-08', '0001-01-01', '2022-11-07', 30.00000000, 'S', '18:42:58', '', 'ACT', 0, '0001-01-01 00:00:00.000', 'CLI', 10564, 'S', 'N', '2022-11-08 00:00:00.000', '', 1, 1, '', 0, '0001-01-01 00:00:00.000', 'S', 'N', 'N', 'N', 'N')
WITH PRIMARY KEY;

UPSERT BSCL."BSCL.Plataforma.BD::ACC.Usuario"
("IdUsuario", "IdEmpresa", "CodigoUsuario", "Clave", "FechaIngreso", "FechaVigencia", "FechaUltimaConexion", "DiasVencimientoClave", "Conectado", "Hora", "Terminal", "IdEstado", "IntentosConexion", "FechaBloqueoTemporal", "TipoEntidad", "IdEntidad", "EsAutorizado", "DebeCambiarClave", "UltimoCambioClave", "CodigoMenu", "IdOrigen", "IdCarga", "ClaveProvisoria", "IntentosConexionClaveProvisoria", "FechaClaveProvisoria", "UsuarioClientes", "UsuarioNegocios", "UsuarioServicios", "UsuarioCorredores", "EsUsuarioBolsa")
VALUES(999999, 1, 'PRUEBASLIBERACIONCLIENTE', '13CA9D17DC3CA121A633E9850CBD396641D968FA9E883D756AA6FA222C77A7B2', '2022-11-08', '0001-01-01', '2022-11-07', 30.00000000, 'S', '18:42:58', '', 'BLO', 0, '0001-01-01 00:00:00.000', 'CLI', 10564, 'S', 'N', '2022-11-08 00:00:00.000', '', 1, 1, '', 0, '0001-01-01 00:00:00.000', 'S', 'N', 'N', 'N', 'N')
WITH PRIMARY KEY;
