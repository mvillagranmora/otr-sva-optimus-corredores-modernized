-- Marcelo Rojas
-- CELBUHOPRO-909
-- Script para insertar Servicio GrabarDetalle DJ1946
 -- Script para insertar Servicio EliminarDetalles DJ1946

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion", "TablasTempIN", "TablasTempOUT", "RegistraUso")
VALUES(6123, 'BSCL.Normativa.BD.Servicios::TRI.DJ1946.EliminarDetalles', 0, 'P', '', '', '', 'S'
FROM DUMMY 
WHERE NOT EXISTS (SELECT 1 FROM BSCL."BSCL.Plataforma.BD::PLA.Servicio" WHERE "IdServicio"=6123);

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion", "TablasTempIN", "TablasTempOUT", "RegistraUso")
SELECT 4306010, 'BSCL.Normativa.BD.Servicios::TRI.DJ1946.GrabarDetalle', 0, 'P', '', '', '', 'S'
FROM DUMMY 
WHERE NOT EXISTS (SELECT 1 FROM BSCL."BSCL.Plataforma.BD::PLA.Servicio" WHERE "IdServicio"=4306010);

UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo"
SET "CodigoSII"='C18', "TipoDiccionario"='', "Orden"=19, "Titulo"='PagadorRut', "Ayuda"='', "TipoDato"='RUT', "Largo"=0, "ValorDefecto"='0', "Obligatorio"='S', "Editable"='S', "ModoMasivo"='N', "Exportable"='S', "OcultoEnGrilla"='N', "OcultoEnMantenedor"='N', "Comun"='N', "Clave"='N', "Grupo"='1. Transacciones informadas', "OrdenCargaExterna"=18, "CantidadDecimales"=0
WHERE "Esquema"='DJ1946' AND "Version"='2022' AND "Nombre"='PagadorRut';

UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo"
SET "CodigoSII"='C17', "TipoDiccionario"='', "Orden"=18, "Titulo"='PagadorNombre', "Ayuda"='', "TipoDato"='TEXT', "Largo"=40, "ValorDefecto"='0', "Obligatorio"='S', "Editable"='S', "ModoMasivo"='N', "Exportable"='S', "OcultoEnGrilla"='N', "OcultoEnMantenedor"='N', "Comun"='N', "Clave"='N', "Grupo"='1. Transacciones informadas', "OrdenCargaExterna"=17, "CantidadDecimales"=0
WHERE "Esquema"='DJ1946' AND "Version"='2022' AND "Nombre"='PagadorNombre';

UPDATE BSCL."BSCL.Normativa.BD::DOC.Campo"
SET "CodigoSII"='C13', "TipoDiccionario"='', "Orden"=14, "Titulo"='MonedaPago', "Ayuda"='', "TipoDato"='TEXT', "Largo"=40, "ValorDefecto"='0', "Obligatorio"='S', "Editable"='S', "ModoMasivo"='N', "Exportable"='S', "OcultoEnGrilla"='N', "OcultoEnMantenedor"='N', "Comun"='N', "Clave"='N', "Grupo"='1. Transacciones informadas', "OrdenCargaExterna"=13, "CantidadDecimales"=0
WHERE "Esquema"='DJ1946' AND "Version"='2022' AND "Nombre"='MonedaPago';