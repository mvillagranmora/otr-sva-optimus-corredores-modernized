SET SCHEMA "BSCL"; 
--DICCIONARIO
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" ) 
SELECT 'UI_SELECCION_CLIENTES_ISFUT', 'CL', 'Seleccion Clientes ISFUT', 'SELCLIISFUT'
FROM DUMMY
WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.Diccionario" WHERE "CodDiccionario"='UI_SELECCION_CLIENTES_ISFUT');
-------------------------------------------------------------------------------------------------------------------------------
--CONFIGURACION ARCHIVO DE SUBIDA
INSERT INTO "BSCL.Plataforma.BD::PLA.Archivo" ("IdArchivo", "IdEmpresa", "CodigoArchivo", "TieneSeparador", "SeparadorColumnas", "TieneCabecera", "IdModulo", "CodigoArchivoDetalle", "CodigoArchivoPie") 
SELECT 160, 1, 'CARGA_CLIENTES_ISFUT', 'S', ';', 'N', 'CUS', '', ''
FROM DUMMY
WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.Archivo" WHERE "IdArchivo"=160 AND "CodigoArchivo" = 'CARGA_CLIENTES_ISFUT');
-------------------------------------------------------------------------------------------------------------------------------
--SERVICIO UTILIZADO
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion", "TablasTempIN", "TablasTempOUT", "RegistraUso")
SELECT 800100,'PLUGIN_TES.jar@cl.bcs.custodia.CrearCargaMasivaISFUT',0,'J','Archivo Carga Masiva ISFUT','','', 'S'
FROM DUMMY
WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.Servicio" WHERE "IdServicio" = 800100);

