/*** Se inscriben aca actualizaciones a la version BASE 2020 + 2021 ***/
/*
 *   consideracion :
 *    
 *   - insert o update es para ambas versiones (obligatorio actualizar script en :
 *   - /Normativa/BD/Scripts/TRIBUTARIO/2021/DJ1894.sql  
 * 
 */

/*
* Creacion 20-01-2020
* Se agrega insert con parámetro que omite montos 0 en DJ 1894 (aportes /rescates que no llegan al peso)
* Se agrega insert con Parámetro para nombre de fondos Sura que inician con 0
* Estos parametros se agregan en la creacion de DJ1894 y trabajan junto al archivo PreOrigen.sql
*/

/* PARAMETRO DJ1894 OMITE FONDO QUE COMIENZE EN 0 */
INSERT INTO BSCL."BSCL.Normativa.BD::NOR.Configuracion"
("IdEmpresa", "CodigoParametro", "Descripcion", "Valor", "IdNormativa", "EsModificableUsuario", "Uso", "ValorDefecto", "ValoresPosibles", "ModificablePorUsuario", "ModificablePostProduccion", "CreadorParametro", "FechaCreacion", "ModificadorParametro", "FechaModificacion", "Observaciones")
SELECT 1 AS "IdEmpresa",'OMITIR_FONDOS_0'AS "CodigoParametro",'Parametro que omite fondos Sura que inician con 0' AS "Descripcion"
, 'S' AS "Valor"
, "Esquema" AS  "IdNormativa"
, 'S' AS "EsModificableUsuario"
, '' AS "Uso"
, 'N' AS "ValorDefecto"
, 'S,N' AS "ValoresPosibles"
, 'S' AS "ModificablePorUsuario"
, '' AS  "ModificablePostProduccion"
, '' AS  "CreadorParametro"
, CURRENT_UTCTIMESTAMP AS "FechaCreacion"
, '' AS "ModificadorParametro"
, CURRENT_UTCTIMESTAMP AS "FechaModificacion"
, '' AS "Observaciones"
FROM BSCL."BSCL.Normativa.BD::NOR.ArchivoNormativa" WHERE "Esquema"='DJ1894' AND "VersionArchivo"=2021
AND NOT EXISTS (SELECT 1 FROM BSCL."BSCL.Normativa.BD::NOR.Configuracion" N WHERE N."CodigoParametro"='OMITIR_FONDOS_0' AND N."IdNormativa"="Esquema");


/* PARAMETRO DJ1894 OMITE MONTOS EN 0 APORTES/RESCATES */
INSERT INTO BSCL."BSCL.Normativa.BD::NOR.Configuracion"
("IdEmpresa", "CodigoParametro", "Descripcion", "Valor", "IdNormativa", "EsModificableUsuario", "Uso", "ValorDefecto", "ValoresPosibles", "ModificablePorUsuario", "ModificablePostProduccion", "CreadorParametro", "FechaCreacion", "ModificadorParametro", "FechaModificacion", "Observaciones")
SELECT 1 AS "IdEmpresa",'OMITIR_MONTOS_0'AS "CodigoParametro",'Parametro que omite montos en 0' AS "Descripcion"
, 'N' AS "Valor"
, "Esquema" AS  "IdNormativa"
, 'S' AS "EsModificableUsuario"
, '' AS "Uso"
, 'N' AS "ValorDefecto"
, 'S,N' AS "ValoresPosibles"
, 'S' AS "ModificablePorUsuario"
, '' AS  "ModificablePostProduccion"
, '' AS  "CreadorParametro"
, CURRENT_UTCTIMESTAMP AS "FechaCreacion"
, '' AS "ModificadorParametro"
, CURRENT_UTCTIMESTAMP AS "FechaModificacion"
, '' AS "Observaciones"
FROM BSCL."BSCL.Normativa.BD::NOR.ArchivoNormativa" WHERE "Esquema"='DJ1894' AND "VersionArchivo"=2021
AND NOT EXISTS (SELECT 1 FROM BSCL."BSCL.Normativa.BD::NOR.Configuracion" N WHERE N."CodigoParametro"='OMITIR_MONTOS_0' AND N."IdNormativa"="Esquema");

