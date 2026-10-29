SET SCHEMA "BSCL";  
/*DICCIONARIO*/

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )
SELECT  
'UI_GANANCIA_DE_CAPITAL',
'CL',
'Ganancia de Capital',
'GANCAP'
FROM DUMMY 
WHERE NOT EXISTS (SELECT 1 FROM  BSCL."BSCL.Plataforma.BD::PLA.Diccionario" WHERE  "CodDiccionario"='UI_GANANCIA_DE_CAPITAL');

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )
SELECT  
'UI_INFORME_GANANCIA_DE_CAPITAL',
'CL',
'Informe Ganancia de Capital',
'INFGCA'
FROM DUMMY 
WHERE NOT EXISTS (SELECT 1 FROM  BSCL."BSCL.Plataforma.BD::PLA.Diccionario" WHERE  "CodDiccionario"='UI_INFORME_GANANCIA_DE_CAPITAL');

/*Diccionario*/
INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario"
("CodDiccionario", "CodLenguaje", "Descripcion", "Abreviado")
SELECT 'UI_GANANCIA_CAP', 'CL', 'Ganancia Capital', 'Ganancia Cap.'
FROM DUMMY
WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.Diccionario" WHERE "CodDiccionario" = 'UI_GANANCIA_CAP');

INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario"
("CodDiccionario", "CodLenguaje", "Descripcion", "Abreviado")
SELECT 'UI_MANTENEDOR_IGK', 'CL', 'Regularizacion IGK.', 'Regula IGK'
FROM DUMMY
WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.Diccionario" WHERE "CodDiccionario" = 'UI_MANTENEDOR_IGK');

INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario"
("CodDiccionario", "CodLenguaje", "Descripcion", "Abreviado")
SELECT 'UI_REGULARIZACION_IGK', 'CL', 'Regularizacion IGK', 'Regula IGK'
FROM DUMMY
WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.Diccionario" WHERE "CodDiccionario" = 'UI_REGULARIZACION_IGK');

INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario"
("CodDiccionario", "CodLenguaje", "Descripcion", "Abreviado")
SELECT 'UI_CLIENTE_RESIDENTE', 'CL', 'Residente ', 'Residente'
FROM DUMMY
WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.Diccionario" WHERE "CodDiccionario" = 'UI_CLIENTE_RESIDENTE');

INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario"
("CodDiccionario", "CodLenguaje", "Descripcion", "Abreviado")
SELECT 'UI_NO_AFECTA_IGK', 'CL', 'Factura No afecta a IGK', 'Factura No afecta a IGK'
FROM DUMMY
WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.Diccionario" WHERE "CodDiccionario" = 'UI_NO_AFECTA_IGK');

INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario"
("CodDiccionario", "CodLenguaje", "Descripcion", "Abreviado")
SELECT 'UI_COMPROBANTE_REGULARIZADO', 'CL', 'Comprobante ::IdComprobante:: regularizado', ''
FROM DUMMY
WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.Diccionario" WHERE "CodDiccionario" = 'UI_COMPROBANTE_REGULARIZADO');

INSERT INTO "BSCL.Plataforma.BD::PLA.Diccionario"
("CodDiccionario", "CodLenguaje", "Descripcion", "Abreviado")
SELECT 'UI_VENTA_NOMINAL', 'CL', 'Venta Nominal', 'Vta Nominal'
FROM DUMMY
WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Plataforma.BD::PLA.Diccionario" WHERE "CodDiccionario" = 'UI_VENTA_NOMINAL');

--REPORTE_IGK
INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion", "TablasTempIN", "TablasTempOUT", "RegistraUso")
SELECT 4306005, 'BSCL.Facturacion.CrystalReports.Servicios::ConsultarGananciaCapital', 0, 'P', 'Consulta comprobantes con retencion IGK', '', '', 'S'
FROM DUMMY WHERE NOT EXISTS (SELECT 1 FROM BSCL."BSCL.Plataforma.BD::PLA.Servicio" WHERE "IdServicio"=4306005);

/* Inserta vista en tabla servicios */
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion", "TablasTempIN", "TablasTempOUT", "RegistraUso")
SELECT 4306001, 'BSCL.Facturacion.BD.Vistas::ConsultarTipoRetencion', 0, 'Q', 'Consulta Tipos de retencion para ganancia capital', '', '', 'S'
FROM DUMMY
WHERE NOT EXISTS(SELECT 1 FROM "BSCL.Plataforma.BD::PLA.Servicio" WHERE "IdServicio" = 4306001);

INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion", "TablasTempIN", "TablasTempOUT", "RegistraUso")
SELECT 4306004, 'PLUGIN_FAC.jar@cl.bcs.facturacion.ModificarRetencionIgk', 0, 'J', 'Modifica comprobante facturacion, monto retencion IGK', '', '', 'S'
FROM DUMMY
where not exists(select 1 from "BSCL.Plataforma.BD::PLA.Servicio" where "IdServicio" = 4306004)

/* Inserta vista en tabla detalle factura mantenedor igk servicios */
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion", "TablasTempIN", "TablasTempOUT", "RegistraUso")
SELECT 4306002, 'BSCL.Facturacion.BD.Vistas::ConsultarComprobanteDetalleMantenedorIgk', 0, 'Q', 'Consulta detalle comprobantes de facturacion para mantenedor igk', '', '', 'S'
FROM DUMMY
WHERE NOT EXISTS(SELECT 1 FROM "BSCL.Plataforma.BD::PLA.Servicio" WHERE "IdServicio" = 4306002);

/*Inserta servicio vista de datos de cabecera de mantenedor igk*/
INSERT INTO "BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion", "TablasTempIN", "TablasTempOUT", "RegistraUso")
SELECT 4306003, 'BSCL.Facturacion.BD.Vistas::ConsultarComprobanteMantenedorIgk', 0, 'Q', 'Consulta comprobantes de facturacion para mantenedor igk', '', '', 'S'
FROM DUMMY
WHERE NOT EXISTS(SELECT 1 FROM "BSCL.Plataforma.BD::PLA.Servicio" WHERE "IdServicio" = 4306003);