-------------------------------------------------------------------------------------------------------------------------------
--CAMPOS GRILLA CARGA MASIVA
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato" ("IdFormato", "CodigoCampo", "TipoDato", "Posicion", "LargoCampo", "EsObligatorio", "Validacion", "MensajeValidacion", "IncluyeDecimales", "CantidadDecimales", "ForzarMayuscula") SELECT 3400, 'IGKIdEventoCapitalCliente', '', 1, 6, 'S', '', '', 'N', 0, 'S'FROM DUMMY WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.Formato" WHERE "CodigoCampo" = 'IGKIdEventoCapitalCliente');
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato" ("IdFormato", "CodigoCampo", "TipoDato", "Posicion", "LargoCampo", "EsObligatorio", "Validacion", "MensajeValidacion", "IncluyeDecimales", "CantidadDecimales", "ForzarMayuscula") SELECT 3401, 'IGKIdTipoEventoCapital', '', 2, 3, 'N', '', '', 'N', 0, 'S' FROM DUMMY WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.Formato" WHERE "CodigoCampo" = 'IGKIdTipoEventoCapital');
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato" ("IdFormato", "CodigoCampo", "TipoDato", "Posicion", "LargoCampo", "EsObligatorio", "Validacion", "MensajeValidacion", "IncluyeDecimales", "CantidadDecimales", "ForzarMayuscula") SELECT 3402, 'IGKNumeroDividendo', '', 3, 15, 'N', '', '', 'N', 0, 'S' FROM DUMMY WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.Formato" WHERE "CodigoCampo" = 'IGKNumeroDividendo');
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato" ("IdFormato", "CodigoCampo", "TipoDato", "Posicion", "LargoCampo", "EsObligatorio", "Validacion", "MensajeValidacion", "IncluyeDecimales", "CantidadDecimales", "ForzarMayuscula") SELECT 3403, 'IGKSecuencia', '', 4, 15, 'N', '', '', 'N', 0, 'S' FROM DUMMY WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.Formato" WHERE "CodigoCampo" = 'IGKSecuencia');
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato" ("IdFormato", "CodigoCampo", "TipoDato", "Posicion", "LargoCampo", "EsObligatorio", "Validacion", "MensajeValidacion", "IncluyeDecimales", "CantidadDecimales", "ForzarMayuscula") SELECT 3404, 'IGKTipoSocioNegocio', '', 5, 3, 'N', '', '', 'N', 0, 'S' FROM DUMMY WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.Formato" WHERE "CodigoCampo" = 'IGKTipoSocioNegocio');
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato" ("IdFormato", "CodigoCampo", "TipoDato", "Posicion", "LargoCampo", "EsObligatorio", "Validacion", "MensajeValidacion", "IncluyeDecimales", "CantidadDecimales", "ForzarMayuscula") SELECT 3405, 'IGKCodigoPortafolio', '', 6, 100, 'N', '', '', 'N', 0, 'S' FROM DUMMY WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.Formato" WHERE "CodigoCampo" = 'IGKCodigoPortafolio');
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato" ("IdFormato", "CodigoCampo", "TipoDato", "Posicion", "LargoCampo", "EsObligatorio", "Validacion", "MensajeValidacion", "IncluyeDecimales", "CantidadDecimales", "ForzarMayuscula") SELECT 3406, 'IGKIdClasificacionTipoInstrumento', '', 7, 3, 'N', '', '', 'N', 0, 'S' FROM DUMMY WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.Formato" WHERE "CodigoCampo" = 'IGKIdClasificacionTipoInstrumento');
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato" ("IdFormato", "CodigoCampo", "TipoDato", "Posicion", "LargoCampo", "EsObligatorio", "Validacion", "MensajeValidacion", "IncluyeDecimales", "CantidadDecimales", "ForzarMayuscula") SELECT 3407, 'IGKNemo', '', 8, 40, 'N', '', '', 'N', 0, 'S' FROM DUMMY WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.Formato" WHERE "CodigoCampo" = 'IGKNemo');
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato" ("IdFormato", "CodigoCampo", "TipoDato", "Posicion", "LargoCampo", "EsObligatorio", "Validacion", "MensajeValidacion", "IncluyeDecimales", "CantidadDecimales", "ForzarMayuscula") SELECT 3408, 'IGKISIN', '', 9, 40, 'N', '', '', 'N', 0, 'S' FROM DUMMY WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.Formato" WHERE "CodigoCampo" = 'IGKISIN');
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato" ("IdFormato", "CodigoCampo", "TipoDato", "Posicion", "LargoCampo", "EsObligatorio", "Validacion", "MensajeValidacion", "IncluyeDecimales", "CantidadDecimales", "ForzarMayuscula") SELECT 3409, 'IGKNemoNuevo', '', 10, 40, 'N', '', '', 'N', 0, 'S' FROM DUMMY WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.Formato" WHERE "CodigoCampo" = 'IGKNemoNuevo');
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato" ("IdFormato", "CodigoCampo", "TipoDato", "Posicion", "LargoCampo", "EsObligatorio", "Validacion", "MensajeValidacion", "IncluyeDecimales", "CantidadDecimales", "ForzarMayuscula") SELECT 3410, 'IGKIdConceptoGarantia', '', 11, 6, 'N', '', '', 'N', 0, 'S' FROM DUMMY WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.Formato" WHERE "CodigoCampo" = 'IGKIdConceptoGarantia');
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato" ("IdFormato", "CodigoCampo", "TipoDato", "Posicion", "LargoCampo", "EsObligatorio", "Validacion", "MensajeValidacion", "IncluyeDecimales", "CantidadDecimales", "ForzarMayuscula") SELECT 3411, 'IGKTipoCartera', '', 12, 3, 'N', '', '', 'N', 0, 'S' FROM DUMMY WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.Formato" WHERE "CodigoCampo" = 'IGKTipoCartera');
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato" ("IdFormato", "CodigoCampo", "TipoDato", "Posicion", "LargoCampo", "EsObligatorio", "Validacion", "MensajeValidacion", "IncluyeDecimales", "CantidadDecimales", "ForzarMayuscula") SELECT 3412, 'IGKDocumentoIdentidad', '', 13, 50, 'N', '', '', 'N', 0, 'S' FROM DUMMY WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.Formato" WHERE "CodigoCampo" = 'IGKDocumentoIdentidad');
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato" ("IdFormato", "CodigoCampo", "TipoDato", "Posicion", "LargoCampo", "EsObligatorio", "Validacion", "MensajeValidacion", "IncluyeDecimales", "CantidadDecimales", "ForzarMayuscula") SELECT 3413, 'IGKNombreCliente', '', 14, 302, 'N', '', '', 'N', 0, 'S' FROM DUMMY WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.Formato" WHERE "CodigoCampo" = 'IGKNombreCliente');
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato" ("IdFormato", "CodigoCampo", "TipoDato", "Posicion", "LargoCampo", "EsObligatorio", "Validacion", "MensajeValidacion", "IncluyeDecimales", "CantidadDecimales", "ForzarMayuscula") SELECT 3414, 'IGKConfiguracionNotificacion', '', 15, 1, 'N', '', '', 'N', 0, 'S' FROM DUMMY WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.Formato" WHERE "CodigoCampo" = 'IGKConfiguracionNotificacion');
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato" ("IdFormato", "CodigoCampo", "TipoDato", "Posicion", "LargoCampo", "EsObligatorio", "Validacion", "MensajeValidacion", "IncluyeDecimales", "CantidadDecimales", "ForzarMayuscula") SELECT 3415, 'IGKTienePreferenciaDePago', '', 16, 1, 'N', '', '', 'N', 0, 'S' FROM DUMMY WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.Formato" WHERE "CodigoCampo" = 'IGKTienePreferenciaDePago');
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato" ("IdFormato", "CodigoCampo", "TipoDato", "Posicion", "LargoCampo", "EsObligatorio", "Validacion", "MensajeValidacion", "IncluyeDecimales", "CantidadDecimales", "ForzarMayuscula") SELECT 3416, 'IGKMontoCapital', '', 17, 40, 'N', '', '', 'S', 8, 'S' FROM DUMMY WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.Formato" WHERE "CodigoCampo" = 'IGKMontoCapital');
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato" ("IdFormato", "CodigoCampo", "TipoDato", "Posicion", "LargoCampo", "EsObligatorio", "Validacion", "MensajeValidacion", "IncluyeDecimales", "CantidadDecimales", "ForzarMayuscula") SELECT 3417, 'IGKPrecio', '', 18, 40, 'N', '', '', 'S', 8, 'S' FROM DUMMY WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.Formato" WHERE "CodigoCampo" = 'IGKPrecio');
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato" ("IdFormato", "CodigoCampo", "TipoDato", "Posicion", "LargoCampo", "EsObligatorio", "Validacion", "MensajeValidacion", "IncluyeDecimales", "CantidadDecimales", "ForzarMayuscula") SELECT 3418, 'IGKFactor', '', 19, 40, 'N', '', '', 'S', 8, 'S' FROM DUMMY WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.Formato" WHERE "CodigoCampo" = 'IGKFactor');
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato" ("IdFormato", "CodigoCampo", "TipoDato", "Posicion", "LargoCampo", "EsObligatorio", "Validacion", "MensajeValidacion", "IncluyeDecimales", "CantidadDecimales", "ForzarMayuscula") SELECT 3419, 'IGKFactorNuevo', '', 20, 40, 'N', '', '', 'S', 8, 'S' FROM DUMMY WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.Formato" WHERE "CodigoCampo" = 'IGKFactorNuevo');
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato" ("IdFormato", "CodigoCampo", "TipoDato", "Posicion", "LargoCampo", "EsObligatorio", "Validacion", "MensajeValidacion", "IncluyeDecimales", "CantidadDecimales", "ForzarMayuscula") SELECT 3420, 'IGKPrecioNuevo', '', 21, 40, 'N', '', '', 'S', 8, 'S' FROM DUMMY WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.Formato" WHERE "CodigoCampo" = 'IGKPrecioNuevo');
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato" ("IdFormato", "CodigoCampo", "TipoDato", "Posicion", "LargoCampo", "EsObligatorio", "Validacion", "MensajeValidacion", "IncluyeDecimales", "CantidadDecimales", "ForzarMayuscula") SELECT 3421, 'IGKCantidadNueva', '', 22, 40, 'N', '', '', 'S', 8, 'S' FROM DUMMY WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.Formato" WHERE "CodigoCampo" = 'IGKCantidadNueva');
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato" ("IdFormato", "CodigoCampo", "TipoDato", "Posicion", "LargoCampo", "EsObligatorio", "Validacion", "MensajeValidacion", "IncluyeDecimales", "CantidadDecimales", "ForzarMayuscula") SELECT 3422, 'IGKMontoInteres', '', 23, 40, 'N', '', '', 'S', 8, 'S' FROM DUMMY WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.Formato" WHERE "CodigoCampo" = 'IGKMontoInteres');
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato" ("IdFormato", "CodigoCampo", "TipoDato", "Posicion", "LargoCampo", "EsObligatorio", "Validacion", "MensajeValidacion", "IncluyeDecimales", "CantidadDecimales", "ForzarMayuscula") SELECT 3423, 'IGKMontoReajuste', '', 24, 40, 'N', '', '', 'S', 8, 'S' FROM DUMMY WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.Formato" WHERE "CodigoCampo" = 'IGKMontoReajuste');
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato" ("IdFormato", "CodigoCampo", "TipoDato", "Posicion", "LargoCampo", "EsObligatorio", "Validacion", "MensajeValidacion", "IncluyeDecimales", "CantidadDecimales", "ForzarMayuscula") SELECT 3424, 'IGKCantidadDerecho', '', 25, 40, 'N', '', '', 'S', 8, 'S' FROM DUMMY WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.Formato" WHERE "CodigoCampo" = 'IGKCantidadDerecho');
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato" ("IdFormato", "CodigoCampo", "TipoDato", "Posicion", "LargoCampo", "EsObligatorio", "Validacion", "MensajeValidacion", "IncluyeDecimales", "CantidadDecimales", "ForzarMayuscula") SELECT 3425, 'IGKTotal', '', 26, 40, 'N', '', '', 'S', 8, 'S' FROM DUMMY WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.Formato" WHERE "CodigoCampo" = 'IGKTotal');
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato" ("IdFormato", "CodigoCampo", "TipoDato", "Posicion", "LargoCampo", "EsObligatorio", "Validacion", "MensajeValidacion", "IncluyeDecimales", "CantidadDecimales", "ForzarMayuscula") SELECT 3426, 'IGKMontoRetencion', '', 27, 40, 'N', '', '', 'S', 10, 'S' FROM DUMMY WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.Formato" WHERE "CodigoCampo" = 'IGKMontoRetencion');
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato" ("IdFormato", "CodigoCampo", "TipoDato", "Posicion", "LargoCampo", "EsObligatorio", "Validacion", "MensajeValidacion", "IncluyeDecimales", "CantidadDecimales", "ForzarMayuscula") SELECT 3427, 'IGKCantidadDerechoArchivo', '', 28, 40, 'N', '', '', 'N', 0, 'S' FROM DUMMY WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.Formato" WHERE "CodigoCampo" = 'IGKCantidadDerechoArchivo');
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato" ("IdFormato", "CodigoCampo", "TipoDato", "Posicion", "LargoCampo", "EsObligatorio", "Validacion", "MensajeValidacion", "IncluyeDecimales", "CantidadDecimales", "ForzarMayuscula") SELECT 3428, 'IGKAccionesDisponiblesArchivo', '', 29, 40, 'N', '', '', 'N', 0, 'S' FROM DUMMY WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.Formato" WHERE "CodigoCampo" = 'IGKAccionesDisponiblesArchivo');
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato" ("IdFormato", "CodigoCampo", "TipoDato", "Posicion", "LargoCampo", "EsObligatorio", "Validacion", "MensajeValidacion", "IncluyeDecimales", "CantidadDecimales", "ForzarMayuscula") SELECT 3429, 'IGKDiferenciaCantidadDerecho', '', 30, 40, 'N', '', '', 'N', 0, 'S' FROM DUMMY WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.Formato" WHERE "CodigoCampo" = 'IGKDiferenciaCantidadDerecho');
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato" ("IdFormato", "CodigoCampo", "TipoDato", "Posicion", "LargoCampo", "EsObligatorio", "Validacion", "MensajeValidacion", "IncluyeDecimales", "CantidadDecimales", "ForzarMayuscula") SELECT 3430, 'IGKDiferenciaAccionesCantidadNueva', '', 31, 40, 'N', '', '', 'N', 0, 'S' FROM DUMMY WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.Formato" WHERE "CodigoCampo" = 'IGKDiferenciaAccionesCantidadNueva');
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato" ("IdFormato", "CodigoCampo", "TipoDato", "Posicion", "LargoCampo", "EsObligatorio", "Validacion", "MensajeValidacion", "IncluyeDecimales", "CantidadDecimales", "ForzarMayuscula") SELECT 3431, 'IGKMontoBeneficioArchivo', '', 32, 40, 'N', '', '', 'N', 0, 'S' FROM DUMMY WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.Formato" WHERE "CodigoCampo" = 'IGKMontoBeneficioArchivo');
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato" ("IdFormato", "CodigoCampo", "TipoDato", "Posicion", "LargoCampo", "EsObligatorio", "Validacion", "MensajeValidacion", "IncluyeDecimales", "CantidadDecimales", "ForzarMayuscula") SELECT 3432, 'IGKDiferenciaMontoBeneficio', '', 33, 40, 'N', '', '', 'N', 0, 'S' FROM DUMMY WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.Formato" WHERE "CodigoCampo" = 'IGKDiferenciaMontoBeneficio');
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato" ("IdFormato", "CodigoCampo", "TipoDato", "Posicion", "LargoCampo", "EsObligatorio", "Validacion", "MensajeValidacion", "IncluyeDecimales", "CantidadDecimales", "ForzarMayuscula") SELECT 3433, 'IGKMontoInteresArchivo', '', 34, 40, 'N', '', '', 'N', 0, 'S' FROM DUMMY WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.Formato" WHERE "CodigoCampo" = 'IGKMontoInteresArchivo');
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato" ("IdFormato", "CodigoCampo", "TipoDato", "Posicion", "LargoCampo", "EsObligatorio", "Validacion", "MensajeValidacion", "IncluyeDecimales", "CantidadDecimales", "ForzarMayuscula") SELECT 3434, 'IGKMontoCapitalArchivo', '', 35, 40, 'N', '', '', 'N', 0, 'S' FROM DUMMY WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.Formato" WHERE "CodigoCampo" = 'IGKMontoCapitalArchivo');
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato" ("IdFormato", "CodigoCampo", "TipoDato", "Posicion", "LargoCampo", "EsObligatorio", "Validacion", "MensajeValidacion", "IncluyeDecimales", "CantidadDecimales", "ForzarMayuscula") SELECT 3435, 'IGKDiferenciaMontoInteres', '', 36, 40, 'N', '', '', 'N', 0, 'S' FROM DUMMY WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.Formato" WHERE "CodigoCampo" = 'IGKDiferenciaMontoInteres');
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato" ("IdFormato", "CodigoCampo", "TipoDato", "Posicion", "LargoCampo", "EsObligatorio", "Validacion", "MensajeValidacion", "IncluyeDecimales", "CantidadDecimales", "ForzarMayuscula") SELECT 3436, 'IGKDiferenciaMontoCapital', '', 37, 40, 'N', '', '', 'N', 0, 'S' FROM DUMMY WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.Formato" WHERE "CodigoCampo" = 'IGKDiferenciaMontoCapital');
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato" ("IdFormato", "CodigoCampo", "TipoDato", "Posicion", "LargoCampo", "EsObligatorio", "Validacion", "MensajeValidacion", "IncluyeDecimales", "CantidadDecimales", "ForzarMayuscula") SELECT 3437, 'IGKMontoRetencionArchivo', '', 38, 40, 'N', '', '', 'N', 0, 'S' FROM DUMMY WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.Formato" WHERE "CodigoCampo" = 'IGKMontoRetencionArchivo');
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato" ("IdFormato", "CodigoCampo", "TipoDato", "Posicion", "LargoCampo", "EsObligatorio", "Validacion", "MensajeValidacion", "IncluyeDecimales", "CantidadDecimales", "ForzarMayuscula") SELECT 3438, 'IGKDiferenciaMontoRetencion', '', 39, 40, 'N', '', '', 'N', 0, 'S' FROM DUMMY WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.Formato" WHERE "CodigoCampo" = 'IGKDiferenciaMontoRetencion');
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato" ("IdFormato", "CodigoCampo", "TipoDato", "Posicion", "LargoCampo", "EsObligatorio", "Validacion", "MensajeValidacion", "IncluyeDecimales", "CantidadDecimales", "ForzarMayuscula") SELECT 3439, 'IGKIdMoneda', '', 40, 3, 'N', '', '', 'N', 0, 'S' FROM DUMMY WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.Formato" WHERE "CodigoCampo" = 'IGKIdMoneda');
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato" ("IdFormato", "CodigoCampo", "TipoDato", "Posicion", "LargoCampo", "EsObligatorio", "Validacion", "MensajeValidacion", "IncluyeDecimales", "CantidadDecimales", "ForzarMayuscula") SELECT 3440, 'IGKFechaPago', '', 41, 10, 'N', '', '', 'N', 0, 'S' FROM DUMMY WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.Formato" WHERE "CodigoCampo" = 'IGKFechaPago');
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato" ("IdFormato", "CodigoCampo", "TipoDato", "Posicion", "LargoCampo", "EsObligatorio", "Validacion", "MensajeValidacion", "IncluyeDecimales", "CantidadDecimales", "ForzarMayuscula") SELECT 3441, 'IGKCodigoUsuarioAgente', '', 42, 40, 'N', '', '', 'N', 0, 'S' FROM DUMMY WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.Formato" WHERE "CodigoCampo" = 'IGKCodigoUsuarioAgente');
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato" ("IdFormato", "CodigoCampo", "TipoDato", "Posicion", "LargoCampo", "EsObligatorio", "Validacion", "MensajeValidacion", "IncluyeDecimales", "CantidadDecimales", "ForzarMayuscula") SELECT 3442, 'IGKNombreAgente', '', 43, 302, 'N', '', '', 'N', 0, 'S' FROM DUMMY WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.Formato" WHERE "CodigoCampo" = 'IGKNombreAgente');
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato" ("IdFormato", "CodigoCampo", "TipoDato", "Posicion", "LargoCampo", "EsObligatorio", "Validacion", "MensajeValidacion", "IncluyeDecimales", "CantidadDecimales", "ForzarMayuscula") SELECT 3443, 'IGKIdBoveda', '', 44, 3, 'N', '', '', 'N', 0, 'S' FROM DUMMY WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.Formato" WHERE "CodigoCampo" = 'IGKIdBoveda');
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato" ("IdFormato", "CodigoCampo", "TipoDato", "Posicion", "LargoCampo", "EsObligatorio", "Validacion", "MensajeValidacion", "IncluyeDecimales", "CantidadDecimales", "ForzarMayuscula") SELECT 3444, 'IGKIdEstado', '', 45, 3, 'N', '', '', 'N', 0, 'S' FROM DUMMY WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.Formato" WHERE "CodigoCampo" = 'IGKIdEstado');
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato" ("IdFormato", "CodigoCampo", "TipoDato", "Posicion", "LargoCampo", "EsObligatorio", "Validacion", "MensajeValidacion", "IncluyeDecimales", "CantidadDecimales", "ForzarMayuscula") SELECT 3445, 'IGKOrigen', '', 46, 3, 'N', '', '', 'N', 0, 'S' FROM DUMMY WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.Formato" WHERE "CodigoCampo" = 'IGKOrigen');
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato" ("IdFormato", "CodigoCampo", "TipoDato", "Posicion", "LargoCampo", "EsObligatorio", "Validacion", "MensajeValidacion", "IncluyeDecimales", "CantidadDecimales", "ForzarMayuscula") SELECT 3446, 'IGKEstadoNotificacion', '', 47, 1, 'N', '', '', 'N', 0, 'S' FROM DUMMY WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.Formato" WHERE "CodigoCampo" = 'IGKEstadoNotificacion');
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato" ("IdFormato", "CodigoCampo", "TipoDato", "Posicion", "LargoCampo", "EsObligatorio", "Validacion", "MensajeValidacion", "IncluyeDecimales", "CantidadDecimales", "ForzarMayuscula") SELECT 3447, 'IGKNombreMedioDePago', '', 48, 100, 'N', '', '', 'N', 0, 'S' FROM DUMMY WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.Formato" WHERE "CodigoCampo" = 'IGKNombreMedioDePago');
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato" ("IdFormato", "CodigoCampo", "TipoDato", "Posicion", "LargoCampo", "EsObligatorio", "Validacion", "MensajeValidacion", "IncluyeDecimales", "CantidadDecimales", "ForzarMayuscula") SELECT 3448, 'IGKTipoCuentaBoveda', '', 49, 40, 'N', '', '', 'N', 0, 'S' FROM DUMMY WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.Formato" WHERE "CodigoCampo" = 'IGKTipoCuentaBoveda');
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato" ("IdFormato", "CodigoCampo", "TipoDato", "Posicion", "LargoCampo", "EsObligatorio", "Validacion", "MensajeValidacion", "IncluyeDecimales", "CantidadDecimales", "ForzarMayuscula") SELECT 3449, 'IGKNumeroCuentaBoveda', '', 50, 40, 'N', '', '', 'N', 0, 'S' FROM DUMMY WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.Formato" WHERE "CodigoCampo" = 'IGKNumeroCuentaBoveda');
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato" ("IdFormato", "CodigoCampo", "TipoDato", "Posicion", "LargoCampo", "EsObligatorio", "Validacion", "MensajeValidacion", "IncluyeDecimales", "CantidadDecimales", "ForzarMayuscula") SELECT 3450, 'IGKPagaClienteCorredor', '', 51, 40, 'N', '', '', 'N', 0, 'S' FROM DUMMY WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.Formato" WHERE "CodigoCampo" = 'IGKPagaClienteCorredor');
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato" ("IdFormato", "CodigoCampo", "TipoDato", "Posicion", "LargoCampo", "EsObligatorio", "Validacion", "MensajeValidacion", "IncluyeDecimales", "CantidadDecimales", "ForzarMayuscula") SELECT 3451, 'IGKIdTipoReajuste', '', 52, 3, 'N', '', '', 'N', 0, 'S' FROM DUMMY WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.Formato" WHERE "CodigoCampo" = 'IGKIdTipoReajuste');
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato" ("IdFormato", "CodigoCampo", "TipoDato", "Posicion", "LargoCampo", "EsObligatorio", "Validacion", "MensajeValidacion", "IncluyeDecimales", "CantidadDecimales", "ForzarMayuscula") SELECT 3452, 'IGKINGMontoInteresCorteCupon', '', 53, 40, 'N', '', '', 'S', 8, 'S' FROM DUMMY WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.Formato" WHERE "CodigoCampo" = 'IGKINGMontoInteresCorteCupon');
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato" ("IdFormato", "CodigoCampo", "TipoDato", "Posicion", "LargoCampo", "EsObligatorio", "Validacion", "MensajeValidacion", "IncluyeDecimales", "CantidadDecimales", "ForzarMayuscula") SELECT 3453, 'IGKAmortizacion', '', 54, 40, 'N', '', '', 'S', 8, 'S' FROM DUMMY WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.Formato" WHERE "CodigoCampo" = 'IGKAmortizacion');
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato" ("IdFormato", "CodigoCampo", "TipoDato", "Posicion", "LargoCampo", "EsObligatorio", "Validacion", "MensajeValidacion", "IncluyeDecimales", "CantidadDecimales", "ForzarMayuscula") SELECT 3454, 'IGKAfectoaClearing', '', 55, 1, 'N', '', '', 'N', 0, 'S' FROM DUMMY WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.Formato" WHERE "CodigoCampo" = 'IGKAfectoaClearing');
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato" ("IdFormato", "CodigoCampo", "TipoDato", "Posicion", "LargoCampo", "EsObligatorio", "Validacion", "MensajeValidacion", "IncluyeDecimales", "CantidadDecimales", "ForzarMayuscula") SELECT 3455, 'IGKProcesadoClearing', '', 56, 1, 'N', '', '', 'N', 0, 'S' FROM DUMMY WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.Formato" WHERE "CodigoCampo" = 'IGKProcesadoClearing');
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato" ("IdFormato", "CodigoCampo", "TipoDato", "Posicion", "LargoCampo", "EsObligatorio", "Validacion", "MensajeValidacion", "IncluyeDecimales", "CantidadDecimales", "ForzarMayuscula") SELECT 3456, 'IGKIdRegimenFondoMutuo', '', 57, 40, 'N', '', '', 'N', 0, 'S' FROM DUMMY WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.Formato" WHERE "CodigoCampo" = 'IGKIdRegimenFondoMutuo');
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato" ("IdFormato", "CodigoCampo", "TipoDato", "Posicion", "LargoCampo", "EsObligatorio", "Validacion", "MensajeValidacion", "IncluyeDecimales", "CantidadDecimales", "ForzarMayuscula") SELECT 3457, 'IGKISFUT', '', 58, 1, 'S', '', '', 'S', 0, 'S' FROM DUMMY WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.Formato" WHERE "CodigoCampo" = 'IGKISFUT');
INSERT INTO "BSCL.Plataforma.BD::PLA.Formato" ("IdFormato", "CodigoCampo", "TipoDato", "Posicion", "LargoCampo", "EsObligatorio", "Validacion", "MensajeValidacion", "IncluyeDecimales", "CantidadDecimales", "ForzarMayuscula") SELECT 3458, 'IGKRetencionISFUT', '', 59, 1, 'S', '', '', 'N', 0, 'S' FROM DUMMY WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.Formato" WHERE "CodigoCampo" = 'IGKRetencionISFUT');
-------------------------------------------------------------------------------------------------------------------------------
--CORRELATIVO TIPO DE ARCHIVO CARGA MASIVA
INSERT INTO "BSCL.Plataforma.BD::PLA.ArchivoFormato" ("IdArchivoFormato", "IdArchivo", "IdFormato") SELECT 3400, 160, 3400 FROM DUMMY WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.ArchivoFormato" WHERE "IdArchivoFormato" = 3400);
INSERT INTO "BSCL.Plataforma.BD::PLA.ArchivoFormato" ("IdArchivoFormato", "IdArchivo", "IdFormato") SELECT 3401, 160, 3401 FROM DUMMY WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.ArchivoFormato" WHERE "IdArchivoFormato" = 3401);
INSERT INTO "BSCL.Plataforma.BD::PLA.ArchivoFormato" ("IdArchivoFormato", "IdArchivo", "IdFormato") SELECT 3402, 160, 3402 FROM DUMMY WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.ArchivoFormato" WHERE "IdArchivoFormato" = 3402);
INSERT INTO "BSCL.Plataforma.BD::PLA.ArchivoFormato" ("IdArchivoFormato", "IdArchivo", "IdFormato") SELECT 3403, 160, 3403 FROM DUMMY WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.ArchivoFormato" WHERE "IdArchivoFormato" = 3403);
INSERT INTO "BSCL.Plataforma.BD::PLA.ArchivoFormato" ("IdArchivoFormato", "IdArchivo", "IdFormato") SELECT 3404, 160, 3404 FROM DUMMY WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.ArchivoFormato" WHERE "IdArchivoFormato" = 3404);
INSERT INTO "BSCL.Plataforma.BD::PLA.ArchivoFormato" ("IdArchivoFormato", "IdArchivo", "IdFormato") SELECT 3405, 160, 3405 FROM DUMMY WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.ArchivoFormato" WHERE "IdArchivoFormato" = 3405);
INSERT INTO "BSCL.Plataforma.BD::PLA.ArchivoFormato" ("IdArchivoFormato", "IdArchivo", "IdFormato") SELECT 3406, 160, 3406 FROM DUMMY WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.ArchivoFormato" WHERE "IdArchivoFormato" = 3406);
INSERT INTO "BSCL.Plataforma.BD::PLA.ArchivoFormato" ("IdArchivoFormato", "IdArchivo", "IdFormato") SELECT 3407, 160, 3407 FROM DUMMY WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.ArchivoFormato" WHERE "IdArchivoFormato" = 3407);
INSERT INTO "BSCL.Plataforma.BD::PLA.ArchivoFormato" ("IdArchivoFormato", "IdArchivo", "IdFormato") SELECT 3408, 160, 3408 FROM DUMMY WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.ArchivoFormato" WHERE "IdArchivoFormato" = 3408);
INSERT INTO "BSCL.Plataforma.BD::PLA.ArchivoFormato" ("IdArchivoFormato", "IdArchivo", "IdFormato") SELECT 3409, 160, 3409 FROM DUMMY WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.ArchivoFormato" WHERE "IdArchivoFormato" = 3409);
INSERT INTO "BSCL.Plataforma.BD::PLA.ArchivoFormato" ("IdArchivoFormato", "IdArchivo", "IdFormato") SELECT 3410, 160, 3410 FROM DUMMY WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.ArchivoFormato" WHERE "IdArchivoFormato" = 3410);
INSERT INTO "BSCL.Plataforma.BD::PLA.ArchivoFormato" ("IdArchivoFormato", "IdArchivo", "IdFormato") SELECT 3411, 160, 3411 FROM DUMMY WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.ArchivoFormato" WHERE "IdArchivoFormato" = 3411);
INSERT INTO "BSCL.Plataforma.BD::PLA.ArchivoFormato" ("IdArchivoFormato", "IdArchivo", "IdFormato") SELECT 3412, 160, 3412 FROM DUMMY WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.ArchivoFormato" WHERE "IdArchivoFormato" = 3412);
INSERT INTO "BSCL.Plataforma.BD::PLA.ArchivoFormato" ("IdArchivoFormato", "IdArchivo", "IdFormato") SELECT 3413, 160, 3413 FROM DUMMY WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.ArchivoFormato" WHERE "IdArchivoFormato" = 3413);
INSERT INTO "BSCL.Plataforma.BD::PLA.ArchivoFormato" ("IdArchivoFormato", "IdArchivo", "IdFormato") SELECT 3414, 160, 3414 FROM DUMMY WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.ArchivoFormato" WHERE "IdArchivoFormato" = 3414);
INSERT INTO "BSCL.Plataforma.BD::PLA.ArchivoFormato" ("IdArchivoFormato", "IdArchivo", "IdFormato") SELECT 3415, 160, 3415 FROM DUMMY WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.ArchivoFormato" WHERE "IdArchivoFormato" = 3415);
INSERT INTO "BSCL.Plataforma.BD::PLA.ArchivoFormato" ("IdArchivoFormato", "IdArchivo", "IdFormato") SELECT 3416, 160, 3416 FROM DUMMY WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.ArchivoFormato" WHERE "IdArchivoFormato" = 3416);
INSERT INTO "BSCL.Plataforma.BD::PLA.ArchivoFormato" ("IdArchivoFormato", "IdArchivo", "IdFormato") SELECT 3417, 160, 3417 FROM DUMMY WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.ArchivoFormato" WHERE "IdArchivoFormato" = 3417);
INSERT INTO "BSCL.Plataforma.BD::PLA.ArchivoFormato" ("IdArchivoFormato", "IdArchivo", "IdFormato") SELECT 3418, 160, 3418 FROM DUMMY WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.ArchivoFormato" WHERE "IdArchivoFormato" = 3418);
INSERT INTO "BSCL.Plataforma.BD::PLA.ArchivoFormato" ("IdArchivoFormato", "IdArchivo", "IdFormato") SELECT 3419, 160, 3419 FROM DUMMY WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.ArchivoFormato" WHERE "IdArchivoFormato" = 3419);
INSERT INTO "BSCL.Plataforma.BD::PLA.ArchivoFormato" ("IdArchivoFormato", "IdArchivo", "IdFormato") SELECT 3420, 160, 3420 FROM DUMMY WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.ArchivoFormato" WHERE "IdArchivoFormato" = 3420);
INSERT INTO "BSCL.Plataforma.BD::PLA.ArchivoFormato" ("IdArchivoFormato", "IdArchivo", "IdFormato") SELECT 3421, 160, 3421 FROM DUMMY WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.ArchivoFormato" WHERE "IdArchivoFormato" = 3421);
INSERT INTO "BSCL.Plataforma.BD::PLA.ArchivoFormato" ("IdArchivoFormato", "IdArchivo", "IdFormato") SELECT 3422, 160, 3422 FROM DUMMY WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.ArchivoFormato" WHERE "IdArchivoFormato" = 3422);
INSERT INTO "BSCL.Plataforma.BD::PLA.ArchivoFormato" ("IdArchivoFormato", "IdArchivo", "IdFormato") SELECT 3423, 160, 3423 FROM DUMMY WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.ArchivoFormato" WHERE "IdArchivoFormato" = 3423);
INSERT INTO "BSCL.Plataforma.BD::PLA.ArchivoFormato" ("IdArchivoFormato", "IdArchivo", "IdFormato") SELECT 3424, 160, 3424 FROM DUMMY WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.ArchivoFormato" WHERE "IdArchivoFormato" = 3424);
INSERT INTO "BSCL.Plataforma.BD::PLA.ArchivoFormato" ("IdArchivoFormato", "IdArchivo", "IdFormato") SELECT 3425, 160, 3425 FROM DUMMY WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.ArchivoFormato" WHERE "IdArchivoFormato" = 3425);
INSERT INTO "BSCL.Plataforma.BD::PLA.ArchivoFormato" ("IdArchivoFormato", "IdArchivo", "IdFormato") SELECT 3426, 160, 3426 FROM DUMMY WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.ArchivoFormato" WHERE "IdArchivoFormato" = 3426);
INSERT INTO "BSCL.Plataforma.BD::PLA.ArchivoFormato" ("IdArchivoFormato", "IdArchivo", "IdFormato") SELECT 3427, 160, 3427 FROM DUMMY WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.ArchivoFormato" WHERE "IdArchivoFormato" = 3427);
INSERT INTO "BSCL.Plataforma.BD::PLA.ArchivoFormato" ("IdArchivoFormato", "IdArchivo", "IdFormato") SELECT 3428, 160, 3428 FROM DUMMY WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.ArchivoFormato" WHERE "IdArchivoFormato" = 3428);
INSERT INTO "BSCL.Plataforma.BD::PLA.ArchivoFormato" ("IdArchivoFormato", "IdArchivo", "IdFormato") SELECT 3429, 160, 3429 FROM DUMMY WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.ArchivoFormato" WHERE "IdArchivoFormato" = 3429);
INSERT INTO "BSCL.Plataforma.BD::PLA.ArchivoFormato" ("IdArchivoFormato", "IdArchivo", "IdFormato") SELECT 3430, 160, 3430 FROM DUMMY WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.ArchivoFormato" WHERE "IdArchivoFormato" = 3430);
INSERT INTO "BSCL.Plataforma.BD::PLA.ArchivoFormato" ("IdArchivoFormato", "IdArchivo", "IdFormato") SELECT 3431, 160, 3431 FROM DUMMY WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.ArchivoFormato" WHERE "IdArchivoFormato" = 3431);
INSERT INTO "BSCL.Plataforma.BD::PLA.ArchivoFormato" ("IdArchivoFormato", "IdArchivo", "IdFormato") SELECT 3432, 160, 3432 FROM DUMMY WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.ArchivoFormato" WHERE "IdArchivoFormato" = 3432);
INSERT INTO "BSCL.Plataforma.BD::PLA.ArchivoFormato" ("IdArchivoFormato", "IdArchivo", "IdFormato") SELECT 3433, 160, 3433 FROM DUMMY WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.ArchivoFormato" WHERE "IdArchivoFormato" = 3433);
INSERT INTO "BSCL.Plataforma.BD::PLA.ArchivoFormato" ("IdArchivoFormato", "IdArchivo", "IdFormato") SELECT 3434, 160, 3434 FROM DUMMY WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.ArchivoFormato" WHERE "IdArchivoFormato" = 3434);
INSERT INTO "BSCL.Plataforma.BD::PLA.ArchivoFormato" ("IdArchivoFormato", "IdArchivo", "IdFormato") SELECT 3435, 160, 3435 FROM DUMMY WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.ArchivoFormato" WHERE "IdArchivoFormato" = 3435);
INSERT INTO "BSCL.Plataforma.BD::PLA.ArchivoFormato" ("IdArchivoFormato", "IdArchivo", "IdFormato") SELECT 3436, 160, 3436 FROM DUMMY WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.ArchivoFormato" WHERE "IdArchivoFormato" = 3436);
INSERT INTO "BSCL.Plataforma.BD::PLA.ArchivoFormato" ("IdArchivoFormato", "IdArchivo", "IdFormato") SELECT 3437, 160, 3437 FROM DUMMY WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.ArchivoFormato" WHERE "IdArchivoFormato" = 3437);
INSERT INTO "BSCL.Plataforma.BD::PLA.ArchivoFormato" ("IdArchivoFormato", "IdArchivo", "IdFormato") SELECT 3438, 160, 3438 FROM DUMMY WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.ArchivoFormato" WHERE "IdArchivoFormato" = 3438);
INSERT INTO "BSCL.Plataforma.BD::PLA.ArchivoFormato" ("IdArchivoFormato", "IdArchivo", "IdFormato") SELECT 3439, 160, 3439 FROM DUMMY WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.ArchivoFormato" WHERE "IdArchivoFormato" = 3439);
INSERT INTO "BSCL.Plataforma.BD::PLA.ArchivoFormato" ("IdArchivoFormato", "IdArchivo", "IdFormato") SELECT 3440, 160, 3440 FROM DUMMY WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.ArchivoFormato" WHERE "IdArchivoFormato" = 3440);
INSERT INTO "BSCL.Plataforma.BD::PLA.ArchivoFormato" ("IdArchivoFormato", "IdArchivo", "IdFormato") SELECT 3441, 160, 3441 FROM DUMMY WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.ArchivoFormato" WHERE "IdArchivoFormato" = 3441);
INSERT INTO "BSCL.Plataforma.BD::PLA.ArchivoFormato" ("IdArchivoFormato", "IdArchivo", "IdFormato") SELECT 3442, 160, 3442 FROM DUMMY WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.ArchivoFormato" WHERE "IdArchivoFormato" = 3442);
INSERT INTO "BSCL.Plataforma.BD::PLA.ArchivoFormato" ("IdArchivoFormato", "IdArchivo", "IdFormato") SELECT 3443, 160, 3443 FROM DUMMY WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.ArchivoFormato" WHERE "IdArchivoFormato" = 3443);
INSERT INTO "BSCL.Plataforma.BD::PLA.ArchivoFormato" ("IdArchivoFormato", "IdArchivo", "IdFormato") SELECT 3444, 160, 3444 FROM DUMMY WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.ArchivoFormato" WHERE "IdArchivoFormato" = 3444);
INSERT INTO "BSCL.Plataforma.BD::PLA.ArchivoFormato" ("IdArchivoFormato", "IdArchivo", "IdFormato") SELECT 3445, 160, 3445 FROM DUMMY WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.ArchivoFormato" WHERE "IdArchivoFormato" = 3445);
INSERT INTO "BSCL.Plataforma.BD::PLA.ArchivoFormato" ("IdArchivoFormato", "IdArchivo", "IdFormato") SELECT 3446, 160, 3446 FROM DUMMY WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.ArchivoFormato" WHERE "IdArchivoFormato" = 3446);
INSERT INTO "BSCL.Plataforma.BD::PLA.ArchivoFormato" ("IdArchivoFormato", "IdArchivo", "IdFormato") SELECT 3447, 160, 3447 FROM DUMMY WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.ArchivoFormato" WHERE "IdArchivoFormato" = 3447);
INSERT INTO "BSCL.Plataforma.BD::PLA.ArchivoFormato" ("IdArchivoFormato", "IdArchivo", "IdFormato") SELECT 3448, 160, 3448 FROM DUMMY WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.ArchivoFormato" WHERE "IdArchivoFormato" = 3448);
INSERT INTO "BSCL.Plataforma.BD::PLA.ArchivoFormato" ("IdArchivoFormato", "IdArchivo", "IdFormato") SELECT 3449, 160, 3449 FROM DUMMY WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.ArchivoFormato" WHERE "IdArchivoFormato" = 3449);
INSERT INTO "BSCL.Plataforma.BD::PLA.ArchivoFormato" ("IdArchivoFormato", "IdArchivo", "IdFormato") SELECT 3450, 160, 3450 FROM DUMMY WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.ArchivoFormato" WHERE "IdArchivoFormato" = 3450);
INSERT INTO "BSCL.Plataforma.BD::PLA.ArchivoFormato" ("IdArchivoFormato", "IdArchivo", "IdFormato") SELECT 3451, 160, 3451 FROM DUMMY WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.ArchivoFormato" WHERE "IdArchivoFormato" = 3451);
INSERT INTO "BSCL.Plataforma.BD::PLA.ArchivoFormato" ("IdArchivoFormato", "IdArchivo", "IdFormato") SELECT 3452, 160, 3452 FROM DUMMY WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.ArchivoFormato" WHERE "IdArchivoFormato" = 3452);
INSERT INTO "BSCL.Plataforma.BD::PLA.ArchivoFormato" ("IdArchivoFormato", "IdArchivo", "IdFormato") SELECT 3453, 160, 3453 FROM DUMMY WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.ArchivoFormato" WHERE "IdArchivoFormato" = 3453);
INSERT INTO "BSCL.Plataforma.BD::PLA.ArchivoFormato" ("IdArchivoFormato", "IdArchivo", "IdFormato") SELECT 3454, 160, 3454 FROM DUMMY WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.ArchivoFormato" WHERE "IdArchivoFormato" = 3454);
INSERT INTO "BSCL.Plataforma.BD::PLA.ArchivoFormato" ("IdArchivoFormato", "IdArchivo", "IdFormato") SELECT 3455, 160, 3455 FROM DUMMY WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.ArchivoFormato" WHERE "IdArchivoFormato" = 3455);
INSERT INTO "BSCL.Plataforma.BD::PLA.ArchivoFormato" ("IdArchivoFormato", "IdArchivo", "IdFormato") SELECT 3456, 160, 3456 FROM DUMMY WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.ArchivoFormato" WHERE "IdArchivoFormato" = 3456);
INSERT INTO "BSCL.Plataforma.BD::PLA.ArchivoFormato" ("IdArchivoFormato", "IdArchivo", "IdFormato") SELECT 3457, 160, 3457 FROM DUMMY WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.ArchivoFormato" WHERE "IdArchivoFormato" = 3457);
INSERT INTO "BSCL.Plataforma.BD::PLA.ArchivoFormato" ("IdArchivoFormato", "IdArchivo", "IdFormato") SELECT 3458, 160, 3458 FROM DUMMY WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.ArchivoFormato" WHERE "IdArchivoFormato" = 3458);
-------------------------------------------------------------------------------------------------------------------------------
--CREACION DE TABLAS
DROP TABLE "BSCL.Custodia.BD::CAR.TipoCargaMasiva";
CREATE COLUMN TABLE "BSCL.Custodia.BD::CAR.TipoCargaMasiva" ("IdTipoCargaMasiva" BIGINT CS_FIXED NOT NULL ,
"CodigoTipoCargaMasiva" NVARCHAR(40) DEFAULT '' NOT NULL ,
"Nombre" NVARCHAR(100) DEFAULT '' NOT NULL ,
"IdEmpresa" BIGINT CS_FIXED DEFAULT 0 NOT NULL ,
"IdEstado" NVARCHAR(3) DEFAULT '' NOT NULL ,
"GeneraPartida" NVARCHAR(1) DEFAULT '' NOT NULL ,
PRIMARY KEY ("IdTipoCargaMasiva")) UNLOAD PRIORITY 5 AUTO MERGE;