/* Fin Parámetros */
-- ::::: CARGA MASIVA
DELETE FROM BSCL."BSCL.Normativa.BD::DOC.CampoCuadratura" WHERE "Esquema" = 'DJ1894' AND "Version" = 2020;
DELETE FROM BSCL."BSCL.Normativa.BD::DOC.CampoCuadratura" WHERE "Esquema" = 'DJ1894' AND "Version" = 2021;
DELETE FROM BSCL."BSCL.Normativa.BD::DOC.Campo" WHERE "Esquema" = 'DJ1894' AND "Version" = 2020;
DELETE FROM BSCL."BSCL.Normativa.BD::DOC.Campo" WHERE "Esquema" = 'DJ1894' AND "Version" = 2021;

/* Campos Inicio */
--Update Version 2020
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1894', '2020', 'Numero', 'C0', '', 2, 'Numero', '', 'ID', 0, '0', 'N', 'N', 'N', 'S', 'N', 'N', 'N', 'S', '1. DATOS DE LOS INFORMADOS', 1, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1894', '2020', 'RutParticipe', 'C3', '', 4, 'Rut Participe', '', 'RUT', 0, '0', 'S', 'N', 'N', 'S', 'N', 'N', 'N', 'N', '1. DATOS DE LOS INFORMADOS', 3, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1894', '2020', 'RutFondo', 'C23', '', 5, 'Rut del Fondo Mutuo', '', 'RUT', 0, '0', 'S', 'N', 'N', 'S', 'N', 'N', 'N', 'N', '1. DATOS DE LOS INFORMADOS', 4, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1894', '2020', 'RunFondo', 'C4', '', 6, 'Run del Fondo Mutuo', '', 'RUT', 0, '0', 'S', 'N', 'N', 'S', 'N', 'N', 'N', 'N', '1. DATOS DE LOS INFORMADOS', 5, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1894', '2020', 'FechaInversion', 'C6', '', 8, 'Fecha Inversion o Reinversion', '', 'DATE', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '2. SECCI�N B1: INVERSIONES Y REINVERSIONES RECIBIDAS EN EL EJERCICIO', 7, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1894', '2020', 'FechaOperacion', '', '', 9, 'Fecha Operacion', '', 'DATE_FILTER', 0, '', 'S', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '5. DATOS DE CONTROL', 8, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1894', '2020', 'FechaRescate', 'C8', '', 11, 'Fecha Rescate', '', 'DATE', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '3. SECCI�N B2: RESCATES', 10, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1894', '2020', 'RutAdminitradoraFondoReceptor', 'C13', '', 15, 'Rut Sociedad Administradora Fondo Receptor de la Reinversi�n', '', 'RUT', 0, '0', 'S', 'N', 'N', 'S', 'N', 'N', 'N', 'N', '4. SECCI�N B3: LIQUIDACIONES DE CUOTAS PARA REINVERSIONES INCISO QUINTO ART. 108', 14, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1894', '2020', 'IdEmisor', '', '', 29, 'Emisor', '', 'NEMO', 0, '', 'S', 'N', 'N', 'N', 'S', 'N', 'N', 'N', '1. DATOS DE LOS INFORMADOS', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1894', '2020', 'TipoOperacion', 'C5', '', 7, 'TIpo Operacion', '', 'ID', 0, '1', 'S', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. DATOS DE LOS INFORMADOS', 6, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1894', '2020', 'MontoNominalInversionActualizado', 'C7', '', 10, 'Monto Nominal Inversion o Reinversion (actualizado)', '', 'AMOUNT', 0, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '2. SECCI�N B1: INVERSIONES Y REINVERSIONES RECIBIDAS EN EL EJERCICIO', 9, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1894', '2020', 'MontoNominalRescate', 'C9', '', 12, 'Monto Nominal Rescate a la Fecha en que se Efectuo', '', 'AMOUNT', 0, '0', 'S', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '3. SECCI�N B2: RESCATES', 11, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1894', '2020', 'MayorValorActualizado', 'C10', '', 13, 'Mayor Valor Actualizado', '', 'AMOUNT', 0, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '3. SECCI�N B2: RESCATES', 12, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1894', '2020', 'MenorValorActualizado', 'C12', '', 14, 'Menor Valor Actualizado', '', 'AMOUNT', 0, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '3. SECCI�N B2: RESCATES', 13, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1894', '2020', 'MontoReinvertido', 'C14', '', 16, 'Monto Reinvertido a Valor de Liquidacion', '', 'AMOUNT', 0, '0', 'S', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '4. SECCI�N B3: LIQUIDACIONES DE CUOTAS PARA REINVERSIONES INCISO QUINTO ART. 108', 15, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1894', '2020', 'NumeroCertificado', 'C16', '', 17, 'Numero Certificado', '', 'ID', 0, '0', 'S', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '6. DATOS CERTIFICADO', 16, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1894', '2020', 'MontoHistorico', '', '', 22, 'Monto Historico', '', 'AMOUNT', 0, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '2. SECCI�N B1: INVERSIONES Y REINVERSIONES RECIBIDAS EN EL EJERCICIO', 19, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1894', '2020', 'ID', '', '', 1, 'Secuencia', '', 'ID', 0, '0', 'N', 'N', 'N', 'N', 'N', 'N', 'S', 'N', '', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1894', '2020', 'RutEmisor', 'C1', '', 3, 'Rut Emisor Instrumento', '', 'RUT', 0, '0', 'S', 'N', 'N', 'S', 'N', 'N', 'N', 'S', '1. DATOS DE LOS INFORMADOS', 2, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1894', '2020', 'IdDocumento', '', '', 23, 'Secuencia Docto', 'Ayuda de Secuencia Docto', 'ID', 0, '0', 'N', 'N', 'N', 'N', 'S', 'S', 'S', 'N', '5. DATOS DE CONTROL', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1894', '2020', 'VboUsuario', '', '', 24, 'VoBo', 'Ayuda de VoBo', 'TEXT', 1, 'N', 'N', 'S', 'S', 'N', 'N', 'N', 'S', 'N', '5. DATOS DE CONTROL', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1894', '2020', 'Observaciones', '', '', 25, 'Observaciones', 'Ayuda de Observaciones', 'TEXT', 250, '', 'N', 'S', 'S', 'N', 'N', 'N', 'S', 'N', '5. DATOS DE CONTROL', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1894', '2020', 'FuenteDatos', '', '', 26, 'Fuente', 'Ayuda de Fuente', 'TEXT', 0, 'INICIAL', 'N', 'N', 'N', 'N', 'N', 'N', 'S', 'N', '5. DATOS DE CONTROL', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1894', '2020', 'CostoUf', '', '', 18, 'Costo Uf', '', 'AMOUNT', 0, '0', 'S', 'N', 'N', 'S', 'N', 'N', 'N', 'N', '3. SECCI�N B2: RESCATES', 17, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1894', '2020', 'IdCliente', '', '', 19, 'Cliente', 'Ayuda de Secuencia Cliente', 'ID_CLIENTE', 0, '0', 'S', 'S', 'N', 'N', 'N', 'N', 'N', 'N', '5. DATOS DE CONTROL', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1894', '2020', 'IdPortafolio', '', '', 20, 'Portafolio', 'Ayuda de Secuencia Portafolio', 'ID_PORTAFOLIO', 0, '0', 'S', 'N', 'N', 'N', 'N', 'N', 'N', 'N', '5. DATOS DE CONTROL', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1894', '2020', 'FolioCartera', '', '', 21, 'FolioCartera', '', 'AMOUNT', 0, '0', 'N', 'N', 'N', 'S', 'N', 'N', 'N', 'N', '1. DATOS DE LOS INFORMADOS', 18, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1894', '2020', 'IdFondo', '', '', 27, 'Fondo', '', 'NEMO', 0, '', 'S', 'N', 'N', 'N', 'S', 'N', 'N', 'N', '1. DATOS DE LOS INFORMADOS', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1894', '2020', 'Nemo', '', '', 28, 'Nemotecnico', '', 'NEMO', 0, '', 'S', 'N', 'N', 'S', 'N', 'N', 'N', 'N', '1. DATOS DE LOS INFORMADOS', 20, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1894', '2020', 'TipoDeclarante', '', '', 30, 'TipoDeclarante', '', 'TEXT', 0, '', 'S', 'N', 'N', 'S', 'S', 'N', 'N', 'N', '1. DATOS DE LOS INFORMADOS', 21, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1894', '2020', 'Cantidad', '', '', 31, 'Cantidad', '', 'AMOUNT', 0, '0', 'N', 'N', 'N', 'S', 'S', 'N', 'N', 'N', '1. DATOS DE LOS INFORMADOS', 22, 0);


INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1894', '2021', 'Numero', 'C0', '', 2, 'Numero', '', 'ID', 0, '0', 'N', 'N', 'N', 'S', 'N', 'N', 'N', 'S', '1. DATOS DE LOS INFORMADOS', 1, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1894', '2021', 'RutParticipe', 'C3', '', 4, 'Rut Participe', '', 'RUT', 0, '0', 'S', 'N', 'N', 'S', 'N', 'N', 'N', 'N', '1. DATOS DE LOS INFORMADOS', 3, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1894', '2021', 'RutFondo', 'C23', '', 5, 'Rut del Fondo Mutuo', '', 'RUT', 0, '0', 'S', 'N', 'N', 'S', 'N', 'N', 'N', 'N', '1. DATOS DE LOS INFORMADOS', 4, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1894', '2021', 'RunFondo', 'C4', '', 6, 'Run del Fondo Mutuo', '', 'RUT', 0, '0', 'S', 'N', 'N', 'S', 'N', 'N', 'N', 'N', '1. DATOS DE LOS INFORMADOS', 5, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1894', '2021', 'FechaInversion', 'C6', '', 8, 'Fecha Inversion o Reinversion', '', 'DATE', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '2. SECCI�N B1: INVERSIONES Y REINVERSIONES RECIBIDAS EN EL EJERCICIO', 7, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1894', '2021', 'FechaOperacion', '', '', 9, 'Fecha Operacion', '', 'DATE_FILTER', 0, '', 'S', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '5. DATOS DE CONTROL', 8, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1894', '2021', 'FechaRescate', 'C8', '', 11, 'Fecha Rescate', '', 'DATE', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '3. SECCI�N B2: RESCATES', 10, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1894', '2021', 'RutAdminitradoraFondoReceptor', 'C13', '', 15, 'Rut Sociedad Administradora Fondo Receptor de la Reinversi�n', '', 'RUT', 0, '0', 'S', 'N', 'N', 'S', 'N', 'N', 'N', 'N', '4. SECCI�N B3: LIQUIDACIONES DE CUOTAS PARA REINVERSIONES INCISO QUINTO ART. 108', 14, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1894', '2021', 'IdEmisor', '', '', 29, 'Emisor', '', 'NEMO', 0, '', 'S', 'N', 'N', 'N', 'S', 'N', 'N', 'N', '1. DATOS DE LOS INFORMADOS', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1894', '2021', 'TipoOperacion', 'C5', '', 7, 'TIpo Operacion', '', 'ID', 0, '1', 'S', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '1. DATOS DE LOS INFORMADOS', 6, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1894', '2021', 'MontoNominalInversionActualizado', 'C7', '', 10, 'Monto Nominal Inversion o Reinversion (actualizado)', '', 'AMOUNT', 0, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '2. SECCI�N B1: INVERSIONES Y REINVERSIONES RECIBIDAS EN EL EJERCICIO', 9, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1894', '2021', 'MontoNominalRescate', 'C9', '', 12, 'Monto Nominal Rescate a la Fecha en que se Efectuo', '', 'AMOUNT', 0, '0', 'S', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '3. SECCI�N B2: RESCATES', 11, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1894', '2021', 'MayorValorActualizado', 'C10', '', 13, 'Mayor Valor Actualizado', '', 'AMOUNT', 0, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '3. SECCI�N B2: RESCATES', 12, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1894', '2021', 'MenorValorActualizado', 'C12', '', 14, 'Menor Valor Actualizado', '', 'AMOUNT', 0, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '3. SECCI�N B2: RESCATES', 13, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1894', '2021', 'MontoReinvertido', 'C14', '', 16, 'Monto Reinvertido a Valor de Liquidacion', '', 'AMOUNT', 0, '0', 'S', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '4. SECCI�N B3: LIQUIDACIONES DE CUOTAS PARA REINVERSIONES INCISO QUINTO ART. 108', 15, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1894', '2021', 'NumeroCertificado', 'C16', '', 17, 'Numero Certificado', '', 'ID', 0, '0', 'S', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '6. DATOS CERTIFICADO', 16, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1894', '2021', 'MontoHistorico', '', '', 22, 'Monto Historico', '', 'AMOUNT', 0, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '2. SECCI�N B1: INVERSIONES Y REINVERSIONES RECIBIDAS EN EL EJERCICIO', 19, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1894', '2021', 'ID', '', '', 1, 'Secuencia', '', 'ID', 0, '0', 'N', 'N', 'N', 'N', 'N', 'N', 'S', 'N', '', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1894', '2021', 'RutEmisor', 'C1', '', 3, 'Rut Emisor Instrumento', '', 'RUT', 0, '0', 'S', 'N', 'N', 'S', 'N', 'N', 'N', 'S', '1. DATOS DE LOS INFORMADOS', 2, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1894', '2021', 'IdDocumento', '', '', 23, 'Secuencia Docto', 'Ayuda de Secuencia Docto', 'ID', 0, '0', 'N', 'N', 'N', 'N', 'S', 'S', 'S', 'N', '5. DATOS DE CONTROL', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1894', '2021', 'VboUsuario', '', '', 24, 'VoBo', 'Ayuda de VoBo', 'TEXT', 1, 'N', 'N', 'S', 'S', 'N', 'N', 'N', 'S', 'N', '5. DATOS DE CONTROL', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1894', '2021', 'Observaciones', '', '', 25, 'Observaciones', 'Ayuda de Observaciones', 'TEXT', 250, '', 'N', 'S', 'S', 'N', 'N', 'N', 'S', 'N', '5. DATOS DE CONTROL', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1894', '2021', 'FuenteDatos', '', '', 26, 'Fuente', 'Ayuda de Fuente', 'TEXT', 0, 'INICIAL', 'N', 'N', 'N', 'N', 'N', 'N', 'S', 'N', '5. DATOS DE CONTROL', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1894', '2021', 'CostoUf', '', '', 18, 'Costo Uf', '', 'AMOUNT', 0, '0', 'S', 'N', 'N', 'S', 'N', 'N', 'N', 'N', '3. SECCI�N B2: RESCATES', 17, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1894', '2021', 'IdCliente', '', '', 19, 'Cliente', 'Ayuda de Secuencia Cliente', 'ID_CLIENTE', 0, '0', 'S', 'S', 'N', 'N', 'N', 'N', 'N', 'N', '5. DATOS DE CONTROL', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1894', '2021', 'IdPortafolio', '', '', 20, 'Portafolio', 'Ayuda de Secuencia Portafolio', 'ID_PORTAFOLIO', 0, '0', 'S', 'N', 'N', 'N', 'N', 'N', 'N', 'N', '5. DATOS DE CONTROL', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1894', '2021', 'FolioCartera', '', '', 21, 'FolioCartera', '', 'AMOUNT', 0, '0', 'N', 'N', 'N', 'S', 'N', 'N', 'N', 'N', '1. DATOS DE LOS INFORMADOS', 18, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1894', '2021', 'IdFondo', '', '', 27, 'Fondo', '', 'NEMO', 0, '', 'S', 'N', 'N', 'N', 'S', 'N', 'N', 'N', '1. DATOS DE LOS INFORMADOS', 0, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1894', '2021', 'Nemo', '', '', 28, 'Nemotecnico', '', 'NEMO', 0, '', 'S', 'N', 'N', 'S', 'N', 'N', 'N', 'N', '1. DATOS DE LOS INFORMADOS', 20, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1894', '2021', 'TipoDeclarante', '', '', 30, 'TipoDeclarante', '', 'TEXT', 0, '', 'S', 'N', 'N', 'S', 'S', 'N', 'N', 'N', '1. DATOS DE LOS INFORMADOS', 21, 0);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
VALUES('DJ1894', '2021', 'Cantidad', '', '', 31, 'Cantidad', '', 'AMOUNT', 0, '0', 'N', 'N', 'N', 'S', 'S', 'N', 'N', 'N', '1. DATOS DE LOS INFORMADOS', 22, 0);


