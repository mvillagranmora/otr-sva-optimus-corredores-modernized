package cl.bcs.plataforma.adapter.in.rest;

import org.springframework.web.bind.annotation.*;

/**
 * Stub controller for commercial (COM) operations.
 * Legacy package: cl.bcs.plataforma.com (101 classes)
 *
 * Covers:
 *   - CalcularCobros, ConsultarCobrosPlantilla, ConsultarCobrosPortafolio
 *   - ConsultarCobrosCorredorCorresponsal, ConsultarCobrosPlantillaPeru
 *   - ConsultarMatrizOperacional, ConsultarMatrizOperacionalExcel
 *   - ConsultarMatrizOperacionalPortafolios, ConsultarMatrizOperacionalPortafolioExcel
 *   - ConsultarProductosPlantilla, ConsultarProductosPortafolio
 *   - CrearConfiguracionPerfilComercialAdicional
 *   - CrearContratoComision, CrearDetalleContratoComision
 *   - CrearGrupoActivos, CrearGrupoActivoDetalle
 *   - ConfirmarCrearPerfilCobros, ConfirmarCrearPerfilCobrosDetalle
 *   - ConfirmarCrearPerfilProducto, ConfirmarCrearPerfilProductoDetalle
 *   - ConfirmarModificarPerfilCobros, ConfirmarModificarPerfilCobrosDetalle
 *   - ConfirmarModificarPerfilProducto, ConfirmarModificarPerfilProductoDetalle
 *   - ConfirmarModificarGrupoActivo, ConfirmarModificarGrupoActivoDetalle
 *   - ConfirmarEliminarConfiguracionPerfilComercialAdicional
 *   - ConfirmarModificarConfiguracionPerfilComercialAdicional
 *   - AutorizacionEliminarConfiguracionPerfilComercialAdicional
 *   - AutorizacionModificarConfiguracionPerfilComercialAdicional
 *   - AutorizarCrearPerfilProductoMasivo, AutorizarCrearPerfilProductoDetalleMasivo
 *   - ConfirmarCrearPerfilCobrosDetalleMasivo, ConfirmarCrearPerfilProductoMasivo
 *   - ConfirmarModificarPerfilCobrosDetalleMasivo
 *
 * TODO: wire when PLA.Servicio DDL + SELECT * export is provided.
 */
@RestController
@RequestMapping("/api/plataforma/comercial")
public class ComercialController {

    @PostMapping("/cobros/calcular")
    public Object calcularCobros(@RequestBody Object request) {
        throw new UnsupportedOperationException("requires HANA — COM.Cobros not yet wired");
    }

    @PostMapping("/cobros/consultar-plantilla")
    public Object consultarCobrosPlantilla(@RequestBody Object request) {
        throw new UnsupportedOperationException("requires HANA — COM.CobrosPlantilla not yet wired");
    }

    @PostMapping("/cobros/consultar-portafolio")
    public Object consultarCobrosPortafolio(@RequestBody Object request) {
        throw new UnsupportedOperationException("requires HANA — COM.CobrosPortafolio not yet wired");
    }

    @PostMapping("/cobros/consultar-corresponsal")
    public Object consultarCobrosCorredorCorresponsal(@RequestBody Object request) {
        throw new UnsupportedOperationException("requires HANA — COM.CobrosCorresponsal not yet wired");
    }

    @PostMapping("/matriz-operacional/consultar")
    public Object consultarMatrizOperacional(@RequestBody Object request) {
        throw new UnsupportedOperationException("requires HANA — COM.MatrizOperacional not yet wired");
    }

    @PostMapping("/matriz-operacional/exportar-excel")
    public Object consultarMatrizOperacionalExcel(@RequestBody Object request) {
        throw new UnsupportedOperationException("requires HANA — COM.MatrizOperacional not yet wired");
    }

    @PostMapping("/productos/consultar-plantilla")
    public Object consultarProductosPlantilla(@RequestBody Object request) {
        throw new UnsupportedOperationException("requires HANA — COM.Productos not yet wired");
    }

    @PostMapping("/productos/consultar-portafolio")
    public Object consultarProductosPortafolio(@RequestBody Object request) {
        throw new UnsupportedOperationException("requires HANA — COM.Productos not yet wired");
    }

    @PostMapping("/contrato-comision/crear")
    public Object crearContratoComision(@RequestBody Object request) {
        throw new UnsupportedOperationException("requires HANA — COM.ContratoComision not yet wired");
    }

    @PostMapping("/contrato-comision/crear-detalle")
    public Object crearDetalleContratoComision(@RequestBody Object request) {
        throw new UnsupportedOperationException("requires HANA — COM.ContratoComision not yet wired");
    }

    @PostMapping("/grupo-activos/crear")
    public Object crearGrupoActivos(@RequestBody Object request) {
        throw new UnsupportedOperationException("requires HANA — COM.GrupoActivos not yet wired");
    }

    @PostMapping("/grupo-activos/crear-detalle")
    public Object crearGrupoActivoDetalle(@RequestBody Object request) {
        throw new UnsupportedOperationException("requires HANA — COM.GrupoActivos not yet wired");
    }

    @PostMapping("/perfil-cobros/crear")
    public Object confirmarCrearPerfilCobros(@RequestBody Object request) {
        throw new UnsupportedOperationException("requires HANA — COM.PerfilCobros not yet wired");
    }

    @PostMapping("/perfil-cobros/modificar")
    public Object confirmarModificarPerfilCobros(@RequestBody Object request) {
        throw new UnsupportedOperationException("requires HANA — COM.PerfilCobros not yet wired");
    }

    @PostMapping("/perfil-producto/crear")
    public Object confirmarCrearPerfilProducto(@RequestBody Object request) {
        throw new UnsupportedOperationException("requires HANA — COM.PerfilProducto not yet wired");
    }

    @PostMapping("/perfil-producto/modificar")
    public Object confirmarModificarPerfilProducto(@RequestBody Object request) {
        throw new UnsupportedOperationException("requires HANA — COM.PerfilProducto not yet wired");
    }

    @PostMapping("/perfil-comercial-adicional/crear")
    public Object crearConfiguracionPerfilComercialAdicional(@RequestBody Object request) {
        throw new UnsupportedOperationException("requires HANA — COM.PerfilComercialAdicional not yet wired");
    }

    @PostMapping("/perfil-comercial-adicional/eliminar")
    public Object confirmarEliminarConfiguracionPerfilComercialAdicional(@RequestBody Object request) {
        throw new UnsupportedOperationException("requires HANA — COM.PerfilComercialAdicional not yet wired");
    }

    @PostMapping("/perfil-comercial-adicional/modificar")
    public Object confirmarModificarConfiguracionPerfilComercialAdicional(@RequestBody Object request) {
        throw new UnsupportedOperationException("requires HANA — COM.PerfilComercialAdicional not yet wired");
    }
}
