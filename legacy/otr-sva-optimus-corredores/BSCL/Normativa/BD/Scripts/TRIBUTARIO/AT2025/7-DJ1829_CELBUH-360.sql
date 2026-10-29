SET SCHEMA BSCL;

UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='RUT', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='1. DATOS DE LOS CONTRATOS INFORMADOS-4'
WHERE "Esquema"='DJ1829' AND "Version"='2024' AND "Nombre"='RutContraparte';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='AMOUNT', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='1. DATOS DE LOS CONTRATOS INFORMADOS-4'
WHERE "Esquema"='DJ1829' AND "Version"='2024' AND "Nombre"='SaldoGarantiasCierre';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='TEXT', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='1. DATOS DE LOS CONTRATOS INFORMADOS-4'
WHERE "Esquema"='DJ1829' AND "Version"='2024' AND "Nombre"='PagosExteriorModalidadPago';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='AMOUNT', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='1. DATOS DE LOS CONTRATOS INFORMADOS-4'
WHERE "Esquema"='DJ1829' AND "Version"='2024' AND "Nombre"='PagosMontoPagadosAdeudados';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='AMOUNT', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='1. DATOS DE LOS CONTRATOS INFORMADOS-4'
WHERE "Esquema"='DJ1829' AND "Version"='2024' AND "Nombre"='OtrosIngresosAsociadosContrato';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='AMOUNT', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='1. DATOS DE LOS CONTRATOS INFORMADOS-4'
WHERE "Esquema"='DJ1829' AND "Version"='2024' AND "Nombre"='OtrosGastosAsociadosContrato';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='AMOUNT', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='1. DATOS DE LOS CONTRATOS INFORMADOS-4'
WHERE "Esquema"='DJ1829' AND "Version"='2024' AND "Nombre"='InversionInicial';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='AMOUNT', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='1. DATOS DE LOS CONTRATOS INFORMADOS-4'
WHERE "Esquema"='DJ1829' AND "Version"='2024' AND "Nombre"='PrimaTotal';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='AMOUNT', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='1. DATOS DE LOS CONTRATOS INFORMADOS-4'
WHERE "Esquema"='DJ1829' AND "Version"='2024' AND "Nombre"='ComisionPactada';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='TEXT', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='1. DATOS DE LOS CONTRATOS INFORMADOS-4'
WHERE "Esquema"='DJ1829' AND "Version"='2024' AND "Nombre"='CuentaContableAsociadaEfectoPatrimonio';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='AMOUNT', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='1. DATOS DE LOS CONTRATOS INFORMADOS-4'
WHERE "Esquema"='DJ1829' AND "Version"='2024' AND "Nombre"='EfectoPatrimonio';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='TEXT', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='1. DATOS DE LOS CONTRATOS INFORMADOS-4'
WHERE "Esquema"='DJ1829' AND "Version"='2024' AND "Nombre"='CuentaContableAsociadaResultadoEjercicio';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='AMOUNT', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='1. DATOS DE LOS CONTRATOS INFORMADOS-3'
WHERE "Esquema"='DJ1829' AND "Version"='2024' AND "Nombre"='ResultadoEjercicio';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='AMOUNT', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='1. DATOS DE LOS CONTRATOS INFORMADOS-3'
WHERE "Esquema"='DJ1829' AND "Version"='2024' AND "Nombre"='ValorJustoContratoConfirnacion';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='AMOUNT', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='1. DATOS DE LOS CONTRATOS INFORMADOS-3'
WHERE "Esquema"='DJ1829' AND "Version"='2024' AND "Nombre"='PrecioMercadoPrecio';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='TEXT', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='1. DATOS DE LOS CONTRATOS INFORMADOS-3'
WHERE "Esquema"='DJ1829' AND "Version"='2024' AND "Nombre"='PrecioMercadoCodigoPrecio';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='DATE', "Obligatorio"='S', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='1. DATOS DE LOS CONTRATOS INFORMADOS-3'
WHERE "Esquema"='DJ1829' AND "Version"='2024' AND "Nombre"='FechaLiquidacionEjercicioOpcion';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='DATE', "Obligatorio"='S', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='1. DATOS DE LOS CONTRATOS INFORMADOS-3'
WHERE "Esquema"='DJ1829' AND "Version"='2024' AND "Nombre"='FechaVencimiento';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='AMOUNT', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='1. DATOS DE LOS CONTRATOS INFORMADOS-3'
WHERE "Esquema"='DJ1829' AND "Version"='2024' AND "Nombre"='SegundoMontoNocional';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='TEXT', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='1. DATOS DE LOS CONTRATOS INFORMADOS-3'
WHERE "Esquema"='DJ1829' AND "Version"='2024' AND "Nombre"='SegundaUnidad';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='AMOUNT', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='1. DATOS DE LOS CONTRATOS INFORMADOS-3'
WHERE "Esquema"='DJ1829' AND "Version"='2024' AND "Nombre"='MontoCantidadContratadoNocional';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='TEXT', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='1. DATOS DE LOS CONTRATOS INFORMADOS-3'
WHERE "Esquema"='DJ1829' AND "Version"='2024' AND "Nombre"='Unidad';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='TEXT', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='1. DATOS DE LOS CONTRATOS INFORMADOS-3'
WHERE "Esquema"='DJ1829' AND "Version"='2024' AND "Nombre"='PrecioFuturoMoneda';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='QTY', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='1. DATOS DE LOS CONTRATOS INFORMADOS-2'
WHERE "Esquema"='DJ1829' AND "Version"='2024' AND "Nombre"='PrecioFuturoPrecio';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='TEXT', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='1. DATOS DE LOS CONTRATOS INFORMADOS-2'
WHERE "Esquema"='DJ1829' AND "Version"='2024' AND "Nombre"='PrecioFuturoCodigoPrecio';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='QTY', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='1. DATOS DE LOS CONTRATOS INFORMADOS-2'
WHERE "Esquema"='DJ1829' AND "Version"='2024' AND "Nombre"='SegundoActivoSubyacenteTasaFija';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='TEXT', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='1. DATOS DE LOS CONTRATOS INFORMADOS-2'
WHERE "Esquema"='DJ1829' AND "Version"='2024' AND "Nombre"='SegundoActivoSubyacenteOtros';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='TEXT', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='1. DATOS DE LOS CONTRATOS INFORMADOS-2'
WHERE "Esquema"='DJ1829' AND "Version"='2024' AND "Nombre"='SegundoActivoSubyacenteCodigo';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='TEXT', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='1. DATOS DE LOS CONTRATOS INFORMADOS-2'
WHERE "Esquema"='DJ1829' AND "Version"='2024' AND "Nombre"='SegundoActivoSubyacenteTipo';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='QTY', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='1. DATOS DE LOS CONTRATOS INFORMADOS-2'
WHERE "Esquema"='DJ1829' AND "Version"='2024' AND "Nombre"='ActivoSubyacenteTasaFija';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='TEXT', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='1. DATOS DE LOS CONTRATOS INFORMADOS-2'
WHERE "Esquema"='DJ1829' AND "Version"='2024' AND "Nombre"='ActivoSubyacenteOtros';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='TEXT', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='1. DATOS DE LOS CONTRATOS INFORMADOS-2'
WHERE "Esquema"='DJ1829' AND "Version"='2024' AND "Nombre"='ActivoSubyacenteCodigo';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='TEXT', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='1. DATOS DE LOS CONTRATOS INFORMADOS-2'
WHERE "Esquema"='DJ1829' AND "Version"='2024' AND "Nombre"='ActivoSubyacenteTipo';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='TEXT', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='1. DATOS DE LOS CONTRATOS INFORMADOS-2'
WHERE "Esquema"='DJ1829' AND "Version"='2024' AND "Nombre"='ContratoPosicionDeclarante';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='TEXT', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='1. DATOS DE LOS CONTRATOS INFORMADOS-1'
WHERE "Esquema"='DJ1829' AND "Version"='2024' AND "Nombre"='ContratoModalidadCumplimiento';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='TEXT', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='1. DATOS DE LOS CONTRATOS INFORMADOS-1'
WHERE "Esquema"='DJ1829' AND "Version"='2024' AND "Nombre"='ContratoNombreInstrumento';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='TEXT', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='1. DATOS DE LOS CONTRATOS INFORMADOS-1'
WHERE "Esquema"='DJ1829' AND "Version"='2024' AND "Nombre"='ContratoTipo';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='TEXT', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='1. DATOS DE LOS CONTRATOS INFORMADOS-1'
WHERE "Esquema"='DJ1829' AND "Version"='2024' AND "Nombre"='ContratoEstadoConfirmacion';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='TEXT', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='1. DATOS DE LOS CONTRATOS INFORMADOS-1'
WHERE "Esquema"='DJ1829' AND "Version"='2024' AND "Nombre"='ContratoVencido';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='DATE', "Obligatorio"='S', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='1. DATOS DE LOS CONTRATOS INFORMADOS-1'
WHERE "Esquema"='DJ1829' AND "Version"='2024' AND "Nombre"='ContratoFechaSuscripcion';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='DECIMAL', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='1. DATOS DE LOS CONTRATOS INFORMADOS-1'
WHERE "Esquema"='DJ1829' AND "Version"='2024' AND "Nombre"='ContratoNumero';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='DATE', "Obligatorio"='S', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='1. DATOS DE LOS CONTRATOS INFORMADOS-1'
WHERE "Esquema"='DJ1829' AND "Version"='2024' AND "Nombre"='AcuerdoMarcoFechaSuscripcion';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='DECIMAL', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='1. DATOS DE LOS CONTRATOS INFORMADOS-1'
WHERE "Esquema"='DJ1829' AND "Version"='2024' AND "Nombre"='AcuerdoMarcoNumero';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='TEXT', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='1. DATOS DE LOS CONTRATOS INFORMADOS-1'
WHERE "Esquema"='DJ1829' AND "Version"='2024' AND "Nombre"='AcuerdoMarcoTipo';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='NEMO', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='1. DATOS DE LOS CONTRATOS INFORMADOS-1'
WHERE "Esquema"='DJ1829' AND "Version"='2024' AND "Nombre"='ModalidadContratacion';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='NEMO', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='1. DATOS DE LOS CONTRATOS INFORMADOS-1'
WHERE "Esquema"='DJ1829' AND "Version"='2024' AND "Nombre"='TipoRelacionContraparte';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='NEMO', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='1. DATOS DE LOS CONTRATOS INFORMADOS-1'
WHERE "Esquema"='DJ1829' AND "Version"='2024' AND "Nombre"='CodigoPaisContraparte';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='RUT', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='1. DATOS DE LOS CONTRATOS INFORMADOS-1'
WHERE "Esquema"='DJ1829' AND "Version"='2024' AND "Nombre"='TaxIDContraparte';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='TEXT', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='S', "Grupo"='2. DATOS DE CONTROL'
WHERE "Esquema"='DJ1829' AND "Version"='2024' AND "Nombre"='Observaciones';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='TEXT', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='S', "Grupo"='2. DATOS DE CONTROL'
WHERE "Esquema"='DJ1829' AND "Version"='2024' AND "Nombre"='VboUsuario';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='TEXT', "Obligatorio"='N', "Editable"='N', "OcultoEnMantenedor"='S', "Grupo"='2. DATOS DE CONTROL'
WHERE "Esquema"='DJ1829' AND "Version"='2024' AND "Nombre"='FuenteDatos';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='ID', "Obligatorio"='N', "Editable"='N', "OcultoEnMantenedor"='S', "Grupo"='2. DATOS DE CONTROL'
WHERE "Esquema"='DJ1829' AND "Version"='2024' AND "Nombre"='IdDocumento';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='ID', "Obligatorio"='N', "Editable"='N', "OcultoEnMantenedor"='S', "Grupo"=''
WHERE "Esquema"='DJ1829' AND "Version"='2024' AND "Nombre"='ID';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='ID_CLIENTE', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='S', "Grupo"='1. DATOS DE LOS CONTRATOS INFORMADOS-1'
WHERE "Esquema"='DJ1829' AND "Version"='2024' AND "Nombre"='IdCliente';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='ID_PORTAFOLIO', "Obligatorio"='N', "Editable"='N', "OcultoEnMantenedor"='S', "Grupo"='1. DATOS DE LOS CONTRATOS INFORMADOS-1'
WHERE "Esquema"='DJ1829' AND "Version"='2024' AND "Nombre"='IdPortafolio';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='ID', "Obligatorio"='N', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='1. DATOS DE LOS CONTRATOS INFORMADOS-2'
WHERE "Esquema"='DJ1829' AND "Version"='2024' AND "Nombre"='Numero';
UPDATE "BSCL.Normativa.BD::DOC.Campo"
SET  "TipoDato"='DATE_FILTER', "Obligatorio"='S', "Editable"='S', "OcultoEnMantenedor"='N', "Grupo"='1. DATOS DE LOS CONTRATOS INFORMADOS-1'
WHERE "Esquema"='DJ1829' AND "Version"='2024' AND "Nombre"='FechaContrato';

COMMIT;