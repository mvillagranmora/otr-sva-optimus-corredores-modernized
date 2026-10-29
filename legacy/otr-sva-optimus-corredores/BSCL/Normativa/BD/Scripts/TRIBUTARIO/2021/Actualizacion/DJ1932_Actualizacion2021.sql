/*** Se inscriben aca actualizaciones a la version BASE 2020 + 2021 ***/
/*
 *   consideracion :
 *    
 *   - insert o update es para ambas versiones (obligatorio actualizar script en :
 *   - /Normativa/BD/Scripts/TRIBUTARIO/2021/DJ1932.sql  
 * 
 */

-- Ejecutar ALTER TABLE solo una vez y de forma manual , sedeja acá como respaldo
 ALTER TABLE BSCL."BSCL.Normativa.BD::TRI.DatosDJ1932" ADD ("CodPaisRenta" VARCHAR(40) DEFAULT '' NOT NULL) 


-- ACTUALIZACION CAMPOS EXISTENTES Version 2020
UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo"
SET "CodigoSII"='', "TipoDiccionario"='', "Orden"=1, "Titulo"='Secuencia', "Ayuda"='', "TipoDato"='ID', "Largo"=0, "ValorDefecto"='0', "Obligatorio"='S', "Editable"='N', "ModoMasivo"='N', "Exportable"='N', "OcultoEnGrilla"='N', "OcultoEnMantenedor"='N', "Comun"='N', "Clave"='N', "Grupo"='', "OrdenCargaExterna"=0, "CantidadDecimales"=0
WHERE "Esquema"='DJ1932' AND "Version"='2020' AND "Nombre"='ID';
UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo"
SET "CodigoSII"='C1', "TipoDiccionario"='', "Orden"=2, "Titulo"='Numero', "Ayuda"='', "TipoDato"='ID', "Largo"=0, "ValorDefecto"='0', "Obligatorio"='N', "Editable"='N', "ModoMasivo"='N', "Exportable"='S', "OcultoEnGrilla"='N', "OcultoEnMantenedor"='N', "Comun"='N', "Clave"='S', "Grupo"='6. DATOS DE CONTROL', "OrdenCargaExterna"=1, "CantidadDecimales"=0
WHERE "Esquema"='DJ1932' AND "Version"='2020' AND "Nombre"='Numero';
UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo"
SET "CodigoSII"='', "TipoDiccionario"='', "Orden"=2, "Titulo"='Secuencia Docto', "Ayuda"='Ayuda de Secuencia Docto', "TipoDato"='ID', "Largo"=0, "ValorDefecto"='0', "Obligatorio"='S', "Editable"='N', "ModoMasivo"='N', "Exportable"='N', "OcultoEnGrilla"='S', "OcultoEnMantenedor"='S', "Comun"='S', "Clave"='N', "Grupo"='2. Control', "OrdenCargaExterna"=0, "CantidadDecimales"=0
WHERE "Esquema"='DJ1932' AND "Version"='2020' AND "Nombre"='IdDocumento';
UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo"
SET "CodigoSII"='C3', "TipoDiccionario"='', "Orden"=3, "Titulo"='MES', "Ayuda"='', "TipoDato"='ID', "Largo"=0, "ValorDefecto"='0', "Obligatorio"='N', "Editable"='S', "ModoMasivo"='N', "Exportable"='S', "OcultoEnGrilla"='N', "OcultoEnMantenedor"='N', "Comun"='N', "Clave"='S', "Grupo"='1. DATOS DE LOS INFORMADOS (Beneficiarios de la Renta) Y MONTO DE LAS OPERACIONES Y RENTAS', "OrdenCargaExterna"=2, "CantidadDecimales"=0
WHERE "Esquema"='DJ1932' AND "Version"='2020' AND "Nombre"='Mes';
UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo"
SET "CodigoSII"='C4', "TipoDiccionario"='', "Orden"=4, "Titulo"='RUT DEPOSITANTE, INVERSIONISTA O PRESTAMISTA', "Ayuda"='', "TipoDato"='RUT', "Largo"=0, "ValorDefecto"='', "Obligatorio"='N', "Editable"='S', "ModoMasivo"='N', "Exportable"='S', "OcultoEnGrilla"='N', "OcultoEnMantenedor"='N', "Comun"='N', "Clave"='S', "Grupo"='2. ANTECEDENTES DE LOS DEPOSITANTES, INVERSIONISTAS O PRESTAMISTAS', "OrdenCargaExterna"=3, "CantidadDecimales"=0
WHERE "Esquema"='DJ1932' AND "Version"='2020' AND "Nombre"='RutDepositanteInversionistaPrestamista';
UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo"
SET "CodigoSII"='C5', "TipoDiccionario"='', "Orden"=5, "Titulo"='TAX-ID DEL DEPOSITANTE, INVERSIONISTA O PRESTAMISTA', "Ayuda"='', "TipoDato"='RUT', "Largo"=0, "ValorDefecto"='', "Obligatorio"='N', "Editable"='S', "ModoMasivo"='N', "Exportable"='S', "OcultoEnGrilla"='N', "OcultoEnMantenedor"='N', "Comun"='N', "Clave"='N', "Grupo"='2. ANTECEDENTES DE LOS DEPOSITANTES, INVERSIONISTAS O PRESTAMISTAS', "OrdenCargaExterna"=4, "CantidadDecimales"=0
WHERE "Esquema"='DJ1932' AND "Version"='2020' AND "Nombre"='TaxID';
UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo"
SET "CodigoSII"='C6', "TipoDiccionario"='', "Orden"=6, "Titulo"='PAIS', "Ayuda"='', "TipoDato"='NEMO', "Largo"=0, "ValorDefecto"='', "Obligatorio"='N', "Editable"='S', "ModoMasivo"='N', "Exportable"='S', "OcultoEnGrilla"='N', "OcultoEnMantenedor"='N', "Comun"='N', "Clave"='N', "Grupo"='2. ANTECEDENTES DE LOS DEPOSITANTES, INVERSIONISTAS O PRESTAMISTAS', "OrdenCargaExterna"=5, "CantidadDecimales"=0
WHERE "Esquema"='DJ1932' AND "Version"='2020' AND "Nombre"='Pais';
UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo"
SET "CodigoSII"='C7', "TipoDiccionario"='', "Orden"=7, "Titulo"='DEPOSITOS, INVERSIONES O PRESTAMOS', "Ayuda"='', "TipoDato"='AMOUNT', "Largo"=0, "ValorDefecto"='0', "Obligatorio"='N', "Editable"='S', "ModoMasivo"='N', "Exportable"='S', "OcultoEnGrilla"='N', "OcultoEnMantenedor"='N', "Comun"='N', "Clave"='N', "Grupo"='3. MONTO DE LAS OPERACIONES', "OrdenCargaExterna"=6, "CantidadDecimales"=0
WHERE "Esquema"='DJ1932' AND "Version"='2020' AND "Nombre"='DepositosInversionesPrestamos';
UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo"
SET "CodigoSII"='C8', "TipoDiccionario"='', "Orden"=8, "Titulo"='RESCATES O DEVOLUCIONES', "Ayuda"='', "TipoDato"='AMOUNT', "Largo"=0, "ValorDefecto"='0', "Obligatorio"='N', "Editable"='S', "ModoMasivo"='N', "Exportable"='S', "OcultoEnGrilla"='N', "OcultoEnMantenedor"='N', "Comun"='N', "Clave"='N', "Grupo"='3. MONTO DE LAS OPERACIONES', "OrdenCargaExterna"=7, "CantidadDecimales"=0
WHERE "Esquema"='DJ1932' AND "Version"='2020' AND "Nombre"='RescatesDevoluciones';
UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo"
SET "CodigoSII"='C9', "TipoDiccionario"='', "Orden"=9, "Titulo"='INTERESES', "Ayuda"='', "TipoDato"='AMOUNT', "Largo"=0, "ValorDefecto"='0', "Obligatorio"='N', "Editable"='S', "ModoMasivo"='N', "Exportable"='S', "OcultoEnGrilla"='N', "OcultoEnMantenedor"='N', "Comun"='N', "Clave"='N', "Grupo"='4. PERCIBIDAS', "OrdenCargaExterna"=8, "CantidadDecimales"=0
WHERE "Esquema"='DJ1932' AND "Version"='2020' AND "Nombre"='InteresesPercibidas';
UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo"
SET "CodigoSII"='C10', "TipoDiccionario"='', "Orden"=10, "Titulo"='DIVIDENDOS', "Ayuda"='', "TipoDato"='AMOUNT', "Largo"=0, "ValorDefecto"='0', "Obligatorio"='N', "Editable"='S', "ModoMasivo"='N', "Exportable"='S', "OcultoEnGrilla"='N', "OcultoEnMantenedor"='N', "Comun"='N', "Clave"='N', "Grupo"='4. PERCIBIDAS', "OrdenCargaExterna"=9, "CantidadDecimales"=0
WHERE "Esquema"='DJ1932' AND "Version"='2020' AND "Nombre"='DividendosPercibidas';
UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo"
SET "CodigoSII"='C11', "TipoDiccionario"='', "Orden"=11, "Titulo"='CONTRATOS POR DIFERENCIAS', "Ayuda"='', "TipoDato"='AMOUNT', "Largo"=0, "ValorDefecto"='0', "Obligatorio"='N', "Editable"='S', "ModoMasivo"='N', "Exportable"='S', "OcultoEnGrilla"='N', "OcultoEnMantenedor"='N', "Comun"='N', "Clave"='N', "Grupo"='4. PERCIBIDAS', "OrdenCargaExterna"=10, "CantidadDecimales"=0
WHERE "Esquema"='DJ1932' AND "Version"='2020' AND "Nombre"='ContratosPorDiferenciaPercibidas';
UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo"
SET "CodigoSII"='C12', "TipoDiccionario"='', "Orden"=12, "Titulo"='OTRAS RENTAS', "Ayuda"='', "TipoDato"='AMOUNT', "Largo"=0, "ValorDefecto"='0', "Obligatorio"='N', "Editable"='S', "ModoMasivo"='N', "Exportable"='S', "OcultoEnGrilla"='N', "OcultoEnMantenedor"='N', "Comun"='N', "Clave"='N', "Grupo"='4. PERCIBIDAS', "OrdenCargaExterna"=11, "CantidadDecimales"=0
WHERE "Esquema"='DJ1932' AND "Version"='2020' AND "Nombre"='OtrasRentasPercibidas';
UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo"
SET "CodigoSII"='C13', "TipoDiccionario"='', "Orden"=13, "Titulo"='INTERESES', "Ayuda"='', "TipoDato"='AMOUNT', "Largo"=0, "ValorDefecto"='0', "Obligatorio"='N', "Editable"='S', "ModoMasivo"='N', "Exportable"='S', "OcultoEnGrilla"='N', "OcultoEnMantenedor"='N', "Comun"='N', "Clave"='N', "Grupo"='5. DEVENGADAS', "OrdenCargaExterna"=12, "CantidadDecimales"=0
WHERE "Esquema"='DJ1932' AND "Version"='2020' AND "Nombre"='InteresesDevengadas';
UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo"
SET "CodigoSII"='C14', "TipoDiccionario"='', "Orden"=14, "Titulo"='OTRAS RENTAS', "Ayuda"='', "TipoDato"='AMOUNT', "Largo"=0, "ValorDefecto"='0', "Obligatorio"='N', "Editable"='S', "ModoMasivo"='N', "Exportable"='S', "OcultoEnGrilla"='N', "OcultoEnMantenedor"='N', "Comun"='N', "Clave"='N', "Grupo"='5. DEVENGADAS', "OrdenCargaExterna"=13, "CantidadDecimales"=0
WHERE "Esquema"='DJ1932' AND "Version"='2020' AND "Nombre"='OtrasRentasDevengadas';
UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo"
SET "CodigoSII"='', "TipoDiccionario"='', "Orden"=16, "Titulo"='VoBo', "Ayuda"='Ayuda de VoBo', "TipoDato"='TEXT', "Largo"=1, "ValorDefecto"='N', "Obligatorio"='N', "Editable"='S', "ModoMasivo"='S', "Exportable"='N', "OcultoEnGrilla"='N', "OcultoEnMantenedor"='N', "Comun"='S', "Clave"='N', "Grupo"='6. DATOS DE CONTROL', "OrdenCargaExterna"=0, "CantidadDecimales"=0
WHERE "Esquema"='DJ1932' AND "Version"='2020' AND "Nombre"='VboUsuario';
UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo"
SET "CodigoSII"='', "TipoDiccionario"='', "Orden"=17, "Titulo"='Observaciones', "Ayuda"='Ayuda de Observaciones', "TipoDato"='TEXT', "Largo"=250, "ValorDefecto"='', "Obligatorio"='N', "Editable"='S', "ModoMasivo"='S', "Exportable"='N', "OcultoEnGrilla"='N', "OcultoEnMantenedor"='N', "Comun"='S', "Clave"='N', "Grupo"='6. DATOS DE CONTROL', "OrdenCargaExterna"=0, "CantidadDecimales"=0
WHERE "Esquema"='DJ1932' AND "Version"='2020' AND "Nombre"='Observaciones';
UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo"
SET "CodigoSII"='', "TipoDiccionario"='', "Orden"=18, "Titulo"='Fuente', "Ayuda"='Ayuda de Fuente', "TipoDato"='TEXT', "Largo"=0, "ValorDefecto"='INICIAL', "Obligatorio"='N', "Editable"='N', "ModoMasivo"='N', "Exportable"='N', "OcultoEnGrilla"='N', "OcultoEnMantenedor"='N', "Comun"='S', "Clave"='N', "Grupo"='6. DATOS DE CONTROL', "OrdenCargaExterna"=0, "CantidadDecimales"=0
WHERE "Esquema"='DJ1932' AND "Version"='2020' AND "Nombre"='FuenteDatos';
UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo"
SET "CodigoSII"='', "TipoDiccionario"='', "Orden"=19, "Titulo"='Secuencia Cliente', "Ayuda"='Ayuda de Secuencia Cliente', "TipoDato"='ID_CLIENTE', "Largo"=0, "ValorDefecto"='0', "Obligatorio"='S', "Editable"='S', "ModoMasivo"='N', "Exportable"='N', "OcultoEnGrilla"='N', "OcultoEnMantenedor"='N', "Comun"='N', "Clave"='N', "Grupo"='6. DATOS DE CONTROL', "OrdenCargaExterna"=0, "CantidadDecimales"=0
WHERE "Esquema"='DJ1932' AND "Version"='2020' AND "Nombre"='IdCliente';
UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo"
SET "CodigoSII"='', "TipoDiccionario"='', "Orden"=20, "Titulo"='Fecha Operacion', "Ayuda"='', "TipoDato"='DATE_FILTER', "Largo"=0, "ValorDefecto"='0', "Obligatorio"='S', "Editable"='S', "ModoMasivo"='N', "Exportable"='S', "OcultoEnGrilla"='N', "OcultoEnMantenedor"='N', "Comun"='N', "Clave"='S', "Grupo"='1. DATOS DE LOS INFORMADOS (Beneficiarios de la Renta) Y MONTO DE LAS OPERACIONES Y RENTAS', "OrdenCargaExterna"=15, "CantidadDecimales"=0
WHERE "Esquema"='DJ1932' AND "Version"='2020' AND "Nombre"='FechaOperacion';

