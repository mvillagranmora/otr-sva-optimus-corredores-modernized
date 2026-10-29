package cl.bcs.fondomutuo.adapter.out.jdbc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.sql.Timestamp;
import java.util.List;

import javax.sql.DataSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import cl.bcs.fondomutuo.TestBd;
import cl.bcs.fondomutuo.kit.InformacionEmpresa;
import cl.bcs.fondomutuo.kit.RevisarRequisitosBasicosSalida;

/** FM-KIT-30..45: ports {@code EmpresaPort}, {@code RevisarRequisitosBasicosPort} and {@code CalendarioPort} over the verbatim legacy SQL. */
class KitEmpresaRequisitosCalendarioTest {

    DataSource ds;
    ConexionUnidadDeTrabajo conexion;
    DiccionarioJdbc dic;
    RevisarRequisitosBasicosJdbc requisitos;

    @BeforeEach
    void preparar() {
        ds = TestBd.nueva();
        conexion = new ConexionUnidadDeTrabajo(ds);
        dic = new DiccionarioJdbc(conexion);
        requisitos = new RevisarRequisitosBasicosJdbc(conexion, dic);
        TestBd.ejecutar(ds,
                "INSERT INTO \"BSCL.Plataforma.BD::PLA.Configuracion\" VALUES (1,'PLA.LocalizacionSistema','es-CL')",
                "INSERT INTO \"BSCL.Plataforma.BD::PLA.Configuracion\" VALUES (1,'PLA.FechaSistema','2026-03-02')",
                "INSERT INTO \"BSCL.Plataforma.BD::PLA.Configuracion\" VALUES (1,'PLA.SegundosZonaHoraria','-10800')",
                "INSERT INTO \"BSCL.Plataforma.BD::PLA.Diccionario\" VALUES ('es-CL','USUARIO_NO_EXISTE','El usuario no existe')",
                "INSERT INTO \"BSCL.Plataforma.BD::PLA.Diccionario\" VALUES ('es-CL','USUARIO_NO_VIGENTE','Usuario no vigente')",
                "INSERT INTO \"BSCL.Plataforma.BD::PLA.Diccionario\" VALUES ('es-CL','MODULO_NO_EXISTE','Modulo no existe')",
                "INSERT INTO \"BSCL.Plataforma.BD::PLA.Diccionario\" VALUES ('es-CL','EMPRESA_NO_EXISTE','Empresa no existe')",
                "INSERT INTO \"BSCL.Plataforma.BD::PLA.Diccionario\" VALUES ('es-CL','USUARIO_VACIO','Usuario vacio')",
                "INSERT INTO \"BSCL.Plataforma.BD::PLA.Diccionario\" VALUES ('es-CL','MODULO_VACIO','Modulo vacio')",
                "INSERT INTO \"BSCL.Plataforma.BD::PLA.Modulo\" VALUES ('FMM')",
                "INSERT INTO \"BSCL.Plataforma.BD::ACC.Usuario\" VALUES (97,1,'ACT')",
                "INSERT INTO \"BSCL.Plataforma.BD::ACC.Usuario\" VALUES (98,1,'act')",
                "INSERT INTO \"BSCL.Plataforma.BD::ACC.Usuario\" VALUES (99,1,'BLO')",
                "INSERT INTO \"BSCL.Plataforma.BD::PLA.Empresa\" VALUES (1,'BCS','ACT',56,10)",
                "INSERT INTO \"BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio\" VALUES (10,500,1,7,8)",
                "INSERT INTO \"BSCL.SocioNegocio.BD::PER.Persona\" (\"IdPersona\",\"Tipo\",\"RazonSocial\",\"IdDocumentoIdentidad\") VALUES (500,'J','Bolsa SA',900)",
                "INSERT INTO \"BSCL.SocioNegocio.BD::PER.DocumentoIdentidad\" VALUES (900,'76.123.456-7')",
                "INSERT INTO \"BSCL.Plataforma.BD::INS.Corredora\" VALUES (3,500)",
                "INSERT INTO \"BSCL.Plataforma.BD::INS.CorredoraCodigos\" VALUES (3,'BCS','COD1')",
                "INSERT INTO \"BSCL.Plataforma.BD::INS.CorredoraCodigos\" VALUES (3,'OTR','COD2')",
                "INSERT INTO \"BSCL.Plataforma.BD::REG.Pais\" VALUES (56,'CL','Chile')");
    }

    @Test
    void fmKit30_empresa_leeLaFilaConLosJoinsDelLegacy() {
        InformacionEmpresa e = new EmpresaJdbc(conexion).informacion(1L);
        assertThat(e.IdEmpresa).isEqualTo(1L);
        assertThat(e.Codigo).isEqualTo("COD1");              // only the corredora code of bolsa 'BCS'
        assertThat(e.NombreCompleto).isEqualTo("Bolsa SA");  // tipo J: razon social
        assertThat(e.DocumentoIdentidad).isEqualTo("76.123.456-7");
        assertThat(e.IdContraparte).isEqualTo(8L);
        assertThat(e.IdCorredora).isEqualTo(3L);
        assertThat(e.CodigoPais).isEqualTo("CL");
    }

    @Test
    void fmKit31_empresaInexistente_fallaConElMensajeDelLegacy() {
        assertThatThrownBy(() -> new EmpresaJdbc(conexion).informacion(77L)).isInstanceOf(IllegalStateException.class).hasMessage("Empresa no existe");
    }