INSERT INTO "BSCL.Custodia.BD::CAR.TipoCargaMasiva"
("IdTipoCargaMasiva", "CodigoTipoCargaMasiva", "Nombre", "IdEmpresa", "IdEstado", "GeneraPartida")
SELECT 1, 'CARGA_CLIENTES_ISFUT', 'CARGA MASIVA CLIENTES ISFUT', 1, 'ACT', 'N'
FROM DUMMY WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Custodia.BD::CAR.TipoCargaMasiva" WHERE "CodigoTipoCargaMasiva" = 'CARGA_CLIENTES_ISFUT' );

DROP TABLE "BSCL.Custodia.BD::CAR.CargaMasiva";
CREATE COLUMN TABLE "BSCL.Custodia.BD::CAR.CargaMasiva" ("IdCargaMasiva" BIGINT CS_FIXED NOT NULL GENERATED BY DEFAULT AS IDENTITY,
"IdEmpresa" BIGINT CS_FIXED DEFAULT 0 NOT NULL ,
"IdUsuario" BIGINT CS_FIXED DEFAULT 0 NOT NULL ,
"CantidadRegistros" INTEGER CS_INT DEFAULT 0 NOT NULL ,
"NombreArchivo" NVARCHAR(100) DEFAULT '' NOT NULL ,
"FechaCarga" SECONDDATE CS_SECONDDATE DEFAULT '0001-01-01' NOT NULL ,
"CodigoTipoCargaMasiva" NVARCHAR(40) DEFAULT '' NOT NULL ,
PRIMARY KEY ("IdCargaMasiva")) UNLOAD PRIORITY 5 AUTO MERGE;