-- NUEVO CAMPO
INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
SELECT 'DJ1932', '2020', 'CodPaisRenta', 'C24', '', 15, 'Codigo Pais Renta', 'Ayuda Codigo Pais Renta', 'TEXT', 250, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '5. DEVENGADAS', 14, 0
FROM DUMMY 
WHERE NOT EXISTS (SELECT 1 FROM BSCL."BSCL.Normativa.BD::DOC.Campo" WHERE "Esquema"='DJ1932' AND "Version"='2020' AND "Nombre"='CodPaisRenta');

-- Grabar Masivo
INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion", "TablasTempIN", "TablasTempOUT", "RegistraUso")
SELECT 4290716, 'BSCL.Normativa.BD.Servicios::TRI.DJ1932.GrabarMasivo', 0, 'P', '', '', '', 'S'
FROM DUMMY 
WHERE NOT EXISTS (SELECT 1 FROM BSCL."BSCL.Plataforma.BD::PLA.Servicio" WHERE "IdServicio"=4290716);

-- Valida si existe el periodo 2021 para el esquema de la DJ1932
DO BEGIN
	IF (SELECT COUNT(*) FROM BSCL."BSCL.Normativa.BD::DOC.Esquema" WHERE "Esquema" = 'DJ1932' AND "Version" = '2021') = 1 THEN
	
		-- 2021
		DELETE FROM "BSCL.Normativa.BD::DOC.Campo" WHERE "Esquema"='DJ1932' AND "Version"='2021';
		
		INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
		("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
		VALUES('DJ1932', '2021', 'ID', '', '', 1, 'Secuencia', '', 'ID', 0, '0', 'S', 'N', 'N', 'N', 'N', 'N', 'N', 'N', '', 0, 0);
		INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
		("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
		VALUES('DJ1932', '2021', 'Numero', 'C1', '', 2, 'Numero', '', 'ID', 0, '0', 'N', 'N', 'N', 'S', 'N', 'N', 'N', 'S', '6. DATOS DE CONTROL', 1, 0);
		INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
		("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
		VALUES('DJ1932', '2021', 'IdDocumento', '', '', 2, 'Secuencia Docto', 'Ayuda de Secuencia Docto', 'ID', 0, '0', 'S', 'N', 'N', 'N', 'S', 'S', 'S', 'N', '2. Control', 0, 0);
		INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
		("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
		VALUES('DJ1932', '2021', 'Mes', 'C3', '', 3, 'MES', '', 'ID', 0, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'S', '1. DATOS DE LOS INFORMADOS (Beneficiarios de la Renta) Y MONTO DE LAS OPERACIONES Y RENTAS', 2, 0);
		INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
		("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
		VALUES('DJ1932', '2021', 'RutDepositanteInversionistaPrestamista', 'C4', '', 4, 'RUT DEPOSITANTE, INVERSIONISTA O PRESTAMISTA', '', 'RUT', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'S', '2. ANTECEDENTES DE LOS DEPOSITANTES, INVERSIONISTAS O PRESTAMISTAS', 3, 0);
		INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
		("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
		VALUES('DJ1932', '2021', 'TaxID', 'C5', '', 5, 'TAX-ID DEL DEPOSITANTE, INVERSIONISTA O PRESTAMISTA', '', 'RUT', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '2. ANTECEDENTES DE LOS DEPOSITANTES, INVERSIONISTAS O PRESTAMISTAS', 4, 0);
		INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
		("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
		VALUES('DJ1932', '2021', 'Pais', 'C6', '', 6, 'PAIS', '', 'NEMO', 0, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '2. ANTECEDENTES DE LOS DEPOSITANTES, INVERSIONISTAS O PRESTAMISTAS', 5, 0);
		INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
		("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
		VALUES('DJ1932', '2021', 'DepositosInversionesPrestamos', 'C7', '', 7, 'DEPOSITOS, INVERSIONES O PRESTAMOS', '', 'AMOUNT', 0, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '3. MONTO DE LAS OPERACIONES', 6, 0);
		INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
		("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
		VALUES('DJ1932', '2021', 'RescatesDevoluciones', 'C8', '', 8, 'RESCATES O DEVOLUCIONES', '', 'AMOUNT', 0, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '3. MONTO DE LAS OPERACIONES', 7, 0);
		INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
		("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
		VALUES('DJ1932', '2021', 'InteresesPercibidas', 'C9', '', 9, 'INTERESES', '', 'AMOUNT', 0, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '4. PERCIBIDAS', 8, 0);
		INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
		("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
		VALUES('DJ1932', '2021', 'DividendosPercibidas', 'C10', '', 10, 'DIVIDENDOS', '', 'AMOUNT', 0, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '4. PERCIBIDAS', 9, 0);
		INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
		("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
		VALUES('DJ1932', '2021', 'ContratosPorDiferenciaPercibidas', 'C11', '', 11, 'CONTRATOS POR DIFERENCIAS', '', 'AMOUNT', 0, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '4. PERCIBIDAS', 10, 0);
		INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
		("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
		VALUES('DJ1932', '2021', 'OtrasRentasPercibidas', 'C12', '', 12, 'OTRAS RENTAS', '', 'AMOUNT', 0, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '4. PERCIBIDAS', 11, 0);
		INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
		("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
		VALUES('DJ1932', '2021', 'InteresesDevengadas', 'C13', '', 13, 'INTERESES', '', 'AMOUNT', 0, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '5. DEVENGADAS', 12, 0);
		INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
		("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
		VALUES('DJ1932', '2021', 'OtrasRentasDevengadas', 'C14', '', 14, 'OTRAS RENTAS', '', 'AMOUNT', 0, '0', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '5. DEVENGADAS', 13, 0);
		INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
		("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
		VALUES('DJ1932', '2021', 'CodPaisRenta', 'C24', '', 15, 'Codigo Pais Renta', 'Ayuda Codigo Pais Renta', 'TEXT', 250, '', 'N', 'S', 'N', 'S', 'N', 'N', 'N', 'N', '5. DEVENGADAS', 14, 0);
		INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
		("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
		VALUES('DJ1932', '2021', 'VboUsuario', '', '', 16, 'VoBo', 'Ayuda de VoBo', 'TEXT', 1, 'N', 'N', 'S', 'S', 'N', 'N', 'N', 'S', 'N', '6. DATOS DE CONTROL', 0, 0);
		INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
		("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
		VALUES('DJ1932', '2021', 'Observaciones', '', '', 17, 'Observaciones', 'Ayuda de Observaciones', 'TEXT', 250, '', 'N', 'S', 'S', 'N', 'N', 'N', 'S', 'N', '6. DATOS DE CONTROL', 0, 0);
		INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
		("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
		VALUES('DJ1932', '2021', 'FuenteDatos', '', '', 18, 'Fuente', 'Ayuda de Fuente', 'TEXT', 0, 'INICIAL', 'N', 'N', 'N', 'N', 'N', 'N', 'S', 'N', '6. DATOS DE CONTROL', 0, 0);
		INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
		("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
		VALUES('DJ1932', '2021', 'IdCliente', '', '', 19, 'Secuencia Cliente', 'Ayuda de Secuencia Cliente', 'ID_CLIENTE', 0, '0', 'S', 'S', 'N', 'N', 'N', 'N', 'N', 'N', '6. DATOS DE CONTROL', 0, 0);
		INSERT INTO BSCL."BSCL.Normativa.BD::DOC.Campo"
		("Esquema", "Version", "Nombre", "CodigoSII", "TipoDiccionario", "Orden", "Titulo", "Ayuda", "TipoDato", "Largo", "ValorDefecto", "Obligatorio", "Editable", "ModoMasivo", "Exportable", "OcultoEnGrilla", "OcultoEnMantenedor", "Comun", "Clave", "Grupo", "OrdenCargaExterna", "CantidadDecimales")
		VALUES('DJ1932', '2021', 'FechaOperacion', '', '', 20, 'Fecha Operacion', '', 'DATE_FILTER', 0, '0', 'S', 'S', 'N', 'S', 'N', 'N', 'N', 'S', '1. DATOS DE LOS INFORMADOS (Beneficiarios de la Renta) Y MONTO DE LAS OPERACIONES Y RENTAS', 15, 0);

	END IF;
END;