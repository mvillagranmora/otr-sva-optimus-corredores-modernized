# (Dominio, plantilla relativa a templates/<Dominio>/, filas de datos del informe principal)
# 2 por dominio (10 dominios con .rpt). Criterio: presentes en el listado HANA QA (CRYSTAL_REPO_ONLY_TEMPLATES.csv marca las
# "repo-only"), pequenas, con .sql al lado, sin subinforme salvo APV/CONTRATO_SAAPV (subinforme + imagen); Custodia y Orden
# aportan pdf417. Contabilidad no tiene ninguna plantilla en HANA QA (todas repo-only salvo ninguna): se eligen las mas pequenas.
CASES = [
    ("APV", "Templates/NOTIFICACIONCLIENTESAAPV.rpt", 1),
    ("APV", "Templates/CONTRATO_SAAPV.rpt", 1),
    ("Contabilidad", "Templates/INFORME_CARTERA_LEASING.rpt", 3),
    ("Contabilidad", "Templates/VencimientosAnticiposPactos.rpt", 3),
    ("CuentaInversion", "Templates/ComprobanteCuentaInversion.rpt", 1),
    ("CuentaInversion", "Templates/Anexo16CuentasPorCobrar.rpt", 3),
    ("Custodia", "Templates/Anexo2.rpt", 3),
    ("Custodia", "Templates/comprobante_garantia_ingreso_retiro.rpt", 1),
    ("Facturacion", "Templates/BoletaFacturacion.rpt", 1),
    ("Facturacion", "Templates/ComprobanteFacturacion.rpt", 1),
    ("FondoMutuo", "Templates/template_cartas_liquidacion.rpt", 1),
    ("FondoMutuo", "Templates/template_informe_liquidacion.rpt", 3),
    ("Mesa", "Templates/MRFVentaInstrumentos.rpt", 1),
    ("Mesa", "Templates/Articulo84.rpt", 3),
    ("Normativa", "Templates/UAF48PEPINFO.rpt", 3),
    ("Normativa", "Templates/Certificado30.rpt", 1),
    ("Orden", "Templates/COMPROBANTE_ORDEN.rpt", 1),
    ("Orden", "Templates/COMPROBANTE_ORDEN_CREDICORP_PERU.rpt", 1),
    ("Tesoreria", "Templates/CartaInstruccionPagoBcoChile.rpt", 1),
    ("Tesoreria", "Templates/DetalleComprobanteTesoreria.rpt", 3),
]