    @Test
    void fmKit32_requisitos_todoBien_noHayMensajes() {
        RevisarRequisitosBasicosSalida s = requisitos.revisar(1L, 97L, "FMM");
        assertThat(s.p_out_Mensajes).isEmpty();
    }

    @Test
    void fmKit33_requisitos_empresaVacia_retornaSinRevisarUsuarioNiModulo() {
        RevisarRequisitosBasicosSalida s = requisitos.revisar(null, null, null);
        assertThat(s.p_out_Mensajes).hasSize(1);
        assertThat(s.p_out_Mensajes.get(0).codigo).isEqualTo("EMPRESA_VACIA");
        assertThat(s.p_out_Mensajes.get(0).descripcion).isEqualTo("Empresa vacia");
        assertThat(s.p_out_Mensajes.get(0).tipo).isEqualTo("ERROR");
        assertThat(s.p_out_Mensajes.get(0).id).isEqualTo(0L);
    }

    @Test
    void fmKit34_requisitos_usuarioYModulo_suOrdenYSuTexto() {
        assertThat(requisitos.revisar(1L, 123L, "XYZ").p_out_Mensajes).extracting(m -> m.codigo).containsExactly("USUARIO_NO_EXISTE", "MODULO_NO_EXISTE");
        assertThat(requisitos.revisar(1L, 99L, "FMM").p_out_Mensajes).extracting(m -> m.codigo).containsExactly("USUARIO_NO_VIGENTE");
        assertThat(requisitos.revisar(1L, 98L, "FMM").p_out_Mensajes).isEmpty();      // equalsIgnoreCase("ACT")
        assertThat(requisitos.revisar(1L, null, null).p_out_Mensajes).extracting(m -> m.codigo).containsExactly("USUARIO_VACIO", "MODULO_VACIO");
        assertThat(requisitos.revisar(1L, 123L, "FMM").p_out_Mensajes.get(0).descripcion).isEqualTo("El usuario no existe");
    }

    @Test
    void fmKit35_requisitos_empresaSinSocioNiCliente_tresMensajesSiNoHayTexto() {
        TestBd.ejecutar(ds, "INSERT INTO \"BSCL.Plataforma.BD::PLA.Empresa\" VALUES (2,'X','ACT',56,NULL)");
        TestBd.ejecutar(ds, "INSERT INTO \"BSCL.Plataforma.BD::PLA.Configuracion\" VALUES (2,'PLA.LocalizacionSistema','es-CL')");
        List<String> codigos = requisitos.revisar(2L, 97L, "FMM").p_out_Mensajes.stream().map(m -> m.codigo).toList();
        assertThat(codigos).containsExactly("SIN_DEFINIR_SOCIO_DE_NEGOCIO_PARA_EMPRESA", "SOCIO_DE_NEGOCIO_DE_EMPRESA_NO_EXISTE", "EMPRESA_NO_CONFIGURADA_COMO_CLIENTE",
                "USUARIO_NO_EXISTE");
    }

    @Test
    void fmKit36_feriados_soloActivosDeLosPaisesPedidos() {
        TestBd.ejecutar(ds, "INSERT INTO \"BSCL.Plataforma.BD::REG.DiaFeriado\" VALUES (1,'N','2026-01-01',1,'a',2026,56,'ACT')",
                "INSERT INTO \"BSCL.Plataforma.BD::REG.DiaFeriado\" VALUES (2,'N','2026-01-02',2,'b',2026,56,'INA')",
                "INSERT INTO \"BSCL.Plataforma.BD::REG.DiaFeriado\" VALUES (3,'N','2026-07-04',4,'c',2026,840,'ACT')");
        CalendarioJdbc cal = new CalendarioJdbc(conexion, new ConfiguracionSistemaJdbc(conexion));
        assertThat(cal.feriados(List.of(56L, 840L))).extracting(f -> f.get("IdFeriado")).containsExactlyInAnyOrder(1L, 3L);
        assertThat(cal.feriados(List.of(32L))).isEmpty();
    }

    @Test
    void fmKit37_monedaFormato_lasColumnasDeLaTabla() {
        TestBd.ejecutar(ds, "INSERT INTO \"BSCL.Plataforma.BD::PLA.MonedaFormato\" VALUES (1,'CLP','MON','FM','APORTE',0,'T',0,'T',0,'T',0,'R',2,'R',4,'R',0,'T','ACT')");
        List<java.util.Map<String, Object>> filas = new CalendarioJdbc(conexion, new ConfiguracionSistemaJdbc(conexion)).monedaFormato();
        assertThat(filas).hasSize(1);
        assertThat(filas.get(0)).containsEntry("IdMoneda", "CLP").containsEntry("MontoDecimales", 0).containsEntry("TipoCambioDecimales", 2);
    }

    @Test
    void fmKit38_reloj_fechaDelSistema_conLaHoraUtcDesplazada_sinCache() {
        Timestamp t = new CalendarioJdbc(conexion, new ConfiguracionSistemaJdbc(conexion)).fechaHoraSistemaLocal(1L);
        // PLA.FechaSistema 2026-03-02 + (14:30:00 UTC - 10800 s) = 11:30:00
        assertThat(t).isEqualTo(Timestamp.valueOf("2026-03-02 11:30:00"));
    }
}
