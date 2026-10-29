SET SCHEMA "BSCL"; 


/*INFORME IGK*/

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



INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )
SELECT  
'UI_SOLICITAR_INFORMES_POR_PERIODO',
'CL',
'Solicitar informes por periodo',
'INFGCA'
FROM DUMMY 
WHERE NOT EXISTS (SELECT 1 FROM  BSCL."BSCL.Plataforma.BD::PLA.Diccionario" WHERE  "CodDiccionario"='UI_SOLICITAR_INFORMES_POR_PERIODO');



--REPORTE_IGK
INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion", "TablasTempIN", "TablasTempOUT", "RegistraUso")
SELECT 4306005, 'BSCL.Facturacion.CrystalReports.Servicios::ConsultarGananciaCapital', 0, 'P', 'Consulta comprobantes con retencion IGK', '', '', 'S'
FROM DUMMY WHERE NOT EXISTS (SELECT 1 FROM BSCL."BSCL.Plataforma.BD::PLA.Servicio" WHERE "IdServicio"=4306005);

SELECT * FROM BSCL."BSCL.Plataforma.BD::PLA.Servicio" WHERE "IdServicio"=4306005

INSERT INTO BSCL."BSCL.Facturacion.BD::FAC.TemplateFacturacion"
("IdTemplateFacturacion", "CodigoTemplateFacturacion", "NombreReporte", "Descripcion", "IdServicio", "FormatoSalida", "IdEmpresa", "IdTipoComprobante", "IdEstado", "LocalizadorSistema")
SELECT 3, 'CMBIGK', 'REPORTE_IGK', 'Informe comprobantes con retencion IGK', 4306005, 'pdf', 1, '', 'ACT', 'CL'
FROM DUMMY WHERE NOT EXISTS (SELECT 1 FROM BSCL."BSCL.Facturacion.BD::FAC.TemplateFacturacion" WHERE "IdTemplateFacturacion"=3);

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion", "TablasTempIN", "TablasTempOUT", "RegistraUso")
SELECT 4175, 'BSCL.Facturacion.BD.Vistas::ConsultarTemplateFacturacion', 0, 'Q', '', '', '', 'S'
FROM DUMMY WHERE NOT EXISTS (SELECT 1 FROM BSCL."BSCL.Plataforma.BD::PLA.Servicio" WHERE "IdServicio"=4175);



/*Parametros*/


INSERT INTO BSCL."BSCL.Facturacion.BD::FAC.ConfiguracionFacturacion"
("IdEmpresa", "CodigoParametro", "Descripcion", "Valor", "EsModificableUsuario", "Uso", "ValorDefecto", "ValoresPosibles", "ModificablePorUsuario", "ModificablePostProduccion", "CreadorParametro", "FechaCreacion", "ModificadorParametro", "FechaModificacion", "Observaciones")
SELECT 1 AS "IdEmpresa",'INFO_IGK_NOMBREARCHIVO_01'AS "CodigoParametro",'Nombre del archivo PDF en  ZIP masivo de certificados.' AS "Descripcion"
,'[RUT]' AS "Valor"
,'S' AS "EsModificableUsuario"
,'Certificado masivo IGK' AS "Uso"
,'[RUT]'  AS "ValorDefecto"
,'[RUT],[PERIODO],texto libre' AS "ValoresPosibles",'S' AS "ModificablePorUsuario",'' AS  "ModificablePostProduccion", '' AS  "CreadorParametro", CURRENT_UTCTIMESTAMP AS "FechaCreacion"
,'' AS "ModificadorParametro", CURRENT_UTCTIMESTAMP AS "FechaModificacion", '' AS "Observaciones"
FROM DUMMY
WHERE NOT EXISTS (SELECT 1 FROM BSCL."BSCL.Facturacion.BD::FAC.ConfiguracionFacturacion" N WHERE N."CodigoParametro"= 'INFO_IGK_NOMBREARCHIVO_01' );  

INSERT INTO BSCL."BSCL.Facturacion.BD::FAC.ConfiguracionFacturacion"
("IdEmpresa", "CodigoParametro", "Descripcion", "Valor", "EsModificableUsuario", "Uso", "ValorDefecto", "ValoresPosibles", "ModificablePorUsuario", "ModificablePostProduccion", "CreadorParametro", "FechaCreacion", "ModificadorParametro", "FechaModificacion", "Observaciones")
SELECT 1 AS "IdEmpresa",'INFO_IGK_NOMBREARCHIVO_02'AS "CodigoParametro",'Nombre del archivo PDF en  ZIP masivo de certificados.' AS "Descripcion"
,'_' AS "Valor"
,'S' AS "EsModificableUsuario"
,'Certificado masivo IGK' AS "Uso"
,'_'  AS "ValorDefecto"
,'[RUT],[PERIODO],texto libre' AS "ValoresPosibles",'S' AS "ModificablePorUsuario",'' AS  "ModificablePostProduccion", '' AS  "CreadorParametro", CURRENT_UTCTIMESTAMP AS "FechaCreacion"
,'' AS "ModificadorParametro", CURRENT_UTCTIMESTAMP AS "FechaModificacion", '' AS "Observaciones"
FROM DUMMY
WHERE NOT EXISTS (SELECT 1 FROM BSCL."BSCL.Facturacion.BD::FAC.ConfiguracionFacturacion" N WHERE N."CodigoParametro"= 'INFO_IGK_NOMBREARCHIVO_02' );  