INSERT INTO BSCL."BSCL.Normativa.BD::DOC.CampoCuadratura"
("Esquema", "Version", "TipoCuadratura", "NombreCampo", "Orden")
VALUES('DJ1894', '2020', 'PorIdFondo', 'IdFondo', 1);
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.CampoCuadratura"
("Esquema", "Version", "TipoCuadratura", "NombreCampo", "Orden")
VALUES('DJ1894', '2021', 'PorIdFondo', 'IdFondo', 1);


/* Campos Fin */

/* Servicios Inicio*/

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion", "TablasTempIN", "TablasTempOUT", "RegistraUso")
SELECT 4290713, 'BSCL.Normativa.BD.Servicios::TRI.DJ1894.GrabarMasivo', 113, 'P', '', '', '', 'S'
FROM DUMMY
WHERE NOT EXISTS (SELECT 1 FROM BSCL."BSCL.Plataforma.BD::PLA.Servicio" WHERE "IdServicio"=4290713);

/* Servicios Fin*/


--INSERT FALTANTE PARA GENERACION DE CERTIFICADO 21
INSERT INTO BSCL."BSCL.Plataforma.BD::BAT.TipoPrograma"(
"IdTipoPrograma","Descripcion","MaximoIntentos","Modulo","NombreClasePrograma","TipoClasePrograma","Nodo","EsPauta","TipoSalida","FormatoSalida","TieneLog")
SELECT 'GenerarCertificado21' ,'Generar Certificado 21' ,1,'Tributario','cl.bcs.normativa.complementos.BcsJobGenerarCertificado21' ,'JAVA','','N','P','','N' 
FROM DUMMY
WHERE NOT EXISTS (SELECT 1 FROM BSCL."BSCL.Plataforma.BD::BAT.TipoPrograma" WHERE "IdTipoPrograma" = 'GenerarCertificado21');



