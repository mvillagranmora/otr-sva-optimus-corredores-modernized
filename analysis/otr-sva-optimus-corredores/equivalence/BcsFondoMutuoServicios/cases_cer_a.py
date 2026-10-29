"""FMM-CER cases of front F-A (FM-CER-01..14): contract, requirements, parameters, reads, quota value, the validation rules per solicitud and the early exit.

Every case names the cards (FM-CER-nn) and the defects (D-F3B-nn / D-NOR-nn) it pins in `ref`. `modo="continua"` = at least one solicitud survives F-A (recorded twice: `completo`
and `parada`, see cases_cer_lib); the default `modo="terminal"` = the legacy ends (or throws) inside F-A.
"""
import cases_cer_lib as L
import cases_cer_caso as C
from cases_cer_lib import aporte, rescate, solo_tabla, config, param, ins, Num  # noqa: F401
from cases_cer_caso import caso, entrada


def _mal(id_):
    """a solicitud that fails ONLY the state rule (state CRT)"""
    return rescate(id_, estado="CRT")


def generar():
    # ------------------------------------------------------------------ FM-CER-01 contract
    caso("contrato-minimo-con-defaults", ["FM-CER-01", "D-NOR-9"],
         {"p_IdEmpresa": 1, "p_IdUsuario": 7, "p_IdModulo": "FMM", "p_FechaCertificacion": "2026-03-02", "p_Solicitudes": [{"IdSolicitudAporteRescate": 1001}]},
         seed=[rescate(1001)], modo="continua")
    caso("contrato-campos-extra-y-canal-por-solicitud", ["FM-CER-01"],
         entrada([{"IdSolicitudAporteRescate": 1001, "IdTipoCanalIngreso": "OWP"}], canal="AGE", p_Filtros=[], p_NotificaOWP="N", p_CertificacionAporteSwitchSimulado="S", campo_ajeno=7),
         seed=[rescate(1001)], modo="continua")
    caso("contrato-ids-y-empresa-como-texto", ["FM-CER-01", "D-NOR-9"], entrada(["1001"], p_IdEmpresa="1", p_IdUsuario="7"), seed=[rescate(1001)], modo="continua")
    caso("contrato-id-no-numerico", ["FM-CER-01", "D-NOR-9"], entrada(["abc"]), seed=[rescate(1001)])
    caso("contrato-solicitudes-nulas", ["FM-CER-01", "FM-CER-71"], entrada(None), seed=[rescate(1001)])
    caso("contrato-sin-clave-solicitudes", ["FM-CER-01", "FM-CER-02"], {k: v for k, v in entrada([1001]).items() if k != "p_Solicitudes"}, seed=[rescate(1001)])
    caso("contrato-id-nulo-en-solicitud", ["FM-CER-01", "FM-CER-04", "D-NOR-9"], entrada([{"IdSolicitudAporteRescate": None}]), seed=[rescate(1001)])
    caso("contrato-fecha-ausente", ["FM-CER-01", "FM-CER-07"], {k: v for k, v in entrada([1001]).items() if k != "p_FechaCertificacion"}, seed=[rescate(1001)])
    caso("contrato-fecha-con-otro-formato", ["FM-CER-01", "FM-CER-07", "D-FEC-6"], entrada([1001], fecha="2026/03/02"), seed=[rescate(1001)])
    caso("contrato-simulacion-S-sale-temprano", ["FM-CER-01", "FM-CER-14", "D-F3B-01"], entrada([1001], sim="S"), seed=[_mal(1001)])
    caso("contrato-empresa-nula", ["FM-CER-01", "FM-CER-71", "DESVIO-KIT-1"], entrada([1001], p_IdEmpresa=None), seed=[rescate(1001)])
    caso("contrato-elemento-nulo-en-la-lista", ["FM-CER-01", "FM-CER-71", "D-NOR-9"], {**entrada([1001]), "p_Solicitudes": [None]}, seed=[rescate(1001)])
    caso("contrato-mismo-id-con-distinto-canal-es-duplicado", ["FM-CER-01", "FM-CER-02"], entrada([{"IdSolicitudAporteRescate": 1001, "IdTipoCanalIngreso": "OPT"},
                                                                                               {"IdSolicitudAporteRescate": 1001, "IdTipoCanalIngreso": "OWP"}]), seed=[rescate(1001)])
    caso("contrato-usuario-y-modulo-nulos", ["FM-CER-01", "FM-CER-02"], entrada([1001], p_IdUsuario=None, p_IdModulo=None), seed=[rescate(1001)])

    # ------------------------------------------------------------------ FM-CER-02 requirements, empty list, repeated ids
    caso("usuario-inexistente", ["FM-CER-02"], entrada([1001], p_IdUsuario=99), seed=[rescate(1001)])
    caso("usuario-inactivo", ["FM-CER-02"], entrada([1001], p_IdUsuario=9), seed=[rescate(1001)])
    caso("modulo-inexistente", ["FM-CER-02"], entrada([1001], p_IdModulo="XXX"), seed=[rescate(1001)])
    caso("empresa-sin-configuracion", ["FM-CER-02", "FM-CER-03", "DESVIO-KIT-1"], entrada([1001], p_IdEmpresa=2, p_IdUsuario=8), seed=[rescate(1001)])
    caso("empresa-inexistente", ["FM-CER-02", "FM-CER-03", "DESVIO-KIT-1"], entrada([1001], p_IdEmpresa=77, p_IdUsuario=7), seed=[rescate(1001)])
    caso("lista-vacia", ["FM-CER-02"], entrada([]), seed=[rescate(1001)])
    caso("ids-repetidos", ["FM-CER-02", "D-REQ-11"], entrada([1001, 1001]), seed=[rescate(1001)])
    caso("ids-repetidos-no-consecutivos", ["FM-CER-02"], entrada([1001, 1002, 1001]), seed=[rescate(1001), rescate(1002)])
    caso("dos-ids-nulos-cuentan-como-duplicado", ["FM-CER-02", "D-NOR-9"], entrada([{"IdSolicitudAporteRescate": None}, {"IdSolicitudAporteRescate": None}]), seed=[rescate(1001)])
    caso("requisitos-ganan-a-lista-vacia", ["FM-CER-02"], entrada([], p_IdUsuario=99), seed=[rescate(1001)])
    caso("requisitos-ganan-a-duplicados", ["FM-CER-02"], entrada([1001, 1001], p_IdUsuario=99), seed=[rescate(1001)])
    caso("pais-sistema-faltante-gana-a-requisitos", ["FM-CER-02", "FM-CER-03"], entrada([1001], p_IdUsuario=99), seed=[rescate(1001), config("PLA.PaisSistema", None)])
    caso("pais-sistema-no-numerico", ["FM-CER-02", "FM-CER-03"], entrada([1001]), seed=[rescate(1001), config("PLA.PaisSistema", "abc")])
    caso("duplicados-ganan-a-parametros-faltantes", ["FM-CER-02", "FM-CER-03"], entrada([1001, 1001]), seed=[rescate(1001), param("VALIDAR_CERTIFICACION", None)])
    caso("lista-vacia-gana-a-parametros-faltantes", ["FM-CER-02", "FM-CER-03"], entrada([]), seed=[param("VALIDAR_CERTIFICACION", None), config("PLA.FechaSistema", None)])

    # ------------------------------------------------------------------ FM-CER-03 parameters
    obligatorios = (("fecha-sistema", config, "PLA.FechaSistema"), ("tipo-comprobante-no-apv", param, "TipoComprobanteFacturaNoApv"),
                    ("tipo-comprobante-apv", param, "TipoComprobanteFacturaApv"), ("calcular-comision", param, "CALCULAR_COMISION_CERTIFICACION"),
                    ("validar-certificacion", param, "VALIDAR_CERTIFICACION"), ("rescate-genera-spot", param, "RESCATE_GENERA_VENTA_SPOT_DOLAR"),
                    ("comprobante-automatico", param, "RESCATE_COMISION_COMPROBANTE_AUTOMATICO"), ("notificar-owp", config, "PLA.NotificarEstadoOrdenOWP"))
    for nombre, fn, codigo in obligatorios:
        caso("parametro-obligatorio-faltante-" + nombre, ["FM-CER-03"], entrada([1001]), seed=[rescate(1001), fn(codigo, None)])
    caso("parametros-faltantes-gana-el-primero-que-se-lee", ["FM-CER-03"], entrada([1001]), seed=[rescate(1001), param("VALIDAR_CERTIFICACION", None), param("TipoComprobanteFacturaApv", None)])
    caso("parametro-opcional-retencion-ausente", ["FM-CER-03", "D-F3B-09"], entrada([1001]), seed=[rescate(1001)], modo="continua")
    caso("parametro-opcional-retencion-presente", ["FM-CER-03", "D-F3B-09"], entrada([1001]), seed=[rescate(1001), config("APV.PorcentajeRetencion", "15")], modo="continua")
    caso("parametro-opcional-retencion-no-numerico", ["FM-CER-03", "D-F3B-09"], entrada([1001]), seed=[rescate(1001), config("APV.PorcentajeRetencion", "quince")], modo="continua")
    caso("parametros-opcionales-tgr-y-sii-ausentes", ["FM-CER-03", "FM-CER-13"], entrada([1001]), seed=[rescate(1001), config("APV.DocumentoIdentidadPersonaTGR", None),
                                                                                                     config("APV.DocumentoIdentidadPersonaSII", None)], modo="continua")
    caso("parametros-con-minusculas-no-activan-nada", ["FM-CER-03", "FM-CER-11"], entrada([1001]),
         seed=[aporte(1001), param("VALIDAR_CERTIFICACION", "s"), param("CALCULAR_COMISION_CERTIFICACION", "s")], modo="continua")

    # ------------------------------------------------------------------ FM-CER-04 reads
    caso("ninguna-solicitud-existe", ["FM-CER-04"], entrada([9999]), seed=[rescate(1001)])
    caso("una-existe-y-otra-no-se-ignora-sin-mensaje", ["FM-CER-04"], entrada([1001, 9999]), seed=[rescate(1001)], modo="continua")
    caso("solicitud-de-otra-empresa-no-se-lee", ["FM-CER-04"], entrada([1001]), seed=[rescate(1001, IdEmpresa=2)])
    caso("orden-de-lectura-por-tipo-solicitud", ["FM-CER-04", "FM-CER-19"], entrada([1003, 1001, 1002]), seed=[rescate(1003), aporte(1001), rescate(1002)], modo="continua")
    caso("traspaso-apv-y-no-apv-mismo-id-gana-el-no-apv", ["FM-CER-04", "FM-CER-35", "D-NOR-9"], entrada([2001]),
         seed=[rescate(2001), L.persona_con_documento(101, 11, "88888881-1"), [ins(L.TRA_APV, IdTraspaso=71, RazonRetiro="TRA", IdPersona=101, DocumentoEntidad="88888881-1"),
                                                                              ins(L.TRA_ORDEN, IdSolicitudAporteRescate=2001, IdTraspaso=71),
                                                                              ins("BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona", IdPersona=101, NombreCompleto="Persona APV", DocumentoIdentidad="88888881-1"),
                                                                              ins(L.ENT_REC, IdEntidadRecaudadora=5, Nombre="Entidad", IdPersona=202), ins(L.TRA_FM, IdTraspaso=81, IdTipoMovimientoTraspaso="TRA", IdEntidadRecaudadora=5),
                                                                              ins(L.TRA_DET, IdSolicitudAporteRescate=2001, IdTraspaso=81)]], modo="continua")
    caso("traspaso-solo-apv", ["FM-CER-04", "FM-CER-35"], entrada([2001]),
         seed=[rescate(2001), [ins(L.TRA_APV, IdTraspaso=71, RazonRetiro="TRA", IdPersona=101, DocumentoEntidad="88888881-1"), ins(L.TRA_ORDEN, IdSolicitudAporteRescate=2001, IdTraspaso=71),
                               ins("BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona", IdPersona=101, NombreCompleto="Persona APV", DocumentoIdentidad="88888881-1")]], modo="continua")
    caso("traspaso-de-la-solicitud-relacionada", ["FM-CER-04", "FM-CER-35"], entrada([2002]),
         seed=[rescate(2002, IdSolicitudAporteRescateRelacionado=2001), solo_tabla(2001, "RES", "CRT"),
               [ins(L.ENT_REC, IdEntidadRecaudadora=5, Nombre="Entidad", IdPersona=202), ins(L.TRA_FM, IdTraspaso=81, IdTipoMovimientoTraspaso="TRA", IdEntidadRecaudadora=5),
                ins(L.TRA_DET, IdSolicitudAporteRescate=2001, IdTraspaso=81)]], modo="continua")
    caso("fila-duplicada-por-join-de-comision", ["FM-CER-04", "D-F3B-12", "D-NOR-9"], entrada([1001]),
         seed=[rescate(1001), [ins(L.COMISION, IdSerieFondo="FM001A", TipoComision="PER"), ins(L.COMISION, IdSerieFondo="FM001A", TipoComision="PER")]])
    caso("comision-no-periodica-no-duplica", ["FM-CER-04", "D-F3B-12"], entrada([1001]), seed=[rescate(1001), [ins(L.COMISION, IdSerieFondo="FM001A", TipoComision="TRI"),
                                                                                                         ins(L.COMISION, IdSerieFondo="FM001A", TipoComision="TRI")]], modo="continua")
    caso("estado-duplicado-en-pla-estado", ["FM-CER-04", "D-NOR-9"], entrada([1001]), seed=[rescate(1001), L.estado_pla("ING", "Ingresada otra vez")])
    caso("persona-tgr-parametrizada-pero-inexistente", ["FM-CER-04", "FM-CER-13"], entrada([1001]), seed=[rescate(1001), config("APV.DocumentoIdentidadPersonaTGR", "99999990-0")],
         modo="continua")

    # ------------------------------------------------------------------ FM-CER-05 / 08 quota value loaded
    def pr(i, **k):
        return C.L.precio(i, **k)
    caso("valor-cuota-cargado-correcto", ["FM-CER-05", "FM-CER-08"], entrada([1001, 1002]), seed=[rescate(1001), aporte(1002)], modo="continua")
    caso("valor-cuota-sin-precio", ["FM-CER-08"], entrada([1001]), seed=[rescate(1001)], colaboradores=L.precios())
    caso("valor-cuota-fecha-fuente-nula", ["FM-CER-08"], entrada([1001]), seed=[rescate(1001)], colaboradores=L.precios(pr(1001, fecha=None)))
    caso("valor-cuota-fecha-fuente-otro-dia", ["FM-CER-08"], entrada([1001]), seed=[rescate(1001)], colaboradores=L.precios(pr(1001, fecha="2026-03-01 00:00:00.000")))
    caso("valor-cuota-misma-fecha-con-hora", ["FM-CER-08"], entrada([1001]), seed=[rescate(1001)], colaboradores=L.precios(pr(1001, fecha="2026-03-02 23:59:59.000")), modo="continua")
    caso("valor-cuota-precio-sin-instrumento-se-descarta", ["FM-CER-05", "FM-CER-08"], entrada([1001]), seed=[rescate(1001)], colaboradores=L.precios(pr(1001, instrumento=None)))
    caso("valor-cuota-precio-repetido-lanza", ["FM-CER-05", "D-NOR-9"], entrada([1001]), seed=[rescate(1001)], colaboradores=L.precios(pr(1001), pr(1001)))
    caso("valor-cuota-precio-de-otro-registro-no-sirve", ["FM-CER-05", "FM-CER-08"], entrada([1001]), seed=[rescate(1001)], colaboradores=L.precios(pr(1002)))
    caso("valor-cuota-fecha-valor-distinta-de-certificacion", ["FM-CER-05", "FM-CER-08"], entrada([1001]),
         seed=[rescate(1001, FechaValorCuota="2026-02-27")], colaboradores=L.precios(pr(1001, fecha="2026-02-27 00:00:00.000")), modo="continua")
    caso("valor-cuota-uno-sin-precio-y-otro-correcto", ["FM-CER-08", "FM-CER-14"], entrada([1001, 1002]), seed=[rescate(1001), rescate(1002)],
         colaboradores=L.precios(pr(1002)), modo="continua")

    # ------------------------------------------------------------------ FM-CER-06 state
    caso("estado-certificado-rechaza", ["FM-CER-06", "FM-CER-14"], entrada([1001]), seed=[rescate(1001, estado="CRT")])
    for e in ("PEN", "ANU", "RZD", ""):
        caso("estado-%s-rechaza" % (e.lower() or "vacio"), ["FM-CER-06"], entrada([1001]), seed=[rescate(1001, estado=e)])
    caso("estado-minuscula-rechaza", ["FM-CER-06"], entrada([1001]), seed=[rescate(1001, estado="ing")])
    caso("estado-aceptado-pasa", ["FM-CER-06"], entrada([1001]), seed=[rescate(1001, estado="ACP")], modo="continua")
    caso("estado-ingresado-pasa", ["FM-CER-06"], entrada([1001]), seed=[rescate(1001, estado="ING")], modo="continua")

    # ------------------------------------------------------------------ FM-CER-07 certification date
    caso("fecha-certificacion-distinta-rechaza", ["FM-CER-07"], entrada([1001]), seed=[rescate(1001, FechaCertificacion="2026-03-03")])
    caso("fecha-certificacion-anterior-rechaza", ["FM-CER-07"], entrada([1001]), seed=[rescate(1001, FechaCertificacion="2026-03-01")])
    caso("fecha-certificacion-sin-fecha-rechaza", ["FM-CER-07"], entrada([1001]), seed=[rescate(1001, FechaCertificacion="0001-01-01")])
    caso("fecha-de-entrada-con-hora-no-coincide", ["FM-CER-07"], entrada([1001], fecha="2026-03-02 00:00:00"), seed=[rescate(1001)])
    caso("fecha-nula-rechaza-a-todas", ["FM-CER-07"], entrada([1001, 1002], p_FechaCertificacion=None), seed=[rescate(1001), aporte(1002)])
    caso("fecha-de-entrada-igual-a-la-de-la-solicitud-en-otro-dia", ["FM-CER-07", "FM-CER-18"], entrada([1001], fecha="2026-02-27"),
         seed=[rescate(1001, FechaCertificacion="2026-02-27")], colaboradores=L.precios(pr(1001)), modo="continua")

    # ------------------------------------------------------------------ FM-CER-09 omnibus
    caso("omnibus-no-creado-rechaza", ["FM-CER-09", "D-F3B-31"], entrada([1001]), seed=[rescate(1001, IdTipoMovimientoFondoMutuo="RESOMNI", IdOmnibus=None)])
    caso("omnibus-en-cero-no-rechaza", ["FM-CER-09"], entrada([1001]), seed=[rescate(1001, IdTipoMovimientoFondoMutuo="RESOMNI", IdOmnibus=0)], modo="continua")
    caso("omnibus-creado-no-rechaza", ["FM-CER-09"], entrada([1001]), seed=[rescate(1001, IdTipoMovimientoFondoMutuo="RESOMNI", IdOmnibus=55)], modo="continua")
    caso("omnibus-tipo-sin-omnibus-no-rechaza", ["FM-CER-09"], entrada([1001]), seed=[rescate(1001, IdOmnibus=None)], modo="continua")
    caso("omnibus-aporte-nulo-rechaza", ["FM-CER-09"], entrada([1001]), seed=[aporte(1001, IdTipoMovimientoFondoMutuo="APOOMNI", IdOmnibus=None)])
    caso("omnibus-switch-de-reinversion-no-rechaza", ["FM-CER-09", "FM-CER-63"], entrada([1001]),
         seed=[aporte(1001, IdTipoMovimientoFondoMutuo="APOREINV", Switch="S", IdOmnibus=None), "UPDATE \"%s\" SET \"GeneraOmnibus\" = 'S' WHERE \"IdTipoMovimientoFondoMutuo\" = 'APOREINV';" % L.TIPOMOV],
         modo="continua")
    caso("omnibus-reinversion-sin-switch-rechaza", ["FM-CER-09"], entrada([1001]),
         seed=[aporte(1001, IdTipoMovimientoFondoMutuo="APOREINV", Switch="N", IdOmnibus=None), "UPDATE \"%s\" SET \"GeneraOmnibus\" = 'S' WHERE \"IdTipoMovimientoFondoMutuo\" = 'APOREINV';" % L.TIPOMOV])

    # ------------------------------------------------------------------ FM-CER-10 treasury voucher not liquidated
    caso("comprobante-no-liquidado-rechaza", ["FM-CER-10"], entrada([1001]), seed=[rescate(1001), L.comprobante_no_liquidado(1001, 7001, "PEN")])
    caso("comprobante-liquidado-no-rechaza", ["FM-CER-10"], entrada([1001]), seed=[rescate(1001), L.comprobante_no_liquidado(1001, 7001, "LIQ")], modo="continua")
    caso("comprobante-de-otro-origen-no-rechaza", ["FM-CER-10"], entrada([1001]), seed=[rescate(1001), L.comprobante_no_liquidado(1001, 7001, "PEN", origen="OTR")], modo="continua")
    caso("comprobante-de-otra-solicitud-no-rechaza", ["FM-CER-10"], entrada([1001]), seed=[rescate(1001), L.comprobante_no_liquidado(1002, 7001, "PEN")], modo="continua")
    caso("comprobantes-dos-para-la-misma-solicitud-gana-el-primero", ["FM-CER-10"], entrada([1001]),
         seed=[rescate(1001), L.comprobante_no_liquidado(1001, 7001, "PEN"), L.comprobante_no_liquidado(1001, 7002, "ANU")])

    # ------------------------------------------------------------------ FM-CER-11 previous liquidation of the aporte
    sv = param("VALIDAR_CERTIFICACION", "S")
    caso("liquidacion-previa-falta-rechaza", ["FM-CER-11"], entrada([1001]), seed=[aporte(1001, EstadoLiquidacion="ING", FolioCartaLiquidacion=0), sv])
    caso("liquidacion-previa-con-folio-pasa", ["FM-CER-11"], entrada([1001]), seed=[aporte(1001, EstadoLiquidacion="ING", FolioCartaLiquidacion=5), sv], modo="continua")
    caso("liquidacion-previa-estado-LIQ-pasa", ["FM-CER-11"], entrada([1001]), seed=[aporte(1001, EstadoLiquidacion="LIQ", FolioCartaLiquidacion=0), sv], modo="continua")
    caso("liquidacion-previa-switch-pasa", ["FM-CER-11", "FM-CER-63"], entrada([1001]), seed=[aporte(1001, Switch="S", EstadoLiquidacion="ING", FolioCartaLiquidacion=0), sv], modo="continua")
    caso("liquidacion-previa-solo-aportes", ["FM-CER-11"], entrada([1001]), seed=[rescate(1001, EstadoLiquidacion="ING", FolioCartaLiquidacion=0), sv], modo="continua")
    caso("liquidacion-previa-parametro-N-no-valida", ["FM-CER-11"], entrada([1001]), seed=[aporte(1001, EstadoLiquidacion="ING", FolioCartaLiquidacion=0), param("VALIDAR_CERTIFICACION", "N")],
         modo="continua")
    caso("liquidacion-previa-parametro-minuscula-no-valida", ["FM-CER-11"], entrada([1001]), seed=[aporte(1001, EstadoLiquidacion="ING", FolioCartaLiquidacion=0), param("VALIDAR_CERTIFICACION", "s")],
         modo="continua")

    # ------------------------------------------------------------------ FM-CER-12 associated solicitud not certified
    def asociada(slug, ref, rel_estado, rel_liq, regimen="REGSA", id_=2002, rel=2001, mod="terminal", **c):
        seed = [rescate(id_, IdRegimenTributario=regimen, IdSolicitudAporteRescateRelacionado=rel, **c)]
        if rel_estado is not None:
            seed.append(solo_tabla(rel, "RES", rel_estado, EstadoLiquidacion=rel_liq))
        caso(slug, ref, entrada([id_]), seed=seed, modo=mod)
    asociada("asociada-no-certificada-rechaza", ["FM-CER-12"], "ING", "ING")
    asociada("asociada-certificada-y-liquidada-pasa", ["FM-CER-12"], "CRT", "LIQ", mod="continua")
    asociada("asociada-certificada-sin-liquidar-rechaza", ["FM-CER-12"], "CRT", "ING")
    asociada("asociada-liquidada-sin-certificar-rechaza", ["FM-CER-12"], "ING", "LIQ")
    asociada("asociada-con-id-menor-que-la-propia-pasa", ["FM-CER-12"], "ING", "ING", id_=2001, rel=2005, mod="continua")
    asociada("asociada-inexistente-en-la-tabla-rechaza", ["FM-CER-12"], None, None)
    asociada("asociada-regimen-que-certifica-sin-asociado-pasa", ["FM-CER-12"], "ING", "ING", regimen="REGSS", mod="continua")
    asociada("asociada-regimen-sin-configuracion-pasa", ["FM-CER-12"], "ING", "ING", regimen="GEN", mod="continua")
    asociada("asociada-regimen-inexistente-pasa", ["FM-CER-12"], "ING", "ING", regimen="NOEXISTE", mod="continua")
    asociada("asociada-switch-no-valida", ["FM-CER-12", "FM-CER-63"], "ING", "ING", mod="continua", Switch="S")
    caso("asociada-solicitud-sin-relacionado-pasa", ["FM-CER-12"], entrada([2002]), seed=[rescate(2002, IdRegimenTributario="REGSA", IdSolicitudAporteRescateRelacionado=0)], modo="continua")

    # ------------------------------------------------------------------ FM-CER-13 TGR / SII not configured
    caso("tgr-no-configurada-rechaza-BF", ["FM-CER-13"], entrada([1001]), seed=[rescate(1001, PropositoPortafolio="PROPOSITOAPV", IdRegimenTributario="BF"), config("APV.DocumentoIdentidadPersonaTGR", "99999990-0")])
    caso("tgr-configurada-pasa-BF", ["FM-CER-13"], entrada([1001]), seed=[rescate(1001, PropositoPortafolio="PROPOSITOAPV", IdRegimenTributario="BF")], modo="continua")
    caso("sii-no-configurada-rechaza-APV-B", ["FM-CER-13"], entrada([1001]), seed=[rescate(1001, PropositoPortafolio="PROPOSITOAPV", IdRegimenTributario="APV-B"),
                                                                                 config("APV.DocumentoIdentidadPersonaSII", "99999990-0")])
    caso("sii-no-configurada-no-valida-APV-AN", ["FM-CER-13", "D-F3B-08"], entrada([1001]), seed=[rescate(1001, PropositoPortafolio="PROPOSITOAPV", IdRegimenTributario="APV-AN"),
                                                                                                config("APV.DocumentoIdentidadPersonaSII", "99999990-0")], modo="continua")
    caso("sii-configurada-pasa-APV-B", ["FM-CER-13"], entrada([1001]), seed=[rescate(1001, PropositoPortafolio="PROPOSITOAPV", IdRegimenTributario="APV-B")], modo="continua")
    caso("tgr-y-sii-faltan-BF-gana-tgr", ["FM-CER-13"], entrada([1001]), seed=[rescate(1001, PropositoPortafolio="PROPOSITOAPV", IdRegimenTributario="BF"),
                                                                            config("APV.DocumentoIdentidadPersonaTGR", "99999990-0"), config("APV.DocumentoIdentidadPersonaSII", "99999990-0")])
    caso("tgr-falta-pero-la-solicitud-es-APV-B-pasa", ["FM-CER-13"], entrada([1001]), seed=[rescate(1001, PropositoPortafolio="PROPOSITOAPV", IdRegimenTributario="APV-B"),
                                                                                         config("APV.DocumentoIdentidadPersonaTGR", "99999990-0")], modo="continua")
    caso("regimen-apv-por-codigo-de-portafolio", ["FM-CER-13"], entrada([1001]), seed=[rescate(1001, PropositoPortafolio="PROPOSITOAPV", IdRegimenTributario="", CodigoPortafolio="APV-B"),
                                                                                    config("APV.DocumentoIdentidadPersonaSII", "99999990-0")])
    caso("proposito-no-apv-ignora-el-regimen", ["FM-CER-13"], entrada([1001]), seed=[rescate(1001, PropositoPortafolio="PROPGEN", IdRegimenTributario="BF"), config("APV.DocumentoIdentidadPersonaTGR", "99999990-0")],
         modo="continua")

    # ------------------------------------------------------------------ order of the rules (the first that fails wins)
    caso("orden-estado-gana-a-fecha", ["FM-CER-06", "FM-CER-07"], entrada([1001]), seed=[rescate(1001, estado="CRT", FechaCertificacion="2026-03-03")])
    caso("orden-fecha-gana-a-valor-cuota", ["FM-CER-07", "FM-CER-08"], entrada([1001]), seed=[rescate(1001, FechaCertificacion="2026-03-03")], colaboradores=L.precios())
    caso("orden-valor-cuota-gana-a-omnibus", ["FM-CER-08", "FM-CER-09"], entrada([1001]), seed=[rescate(1001, IdTipoMovimientoFondoMutuo="RESOMNI", IdOmnibus=None)], colaboradores=L.precios())
    caso("orden-omnibus-gana-a-comprobante", ["FM-CER-09", "FM-CER-10"], entrada([1001]), seed=[rescate(1001, IdTipoMovimientoFondoMutuo="RESOMNI", IdOmnibus=None), L.comprobante_no_liquidado(1001)])
    caso("orden-comprobante-gana-a-liquidacion", ["FM-CER-10", "FM-CER-11"], entrada([1001]), seed=[aporte(1001, FolioCartaLiquidacion=0), L.comprobante_no_liquidado(1001), sv])
    caso("orden-liquidacion-gana-a-asociada", ["FM-CER-11", "FM-CER-12"], entrada([2002]),
         seed=[aporte(2002, IdRegimenTributario="REGSA", IdSolicitudAporteRescateRelacionado=2001), solo_tabla(2001, "RES", "ING"), sv])
    caso("orden-asociada-gana-a-tgr", ["FM-CER-12", "FM-CER-13"], entrada([2002]),
         seed=[rescate(2002, IdRegimenTributario="REGSA", IdSolicitudAporteRescateRelacionado=2001, PropositoPortafolio="PROPOSITOAPV"), solo_tabla(2001, "RES", "ING"),
               config("APV.DocumentoIdentidadPersonaTGR", "99999990-0")])
    caso("cada-solicitud-con-su-codigo-en-la-salida-temprana", ["FM-CER-06", "FM-CER-07", "FM-CER-08", "FM-CER-14"], entrada([1001, 1002, 1003, 1004]),
         seed=[rescate(1001, estado="CRT"), rescate(1002, FechaCertificacion="2026-03-03"), rescate(1003), aporte(1004, IdTipoMovimientoFondoMutuo="APOOMNI", IdOmnibus=None)],
         colaboradores=L.precios(pr(1001), pr(1002), pr(1004)))

    # ------------------------------------------------------------------ FM-CER-14 count and early exit
    caso("salida-temprana-una-rechazada", ["FM-CER-14", "D-F3B-16"], entrada([1001]), seed=[_mal(1001)])
    caso("salida-temprana-simulacion-N-escribe-el-log-y-revierte", ["FM-CER-14", "D-F3B-01"], entrada([1001, 1002], sim="N"), seed=[_mal(1001), _mal(1002)])
    caso("salida-temprana-con-cifras-nulas-en-el-log", ["FM-CER-14", "FM-CER-66"], entrada([1001]),
         seed=[rescate(1001, estado="CRT", NumeroCuotas=None, ValorCuota=None, ParidadFechaCertificacion=0, Monto=Num("1234.56789012"))])
    caso("salida-temprana-estado-sin-fila-en-pla-estado-lanza", ["FM-CER-14", "FM-CER-67", "FM-CER-71"], entrada([1001]), seed=[_mal(1001), "DELETE FROM \"%s\" WHERE \"IdEstado\" = 'RZD';" % L.ESTADO])
    caso("salida-temprana-dos-veces-en-la-misma-sesion", ["FM-CER-14", "FM-CER-66"], entrada([1002]), seed=[_mal(1001), _mal(1002)], previas=[entrada([1001])])
    caso("salida-temprana-lote-de-cinco", ["FM-CER-14"], entrada([1005, 1003, 1001, 1004, 1002]), seed=[_mal(1001), _mal(1002), aporte(1003, estado="ANU"), aporte(1004, estado="PEN"), _mal(1005)])
    caso("lote-mixto-una-valida-y-una-rechazada", ["FM-CER-14", "FM-CER-69"], entrada([1001, 1002]), seed=[rescate(1001), _mal(1002)], modo="continua")
    caso("salida-temprana-con-descripcion-por-parametros", ["FM-CER-10", "FM-CER-12", "FM-CER-14", "FM-CER-67"], entrada([1001, 2002]),
         seed=[rescate(1001), L.comprobante_no_liquidado(1001, 7777, "PEN"), rescate(2002, IdRegimenTributario="REGSA", IdSolicitudAporteRescateRelacionado=2001), solo_tabla(2001, "RES", "ING")])
    caso("tipo-de-solicitud-ajeno-pasa-las-reglas", ["FM-CER-14", "FM-CER-19", "D-F3B-16"], entrada([1001]), seed=[sol_ajena(1001)], modo="continua")


def sol_ajena(id_):
    return L.sol(id_, "XXX")