INSERT INTO BSCL."BSCL.Facturacion.BD::FAC.ConfiguracionFacturacion"
("IdEmpresa", "CodigoParametro", "Descripcion", "Valor", "EsModificableUsuario", "Uso", "ValorDefecto", "ValoresPosibles", "ModificablePorUsuario", "ModificablePostProduccion", "CreadorParametro", "FechaCreacion", "ModificadorParametro", "FechaModificacion", "Observaciones")
SELECT 1 AS "IdEmpresa",'INFO_IGK_NOMBREARCHIVO_03'AS "CodigoParametro",'Nombre del archivo PDF en  ZIP masivo de certificados.' AS "Descripcion"
,'[PERIODO]' AS "Valor"
,'S' AS "EsModificableUsuario"
,'Certificado masivo IGK' AS "Uso"
,'[PERIODO]'  AS "ValorDefecto"
,'[RUT],[PERIODO],texto libre' AS "ValoresPosibles",'S' AS "ModificablePorUsuario",'' AS  "ModificablePostProduccion", '' AS  "CreadorParametro", CURRENT_UTCTIMESTAMP AS "FechaCreacion"
,'' AS "ModificadorParametro", CURRENT_UTCTIMESTAMP AS "FechaModificacion", '' AS "Observaciones"
FROM DUMMY
WHERE NOT EXISTS (SELECT 1 FROM BSCL."BSCL.Facturacion.BD::FAC.ConfiguracionFacturacion" N WHERE N."CodigoParametro"= 'INFO_IGK_NOMBREARCHIVO_03' );  


INSERT INTO BSCL."BSCL.Facturacion.BD::FAC.ConfiguracionFacturacion"
("IdEmpresa", "CodigoParametro", "Descripcion", "Valor", "EsModificableUsuario", "Uso", "ValorDefecto", "ValoresPosibles", "ModificablePorUsuario", "ModificablePostProduccion", "CreadorParametro", "FechaCreacion", "ModificadorParametro", "FechaModificacion", "Observaciones")
SELECT 1 AS "IdEmpresa",'INFO_IGK_NOMBREARCHIVO_04'AS "CodigoParametro",'Nombre del archivo PDF en  ZIP masivo de certificados.' AS "Descripcion"
,'_INFO_IGK' AS "Valor"
,'S' AS "EsModificableUsuario"
,'Certificado masivo IGK' AS "Uso"
,'_INFO_IGK'  AS "ValorDefecto"
,'[RUT],[PERIODO],texto libre' AS "ValoresPosibles",'S' AS "ModificablePorUsuario",'' AS  "ModificablePostProduccion", '' AS  "CreadorParametro", CURRENT_UTCTIMESTAMP AS "FechaCreacion"
,'' AS "ModificadorParametro", CURRENT_UTCTIMESTAMP AS "FechaModificacion", '' AS "Observaciones"
FROM DUMMY
WHERE NOT EXISTS (SELECT 1 FROM BSCL."BSCL.Facturacion.BD::FAC.ConfiguracionFacturacion" N WHERE N."CodigoParametro"= 'INFO_IGK_NOMBREARCHIVO_04' );  



INSERT INTO BSCL."BSCL.Facturacion.BD::FAC.ConfiguracionFacturacion"
("IdEmpresa", "CodigoParametro", "Descripcion", "Valor", "EsModificableUsuario", "Uso", "ValorDefecto", "ValoresPosibles", "ModificablePorUsuario", "ModificablePostProduccion", "CreadorParametro", "FechaCreacion", "ModificadorParametro", "FechaModificacion", "Observaciones")
SELECT 1 AS "IdEmpresa",'INFO_IGK_NOMBREARCHIVO_05'AS "CodigoParametro",'Nombre del archivo PDF en  ZIP masivo de certificados.' AS "Descripcion"
,'' AS "Valor"
,'S' AS "EsModificableUsuario"
,'Certificado masivo IGK' AS "Uso"
,''  AS "ValorDefecto"
,'[RUT],[PERIODO],texto libre' AS "ValoresPosibles",'S' AS "ModificablePorUsuario",'' AS  "ModificablePostProduccion", '' AS  "CreadorParametro", CURRENT_UTCTIMESTAMP AS "FechaCreacion"
,'' AS "ModificadorParametro", CURRENT_UTCTIMESTAMP AS "FechaModificacion", '' AS "Observaciones"
FROM DUMMY
WHERE NOT EXISTS (SELECT 1 FROM BSCL."BSCL.Facturacion.BD::FAC.ConfiguracionFacturacion" N WHERE N."CodigoParametro"= 'INFO_IGK_NOMBREARCHIVO_05' );  