/* DELETE A HOMOLOGACION*/
DELETE FROM BSCL."BSCL.Normativa.BD::DOC.Homologacion" WHERE "Esquema" = 'DJ1894' AND "Version" = 2021 AND "Tipo"='Tipo Operacion';

/* INSERT A HOMOLOGACION*/
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES(38637, 'DJ1894', '2021', 'Tipo Operacion', '', 'RESNORM-S-APODIV', '6', 'VIG');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES(38638, 'DJ1894', '2021', 'Tipo Operacion', '', 'RESNORM-S-APONORM', '6', 'VIG');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES(38639, 'DJ1894', '2021', 'Tipo Operacion', '', 'RESNORM-S-APOREPRO', '6', 'VIG');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES(38640, 'DJ1894', '2021', 'Tipo Operacion', '', 'RESNORM-N-APODIV', '6', 'VIG');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES(38641, 'DJ1894', '2021', 'Tipo Operacion', '', 'RESNORM-N-APONORM', '6', 'VIG');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES(38642, 'DJ1894', '2021', 'Tipo Operacion', '', 'RESNORM-N-APOREPRO', '6', 'VIG');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES(38643, 'DJ1894', '2021', 'Tipo Operacion', '', 'RESNORM-S-APOREINV', '7', 'VIG');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES(38644, 'DJ1894', '2021', 'Tipo Operacion', '', 'RESNORM-N-APOREINV', '7', 'VIG');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES(38647, 'DJ1894', '2021', 'Tipo Operacion', '', 'RESCDF-N-APODIV', '8', 'VIG');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES(38648, 'DJ1894', '2021', 'Tipo Operacion', '', 'RESCDF-S-APODIV', '8', 'VIG');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES(38649, 'DJ1894', '2021', 'Tipo Operacion', '', 'APONORM', '1', 'VIG');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES(38650, 'DJ1894', '2021', 'Tipo Operacion', '', 'APORESPRO', '1', 'VIG');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES(38651, 'DJ1894', '2021', 'Tipo Operacion', '', 'APOREINV', '3', 'VIG');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES(38652, 'DJ1894', '2021', 'Tipo Operacion', '', 'APOCANJE', '1', 'VIG');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES(38653, 'DJ1894', '2021', 'Tipo Operacion', '', 'APOCDF', '1', 'VIG');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES(38654, 'DJ1894', '2021', 'Tipo Operacion', '', 'APODIV', '1', 'VIG');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES(38655, 'DJ1894', '2021', 'Tipo Operacion', '', 'APODIVCFI', '1', 'VIG');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES(38656, 'DJ1894', '2021', 'Tipo Operacion', '', 'APOFUSION', '1', 'VIG');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES(38657, 'DJ1894', '2021', 'Tipo Operacion', '', 'APOREGULA', '1', 'VIG');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES(38658, 'DJ1894', '2021', 'Tipo Operacion', '', 'APOREPRO', '1', 'VIG');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES(38659, 'DJ1894', '2021', 'Tipo Operacion', '', 'APOTRASP', '1', 'VIG');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES(38660, 'DJ1894', '2021', 'Tipo Operacion', '', 'INGGTA', '1', 'VIG');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES(38661, 'DJ1894', '2021', 'Tipo Operacion', '', 'INGTIT', '1', 'VIG');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES(38662, 'DJ1894', '2021', 'Tipo Operacion', '', 'RESREINV-N-APONORM', '8', 'VIG');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES(38663, 'DJ1894', '2021', 'Tipo Operacion', '', 'RESREINV-S-APONORM', '6', 'VIG');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES(38664, 'DJ1894', '2021', 'Tipo Operacion', '', 'RESREINV-S-APORESPRO', '6', 'VIG');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES(38665, 'DJ1894', '2021', 'Tipo Operacion', '', 'RESREINV-N-APORESPRO', '8', 'VIG');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES(38667, 'DJ1894', '2021', 'Tipo Operacion', '', 'RESREINV-S-APOOVER', '6', 'VIG');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES(38668, 'DJ1894', '2021', 'Tipo Operacion', '', 'RESREINV-N-APOOVER', '8', 'VIG');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES(38669, 'DJ1894', '2021', 'Tipo Operacion', '', 'RESCDF-N-APONORM', '8', 'VIG');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES(38670, 'DJ1894', '2021', 'Tipo Operacion', '', 'RESCDF-S-APONORM', '8', 'VIG');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES(38671, 'DJ1894', '2021', 'Tipo Operacion', '', 'RESCDF-S-APORESPRO', '8', 'VIG');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES(38672, 'DJ1894', '2021', 'Tipo Operacion', '', 'RESCDF-N-APORESPRO', '8', 'VIG');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES(38673, 'DJ1894', '2021', 'Tipo Operacion', '', 'RESCDF-N-APOOVER', '8', 'VIG');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES(38674, 'DJ1894', '2021', 'Tipo Operacion', '', 'RESCDF-S-APOOVER', '8', 'VIG');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES(38675, 'DJ1894', '2021', 'Tipo Operacion', '', 'RESCDF-S-APOREINV', '9', 'VIG');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES(38676, 'DJ1894', '2021', 'Tipo Operacion', '', 'RESCDF-N-APOREINV', '9', 'VIG');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES(38677, 'DJ1894', '2021', 'Tipo Operacion', '', 'RESREINV-N-APOREINV', '9', 'VIG');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES(38678, 'DJ1894', '2021', 'Tipo Operacion', '', 'RESREINV-S-APOREINV', '7', 'VIG');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES(38679, 'DJ1894', '2021', 'Tipo Operacion', '', 'RESNORM-S-APOCANJE', '6', 'VIG');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES(38680, 'DJ1894', '2021', 'Tipo Operacion', '', 'RESNORM-N-APOCANJE', '6', 'VIG');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES(38681, 'DJ1894', '2021', 'Tipo Operacion', '', 'RESNORM-N-APODIVCFI', '6', 'VIG');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES(38682, 'DJ1894', '2021', 'Tipo Operacion', '', 'RESNORM-S-APODIVCFI', '6', 'VIG');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES(38683, 'DJ1894', '2021', 'Tipo Operacion', '', 'RESNORM-S-APOFUSION', '6', 'VIG');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES(38684, 'DJ1894', '2021', 'Tipo Operacion', '', 'RESNORM-N-APOFUSION', '6', 'VIG');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES(38685, 'DJ1894', '2021', 'Tipo Operacion', '', 'RESNORM-N-APOREGULA', '6', 'VIG');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES(38686, 'DJ1894', '2021', 'Tipo Operacion', '', 'RESNORM-S-APOREGULA', '6', 'VIG');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES(38687, 'DJ1894', '2021', 'Tipo Operacion', '', 'RESNORM-S-APOTRASP', '6', 'VIG');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES(38688, 'DJ1894', '2021', 'Tipo Operacion', '', 'RESNORM-N-APOTRASP', '6', 'VIG');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES(38689, 'DJ1894', '2021', 'Tipo Operacion', '', 'RESNORM-N-INGGTA', '6', 'VIG');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES(38690, 'DJ1894', '2021', 'Tipo Operacion', '', 'RESNORM-S-INGGTA', '6', 'VIG');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES(38691, 'DJ1894', '2021', 'Tipo Operacion', '', 'RESNORM-S-INGTIT', '6', 'VIG');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES(38692, 'DJ1894', '2021', 'Tipo Operacion', '', 'RESNORM-N-INGTIT', '6', 'VIG');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES(38693, 'DJ1894', '2021', 'Tipo Operacion', '', 'RESREINV-S-APOCANJE', '6', 'VIG');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES(38694, 'DJ1894', '2021', 'Tipo Operacion', '', 'RESREINV-N-APOCANJE', '8', 'VIG');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES(38695, 'DJ1894', '2021', 'Tipo Operacion', '', 'RESREINV-S-APOCDF', '7', 'VIG');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES(38696, 'DJ1894', '2021', 'Tipo Operacion', '', 'RESREINV-N-APOCDF', '9', 'VIG');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES(38697, 'DJ1894', '2021', 'Tipo Operacion', '', 'RESREINV-S-APODIV', '6', 'VIG');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES(38698, 'DJ1894', '2021', 'Tipo Operacion', '', 'RESREINV-N-APODIV', '8', 'VIG');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES(38699, 'DJ1894', '2021', 'Tipo Operacion', '', 'RESREINV-S-APODIVCFI', '6', 'VIG');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES(38700, 'DJ1894', '2021', 'Tipo Operacion', '', 'RESREINV-N-APODIVCFI', '8', 'VIG');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES(38701, 'DJ1894', '2021', 'Tipo Operacion', '', 'RESREINV-S-APOFUSION', '6', 'VIG');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES(38702, 'DJ1894', '2021', 'Tipo Operacion', '', 'RESREINV-N-APOFUSION', '8', 'VIG');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES(38703, 'DJ1894', '2021', 'Tipo Operacion', '', 'RESREINV-S-APOREGULA', '6', 'VIG');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES(38704, 'DJ1894', '2021', 'Tipo Operacion', '', 'RESREINV-N-APOREGULA', '8', 'VIG');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES(38705, 'DJ1894', '2021', 'Tipo Operacion', '', 'RESREINV-S-APOTRASP', '6', 'VIG');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES(38706, 'DJ1894', '2021', 'Tipo Operacion', '', 'RESREINV-N-APOTRASP', '8', 'VIG');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES(38707, 'DJ1894', '2021', 'Tipo Operacion', '', 'RESREINV-S-INGGTA', '6', 'VIG');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES(38708, 'DJ1894', '2021', 'Tipo Operacion', '', 'RESREINV-N-INGGTA', '8', 'VIG');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES(38709, 'DJ1894', '2021', 'Tipo Operacion', '', 'RESREINV-S-INGTIT', '6', 'VIG');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES(38710, 'DJ1894', '2021', 'Tipo Operacion', '', 'RESREINV-N-INGTIT', '8', 'VIG');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES(38711, 'DJ1894', '2021', 'Tipo Operacion', '', 'RESCDF-S-APOCANJE', '8', 'VIG');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES(38712, 'DJ1894', '2021', 'Tipo Operacion', '', 'RESCDF-N-APOCANJE', '8', 'VIG');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES(38713, 'DJ1894', '2021', 'Tipo Operacion', '', 'RESCDF-S-APOCDF', '9', 'VIG');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES(38714, 'DJ1894', '2021', 'Tipo Operacion', '', 'RESCDF-N-APOCDF', '9', 'VIG');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES(38715, 'DJ1894', '2021', 'Tipo Operacion', '', 'RESCDF-S-APODIVCDF', '8', 'VIG');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES(38716, 'DJ1894', '2021', 'Tipo Operacion', '', 'RESCDF-N-APODIVCDF', '8', 'VIG');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES(38717, 'DJ1894', '2021', 'Tipo Operacion', '', 'RESCDF-S-APOFUSION', '8', 'VIG');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES(38718, 'DJ1894', '2021', 'Tipo Operacion', '', 'RESCDF-N-APOFUSION', '8', 'VIG');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES(38719, 'DJ1894', '2021', 'Tipo Operacion', '', 'RESCDF-S-APOREGULA', '8', 'VIG');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES(38720, 'DJ1894', '2021', 'Tipo Operacion', '', 'RESCDF-N-APOREGULA', '8', 'VIG');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES(38721, 'DJ1894', '2021', 'Tipo Operacion', '', 'RESCDF-S-APOTRASP', '8', 'VIG');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES(38722, 'DJ1894', '2021', 'Tipo Operacion', '', 'RESCDF-N-APOTRASP', '8', 'VIG');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES(38723, 'DJ1894', '2021', 'Tipo Operacion', '', 'RESCDF-S-INGGTA', '8', 'VIG');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES(38724, 'DJ1894', '2021', 'Tipo Operacion', '', 'RESCDF-N-INGGTA', '8', 'VIG');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES(38725, 'DJ1894', '2021', 'Tipo Operacion', '', 'RESCDF-S-INGTIT', '8', 'VIG');

INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Homologacion"
("IdHomologacion", "Esquema", "Version", "Tipo", "Descripcion", "ValorOrigen", "ValorDestino", "IdEstado")
VALUES(38726, 'DJ1894', '2021', 'Tipo Operacion', '', 'RESCDF-N-INGTIT', '8', 'VIG');