DROP TABLE "BSCL.Custodia.BD::CAR.CargaMasivaDetalleISFUT";
CREATE COLUMN TABLE "BSCL.Custodia.BD::CAR.CargaMasivaDetalleISFUT" ("IdCargaMasivaDetalleISFUT" BIGINT CS_FIXED  NOT NULL,
"IdEventoCapitalCliente" BIGINT CS_FIXED NOT NULL,
"IdCargaMasivaISFUT" BIGINT CS_FIXED NOT NULL,
"IdTipoEventoCapital" NVARCHAR(3) NULL,
"NumeroDividendo" NVARCHAR(40) NULL,
"Secuencia" NVARCHAR(40) NULL,
"TipoSocioNegocio" NVARCHAR(3) NULL,
"CodigoPortafolio" NVARCHAR(40) NULL,
"IdClasificacionTipoInstrumento" NVARCHAR(3) NULL,
"Nemo" NVARCHAR(40) NULL,
"ISIN" NVARCHAR(40) NULL,
"NemoNuevo" NVARCHAR(40) NULL,
"IdConceptoGarantia" NVARCHAR(40) NULL,
"TipoCartera" NVARCHAR(40) NULL,
"DocumentoIdentidad" NVARCHAR(50) NULL,
"NombreCliente" NVARCHAR(302)   NULL,
"ConfiguracionNotificacion" NVARCHAR(1) NULL,
"TienePreferenciaDePago" NVARCHAR(1) NULL,
"MontoCapital" NVARCHAR(40) NULL,
"Precio" NVARCHAR(40) NULL,
"Factor" NVARCHAR(40) NULL,
"FactorNuevo" NVARCHAR(40) NULL,
"PrecioNuevo" NVARCHAR(40) NULL,
"CantidadNueva" NVARCHAR(40) NULL,
"MontoInteres" NVARCHAR(40) NULL,
"MontoReajuste" NVARCHAR(40) NULL,
"CantidadDerecho" NVARCHAR(40) NULL,
"Total" NVARCHAR(40) NULL,
"MontoRetencion" NVARCHAR(40) NULL,
"CantidadDerechoArchivo" NVARCHAR(40) NULL,
"AccionesDisponiblesArchivo" NVARCHAR(40) NULL,
"DiferenciaCantidadDerecho" NVARCHAR(40) NULL,
"DiferenciaAccionesCantidadNueva" NVARCHAR(40) NULL,
"MontoBeneficioArchivo" NVARCHAR(40) NULL,
"DiferenciaMontoBeneficio" NVARCHAR(40) NULL,
"MontoInteresArchivo" NVARCHAR(40) NULL,
"MontoCapitalArchivo" NVARCHAR(40) NULL,
"DiferenciaMontoInteres" NVARCHAR(40) NULL,
"DiferenciaMontoCapital" NVARCHAR(40) NULL,
"MontoRetencionArchivo" NVARCHAR(40) NULL,
"DiferenciaMontoRetencion" NVARCHAR(40) NULL,
"IdMoneda" NVARCHAR(3) NULL,
"FechaPago" NVARCHAR(40) NULL,
"CodigoUsuarioAgente" NVARCHAR(40) NULL,
"NombreAgente" NVARCHAR(302) NULL,
"IdBoveda" NVARCHAR(3) NULL,
"IdEstado" NVARCHAR(3) NULL,
"Origen" NVARCHAR(40) NULL,
"EstadoNotificacion" NVARCHAR(1) NULL,
"NombreMedioDePago" NVARCHAR(100) NULL,
"TipoCuentaBoveda" NVARCHAR(40) NULL,
"NumeroCuentaBoveda" NVARCHAR(40) NULL,
"PagaClienteCorredor" NVARCHAR(40) NULL,
"IdTipoReajuste" NVARCHAR(40) NULL,
"INGMontoInteresCorteCupon" NVARCHAR(40) NULL,
"Amortizacion" NVARCHAR(40) NULL,
"AfectoaClearing" NVARCHAR(1) NULL,
"ProcesadoClearing" NVARCHAR(1) NULL,
"IdRegimenFondoMutuo" NVARCHAR(40) NULL,
"ISFUT" NVARCHAR(1) NOT NULL,
"RetencionISFUT" NVARCHAR(1) NOT NULL,
"Estado" NVARCHAR(3) NULL,
"TipoArchivo" NVARCHAR(10) NULL,
"EstadoCargo" NVARCHAR(1) NULL,
PRIMARY KEY ("IdCargaMasivaDetalleISFUT")) UNLOAD PRIORITY 5 AUTO MERGE;
-------------------------------------------------------------------------------------------------------------------------------
--CREACION SECUENCIAS
DROP SEQUENCE "BSCL.Custodia.BD.Secuencias::IdCargaMasivaISFUT"   CASCADE;
CREATE SEQUENCE "BSCL.Custodia.BD.Secuencias::IdCargaMasivaISFUT"
START WITH 1
INCREMENT BY 1
MINVALUE 1;

DROP SEQUENCE "BSCL.Custodia.BD.Secuencias::IdCargaMasivaDetalleISFUT"  CASCADE;
CREATE SEQUENCE "BSCL.Custodia.BD.Secuencias::IdCargaMasivaDetalleISFUT"
START WITH 1
INCREMENT BY 1
MINVALUE 1;
--------------------------------------------------------------------------------------------------------------------------------