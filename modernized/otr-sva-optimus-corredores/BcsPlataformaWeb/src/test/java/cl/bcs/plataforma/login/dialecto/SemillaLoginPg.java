package cl.bcs.plataforma.login.dialecto;

/** Fake rows (no real people, no real data) for the bootstrap flows, as INSERTs in HANA shape for the H2 schema of the login tests. */
final class SemillaLoginPg {

    private SemillaLoginPg() {
    }

    /** the same rows the existing BootstrapTest seeds, plus one user and the session of the token TOKEN-DE-PRUEBA */
    static String[] bootstrapHana() {
        return new String[] {
                "INSERT INTO \"BSCL.Plataforma.BD::PLA.Configuracion\" VALUES (1, 'PLA.Ambiente', 'DES')",
                "INSERT INTO \"BSCL.Plataforma.BD::PLA.Configuracion\" VALUES (1, 'PLA.SegundosZonaHoraria', '-10800')",
                "INSERT INTO \"BSCL.Plataforma.BD::PLA.Configuracion\" VALUES (1, 'PLA.MuestraSSOGoogle', 'S')",
                "INSERT INTO \"BSCL.Plataforma.BD::PLA.Configuracion\" VALUES (1, 'PLA.LocalizacionSistema', 'CL')",
                "INSERT INTO \"BSCL.Plataforma.BD::PLA.Configuracion\" VALUES (1, 'PLA.Version', '3.2')",
                "INSERT INTO \"BSCL.Plataforma.BD::PLA.Configuracion\" VALUES (1, 'PLA.ZonaHoraria', 'America/Santiago')",
                "INSERT INTO \"BSCL.Plataforma.BD::PLA.Configuracion\" VALUES (1, 'PLA.MonedaSistema', 'CLP')",
                "INSERT INTO \"BSCL.Plataforma.BD::PLA.Configuracion\" VALUES (1, 'PLA.BolsaDefectoSistema', 'BCS')",
                "INSERT INTO \"BSCL.APV.BD::PLA.ConfiguracionAPV\" VALUES (1, 'APV.Habilitado', 'S')",
                "INSERT INTO \"BSCL.Plataforma.BD::PRO.Configuracion\" VALUES (1, 'PRODUCTO.Cualquiera', 'si-sale')",
                "INSERT INTO \"BSCL.Plataforma.BD::PLA.Diccionario\" VALUES ('HOLA', 'CL', ' Hola ', ' H ')",
                "INSERT INTO \"BSCL.Plataforma.BD::PLA.Diccionario\" VALUES ('CHAO', 'ES', 'Bye', NULL)",
                "INSERT INTO \"BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa\" VALUES (1, 'Corredora Ficticia S.A.', 'ACT')",
                "INSERT INTO \"BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa\" VALUES (2, 'Inactiva', 'BLO')",
                "INSERT INTO \"BSCL.Plataforma.BD.Vistas::PLA.ConsultarImagen\" VALUES ('LOGO_CORREDOR_LOGIN', X'0102FF')",
                "INSERT INTO \"BSCL.Plataforma.BD::REG.DiaFeriado\" VALUES (1, 'FERIADO', DATE '2026-03-04', NULL, 'FERIADO FICTICIO', 2026, 44, 'ACT')",
                "INSERT INTO \"BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento\" VALUES ('ACC', 'COMCN')",
                "INSERT INTO \"BSCL.Plataforma.BD::INS.TipoOperacion\" VALUES ('COMCN', 'COMPRA', 'RV', 'N', 'N', 'S')",
                "INSERT INTO \"BSCL.Plataforma.BD.Vistas::PLA.ConsultarConfiguracionUsuario\" VALUES (1, 1, 'tema', 'oscuro')",
                "INSERT INTO \"BSCL.Plataforma.BD.Vistas::PLA.ConsultarConfiguracionGrilla\" VALUES (1, 'filas', '50')",
                "INSERT INTO \"BSCL.Plataforma.BD::PLA.ConfiguracionFiltroInterfaz\" VALUES (1, 'ORD', 'COMBO', 'cmbMoneda', 'ACT', 'a=1')",
                "INSERT INTO \"BSCL.Plataforma.BD::PLA.Empresa\" VALUES (1, 'Corredora Ficticia S.A.', 44, 100)",
                "INSERT INTO \"BSCL.SocioNegocio.BD::SOC.SocioDeNegocio\" VALUES (100, NULL, NULL, 777, 1)",
                "INSERT INTO \"BSCL.Plataforma.BD::INS.Corredora\" VALUES (50, 777)",
                "INSERT INTO \"BSCL.Plataforma.BD::PLA.ValorDefectoSistema\" VALUES ('V1', 'Valor uno', 'x', 1, 'ACT')",
                "INSERT INTO \"BSCL.Plataforma.BD.Vistas::ACC.ConsultarUsuarioAcceso\" VALUES (1, 1, 'mnuOrdenes', 'S', 'S', 'S', 0, 1, 10, 'Ordenes', 'MENU', 700)",
                "INSERT INTO \"BSCL.Plataforma.BD::ACC.Usuario\" (\"IdUsuario\",\"IdEmpresa\",\"CodigoUsuario\",\"Clave\",\"FechaVigencia\",\"IdEstado\",\"IntentosConexion\","
                        + "\"FechaBloqueoTemporal\",\"EsAutorizado\",\"TipoEntidad\",\"IdEntidad\",\"UsuarioCorredores\",\"CodigoMenu\") VALUES (1,1,'JPEREZ','X',DATE '0001-01-01','ACT',0,"
                        + "TIMESTAMP '0001-01-01 00:00:00','S','EMP',100,'S','menu_corredor')",
                "INSERT INTO \"BSCL.Plataforma.BD::ACC.Sesion\" (\"IdTokenSesion\",\"IdUsuario\",\"TokenValido\",\"FechaUltimoUso\",\"FechaIngreso\",\"IdCanal\") VALUES ('TOKEN-DE-PRUEBA',"
                        + "1,'S',TIMESTAMP '2026-03-02 08:00:00',TIMESTAMP '2026-03-02 08:00:00','COR')" };
    }
}
